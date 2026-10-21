# Intelligence update: implementation design and delivery

Engineering direction, originally planned 2026-10-21 against `0c92ded`.
The following implementation checkpoint supersedes proposals where explicitly
resolved; later architecture remains future work. Read with
[the gameplay plan](INTELLIGENCE_PLAN.md), [acceptance scenarios](INTELLIGENCE_SCENARIOS.md)
and [actual cognition/learning rules](COGNITION_SPECS.md). Future class names/APIs
remain sketches; resolve native APIs against the target checkout. Prototype
values require play/profile evidence, not only passing tests.

## Implementation checkpoint: I1 and initial I2

- `Genome(long packed, int mindPacked)`, `g2.body.mind`, full equality,
  separate cognitive enum, three-word entity sync/cache and protocol **19**.
  Both cognitive copies contribute overlapping vectors; no body IDs move.
- Full inheritance, sampling, parent/item/infusion/basket/archive paths, chosen
  donor-copy splicing, separate Mind pages and exact bounded joint forecasts.
  Registry purity uses eleven pairs; body circuits/shows retain seven.
- Uniform legacy helper genotype; old adults gain familiar routines once.
  Old seeds retain body-only mutation through an explicit `body_mutation`
  marker; new seeds choose one of eleven mutation targets at the old event rate.
- `Learning`/`BunnidogLearning` and the live Lessons panel teach actual harvest,
  garden, nursery and local trough transfer. Garden/Supply eligibility combines
  capacity and personal knowledge with existing physical/world checks. Instructor
  sessions are local and transient; practice survives reload/travel/splicing.
- This first teaching model is **attended practice**, using existing safe paid
  actions. Watched player demonstrations, remembered source/destination routes,
  remaining I3 work/tactics, NPC teaching/culture, shared jobs and civilization
  remain unfinished. Do not treat local trough transfer as a long-range courier.

## 1. Engineering principles

1. Genetic capacity, skill, cultural knowledge, opportunity and authority are
   different inputs. One `isSmart` field cannot represent them.
2. Simulate concrete local actions. A colony's resource report is an index over
   real authorized inventories, not another inventory or production counter.
3. Preserve the actual world as authority. Plans, route memories and receipts
   become stale when blocks, stock, residents or permissions change.
4. One animal has one current movement/work intent. High-level planning must
   not let combat, a delivery and a teaching session own it simultaneously.
5. Every expensive process is bounded, interruptible and scheduled fairly.
   Intelligence does not authorize unlimited search or loading missing chunks.
6. Prefer reuse of current validated actions through thin actor-aware services.
   Do not maintain one harvest/crafting implementation for pets and another for
   citizens with subtly different resource, protection or cooldown behavior.
7. Ship complete experiences. A capacity schema can be introduced before gates,
   but do not disable existing behavior before replacement learning and clear
   feedback are ready. Preserve saves and trusted emergency commands throughout.

## 2. Expand the genotype without corrupting it

### The actual limit

`Genome` currently holds seven diploid genes in a `long`: eight bits per gene,
56 bits total, two four-bit allele IDs per pair. `syncLow()` carries 24 bits;
`syncHigh()` carries the remaining 32. The high sync word is already full.
Appending four genes to the current `gene.ordinal() * 8` shift is not viable:
Java masks shift distances, so a ninth slot starts overwriting old ones.

Recommended representation:

- Preserve the existing seven body-locus IDs and `Gene` allele tables exactly.
- Add `CognitiveGene` with four stable IDs: MEMORY, REASONING, HANDLING,
  COMMUNICATION. Keep a separate, explicitly codominant allele table.
- Evolve the immutable full `Genome` value to contain the existing body word
  plus a 32-bit cognitive word. A conceptual signature is
  `Genome(long packed, int mindPacked)`; its final Java API can use clearer
  accessors, but `.packed()` must never silently mean the complete genotype.
- Preserve body convenience operations; every body `.with(...)` must retain the
  mind word. Add typed cognitive operations. Equality/hash identity includes both.
- Provide a stable complete-locus view for inheritance, mutation, comparison,
  registry and complete splicing. Existing body-only mathematics explicitly
  iterate body loci. Avoid replacing every `Gene.values()` blindly.
- Start with at most sixteen alleles per cognitive locus. That is ample for
  this design and keeps the new word compact. Do not prefill sixteen variants
  just because the storage permits it.

Body dominance and its familiar shown/carried language stay unchanged. A
cognitive pair shows both contributions, not a fictitious hidden copy. Keep
cached phenotypes immutable and derived from the complete genotype.

### Versioned external form

Proposed new canonical form:

```
g2.00-01-20-00-03-11-00.11-22-11-22
```

The first group is the old seven-pair order; the second has four cognitive pairs.
A period is deliberate: existing wrappers already use `/`, `|`, `;`, `<`, `+`,
`#`, `@`, `$`, `!` and `~`. In particular, Pedigree parent pairs use `+`, and
LineageNames uses `~`. Audit all remaining wrappers before fixing the grammar.

