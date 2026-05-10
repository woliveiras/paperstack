# Paperstack

A mobile app to browse, save, and read arXiv papers by category.

Built with Android (Kotlin + Jetpack Compose) and the [arXiv API](https://arxiv.org/help/api).

## Features

- Browse papers by arXiv category (cs.AI, cs.PL, etc.)
- Save papers to a personal reading list
- Read abstracts and open PDFs
- Daily feed of new submissions

## Stack

- **Android** (Kotlin, Jetpack Compose, Material 3)
- **Hilt** (dependency injection)
- **Room** (saved papers) + **DataStore** (settings)
- **OkHttp** + **XmlPullParser** (arXiv Atom/XML API)
- **Coroutines + Flow** (async)

## Development

```sh
./gradlew assembleDebug       # debug build
./gradlew test                # unit tests (JUnit5 + MockK)
./gradlew connectedCheck      # instrumented tests
./gradlew lint                # lint
```

## Docs

- [Specs](docs/specs/README.md) — feature specs
- [ADRs](docs/adrs/README.md) — architecture decision records

## License

[MIT](LICENSE)
