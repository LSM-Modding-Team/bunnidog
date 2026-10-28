# Crown dungeon enchantments — proposed room treasures

**Planning only.** This batch changes dungeon placement and route generation;
none of the enchantments below are registered, obtainable or added to loot yet.
Names, levels and numbers are proposed implementation targets, not shipped stats.

## What finding one should feel like

The Great Warren already demands exceptional equipment. A rare Crown should
offer more than a slightly larger Sharpness number: a crossbow that can shoot
through a firing slit, boots that let me cross a broken bridge, a reason to bring
my favourite bunnidog, or a way to interrupt a Lifebinder's healing. These are
new uses for existing gear, not another set of replacement weapons.

The building should explain the treasure. Optical books belong in the Glass
Gallery, companion equipment in the nursery, volatile techniques in the Furnace.
A player looking for one particular effect can choose that wing. Real enchanted
books, ordinary tooltips and the anvil communicate acquisition; no compulsory
manual, new skill screen, unsolicited stat dump or permanent HUD is needed.
The existing dungeon keys, Sovereign Core, genetics archive and generous generic
supplies remain valuable independently.

## Proposed first collection

| Enchantment | Equipment / levels | Intended effect and initial numbers | Place to find it |
| --- | --- | --- | --- |
| **Phase Thread** | Crossbow, I–III | A fully charged bolt can pass through **2/4/6 solid blocks** before returning to normal collision. It remains a real projectile, with normal travel time/gravity, 48-block total phasing reach and one normal hit. No armour/shield bypass. Spending **one Prism Cell per shot** buys the effect; without it the crossbow fires normally. | Glass Gallery: optical reading room and trial reward |
| **Refraction** | Prism/Sovereign Lance, I–III | After its first accepted hit, the beam forks into two rays at ±15°, each with **35/50/65%** of the primary shot's damage and half its remaining reach. A creature can take only one ray's damage per discharge. Adds **two Prism Cells** to the selected mode's normal cost; no free rays if payment fails. | Observatory's upper instrument chamber; higher levels after the Glass trial |
| **Voidstep** | Boots, I–III | **1/2/3 extra airborne jumps** between grounded rests. A jump gives ordinary jump-height lift and preserves horizontal movement, allowing recovery from a missed island edge. Each spends **4 XP points** and two durability; no XP means no jump. Recharge after two seconds on real solid ground, never by touching a wall, landing on an entity or toggling Elytra. Disabled while gliding/riding. | Observatory roofwalk side treasury |
| **Slipstream** | Elytra and its existing upgrades, I–III | A genuinely consumed rocket provides **+10/20/30% gliding acceleration for two seconds**. Duration refreshes rather than stacking; each activation costs two extra Elytra durability. Works with Flight-1 rockets and does not alter their item components, lifetime or fuel cost. Flight handling and turns, rather than passive immunity, are the reward. | Observatory survey cabinet and Glass reward pool |
| **Pack Momentum** | Bunnidog barding, I–III | When this animal lands an accepted hit, a **different bonded companion** attacking that same enemy within three seconds gets **+20/35/50%** damage on its next hit. A successful relay can continue through the pack, with a three-second recovery per animal. Encourages complementary companions; no attack-speed change, free flight, extra loyalty, knowledge or gene points. | Nursery tack room and Brood trial reward |
| **Intercession** | Bunnidog barding, I–III | An adult companion actively assisting its keeper within six blocks can intercept **15/25/35%** of one incoming keeper hit every four seconds. It takes that transferred amount as health damage and spends matching gear wear. It must be conscious and capable of assisting; it cannot intercept a lethal share of damage. No transfers between guardians, recursive interception, heart-debt transfer or Totem interception. | Brood Vaults' family memorial; higher levels after the Brood trial |
| **Blood Oath** | Swords and axes, I–III | Heal **10/15/20% of actual enemy health removed**, up to **2/3/4 hearts per accepted hit**, with a one-second recovery. Enemy brood can sustain a player while they fight a swarm; friendly pets, teammates, overkill, absorption, blocked hits and damage spawned by other procs cannot generate healing. Ordinary native healing cancellation still applies. | Heart Furnace's chained-heart reliquary |
| **Severance** | Swords and axes, I–III | An accepted melee hit suppresses that enemy's received healing by **35/50/65% for four seconds**. Refreshes one mark rather than stacking. This affects real Lifebinder drains and support healing, never permanently removes health or alters its existing heart-container debt. | Furnace cooling gallery and trial reward |
| **Aftershock** | Blastmaw Launcher, I–III | A successful blast leaves a visible short-lived ring that pulses once after 0.8 seconds, dealing **20/30/40%** of that victim's accepted primary health damage to that same surviving victim. No second crater, secondary explosion, recursive proc or change to the selected **3/6/9/12/15** explosion power. Costs **one additional Volatile Charge** when firing. Gives a Blastmaw-style follow-up without restoring the launcher's former baseline strength. | Furnace powder magazine |
| **Vein Resonance** | Astralite pickaxe, I–III | Sneak-mine an ore to resonate through continuous Moonstone and harvest up to **8/16/24 ore blocks** of that type within **3/4/5 blocks**, following native tool, permission, Fortune/Silk Touch and per-block durability rules. The lunar generator scatters ores rather than forming connected veins; the search therefore follows connected lunar rock, never jumps void or crosses protected blocks. Pays **one Prism Cell per activation**; stops rather than breaks a tool or crosses unloaded chunks. Works on lunar ores, not ordinary Moonstone or fortress masonry. First-dungeon preparation still requires extensive mining; later expeditions become faster. | Workshop's mineral assay room and Furnace reward pool |
| **Starfall** | Mace, I–III | On a genuine smash hit after at least six blocks of fall, create a **3/4/5-block** landing shockwave, dealing **25/35/45%** of the accepted main hit to nearby enemies and knocking them away. Primary victim cannot be hit twice. No terrain destruction or self-launch; one wave per fall, with extra wear of 4/6/8. | Upper observatory's fallen-star niche; rare Glass trial book |
| **Royal Reach** | Swords, axes and maces, I–III | **+0.5/1/1.5 blocks of actual melee reach** while held. Does not extend block interaction, sweeping radius, launchers or beams. No additional base damage. A strong, broadly useful final prize for fighting multiple armoured Apexes. | Crown treasury after victory; optional third-key archive has an independent rare book |