Read legacy six-pair and seven-pair forms. A six-pair body still gains plain Diet.
New writes use the versioned complete form when genetics are rewritten. Do not
mistake an unknown future prefix for corrupt legacy data and silently zero it.
Unrecognized saved samples/records should remain recoverable, rather than being
replaced by a default animal. Exact parser policy should distinguish invalid
known data, supported legacy data and unsupported future data.

### Legacy policy: preserve useful existing animals

Recommended default: missing cognitive data in existing saved animals and saved
genetic items resolves to one documented **legacy helper** genotype. It must be
heritable, strong enough for the current familiar work/ordinary partner repertoire,
and short of independent civic planning. Prototype around familiar/practical
capacities near the middle of the four ranges; finalize against all current tasks.

Already-existing adults also receive a bounded set of familiar legacy routines
needed to preserve their current chores and training. This is individual migrated
knowledge, not a gene that uploads jobs. Mark the migration once with a schema;
never reapply free lessons after capture, reload or splicing. Existing work modes,
owner preferences, meals, recovery, named routes and party intent remain intact.

Legacy samples, parent snapshots, seeds and infusions receive the same cognitive
genotype, so two copies of the same old record never disagree. Their skill state
is unknown, not fabricated from a sample. Existing Registry lines remain fixed
under this profile. Newborns inherit capacity, not the migrated adult's lessons;
short basic training and observation must be available before new gating ships.

New natural founders deliberately roll the new cognitive population distribution.
Do not treat missing NBT on every fresh founder as an old-save migration. Separate
new-spawn initialization from legacy decode. Hand-authored legacy summon/genome
forms can continue to mean the documented helper profile.

Do not hash body appearance into a secretly different mind for each old animal,
or grant an invisible permanent “ignore intelligence” flag. The same full genotype
must predict the same inherited profile, independent of entity UUID and item path.

### Full migration inventory

| Surface | Required work |
| --- | --- |
| Entity state and cache | Save full genome/schema; add cognitive sync word; include it in cache invalidation; initial entity-data values must not expose partial profiles |
| Parents and breeding | Both saved birth-parent genomes and actual offspring inherit all loci |
| Seed / real-seed comparison | Preserve both complete parents, saved quality, treatment and legacy law where needed |
| Tissue and killing/held-syringe sampling | Copy both words exactly, without learned procedures or community ownership |
| Pedigree, Registry and LineageNames | Full genotype identity and parent links; preserve names during canonical migration; avoid delimiter ambiguity |
| Table / Cross / inheritance / goals | Exact multi-gene capacity odds, dosage, mutation caveats, and existing body/stat forecasts |
| Splicer | Select/preview cognitive loci; preserve unused genotype word, stats, health ratio, bond, injury and personal learning |
| Frozen infusion / Juicer | Carry complete rolled genotype; retain existing body effect rules unless deliberately extended |
| Travel Basket / dispensers / rescue | Carry the whole entity, including learning/residency; release stale task reservations; no health/knowledge/meal reset |
| Party / detector / archived trophies | Correct full or deliberately body-only identity, made visible; no accidental mismatch between item and live entity |
| Commands, filters and networking | Accept documented old/new forms; preserve old predicate meanings; enforce new bounded payload lengths and a new protocol version |
| Progress / wild discoveries / shows | Distinguish body-origin achievements from cognitive capability, skills and culture |

### Keep old signals and qualifications intentional

The current seven-pair analog comparator uses `1 + 2 * matchedPairs`, capped by
its natural maximum of 15. Eleven pairs would break that contract. Keep legacy
circuits on an explicit **body** scope by default. Later add mind/full scopes,
shown in the detector panel: exact gives 15 only for a complete scoped match;
analog gives `1 + floor(14 * matched / scopedPairCount)` for a valid single
candidate, and zero for an absent/blocked/ambiguous candidate. With seven body
pairs this reproduces every old signal. Do not silently change existing Jar
signals when adding a mind inspection page.

Use separate body-fixed and full-genotype-fixed counts. Existing show eligibility,
physical commissions and body-origin discovery conditions keep their documented
seven-gene meaning. A complete stable-line Registry can require all eleven pairs;
legacy helper pairs are fixed, so already-pure old records remain eligible. A
working animal never needs Registry purity merely to hold a job. New learning/
service qualifications must explicitly test demonstrated skill rather than a
sample's potential, and must allow talented wild-born adults where appropriate.

`ModNetwork.VERSION` was 18 at planning time; cognition now requires **19** on
both client and server.
Do not synchronize an entire archive or job graph through per-entity metadata.

## 3. Pure capacity and inheritance rules

Proposed pure types in `genetics/`:

- `CognitiveAllele`: stable ID/key, contribution vector, optional workload/style
  modifiers and founder/mutation metadata.
- `Cognition`: maps the complete genotype to a small immutable `MindProfile`.
- `CapabilityRules`: returns typed results for a task using capacity and the
  specified support context. Results explain genetically possible, requires
  instruction, supported-only, and beyond-capacity states.
