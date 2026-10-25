# Apex ingredients and expedition equipment

The player's October 25 review found the initial realm rewards too conservative:
a well-equipped vanilla endgame player survives only minutes. Rewards now support
substantial power progression. Enemy combat values are unchanged. This revision
supersedes the initial loot/equipment numbers in GREAT_WARREN_SPECS.md.

## Parts from enemies; bulk materials from ruins

Player-credited **wild and ritual** Apex kills provide 4–8 role parts and 2–4 Apex
Marrow. Flying forms additionally provide 2–4 Aerial Membranes. Rituals retain
their guaranteed source Crest; wild Crests remain a rare bonus. This supplies a
preparation path before entering the realm. Temporary brood drop no materials/XP;
explicitly capturing one with the Gene Siphon is now an authorized exception to
the former no-permanent-brood-products rule.

| Enemy | Unique part | Principal uses |
|---|---|---|
| Melee Apex | Apex Fang | Lance, launcher, Cataclysm Core |
| Prismmaw | Prism Lens | Lance, aerial artifact, vacuum, flask |
| Broodcaller | Chorus Heart | Heartbound artifact, vacuum, flask, wards |
| Blastmaw | Volatile Heart | Launcher, charges, Cataclysm Core, aerial artifact |
| Lifebinder | Heart Spool | Heartbound/aerial artifacts, vacuum, flask, Cataclysm Core |
| All forms | Apex Marrow | Structural parts, artifacts, defensive Rootstone |
| Flying forms | Aerial Membrane | Armored Elytra, flask, lance, wards |

Ruins now have three caches: wealth, bulk supplies, and the preserved genetic
archive. New wealth caches include **48–64 diamonds, 12–24 netherite ingots,
8–16 enchanted golden apples, 2–4 Totems, an Elytra, gold/emerald blocks and Blaze
Rods together**. Supply caches give 128–192 amethyst shards and gunpowder,
32–64 copper blocks/crying obsidian/carrot meals/bottles/fire charges, and 32–48
Ender Pearls. These are independent pools with room for their native split stacks.

Unique Apex parts and finished mod gear are not generic treasure rolls. The
archive keeps its exact seed/tissue records, Mutagen and Starlit Dust, but its
former finished cells become raw amethyst. Saved completed structures and opened
containers are never refilled or rerolled. New ruin pieces add the third chest;
existing generated ruins retain their saved shape/stock.

## Upgrade existing equipment

Native smithing transforms copy actual item components: enchantments, custom
names, trims and damage remain. Artifact additions use the ordinary base/addition
slots, with no extra template required. Brief artifact lore and native recipe
unlocks reveal the smithing route.

**Heartbound Netherite.** Heartbound Artifacts combine Spools, Chorus Heart,
Marrow and enchanted golden apples. Smith an artifact into each Netherite armor
piece. Each equipped piece gives **three extra max HP / 1.5 heart containers**,
with the original Netherite armor attributes and enchantment compatibility.
The health benefit persists through saves and is removed when unequipped;
it is not an infinitely stackable consumable upgrade.

Lifebinder losses inflicted while wearing any Heartbound piece receive individual
recovery deadlines: **60 seconds with one piece, 45 with two, 30 with three,
15 with four**. Each hit recovers separately. Removing armor does not remove or
reset an existing deadline. Earlier unprotected debts keep their established
caster-kill/death/Totem repayment. Totem repayments remove corresponding pending
timers, preventing an old timer from paying a newer debt. Existing marks saves
remain valid; deadlines persist in the player's native persistent data.

**Armored Elytra.** An Aerial Artifact combines Membranes, Lens, Spools, Volatile
Heart and a netherite block. Smith it into an actual Elytra. The old
`apex_wing_harness` item ID is retained for save compatibility, but its standalone
crafting recipe is replaced. It now gives **eight armor points, three toughness**,
native Protection/durability enchantment support, normal gliding and 864 durability.
Its existing Prism Cell climbing bursts remain. It occupies the chest slot and
therefore trades the fourth Heartbound health/recovery bonus for flight.

## Weapons and rescue

**Prism Lance:** now combines several lenses, a Fang, Spool, Membrane and
netherite. Its charged ray passes through blocks/mobs/shields and now armor,
dealing **64 raw damage**. It retains its 32-tick charge, 30-tick recovery,
32-block range, one-cell fuel and one-durability cost. Saved lances retain their
identity and receive the new behavior.

