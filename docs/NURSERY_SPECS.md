# N1 — redstone seed nursery

Dispensers can plant a saved litter seed into the adjacent empty block above
farmland. Face the planting space, not the soil. One successful pulse spends
one seed and stores its exact existing parents, bred stats, quality and Mutagen
flag in the ordinary Seed Crop block entity. It rolls no litter or mutation.
Hand planting and dispensing share the same loaded-air/farmland placement
transaction; existing crop/seed save formats are retained.
Seed Crops now use the native `maintains_farmland` block tag, so planted dry
plots retain farmland like ordinary crops. Trampled dirt still supports the
existing crop and retains its litter, as before.

A dispenser facing an unripe planted Seed Crop can use one Starlit Dust to ripen
it immediately. Hand dust and dispenser dust share the same transaction and
retain the saved litter. Bone meal keeps its existing native partial-growth
behavior. Breaking a ripe crop still performs the existing hatch; no resident
gardener cultivates or harvests these embryo crops.

Missing/malformed seed data, obstruction, wrong soil, unloaded/border refusal,
failed placement, empty/invalid crops, already-ripe crops and other plants
consume/eject nothing. Valid saved crop data and support are required before
spending dust. A Dropper provides ordinary seed/dust item delivery. Tools and
consumables in other slots keep their existing dispenser behavior.

The native dispenser click/particles report success or failure. No fake player,
new item, inventory, recipe, payload, entity or saved component is needed. Existing
hand creative preservation applies; dispensers always spend one on success.

## Evidence — 2026-10-17

Actual Java 25 `build runGameTestServer` passes all 31 native scenarios.
`NurseryGameTest` uses redstone/scheduled dispenser pulses for missing litter,
obstruction, wrong soil, exact one-seed planting, occupied crop, one-dust ripening,
already-ripe/empty/other-plant refusals and no ejection. It round-trips the actual
crop block entity with both parental genomes/stats, quality and Mutagen intact.
Native hand placement/ripening, repeat refusal and creative preservation pass.
It also runs the native dry-farmland random tick beneath a planted Seed Crop.
The fixture uses nearby water until this deliberate dry-soil check, avoiding
random decay of empty farmland before planting.

The revised guide preview is inspected. Client redstone contraptions, natural
chunk travel and multiplayer nursery workflows remain gameplay review.

## N2 — preserve the planted item

Planting by hand or dispenser now stores a one-item copy of the whole seed,
including its custom name and arbitrary data components. Unripe player recovery
and lost-support recovery return that item. Functional seed updates and hand
Mutagen treatment change only `seed_data`; other components survive. Reads and
source stacks are defensive copies. Optional `SeedItem` uses the native item
codec beside the existing authoritative `Seed` text; old crops reconstruct their
ordinary functional seed. Ripening and real litter genetics stay unchanged.

N2 evidence: Java 25 build/all 32 native scenarios pass, including named/custom
seed copies, native item/crop saves and legacy reconstruction, actual survival
break, support loss and exact Mutagen split costs without component loss.

## N3 — Nursery Cradle

A craftable full-block incubator holds one whole litter seed. Recipe:
`WWW / GNG / PPP` (any wool, glass, one Nest, any planks). One minute of loaded
incubation advances one of seven stages; saved remaining time pauses while
unloaded. Bone meal adds the existing 2–5 seed-crop stages; Starlit Dust ripens
at once. Full seeds, names, components, age and remaining time save natively.

Use a seed to insert one. Empty-hand use inspects, or attempts a ripe hatch;
sneak-use with both hands empty withdraws the seed, resetting incubation.
Hoppers supply/extract the one seed through the native transactional handler;
aborted transfers preserve incubation. A comparator reads 0 empty, then
1/3/5/7/9/11/13/15 as stages advance. Breaking drops contents once; pistons
cannot move the block. Calipers reads the stored seed forecast. Dispensers can
supply a seed or apply Starlit Dust to its front face.

