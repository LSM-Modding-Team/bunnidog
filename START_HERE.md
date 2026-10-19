# Current project status

Read `AGENTS.md` for the continuation workflow, `README.md` for build/run commands,
and only the specifications/source relevant to the next batch. Historical sandbox,
upload and approval routines in `docs/history/` do not apply. User requests and
standing design latitude determine scope; a feature or push is a checkpoint.

## Project and delivery

Bunnidogs targets Minecraft 26.2 / NeoForge 26.2.0.88, Java 25 and Gradle 9.2.1.
Pure rules live in `genetics/` and `layout/`; native glue and client code remain
separate. A warren contains dens, mouths and tunnels; a settlement includes its
surface trees, houses, paths and landmarks (levels 1–5).

Work directly on `main`, preserving player saves and unrelated changes. Push
coherent validated batches under the standing continuation authorization.
The user downloads JARs from GitHub Actions artifacts; never attach/link a local
JAR in chat. Discover `get_usage_limits` at substantial starts/checkpoints. It
is not exposed in this session: use available work budget without inventing quota.
Reported percentages are sustained-run targets, including 100%; continue across
checkpoints/compaction and reserve only the capacity needed to deliver safely.

## Latest gameplay work — 2026-10-19

- **H11, planned Build carrying capacity:** ordinary adults equip one to four
  native bundles according to expressed Build. Whole contents/names/colors,
  last-first retrieval, excess retention and native equipment death/basket saves
  conserve actual cargo. Excess bundles impose a named 25%-each movement penalty;
  visible cargo breaks camouflage. Separate fitted panniers add no geometry/pass
  to unadorned animals and preserve the approved sculpt. Build/model/all **87**
  native cases pass at the cargo checkpoint; actual-sculpt preview inspected.
  Fresh guide: **111 pages, nine indices, 82 links**. Protocol **12** requires
  updated client and server. See `docs/CARRYING_SPECS.md`.

- **A10/V1/V2/R1, fieldcraft:** finishes coat camouflage, Breeder villagers,
  abandoned breeding barns and the live Pedigree Comparator from the original
  plans. Healthy calm coats blend on matching ground with reduced native sight
  and STEP vibrations. Native careers buy regional pelts and supply workshops;
  dry-site versioned barns have real preserved one-time loot and empty stations.
  Sample-programmed redstone reads all seven live gene pairs, including hidden
  copies. Build/all **85** native scenarios pass at the implementation checkpoint.
  The guide now has **109 pages, nine indices and 82 chapter links**. Update both
  sides: protocol **11**. See `docs/BREEDER_SPECS.md`, `CARE_SPECS.md` A10.

- **G6/S11, planned Breeding Pen and seed quality colors:** a two-manger Pen
  spends one actual guava on each isolated, healthy housed parent, then leaves
  mating/seeds/rest to the native BreedGoal. Directional hoppers, saved stock,
  one-shot redstone, player/Calipers controls and callback-safe costs are in
  place. Eight seed palettes read existing stored quality directly, including
  old seeds; no save migration or hatching math changes. The guide has 100 pages,
  eight indices and 73 chapter links. Pen/front-label, seed and guide previews
  inspected. Update both sides: protocol **10**. See `docs/BREEDING_PEN_SPECS.md`.

- **J1/J2/G5/D12/T5, breeder workshop:** the planned Juicer presses all eight
  guava tiers and performs paid random seed infusion, with directional hoppers,
  saved five-second work, output waiting and redstone pause. Rich carriers lift
  effect quality while preserving seed inheritance/drift. Books/Registries
  name exact recorded genotypes through acknowledged edits, with preserved
  snapshots and searchable labels. Twelve pelt rugs and sample Jars complete
  the collection; jars preserve samples and expose fixed-pair signals. The guide
  reaches 98 pages/eight indices/72 chapter links. Workshop/guide previews are
  inspected. Update both sides: protocol **9**. See `docs/WORKSHOP_SPECS.md`.

- **A9, planned fertility visibility:** existing fertility alleles now show
  distinct tail-tip phenotypes: bare, one rosette, closed green bud, three
  rosettes or violet star. Hidden copies stay hidden; pups inherit the look.
  Breeding/readiness/Bloom values stay intact. Combined build/model/all 73
  native cases pass; all mark/foot variants reset correctly and exact approved
  base coordinates/normals remain. Actual geometry and 92-page/seven-index
  guide previews inspected. Update client and server together: protocol **8**
  also prevents older clients misreading the new Phase/webbed allele ids.

- **A8, planned swamp swimming/webbed paws:** appended LEGS `webbed` gives
  fresh swamp lines native +0.5 Swim Speed, expressed toe membranes and water
  paddling, with land/jump tradeoffs and retained drowning. Native movement,
  hidden copies and external-attribute save/gene changes pass with all 73
  cases and the model gate. All 2,172 base faces retain exact coordinates/
  normals; only the expressed variant adds 72 faces. Geometry and 91-page/
  seven-index guide previews inspected. See `docs/CARE_SPECS.md` A8.

- **U1 completion, entrance trails:** new version-seven small surface warrens
  fit complete worn trails from each actual mouth round its mound to the tree
  patch. Unsafe sides fall back or stay untouched; old starts keep their plans.
  Native new/legacy piece generation, mound/tree/Nest preservation and blocked
  container/fluid/ore cases pass with all 72 scenarios. The actual-layout
  preview is inspected. See `docs/WARREN_PLAN.md` 25.

