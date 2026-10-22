# Localization

Minecraft's language setting controls the mod automatically. There is no mod
language option. English uses `en_us.json`; neutral Peruvian Spanish uses one
maintained file, `src/main/resources/assets/bunnidogs/lang/es_pe.json`.

`generateLocalizations` packages that same Spanish translation for all current
Minecraft Spanish choices (`es_ar`, `es_cl`, `es_ec`, `es_es`, `es_mx`, `es_uy`,
`es_ve`) and the other standard Spanish country codes. Those aliases are generated,
not separate authored translations. No custom Peru entry is added to Minecraft's
language picker, since the vanilla game does not currently supply that locale.

The authored catalog contains 2,176 keys in both languages (2,304 packaged after
generated guide previews). Cognitive copies, capacity/learning feedback and
separate mind forecasts share the same regional Spanish aliases. Items, blocks,
advancements, subtitles, feedback, care/combat/workshop screens and the optional
reference are covered, including genetic names, effects and detailed forecasts.
Developer console diagnostics retain their canonical identifiers. Player names,
lineage names, Ear Tag codes, command/filter syntax and saved DNA/stat/record
formats are never translated. Existing player-edited signs are not overwritten;
new warren signs carry translatable Components.

## Wording

Use familiar neutral Spanish: *guayaba*, *guayabo*, *jugo*, *madriguera*, *cría*,
*calibrador*, *arete de identificación*, *libro de pedigrí*, *canasta de viaje*.
The five displayed stats are Vitalidad, Velocidad, Salto, Sentidos and Floración.
Use short labels where the actual screen requires them; detailed hovers retain
the full explanation. Avoid separate Spain/Argentina/Mexico wording.

## Adding text

- Ordinary Minecraft UI/messages use translatable Components. `TextComponents`
  builds shared allele/stat/look descriptions without resolving English on the
  server, so different recipients can use different languages.
- Pure genetics/readout/layout code uses `Texts.t(english, args)` with an English
  fallback. `Texts.key` derives stable `text.bunnidogs.<hash>` catalog IDs from
  that fallback. The client scopes the language resolver around layout work;
  translation occurs before wrapping, fitting or measuring. It never changes
  rule tables, allele keys or save values. Avoid translating static constants
  when a class loads; translate when presenting them.
- `python3 tools/i18n/sync_messages.py --write` collects literal fallbacks and
  adds missing entries. Translate the Spanish additions before delivery. Dynamic
  allele, food, effect and origin labels also need catalog entries; the runtime
  localization check exercises those shared readouts. Calling the script without
  `--write` verifies the literal catalog without changing it.
- Keep placeholder arguments and their types/indices intact. Pure readouts restore
  numeric precision after Minecraft normalizes `%d`/`%f` to `%s` on language load.
  Never format a player-provided string as a translation template.

The build also generates 128 compact guide previews in each language, preserving
full chapter text in the catalog and on native hover tooltips. The native book
shows at most fourteen lines: previews are limited to six estimated body lines,
with room for headings, the hover hint and contents link. Chapters keep all 141
logical pages and their existing navigation. Opening an authored old guide
refreshes its content; renamed-author/title archive copies are preserved.

## Validation

`./gradlew build` includes `checkLocalization`: UTF-8 JSON and duplicate keys,
English/Spanish key and placeholder contracts, shared regional values, formatted
numbers, nested/failed locale cleanup, saved-format stability and representative
Spanish Calipers/Book/Registry/seed/Table/goal/splicing page bounds and overlaps.
The ordinary maintained genetics/layout gates still run too.

For a layout preview:

```
./gradlew checkLocalization -PlocalizationPreview=build/spanish-layout.txt
python3 art/gen_book_preview.py --output art/spanish_book_preview.png < build/spanish-layout.txt
```

`art/spanish_book_preview.png` was inspected with a substitute font; it is not a
Minecraft client/GPU capture. Native GameTests cover actual station/component
and save behavior. Final build/CI evidence is in `START_HERE.md`.

A client pass should switch between English and two Spanish settings, open care,
combat and Table views, inspect an actual litter, and hover a long guide chapter.
The text should switch without changing names or data. Native font rendering and
hover feel still need that client pass; headless checks do not establish them.
