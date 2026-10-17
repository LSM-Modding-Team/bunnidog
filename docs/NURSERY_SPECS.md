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
