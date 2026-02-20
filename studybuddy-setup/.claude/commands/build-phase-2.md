Build Phase 2: Dictée Mode. Read `docs/build-instructions.md` sections 6 and 13 before starting.

Prerequisites: Phase 1 must be complete (core modules and shared-points exist and compile).

## What to build

### PR 7: shared-tts
Build the TTS wrapper module (section 6, "TTS Module"):
- `TtsManager` singleton with Hilt `@Inject`:
  - `initialize()` — init Android TextToSpeech engine
  - `speak(text, locale, speed)` — play word with correct locale
  - `stop()` / `release()`
  - `getInstalledVoices(): List<VoicePack>`
  - `downloadVoice(locale): Flow<DownloadProgress>`
  - `isLocaleAvailable(locale): Boolean`
- `TtsState` sealed class: Initializing, Ready, Speaking, Error
- `VoicePackDownloadWorker` using WorkManager for background downloads
- Expose state via `StateFlow<TtsState>`
- Support locales: fr-FR, en-GB, de-DE
- Speed options: normal (1.0f), slow (0.7f)
- Fallback: use default system voice if neural unavailable
- Unit tests for state transitions (use MockK to mock TextToSpeech)
- Commit: `feat(tts): add TTS manager with offline voice pack support`

### PR 8: shared-ink
Build the ML Kit handwriting module (section 6, "Ink Module"):
- `InkRecognitionManager` singleton with Hilt:
  - `initialize(languageTag)` — download model if needed, create recognizer
  - `recognize(ink: Ink): Result<String>` — returns recognized text
  - `isModelDownloaded(languageTag): Boolean`
  - `downloadModel(languageTag): Flow<DownloadProgress>`
- `HandwritingCanvas` composable:
  - Captures touch/stylus input as `Ink` strokes
  - Displays ruled lines for writing guides
  - Clear button to reset canvas
  - Real-time stroke rendering
- Support language tags: fr, en, de
- Compose canvas that captures `MotionEvent` and converts to ML Kit `Ink.Stroke`
- Unit tests for stroke-to-ink conversion
- Commit: `feat(ink): add ML Kit handwriting recognition with canvas composable`

### PR 9: feature-dictee
Build all 4 screens from section 6:

**DicteeListScreen:**
- Display all word lists with title, language flag, word count, mastery %
- "New List" button → dialog to create list (title + language picker)
- Mastery progress bar per list (color: red < 50%, yellow < 80%, green >= 80%)
- Tap list → navigate to DicteeWordEntryScreen
- Swipe to delete list with undo snackbar

**DicteeWordEntryScreen:**
- Show all words in a list with mastery indicator dots
- Add word: text field at bottom, validate non-empty
- TTS preview button per word (play via shared-tts)
- Swipe to delete word with undo
- "Start Practice" button → navigate to DicteePracticeScreen
- Edit mode toggle for bulk operations

**DicteePracticeScreen:**
- MVI: `DicteePracticeState` / `DicteePracticeIntent` as specified in section 6
- TTS plays word automatically on load (using list's language locale)
- Tap 🔊 to replay (unlimited replays)
- Toggle between keyboard input and handwriting canvas
- Keyboard mode: centered text field with letter spacing
- Handwriting mode: `HandwritingCanvas` from shared-ink, show recognized text
- Hint system: show first letter + word length
- "Check" button → `CheckSpellingUseCase` with accent strictness from settings
- Progress bar showing current word / total words
- Points display (running session total)

**DicteeFeedbackScreen (or overlay within Practice):**
- Correct: confetti animation (CelebrationOverlay), green highlight, +points fly-up, "Next Word" button
- Incorrect: gentle shake animation, show correct spelling with differences highlighted, "Try Again" / "Next Word" buttons
- Never punishing — always encouraging messages
- Streak display if applicable

**DicteePracticeViewModel:**
- Loads words in random order, weighted by inverse mastery (words with lower correctCount appear more often)
- Tracks per-session score with streak multiplier
- Updates word mastery (attempts + correctCount) in Room after each attempt
- Awards points via shared-points `AwardPointsUseCase`
- Handles both keyboard and handwriting input modes
- Perfect list completion triggers bonus points

**Navigation:**
- Wire up: DicteeListScreen → DicteeWordEntryScreen → DicteePracticeScreen
- Back navigation at each level
- Pass listId as nav argument

- Commit: `feat(dictee): add dictée mode with word lists, practice, TTS, and handwriting`

### PR 10: Dictée tests
- ViewModel tests with Turbine for all state transitions:
  - Load words → state has words
  - Submit correct answer → feedback=Correct, streak increments, score increases
  - Submit incorrect answer → feedback=Incorrect, streak resets
  - Complete all words → navigate to results
  - Toggle input mode → state.inputMode changes
- `CheckSpellingUseCase` parameterized tests (section 15.2):
  - Exact match, case insensitive, accent lenient, accent strict, wrong letter
- Integration test: add word → start practice → submit answer → verify mastery updated in Room
- Commit: `test(dictee): add comprehensive tests for dictée mode`

## Verification

- All dictée screens render correctly in Preview
- TTS plays words in French, English, and German
- Handwriting canvas captures strokes and recognizes text
- Correct/incorrect flow works end-to-end
- Points are awarded and persisted
- `./gradlew testDebugUnitTest` passes
