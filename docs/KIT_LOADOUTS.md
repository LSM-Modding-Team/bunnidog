# Combat testing kits

`/kit vanilla|bunnidog early|mid|late|endgame replace` explicitly replaces the
player's inventory. The unconfirmed command still only prints its existing
replacement instruction. These are progression testing supplies, not arena rules
or rewards earned in Survival. Existing inventories are changed only by invoking
the command again.

## Current inventory changes

Both endgame families now have 128 XP bottles instead of both steak stacks,
128 end crystals instead of the cobblestone/plank stacks, and 128 wind charges.
Three stacks of obsidian remain: one immediately accessible and two boxed.
The axe, shovel, hoe, crossbow and trident are in the Shulker, retaining their
actual enchantments. The trident has Riptide III without Channeling or Loyalty.
The mace remains in the main inventory.

All enchanted late/endgame bows have Infinity instead of Mending. Their single
64-arrow stack also supplies the boxed crossbow. The spare boxed arrow stack is
removed. Flight-1 rockets total three stacks, one carried and two boxed, rather
than four. Other durable equipment keeps Mending I and Unbreaking III.

Late kits also box their shovel, hoe and crossbow, but keep the axe accessible
for shield combat. Early/mid kits retain their tool sets and offhand shield,
without an unnecessary second shield. All non-endgame kits gain one lava bucket;
mid adds eight cobwebs and late adds sixteen. Their ordinary food and building
blocks remain. Vanilla late/endgame retain three Strength II potions and add
one Speed II plus one eight-minute Fire Resistance potion. Bunnidog late/endgame
keep their actual infused-juice flask rather than duplicate ordinary potions.

Apple totals are unchanged: late 64 golden plus five enchanted; endgame 256
golden plus 20 enchanted, including the box. Bunnidog Astralite/artifact gear,
native upgraded tool components, power settings, ammo and elite flask contents
remain. Endgame's remaining free slots still contain the appropriate totems.

Main stock is checked against 36 slots and boxed stock against 27 before the
inventory replacement commits. Filling with totems happens after real supplies
and equipment. Do not silently truncate future additions, including the flask.
The existing native kit scenario exercises all eight family/stage combinations,
exact quantities, real box placement and compatible enchantments.

## Reference inventories and recommendations

Looked up 2026-10-27. PvP modes are specialisations, not a universal Survival
progression. These sources inform utility and layout; they do not supersede the
player's requested apple quantities, Prot IV/no-Thorns baseline or mod infusions.

- [ACKCRAFT's published arena kits](https://wiki.ackcraft.net/en/arena/kitler/)
  separate basic iron shield/axe play from enchanted Diamond and potion-heavy
  Netherite loadouts. Its mace inventory explicitly includes two wind-charge
  stacks. This supports staged utility rather than handing every item to early
  kits. Its hunger-off arena counts are not Survival food requirements.
- [Carpet-PvP's actual kit tables](https://github.com/AndrewCTF/Carpet-PvP/blob/main/docs/Kits.md)
  show an SMP inventory combining repair XP, healing, buffs, pearls, webs and
  water, alongside separate axe, mace and crystal configurations. This supports
  keeping active supplies accessible and moving optional tools into reserve.
- [Neikiri's 26.2 UHC inventory](https://github.com/neikiri/minecraft-kits/blob/main/26.2/pvp/uhc-kit.txt)
  uses an Infinity bow and complementary healing/movement buffs. Its disabled
  natural regeneration is a mode-specific rule, not a change made to this mod.
- [Neikiri's crystal inventory](https://github.com/neikiri/minecraft-kits/blob/main/1.21.11/pvp/crystal-kit.txt)
  pairs crystals with obsidian, wind charges and totems. It is explicitly a
  1.21.11 example; native 26.2 components come from this checkout, not that give
  command. Its Blast Protection choice is not applied to the player's existing
  general-purpose Protection IV benchmark.

Suggested next choices, **not added in this pass**:

| Stage | Proposed choice | Why |
| --- | --- | --- |
| Early | An optional combat-focused arrangement keeping sword, axe, water, lava and blocks in the hotbar | Makes shield breaking, cover and bucket play available without inventory sorting. Keep the current mixed survival kit as the default. |
| Mid | Moderate Protection II/Sharpness II enchants and a few healing supplies | Gives Diamond preparation a useful middle step before max-enchanted Netherite, following the published staged kits. |
| Late | Replace the boxed spare steak with repair XP; offer splash Healing II as an alternative to extra totems | Supports Mending during longer fights while keeping the main food stack, and gives an active recovery option. |
| Endgame | Optional crystal-focused and mace-focused variants, rather than adding every counter to the default | Crystal can exchange reserve utilities for webs/anchor supplies; mace can prioritise movement and an alternate Breach mace. Keep current Density/Crystal mixed kit and requested quantities intact. |

Anchors/glowstone, a second specialised mace, tipped arrows and spare armour are
candidate trades for limited slots, not unconditional extra stock. Ordinary
Infinity arrows do not make tipped arrows or crossbow shots free. No changes to
world combat, player damage rules, item recipes or armour-design work are part
of this kit pass.
