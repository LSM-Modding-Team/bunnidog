# Rebalance candidate: native observations

Implementation candidate; human balance acceptance pending. Donor development and refill figures are conditional arithmetic. Healing uses the actual native effect tick. The 600-world-tick pressure comparison uses the actual endgame kit and paid flask entries, pins positions and schedules native attacks; it is not autonomous AI combat or a measured Survival journey. No client, solo/co-op Crown clear, natural acquisition time or farm throughput is certified.

Reproduce with `./gradlew runGameTestServer '-PgameTestFilter=bunnidogs:rebalance_*'`
and `python3 tools/rebalance_audit.py build/game-test/logs/latest.log`.
The full native run also emits these records. Exact frozen donor inputs and all native
measurements remain in the adjacent JSON. See [the plan](../STATS_REBALANCING_PLAN.md)
for pending human trials and retained acquisition/enemy/reward decisions.

## Conditional selected donor route

Zero-point homozygous founders, retained matching offspring, ordinary average fertility.
These steps exclude gene assembly, sex/selection failures, replacement-parent maturation,
space, feeding and transport. Q4 setup food is not required on later Q0 refill seeds.

| Family | Q4 / subsequent Q0 parent generations | Rolled V / W / L / S / B | Main effect | Duration |
| --- | ---: | --- | --- | ---: |
| Late Strength | 1 / 2 | 24 / 0 / -3 / -6 / 0 | strength 7 | 73.25 s |
| Endgame Strength | 9 / 1 | 116 / 0 / -2 / -4 / 0 | strength 30 | 99 s |
| Late recovery | 2 / 1 | 0 / -2 / -2 / 24 / 0 | regeneration 3 | 65.1 s |
| Endgame recovery | 13 / 0 | 0 / -1 / -1 / 120 / 0 | regeneration 5 | 88 s |

## Conditional continuous refill

One real seed and carrier per drink; four plus four ideal pairs means sixteen adults.
Pair ceilings exclude real walking, crowding, food, selection and station constraints.
Independent effect refresh and the fixed alternating flask have different consumption.

| Carrier | Strength / recovery duration | Independently selected Strength / recovery per hour | Alternating drinks of each family per hour | Ideal Strength + recovery pairs for that order | 96-drink ideal overlap |
| --- | ---: | ---: | ---: | --- | ---: |
| plain | 90 / 80 s | 40.00 / 45.00 | 45.00 | 4 + 4 | 64 min |
| golden | 99 / 88 s | 36.36 / 40.91 | 40.91 | 4 + 4 | 70.4 min |

Golden carriers also bring the Strength reference's Resistance from II to III.
New kits start with Strength and alternate 16/16 late, 48/48 endgame; old flasks keep
their exact order. Ideal overlap excludes startup, interruptions, losses and late refreshes.

One refill seed needs two plain parent feedings; its carrier needs another fruit.
The current golden-carrier recipe additionally uses 8 Gold Ingots per drink.
Carriers can use the ordinary shapeless recipe or a five-second press; infusion itself
occupies five machine seconds per drink. These are demand figures, not acquisition rates.

| Carrier | Total drinks/hour | Parent + carrier guavas/hour | Gold Ingots/hour | Infusion machine seconds/hour | Extra seconds if also pressing carriers |
| --- | ---: | ---: | ---: | ---: | ---: |
| plain | 90.00 | 270.00 | 0.00 | 450.00 | 450.00 |
| golden | 81.82 | 245.45 | 654.55 | 409.09 | 409.09 |

A fresh 96-drink golden flask therefore needs 288 fruit and 768 Gold while producing
its 96 seeds/carriers, before other supplies or losses. Glass returns on drinking
(or drops when inventory is full); recovered bottles can be reused, lost ones cannot.
Parallel stations reduce machine occupancy, not seed/fruit/gold demand.

## Native healing and controlled pressure

| Regeneration level | HP restored by 600 native effect ticks with ample missing HP | HP/s |
| ---: | ---: | ---: |
| 1 | 12 | 0.4 |
| 2 | 24 | 0.8 |
| 3 | 50 | 1.667 |
| 4 | 100 | 3.333 |
| 5 | 200 | 6.667 |
| 6 | 600 | 20 |

The separate pressure case keeps full enchanted Sovereign gear, apples, ammo, two real
Shulkers and normal emergency Totems. The embedded mock connection needs an explicit
once-per-world-tick `ServerPlayer.doTick()` body driver for food/effects/drinking.
Two real 32-tick drinks spend 96 → 95 → 94 original entries; the midpoint still has
96 entries and active item use. Only active Regeneration is overridden to III/IV/V/VI.
Each profile receives two Astralite Brutes' staggered native melee attempts, periodic
native-tagged Prism damage and the Lifebinder's actual tether/debt lifecycle. Natural
food healing is enabled. Positions are pinned; native damage immunity/mitigation remain.
No player attacks, apple consumption, Standard use, emergency re-equipping or retreat
are scripted. This isolates relative pressure; it is not a room or boss clear.

| Regen | Survived window | Lowest HP observed | HP / max HP at end | Accepted capped healing HP | Health damage HP | Accepted damage events | Normal Totems used | Debt hearts left | Drinks left |
| ---: | --- | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: |
| 3 | yes | 10.17 | 11.17 / 42 | 40.57 | 73.40 | 67 | 0 | 1 | 94 |
| 4 | yes | 36.33 | 38.81 / 42 | 68.46 | 73.40 | 67 | 0 | 1 | 94 |
| 5 | yes | 38.04 | 42.00 / 42 | 71.88 | 73.40 | 67 | 0 | 1 | 94 |
| 6 | yes | 38.24 | 42.00 / 42 | 71.88 | 73.40 | 67 | 0 | 1 | 94 |

Reported healing is capped at missing HP at the native event; unused potential is not
counted as useful recovery. Event totals are observations, not nominal summed enemy DPS.
VI's 20 HP/s potential is three times V's; a similar result under this pressure does not
establish similar power under a burstier real encounter. Human I–VI comparison stays open.

## Distinct defensive mechanisms

- A freshly paid combat drink refills **32 HP** of native absorption, including an active-effect refresh.
- Regeneration heals ordinary missing HP and does not refill that buffer. Six separate
  native-tagged beam hits spend it and reach health; armour bypass still respects the
  actual Resistance/enchantment tags.
- Standard V spends **5 real Spools** for **4 seconds** of Resistance V.
  Native hidden effects then restore the already-paid, still-running juice Resistance III.
- Repeated paid absorption refresh is retained, with its real 32-tick drinking commitment;
  deliberate spam, high-quality archive recovery and overlapping Standards need human tests.

## Lance cycle comparison

| Weapon | Mode-I damage / cell | Charge + recovery ticks | Range | Ideal repeated raw DPS |
| --- | ---: | ---: | ---: | ---: |
| PRISM | 64 | 32 + 30 | 32 | 20.65 |
| SOVEREIGN | 56 | 24 + 24 | 48 | 23.33 |

Sovereign I–V deal 56/112/168/224/280 and spend 1–5 cells/wear. The existing
native equipment case checks all five real target hits/payments. Prism keeps better
damage per cell; Sovereign has the speed/range advantage. Line targets, charge
interruptions, real ammo acquisition and actual boss damage remain human acceptance.
