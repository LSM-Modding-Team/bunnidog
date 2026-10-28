# Crown Warren and Sovereign rewards

> The material economy now ships. The tables below and
> [LANTERN_WARRENS_AND_GUILD_PROJECTS.md](LANTERN_WARRENS_AND_GUILD_PROJECTS.md)
> record split whole-site mineral/material shipments, useful paid projects and
> substantially reduced vanilla wealth. Existing opened inventories remain exact.

> Current lunar forge, equipment, structural palette, supply and AI changes are
> recorded in [LUNAR_DUNGEON_UPDATE.md](LUNAR_DUNGEON_UPDATE.md) and
> [BARDING_SPECS.md](BARDING_SPECS.md). Real Astralite equipment now replaces
> fresh dungeon actors' genetic/champion damage and health boosts.

> The architectural redesign now ships for fresh sites. The accepted design and
> implementation record are in [CROWN_WARREN_REDESIGN_PLAN.md](CROWN_WARREN_REDESIGN_PLAN.md).
> Old sites retain their original geometry and progress; they are not converted.

> Latest terrain/tier request: [ASTRALITE_AND_SKY_SPECS.md](ASTRALITE_AND_SKY_SPECS.md)
> supersedes the initial Overworld terrain and Netherite artifact bases below.
> Astralite is obtained before the dungeon; artifact armor keeps its new material.

Implemented 2026-10-25, continuing the accepted AX7 plan. Ordinary Apex combat
numbers remain those accepted in `APEX_SPECS.md`. This is a separate finale;
headless evidence does not establish its natural Survival balance or appearance.

## Finding and entering

Crown Warrens generate in **new Great Warren terrain**, through their own native
random-spread structure set: spacing **512 chunks**, separation **256**. This is
about **7.1x rarer by area** than the previous 192/96 placement, with candidate regions 8,192 blocks
across. Foundations and multi-island routes share that exact placement. The realm
supplies a fitted Crown island; the legacy non-realm fallback retains its bounded
nine-point terrain check.
`/locate structure bunnidogs:crown_warren` uses the normal structure registry.
Existing generated terrain, homes and stock are not retroactively rebuilt.

Fresh sites use **layout revision 2**, a bounded 240 × 240-block root-built
citadel with local foundation feet. The vaulted keep retains an approximately
85 × 85 playable boss floor and 32 clear vertical blocks, with side galleries
outside that envelope. The highest crown is at relative +67 (Y148 at the realm
floor), below the Y170 moon ecology band. Root feet extend down only where
authored supports need them, capped at 24 additional blocks. The island outside
the occupied buildings retains its terrain; existing paired returns never move.

A gatehouse with unequal stair towers leads through a furnished courtyard.
The wayhouse retains crafting, smithing, an Ender Chest and a Warren Ward.
Library, nursery and workshop approaches are independent of the sealed boss hall.
Store thresholds, stairs, gallery landings and returns form real walking routes;
no obligatory wing approach passes through the arena. The keep's side galleries
have stairs inside the hall, while the observatory roofwalk ends outside its wall.

| Wing | Architecture | Two finite waves |
| --- | --- | --- |
| Glass Gallery | Library, atrium and raised observatory | Prismmaw, Brute, Lifebinder, flying Brute; then ground/flying Prismmaws, Brute, Blastmaw, Lifebinder |
| Brood Vaults | Stores, seed kitchen, nesting court and nursery terraces | Broodcaller, Brute, Prismmaw, flying Brute; then Broodcaller, two Brutes, flying Prismmaw, Lifebinder |
| Heart Furnace | Workshop, freight stair, cooling gallery and chained-heart chamber | Lifebinder, Blastmaw, Brute, Prismmaw; then Blastmaw, Lifebinder, Prismmaw, ground/flying Brutes |

