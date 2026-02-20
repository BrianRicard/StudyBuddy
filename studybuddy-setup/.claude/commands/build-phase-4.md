Build Phase 4: Avatar & Rewards. Read `docs/build-instructions.md` sections 8 and 9 before starting.

Prerequisites: Phase 1 complete (core modules), shared-points module exists.

## What to build

### PR 13: feature-avatar
Build the Avatar Closet screen from section 8:

**Avatar data (hardcoded catalog in core-domain):**
- 8 character bodies: fox 🦊, cat 🐱, unicorn 🦄, panda 🐼, butterfly 🦋, bunny 🐰, owl 🦉, dragon 🐉
- Hats (8): none, top hat 🎩, crown 👑, wizard 🧙, party 🥳, beret, flower 🌺, cap 🧢
- Face accessories (6): none, shades 🕶️, monocle 🧐, glasses 👓, mask 🎭, star ⭐
- Outfits (6): none, scarf 🧣, bow tie 🎀, cape 🦸, medal 🏅, necklace 📿
- Pets (6): none, chick 🐥, hamster 🐹, fish 🐠, snail 🐌, ladybug 🐞
- Each item has: id, name, emoji/icon, starCost (0 = free/starter), category
- Free starter items: none (all categories), top hat, shades, glasses, scarf, party hat, chick

**AvatarConfig data model:**
```kotlin
data class AvatarConfig(
    val bodyId: String = "unicorn",
    val hatId: String = "none",
    val faceId: String = "none",
    val outfitId: String = "none",
    val petId: String = "none",
    val equippedTitle: String? = null,
)
```

**AvatarComposite composable (in core-ui, already stubbed):**
- Renders the character emoji at center
- Layers hat above, face accessory to the side, outfit below, pet at corner
- Accepts `size` parameter for different contexts (50dp home header, 130dp closet)
- Accepts `AvatarConfig` and renders accordingly

**AvatarClosetScreen:**
- Large avatar preview at top (130dp) with real-time updates
- Character selection: horizontal scrollable row of 8 characters
- 4 accessory tabs: Hats 🎩, Face 🕶️, Outfit 👔, Pets 🐾
- Items grid (3 columns): show emoji, name, owned/locked/equipped state
- Owned items: tap to equip immediately → update AvatarConfig in Room
- Locked items: show star cost badge, tap → purchase confirmation dialog
- Purchase flow: check balance → deduct stars → add to OwnedRewards → equip → success animation

**AvatarClosetViewModel:**
- Load AvatarConfig from AvatarRepository
- Load owned items from RewardsRepository
- Handle item selection (equip if owned, purchase dialog if locked)
- `PurchaseItemUseCase`: validates balance, deducts points, adds to owned, updates avatar
- Emit success/failure effects

- Commit: `feat(avatar): add avatar closet with character selection, accessories, and purchase flow`

### PR 14: feature-rewards
Build the Rewards Shop from section 9 with 4 tabs:

**Tab 1: Avatar**
- Grid view of all purchasable avatar items grouped by category (Hats, Pets, Outfits & Face)
- Each item: emoji, name, "✓ Owned" or "⭐ {cost}" purchase button
- Tap purchase button → confirmation dialog → deduct stars → mark owned
- Link to Avatar Closet for equipping

**Tab 2: Themes**
- List of 6 app themes with gradient preview cards:
  - Sunset (default, free), Ocean, Forest, Galaxy, Candy, Arctic
  - Each shows: gradient bar, name, "✓ Active" / "Owned" / "⭐ {cost}"
- Purchase theme → persist to DataStore via SettingsRepository
- Active theme changes `StudyBuddyTheme` colorScheme

**Tab 3: Effects**
- Celebration effects grid (2 columns): Confetti (free), Fireworks, Unicorn Dance, Rainbow Burst, Star Shower, Rock Star, Champion, Dragon Fire
- Correct answer sounds grid: Chime (free), Fanfare, Arcade, Musical
- Each shows: emoji, name, description, owned/cost
- Purchase → persist selected effect/sound to DataStore

**Tab 4: Titles**
- List of 8 titles earned through achievements:
  - 🌟 Rising Star (earn 100 stars), 📝 Word Wizard (master 25 words), ⚡ Speed Demon (avg < 3s), 🔥 Streak Champion (7-day streak), 🏆 Perfect Scholar (100% on 10 sessions), 💎 Star Collector (5,000 stars), 🌍 Polyglot (practice in 3 languages), 🎓 Grand Master (unlock all titles)
- Show requirement for each, "Equip" button for unlocked, 🔒 for locked
- Equipped title displays under profile name throughout app

**RewardsShopViewModel:**
- Load all items with owned/locked status
- Load current star balance
- Handle purchases across all categories
- Check title unlock conditions against actual stats from repositories

**RewardCatalog.kt (in core-domain):**
- Hardcoded list of all reward items with costs
- Includes helper functions: `getItemsByCategory()`, `getStarterItems()`

- Commit: `feat(rewards): add rewards shop with avatar items, themes, effects, and titles`

### PR 15: Avatar & Rewards tests
- `PurchaseItemUseCase` tests:
  - Sufficient balance → purchase succeeds, stars deducted, item owned
  - Insufficient balance → purchase fails with error, stars unchanged
  - Already owned → purchase fails gracefully
- AvatarClosetViewModel tests:
  - Equip owned item → avatarConfig updates
  - Purchase and equip → stars deducted, item owned, avatarConfig updates
- Title unlock logic tests:
  - Each title condition verified against mocked repository data
- Theme persistence test:
  - Purchase theme → DataStore updated → Theme composable reads new value
- Commit: `test(avatar,rewards): add unit tests for purchase flow and avatar management`

## Verification

- Avatar renders correctly with all accessory combinations
- Purchase flow deducts stars and updates UI
- Theme changes apply to the entire app
- Titles display correctly under profile name
- All items in catalog are accessible
