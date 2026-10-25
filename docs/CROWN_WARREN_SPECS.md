# Crown Warren and Sovereign rewards

Implemented 2026-10-25, continuing the accepted AX7 plan. Ordinary Apex combat
numbers remain those accepted in `APEX_SPECS.md`. This is a separate finale;
headless evidence does not establish its natural Survival balance or appearance.

## Finding and entering

Crown Warrens generate in **new Great Warren terrain**, through their own native
random-spread structure set: spacing 64 chunks, separation 24. A bounded nine-point
terrain fit rejects submerged, excessively uneven or ceiling-clipped sites.
`/locate structure bunnidogs:crown_warren` uses the normal structure registry.
Existing generated terrain, homes and stock are not retroactively rebuilt.

The layout occupies at most 181 × 181 blocks. Three halls branch around an
85-block central arena with a 31-block roof. A sheltered preparation court has
crafting/smithing, an Ender Chest and a real Warren Ward. Outer routes connect
both side halls to staging; the northern hall connects through the arena. The
existing paired Warren Anchor remains the expedition's return route.

| Wing | Terrain | Two finite waves |
| --- | --- | --- |
| Glass Gallery | Calcite, quartz balconies/stairs, amethyst cover, glass roof | Prismmaw + Brute; then two Prismmaws, one flying |
| Brood Vaults | Moss, low divided pens, open center and cover | Broodcaller; then Broodcaller + Brute |
| Heart Furnace | Basalt, blackstone forges, visible magma channels | Lifebinder + Brute; then Blastmaw |

A normal click on a hall's physical seal starts it, including while holding a
weapon. Existing enemy fight values apply. Confirmed deaths advance waves;
unloading, discarding or losing an actor never counts as victory. Finished halls
pay a generic supply chest and provide their own site-bound keystone. Lost keys
can be replaced from a cleared seal without refilling its one-time cache.

The central seal accepts **any two different matching-site keystones**. Its
three colored sockets and physical Crown Gate preserve opening in saves. Shown
offhand keys/sigils work alongside an ordinary main-hand weapon. Keys from another
site or repeated identical keys remain untouched. The optional third key earns
one frozen genetic archive; it is not a condition of starting the boss.

No tutorial menu, ambient instructions, mandatory guide chapter or automatic
stat panel is added. Seals use concise feedback only when interacted with.
Critical gates/seals resist mining, blasts, terrain attacks and piston movement.
Other player-made shortcuts remain useful; opening a route does not skip the
central seal's actual encounter conditions.

## Crowned Apex

A distinct entity (`bunnidogs:crowned_apex`) carries five colored crests and
resonant back organs on the existing animal sculpt. Its independent crown layer
follows actual body/head proportions; wings unfold in the aerial final phase.
The boss has its own native boss bar and remains measurable with Calipers.

Solo health is **3,000**, with 16 armor and 30 raw melee damage. The engagement
roster freezes at the starter plus up to three nearby Survival players. Health
scales by `3000 × (1 + .55 × (players − 1))`, up to 7,950 for four players.
Visitors arriving later cannot inflate health or acquire an idle core entitlement.

A synchronized, boss-only `crown_health` attribute supplies a real native health
pool above vanilla's 1,024 bound. Native damage, healing, setHealth, saves and the
boss bar all use that pool. Global attribute bounds stay intact. Phase changes
preserve damage rather than replacing health bars or imposing per-hit caps.

| Health band | Pattern and openings |
| --- | --- |
| Above two thirds — The Hunt | Alternates committed pounces and aimed beams; ordinary bites/chasing during recovery |
| Middle third — The Living Crown | One finite pack: Lifebinder, directed Prismmaw, three elite brood. Visible vitality links; boss remains directly damageable |
| Last third — The Broken Crown | Continuous flight between warned dives and paired large detonations, with recovery; 44 raw melee damage |

Pounces warn their fixed landing point for 28 ticks, then follow a real colliding,
terrain-clearing arc. Beams warn for 34 ticks and pulse six times over 60 ticks,
with up to 64-block reach (clipped to encounter bounds) and 24 raw piercing damage
per accepted hit. The aimed
line stays committed while the boss casts. Native protection/damage hooks remain.

The Living Crown summons its pack once. Its support link has 120 HP of finite
healing income, spent only on actual accepted healing; the ordinary Lifebinder's
small saved reserve remains finite too. Killing support stops its link. A single
directed Prismmaw can issue a warned, fixed-direction shot during boss recovery;
it does not run an independent full attack rotation. Brood retains normal elite
rules and its finite lifetime. Phase transitions and victory clean the old pack.

Final flight orbits around the chamber at roughly 17 blocks above its floor.
Aerial casts retain no-gravity and stop stale ground navigation. The boss is
fall-damage immune. Detonations mark two separated 19-block circles for about
three seconds, then use native power-9 explosions, leaving a central safe lane.
Family immunity, griefing rules, cancellation and protected Rootstone apply.
The director never adds a fresh hook/stun to a point-blank blast. Player flight,
ranged attacks, building and combat companions retain their normal uses.

## Saves, retreat and rewards

