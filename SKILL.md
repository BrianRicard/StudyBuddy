---
name: "studybuddy"
description: "Conventions, architecture, and pitfalls for the StudyBuddy Android app (Kotlin + Jetpack Compose, kids ages 6–10)."
domain: "android-kotlin-compose"
confidence: "high"
source: "Derived from CLAUDE.md, README.md, settings.gradle.kts, studybuddy-setup/ specs."
---

## Context

StudyBuddy is a local-first Android study app for kids 6–10 (Dictée, Speed Math, Avatar dress-up, Rewards Shop, Reading, Poems). Apply this skill whenever working in `C:\src\StudyBuddy\StudyBuddy` — adding features, fixing crashes, writing tests, or modifying CI. The codebase is a Gradle multi-module project (Kotlin 2.1, AGP 8.7.3, Compose BOM 2024.12.01) using Clean Architecture + MVI with Hilt and Room.

Authoritative spec: `CLAUDE.md`. Setup spec: `README.md`. Phase build commands: `studybuddy-setup/.claude/commands/build-phase-*.md`.

## Patterns

### Module Layout
- `app/` — application module (`studybuddy.android.application` plugin only here).
- `core/{core-common, core-domain, core-data, core-ui}` — pure Kotlin / Android library modules.
- `feature/feature-*` — each screen domain (home, dictee, math, avatar, rewards, stats, settings, backup, onboarding, poems, reading). Use the `studybuddy.android.feature` convention plugin.
- `shared/{shared-points, shared-tts, shared-ink, shared-whisper}` — cross-feature helpers (points engine, TTS, ML Kit ink, Whisper STT).
- `build-logic/convention/` — convention plugins. Never apply Android plugins directly; apply `studybuddy.android.*` instead.
- All version coordinates live in `gradle/libs.versions.toml`. Never hardcode versions in module `build.gradle.kts`.

### Architecture (MVI + Clean)
- Each feature ViewModel exposes: `data class State`, `sealed interface Intent`, `sealed interface Effect`. UI sends `Intent`s; VM emits `StateFlow<State>` and `SharedFlow<Effect>`.
- Data flow: Room DAO `Flow<T>` → Repository (`Flow<DomainModel>`) → UseCase → ViewModel `StateFlow<State>` → Compose `collectAsStateWithLifecycle()`.
- Repositories are interfaces in `core-domain`; implementations live in `core-data` as `Local*Repository` and bound via `@Binds` in `core/core-data/src/.../di/RepositoryModule.kt`. Cloud sync is added later by introducing a `Cloud*Repository` and swapping the binding — **do not change UI/VM code for sync**.
- Every repository interface keeps `suspend fun sync()` even when it is currently a no-op. It's the cloud migration hook.

### Room
- 8 baseline entities: Profile, DicteeList, DicteeWord, MathSession, PointEvent, AvatarConfig, RewardItem, Badge.
- All primary keys are `String` UUIDs (`UUID.randomUUID().toString()`). Never use auto-increment.
- Every entity has an `updatedAt: Long` (epoch ms) for conflict resolution.
- `exportSchema = true`; schemas committed under `core/core-data/schemas/`. Bumping the DB version requires a migration class + schema diff.

### Compose / UI
- Material 3 only. Stateless composables preferred; hoist state to ViewModels.
- `Modifier` is the first optional parameter on every public composable.
- Every screen-level composable has an `@Preview`.
- Animations must respect `AccessibilityManager.isAnimationsEnabled` / reduced-motion settings.
- 6 themes: Sunset (default/free), Ocean, Forest, Galaxy, Candy, Arctic. Typography: Nunito (headings) + DM Sans (body).

### Points System (`shared-points` + `core-common/PointValues.kt`)
- Awards: dictée typed=10, dictée handwritten=15, dictée perfect list=50, math correct=5, 5-streak=25, 10-streak=75, daily login=10, first session of day=20, weekly challenge=100.
- Streak multipliers: 0–4 ×1.0, 5–9 ×1.5, 10–19 ×2.0, 20+ ×3.0.
- Points are cosmetic only — **never gate gameplay or learning content** behind point spend.

### Localization
- Three locales: `fr`, `en`, `de`. Strings live in `res/values-{locale}/strings.xml`.
- Each `DicteeList` carries its own `language` field independent of the app locale (a French list still speaks French TTS even if the UI is in English).

### Accent Comparison (Dictée)
- Use `java.text.Normalizer.NFD` + strip combining marks for *lenient* mode in `core-common/.../StringExtensions.kt`. Strict mode compares raw strings. Always test both via `@ParameterizedTest`.