- **A7, planned End Phase ability:** appended LEGS `phase` gives fresh End
  founders and bred descendants a defensive blink after real nonlethal attacks.
  Damage remains; safe loaded landings, home/transport limits and native
  cancellation apply. Twenty-second recovery survives native saves/baskets.
  Stat tradeoffs, juice and readouts are connected. Java 25 build/all 71 native
  scenarios pass; the 90-page/seven-index guide preview is inspected. See
  `docs/CARE_SPECS.md` A7. Client combat remains unverified.

- **A6, snow surface home routes:** animals with actual native powder-snow
  footing can use its supported dry surface for domestic routes. Deep powder,
  changed/lost footing or floor, magma and neighboring fire still stop jobs.
  Woolly/hidden snow alone give no surface exception. Native costs/collision,
  saves and equipment remain unchanged. Java 25 build/all 70 native cases
  pass, including a real powder-only corridor path and meal. The 88-page
  guide/six-index/fifty-five-link preview is inspected. See `docs/CARE_SPECS.md` A6.

- **D11, plain-guava hand care:** sneak-feed plain guavas for ordinary two-HP
  healing/pup growth without starting breeding. Healthy adults and upgraded
  guavas keep food; existing love quality/time, rests and work preference stay.
  Normal feeding still breeds. Manual food pays from the actual hand before
  healing callbacks, preserving callback-refilled stock. Java 25 build/all 69
  native cases pass; 87-page/six-index/fifty-five-link guide preview inspected.
  See `docs/FORAGING_SPECS.md` D11.

- **L35/N10, keeper controls:** sneak-hoe an individual to pause/resume chores
  while retaining meals, home and care; the optional preference survives saves
  and Travel Baskets. Sneak-hoe a Cradle for manual-only hatching. Incubation,
  tending, stock and manual hatching continue; queued redstone respects the
  switch, and re-enabling under power creates no new edge. Defaults preserve
  old behavior. Java 25 build/all 68 native cases pass; 86-page guide/six
  indices/fifty-four chapter links preview inspected. See `docs/RESIDENT_WORK.md`
  L35 and `docs/NURSERY_SPECS.md` N10.

- **L34, resident supply:** appended Supply mode lets healthy Senses +20 adults
  move one ordinary surplus food to a nearby meal dish below eight food, keeping
  eight at source. Both exact stacks share one native transaction, safe paths,
  power/filter/protection rechecks and GardenWait. Callback removal of both
  dishes conserves their committed stock. Compost now also rechecks the entire
  job after reserving food and refunds interrupted mode/Bloom changes without
  work rest. Java 25 build/all 66 native cases pass. The 84-page guide has six
  indices/fifty-two chapter links; Supply/Specialist/Working previews inspected.
  See `docs/RESIDENT_WORK.md` L34 and `docs/SETTLEMENT_LIFE.md` L14.


- **D10/L33/H10, End care and basket stations:** Bloom +20 chorus-compatible
  adults revive only spent flowers, preserving branches and yielding one fruit
  to a work trough or direct meal. Native dispensers pack/release one existing
  animal, preserving paused care and exact identity. Full-body dry placement,
  reentrant UUID guards and whole-slot reservation conserve baskets through
  cancelled spawns, removed sources and callback refills. Java 25 build/all 64
  native cases pass; the 82-page guide preview is inspected. See
  `docs/FORAGING_SPECS.md` D10 and `docs/HOME_SPECS.md` H10.


- **D9/L32, safe hunts and work roll call:** hunters/fishers choose bounded
  dry approach/shore routes, interrupt on changed homes/terrain/protection
  and leave named, led or transported prey alone. Only an eligible chosen
  prey kill pays meal healing. Shared routes now recheck current native
  hazard types, including a changed floor under cached nodes. Sneak-Calipers
  on a trough reports selected-mode eligibility, food/filter, traits and
  pause/stock limits for loaded residents without paths, tags or mutations.
  Java 25 build/all 61 native cases pass; 80-page guide preview inspected.
  See `docs/FORAGING_SPECS.md` D9 and `docs/RESIDENT_WORK.md` L32.

- **L31, meadow melon plots:** fresh version-six meadow farms mix two young
  melon stems with crops, one native water source and two reserved dirt fruit
  gaps. Optional corner troughs and the original 3×3 reservation remain.
  Version-five/older plans keep their prior crops; other regional layouts
  remain unchanged. Java 25 build/all 59 native cases and focused rotated
  layout checks pass, including real native fruit growth, protected gap
  refusal and exact generation-version saves. Farm/78-page guide previews
  inspected. See `docs/SETTLEMENT_LIFE.md` L31 and `docs/WARREN_PLAN.md` 24.

- **L28–L30/N9, selected resident jobs:** sneak-hoe selects/enables Mixed,
  Garden, Gather, Compost or Nursery work, with default/legacy Mixed behavior.
  Garden Bloom +10 adults replant valid gaps from real self-planting food;
  Nursery Bloom +20 adults spend a plain guava for one exact Cradle stage.
  Both keep eight food, share GardenWait, preserve components and refund
  callback-interrupted costs. Nurses preserve the whole litter and never
  hatch it. Calipers names the nursery bonus. The guide has 77 pages/five
  indices/forty-six chapter links. Java 25 build/all 59 native cases pass;
  guide preview inspected. See `docs/RESIDENT_WORK.md` and N9 in
  `docs/NURSERY_SPECS.md`.

- **O6/O7/D8/H9/L27, orchard and habitat routines:** shears dispensers pick
  ripe guavas with one wear while retaining native animal/beehive shearing.
  Plain guavas plant canopy gaps; invalid planting keeps ordinary food
  ejection, with higher tiers untouched. Injured matching residents eat spare
  cactus tops/same-color mushrooms, ripe wart and linked melons, preserving
  growing stock through shared work/meal rules and protection rechecks.
  Nests expose claimed/free comparator signals through normal POI tickets;
  rehoming, travel and death notify without loading remote terrain. Meadow
  stores add melon food and an independent 35% chance of one/two seeds.
  The guide reaches 74 pages/five indices/forty-three chapter links. Java 25
  build/all 56 native cases pass; guide preview inspected. See
  `docs/ORCHARD_SPECS.md` O6–O7, `docs/FORAGING_SPECS.md` D8,
  `docs/HOME_SPECS.md` H9 and `docs/SETTLEMENT_LIFE.md` L27.

