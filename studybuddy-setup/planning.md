# 📚 StudyBuddy — App Planning Document

> A fun, animated study aid for kids — Dictée, Speed Math, and more.

---

## 1. Technology Stack

### Why Kotlin + Jetpack Compose

| Concern | Recommendation | Rationale |
|---|---|---|
| **Language** | Kotlin | Google's official Android language; modern, concise, null-safe |
| **UI Framework** | Jetpack Compose | Declarative UI, first-class animation APIs, Material 3 built-in, Google's future direction |
| **Alt considered** | Flutter / KMP | Both viable, but Compose is the most native, best-supported path for Android-only today with the easiest upgrade path later |

Jetpack Compose is the safest long-term bet for a native Android app. It has deep integration with the Android ecosystem, excellent tooling in Android Studio, and Google is actively migrating all their own apps to it.

### Core Libraries

| Layer | Library | Purpose |
|---|---|---|
| DI | Hilt | Standard dependency injection; makes swapping data sources trivial |
| Local DB | Room | SQLite abstraction with coroutines/Flow support; migration-friendly |
| Navigation | Compose Navigation | Type-safe nav with animation transitions |
| Animations | Lottie Compose + Compose Animation APIs | Lottie for rich reward animations; Compose APIs for micro-interactions |
| TTS | Android TextToSpeech | Built-in, supports FR/EN/DE out of the box |
| Handwriting | ML Kit Digital Ink Recognition | On-device stylus/finger writing recognition; supports all 3 languages |
| Internationalization | Android Resource system (`strings.xml`) | Native i18n with plurals, per-locale formatting |
| Async | Kotlin Coroutines + Flow | Reactive data streams from Room → UI |
| Testing | JUnit 5, Turbine, Compose Testing | Unit + UI testing |
| Build | Gradle with version catalogs | Centralized dependency management |

---

## 2. Architecture

### Clean Architecture + MVI

```
┌─────────────────────────────────────────────────┐
│                   UI Layer                       │
│  Compose Screens → ViewModels (MVI: State +     │
│  Intent + Effect)                                │
├─────────────────────────────────────────────────┤
│                 Domain Layer                     │
│  Use Cases · Domain Models · Repository          │
│  Interfaces                                      │
├─────────────────────────────────────────────────┤
│                  Data Layer                       │
│  ┌──────────────┐    ┌──────────────────────┐   │
│  │ LocalSource  │    │ RemoteSource (stub)   │   │
│  │ Room DB      │    │ Interface only, no    │   │
│  │              │    │ implementation yet     │   │
│  └──────────────┘    └──────────────────────┘   │
│         └────────┬────────┘                      │
│          RepositoryImpl                          │
│  (reads from local; cloud flag for future sync)  │
└─────────────────────────────────────────────────┘
```

### Why MVI over MVVM

For a kid's app with animated state transitions (correct answer → confetti, wrong → encouragement), MVI gives us a **single state object per screen**. This makes animations deterministic and testable:

```kotlin
// Example: SpeedMath screen state
data class SpeedMathState(
    val currentProblem: MathProblem?,
    val userAnswer: String,
    val feedback: Feedback?,        // Correct, Incorrect, TimeUp
    val score: Int,
    val streak: Int,
    val timeRemaining: Duration,
    val difficulty: Difficulty
)

sealed interface SpeedMathIntent {
    data class DigitEntered(val digit: Int) : SpeedMathIntent
    data object Submit : SpeedMathIntent
    data object NextProblem : SpeedMathIntent
    data object Pause : SpeedMathIntent
}
```

### Cloud Migration Path

The repository pattern makes this straightforward:

```kotlin
// Define the contract
interface WordRepository {
    fun getWords(listId: String): Flow<List<Word>>
    suspend fun saveWord(word: Word)
    suspend fun sync()  // no-op locally, real sync later
}

// Current implementation
class LocalWordRepository(
    private val dao: WordDao
) : WordRepository {
    override suspend fun sync() { /* no-op */ }
}

// Future implementation
class CloudWordRepository(
    private val dao: WordDao,
    private val api: StudyBuddyApi,       // Retrofit/Ktor
    private val syncManager: SyncManager   // conflict resolution
) : WordRepository {
    override suspend fun sync() { /* real sync */ }
}
```

