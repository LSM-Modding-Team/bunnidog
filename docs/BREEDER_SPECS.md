# Breeders, abandoned barns and genetic redstone

Implemented 2026-10-19 from the original `DESIGN.md` Breeder, abandoned breeding
barn and Pedigree Comparator proposals. These give the completed workshop an
economy, an exploration site and live genetic sorting. That checkpoint used
network protocol **11**; current H11 uses **12**, so update client and server
together. Existing animal, seed, sample, pedigree and warren saves retain their
formats. Camouflage is `CARE_SPECS.md` A10.

V3/V4 adds three intact regional kennels and a public Show Ring arena alongside
the abandoned barn. Their housing/workshop stations begin empty and use existing
mechanics; native explorer maps and supplies are described in `VENUE_SPECS.md`.
Current required protocol is **13**.
The V3/V4 placement review also corrects new barn starts for both native anchor
and ground offsets, so their floor replaces the supporting surface rather than
sitting one block below it. Saved starts retain their recorded positions.

## V1 — native Breeder profession

An unemployed adult villager can claim a **Breeding Pen** as a job site. Every
Pen state belongs to a new one-ticket POI, separate from the animals' Nest POIs.
Native villager assignment, work schedules, XP, career locking after trading,
restocking, reputation, curing and merchant save/network handling apply.

The green apron has leather straps, copper pockets and an embroidered guava.
Villagers and zombie villagers have profession textures; existing biome clothes
and level badges remain native. No animal model or custom merchant UI is added.
The villager does not inject food into a Pen: G6 courtship still pays real stock.

Base offers, before native price changes:

| Rank | Offers | Uses per restock |
|---|---|---|
| Novice | 20 plain guavas → 1 emerald; 1 emerald → 4 Ear Tags; 2 emeralds → Field Guide | 16 / 12 / 4 |
| Apprentice | 4 regional pelts → 1 emerald; 4 emeralds → Sampling Syringe | 12 / 4 |
| Journeyman | 8 sinew → 1 emerald; 10 emeralds → Breeding Pen | 12 / 4 |
| Expert | 12 emeralds → Juicer; 8 emeralds → golden guava | 3 / 6 |
| Master | 32 emeralds + 4 diamonds → diamond guava; 24 emeralds + 1 Mutant Marrow → Gene Splicer | 3 / 2 |

The apprentice contract follows the villager's native variant: plains **tan**,
desert **sand**, savanna **cream**, taiga **brown**, snow **snow**, swamp **moss**,
jungle **spotted**. Only the matching contract and syringe generate. A third-party
variant without one of those predicates still has the syringe; datapacks can
append its contract. Existing offers persist natively when a villager moves.
Higher guava tiers and wild-only alleles retain their existing acquisition paths.
The Splicer's actual rare Marrow payment preserves its progression gate.

Minecraft 26.2 uses native `villager_trade`, `trade_set` and trade tags. These
editable JSON files supply all offers and conditions; no old trade event hook,
custom payment code or currency ledger is involved.

## V2 — abandoned breeding barn

`bunnidogs:breeding_barn` is a rare Overworld structure in plains, meadow,
savanna, taiga, snowy plains, cherry groves and forest biomes. Random-spread
placement uses spacing **36**, separation **14**, salt **784901663**, and excludes
candidate warren-set chunks within three chunks. Twenty-five terrain samples
cover a radius of ten blocks. All need dry dirt/grass/sand/stone/snow-block
support, with at most **one block** of height variation. Other base surfaces,
sampled water and steep ground reject the site. Native beard-thin terrain adaptation
fits the small foundation. This is a clearing preflight, not a claim that every
natural-world terrain arrangement has been manually inspected.

The authored **19 × 11 × 19** template has weathered guava walls, a holed roof,
open doorways, a hay loft, hanging lantern, empty Juicer, Genetics Table,
Specimen Jar and Trophy Pedestal. The side paddock has a Pen, two floor Nests,
hay and composter. Its missing rail is a deliberate restoration task; enclose
the paddock before bringing animals. A sapling waits beside the building.
No animals, villagers, seeds or stocked station inventories are injected.

