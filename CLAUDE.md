# StudyBuddy — Android App

> A fun, animated study aid for kids (ages 6–10). Dictée, Speed Math, Avatar dress-up, Rewards Shop.
> Built with Kotlin + Jetpack Compose. Local-first with cloud migration hooks.

## Project Status

This repository is in **pre-build / planning stage**. All specifications, screen designs, and phased build instructions live in `studybuddy-setup/`. No Android source code exists yet — it will be generated following the phased build plan.

## Specification Files

| File | Purpose |
|---|---|
| `studybuddy-setup/build-instructions.md` | Complete 2000+ line technical spec — **read before making any changes** |
| `studybuddy-setup/prototype.jsx` | React component rendering all screen designs (open in browser) |
| `studybuddy-setup/planning.md` | Architecture planning doc: tech choices, data model, animations |
| `studybuddy-setup/CLAUDE.md` | Original context file (to be copied into the built project) |
| `studybuddy-setup/.claude/commands/build-phase-*.md` | 6 phased slash commands for incremental app construction |

## Build Phases

The app is built in 6 phases, each producing multiple PRs:

| Phase | Slash Command | What It Builds |
|---|---|---|
| 1 — Foundation | `/project:build-phase-1` | Scaffold, CI, core-common, core-domain, core-data, core-ui, shared-points |
| 2 — Dictée | `/project:build-phase-2` | shared-tts, shared-ink, feature-dictee, dictée tests |
| 3 — Speed Math | `/project:build-phase-3` | feature-math (setup/play/results), math tests |
| 4 — Avatar & Rewards | `/project:build-phase-4` | feature-avatar, feature-rewards, purchase flow tests |
| 5 — Infrastructure | `/project:build-phase-5` | feature-settings, feature-backup, feature-stats, feature-onboarding |
| 6 — Integration & Polish | `/project:build-phase-6` | feature-home, app module, animations, localization, accessibility, release |

## Tech Stack

- **Language:** Kotlin 2.1+
- **UI:** Jetpack Compose with Material 3
- **DI:** Hilt (Dagger)
- **Database:** Room (local SQLite)
- **Async:** Kotlin Coroutines + Flow
- **TTS:** Android TextToSpeech (offline voice packs)
- **Handwriting:** ML Kit Digital Ink Recognition
- **Animations:** Lottie Compose + Compose Animation APIs
- **Serialization:** Kotlinx Serialization
- **Testing:** JUnit 5, Turbine, MockK, Compose UI Testing
- **Linting:** Detekt + Ktlint
- **Coverage:** Kover
- **CI/CD:** GitHub Actions (4 workflows: ci, release, dependencies, coverage)

### Key Dependency Versions

| Dependency | Version |
|---|---|
| AGP | 8.7.3 |
| Kotlin | 2.1.0 |
| KSP | 2.1.0-1.0.29 |
| Compose BOM | 2024.12.01 |
| Hilt | 2.53.1 |
| Room | 2.6.1 |
| Navigation | 2.8.5 |
| Coroutines | 1.9.0 |
| JUnit 5 | 5.11.4 |
| Turbine | 1.2.0 |
| MockK | 1.13.14 |

All versions are centralized in `gradle/libs.versions.toml` (created in Phase 1).

## Architecture

- **Pattern:** Clean Architecture + MVI (Model-View-Intent)
- **Package name:** `com.studybuddy.app`
- **SDK targets:** minSdk 26, targetSdk 35, compileSdk 35
- **Every feature ViewModel** follows: `State` (data class) + `Intent` (sealed interface) + `Effect` (sealed interface)
- **Repository pattern** with Hilt `@Binds`: all repos have a `Local*Repository` impl. When cloud sync is needed, create a `Cloud*Repository` and swap the binding in `core/core-data/src/.../di/RepositoryModule.kt` — zero UI changes.
- **Data flows:** Room DAOs return `Flow<T>` → Repositories expose `Flow<T>` → ViewModels collect into `StateFlow<State>`

### Why MVI

For a kid's app with animated state transitions (correct answer → confetti, wrong → encouragement), MVI gives a single state object per screen. This makes animations deterministic and testable.

## Module Structure

```
app/                          → App shell, nav host, Hilt entry point
build-logic/                  → Convention plugins (application, library, compose, feature, hilt, room, test)
core/core-ui/                 → Theme, design system, shared composables, animations
core/core-data/               → Room DB, entities, DAOs, repository impls, mappers
core/core-domain/             → Domain models, repository interfaces, use cases
core/core-common/             → Extensions, utils, constants, point values
feature/feature-home/         → Home screen
feature/feature-dictee/       → Dictée (word lists, practice, TTS, handwriting)
feature/feature-math/         → Speed Math (setup, play, results)
feature/feature-avatar/       → Avatar closet (character + accessories)
feature/feature-rewards/      → Rewards shop (4 tabs: avatar, themes, effects, titles)
feature/feature-stats/        → Progress charts, badges
feature/feature-settings/     → Settings, voice pack manager, parent zone
feature/feature-backup/       → Backup, restore, export (PDF, JSON, CSV)
feature/feature-onboarding/   → 3-step onboarding (name, avatar, TTS download)
feature/feature-poems/        → STUB — future poem mode
shared/shared-points/         → Points system logic + UI components
shared/shared-tts/            → TextToSpeech manager + voice pack downloads
shared/shared-ink/            → ML Kit Digital Ink handwriting recognition
```

### Convention Plugins (build-logic/)