Swap via Hilt module — zero UI changes needed.

When choosing a cloud backend later, top candidates would be:
- **Firebase/Firestore** — easiest for offline-first sync, generous free tier
- **Supabase** — open-source Firebase alternative, PostgreSQL-based
- **Custom API + Room sync** — full control, more work

---

## 3. Data Model

### Room Database Schema

```
┌──────────────┐     ┌──────────────────┐     ┌────────────────┐
│   Profile     │     │   DicteeList     │     │   DicteeWord   │
│──────────────│     │──────────────────│     │────────────────│
│ id (PK)      │──┐  │ id (PK)          │──┐  │ id (PK)        │
│ name         │  │  │ profileId (FK)   │  │  │ listId (FK)    │
│ avatarRes    │  │  │ title            │  │  │ word           │
│ locale       │  │  │ language         │  │  │ mastered       │
│ totalPoints  │  │  │ createdAt        │  │  │ attempts       │
│ createdAt    │  │  │ updatedAt        │  │  │ correctCount   │
└──────────────┘  │  └──────────────────┘  │  │ lastAttemptAt  │
                  │                         │  └────────────────┘
                  │  ┌──────────────────┐   │
                  │  │ MathSession      │   │  ┌────────────────┐
                  └──│──────────────────│   │  │ PointEvent     │
                     │ id (PK)          │   │  │────────────────│
                     │ profileId (FK)   │   │  │ id (PK)        │
                     │ operators []     │   │  │ profileId (FK) │
                     │ numberRange      │   │  │ source (enum)  │
                     │ totalProblems    │   │  │ points         │
                     │ correctCount     │   │  │ reason         │
                     │ bestStreak       │   │  │ timestamp      │
                     │ avgResponseMs    │   │  └────────────────┘
                     │ completedAt      │
                     └──────────────────┘

Future:
┌──────────────────┐
│   Poem           │
│──────────────────│
│ id (PK)          │
│ profileId (FK)   │
│ title            │
│ text             │
│ language         │
│ audioUri         │
│ bestScore        │
└──────────────────┘
```

### Type Converters

```kotlin
// Store operator lists as JSON in Room
class Converters {
    @TypeConverter
    fun fromOperators(ops: Set<Operator>): String = Json.encodeToString(ops)
    
    @TypeConverter
    fun toOperators(json: String): Set<Operator> = Json.decodeFromString(json)
}
```

---

## 4. Point System — "⭐ Star Points"

### Earning Points

| Action | Points | Bonus |
|---|---|---|
| Dictée word correct (typed) | 10 | — |
| Dictée word correct (handwritten) | 15 | Harder input → more reward |
| Dictée full list perfect | 50 | Completion bonus |
| Math problem correct | 5 | — |
| Math 5-streak | 25 | Streak multiplier |
| Math 10-streak | 75 | Bigger streak bonus |
| Daily login | 10 | Consistency reward |
| First session of the day | 20 | Encouragement to start |
| Weekly challenge complete | 100 | Long-term engagement |

### Spending / Unlocking

Points unlock cosmetic rewards — no pay-to-win, purely motivational:

- **Avatar accessories** — hats, glasses, pets (every 100 pts)
- **App themes** — color schemes, backgrounds (every 250 pts)
- **Celebration animations** — new confetti styles, character dances (every 500 pts)
- **Titles** — "Math Wizard", "Word Champion", "Super Scholar" (milestones)

### Streak & Multiplier System

```
Base points × Streak multiplier

Streak 0–4:   ×1.0
Streak 5–9:   ×1.5
Streak 10–19: ×2.0
Streak 20+:   ×3.0
```

Visual feedback: the streak counter animates and glows more intensely as it grows. Breaking a streak shows a gentle "Try again!" — never punishing.

