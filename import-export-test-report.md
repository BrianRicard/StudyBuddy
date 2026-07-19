# StudyBuddy — Import/Export Feature Test Report

- **Date:** 2026-07-18
- **Build:** debug APK @ commit `6e8c961` (latest `main` at test time)
- **Device:** emulator-5554, Android 12 (API 31), 1080×2400, device locale fr
- **Test data:** `~/temp/studybuddy_backup.json` (20 lists / 177 words / 5 math sessions / 259 point events / 34 rewards), `~/temp/studybuddy_word_lists.csv` (177 word rows)
- **Method:** manual UI-driven testing via adb (SAF pickers included), with results verified at the database level (`run-as` pull of `studybuddy.db` **+ `-wal`** — the WAL is required for current state) and via logcat.

## Summary

All four import/export operations function end-to-end on the latest version — no crashes
(zero `FATAL EXCEPTION` in the session logcat). However, testing the realistic
"new phone" journey uncovered **one major silent-data-loss bug** in the restore flow,
plus several minor/cosmetic issues.

| Feature | Result |
|---|---|
| Export JSON (full backup) | ✅ Pass — round-trip byte-identical |
| Export CSV (word lists) | ✅ Pass — content-identical to source CSV |
| Export PDF (progress report) | ⚠️ Works, but wrong "Total Stars" value; no charts despite label |
| Import CSV (word lists) | ⚠️ Works — but duplicates existing lists; mislabeled spinner |
| Restore JSON (full backup) | ❌ **Major bug in fresh-install scenario — see Bug 1** |

## Bug 1 — MAJOR: restore on a fresh install silently wipes restored dictée progress

**Severity:** Major (silent data loss in the flagship "transfer to another device" flow).

**Reproduction (verified live twice):**
1. Fresh install (`pm clear`), complete onboarding.
2. Settings → Parent Zone → Backup & Export → *Restore from Backup* → select
   `studybuddy_backup.json` → confirm. Snackbar: "Data restored successfully".
   At this point the DB is exactly the backup: 177 words, **113 mastered** (verified).
3. Open the Dictée screen (first visit).

**Expected:** restored lists keep their progress (113 mastered words, attempts history).

**Actual:** mastered words drop to **20**; all 156 restored words in the 17 bundled lists
(`fr_dictee_01..17`) are deleted and replaced by fresh words (`mastered=false, attempts=0`,
new UUIDs). Only the 3 custom lists (21 words) survive. In the UI, e.g. "Unit 2: an · en"
goes from 100% → 0%. No error or warning is shown.

**Root cause:**
- `DicteeListViewModel.seedDefaultListsIfNeeded()`
  (`feature/feature-dictee/.../dictee/list/DicteeListViewModel.kt:115-121`) decides whether
  to seed bundled lists from the DataStore flag `dictee_seeded` — which is `false` on any
  fresh install — instead of checking whether the lists table already contains data.
- Seeding (`LocalDicteeRepository.seedDefaultLists`) inserts lists with stable IDs
  (`fr_dictee_01…`) via `INSERT … ON CONFLICT REPLACE`. The REPLACE deletes the restored
  list rows, and the `ForeignKey(onDelete = CASCADE)` on `DicteeWordEntity` deletes all
  their restored words; fresh words get random UUIDs, so nothing merges.
- `BackupManager.restoreFromBackup` only touches Room and cannot set the DataStore flag.

**Suggested fix:** make the seed check content-based (seed only when the lists table is
empty, or skip list IDs that already exist), and/or have the restore flow mark bundled
content as seeded. Note the restore itself is fine — the data is destroyed by the
*post-restore seed*, after the success message.

## Bug 2 — Minor: backup JSON never contains a schema `version`

`BackupData.version` (`core/core-data/.../backup/BackupManager.kt:23`) defaults to
`AppConstants.BACKUP_SCHEMA_VERSION` (2), and the `Json` instance uses
`encodeDefaults = false`, so the field is **never serialized**. Verified in both the
user's file and fresh exports. The "JSON with schema version" format documented in
`CLAUDE.md` is effectively unversioned — a future v3 restore cannot detect a v1/v2 file.
Fix: set `encodeDefaults = true`, or make `version` a required constructor param.

## Bug 3 — Minor: PDF report shows "Total Stars: 0" while the app shows 464

`PdfReportGenerator.generateReport` uses `profile.totalPoints`
(`core/core-data/.../backup/PdfReportGenerator.kt:43`), which is stale (0 in the test
data) instead of the points ledger (point events sum to 464, as shown on Home). Parents
exporting a progress report get a wrong headline number. Verified by rendering the
exported PDF.

## Bug 4 — Cosmetic: PDF export is advertised as "with charts"

The card description reads "A printable report **with charts** showing your learning
progress across all activities." The generated PDF contains only plain-text lines
(title, student, totals, accuracy) — no charts exist in `PdfReportGenerator`.
Fix the description or add the charts.

## Bug 5 — Cosmetic: CSV import shows an "Exporting…" spinner label

The Import card reuses `ExportOptionCard` with `isExporting = state.isImporting`
(`feature/feature-backup/.../BackupExportScreen.kt:295-305`), so while a CSV import
runs, the button shows the string `backup_exporting` ("Exporting…") instead of
"Importing…". Observed live during the 177-word import.

## Bug 6 — Minor UX: CSV import fully duplicates existing lists

Importing the app's own CSV export into the same profile created **40 lists / 354 words**
(every list duplicated with a new UUID; verified in DB and UI). There is no
deduplication, merge, or "lists already exist" prompt. Fine for transfer to an empty
device, surprising for re-import/merge scenarios.

## Bug 7 — Minor UX: "Last Backup" timestamp is not persisted

`BackupExportState.lastBackupDate` lives only in the ViewModel. After a successful
backup, leaving and re-entering the screen shows "No backups yet" (observed). Either
persist the timestamp (DataStore) or derive it from actual backup history.

## Observations (not bugs)

- Parent Zone re-locks when leaving Settings (ViewModel-scoped state) — defensible
  security UX; PIN create + verify flows both work.
- The SAF picker UI follows the device locale (French) while in-app strings correctly
  follow `app_locale` (English) — platform behavior, not an app issue.
- Parent Zone PIN gate works; PIN is stored as a plain `String.hashCode()` — acceptable
  for a kids app, but trivially brute-forceable if that ever matters.

## Evidence artifacts (on the test host, `/tmp/uitest/`)

- Screenshots: `01_firstlaunch.png`, `47_dictee_lists.png`, `53_confirm.png`,
  `54_restored.png`, `70_dictee_wiped.png` (Unit 2 at 0% after the wipe), etc.
- Pulled exports: `exported_backup.json`, `exported_lists.csv`, `exported_report.pdf`
  (+ `pdf_page1.png` render).
- DB snapshots: `dbfull/studybuddy.db` (+ `-wal`) at each stage.
- Full session logcat: `logcat_full.txt`.
- On-device exports remain in the emulator's `Download/` folder.
- Emulator was left in its pre-test state (myriam's data, 177 words / 113 mastered).
