---
name: android
description: >
  Patterns and conventions for the Android (Jetpack Compose + Kotlin) app. Use when
  implementing or reviewing any screen, composable, ViewModel, repository, or service.
  Trigger phrases: "android pattern", "jetpack compose", "new screen", "new composable",
  "viewmodel", "/android".
applyTo:
  - "app/src/main/kotlin/**"
  - "app/src/test/kotlin/**"
  - "app/src/androidTest/kotlin/**"
---

# Android (Jetpack Compose) Patterns

## Project structure

```
app/src/main/kotlin/com/paperstack/
  MainActivity.kt               # Single Activity (@AndroidEntryPoint)
  PaperStackApplication.kt      # Application class (@HiltAndroidApp)
  data/
    local/
      db/
        PaperStackDatabase.kt   # @Database class
        dao/SavedPaperDao.kt    # @Dao interface
        entity/SavedPaperEntity.kt
      datastore/
        SettingsDataStore.kt    # DataStore<Preferences> wrapper
    remote/
      ArxivApiService.kt        # OkHttp + XmlPullParser
    repository/
      FeedRepository.kt
      SavedPaperRepository.kt
      SettingsRepository.kt
  domain/
    model/
      Paper.kt
      Settings.kt
  ui/
    onboarding/
      OnboardingScreen.kt
      OnboardingViewModel.kt
    feed/
      FeedScreen.kt
      FeedViewModel.kt
      FeedUiState.kt
    detail/
      DetailScreen.kt
      DetailViewModel.kt
    saved/
      SavedScreen.kt
      SavedViewModel.kt
    components/               # Reusable @Composable functions
    theme/
      Theme.kt / Color.kt / Type.kt
  di/
    AppModule.kt              # @Module @InstallIn(SingletonComponent::class)
```

## Kotlin rules

- **No `!!`** unless you can prove non-null at call site. Prefer `?.let`, `requireNotNull`, or early return.
- No Java files in `src/main`. Kotlin only.
- Data classes for domain models. Sealed interfaces for UiState.
- Extension functions in the file they extend, or in a dedicated `*Extensions.kt`.

## ViewModel pattern

```kotlin
@HiltViewModel
class FeedViewModel @Inject constructor(
    private val feedRepository: FeedRepository,
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<FeedUiState>(FeedUiState.Loading)
    val uiState: StateFlow<FeedUiState> = _uiState.asStateFlow()

    fun fetchInitial() {
        viewModelScope.launch {
            _uiState.value = FeedUiState.Loading
            feedRepository.fetchPapers(settingsRepository.activeCategory, start = 0)
                .onSuccess { papers -> _uiState.value = FeedUiState.Success(papers) }
                .onFailure { e -> _uiState.value = FeedUiState.Error(e.message ?: "Unknown error") }
        }
    }
}

sealed interface FeedUiState {
    data object Loading : FeedUiState
    data class Success(val papers: List<Paper>, val hasMore: Boolean = true) : FeedUiState
    data class Error(val message: String) : FeedUiState
}
```

## Screen pattern

```kotlin
@Composable
fun FeedScreen(
    viewModel: FeedViewModel = hiltViewModel(),
    onPaperClick: (Paper) -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    when (val state = uiState) {
        is FeedUiState.Loading -> CircularProgressIndicator()
        is FeedUiState.Success -> FeedContent(state.papers, onPaperClick)
        is FeedUiState.Error   -> ErrorMessage(state.message) { viewModel.fetchInitial() }
    }
}
```

## Drawer navigation pattern

```kotlin
val drawerState = rememberDrawerState(DrawerValue.Closed)
val scope = rememberCoroutineScope()

ModalNavigationDrawer(
    drawerState = drawerState,
    drawerContent = {
        ModalDrawerSheet {
            categories.forEach { category ->
                NavigationDrawerItem(
                    label = { Text(category.name) },
                    selected = category.code == activeCategory,
                    onClick = {
                        onCategorySelected(category.code)
                        scope.launch { drawerState.close() }
                    }
                )
            }
            NavigationDrawerItem(
                label = { Text("+ Add categories") },
                selected = false,
                onClick = onAddCategories,
            )
        }
    }
) { FeedScreen(onMenuClick = { scope.launch { drawerState.open() } }) }
```

## DataStore pattern

```kotlin
// SettingsDataStore.kt
private val Context.dataStore by preferencesDataStore(name = "settings")

val DISPLAY_NAME          = stringPreferencesKey("display_name")
val SELECTED_CATEGORIES   = stringPreferencesKey("selected_categories")  // JSON array
val ACTIVE_CATEGORY       = stringPreferencesKey("active_category")
val ONBOARDING_COMPLETED  = booleanPreferencesKey("onboarding_completed")
```