Occupied room geometry, details and furnishing come from reusable authored
modules baked into **188 native StructureTemplates**, owned by horizontal chunks.
Only authored interiors/headroom are cleared. Wall bays, deep arches, pitched
and vaulted roofs, stairs and foundations are actual blocks, not renderer-only
geometry. Rootstone fighting floors withstand Apex terrain attacks; small warm
recessed Rootstone lights and ordinary lanterns illuminate rooms without a
colored floor runway. Supply barrels/chests have finite generic expedition loot;
there are no stock-refilling room spawners or free exclusive Apex parts.
Every hanging light now has a continuous native iron chain to a real roof/beam.
The support pass runs after architecture, so later roofing cannot erase it.
Unsupported exterior lamps become supported standing lights. Existing V2 repair
can restore missing chain cells above a surviving lamp when its original solid
anchor remains; it does not replace removed lamps or rebuild the room.

There is one finite saved **guard-pair** attempt in each wing's approach room
and the entry precinct. It wakes only near a living Survival visitor, with its
known bounded room already loaded. The pair shares its controller with the trial,
grants no wing key/cache, and never rearms after death, reset or reload.
Approach guards do not create a boss encounter. A normal click on a hall's
physical seal starts the existing trial, including while holding a weapon.
Confirmed deaths advance waves; unload/removal never counts as victory.
Completed halls drop a glowing, site-bound key immediately, pay their original
one-time generic cache and replace lost keys on later use without refilling it.

### Dungeon difficulty above the realm baseline

The latest balance request supersedes the initial one/two-Apex trial groups.
Normal Great Warren land already spawns ground packs of two or three, which
readily overlap other roles and flying enemies. Dungeon fights now deliberately
exceed that baseline: **four Apexes in wave one, five in wave two**, with mixed
melee/ranged/support threats and an aerial member in each trial. Each wave has
at most one Broodcaller and one Lifebinder. Summon/lifetime limits remain intact;
there are still exactly two finite waves, not an endless reinforcement loop.

Fresh dungeon actors use the ordinary wild role lineages and 25–55-point combat
range, with no forced stocky build or high-point champion genome. All Apex
approach guards, trial actors and boss escorts wear actual Astralite BODY barding:
+14 armour, +5 toughness, ×2 health, ×1.6 damage, +8% movement and +0.20 knockback
resistance. Native saved equipment/modifiers survive enrage/reload without
stacking or healing. Fixed explosions also use equipment damage while retaining
their actual explosion power. Old active actors keep their saved legacy profiles;
new stages and rematches use equipment. Temporary ordinary brood receive none.

Trials freeze the starter and up to three nearby living Survival players at
engagement. Each additional participant adds 15% champion health, capped at
960 HP per actor to stay below native ordinary-Apex health limits. Saves and the
second wave retain the original party size; arrivals/departures cannot keep
rescaling a live fight. This tuning applies to new engagements in both old and
new layouts. Existing tracked enemies/paid progress are never replaced on load.
Wild and ritual Apex combat rules, global spawn tables and player equipment are
unchanged by this dungeon-specific pass.

**Two distinct matching-site keys** fit two actual seats beside the throne gate.
They preserve insertion order and open that gate permanently. The optional third
key fits a separate seat on the archive door, opens only that archive and earns
one frozen genetic archive. The treasury opens on boss victory. Shown offhand
keys/sigils retain priority alongside a main-hand weapon. Wrong-site/duplicate
keys are not consumed. Direct gate/seat interaction checks permission and reach
at the touched block, revalidates its controlling seal, and cannot accidentally
start a boss through the optional archive. Concise key feedback appears on use.

New seals save resolved world-space floors, room bounds, legal spawn pads,
patrol coordinates, gates, seats and reward niches. This prevents raised/lowered
rooms being interpreted as the old one-floor encounter radius. Placement configures
seals safely on detached ProtoChunks. A chunk completion journal and container
protection preserve interrupted generation/replay. Short encounter leases retain
only known already-loaded room chunks; simulation never loads an outer ring.

**Legacy layout V1** remains supported. Missing saved piece revision or controller
geometry always means the original 181 × 181 layout, with its old offsets,
wayfinding, three-seat control texture, 31-block arena roof, stock and keys. New
castle geometry never replaces it during load or repair. The latest rare Crown
placement and island journey are unchanged.

