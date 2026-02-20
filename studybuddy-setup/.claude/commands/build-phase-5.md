Build Phase 5: Infrastructure. Read `docs/build-instructions.md` sections 10, 11, 12, 13, and 14 before starting.

Prerequisites: Phases 1–4 complete. All core modules, shared modules, and feature modules for dictee, math, avatar, rewards exist.

## What to build

### PR 16: feature-settings
Build the Settings screen from section 11:

**SettingsScreen (main):**
- Profile card: avatar composite + name + equipped title → tap navigates to Avatar Closet
- General section:
  - App Language: picker dialog (🇫🇷 Français, 🇬🇧 English, 🇩🇪 Deutsch) → updates app locale via AppCompatDelegate
  - Study Reminders: time picker → schedule WorkManager PeriodicWorkRequest for daily notification
  - Sound Effects: toggle → DataStore
  - Haptic Feedback: toggle → DataStore
- Voice Packs section:
  - Show installed/available voice packs per language
  - Each row: flag, language name, voice name, size, status (✓ Installed / Download button)
  - Tap "Download" → start download via shared-tts TtsManager
  - Tap installed → options: Test (play sample), Update, Remove
  - Navigate to VoicePackManagerScreen for full management
- Learning section:
  - Daily Goal: picker (3, 5, 10 activities)
  - Accent Strictness: picker (Lenient / Strict) → DataStore
  - Default Timer: picker (10s, 15s, 30s, ∞) → DataStore
  - Dictée Input: picker (Keyboard / Handwriting) → DataStore
  - Show Hints: toggle → DataStore
- Parent Zone 🔒: PIN-gated section
  - First access: set a 4-digit PIN, stored hashed in DataStore
  - Subsequent access: enter PIN to unlock
  - Items behind PIN:
    - Progress Reports → navigate to Stats screen
    - Screen Time Limits → picker (30min, 45min, 1h, 2h, Unlimited) → DataStore
    - Backup & Export → navigate to BackupExportScreen
    - Cloud Sync: toggle (currently disabled, shows "Coming Soon" toast)
    - Reset All Data: confirmation dialog (type "RESET" to confirm) → wipe Room DB + DataStore

**VoicePackManagerScreen:**
- Installed voices with Test / Update / Remove buttons
- Available voices with Download button + progress bar
- Storage usage summary per pack
- See shared-tts module for TtsManager API

**Screen time enforcement:**
- Track cumulative session time today in DataStore
- When limit reached, show overlay: "Great work today! Come back tomorrow 🌟"
- Overlay blocks all modes but allows Settings and Stats access

**All settings persisted via DataStore Preferences** using keys from section 11.

- Commit: `feat(settings): add settings screen with voice packs, parent zone, and screen time`

### PR 17: feature-backup
Build the Backup & Export screen from section 12:

**BackupExportScreen:**
- Last backup info: date/time, file size
- "Backup Now" button → `CreateBackupUseCase`:
  1. Query all Room tables
  2. Map entities to serializable DTOs
  3. Wrap in BackupSchema JSON (version, exportedAt, profile, avatar, rewards, lists, sessions, points, settings)
  4. Write to app-specific external storage: `studybuddy_backup_{date}_{name}.json`
  5. Show success toast with file path
- "Restore" button → file picker (ACTION_OPEN_DOCUMENT, type json) → `RestoreBackupUseCase`:
  1. Parse JSON, validate schema version
  2. Confirm dialog: "This will replace all current data. Continue?"
  3. Clear Room DB
  4. Insert all data from backup
  5. Restart app / refresh all data
- Auto-backup section:
  - Enable/disable toggle → DataStore
  - Frequency picker: Daily, Weekly
  - Save location: Device (default)
  - Implement via WorkManager PeriodicWorkRequest

**Export options:**
- PDF Progress Report → `ExportProgressReportUseCase`:
  - Use Android `PdfDocument` + Canvas API
  - Page 1: Profile summary, total stars, streak, session count
  - Page 2: Dictée progress per list with word mastery
  - Page 3: Math stats with accuracy, speed trends
  - Share via `Intent.ACTION_SEND` with PDF MIME type
