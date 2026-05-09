# Paperstack

Mobile app to browse, save, and read arXiv papers by category.

## Stack

| Layer | Technology |
|-------|-----------|
| Framework | Expo SDK (React Native) |
| Language | TypeScript — strict mode |
| Routing | Expo Router (file-based) |
| Data source | arXiv Atom/XML API |
| Storage | AsyncStorage (favorites, preferences) |

## Build & Test

```sh
npx expo start            # dev server (Expo Go)
npx expo run:ios          # iOS simulator
npx expo run:android      # Android emulator
npx expo export           # production build
npx tsc --noEmit          # type-check only
npx vitest run            # unit tests (CI)
npx vitest                # unit tests (watch mode)
```

## Architecture

```
app/              # Expo Router screens (file-based routing)
  (tabs)/         # Bottom tab navigator
  _layout.tsx     # Root layout / providers
components/       # Reusable UI components
hooks/            # Custom React hooks
services/         # External integrations (arXiv API)
store/            # Global state
types/            # Shared TypeScript types
```

## Conventions

- **TDD is mandatory.** Write the failing test first, then the implementation. Never write production code without a failing test covering it.
- **TypeScript everywhere.** No `.js` files. Strict mode on.
- **API calls only through `services/arxiv.ts`.** No `fetch` directly in screens or components.
- **arXiv rate limit:** max 1 request per 3 seconds. Respect it — no parallel bursts.
- **XML parsing:** use `fast-xml-parser`. Do not regex-parse XML.
- **State:** Zustand stores in `store/`. No prop drilling beyond 2 levels.
- **Routing:** Expo Router file conventions. No manual `navigation.navigate` outside typed wrappers.
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
| `expo` | `/expo`, "expo pattern", "react native" |
| `write-prd` | `/write-prd`, "write prd", "product requirements" |
| `madr` | `/madr`, "new adr", "architecture decision" |
| `unit-testing` | `/unit-test`, "write test", "unit test" |