An operator standing inside a dungeon may run `/bunnidogs dungeon repair`.
It dispatches by the actual saved piece revision and touches loaded chunks only.
V1 keeps its original obstruction/marker repair. V2 clears natural obstructions
from authored air, restores missing controls/physical seats and only still-closed
saved gates. Existing containers, artificial player blocks and all stock/journals
are preserved; opened paid gates are never rebuilt. It never force-loads the site.

No tutorial menu, ambient instructions, mandatory guide chapter or automatic
stat panel is added. Seals use concise feedback only when interacted with.
Critical gates/seals retain their existing protections. Fresh structural walls,
roofs, floors, stairs, slabs, railings, beams, windows and rootlights use the new
lunar/rootstone palette: hardness 200, maximum finite explosion resistance,
Astralite harvest tier and explicit immunity to entity terrain clearing.
Furnishings remain ordinary interactive blocks. `/bunnidogs dungeon fortify`
upgrades matching old structural cells in loaded chunks at their actual template
positions, preserving shape, missing/player-edited blocks, inventories and ledgers.
It does not paste a fresh dungeon or refill an opened barrel.
Other player-made shortcuts remain useful; opening a route does not skip the
central seal's actual encounter conditions.

## Crowned Apex

A distinct entity (`bunnidogs:crowned_apex`) carries five colored crests and
resonant back organs on the existing animal sculpt. Its independent crown layer
follows actual body/head proportions; wings unfold in the aerial final phase.
The boss has its own native boss bar and remains measurable with Calipers.

Solo health is **6,000**, with 18 armor, 40 raw melee damage and movement speed
.46. The engagement roster freezes at the starter plus up to three nearby Survival players. Health
scales by `6000 × (1 + .55 × (players − 1))`, up to 15,900 for four players.
Visitors arriving later cannot inflate health or acquire an idle core entitlement.

A synchronized, boss-only `crown_health` attribute supplies a real native health
pool above vanilla's 1,024 bound. Native damage, healing, setHealth, saves and the
boss bar all use that pool. Global attribute bounds stay intact. Phase changes
preserve damage rather than replacing health bars or imposing per-hit caps.

| Health band | Pattern and openings |
| --- | --- |
| Above two thirds — The Hunt | Pounces, tracking beams, a three-volley barrage and paired detonations, with active pursuit/bites in recovery |
| Middle third — The Living Crown | Pounces and tracking beams alongside one finite active pack: champion Lifebinder, ground/flying Prismmaws, Brute and three elite brood |
| Last third — The Broken Crown | Continuous flight, tracking beams, three-volley barrages, faster pounces and paired detonations, with two finite flying champion Prismmaws; 56 raw melee damage |

The director retains the actual native HP, armour, party scaling and individual
attack damage. Ground pursuit uses speed .46 with a
1.15 navigation multiplier, and only stops when a real visible bite is possible.
Bites recover in 20 ticks. A scheduled pounce becomes a beam if the player is
already in bite range. The middle phase no longer loops the beam exclusively;
the final phase now uses beams too. Each major attack still has its own full
warning and the boss does not launch two majors simultaneously.

Pounces retain their 28-tick warning, updating the orange landing marker for
the first 20 ticks, then committing for the final eight ticks and the arc.
The colliding, terrain-clearing arc is 25% faster than the ordinary Apex's,
with the same loaded-world/protection/bounds checks. Post-pounce recovery is
22 ticks. Its actual landing produces a short-radius impact at .65 times melee
damage, with .8 knockback, matching the four-block warning ring. Native damage
hooks, line of sight, family safety and ordinary hit invulnerability apply;
the same target cannot bypass native i-frames by taking both bite and landing.
Ordinary ground/flying Apex jump and dive tuning stays unchanged.

Beams retain 34 harmless warning ticks and six damaging pulses over 60 ticks,
up to 64-block reach (clipped to the arena) and 32 raw piercing damage. They now
turn toward the current target, capped at **.012/.015/.018 radians per tick**
by phase after the initial aim acquisition (first 24 warning ticks: .055).
Even an exact target reversal obeys that angular cap. Sprinting closer to the
source, changing direction or using a quick movement tool can outrun the sweep;
the ray never instantly snaps onto a relocated player. The actual current ray
is also shown between damage pulses. Its source follows the mouth. Recovery
is 28 ticks. Ordinary Prismmaw tracking/fixed-origin rules stay unchanged.

