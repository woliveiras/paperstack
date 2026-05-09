---
name: arxiv-api
description: >
  arXiv Atom/XML API integration patterns. Use when implementing or reviewing
  any code in data/remote/ArxivApiService.kt or any file that calls the arXiv API.
  Trigger phrases: "arxiv api", "fetch papers", "query arxiv", "/arxiv-api",
  or when editing ArxivApiService.kt.
applyTo:
  - "app/src/main/kotlin/com/paperstack/data/remote/**"
---

# arXiv API

All arXiv data access MUST go through `data/remote/ArxivApiService.kt`. No direct
OkHttp calls to `export.arxiv.org` anywhere else in the codebase.

## Base URL

```
https://export.arxiv.org/api/query
```

## Query parameters

| Parameter | Type | Description |
|-----------|------|-------------|
| `search_query` | string | `cat:cs.AI` or `cat:cs.AI+AND+cat:cs.PL` |
| `sortBy` | string | `submittedDate` \| `lastUpdatedDate` \| `relevance` |
| `sortOrder` | string | `descending` \| `ascending` |
| `start` | number | Pagination offset (default 0) |
| `max_results` | number | Page size — **max 100**, default 10 |

## Example

```
GET https://export.arxiv.org/api/query?search_query=cat:cs.AI&sortBy=submittedDate&sortOrder=descending&start=0&max_results=25
```

## Response format

The API returns **Atom/XML**. Use Android's built-in `XmlPullParser` to parse it.
Do NOT use regex, string splitting, or third-party XML libraries.

```kotlin
// In ArxivApiService.kt
val parser: XmlPullParser = Xml.newPullParser()
parser.setInput(responseBody.byteStream(), "UTF-8")
// Walk the event stream: START_TAG, TEXT, END_TAG
```

Key fields per `<entry>`:

| XML field | Description |
|-----------|-------------|
| `id` | Canonical URL, e.g. `http://arxiv.org/abs/2605.06667v1` |
| `title` | Paper title |
| `summary` | Abstract |
| `published` | ISO 8601 date |
| `updated` | ISO 8601 date (last version) |
| `author[].name` | Author names |
| `link[@rel="related"][@type="application/pdf"]` | Direct PDF URL |
| `category[@term]` | arXiv category code |
| `arxiv:primary_category[@term]` | Primary category |
| `arxiv:comment` | Conference/journal info if present |

## Rate limiting

- **Hard limit: 1 request per 3 seconds.**
- Never fire parallel requests to the arXiv API.
- Add `User-Agent: Paperstack/1.0 (contact@example.com)` header via OkHttp `Interceptor`.
- Implement exponential backoff on 503 responses.

## Pagination

Use `start` for pagination. Do not request `max_results > 100`.

```ts
// Page 2 with 25 items per page
start = (page - 1) * pageSize  // e.g. start=25 for page 2
```

## Category codes

Multiple categories can be combined with `+AND+`:

```
search_query=cat:cs.AI+AND+cat:cs.PL
```

Or use OR semantics:

```
search_query=cat:cs.AI+OR+cat:cs.PL
```

## PDF URL convention

Given the abstract URL `http://arxiv.org/abs/XXXX.XXXXXvN`, the PDF is:

```
https://arxiv.org/pdf/XXXX.XXXXXvN
```

Strip the `v1`/`v2` suffix to always get the latest version:

```
https://arxiv.org/pdf/XXXX.XXXXX
```

## Error handling

| HTTP status | Meaning | Action |
|-------------|---------|--------|
| 200 | OK | Parse XML |
| 400 | Bad query | Log query, show user error |
| 503 | Overloaded | Retry with backoff (max 3 times) |

An empty result set returns 200 with `<opensearch:totalResults>0</opensearch:totalResults>` — not an error.

## Anti-patterns

- ❌ Calling OkHttp outside `data/remote/ArxivApiService.kt`
- ❌ Requesting `max_results > 100`
- ❌ Firing multiple requests in parallel
- ❌ Parsing XML with regex, string splitting, or third-party libraries
- ❌ Ignoring rate limits on user-triggered refreshes
