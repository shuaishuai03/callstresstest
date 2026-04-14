# 贡献指南

本文件描述当前仓库真实可执行的开发与提交流程。

## 开发环境

- JDK：建议 `17+`，仓库已在 `JDK 21` 环境下验证。
- Android SDK：`Platform 34`。
- Android Build-Tools：`35.0.0` 或更高兼容版本。
- Gradle Wrapper：仓库自带 `gradlew`。
- 设备：如果需要手工验证拨号逻辑，请准备支持通话的 Android 真机。

## 首次准备

1. 克隆仓库。
2. 复制 [`local.properties.example`](local.properties.example) 为 `local.properties`。
3. 将 `sdk.dir` 改为本机 Android SDK 路径。
4. 执行：

```bash
bash gradlew testDebugUnitTest assembleDebug
```

如果这一步无法通过，不要继续提交功能改动。

## 本地验证基线

### 必跑命令

```bash
bash gradlew testDebugUnitTest assembleDebug
```

### 手工验证建议

- 首次启动时权限申请是否正确。
- 中国大陆号码是否自动补齐 `+86`。
- 带国家码的国际号码是否能通过格式校验。
- 间隔小于 `5000` 时是否拒绝启动。
- 单次测试完成后是否能通过通话记录确认成功/失败。
- 来电中断时是否暂停并在来电结束后继续。

## Issue 与 PR

仓库已提供以下模板：

- bug 报告：`.github/ISSUE_TEMPLATE/bug_report.md`
- 功能请求：`.github/ISSUE_TEMPLATE/feature_request.md`
- PR 模板：`.github/PULL_REQUEST_TEMPLATE.md`

提交前请先使用对应模板整理信息，尤其要写清楚复现步骤、设备环境与验证结果。

## 代码提交要求

- 保持改动与任务直接相关，不顺手引入无关重构。
- 优先修复事实错误，再补文档。
- 新增用户可见行为时，同步更新 `README.md`、`TUTORIAL.md` 和 `CHANGELOG.md`。
- 如果新增或删除权限，必须同时更新 `AndroidManifest.xml` 与文档的权限说明。
- 若修改 APK 资产或版本号，必须同步更新仓库根目录 APK 文件名和 `SHA256SUMS`。

## 提交信息建议

推荐使用以下前缀：

- `feat:` 新功能
- `fix:` 缺陷修复
- `docs:` 文档更新
- `build:` 构建或依赖调整
- `ci:` CI 配置变更
- `test:` 测试补充

示例：

```text
fix: add legacy call state listener fallback
docs: rewrite repository documentation after audit
ci: add android build workflow
```

## 文档要求

- 只写仓库里真实存在、能验证的内容。
- 不要写占位用户名、占位邮箱、占位发布页。
- 不要把 roadmap 写成已实现功能。
- 如果某项能力依赖系统行为或权限前提，必须显式说明限制。

## 安全问题

请先阅读 [`SECURITY.md`](SECURITY.md)。不要把高风险安全细节直接写进公开缺陷描述。