The Living Crown summons its pack once, with legal targets assigned immediately.
All champions run native movement and attack AI: the Lifebinder can use hooks/
anvils/drain, and both Prismmaws reposition/fire normally. This replaces the old
NoAI support/caster scheme and its sparse boss-directed Prism shot. Old saved
middle-phase disabled casters resume once, without spawning extra actors or
re-equipping/refilling them; actual Resonant Stun delays that repair. The saved
pack flags, actor identities, Astralite gear and room boundaries remain.

Its visible support link retains 360 HP of finite healing income, spent only on
actual accepted healing, at most 12 HP per one-second opportunity. Ordinary
Lifebinder drain converts only real accepted damage into additional healing;
its reserve/cancellation rules remain. Killing support stops the link. Retiring
that summon at a phase transition/reset/victory releases only its outstanding
heart debt and stun, so no vanished caster leaves permanent losses behind.
Offline debt ownership remains in SavedData. Other unrelated casters' debts
are not removed by dismissal. Brood retains normal elite rules/finite lifetime.
The final escorts are attempted only once across reloads and cleaned at the
usual retreat/victory points.

Final flight orbits the current target roughly 12 blocks out and 12 blocks above
it, at controller speed .9 rather than a distant fixed chamber-centred loop.
The boss returns to flight during recovery; casts retain no-gravity, stop stale
ground navigation and remain fall-damage immune. Detonations retain the two
19-block warning circles and native power-nine explosions, with 32-tick recovery.
The first centre locks to the target's clamped arena position and the second
locks on the opposite side. This removes a permanent central camping lane,
while retaining all 60 warning ticks and fixed centres after commitment. Safe
routes depend on the shown pair of circles rather than a memorised spot.
The Living phase excludes detonations while its active
hook/anvil support is present. Family immunity, griefing rules, cancellation,
protected masonry, player movement tools and combat companions remain meaningful.

The Hunt and Broken Crown also use a **three-volley barrage**: each volley
fires two native power-four fireballs, locking aim eight ticks before launch.
Soul-flame rings and native sounds warn each shot. The whole cast lasts 64 ticks
then recovers for 24. Projectiles retain enemy Apex family immunity and native
griefing/protection, and only this boss's unfinished shots retire on a phase
transition. The Living Crown omits barrage/explosions while hooks/anvils provide
its extra pressure. Saved rotation position resumes across reloads and resets
at each phase boundary, so no attack can be skipped indefinitely by phase index.

## Saves, retreat and rewards

Each controller stores its site/run identity, confirmed enemy UUIDs, sockets,
completion, frozen contribution roster and pending rewards. Only an explicit
start loads its finite known encounter neighborhood. Short nonpersistent native
leases retain that already-loaded footprint and its actual controller chunk at low render
distance. Loading and simulation are separate: simulation cannot generate a new
outer ring. On reload, a saved nearby actor can recover its one known controller
chunk for up to three seconds; it never searches distant chunks. Reset/victory
release the leases, and unreached leases expire automatically. Wing ticket values
remain distinct, so one hall cannot cancel another overlapping hall's lease. Rejected/changed native spawns remove the unpaid pack and spend nothing.
A rematch consumes one **actually offered Crown Sigil**, after successful native
spawn/payment revalidation. Its recipe uses existing realm parts, not the five
original legendary offerings. Saved active rematches remain already paid.

Ten seconds without a living Survival player in the encounter, a missing tracked
actor, or Peaceful mode resets an unfinished fight. Actors are steered and physically contained within saved bounds.
Guards use the dungeon footprint; trials/adds use their own room. Targets outside
the allowed area are rejected. Displaced old actors return without being counted
as killed; no ordinary boundary crossing deletes an active enemy. Cleanup touches encounter actors and preserves player items,
blocks and earned reward ledgers. Old active actors cannot join a later run.

