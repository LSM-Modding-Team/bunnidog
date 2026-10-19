# Resident work L28–L30 / N9

These jobs extend the existing opted-in, healthy adult routine. Work needs a
claimed Nest, daytime, safe loaded routes, sight/reach, no danger/transport,
native mob griefing and a hoe-enabled, unpowered trough. Diet and exact food
components still apply. All jobs share the saved one-minute GardenWait; no
carried inventory, forced terrain loading or new animal field is introduced.

## L28: choose a trough's work

Sneak-use any hoe to cycle a mode and enable work. Normal hoe use toggles work
without changing that mode. Either hand works, costs no durability and respects
Adventure/interaction protection. Redstone pauses resident access as before.

| Mode | Resident jobs |
|---|---|
| Mixed (default) | Existing harvest, cultivation, surplus compost and loose-food gathering |
| Garden | Harvest, cultivation, surplus replanting and loose-food gathering |
| Gather | Loose-food gathering only |
| Compost | Surplus composting only |
| Nursery | One-stage Cradle tending only |

Modes affect work, while food/healing, filters, stock capacity, native hoppers
and comparator behavior keep their existing rules. Empty-hand inspection names
the current mode and enable state. A native optional `WorkMode` string saves it
alongside existing Food/FoodFilter. Missing/unknown values use Mixed; old block
states need no migration and existing enabled troughs retain their old routine.

## L29: restore garden gaps

Bloom +10 adults in Garden mode can spend one real food while leaving at least
eight in the trough. They plant into loaded, dry, empty home terrain within the
existing work area, on valid native support. Foods that plant themselves are
carrots, potatoes, sweet/glow berries, mushrooms, Nether fungi, wart and cactus.
One plain guava establishes hanging fruit beneath leaves. One melon slice pays
for its ordinary one-seed recipe and a young melon stem. Wheat/beetroot need
their separate seeds and are not silently converted; upgraded guavas and embryo
seeds never qualify. Cactus work plants a base rather than extending columns,
and refuses a cell intersecting a living animal.

Harvest/cultivation takes priority. Planting pays no extra produce and preserves
the remaining source components/filter; native growth handles the new plant.
Explicit Garden mode keeps this food-spending action out of legacy Mixed work.

## L30 / N9: nursery tending

Bloom +20 adults in Nursery mode spend one ordinary guava above the same
eight-food reserve to advance a nearby occupied Nursery Cradle by exactly one
stage. Upgraded guavas are left for breeding. No quality, parents, stats, Mutagen,
name or other seed component changes. Growth wait resets as for normal stage
advancement. Ripe/empty Cradles refuse; workers never hatch the litter. Existing
player/redstone hatching, pad safety and incubation remain shared.

The worker captures the actual Cradle, exact whole seed and current age. A
replacement seed, changed age, missing block, lost Bloom, mode/power change or
unsafe route cancels the stale job. The native one-stage helper rechecks the
same seed/age and attachment atomically before changing incubation.

## Conservation and protection

Before spending, native grief/destroy hooks run and the job rechecks. Planting
also invokes the native mob placement gate at the target before writing, with
the same pre-placement snapshot convention as native mob placement. Recheck
again after that gate and after reserving stock, including callbacks caused
by stock/comparator notifications. A failed job returns the exact reserved
item to its original attached trough or drops it once if the source is gone
or cannot accept it. Success consumes it only after placing/growing. Eight
unspent food remain; no free plant, overwritten container or lost litter.

## Evidence

Java 25 build/all 59 native cases pass. Three new scenarios check real crouched
off-hand controls/cycling, normal pause, saved/legacy/unknown policies and
Adventure refusal; real claimed-home paths and Bloom/reserve/mode thresholds;
native placement refusal/container replacement/source-removal conservation;
one-item named carrot/guava planting and shared rest; cactus occupant and embryo/
upgraded-food exclusions; nursery Bloom +19/+20, reserve/tier refusal, a seed
swapped during real stock notifications, exact one-stage cost with unchanged
whole named treated litter, and explicit ripe hatching. Existing work, care,
automation and nursery scenarios remain passing. The guide preview uses a
substitute font; natural worker cycles and client controls need gameplay review.