- **L25/L26, renewable melon beds:** foragers/omnivores harvest one slice
  only beside a living attached melon stem, leaving decorative melons intact.
  Native neighbor updates restore the mature stem. Bloom +10 advances young
  stems by one age without food or forced fruit. Diet, exact filters, native
  protection rechecks and shared work rest apply. Matching hand-fed slices
  heal adults/grow pups without breeding. The guide reaches 70 pages with
  forty chapter links. Java 25 build/all 53 native cases pass; new chapter/
  Working-index preview inspected. See `docs/SETTLEMENT_LIFE.md` L25–L26.

- **D7/L24/P6, loose-food use:** injured adults take one old ordinary loose
  food as a normal daytime meal. Healthy Senses +10 adults collect it beside
  an enabled trough with shared garden rest. Ten-second grace, native pickup
  delays/reservations, Diet, exact components, power and safe loaded paths
  apply; upgraded guavas stay. Native journals conserve source/destination
  through abort and reentrant removal. The guide adds a fifth Working index.
  Java 25 build/all 52 native cases pass; text/navigation preview inspected.
  See `docs/FORAGING_SPECS.md` D7, `docs/SETTLEMENT_LIFE.md` L24
  and `docs/FIELD_GUIDE.md` P6.

- **C10, cold-climate care:** healthy expressed snow coats at Bloom +10
  yield one snowball when brushed in a climate cold enough for native snow.
  Live climate, hand/dispenser care and Calipers share the same yield rules;
  whiskers/Starlit products combine under one wear and saved grooming rest.
  Native shearing remains independent. Java 25 build/all 50 native cases pass;
  guide preview inspected. See `docs/CARE_SPECS.md` C10.

- **O4/O5/L23/N8, habitat completion:** native dispenser sapling stations
  retain blocked stock; saplings follow 26.2 vegetation soil, recover after
  support loss and grow only supported, loaded, bounded trees. Litter crops
  restore grass/podzol/mycelium fallback without changing planting or saved
  litters. Fresh five-tree cave/Nether features can fit regional farms using
  their actual terrain mode. Java 25 build/all 49 native cases pass; guide
  preview inspected. See `docs/ORCHARD_SPECS.md` O4–O5,
  `docs/SETTLEMENT_LIFE.md` L23 and `docs/NURSERY_SPECS.md` N8.

- **D6, fruit meals and safer grazing:** injured adults eat ripe hanging
  guavas, plus diet-matching sweet/glow berries, near home by day. Plants
  remain, meals heal two HP and share normal food rest. All field grazing
  now uses complete safe loaded routes and rechecks after protection callbacks;
  a callback-created chest is preserved without healing. Legacy crop/hay
  meals remain. Java 25 build/all 48 native cases pass; guide preview inspected.
  See `docs/FORAGING_SPECS.md` D6.

- **L22, sustainable mushroom beds:** healthy mushroom eaters gather one
  spare red/brown mushroom into enabled troughs only while another living
  mushroom of that color remains in the loaded home patch. Native spread,
  protected safe work, exact filters, power and shared rest apply. A companion
  lost inside protection callbacks prevents the harvest without cost. Java 25
  build/all 47 native cases pass; guide preview inspected. See
  `docs/SETTLEMENT_LIFE.md` L22.

- **O1–O3, guava orchards:** a plain guava establishes green fruit beneath
  any leaves; native bone meal advances one age by hand/dispenser. Silk Touch
  now recovers guava leaves alongside shears. Actual-hand survival/creative,
  Adventure/protection rollback, exact loot and scheduled support loss pass.
  Java 25 build/all 46 native cases pass; orchard/region/Habitat guide preview
  inspected. See `docs/ORCHARD_SPECS.md`.

- **L21, regional farms:** new version-five starts fit desert cactus beds,
  soul-sand wart, swamp mushrooms, cave glow-berry trellises and End chorus
  within the old 3×3 plot. Optional empty enabled troughs, safe terrain fitting
  and existing containers are preserved; older starts retain their exact plans.
  Java 25 build/all 45 native cases pass; actual-layout and guide previews
  inspected. See `docs/SETTLEMENT_LIFE.md` L21 and `docs/WARREN_PLAN.md` 22.

- **L20/P5, cactus gardens and habitat index:** desert browsers trim one
  spare cactus top into an enabled trough, preserving bases/flowers and
  rechecking native protection callbacks. Safe loaded routes, filters,
  power and saved work recovery apply. The guide gains a fourth Habitat
  index and cactus chapter. Java 25 build/all 44 native cases pass;
  cactus/Main/Habitat preview inspected. See `docs/SETTLEMENT_LIFE.md` L20
  and `docs/FIELD_GUIDE.md` P5.

- **T9/T10, litter plans:** held guavas, optional Mutagen and Fortune tools are
  reusable forecast cues. The server captures their tiers/flag; seed maths
  combines feed/starborn once and previews real harvest drift/mutation without
  spending anything. Plain A|B remains readable; optional plan suffix moves
  the network to **7**, with all saved items/animals/slots unchanged. Java 25
  build/all 43 cases and focused plan/page checks pass; forecast/guide/index
  previews inspected. See `docs/TABLE_SPECS.md` 9–10.

