# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Planned Features
- Support for international phone numbers
- Export test results to CSV/JSON
- Call recording capability (where legally permitted)
- Dark mode support
- Multiple phone number testing
- Scheduled test execution
- Network quality metrics

## [1.0.0] - 2024-11-08

### Added
- Initial release of Call Stress Test
- Automated phone call stress testing functionality
- Real-time call state monitoring (IDLE, RINGING, OFFHOOK)
- Configurable test parameters:
  - Phone number input with automatic formatting
  - Customizable call count (unlimited)
  - Adjustable interval timing (minimum 5 seconds)
- Comprehensive statistics tracking:
  - Total calls counter
  - Successful calls counter
  - Failed calls counter
  - Success rate percentage
- Visual progress indicators:
  - Progress bar
  - Real-time status updates
  - Statistics card
- Detailed logging system:
  - Timestamped log entries
  - Call state change notifications
  - Success/failure indicators
  - Scrollable log view
- Safety features:
  - Minimum 5-second interval enforcement
  - 30-second call timeout detection
  - Confirmation dialogs for large batches (>1000 calls)
  - Stop test functionality
- Permission management:
  - Runtime permission requests
  - Permission status checking
  - User-friendly permission explanations
- Error handling:
  - Input validation
  - Network error detection
  - Graceful failure recovery
- Material Design UI:
  - Clean, intuitive interface
  - CardView statistics display
  - Responsive layout
  - User-friendly controls
- Android telephony integration:
  - TelephonyManager integration
  - TelephonyCallback for Android 12+
  - Call state monitoring
  - Automatic call placement
- Thread management:
  - Handler-based scheduling
  - ExecutorService for background tasks
  - Proper lifecycle management
- Phone number formatting:
  - Support for Chinese mobile numbers (+86)
  - Automatic country code detection
  - Format validation

### Technical Details
- Minimum SDK: API 24 (Android 7.0)
- Target SDK: API 36 (Android 14+)
- Language: Kotlin
- Architecture: Single Activity
- UI Framework: Android Views with Material Design

### Known Issues
- None reported

---

## Version History

### Version Numbering

This project uses [Semantic Versioning](https://semver.org/):
- **MAJOR** version: Incompatible API changes
- **MINOR** version: New functionality (backwards-compatible)
- **PATCH** version: Bug fixes (backwards-compatible)

### Release Types

- **Stable**: Production-ready releases
- **Beta**: Feature-complete but may have bugs
- **Alpha**: Early testing releases

---

## How to Update

### For Users

1. Download the latest APK from [Releases](../../releases)
2. Uninstall the old version (optional, but recommended)
3. Install the new APK
4. Grant permissions when prompted

### For Developers

1. Pull the latest changes:
   ```bash
   git pull origin main
   ```

2. Sync Gradle dependencies:
   ```bash
   ./gradlew clean build
   ```

3. Test on device:
   ```bash
   ./gradlew assembleDebug
   adb install app/build/outputs/apk/debug/app-debug.apk
   ```

---

## Migration Guides

### Migrating to 1.0.0

This is the initial release, no migration needed.

---

## Deprecation Notices

None at this time.

---

## Security Updates

### Reporting Security Issues

If you discover a security vulnerability, please email: security@example.com

Do NOT create a public issue for security vulnerabilities.

---

## Contributors

Thank you to all contributors who have helped improve this project!

- Initial development and release

---

## Links

- [GitHub Repository](https://github.com/yourusername/CallStressTest)
- [Issue Tracker](https://github.com/yourusername/CallStressTest/issues)
- [Releases](https://github.com/yourusername/CallStressTest/releases)
- [Documentation](README.md)

---

[Unreleased]: https://github.com/yourusername/CallStressTest/compare/v1.0.0...HEAD
[1.0.0]: https://github.com/yourusername/CallStressTest/releases/tag/v1.0.0
