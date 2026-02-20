# StudyBuddy — Android App

> A fun, animated study aid for kids (ages 6–10). Dictée, Speed Math, Avatar dress-up, Rewards Shop.
> Built with Kotlin + Jetpack Compose. Local-first with cloud migration hooks.

## Project Overview

StudyBuddy is a multi-module Android app targeting **minSdk 26, targetSdk 35, compileSdk 35**.
The full specification is in `docs/build-instructions.md`. Read it before making changes.
The screen designs are in `docs/prototype.jsx` (React component — view in browser for reference).
The original planning doc is in `docs/planning.md`.

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
- **CI/CD:** GitHub Actions (already configured in `.github/workflows/`)

## Architecture

- **Pattern:** Clean Architecture + MVI (Model-View-Intent)
- **Every feature ViewModel** follows: `State` (data class) + `Intent` (sealed interface) + `Effect` (sealed interface)
- **Repository pattern** with Hilt `@Binds`: all repos have a `Local*Repository` impl. When cloud sync is needed, create a `Cloud*Repository` and swap the binding in `core/core-data/src/.../di/RepositoryModule.kt` — zero UI changes.
- **Data flows:** Room DAOs return `Flow<T>` → Repositories expose `Flow<T>` → ViewModels collect into `StateFlow<State>`

## Module Structure

```
app/                          → App shell, nav host, Hilt entry point
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
- Minimum coverage: 80% overall, 90% for core-domain and shared-points

## Key Implementation Notes

- **Accent comparison:** Use `java.text.Normalizer.NFD` to strip diacritics for lenient mode. See `core-common/.../StringExtensions.kt`.
- **Math generation:** Division must have no remainder. Subtraction results must be non-negative. Power limited to base 2–5, exponent 1–3. No trivial problems (×0, ×1, +0) at medium+ difficulty.
- **Points:** All values defined in `core-common/.../PointValues.kt`. Streak multipliers: 0–4 = ×1.0, 5–9 = ×1.5, 10–19 = ×2.0, 20+ = ×3.0.
- **Localization:** 3 languages: French (fr), English (en), German (de). Use `res/values-{locale}/strings.xml`. Each dictée list stores its own language independently of the app locale.
- **Backup format:** JSON with schema version. See `docs/build-instructions.md` section 12 for the schema.
- **Cloud hooks:** Every repository interface has a `suspend fun sync()` that is currently a no-op. Do not remove it.

## File References

For detailed specifications on any module, read `docs/build-instructions.md`.
For UI/UX reference, open `docs/prototype.jsx` in a browser.
For the original planning document, see `docs/planning.md`.
