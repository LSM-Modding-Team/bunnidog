# The Animal Sheet and the restructured Pedigree Book (Phase 8) — S1-S8 and S5b coded (see `docs/CHUNKS.md`); only the Genetics Table is open (the page-turn sound is S7b, session 29)

**CURRENT (session 43): ALL DONE: S1-S8, S5b and S7b work. Nothing open here.**

Docs only. Read this together with `docs/DESIGN.md` ("Phase 8 restructure decisions") and the "Phase 8a" notes in `docs/history/PHASES.md`.
Where this file and an older Phase 8a statement differ, THIS FILE WINS (8a built a 3-animals-per-page list; this replaces that list).

## 1. What the user asked (session 19, in order)
1. "A normal person doesn't know what 22-32-00-10-00-01 means." The book must not show raw genome codes as the main information.
2. "1 page per bunnidog. It should contain everything. Both specific and user-friendly." The page shows: the bunnidog **rendered**; the **genome code**; **an explanation of the genome**; and the **stats in real units**, e.g. `Vitality: +3 (number of hearts rendered)`, `Speed: +7 (speed in m/s)`.
3. Restructure the plan and update the docs (this file).

## 2. Decisions (made without asking; change freely)
* **"1 page" = one two-page SPREAD of the open book per animal.** One column is about 124 x 156 px: it cannot hold a portrait, a gene table AND five stats with units. The left page is the animal (who it is, what it is made of), the right page is what it can do (stats, growth, offspring, parents). If the user meant a single page, tell Claude: the alternative is a taller book with tabs, which needs more new API.
* **The book becomes: 1 contents spread + 1 spread per logged animal** (so 1 + up to 24 spreads). Previous / Next walk through them in order. The contents spread replaces the 3-animals-per-page list of 8a (`PedigreeBookLayout`).
* **The code stays, but it is never the explanation.** The genome code is shown once per sheet (it is the animal's exact identity, useful for `/summon` and for the breeders who like it), with each of its six pairs lined up with a plain-words row, and the sheet explains how to read it.
* **A sheet is a snapshot taken when the animal is logged** (genome, bred stats, pup or adult, ear tag, parents' genomes). Stats and genes do not change during an animal's life; a pup that was logged keeps showing "pup" until the player logs it again (which updates the entry, like an ear tag does today).
* **One model for every window:** an `AnimalSheet` (= the data of one `Pedigree.Entry`, see 7) is shown by the book (from the log) and later by the Calipers window (8b, from a packet with the same fields) and the Genetics Table/seed windows. One layout, one screen class.
* **Everything that decides numbers stays pure Java** (like the drops engine): plain-language text, unit conversions, page layout. The screen only draws what the layout lists, so what the self-check tests is what is drawn. The preview tool (`tools/book_preview.py`) is extended to draw sheets the same way.

## 3. The book, spread by spread
**Spread 0, contents.** Left page: title "Pedigree Book", "N of 24 animals", and "How to read a genome" (see 5.4). Right page (and the rest of the left): the index, one line per animal `#7 BD-3F7A2C  spotted, short ears` (the look phrase, 5.1), in two columns of about 14 lines (28 lines hold the 24-animal maximum). The book shows no index lines past what is logged.
**Spreads 1..N, one per animal in book order.** Layout below. Previous / Next walk contents -> #1 -> #2 -> ... -> #N and stop at the ends (greyed out).
**Empty book:** unchanged (chat message, no window).
**Jumping from an index line to its sheet** (a click) is a later chunk (S7) because the click handler's signature changed in 26.2 (see API_NOTES). Until then: Next / Previous only.

## 4. The sheet (one spread). Budget: the page is 17 lines of 9 px
Book texture grows from 300 x 190 to about **312 x 216** (still inside the 320 x 240 minimum GUI size, buttons 20 px under it); columns about 124 px wide, text area about 156 px tall. All sizes are to be tuned with the preview image.

**LEFT PAGE: the animal (17 lines)**
| Lines | What |
|---|---|
| 1 | `#7  BD-3F7A2C` (number, ear tag) and right-aligned `adult · gen 1` or `pup · gen 1` |
| 5 (45 px) | **The portrait**: the bunnidog rendered, in a framed box on the parchment (see S4; turns slowly) |
| 2 | `GENOME` label and the code `22-32-00-10-00-01-00`, each pair drawn separately so a pair can light up (gold) when its gene row is hovered |
| 7 | Seven gene rows, one per gene, in code order: `Ears  short`, `Coat  spotted /brown`, `Legs  standard`, `Build  average /stocky`, `Temper.  loyal`, `Fertil.  average /prolific`, `Diet  plain`. The word after `/` (muted) is the **hidden** gene the animal carries |
| 1 | Legend: `/x = hidden gene it carries` |

**RIGHT PAGE: what it can do (17 lines)**
| Lines | What |
|---|---|
| 1 | `STATS` (and a muted `points -> real value`) |
| 2 each = 10 | **Vitality** `+3`: a row of heart icons (full/half), then `7.4 hearts`. **Speed** `+7`: `13.8 blocks/s`. **Leap** `0`: `1.4 blocks high`. **Senses** `+2`: `hears 21 blocks`. **Bloom** `+12`: `grows up in 9.4 min`. Each real value is shown with the plain bunnidog baseline in muted text (`base 4.0 hearts`) so "+3" means something |
| 3 | `NEXT LITTER` : what the line will drift toward when bred with a same partner (from `Drift`): `Vitality +1.0, Leap -0.5`, or `no drift`; plus the starborn bonus line when it applies |
| 3 | `PARENTS`: `Mother #3 tan, long ears` / `Father not in the book: slate, hoofed`; or `no recorded parents (wild or summoned)` |

**Hover boxes** (drawn by the screen with plain `fill` + `text`, not the vanilla tooltip methods whose parameters are not verified): hover a gene row -> what the gene does and how it is inherited (`Short ears: hears 35% less far. Hidden "long": about half of its pups get it.`) and the matching pair of the code lights up; hover a stat -> the working (`8 HP base x 1.375 stocky build x 1.12 trained = 12.4 HP`); hover the portrait -> what it drops when killed (the `Drops.describe` content in words); hover the code -> the full "how to read" text. This keeps "everything" on the sheet without crowding it.

## 5. Plain language (all pure Java, new `genetics/Plain.java`; one table, tested for completeness)
### 5.1 The look phrase (index lines, parent summaries)
`coat` always, `temperament` always last, and each other gene only when its expressed allele is NOT the common default (allele id 0: standard, average, loyal... but never skipped for coat): `Spotted, short ears, long legs, loyal` / `Tan, loyal`. Hidden genes are not in the look phrase.
### 5.2 Carries
`Carries: brown, stocky, prolific` = the hidden allele of every gene that has one, as plain words.
### 5.3 One sentence per allele
A table `allele -> (word, effect sentence)` for EVERY allele of EVERY gene (the self-check fails if one is missing, so a future allele cannot be forgotten). Words are the allele names unless the name is jargon; effect sentences are derived from the numbers in `Gene` so they cannot disagree with the game (e.g. ears `value 0.65` -> "hears 35% less far"; legs `1.20` -> "about 12% faster and 24% higher jumps" from `Traits`). Jargon to explain in a sentence: chonker, swarm, starborn, springy, fennec, bat, hoofed, woolly, lean, fleet, glow, and the four temperaments (read `Temperament.java` when writing them).
### 5.4 How to read a genome (page 1 and the code's hover box)
"Seven pairs, one per gene, in this order: Ears, Coat, Legs, Build, Temperament, Fertility, Diet. In each pair the first digit came from the mother and the second from the father. When the two differ, the stronger one shows and the other is carried: it does not show on this animal but it can pass to its pups."

## 6. Real units (all pure Java, new `genetics/Units.java`)
Base values from `Bunnidog`: health 8.0, speed 0.25, jump 0.42, follow range 16.0. Final value = base x gene factor (`Traits`) x bred factor (`Stats.multiplier`).
| Stat | Real value shown | How |
|---|---|---|
| Vitality | hearts (icons) and `x.x hearts` | `hearts = 8.0 x Traits.healthMultiplier() x Stats.multiplier(VITALITY) / 2` (one heart = 2 HP); icons in halves, up to 10 icons, beyond that one icon and `xN` |
| Speed | `blocks/s` | `attribute x 43.17`. **ASSUMED:** 43.17 is the vanilla ground factor (player 0.1 = 4.317 blocks/s; I recall horse 0.3375 = 14.6 blocks/s, same factor). To be CALIBRATED in game: time a bunnidog over 20 flat blocks; the constant is one line. Base 0.25 -> 10.8 blocks/s |
| Leap | `blocks high` | jump height from a small simulation of vanilla vertical motion (gravity 0.08, drag 0.98 per tick) for `attribute = 0.42 x Traits.jumpMultiplier() x multiplier(LEAP)`. Self-check: 0.42 -> about 1.25 blocks (the known vanilla value) |
| Senses | `blocks` it notices things from | `16 x Traits.hearingMultiplier() x multiplier(SENSES)` (the follow range attribute is in blocks) |
| Bloom | `minutes to grow up` | `Growth.minutesToAdult(Stats.growthMultiplier())` (exists today) |
Points are always shown too (`+3`), because the points are what breeding changes; the real values are what they mean.

## 7. Data: the entry grows (save format v2, backward compatible)
Today a `Pedigree.Entry` = genome, mother genome, father genome, optional UUID, optional ear tag. The sheet also needs **stats** and **pup or adult**. New text form (appended after the tag, before the parents): `genome#uuid@tag$3,-1,0,2,12!<mother+father`, where `$a,b,c,d,e` are the five stat points and a trailing `!` right after them means "was a pup". Old books (no `$`) still parse; their sheets say `stats not recorded: log this animal again`. Logging an animal already in the book with new facts is an UPDATED (the same path the ear tag uses). Size: about 150 characters per entry, 24 entries about 3.6 KB in a string data component (limit 32,767): fine. Parents are recorded by genome only (their stats are not known), so parent summaries are looks, not sheets.

## 8. Code structure (new/changed). All in pure packages unless marked
* NEW `genetics/Plain.java` (5), `genetics/Units.java` (6).
* CHANGED `genetics/Pedigree.java` (Entry gets `Optional<Stats> stats` and `boolean pup`; `rows()` carries what the sheet needs; text form v2), `genetics/Readout.java` (chat lines use `Plain`; the raw code moves to the LAST line, `Code: ...`).
* NEW `layout/SheetLayout.java`: for one entry, the list of things to draw with positions: text lines (with a color kind), heart icons, the portrait rectangle, gene-row hotspots with their hover text. Replaces the list part of `PedigreeBookLayout`; `BookPaging.wrap` is kept. NEW `layout/BookContents.java` for spread 0.
* CHANGED client `PedigreeScreen` (draws spreads, hit-tests hotspots, draws hover boxes), `ClientScreens` (door unchanged). NEW art: bigger `pedigree_book.png` plus a small icon strip (full / half / empty heart, and one 9 x 9 icon per stat) drawn in our own texture, so no vanilla sprite names are needed. `art/gen_book_gui.py` extended.
* CHANGED `Bunnidog#mobInteract` (passes stats and pup flag when logging) and `PedigreeBookItem.log` message.

## 9. Chunks (small, each compile-gated by the user; Claude never starts the next before the user reports)
| Chunk | What | New Minecraft API |
|---|---|---|
| **S1** | `Plain` + `Units` (pure) with tests: the user's example `22-32-00-10-00-01` decodes exactly to "short ears, spotted coat carrying brown, standard legs, average build carrying stocky, loyal, average fertility carrying prolific"; every allele has text; unit formulas incl. the jump simulation. No behavior change in the game | none |
| **S2** | Entry v2 (stats + pup) with parse/format tests and old-book compatibility; logging stores them; the chat readouts (Calipers, seed, tissue, Pedigree log messages, tissue tooltip) use plain words with the code last | none |
| **S3** | `SheetLayout` + `BookContents` + the new screen and texture: contents spread + one sheet per animal, hearts as icons, **portrait box as a placeholder** (framed, coat-colored); preview images for the user to judge BEFORE the portrait exists | same as 8a (`blit`, `fill`, `text`) |
| **S4** | The portrait. FIRST verify how to draw a living entity in a 26.x GUI (see API_NOTES: the picture-in-picture `entity` method exists, parameters unknown); then a throwaway client-side `Bunnidog` built from the snapshot (genome, pup flag) in the player's level. **If it cannot be verified, the fallback is a flat portrait**: coat swatch plus silhouette sized by the genes; the sheet works without it | `GuiGraphicsExtractor#entity` or `InventoryScreen`'s entity drawing, entity construction on the client |
| **S5** | Hover boxes + the pair-highlight (custom drawn from `fill` and `text`) | none (mouse position is already a parameter of `extractRenderState`) |
| **S6** | The Calipers window (8b): server sends the sheet fields of one bunnidog in a small payload, the client opens the SAME screen (a book with one sheet, no contents). Needs the payload registration verified first | networking: payload registration (new) |
| **S7** | Click an index line to jump to its sheet, arrow keys to turn, page-turn sound | mouse and key event signatures changed in 26.2 (`MouseButtonEvent`): verify |
| **S8** | Seed and Tissue Sample windows (same sheet, no parents), Genetics Table later | menu (8c) |

## 10. Risks and open questions
* **The speed constant (43.17)** may be off for these mobs: the sheet would then show a wrong m/s. Mitigation: S1 puts it in one constant and the test list for S3 asks the user to time a bunnidog.
* **Space.** 17 lines per page is tight: animals with long ear tags or a pup flag still fit (one line each), but the right page is full. If the preview shows it cramped, the first thing to move to hover is the "next litter" line.
* **Entity rendering in a GUI** (S4) is the least certain piece of API in the whole plan; the flat fallback keeps the sheet usable.
* **A 312 x 216 book** does not fit a very small window at a high GUI scale; the screen will clamp it to the window (position only: it will not shrink), which can cut the bottom at tiny sizes. If that matters, a "compact" texture is a later chunk.
* **English only.** All sentences live in `Plain`, one place, so a language file is a mechanical later step.
* Question for the user, answered by their reaction to the S3 preview: is one spread per animal what "1 page" meant?

## 11. Where S3 deviated from this spec (session 22)
Book 312 x 200 (not 216) and drawn with fills, no texture; portrait = flat coat swatch (S4 is the real one); hearts are drawn from a bitmap, not a texture strip; contents index has no ear tags; the left header shows only `#n  tag` and `adult`/`pup`, with `generation n` on the GENOME line; parents are `Mom`/`Dad`; page text area 130 x 156 px; gene-row hidden genes are `/word` right after the shown word. Everything else (spreads, order, right-page contents, 17-line budget) is as specified. See "Phase 8 S3" in docs/history/PHASES.md.

## 12. Where S4 deviated from this spec (session 23)
The verification came out well, so the flat silhouette fallback was NOT needed as the main path (it stays as the safety net): the portrait is the real animal, drawn with `InventoryScreen.renderEntityInInventoryFollowsAngle` from a throwaway client-side `Bunnidog` built from the logged genome and pup flag. It sways slowly by itself (it does not follow the mouse) and is not ticked (no tail wag). The box is the S3 one (130 x 54 px, not the 66 px of section 4). Hover-the-portrait-for-drops is still S5. See "Phase 8 S4" in docs/history/PHASES.md.

## 13. Where S5 deviated from this spec (session 24)
Done: hover on the six gene rows (with the pair highlight), the code, each stat, plus two extras that cost nothing (NEXT LITTER and each parent line show their whole text, because the page clips them). NOT done: hovering the portrait for what the animal drops (spec section 4); it needs a sheet wording of `Drops` and is a small chunk of its own. Hovering a single code pair shows the whole code's "how to read" box, not that gene's box.

## 14. S5b (session 25)
The one hover S5 left out is done: the portrait box lists what the animal drops (`Drops.describe`, the Calipers wording). Spec section 4's hover list is now complete.

## 15. Where S7 deviated from this spec (session 26)
Done before S6 because it needs no networking. Index-line click and arrow keys as specified; extra: the Mom / Dad line of a sheet jumps to the parent's sheet when the parent is in the book. NOT done: the page-turn sound (no sound name verified yet).

## 16. Where S6 deviated from this spec (session 27)
The spec said "server sends the sheet fields of one bunnidog in a small payload". The payload is ONE string: the text form of the animal's `Pedigree.Entry` (the same text the book saves, so the fields and their parser already exist and are tested). The client opens the SAME `PedigreeScreen` with a single entry (no contents, no buttons, no arrow keys, no links). The header of a single sheet is the ear tag (or `Bunnidog`) instead of `#n  tag`, and it has no `generation n` and no parent numbers (those only exist inside a book). The chat readout of the Calipers on a bunnidog is replaced by the window for real players (a non-player keeps it). NOT done: the seed / crop / Tissue Sample windows (S8).

## 17. Where S8 deviated from this spec (session 28)
Done: the seed window and the Tissue Sample window, on the same screen (no menu API needed: they are read-only windows, opened by a packet like S6). A seed has TWO animals in it, so its window is a folio: spread 0 = the seed itself (quality, litter, mutation, drift, with hover boxes), spreads 1 and 2 = the mother's and the father's sheets, reached by clicking their block or with Next. A sample is one sheet without adult / pup or parents. NOT done: the Genetics Table (the block version, a menu), the page-turn sound.

## 18. S7b: the page-turn sound (session 29)
The one item section 15 left out. `PedigreeScreen.pageSound()` plays vanilla's book sound through the client's sound manager whenever the spread actually changes (buttons, arrow keys, index and parent links, the seed window's Mother / Father blocks). It is silent when the target spread is the current one. The one-sheet windows never turn, so they stay silent. Two vanilla names are unverified until the user compiles (see "Phase 8 S7b" in docs/history/PHASES.md).


Diet layout update (2026-10-16): the portrait height is 45 px, Genome label y=59,
code y=68, gene rows y=79..133 and legend y=145. The forecast budgets its gaps
for seven gene blocks, preserving the footer. Old six-pair payloads load with
plain Diet; hover text explains its foods and feeding/breeding distinction.
A temporary layout-only preview of actual Page data was inspected using a test
font and portrait placeholder; final Minecraft font/portrait appearance remains
a gameplay check.

## S9: record a carried animal

Hold a filled Travel Basket in the other hand and **sneak-use** a Pedigree Book
to log its occupant without unpacking. The same gesture on a Purebred Registry
registers a qualifying stable bred adult. Ordinary use still opens the existing
book/registry, so logging is deliberate. Both hands work; aiming at a block with
the record book also handles this gesture before any unrelated item use.

The server reads the existing full `Pedigree.Entry` from native basket data:
UUID, genome, stats, age class, parents and Ear Tag. Missing/malformed identity
is refused with feedback while retaining the entire basket. All existing book
capacity/dedup/update and registry adult/parents/seven-fixed-pair/unique-line
rules apply. Registry creation shares its live-animal transaction and Breeds
True advancement. No animal is instantiated, released, edited or healed; native
payload, recovery, home tickets and item counts stay intact. The actual held
book receives creative writes. Existing snapshot components, UI and protocol 6
are retained. Native tooltips and guide pages 7/17/39 explain the controls.

S9 evidence (2026-10-17): actual Java 25 build and all 30 native scenarios pass.
The existing Basket case now exercises real game-mode air and block use,
normal-use preservation, exact native records, creative/off-hand writes,
duplicate protection, carried registry advancement, missing UUID refusal and
real pup refusal. Its later release still checks all paused native state.
The shortened record/registry/basket preview is inspected; actual client
navigation and fonts remain gameplay review.
