#!/usr/bin/env python3
"""Native Breeder profession: editable 26.2 trades, POI tag and a guava-work apron."""
import json
from PIL import Image, ImageDraw, ImageFont
from gen_syringe import ROOT, RES, write

REGIONAL_PELTS = {'plains':'tan', 'desert':'sand', 'savanna':'cream', 'taiga':'brown',
                  'snow':'snow', 'swamp':'moss', 'jungle':'spotted'}


def main():
    trades = {i: [] for i in range(1, 6)}

    def trade(level, name, wanted, count, given, quantity=1, uses=12, xp=5, extra=None, variant=None):
        data = {'wants': {'id': wanted, 'count': count}, 'gives': {'id': given, 'count': quantity},
                'max_uses': uses, 'xp': xp, 'reputation_discount': .05}
        if extra:
            data['additional_wants'] = {'id': extra[0], 'count': extra[1]}
        if variant:
            data['merchant_predicate'] = {'condition': 'minecraft:entity_properties', 'entity': 'this',
                'predicate': {'minecraft:predicates': {'minecraft:villager/variant': ['minecraft:'+variant]}}}
        ident = f'bunnidogs:breeder/{level}/{name}'
        trades[level].append(ident)
        write(f'data/bunnidogs/villager_trade/breeder/{level}/{name}.json', data)

    trade(1, 'guava_emerald', 'bunnidogs:guava', 20, 'minecraft:emerald', uses=16, xp=2)
    trade(1, 'ear_tags', 'minecraft:emerald', 1, 'bunnidogs:ear_tag', 4, xp=1)
    trade(1, 'field_guide', 'minecraft:emerald', 2, 'bunnidogs:field_guide', uses=4, xp=1)
    for variant, coat in REGIONAL_PELTS.items():
        trade(2, coat+'_pelt', 'bunnidogs:'+coat+'_pelt', 4, 'minecraft:emerald', xp=10, variant=variant)
    trade(2, 'syringe', 'minecraft:emerald', 4, 'bunnidogs:syringe', uses=4, xp=5)
    trade(3, 'sinew_emerald', 'bunnidogs:sinew', 8, 'minecraft:emerald', xp=20)
    trade(3, 'breeding_pen', 'minecraft:emerald', 10, 'bunnidogs:breeding_pen', uses=4, xp=10)
    trade(4, 'juicer', 'minecraft:emerald', 12, 'bunnidogs:juicer', uses=3, xp=15)
    trade(4, 'golden_guava', 'minecraft:emerald', 8, 'bunnidogs:golden_guava', uses=6, xp=15)
    trade(5, 'diamond_guava', 'minecraft:emerald', 32, 'bunnidogs:diamond_guava', uses=3, xp=30, extra=('minecraft:diamond', 4))
    trade(5, 'gene_splicer', 'minecraft:emerald', 24, 'bunnidogs:gene_splicer', uses=2, xp=30, extra=('bunnidogs:mutant_marrow', 1))
    for level, values in trades.items():
        write(f'data/bunnidogs/tags/villager_trade/breeder/level_{level}.json', {'values': values})
        write(f'data/bunnidogs/trade_set/breeder/level_{level}.json', {'trades': f'#bunnidogs:breeder/level_{level}',
            'amount': 3 if level == 1 else 2, 'random_sequence': f'bunnidogs:trade_set/breeder/level_{level}'})
    write('data/minecraft/tags/point_of_interest_type/acquirable_job_site.json', {'replace': False, 'values': ['bunnidogs:breeding_pen']})

    # Vanilla 64x64 villager UV: a sleeveless green work apron over the biome clothes,
    # two leather straps and copper pockets. No hat, animal mesh or new renderer.
    apron = Image.new('RGBA', (64, 64)); d = ImageDraw.Draw(apron)
    green = (83, 113, 65); light = (108, 139, 77); shadow = (56, 77, 47); leather = (116, 75, 43)
    # The outer jacket uses texOffs(0,38), 8x20x6: front 6..13/44..63,
    # back 20..27. Painting only the inner body would hide the apron under biome clothes.
    for left in (6,20):
        d.rectangle((left,47,left+7,63), fill=green)
        d.rectangle((left+1,44,left+1,49), fill=leather)
        d.rectangle((left+6,44,left+6,49), fill=leather)
        d.line((left,52,left+7,52), fill=leather, width=2)
        d.line((left+1,62,left+6,62), fill=light)
    for left in (0,14): d.rectangle((left,52,left+5,53), fill=leather)
    d.rectangle((7,55,9,58), fill=(159,104,55)); d.rectangle((11,55,13,58), fill=(159,104,55))
    d.point((8,56), fill=(224,179,102)); d.point((12,56), fill=(224,179,102))
    # Small embroidered guava on the bib.
    d.ellipse((8,48,11,51), fill=(219,193,77)); d.point((10,47), fill=(143,186,83))
    for folder in ('villager', 'zombie_villager'):
        path = RES/f'assets/bunnidogs/textures/entity/{folder}/profession/breeder.png'
        path.parent.mkdir(parents=True, exist_ok=True); apron.save(path)
        write(f'assets/bunnidogs/textures/entity/{folder}/profession/breeder.png.mcmeta', {'villager': {'hat': 'none'}})
    path = RES/'assets/bunnidogs/lang/en_us.json'; language = json.loads(path.read_text())
    language['entity.bunnidogs.villager.breeder'] = 'Breeder'
    path.write_text(json.dumps(language, indent=2)+'\n')
    preview = Image.new('RGB', (780,300), (40,43,36)); p = ImageDraw.Draw(preview)
    font = ImageFont.truetype('DejaVuSans.ttf', 16)
    p.text((18,14), 'Breeder — guava apron, leather straps and copper pockets', font=font, fill=(237,225,190))
    preview.paste(apron.resize((256,256), Image.Resampling.NEAREST), (12,42), apron.resize((256,256), Image.Resampling.NEAREST))
    front = apron.crop((6,44,14,64)).resize((88,220),Image.Resampling.NEAREST)
    preview.paste(front,(310,62),front)
    for i, row in enumerate(('Pen job site; normal villager career', 'Novice: guavas, tags, guide', 'Apprentice: local pelts, syringe',
                             'Journeyman: sinew, pens', 'Expert: Juicer, golden guava', 'Master: diamond guava, splicer')):
        p.text((435,62+i*32),row,font=font,fill=(220,218,189))
    preview.save(ROOT/'art/breeder_preview.png')
    print('Generated Breeder trade sets, regional pelt contracts, native profession textures and preview.')


if __name__ == '__main__':
    main()
