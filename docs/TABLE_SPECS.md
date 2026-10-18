# TABLE_SPECS — the Genetics Table (plan, session 36)

**CURRENT (2026-10-18): T9/T10 preview guava feeds, Mutagen and Fortune from held cues.
T7 adds native hopper/comparator automation; T8 repairs actual-hand placement. T5/T6 splice plans and inherited-copy forecasts remain.
The user confirmed the earlier d7f39a9 release broadly.**

The user picked the Genetics Table from the open roadmap (session 35: "Genetics Table (block, uses whiskers)"). The roadmap only had one line
for it ("the block version of the Calipers", DESIGN line 423: "a Genetics Table block can show the same `genetics/Readout` lines later"),
so this file records the plan and implementation. **Status of each chunk is in `docs/CHUNKS.md`.**

## 1. What it is for (the honest version)
The Calipers already read an animal, a seed or a Tissue Sample. A table that only re-reads a sample would be a gimmick (the user dislikes those).
What a block can do that a hand tool cannot is **keep things and compare them**. So the table is a small lab bench that holds Tissue Samples
(the item that waits for a use since D7a) and, from chunk T3, **forecasts a litter**: put the sample of the mother and the sample of the father on
it and it tells you, in the book's plain words, what their pups are likely to look like, how many to expect and where their stats will drift.
That is a breeding-management tool (the user's taste), costs no new mechanic and uses only data the mod already has.

* **Ingredients:** whiskers (Senses, drop D4) are in the recipe, as the drops spec promised ("Whisker -> Genetics Table").
* **No menu, no slots, no inventory screen.** The project has never used a container menu and the API is unverified; the table works like the Trophy
  Pedestal (right-click to put a sample on, right-click to read, sneak to take back) and shows its reading in the window screens the mod already has.

## 2. Chunks (historical breakdown; current workflow is in AGENTS.md)
| Chunk | What | New Minecraft names |
|---|---|---|
| T1 (written, session 36) | The block: model, textures, recipe (2 whiskers + copper ingot + 5 planks), loot, tag. A block entity that holds ONE Tissue Sample (saved, synced). Right-click with a sample = put it on (or swap); right-click with anything else = read it (the existing one-sheet sample window opens); sneak + empty hands = take it back. The "what does this click do" rule is the pure `genetics/TableUse` | `Player#isShiftKeyDown` (seen in NeoForge's tests); everything else copies the pedestal and the Calipers |
| T2 (written, session 37) | A second slot (A and B). The reading becomes a window of spreads walked with Previous / Next, like the seed window: sample A's sheet, sample B's sheet. New payload (two sample texts), `SheetLayout`-style spread builder | none expected (same payload pattern as S8) |
| T3 (written, session 38) | **The forecast spread** (the point of the table): pure `genetics/Cross.java`: for each gene the odds of each shown look in a pup of A x B (Mendel, 50% per allele), the litter size from the parents' Fertility, the expected drift of the pups' stats from both parents' stats (the same `Trends`/`Stats` maths the seed uses, quality 0), the mutation chance. Shown as a third spread in plain words (`Plain`) with hover boxes like the sheets | none (pure Java + the existing screen) |
| T4 (implemented, 2026-10-15) | Stationary A/B sample vials on the bench and a quiet interaction click | Existing pedestal extraction/submission pattern; `SoundEvents.WOODEN_BUTTON_CLICK_ON` |

Left out on purpose (ideas, not planned): a Seed slot, redstone output, power, consuming the samples (the table never uses them up), reading the
Pedigree Book from the table, a Gene Splicer built on the same block (that is its own design turn).

## 3. T1 decisions made without asking
1. **One sample, any hand.** Only a Tissue Sample WITH readable genes can be put on (a blank creative one is ignored). With a sample in hand and the table empty it goes on (one of the stack; creative keeps the stack); with the table loaded it SWAPS (the old sample comes back to your inventory).
2. **Reading = any other click.** An empty hand or any other item (the Calipers, a pickaxe) opens the sample window of what is on the table. Sneaking with an item in hand never reaches the block (vanilla), so taking back needs **sneak + both hands empty**.
3. **Reading reuses the S8 sample window** (`ModNetwork.sendSample`): one sheet of the animal the sample came from. No new network or screen code in T1.
4. **The click rules are pure** (`genetics/TableUse.decide`): five outcomes (STORE, SWAP, TAKE, READ, NOTHING), checked exhaustively; the block only does what it says.
5. **Synced like the pedestal** (update tag / packet trio) so the client decides the same way as the server (it needs to know whether the table is loaded to answer the click) and T4 can draw the vial without touching the save format.
6. **Looks:** a wooden bench with brass trim and a green work mat, four legs, and a small copper scope on top. Axe-mineable (it is wood), drops itself, breaking it pops the sample out.
7. **Recipe:** `W C W / P P P / P . P` (W whisker, C copper ingot, P any planks, the vanilla planks tag).

## 4. T2 decisions made without asking (session 37)
1. **The table is a stack.** A fills first, then B. With both full, a new sample SWAPS with B (the one put on last). Sneak + empty hands take B first, then A. So there is no "which slot did I click" question (the table has one click surface) and the pure rules stay five facts and one slot function (`TableUse.decide` / `slot`).
2. **One window, one spread per sample.** Reading always opens the table window (header `Table: sample A` / `Table: sample B`), even with one sample (one spread, Previous / Next greyed); the one-sheet `Tissue sample` window stays for the Calipers. T3 adds the forecast as the last spread, only when both slots are full.
3. **Same sheet as the sample window**: the spreads are `SheetLayout`'s sample sheet (no adult / pup, no parents, the portrait box, hover boxes), only the header differs (`Kind.TABLE_A` / `TABLE_B`).
4. **Wire = text.** `TablePayload` is one string, `A|B`, each side `Tissue#toString` or empty (`TableSheet.toString` / `parse`), the same pattern as every other payload; the network version went to "3".
5. **A T1 table keeps its sample**: the block entity reads the old `Item` key into slot A when `SampleA` is missing.
6. **Left out on purpose:** the forecast (T3), drawing the samples on the table and a sound (T4), a hover on which slot a click would use.

## 5. T3 decisions made without asking (session 38)
1. **Plain guavas.** The forecast does not know which guava you will feed, so it uses guava quality 0: the only quality it counts is the parents' own starborn bonus (the seed adds that by itself). The Quality row and its hover say better guavas and Fortune raise it. The numbers are the seed's own (`Cross.seed()` = `Seed.of(A, B, 0)`), so the table cannot disagree with the Calipers on the real seed.
2. **Odds before mutation.** Looks are Mendel only (four equally likely pairs, the stronger allele shows: 25 / 50 / 75 / 100%). A mutation hits one random gene of a pup, so it is shown as its own row (`Mutation x% per pup`), not smeared over the odds; the right page and the hovers say so.
3. **Expected points, not a spread.** A stat row shows the points of A and of B and the points a pup is expected to have (average + drift). The rounding to whole points is random per pup, so siblings differ by a point; the hover says that instead of listing odds per stat.
4. **Only with both samples.** The forecast is spread 2 of the window and exists only when both slots are full (`TableSheet.isForecast`); with one sample the window is unchanged from T2.
5. **No portrait and no links** on the forecast (a litter has no single look); every row has a hover instead.
6. **Left out on purpose:** the odds of a pup CARRYING a gene (only what shows), the chance that a litter of n has at least one pup of a look, real units for the expected stats (a pup's genes are not known), a guava / Fortune selector.

## 6. T4: visible samples (2026-10-15)

`GeneticsTableRenderer` draws the actual synced Tissue Sample item for each
occupied slot, stationary at the front left/right of the bench and clear of the
microscope. It follows the existing pedestal's 26.2 render-state extraction and
item submission API. Taking a sample clears its render state. No new block-state
property, payload or saved-item format is needed; T1's old `Item` fallback to A
still applies.

Successful store, swap, take and read actions play one quiet wooden-button click
from the server. Empty/invalid actions remain silent. The table never consumes a
sample. Gradle compilation and model checks pass. The user reports the release compiled
and worked in game; A only, A+B, swap, take, chunk reload and sound remain useful
regression checks, without separate reported results.

Diet update (2026-10-16): the sample sheets and forecast now include inherited
Diet as a seventh row. Old loaded six-gene samples normalize to plain Diet
through the existing tissue parser. Both client and server require network
version 4 at introduction (current protocol is 7). `checkDiets` verifies old table payloads, the diet inheritance odds
and seven-block forecast clearance; the new diet behavior is gameplay-pending.

## 7. T5/T6: plan splices and fix hidden alleles (2026-10-16)

Two readable samples now give **five spreads**, in order: sample A, sample B,
the existing look/stat forecast, inherited-copy odds, and splice plans. One sample
still gives just its own sheet. The saved slots and `A|B` payload remain unchanged.
Analysis spreads have no individual portrait. Use with a Gene Splicer in the main
hand to open directly on the splice plan; other reads start at sample A.

**T5 — Splice plans:** the left page treats A as recipient/B as donor; the right
page reverses that direction. All seven genes show the exact before/after code
pair, the donor's expressed allele and whether it would show or stay hidden.
Already-carried alleles explicitly refuse. Hover describes the replacement,
costs and recovery. The page calls the actual pure `Splicing.plan`, so it uses
the tool's lower-dominance copy rule, including first-copy ties. Reading spends
nothing and edits no animal. Samples are snapshots; a live animal can have changed.

**T6 — Inherited copies:** left page gives the chance a pup carries at least one
copy of an allele, including homozygotes and hidden copies. These probabilities
can overlap and need not add to 100%. The right page gives the chance of two
matching copies of each allele. Every gene has a full hover even when a long
summary clips. All probabilities enumerate the four actual parental combinations;
they are before mutation. Hover also gives the chance of at least one success
in an uncrowded litter, mixing native rounded litter sizes rather than treating
the mean as a fractional number of pups. Crowding can reduce that litter.

The **Two Copies** heading shows the probability that all seven pairs are fixed
in one pup (independent gene probabilities multiplied); its hover gives the
whole-litter probability and explains that this is not one specific target breed.
This supports the Purebred Registry (`REGISTRY_SPECS.md`). Hover GENOME on existing
sheets for a fixed-pair count and remaining mixed genes.

Evidence: the actual build and server suite pass for native registry use, table
read preservation and saved table inputs. `checkBreedingLab`, included in build,
checks recessive/carrier/fixed crosses, integer/fractional litter probabilities,
both-direction splice pages, legacy payloads, page clearance and registry bounds.
Client navigation, pointer hovers and multiplayer packet delivery remain gameplay
review. No existing creature geometry or renderer changed.

## 8. T7/T8: a connected lab bench (2026-10-18)

**T7 — Automation:** native item transfer exposes the existing saved A/B slots
on every side. Each holds one readable Tissue Sample, including arbitrary
components. Hoppers fill A then B; a full table refuses further input instead
of swapping or stacking. Extraction starts at A. Power the appropriate hopper
to retain samples for comparison. Transfers never consume/interpret a sample,
roll offspring, mutate animals or change the table's manual LIFO withdrawal.
Comparators read 0 empty, 8 for one sample and 15 for both. This gives a useful
lab workflow from the existing dispenser sampling stations through hoppers.

Both slots use native transactional journals over the actual saved stacks.
Aborts restore all touched slots and issue no renderer/comparator updates;
root commits mark/save/sync the ordinary block entity. Input validation refuses
blank/malformed samples and other items; extraction can recover existing saved
contents even if unreadable. `SampleA`, `SampleB` and old single-sample `Item`
loading stay intact. No replacement inventory, block state, ticking routine,
menu or payload is introduced. Breaking still returns both whole samples.

**T8 — Actual hand:** native item-on-block handling uses the real interacting
hand for storing/swapping a sample. An empty main hand passes to a filled off
hand; both hands empty are required for sneak-withdrawal. One success stores
one item, survival spends one from that actual stack and creative preserves
it. Ordinary no-sample reading and B-first manual take/swap remain unchanged.
This resolves the old main-hand fallback despite T1's any-hand design.

Guide page 15 now describes hopper ordering and comparator signals without
renumbering pages. Client sample-vial rendering uses existing A/B sync.

T7/T8 evidence: actual Java 25 build/all 42 native cases pass. A real input
hopper fills two distinct named samples A/B and retains surplus; output drains
one whole sample. An aborted two-slot transaction restores both inputs. Native
comparators update to 15, 8 and 0. The case covers unreadable/non-sample input,
whole A/B saves and legacy Item loading, actual server-mode survival/creative
off-hand placement, and exactly two remaining named drops on break. Existing
planning/payload/pure forecast cases remain passing. Laboratory/roll-call/index
guide preview inspected. Client vials, real hopper circuits and modded devices
remain gameplay review.

## 9. T9: preview the feed and treatment before spending them

Read the table with one guava in either hand to preview that tier for both
parents. With two guavas, use their actual quality average, matching real
breeding. A Mutagen in either hand previews treating that prospective seed.
One guava opposite Mutagen combines both choices. Clear cues for the default
plain, untreated forecast. Both samples are needed for analysis; a single
sample still gives only its source sheet. No item is spent, and no table,
animal, seed or inventory is edited. All counts/names/components persist.

The plan uses the actual `Seed.of` and `withMutagen` rules. Guava quality and
starborn bonus combine once, then expected bred stats use that seed's drift.
Treatment uses the native mutation floor; per-gene look/copy odds and litter
size remain unchanged and explicitly precede mutation. Fortune remains a
later crop-harvest bonus. Quality hover names the exact feed pair, both
quality contributions and the before-Fortune total. Mutation hover/right-side
working uses the chosen rate. Advanced plans open directly on the forecast;
normal reads and the Gene Splicer shortcut retain their existing destinations.

This is a server-captured immutable plan, not a client guess at held items
when the packet arrives. `TableSheet` accepts old `A|B` as plain/untreated and
keeps that exact serialization for default plans. Nondefault payloads append
`|tierA,tierB,treated[,fortune]`, with validated tier/level bounds and a 0/1 flag. Invalid
plans refuse. Native A/B block saves are unchanged; no plan is persisted on
the bench. Network **7** requires matching updated clients and server because
the table message gained this optional form. Other item/animal formats stay
unchanged. Guide page 54 and guava/Mutagen tooltips explain the controls.

T9 evidence: actual Java 25 build/all 43 native cases pass. Focused lab checks
compare mixed tiers, starborn and treatment against the real seed; preserve
Mendelian/litter odds; reject malformed plans; and check page bounds for
ordinary, mixed and top-tier plans. The native table case uses named two-hand
feeds and a vial, reads without changing cues/samples, checks defaults after
clearing, and round-trips the actual payload codec. Plain-versus-planned
forecast and guide/index previews are inspected with substitute fonts. Generate
that forecast with `tools/PlanDump.java` + `tools/SheetDump.java`, then pipe to
`art/gen_book_preview.py --output art/planning_preview.png`. Actual client
navigation/fonts and multiplayer delivery remain gameplay review.

## 10. T10: preview the actual harvest tool

A Fortune-enchanted item in either hand adds its native gameplay enchantment
level to the immutable plan. Two items choose the highest level rather than
adding them. Use the same `ItemStack.getEnchantmentLevel`/registry lookup as
the real Seed Crop harvest; native levels 0–255 are supported. Missing Fortune
in a replaced registry means no bonus. The tool, enchantments, durability and
all other components stay intact. Only currently held cues participate:

| Held cues | Preview |
|---|---|
| One guava | That tier for both parents |
| Two guavas | Their actual quality average |
| Guava + Mutagen | That feed for both, with treatment |
| Guava + Fortune tool | That feed for both, with harvest bonus |
| Mutagen + Fortune tool | Plain feed, treatment and harvest bonus |
| Two Fortune tools | Plain feed with the higher level |

Seed quality remains the feed average plus starborn. Effective **hatching**
quality additionally uses the real `Seed.fortuneBonus` (0.5 per level), and
expected stat drift calls the real seed with that extra quality. The quality
row/hover distinguishes seed subtotal, bonus and effective total. Fortune
changes no litter size, mutation probability, genes or pre-mutation copy odds.
The actual bonus is earned only by harvesting a Seed Crop with the appropriate
tool. Cradle hatching grants none; clear the Fortune cue to plan a Cradle.

A nonzero level is an optional fourth comma field in the same protocol-7 plan;
old default A|B and three-field plans remain readable. No saved item/seed data
or actual harvest mechanics change. Guide page 54 points to **Plan the harvest**
on page 55; page IDs and the ten-entry New index stay stable.

T10 evidence: actual Java 25 build/all 43 native cases pass. Focused math
checks match real seed drift with Fortune III, preserve base quality and
pre-mutation/litter odds, reject negative/oversized/malformed levels and bound
pages even at native level 255. Native table reads an actual named, worn,
Fortune-enchanted tool, retains the feed/tool and exact 2 -> 3.5 quality working,
selects the maximum of two tools, and round-trips the extended native packet.
Plain/feed-treatment/harvest forecast and guide previews are inspected. Client
input/fonts and natural progression balance remain gameplay review.