- `Learning`: bounded personal skill/procedure state transitions, separated from
  world lookups. Knowledge references are identifiers plus parameters, not code.
- `CapabilityForecast`: probabilities for a purpose profile and its subrequirements.

Perceptual observations still depend on Ears/Senses. Carry capacity, movement,
armor, oxygen and immunities still depend on their current body genes/stats.
Genetic role predicates combine those with the mind profile where appropriate.
Do not grant virtual hearing, extra air or arbitrary path reach for Reasoning.

Keep five bred stats. Feed quality and Fortune retain their current effect on
inherited physical stat drift; they do not linearly pump cognitive values. Mind
capacity changes through inherited allele combinations, mutation and deliberate
splicing. Practice changes skill inside that capacity, not DNA.

### Forecasting combined capacities

Enumerate the small cognitive genotype possibilities per parent pair, combine
independent-locus probabilities, then evaluate the same pure capacity predicates
used by work. Four diploid loci produce at most four transmission combinations
per locus before deduplication: a maximum 256 joint cognitive outcomes for a pair.
This is bounded; do not enumerate every full-body genotype cross product.

For a purpose requiring body traits/stats too, combine independent inheritance
facts only where independence is actually valid. Keep the seed's own fertility,
quality and stat-rounding laws. “Can learn builder” is not “will arrive educated,”
“will choose this job” or “will succeed at this particular terrain.” Supported
versus independent odds should be distinct. Actual pup readouts use its real
rolled genes, not the parents' forecast.

### Mutation and splicing decisions

Keep at most one ordinary mutation event per pup and preserve the current
per-pup fertility/Mutagen chance. Adding four targets changes the distribution;
do not secretly multiply the chance by eleven. Prototype weighted selection
across the seven body and four cognitive loci, and document the resulting
body/cognitive odds in forecasts. Legacy saved seeds may need an explicit old
mutation-law marker so migrating a held “actual seed” does not rewrite its
already described law. This must be resolved before the codec release.

Do not promise byte-identical RNG consumption after expansion. Preserve the
legacy semantic distributions where promised; use separated random streams when
needed so cognitive inheritance cannot perturb a saved body-only roll or be
rerolled by opening UI. Forecasts and inspection never advance hatch RNG.

A cognitive splice recalculates capability immediately. Stop an incompatible
active job safely and release its leases, but keep acquired knowledge dormant.
Restoring capacity can make that knowledge usable again. New capacity does not
teach recipes, improve stats, refill health or skip tool recovery.

## 4. Reconcile knowledge, ability and actual eligibility

Use one typed evaluation path shared by UI, work selection and execution:

```
inherited capacity
  + learned procedure and familiarity
  + valid local instruction/mentor support
  + body/stat prerequisites
  + actor authority and site policy
  + current health/rest/stock/tool/path conditions
  -> eligibility, supported mode, concrete blocking reason
```

Reasons must have a stable priority: serious danger before missing education;
missing capacity before suggesting more practice; unavailable source before a
failed destination path. Return structured keys/arguments so the player's chosen
language renders the reason. Do not parse English explanatory text for logic.

Transient tiredness cannot make the UI falsely say an animal genetically cannot
learn. A rejected action neither consumes lesson materials nor pretends to teach
it. Read-only inspection does not start a search, reset cooldowns or commit work.

Represent a learned procedure as a short validated sequence from a finite action
vocabulary, with typed local references and limits. No saved executable scripts,
unbounded condition trees or arbitrary recipe IDs that bypass normal costs.
Useful independent behavior comes from selecting and combining supported actions,
not generating code. No external language model/API is required at runtime.

Skills progress through a few meaningful states: observed, assisted, practiced,
and teachable where eligible. One attended success can establish an easy routine;
a small variety of real successful work can justify independent practice. Do not
reward fake repetitions, previews, source/destination loops or a job repeatedly
cancelled just before payment. Avoid decaying mastered basic skills when the player
takes a break from the game.

A teaching session needs a present knowledgeable demonstrator, a receptive pupil,
a valid example and its real prerequisites. A teacher cannot broadcast lessons
through unloaded chunks. Records store procedures; pupils still need capacity,
materials and practice. Social association may make some lessons easier, but not
turn them into an affection-payment grind.

## 5. One intent controller, reusable world actions

Keep native Goal navigation and established combat/refuge services. Introduce a
small high-level arbiter instead of stacking a Goal per profession.

Conceptual ownership order:

1. Downed/rescue state and immediate physical survival.
2. Understood emergency recall and urgent escape.
3. Immediate danger/authorized combat, under the existing combat controller.
4. Necessary care/recovery and explicit player interactions/transport.
5. Player-directed trail orders, courtship and other deliberate commitments.
6. Personal essential needs and urgent communal safety/repair.
7. A current learned job, attended lesson or paid civic operation.
8. Social/rest/home routines and ordinary wandering.

