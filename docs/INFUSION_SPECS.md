# Bred infusion potency and reference donor lines

The first stat/kit pass from [the acquisition-aware rebalance plan](STATS_REBALANCING_PLAN.md)
now ships. These are initial coefficients, not measured Survival balance. Duration,
breeding, carrier recipes, animal attributes and encounter numbers are unchanged.
The remaining recovery, production, encounter and reward comparisons in that plan
remain open.

## Potency

Only an expressed allele grants an effect. Potency uses the **one frozen rolled
individual** inside the bottle, not its parents' unrolled stats. Let Q be the
saved infusion quality and clamp each stat contribution below at zero.

| Effect | Potency P | Resulting level |
| --- | --- | --- |
| Strength | Q + Vitality | 1 + floor(P / 4) |
| Regeneration | Q + Senses / 4 | 1 + floor(log2(1 + P / 4)) |
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

## Conditional kit references

`genetics/ExpeditionInfusions.java` derives the kit's two families from selected
homozygous zero-point founders. This is a reproducible **conditional stat route**:
it excludes assembling the founders, sex/selection failures, raising replacement
parents, farm layout, actual loaded time and moving stock. No operator kit
represents a player's earned Survival inventory.

The Strength family expresses short ears, woolly build, ember coat and feral
temperament; all other genes use the defaults. These give four Vitality drift
points at Q0 and twelve at Q4. The recovery family expresses long ears, short
legs and loyal temperament, with other genes default; these give two Senses
points at Q0 and six at Q4. Fertility stays average and Bloom stays zero.

| Reference | Q4 selected parent generations | Subsequent Q0 selected parent generations | Production seed | Rolled relevant stat | Carrier | Main effect |
| --- | ---: | ---: | --- | ---: | --- | --- |
| Late Strength | 1 | 2 | Q0 | Vitality 24 | Golden, Q1 | Strength VII |
| Endgame Strength | 9 | 1 | Q0 | Vitality 116 | Golden, Q1 | Strength XXX |
| Late recovery | 9 | 0 | Q0 | Senses 56 | Golden, Q1 | Regeneration III |
| Endgame recovery | 39 | 2 | Q0 | Senses 240 | Golden, Q1 | Regeneration V |

Each production bottle is one actual deterministic `Infusion.roll` of the plain
seed with a golden carrier. The checks also reproduce the setup using actual
`Seed.hatch` outcomes while retaining the selected genome. Quality 116/24 and
blanket positive stats are no longer injected into newly requested kits.

An endgame Strength bottle lasts 99 seconds, its Resistance III 132 seconds and
Absorption VIII 132 seconds. The separate recovery bottle lasts 44 seconds,
with native Regeneration V (about 6.67 HP/s when health is missing). The flask
retains one Strength drink per two recovery drinks, all 32/96 drinks, original
stack order, ammunition, apples, gear and native Shulker contents. A late
Strength drink lasts about 73 seconds, so the same stock proportions are not a
promise of continuous overlapping uptime.

Plain carriers are also valid: the developed endgame individual reaches XXX
at Q0, while its defensive Resistance is II. Golden carriers bring that
Resistance to III. Selecting the line and making a consumable seed are separate
investments; filling a flask does not require raising another pup per drink.
At average fertility the established-pair ceiling remains twelve seeds per
loaded hour, before actual constraints. This does not establish natural
production rates or sustained recovery uptime.

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
regeneration or forced terrain loading accompanies this change. Network **49**
requires matching client/server builds so tooltips and server effects agree.

`checkInfusions` covers potency boundaries, separate genes/stats/duration,
duplicate effects, native representation bounds, actual selected hatchlings,
reference rolls, save round trips, exact captures and carrier independence.
Native `infusion_native` checks paid drinking, actual Strength attributes,
Absorption and Regeneration ticks; `infusion_sources` records four real ordinary
and four Crown archive rolls. Existing juicer, vacuum, flask and kit cases retain
their native payment, ordering and stock checks. These are controlled headless
observations, not client feel, ordinary expedition timing or a mixed-role
Survival balance verdict.

Java 25 build, model/localization gates and both focused native cases passed.
The exact archive parent/child records and resulting effects are retained in
[`audits/infusion-observations.md`](audits/infusion-observations.md) and its
adjacent JSON; `tools/infusion_audit.py` regenerates them from native logs.