- **L19, recovery rest:** adults below half health seek a safe place near
  their real Nest instead of strolling by day. Meals and other important
  goals preempt; thunder releases them for shelter. No passive healing or
  saved state. Calipers reports readiness. Java 25 build/all 43 native cases
  pass; recovery/index preview inspected. See `docs/SETTLEMENT_LIFE.md` L19.

- **T7/T8, connected lab bench:** hoppers fill A then B and drain A first,
  retaining whole sample components with native transaction rollback. Actual
  comparators read 0/8/15. Manual placement now uses the real hand, including
  off-hand survival/creative. Save keys, forecasts and B-first manual take
  remain. Java 25 build/all 42 cases pass; lab guide preview inspected. See
  `docs/TABLE_SPECS.md` 8.

- **H8/L18, resident wayfinding and roll call:** normal compass use on a
  resident marks its recorded Nest without requesting its terrain. Sneak-use
  Calipers on a bell for loaded neighbors, injured first, with tags/health/
  foods/homes. Both preserve tickets and animal state. Java 25 build/all 41
  cases pass; home/roll-call/index preview inspected. See `docs/HOME_SPECS.md`
  H8 and `docs/SETTLEMENT_LIFE.md` L18.

- **E5, resident finder:** rename a spare Ear Tag to an animal code and
  sneak-use Calipers with it opposite. Loaded matches within 24 blocks get
  ten seconds of native outline; five-second native tool rest also follows
  empty searches. Items, biology, homes and care recovery persist. Java 25
  build/all 41 native cases pass; finder/index preview inspected. See
  `docs/EXPLORATION_SPECS.md` E5.

- **S10, breeding-record filters:** native contents inputs in Pedigree Books
  and Registries query tags, expressed/hidden traits, recorded age and bred
  stat comparisons. Original numbers/parents remain intact; arrows follow
  matches, including after a parent-link detour. No item/record changes or
  new packet. Java 25 build/focused query/layout checks pass; filtered/empty
  record and guide previews inspected. Native input/font use remains client
  review. See `docs/SHEET_SPECS.md` S10.

- **P4/E4, navigation and working-line goals:** the 50-page guide has three
  linked contents pages/twenty-four chapter links, including direct nursery,
  guard, map, scout and trough-pause entries. Native one-time XP milestones
  reward a successful guard command, found-ore survey and focused survey.
  Refusals grant none. Build/all 40 native cases pass; three-index preview
  inspected. See `docs/FIELD_GUIDE.md` P4 and `docs/EXPLORATION_SPECS.md` E4.

- **L17, trough redstone pause:** power reserves a trough from resident
  meals and crop/compost work; manual/automation stock, exact filters and
  comparator output remain available. Jobs recheck before commitment and
  after native protection callbacks. Build/all 40 native cases pass,
  including interrupted meals, a powered crop callback and surplus refusal.
  Guide preview inspected. See `docs/SETTLEMENT_LIFE.md` L17.

- **E3, focused surveys:** Senses +20 mineral scouts can select an ore family
  from a reusable mineral in the other hand. Native family tags include
  deepslate; every focus shares normal range/rest and empty searches never
  fall back to unwanted ore. Coordinates persist in chat. Build/all 40 cases
  pass; updated guide preview inspected. See `docs/EXPLORATION_SPECS.md` E3.

- **E2, mineral scouts:** sneak-use a compass on a half-healthy bat-eared
  adult at Senses +10 to mark the nearest ore within six blocks. Through-rock
  survey reads loaded/bordered terrain only, with saved thirty-second rest
  even when empty. Whole compass details and animal/ore state persist;
  Calipers is read-only. Build/all 40 native cases pass; guide preview
  inspected. See `docs/EXPLORATION_SPECS.md` E2.

- **E1, Warren Explorer Maps:** journeyman cartographers may sell a native
  map for fourteen emeralds and a compass. The appended trade pool preserves
  ordinary offers; native map modifiers omit unlocated results. Targets are
  natural Overworld warrens, using the normal village marker. See
  `docs/EXPLORATION_SPECS.md` E1.

- **L16, Nest guards:** sneak-use a bone to opt a feral adult with a Nest
  into bounded defense. Saved preference, selected visible hostiles, complete
  dry routes, eight-block home boundary, native bites and lead/health/gene
  interruptions; Calipers reports status without a search. Old animals start
  off. Build/all 39 server cases pass; guide/index preview inspected. See
  `docs/SETTLEMENT_LIFE.md` L16.

- **A5, snow footing:** expressed snow coats use native powder-snow surface
  collision and escape jumping. Woolly keeps freezing protection without
  footing, hidden snow gains none, and gene edits update immediately. Native
  descending/inside-snow rules and conservative home routes remain. Actual
  build/all 38 server cases pass; guide preview inspected. See
  `docs/CARE_SPECS.md` A5.

- **N7, grand-tree nurseries:** new level-five halls may fit an empty Cradle
  in an old hay station's footprint with a complete hatch pad. Its fit also
  requires the ordinary trough/bell; floor Nests, labels, broad aisles and
  room bounds stay intact. Saved fixture IDs append; old starts and occupied
  blocks/incubation remain preserved. See `docs/NURSERY_SPECS.md` N7.

- **N6, hatch-pad planning:** sneak-Calipers reads Cradle incubation/signal,
  crowding and actual pad obstructions, with temporary native markers. The
  same loaded-world preflight gates real hatching; inspection consumes/rolls
  nothing. Normal Calipers retains the litter forecast. Native structure
  rotation/mirroring updates its facing. See `docs/NURSERY_SPECS.md` N6.