`registerGoals()` currently runs during the superclass constructor, before the
animal's genome/state exists. Register adapters without consulting an initialized
mind there; evaluate safe predicates after initialization, as the current body
Goals do. Updating a profile must cancel/reconcile work, not re-register duplicate
Goals on every genetic change.

Exact native priorities require integration with current `registerGoals`; the
list is an intent contract, not a demand to renumber existing Goals mechanically.
No world operation may occur from `canUse()` inspection. Acquire a task when it
actually starts; interrupting any work path releases its claims. A disabled work
preference, powered pause, keeper move order or genetic change is a real transition.

Proposed runtime separation:

| Part | Responsibility |
| --- | --- |
| `MindState` on the animal | Cached capacity, bounded learning, familiar references, current intent; no town inventory |
| `WorkController` / native work Goal | Own one task, navigate, validate, perform an action, yield safely |
| `WorkActions` adapters | Thin services over existing harvest, planting, transfer, machine, craft, care and new construction actions |
| `Community` record | Identity, policy, bounded site/districts, membership references, current needs/projects |
| `KnowledgeCatalog` | IDs/versioned procedures, teacher/record provenance and actual accessibility |
| `JobBoard` | Local observed demand, bounded offers, reservations and failure/backoff state |
| `ConstructionPlan` | Pure validated cells/stages/ingredients/access; no block mutation |
| `SettlementRuntime` | Loaded server-thread observations, fair scheduling, actor/protection integration |

Community data is scoped by dimension and stable UUID. Current keeper combat
membership has its own global UUID ownership and must not be reused as a town.
One citizen can reside in one community and temporarily travel with a keeper;
residency, keeper bond, profession, party membership and current task are separate.

## 6. Work contracts and resource conservation

A task should include:

- Stable task/operation ID, community/site/actor authority and version.
- A small purpose and allowed action type.
- Source/destination/tool/workpad references and observed revisions.
- Exact component-sensitive resources and permitted outputs/byproducts.
- Capacity/skill/support requirements, current stage and a bounded lease.
- Abort/repair policy and a specific reason when blocked.

A typical lifecycle:

`offered -> claimed -> approaching -> revalidated -> working -> committed -> completed`

Interruptions become waiting, safely released or cancelled. A completed operation
cannot be paid twice by two workers, a reentrant callback, repeated use, a stale
job board entry or reload replay. Persist only state that is needed to reconcile
work; do not serialize a live native Path object.

Reuse actual `ItemStack` components, tool damage, recipe results, container items
and station journals. A store report never substitutes for extracting an item.
Lease one small operation's resources, not a whole town's next ten projects.
Repair, meals and childcare can preempt an ambitious annex. Avoid deadlock where
two unfinished projects each hold the ingredient the other needs.

Transfers and recipes use the existing root transaction model where possible.
Recheck claims, stock, protection and destinations at commit. Plan purity does
not guarantee the world is still valid when a worker arrives. Keep outputs paid
before exposing them through notifications/hoppers; retain current reentrancy
and component-preservation guarantees.

Registered civic residents also need a deliberate interaction policy around
existing temptation, feeding and capture. A visitor waving guava must not pull a
carer away from a live nursery or trigger unplanned communal breeding. Preserve
normal guava courtship and familiar behavior for ordinary/non-civic animals;
use invitation, authorized manual pairing and explicit care for civic ones.
Accepted work rations/treats are not breeding consent. A resident travelling with
an invited keeper yields its job correctly; packing it cannot leave a second
active worker in the town index. Explain these distinctions through the current
care interaction rather than a silent refusal or a universal capture ban.

NPC operation of a player station requires explicit station/site authorization.
Do not bypass redstone pause, filters, protected reserve food or manual hatch
preferences just because an animal is clever. Own civic stations can authorize
NPC hatching/feeding through a deliberate policy. Existing machine clocks still
run once; an operator does not also increment a hidden processing clock.

Recipes need a bounded civic allowlist or an adapter for actual native crafting
recipes. Support tool/container remainders and tag ingredients. Do not let a
planner invoke arbitrary mod recipes, summon rituals, infuse litter seeds, spend
Mutagen/legends or consume named keepsakes because those inputs happen to be in
nearby storage. Breeding stock, archive records, trophies and private goods need
explicit protected categories.

## 7. Construction that respects terrain and the player's world

### Start with a useful annex

A first construction project is a small, supported sleeping annex with a safe
entrance and at least one usable Nest, or an equally small weatherproof cache.
It needs actual missing space and paid materials. Show surveying, delivery,
placement and occupation. A template appearing in one tick is not live building.

Reuse geometry/palette ideas from `HouseShape`, `DenShape` and `TunnelShape`, but
do not call worldgen placement from a live worker. Worldgen can assume initial
terrain and chunk ownership; runtime construction needs dynamic protection,
materials, interruptions, access and repair. Plans must expose these differences.

Fit entrances, passages and work clearance to actual intended residents, including
large Build variants and their real collision boxes. A nominal one-block doorway
must not strand a chonker inside a “completed” town. Public circulation and player
inspection need deliberate clearance; species-sized side spaces are optional.
Lighting is an actual required resource where needed: glow-coated animals are
self-lit and do not illuminate nearby blocks or prevent hostile spawning.

