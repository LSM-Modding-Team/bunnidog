# V5 — Breeder commissions

Implemented 2026-10-20. A commission board gives players concrete
breeding targets that differ from Show Ring judging. Existing shows reward
balanced stats; commissions request particular combinations of inherited
abilities. They never consume the animal or issue an unearned show award.

## Station and submission

Craft `PPP /  W  / CCC` with three planks, one Whisker and three copper ingots.
Place the board by an existing Breeder's Pen. Its facing marks the one-block
front pad; keep that block and its headroom empty above solid support. Exactly
one ordinary adult's feet must be inside the pad. Leads are allowed; rides or
passengers are not. The animal needs both recorded birth-parent genomes, at
least half health and calm, without current danger, love or a recent attacker.
Keep a living adult with the actual native Breeder profession within eight
blocks. The board does not replace the Pen as a job site or create a villager.

Empty hands cycle the saved selection; sneak-empty and Calipers inspect without
changing it. One actual paper in either hand submits the selected request with
normal build/reach permission. Creative keeps the required paper. A refusal
keeps paper. The live animal, parents, genes, stats, equipment and home remain.
No Tissue Sample, invented pedigree or copied saved book can stand in for it.

## Practical breeding targets

“Fixed” means both copies of that gene carry the named allele. Other pairs may
remain mixed except in Crossing Branches. Bred stat points are additional to
the phenotype's existing physical tradeoffs.

| Request | Fixed traits | Bred stats / demonstration | One-time material reward | XP |
|---|---|---|---|---|
| Night Guide | Pale coat, bat ears | Senses +20 | 4 emeralds, 8 glow berries | 50 |
| Coastal Scout | Seal coat, paddle legs, shore-forager Diet | Senses +10, Swiftness +10 | 6 emeralds, lead, 8 dried kelp | 50 |
| Mountain Courier | Hoofed legs, a fixed Build with capacity 3–4 | Leap +20, Swiftness +10; at least one equipped bundle | 6 emeralds, bundle | 50 |
| Ember Gardener | Ember coat, nether-forager Diet | Bloom +10 | 4 emeralds, 8 nether wart, 4 bone meal | 50 |
| Frost Steward | Snow coat, tiny ears, fisher Diet | Bloom +10, Vitality +20 | 4 emeralds, 4 snowballs, 8 cod | 50 |
| Desert Messenger | Fennec ears, fleet legs, lean Build | Swiftness +20, Senses +10 | 6 emeralds, 4 string, lead | 50 |
| Swamp Herbalist | Moss coat, webbed legs, mushroom-eater Diet | Bloom +10, Senses +10 | 4 emeralds, 4 bone meal, 4 moss blocks | 50 |
| End Nursery | Void coat, Phase legs, starborn fertility, chorus-eater Diet | Bloom +20 | 6 emeralds, 4 chorus fruit, 2 Starlit Dust | 50 |
| Crossing Branches | All seven pairs fixed, at least four distinct expressed wild origins | Senses +20 | 12 emeralds, golden guava | 100 |

The eight regional targets connect every wild lineage to a practical ability;
the combined line rewards intentional crossbreeding. Crossing Branches counts
distinct allele origins, rather than pretending to know the animal's birthplace.
Common genes contribute no wild origin. Courier cargo stays equipped.

## Native progress, conservation and signals

Each request pays once per player through its native impossible-criterion
advancement and advancement-reward loot table: eight 50-XP goals and one 100-XP
challenge. Native player saves own the progress, totaling 500 XP. Reading,
cycling, breaking/replacing boards and later animal edits do not repay a goal.
Different keepers may demonstrate a qualified living animal independently.

The station reserves actual paper and an in-progress guard before native
award/reward callbacks. Native criterion progress commits before those callbacks;
reentry through the same or another board cannot submit it again. A false award
refunds exact paper/components beside any replaced hand stock. A committed
award survives a removed station and preserves callback changes. Successful
submission does not issue a Show ribbon or consume any animal/cargo.

Stable string `Request` saves selection. It stores no animal, inventory or copy
of personal advancement progress. Invalid/legacy selections default to Night
Guide. The block rotates/mirrors its real front pad, drops normally and is
axe-mineable. Its native scheduled twenty-tick poll updates the ready sheet and
comparator output: 15 for a currently qualified animal plus Breeder, otherwise
0. This shared signal reflects the live pad, independently of a particular
keeper's completion; personal inspection also reports an already completed
request. Redstone reads readiness and does not automatically submit paper.

## Evidence

Java 25 build and all **100** native cases pass at the combined checkpoint.
The native commission case uses actual held-paper/block interactions, a real
Breeder career, both birth parents and every one of the nine live genotypes.
It checks wrong-career, near-miss stats, blocked/crowded pads, permissions,
courier equipment, exact one-time XP/materials, unchanged animal/cargo, native
board saves/rotation/comparator state and read-only Calipers. The native reward
callback reenters two stations, tries to change selection, replaces the hand
and removes the paying board: one actual form and one payout remain committed.

The shipped-model/texture and 141-page guide previews are inspected. Client UI,
native rendering and long-running progression balance remain gameplay review.
Update both sides for content protocol **15**; existing saved animals, genomes,
samples, books, reward progress and world structures retain their formats.