- **N5, in-place Mutagen:** use or dispense a vial at a planted seed/occupied
  Cradle. One success updates only its existing Mutagen flag, preserving whole
  item components, age and remaining incubation. Already treated/invalid
  targets retain the vial; native hand permissions/creative costs apply.
  See `docs/NURSERY_SPECS.md` N5.

- **C9/L15, living materials and orchard recycling:** brushing an expressed
  moss adult at Bloom +10 yields one Moss Carpet alongside any qualified
  whisker/Starlit products, sharing one wear and saved recovery. Native
  compost data adds plain guavas at 65% and leaves/saplings at 30%; higher-tier
  guavas remain excluded. Plain Bloom gardeners can recycle orchard surplus.
  See `docs/CARE_SPECS.md` C9 and `docs/SETTLEMENT_LIFE.md` L15.

- **L14, surplus composting:** Bloom +10 residents work a vanilla composter
  beside a hoe-enabled trough after harvesting/cultivation. One real suitable
  food per job, native chances, eight-item reserve and shared saved garden
  recovery. Existing protection, safe routes, Diet and domestic restrictions
  apply; ordinary maturation/hopper output supply bone meal for the nursery.
  See `docs/SETTLEMENT_LIFE.md` L14.

- **N4, committed crop harvest:** break protection and native permissions finish
  before seed/pup side effects. Cancelled breaks preserve everything; ordinary
  environmental removal returns the saved seed. Crop/command spawn refusal
  rolls back siblings; a removed crop refunds its original seed. Fortune and
  creative ripe hatching remain. See `docs/NURSERY_SPECS.md` N4.

- **N3, Nursery Cradle:** craft wool/glass/Nest/planks into a saved one-seed
  incubator. Loaded minutes, bone meal or Starlit Dust advance growth; hoppers,
  comparators, dispensers and Calipers share native controls. Empty-hand use
  or a fresh redstone edge hatches onto a clear dry 3×3 pad in front. Blocked,
  crowded or externally refused litters retain the seed; partial spawns roll
  back. Sneak-empty withdraws and resets growth. See `docs/NURSERY_SPECS.md` N3.

- **N2, seed provenance:** crops retain named seeds and arbitrary item
  components through native saves, unripe harvest and support loss. Legacy
  crops reconstruct their functional seed. Mutagen changes only its seed data.
  See `docs/NURSERY_SPECS.md` N2.

- **N1, nursery dispensers:** plant a saved litter into empty space above
  farmland or ripen one valid planted crop with Starlit Dust. Hand/machine
  actions share success-only costs and preserve the full functional seed.
  Refusals retain items. Seed Crops now maintain dry farmland. See
  `docs/NURSERY_SPECS.md`.

- **S9, carried records:** sneak-use a Pedigree Book or Purebred Registry with
  a filled basket in the other hand to log its native identity snapshot without
  unpacking. Registry eligibility, first representative and advancement stay
  shared with live registration. Air/block/off-hand/creative use and malformed
  identity refusal are covered. See `docs/SHEET_SPECS.md` S9.

- **L13, nighttime home care:** injured adults may eat real trough stock along
  safe routes wholly within four blocks of their Nest at night, then resume
  normal night waiting. Outdoor foraging/work remain daytime; the existing
  meal recovery is shared. Calipers reports trough-only readiness. Daytime
  troughs also use safe paths/replans. See `docs/SETTLEMENT_LIFE.md` L13.

- **H7, Nest wayfinding:** use a compass to mark a Nest in its dimension.
  Markers persist if the Nest is removed, preserve components and handle stacks,
  creative/off-hand use and inventory overflow. Empty-hand inspection reports
  ticket/resident without changing homes. See `docs/HOME_SPECS.md` H7.

- **L12, bred cultivation:** Bloom +10 adults advance matching unripe crops by
  one stage between ripe harvests. Growth and harvest share existing work rest,
  trough capacity/filters and protected safe routes. Ripe sweet berries now
  yield one food while keeping the bush. Calipers names the Bloom bonus. See
  `docs/SETTLEMENT_LIFE.md` L12.

Established earlier systems include native guide navigation (P3), full-state
Travel Baskets (H6), dispenser care with native armadillo fallback (C8), saved
trough filters (L11), coat-colored fleece (C7), actual bell calls (L10) and
transactional hopper/comparator troughs (L9). Earlier home/food Calipers, refuge,
storm shelter, fishing, terrain traits, local supplies, living dust and fitted
farms are also complete. Their rules/evidence remain in the specifications
listed below; do not redo them.

The native Field Guide has 88 translated pages and fifty-five chapter links
across six linked contents pages.
`art/gen_guide.py` owns text/icon/preview; `art/gen_pantries.py` owns eight
lineage food tables and shares the guide pool. See `docs/FIELD_GUIDE.md`.

## Implemented systems — do not redo

