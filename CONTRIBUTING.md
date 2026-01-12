# Contributing to Call Stress Test

Thank you for your interest in contributing to Call Stress Test! This document provides guidelines and instructions for contributing to the project.

## Table of Contents

- [Code of Conduct](#code-of-conduct)
- [Getting Started](#getting-started)
- [How to Contribute](#how-to-contribute)
- [Development Setup](#development-setup)
- [Coding Standards](#coding-standards)
- [Testing Guidelines](#testing-guidelines)
- [Commit Message Guidelines](#commit-message-guidelines)
- [Pull Request Process](#pull-request-process)

## Code of Conduct

### Our Pledge

We are committed to providing a welcoming and inclusive environment for all contributors. We expect all participants to:

- Be respectful and considerate
- Accept constructive criticism gracefully
- Focus on what is best for the community
- Show empathy towards other community members

### Unacceptable Behavior

- Harassment, discrimination, or offensive comments
- Trolling or insulting/derogatory comments
- Publishing others' private information
- Any conduct that could be considered inappropriate in a professional setting

## Getting Started

### Prerequisites

Before contributing, ensure you have:

- Android Studio Arctic Fox (2020.3.1) or newer
- JDK 11 or higher
- Git installed and configured
- A GitHub account
- An Android device for testing (API 24+)

### Fork and Clone

1. Fork the repository on GitHub
2. Clone your fork locally:
   ```bash
   git clone https://github.com/YOUR_USERNAME/CallStressTest.git
   cd CallStressTest
   ```
3. Add the upstream repository:
   ```bash
   git remote add upstream https://github.com/ORIGINAL_OWNER/CallStressTest.git
   ```

## How to Contribute

### Reporting Bugs

Before creating a bug report:
1. Check the [existing issues](../../issues) to avoid duplicates
2. Verify the bug exists in the latest version
3. Test on a physical Android device

When creating a bug report, include:
- **Clear title**: Descriptive summary of the issue
- **Description**: Detailed explanation of the problem
- **Steps to reproduce**: Numbered list of steps
- **Expected behavior**: What should happen
- **Actual behavior**: What actually happens
- **Environment**:
  - Device model and manufacturer
  - Android version
  - App version
- **Screenshots/Logs**: If applicable
- **Additional context**: Any other relevant information

**Example Bug Report:**
```markdown
## Bug: App crashes when making 100+ calls

### Description
The app crashes after approximately 100 calls during stress testing.

### Steps to Reproduce
1. Open the app
2. Enter phone number: +8613800138000
3. Set call count: 150
4. Set interval: 5000ms
5. Start test
6. App crashes around call #100

### Expected Behavior
App should complete all 150 calls without crashing.

### Actual Behavior
App crashes with "OutOfMemoryError" after ~100 calls.

### Environment
- Device: Samsung Galaxy S21
- Android: 13
- App Version: 1.0

### Logs
```
E/AndroidRuntime: FATAL EXCEPTION: main
    java.lang.OutOfMemoryError: Failed to allocate...
```
```

### Suggesting Features

When suggesting a new feature:
1. Check if it's already been suggested
2. Explain the use case and benefits
3. Consider implementation complexity
4. Discuss potential drawbacks

**Feature Request Template:**
```markdown
## Feature Request: [Feature Name]

### Problem Statement
Describe the problem this feature would solve.

### Proposed Solution
Explain your proposed solution in detail.

### Alternatives Considered
List any alternative solutions you've considered.

### Benefits
- Benefit 1
- Benefit 2

### Implementation Notes
Any technical considerations or suggestions.
```

### Improving Documentation

Documentation improvements are always welcome:
- Fix typos or grammatical errors
- Clarify confusing sections
- Add missing information
- Improve code examples
- Translate documentation

## Development Setup

### Initial Setup

1. **Open Project in Android Studio**
   ```bash
   # Open Android Studio
   # File → Open → Select CallStressTest directory
   ```

2. **Sync Gradle**
   - Wait for Gradle sync to complete
   - Resolve any dependency issues

3. **Configure Device**
   - Enable Developer Options
   - Enable USB Debugging
   - Connect via USB

4. **Run the App**
   ```bash
   ./gradlew assembleDebug
   adb install app/build/outputs/apk/debug/app-debug.apk
   ```

### Project Structure

```
CallStressTest/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/example/callstresstest/
│   │   │   │   ├── MainActivity.kt          # Main activity
│   │   │   │   └── ui/theme/                # Theme files
│   │   │   ├── res/
│   │   │   │   ├── layout/                  # XML layouts
│   │   │   │   ├── values/                  # Strings, colors, themes
│   │   │   │   └── drawable/                # Icons and graphics
│   │   │   └── AndroidManifest.xml          # App manifest
│   │   ├── androidTest/                     # Instrumented tests
│   │   └── test/                            # Unit tests
│   └── build.gradle                         # App-level Gradle
├── gradle/                                  # Gradle wrapper
├── build.gradle                             # Project-level Gradle
├── settings.gradle                          # Gradle settings
├── README.md                                # Main documentation
├── CONTRIBUTING.md                          # This file
└── LICENSE                                  # MIT License
```

## Coding Standards

### Kotlin Style Guide

Follow the [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html):

#### Naming Conventions

```kotlin
// Classes: PascalCase
class MainActivity : AppCompatActivity()

// Functions: camelCase
fun makeNextCall(phoneNumber: String)

// Variables: camelCase
val completedCalls = AtomicInteger(0)
var isTestRunning = false

// Constants: UPPER_SNAKE_CASE
companion object {
    private const val MIN_CALL_INTERVAL = 5000L
}

// Resources: snake_case
R.id.btn_start_test
R.layout.activity_main
```

#### Code Formatting

```kotlin
// Use 4 spaces for indentation (no tabs)
class MainActivity : AppCompatActivity() {

    // Blank line after class declaration
    private lateinit var etPhoneNumber: EditText

    // Group related variables
    private var isTestRunning = false
    private var isCallActive = false

    // Blank line before functions
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Blank lines between logical sections
        initializeViews()
        setupListeners()
    }
}
```

#### Comments

```kotlin
// Single-line comments for brief explanations
val interval = 5000L // 5 seconds

/**
 * Multi-line comments for functions and classes
 *
 * @param phoneNumber The phone number to call
 * @param interval Time between calls in milliseconds
 */
fun startStressTest(phoneNumber: String, interval: Long) {
    // Implementation
}

// TODO: Add support for international numbers
// FIXME: Handle edge case when SIM card is removed
```

### Android Best Practices

1. **Lifecycle Awareness**
   ```kotlin
   override fun onDestroy() {
       super.onDestroy()
       // Clean up resources
       executorService?.shutdownNow()
       handler.removeCallbacksAndMessages(null)
   }
   ```

2. **Permission Handling**
   ```kotlin
   if (ContextCompat.checkSelfPermission(this, permission)
       != PackageManager.PERMISSION_GRANTED) {
       ActivityCompat.requestPermissions(this, permissions, REQUEST_CODE)
   }
   ```

3. **Thread Safety**
   ```kotlin
   // Use AtomicInteger for thread-safe counters
   private val completedCalls = AtomicInteger(0)

   // Use Handler for UI updates
   handler.post {
       tvLog.append(message)
   }
   ```

4. **Resource Management**
   ```kotlin
   // Use lateinit for views
   private lateinit var btnStartTest: Button

   // Initialize in onCreate
   override fun onCreate(savedInstanceState: Bundle?) {
       super.onCreate(savedInstanceState)
       btnStartTest = findViewById(R.id.btnStartTest)
   }
   ```

## Testing Guidelines

### Unit Tests

Write unit tests for business logic:

```kotlin
@Test
fun testPhoneNumberFormatting() {
    val formatted = formatPhoneNumber("13800138000")
    assertEquals("+8613800138000", formatted)
}

@Test
fun testCallCountValidation() {
    val result = validateCallCount("10")
    assertTrue(result.isValid)
}
```

### Instrumented Tests

Test Android components:

```kotlin
@Test
fun testPermissionRequest() {
    val scenario = ActivityScenario.launch(MainActivity::class.java)
    scenario.onActivity { activity ->
        // Test permission handling
    }
}
```

### Manual Testing Checklist

Before submitting a PR, test:
- [ ] App launches successfully
- [ ] All permissions are requested
- [ ] Phone number validation works
- [ ] Calls are made successfully
- [ ] Progress updates correctly
- [ ] Stop button works
- [ ] Logs are accurate
- [ ] App handles errors gracefully
- [ ] No memory leaks
- [ ] Works on different Android versions

## Commit Message Guidelines

### Format

```
<type>(<scope>): <subject>

<body>

<footer>
```

### Types

- **feat**: New feature
- **fix**: Bug fix
- **docs**: Documentation changes
- **style**: Code style changes (formatting, no logic change)
- **refactor**: Code refactoring
- **test**: Adding or updating tests
- **chore**: Maintenance tasks

### Examples

```
feat(calling): Add support for international numbers

- Add country code detection
- Support multiple phone number formats
- Update validation logic

Closes #123
```

```
fix(ui): Fix progress bar not updating

The progress bar was not updating due to incorrect
thread handling. Now using Handler.post() for UI updates.

Fixes #456
```

```
docs(readme): Update installation instructions

- Add screenshots
- Clarify permission requirements
- Fix typos
```

### Best Practices

- Use present tense ("Add feature" not "Added feature")
- Keep subject line under 50 characters
- Capitalize subject line
- No period at the end of subject
- Separate subject from body with blank line
- Wrap body at 72 characters
- Explain what and why, not how

## Pull Request Process

### Before Submitting

1. **Update your fork**
   ```bash
   git fetch upstream
   git checkout main
   git merge upstream/main
   ```

2. **Create a feature branch**
   ```bash
   git checkout -b feature/your-feature-name
   ```

3. **Make your changes**
   - Write clean, documented code
   - Follow coding standards
   - Add tests if applicable

4. **Test thoroughly**
   - Run on physical device
   - Test on multiple Android versions
   - Verify no regressions

5. **Commit your changes**
   ```bash
   git add .
   git commit -m "feat: your feature description"
   ```

6. **Push to your fork**
   ```bash
   git push origin feature/your-feature-name
   ```

### Creating the Pull Request

1. Go to your fork on GitHub
2. Click "New Pull Request"
3. Select your feature branch
4. Fill in the PR template:

```markdown
## Description
Brief description of changes

## Type of Change
- [ ] Bug fix
- [ ] New feature
- [ ] Documentation update
- [ ] Code refactoring

## Testing
- [ ] Tested on physical device
- [ ] Added unit tests
- [ ] Updated documentation

## Screenshots
(If applicable)

## Checklist
- [ ] Code follows style guidelines
- [ ] Self-review completed
- [ ] Comments added for complex code
- [ ] Documentation updated
- [ ] No new warnings
- [ ] Tests pass
```

### Review Process

1. **Automated Checks**: CI/CD will run tests
2. **Code Review**: Maintainers will review your code
3. **Feedback**: Address any requested changes
4. **Approval**: Once approved, your PR will be merged

### After Merge

1. **Delete your branch**
   ```bash
   git branch -d feature/your-feature-name
   git push origin --delete feature/your-feature-name
   ```

2. **Update your fork**
   ```bash
   git checkout main
   git pull upstream main
   git push origin main
   ```

## Questions?

If you have questions:
- Check existing [Issues](../../issues) and [Discussions](../../discussions)
- Create a new discussion for general questions
- Create an issue for specific problems

## Recognition

Contributors will be recognized in:
- README.md acknowledgments section
- Release notes
- GitHub contributors page

Thank you for contributing to Call Stress Test! 🎉
