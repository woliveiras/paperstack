---
name: debug
description: >
  Debugging workflow for the Android (Kotlin + Jetpack Compose) app. Use when tracking down
  runtime errors, unexpected behavior, network issues, or state problems.
  Trigger phrases: "debug", "não está funcionando", "erro em runtime",
  "por que isso falha", "breakpoint", "/debug".
---

# Debugging — Android / Jetpack Compose

## Tools available

| Tool | What it debugs |
|------|---------------|
| Logcat (`adb logcat`) | Runtime logs, crashes, ANRs |
| Android Studio Debugger | Breakpoints, step-through, evaluate |
| Layout Inspector | Compose tree, recomposition counts |
| Network Profiler | OkHttp requests/responses |
| `./gradlew test --info` | Unit test failures with stack traces |

## Step-by-step workflow

### 1. Identify the layer

| Symptom | Likely layer | Start here |
|---------|-------------|------------|
| Compilation error | Kotlin / Gradle | `./gradlew compileDebugKotlin 2>&1 \| grep "^e:"` |
| Test failure | Unit logic | `./gradlew test --tests "ClassName" --info` |
| Crash on startup | Hilt / Activity | Logcat for `FATAL EXCEPTION` |
| Wrong data shown | Repository / API | `ArxivApiServiceImpl.kt`, repository layer |
| State not updating | ViewModel | StateFlow emissions, check `viewModelScope` |
| Navigation broken | NavHost | `MainActivity.kt` routes |
| PDF not opening | Intent / FileProvider | `DetailViewModel.kt` download logic |
| DataStore lost | Persistence | `SettingsDataStore.kt` |

### 2. Check Logcat first

```sh
adb logcat -s "paperstack" "*:E"   # errors only
adb logcat | grep -E "FATAL|Exception|Error"
```

Or filter by package:
```sh
adb logcat --pid=$(adb shell pidof com.paperstack)
```

### 3. Inspect network (arXiv API)

Add temporary logging in `ArxivApiServiceImpl.kt`:

```kotlin
Log.d("ArxivApi", "request: $url")
Log.d("ArxivApi", "response code: ${response.code}")
Log.d("ArxivApi", "body: ${response.body?.string()}")  // remove after debug
```

**Never commit Log.d calls** — remove before PR.

### 4. Inspect ViewModel state

Add temporary logging in the ViewModel:

```kotlin
Log.d("FeedVM", "state: ${_state.value}")
```

Or use the debugger: set a breakpoint in the ViewModel and inspect `_state.value`.

### 5. Isolate with a unit test

If the bug is in a service or repository, write a failing test:

```sh
./gradlew test --tests "com.paperstack.data.remote.ArxivApiServiceImplTest" --info
```

This is often faster than running the full app.

### 6. XML parsing issues

If the arXiv response parses incorrectly:

1. Log the raw XML (step 3)
2. Create a test fixture in the test class
3. Run `parseResponse()` against it with MockWebServer
4. Adjust `XmlPullParser` logic — do NOT switch to regex parsing

### 7. Hilt / DI issues

| Issue | Fix |
|-------|-----|
| `UninitializedPropertyAccessException` | Missing `@Inject` or `@HiltViewModel` |
| `MissingBinding` | Check `@Module` / `@Provides` in `di/` |
| `Kotlin metadata unsupported` | Clean build: `./gradlew clean assembleDebug` |
| Hilt compilation error | Verify KSP + Hilt versions are compatible |

### 8. Compose-specific

| Issue | Fix |
|-------|-----|
| Recomposition loop | Check mutable state reads in composables |
| `@OptIn` required | Add `@OptIn(ExperimentalMaterial3Api::class)` |
| Unresolved reference | Verify import + dependency in `build.gradle.kts` |
| Preview crash | Ensure `@Preview` composable has no Hilt deps |

## Anti-patterns

- ❌ Leaving `Log.d` in committed code
- ❌ Using `!!` to silence nullability — fix the null check
- ❌ Suppressing lint warnings to work around a bug
- ❌ Debugging state by reading DataStore directly — use ViewModel StateFlow
- ❌ Guessing at XML structure — always log the raw response first
