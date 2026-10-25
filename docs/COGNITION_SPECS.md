> Apex play-review integration: canonical genomes are now g3 with an appended

> Latest implemented progression: [HEARTH_PROGRESSION_SPECS.md](HEARTH_PROGRESSION_SPECS.md)
> supersedes the old offering/entry rules and adds a focused sustainable household,
> resident teaching and paid construction. Broader civilization stages remain open.
> Wings body locus; legacy/g2 minds and their locus IDs remain unchanged. New seeds
> include twelve loci; original body-only mutation seeds retain their seven targets.
> See [APEX_SPECS.md](APEX_SPECS.md) for actual flight and migration behavior.

# Cognitive genetics and attended learning

Implemented 2026-10-21: I1's complete genotype foundation and the first practical
I2 learning loop. This document describes shipped code; the broader ambitions
remain in [INTELLIGENCE_PLAN.md](INTELLIGENCE_PLAN.md),
[INTELLIGENCE_ENGINEERING.md](INTELLIGENCE_ENGINEERING.md) and
[INTELLIGENCE_SCENARIOS.md](INTELLIGENCE_SCENARIOS.md).

## Playing the first learning loop

Interact with an animal, then choose **Work → Lessons**. Choose a routine;
unavailable choices have local hover feedback. An instructor stays within eight
blocks, with sight of the animal and both work targets. Successful real work
adds personal practice; opening a panel, waiting, failed work or unavailable
stock grants none. Existing work rest still applies, including during practice.

| Routine | Actual action and existing requirements |
| --- | --- |
| Harvest | Collect one eligible ripe plant into an enabled nearby trough; matching Diet, healthy adult, claimed home and daylight. No bred-point minimum. |
| Delivery | Move one component-preserving ordinary food between nearby troughs; Supply mode, Senses +20, source above eight, destination below eight, existing four-block range and safe access. |
| Garden | Existing cultivation, replanting and composting actions; Bloom +10, compatible stock and selected work policy. Food-spending work preserves eight food. |
| Nursery | Advance an unfinished real Cradle; Bloom +20, Nursery mode and a real plain guava above the eight-food reserve. |

Ordinary work menus show personally practiced routines. Lessons offer Teach,
Guide or a learned label without predicting untried genetic potential or listing
bred thresholds. Disabled actions identify actual physical blockers.
After practice, status distinguishes **learning**, **learned** and
**learned; needs guidance**. Calipers expose exact inherited capacity. Genetic potential does not
claim a learned job. A compatible mind can learn independent work; a supported
mind can know the routine and still need an active nearby guide each time.
Limited animals retain companionship, care, home, cargo and trusted recall.

Lessons focus one routine in the existing Garden/Supply Goals; they do not add
another movement owner. Existing Diet, Bloom/Senses, work policy, redstone,
health, route, protection, stock and recovery checks remain authoritative. A
teacher must have build permission and be the bonded keeper when one exists.
Schooling an unbonded adult does not silently bond it or enroll a fighter.
An active lesson takes priority over food temptation; a skittish learner
recognizes its present instructor without gaining a permanent bond.
Workers keep approaching their safe work spot until they can see both targets;
being close behind a trough or below a bank does not stop the approach early.

Sessions last at most ten loaded minutes. Walking away or losing sight pauses
guided work; returning can resume the current session. Recall/trail orders,
danger, work pause, incompatible splicing, reload and travel end the session.
Knowledge survives. A restored capacity can make dormant knowledge usable
again. An independent graduate no longer needs the instructor.

## Practice activity and interruption feedback (2026-10-22)

A successful lesson start closes its real conversation. Garden/Supply Goals
report searching, approaching and working from their actual lifecycle; guided
work reports waiting when its real instructor is absent or occluded, and shared
recovery reports resting. Work/Lessons shows that status only when requested.
No new status HUD or automatic tutorial is added. A stopped chosen session gives
its nearby teacher one short interruption event and preserves personal knowledge.

Actual physical blockers (work pause, active trail order, night, health, home and
world work permission) have local reasons. Supported/limited potential and body
point thresholds stay separate. Requested Calipers Work readings now show actual
learning status beside concise physical requirements and the eight-food reserve;
a high stat does not label a routine as already learned or independently usable.
These are transient activity/reporting changes around the existing paid actions,
not observed-player imitation, saved routes, new professions or shared culture.

## Inherited potential

`Genome` contains a 64-bit body word (the original seven bytes plus Wings) and
a separate 32-bit mind word. Four saved cognitive loci retain their original
indices after the seven original body loci in `Locus.all()`:
Memory, Reasoning, Handling and Communication. Each has two four-bit copies.
`CognitiveGene` is the append-only source of allele IDs and contribution vectors;
never reorder existing body or cognitive IDs.

Each cognitive allele contributes a vector. Average the two copies at each
locus, then sum the four vectors into capacities bounded 0–4. The effects
overlap: reasoning alleles can also affect handling/communication, for example.
Both copies contribute; cognitive alleles have no dominant/hidden distinction.
The highest combined profile reaches four on every axis, but no single locus
creates that profile. Founder weights are 3/6/3/1 for allele IDs 0/1/2/3 in
every region; neither a biome nor a body color determines intelligence.