A fresh redstone rising edge attempts one ripe hatch. Holding power does not
repeat or hatch a seed that ripens later; pulse again. The entire 3×3 pad one
to three blocks in front needs loaded dry two-block air, safe sturdy floors,
world-border clearance and no living occupants. The existing forty-animal
local crowd limit applies. Each rolled pup must also fit its actual bounds.
Create/check the entire litter before adding it; native spawn refusal removes
any newly added siblings and retains the seed. Successful hatching consumes
the seed once and uses the existing genes, bred stats, quality/Mutagen and
parent records, with no Fortune bonus. Every failure retains stored data.

Existing crops, settlements, homes and animal save formats are preserved. No
screen, payload, custom data component or creature visual change is introduced.

N3 evidence: actual Java 25 build/all 35 native server scenarios pass. Three
dedicated cases cover real hopper insertion/extraction and transaction abort,
saved exact remaining incubation, actual ticker/comparator, survival insertion,
bone meal/dust/withdrawal, one block/one stock break drops, obstruction/fluid/
hazard/occupant/crowd refusal, parental records, empty-hand hatching and fresh
versus held redstone power. An actual native spawn listener cancels the second
pup after removing the Cradle during the first spawn: reentrant extraction and
hatching see reserved stock, all siblings roll back, exactly the named seed
returns, and retry succeeds. The existing dispenser case additionally covers
real Cradle seed/bone-meal/dust pulses and ripe refusal.

The authored block textures and guide pages 1/41/42 are previewed and inspected.
Native client appearance, custom fonts, natural unload/reload and modded transfer
devices remain gameplay review; server fixtures do not establish those details.

## N4 — committed harvest and environmental recovery

Crop harvest now runs from native `onDestroyedByPlayer`, after every break-event
handler and the player's native restrictions have accepted removal. Cancelled
breaks create no seed items or pups. A crowded ripe crop stays planted and
retains the entire seed; accepted survival unripe breaks recover it once.
Creative still destroys unripe crops without drops and hatches ripe crops.

The crop reserves its full item before removal. Ordinary environmental removal
(including replacement by water) returns the saved seed through native block
entity removal, without rolling pups. Lost-support upkeep shares that same path.
Native block-drop rules and explicit no-side-effects removals apply normally.
Player ripe hatching keeps its existing placement, Fortune bonus and genetics.
Both crop and debug-command hatches prepare the entire litter and roll back all
new siblings on native spawn refusal. After a committed crop removal, a failed
hatch refunds the original seed rather than overwriting the replacement block.

N4 evidence: actual Java 25 build/all 36 native scenarios pass. A real late
`BreakBlockEvent` cancellation preserves both unripe and ripe crops without
premature items/pups; Adventure restrictions, accepted survival/creative
harvests, original parental records and crowded ripe retention pass. A native
spawn listener refuses the second pup only after observing the crop gone:
no siblings remain and exactly the named seed returns. Replacing a ripe crop
with water preserves the water and refunds that seed without a litter. Existing
support-loss, legacy item saves, Cradle and dispenser cases remain passing.

## N5 — treat a litter in place

Use Mutagen on a valid Seed Crop or occupied Nursery Cradle, or face either
with a Mutagen dispenser. One successful application sets the existing saved
Mutagen flag for that exact litter. It changes no parents, bred stats, quality,
item name/components, crop age, Cradle stage or remaining incubation. Ripe
seeds may be treated before hatching. A previously treated, empty, malformed,
unsupported or inappropriate target retains the vial; machines eject nothing.
Native hand permissions apply and creative preserves the vial. Dispensers
always spend one on success. Existing held-seed/off-hand treatment is preserved.
Withdrawal, seed recovery, native saves and Calipers all use the updated seed.
No new item, data component, packet or mutation/inheritance rule is introduced.

N5 evidence: actual Java 25 build/all 38 native scenarios pass. The native
treatment case uses real survival off-hand crop interaction, Adventure refusal,
creative actual-stack preservation, named exact seed/parents/stats/quality,
unchanged ripe crop and saved Cradle stage/remaining timer, plus native codec
round-trip. Real scheduled dispenser pulses exercise Cradle/crop success,
already-treated/empty/wrong targets and no vial ejection. Native incubation
continues naturally after treatment rather than restarting. The 44-page guide
preview includes the new treatment page; actual client use remains review.
