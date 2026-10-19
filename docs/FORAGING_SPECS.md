# Outdoor food D6

Implemented 2026-10-18. The original Diet grazing/hunting rules and D5 shore
fishing remain; this extends plant meals and closes an unsafe stale-write path
in legacy grazing. Trough and hand-feeding controls are unchanged.

## Fruit meals

Injured ordinary adults with a claimed Nest can eat ripe hanging guavas within
their daytime home area. Guava suits every Diet through the existing food tag.
Sweet berries at age two/three and fruiting native cave-vine heads/bodies are
also available when `canEat` accepts their item, including data-pack Diet tag
extensions. One meal heals **2 HP** and starts the existing saved **600-tick**
food recovery shared with troughs, grazing and hunts. No hunger meter, breeding
feed or love mode. Full health, babies, no home, combat/danger, player transport,
night and existing recovery retain their ordinary refusals.

Guava Fruit resets to age zero, sweet berries to age one, and cave vines lose
only their berries. The actual plant type stays; there are no loose duplicate
food items. Native growth determines regrowth. Leaves/support must remain
valid. No trough or hoe is required for a direct meal. Healthy residents use
the separate opted-in gardening system; night meals remain near-Nest troughs.

## Safe native commitment

All grazing now chooses dry sturdy standing space with a complete loaded,
home-bounded hazard-safe path, including old hay/grass/crop meals. Adjacent
standing avoids deliberately routing into damaging berry bushes or planted
crops. An eight-path budget bounds each search. Sight, reach and route validity
are rechecked while eating. Missing safe standing space simply refuses.

World-changing meals honor native mob-griefing and destroy protection. The
planned block state, animal eligibility, Diet, support and route recheck after
callbacks, before changing the plant. The previous implementation retained a
crop state across callbacks and could overwrite a replacement chest; both
legacy crops and fruit now preserve it without healing or starting meal rest.
Only a successful block update commits healing. Existing non-destructive hay
feeding remains available when mob griefing is disabled; it consumes no block.
No Nest ticket, item component or animal save format changes.

## Evidence

Java 25 build/all 48 native scenarios pass. The added case uses a real home,
complete safe paths, ordinary-Diet ripe guava, native head/body glow vines,
sweet-berry Diet refusal/acceptance, exact plant resets and shared healing/rest.
Actual protection callbacks replace both a guava and legacy wheat with chests;
neither is overwritten. Unripe/full-health/no-home/night/griefing refusal,
no loose duplicates and unchanged griefing-off hay/crop regrowth also pass.
The guide adds page 61 and a fifth Habitat link. Software text preview is
inspected; natural resident feeding and client appearance remain gameplay review.

## D7: ordinary loose-food meals

Injured ordinary adults may eat one compatible loose food near their claimed
Nest by day, sharing the existing two-HP meal and saved thirty-second food
recovery. This follows field grazing in the normal goal order. The item must
be at least ten seconds old, have no native pickup delay or owner reservation,
lie in loaded dry home terrain and be reachable by a complete safe route.
Upgraded guavas are excluded, preserving breeding supplies. Native Diet and
trough-safe food rules also exclude container/remainder items. There is no love
mode, automatic pup growth, food spawning or inventory raiding.

A native mob-griefing callback precedes commitment. New reservations, missing/
changed items, changed eligibility and unsafe paths cancel without costs.
One item is extracted through a native journal; remaining names/components
stay intact and only an empty source entity is discarded. Pickup animation
and thrown-item criteria use the committed one-item snapshot. Native player
pickup events are player-only; this uses the ordinary mob-griefing hook.

Java 25 build/all 52 native cases pass. The new meal case checks age boundary,
pickup delay, owner reservation, upgraded-guava/night/griefing/Diet refusals,
callback-added reservation, exact named-stack remainder, two-HP/shared rest,
stale disappearance and final-item removal. The guide chapter and new index
are previewed; natural feeding/client appearance remain gameplay review.

## D8: sustainable habitat meals

Injured adults gain four more Diet-matching daytime meals inside their claimed
home area, without a trough. Desert browsers can trim a spare top from a two/
three-block cactus; the base, lower segments and any attached cactus flower
remain. Mushroom eaters may consume one extra red/brown mushroom only while
another living same-color mushroom remains within three blocks in loaded home
terrain. Nether foragers eat fully ripe wart, resetting it to age zero on its
soul sand. Foragers/omnivores eat a melon only if a living attached melon stem
points at it; native neighbor updates restore that stem at age seven. Decorative
melons and young plants stay. Data-pack Diet extensions use the normal food tags.

All four share D6's two-HP healing, saved 600-tick food rest, health/home/daytime/
danger/transport rules, complete safe routes, sight/reach, mob-griefing and
native destroy protection. A tall cactus uses the same bounded three-block
reach and near-face sight point as healthy gardening; workers never need to
stand on the cactus. No loose food is also paid. Growing-stock rules are now
shared by direct meals and trough gardening in ResidentPlants, retaining prior
harvest behavior. Stock, Diet, support and route recheck after protection hooks.

Java 25 build/all 56 native cases pass. A new native case checks a real home/
path, solitary/three-high/flowered cactus, callback-added flower, griefing,
separate red/brown seed stocks, lost/restored same-color companion, exact
meal healing/recovery, unripe/Diet wart refusals and age-zero reset, decorative/
detached melon refusal and native age-seven stem reset. No loose food is created.
Existing cactus/mushroom/melon gardening and D6/D7 meals remain passing. Guide
preview inspected; natural ecology and client routines need gameplay review.
