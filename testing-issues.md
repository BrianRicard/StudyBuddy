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

### Issue 6: Missing containerColor on Home Scaffold (PR #23)
- **Screen**: Home
- **Problem**: Scaffold and loading state Box missing themed background color
- **Fix**: Added `containerColor` to Scaffold, added `.background()` to loading Box

### Issue 7: Missing containerColor on Settings Scaffold (PR #23)
- **Screen**: Settings
- **Problem**: Scaffold missing themed background color
- **Fix**: Added `containerColor = MaterialTheme.colorScheme.background` to Scaffold

### Issue 8: Missing containerColor on Rewards Shop Scaffold (PR #23)
- **Screen**: Rewards Shop
- **Problem**: Scaffold missing themed background color
- **Fix**: Added `containerColor = MaterialTheme.colorScheme.background` to Scaffold

### Issue 9: Missing containerColor on Backup & Export Scaffold (PR #23)
- **Screen**: Backup & Export
- **Problem**: Scaffold missing themed background color
- **Fix**: Added `containerColor = MaterialTheme.colorScheme.background` to Scaffold

### Issue 10: Missing containerColor on Onboarding Scaffold (PR #23)
- **Screen**: Onboarding
- **Problem**: Scaffold missing themed background color
- **Fix**: Added `containerColor = MaterialTheme.colorScheme.background` to Scaffold

### Issue 11: Purchase Success No Feedback (PR #23)
- **Screen**: Rewards Shop
- **Problem**: PurchaseSuccess effect handler was a no-op; users got no feedback when purchase succeeded
- **Fix**: Added SnackbarHost and "Unlocked {itemName}!" message on PurchaseSuccess

### Issue 12: Onboarding Error No Feedback (PR #23)
- **Screen**: Onboarding
- **Problem**: ShowError effect handler was a no-op; users saw no error feedback
- **Fix**: Added SnackbarHost and wired ShowError.message to snackbar

## Open Issues (from code review)

### Issue 13: Hardcoded strings not localized
- **Screens**: BackupExportScreen, SettingsScreen, RewardsShopScreen, OnboardingScreen
- **Problem**: Many strings are hardcoded instead of using stringResource()
- **Severity**: Minor - functional but not i18n-ready

### Issue 14: Parent PIN uses String.hashCode() (not cryptographic)
- **Screen**: Settings (Parent Zone)
- **Problem**: PIN hashing uses Java hashCode() which has trivial collisions
- **Severity**: Medium - but acceptable for kid's app with no sensitive data

### Issue 15: PIN hash lost on process death
- **Screen**: Settings (Parent Zone)
- **Problem**: PIN hash stored only in memory (var storedPinHash), lost on process death
- **Severity**: Medium - parent loses PIN on app kill

### Issue 16: Missing empty state for recent activities
- **Screen**: Home
- **Problem**: No empty state shown when recentActivities is empty (first-time users see blank)
- **Severity**: Minor - cosmetic

## Screens Tested
- [x] Onboarding (3 steps)
- [x] Home Screen
- [x] Stats
- [x] Rewards Shop (all 4 tabs)
- [x] Avatar Closet
- [x] Settings
- [x] Dictee (empty state)
- [x] Math Setup
- [x] Math Play (correct, wrong, timeout, pause)
- [ ] Math Results
- [ ] Dictee with word lists
- [ ] Dictee handwriting mode
- [ ] Theme switching
- [ ] Parent Zone
- [ ] Backup/Restore
- [ ] All avatar characters with accessories