---

## 5. Screen-by-Screen Breakdown

### 5.1 Home Screen

```
┌─────────────────────────────┐
│  👋 Bonjour, Léa!     ⭐ 340│
│                              │
│  ┌──────────┐ ┌──────────┐  │
│  │  📝      │ │  🔢      │  │
│  │ Dictée   │ │  Speed   │  │
│  │          │ │  Math    │  │
│  └──────────┘ └──────────┘  │
│                              │
│  ┌──────────┐ ┌──────────┐  │
│  │  📖      │ │  🏆      │  │
│  │ Poems    │ │  My      │  │
│  │ (Soon!)  │ │  Stats   │  │
│  └──────────┘ └──────────┘  │
│                              │
│  🔥 3-day streak!           │
│  Today's challenge: ██░░ 40%│
└─────────────────────────────┘
```

- Greeting adapts to time of day and locale
- Mode cards have subtle idle animations (floating, gentle bob)
- Future modes show a "Coming Soon" lock with a playful animation
- Daily challenge progress bar

### 5.2 Dictée — Word Entry

```
┌─────────────────────────────┐
│  ← Dictée         + New List│
│                              │
│  📋 Ma liste du lundi       │
│  ─────────────────────────  │
│  + Add a word...            │
│                              │
│  maison          ✓  ✕       │
│  papillon        ✓  ✕       │
│  bibliothèque    ✓  ✕       │
│  extraordinaire  ✓  ✕       │
│                              │
│  ┌──────────────────────┐   │
│  │  ▶  Start Practice   │   │
│  └──────────────────────┘   │
│                              │
│  Stats: 4 words · 2 mastered│
└─────────────────────────────┘
```

- Words can be typed or voice-entered
- Each word shows mastery indicator (color dot: red → yellow → green)
- Swipe to delete with undo

### 5.3 Dictée — Practice Mode

```
┌─────────────────────────────┐
│  Word 3 of 8        ⭐ +10  │
│                              │
│       🔊 (tap to replay)    │
│                              │
│  ┌─────────────────────┐    │
│  │                     │    │
│  │  [writing canvas]   │    │
│  │  or keyboard input  │    │
│  │                     │    │
│  └─────────────────────┘    │
│                              │
│  ⌨️ / ✏️  toggle input mode │
│                              │
│  ┌──────────────────────┐   │
│  │     ✓  Check         │   │
│  └──────────────────────┘   │
│                              │
│  ██████████░░░░ 3/8         │
└─────────────────────────────┘
```

**Flow:**
1. App plays the word via TTS (language-aware)
2. Child taps 🔊 to replay (unlimited)
3. Types on keyboard OR writes with stylus on canvas
4. If handwritten → ML Kit recognizes text
5. On submit: compare with stored word
6. Correct → confetti + points + cheerful sound
7. Incorrect → show correct spelling, gentle encouragement, option to retry

**Tolerance for handwriting:**
- Accent-aware comparison (configurable strictness)
- Option: "Accept without accents" for younger learners

### 5.4 Speed Math — Setup

```
┌─────────────────────────────┐
│  ← Speed Math               │
│                              │
│  Operators                   │
│  ┌───┬───┬───┬───┬───┐     │
│  │ + │ - │ × │ ÷ │ ^ │     │
│  │ ✓ │ ✓ │   │   │   │     │
│  └───┴───┴───┴───┴───┘     │
│                              │
│  Number Range                │
│  ┌──┐         ┌──┐          │
│  │1 │───●─────│12│          │
│  └──┘         └──┘          │
│                              │
│  Time per problem            │
│  ○ 10s  ● 15s  ○ 30s  ○ ∞  │
│                              │
│  Number of problems          │
│  ○ 10   ● 20   ○ 50         │
│                              │
│  ┌──────────────────────┐   │
│  │     ▶  Go!            │   │
│  └──────────────────────┘   │
└─────────────────────────────┘
```

