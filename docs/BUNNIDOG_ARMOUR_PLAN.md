# Bunnidog barding — one visible equipment item

Design proposal following the player's request for tiered, whole-animal armour.
Revised after the player rejected light high-tier clothing and then asked for a
smoother progression: padding → partial plate → fitted armour → full plate →
reinforced full plate. Each upgrade visibly extends the previous suit. Netherite
and Astralite retain **complete articulated plate coverage**.
**Not implemented yet.** The current release still uses the dungeon champion
profiles described in `CROWN_WARREN_SPECS.md`. This design replaces those profiles
when the equipment system is implemented; it does not add another bonus on top.

## What the player sees and does

A bunnidog wears **one suit of barding**, rather than four armour pieces. The
same item fits an ordinary adult, a winged adult and every Apex body. It is an
overall equipment upgrade: protection, health, bite/ability damage and modest
mobility, with no changes to inherited genes or learning. A common unarmoured
adult remains a useful companion. Breeding a good fighter remains worthwhile.

Five tiers follow the existing material progression:
**Leather → Iron → Diamond → Netherite → Astralite**. No new standalone ore,
equipment workstation, animal level or armour proficiency is needed. Gold is
omitted from this first progression rather than adding a mandatory intermediate
tier with no clear purpose.

Hold barding and use it on your calm, bonded adult to equip it. Using a different
suit swaps the two actual stacks and returns the old one to your inventory (or
drops it normally if full). Equipped barding is shown as an item in the existing
animal conversation's deliberate equipment/cargo choices, with a **Remove armour**
action. Keep the normal borderless animal conversation: no new opening screen,
ambient statistics, permanent hints or extra click before ordinary interaction.
The item's native tooltip carries the equipment bonuses; genetics still need
their existing measuring tools.

Only the keeper may equip, swap or remove armour, within ordinary interaction
reach and build permission. Refuse swaps during combat, injury recovery, courting
or another exclusive transfer. A quiet, downed companion may have its armour
removed for repair without first paying for recovery. Pups cannot wear it.
Unowned adults must be bonded first. Hostile Apexes cannot be disarmed, equipped
or calmed by right-clicking them; their equipment comes from encounter creation
or explicit administrative commands. Shears retain their existing use.

## Shape and material language

The top tiers must immediately read as **fully armoured creatures**, with
substantial metal volume and a complete silhouette from front, side and rear.
The lower and middle tiers build towards this through incremental coverage:
Iron introduces the open helmet and greaves that Diamond subsequently encloses,
rather than Diamond suddenly introducing head and leg armour from nothing.
Fit the approved anatomy rather than putting a rectangular box around the body.
The complete suit consists of the following components; the tier table below
specifies when each becomes padded, partly plated or fully enclosed.

- A full domed helmet with brow rim, reinforced cheeks, rigid nasal/face guard,
  narrow eye openings and a breathing/bite grille. No exposed forehead or large
  bare cheeks. The faceplate bridges facial detail instead of copying lips and
  nostrils as a metallic face. Keep it recognisable as a helmet.
- Segmented sleeves protect the long rabbit ears, preserving their tapered
  outline and bending. Armour also closes around the smaller ear bases.
- A substantial gorget joins the helmet to the breastplate. An enclosed cuirass
  protects chest, flanks, entire back, belly and rump, with overlapping plates
  and a central crest. Narrow articulation seams replace large exposed gaps.
- Raised pauldrons and hip guards overlap plated upper legs. All four limbs
  have articulated knee/hock guards, greaves and paw sabatons, not bare legs
  emerging from a cloth vest. Small joint gaps and claw/sole clearance remain.
- A segmented tail guard completes the rear, following the existing tail bone.

Helmet, body and limb components are visual parts of **one equipped item**;
there are no separately crafted/managed head, torso or boot items. The resulting
anatomy is still quadrupedal, with the original ears, stance and paw shapes.
Genes remain identifiable through the animal's build, ear/leg proportions and
behaviour, with coat glimpses at small joints; keeping most fur visible is no
longer a high-tier design goal. The helmet makes equipment unmistakable during
a frontal encounter, rather than relying on a small buckle below the face.

