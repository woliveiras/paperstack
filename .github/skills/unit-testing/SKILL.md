---
name: unit-testing
description: >
  Unit testing patterns and conventions using JUnit5 + MockK + Turbine. Use when
  writing, reviewing, or debugging unit tests in this project. Trigger phrases:
  "write test", "unit test", "test this viewmodel", "test this repository", "/unit-test".
applyTo:
  - "app/src/test/**"
  - "app/src/androidTest/**"
---

# Unit Testing with JUnit5 + MockK + Turbine

## TDD workflow — mandatory

This project follows strict Test-Driven Development. The order is **always**:

1. **Red** — write a failing test that describes the behavior you want
2. **Green** — write the minimum code to make the test pass
3. **Refactor** — clean up, keeping tests green

Never write production code without a failing test first. If you are asked to implement something without a test, write the test first and show it failing before proceeding.

## Commands

```sh
./gradlew test                 # unit tests (JUnit5, no device needed)
./gradlew connectedCheck       # instrumented tests (device/emulator required)
./gradlew test --tests "com.paperstack.data.remote.ArxivApiServiceTest"  # single class
```

## What to test

| Layer | Test type | Notes |
|-------|-----------|-------|
| `data/remote/ArxivApiService` | Unit | MockK OkHttp; test XML parsing, errors |
| `data/repository/` | Unit | MockK DAOs and ApiService |
| `ui/*/ViewModel` | Unit | MockK repository; test StateFlow with Turbine |
| `data/local/dao/` | Instrumented | Room in-memory DB (requires device/emulator) |
| `ui/` screens | Instrumented | Compose UI test (requires device/emulator) |

## File location

Unit tests live in `app/src/test/kotlin/com/paperstack/` **mirroring the main source tree**:

```
main/kotlin/com/paperstack/data/remote/ArxivApiService.kt
test/kotlin/com/paperstack/data/remote/ArxivApiServiceTest.kt   ✅
```

Instrumented tests live in `app/src/androidTest/kotlin/com/paperstack/`.

## Test structure

Use `@Nested` for grouping, backtick function names for readability.

```kotlin
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertEquals

class FeedViewModelTest {

    private val feedRepository = mockk<FeedRepository>()
    private val settingsRepository = mockk<SettingsRepository>()
    private lateinit var viewModel: FeedViewModel

    @BeforeEach
    fun setUp() {
        viewModel = FeedViewModel(feedRepository, settingsRepository)
    }

    @Nested
    inner class `fetchInitial` {

        @Test
        fun `emits Success with papers on repository success`() = runTest {
            val papers = listOf(fakePaper())
            coEvery { settingsRepository.activeCategory } returns "cs.AI"
            coEvery { feedRepository.fetchPapers("cs.AI", 0) } returns Result.success(papers)

            viewModel.uiState.test {
                viewModel.fetchInitial()
                assertEquals(FeedUiState.Loading, awaitItem())
                assertEquals(FeedUiState.Success(papers), awaitItem())
                cancelAndIgnoreRemainingEvents()
            }
        }

        @Test
        fun `emits Error when repository fails`() = runTest {
            coEvery { settingsRepository.activeCategory } returns "cs.AI"
            coEvery { feedRepository.fetchPapers(any(), any()) } returns Result.failure(Exception("timeout"))

            viewModel.uiState.test {
                viewModel.fetchInitial()
                skipItems(1) // Loading
                val error = awaitItem() as FeedUiState.Error
                assertEquals("timeout", error.message)
                cancelAndIgnoreRemainingEvents()
            }
        }
    }
}
```

## MockK patterns

```kotlin
// Function mock
val repo = mockk<FeedRepository>()

// Suspend function stub
coEvery { repo.fetchPapers(any(), any()) } returns Result.success(emptyList())

// Verify call
coVerify(exactly = 1) { repo.fetchPapers("cs.AI", 0) }

// Relax all unset interactions
val repo = mockk<FeedRepository>(relaxed = true)
```

## Turbine (Flow testing)

```kotlin
viewModel.uiState.test {
    awaitItem()   // consume emission
    skipItems(2)  // skip N emissions
    val item = awaitItem()
    cancelAndIgnoreRemainingEvents()
}
```

## XML fixtures

Store XML fixtures as `.kt` constants in `__fixtures__/` next to the test:

```
test/kotlin/com/paperstack/data/remote/__fixtures__/ArxivFixtures.kt
```

```kotlin
// ArxivFixtures.kt
object ArxivFixtures {
    val SINGLE_ENTRY_XML = """
        <?xml version='1.0' encoding='UTF-8'?>
        <feed xmlns="http://www.w3.org/2005/Atom"
              xmlns:arxiv="http://arxiv.org/schemas/atom">
          <opensearch:totalResults xmlns:opensearch="http://a9.com/-/spec/opensearch/1.1/">1</opensearch:totalResults>
          <entry>
            <id>http://arxiv.org/abs/2605.00001v1</id>
            <title>Test Paper Title</title>
            <summary>Abstract text here.</summary>
            <published>2026-05-01T00:00:00Z</published>
            <updated>2026-05-01T00:00:00Z</updated>
            <author><name>Author One</name></author>
            <link href="https://arxiv.org/pdf/2605.00001v1" rel="related" type="application/pdf"/>
            <category term="cs.AI" scheme="http://arxiv.org/schemas/atom"/>
            <arxiv:primary_category term="cs.AI"/>
          </entry>
        </feed>
    """.trimIndent()
}
```


## TDD workflow — mandatory

This project follows strict Test-Driven Development. The order is **always**:

1. **Red** — write a failing test that describes the behavior you want
2. **Green** — write the minimum code to make the test pass
3. **Refactor** — clean up, keeping tests green

Never write production code without a failing test first. If you are asked to implement something without a test, write the test first and show it failing before proceeding.

## Commands

```sh
./gradlew test                 # unit tests (JUnit5, no device needed)
./gradlew connectedCheck       # instrumented tests (device/emulator required)
./gradlew test --tests "com.paperstack.data.remote.ArxivApiServiceTest"  # single class
```
