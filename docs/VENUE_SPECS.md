# V3/V4 — Regional kennels and public Show Ring arenas

Implemented 2026-10-19 from the original regional-kennel and Show Ring arena
sketches. These are intact places to establish a line or bring an earned entrant,
complementing the abandoned barn restoration site. They reuse ordinary housing,
workshop and G3 judging rules. Current required client/server protocol is **13**.

## V3 — three regional kennels

Each native template is **23 × 8 × 23** with a sheltered, fenced two-parent
yard and a separate breeder's room. The structure follows its biome:

| Structure id | Biomes | Materials |
|---|---|---|
| `bunnidogs:meadow_kennel` | Plains, sunflower plains, meadow, cherry grove | Pale terracotta, oak posts, guava gable and a grassy yard |
| `bunnidogs:desert_kennel` | Desert, savanna | Cut sandstone, acacia posts, a flat shade roof and sand yard |
| `bunnidogs:snowy_kennel` | Snowy plains, snowy taiga, taiga | Spruce, stone bricks, a gabled roof and firm coarse-dirt yard |

All have two floor Nests inside the animal shelter, one Breeding Pen, an empty
Feeding Trough, Juicer, Genetics Table with an empty Specimen Jar, empty Nursery
Cradle, blue bed, composter, hay and a guava sapling. The two Nests are two blocks
apart and within the Pen's four-block reach. The house opens into the yard;
an outside gate also admits animals. Native rotation includes the actual Pen,
gate, bed, stations and paths.

Bring the parents, claim their Nests and supply both mangers. Existing twelve-
block adult isolation, readiness, native walking/visibility, real food costs
and courtship apply. More nearby adults prevent accidental pairing. No regional
alleles, seed, animal, villager or stocked station is injected. An unemployed
villager brought to the Pen can use the existing native Breeder career; the
template does not appoint one or lock a merchant. Alter fences/shelter as needed
for an individual line and any obstacles preserved during generation.

One deferred-loot supply chest yields a Field Guide and three to five rolls of
plain guavas, bone meal, Ear Tags, string, leather or a rarer golden guava.
It supplies an establishment task, without replacing collection or breeding.
The native **Room for a Lineage** location advancement accepts any of the three.

## V4 — Show Ring arena

`bunnidogs:show_ring` is a **31 × 7 × 31** open-air showground in plains,
sunflower plains, meadow or savanna. A wide entrance leads to an eleven-block
ring, three rows of spectator stairs and a roofed judging dais. The dais holds
one ordinary Judging Stand and three empty Trophy Pedestals under bronze,
silver and gold color markers. Two shaded holding bays each provide a floor
Nest, trough and gate. They contain no automatic breeding controller.

Lead an entrant to the gold floor mark near the stand. That position is within
the native six-block judging range. G3 checks birth parents, all seven fixed
pairs, adulthood, health and balanced bred stats exactly as at a player-built
stand. Discovery changes no eligibility, animal or award history. A placed
arena awards a real qualified representative once; repeat use cannot create
another ribbon. It grants no free winner, score bonus or rare material.

The supply chest has a Field Guide and lead, plus three to five ordinary rolls:
guavas, string, Ear Tags, white wool or the three display-color dyes. Ribbons,
pedigree winners, Marrow and legendary drops never appear in this loot. The
native **The Showground** location advancement records discovery. G3's existing
Champion Line advancement still requires actual gold judging.

## Placement, maps and saves

The new `bunnidogs:breeder_venue` structure type decodes a native start-pool
holder, named jigsaw anchor and bounded site radius. Minecraft 26.2 native pool
pieces retain their own rotation, chunk clipping, serialization and loot.
The authored `*/v1.nbt` resources are permanent saved-start identifiers; a later
geometry revision receives another name. Existing barns and warren formats stay
intact, and already-generated terrain receives no retroactive venues.

- A shared venue placement set uses spacing **32**, separation **12**, salt
  **631447208**, and excludes candidate warren-set chunks within three chunks.
  Each regional kennel has weight **3**, arena weight **1**; native placement
  tries entries against their biome tags. One chosen venue per placement keeps
  the new buildings apart. Existing barn placement remains unchanged.
- Kennel and arena preflights sample square radii **12** and **16**, at four-
  block intervals including both edges and center: **49** / **81** terrain
  columns. They require supported dry dirt/grass/sand/Overworld stone/snow block
  and at most **one block** of height difference, building at the highest
  support with native beard-thin adaptation. The start compensates for native
  anchor/ground offsets so template floor y=0 replaces that supporting surface,
  rather than sinking a block. The same correction applies to new barn starts;
  already-saved piece positions remain authoritative. Sampling reads base terrain before
  decoration, rather than promising a tree-free surveyed clearing. It cannot
  describe every unsampled block or every other structure's eventual placement.
- The existing native barn-preservation processor skips existing block entities,
  fluid and protected/ore-tagged cells even for air carves. Replaying a saved
  piece keeps opened chests and player-stocked stations. Ordinary replaced cells
  can rebuild on explicit replay; this is not a build restoration system.

Journeyman cartographers may sell a **Kennel Explorer Map** for
**12 emeralds + a compass**; expert cartographers may sell a **Showground Explorer Map** for
**16 emeralds + a compass**. Both have six native uses, normal discounts/demand
and XP (10/15). The kennel tag accepts all three regions; the arena tag accepts
the showground. The native search uses a house marker, radius 50 and skips
existing chunks. Failed searches discard the offer rather than sell blank maps.
Existing Warren and Barn map offers remain additive. Natural search distance,
availability and appearance still require gameplay review.

Use `/locate structure #bunnidogs:regional_kennels` or
`/locate structure bunnidogs:show_ring`. The four individual structure ids also support native
`/place structure` with the same preflight.

Art/data source: `art/gen_breeder_venues.py`, using the deterministic native NBT
writer. No new animal mesh, renderer, inventory format or payload is introduced.
The Field Guide appends index **112** and pages **113–116**: **116 pages, ten
indices, 88 chapter links**. More chapters now links Workshops & sites, which
keeps the existing workshop index reachable alongside barns and new venues.
Old page numbers and intentionally customized book contents remain intact.

## Evidence

Native fixture coverage loads every shipped palette/property and all four
structure codecs, makes actual dry-site starts, and places each of four templates
in every rotation using real per-chunk native bounds. It claims both kennel
Nests and spends two real guavas through the actual Pen in every regional
rotation, then judges an earned gold entrant once at the arena's marked place.
Saved piece replay preserves genuine chest stock and occupied pedestals;
protected preexisting containers, fluids and ore remain. The guide's customized
native item round trip encodes all appended pages. See `START_HERE.md` for the
combined final build/native result.

The four generated layout previews and guide text preview are separate evidence.
GPU appearance, native map routes, natural structure frequency/terrain diversity
and player balance remain gameplay review.
