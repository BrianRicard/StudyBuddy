Build Phase 1: Foundation. Read `docs/build-instructions.md` in full before starting.

## What to build

### PR 1: Project scaffold + CI
1. Initialize the Android project with package `com.studybuddy.app`, minSdk 26, targetSdk 35, compileSdk 35.
2. Create `gradle/libs.versions.toml` exactly as specified in section 3.3 of the build instructions.
3. Create root `build.gradle.kts` and `settings.gradle.kts` with all modules listed in section 3.5.
4. Create the `build-logic/` convention plugins described in section 3.6:
   - `studybuddy.android.application`
   - `studybuddy.android.library`
   - `studybuddy.android.compose`
   - `studybuddy.android.feature` (library + compose + hilt + nav)
   - `studybuddy.android.hilt`
   - `studybuddy.android.room`
   - `studybuddy.jvm.test` (JUnit 5)
5. Copy the 4 GitHub Actions workflow files from section 2 into `.github/workflows/`.
6. Add `detekt.yml` config and `.editorconfig`.
7. Create the `.github/pull_request_template.md` from section 18.5.
8. Create stub `build.gradle.kts` for EVERY module listed in settings.gradle.kts using the appropriate convention plugin.
9. Verify the project compiles: `./gradlew assembleDebug`
10. Commit: `chore(ci): scaffold project structure with multi-module setup`

### PR 2: core-common
Build everything in section 4.1:
- `StringExtensions.kt` with `matchesWord()` (accent-aware comparison)
- `FlowExtensions.kt`
- `DateTimeExtensions.kt`
- `Result.kt` sealed class (Success, Error, Loading)
- `PointValues.kt` with ALL point constants and `streakMultiplier()`
- `AppConstants.kt`
- `SupportedLocale.kt` enum (FR, EN, DE)
- `DispatchersModule.kt` (Hilt module providing IO, Default, Main)
- Unit tests for `matchesWord()` (parameterized, see section 15.2) and `streakMultiplier()`
- Commit: `feat(core): add core-common with extensions, constants, and utilities`

### PR 3: core-domain
Build everything in section 4.2:
- ALL domain models in `model/` (Profile, DicteeList, DicteeWord, MathSession, MathProblem, PointEvent, AvatarConfig, RewardItem, Badge, Operator, Difficulty, Feedback, InputMode, VoicePack)
- ALL repository interfaces in `repository/` (9 interfaces, each with a `suspend fun sync()`)
- ALL use cases in `usecase/` (dictee, math, points, avatar, backup groups)
- Unit tests for `GenerateProblemUseCase` (section 15.2: division no remainder, subtraction non-negative, power range)
- Unit tests for `CheckSpellingUseCase`
- Commit: `feat(core): add core-domain with models, repository interfaces, and use cases`

### PR 4: core-data
Build everything in section 4.3:
- ALL Room entities (8 entities)
- `Converters.kt` for enums, sets, kotlinx-datetime
- ALL DAOs (7 DAOs with Flow return types)
- `StudyBuddyDatabase.kt` with `exportSchema = true`
- ALL repository implementations (9 Local*Repository classes)
- Mapper classes (Entity <-> Domain)
- `DatabaseModule.kt` and `RepositoryModule.kt` Hilt modules
- Stub `RemoteDataSource.kt` interface
- `BackupManager.kt`, `PdfReportGenerator.kt`, `CsvExporter.kt` stubs
- Room database tests with in-memory DB (insert/retrieve for each DAO)
- Commit: `feat(core): add core-data with Room database, DAOs, and repository implementations`

### PR 5: core-ui
Build everything in section 4.4:
- `Theme.kt` with `StudyBuddyTheme` composable supporting 6 color themes (Sunset, Ocean, Forest, Galaxy, Candy, Arctic)
- `Color.kt` with color tokens for each theme
- `Type.kt` typography (Nunito for headings, DM Sans for body)
- `Shape.kt` rounded corner system
- ALL shared components: StudyBuddyCard, StudyBuddyButton, PointsBadge, StreakIndicator, ProgressBar, AvatarComposite, EmptyState, LoadingState, ErrorState
- ALL animation composables: CelebrationOverlay, CorrectAnswerAnimation, IncorrectAnimation, StreakFireAnimation, PointsFlyUp
- Navigation: StudyBuddyNavHost
- Modifiers: BounceClick, ShakeAnimation
- `@Preview` for every component
- Commit: `feat(core): add core-ui with design system, theme, components, and animations`

### PR 6: shared-points
Build the shared-points module:
- Points calculation logic (apply streak multiplier, sum bonuses)
- `AwardPointsUseCase` integration with PointsRepository
- `PointsBadge` composable (animated star counter)
- `StreakIndicator` composable (fire that grows with streak)
- `PointsFlyUp` animation composable
- Unit tests for all point calculations
- Commit: `feat(shared): add shared-points with calculation logic and UI components`

## Verification

After all 6 PRs are merged to develop:
- `./gradlew assembleDebug` passes
- `./gradlew testDebugUnitTest` passes with all tests green
- `./gradlew lintDebug` has no errors
- `./gradlew detekt` passes
- `./gradlew ktlintCheck` passes
