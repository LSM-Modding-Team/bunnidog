#!/usr/bin/env python3
"""Extract controlled native rebalance observations; human Survival acceptance stays pending."""
import argparse
import json
from pathlib import Path


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("log", type=Path, help="Passing GameTest log with all three REBALANCE_*_AUDIT records")
    parser.add_argument("--output", type=Path, default=Path("docs/audits/rebalance-observations"))
    args = parser.parse_args()
    records = {}
    for line in args.log.read_text().splitlines():
        for name in ("supply", "pressure", "defence"):
            marker = f"REBALANCE_{name.upper()}_AUDIT "
            if marker in line:
                records[name] = json.loads(line.split(marker, 1)[1])
    if set(records) != {"supply", "pressure", "defence"}:
        parser.error("Need supply, pressure and defence records from the same native run")
    if any(row.get("human_acceptance") != "pending" for row in records.values()):
        parser.error("Native observations cannot certify human acceptance")
    scope = (
        "Implementation candidate; human balance acceptance pending. Donor development and refill figures "
        "are conditional arithmetic. Healing uses the actual native effect tick. The 600-world-tick pressure "
        "comparison uses the actual endgame kit and paid flask entries, pins positions and schedules native "
        "attacks; it is not autonomous AI combat or a measured Survival journey. No client, solo/co-op Crown "
        "clear, natural acquisition time or farm throughput is certified."
    )
    root = Path(__file__).resolve().parents[1]
    golden_recipe = json.loads((root / "src/main/resources/data/bunnidogs/recipe/golden_guava.json").read_text())
    gold_per_carrier = sum(symbol in golden_recipe["key"] and golden_recipe["key"][symbol] == "minecraft:gold_ingot"
                           for row in golden_recipe["pattern"] for symbol in row)
    refill = []
    for row in records["supply"]["conditional_continuous_uptime"]:
        drinks = row["alternating_strength_drinks_per_hour"] + row["alternating_recovery_drinks_per_hour"]
        refill.append({"carrier": row["carrier"], "total_drinks_per_hour": drinks,
                       "parent_and_carrier_guavas_per_hour": 3 * drinks,
                       "gold_ingots_per_hour": gold_per_carrier * drinks if row["carrier"] == "golden" else 0,
                       "infusion_station_seconds_per_hour": 5 * drinks,
                       "additional_seconds_if_carriers_also_pressed": 5 * drinks})
    recipe_account = {"basis": "Current golden carrier recipe; conditional two plain parent feedings and one carrier fruit per drink. Existing shapeless carrier crafting bypasses pressing; infusion remains five machine seconds. No waste or replacement parents included.",
                      "gold_ingots_per_golden_carrier": gold_per_carrier, "refill": refill}
    args.output.parent.mkdir(parents=True, exist_ok=True)
    args.output.with_suffix(".json").write_text(json.dumps({"scope": scope, **records, "conditional_recipe_account": recipe_account}, indent=2) + "\n")
    supply, pressure, defence = (records[key] for key in ("supply", "pressure", "defence"))
    lines = [
        "# Rebalance candidate: native observations", "", scope, "",
        "Reproduce with `./gradlew runGameTestServer '-PgameTestFilter=bunnidogs:rebalance_*'`",
        "and `python3 tools/rebalance_audit.py build/game-test/logs/latest.log`.",
        "The full native run also emits these records. Exact frozen donor inputs and all native",
        "measurements remain in the adjacent JSON. See [the plan](../STATS_REBALANCING_PLAN.md)",
        "for pending human trials and retained acquisition/enemy/reward decisions.", "",
        "## Conditional selected donor route", "",
        "Zero-point homozygous founders, retained matching offspring, ordinary average fertility.",
        "These steps exclude gene assembly, sex/selection failures, replacement-parent maturation,",
        "space, feeding and transport. Q4 setup food is not required on later Q0 refill seeds.", "",
        "| Family | Q4 / subsequent Q0 parent generations | Rolled V / W / L / S / B | Main effect | Duration |",
        "| --- | ---: | --- | --- | ---: |",
    ]
    names = ("Late Strength", "Endgame Strength", "Late recovery", "Endgame recovery")
    for name, row in zip(names, supply["conditional_donors"], strict=True):
        stats = row["frozen_bottle"].rsplit("/", 1)[1].split("|", 1)[0].replace(",", " / ")
        effect = next(e for e in row["effects"] if e["effect"] ==
                      ("minecraft:strength" if "Strength" in name else "minecraft:regeneration"))
        lines.append(f'| {name} | {row["rich_generations"]} / {row["plain_generations"]} | {stats} | '
                     f'{effect["effect"].split(":")[1]} {effect["level"]} | {effect["seconds"]:g} s |')
    lines += [
        "", "## Conditional continuous refill", "",
        "One real seed and carrier per drink; four plus four ideal pairs means sixteen adults.",
        "Pair ceilings exclude real walking, crowding, food, selection and station constraints.",
        "Independent effect refresh and the fixed alternating flask have different consumption.", "",
        "| Carrier | Strength / recovery duration | Independently selected Strength / recovery per hour | Alternating drinks of each family per hour | Ideal Strength + recovery pairs for that order | 96-drink ideal overlap |",
        "| --- | ---: | ---: | ---: | --- | ---: |",
    ]
    for row in supply["conditional_continuous_uptime"]:
        lines.append(f'| {row["carrier"]} | {row["strength_seconds"]:g} / {row["recovery_seconds"]:g} s | '
                     f'{row["strength_drinks_per_hour_at_full_uptime"]:.2f} / {row["recovery_drinks_per_hour_at_full_uptime"]:.2f} | '
                     f'{row["alternating_strength_drinks_per_hour"]:.2f} | '
                     f'{row["alternating_strength_pairs"]} + {row["alternating_recovery_pairs"]} | '
                     f'{row["endgame_ideal_overlap_minutes"]:g} min |')
    lines += [
        "", "Golden carriers also bring the Strength reference's Resistance from II to III.",
        "New kits start with Strength and alternate 16/16 late, 48/48 endgame; old flasks keep",
        "their exact order. Ideal overlap excludes startup, interruptions, losses and late refreshes.", "",
        "One refill seed needs two plain parent feedings; its carrier needs another fruit.",
        f'The current golden-carrier recipe additionally uses {gold_per_carrier} Gold Ingots per drink.',
        "Carriers can use the ordinary shapeless recipe or a five-second press; infusion itself",
        "occupies five machine seconds per drink. These are demand figures, not acquisition rates.", "",
        "| Carrier | Total drinks/hour | Parent + carrier guavas/hour | Gold Ingots/hour | Infusion machine seconds/hour | Extra seconds if also pressing carriers |",
        "| --- | ---: | ---: | ---: | ---: | ---: |",
    ]
    for row in refill:
        lines.append(f'| {row["carrier"]} | {row["total_drinks_per_hour"]:.2f} | '
                     f'{row["parent_and_carrier_guavas_per_hour"]:.2f} | {row["gold_ingots_per_hour"]:.2f} | '
                     f'{row["infusion_station_seconds_per_hour"]:.2f} | {row["additional_seconds_if_carriers_also_pressed"]:.2f} |')
    lines += [
        "", "A fresh 96-drink golden flask therefore needs 288 fruit and 768 Gold while producing",
        "its 96 seeds/carriers, before other supplies or losses. Glass returns on drinking",
        "(or drops when inventory is full); recovered bottles can be reused, lost ones cannot.",
        "Parallel stations reduce machine occupancy, not seed/fruit/gold demand.", "",
        "## Native healing and controlled pressure", "",
        "| Regeneration level | HP restored by 600 native effect ticks with ample missing HP | HP/s |",
        "| ---: | ---: | ---: |",
    ]
    for row in supply["native_regeneration"]:
        lines.append(f'| {row["level"]} | {row["native_hp_in_30_seconds"]:g} | {row["native_hp_per_second"]:.4g} |')
    lines += [
        "", "The separate pressure case keeps full enchanted Sovereign gear, apples, ammo, two real",
        "Shulkers and normal emergency Totems. The embedded mock connection needs an explicit",
        "once-per-world-tick `ServerPlayer.doTick()` body driver for food/effects/drinking.",
        "Two real 32-tick drinks spend 96 → 95 → 94 original entries; the midpoint still has",
        "96 entries and active item use. Only active Regeneration is overridden to III/IV/V/VI.",
        "Each profile receives two Astralite Brutes' staggered native melee attempts, periodic",
        "native-tagged Prism damage and the Lifebinder's actual tether/debt lifecycle. Natural",
        "food healing is enabled. Positions are pinned; native damage immunity/mitigation remain.",
        "No player attacks, apple consumption, Standard use, emergency re-equipping or retreat",
        "are scripted. This isolates relative pressure; it is not a room or boss clear.", "",
        "| Regen | Survived window | Lowest HP observed | HP / max HP at end | Accepted capped healing HP | Health damage HP | Accepted damage events | Normal Totems used | Debt hearts left | Drinks left |",
        "| ---: | --- | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: |",
    ]
    for row in pressure["profiles"]:
        lines.append(f'| {row["regeneration_level"]} | {"yes" if row["alive"] else "no"} | '
                     f'{row["minimum_health"]:.2f} | {row["health_left"]:.2f} / {row["max_health_left"]:g} | '
                     f'{row["native_accepted_healing_hp"]:.2f} | {row["native_health_damage_hp"]:.2f} | '
                     f'{row["native_damage_events"]} | {row["normal_totems_used"]} | '
                     f'{row["heart_debt_left"]} | {row["flask_drinks_left"]} |')
    lines += [
        "", "Reported healing is capped at missing HP at the native event; unused potential is not",
        "counted as useful recovery. Event totals are observations, not nominal summed enemy DPS.",
        "VI's 20 HP/s potential is three times V's; a similar result under this pressure does not",
        "establish similar power under a burstier real encounter. Human I–VI comparison stays open.", "",
        "## Distinct defensive mechanisms", "",
        f'- A freshly paid combat drink refills **{defence["absorption_refill_per_paid_combat_bottle_hp"]} HP** of native absorption, including an active-effect refresh.',
        "- Regeneration heals ordinary missing HP and does not refill that buffer. Six separate",
        "  native-tagged beam hits spend it and reach health; armour bypass still respects the",
        "  actual Resistance/enchantment tags.",
        f'- Standard V spends **{defence["standard_power_five_spools"]} real Spools** for **{defence["standard_resistance_v_ticks"] / 20:g} seconds** of Resistance V.',
        "  Native hidden effects then restore the already-paid, still-running juice Resistance III.",
        "- Repeated paid absorption refresh is retained, with its real 32-tick drinking commitment;",
        "  deliberate spam, high-quality archive recovery and overlapping Standards need human tests.", "",
        "## Lance cycle comparison", "",
        "| Weapon | Mode-I damage / cell | Charge + recovery ticks | Range | Ideal repeated raw DPS |",
        "| --- | ---: | ---: | ---: | ---: |",
    ]
    for row in supply["lances"]:
        lines.append(f'| {row["weapon"]} | {row["damage_per_cell"]} | '
                     f'{row["charge_ticks"]} + {row["recovery_ticks"]} | {row["range"]} | '
                     f'{row["mode_one_raw_damage_per_second"]:.2f} |')
    lines += [
        "", "Sovereign I–V deal 56/112/168/224/280 and spend 1–5 cells/wear. The existing",
        "native equipment case checks all five real target hits/payments. Prism keeps better",
        "damage per cell; Sovereign has the speed/range advantage. Line targets, charge",
        "interruptions, real ammo acquisition and actual boss damage remain human acceptance.", "",
    ]
    args.output.with_suffix(".md").write_text("\n".join(lines))


if __name__ == "__main__":
    main()
