# StudyBuddy Testing Issues Log

## Fixed Issues

### Issue 1: Stats Loading State (PR #21 - merged)
- **Screen**: Stats
- **Problem**: Loading state showed plain white background instead of themed beige
- **Fix**: Added `containerColor = MaterialTheme.colorScheme.background` to Scaffold

### Issue 2: Math Timeout vs Wrong Answer (PR #21 - merged)
- **Screen**: Math Play
- **Problem**: Timeout and wrong answer both used identical red styling
- **Fix**: Added `TimeoutAmber` color; timeout now shows amber, wrong shows red

### Issue 3: Math Timeout Feedback Color (PR #21 - merged)
- **Screen**: Math Play
- **Problem**: Timeout feedback message used red (error color) instead of distinct color
- **Fix**: FeedbackMessage now uses amber for TimeUp, red only for Incorrect

### Issue 4: Dictee Handwriting Error Handling (PR #22 - merged)
- **Screen**: Dictee Practice (handwriting mode)
- **Problem**: No error handling for recognition failures; Check button stayed disabled with no feedback
- **Fix**: Added recognitionPending/recognitionError states, "Recognizing..." indicator, error messages

### Issue 5: Avatar Accessories Too Small (PR #22 - merged)
- **Screen**: Avatar Closet
- **Problem**: Masks are tiny and positioned between avatar's eyes only; accessories barely visible
- **Fix**: Scaled up all accessory constants (HAT 0.28→0.45, FACE 0.24→0.40, OUTFIT 0.24→0.38, PET 0.22→0.32)

### Issue 6: Missing containerColor on Home Scaffold (PR #23 - merged)
- **Screen**: Home
- **Problem**: Scaffold and loading state Box missing themed background color
- **Fix**: Added `containerColor` to Scaffold, added `.background()` to loading Box

### Issue 7: Missing containerColor on Settings Scaffold (PR #23 - merged)
- **Screen**: Settings
- **Problem**: Scaffold missing themed background color
- **Fix**: Added `containerColor = MaterialTheme.colorScheme.background` to Scaffold

### Issue 8: Missing containerColor on Rewards Shop Scaffold (PR #23 - merged)
- **Screen**: Rewards Shop
- **Problem**: Scaffold missing themed background color
- **Fix**: Added `containerColor = MaterialTheme.colorScheme.background` to Scaffold

### Issue 9: Missing containerColor on Backup & Export Scaffold (PR #23 - merged)
- **Screen**: Backup & Export
- **Problem**: Scaffold missing themed background color
- **Fix**: Added `containerColor = MaterialTheme.colorScheme.background` to Scaffold

### Issue 10: Missing containerColor on Onboarding Scaffold (PR #23 - merged)
- **Screen**: Onboarding
- **Problem**: Scaffold missing themed background color
- **Fix**: Added `containerColor = MaterialTheme.colorScheme.background` to Scaffold

### Issue 11: Purchase Success No Feedback (PR #23 - merged)
- **Screen**: Rewards Shop
- **Problem**: PurchaseSuccess effect handler was a no-op; users got no feedback when purchase succeeded
- **Fix**: Added SnackbarHost and "Unlocked {itemName}!" message on PurchaseSuccess

### Issue 12: Onboarding Error No Feedback (PR #23 - merged)
- **Screen**: Onboarding
- **Problem**: ShowError effect handler was a no-op; users saw no error feedback
- **Fix**: Added SnackbarHost and wired ShowError.message to snackbar

### Issue 15: PIN hash persisted in DataStore (PR #24)
- **Screen**: Settings (Parent Zone)
- **Problem**: PIN hash stored only in memory (var storedPinHash), lost on process death
- **Fix**: Added `getParentPinHash()`/`setParentPinHash()` to SettingsRepository + DataStore impl; ViewModel now reads/writes via repository

### Issue 16: Home empty state for recent activities (PR #24)
- **Screen**: Home
- **Problem**: No empty state shown when recentActivities is empty (first-time users see blank)
- **Fix**: Added EmptyRecentActivity composable with "No activity yet" message and encouragement

### Issue 17: PIN dialog keyboard Done action (PR #24)
- **Screen**: Settings (Parent Zone)
- **Problem**: Soft keyboard can cover the "Set PIN" button, causing a silent failure on first attempt
- **Fix**: Added `imeAction = ImeAction.Done` and `keyboardActions` to PIN text field; user can submit via keyboard "Done" button

## Open Issues

### Issue 13: Hardcoded strings not localized
- **Screens**: BackupExportScreen, SettingsScreen, RewardsShopScreen, OnboardingScreen
- **Problem**: Many strings are hardcoded instead of using stringResource()
- **Severity**: Minor - functional but not i18n-ready

### Issue 14: Parent PIN uses String.hashCode() (not cryptographic)
- **Screen**: Settings (Parent Zone)
- **Problem**: PIN hashing uses Java hashCode() which has trivial collisions
- **Severity**: Medium - but acceptable for kid's app with no sensitive data

### Issue 18: Extra "Maple" theme not in spec
- **Screen**: Rewards Shop (Themes)
- **Problem**: 7 themes exist instead of the 6 defined in the spec (extra "Maple" theme)
- **Severity**: Info - may be intentional addition

## Screens Tested
- [x] Onboarding (3 steps)
- [x] Home Screen
- [x] Stats
- [x] Rewards Shop (all 4 tabs)
- [x] Avatar Closet (face, hats, outfits, pets tabs)
- [x] Settings
- [x] Dictee (empty state - "No Word Lists Yet" with Import and New List FAB)
- [x] Math Setup (operators, number range, time, problem count, smart mode)
- [x] Math Play (correct, wrong, timeout, pause, streak indicator)
- [x] Math Results (score, accuracy, best streak, avg time, points breakdown, badge)
- [x] Theme switching (Ocean theme purchased and activated successfully)
- [x] Parent Zone (PIN creation, unlock, options visible)
- [x] Backup/Restore (Backup Now, Restore, Export PDF/JSON/CSV options)
- [ ] Dictee with word lists (requires creating/importing a list first)
- [ ] Dictee handwriting mode (requires active word list)
- [ ] All 18 avatar characters with accessories (tested Fox/Cat with various accessories)