One native deferred-loot chest contains a Field Guide and three to five ordinary
supply rolls: guavas, bone meal, Ear Tags, leather, string, sinew, or a rarer
golden guava/syringe. Loot has no Marrow, legendary drops or wild-gene sample.
The structure grants the native **Old Breeding Grounds** discovery advancement.
Journeyman cartographers may add a **Barn Explorer Map** for 10 emeralds and a
compass. It uses a native house marker and the barn structure tag; a failed
native search discards the offer rather than selling a blank map. Existing
Warren Explorer Map and vanilla offers remain in the pool.

Native pool pieces own rotation, chunk-bounded placement and serialization.
Their processor preserves **every existing block entity**, fluids, native
`features_cannot_replace`, common-tag ores and ancient debris, including air
carves. Replaying a saved piece therefore preserves opened supply stock and
station/player containers. Other ordinary blocks can be rebuilt on an explicit
piece replay; this is not a player build-restoration system.

`breeding_barn/v1.nbt` is a versioned native template resource. Future geometry
revisions get another identifier and retain v1 for saved starts. Existing
warrens, structure starts and generated chunks are unchanged. Find new ones with
`/locate structure bunnidogs:breeding_barn`; `/place structure` uses the same
dry-site preflight. Sources are `art/gen_breeding_barn.py` and the deterministic
`art/nbt_template.py` writer, using the resolved 26.2 data version **4903**.

## R1 — Pedigree Comparator

Craft `QGQ / CWC /  R `: two quartz, glass, two copper, a whisker and a native
comparator. The new metal instrument's front lens faces its one-block reading
pad. Imprint it with a Tissue Sample from either hand. This **copies information**
without consuming or changing the sample. The full `genome/stats` reference
saves on the block entity; only its genome determines output.

The front block and the air above it must be empty and loaded, inside the world
border. Exactly **one living bunnidog's feet** must be inside that one-block pad.
Pups are eligible; the Apex is unavailable. A body merely intersecting the edge
does not count. Blocked, empty, crowded, unconfigured or unavailable pads output
**0**. The detector never moves animals or requests chunks.

- **Exact**, the default: output **15** for matching all seven gene pairs;
  otherwise **0**.
- **Analog**: output **1 + 2 × matched gene pairs**, from 1 to 15 when one animal
  is present. A zero-pair reading is 1, distinguishable from no reading.
- Pair matching includes **hidden copies**, with maternal/paternal order ignored
  within each pair. Bred stats, age, tags, health, parents and visible dominance
  alone do not decide a match. Saved allele order is never rewritten.

Both modes expose ordinary weak redstone power and native comparator output on
all sides. A scheduled check runs every **10 ticks** while the chunk ticks;
placement/load resumes checks. The copied-reference lens and live-output lamp
are distinct multipart overlays. There is no continuously ticking block entity.

Use **both hands empty** normally to switch modes. Sneak with both empty to
clear the reference. Calipers from either hand inspect mode, seven-pair result,
current output and copied genome without changing anything. Adventure players
can inspect; writing/mode/clear actions require normal build permission. Reading
can differ from the last output for up to half a second between checks.

There is no hopper inventory or physical sample to recover. Breaking drops the
instrument once and clears its information; re-imprint it from the retained
sample. Pistons cannot move it. Native block rotation/mirroring follows the pad.
Use ordinary gates, water or rails to route animals; the instrument supplies the
genetic signal rather than implementing a second transport system.

## Evidence

Java 25 compilation and all **85** native scenarios pass at the implementation
checkpoint. Focused native coverage includes:

- actual Pen POI claim and native profession assignment; each career rank and
  all seven regional pelt predicates; real two-input payment/refusal, XP/uses,
  native merchant reload/restock and traded/untraded career behavior;
- actual shipped NBT/property validation, dry-site/native-start planning, all
  four native piece rotations, complete furnishings/doorways, real deferred
  chest loot, saved native piece replay and preserved containers/fluids/ore;
- real sample/empty-hand/Calipers controls, hidden versus expressed copies,
  reversed pair order, pups/bred-stat independence, native comparator and lamp
  signals, moved/blocked/crowded pads, saved reference and Adventure refusal.

The default model gate and final combined build are recorded in `START_HERE.md`.
Breeder apron, barn layout, instrument overlays and 109-page guide previews are
inspected separately. Headless fixtures establish native behavior; GPU appearance,
natural barn frequency, cartographer route searches and play balance remain
manual gameplay review.
