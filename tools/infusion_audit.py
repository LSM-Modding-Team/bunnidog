#!/usr/bin/env python3
"""Extract controlled native archive observations; this does not simulate Survival acquisition."""
import argparse
import json
from pathlib import Path


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("log", type=Path, help="GameTest log containing INFUSION_SOURCE_AUDIT")
    parser.add_argument("--output", type=Path, default=Path("docs/audits/infusion-observations"))
    args = parser.parse_args()
    marker = "INFUSION_SOURCE_AUDIT "
    records = [json.loads(line.split(marker, 1)[1]) for line in args.log.read_text().splitlines() if marker in line]
    if not records:
        parser.error("No native infusion source observations found")
    samples = records[-1]
    scope = ("Four ordinary and four Crown archive generator samples, each rolled once. "
             "Ordinary archive generation also depends on its native fixture position; exact saved parent/child "
             "records below are authoritative. These are finite controlled observations, not population frequencies, "
             "a measured Survival route or renewable inherited archive quality.")
    args.output.parent.mkdir(parents=True, exist_ok=True)
    args.output.with_suffix(".json").write_text(json.dumps({"scope": scope, "samples": samples}, indent=2) + "\n")
    lines = ["# Native infusion archive observations", "", scope, "",
             "Reproduce with `./gradlew runGameTestServer '-PgameTestFilter=bunnidogs:infusion_sources'`",
             "and `python3 tools/infusion_audit.py build/game-test/logs/latest.log`.", "",
             "| Source | Sample / roll seed | Infusion Q | Rolled V / W / L / S / B | Strength | Regeneration | Plain descendant Q |",
             "| --- | ---: | ---: | --- | ---: | ---: | ---: |"]
    for row in samples:
        individual, quality = row["rolled_individual"].rsplit("|", 1)
        points = individual.rsplit("/", 1)[1].replace(",", " / ")
        effects = {e["id"]: e["level"] for e in row["effects"]}
        lines.append(f'| {row["source"]} | {row["sample"]} | {float(quality):g} | {points} | '
                     f'{effects.get("minecraft:strength", "—")} | {effects.get("minecraft:regeneration", "—")} | '
                     f'{row["plain_descendant_quality"]:.4g} |')
    lines += ["", "A dash means the rolled genome does not express that effect. High parent stats or quality",
              "cannot supply an absent allele. A starborn descendant may recompute a small real Bloom bonus;",
              "it does not copy the archive's original quality. Full exact records and other effects are in",
              "the adjacent JSON. No conclusion about all possible archive rolls follows from these eight.", ""]
    args.output.with_suffix(".md").write_text("\n".join(lines))


if __name__ == "__main__":
    main()