Each controller stores its site/run identity, confirmed enemy UUIDs, sockets,
completion, frozen contribution roster and pending rewards. Only an explicit
start loads its finite known encounter neighborhood. Short nonpersistent native
leases retain that already-loaded footprint and its controller at low render
distance. Loading and simulation are separate: simulation cannot generate a new
outer ring. On reload, a saved nearby actor can recover its one known controller
chunk for up to three seconds; it never searches distant chunks. Reset/victory
release the leases, and unreached leases expire automatically. Rejected/changed native spawns remove the unpaid pack and spend nothing.
A rematch consumes one **actually offered Crown Sigil**, after successful native
spawn/payment revalidation. Its recipe uses existing realm parts, not the five
original legendary offerings. Saved active rematches remain already paid.

Ten seconds without a living Survival player in the encounter, a missing tracked
actor, or Peaceful mode resets an unfinished fight. Actors outside their local
bounds are removed. Cleanup touches encounter actors and preserves player items,
blocks and earned reward ledgers. Old active actors cannot join a later run.

Each frozen participant needs 30 contribution points. Actual damage to the boss
or bound adds counts; owned companions credit their keeper. Accepted shield
blocking of an encounter attack counts at half value. Concord Standard healing
of another actual nearby engaged ally counts its accepted healing. Receiving
healing, standing nearby or merely entering later grants no credit.

The native post-damage callback occurs **after** native death. Victory therefore
waits for that callback (or the next controller tick for direct deaths), so the
accepted killing hit participates before rewards resolve. Cancelled deaths do not
advance the encounter. Normal mob-drop rules govern kill rewards.

A qualifying victory grants:

- One saved **Sovereign Core entitlement per qualifying player**, claimed at the
  seal. Offline/dead players can return to claim. Full inventory plus cancelled
  native item delivery preserves the entitlement; repeated claims/reloads do not
  duplicate it.
- A displayable Crown trophy carrying the actual dungeon and victory identity.
  Failed trophy delivery remains independently claimable after the core.
- A generic vault: 24–32 diamond blocks, 8–12 Netherite blocks, 32–48 enchanted
  golden apples, 4–6 Totems, and large crafting/expedition stocks. Unique role
  ingredients instead drop from the actual Crowned Apex: 16–24 of each of the
  five role parts, Marrow and aerial membranes. The boss pays 10,000 XP.
- A separately frozen archive: ordinary heritable winged End/Cave parents with
  70–100 stat points, quality 36–47 seed, matching reusable tissues, Mutagen and
  Starlit Dust. These breed ordinary bunnidogs through existing genetics; they do
  not grant the boss's separate health pool, an instant profession or a civilization.

Wing caches contain generic wealth and bulk reagents. Temporary dungeon enemies
and boss adds do not provide ordinary ritual Crests, role parts or XP. Paid genetic
vacuum capture retains the user's existing exception for brood-to-seed capture.

Reward chests use empty physical plinths only; existing containers are never
replaced/refilled. Blocked vault/archive deliveries remain saved until space is
available. Archives store their exact whole seed/sample items once; they never
combine deferred loot tables with inventory contents or reroll on inspection.

## Sovereign choices

Each smithing upgrade consumes one Core and the player's actual previous item.
Native smithing preserves names, wear, enchantments and other explicit components.

| Upgrade | Actual benefit |
| --- | --- |
| Heartbound piece → Sovereign armor | +3 equipped hearts per piece; retains independent Heartbound 60/45/30/15-second heart recovery. Mixed old/new sets work |
| Prism Lance → Sovereign Lance | 24-tick charge, 48-block piercing reach, 112 raw damage, 24-tick cooldown; one Prism Cell and two wear per shot |
| Armored Elytra → Sovereign Elytra | Native glider, 10 armor/4 toughness, durability 1,296; same real cells, three wear and 30-tick burst cooldown |
| Core + parts → Concord Standard | 30-tick charge; one Heart Spool/wear per cast, heals six HP and grants four-second Resistance I to up to 16 nearby allies; five-second cooldown |

The Standard supports own living companions, native teammates and engaged
Crown-party players. It does not replace downed-animal revival or grant free
healing credit for self-healing. Interrupted charges spend nothing. Native fuel,
cooldown, permission and whole-item checks remain authoritative.

All new text is English plus the shared neutral Peruvian Spanish source.
Protocol **30** requires matching client/server content. Original ordinary Apex
values and old gear IDs/saves remain compatible.

## Evidence and remaining review

The full native suite covers finite hall waves, actual key/hand/gate behavior,
entity/controller saves, truthful high health, phase/pack cleanup, damaging beam
warnings, sustained native aerial physics, abandoned-fight cleanup, frozen
multiplayer scaling, killing-hit contribution, cancelled spawns, paid rematches,
blocked/retried core and trophy delivery, smithing/equipment/fuel/support behavior,
and real detached `WorldGenRegion`/`ProtoChunk` placement/replay. Shipped realm
terrain admits an actual random-spread dungeon candidate in the native fixture.
The native structure cells also produce the layout preview.

Model checks exercise actual crown geometry, UV bounds, head/body transforms and
visibility reset. Generated crown, gear and layout previews are inspected.
These checks do not establish graphical lighting/animation, natural chunk-order
performance, full solo/co-op fight pacing or the new economy. Those need player
review. Additional rooms, regrowing cover, other Sovereign branches and the two
remaining proposed realm regions are future extensions, not prerequisites hidden
behind the implemented finale. Intelligence/civilization remains the next major
unfinished roadmap after this Apex expedition stage and reported fixes.