| Plugin ID | Applied To |
|---|---|
| `studybuddy.android.application` | `app/` only |
| `studybuddy.android.library` | All library modules |
| `studybuddy.android.compose` | Compose-enabled modules |
| `studybuddy.android.feature` | Feature modules (library + compose + hilt + nav) |
| `studybuddy.android.hilt` | Modules requiring DI |
| `studybuddy.android.room` | `core-data` |
| `studybuddy.jvm.test` | All modules with JUnit 5 tests |

## Coding Standards

- Follow Kotlin coding conventions
- Max line length: 120 characters
- Use `data class` for all state objects
- Use `sealed interface` for intents and effects
- Prefer `when` over `if-else` chains
- Use named arguments for functions with 3+ parameters
- Stateless Compose composables preferred (state hoisting)
- Add `@Preview` to every screen composable
- Use `Modifier` as first optional parameter in composables
- Run `./gradlew detekt ktlintCheck` before committing

## Commit Convention

```
type(scope): description

Types: feat, fix, chore, docs, test, refactor, style, perf
Scopes: app, core, dictee, math, avatar, rewards, stats, settings, backup, tts, onboarding, ci
```

Examples:
- `feat(dictee): add handwriting input mode with ML Kit recognition`
- `fix(math): prevent division by zero in problem generation`
- `test(points): add parameterized tests for streak multiplier`

## Branch Strategy

```
main              ← production releases, protected
├── develop       ← integration branch, PR target
│   ├── feature/* ← feature branches
│   ├── fix/*     ← bug fixes
│   └── chore/*   ← maintenance, CI, deps
└── release/*     ← release candidates
```

## Build Commands

```bash
./gradlew assembleDebug                # Build debug APK
./gradlew testDebugUnitTest            # Run all unit tests
./gradlew lintDebug                    # Android lint
./gradlew detekt                       # Static analysis
./gradlew ktlintCheck                  # Code style
./gradlew koverXmlReportDebug          # Code coverage
./gradlew connectedDebugAndroidTest    # Instrumented tests (needs emulator)
./gradlew dependencyUpdates            # Check for dependency updates
```

## Testing Requirements

- Every use case must have unit tests (target: 90% coverage)
- Every ViewModel must have state transition tests using Turbine
- Math problem generation must be tested with `@RepeatedTest(100)` for constraint validation
- Dictée spelling comparison must use `@ParameterizedTest` with accent strict/lenient cases
- Room DAOs must be tested with in-memory database

### Coverage Targets

| Module | Minimum |
|---|---|
| `core-domain` (use cases) | 90% |
| `core-common` (utilities) | 95% |
| `shared-points` | 90% |
| `core-data` (repositories, mappers) | 80% |
| Feature ViewModels | 80% |
| UI composables | 60% |
| **Overall** | **80%** |

## Key Implementation Notes

### Accent Comparison
Use `java.text.Normalizer.NFD` to strip diacritics for lenient mode. See `core-common/.../StringExtensions.kt`.

### Math Problem Generation
- Division must have no remainder
- Subtraction results must be non-negative
- Power limited to base 2–5, exponent 1–3, max result 125
- No trivial problems (×0, ×1, +0) at medium+ difficulty

### Points System
All values defined in `core-common/.../PointValues.kt`.

| Action | Points |
|---|---|
| Dictée word correct (typed) | 10 |
| Dictée word correct (handwritten) | 15 |
| Dictée full list perfect | 50 |
| Math problem correct | 5 |
| Math 5-streak | 25 |
| Math 10-streak | 75 |
| Daily login | 10 |
| First session of the day | 20 |
| Weekly challenge complete | 100 |

Streak multipliers: 0–4 = ×1.0, 5–9 = ×1.5, 10–19 = ×2.0, 20+ = ×3.0.

### Localization
3 languages: French (fr), English (en), German (de). Use `res/values-{locale}/strings.xml`. Each dictée list stores its own language independently of the app locale.

### Room Database
8 entities: Profile, DicteeList, DicteeWord, MathSession, PointEvent, AvatarConfig, RewardItem, Badge. All use UUID primary keys and `updatedAt` timestamps for conflict resolution. `exportSchema = true`.

### Themes
6 app themes: Sunset (default/free), Ocean, Forest, Galaxy, Candy, Arctic. Typography: Nunito for headings, DM Sans for body.

### Cloud Migration Hooks
Every repository interface has a `suspend fun sync()` that is currently a no-op. **Do not remove it.** When cloud sync is needed, create a `Cloud*Repository` and swap the Hilt binding — zero UI changes.

### Backup Format
JSON with schema version. See `studybuddy-setup/build-instructions.md` section 12 for the full schema.

## CI/CD Workflows

| Workflow | Trigger | Purpose |
|---|---|---|
| `ci.yml` | Push/PR to main or develop | Lint, unit tests, build debug APK, instrumented tests (PR to main only) |
| `release.yml` | `v*` tags | Build signed APK + AAB, create GitHub Release |
| `dependencies.yml` | Weekly (Monday 8 AM UTC) | Check for dependency updates |
| `coverage.yml` | Push to develop | Kover coverage report, Codecov upload |

Required GitHub Secrets: `KEYSTORE_BASE64`, `KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD`, `FIREBASE_APP_ID` (optional).

## Important Warnings

- **Read `studybuddy-setup/build-instructions.md` in full** before starting any implementation work
- **Do not remove `sync()` stubs** from repository interfaces — they are cloud migration hooks
- **Do not add network dependencies** — the app is local-first; cloud sync comes later
- **All Room entities use UUID primary keys** — never use auto-increment
- **Points are cosmetic only** — no pay-to-win mechanics
- **Animations must respect** Android reduced-motion accessibility settings
- **Never punishing feedback** — wrong answers get gentle encouragement, never negative messaging
