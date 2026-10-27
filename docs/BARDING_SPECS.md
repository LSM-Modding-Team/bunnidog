# Bunnidog and Apex equipment

Real single-item BODY equipment now ships in Leather, Iron, Diamond, Netherite
and Astralite tiers, following the accepted progressively armoured design and
textured material study. `art/gen_barding.py` exports five native animated meshes
and atlases. `art/barding_shipped_preview.png` reads those binary exports. The
original fur/sculpt, wings, crown and packs remain separate layers.

Hold a suit and right-click a calm bonded adult. A swap spends one actual held
item and returns the old suit once, with normal full-inventory drop behaviour.
The keeper's animal conversation shows **Remove armour** when equipped. Unowned
animals, pups, hostility, combat/recovery, fire and transport refuse changes.
Quiet downed animals can have gear removed. Hostile Apexes cannot be disarmed
through conversation; dungeon equipment is installed at fresh spawn. Ordinary
wild Apexes do not automatically receive armour.

| Tier | Armour / toughness | Health | Damage | Speed | Knockback | Durability |
| --- | --- | --- | --- | --- | --- | --- |
| Leather | +2 / 0 | ×1.10 | ×1.05 | unchanged | unchanged | 320 |
| Iron | +6 / +1 | ×1.25 | ×1.15 | unchanged | +0.05 | 800 |
| Diamond | +10 / +2 | ×1.50 | ×1.30 | +2% | +0.10 | 1,600 |
| Netherite | +12 / +3 | ×1.75 | ×1.45 | +5% | +0.15 | 2,400 |
| Astralite | +14 / +5 | ×2 | ×1.6 | +8% | +0.20 | 7,200 |

All benefits derive from the real native BODY stack. Saved named modifiers keep
native load-time health truthful; refresh reconciles those values to actual gear
without stacking or healing. Swaps/removal clamp current absolute health to the
new maximum; they do not grant health or genetic points. Native attribute caps
remain. Wings retain existing flight, with the same modest speed factor.
Actual damage and effects still use native protection/cancellation rules.

The suit loses wear on accepted armour-mitigated damage. Native Unbreaking,
Mending, applicable armour enchants and material anvil repair are supported;
Mending can repair removed gear held by the player collecting XP. No automatic
keeper-XP theft. Equipped pet gear has native guaranteed drop ownership; dungeon
champion gear uses a 25% native equipment drop chance, and the Crowned Apex's suit
is guaranteed. Discard/cleanup gives no equipment loot. Netherite/Astralite suits
resist native fire/blast item damage. Native death/gamerule/curse semantics apply.

Leather is crafted with six leather, an iron nugget and string. Native smithing
upgrades the previous suit with an iron block, then a diamond block. Netherite
uses a Netherite Upgrade Template/ingot. Astralite uses the Astralite Upgrade
Template and actual **Astralite Plating** from the new lunar forge. All native
names, enchantments and damage components remain owned by the upgraded stack.
Each stage unlocks in the recipe book. This is one item, not four separate slots.

Travel Basket whole-animal saves preserve gear. The Genetic Vacuum refuses an
armoured animal rather than deleting its equipment into a genetics-only seed.
Stats/seeds/samples/offspring never inherit equipment. Armour provides no learning,
loyalty, fertility, cargo capacity, attack-cadence or artificial bred points.

Fresh dungeon Apexes use wild-range lineages/points and actual Astralite suits.
Frozen cooperative scaling is separate: +15% per extra participant, within the
960 native champion pool target. Legacy active profiles remain in old saved
actors until their encounter ends. The Crowned Apex rebases new encounters to
3,000 pre-equipment solo HP, so armour produces 6,000 (15,900 with four players),
40/56 melee and 32 beam damage rather than doubling the already-buffed baseline.

Native checks cover actual paid equip/swap, wound conservation, attributes and
native BODY save/load/removal, plus dungeon profiles/enrage/co-op/phase/reload.
Client classes compile and binary previews inspect the shipped surfaces. Natural
client motion, folded/spread wing/pack clearance and Survival acquisition remain
human review; headless previews are not live Minecraft captures.
