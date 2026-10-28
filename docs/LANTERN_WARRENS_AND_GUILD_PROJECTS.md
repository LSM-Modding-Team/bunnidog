# Lantern Warrens and material projects

Implemented 2026-10-28. This is the current implementation record for the
accepted [material economy proposal](GREAT_WARREN_MATERIAL_ECONOMY_PLAN.md) and
the player's request for peaceful underground towns, proper bridges, supported
dungeon lights and a more complete Crowned fight.

The subsequent polish replaces generic workshop chests with native deliberate
input/result menus, adds charge/fuel bars and recognizable item icons, and
finishes real wagon hitches, stable town routing and kitchen irrigation. Current
interaction/evidence details are in `COHESION_PLAN.md`; protocol 46 requires
matching client/server menu content.

## A journey with places to return to

Lantern Warrens are peaceful, inhabited underground towns in **new Great Warren
islands**, separate from Crown dungeons. Their random-spread set uses 32/16-chunk
spacing/separation, then rejects sites without sufficient island rock. They are
less common than ordinary 20-chunk ruins, much more common than 512-chunk Crown
destinations, and never consume a Crown island. `/locate structure
bunnidogs:lantern_warren` uses the native registry.

A stone gatehouse, beacon and graded surface entrance lead down four switchback
stair flights. Below is a vaulted market with a fountain, four awning stalls,
reading alcoves and side galleries. Two upstairs residential wings have beds,
storage, supported hanging lamps, small tables and shared sitting areas. A lit,
covered-water-irrigated kitchen garden, furnished workshop and
large portal hall make a working district rather than an empty stone shell.
Arches, pillars, stairs, roof beams and continuous lantern chains are actual
blocks. The 30 native chunk templates come from `art/underwarren.py`; placement
clears only the authored occupied volume, with detached-ProtoChunk safety and
saved chunk completion. Later realm landmarks respect its footprint.

The town heart attempts eight residents once, after its full known footprint is
loaded. These are large, peaceful **Lantern Warren Elders**, with varied Cave
genomes and strong inherited cognitive capacity, rather than hostile Apexes.
Two each work as Rootmasons, Armourers, Quartermasters and Waykeepers. They walk
between work and residential areas, pause to face a trading customer, and flee
danger. The town suppresses new natural monster spawning locally; it does not
erase active attackers or protect an expedition outside.

Native Minecraft trading shows actual offers. Each merchant owns a saved
27-slot stock; purchases remove its real output and add the paid inputs. Offers
cannot refill from reopening or reloading. Workers make frames, cells, bedding,
bread and rescue Totems from their actual supplies. Quartermasters harvest only
their own kitchen's mature wheat, store native crop drops and spend a real seed
to replant, respecting native griefing/protection hooks. Additional production
depends on crops or player-supplied ingredients. There is no free timed reset.

Waykeepers exchange one ordinary Totem plus **48 Astralite Shards** for a
**Homeward Totem** bound to that actual town. Other offers exchange common bulk
materials for frames, cells, dressings, bedding or food. They do not sell raw
Astralite or bypass the pre-dungeon lunar mining loop.

## The Lantern network and rescue

An existing town's heart registers its exact world-owned location. Walking into
its visible portal travels to another real town; direct gate use also travels,
and crouch-use cycles known destinations. Brief feedback appears only on use.
When the network knows just one town, explicit first portal use can reveal a
seed-valid neighbour: a bounded placement search chooses at most two candidates
and generates only their actual native structure footprints. It must find the
real generated town heart before admitting a destination. This does not paste
emergency rooms, search during background ticks or keep a distant town loaded.

Travel loads the known landing neighbourhood, validates the original heart and
safe landing, and applies a short anti-bounce cooldown. Blocked or destroyed
destinations fail safely. Normal gate travel refuses mounted players and players
carrying passengers. No inventory, cargo or companion is cloned.

A Homeward Totem uses native death protection. **Only a confirmed successful
pop** spends it and attempts rescue to its saved town, including from another
dimension. A blocked landing leaves native Totem survival intact. Existing
Lifebinder debt repayment still applies. Directly use a gate while holding the
Totem to bind it to that town; merely walking through while holding it still
travels normally. The one-line tooltip states its purpose. There is no separate
teleport setup screen or persistent tutorial.

This authored guild culture is distinct from the unfinished I1–I10 autonomous
society roadmap. It does not teach all player animals every profession or prove
that wild families can construct equivalent cities.

## Paid material projects

One Guava Log crafts four **Root Fibre**. Nine-unit Fibre Rolls, Dust Pouches,
Sinew Bales and Moonstone Crates provide grid-sized intermediate ingredients;
each can be unpacked into its exact nine inputs. A Crate contains nine
Compressed Moonstone, equivalent to 81 Moonstone. Component-bearing animal
seeds, trophies and role remnants are never converted to anonymous bulk units.

