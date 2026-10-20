# G7 — genetic discoveries

Implemented 2026-10-20. These goals give existing Pedigree Books a breeding and
exploration purpose, including the skeleton's **Nature Finds a Way** proposal.
They use actual saved observations and native once-per-player advancement
rewards. No additional currency, animal birth flag or item component is added.

## Nature Finds a Way

Record a bred animal carrying any mutation-only allele to earn **50 XP**.
The actual snapshot must include both birth-parent genomes; an unparented
spawned/creative rare animal does not qualify. Either copy of any gene counts,
including a hidden copy. Inherited or spliced mutation-origin alleles qualify:
the saved format does not prove that a mutation occurred in this generation,
and the goal deliberately does not claim that provenance.

Re-recording a known animal updates its real snapshot before evaluation. A
full book does not invent an unsaved observation. Reading an existing valid
book can recognize its already recorded knowledge.

## Wild Gene Library

Keep genes from all **eight** wild origins in one Pedigree Book to earn
**100 XP**: Tundra, Desert, Swamp, Cave, Mountain, Nether, End and Island.
The tooltip shows the current distinct signature count. Meadow has no
exclusive wild allele and adds no required signature.

Progress comes from allele origins in the actual logged animals' seven pairs,
not their position or appearance. Hidden copies and mixed descendants count.
Unlogged parents do not add signatures merely by appearing in family history.
This measures collected genetic knowledge rather than inventing a saved
birthplace or requiring eight currently expressed coats.

Both goals evaluate successful saved observations and server-side book reading.
Whole native item saves preserve progress. A gifted or older book can teach a
new keeper; re-reading/updating never repeats an already awarded player's XP.
The existing 24-animal cap, IDs, names, genomes and record format stay intact.

## Evidence

Focused pure rules exercise carried wild origins, the eight-origin target and
parented mutation observations. The native case logs actual animals through
held-book interactions, withholds the library until the Island signature,
updates a parented animal, checks native reward/one-time XP and reads an actual
item-codec round trip from another keeper's off hand. Combined Java 25 build
and all **95** native cases pass. The 124-page guide includes both goals;
native client text/navigation remain gameplay review.
