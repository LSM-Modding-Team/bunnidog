# Bred infusion potency and reference donor lines

The implementation candidate from [the acquisition-aware rebalance plan](STATS_REBALANCING_PLAN.md)
now includes recovery potency/duration, specialist donors, alternating flask stock
and a targeted Sovereign Lance correction. **Human balance acceptance is pending.**
Breeding, carrier recipes, animal attributes, rewards and encounter numbers retain
their current values after the source/mitigation review. Native mechanics and
conditional refill arithmetic do not measure natural Survival pacing.

## Potency

Only an expressed allele grants an effect. Potency uses the **one frozen rolled
individual** inside the bottle, not its parents' unrolled stats. Let Q be the
saved infusion quality and clamp each stat contribution below at zero.

| Effect | Potency P | Resulting level |
| --- | --- | --- |
| Strength | Q + Vitality | 1 + floor(P / 4) |
| Regeneration | Q + Senses / 2 | 1 + floor(log2(1 + P / 4)) |
| Resistance | Q + Vitality / 16 | 1 + floor(P / 4), existing III limit |
| Absorption | Q + Vitality / 4 | 1 + floor(P / 4) |
| Speed | Q + Swiftness / 4 | 1 + floor(P / 4) |
| Jump Boost | Q + Leap / 4 | 1 + floor(P / 4) |
| Luck | Q + Bloom / 4 | 1 + floor(P / 4) |

Coat Strength and Absorption share those potency associations; they keep their
original statless durations. Fixed utility and drawback amplifiers stay fixed.
Duplicate effects still take their strongest amplifier and longest duration once.
Strength XXX remains an endgame reference, not a ceiling. Scaling effects other
than Resistance retain only the native amplifier representation bound.

Quality remains a separate saved input. Potency never feeds back into litter
size, drift, mutations, feeding memory or carrier quality. A carrier lifts the
infusion quality using the existing maximum rule; it does not reroll the child
or improve its inherited stats. Paid captured seeds preserve their exact
individual at quality zero.

Loyal Regeneration now has **40 seconds base / 240 seconds maximum**:
`min(240, 40 × durationFactor(Q, Senses))`. Plain zero-stat
founders get Regeneration I for forty seconds. The Senses duration contribution
still saturates at fifty points; raising potency past that does not extend
duration again. No other effect gains a new duration bonus or limit.

## Conditional kit references

`genetics/ExpeditionInfusions.java` derives the kit's two families from selected
homozygous zero-point founders. This is a reproducible **conditional stat route**:
it excludes assembling the founders, sex/selection failures, raising replacement
parents, farm layout, actual loaded time and moving stock. No operator kit
represents a player's earned Survival inventory.

The Strength family expresses short ears, woolly build, ember coat and feral
temperament; all other genes use the defaults. These give four Vitality drift
points at Q0 and twelve at Q4. The recovery family expresses bat ears, short
legs and loyal temperament, with other genes default; these give three Senses
points at Q0 and nine at Q4. Fertility stays average and Bloom stays zero.
The references retain their real negative movement drift and allele drawbacks;
the bat-ear recovery bottle also grants native Glowing/Night Vision.

| Reference | Q4 selected parent generations | Subsequent Q0 selected parent generations | Production seed | Rolled relevant stat | Carrier | Main effect |
| --- | ---: | ---: | --- | ---: | --- | --- |
| Late Strength | 1 | 2 | Q0 | Vitality 24 | Golden, Q1 | Strength VII |
| Endgame Strength | 9 | 1 | Q0 | Vitality 116 | Golden, Q1 | Strength XXX |
| Late recovery | 2 | 1 | Q0 | Senses 24 | Golden, Q1 | Regeneration III |
| Endgame recovery | 13 | 0 | Q0 | Senses 120 | Golden, Q1 | Regeneration V |

Each production bottle is one actual deterministic `Infusion.roll` of the plain
seed with a golden carrier. The checks also reproduce the setup using actual
`Seed.hatch` outcomes while retaining the selected genome. Quality 116/24 and
blanket positive stats are no longer injected into newly requested kits.