`CapabilityRules` tests the same combinations used by learning and forecasts.
Building, teaching and planning predicates are reserved foundation rules;
they do not implement those professions or appear as available jobs.

Personal `Learning` stores four bounded practice values, 0–6. One completed
guided operation gains `clamp(1 + floor(memory + communication), 1, 6)` units.
A practical helper needs two successful operations; the strongest retention
and communication can learn in one. These are prototype pacing values, not
natural Survival balance evidence. Practice does not improve DNA or bred
points, and capacity beyond the supported threshold cannot be ground past.

## Inspection and breeding

Calipers, sample, seed, book/Registry and Genetics Table windows have a **Mind**
button beside the existing body view. Mind pages show both cognitive copies,
their combined capacities and routine potential. Table/seed forecasts enumerate
at most 256 joint cognitive transmissions and evaluate real task requirements;
they distinguish independent potential, supported-only potential and genuine
limits. They never advance hatch RNG or claim pups inherit education. Existing
body/stat forecasts retain their meanings.

Mind splice previews show both donor copies. Sneak-use the tool to cycle the
seven original body selections followed by copy 1/copy 2 of each cognitive
locus, then the new Wings selection. Existing saved tool IDs are unchanged. A mind splice replaces the recipient copy contributing less to that
axis, first on a tie; already-carried alleles refuse without cost. It costs the
existing sample, Mutagen and tool use/recovery. Body genes, stats, health ratio,
bond, injury and personal lessons are preserved. Breed to fix both copies.

Snapshot filters accept `mind:memory>=2.5` and `mind:retentive`, using canonical
English keys regardless of interface language. `shown:`/`carried:` keep body
dominance semantics. A snapshot cannot certify personal skill or culture.

The Registry now requires all twelve pairs fixed, including Wings. Its complete purity forecast
is the product of body purity and exact mind purity. Shows, physical commissions,
Jar outputs and existing Pedigree Comparator circuits keep their seven-body-pair
contracts, including the analog maximum of 15. Detector help states that scope.

## Save and mutation contracts

Canonical genotype text is `g3.<eight body pairs>.<four mind pairs>`, for example
`g3.00-00-00-00-00-00-00-00.11-11-11-11`. The period does not collide with the
existing Seed/Tissue/Infusion/Pedigree/LineageNames delimiters. Full equality
includes both words; `.packed()` deliberately remains only the body word.
Entities sync three integers and invalidate capacity caches on either word.
Required client/server protocol is **25** after the complete body-word update.

Valid old six/seven-pair genomes resolve to the same fixed, heritable helper
mind `11-11-11-11`: capacities 2.25/2.00/1.75/2.00. Six-pair forms also retain
the previous plain-Diet migration. These and old g2 records gain grounded Wings. No random UUID/body-color migration occurs.
Samples, parent snapshots, seeds and old names agree on that same full identity.

An old adult with a legacy genome and no `Learning` field receives familiar
current routines once. Personal state is then always saved as
`Learning:"l1.h-d-g-n"`, with four decimal 0–6 values. An explicit empty schema
does not receive free lessons on later loads. Pups and newly created animals
start without those adult routines. Basket save/release carries the individual
state; tissue, offspring parents, trophies and frozen juice carry genetics,
not education. Unreadable future sample payloads stay untouched; book writes
and name edits refuse to discard opaque future-version records.

One mutation event at most remains per pup at the existing fertility/Mutagen
chance. New seeds choose uniformly among eight body and four cognitive loci.
Adding targets does not multiply the mutation chance. Existing saved seeds
retain **body-only mutation targets**, recorded as `|body_mutation` when
rewritten; `|mutagen` can coexist with that marker. The flag survives treatment,
actual-seed comparison, planting and infusion. Inspection does not reroll seeds.
Quality, Fortune, stat drift and potion effects retain their physical roles.

## Evidence and remaining work

Maintained `checkCognition` covers full codecs, old forms/laws, both-copy dosage,
joint forecasts, mutation, founder variation, body signal contracts and personal
learning. Native scenarios cover the actual item/entity/basket/splice paths,
care-menu lessons, real harvest and paid component-preserving transfer,
supported/limited minds, a normal-AI skittish learner with a food-holding teacher,
reload and legacy migration, and ordinary low-capacity
attachment/Whistle recall. Localization checks include the separate mind pages
in the shared Spanish translation.

The initial foundation below has since gained the concrete watched-delivery
journey in [DELIVERY_LEARNING_SPECS.md](DELIVERY_LEARNING_SPECS.md), plus the
Hearth teaching/household loop. The earlier checkpoint was **initial I2**, with
some garden/nursery integration from I3. These are
attended practice sessions around existing local actions, not observed player
demonstrations or saved courier programs. Learned source/destination routes, native delivery observation and route-specific
interruption feedback now ship in that current spec. Continue general observed
lessons and remaining gather/scouting/care/tactics without bypassing taught limitations.
NPC teachers, shared culture, professions, commons, construction, independent
family continuity and autonomous civilization remain I4–I10 work. No client/GPU
playtest, long-term colony simulation or natural multiplayer balance is claimed.
