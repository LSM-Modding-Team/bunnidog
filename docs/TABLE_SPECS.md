# TABLE_SPECS — the Genetics Table (plan, session 36)

**CURRENT (2026-10-15): T1–T3 work. T4 is implemented and compiles; its in-game appearance/sound still needs review.**

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
sample. Gradle compilation and model checks pass; inspect A only, A+B, swap,
take, chunk reload and the sound in Minecraft before calling T4 gameplay-tested.