An endgame Strength bottle lasts 99 seconds, its Resistance III 132 seconds and
Absorption VIII 132 seconds. The separate recovery bottle lasts **88 seconds**,
with native Regeneration V (about 6.67 HP/s when health is missing). The flask
now **alternates Strength/recovery**, starting with Strength: late has 16/16,
endgame 48/48. The totals remain 32/96 drinks, with the original ammunition,
apples, gear and native Shulker contents. Late Strength lasts 73.25 seconds
and recovery 65.1 seconds. New kits need both real drinks; use still commits
32 native ticks per drink. The previous one-to-two stock order would waste
the longer recovery effect and force an extra paid drink each cycle.

Plain carriers are also valid: the developed endgame individual reaches XXX
at Q0, while its defensive Resistance is II. Golden carriers bring that
Resistance to III. Selecting the line and making a consumable seed are separate
investments; filling a flask does not require raising another pup per drink.
At average fertility the established-pair ceiling remains twelve seeds per
loaded hour, before actual constraints. With golden carriers the alternating
endgame flask can refresh both every 88 seconds: 40.91 drinks of **each**
family/hour, requiring at least four Strength pairs plus four recovery pairs
at that ceiling. Its 48 pairs contain 70.4 minutes of ideal overlap, excluding
startup, gaps, losses and human decisions. Plain carriers give 90/80-second
Strength/recovery, requiring 45 of each/hour in that same fixed order. Round
the families separately; their output is not interchangeable. This does not
establish natural production rates or measured expedition duration.

Ordinary and Crown archives remain finite alternatives and possible parent
investments. Their parent records must be rolled before comparing potency.
Descendants recompute seed quality from their actual feed and expressed
starborn bonus, rather than inheriting an archive's saved quality.

## Existing saves and evidence

Existing bottles, captured records and flask entries keep their exact genomes,
stats, quality, components, ordering and ownership. Current rules evaluate those
saved inputs when displaying or drinking. Legitimate high-quality and former
operator bottles can therefore become stronger; they are never downgraded to
the new reference or assigned a fabricated Survival history. No restocking,
regeneration or forced terrain loading accompanies this change. In particular,
old flasks are not reordered and legitimate S240 bottles are not cut to S120.
They use the new potency/duration rules on their saved inputs. Network **50**
requires matching client/server builds so tooltips and server effects agree.

`checkInfusions` covers potency boundaries, separate genes/stats/duration,
duplicate effects, native representation bounds, actual selected hatchlings,
reference rolls, save round trips, exact captures and carrier independence.
Native `infusion_native` checks paid drinking, actual Strength attributes,
Absorption and Regeneration ticks; `infusion_sources` records four real ordinary
and four Crown archive rolls. Existing juicer, vacuum, flask and kit cases retain
their native payment, ordering and stock checks. These are controlled headless
observations, not client feel, ordinary expedition timing or a mixed-role
Survival balance verdict. Three `rebalance_*` scenarios additionally record
conditional supply, native I–VI healing, real paid 32-tick flask use, controlled
mixed-role pressure and distinct absorption/Standard semantics. See
[`audits/rebalance-observations.md`](audits/rebalance-observations.md).

Regeneration does not refill spent absorption. A new paid combat drink does
refill the native 32-HP buffer while its existing effect is active. This stays
native and is an explicit human stress test. Standard power V separately spends
five real Spools for four seconds of Resistance V; native hidden effects then
restore the already-paid, still-running juice Resistance III.

Java 25 build, model/localization and all **294** required native cases pass,
including both infusion and all three rebalance cases. These verify mechanics;
the plan's human trials remain pending. Current release status is in
`../START_HERE.md`.
The exact archive parent/child records and resulting effects are retained in
[`audits/infusion-observations.md`](audits/infusion-observations.md) and its
adjacent JSON; `tools/infusion_audit.py` regenerates them from native logs.
