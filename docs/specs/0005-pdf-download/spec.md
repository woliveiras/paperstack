---
status: Approved
number: "0005"
title: PDF Download
depends_on: ["0002"]
blocks: []
created: 2026-05-09
updated: 2026-05-09
owner: ""
---

# 0005 — PDF Download

## Context

Users want to download a paper's PDF to their device for offline reading. All storage is local — no backend or cloud involved. The download is triggered from the paper detail screen.

Driven by PRD: [docs/prds/0001-browse-by-category.md](../../prds/0001-browse-by-category.md)

## Goal

Allow users to download a paper's PDF to the device file system and open it with the native document viewer.

## Non-goals

- In-app PDF rendering
- Managing or deleting downloaded files from within the app (v2)
- Uploading or sharing downloaded files

## Functional requirements

- RF1: The "Download PDF" button on the paper detail screen (spec 0003) initiates the download.
- RF2: Downloads are handled via `expo-file-system` (`FileSystem.downloadAsync`).
- RF3: The PDF is saved to `FileSystem.documentDirectory + 'papers/<id>.pdf'`.
- RF4: While downloading, a progress indicator replaces the "Download PDF" button.
- RF5: On success, the button changes to "Open PDF" and the file is opened with `expo-sharing` or `Linking.openURL` with the local file URI.
- RF6: If the file already exists on disk, the "Download PDF" button is replaced with "Open PDF" on mount — no re-download.
- RF7: On download error, an inline error message is shown with a "Retry" option.
- RF8: The download state (idle / downloading / downloaded / error) is managed in a local component hook (`useDownload`), not in a global store.

## Contracts

### useDownload hook

```ts
// hooks/useDownload.ts
type DownloadState = 'idle' | 'downloading' | 'downloaded' | 'error'

interface UseDownloadResult {
  state: DownloadState
  progress: number        // 0–1
  download: () => Promise<void>
  open: () => void
  error: string | null
}

export function useDownload(paper: Paper): UseDownloadResult
```

### File path convention

```ts
const localPath = FileSystem.documentDirectory + `papers/${paper.id}.pdf`
```

### PDF URL

Use the versioned PDF URL from `paper.pdfUrl` (e.g. `https://arxiv.org/pdf/2605.06667v1`).

## Acceptance criteria

- [ ] AC1: Tapping "Download PDF" starts the download and shows a progress indicator.
- [ ] AC2: After a successful download, the button changes to "Open PDF".
- [ ] AC3: Tapping "Open PDF" opens the file in the device's native document viewer.
- [ ] AC4: If the file already exists on disk, "Open PDF" is shown immediately without re-downloading.
- [ ] AC5: A download error shows an error message and a "Retry" button.
- [ ] AC6: `useDownload` — `download` and `open` are covered by unit tests with mocked `expo-file-system`.
- [ ] AC7: Concurrent taps on "Download PDF" do not trigger multiple parallel downloads.

## Risks

| Risk | Mitigation |
|------|-----------|
| Device storage full | `expo-file-system` throws; catch and show user-friendly error |
| PDF URL changes between versions | Always use the URL from the `Paper` object, never construct it manually |
| Large PDFs on slow connections | Progress indicator keeps the user informed |

## References

- PRD: [docs/prds/0001-browse-by-category.md](../../prds/0001-browse-by-category.md)
- Depends on: spec 0002 (paper feed)