The proposed first implementation should start with Phase Thread, Voidstep,
Blood Oath, Severance, Vein Resonance and Pack Momentum. They cover exploration,
vanilla equipment, mining, companion builds and combat with distinct visible
payoffs. Refraction, Intercession and Starfall need the most careful native
damage/projectile integration and should follow once the common machinery works.

## Real choices and compatibility

- **Blood Oath and Severance are mutually exclusive.** Sustain while fighting a
  swarm, or stop the enemy support engine. Both retain normal compatible vanilla
  damage enchants and Mending/Unbreaking.
- **Pack Momentum and Intercession are mutually exclusive.** A group attacker
  and a bodyguard suit should serve different jobs. The book does not make an
  untrained or genetically incapable animal obey a new order. Hostile dungeon
  equipment is ordinary Astralite barding by default, not automatically enchanted
  with the full treasure collection.
- **Phase Thread conflicts with Piercing and Multishot.** Ordinary vanilla
  crossbows remain useful, and one ammunition item must not generate an unlimited
  swarm of terrain-piercing bolts. Phase Thread stops at protected
  controller/gate/socket materials; ordinary masonry may be phased.
- **Starfall conflicts with Wind Burst.** Choose a clearing landing or a vertical
  reset; Density/Breach retain their own native exclusion. Royal Reach should
  coexist with either playstyle.
- Royal Reach changes melee entity reach only. Refraction uses the lance's own
  targeting and native damage rules. Neither becomes a universal attribute bonus
  on every weapon or a way to reach a control through a closed dungeon gate.
- These are treasure enchants, not a universal requirement to survive. They must
  not increase a tool's harvest tier: Netherite still cannot mine lunar ores.
  Armour effects do not grant permanent stat/strength caps, erase heart debt,
  replace Heartbound's independent timers, or provide passive Resistance V.
- Existing damage levels can be very high. Proc damage uses real accepted damage
  and explicit per-activation limits; those limits apply to the new proc, not a
  cap on the player's existing Strength, weapon damage or infused drinks.

