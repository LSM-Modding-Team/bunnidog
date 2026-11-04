# Founder roles and the primitive furniture bottleneck

The original primitive placement rule required independent Building even for
the first marked workbench or bedding. Current cognitive weights give an
individual only **0.666%** independent Building potential. In 100,000 sampled
three-founder glades using the actual `WildHearth.configure` genome-roll path,
only **1,940 (1.94%)** contained that architect. **65,234 (65.234%)** contained
an independent craftsperson. No founder weights, genomes or capacity thresholds
were changed for this review.

That made first beds depend on an architect before the family could breed one.
New workbench glades now let a resident with **personally learned Crafting** and
independent Crafting potential assemble only the original nine primitive furniture
plots: table, furnace, four lights and three Nests. Actual materials, walking,
authority, protection and receipts remain. Rooms, walls, roofs, annexes and general
repairs retain Building requirements. A craft-only assembler gains no Building
experience and cannot work around a limiting splice or unknown learning record.

| Potential coverage | Independent-draw model | Actual configured founder-roll sample |
| --- | ---: | ---: |
| Architect present / former first-workshop gate | 1.985% | 1.940% |
| Primitive craft/harvest/haul potential | 64.929% | 65.234% |
| Those roles plus planting/family-care potential | 49.322% | 49.723% |

These are **genetic opportunity figures**, not Survival success rates. They grant
neither learned skill nor stock. The family still needs safe real work, meals,
renewable supplies, Nests, growth and actual seed/birth handling. Three founder
seeds use related Java Random seeds; their measured sample is separate from the
ideal independent-draw calculation. Natural founder selection, terrain eligibility,
losses, discoverability, long-term staffing and client/co-op behavior remain human
review. The initial root cache remains generated infrastructure.

The controlled native reference uses the ordinary helper profile
`(2.25, 2.0, 1.75, 2.0)`, with zero bred points and empty learning. It independently
learns Crafting but is genetically limited at Building. Native checks cover paid
first table/Nest, no room access/architecture experience, missing experience,
callback capacity loss with original parcel recovery, and actual autonomous
first table/tool/claimed bed. A focused observation completed after **8,554 loaded
ticks**; its real growth/rations/movement do not establish natural timing.
The full 315-case native regression passes; a focused follow-up also proves a
nearby qualified foreman cannot bypass Building's physical Bloom gate during
craft-only furniture assembly.

Reproduce after compiling the main Java sources with JDK 25:

```sh
mkdir -p /tmp/bunnidog-founder-audit
javac -cp build/classes/java/main -d /tmp/bunnidog-founder-audit tools/audits/FounderCoverage.java
java -cp /tmp/bunnidog-founder-audit:build/classes/java/main FounderCoverage
```

The tool calls the actual compiled genetics and founder rules; it does not roll
or replace any living world residents. [The JSON snapshot](founder-role-coverage.json)
retains exact model fractions, sample seed/counts and source hashes. This supports
the implementation/evidence sequence in [the intelligence engineering plan](../INTELLIGENCE_ENGINEERING.md).