| Area | Current behavior and specification |
|---|---|
| Genetics and breeding | Seven genes, inherited pairs, bred stats/Bloom, eight guava tiers, litter seeds/crops, infused juice. `docs/FEATURES.md`, `docs/FIXES_PLAN.md` 6 |
| Sheets and records | Calipers, Pedigree Book, Ear Tags, seed/sample sheets, forecasts, carried-animal records and local tag/trait/stat record filters. `docs/SHEET_SPECS.md` S1–S10 |
| Nursery | Whole saved seeds, protected crop harvest/recovery, dirt/grass soil fallback, in-place Mutagen, dispenser stations and a Cradle with incubation, native automation, checked hatching/pad planning and fitted grand-tree nurseries plus exact paid resident tending. `docs/NURSERY_SPECS.md` N1–N10 |
| Laboratory | Two sample slots, native hopper/comparator automation, actual-hand placement, vial display, planned feed/treatment/harvest forecasts, pup/copy odds and both-direction splice plans. `docs/TABLE_SPECS.md` T1–T10 |
| Gene Splicer | One donor expressed copy, chosen gene, sample/Mutagen/tool cost, recipient recovery; breeding fixes pairs. `docs/SPLICER_SPECS.md` |
| Registry and shows | Distinct stable bred lines, linked snapshots, balanced-stat bronze/silver/gold judging, per-animal awards, ribbons and Champion Line. `docs/REGISTRY_SPECS.md`, `docs/SHOW_SPECS.md` |
| Care | Live samples, fleece palettes, whiskers/dust/moss/cold-climate frost, dispenser care, saved recoveries and sneak-Calipers home/food/care/work status. `docs/CARE_SPECS.md` C1–C10 |
| Adaptations | Expressed fire/cold/fall/Poison/cactus/Levitation benefits, slate armor, heavy-build knockback resistance, self-lit glow, terrain movement and snow footing, End Phase, swamp swimming and visible fertility marks. `docs/CARE_SPECS.md` A1–A9 |
| Outdoor food | Home-bounded crop/grass/hay/fruit and sustainable habitat meals, old ordinary loose food, bounded dry hunting/shore fishing, spent-chorus care, reservations and callback-safe commits. `docs/FORAGING_SPECS.md` D6–D11 |
| Homes/travel | Claimable Nests and comparator signals, day tether/night return, floor Nests, native leads/rides and led-adult reassignment. Travel Baskets preserve whole animals by hand/native dispenser; compasses mark places and resident homes. `docs/HOME_SPECS.md` H1–H10 |
| Settlement life | Trough stock/automation/filters/power, Diet healing and short night meals; bell visits, regional and meadow-melon farms, local supplies, refuge/storm/rest/guards and read-only roll calls. Selected harvest/cultivation/compost/gather/garden-restoration/nursery/supply jobs share exact stock, safe routes and saved work rest. `docs/SETTLEMENT_LIFE.md` L1–L35, `docs/RESIDENT_WORK.md` |
| Warrens | Fitted dens/mouths/tunnels/cellars, landmark rooms, grand-tree hall/loop/lower commons, cave/Nether/End entrances and saved labels. `docs/WARREN_PLAN.md` U1–U8 |
| Exploration | Native cartographer Warren Maps, bred bat-ear compass surveys with optional mineral-family focus at Senses +20, native working-line milestones and a loaded tagged-resident finder. `docs/EXPLORATION_SPECS.md` E1–E5 |
| Wood and travel | Guava building/redstone set, doors/shutters/lamps, four sign forms, native boats/chest boats with custom assets and drops. `docs/FIXES_PLAN.md` 4–5 |
| Orchards | Plain-guava hanging fruit propagation, native bone meal, shears/Silk Touch leaf recovery, dispenser saplings/fruit picking/canopy repair and supported/bounded tree growth. `docs/ORCHARD_SPECS.md` O1–O7 |
| Drops and endgame | Pelts/materials, Mutagen/Marrow/Tissue/Dust, five stat legends and pedestals. Sample-defined Apex ritual, inherited combat, warned attacks/enrage, boss bar and source-line crest. `docs/DROPS_SPECS.md`, `docs/APEX_SPECS.md` |
| Apex preparation | Crucible missing-offering status; main-hand Calipers/off-hand sample forecasts real boss values without spending or spawning. `docs/APEX_SPECS.md` P2 |

## Save and implementation constraints

- Keep gene/allele IDs append-only. Old six-pair data loads with plain/plain
  Diet; current genomes use seven pairs / 56 bits and mask the signed high sync
  word. Current network protocol is **12**; matching client/server versions apply.
- Preserve whole item components in samples, books, table slots, vehicles,
  troughs and trophies. Existing inventories and containers are never restocked.
- Structures save fitted tunnel cells, path exclusions, labels and optional
  `LifeFixtures`; column-owned underground writes preserve later containers,
  fluids and hard blocks. Surface props remain anchor-owned. `WoodDetails` and
  `WarrenVersion` preserve old starts; do not regenerate existing villages.
- Native terrain fitting can omit unsafe rooms, ramps, furniture or networks.
  Floor Nests, exits, water containment and existing player edits must stay safe.
- Named transient genetic modifiers preserve external attributes and reapply
  after native load. Care/food/work rest saves remaining ticks and pauses while
  unloaded. Apex is excluded from domestic tools, breeding, Nests and transport.

## Approved visuals

The sole base model is approved **Low**, 2,172 quads / 8,688 vertices with twelve
512px coat atlases. Expressed webbed legs add 72 optional membrane faces / 288
vertices, leaving all approved base coordinates/normals exact. The four mutually exclusive fertility marks add 0/40/18/120/38 faces.
The stored mesh has 2,460 faces / 9,840 vertices; an unmarked ordinary animal
retains the original render budget. The largest webbed/swarm combination renders
2,364 faces / 9,456 vertices. Preserve the photographic baby face, human/rabbit ears,
compact robust body, outward paws, short fur and source imperfections. Do not
restore tiers, distance switching or graphics configuration. Sources:
`art/sculpt_bunnidog.py`, `art/gen_bunnidog.py`, `art/reference/`.

H11's fitted bundle panniers are separate optional equipment from
`art/gen_packs.py`: zero through four bundles add 0/78/96/114/132 quads.
Unadorned animals skip the pass; the largest equipped combination is 2,496
faces / 9,984 vertices. Native model checks verify exact shared body/pup/leg
poses and per-animal reset; the actual-sculpt accessory preview is inspected.

