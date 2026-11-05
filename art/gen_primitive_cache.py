"""Generate only the paid primitive cache assets, preserving all unrelated current art and prose."""
from pathlib import Path
import json

ROOT = Path(__file__).resolve().parents[1]
RES = ROOT / 'src/main/resources'
ASSETS = RES / 'assets/bunnidogs'
DATA = RES / 'data/bunnidogs'

def write(root, path, value):
    dest = root / path
    dest.parent.mkdir(parents=True, exist_ok=True)
    dest.write_text(json.dumps(value, ensure_ascii=False, indent=2) + '\n')

def box(lo, hi, texture):
    return {'from': lo, 'to': hi, 'faces': {face: {'texture': '#' + texture, 'uv': [0, 0, 16, 16]} for face in ['up', 'down', 'north', 'south', 'east', 'west']}}

write(ASSETS, 'models/block/hearth_tree_bare.json', {
    'parent': 'minecraft:block/block',
    'textures': {'particle': 'bunnidogs:block/guava_log', 'wood': 'bunnidogs:block/guava_log', 'leaf': 'bunnidogs:block/hearth_leaf'},
    'elements': [box([5, 0, 5], [11, 12, 11], 'wood'), box([4, 10, 4], [12, 16, 12], 'leaf')]})
write(ASSETS, 'models/item/root_cache.json', {
    'parent': 'minecraft:block/block',
    'textures': {'particle': 'bunnidogs:block/hearth_store', 'store': 'bunnidogs:block/hearth_store', 'frame': 'bunnidogs:block/hearth_frame'},
    'elements': [box([1, 0, 1], [15, 7, 15], 'store'), box([1, 6, 1], [15, 9, 15], 'frame')]})
write(ASSETS, 'items/root_cache.json', {'model': {'type': 'minecraft:model', 'model': 'bunnidogs:item/root_cache'}})
write(ASSETS, 'blockstates/hearth_tree.json', {'variants': {
    f'cached={str(cached).lower()},stage={stage}': {'model': f'bunnidogs:block/hearth_tree_{stage}' if cached else 'bunnidogs:block/hearth_tree_bare'}
    for cached in [False, True] for stage in range(5)}})
write(DATA, 'loot_table/blocks/hearth_tree.json', {'type': 'minecraft:block', 'pools': [{
    'rolls': 1, 'entries': [{'type': 'minecraft:item', 'name': 'bunnidogs:hearth_tree'}],
    'conditions': [{'condition': 'minecraft:survives_explosion'}, {'condition': 'minecraft:block_state_property', 'block': 'bunnidogs:hearth_tree', 'properties': {'cached': 'true'}}]}]})
write(DATA, 'recipe/root_cache.json', {'type': 'minecraft:crafting_shaped', 'category': 'misc', 'pattern': ['PP', 'P '], 'key': {'P': '#minecraft:planks'}, 'result': {'id': 'bunnidogs:root_cache'}})
write(DATA, 'advancement/recipes/hearth/root_cache.json', {'parent': 'minecraft:recipes/root', 'criteria': {'material': {'trigger': 'minecraft:inventory_changed', 'conditions': {'items': [{'items': '#minecraft:planks'}]}}}, 'requirements': [['material']], 'rewards': {'recipes': ['bunnidogs:root_cache']}})
for locale, changes in {
    'en_us': {'item.bunnidogs.root_cache': 'Root Cache', 'tooltip.bunnidogs.root_cache': 'Wooden stores for an existing wild camp.', 'message.bunnidogs.hearth.need.cache': 'The camp needs a wooden cache.'},
    'es_pe': {'item.bunnidogs.root_cache': 'Almacén de raíces', 'tooltip.bunnidogs.root_cache': 'Depósito de madera para un campamento silvestre existente.', 'message.bunnidogs.hearth.need.cache': 'El campamento necesita un almacén de madera.'}
}.items():
    path = Path('lang') / f'{locale}.json'
    text = json.loads((ASSETS / path).read_text())
    text.update(changes)
    write(ASSETS, path, text)
