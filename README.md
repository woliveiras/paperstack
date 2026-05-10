<p align="center">
  <img src="docs/logo.svg" width="128" alt="Paperstack logo" />
</p>

<h1 align="center">Paperstack</h1>

<p align="center">
  Browse, save, and read arXiv papers by category, right from your phone.
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Platform-Android-3DDC84?logo=android&logoColor=white" alt="Android" />
  <img src="https://img.shields.io/badge/Kotlin-2.1-7F52FF?logo=kotlin&logoColor=white" alt="Kotlin" />
  <img src="https://img.shields.io/badge/Jetpack_Compose-Material_3-4285F4?logo=jetpackcompose&logoColor=white" alt="Jetpack Compose" />
  <img src="https://img.shields.io/badge/Min_SDK-26-34A853" alt="Min SDK 26" />
  <img src="https://img.shields.io/badge/License-MIT-blue" alt="MIT License" />
</p>

---

## Features

- **Browse by category** -- cs.AI, cs.PL, math.CO, and more
- **Save papers** to a personal reading list (offline, local database)
- **Read abstracts** and open PDFs directly
- **Daily feed** of new arXiv submissions

## Architecture

```
data/
  local/db/          Room database, DAOs, entities
  local/datastore/   DataStore preferences
  remote/            ArxivApiService (OkHttp + XmlPullParser)
  repository/        FeedRepository, SavedPaperRepository
domain/model/        Paper, Settings
ui/
  feed/              Feed screen + ViewModel
  detail/            Paper detail screen + ViewModel
  saved/             Saved papers screen + ViewModel
  onboarding/        Onboarding flow
  components/        Reusable Composables
  theme/             Material 3 theme
di/                  Hilt modules
```

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Language | Kotlin (strict null safety) |
| UI | Jetpack Compose + Material 3 |
| DI | Hilt |
| Local storage | Room (papers) + DataStore (settings) |
| HTTP + parsing | OkHttp + XmlPullParser |
| Async | Coroutines + Flow / StateFlow |
| Data source | [arXiv Atom/XML API](https://arxiv.org/help/api) |

## Getting Started

### Prerequisites

- Android Studio Ladybug or newer
- JDK 21
- Android SDK 36

### Build & Run

```sh
./gradlew assembleDebug          # debug APK
./gradlew installDebug           # install on connected device/emulator
```

### Test

```sh
./gradlew test                   # unit tests (JUnit5 + MockK)
./gradlew connectedCheck         # instrumented tests (device required)
./gradlew lint                   # lint checks
```

## Documentation

| Doc | Description |
|-----|-------------|
| [Specs](docs/specs/README.md) | Feature specifications |
| [ADRs](docs/adrs/README.md) | Architecture decision records |
| [PRDs](docs/prds/) | Product requirements documents |

## Contributing

1. Fork the repo
2. Create a feature branch (`git checkout -b feat/my-feature`)
3. Write tests first (TDD is mandatory)
4. Commit with [Conventional Commits](https://www.conventionalcommits.org/)
5. Open a Pull Request

## License

[MIT](LICENSE)
