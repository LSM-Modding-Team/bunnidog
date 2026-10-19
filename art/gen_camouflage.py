#!/usr/bin/env python3
"""Camouflage's editable native ground tags and English feedback. No model edits."""
import json
from gen_syringe import RES, write


def main():
    grounds = {
        'sand': ['sand', 'red_sand', 'sandstone', 'smooth_sandstone', 'cut_sandstone',
                 'red_sandstone', 'smooth_red_sandstone', 'cut_red_sandstone'],
        'earth': ['dirt', 'coarse_dirt', 'rooted_dirt', 'podzol', 'dirt_path', 'farmland', 'mud', 'muddy_mangrove_roots'],
        'green': ['grass_block', 'moss_block', 'moss_carpet', 'pale_moss_block', 'pale_moss_carpet', '#minecraft:leaves'],
        'snow': ['snow', 'snow_block', 'powder_snow'],
        'stone': ['stone', 'diorite', 'polished_diorite', 'calcite', 'tuff'],
        'dark_stone': ['deepslate', 'cobbled_deepslate', 'polished_deepslate', 'deepslate_bricks', 'deepslate_tiles',
                       'cracked_deepslate_bricks', 'cracked_deepslate_tiles', 'reinforced_deepslate', 'blackstone',
                       'polished_blackstone', 'polished_blackstone_bricks', 'basalt', 'smooth_basalt', 'polished_basalt'],
        'nether': ['netherrack', 'nether_wart_block', 'nether_bricks', 'red_nether_bricks', 'crimson_nylium'],
        'obsidian': ['obsidian', 'crying_obsidian', 'purpur_block', 'purpur_pillar'],
    }
    for name, values in grounds.items():
        write(f'data/bunnidogs/tags/block/camouflage/{name}.json', {
            'replace': False, 'values': [v if v.startswith('#') else 'minecraft:'+v for v in values]})
    text = {
        'message.bunnidogs.care.camouflage': 'Camouflage: %s',
        'message.bunnidogs.camouflage.unavailable': 'unavailable',
        'message.bunnidogs.camouflage.luminous': 'a luminous coat cannot blend in',
        'message.bunnidogs.camouflage.disturbed': 'needs full health, calm, dry footing and no cargo, transport or glowing effect',
        'message.bunnidogs.camouflage.exposed': 'exposed; matching ground is %s',
        'message.bunnidogs.camouflage.blended': 'blended on %s; half visual range and no walking vibrations',
    }
    path = RES/'assets/bunnidogs/lang/en_us.json'
    language = json.loads(path.read_text()); language.update(text)
    path.write_text(json.dumps(language, indent=2)+'\n')
    print('Generated eight camouflage ground tags and care feedback.')


if __name__ == '__main__':
    main()