| Tier | Visible construction |
| --- | --- |
| Leather | Warm brown padded chest/flank harness and back strip, soft crown cap and narrow leather wrist/hock wraps. Face, ears, paws and most limbs remain free. Stitching and small brass fasteners establish the suit's common construction. |
| Iron | Adds a silver open-face helmet with temple/nasal guards, short ear-base collars, shoulder plates, three flank panels, a partial breast/belly plate and two back plates. All four legs gain greaves and small knee/hock guards; exposed upper limbs, face, ear tips, paws and rump still distinguish it from the next tier. |
| Diamond | Blue-cyan fitted armour closes the chest/back/belly/rump and adds a broad-opening visor, longer ear sleeves, upper-leg protection, paw sabatons and a tail-base guard. Modest shoulder/hip plates and dark flexible mail at the joints keep it visibly lighter than Netherite. The muzzle and ear/tail tips are still freer. |
| Netherite | Completes heavy coverage in charcoal-violet: closed visor with small eye/breathing openings, full ear sleeves and tail guard, thick gorget, overlapping cuirass, broad raised pauldrons and fully articulated plated limbs. Closes the remaining Diamond openings and replaces its flexible sections with overlapping protective lames. Pale edges reveal thickness. |
| Astralite | Reinforces that complete suit: indigo-grey plate, doubled gorget, raised overlapping spine lames, strengthened shoulder/joint guards and copper fasteners. A distinct chest crest and restrained turquoise inlays match existing Astralite equipment. Extra fitted reinforcement visibly distinguishes it from Netherite without returning to light coverage. |

The progression is anatomical as well as material: a leather crown cap becomes
an open Iron helmet, then a broad-opening Diamond visor, then a closed Netherite
helmet and reinforced Astralite helm. Leg wraps become greaves, then upper-leg
and paw armour, then full articulated protection. Body padding acquires separate
metal panels before becoming an enclosed cuirass, then receives thicker overlap
and reinforcement. Do not collapse these intermediate stages into two repeated
meshes (one harness and one recoloured complete suit).

Shape changes as well as colour distinguish the tiers. Astralite inlays must read
in ordinary light; they do not create a bright aura or hide attack particles.
Dungeon equipment is the same Astralite item players can craft, not an invisible
enemy-only tier. The Crowned Apex retains its separate crown above the barding.

Wing roots pass through fitted articulated sockets in the shoulder/back armour,
not large bare patches of torso. Winged upper-tier suits add leading-edge and
root guards, with overlapping coverts over the inner wing; outer flight feathers
remain free. Follow the existing moving shoulder/hand bones and folded outline.
Existing bundle packs mount outside the plate on actual anchor points, and their
filled silhouette must not disappear inside the cuirass. Fit slim/chonker builds
and short/long legs through shared rig transforms, without changing collision, size, wing genes
or the original body mesh. Armour shares hit flashes, Blastmaw white/red fuse
flashes and swelling, aerial dive poses and the downed pose.

`art/barding_design.png` is a headless **concept**, using the existing sculpt and
proposed fitted surfaces. It is not a shipped armour model or an in-game capture.
The concept source is `art/preview_barding_design.py`; its studies show all five
tiers, frontal visor coverage, rear/side views and different builds. Wing/pack
clearance, full native Apex scale and moving poses still need the eventual
runtime model and client review.

## Starting balance

These are proposed equipment values, to be tuned from actual play. Armour and
toughness are native points; health, damage and speed multiply the unarmoured
animal's corresponding attributes. They do not replace the genetic foundation.
The coverage revision changes the visual design, not these proposed numbers.
Head/leg plates do not add a separate set of regional damage multipliers.

| Tier | Armour | Toughness | Max health | Outgoing damage | Movement | Knockback resistance | Durability |
| --- | ---: | ---: | ---: | ---: | ---: | ---: | ---: |
| Leather | +2 | +0 | ×1.10 | ×1.05 | unchanged | +0 | 320 |
| Iron | +6 | +1 | ×1.25 | ×1.15 | unchanged | +0.05 | 800 |
| Diamond | +10 | +2 | ×1.50 | ×1.30 | +2% | +0.10 | 1,600 |
| Netherite | +12 | +3 | ×1.75 | ×1.45 | +5% | +0.15 | 2,400 |
| Astralite | +14 | +5 | ×2.00 | ×1.60 | +8% | +0.20 | 7,200 |

No bonus to intelligence, loyalty, fertility, breeding points, carrying capacity,
attack cadence, jump reach or ability cooldown. Genes and existing role AI keep
those differences. Equipment movement affects the normal walking/swimming/flying
travel controller through one speed factor; do not multiply flight twice or
change its orbit distance. Existing cargo overload still applies.

