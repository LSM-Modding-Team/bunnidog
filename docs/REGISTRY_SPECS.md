# Purebred Registry G2

**G5 naming is implemented:** select a record in an opened Registry and choose
**Name lineage**. Names are local to this book's exact genotypes and searchable
with `line:text`. A bounded server-acknowledged edit preserves first-representative
snapshots, qualification and advancement rules. An optional `lineage_names`
component carries labels; old books need no migration. See `WORKSHOP_SPECS.md` G5.

Implemented 2026-10-16. The registry gives breeding a persistent collection goal:
fix a stable genome, then record an adult representative. A stable mixed-lineage
breed is valid; matching a wild founder recipe is not a requirement.

## Crafting and qualification

- Shapeless: one **vanilla book + gold ingot + bunnidog whisker**. This does not
  consume a Pedigree Book or its records. Obtaining a whisker unlocks the recipe.
- Rare, reusable, unstackable, in Tools & Utilities. Command:
  `/give @s bunnidogs:purebred_registry`.
- Use on a living adult with both recorded birth parents, recorded stats/identity
  and two identical copies in **every one of the seven genes, including Diet**.
  Wild catches without recorded parents, pups and mixed pairs refuse clearly.
- Registration does not change genes, health, age, parents, bred stats or the book
  count. There is no per-registration material cost or animal recovery.
- One entry per stable genome, at most 24 lines. Another animal with the same
  genome does not replace the first representative or update its historic stats.
  The representative's UUID, ear tag, adult stats and birth-parent genomes persist.
- A successful registration awards **Breeds True**, a husbandry goal with 25 XP,
  once per player through native advancement progress. Reading or copying a book
  does not award it; qualification is checked on the live animal by the server.

## Reading and breeding

Use air to open the registry, including an empty one. The index explains the
requirements and links to ordinary full animal sheets. Each record is a snapshot;
editing or losing the representative does not erase the registered breed.

S9 also accepts a carried animal: hold its filled Travel Basket in the other
hand and sneak-use the registry. This uses the same qualification, first-line
snapshot, duplicate handling and Breeds True award as live registration.
Ordinary use still opens the index. Pups remain ineligible even while carried.
The basket and its animal state are preserved; see `SHEET_SPECS.md` S9.

Hover **GENOME** on an animal/sample sheet to see its fixed-pair count and which
genes still differ. The Genetics Table's **Two Copies** page forecasts matching
pairs, plus the chance that a pup fixes all seven pairs. This latter probability
allows any fixed allele at each gene; it does not promise a particular target
breed. Crossing two adults with exactly the same registered genome breeds true
before mutation. Bred stats continue to drift normally.

The Gene Splicer can introduce an allele, but refuses an already-carried allele,
so it cannot fix a pair by repeated use. Breed to complete the registry goal.

## Compatibility and evidence

The new item reuses the existing `pedigree_data` snapshot component and text
format. It filters unfinished/duplicate records on read. The ordinary Pedigree
Book still logs individuals and updates their snapshots as before. No genome,
animal or existing item format changes. Protocol 6 was the G2 checkpoint;
current G5 edit packets require **protocol 9** on both sides.

Native creative entity interaction passes a temporary stack copy. Registry writes
target the actual held book; Gene Splicer selection on animals now does likewise.
The actual server case checks qualification/refusals, seventh-gene enforcement,
creative registration, historical duplicate protection, preserved animal state,
native advancement progress and item-codec save/load. Focused pure checks cover
capacity, unique lines, persistence and a full index. The icon preview was
inspected; client navigation and multiplayer gameplay still need a playtest.
