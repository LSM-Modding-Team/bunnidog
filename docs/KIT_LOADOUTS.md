# Combat testing kits

The command explicitly replaces the player's inventory:

```
/kit vanilla early replace
/kit vanilla mid replace
/kit vanilla late replace
/kit vanilla endgame crystal replace
/kit vanilla endgame mace replace
```

Use `bunnidog` instead of `vanilla` for the modded family. Existing
`/kit <family> endgame replace` is an alias for Crystal. Omitting `replace`
only prints the existing replacement instruction; permissions are unchanged.
Current inventories are not edited until the player invokes the command again.

## Current loadouts

- **Early:** iron equipment, normal food, shield and building supplies. The
  hotbar puts sword, axe, bow, pickaxe, water, lava, food and blocks first.
- **Mid:** Diamond equipment with Protection II, Sharpness II, Power II,
  Efficiency III and Unbreaking II where supported. Eight webs and two native
  splash Healing II potions complement food, buckets and pearls. No max-enchant
  jump or automatic Mending at this stage.
- **Late:** max-enchanted gear, 64 golden/five enchanted apples, one main steak
  stack and 64 boxed XP bottles instead of the spare steak. Shovel, hoe and
  crossbow are boxed; the axe stays accessible. Sixteen webs and bucket utility
  remain. Vanilla carries Strength II, Speed II, long Fire Resistance and two
  splash Healing II; Bunnidog uses its actual 32-drink infused flask.
- **Endgame Crystal:** 128 crystals (64 carried, 64 boxed), 192 obsidian
  (64 carried, 128 boxed), 64 anchors, 64 glowstone and 64 webs. The Density/Wind Burst mace is boxed with
  the trident, crossbow, axe, shovel and hoe. Its hotbar prioritises crystals,
  bases and placement tools rather than the backup mace.
- **Endgame Mace:** a Density V/Wind Burst III mace is directly accessible;
  a separate Breach IV/Wind Burst III mace is boxed. These incompatible damage
  enchants never coexist on the same stack. No crystals, obsidian, anchors or
  glowstone; 64 cobblestone and 64 webs replace the explosive supplies. The
  hotbar prioritises the mace, wind, pearls, water, sustain, webs and blocks.

Both endgame variants retain 128 XP bottles, 128 wind charges, 192 Flight-1
rockets, one 64-arrow stack, 256 golden/20 enchanted apples, native Riptide III
trident (no Channeling/Loyalty), Infinity bow (no Mending), elytra and ordinary
max-enchanted gear. Other durable gear keeps Mending I and Unbreaking III.
Vanilla has the Strength/Speed/Fire Resistance potions plus two Healing II
splashes. Modded variants retain Astralite/artifact gear, all ammo, power
settings, Calipers/guavas and the unchanged elite 96-drink flask; ordinary
potions do not crowd out that stock.

Endgame has **two real Shulkers**: a blue **Equipment** box (backup weapons,
shield, specialised tools, potions and mod ammo) and a red **Supplies** box
(bulk refills). Names follow the player's Minecraft language, including the
shared Spanish source. Keep one stack of active pearls, wind charges, rockets,
XP bottles and golden apples accessible, plus enchanted apples, the pickaxe and
Elytra. Modded endgame also exposes its flask and one Prism Cell stack. The bow,
arrows, extra pearls/wind, optional crystal anchors/glowstone/webs, secondary
weapons, Calipers/guavas, launcher, Siphon and Standard move into the appropriate
box. Every previous tool, apple, potion, rocket, flask and ammo total is retained.
Late kits keep their existing single Shulker.

All remaining main/boxed slots become real nonstackable Totems. Vanilla endgame
uses ordinary Totems. Bunnidog endgame fills in a repeating normal/normal/
explosive pattern, including box spare slots, instead of filling exclusively with
Cataclysm Totems. Its hotbar emergency slot and offhand start with normal Totems;
explosive backups are readily available in the main inventory.

| Endgame loadout | Main Totem slots (of 36, including hotbar) | Normal / explosive in main | Offhand |
| --- | ---: | --- | --- |
| Vanilla Crystal | 22 | 22 / 0 | Normal Totem |
| Vanilla Mace | 21 | 21 / 0 | Normal Totem |
| Bunnidog Crystal | 20 | 14 / 6 | Normal Totem |
| Bunnidog Mace | 19 | 13 / 6 | Normal Totem |

Both boxes also contain Totem refills in their unused slots. The usable kit
requires ordinary manual retrieval/re-equipping; there is no automatic Totem
replacement or remote access to Shulker contents.

## Hotbar organisation

These are actual moved stacks, not duplicate preview items. The remaining main
inventory keeps immediate combat supplies and mostly Totems. Late uses one
real 27-slot Shulker; endgame uses two, with equipment separated from bulk refills.

| Slot | Early | Mid | Late | Endgame Crystal | Endgame Mace |
| --- | --- | --- | --- | --- | --- |
| 1 | Sword | Sword | Sword | Sword | Sword |
| 2 | Axe | Axe | Axe | Crystals | Density mace |
| 3 | Bow | Bow | Bow | Obsidian | Wind charges |
| 4 | Pickaxe | Pickaxe | Pickaxe | Pickaxe | Pearls |
| 5 | Water | Water | Water | Pearls | Water |
| 6 | Lava | Lava | Lava | Golden apples | Golden apples |
| 7 | Steak | Golden apples | Golden apples | Wind charges | Webs |
| 8 | Planks | Pearls | Pearls | Water | Cobblestone |
| 9 | Cobblestone | Cobblestone | Totem | Totem | Totem |

For Bunnidog late/endgame, the appropriate lance replaces the sword in slot 1,
while the flask occupies slot 6. Endgame boxes the backup sword, keeps golden
apples accessible and starts with normal Totems in slot 9/offhand. Late retains
its prior main-inventory sword and Cataclysm Totem setup. Earlier kits use an
offhand shield.

Supplies are checked against 36 main slots and each independent 27-slot box
before inventory replacement. Fillers are added last, and hotbar arrangement transfers existing stacks without
creating extra supply. Do not silently truncate future kit additions. The native
kit scenario covers all eight original family/stage combinations plus the four
explicit endgame family/variant combinations, compatible enchantments, supplies,
real named/coloured boxed alternatives, all refills, both Totem types and
majority-Totem main inventories. The focused native kit selector is
`./gradlew runGameTestServer -PgameTestFilter=bunnidogs:kit_*`.

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

The previously suggested organisation, midgame enchants, healing supplies,
late-game repair XP and endgame specialisations now ship under the explicit
follow-up request. The table above records implementation rather than proposed
extras. Tipped arrows and spare armour remain future possible trades for limited
slots, not added stock. Ordinary Infinity arrows do not make tipped arrows or
crossbow shots free. This inventory pass does not change world combat or recipes. Current creature
armour is recorded separately in `BARDING_SPECS.md`.