**Smart problem generation:**
- Ensures solvable problems (no division remainders unless configured)
- Gradually increases difficulty within a session based on performance
- Avoids trivial problems (×0, ×1, +0) unless at lowest difficulty
- Power (^) limited to sensible ranges (e.g., 2^1 through 5^3)

### 5.5 Speed Math — Play

```
┌─────────────────────────────┐
│  ⭐ 45    🔥 Streak: 7  ×1.5│
│                              │
│  ━━━━━━━━━━░░░░  (timer)    │
│                              │
│         12 + 7 = ?           │
│                              │
│      ┌──────────┐           │
│      │    19    ←│           │
│      └──────────┘           │
│                              │
│  ┌─┬─┬─┐                   │
│  │1│2│3│    Number pad      │
│  ├─┼─┼─┤    (big, friendly  │
│  │4│5│6│     touch targets) │
│  ├─┼─┼─┤                   │
│  │7│8│9│                   │
│  ├─┼─┼─┤                   │
│  │⌫│0│✓│                   │
│  └─┴─┴─┘                   │
│                              │
│  Problem 4 of 20            │
└─────────────────────────────┘
```

**Feedback animations:**
- Correct: Number pops with green glow, points fly up, streak counter pulses
- Wrong: Gentle red shake, correct answer shown briefly, encouraging message
- Streak milestone (5, 10, 20): Special celebration animation
- Timer running low: bar turns orange → red with subtle pulse

### 5.6 Stats & Progress

```
┌─────────────────────────────┐
│  ← My Progress              │
│                              │
│  ⭐ 1,240 total points      │
│  🔥 Best streak: 14 days    │
│                              │
│  This Week                   │
│  ┌─────────────────────┐    │
│  │ █                   │ M  │
│  │ ███                 │ T  │
│  │ ██                  │ W  │
│  │ ████                │ T  │
│  │ █████               │ F  │
│  │                     │ S  │
│  │ ██                  │ S  │
│  └─────────────────────┘    │
│                              │
│  Dictée accuracy: 78% → 89% │
│  Math avg speed: 8.2s → 5.1s│
│                              │
│  🏅 Badges                   │
│  [⭐ First 100] [🔥 7-day]  │
│  [📝 50 words] [🔢 Speed!]  │
└─────────────────────────────┘
```

---

## 6. Localization Strategy

### Resource Structure

```
res/
├── values/           # Default (English)
│   └── strings.xml
├── values-fr/        # French
│   └── strings.xml
├── values-de/        # German
│   └── strings.xml
```

### Language-Sensitive Components

| Component | Handling |
|---|---|
| UI strings | Standard Android `strings.xml` per locale |
| TTS engine | Set `Locale` on `TextToSpeech` per dictée list language |
| ML Kit ink model | Download FR/EN/DE models on first use |
| Number formatting | `NumberFormat.getInstance(locale)` |
| Keyboard hints | Locale-aware input type |
| Date/Time | `DateTimeFormatter` with locale |

### Per-List Language

Each dictée list stores its language independently. A child studying French at a German school can have FR dictée lists while the app UI is in German.

---

## 7. Animation Strategy

### Library Choices

| Animation Type | Tool | Example |
|---|---|---|
| Screen transitions | Compose Navigation animations | Slide-in, fade, shared element |
| Micro-interactions | Compose `animate*AsState` | Button press, toggle, input focus |
| Reward celebrations | Lottie | Confetti, star burst, character dance |
| Progress indicators | Compose Canvas + `Animatable` | Streak fire, timer bar |
| Idle / ambient | Compose `InfiniteTransition` | Floating cards on home, gentle pulse |

### Animation Principles (for kids)

1. **Rewarding, not distracting** — big celebrations for success, subtle ambient otherwise
2. **Never punishing** — wrong answers get gentle, encouraging feedback
3. **Responsive** — every tap has immediate visual feedback (< 100ms)
4. **Skippable** — long animations can be tapped through
5. **Reduced motion** — respect Android accessibility settings

---

## 8. Module Structure

