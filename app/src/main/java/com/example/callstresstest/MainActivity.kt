package com.example.callstresstest

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.telephony.TelephonyCallback
import android.telephony.TelephonyManager
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger

class MainActivity : AppCompatActivity() {

    companion object {
        private const val PERMISSION_REQUEST_CODE = 100
        private const val MIN_CALL_INTERVAL = 5000L // 最小5秒间隔
        private const val CALL_TIMEOUT = 30000L // 30秒超时
    }

    // UI Components
    private lateinit var etPhoneNumber: EditText
    private lateinit var etCallCount: EditText
    private lateinit var etCallInterval: EditText
    private lateinit var btnStartTest: Button
    private lateinit var btnStopTest: Button
    private lateinit var btnClearLog: Button
    private lateinit var tvProgress: TextView
    private lateinit var tvTotalCalls: TextView
    private lateinit var tvSuccessCalls: TextView
    private lateinit var tvFailedCalls: TextView
    private lateinit var tvCurrentStatus: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var scrollLog: ScrollView
    private lateinit var tvLog: TextView
    private lateinit var cardStats: CardView

    // Test Variables
    private val completedCalls = AtomicInteger(0)
    private val failedCalls = AtomicInteger(0)
    private var totalCalls = 0
    private var currentCallNumber = 0
    private var isTestRunning = false
    private var isCallActive = false
    private var lastCallState = TelephonyManager.CALL_STATE_IDLE
    private var callStartTime = 0L
    private val handler = Handler(Looper.getMainLooper())
    private var executorService: ExecutorService? = null
    private var telephonyManager: TelephonyManager? = null
    private var callStateCallback: MyTelephonyCallback? = null
    private var callTimeoutRunnable: Runnable? = null
    private var waitingForNextCall = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initializeViews()
        setupListeners()
        checkPermissions()