The latest head-lighting fix uses authored surface-derivative normals and one
continuous front UV chart; no geometry was added. Closely spaced front-ring
normal change fell from 0.478 to 0.038. Software preview inspected. The user's
prior Low/Ultra screenshots establish earlier rendering; final client lighting
and performance of the fix remain gameplay review. See `docs/BUNNIDOG_MODEL.md`
and `docs/GRAPHICS.md`. A8/A9 add expressed webbing/paddling and tail-tip variants; the approved base
geometry and face remain exact.

## Validation and evidence

Use actual Java 25 compilation. In the current workspace:

```sh
source /workspace/.bunnidog-setup/activate.sh
./gradlew build runGameTestServer --no-daemon
```

`build` includes focused diet, breeding-lab, workshop/wood/boat asset and warren checks.
Add `checkBunnidogModel` for mesh/renderer/animation changes. GitHub Actions runs
`build checkBunnidogModel runGameTestServer` before uploading its commit artifact.
Avoid the large GenomeCheck suite unless substantial pure genetics/layout work
requires it; do not repeat passing checks without a relevant change or concern.

The Java 25 build and all **eighty-seven** native scenarios pass (eighty-six mod
cases plus native sanity). Latest coverage:

- **H11:** native colored bundles/full item components, actual hand controls,
  all expressed capacities/hidden copies, smaller-build cargo retention and
  one overload multiplier with native/external-attribute reloads; immutable care
  inspection, last-first return, Adventure/pup refusal and creative behavior.
  Real equipment death with mob loot off/reentrant callback and real basket
  capture/release conserve whole cargo/identity. Optional geometry resets and
  exact shared body/pup/leg poses pass; gameplay/GPU review remains separate.

- **A10/V1/V2/R1:** actual native targeting and real sculk STEP/eating signals;
  native Pen job assignment, all career ranks/regional offers, payments, stock
  and merchant saves; authored NBT/native start, four rotated barn pieces, real
  deferred loot and preserved-container replay; actual sample controls, unphased/
  hidden gene pairs and native comparator/lamp responses. Final guide count is
  111 after H11. Client art, natural barn distribution/map searches remain gameplay review.

- **G6/S11:** real hopper-fed redstone courtship, actual native mating with the
  correct two parental genomes, tier average and cooldown; held-power refusal;
  exact food saves/hand controls, read-only Calipers, hazards/third adults/pups/
  protection and unloaded boundaries; parent-change cancellation, recursive
  feeding and commit-time controller removal conserve paid food/love. Every
  Pen visual state and quality threshold/fraction maps to shipped assets; native
  guide item codec retains all 100 pages. The existing animal model gate passes.
  Native client item rendering and Pen gameplay remain manual review.

- **J1/J2/G5/D12/T5:** real top/side/output hoppers, every tier's crafting and
  pressing, blocked/aborted output, saved partial work, power pause and unchanged
  seed inheritance; bounded acknowledged name edits, Unicode/snapshot/native
  item/guide saves and changed-book refusal; all twelve native rug recipes,
  placement/support drops; jar coats, full samples, access and actual comparator
  outputs. Named page bounds/search, visual states/texture references and the
  unchanged animal model gate pass. Native client GUI/art playtests remain open.
  Historical GenomeCheck source assertions were not rerun.

- **A8/A9/U1 completion:** real new/legacy structure pieces and fully fitted
  trails preserve mouths, tree, Nests, containers, fluids and ores. Native
  swimming establishes stronger propulsion with retained drowning and exact
  external-attribute saves. Actual model draws cover every optional phenotype
  and shared-render resets. Focused genetics/inheritance/legacy/trend/readout/
  juice checks pass. Current optional geometry and guide previews inspected.

- **A7:** actual attacks and defensive blinks, retained damage/home, recovery
  saves, hidden-copy/pup/transport exclusions, native cancellation, unsafe
  redirects and callback gene/position changes. Focused pure inheritance,
  legacy-format, range and juice checks pass. The historical full GenomeCheck
  also reports 32 stale assertions against older source-code shapes; those
  source-string checks do not establish current native behavior and were not
  rewritten in this feature batch.

- **D9/L32:** real wild-prey kill/heal/rest, named/leashed/boat exclusions,
  new magma under an existing route, renamed prey, relocation and target
  ownership, with legacy shore fishing passing. Native off-hand work reports
  preserve whole stock/filter/mode, animal NBT/tag/navigation and tool; trait,
  pup, power/enable and remote loaded-only boundaries pass.

- **L28–L30/N9:** actual crouched off-hand mode cycling/toggle/Adventure,
  policy/native legacy saves, exact named stock/filter, real paths and Bloom/
  reserve thresholds, native placement refusal and replacement containers,
  source removal/refund conservation, paid carrot/guava restoration, cactus
  occupant/embryo exclusions, stock-callback litter swaps, exact nursery
  stage/cost/pedigree preservation and no autonomous hatch.

- **O6/O7:** real picking/planting pulses, exact wear/stock/components, stale/
  unripe/unsupported/unloaded refusal, hopper collection, ordinary beehive
  fallback, blocked-container food ejection and upgraded-guava exclusion.
- **D8:** actual safe paths, three-high cactus top/flower preservation,
  same-color mushroom seed stock, callback invalidation, ripe wart reset,
  native melon-stem reset, exact healing/shared rest and no loose duplicates.
- **H9/L27:** real comparator claim/failed-move/save/travel/rehome/death
  transitions, scheduled stale-signal recovery and non-loading remote notifications; native seeded
  chest rolls cover optional seeds with preserved food/guide pools.

- **L25/L26:** living/wrong-facing/decorative melon cases, Diet/filter refusals,
  protection-callback stem loss, exact slice/native stem reset, no loose copy,
  Bloom thresholds, one-stage/no-food growth, mature refusal and hand feeding.
