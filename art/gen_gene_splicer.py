"""Gene Splicer: paired glass channels, copper coil and iron housing at the tools' native 16px scale."""
import json
from pathlib import Path
from PIL import Image, ImageDraw
from gen_syringe import write

ROOT = Path(__file__).resolve().parents[1]
RES = ROOT / 'src/main/resources'


def main():
    img = Image.new('RGBA',(16,16)); d = ImageDraw.Draw(img)
    edge, iron, copper = (49,56,68), (193,208,214), (210,126,78)
    d.polygon([(1,11),(8,4),(12,8),(5,15)],fill=edge)
    d.line((3,11,8,6),fill=iron,width=3)
    d.line((5,13,10,8),fill=copper,width=2)
    d.rectangle((7,1,10,6),fill=edge); d.rectangle((8,2,9,5),fill=(151,222,224))
    d.rectangle((11,3,14,8),fill=edge); d.rectangle((12,4,13,7),fill=(204,125,190))
    d.line((6,7,10,11),fill=edge,width=3); d.line((6,7,10,11),fill=copper,width=1)
    d.line((2,13,4,15),fill=iron,width=2)
    img.save(RES / 'assets/bunnidogs/textures/item/gene_splicer.png')
    preview = Image.new('RGBA',(256,256),(45,43,42,255))
    preview.alpha_composite(img.resize((256,256),Image.Resampling.NEAREST)); preview.save(ROOT / 'art/gene_splicer_preview.png')
    write('assets/bunnidogs/items/gene_splicer.json',{'model':{'type':'minecraft:model','model':'bunnidogs:item/gene_splicer'}})
    write('assets/bunnidogs/models/item/gene_splicer.json',{'parent':'minecraft:item/handheld','textures':{'layer0':'bunnidogs:item/gene_splicer'}})
    write('data/bunnidogs/recipe/gene_splicer.json',{'type':'minecraft:crafting_shaped','pattern':['DMD','CIC',' I '],
          'key':{'D':'minecraft:diamond','M':'bunnidogs:mutant_marrow','C':'minecraft:copper_ingot','I':'minecraft:iron_ingot'},'result':{'id':'bunnidogs:gene_splicer'}})
    write('data/bunnidogs/advancement/recipes/tools/gene_splicer.json',{'parent':'minecraft:recipes/root',
          'criteria':{'has_marrow':{'trigger':'minecraft:inventory_changed','conditions':{'items':[{'items':'bunnidogs:mutant_marrow'}]}},
                      'has_recipe':{'trigger':'minecraft:recipe_unlocked','conditions':{'recipe':'bunnidogs:gene_splicer'}}},
          'requirements':[['has_marrow','has_recipe']],'rewards':{'recipes':['bunnidogs:gene_splicer']}})
    for path in (RES / 'assets/bunnidogs/lang').glob('*.json'):
        value = json.loads(path.read_text()); value.update({
          'item.bunnidogs.gene_splicer':'Gene Splicer',
          'tooltip.bunnidogs.gene_splicer.select':'Sneak-use to select a gene.',
          'tooltip.bunnidogs.gene_splicer.donor':'Other hand: donor Tissue Sample. Use on a healthy adult.',
          'tooltip.bunnidogs.gene_splicer.cost':'Costs one sample and one Mutagen per splice.',
          'tooltip.bunnidogs.gene_splicer.slot':'Replaces the less dominant copy of the selected gene.',
          'tooltip.bunnidogs.gene_splicer.copy':'Copies one allele; breed to fix the trait. Bred stats stay.',
          'message.bunnidogs.splice.selected':'Selected gene: %s',
          'message.bunnidogs.splice.health':'Splicing needs an adult at least half healthy.',
          'message.bunnidogs.splice.wait':'This bunnidog needs %s more seconds before another splice.',
          'message.bunnidogs.splice.donor':'Hold a donor Tissue Sample in the other hand.',
          'message.bunnidogs.splice.carried':'This bunnidog already carries that allele. Breed to fix the trait.',
          'message.bunnidogs.splice.mutagen':'Carry one Mutagen for this splice.',
          'message.bunnidogs.splice.done':'Spliced %s: %s. The donor allele may be carried without showing.'})
        path.write_text(json.dumps(value,indent=2)+'\n')
    print('Generated Gene Splicer icon, crafting, recipe unlock and instructions.')


if __name__ == '__main__': main()