Damage scales **once** at the outgoing attack calculation, including bites,
pounces, beams, drains, brood throws and anvils. It must not both increase the
attack attribute and multiply the resulting hit again. For Blastmaw explosions
and fireballs, multiply entity damage while keeping explosion power, block
destruction, fuse and attack timing unchanged. Self-explosion health cost is
still a direct cost proportional to equipped max health; do not multiply it by
the outgoing damage bonus or let its own armour reduce it. Lifebinder healing
continues to use the real accepted drain, with its existing multiplier. No bonus
to heart debt or stun duration.
Broodcaller spawns keep their own genetics and stats: equipment is neither
inherited nor copied to summoned creatures.

Apply health and damage limits at the actual native attributes, so effective
readouts agree with real combat. Ordinary Apex health still uses the native
1,024 bound; the Crowned Apex retains its dedicated 16,000-bound health attribute.
Never reduce an exceptionally bred animal's existing base health to make room
for armour. Native armour/knockback caps remain authoritative. Resistance,
armour-bypassing damage and protection cancellation keep their usual semantics.

Equipping or swapping cannot heal: preserve current absolute HP and clamp only
if the new maximum is lower. Removal/breakage cannot kill a living animal on its
own, grant temporary HP, clear injury or reset ability timers. Fresh encounter
actors are equipped before receiving their initial full health. Genetics and
enrage recalculation must retain equipment without stacking modifiers.

## Crafting and upkeep

Use one non-stackable, durable item per tier. Proposed IDs are
`leather_bunnidog_barding`, `iron_bunnidog_barding`,
`diamond_bunnidog_barding`, `netherite_bunnidog_barding` and
`astralite_bunnidog_barding`. English display names use “Leather Bunnidog
Barding”, etc.; shared neutral Spanish uses “Armadura de cuero para bunnidog”,
“Armadura de hierro para bunnidog”, etc. These are future translatable keys,
not hardcoded UI labels.

The initial leather harness is crafted with six leather, one iron nugget and one
string, with pattern `L L / LNL / LSL`. Further tiers use native smithing so
names, existing damage components and compatible enchantments survive:

| Upgrade | Base | Addition | Template |
| --- | --- | --- | --- |
| Iron | Leather barding | Iron block | None |
| Diamond | Iron barding | Diamond block | None |
| Netherite | Diamond barding | Netherite ingot | Vanilla Netherite Upgrade |
| Astralite | Netherite barding | Astralite Reinforcement | Existing Astralite Upgrade |

An Astralite Reinforcement is a crafting ingredient, not another wearable piece:
four Astralite Ingots, two Cinderite Ingots, two Aerial Membranes and one Apex
Marrow, pattern `AMA / CRC / AMA` with A=Astralite, M=Membrane and R=Marrow.
This prices the complete suit as four player armour upgrades while keeping it
fully obtainable before the Crown dungeon. It introduces no new boss-only gate.

Repair with the corresponding ordinary material (leather, iron, diamond,
Netherite Ingot, Astralite Ingot); one material restores a quarter of the maximum
durability through native anvil rules. Support native Unbreaking III, Mending,
and applicable Protection variants. The whole suit counts as **one equipped
item**, not four enchantment contributions. Mending works when the player holds
the removed suit and collects XP; pets do not steal their keeper's XP through a
new automatic repair system. Protection adds ordinary mitigation, not extra
health or immunity to special attacks. Do not silently make all player-only
enchantments or armour artifacts apply to animal equipment.

Wear follows accepted damaging hits that the armour can mitigate, through the
native armour damage path. Cancelled attacks, friendly-fire immunity and hits
against an already downed animal spend nothing. There is no wear for walking,
working, swimming or merely standing in the realm. Breaking removes that one
owned stack and its modifiers exactly once. Netherite/Astralite dropped items
resist fire; Astralite equipment also resists explosions, matching lunar materials.

## Replacing the dungeon's genetic buffs

Retain the four/five mixed formations, finite approach pairs, readable cast
warnings, limited summons, richer caches, keys and reward journals from the
current dungeon balance pass. **All newly spawned dungeon Apexes wear real
Astralite barding:** approach guards, wing champions, middle-phase adds, final
flying Prismmaws and the Crowned Apex. Ordinary brood minions do not receive it.

Use the same role-appropriate founder genomes and 25–55-point range as ordinary
wild Great Warren Apexes, with actual wings where needed. Remove the forced
stocky build, 75–85 Vitality/other enhanced point ranges and the separate +50%
champion HP/+15% damage modifiers. Never write equipment bonuses into genes,
pedigrees, breeding forecasts or captured seeds.

