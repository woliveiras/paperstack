# Paperstack

Mobile app to browse, save, and read arXiv papers by category.

## Stack

| Layer | Technology |
|-------|-----------|
| Platform | Android (native) |
| Language | Kotlin — strict null safety |
| UI | Jetpack Compose + Material 3 |
| Navigation | Navigation Compose (Drawer via `ModalNavigationDrawer`) |
| Data source | arXiv Atom/XML API |
| DI | Hilt |
| Storage (settings) | DataStore Preferences |
| Storage (saved papers) | Room (SQLite) |
| HTTP | OkHttp |
| XML parsing | Android built-in `XmlPullParser` |
| Async | Kotlin Coroutines + Flow / StateFlow |
| Serialization | kotlinx-serialization |

## Build & Test

```sh
./gradlew assembleDebug       # debug build
./gradlew assembleRelease     # release build
./gradlew test                # unit tests (JUnit5 + MockK)
./gradlew connectedCheck      # instrumented tests (device/emulator required)
./gradlew lint                # lint
```

## Architecture

```
app/src/main/kotlin/com/paperstack/
  MainActivity.kt              # single Activity (Hilt entry point)
  PaperstackApplication.kt     # Application class (@HiltAndroidApp)
  data/
    local/
      db/                      # Room database, DAOs, entities
      datastore/               # DataStore settings
    remote/                    # ArxivApiService (OkHttp + XmlPullParser)
    repository/                # FeedRepository, SavedPaperRepository
  domain/
    model/                     # Paper, Settings domain models
  ui/
    onboarding/                # Onboarding screens + ViewModel
    feed/                      # Feed screen + DrawerLayout + ViewModel
    detail/                    # Paper detail screen + ViewModel
    saved/                     # Saved papers screen + ViewModel
    components/                # Reusable Composables
    theme/                     # MaterialTheme, colors, typography
  di/                          # Hilt modules
```

## Conventions

- **English only.** All code, comments, commit messages, docs, specs, ADRs, and PRDs must be written in English.
- **TDD is mandatory.** Write the failing test first, then the implementation. Never write production code without a failing test covering it.
- **Kotlin everywhere.** No Java files in `src/main`. Null safety enforced — no `!!` without justification.
- **API calls only through `data/remote/ArxivApiService.kt`.** No OkHttp calls directly in ViewModels or Composables.
- **arXiv rate limit:** max 1 request per 3 seconds. Enforce in the repository layer.
- **XML parsing:** use `android.util.Xml` / `XmlPullParser`. Do not regex-parse XML.
- **State:** ViewModel + `StateFlow<UiState>` per screen. No business logic in Composables.
- **DI:** Hilt. All dependencies injected — no manual object graph.
- **ADRs** for architecture decisions → `docs/adrs/` (use `/madr` skill)
- **Specs** for features → `docs/specs/` (use `/spec` skill)
- **PRDs** before specs → write with `/write-prd` skill

## Docs

- [Specs index](docs/specs/README.md)
- [ADRs index](docs/adrs/README.md)

## Skills available

| Skill | Trigger |
|-------|---------|
| `spec-driven` | `/spec`, "new spec", "create spec" |
| `arxiv-api` | `/arxiv-api`, "arxiv endpoint", "api query" |
| `android` | `/android`, "android pattern", "jetpack compose", "new screen/composable" |
| `write-prd` | `/write-prd`, "write prd", "product requirements" |
| `madr` | `/madr`, "new adr", "architecture decision" |
| `unit-testing` | `/unit-test`, "write test", "unit test" |
| `debug` | `/debug`, "debug", "não está funcionando", "erro em runtime" |