A pure plan describes stages and dependencies: survey, reserve footprint, prepare
safe surface, supports, shell, exit, furnishings and final occupancy inspection.
Even partial work must leave a safe way out for builders and residents. Do not
excavate a roof before its support or wall the last entrance shut.

### A trustworthy build boundary

Independent live construction defaults to a small recorded wilderness reserve
created with a **new** qualifying settlement. Existing worldgen sites do not
retroactively claim surrounding player terrain. Their adoption/activation is a
separate deliberate world interaction. Player-supported towns use marked plots.

Initial prototype: a reserve on the order of 32–48 blocks across, subject to terrain
fit and actual server/protection policy. This is a test envelope, not a fixed
promise of city size. Later districts must add explicit surveyed land and connected
routes; no silent radial sprawl into a nearby base.

Inside a reserve:

- Track NPC-built cells/ownership and the expected structure revision.
- Treat player edits as protected conflicts unless the player releases them to
  a project. Vanilla does not reliably identify every historical placed block;
  do not infer ownership from “looks like dirt/stone.”
- Never clear arbitrary block entities, containers, redstone, portals, rails,
  monuments, player beds, claimed land or a structure just because it is in the way.
- Restrict excavation to surveyed allowed material/cells, never general ore-seeking
  tunnels through unknown terrain. Felling is similarly bounded and preserves
  a route and replanting plan.
- Stop for fluids, unsupported roofs, border/unloaded edges and unknown protection
  results. New observations can cause a revised plan, not blind destructive retry.
- Record player interference as a readable blocked/revised project. Do not rebuild
  the wall the player just removed every five seconds.

Use actual protection hooks with a stable, non-privileged actor identity and the
relevant site authorization. The actor API must distinguish a bonded pet's work,
a community's own plot and access donated by a player. A universal operator
FakePlayer that bypasses claims is not acceptable implementation.

### Placement, costs and crashes

Inventory transactions do not automatically make arbitrary world edits atomic.
A construction operation needs a material reservation, expected-state check,
unique receipt, native action/protection handling and an explicit compensation
policy for failures or reentrant changes. Do not advertise `setBlock` plus an
inventory decrement as a proven crash-safe transaction.

Co-locate a small work cache/journal where practical and reconcile recorded
operations with actual placed state on load. Target invariants are no duplicates
or repeated costs under normal commit/cancel/save/reload and injected reentrancy.
A hard power loss can save neighboring chunks and global data at different times;
the design must explicitly test that failure model. Ambiguous receipts should
pause for reconciliation and must not mint automatic refunds. Never roll back
unrelated player edits or all of a town's inventories to “repair” a project.

Completed blocks belong to the actual world. Breaking them later gives ordinary
appropriate loot; it does not also refund a blueprint's whole material bill.
Materials in transit stay in a real carrier/work cache and drop/return according
to a documented ownership policy when the carrier dies or leaves.

## 8. Autonomous planning without omniscience

Use observed needs plus a bounded dependency planner. The community knows its
surveyed/remembered sites, accessible records and observed stock. A remote memory
is “last seen,” not live inventory access or a guaranteed safe route.

One possible cycle:

1. Refresh a small share of loaded local facts.
2. Recognize an unresolved need, such as no room for an eligible litter.
3. Choose a known plan that could satisfy it.
4. Expand only a short set of missing dependencies: food reserve, planks, Nest,
   qualified builder, safe plot.
5. Offer a few concrete jobs and one workfront.
6. Observe paid progress; revise or back off on new facts.

A finite authored action vocabulary and small recipe/plan catalog is sufficient
for emergent outcomes. Invention means discovering/combining supported techniques
and composing legal local plans; it does not create new block/item types or
compile new behavior at runtime. Species-level primitives such as grasping,
chewing, scratching and stacking provide an initial affordance vocabulary.
Discovering a workshop technique requires observable prerequisites and an actual
bounded experiment with allowed ordinary ingredients. Low-capability animals may
need a demonstration; capable pioneers can reach these discoveries without a
player or pre-existing school. Finalize the first discovery chain along with its
bootstrap recipes, rather than leaving a planner waiting for a teacher that can
never exist. Avoid general unconstrained crafting search, global
resource queries and full-world pathfinding. Different observed circumstances,
capabilities and available cultural procedures should produce different choices.

A public project requires a capable proposer, knowledge and a feasible working
crew. The same animal need not perform every step. Supported jobs contain short
local instructions from an accessible plan/supervisor; the supervisor's capacity
is not added numerically to every worker's profile.

If an architect leaves, a crew can finish a sufficiently recorded plan. If all
communication disappears, complex new cooperative projects stop while independent
routines continue. If a stored plan's structure revision changes, re-survey it.
There must be no eternally oscillating “build, cancel, survey, build” loop.

## 9. Population, ecology, culture and relationships

### Resources and needs