Each frozen participant needs 30 contribution points. Actual damage to the boss
or bound adds counts; owned companions credit their keeper. Accepted shield
blocking of an encounter attack counts at half value. Concord Standard healing
of another actual nearby engaged ally counts its accepted healing. Receiving
healing, standing nearby or merely entering later grants no credit.

The native post-damage callback occurs **after** native death. Victory therefore
waits for that callback (or the next controller tick for direct deaths), so the
accepted killing hit participates before rewards resolve. Cancelled deaths do not
advance the encounter. Normal mob-drop rules govern kill rewards.

A qualifying victory grants:

- One saved **Sovereign Core entitlement per qualifying player**, claimed at the
  seal. Offline/dead players can return to claim. Full inventory plus cancelled
  native item delivery preserves the entitlement; repeated claims/reloads do not
  duplicate it.
- A displayable Crown trophy carrying the actual dungeon and victory identity.
  Failed trophy delivery remains independently claimable after the core.
- A generic foundry vault: 192–256 Astralite Shards, 256–512 raw Cinderite,
  128 Moonstone, 128 Dust, 256–512 Root Fibre and 64 Sinew, at most 25 native
  slots. Unique role ingredients instead drop from the actual Crowned Apex: 16–24 of each of the
  five role parts, Marrow and aerial membranes. The boss pays 10,000 XP.
- A separately frozen archive: ordinary heritable winged End/Cave parents with
  70–100 stat points, quality 36–47 seed, matching reusable tissues, Mutagen and
  Starlit Dust. These breed ordinary bunnidogs through existing genetics; they do
  not grant the boss's separate health pool, an instant profession or a civilization.

Architectural stores split whole-site budgets by actual room: Moonstone/Fibre/
Cinderite freight, Glass Dust/Whiskers, Brood Sinew/bedding materials and Furnace
raw minerals. Each native container has its own declared share, recorded in
`structure/crown_store_budget.json`; adding decorative barrels cannot multiply
the complete budget. No free entry-store Astralite replaces pre-dungeon mining.

Earned Glass and Furnace caches add **32–64 / 64–128 Shards** respectively;
Brood grants four Dressings and sixteen Bedding. Each also has 8–16 golden
apples, 1–2 enchanted apples, one Totem and sixteen XP bottles. Together with
architectural stock and the vault, a complete site supplies **288–448 Shards,
576–896 raw-equivalent Cinderite, 512–896 Moonstone, 384–768 Fibre, 192–256
Dust, 96–128 Sinew and 16–32 Whiskers**, plus kill-exclusive parts.

Vanilla treasure across the entire site is **32–64 golden apples, 4–8 enchanted
apples, 4–6 Totems, 8–16 diamonds and 1–3 Netherite Ingots**, with modest XP,
pearls and Flight-1 rockets. The old unresolved untyped supply table becomes a
mod-material crate; existing actual contents never refill. Kits are independent.
Personal Core count, exclusive drops and rematch payments are unchanged. Temporary dungeon enemies
and boss adds do not provide ordinary ritual Crests, role parts or XP. Paid genetic
vacuum capture retains the user's existing exception for brood-to-seed capture.

V2 rewards sit in their respective wing niches or the gated treasury/archive,
outside the rematch arena. V1 reward plinths retain their original positions.
Pending deliveries remain in the original controller until their actual niche is loaded. Chests use empty plinths only; existing containers are never
replaced/refilled. Blocked vault/archive deliveries remain saved until space is
available. Archives store their exact whole seed/sample items once; they never
combine deferred loot tables with inventory contents or reroll on inspection.

## Planned room enchantment treasures

`DUNGEON_ENCHANTMENTS_PLAN.md` proposes powerful, specialised books tied to the
Glass Gallery/observatory, Brood Vaults, Furnace/workshop and final treasury.
This is **planning only**; none of those books or effects are in runtime loot.
Future book cabinets must own finite separate stock, since the generous generic
27-slot tables already have a full maximum-roll budget. Existing loot/Core/key/
archive journals remain unchanged by this proposal.

## Sovereign choices

Each smithing upgrade consumes one Core and the player's actual previous item.
Native smithing preserves names, wear, enchantments and other explicit components.