The following bills expand all intermediates, starting with ordinary Moonstone
and refined Cinderite. Raw Cinderite smelts one-for-one; fuel is additional.
These costs do not alter the existing Astralite armour/tool path.

| Finished craft | Cinderite | Moonstone | Root Fibre | Other ingredients |
| --- | ---: | ---: | ---: | --- |
| Cinderroot Frame | 36 | 18 | 18 | — |
| Pack Armoury | 72 | 117 | 54 | 2 Apex Marrow |
| Resonance Binding Bench | 72 | 117 | 54 | 2 Prism Lenses |
| Nursery Hearth | 72 | 117 | 54 | 2 Chorus Hearts |
| Launch Aerie | 72 | 117 | 54 | 2 Aerial Membranes |
| Bastion Heart | 144 | 234 | 72 | 18 Shards and an actual Warren Ward |
| Kennel Wagon | 144 | 72 | 81 | 18 Sinew, 1 Chorus Heart, 1 Aerial Membrane |
| Empty Prism Battery | 72 | 135 | 45 | 9 Shards, 1 Lens, 1 Whisker |
| Empty Volatile Magazine | 54 | 45 | 36 | 1 Volatile Heart |
| Four Concord Field Dressings | 36 | — | — | 18 Sinew, 18 Dust, 1 Heart Spool |
| Four Nursery Bedding | — | — | 4 | 2 Sinew, 1 Dust |

Blocks use ordinary gray native inventory screens, actual carried stacks and
27 original saved slots. Workshops reserve workpiece/book/result slots with
remaining supplies below; other projects show actual supply stock and fuel state.
Old contents remain retrievable. Redstone pauses their work. Placing a station grants no animal
skill. Existing hauling/manufacturing can use physical stock and ordinary
recipes; the blocks do not teleport goods or replace workforce learning.

* **Pack Armoury:** repairs the deliberately selected barding by up to 120 durability for
  one appropriate tier ingredient, two Cinderite and four Sinew. Astralite uses
  one Shard. Wounds, enchantments, names and exact stack ownership survive.
  The real suit moves to its output after one paid job; stored spare suits are
  never selected and an occupied output blocks another payment. Native anvils/
  Mending remain alternatives.
* **Resonance Binding Bench:** consumes an actual compatible enchanted book,
  eight Cinderite, eight Dust and one Shard per sum of actually applied levels. It
  applies only higher supported levels within native maxima and compatible
  sets, preserving wear and other components. Redundant/unsupported parts of a
  mixed book do not prevent its useful compatible upgrades. The actual result
  owns the original equipment. It does not clone books, combine
  two equal levels into a higher one or introduce the still-planned dungeon
  enchantments.
* **Bastion Heart:** retains 24-block natural-spawn suppression and expands it
  to 48 while powered. One Cinderite supplies 1,200 loaded ticks; one Dust
  supplies 12,000. Redstone pauses fuel and expansion. It does not cancel active
  enemies, waves or projectiles and incurs no offline bill.
* **Launch Aerie:** spends two Cinderite and one Dust from stock to launch a
  player wearing a usable native glider. Crouch-use opens its supplies; that
  screen also has a paid Launch button which closes the menu on success.
  Unready direct use opens supplies with its actual blocker. It
  grants no wingless flight or teleport.
* **Nursery Hearth:** sheltered nearby pups can grow up to 25% sooner in loaded
  time, spending one bedding and one Guava per 600 active ticks. A station lease
  and per-pup timer prevent stacked hearths duplicating growth. No genetic
  ceiling, mutation or inherited education changes.

### Finite carried supply

A **Prism Battery** holds at most 512 Cell-equivalent units. Crouch-use refills
256 for 18 Cinderite plus 36 Dust. A **Volatile Magazine** holds at most 128
Charge-equivalent units; a 64-unit refill costs 18 each Cinderite/Fibre/Dust plus
one Volatile Heart. Empty shells craft empty. Insufficient capacity rejects the
whole refill without spending ingredients. Charge is a saved native component
on one nonstackable item.

Existing Prism/Sovereign Lances, flight bursts, Gene Siphon and Launcher spend
their same exact units. Ordinary ammo pays first, then reservoirs; mixed payment
cannot exceed available units. Using a battery directly activates an equipped
supported wing burst with its original wear/cooldown, shared with plain Cells.
Launcher modes remain power 3/6/9/12/15. **Field Dressings** fuel the Concord
Standard one-for-one alongside Spools, without becoming organs or Eye remnants.

### A physical caravan

