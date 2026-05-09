---
name: unit-testing
description: >
  Unit testing patterns and conventions using Vitest. Use when writing, reviewing,
  or debugging unit tests in this project. Trigger phrases: "write test",
  "unit test", "test this hook", "test this service", "/unit-test".
applyTo:
  - "**/*.test.ts"
  - "**/*.test.tsx"
  - "**/*.spec.ts"
  - "**/*.spec.tsx"
---

# Unit Testing with Vitest

## TDD workflow — mandatory

This project follows strict Test-Driven Development. The order is **always**:

1. **Red** — write a failing test that describes the behavior you want
2. **Green** — write the minimum code to make the test pass
3. **Refactor** — clean up, keeping tests green

Never write production code without a failing test first. If you are asked to implement something without a test, write the test first and show it failing before proceeding.

## Commands

```sh
npx vitest run      # single run (CI)
npx vitest          # watch mode (development)
npx vitest --coverage  # coverage report
```

## What to test

| Layer | Test? | Notes |
|-------|-------|-------|
| `services/arxiv.ts` | ✅ Yes | Mock `fetch`; test parsing, pagination, error handling |
| `hooks/` | ✅ Yes | Use `@testing-library/react-hooks` |
| `store/` | ✅ Yes | Test state transitions; no UI |
| `components/` | 🟡 Selective | Only for complex render logic; not layout |
| `app/` screens | ❌ No | E2E territory |

## File location

Tests live **next to the file they test**:

```
services/arxiv.ts
services/arxiv.test.ts   ✅

hooks/useArxivFeed.ts
hooks/useArxivFeed.test.ts  ✅
```

Do not create a separate `__tests__/` folder.

## Test structure

Use `describe` → `it` (not `test`). One `describe` per module, nested `describe` for sub-cases.

```ts
import { describe, it, expect, vi, beforeEach } from 'vitest'

describe('arxiv service', () => {
  describe('fetchPapers', () => {
    beforeEach(() => {
      vi.resetAllMocks()
    })

    it('returns parsed papers on success', async () => {
      // arrange
      vi.stubGlobal('fetch', vi.fn().mockResolvedValue({
        ok: true,
        text: async () => FIXTURE_XML,
      }))

      // act
      const result = await fetchPapers({ category: 'cs.AI', page: 1 })

      // assert
      expect(result.papers).toHaveLength(2)
      expect(result.papers[0].title).toBe('Expected Title')
    })

    it('throws on non-ok response', async () => {
      vi.stubGlobal('fetch', vi.fn().mockResolvedValue({ ok: false, status: 503 }))
      await expect(fetchPapers({ category: 'cs.AI', page: 1 })).rejects.toThrow()
    })
  })
})
```

## Mocking

- Use `vi.fn()` for function mocks.
- Use `vi.stubGlobal('fetch', ...)` to mock `fetch` — never use `jest.mock`.
- Use `vi.mock('module')` for module-level mocks.
- Always call `vi.resetAllMocks()` in `beforeEach`.

## XML fixtures

Store XML fixtures as `.ts` constants in a `__fixtures__/` folder next to the test:

```
services/__fixtures__/arxiv-response.ts
```

```ts
// services/__fixtures__/arxiv-response.ts
export const SINGLE_ENTRY_XML = `<?xml version='1.0' encoding='UTF-8'?>
<feed xmlns="http://www.w3.org/2005/Atom">
  <entry>
    <id>http://arxiv.org/abs/2605.00001v1</id>
    <title>Test Paper Title</title>
    ...
  </entry>
</feed>`
```

## Hooks testing

Use `renderHook` from `@testing-library/react-native`:

```ts
import { renderHook, waitFor } from '@testing-library/react-native'
import { useArxivFeed } from './useArxivFeed'

it('loads papers on mount', async () => {
  const { result } = renderHook(() => useArxivFeed('cs.AI'))
  await waitFor(() => expect(result.current.isLoading).toBe(false))
  expect(result.current.papers.length).toBeGreaterThan(0)
})
```

## Store testing

Test Zustand stores by calling actions directly — no component needed:

```ts
import { useSavedStore } from '../store/savedStore'

it('saves a paper', () => {
  const { savePaper, savedPapers } = useSavedStore.getState()
  savePaper(MOCK_PAPER)
  expect(useSavedStore.getState().savedPapers).toHaveLength(1)
})
```

Reset store between tests:

```ts
beforeEach(() => {
  useSavedStore.setState({ savedPapers: [] })
})
```

## Coverage

Target ≥80% line coverage for `services/` and `hooks/`. No coverage target for `components/`.

## Anti-patterns

- ❌ Testing implementation details (internal function calls, private state)
- ❌ Snapshot tests for logic — use explicit `expect` assertions
- ❌ `test()` instead of `it()` (inconsistent with project style)
- ❌ Fixtures inlined in test files (use `__fixtures__/`)
- ❌ `jest.mock` — this project uses `vi.mock`
- ❌ Tests that depend on network (all network calls must be mocked)
