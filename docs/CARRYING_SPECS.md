# H11 — inherited carrying capacity

Implemented 2026-10-19 from the original `DESIGN.md` Build/carrying-capacity
proposal. Ordinary adults can carry **whole native bundles** in visible leather
panniers. This gives Build a practical use while keeping native bundle packing,
tooltips, selected contents, colors and item codecs. H11 introduced protocol 12
for equipped-bundle count/art; current **protocol 15** adds trail companions
and commissions. Update both sides. H13's opt-in cargo meals are described at
the end and in `TRAIL_SPECS.md`.

## Capacity and controls

| Expressed Build | Bundle slots |
|---|---|
| lean, slim | 1 |
| average | 2 |
| stocky, woolly | 3 |
| chonker | 4 |

Hidden copies grant no extra slots. Bred points do not replace the Build limit.
Newborns inherit capacity through their genome; they inherit no physical cargo.

- Use a vanilla bundle, empty or filled, from either hand on a calm ordinary
  adult to equip it. Native dyed bundle variants work. Survival spends the
  actual whole bundle; creative preserves the hand and equips one copy.
- Both hands empty, normal use: show count/capacity and chat entries with native
  item hovers. Sneak with **both hands empty**: retrieve the last bundle, keeping
  the rest. Take a bundle back to edit it with the native inventory controls.
- Calipers includes a read-only cargo row. Adventure users can inspect; equipping
  and withdrawal require normal build permission. Pups refuse new equipment but
  can return existing cargo if age was changed externally. Apex refuses equipment.
- Combat/refuge, love, current hurt/fire and the first five seconds of native
  recent-attacker memory refuse new bundles. Leads and rides remain compatible
  with carrying. Refusal consumes no stock and does not spill the held bundle.

Each slot holds one whole bundle. Names, native colored item ids, arbitrary item
components and all contained items remain on that stack. There is no alternate
bundle-weight rule, extra inventory menu, hopper capability or per-tick storage
scan. Hover/inspection returns snapshots rather than mutable owned stacks.

## Smaller builds and excess cargo

Gene changes preserve all existing cargo. A smaller carrier cannot add more while
at/above its new limit. Each **excess** bundle reduces native movement speed by
**25%**, capped at **75%**. This makes capacity matter without discarding items
after a splice or command change. Retrieval removes the penalty as excess falls;
items within capacity impose no penalty. Calipers/status reports overload.

The speed adjustment is one named **transient** multiplier. It composes with
other native attributes and external modifiers, reapplies after native load and
cannot accumulate with repeated saves/gear changes. It never rewrites bred stats.
Visible cargo breaks A10 camouflage; equipping/returning also emits native
`EQUIP` vibration. Removing all cargo can restore matching-ground cover.

## Saves, transport and death

Optional `Bundle0`–`Bundle3` fields use the native whole `ItemStack.CODEC`.
Existing animals load empty; valid slots compact in order. There is no genome,
seed, sample or Travel Basket format migration. The synced count is derived from
the owned stacks. Saves retain excess stock as well as current Build.

The existing Travel Basket copies full entity data, preserving cargo, identity
and ordinary paused care timers. Packing/discarding the original produces no
equipment drop; releasing restores the same bundles. Player cargo drops through
native `dropEquipment` on death, including with mob loot disabled. Native drop
capture/protection hooks and item-entity behavior remain in charge afterward.

The actual hand pays before equipment becomes visible to callbacks. Withdrawal
reserves/removes its bundle before inventory/drop/event notifications. Death
reserves and clears **all** bundles before spawning captured equipment drops, so
reentrant drop callbacks see no owned cargo to duplicate.

## Visuals and evidence

`art/gen_packs.py` owns the generated native equipment layer, 64×32 leather atlas,
English feedback and actual-sculpt preview. Ten fitted strap segments and up to
four bags use a separate native material pass. Shared body/ground transforms
follow expressed legs/build and pup scaling. The approved sculpt, 512px fur
atlases, head UVs, normals and base mesh coordinates remain intact.

Unadorned animals keep **2,172 faces / 8,688 vertices** and submit no equipment
pass. One through four bundles add **78 / 96 / 114 / 132 quads**, respectively.
The largest current webbed/swarm/four-bundle combination is **2,496 faces /
9,984 vertices**. The stored sculpt remains **2,460 faces / 9,840 vertices**;
equipment is separate. The model gate verifies actual extra draws, every bag's
visibility reset and exact body/root pose across builds, legs and pup scaling.

Build/model checks and all **87 native scenarios** pass at the cargo checkpoint.
Native cases use actual hands, colored/named bundles holding diamonds and a
component-bearing Tissue Sample, all Build limits/hidden copies, full refusal,
smaller-build retention, immutable snapshots, native saves and external speed
modifiers. Actual controls cover Adventure, read-only Calipers, last-first return,
pup refusal, creative copies and restored camouflage. Actual death with mob loot
off and a reentrant drop callback conserves the exact bundle once. Actual basket
capture/release retains every bundle and identity without duplicate item drops.

The fitted accessory/actual-sculpt and Field Guide previews are inspected.
Native GPU attachment, play balance and long trips remain manual gameplay review.
## H13 extension — opted-in trail provisions

Keepers may use a bowl on a trained trail partner to permit suitable ordinary
meals from its own carried bundles during Follow/Wait. One actual item heals
two HP with shared food recovery; upgraded guavas/unrelated cargo remain.
Native names/colors, remaining templates/order and callback-refilled equipment
are conserved before healing. Empty bags stay equipped. The default is off,
and ordinary resident mode keeps its existing food behavior. Baskets preserve
the preference/cargo/rest. See `TRAIL_SPECS.md` for full rules and native cases.
The base H11 capacity/overload/equipment rules below remain the foundation.