The visible covered **Kennel Wagon** carries 27 actual freight slots, four actual
companion passengers and its owner driver. A sufficiently strong, trained adult
puller with Carrying capacity at least three is hitched using one real Lead.
The hitch is a native visible lead connection. Owner-used shears release it and
return its one Lead; native snapping/cleanup and repeated cuts cannot refund it
twice. Legacy paid hitches acquire their real connection on loaded reunion.
An owner-led companion transfers its existing paid lead into the hitch rather
than consuming a second one. Other keepers' or unresolved leashes are protected.
The Trail Whistle boards/releases the owner's nearby companions; empty-hand use
boards the owner as driver and crouch-use opens cargo. The puller is not copied
into a passenger slot.

Ground driving is slower with heavy freight and requires the actual valid
nearby puller. An unridden wagon follows that puller physically. Water, missing/
downed pullers, distance and unloaded terrain stop travel; no pet, freight or
route gets teleported. Native passengers preserve their own wounds, genes,
learning and equipment. Destruction releases the real cargo, wagon and hitch
Lead once. The wagon does not complete autonomous caravan diplomacy.

## Room-specific treasure

Fresh architectural stores have per-container tables which **split** these
whole-site budgets. Decoration count never multiplies a complete shipment.
`structure/crown_store_budget.json` records the split. Wing rewards are distinct
earned tables, so free entry freight cannot supply the first Astralite set.

| Complete-site storage group | Mod-material budget |
| --- | --- |
| Entry freight | 128–256 Moonstone, 64–128 Fibre, 64 raw Cinderite |
| Glass stores | 32–64 Dust, 16–32 Whiskers, 64 Cinderite |
| Brood stores | 64–128 Fibre, 32–64 Sinew, 128–256 Moonstone, 64 Cinderite |
| Furnace stores | 128–192 raw Cinderite, 128–256 Moonstone, 32–64 Dust |
| Earned Glass/Furnace rewards | 32–64 / 64–128 Shards |
| Earned Brood reward | 4 Dressings, 16 Bedding |
| Crown foundry vault | 192–256 Shards, 256–512 raw Cinderite, 128 Moonstone, 128 Dust, 256–512 Fibre, 64 Sinew |

A complete raid therefore yields **288–448 Shards, 576–896 raw-equivalent
Cinderite, 512–896 Moonstone, 384–768 Fibre, 192–256 Dust, 96–128 Sinew and
16–32 Whiskers**, plus earned kill parts. It can finance several pack upgrades
or substantial infrastructure, without paying for every project at once.

Vanilla treasure across the entire site is **32–64 golden apples, 4–8 enchanted
apples, 4–6 Totems, 8–16 diamonds and 1–3 Netherite Ingots**, plus modest XP,
pearls and Flight-1 rockets. The foundry vault uses at most 25 native slots;
books/Core/archive own separate stock. Native personal rewards, unique kill
drops, physical keys and original rematch payments remain separate. Existing
opened containers never refill. An old untyped unresolved supply table is a
single modest mod-material crate, not the new whole-site budget repeated.

## Architecture compatibility and evidence

New route spans have masonry arches, deep ribs, pale carriageways, parapets,
root-beam crossframes and recessed lights. Their five-wide interior has five
clear blocks above the road, graded native stairs/slabs, and preserved tunnel
roofs/hills. The independent island/Dijkstra/Crown placement remains unchanged.
An operator can explicitly run **`/bunnidogs bridges renovate`** to upgrade only
loaded surviving old road cells within 64 blocks. Containers, natural terrain
and other player blocks remain. No old/new route stitching is promised.

Fresh Crown lamps have continuous native iron chains to actual roof beams.
Existing **`/bunnidogs dungeon repair`** can restore missing chain cells above
surviving lamps when their anchor still exists, without rebuilding removed
lamps, resetting progress or touching stock. Towns require new terrain. Ordinary
loads never repaste old saves or force-load a whole settlement.

The full native gate passed **264 scenarios**, including finite/mixed ammo,
stock repair/book binding/destruction, fuel/nursery/launch, exact trading and
Totem pop, paid wagon hitch/passengers/cargo, all eight generated live residents,
detached town generation/replay, collision-aware walking to five key rooms,
and first-portal travel to an actual seed-valid naturally generated neighbour.
The Crown director scenario spans 1,600 native ticks across all three phases.
Build/model/localization gates passed. Final native-template previews are in
`art/lantern_warren/`, `art/bridges/` and `art/citadel/`, with template hashes.
These headless renders approximate minor native models and lighting; they do
not establish client performance or natural solo/co-op Survival balance.
Protocol **46** requires matching client/server content for the new entities,
blocks, items and components.
