@file:Suppress("DEPRECATION")

package com.example.callstresstest

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.CallLog
import android.telephony.PhoneStateListener
import android.telephony.TelephonyCallback
import android.telephony.TelephonyManager
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.atomic.AtomicInteger

class MainActivity : AppCompatActivity() {

    companion object {
        private const val PERMISSION_REQUEST_CODE = 100
        private const val MIN_CALL_INTERVAL = 5000L
        private const val CALL_TIMEOUT = 30000L
        private const val CALL_LOG_LOOKBACK_BUFFER_MS = 3000L
    }

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
    private lateinit var tvVersion: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var scrollLog: ScrollView
    private lateinit var tvLog: TextView

    private val completedCalls = AtomicInteger(0)
    private val failedCalls = AtomicInteger(0)
    private val handler = Handler(Looper.getMainLooper())

    private var totalCalls = 0
    private var currentCallNumber = 0
    private var isTestRunning = false
    private var isCallActive = false
    private var isPausedForIncomingCall = false
    private var lastCallState = TelephonyManager.CALL_STATE_IDLE
    private var telephonyManager: TelephonyManager? = null
    private var callStateCallback: MyTelephonyCallback? = null
    private var phoneStateListener: LegacyPhoneStateListener? = null
    private var callTimeoutRunnable: Runnable? = null
    private var waitingForNextCall = false
    private var currentTestPhoneNumber = ""
    private var currentIntervalMs = MIN_CALL_INTERVAL
    private var currentDialedNumber: String? = null
    private var currentAttemptStartedAt = 0L
    private val nextCallRunnable = Runnable {
        waitingForNextCall = false
        if (isTestRunning && !isPausedForIncomingCall) {
            makeNextCall(currentTestPhoneNumber)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initializeViews()
        setupListeners()
        telephonyManager = getSystemService(TELEPHONY_SERVICE) as TelephonyManager
        checkPermissions()
    }

    override fun onResume() {
        super.onResume()
        if (hasPermission(Manifest.permission.READ_PHONE_STATE)) {
            setupCallStateListener()
        }
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
        tvVersion = findViewById(R.id.tvVersion)
        progressBar = findViewById(R.id.progressBar)
        scrollLog = findViewById(R.id.scrollLog)
        tvLog = findViewById(R.id.tvLog)

        btnStopTest.isEnabled = false
        tvVersion.text = getString(R.string.screen_version, readVersionName())
        updateStats()
        updateCurrentStatus(getString(R.string.status_ready))
    }

    private fun setupListeners() {
        btnStartTest.setOnClickListener { validateAndStartTest() }
        btnStopTest.setOnClickListener { stopStressTest() }
        btnClearLog.setOnClickListener { clearLog() }
    }

    private fun setupCallStateListener() {
        if (!hasPermission(Manifest.permission.READ_PHONE_STATE)) {
            logMessage(getString(R.string.log_listener_permission_missing), true)
            return
        }

        teardownCallStateListener()

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                callStateCallback = MyTelephonyCallback()
                telephonyManager?.registerTelephonyCallback(
                    mainExecutor,
                    callStateCallback!!
                )
            } else {
                @Suppress("DEPRECATION")
                LegacyPhoneStateListener().also {
                    phoneStateListener = it
                    telephonyManager?.listen(it, PhoneStateListener.LISTEN_CALL_STATE)
                }
            }
        } catch (e: SecurityException) {
            logMessage(getString(R.string.log_listener_permission_missing), true)
        }
    }

    private inner class MyTelephonyCallback : TelephonyCallback(),
        TelephonyCallback.CallStateListener {
        override fun onCallStateChanged(state: Int) {
            handleCallStateChange(state)
        }
    }

    private inner class LegacyPhoneStateListener : PhoneStateListener() {
        @Deprecated("Deprecated in Java")
        override fun onCallStateChanged(state: Int, phoneNumber: String?) {
            super.onCallStateChanged(state, phoneNumber)
            handleCallStateChange(state)
        }
    }

    private fun handleCallStateChange(state: Int) {
        if (state == lastCallState) {
            return
        }

        logMessage(
            getString(
                R.string.log_call_state_change,
                getStateString(lastCallState),
                getStateString(state)
            ),
            false
        )

        when (state) {
            TelephonyManager.CALL_STATE_IDLE -> handleIdleState()
            TelephonyManager.CALL_STATE_OFFHOOK -> {
                if (isTestRunning && isCallActive) {
                    updateCurrentStatus(getString(R.string.status_waiting_for_result))
                }
            }
            TelephonyManager.CALL_STATE_RINGING -> handleIncomingCallDetected()
        }

        lastCallState = state
    }

    private fun handleIdleState() {
        callTimeoutRunnable?.let { handler.removeCallbacks(it) }

        if (!isTestRunning) {
            return
        }

        if (isCallActive && !waitingForNextCall) {
            val pausedByIncomingCall = isPausedForIncomingCall
            isPausedForIncomingCall = false
            finalizeCurrentCall(pausedByIncomingCall)
            return
        }

        if (isPausedForIncomingCall) {
            isPausedForIncomingCall = false
            logMessage(getString(R.string.log_incoming_cleared), true)
            updateCurrentStatus(getString(R.string.status_resuming))
            if (currentCallNumber < totalCalls) {
                scheduleNextCall()
            }
        }
    }

    private fun handleIncomingCallDetected() {
        if (!isTestRunning) {
            return
        }

        isPausedForIncomingCall = true
        waitingForNextCall = false
        handler.removeCallbacks(nextCallRunnable)
        callTimeoutRunnable?.let { handler.removeCallbacks(it) }
        logMessage(getString(R.string.log_incoming_detected), true)
        updateCurrentStatus(getString(R.string.status_paused_for_incoming))
    }

    private fun getStateString(state: Int): String {
        return when (state) {
            TelephonyManager.CALL_STATE_IDLE -> getString(R.string.call_state_idle)
            TelephonyManager.CALL_STATE_OFFHOOK -> getString(R.string.call_state_offhook)
            TelephonyManager.CALL_STATE_RINGING -> getString(R.string.call_state_ringing)
            else -> getString(R.string.call_state_unknown)
        }
    }

    private fun scheduleNextCall() {
        waitingForNextCall = true
        callTimeoutRunnable?.let { handler.removeCallbacks(it) }
        handler.removeCallbacks(nextCallRunnable)
        logMessage(
            getString(R.string.log_wait_next_call, currentIntervalMs / 1000),
            false
        )
        handler.postDelayed(nextCallRunnable, currentIntervalMs)
    }

    private fun checkPermissions() {
        val permissions = arrayOf(
            Manifest.permission.CALL_PHONE,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.READ_CALL_LOG
        )

        if (!hasAllRequiredPermissions()) {
            ActivityCompat.requestPermissions(this, permissions, PERMISSION_REQUEST_CODE)
        } else {
            logMessage(getString(R.string.log_permissions_granted), false)
            setupCallStateListener()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (!grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                showDialog(
                    getString(R.string.dialog_title_permission_missing),
                    getString(R.string.dialog_message_permission_missing)
                )
            } else {
                logMessage(getString(R.string.log_permissions_granted), false)
                setupCallStateListener()
            }
        }
    }

    private fun validateAndStartTest() {
        if (!hasAllRequiredPermissions()) {
            checkPermissions()
            showDialog(
                getString(R.string.dialog_title_permission_missing),
                getString(R.string.dialog_message_permission_missing)
            )
            return
        }

        val phoneNumber = etPhoneNumber.text.toString().trim()
        val callCountStr = etCallCount.text.toString().trim()
        val intervalStr = etCallInterval.text.toString().trim()

        if (phoneNumber.isEmpty()) {
            showDialog(
                getString(R.string.dialog_title_input_error),
                getString(R.string.dialog_message_phone_required)
            )
            return
        }

        val formattedNumber = CallNumberUtils.formatForDialing(phoneNumber)
        if (formattedNumber == null) {
            showDialog(
                getString(R.string.dialog_title_number_error),
                getString(R.string.dialog_message_number_error)
            )
            return
        }

        if (callCountStr.isEmpty()) {
            showDialog(
                getString(R.string.dialog_title_input_error),
                getString(R.string.dialog_message_count_required)
            )
            return
        }

        val callCount = try {
            callCountStr.toInt()
        } catch (e: NumberFormatException) {
            showDialog(
                getString(R.string.dialog_title_input_error),
                getString(R.string.dialog_message_count_numeric)
            )
            return
        }

        if (callCount < 1) {
            showDialog(
                getString(R.string.dialog_title_value_error),
                getString(R.string.dialog_message_count_positive)
            )
            return
        }

        if (callCount > 1000) {
            AlertDialog.Builder(this)
                .setTitle(R.string.dialog_title_large_batch)
                .setMessage(getString(R.string.dialog_message_large_batch, callCount))
                .setPositiveButton(R.string.action_continue) { _, _ ->
                    proceedWithValidation(formattedNumber, callCount, intervalStr)
                }
                .setNegativeButton(R.string.action_cancel, null)
                .show()
        } else {
            proceedWithValidation(formattedNumber, callCount, intervalStr)
        }
    }

    private fun proceedWithValidation(formattedNumber: String, callCount: Int, intervalStr: String) {
        val interval = if (intervalStr.isEmpty()) {
            MIN_CALL_INTERVAL
        } else {
            try {
                intervalStr.toLong()
            } catch (e: NumberFormatException) {
                showDialog(
                    getString(R.string.dialog_title_input_error),
                    getString(R.string.dialog_message_interval_numeric)
                )
                return
            }
        }

        if (interval < MIN_CALL_INTERVAL) {
            showDialog(
                getString(R.string.dialog_title_interval_too_short),
                getString(R.string.dialog_message_interval_too_short, MIN_CALL_INTERVAL / 1000)
            )
            return
        }

        val estimatedMinutes = (callCount * interval / 1000 / 60).toInt()
        AlertDialog.Builder(this)
            .setTitle(R.string.dialog_title_confirm_start)
            .setMessage(
                getString(
                    R.string.dialog_message_confirm_start,
                    formattedNumber,
                    callCount,
                    interval / 1000,
                    estimatedMinutes
                )
            )
            .setPositiveButton(R.string.action_start) { _, _ ->
                startStressTest(formattedNumber, callCount, interval)
            }
            .setNegativeButton(R.string.action_cancel, null)
            .show()
    }

    private fun startStressTest(phoneNumber: String, callCount: Int, interval: Long) {
        isTestRunning = true
        completedCalls.set(0)
        failedCalls.set(0)
        totalCalls = callCount
        currentCallNumber = 0
        waitingForNextCall = false
        isPausedForIncomingCall = false
        lastCallState = TelephonyManager.CALL_STATE_IDLE
        currentTestPhoneNumber = phoneNumber
        currentIntervalMs = interval
        currentDialedNumber = null
        currentAttemptStartedAt = 0L
        handler.removeCallbacks(nextCallRunnable)
        callTimeoutRunnable?.let { handler.removeCallbacks(it) }

        btnStartTest.isEnabled = false
        btnStopTest.isEnabled = true
        etPhoneNumber.isEnabled = false
        etCallCount.isEnabled = false
        etCallInterval.isEnabled = false

        progressBar.max = callCount
        progressBar.progress = 0

        updateCurrentStatus(getString(R.string.status_preparing))
        updateStats()

        logMessage(getString(R.string.log_divider), false)
        logMessage(getString(R.string.log_test_started), true)
        logMessage(getString(R.string.log_test_number, phoneNumber), false)
        logMessage(getString(R.string.log_test_count, callCount), false)
        logMessage(getString(R.string.log_test_interval, interval / 1000), false)
        logMessage(getString(R.string.log_divider), false)

        handler.postDelayed({
            makeNextCall(currentTestPhoneNumber)
        }, 1000)
    }

    private fun makeNextCall(phoneNumber: String) {
        if (!isTestRunning || waitingForNextCall || isPausedForIncomingCall) {
            return
        }

        currentCallNumber++

        if (currentCallNumber > totalCalls) {
            completeTest()
            return
        }

        updateCurrentStatus(getString(R.string.status_dialing, currentCallNumber, totalCalls))
        logMessage(getString(R.string.log_minor_divider), false)
        logMessage(getString(R.string.log_call_attempt, currentCallNumber), true)

        try {
            val callIntent = Intent(Intent.ACTION_CALL).apply {
                data = Uri.parse("tel:$phoneNumber")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }

            if (hasPermission(Manifest.permission.CALL_PHONE)) {
                currentDialedNumber = phoneNumber
                currentAttemptStartedAt = System.currentTimeMillis()
                startActivity(callIntent)
                isCallActive = true
                updateCurrentStatus(getString(R.string.status_waiting_for_result))
                logMessage(getString(R.string.log_call_intent_sent), false)
                logMessage(getString(R.string.log_call_waiting_result), false)

                callTimeoutRunnable = Runnable {
                    if (isCallActive && isTestRunning && !waitingForNextCall) {
                        isCallActive = false
                        currentDialedNumber = null
                        currentAttemptStartedAt = 0L
                        failedCalls.incrementAndGet()
                        logMessage(
                            getString(R.string.log_call_timeout, CALL_TIMEOUT / 1000),
                            true
                        )
                        updateProgress()
                        finishOrScheduleNextCall()
                    }
                }

                handler.postDelayed(callTimeoutRunnable!!, CALL_TIMEOUT)
            } else {
                failedCalls.incrementAndGet()
                logMessage(getString(R.string.log_call_permission_denied), true)
                updateProgress()
                finishOrScheduleNextCall()
            }
        } catch (e: Exception) {
            failedCalls.incrementAndGet()
            currentDialedNumber = null
            currentAttemptStartedAt = 0L
            logMessage(
                getString(R.string.log_call_failed, e.message ?: "unknown error"),
                true
            )
            updateProgress()
            finishOrScheduleNextCall()
        }
    }

    private fun finalizeCurrentCall(pausedByIncomingCall: Boolean) {
        val dialedNumber = currentDialedNumber
        currentDialedNumber = null
        isCallActive = false

        val verification = if (dialedNumber != null) {
            verifyLatestOutgoingCall(dialedNumber, currentAttemptStartedAt)
        } else {
            null
        }
        currentAttemptStartedAt = 0L

        when {
            verification?.durationSeconds?.let { it > 0 } == true -> {
                completedCalls.incrementAndGet()
                logMessage(
                    getString(R.string.log_call_verified_success, verification.durationSeconds),
                    true
                )
            }
            verification != null -> {
                failedCalls.incrementAndGet()
                logMessage(getString(R.string.log_call_verified_failed), true)
            }
            else -> {
                failedCalls.incrementAndGet()
                if (hasPermission(Manifest.permission.READ_CALL_LOG)) {
                    logMessage(getString(R.string.log_call_verification_unavailable), true)
                }
            }
        }

        if (pausedByIncomingCall) {
            logMessage(getString(R.string.log_incoming_cleared), false)
        }

        updateProgress()
        finishOrScheduleNextCall()
    }

    private fun finishOrScheduleNextCall() {
        if (!isTestRunning) {
            return
        }

        if (currentCallNumber >= totalCalls) {
            completeTest()
        } else if (!isPausedForIncomingCall) {
            scheduleNextCall()
        }
    }

    private fun stopStressTest() {
        AlertDialog.Builder(this)
            .setTitle(R.string.dialog_title_confirm_stop)
            .setMessage(R.string.dialog_message_confirm_stop)
            .setPositiveButton(R.string.action_stop) { _, _ ->
                isTestRunning = false
                isCallActive = false
                isPausedForIncomingCall = false
                waitingForNextCall = false
                callTimeoutRunnable?.let { handler.removeCallbacks(it) }
                handler.removeCallbacks(nextCallRunnable)
                currentDialedNumber = null
                currentAttemptStartedAt = 0L
                resetUI()
                logMessage(getString(R.string.log_manual_stop), true)
                updateCurrentStatus(getString(R.string.status_stopped))
            }
            .setNegativeButton(R.string.action_cancel, null)
            .show()
    }

    private fun completeTest() {
        isTestRunning = false
        isCallActive = false
        isPausedForIncomingCall = false
        waitingForNextCall = false
        callTimeoutRunnable?.let { handler.removeCallbacks(it) }
        handler.removeCallbacks(nextCallRunnable)
        resetUI()

        val successRate = if (totalCalls > 0) {
            (completedCalls.get().toFloat() / totalCalls * 100).toInt()
        } else 0

        logMessage(getString(R.string.log_divider), false)
        logMessage(getString(R.string.log_test_completed), true)
        logMessage(getString(R.string.log_result_total, totalCalls), false)
        logMessage(getString(R.string.log_result_success, completedCalls.get()), false)
        logMessage(getString(R.string.log_result_failed, failedCalls.get()), false)
        logMessage(getString(R.string.log_result_rate, successRate), false)
        logMessage(getString(R.string.log_divider), false)

        updateCurrentStatus(getString(R.string.status_completed, successRate))

        showDialog(
            getString(R.string.dialog_title_test_complete),
            getString(
                R.string.dialog_message_test_complete,
                totalCalls,
                completedCalls.get(),
                failedCalls.get(),
                successRate
            )
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
        tvProgress.text = getString(R.string.progress_text, progressBar.progress, totalCalls)
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
        logMessage(getString(R.string.log_cleared), false)
    }

    private fun showDialog(title: String, message: String) {
        AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(R.string.action_confirm, null)
            .show()
    }

    private fun hasAllRequiredPermissions(): Boolean {
        return hasPermission(Manifest.permission.CALL_PHONE) &&
            hasPermission(Manifest.permission.READ_PHONE_STATE) &&
            hasPermission(Manifest.permission.READ_CALL_LOG)
    }

    private fun readVersionName(): String {
        return try {
            val packageInfo = packageManager.getPackageInfo(packageName, 0)
            packageInfo.versionName ?: "unknown"
        } catch (_: Exception) {
            "unknown"
        }
    }

    private fun hasPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
    }

    private fun verifyLatestOutgoingCall(
        targetNumber: String,
        startedAt: Long
    ): CallVerification? {
        if (!hasPermission(Manifest.permission.READ_CALL_LOG) || startedAt == 0L) {
            return null
        }

        val projection = arrayOf(
            CallLog.Calls.NUMBER,
            CallLog.Calls.DATE,
            CallLog.Calls.DURATION,
            CallLog.Calls.TYPE
        )
        val selection = "${CallLog.Calls.DATE} >= ? AND ${CallLog.Calls.TYPE} = ?"
        val selectionArgs = arrayOf(
            (startedAt - CALL_LOG_LOOKBACK_BUFFER_MS).toString(),
            CallLog.Calls.OUTGOING_TYPE.toString()
        )

        return try {
            contentResolver.query(
                CallLog.Calls.CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                "${CallLog.Calls.DATE} DESC"
            )?.use { cursor ->
                val numberIndex = cursor.getColumnIndexOrThrow(CallLog.Calls.NUMBER)
                val durationIndex = cursor.getColumnIndexOrThrow(CallLog.Calls.DURATION)

                while (cursor.moveToNext()) {
                    val loggedNumber = cursor.getString(numberIndex)
                    if (CallNumberUtils.numbersMatch(loggedNumber, targetNumber)) {
                        return CallVerification(
                            durationSeconds = cursor.getLong(durationIndex)
                        )
                    }
                }
                null
            }
        } catch (e: SecurityException) {
            logMessage(getString(R.string.log_call_log_unavailable, e.message ?: "security exception"), true)
            null
        } catch (e: Exception) {
            logMessage(getString(R.string.log_call_log_unavailable, e.message ?: "unknown error"), true)
            null
        }
    }

    private fun teardownCallStateListener() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                callStateCallback?.let { telephonyManager?.unregisterTelephonyCallback(it) }
                callStateCallback = null
            } else {
                @Suppress("DEPRECATION")
                phoneStateListener?.let {
                    telephonyManager?.listen(it, PhoneStateListener.LISTEN_NONE)
                }
                phoneStateListener = null
            }
        } catch (_: Exception) {
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(nextCallRunnable)
        callTimeoutRunnable?.let { handler.removeCallbacks(it) }
        teardownCallStateListener()
    }

    private data class CallVerification(
        val durationSeconds: Long
    )
}
