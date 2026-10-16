"""Modest native food stores, matching each wild lineage's inherited Diet."""
from gen_syringe import write
from gen_guide import pantry_guide_pool

FOODS = {
    'meadow': [('wheat',6),('carrot',4),('potato',4),('beetroot',3),('apple',2),('sweet_berries',2)],
    'tundra': [('cod',6),('salmon',6)],
    'desert': [('cactus',6),('dried_kelp',4)],
    'swamp': [('red_mushroom',6),('brown_mushroom',6)],
    'cave': [('glow_berries',6)],
    'mountain': [('wheat',6),('moss_block',4),('azalea_leaves',2),('hay_block',1)],
    'nether': [('nether_wart',6),('crimson_fungus',3),('warped_fungus',3)],
    'end': [('chorus_fruit',6)],
}


def uniform(low, high):
    return {'type':'minecraft:uniform','min':low,'max':high}


def item(name, weight, low, high):
    return {'type':'minecraft:item','name':name,'weight':weight,
            'functions':[{'function':'minecraft:set_count','count':uniform(low,high)}]}


def main():
    for lineage,foods in FOODS.items():
        key = f'chests/warren_pantry_{lineage}'
        write(f'data/bunnidogs/loot_table/{key}.json',{'type':'minecraft:chest','random_sequence':f'bunnidogs:{key}',
              'pools':[{'rolls':1,'entries':[item('bunnidogs:guava',1,1,2)]},
                       {'rolls':uniform(1,3),'entries':[item(f'minecraft:{name}',weight,1,3) for name,weight in foods]},
                       pantry_guide_pool()]})
    print('Generated eight lineage pantries: guavas, local foods and an independent Field Guide chance.')


if __name__ == '__main__': main()