Index real authorized stocks by purpose and Diet compatibility. Forty mushrooms
are not forty usable meals for a hunter-only household. Keep per-station reserves
and protected breeding ingredients in the same accounting. Stock in an unloaded
chunk or inaccessible private chest is not available.

Long work spends a bounded support meal/stamina budget; passive healthy pets keep
their existing baseline. Under scarcity, stop expansion/breeding, reprioritize
foraging and allow rest/simple work. Do not impose universal starvation damage
or offline hunger. New costs must be visible before recruiting a town's workforce.

Work rations are an explicit purpose, separate from the player's normal guava
courtship gesture. A healthy worker eating an authorized ration must not enter
love mode. Reuse paid Diet-aware food handling without double-consuming a meal
when it also heals an injury; preserve appropriate shared rest and station power
policy. Rest/support state survives capture and reload, so a basket cannot reset
a long shift. The interface explains “needs rest/meal for this work,” not a new
universal pet hunger obligation.

Select a primitive recipe/action set that can bootstrap from normal local
resources. Prove no circular dependence on metal tools, player trades or a full
crafting hall. Reuse existing vanilla item drops/recipes where suitable. Any new
animal-specific soil scratching, clipping or simple manufacturing is an explicit
paid/renewable action, not a claim that vanilla currently provides it.

### A concrete first bootstrap to prove

Begin in a modest Overworld woodland/meadow site with a guava source, ordinary
logs, grass/seeds, reachable water and accessible stone. This is the first proof,
not a promise that an isolated End island can reproduce every material chain.
Use current native recipe inputs/results, resolved against this checkout, for:

1. Gather actual food/seeds/sticks and establish a temporary safe den/rest area.
2. Obtain a bounded natural log using an explicit animal work action; process it
   into native planks and sticks. This action needs time/protection and actual
   block drops, not a credited “wood resource.”
3. Make a basic public cache from a proposed simple wood recipe and a real native
   crafting table from planks. Primitive cache crafting must not require itself.
4. Make a wooden pick through the native recipe; quarry only the surveyed exposed
   stone needed for a furnace and later tools. Respect actual harvest/tool rules.
5. Make charcoal in a real furnace with real fuel and logs; make torches through
   their native recipe. No free lamps or light supplied by the creature model.
6. Scratch suitable soil by water as a deliberately designed animal action, plant
   collected wheat seeds, and wait/tend within actual growth/ability rules.
7. Make existing Nests using their real wheat/wood recipe; construct a minimal
   safe, lit sleeping space and maintain suitable food reserves.
8. Only then commit to the paid seed/nursery/birth chain and an occupied annex.

This reveals the actual initial action/recipe vocabulary and its possible
circular dependencies before adding weaving, metallurgy, a new fiber economy or
an entire profession tree. A player may accelerate it by donating stock; a
no-player run must still close the loop. Cave, Nether and End settlements need
regional bootstrap/import plans later, without pretending water or Overworld
crops behave identically there.

### Actual new generations

Population growth needs a capacity-aware nursery plan, suitable calm adults,
actual breeding guavas, a seed, an authorized safe growing site, safe hatching
space and prospective caretakers/food. It is a normal job chain with real refusal
states, not `spawnChild()` from a demographic timer.

Use local population and housing limits with global server scheduling limits.
Prototype small: roughly 6–12 inhabitants to prove self-support, then 12–24
working residents plus a bounded nursery. These are initial profiling targets,
not the final definition of a civilization. District scale beyond that needs
measurement; do not promise unlimited city populations or fake distant residents
as endlessly productive counters.

Mating choices can favor social relationships, compatible care needs and local
variation. They should not secretly solve a genetic optimizer and produce the
perfect heir every generation. The player remains uniquely able to make informed
crosses. A child inherits genotype, not citizenship office, political authority,
relationships with strangers or the community's complete knowledge.

### Cultural records

Represent procedure/plan knowledge with stable versioned IDs, prerequisites and
provenance. Availability comes from a living knowledgeable participant or an
accessible physical record. Repeated demonstrations, mentorship and copied records
make it robust. The global SavedData catalog is an index, not an indestructible
library everyone can consult from anywhere.

A lost archive does not erase mastered skills in living residents. A lost teacher
can make an unpracticed process unavailable until someone studies a retained record
or rediscovers it. Basic survival remains discoverable without an external donor.
Keep copied records as actual items; their genotype metadata and a tissue sample
are different things.

Relationships remain a small set of meaningful familiar individuals/communities,
not an all-pairs permanent social graph. Persist bounded recent events and strong
links. Diplomacy starts with recognized visitors, aid, agreements and surplus
exchange. Do not add unpredictable warfare, political simulation or territorial
conquest as a prerequisite for a functioning local town.

## 10. Regions, paths, unloading and performance

The existing domestic routes are dry, safe and home-bounded. Do not globally raise
the sixteen-block home range or bypass `HomeRoutes` so couriers can travel farther.
Add an explicit job travel scope for surveyed connected waypoints/districts while
retaining relevant terrain checks. Water routes, boats and high-risk vertical
building are separate validated capabilities; coastal genes alone do not make
current land navigation safe underwater.

