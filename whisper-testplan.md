# Whisper Speech Recognition — Manual Test Plan

**Feature**: On-device speech recognition for poem recitation scoring
**Module**: `shared/shared-whisper` + `feature/feature-poems`
**Date**: 2026-03-02
**Emulator**: Pixel 6, API 31, x86_64

---

## Prerequisites

- [ ] Debug APK installed on device/emulator
- [ ] Complete onboarding (name + avatar)
- [ ] At least one poem visible in Poems browse tab
- [ ] Internet connection available (for model download)

---

## Test 1: Poems Screen Navigation

| Step | Action | Expected Result | Status |
|------|--------|-----------------|--------|
| 1.1 | Open app, navigate to Home | Home screen loads | |
| 1.2 | Tap "Poems" card on Home | Poems browse screen appears | |
| 1.3 | Verify poems are listed | At least one poem shows with title + author | |
| 1.4 | Tap any poem | PoemDetailScreen loads with poem text | |
| 1.5 | Verify dual FABs visible | Two FABs: small TTS (speaker icon) + large mic (microphone icon) | |
| 1.6 | Verify back button works | ArrowBack in TopAppBar navigates back | |

---

## Test 2: TTS Read Aloud (Existing Feature)

| Step | Action | Expected Result | Status |
|------|--------|-----------------|--------|
| 2.1 | On poem detail, tap small speaker FAB | Read-aloud starts, first line highlights | |
| 2.2 | Wait for TTS to advance | Lines highlight sequentially | |
| 2.3 | Tap stop button (speaker FAB changes to stop) | Reading stops, highlight clears | |
| 2.4 | Verify TTS FAB hides during recording | Start recording first, TTS FAB should disappear | |

---

## Test 3: Microphone Permission Flow

| Step | Action | Expected Result | Status |
|------|--------|-----------------|--------|
| 3.1 | On poem detail, tap mic FAB (first time) | Model download sheet appears (if model not downloaded) OR permission dialog appears | |
| 3.2 | If model download sheet: wait for download | Progress bar fills, sheet dismisses when done | |
| 3.3 | If permission dialog: deny permission | Snackbar: "Microphone access is needed to hear you read" | |
| 3.4 | Tap mic FAB again | Permission dialog appears again | |
| 3.5 | Grant permission | Recording starts (mic FAB turns red) | |

---

## Test 4: Model Download Flow

| Step | Action | Expected Result | Status |
|------|--------|-----------------|--------|
| 4.1 | Fresh install (no model cached) | First mic tap triggers model download sheet | |
| 4.2 | Download sheet shows progress | "Downloading speech model..." text, progress bar, "40 MB" label | |
| 4.3 | Wait for download to complete | Sheet dismisses, recording starts (or permission requested) | |
| 4.4 | Kill app and relaunch, tap mic again | No download sheet — model already cached | |

---

## Test 5: Recording State Machine

| Step | Action | Expected Result | Status |
|------|--------|-----------------|--------|
| 5.1 | With permission granted + model loaded, tap mic FAB | FAB turns red, mic → stop icon, state = RECORDING | |
| 5.2 | Observe amplitude animation | FAB pulses/scales based on audio amplitude | |
| 5.3 | TTS speaker FAB hidden during recording | Only mic FAB visible | |
| 5.4 | Tap stop (red FAB) | State → PROCESSING, "Let me check how you did..." + spinner | |
| 5.5 | Wait for transcription | Processing indicator disappears, result bottom sheet appears | |

---

## Test 6: Recording While Tapping Words

| Step | Action | Expected Result | Status |
|------|--------|-----------------|--------|
| 6.1 | Start recording (mic FAB turns red) | Recording active | |
| 6.2 | Tap a word in the poem text | Snackbar: "Turn off the microphone first!" | |
| 6.3 | Stop recording, wait for results | Results appear | |
| 6.4 | Tap a word after scoring | TTS speaks that word (SpeakWord effect) | |

---

## Test 7: Result Bottom Sheet

