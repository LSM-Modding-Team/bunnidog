"""Purebred Registry: teal book, gilded spine and paired-allele seal, at native 16px scale."""
import json
from pathlib import Path
from PIL import Image, ImageDraw
from gen_syringe import write

ROOT = Path(__file__).resolve().parents[1]
RES = ROOT / 'src/main/resources'


def main():
    img = Image.new('RGBA', (16, 16)); d = ImageDraw.Draw(img)
    edge, cover, light, gold = (30, 48, 50), (37, 112, 103), (59, 150, 132), (224, 180, 77)
    d.polygon([(1, 3), (11, 1), (14, 3), (14, 13), (4, 15), (1, 13)], fill=edge)
    d.polygon([(3, 4), (13, 2), (13, 12), (3, 14)], fill=cover)
    d.line((4, 4, 12, 2), fill=light)
    d.line((2, 4, 2, 12), fill=gold)
    d.line((4, 14, 13, 12), fill=(229, 216, 180))
    d.line((4, 12, 4, 5), fill=gold)
    d.ellipse((6, 5, 12, 11), fill=edge)
    d.ellipse((7, 5, 12, 10), fill=gold)
    d.line((8, 6, 8, 9), fill=(255, 240, 186))
    d.line((10, 6, 10, 9), fill=(255, 240, 186))
    img.save(RES / 'assets/bunnidogs/textures/item/purebred_registry.png')
    preview = Image.new('RGBA', (256, 256), (45, 43, 42, 255))
    preview.alpha_composite(img.resize((256, 256), Image.Resampling.NEAREST))
    preview.save(ROOT / 'art/registry_preview.png')
    write('assets/bunnidogs/items/purebred_registry.json', {'model': {'type': 'minecraft:model', 'model': 'bunnidogs:item/purebred_registry'}})
    write('assets/bunnidogs/models/item/purebred_registry.json', {'parent': 'minecraft:item/generated', 'textures': {'layer0': 'bunnidogs:item/purebred_registry'}})
    write('data/bunnidogs/recipe/purebred_registry.json', {'type': 'minecraft:crafting_shapeless',
          'ingredients': ['minecraft:book', 'minecraft:gold_ingot', 'bunnidogs:whisker'], 'result': {'id': 'bunnidogs:purebred_registry'}})
    write('data/bunnidogs/advancement/recipes/tools/purebred_registry.json', {'parent': 'minecraft:recipes/root',
          'criteria': {'has_whisker': {'trigger': 'minecraft:inventory_changed', 'conditions': {'items': [{'items': 'bunnidogs:whisker'}]}},
                       'has_recipe': {'trigger': 'minecraft:recipe_unlocked', 'conditions': {'recipe': 'bunnidogs:purebred_registry'}}},
          'requirements': [['has_whisker', 'has_recipe']], 'rewards': {'recipes': ['bunnidogs:purebred_registry']}})
    write('data/bunnidogs/advancement/breeds_true.json', {'parent': 'minecraft:husbandry/root',
          'criteria': {'registered': {'trigger': 'minecraft:impossible'}}, 'requirements': [['registered']],
          'display': {'icon': {'id': 'bunnidogs:purebred_registry'}, 'title': {'translate': 'advancement.bunnidogs.breeds_true.title'},
                      'description': {'translate': 'advancement.bunnidogs.breeds_true.description'},
                      'frame': 'goal', 'show_toast': True, 'announce_to_chat': True, 'hidden': False}, 'rewards': {'experience': 25}})
    for path in (RES / 'assets/bunnidogs/lang').glob('*.json'):
        value = json.loads(path.read_text()); value.update({
          'item.bunnidogs.purebred_registry': 'Purebred Registry',
          'tooltip.bunnidogs.registry.count': '%s of %s stable lines registered',
          'tooltip.bunnidogs.registry.use': 'Use on an adult to register; use air to open.',
          'tooltip.bunnidogs.registry.goal': 'Needs recorded parents and 7 matching gene pairs.',
          'tooltip.bunnidogs.registry.snapshot': 'One snapshot per genome. No animal or item cost.',
          'message.bunnidogs.registry.added': 'Registered stable line #%s. Its adult genes, stats and parents are recorded.',
          'message.bunnidogs.registry.already': 'This stable line or representative is already registered.',
          'message.bunnidogs.registry.full': 'This registry already holds %s stable lines.',
          'message.bunnidogs.registry.pup': 'Let this pup grow before registering its line.',
          'message.bunnidogs.registry.parents': 'Breed a bunnidog with recorded parents to register its line.',
          'message.bunnidogs.registry.mixed': 'Only %s of %s gene pairs match. Calipers and the Genetics Table show what remains.',
          'tooltip.bunnidogs.gene_splicer.preview': 'Use a two-sample Genetics Table to preview splicing.',
          'advancement.bunnidogs.breeds_true.title': 'Breeds True',
          'advancement.bunnidogs.breeds_true.description': 'Register a bred adult with matching copies in all seven genes.'})
        path.write_text(json.dumps(value, indent=2) + '\n')
    print('Generated Purebred Registry art, recipe, unlock, advancement and instructions.')


if __name__ == '__main__': main()
