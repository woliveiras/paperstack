---
name: debug
description: >
  Debugging workflow for the Expo / React Native app. Use when tracking down
  runtime errors, unexpected behavior, network issues, or state problems.
  Trigger phrases: "debug", "não está funcionando", "erro em runtime",
  "por que isso falha", "breakpoint", "/debug".
---

# Debugging — Expo / React Native

## Tools available

| Tool | What it debugs |
|------|---------------|
| Expo DevTools (`npx expo start`) | General runtime, logs, QR |
| React Native DevTools (Chrome) | JS breakpoints, call stack, console |
| Flipper | Network, AsyncStorage, React DevTools |
| `console.log` / `console.error` | Quick sanity checks |
| Vitest (`--reporter=verbose`) | Unit test failures |
| `npx tsc --noEmit` | Type errors without running the app |

## Step-by-step workflow

### 1. Identify the layer

| Symptom | Likely layer | Start here |
|---------|-------------|------------|
| TypeScript error | Types | `npx tsc --noEmit` |
| Test failure | Unit logic | `npx vitest run --reporter=verbose` |
| Crash on startup | Root layout / providers | `app/_layout.tsx` |
| Wrong data shown | Service / hook | `services/arxiv.ts`, `hooks/` |
| State not updating | Zustand store | `store/` |
| Navigation broken | Expo Router | `app/` file structure |
| PDF not opening | Platform API | `Linking.openURL` call |
| AsyncStorage lost | Persistence | `zustand/middleware` persist config |

### 2. Check logs first

```sh
npx expo start --clear   # clear Metro cache before debugging
```

Filter Metro logs:
```sh
npx expo start 2>&1 | grep -E "ERROR|WARN"
```

### 3. Inspect network (arXiv API)

Add a temporary log in `services/arxiv.ts`:

```ts
console.log('[arxiv] request:', url)
console.log('[arxiv] response status:', res.status)
console.log('[arxiv] raw xml:', await res.text())  // remove after debug
```

**Never commit `console.log` calls** — remove before PR.

### 4. Inspect Zustand store

Dump store state at any point:

```ts
import { useFeedStore } from '../store/feedStore'
console.log('[store]', useFeedStore.getState())
```

Or add the Zustand devtools middleware temporarily:

```ts
import { devtools } from 'zustand/middleware'
// wrap your store create() with devtools() for Redux DevTools support
```

### 5. Isolate with a unit test

If the bug is in a service or hook, write a failing test that reproduces it:

```sh
npx vitest run services/arxiv.test.ts --reporter=verbose
```

This is often faster than running the full app.

### 6. XML parsing issues

If the arXiv response parses incorrectly:

1. Log the raw XML (step 3)
2. Paste it into a minimal test with a fixture
3. Run `fast-xml-parser` against it with the exact options used in `services/arxiv.ts`
4. Adjust the parser options — do NOT switch to string/regex parsing

### 7. React Native specific

| Issue | Fix |
|-------|-----|
| White screen on startup | Check `app/_layout.tsx` for unhandled errors |
| "Unable to resolve module" | `npx expo start --clear` |
| Stale state after hot reload | Full reload: `R` in terminal or shake device |
| Fonts not loading | Ensure `useFonts` resolves before rendering |
| `undefined is not an object` | Check optional chaining on API response fields |

## Anti-patterns

- ❌ Leaving `console.log` in committed code
- ❌ Adding `any` to silence a TypeScript error — fix the type
- ❌ Disabling ESLint rules to work around a bug
- ❌ Debugging state by reading AsyncStorage directly — use store selectors
- ❌ Guessing at XML structure — always log the raw response first