| Upgrade | Actual benefit |
| --- | --- |
| Heartbound piece → Sovereign armor | +3 equipped hearts per piece; retains independent Heartbound 60/45/30/15-second heart recovery. Mixed old/new sets work |
| Prism Lance → Sovereign Lance | 24-tick charge, 48-block piercing reach, 24-tick cooldown; powers 1–5 deal 45–225 damage and consume 1–5 Prism Cells/wear |
| Armored Elytra → Sovereign Elytra | Native glider, 10 armor/4 toughness, durability 1,296; same real cells, three wear and 30-tick burst cooldown |
| Core + parts → Concord Standard | 30-tick charge; powers 1–5 heal 5–25 hearts and grant four-second Resistance I–V to up to 16 allies, spending 1–5 Heart Spools/wear; five-second cooldown |

The Standard supports own living companions, native teammates and engaged
Crown-party players. It does not replace downed-animal revival or grant free
healing credit for self-healing. Interrupted charges spend nothing. Native fuel,
cooldown, permission and whole-item checks remain authoritative.

All new text is English plus the shared neutral Peruvian Spanish source.
Current protocol **46** requires matching client/server content. Original ordinary Apex
values and old gear IDs/saves remain compatible.

## Evidence and remaining review

The full native suite covers champion wounds/enrage/reload, frozen cooperative
wing scaling and four-to-five mixed waves, the larger finite phase packs, finite hall waves, actual key/hand/gate behavior,
entity/controller saves, truthful high health, phase/pack cleanup, damaging beam
warnings, sustained native aerial physics, abandoned-fight cleanup, frozen
multiplayer scaling, killing-hit contribution, cancelled spawns, paid rematches,
blocked/retried core and trophy delivery, smithing/equipment/fuel/support behavior,
and real detached `WorldGenRegion`/`ProtoChunk` placement/replay. Shipped realm
terrain admits an actual random-spread dungeon candidate in the native fixture.
The V2 native fixture places the complete furnished citadel and exercises all
four actual approach guards, all three trials, remote protected seats, ordered
keys, the archive/treasury, final reward, native saves and generation/repair stock
preservation. A separate unfinished-ProtoChunk fixture covers all four V2 seals;
missing-revision V1 saves remain covered. The shipped-NBT walking audit proves
independent closed-gate wing routes, legal pads/jambs and the clear Crown volume.
The suite now contains 264 scenarios. Existing play-review fixtures cover a
real saved launcher shot passing allies and damaging an enemy, friendly blast/
knockback immunity and explosion cancellation; native shield raising/blocking/
recovery and variant exclusion; a damaging tracking sweep plus a real escape;
old disabled-pack save migration, moving/casting champions and support debt
cleanup; and 1,600 native ticks of autonomous three-phase attack selection,
including the new barrage and sustained flight. The full 264-case native gate
passes, including guild stock, town generation/trading/portals and native Totem
rescue. The existing lunar forge, native
barding ownership/save, movement bounds and supply/masonry checks remain;
build/model/localization gates also run.
A repeat run exposed the pre-existing field test's open irrigation shaft under
a one-block-thick platform; that fixture now uses a real waterlogged cover so
random crop velocity cannot turn its hauling assertion into an unreachable void drop.

Model checks exercise actual crown geometry, UV bounds, head/body transforms and
visibility reset. Generated crown/gear previews remain covered. Final V2 architectural previews
in `art/citadel/` are rendered through Blender from the shipped native NBTs, with
per-template SHA-256 provenance. Exterior, gatehouse/court, library, stair/gallery,
observatory, Brood court/nursery, furnace and Crown hall are inspected. Neutral
ambient fill and approximate minor block shapes make this headless geometry
evidence; terrain, actual actors and native client lighting are not simulated.
These checks do not establish graphical lighting/animation, natural chunk-order
performance, full solo/co-op fight pacing or the new economy. Those need player
review. Additional rooms, regrowing cover, other Sovereign branches and the two
remaining proposed realm regions are future extensions, not prerequisites hidden
behind the implemented finale. Intelligence/civilization remains the next major
unfinished roadmap after this Apex expedition stage and reported fixes.
