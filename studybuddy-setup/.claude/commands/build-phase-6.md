Build Phase 6: Integration & Polish. Read `docs/build-instructions.md` sections 5, 15, 16, and 18 before starting.

Prerequisites: ALL previous phases (1–5) complete. Every module exists and compiles.

## What to build

### PR 20: feature-home (connect everything)
Build the Home screen from section 5:

**HomeScreen:**
- Header: avatar composite (tap → Avatar Closet) + greeting + star badge
- Greeting logic: locale-aware + time-of-day
  - French: "Bonjour" / "Bon après-midi" / "Bonsoir"
  - English: "Good morning" / "Good afternoon" / "Good evening"
  - German: "Guten Morgen" / "Guten Tag" / "Guten Abend"
- Streak banner: consecutive days with ≥1 session, dot indicators for week
- Daily challenge: progress bar (sessions today / daily goal), completion bonus info
- Mode cards grid (2×2): Dictée, Speed Math (active), Poems, More (locked with 🔒)
- Recent activity: last 5 sessions from PointEvents with mode, score, time ago

**HomeViewModel:**
- Load Profile from ProfileRepository
- Load AvatarConfig from AvatarRepository
- Load total points from PointsRepository
- Calculate streak from PointEvents (consecutive days)
- Count today's sessions for daily challenge progress
- Load recent activities (last 5 PointEvents with details)
- Daily challenge completion check: if goal reached and not yet awarded today → award bonus

**Navigation targets:**
- Dictée card → DicteeListScreen
- Speed Math card → MathSetupScreen
- Avatar tap → AvatarClosetScreen
- Star badge tap → StatsScreen

- Commit: `feat(home): add home screen with live data from all modules`

### PR 21: app module (navigation + DI wiring)
Wire everything together in the app module:

**StudyBuddyApplication:**
- `@HiltAndroidApp` application class
- Initialize LeakCanary (debug only)

**MainActivity:**
- `@AndroidEntryPoint`
- Set content to `StudyBuddyTheme` wrapping `StudyBuddyNavHost`
- Read theme selection from DataStore, pass to theme
- Edge-to-edge display setup

**StudyBuddyNavHost (in core-ui):**
- Define all routes as sealed class/object
- Navigation graph with Compose Navigation:
  - Splash → Onboarding (if not complete) or Home
  - Home → Dictée flow, Math flow, Avatar, Stats, Rewards, Settings
  - Settings → VoicePackManager, BackupExport
  - Bottom navigation: Home, Stats, Rewards, Avatar, Settings
- Screen transition animations:
  - Push: slide in from right
  - Pop: slide out to right
  - Bottom nav: crossfade
  - Modal: slide up from bottom

**Navigation routes:**
```
splash
onboarding
home
dictee/lists
dictee/words/{listId}
dictee/practice/{listId}
math/setup
math/play/{sessionConfig}
math/results/{sessionId}
avatar
rewards
stats
settings
settings/voice-packs
settings/backup
```

- Commit: `feat(app): wire navigation, DI, and app entry point`

### PR 22: Animation polish pass
Review and enhance animations across all screens:

- Home: mode cards have gentle floating/bob idle animation using `InfiniteTransition`
- Dictée: word card flip animation on reveal
- Math: numpad button press has scale + haptic feedback (Modifier.bounceClick)
- Math: timer bar color transitions smoothly (green → orange → red)
- Math: streak fire animation grows with streak level (Lottie or Compose Canvas)
- Points fly-up: text animates upward and fades out over 1s
- Correct answer: green glow pulse + scale from 1.0 → 1.1 → 1.0
- Incorrect answer: horizontal shake (3 cycles, 8dp amplitude, 400ms)
- Screen transitions: shared element transition for avatar between Home and Closet
- Celebration overlay: selection from purchased effects (confetti default)
- Badge unlock: gold shimmer animation + bounce
- Respect `Settings.Global.ANIMATOR_DURATION_SCALE` for accessibility (reduced motion)

- Commit: `style(ui): polish animations across all screens`

### PR 23: Localization
Add complete translations for all 3 languages:

Create `res/values/strings.xml` (English — default)
Create `res/values-fr/strings.xml` (French)
Create `res/values-de/strings.xml` (German)

**Strings to translate (ALL user-facing text):**
- Navigation labels (Home, Stats, Rewards, Avatar, Settings)
- Home screen (greeting templates, streak messages, daily challenge)
- Dictée (list management, practice instructions, feedback messages)
- Speed Math (setup labels, play UI, results)
- Avatar & Rewards (item names, purchase dialogs, tab labels)
- Stats (labels, badge names, trend descriptions)
- Settings (all section headers, option labels, parent zone)
- Backup (buttons, status messages, export descriptions)
- Onboarding (all 3 steps)
- Error messages, empty states, loading states
- Encouragement messages (randomized pool of 10+ per language)

**Localization testing:**
- Switch app language in Settings → verify all screens update
- Verify date/time formatting per locale
- Verify number formatting (1.234 vs 1,234)
- Verify TTS speaks in correct language per dictée list (independent of app locale)

- Commit: `feat(i18n): add French, English, and German translations`

### PR 24: Accessibility pass
- All images and icons have `contentDescription`
- All interactive elements have minimum 48dp touch targets
- Screen reader support: logical focus order on every screen
- Color contrast: verify all text meets WCAG AA (4.5:1 normal, 3:1 large)
- Reduced motion: check `ANIMATOR_DURATION_SCALE`, disable non-essential animations
- Font scaling: test with system font size at 200% — verify no text clipping
- Keyboard navigation: verify all interactive elements are reachable
- TalkBack testing: navigate through Dictée practice and Math play flows

- Commit: `feat(a11y): add content descriptions, touch targets, and reduced motion support`

### PR 25: Final QA + release pipeline test
- Run full test suite: `./gradlew testDebugUnitTest` — all green
- Run lint: `./gradlew lintDebug` — no errors
- Run Detekt: `./gradlew detekt` — passes
- Run Ktlint: `./gradlew ktlintCheck` — passes
- Run coverage: `./gradlew koverXmlReportDebug` — verify minimums met
- Build release APK: `./gradlew assembleRelease` (needs signing config)
- Test release pipeline: create tag `v0.1.0-rc1`, verify GitHub Actions runs
- Manual QA checklist:
  - [ ] Fresh install: onboarding flow completes
  - [ ] Dictée: add list, add words, practice (keyboard + handwriting), scoring works
  - [ ] Speed Math: all operators, timer, streaks, results
  - [ ] Avatar: select character, equip accessories, purchase with stars
  - [ ] Rewards: buy theme, effect, sound — changes apply
  - [ ] Stats: data is accurate after sessions
  - [ ] Backup: create, restore on fresh install
  - [ ] Export: PDF opens, CSV valid
  - [ ] Settings: all toggles persist, language switch works
  - [ ] Voice packs: download, test, remove
  - [ ] Parent PIN: set, lock, unlock
  - [ ] Screen time: limit enforced
  - [ ] 3 languages: switch and verify
  - [ ] Orientation: portrait locked (verify in manifest)
  - [ ] Dark mode: handled gracefully (or disabled)
- Merge to main, tag `v1.0.0`

- Commit: `chore(release): final QA pass and v1.0.0 release preparation`

## Done! 🎉

The app is feature-complete and ready for internal testing or Play Store submission.