| Step | Action | Expected Result | Status |
|------|--------|-----------------|--------|
| 7.1 | After recording + scoring, sheet appears | Title: "How did you do?" | |
| 7.2 | Star rating visible | 1-5 filled stars with animation | |
| 7.3 | Encouragement message shown | One of: Excellent/Good job/Nice effort/Keep practising | |
| 7.4 | Colour legend present | 4 items: green=Great, red=Try again, yellow=Didn't catch, grey=Not reached | |
| 7.5 | Words displayed with colours | FlowRow of all poem words, each coloured by state | |
| 7.6 | "Words to practise" section | Shows INCORRECT + UNCLEAR words (if any) | |
| 7.7 | Tap a word in results | TTS speaks that word | |
| 7.8 | Tap "Try Again" | Sheet dismisses, words reset to UNREAD, state = IDLE | |
| 7.9 | Dismiss by swiping down | Sheet closes, scored words remain visible on screen | |

---

## Test 8: Word Colouring After Scoring

| Step | Action | Expected Result | Status |
|------|--------|-----------------|--------|
| 8.1 | After scoring, observe poem lines | Words display in FlowRow (not plain text) | |
| 8.2 | CORRECT words | Soft green background (#C8E6C9) | |
| 8.3 | INCORRECT words | Soft red background (#FFCDD2) + underline | |
| 8.4 | UNCLEAR words | Soft yellow background (#FFF9C4) | |
| 8.5 | SKIPPED words | Transparent background, alpha 0.35 (greyed out) | |
| 8.6 | UNREAD words (before any recording) | Normal text, no background colour | |

---

## Test 9: Favourite Toggle

| Step | Action | Expected Result | Status |
|------|--------|-----------------|--------|
| 9.1 | Tap heart icon in TopAppBar | Heart fills, poem marked as favourite | |
| 9.2 | Tap again | Heart unfills, favourite removed | |
| 9.3 | Navigate back, go to Favourites tab | Favourited poem appears/disappears correctly | |

---

## Test 10: Edge Cases

| Step | Action | Expected Result | Status |
|------|--------|-----------------|--------|
| 10.1 | Start recording, stop immediately (< 1 sec) | Transcription runs (may produce empty text), all words SKIPPED, 1 star | |
| 10.2 | Start read-aloud, then tap mic FAB | Read-aloud stops, recording starts | |
| 10.3 | Navigate back while recording | Recording stops, no crash | |
| 10.4 | Rotate device during processing | No crash, state preserved | |
| 10.5 | Very short poem (1-2 lines) | All features work: record, score, result sheet | |
| 10.6 | Long poem (20+ lines) | LazyColumn scrolls, all words scored, result sheet scrollable | |

---

## Test 11: Localization

| Step | Action | Expected Result | Status |
|------|--------|-----------------|--------|
| 11.1 | Switch app language to French | All poem UI strings in French | |
| 11.2 | Recording UI strings | "Commencer l'enregistrement", "Voyons comment tu as lu..." | |
| 11.3 | Result sheet in French | "Comment tu as fait?", "Super!", "Réessayer" | |
| 11.4 | Switch to German | "Aufnahme starten", "Mal schauen...", "Nochmal versuchen" | |

---

## Test 12: No-Crash Smoke Test

| Step | Action | Expected Result | Status |
|------|--------|-----------------|--------|
| 12.1 | Open every poem in browse list | No crash on any poem | |
| 12.2 | Rapidly tap mic FAB on/off | No crash, state transitions correctly | |
| 12.3 | Background app during recording, return | No crash (recording may stop) | |
| 12.4 | Kill process, relaunch, open poem | Clean state, no crash | |

---

## Results Summary

| Category | Pass | Fail | Skip | Notes |
|----------|------|------|------|-------|
| Navigation | | | | |
| TTS Read Aloud | | | | |
| Permission Flow | | | | |
| Model Download | | | | |
| Recording States | | | | |
| Word Tap Behaviour | | | | |
| Result Bottom Sheet | | | | |
| Word Colouring | | | | |
| Favourite Toggle | | | | |
| Edge Cases | | | | |
| Localization | | | | |
| Smoke Test | | | | |
| **TOTAL** | | | | |

---

## Notes

- Emulator virtual microphone produces silence or noise — whisper will transcribe empty/garbage text. This is expected and tests the SKIPPED/UNCLEAR scoring paths.
- Real device testing with a child reading is needed for accuracy validation.
- Model download requires internet; test on device with connectivity.
- The TINY model (40MB) is used by default for fastest download + inference.