```
study-buddy/
├── app/                          # App module, DI setup, navigation
├── core/
│   ├── core-ui/                  # Theme, shared composables, animations
│   ├── core-data/                # Room DB, DAOs, base repository
│   ├── core-domain/              # Shared domain models, use cases
│   └── core-common/              # Extensions, utils, constants
├── feature/
│   ├── feature-home/             # Home screen
│   ├── feature-dictee/           # Dictée mode (entry + practice)
│   ├── feature-math/             # Speed Math (setup + play)
│   ├── feature-stats/            # Progress & badges
│   ├── feature-profile/          # Avatar, settings, language
│   └── feature-poems/            # (Future) Poem mode
└── shared/
    ├── shared-points/            # Point system logic & UI components
    ├── shared-tts/               # TextToSpeech wrapper
    └── shared-ink/               # ML Kit handwriting wrapper
```

**Why multi-module:**
- Faster incremental builds
- Enforced separation of concerns
- Easy to add new feature modules (Poems, etc.)
- Shared modules prevent duplication

---

## 9. Future: Poems Mode — Architecture Prep

Even though Poems is a future feature, the architecture accounts for it:

| Concern | Preparation |
|---|---|
| OCR / Scanning | ML Kit Text Recognition (same SDK family as Ink) |
| Audio recording | `MediaRecorder` → saved as local file |
| Pronunciation grading | Google Speech-to-Text → compare phoneme alignment (or a dedicated API like Speechace) |
| Poem storage | `Poem` entity already sketched in data model |
| UI | New `feature-poems` module, plugs into existing nav |

---

## 10. Development Phases

### Phase 1 — Foundation (2–3 weeks)
- Project setup, module structure, Hilt DI
- Room database with all entities
- Core theme, typography, color system (Material 3 dynamic color)
- Profile creation flow
- Home screen with navigation shell
- Point system core logic + shared UI (animated counter)
- Lottie reward animations integrated

### Phase 2 — Dictée (2–3 weeks)
- Word list CRUD (create, edit, delete lists and words)
- TTS integration with FR/EN/DE
- Keyboard practice mode with scoring
- ML Kit handwriting canvas + recognition
- Spelling comparison engine (accent tolerance)
- Points integration + feedback animations

### Phase 3 — Speed Math (2 weeks)
- Setup screen (operators, range, timer, count)
- Problem generation engine (smart, no trivial/impossible)
- Play screen with numpad, timer, streaks
- Results summary screen
- Points integration + streak multipliers

### Phase 4 — Stats & Polish (1–2 weeks)
- Progress charts (Compose Canvas or Vico charts)
- Badge/achievement system
- Daily challenge system
- Settings (language, accessibility, data export)
- Final animations pass, haptic feedback
- QA and device testing

### Phase 5 — Future
- Poems mode
- Cloud sync (Firebase or Supabase)
- Parent dashboard / progress reports
- Multiplayer math challenges

---

## 11. Key Design Decisions Summary

| Decision | Choice | Rationale |
|---|---|---|
| Native vs Cross-platform | Native (Compose) | Best animation support, TTS/ML Kit integration, Google's future |
| State management | MVI | Single state = deterministic animations, easy testing |
| Data layer | Repository pattern | Swap local ↔ cloud with zero UI changes |
| Handwriting | ML Kit Digital Ink | On-device, free, supports FR/EN/DE |
| Animations | Lottie + Compose | Lottie for rich rewards, Compose for everything else |
| Multi-module | Yes | Scalability, build speed, separation |
| Min SDK | 26 (Android 8.0) | Covers 95%+ of devices, unlocks modern APIs |

---

## 12. Tooling & Quality

| Tool | Purpose |
|---|---|
| Android Studio Hedgehog+ | IDE |
| Detekt + Ktlint | Code style & static analysis |
| LeakCanary | Memory leak detection (kids tap a LOT) |
| Firebase Crashlytics | Crash reporting (free, no data sync needed) |
| Maestro or Compose UI tests | Automated UI testing |
| GitHub Actions | CI/CD pipeline |

---

*Document version: 1.0 — February 2026*