**Blastmaw Launcher:** mixed Volatile Hearts/Fangs/Marrow with netherite and a
Blaze Rod. Launches real **power-four** native fireballs, including terrain/fire
behavior and player kill credit. One Volatile Charge and one durability per
shot; 40-tick repeat delay. Hearts, gunpowder and Fire Charges craft sixteen
rounds. These are player weapons; the enemy family's fireball immunity does not
protect targets from them. They can hurt the shooter normally.

**Cataclysm Totem:** combine Volatile Hearts, Marrow, Spools and a Fang into a
Core, then smith it into a real Totem. It uses native death protection. Only a
confirmed successful pop triggers its **power-twenty**, terrain-damaging blast.
Its owner takes no blast damage/knockback and gains a short Slow Falling escape.
Other survivors within forty blocks receive **five seconds of Resonant Stun**.
Mob AI, movement/jump and outgoing damage are suppressed; existing NoAI state is
preserved and ordinary AI resumes after the effect. Apex custom ability ticks
also pause. Other players, pets, loot and builds are not exempt from the blast;
this is a situational rescue, not a safe base accessory. Native explosion
cancellation/protection still applies. Totem heart repayment remains intact.

## Functional equipment

**Gene Siphon:** Hearts, Lens, Spools and Marrow with netherite and a vanilla
Bundle. Holding use pulls an aimed, visible ordinary bunnidog or hostile brood
within nine blocks. A completed twenty-tick cycle converts one actual animal,
paying one Prism Cell/one durability, and inserts a seed into the **real vanilla
BundleContents** inventory. Inventory insertion/extraction, grid tooltip,
selection and fullness follow BundleItem; crouched world use retains bundle
unloading. It never equips itself as an animal's cargo bundle.

Each captured seed hatches **one exact-genome, exact-stat pup**, with no extra
litter, mutation or quality/Fortune drift. The appended `captured` seed marker
survives crops, Cradles and saves. Infusions and body/mind/commission forecasts
honor that exact record. Ordinary seed formats/mutation laws remain unchanged.
Genes/points are copied; bond, age, learned routines and temporary hostile species
multipliers are not. Actual Apex bosses, other keepers' animals, leashed animals,
passengers and animals with carried cargo cannot be captured. A nearly broken or
full tool cannot delete an animal. Chorus Hearts repair the device. There are
no death drops or XP from extraction.

**Abyssal Flask:** Hearts, Lens, Spools and a Membrane around a glass bottle.
Inventory clicks insert real drinkable potions or guava juices; newest insertion
is on top. Right-click with an empty cursor extracts the top intact drink.
Normal use drinks that top entry using its actual native potion/juice behavior,
including effects and bottle remainders. Splash/lingering potions are excluded.
Individual frozen infusion components and order never reroll.

There is **no artificial capacity limit**. To avoid an ever-growing network item
payload, contents are stored with the world in FlaskStorage; the item carries a
persistent identity and a small immutable count/top preview. Dimensions share
the same storage. Copies of the same identity share the same real pool, rather
than duplicating drinks. The preview is a custom read-only component, not an
extractable virtual Container component. Preserve world data when transferring
saves: this is world-backed storage, not a self-contained NBT export to a
separate world. The flask is fire resistant and cannot nest inside other
container items.

## Evidence and assets

Java 25 compilation and 169 native cases pass for the implemented behavior.
Focused cases cover native smithing retention/Protection compatibility, independent
heart deadlines, native successful Totem pop/crater/stun/AI recovery, paid launcher,
actual suction/capture and frozen hatching/infusion/forecasts, real flask clicks,
ordered native drinking and registry-aware saved storage over 300 drinks.
The original ritual case now requires its guaranteed Crest plus actual parts.

Art: `art/gen_apex_rewards.py` runs at the end of `art/gen_realm.py`, authoring
icons, armor inlay/equipment resources, stun icon, recipes/unlocks and loot.
`art/apex_rewards_preview.png` is inspected. Model/localization checks pass;
these are headless checks, not a graphical client playtest or proof of final
natural encounter balance. The Crown Warren/final boss and unrelated roadmaps
remain paused for the player's review.