## Room rewards, abundance and discovery

**Do not append books to the current generic barrel/wing/vault tables.** Their
worst-case rolls already occupy all 27 slots. A book that is silently discarded
by a full container is not treasure. Plan dedicated, physically placed book
caches: a small locked cabinet or vanilla chest niche, with an independent table
and an explicit capacity budget. Retain working stairs and encounter headroom.

Proposed distribution:

- Two small optional caches per wing. One level-I book with **40% chance per
  cache**, drawn only from that wing's family. Distinct room dressing identifies
  likely themes. Supply barrels remain generic supplies.
- Each first trial victory delivers **two distinct books from that wing's pool**
  into its dedicated reward cabinet: one guaranteed level II, the other weighted
  70% level II / 30% level III. These are substantial rewards for two mixed waves.
- Crown victory stocks a dedicated treasury book chest with **two level-III
  books from different families**; one is Royal Reach or a highest-tier specialist
  technique. The rare final prize accompanies, rather than replaces, the existing
  Core, trophy and wealth.
- The optional third-key archive provides **one additional level-III book** from
  the general collection with a bias toward the observatory/companion books.
  Its saved seed/sample archive remains separately owned and unchanged.
- All book stock rolls once for that physical site/container. Looking, breaking,
  reloading, replacing a lost key, or paying a boss rematch does not reroll/refill
  it. Trade and ordinary anvils let groups share finds; no player-completion
  checks, invisible pity currency or personal hunting lock is required.

Roll only defined useful levels, not random combinations of curses and irrelevant
enchants. Names should communicate their basic purpose; native enchantment
descriptions can give a short explanation and the real ammo/XP requirement. No
ambient hints, lore wall before use, stat-tool bypass or new progression menu.
The dungeon becomes an expedition for specific build tools as well as raw wealth.

## Implementation approach for later

Use this checkout's resolved Minecraft 26.2 native enchantment definitions,
supported-item tags, level costs, exclusive sets, stored-enchantment books and
anvil semantics. Maintain English and the shared neutral Peruvian Spanish source.
Check those APIs before coding; older-version enchantment code
is not evidence. Keep these books out of the enchanting table, ordinary mob gear,
villager offers and generic random loot unless a later design explicitly adds a
source. Data packs should be able to change levels, supported items and pools.

Simple reach/flight modifiers can use native attribute/effect components where
they express the exact behaviour. Stateful jumps, phasing, relays and healing
need a small server-authoritative controller with per-action ownership:

1. Validate the actual equipped enchanted stack, active mode, authority, target
   and exact available resources. Compute the whole cost before committing.
2. Spend real cells/charges/XP only for a successfully started action. Refraction
   must not consume the base shot then silently invent the additional cells.
3. Snapshot relevant weapon enchants onto the real paid projectile/beam. Switching
   items after firing neither grants new powers nor erases the paid shot's state.
4. Apply native permission, damage, healing, protection, Totem and cancellation
   hooks. Track accepted health damage separately from requested damage. Secondary
   effects carry a non-recursive source marker.
5. Keep bounded per-entity cooldowns and per-action hit sets. Stop at unloaded
   chunks; no persistent chunk forcing, fake kills, duplicated drops or attack
   packets trusted solely because the client requested them.
6. Barding enchants derive from the native BODY stack and survive paid smithing,
   saves and baskets, never transfer to seeds/genomes. Interception must reserve
   its real guardian cost before reducing a keeper hit, so it cannot become free
   mitigation if an event refuses the transfer.
7. Use contextual effects: a brief jump mote, refracted beam, healing mark or
   small sound. Only show failure feedback after an attempted paid action. No
   persistent meters or automatic tool-stat panels.

Before shipping any family, cover one real native action plus its meaningful
failure paths: no ammo/XP, cancelled spawn/hit/heal, interrupted charge, native
save/reload, item swap, full book cache and repeated access. Vein Resonance needs
real protected-tool drops at a chunk boundary; flight/jumps need authoritative
movement and logout/death recovery; Intercession needs cancelled/lethal/Totem
hits and two-guardian recursion cases. Inspect room placements/actual animations
and play the resulting equipment naturally rather than treating test counts as
proof of fun or balanced Survival pacing.