## Room pattern

```kotlin
@Entity(tableName = "saved_papers")
data class SavedPaperEntity(
    @PrimaryKey val id: String,
    val title: String,
    val authors: String,     // JSON array (kotlinx-serialization)
    val abstract: String,
    val pdfUrl: String,
    val categories: String,  // JSON array (kotlinx-serialization)
    val submittedDate: String,
    val savedAt: Long = System.currentTimeMillis(),
)

@Dao
interface SavedPaperDao {
    @Query("SELECT * FROM saved_papers ORDER BY savedAt DESC")
    fun getAll(): Flow<List<SavedPaperEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(paper: SavedPaperEntity)

    @Query("DELETE FROM saved_papers WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("SELECT EXISTS(SELECT 1 FROM saved_papers WHERE id = :id)")
    fun isSaved(id: String): Flow<Boolean>
}
```

## Persistence keys

| Domain   | Storage              | Key / Table          |
|----------|----------------------|----------------------|
| Settings | DataStore Preferences | `"settings"`         |
| Saved papers | Room SQLite     | `saved_papers`       |

## Anti-patterns

- ❌ Business logic inside a `@Composable`
- ❌ OkHttp/network calls in ViewModel — only in `data/remote/`
- ❌ `GlobalScope` — always `viewModelScope` or injected `CoroutineScope`
- ❌ `SharedPreferences` — use DataStore instead
- ❌ XML layouts (`res/layout/*.xml`) — Jetpack Compose only
- ❌ `!!` on nullable values without a proven invariant
- ❌ Manual dependency instantiation — use Hilt


## Project structure

```
app/                      # Expo Router — file = route
  _layout.tsx             # Root layout (providers, theme)
  (tabs)/
    _layout.tsx           # Tab bar config
    index.tsx             # Home / feed screen
    saved.tsx             # Saved papers screen
    settings.tsx          # Settings screen
components/               # Reusable UI pieces
  PaperCard.tsx
  CategoryPicker.tsx
hooks/                    # Custom hooks (no UI logic)
  useArxivFeed.ts
  useSavedPapers.ts
services/                 # External integrations
  arxiv.ts                # arXiv API (see arxiv-api skill)
store/                    # Zustand global state
  feedStore.ts
  savedStore.ts
  settingsStore.ts
types/                    # Shared TypeScript types
  paper.ts
  category.ts
```

## TypeScript rules

- Strict mode is **on** (`"strict": true` in tsconfig).
- No `any`. Use `unknown` and narrow with type guards.
- All props interfaces use the `Props` suffix: `PaperCardProps`.
- All API response types live in `types/`.

## Routing (Expo Router)

- Use file-based routing. No `navigation.navigate('ScreenName')` with strings.
- Use typed routes: `router.push('/(tabs)/saved')`.
- Shared layouts go in `_layout.tsx` at the appropriate folder level.
- Deep links map directly to file paths.

## State management (Zustand)

- One store per domain (`feedStore`, `savedStore`, `settingsStore`).
- Stores live in `store/`. Never define store logic inside components.
- Use selectors to avoid re-renders: `const papers = useFeedStore(s => s.papers)`.
- Persist `savedStore` and `settingsStore` with `zustand/middleware` `persist`.

## Components

- One component per file.
- No business logic in components — delegate to hooks.
- Props must be typed with an explicit interface.
- Use `React.memo` only when profiling confirms a render bottleneck.

## Hooks

- Hooks that fetch data return `{ data, isLoading, error }`.
- Never call `services/arxiv.ts` directly in a component — always via a hook.
- Side effects belong in `useEffect` inside hooks, not in components.

## Styling

- Use NativeWind (Tailwind for RN) for styling.
- No inline `StyleSheet.create` objects for new code.
- Dark/light mode via NativeWind's `dark:` variant.

## AsyncStorage

- Keys: `@paperstack/<domain>` (e.g., `@paperstack/saved`, `@paperstack/settings`).
- Never read/write AsyncStorage directly in components — use the store's `persist` middleware.

## Anti-patterns

- ❌ `fetch` calls outside `services/`
- ❌ Navigation with hardcoded string routes
- ❌ Business logic inside screen files
- ❌ Multiple Zustand stores for the same domain
- ❌ `StyleSheet.create` in new components (use NativeWind)
- ❌ Untyped component props (`props: any`)