Frozen party scaling remains an **encounter rule**, separate from equipment:
wing champions gain 15% health per extra player, up to four players, retaining
the existing 960-health target cap without lowering their solo equipped pool.
It does not grant more armour, damage or genetic points and cannot be changed
by taking the barding off. Approach guards remain unscaled.

Rebase the Crowned Apex's own attributes around its actual equipment. For
example, 3,000 unarmoured solo HP with Astralite ×2 produces the current 6,000;
four-player underlying 7,950 produces 15,900. Base melee 25/35 becomes 40/56;
base beam 20 becomes 32. Base armour 4 plus Astralite 14 produces 18. This avoids
accidentally making it a 12,000-HP solo boss by adding equipment to the existing
already-buffed numbers. Its armour gains real toughness, and the encounter must
be rechecked for effective durability. Do not restore old champion genetic
bonuses to compensate for equipment during balancing.

Guard/champion equipment has a proposed 25% native drop chance, retaining its
actual remaining wear and components. It is earned from the finite fight,
not a renewable inventory on every reload. The Crowned Apex drops its suit
once at death in addition to its existing rewards; this shared physical item
is distinct from personal Sovereign Cores. No fabricated replacement suit on
escape, discard, unloaded actor detection, phase cleanup or repair. Do not add
extra armour rolls to generically summoned brood or overwrite other loot.
Players can therefore recognise, craft and sometimes recover the same equipment
they see on enemies. Armour never appears from breeding or seed extraction.

## Data ownership and implementation boundaries

Prefer the native **BODY** equipment slot with a real ItemStack and ordinary
equipment tracking, save/load, attribute components and drop ownership. Verify
26.2's wolf/horse BODY handling against resolved sources before implementing;
do not assume that a humanoid armour component will apply correctly to this mob.
Use a shared tier profile for crafting tooltips, equipment modifiers and render
selection. Custom modifiers, if required, are transient and derived from the
actual slot; the stack is the authority, not a parallel saved tier integer.
Save only an encounter revision where compatibility requires it.

The client render snapshot contains just the actual tier/required visual
components. Add one fitted armour layer following body/head/ear/leg/tail transforms;
avoid per-frame item or world scans. Five palettes and selectively visible
plate groups can share a mesh. Do not recolour or replace the animal's coat
atlas, Crown layer, wing layer or bundle inventory.

Transactions reserve ownership before callbacks and revalidate the actual held
stack, keeper, reach, animal and equipment slot. Failed interactions spend
nothing; return old gear once, including with a full player inventory. Drop,
death, menu close and reentrant callbacks cannot duplicate or delete it. Native
Travel Basket whole-animal saves retain the exact equipment. Genetic Vacuum
must refuse an armoured animal, as it already refuses bundle carriers: a seed
cannot hold equipment and extraction must not destroy it. Calipers, tissue,
seed outputs and reproduction continue to describe genetics only.

Old animals with no equipment stay unarmoured. Do not grant every Apex free
gear on tick/load, auto-refill caches or convert old sites. Already active saved
champions retain their original genetic profile/legacy modifiers until that
encounter ends; newly created stages use the new system, including in existing
dungeons. A saved old Crowned fight must keep its current effective health and
wounds without suddenly gaining another ×2. Track the new encounter equipment
revision at creation rather than treating an absent stack as missing gear that
needs automatic replacement. Removing or breaking a suit must remain meaningful.

## Implementation and evidence

Build the complete vertical slice together: items/tags/recipes and shared tier
rules; native equipment ownership/stats/wear; owner interactions and vacuum
boundary; fitted visible armour; dungeon replacement; English/shared Spanish;
and concise status/spec updates. Include appropriate companion barding in
Bunnidog testing kits without altering vanilla player kits. No extra animal
equipment screens, artifact trees or unrelated intelligence features in this pass.

Use focused native evidence for real slot/save/basket conservation; tier changes
without healing; ordinary vs armoured damage; bypass/cancellation/breakage;
owner/full-inventory/reentrant swaps; seed/vacuum rejection; upgrade component
preservation; and actual furnished dungeon spawns with unchanged genetics and
Astralite gear. Cover enrage/reload/co-op scaling and finite equipment drops.
Do not prove attributes with source-text assertions or duplicate pure-table tests.

Inspect actual exported armour on normal/slim/chonker builds and all five tiers,
front/side/rear views, ordinary and Apex scale, folded/spread wings, loaded packs,
walking/bite/dive/downed poses, and Blastmaw flashes. A concept render is not
runtime evidence. Natural client combat and Survival acquisition remain player
review after build/model/native checks and the verified direct-JAR delivery.
