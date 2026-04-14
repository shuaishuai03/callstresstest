# Call Stress Test

面向真实 Android 手机的批量外呼测试工具。当前仓库已完成一次实现与文档对齐审计，以下内容只描述仓库内已经存在并已验证的能力。

## 当前版本

- 应用版本：`1.1.0`
- 仓库内可安装包：[`CallStressTest-1.1.0-debug.apk`](CallStressTest-1.1.0-debug.apk)
- 校验文件：[`SHA256SUMS`](SHA256SUMS)
- 主界面截图：![Application Interface](📱%20Screenshots/Application%20Interface.jpg)

仓库内 APK 为调试构建，适合仓库验证、手工测试和功能回归；如果你需要自行签名或调整权限策略，请按“从源码构建”执行。

## 已实现能力

- 批量发起外呼测试，可配置目标号码、测试次数和间隔时间。
- 支持 Android 7.0+（API 24+）的通话状态监听。
- Android 12+ 使用 `TelephonyCallback`，Android 7.0-11 使用 `PhoneStateListener` 回退实现。
- 使用通话记录做辅助判定，区分“已接通”和“未接通”。
- 中国大陆 11 位手机号自动补齐 `+86`。
- 国际号码支持显式国家码输入，例如 `+14155552671` 或 `0049151555123456`。
- 测试过程中遇到来电会暂停队列，来电结束后继续。
- UI 内提供实时进度、成功/失败统计和时间戳日志。

## 运行前提

- 真实 Android 手机，系统版本至少为 Android 7.0 / API 24。
- 可正常拨号的 SIM 卡和蜂窝网络。
- 已授予 `CALL_PHONE`、`READ_PHONE_STATE`、`READ_CALL_LOG`。
- 已知本仓库在 `Android SDK Platform 34`、`Build-Tools 35.0.0`、`JDK 21` 环境下通过本地构建验证。

## 安装方式

### 方式一：安装仓库内 APK

1. 下载或直接取用仓库根目录中的 [`CallStressTest-1.1.0-debug.apk`](CallStressTest-1.1.0-debug.apk)。
2. 对照 [`SHA256SUMS`](SHA256SUMS) 校验文件完整性。
3. 在手机上允许当前安装来源。
4. 安装 APK 并在首次启动时授予所需权限。

### 方式二：从源码构建

1. 确保本机已有 Android SDK。
2. 复制 [`local.properties.example`](local.properties.example) 为 `local.properties`，并将 `sdk.dir` 改为本机 SDK 路径。
3. 执行：

```bash
bash gradlew testDebugUnitTest assembleDebug
```

4. 构建产物位于：

```text
app/build/outputs/apk/debug/app-debug.apk
```

## 快速使用

1. 启动应用并授予全部权限。
2. 输入测试号码。
3. 输入测试次数。
4. 输入测试间隔，最少 `5000` 毫秒。
5. 点击“开始测试”。
6. 观察进度条、统计区和日志区。
7. 测试结束后查看成功率与日志明细。

## 输入规则

### 电话号码

- 中国大陆手机号：`13800138000`，应用会自动转为 `+8613800138000`。
- 已带国家码的国际号码：`+14155552671`。
- 以 `00` 开头的国际号码：`0049151555123456`，应用会自动转为 `+49151555123456`。
- 不接受无国家码的非中国大陆号码。

### 测试次数

- 必须是大于 `0` 的整数。
- 当测试次数超过 `1000` 时，应用会显示额外确认弹窗。

### 测试间隔

- 最小间隔固定为 `5000` 毫秒。
- 低于最小值时不会开始测试。

## 权限说明

| 权限 | 用途 | 是否必需 |
| --- | --- | --- |
| `CALL_PHONE` | 发起外呼 | 是 |
| `READ_PHONE_STATE` | 监听通话状态与来电暂停 | 是 |
| `READ_CALL_LOG` | 读取最近通话记录，辅助判定是否接通 | 是 |

应用不会向仓库外发送数据，也不会把结果上传到任何服务器。

## 结果判定逻辑

- 测试发起后，应用等待通话结束。
- 通话结束后，应用读取最近的匹配外呼记录。
- 若通话记录时长大于 `0` 秒，则计为成功。
- 若通话记录存在但时长为 `0`，则计为失败。
- 若 `30` 秒内仍未结束当前通话，则按失败处理并继续队列。
- 若测试期间出现来电，当前测试队列会暂停，待来电结束后恢复。

## 已知限制

- 必须在具备通话能力的真机上运行；模拟器通常不适用。
- 应用只能发起拨号，不能自动挂断系统电话界面中的通话。
- 结果判定依赖通话记录可用性；如果系统限制了通话记录访问，应用会保守地按失败处理该次测试。
- 仓库内 APK 是调试构建，不包含自定义签名流程。

## 仓库内协作入口

- 贡献说明：[`CONTRIBUTING.md`](CONTRIBUTING.md)
- 使用教程：[`TUTORIAL.md`](TUTORIAL.md)
- 安全策略：[`SECURITY.md`](SECURITY.md)
- 行为准则：[`CODE_OF_CONDUCT.md`](CODE_OF_CONDUCT.md)
- 变更记录：[`CHANGELOG.md`](CHANGELOG.md)
