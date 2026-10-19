# G6 — Breeding Pen and S11 — seed quality colors

G6 completes the original Breeding Pen block in `DESIGN.md`'s breeding content
layer. S11 completes the earlier missing visual cue for seed quality. Existing
Bloom already supplies gene/stat-dependent pup growth through `Growth`; the old
Phase 2 growth note does not justify adding a second growth modifier.

## The Pen

Craft `FHF / PWP / CCC`: two wooden fences, hay, two planks, one Whisker and
three copper ingots. Place this two-manger controller inside the keeper's pen;
it does not create fences or move animals. Its front faces the placer. Looking
at that front, **A is left and B right**. Click a half with guavas to fill it
from either hand. Both mangers preserve one exact stack of up to 16, including
the guava tier/name/components. Survival pays actual hand stock; creative keeps
the actual hand. The model independently shows A/B stock, with native rotations.
Only guavas still allowed by `bunnidogs:bunnidog_food` are accepted or used;
datapack changes cannot make the Pen bypass the configured breeding-food policy.

Give the pair actual Nests and dry walking space. The Pen finds exactly two
living ordinary **adults** in its twelve-block surrounding search box. A third
adult refuses the attempt, even if that third animal is resting or led. Pups
do not count until adult. Both chosen parents must be within four blocks of
the Pen, less than three blocks apart, in sight of each other and on safe dry
ground. A/B assignment follows their positions across the front, with UUID ties.

Each parent must be healthy, age zero, out of love/guava memory, without leads,
rides, passengers, combat, refuge, water/lava or fire. It must retain an actual
loaded Nest and stand within its home boundary. Native home-route hazard types
and snow footing apply. The complete surrounding census area must already be
loaded; inspection makes no chunk requests or path requests and changes no
navigation or home tickets. Calipers, including sneak-use, report status and the
two parent identifiers with their corresponding guava names/counts.

Empty-hand ordinary use deliberately feeds a ready pair. Sneak with **both
hands empty** withdraws both mangers instead. Adventure may inspect but cannot
insert, feed or withdraw. A real rising redstone edge schedules one automatic
attempt; held power and stock changes cannot feed again. Placing under power
does not create an edge. A future pulse can retry when parents are ready.

Automation inserts A from above and B horizontally. Bottom access is closed;
automatic extraction cannot drain the parental food. Hoppers on the surrounding
floor can collect the eventual loose litter seed. Comparators read food stock:
zero if both empty, otherwise `1 + floor(14 * min(A count, B count) / 16)`.
Only one stocked manger gives 1, while sixteen in both give 15. The signal is
stock, not a promise that the pair is ready.

## Paid natural courtship

A successful attempt spends **one guava from each manger**, remembers the
corresponding actual tier on each parent and starts native love. The existing
BreedGoal walks/mates them normally and creates one seed from their real genes,
stats, averaged guava qualities and existing Starborn bonus. Ordinary fertility
rest follows. The Pen never directly creates a seed, teleports a parent, rolls
pups or bypasses mutation/crowding rules. The seed is an ordinary loose drop.
Later attacks, player control or movement can interrupt the paid courtship,
just as with ordinary hand feeding; a successful feed does not guarantee a seed.
Players can also change a mating by introducing another in-love adult later.

Automatic feeding respects the native mob-griefing rule/event for both parents.
Deliberate player feeding remains available when only automation is protected.
The exact controller, facing, pair/readiness and both exact food resources are
rechecked after native permission callbacks. Interrupted attempts preserve stock.
A busy guard refuses recursive feeding during those callbacks.

Both costs share one native root transaction. Native love/tier states are set
after both costs reserve successfully and before root-commit notifications.
Those notifications therefore cannot observe a fed parent alongside its unpaid
food. Removing the controller during committed stock notifications drops only
the remaining stock; it cannot duplicate either consumed guava or feed again.

Only actual food stacks and ordinary block states are saved. Native animal
love, remembered tiers and rest use the existing animal save format. Breaking
the Pen returns remaining food plus the ordinary block drop; it resists pistons.
There is no saved pair claim or new global animal ledger.

## S11 — Stored-quality colors

Seeds retain their original cream silhouette at quality zero. Higher stored
qualities use the corresponding guava palette, plus small light markings to
distinguish dark/bright neighbors. Bands are **0, 1, 2, 4, 7, 12, 20 and 33**;
choose the highest threshold reached. Thus 1.5 is golden, 6.99 netherite and
20 Dragon Egg. Tooltips still show the exact quality and explain the color band.
The look does not claim a particular genotype, litter size or original fruit.

Native item range dispatch uses the client-only `bunnidogs:seed_quality` numeric
property, which reads the existing valid `seed_data` directly. Old, carried,
planted/recovered, gifted and treated seeds all use the same rule, with no new
CustomModelData stamp, quality component or save migration. Missing/malformed
data uses the ordinary seed fallback. Fortune remains added only at harvest;
Mutagen/genes/stats and actual hatching/juice math are untouched.

`art/gen_seed.py` owns all eight item palettes and the range-dispatch model.
`art/gen_breeding_pen.py` owns the Pen's independent fill/facing models, textures,
recipe/unlock/drop/text and front-facing preview, reusing workshop asset helpers.
The Pen and seed art previews are inspected. Native GPU appearance remains an
in-game check; headless resource checks do not establish it.

## Compatibility, guide and validation

Update both sides for **protocol 10**, which requires the new controller and
client model property. Existing genome, animal, seed and other item formats
remain forward compatible. There are no new custom packets. Fresh Field Guides
have **100 pages, eight indices and 73 chapter links**: workshop index 98 links
to Pen page 99, which points to seed-color page 100. Both return to 98. Existing
written guides retain their saved pages; craft a fresh one for new chapters.

Native scenarios cover genuine A/B hopper input, front-side hand placement,
creative/adventure/Calipers behavior, a real redstone pulse followed by actual
BreedGoal mating and correct seed quality/rest, held-power refusal, full-component
food saves and withdrawal. Further cases cover third adults/pups, health/rest/
leads, walls/magma, remote unloaded inspection, protection refusal and parent
movement, recursive activation and source removal at commit. The existing
native item codec retains the complete hundred-page guide. Workshop checks
cover every Pen visual state, texture/model reference and seed-band threshold/
fraction boundary. See `START_HERE.md` for the final build result.
