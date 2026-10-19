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

## D9: safe hunting and shore approaches

Food pursuit now uses bounded, complete, dry, home-contained native routes
rather than handing moving prey directly to unrestricted melee navigation.
Search up to four nearest compatible wild animals inside an eight-block
sphere, with eight route attempts. A prey's cell or a nearby supported dry
spot is usable; fish can be bitten from a safe shore already in native melee
reach. Moving prey is replanned every ten ticks, with a twenty-second visit
bound and native twenty-tick bite spacing. No teleport or forced water entry.

Named, led, riding or vehicle animals join the existing baby/player/bunnidog/
tame/ally exclusions. Players can protect untamed livestock by naming or leading
it. Eligibility, sight, native melee reach, current home and target ownership
recheck through pursuit; another combat target is never cleared by stopping
this goal. Native damage/protection still decides the hit. Only a real selected
prey kill while current Diet/care/home eligibility still matches heals four
HP and starts the existing saved 600-tick food rest. No new animal save field.

Shared HomeRoutes now evaluates current native path types as well as cached
node types. New fire/water/magma/powder-snow hazards and lost floors invalidate
old routes. Native neighbor path classification is only queried after its
loaded bounds are checked; no missing terrain is requested. Existing route
users, including gardening, refuge and guards, share this correction. Adjacent
non-contact cactus/berry warning types remain usable for normal safe work.

Java 25 build/all 61 native cases pass. The new safety case exercises actual
paths and a real native meal kill, named/leashed/boat exclusions, magma placed
under an already planned route, interruption on renaming and relocation, and
preservation of a replacement combat target. The prior fishing case still
performs its real shore kill with exact healing/rest. All earlier resident/
care/automation scenarios remain passing. Guide preview inspected; natural
pursuit over varied terrain and client behavior need gameplay review.

## D10 / L33: care for spent chorus

Bloom +20 adults whose Diet accepts chorus fruit can revive a supported spent
chorus flower (native AGE 5) to AGE 0 for one fruit. Healthy residents bank it
at a Mixed/Garden enabled trough; injured adults can eat that fruit directly
for the normal two HP and 600-tick food rest, without a trough. Direct meals
never teleport the animal or create an additional loose fruit. Healthy work
shares the existing 1,200-tick GardenWait. Calipers reports the Bloom threshold
only for chorus-compatible lines. This adds a bred End-garden role; ordinary
Bloom-zero founders gain no free flower work.

Chorus flower ages 0–4 describe native branch generations, not ripeness. Those
living flowers stay unchanged, including at high Bloom. Reviving only spent
flowers preserves the branch and end-stone support, then allows ordinary
native random growth; it never forces a branch, fruit drop or growth tick.
The fruit is this mod's care yield, separate from vanilla flower block loot.
Existing home/daytime/health/transport/danger, Diet/filter/capacity, safe dry
routes, power/mode, native grief/destroy protection and saved recovery rules
apply. Exact state, living support and the Bloom bonus recheck after callbacks.

Java 25 build/all 64 native cases pass. The new chorus case checks living-age,
Bloom +19/+20, Diet, work mode and power refusals, a protection callback that
removes the bonus, exact one-fruit/age-zero/shared-rest work, preserved branches
and a direct two-HP meal without loose food. The End/basket guide preview is
inspected; natural native branch growth and client behavior remain playtest review.

## D11: deliberate hand care with a plain guava

Sneak-feed a plain guava to an ordinary living bunnidog for direct care instead
of breeding. One guava heals the normal two HP and grows a pup by the existing
hand-food amount. Every Diet accepts this explicit guava care, including plain
lines that otherwise have few hand-fed healing foods. An adult needing no
healing keeps the guava and enters no love mode. Higher-tier guavas are kept
with an explanation; use normal feeding to spend them on breeding as before.

Manual care uses the existing hand-food rules, including creative preservation
and no new hand-feeding cooldown. It neither resets nor reduces existing meal,
work or care rests, breeding cooldown or an already remembered love tier/time.
Paused workers may still be cared for. Ordinary non-breeding hand foods keep
their Diet and need/growth rules. Both manual care paths now consume the actual
hand's input before native healing callbacks, preserving any replacement stock
that a callback puts in that hand. Genome, stats, ancestry and tags remain
unchanged; the Apex still rejects food. No new item, field or packet is added.

Java 25 build/all 69 native scenarios pass. The new native hand-care case uses
a named actual off-hand stack, paused plain-Diet adult with breeding cooldown,
exact two-HP healing, preserved food/work rest, ready adult without love/memory,
healthy and upgraded refusal, real pup growth, creative hand preservation,
normal Golden-guava breeding and care retaining its exact love tier/time. A
real native heal callback replaces the hand with diamonds after seeing the
reserved final guava; new stock and biology remain intact. The Apex still
refuses. The new care/food/Specialist/Cradle guide preview is inspected; client
gestures and long-term husbandry remain gameplay review.