- One-step survival/recall remains cheap and responsive every necessary tick.
- Cache genetic capacity; recalculate only for a changed full genotype or explicit
  relevant state, not from a recipe scan each Goal check.
- Refresh observations incrementally and coalesce requests at the community level.
- Bound candidate jobs, remembered sites, route lengths, plan depth, project queue,
  resource inspection and task retry rate. Give blocked tasks useful backoff.
- Schedule higher-order thinking across ticks with fairness and a server-wide
  budget. A high-capability animal makes better bounded choices, not unlimited
  expensive decisions per tick. Preserve budget for safety/navigation.
- Keep all live world mutations on the server thread. Pure planning may later run
  over immutable snapshots elsewhere; never hand live Levels/entities to workers.
- On unload, pause work and release or expire transient leases safely. Reload
  revalidates sites, actors and resources before resuming.
- No force-loaded colonies, offscreen item production, retroactive breeding,
  simulated battles or wall-clock disaster catch-up. A fully unloaded community
  waits. Partially loaded jobs wait at an actual boundary.

Register newly generated community seeds idempotently when their area becomes
available on the server thread. Worldgen can place a bounded bootstrap marker
with a deterministic site ID; it must not mutate global community SavedData from
an asynchronous generation worker. Repeated chunk loads cannot duplicate founders,
start another population or claim an old player site.

Prototype performance targets should include measured tick time/memory at small,
medium and multi-community populations, with other mobs and meaningful work
present. A tentative few-millisecond aggregate planning budget is a hypothesis;
record hardware, population, loaded area and worst spikes before setting defaults.
Do not mistake an empty test world's TPS for a proven hundred-citizen city.

## 11. Multiplayer and automation

Store authority separately from capability. Shared public stock, plot permissions,
private keeper items and visitor trade rights need explicit ownership. Competing
players cannot both claim the same worker or spend the same stock through stale
menus. Invitations/residency changes release jobs, keep personal possessions and
explain any community property that must be returned.

Player combat party selection remains explicit and limited; a settlement full of
guards is not silently recruited into a keeper's three slots. Citizen defense can
use separate local roles and bounded threat response, respecting actual hearing,
relationships and protection. Emergency evacuation does not recruit everyone into
an attack on the nearest named entity.

Hoppers/redstone remain valid alternatives and partners. A powered pause wins over
work selection. Public stock reports and existing comparator outputs reflect real
inventory or documented readiness, never a fictional economic balance. Let the
player automate crops while residents provide education, flexible logistics,
repairs and local decisions that a hopper cannot make.

Trade moves actual stock on both sides. Essential reserves, tools, records and
breeding stock are excluded from automatic export unless authorized. Prices and
local demand can create reasons to visit, but must not create a deterministic
buy/sell loop that mints currency without real production/consumption. Human
Breeder villagers retain their own native offers and restocks.

## 12. Information and presentation architecture

The existing seven-row animal sheet cannot simply display eleven genes. Keep its
physical summary readable and add a mind/learning spread or tab. A sample's mind
page describes capacity; a live animal adds actual education/current role. The
Table adds purpose-focused cognitive odds without changing old parent/seed spread
indices blindly. Inspectors report missing capacity versus missing demonstration
versus missing tools/path/permission.

Personal name, mind description, current intent and one actionable reason should
be enough in normal care. Detailed vectors, both allele copies, dosage rules,
knowledge provenance and exact odds belong behind explicit inspection. Render
short reasons with translatable Components for each recipient; pure pages resolve
language before fitting. Update English and canonical Peruvian Spanish together.

Do not synchronize town history continuously. Send bounded, authorized snapshots
for the screen the player opened, with revision/context checks for mutations.
Client work markers are requested aids, not a permanent world-sized overlay.
Animation/tool props are a later visual task with actual renderer/model validation;
planner output and substitute-font previews do not verify in-game readability.

## 13. Implementation sequence

Each row is a coherent experience, not a mandatory one-turn chunk. Continue across
related rows when authorized time permits, reserving real delivery capacity. Do
not ship a later checkbox while an earlier player journey is still fictitious.

