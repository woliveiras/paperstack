---
name: expo
description: >
  Patterns and conventions for the Expo (React Native) app. Use when implementing
  or reviewing any screen, component, hook, or service in this project.
  Trigger phrases: "expo pattern", "react native", "new screen", "new component",
  "add navigation", "/expo".
applyTo:
  - "app/**"
  - "components/**"
  - "hooks/**"
  - "services/**"
  - "store/**"
---

# Expo / React Native Patterns

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