- Raw Data (JSON): same as backup file, share via Intent
- Word Lists (CSV): `CsvExporter` — one CSV per dictée list with columns: word, attempts, correct, mastery%
  - Share via Intent
- Email Report: create PDF, open email compose intent with attachment

**Storage info display:**
- Query Room DB file size
- Sum voice pack sizes from VoicePackRepository
- Display: "47 sessions · 3 word lists · 24 words · 2 voice packs"

- Commit: `feat(backup): add backup, restore, and export with PDF, JSON, and CSV`

### PR 18: feature-stats
Build the Stats screen from section 10:

**StatsScreen:**
- Summary cards row: Total Stars (⭐), Day Streak (🔥), Sessions (📚)
- Weekly bar chart: points earned per day for current week
  - Use Compose Canvas: draw rounded-top bars with animation
  - Highlight today's bar with primary color, others with primarySoft
  - Animate bar heights with `animateFloatAsState`
- Progress section:
  - Dictée: accuracy trend (e.g., "78% → 89%") with 📈 indicator
  - Speed Math: avg response time trend (e.g., "8.2s → 5.1s") with ⚡ indicator
  - Calculate from last 10 sessions vs previous 10
- Badge collection:
  - Grid of all badges: earned (highlighted) vs locked (dimmed)
  - Same badge definitions as Rewards titles
  - Check unlock conditions from repositories

**Data queries:**
- Total points: `PointsDao.getTotalPoints(profileId)`
- Streak: calculate from PointEvents — count consecutive days with at least 1 event
- Weekly data: `PointsDao.getPointsForDateRange(startOfWeek, endOfWeek)` grouped by day
- Dictée accuracy: `DicteeDao.getRecentAccuracy(limit = 10)` vs `getRecentAccuracy(offset = 10, limit = 10)`
- Math speed: `MathDao.getRecentAvgResponseTime(limit = 10)` vs previous 10

- Commit: `feat(stats): add progress screen with charts, trends, and badges`

### PR 19: feature-onboarding
Build the 3-step onboarding flow from section 14:

**Step 1: Welcome**
- 👋 emoji hero
- "What's your name?" text input (validate non-empty)
- App language selector: 3 flag cards (FR, EN, DE)
- "Next → Choose Your Buddy" button

**Step 2: Choose Your Buddy**
- Live avatar preview (120dp) updates as selections change
- Character grid: 4×2 grid of 8 characters with selection highlight
- Starter hat selection: show only free/owned hats (5 items including "none")
- Starter face accessory: show only free/owned face items
- Info banner: "🎁 Earn ⭐ stars by studying to unlock more!"
- "← Back" and "Next → Voice Setup" buttons

**Step 3: Download Voices**
- 🔊 hero icon
- Explanation text: voices needed for offline dictée
- 3 voice pack cards (FR, EN, DE) each showing:
  - Flag, language name, size, status (downloading/ready/waiting)
  - Progress bar for active download
- Auto-start downloads when screen loads
- "Skip for now →" link text
- "Let's Go! 🚀" button

**On completion:**
1. Create Profile in Room (name, locale, createdAt)
2. Save AvatarConfig in Room (selected body + accessories)
3. Mark starter items as owned in OwnedRewards
4. Set `ONBOARDING_COMPLETE = true` in DataStore
5. Navigate to Home screen (clear backstack)

**Gate logic:**
- App entry point checks DataStore `ONBOARDING_COMPLETE`
- If false → navigate to Onboarding, prevent back to Home
- If true → navigate to Home

- Commit: `feat(onboarding): add 3-step onboarding with profile, avatar, and voice setup`

## Verification

- Settings changes persist across app restart
- Parent PIN works (set + verify)
- Backup creates valid JSON file
- Restore replaces all data correctly
- PDF export opens in viewer
- CSV exports are valid spreadsheets
- Stats show accurate data from Room
- Onboarding completes and gates Home correctly
- Voice downloads show progress