### Math Problem Generation (`feature-math`)
- Division must have integer remainder 0.
- Subtraction results must be ≥ 0.
- Powers: base 2–5, exponent 1–3, max result 125.
- At medium+ difficulty, exclude trivial operands (`×0`, `×1`, `+0`).
- Validate constraints with `@RepeatedTest(100)`.

### Testing
- JUnit 5 + Turbine + MockK. In-memory Room DB for DAO tests. Compose UI tests on instrumented runner.
- Coverage minimums (Kover): core-domain 90%, core-common 95%, shared-points 90%, core-data 80%, feature VMs 80%, UI 60%, overall 80%.
- Run before commit: `./gradlew detekt ktlintCheck testDebugUnitTest`.

### Git / Commits / Branches
- Conventional commits: `type(scope): description`. Types: feat, fix, chore, docs, test, refactor, style, perf. Scopes: app, core, dictee, math, avatar, rewards, stats, settings, backup, tts, onboarding, ci.
- Branch flow: `main` (protected) ← `develop` ← `feature/*` | `fix/*` | `chore/*`. Releases on `release/*` and `v*` tags.
- Crash-report fixes go on `fix/crash-<fingerprint>` branches.

### CI/CD (`.github/workflows/`)
- `ci.yml` — lint + unit + debug APK on push/PR to main|develop; instrumented tests only on PRs to main.
- `release.yml` — signed APK + AAB on `v*` tags. Required secrets: `KEYSTORE_BASE64`, `KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD`.
- `dependencies.yml` — weekly Mon 08:00 UTC dep check.
- `coverage.yml` — Kover + Codecov on push to develop.

### Toolchain
- **JDK 17 only.** AGP 8.7.3 breaks on JDK 21 (`validateSigningDebug` → `NoClassDefFoundError: org/bouncycastle/asn1/edec/EdECObjectIdentifiers`).
- minSdk 26, targetSdk 35, compileSdk 35.
- Emulator for instrumented tests: `system-images;android-31;google_apis;x86_64`, KVM required.

## Examples

### Adding a feature module
1. Create `feature/feature-<name>/build.gradle.kts` applying `studybuddy.android.feature`.
2. Add `include(":feature:feature-<name>")` to `settings.gradle.kts`.
3. Define `<Name>State` (data class), `<Name>Intent` (sealed interface), `<Name>Effect` (sealed interface).
4. Inject use cases via Hilt `@HiltViewModel`. Collect repository `Flow`s with `viewModelScope`.
5. Provide `@Preview` for each top-level screen composable.

### Fixing a crash report (issue labeled `crash-report`)
1. Read stacktrace + Custom Data (last_section, difficulty, language, db_version).
2. For Room migration crashes, diff `core/core-data/schemas/<entity>/<from>.json` vs `<to>.json`.
3. Branch: `fix/crash-<fingerprint>` from `develop`.
4. Surgical fix only. Add a regression test reproducing the crash.
5. PR references the crash issue number.

## Anti-Patterns

- **Don't** remove `suspend fun sync()` from any repository interface — it's the cloud migration hook.
- **Don't** add network/HTTP dependencies. The app is local-first; cloud sync ships later behind a different repository binding.
- **Don't** use auto-increment primary keys — every entity uses UUID strings.
- **Don't** apply `com.android.library` / `com.android.application` directly in module scripts — use `studybuddy.android.*` convention plugins.
- **Don't** hardcode dependency versions in module `build.gradle.kts` — add to `gradle/libs.versions.toml`.
- **Don't** modify the reward/points formula or rename avatar asset filenames without explicit approval (DB rows reference assets by name).
- **Don't** rename or remove DataStore / SharedPreferences keys without a written migration.
- **Don't** ship punishing or negative feedback for wrong answers — encouragement only.
- **Don't** introduce pay-to-win mechanics — points are cosmetic.
- **Don't** build with JDK 21. Use JDK 17.
- **Don't** ignore reduced-motion accessibility settings when adding animations.
- **Don't** skip `@Preview` on screen composables or `Modifier` as first optional param.

## See Also

- `CLAUDE.md` — full technical spec (read before any non-trivial change).
- `README.md` — local toolchain setup (JDK 17, Android SDK, emulator).
- `studybuddy-setup/build-instructions.md` — 2000+ line ground-truth build doc.
- `studybuddy-setup/.claude/commands/build-phase-{1..6}.md` — phased build slash commands.
- `detekt.yml`, `lint.xml`, `owasp-suppressions.xml` — static analysis configs.
