# Call Stress Test - Complete Usage Tutorial

This comprehensive tutorial will guide you through every aspect of using the Call Stress Test application, from installation to advanced usage scenarios.

## Table of Contents

1. [Quick Start Guide](#quick-start-guide)
2. [Detailed Installation](#detailed-installation)
3. [First-Time Setup](#first-time-setup)
4. [Basic Usage](#basic-usage)
5. [Advanced Features](#advanced-features)
6. [Use Cases](#use-cases)
7. [Best Practices](#best-practices)
8. [Troubleshooting](#troubleshooting)
9. [FAQ](#faq)

---

## Quick Start Guide

**5-Minute Setup:**

1. Install the APK on your Android device
2. Grant phone permissions
3. Enter a phone number
4. Set call count and interval
5. Tap "Start Test"

---

## Detailed Installation

### Prerequisites

Before installing, ensure you have:

✅ **Android Device Requirements:**
- Physical Android device (not emulator)
- Android 7.0 (API 24) or higher
- Active SIM card with calling capability
- Sufficient storage space (< 10 MB)

✅ **Network Requirements:**
- Active cellular network connection
- Sufficient account balance for calls
- No call blocking features enabled

### Installation Steps

#### Method 1: Direct APK Installation

1. **Download the APK**
   - Visit the [Releases page](../../releases)
   - Download the latest `CallStressTest-v1.0.0.apk`
   - Save to your device's Downloads folder

2. **Enable Unknown Sources**
   - Open **Settings** on your device
   - Navigate to **Security** or **Privacy**
   - Find **Install unknown apps** or **Unknown sources**
   - Select your browser or file manager
   - Toggle **Allow from this source** to ON

3. **Install the APK**
   - Open your file manager
   - Navigate to Downloads folder
   - Tap on `CallStressTest-v1.0.0.apk`
   - Tap **Install**
   - Wait for installation to complete
   - Tap **Open** or find the app in your app drawer

#### Method 2: ADB Installation (For Developers)

1. **Enable Developer Options**
   ```
   Settings → About Phone → Tap "Build Number" 7 times
   ```

2. **Enable USB Debugging**
   ```
   Settings → Developer Options → USB Debugging → ON
   ```

3. **Connect Device and Install**
   ```bash
   # Connect device via USB
   adb devices

   # Install APK
   adb install CallStressTest-v1.0.0.apk

   # Launch app
   adb shell am start -n com.example.callstresstest/.MainActivity
   ```

---

## First-Time Setup

### Step 1: Launch the App

1. Open your app drawer
2. Find **Call Stress Test** icon
3. Tap to launch

### Step 2: Grant Permissions

The app will request three critical permissions:

#### Permission 1: Phone (Make and manage calls)
- **Why needed**: To automatically place phone calls
- **What it does**: Allows the app to dial numbers
- **Action**: Tap **Allow**

#### Permission 2: Phone State (Read phone state)
- **Why needed**: To monitor call status (ringing, connected, ended)
- **What it does**: Detects when calls connect or fail
- **Action**: Tap **Allow**

#### Permission 3: Call Log (Read call log)
- **Why needed**: To verify call history
- **What it does**: Accesses call records for validation
- **Action**: Tap **Allow**

**Important**: All three permissions are required for the app to function. If you deny any permission, the app will show a warning dialog.

### Step 3: Verify Setup

After granting permissions, you should see:
- ✅ Main screen with input fields
- ✅ "Start Test" button enabled
- ✅ Empty log window
- ✅ Statistics showing 0/0/0

---

## Basic Usage

### Understanding the Interface

#### Input Section (Top)

1. **Phone Number Field**
   - Label: "测试电话号码" (Test Phone Number)
   - Purpose: Enter the number to call
   - Format: Accepts various formats
   - Example: `13800138000`, `+8613800138000`

2. **Call Count Field**
   - Label: "测试次数" (Test Count)
   - Purpose: Number of calls to make
   - Range: 1 to unlimited
   - Example: `10` for 10 calls

3. **Call Interval Field**
   - Label: "间隔时间(毫秒)" (Interval in milliseconds)
   - Purpose: Time between calls
   - Minimum: 5000 (5 seconds)
   - Example: `10000` for 10 seconds

#### Control Buttons

- **Start Test** (开始测试): Begin the stress test
- **Stop Test** (停止测试): Halt the test early
- **Clear Log** (清空日志): Clear the log window

#### Statistics Card (Middle)

- **Total Calls** (总计): Total number of calls configured
- **Successful** (成功): Calls that connected successfully
- **Failed** (失败): Calls that failed or timed out
- **Progress** (进度): Current progress (e.g., 5/10)

#### Status Display

- **Current Status**: Real-time status message
- **Progress Bar**: Visual progress indicator

#### Log Window (Bottom)

- Scrollable log with timestamps
- Shows all events and state changes
- Auto-scrolls to latest entry

### Making Your First Test Call

#### Step-by-Step Example

Let's make 5 test calls with 10-second intervals:

1. **Enter Phone Number**
   ```
   Field: 测试电话号码
   Input: 13800138000
   ```

2. **Set Call Count**
   ```
   Field: 测试次数
   Input: 5
   ```

3. **Set Interval**
   ```
   Field: 间隔时间(毫秒)
   Input: 10000
   ```

4. **Review Configuration**
   - Phone: +8613800138000 (auto-formatted)
   - Count: 5 calls
   - Interval: 10 seconds
   - Estimated time: ~1 minute

5. **Start Test**
   - Tap **Start Test** button
   - Review confirmation dialog
   - Tap **Start** to confirm

6. **Monitor Progress**
   - Watch the progress bar fill
   - Check statistics update
   - Read log messages
   - Observe call state changes

7. **Wait for Completion**
   - Test runs automatically
   - Each call is made with 10-second intervals
   - Statistics update in real-time
   - Completion dialog appears when done

8. **Review Results**
   ```
   Total: 5 calls
   Successful: 4 calls
   Failed: 1 call
   Success Rate: 80%
   ```

### Understanding Call States

The app monitors three call states:

#### 1. IDLE (空闲)
- **Meaning**: No active call
- **When**: Before dialing, after call ends
- **Log**: "通话状态变化: IDLE(空闲)"

#### 2. RINGING (响铃)
- **Meaning**: Call is ringing, waiting for answer
- **When**: After dialing, before connection
- **Log**: "🔔 正在呼叫，等待接听..."

#### 3. OFFHOOK (通话中)
- **Meaning**: Call is connected and active
- **When**: After call is answered
- **Log**: "📞 通话已接通"

### Call Outcomes

#### Successful Call
```
[10:30:15] 📞 第 1 次拨号
[10:30:18] 🔔 正在呼叫，等待接听...
[10:30:22] 📞 通话已接通
[10:30:35] ✅ 通话已接通并结束（通话时长：13秒）
```

#### Failed Call (No Answer)
```
[10:30:45] 📞 第 2 次拨号
[10:30:48] 🔔 正在呼叫，等待接听...
[10:31:05] ❌ 通话未接通（无人接听或被拒接）
```

#### Failed Call (Timeout)
```
[10:31:15] 📞 第 3 次拨号
[10:31:18] 🔔 正在呼叫，等待接听...
[10:31:48] ⏱️ 通话超时（30秒无响应）
```

---

## Advanced Features

### Phone Number Formatting

The app automatically formats phone numbers:

#### Supported Formats

| Input Format | Auto-Formatted Output |
|--------------|----------------------|
| `13800138000` | `+8613800138000` |
| `+8613800138000` | `+8613800138000` |
| `86 138 0013 8000` | `+8613800138000` |
| `138-0013-8000` | `+8613800138000` |

#### International Numbers

Currently optimized for Chinese mobile numbers (+86). Support for other countries is planned.

### Large Batch Testing

For tests with >1000 calls:

1. **Warning Dialog**
   - App shows warning for large batches
   - Displays estimated time
   - Requires explicit confirmation

2. **Example: 2000 Calls**
   ```
   Phone: +8613800138000
   Count: 2000
   Interval: 10000ms (10 seconds)
   Estimated Time: ~333 minutes (5.5 hours)
   ```

3. **Recommendations**
   - Test during off-peak hours
   - Ensure device is charged
   - Monitor for carrier restrictions
   - Use longer intervals (15-30 seconds)

### Stopping Tests Early

To stop a test before completion:

1. Tap **Stop Test** button
2. Confirm in dialog
3. Review partial results
4. Statistics show completed calls only

### Log Management

#### Viewing Logs
- Scroll through log window
- Timestamps show exact timing
- Icons indicate event types:
  - 📞 Call initiated
  - 🔔 Ringing
  - ✅ Success
  - ❌ Failure
  - ⏱️ Timeout
  - 📡 State change

#### Clearing Logs
- Tap **Clear Log** button
- Logs are cleared immediately
- Statistics are preserved
- New entry confirms clearing

---

## Use Cases

### Use Case 1: Network Quality Testing

**Scenario**: Test cellular network stability

**Configuration**:
```
Phone: Your own number
Count: 50
Interval: 15000ms (15 seconds)
```

**Process**:
1. Start test during peak hours
2. Monitor success rate
3. Note any patterns in failures
4. Compare with off-peak results

**Expected Results**:
- Peak hours: 70-85% success rate
- Off-peak: 90-95% success rate

### Use Case 2: Device Stress Testing

**Scenario**: Test device performance under load

**Configuration**:
```
Phone: Test number
Count: 100
Interval: 5000ms (5 seconds)
```

**Process**:
1. Monitor device temperature
2. Check battery drain
3. Observe app stability
4. Note any crashes or freezes

**Metrics to Track**:
- Memory usage
- CPU usage
- Battery consumption
- Call success rate

### Use Case 3: App Development Testing

**Scenario**: Test telephony integration in your app

**Configuration**:
```
Phone: Development test number
Count: 20
Interval: 10000ms (10 seconds)
```

**Process**:
1. Run alongside your app
2. Monitor for conflicts
3. Check permission handling
4. Verify call state detection

### Use Case 4: Carrier Limit Testing

**Scenario**: Identify carrier rate limits

**Configuration**:
```
Phone: Test number
Count: 200
Interval: 5000ms (5 seconds)
```

**Process**:
1. Start test
2. Monitor for sudden failures
3. Note when failures begin
4. Identify rate limit threshold

**Common Limits**:
- Some carriers: 50-100 calls/hour
- Others: 200-300 calls/hour

---

## Best Practices

### Safety Guidelines

1. **Always Get Permission**
   - Only call numbers you own
   - Get explicit consent for other numbers
   - Respect privacy and regulations

2. **Use Reasonable Intervals**
   - Minimum: 5 seconds (enforced)
   - Recommended: 10-15 seconds
   - For large batches: 20-30 seconds

3. **Monitor Your Account**
   - Check for carrier warnings
   - Monitor call charges
   - Watch for service restrictions

4. **Test Responsibly**
   - Avoid peak hours for large tests
   - Don't exceed carrier limits
   - Stop if issues occur

### Optimization Tips

1. **Battery Management**
   - Keep device charged
   - Disable battery optimization for app
   - Use power-saving mode cautiously

2. **Network Optimization**
   - Use strong signal areas
   - Avoid network congestion
   - Test on different networks

3. **Result Accuracy**
   - Use consistent test conditions
   - Repeat tests for reliability
   - Document environmental factors

### Testing Strategies

1. **Incremental Testing**
   ```
   Test 1: 10 calls, 10s interval
   Test 2: 50 calls, 10s interval
   Test 3: 100 calls, 15s interval
   ```

2. **Comparative Testing**
   - Test different times of day
   - Test different locations
   - Test different networks

3. **Baseline Establishment**
   - Run initial test with known-good conditions
   - Use as reference for future tests
   - Document baseline metrics

---

## Troubleshooting

### Common Issues and Solutions

#### Issue 1: Permissions Denied

**Symptoms**:
- App shows permission error
- Calls don't start
- "Permission denied" in logs

**Solutions**:
1. Go to Settings → Apps → Call Stress Test → Permissions
2. Enable all required permissions
3. Restart the app
4. Try test again

#### Issue 2: All Calls Failing

**Symptoms**:
- 0% success rate
- All calls marked as failed
- No ringing detected

**Possible Causes**:
- Invalid phone number
- No network connection
- SIM card issue
- Call blocking enabled

**Solutions**:
1. Verify phone number format
2. Check network signal strength
3. Ensure SIM card is active
4. Disable call blocking features
5. Try a different number

#### Issue 3: App Crashes

**Symptoms**:
- App closes unexpectedly
- "App has stopped" message
- Crashes during test

**Solutions**:
1. Clear app cache:
   ```
   Settings → Apps → Call Stress Test → Storage → Clear Cache
   ```
2. Restart device
3. Reinstall app
4. Check Android version compatibility
5. Report bug with logs

#### Issue 4: Calls Timeout

**Symptoms**:
- Many timeout errors
- 30-second timeout messages
- Low success rate

**Possible Causes**:
- Poor network quality
- Number not answering
- Network congestion

**Solutions**:
1. Move to better signal area
2. Test with different number
3. Increase interval time
4. Test during off-peak hours

#### Issue 5: Progress Not Updating

**Symptoms**:
- Statistics don't change
- Progress bar stuck
- Logs not updating

**Solutions**:
1. Wait for current call to complete
2. Check if test is actually running
3. Stop and restart test
4. Restart app

---

## FAQ

### General Questions

**Q: Is this app legal to use?**
A: Yes, when used responsibly for testing purposes with your own numbers or with explicit permission. Always comply with local telecommunications laws.

**Q: Does this app work on emulators?**
A: No, it requires a physical Android device with telephony capabilities and an active SIM card.

**Q: Can I use this for automated calling services?**
A: This app is designed for testing purposes only, not for production calling services or telemarketing.

**Q: Does the app store my call data?**
A: No, the app only displays real-time logs and statistics. No data is stored permanently or sent to external servers.

### Technical Questions

**Q: What's the maximum number of calls I can make?**
A: There's no hard limit in the app, but carrier limits typically range from 50-300 calls per hour.

**Q: Why is there a 5-second minimum interval?**
A: To protect your device and prevent carrier rate limiting or account suspension.

**Q: Can I run tests in the background?**
A: The app works best in the foreground. Background operation may be limited by Android's battery optimization.

**Q: Does this work with VoIP numbers?**
A: It works with any number that can be dialed through the standard Android dialer, including VoIP numbers.

### Usage Questions

**Q: How do I test international numbers?**
A: Enter the full international format (e.g., +1234567890). Current version is optimized for Chinese numbers but supports others.

**Q: Can I test multiple numbers simultaneously?**
A: Not in the current version. This feature is planned for future releases.

**Q: What happens if I receive a call during testing?**
A: The test will pause. You can answer the incoming call, then resume or restart the test.

**Q: Can I export test results?**
A: Not in the current version. Export functionality is planned for future releases.

### Troubleshooting Questions

**Q: Why do some calls show as failed even though they connected?**
A: This may occur if the call state isn't detected properly. Ensure all permissions are granted and try restarting the app.

**Q: The app says "Permission denied" but I granted all permissions. What should I do?**
A: Go to Settings → Apps → Call Stress Test → Permissions and verify all permissions are enabled. If issues persist, reinstall the app.

**Q: Can I use this app on Android 6.0 or lower?**
A: No, the minimum supported version is Android 7.0 (API 24).

---

## Additional Resources

### Documentation
- [README.md](README.md) - Main documentation
- [CONTRIBUTING.md](CONTRIBUTING.md) - Contribution guidelines
- [CHANGELOG.md](CHANGELOG.md) - Version history

### Support
- [GitHub Issues](../../issues) - Report bugs
- [GitHub Discussions](../../discussions) - Ask questions
- Email: support@example.com

### Community
- Share your use cases
- Contribute improvements
- Report bugs and suggestions

---

## Conclusion

You now have a complete understanding of how to use Call Stress Test effectively. Remember to:

✅ Use responsibly and legally
✅ Test with appropriate intervals
✅ Monitor your carrier account
✅ Report bugs and suggestions
✅ Contribute to the project

Happy testing! 🚀