| Stage | Concrete scope | Completion evidence |
| --- | --- | --- |
| I0: design (delivered) | Gameplay range, genetic model, full migration inventory, concrete colony loop, architecture and scenarios | Planning documents committed before implementation |
| I1: genetic foundation | Full genotype codec/sync, four cognitive loci and derived capacity, legacy migration, all genetic item/capture/breeding paths, honest mind inspection | Old save/item fixtures retain identity and usefulness; offspring/sample/basket/splice round trips; both languages; no new capacity gates silently active |
| I2: one useful learner | Familiar harvest and a short source-to-destination lesson, visible limitations, supported versus independent work, reliable interruption | An ordinary compatible adult learns in play, a limited one gets an honest alternative, recall/safety interrupt safely, paid items arrive once |
| I3: integrate existing work | Replant/compost/gather/supply/nursery/scouting/care capability rules and accessible lessons; current stat/Diet gates; broader role forecasts | New low-end limitations actually apply with feedback; migrated work lines still function; no obsolete ungated parallel Goal bypass |
| I4: professions and local commons | Shared authorized cache, grower/hauler/storekeeper, bounded jobs, food/reserve policy and real station operation | Two complementary residents sustain a real local production/supply loop without duplicate stock or constant player orders |
| I5: first live construction | Pure annex plan, survey/plot, materials/tools, supervised builder/helper, interruptions and repair | A crew constructs and occupies a small usable room with paid materials and safe access; blocked/protected/reload cases preserve world/items |
| I6: planning and teaching | Needs/dependency planner, public instructions, skilled teachers/records, redundant essential roles | A capable resident notices a real need, organizes an affordable plan and can hand it off; pupils learn through actual available knowledge |
| I7: independent family continuity | Primitive bootstrap recipes, autonomous actual seed/birth/care chain, support meals, population controls, migration/invitation | A small wild family starts and sustains itself through a real new generation without player feeding, tools or manual job assignment |
| I8: living settlements | Qualified new-world sites, physical/cultural states, regional palettes, broader professions, recovery and player relationships | A functioning wild community creates useful infrastructure after finite starter stock, copes with a missing specialist and remains bounded/understandable |
| I9: communities in a world | Actual courier agreements, records/knowledge exchange, specialist invitations, limited district expansion and regional trade | Two independently functioning communities exchange real goods/knowledge; borders/unloads/protection never become teleportation or free production |
| I10: advanced expression and tuning | A small set of useful advanced tactics/tools/public works, cultural details and carefully profiled larger districts | Natural Survival/client/multiplayer evidence supports the intended payoff and server cost; no declaration of completion from headless counts alone |

I1 is intentionally broad across serialization surfaces: a half-expanded genome
is not a safe release. It may require several implementation checkpoints. I2/I3
should not be replaced by a large job menu with nothing learnable. I7 is essential
to the user's promise, not an optional decorative worldgen afterthought.

Existing unfinished combat breeding handoffs can share the new purpose/capacity
inspection work. Its physical signature moves are not a prerequisite for this
update, and do not take over the next intelligence-focused continuation. Keep
implemented combat dependable while intelligence reshapes its planning ceiling.

## 14. Validation proportional to the risk

The original planning batch required no game build. Implementation requires real
Java 25 compilation and focused checks at coherent boundaries.

- **Pure genetics:** all legacy body forms/allele order, complete schema parse,
  signed cognitive sync bits, dosage, both-parent transmission, exact role odds,
  mutation law and splicing conservation. Include deliberately complementary minds
  and edge cases, not just all-min/all-max profiles.
- **Migration/item integration:** actual existing animal, seed, tissue, book,
  parent/name, bottle, trophy, basket and Table data; no lost owner/health/injury,
  implicit education of samples, recurring free lessons or expanded-genome truncation.
- **World actions:** actual extraction/delivery, tool wear, recipe remainders,
  protection, power/filter behavior, double claim, reentrancy, interruption and
  normal save/reload. Construction has separate world-edit/journal failure tests.
- **Autonomy:** a few long meaningful scenarios, including no-player bootstrap,
  shortage, missing planner/teacher, real birth/housing and a changed path. Do not
  substitute thousands of source-string assertions for an end-to-end colony.
- **Performance:** inhabited, working communities at bounded sizes; multiple
  loaded districts; blocked sites; unfair scheduling/backoff and repeated reloads.
- **Client and play:** English/Spanish care/lessons, understandable purpose of a
  limited animal, role discovery without a manual, visible paid construction,
  recovery/recall and actual server/multiplayer interaction.

Record build, pure/headless, native integration, visual/client and natural Survival
evidence separately. The current 116 passing native scenarios establish the
existing mod's baseline, not intelligence behavior. Every stage must state what
has actually been demonstrated and what still requires play.

## 15. Choices fixed for planning, and choices to prototype

Working defaults for implementation: four cognitive loci; codominant contributions;
no sixth bred stat; explicit compatible helper migration; personal learning and
non-genetic culture; bounded local symbolic planning; real paid work and
construction; safe limited wilderness reserves; pause on unload; reliable safety
commands; existing ordinary animals remain worthwhile.

Resolve through small prototypes: exact allele tables/weights, task thresholds,
lesson pace, work-meal/rest rates, primitive recipe/input closure, first region's
bootstrap resources, allowed material/recipe list, invitation/residency UI,
construction receipt persistence under a hard crash, and measured population/
planning budgets. These are implementation decisions to refine with evidence,
not an instruction to ask the user to approve every routine choice.

Defer until basic autonomy works: arbitrary user scripting, LLM-driven dialogue,
conquest/war systems, general mod-machine operation, unsafe remote water/void
routes, unlimited expansion, complex custom combat formations, dynamic laws/taxes,
and huge offscreen economic simulation. Their absence must not be used to excuse
an unfinished independent town.