        telephonyManager = getSystemService(TELEPHONY_SERVICE) as TelephonyManager
        setupCallStateListener()
    }

    private fun initializeViews() {
        etPhoneNumber = findViewById(R.id.etPhoneNumber)
        etCallCount = findViewById(R.id.etCallCount)
        etCallInterval = findViewById(R.id.etCallInterval)
        btnStartTest = findViewById(R.id.btnStartTest)
        btnStopTest = findViewById(R.id.btnStopTest)
        btnClearLog = findViewById(R.id.btnClearLog)
        tvProgress = findViewById(R.id.tvProgress)
        tvTotalCalls = findViewById(R.id.tvTotalCalls)
        tvSuccessCalls = findViewById(R.id.tvSuccessCalls)
        tvFailedCalls = findViewById(R.id.tvFailedCalls)
        tvCurrentStatus = findViewById(R.id.tvCurrentStatus)
        progressBar = findViewById(R.id.progressBar)
        scrollLog = findViewById(R.id.scrollLog)
        tvLog = findViewById(R.id.tvLog)
        cardStats = findViewById(R.id.cardStats)

        btnStopTest.isEnabled = false
        updateStats()
    }

    private fun setupListeners() {
        btnStartTest.setOnClickListener { validateAndStartTest() }
        btnStopTest.setOnClickListener { stopStressTest() }
        btnClearLog.setOnClickListener { clearLog() }
    }

    private fun setupCallStateListener() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            callStateCallback = MyTelephonyCallback()
            try {
                telephonyManager?.registerTelephonyCallback(
                    mainExecutor,
                    callStateCallback!!
                )
            } catch (e: SecurityException) {
                logMessage("⚠️ 无法注册通话状态监听器：权限不足", true)
            }
        }
    }

    private inner class MyTelephonyCallback : TelephonyCallback(),
        TelephonyCallback.CallStateListener {
        override fun onCallStateChanged(state: Int) {
            handleCallStateChange(state)
        }
    }

    private fun handleCallStateChange(state: Int) {
        logMessage("📡 通话状态变化: ${getStateString(lastCallState)} -> ${getStateString(state)}", false)

        when (state) {
            TelephonyManager.CALL_STATE_IDLE -> {
                // 通话空闲状态
                if (isTestRunning && !waitingForNextCall) {
                    if (lastCallState == TelephonyManager.CALL_STATE_OFFHOOK) {
                        // 从通话中结束 -> 接通了电话
                        val callDuration = (System.currentTimeMillis() - callStartTime) / 1000
                        logMessage("✅ 通话已接通并结束（通话时长：${callDuration}秒）", true)
                        completedCalls.incrementAndGet()
                        isCallActive = false
                        updateProgress()
                        scheduleNextCall()
                    } else if (lastCallState == TelephonyManager.CALL_STATE_RINGING) {
                        // 从响铃结束但未接通 -> 未接通（可能无人接听）
                        logMessage("❌ 通话未接通（无人接听或被拒接）", true)
                        failedCalls.incrementAndGet()
                        isCallActive = false
                        updateProgress()
                        scheduleNextCall()
                    } else if (isCallActive) {
                        // 其他情况下的通话结束
                        logMessage("⚠️ 通话异常结束", true)
                        failedCalls.incrementAndGet()
                        isCallActive = false
                        updateProgress()
                        scheduleNextCall()
                    }
                }
                callTimeoutRunnable?.let { handler.removeCallbacks(it) }
            }

            TelephonyManager.CALL_STATE_OFFHOOK -> {
                // 通话进行中（已接通）
                if (isTestRunning) {
                    isCallActive = true
                    callStartTime = System.currentTimeMillis()
                    callTimeoutRunnable?.let { handler.removeCallbacks(it) }
                    logMessage("📞 通话已接通", false)
                }
            }

            TelephonyManager.CALL_STATE_RINGING -> {
                // 响铃中
                if (isTestRunning) {
                    logMessage("🔔 正在呼叫，等待接听...", false)
                }
            }
        }

        lastCallState = state
    }

    private fun getStateString(state: Int): String {
        return when (state) {
            TelephonyManager.CALL_STATE_IDLE -> "IDLE(空闲)"
            TelephonyManager.CALL_STATE_OFFHOOK -> "OFFHOOK(通话中)"
            TelephonyManager.CALL_STATE_RINGING -> "RINGING(响铃)"
            else -> "UNKNOWN(未知)"
        }
    }

    private fun scheduleNextCall() {
        waitingForNextCall = true
        callTimeoutRunnable?.let { handler.removeCallbacks(it) }

        val interval = try {
            etCallInterval.text.toString().toLongOrNull() ?: MIN_CALL_INTERVAL
        } catch (e: Exception) {
            MIN_CALL_INTERVAL
        }

        logMessage("⏳ 等待 ${interval / 1000} 秒后进行下一次拨号...", false)

        handler.postDelayed({
            waitingForNextCall = false
            if (isTestRunning) {
                continueNextCall()
            }
        }, interval)
    }

    private fun checkPermissions() {
        val permissions = arrayOf(
            Manifest.permission.CALL_PHONE,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.READ_CALL_LOG
        )

        val allGranted = permissions.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }

        if (!allGranted) {
            ActivityCompat.requestPermissions(this, permissions, PERMISSION_REQUEST_CODE)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == PERMISSION_REQUEST_CODE) {
            val allGranted = grantResults.all { it == PackageManager.PERMISSION_GRANTED }

            if (!allGranted) {
                showDialog("权限不足", "应用需要以下权限才能运行：\n• 拨打电话\n• 读取通话状态\n• 读取通话记录\n\n请在设置中授予权限。")
            } else {
                logMessage("✓ 所有权限已授予", false)
            }
        }
    }

    private fun validateAndStartTest() {
        val phoneNumber = etPhoneNumber.text.toString().trim()
        val callCountStr = etCallCount.text.toString().trim()
        val intervalStr = etCallInterval.text.toString().trim()

        // 验证电话号码
        if (phoneNumber.isEmpty()) {
            showDialog("输入错误", "请输入测试电话号码")
            return
        }

        val formattedNumber = formatPhoneNumber(phoneNumber)
        if (formattedNumber.length < 11) {
            showDialog("号码错误", "请输入有效的11位手机号码")
            return
        }

        // 验证通话次数（移除上限限制）
        if (callCountStr.isEmpty()) {
            showDialog("输入错误", "请输入测试次数")
            return
        }

        val callCount = try {
            callCountStr.toInt()
        } catch (e: NumberFormatException) {
            showDialog("输入错误", "测试次数必须是数字")
            return
        }

        if (callCount < 1) {
            showDialog("数值错误", "测试次数必须大于 0")
            return
        }

        if (callCount > 1000) {
            AlertDialog.Builder(this)
                .setTitle("⚠️ 警告")
                .setMessage("测试次数较大（${callCount}次），可能需要较长时间。\n\n是否继续？")
                .setPositiveButton("继续") { _, _ ->
                    proceedWithValidation(formattedNumber, callCount, intervalStr)
                }
                .setNegativeButton("取消", null)
                .show()
        } else {
            proceedWithValidation(formattedNumber, callCount, intervalStr)
        }
    }

    private fun proceedWithValidation(formattedNumber: String, callCount: Int, intervalStr: String) {
        // 验证间隔时间
        val interval = if (intervalStr.isEmpty()) {
            MIN_CALL_INTERVAL
        } else {
            try {
                intervalStr.toLong()
            } catch (e: NumberFormatException) {
                showDialog("输入错误", "间隔时间必须是数字")
                return
            }
        }

        if (interval < MIN_CALL_INTERVAL) {
            showDialog("间隔过短", "为保护手机，间隔时间不能少于 ${MIN_CALL_INTERVAL / 1000} 秒")
            return
        }

        // 确认开始测试
        val estimatedMinutes = (callCount * interval / 1000 / 60).toInt()
        val message = """
            即将开始压力测试：
            
            📱 测试号码：$formattedNumber
            🔢 测试次数：$callCount 次
            ⏱️ 间隔时间：${interval / 1000} 秒
            
            预计耗时：约 $estimatedMinutes 分钟
            
            请确认是否开始？
        """.trimIndent()

        AlertDialog.Builder(this)
            .setTitle("确认开始测试")
            .setMessage(message)
            .setPositiveButton("开始") { _, _ ->
                startStressTest(formattedNumber, callCount, interval)
            }
            .setNegativeButton("取消", null)
            .show()
    }

    private fun formatPhoneNumber(phoneNumber: String): String {
        var cleaned = phoneNumber.replace(Regex("[\\s-()]"), "")

        if (cleaned.startsWith("+86")) {
            return cleaned
        }

        if (cleaned.startsWith("86") && cleaned.length > 11) {
            return "+$cleaned"
        }

        if (cleaned.startsWith("1") && cleaned.length == 11) {
            return "+86$cleaned"
        }

        if (cleaned.length == 11) {
            return "+86$cleaned"
        }

        return cleaned
    }

    private fun startStressTest(phoneNumber: String, callCount: Int, interval: Long) {
        isTestRunning = true
        completedCalls.set(0)
        failedCalls.set(0)
        totalCalls = callCount
        currentCallNumber = 0
        waitingForNextCall = false
        lastCallState = TelephonyManager.CALL_STATE_IDLE

        btnStartTest.isEnabled = false
        btnStopTest.isEnabled = true
        etPhoneNumber.isEnabled = false
        etCallCount.isEnabled = false
        etCallInterval.isEnabled = false

        progressBar.max = callCount
        progressBar.progress = 0

        updateCurrentStatus("准备开始测试...")
        updateStats()

        logMessage("═════════════════════════════════", false)
        logMessage("🚀 压力测试开始", true)
        logMessage("测试号码：$phoneNumber", false)
        logMessage("总次数：$callCount 次", false)
        logMessage("间隔：${interval / 1000} 秒", false)
        logMessage("═════════════════════════════════", false)

        executorService = Executors.newSingleThreadExecutor()

        handler.postDelayed({
            makeNextCall(phoneNumber, interval)
        }, 1000)
    }

    private fun makeNextCall(phoneNumber: String, interval: Long) {
        if (!isTestRunning || waitingForNextCall) return

        currentCallNumber++

        if (currentCallNumber > totalCalls) {
            completeTest()
            return
        }

        updateCurrentStatus("正在拨打第 $currentCallNumber/$totalCalls 次电话...")
        logMessage("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━", false)
        logMessage("📞 第 $currentCallNumber 次拨号", true)

        try {
            val callIntent = Intent(Intent.ACTION_CALL).apply {
                data = Uri.parse("tel:$phoneNumber")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE)
                == PackageManager.PERMISSION_GRANTED) {

                startActivity(callIntent)
                isCallActive = true

                // 设置超时检测
                callTimeoutRunnable = Runnable {
                    if (isCallActive && isTestRunning && !waitingForNextCall) {
                        isCallActive = false
                        failedCalls.incrementAndGet()
                        logMessage("⏱️ 通话超时（${CALL_TIMEOUT / 1000}秒无响应）", true)
                        updateProgress()
                        scheduleNextCall()
                    }
                }

                handler.postDelayed(callTimeoutRunnable!!, CALL_TIMEOUT)

            } else {
                failedCalls.incrementAndGet()
                logMessage("❌ 权限被拒绝", true)
                updateProgress()
                scheduleNextCall()
            }

        } catch (e: Exception) {
            failedCalls.incrementAndGet()
            logMessage("❌ 拨号失败：${e.message}", true)
            updateProgress()
            scheduleNextCall()
        }
    }

    private fun continueNextCall() {
        val phoneNumber = formatPhoneNumber(etPhoneNumber.text.toString().trim())
        val interval = try {
            etCallInterval.text.toString().toLongOrNull() ?: MIN_CALL_INTERVAL
        } catch (e: Exception) {
            MIN_CALL_INTERVAL
        }

        makeNextCall(phoneNumber, interval)
    }

    private fun stopStressTest() {
        AlertDialog.Builder(this)
            .setTitle("确认停止")
            .setMessage("确定要停止当前测试吗？")
            .setPositiveButton("停止") { _, _ ->
                isTestRunning = false
                isCallActive = false
                waitingForNextCall = false
                callTimeoutRunnable?.let { handler.removeCallbacks(it) }
                executorService?.shutdownNow()
                resetUI()
                logMessage("⏹️ 测试已手动停止", true)
                updateCurrentStatus("测试已停止")
            }
            .setNegativeButton("取消", null)
            .show()
    }

    private fun completeTest() {
        isTestRunning = false
        isCallActive = false
        waitingForNextCall = false
        callTimeoutRunnable?.let { handler.removeCallbacks(it) }
        resetUI()

        val successRate = if (totalCalls > 0) {
            (completedCalls.get().toFloat() / totalCalls * 100).toInt()
        } else 0

        logMessage("═════════════════════════════════", false)
        logMessage("✅ 测试完成！", true)
        logMessage("总计：$totalCalls 次", false)
        logMessage("成功：${completedCalls.get()} 次", false)
        logMessage("失败：${failedCalls.get()} 次", false)
        logMessage("成功率：$successRate%", false)
        logMessage("═════════════════════════════════", false)

        updateCurrentStatus("测试完成 - 成功率 $successRate%")

        showDialog(
            "测试完成",
            """
            压力测试已完成！
            
            📊 测试结果：
            • 总计：$totalCalls 次
            • 成功：${completedCalls.get()} 次
            • 失败：${failedCalls.get()} 次
            • 成功率：$successRate%
            """.trimIndent()
        )
    }

    private fun resetUI() {
        btnStartTest.isEnabled = true
        btnStopTest.isEnabled = false
        etPhoneNumber.isEnabled = true
        etCallCount.isEnabled = true
        etCallInterval.isEnabled = true
    }

    private fun updateProgress() {
        val total = completedCalls.get() + failedCalls.get()
        progressBar.progress = total
        updateStats()
    }

    private fun updateStats() {
        tvTotalCalls.text = totalCalls.toString()
        tvSuccessCalls.text = completedCalls.get().toString()
        tvFailedCalls.text = failedCalls.get().toString()
        tvProgress.text = "进度：${progressBar.progress}/$totalCalls"
    }

    private fun updateCurrentStatus(status: String) {
        tvCurrentStatus.text = status
    }

    private fun logMessage(message: String, isImportant: Boolean) {
        val timeStamp = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        val prefix = if (isImportant) "[$timeStamp] " else "[$timeStamp] "
        val fullMessage = prefix + message + "\n"

        handler.post {
            tvLog.append(fullMessage)
            scrollLog.post { scrollLog.fullScroll(View.FOCUS_DOWN) }
        }
    }

    private fun clearLog() {
        tvLog.text = ""
        logMessage("日志已清空", false)
    }

    private fun showDialog(title: String, message: String) {
        AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("确定", null)
            .show()
    }

    override fun onDestroy() {
        super.onDestroy()
        executorService?.shutdownNow()
        callTimeoutRunnable?.let { handler.removeCallbacks(it) }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            callStateCallback?.let {
                telephonyManager?.unregisterTelephonyCallback(it)
            }
        }
    }
}
