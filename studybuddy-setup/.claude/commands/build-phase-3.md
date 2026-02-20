Build Phase 3: Speed Math. Read `docs/build-instructions.md` section 7 before starting.

Prerequisites: Phase 1 complete (core modules exist).

## What to build

### PR 11: feature-math
Build all 3 screens from section 7:

**MathSetupScreen:**
- Operator multi-select toggle buttons: +, −, ×, ÷, ^ (at least one required)
- Number range slider: min 1, max 17, dual-thumb slider
- Time per problem: 10s, 15s, 30s, ∞ (single select)
- Number of problems: 10, 20, 50 (single select)
- "Smart mode" info banner explaining adaptive difficulty
- "Go!" button → navigate to MathPlayScreen
- Load defaults from SettingsRepository (default timer, etc.)

**MathPlayScreen:**
- MVI: `MathPlayState` / `MathPlayIntent` as specified in section 7
- Problem display: large text "12 + 7 = ?" centered
- Answer display: bordered box showing typed digits
- Custom numpad: 3×4 grid (1-9, backspace, 0, submit ✓)
  - Minimum touch target: 48dp per button
  - Submit button highlighted green
- Countdown timer bar: green → orange (<30%) → red (<10%) with pulse animation
- Streak counter with 🔥 icon, shows multiplier (×1.5, ×2.0, ×3.0)
- Points badge showing session running total
- Problem counter: "4/20"
- Pause button → overlay with Resume / Quit options

**Timer logic:**
- `LaunchedEffect` with `delay(100)` tick updating `timeRemainingMs`
- Auto-submit as incorrect when timer reaches 0
- Timer pauses when feedback is showing
- Timer resets for each new problem

**Problem generation (via GenerateProblemUseCase):**
- Pick random operator from selected set
- Generate operands within configured range
- Division: ensure `operandA % operandB == 0` and `operandB != 0`
- Subtraction: ensure `operandA >= operandB` (non-negative result)
- Power: base range 2–5, exponent 1–3, result ≤ 125
- Adaptive: if streak > 0, expand range by `streak / 5` (capped at +5)
- No trivial problems at medium+ difficulty (×0, ×1, +0, ÷1)
- Avoid repeating the same problem within a session

**Feedback:**
- Correct: green glow on answer box, scale animation, points fly up, streak increments
- Incorrect: red shake on answer box, show correct answer for 1.5s, streak resets to 0
- Streak milestones (5, 10, 20): trigger CelebrationOverlay animation
- Brief delay (1s correct, 2s incorrect) then auto-advance to next problem

**MathResultsScreen:**
- Summary grid: Score (18/20), Accuracy (90%), Best Streak (12), Avg Time (4.2s)
- Points earned with streak bonus breakdown
- Badge unlock notification if earned (e.g., "Streak Master — 10+ streak")
- "Home" button, "Play Again" button (pre-filled with same settings)
- Save session to Room via `SaveMathSessionUseCase`

- Commit: `feat(math): add speed math with setup, adaptive play, and results`

### PR 12: Speed Math tests
- `GenerateProblemUseCase` tests (section 15.2):
  - `@RepeatedTest(100)`: division has no remainder
  - `@RepeatedTest(100)`: subtraction result non-negative
  - Power problems within sensible range
  - No trivial problems at medium difficulty
  - Adaptive range expansion works
- `MathPlayViewModel` Turbine tests:
  - Digit entered → userAnswer updates
  - Submit correct → feedback Correct, streak +1, score +points
  - Submit incorrect → feedback Incorrect, streak resets to 0
  - Timer expires → auto-marked incorrect
  - All problems done → isComplete = true
  - Pause → isPaused = true, timer stops
  - Resume → isPaused = false, timer continues
- Points calculation test: verify streak multiplier applies to session score
- Commit: `test(math): add unit tests for problem generation and play ViewModel`

## Verification

- Setup screen selections persist through navigation
- Numpad is responsive with big touch targets
- Timer countdown is smooth and accurate
- Streak multiplier displays and applies correctly
- Results screen shows accurate stats
- Session is saved to Room database
- `./gradlew testDebugUnitTest` passes