- **D7/L24:** grace/pickup reservations, upgraded-food refusal, safe paths,
  callback reservation/power, exact components, shared recovery, native journal
  abort/full-destination rollback and reentrant trough removal conserving food.
- **C10:** actual warm/cold biomes, Bloom +9/+10, hidden snow, off-hand brush
  cost/recovery, combined products and independent native dispenser shearing.
- **N1:** real redstone pulses, malformed/blocked/wrong-soil/occupied planting,
  exact parental genomes/stats/quality/Mutagen and native crop saves, dust cost,
  ripe/empty/other-plant refusals, no ejection, real hand survival/creative use
  and native dry-farmland upkeep beneath a Seed Crop.
- **N2:** whole named/custom-data seeds, defensive copies, native new/legacy
  saves, actual survival unripe break, support loss and Mutagen split costs.
- **N3:** actual hopper supply/drain and transaction abort, native save/ticker/
  comparator, hand insertion/growth/withdrawal/break drops, real dispenser
  seed/bone-meal/dust pulses, dry/clear/unoccupied pad and crowd refusals,
  actual empty-hand/rising-edge hatching and held-power refusal. External
  spawn cancellation plus reentrant removal rolls back siblings and returns
  exactly the original named seed; retry succeeds.
- **N4:** actual late break cancellation for unripe/ripe crops, Adventure
  restriction, survival/creative harvests, crowd retention, native partial-spawn
  refusal with no remaining siblings/one seed refund, and water replacement.
- **L14:** real claimed home/path, Bloom/reserve thresholds, committed transfer
  interruption, lost Bloom, griefing/protection refusal, crop priorities,
  exact named reserve/filter, shared work rest, non-compostable refusal,
  deterministic native failed/successful chances, actual maturation and hopper
  output of exactly one bone meal.
- **C9/L15:** actual hidden/expressed moss and Bloom +9/+10 hand care,
  combined moss/whisker/Starlit products, gene-change recovery refusal and
  real dispenser collection. Native compost data loads correct values,
  excludes all seven higher guava tiers and lets a plain resident spend one
  surplus guava while keeping its eight-item reserve.
- **N5:** native off-hand ripe-crop treatment, creative Cradle preservation,
  Adventure refusal, whole-item native saves and unchanged stage/remaining
  time. Real dispenser Cradle/crop treatment, repeat/empty/wrong-target refusal
  and no vial ejection pass; existing held-seed Mutagen remains covered by N2.
- **N6:** actual native sneak-Calipers on clear unripe/blocked ripe Cradles
  preserves seed/stage/time/tool and creates no pups; shared inspection reports
  matching crowd refusal. Existing native spawn/redstone cases remain passing.
- **N7:** focused layout checks preserve complete pads, ordinary troughs/bells,
  all Nests/labels and route connectivity, omitting the Cradle where needed.
  Native owner-column placement, empty initial BE, repeat seed/incubation
  preservation and actual optional/legacy structure saves pass.
- **S9/H6:** exact carried records through air/block/off-hand/creative use,
  duplicate protection, registry award, invalid UUID/pup refusal and unchanged
  payload. Basket release still preserves all six paused timers, identity,
  ancestry/tag/name/health/age and home-ticket transfer.
- **L13:** native night waiting/meal arbitration, exact food/healing/shared
  rest, short night bounds, ordinary daytime reach, outdoor/work exclusions,
  lead interruption, damaging/blocked routes and Apex exclusion.
- **H7:** server free/claimed inspection, stack conservation, named off-hand
  rebinding, native marker save/tick after Nest removal, creative, permission
  and full-inventory cases.
- **L12:** Bloom threshold, one-stage/no-food growth, shared rest, ripe priority,
  Diet/filter/griefing refusals, interrupted growth, berries, guavas/vines and
  embryo exclusion. Care-dispenser drops now stay in a physical test catchment.

The Cradle textures and current guide/forecast previews are inspected with a substitute
font; the approved
creature model is unchanged. GitHub additionally checks that model before the
exact-commit artifact upload. All earlier care/lab/show/registry, diet, home,
transport, refuge/weather, fixture/garden, pantry, automation/filter/bell,
sign/boat and Apex cases remain in the suite; detailed evidence lives in their
specifications. Fresh mob pathfinding needs a native physics tick on the floor.
Native path regions use non-loading getChunkNow and sight checks loaded bounds.

Native cases establish server behavior. Client interaction/book fonts/lighting,
natural chunk order/travel/unload, long multiplayer routines, modded transfer
devices and progression/decorative breeding balance remain gameplay review.

Earlier user feedback: U2/U3 received “good, works well”; the six-stage `d7f39a9`
release (U4/U5/U6b/T4/W3/V1c) received “everything compiled and worked this run”.
Individual edge cases were not itemized. These reports do not confirm newer
features. U6a's entry advancement needs a natural structure rather than a
command feature; U1/H3b natural home/den review remains useful.

## Continuing

`docs/ROADMAP.md` tracks unfinished planned work as well as optional candidates.
Prioritize completing those features over further comfort controls. Substantial
coherent additions remain authorized; define bounded rules and complete meaningful
batches through build and delivery. Keep missing work distinct from manual playtest
evidence: a pending gameplay check does not make an implemented system a stub.
For terrain review use `/bunnidogs warren 1 surface`, `3 surface`, `5 surface`,
cave/Nether `1`, End `5 surface`, and natural structure lookup:
`/locate structure bunnidogs:warren_village` or `bunnidogs:warren_end_village`.

API lookups: `docs/API_NOTES.md`. Player reference: `docs/FEATURES.md`.
Conventions/art/environment: `docs/REFERENCE.md`. Original decisions and old
chunk evidence: `docs/DESIGN.md`, `docs/CHUNKS.md`, `docs/history/`.
