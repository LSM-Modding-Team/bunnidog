#!/usr/bin/env python3
"""Recipe/loot census from shipped data plus native scale_audit observations.
No fixed exchange rate between ores, rare drops, XP and player time is assumed.
Run after scale_audit: python3 tools/progression_audit.py build/scale-full.log
"""
from pathlib import Path
from collections import Counter,defaultdict
import json,math,sys
ROOT=Path(__file__).resolve().parents[1];DATA=ROOT/'src/main/resources/data/bunnidogs'
OUT=ROOT/'docs/audits';OUT.mkdir(exist_ok=True)
recipes={p.stem:json.loads(p.read_text()) for p in (DATA/'recipe').glob('*.json')}
# Canonical output lookup includes the named smelting recipes; prefer the blast furnace.
by_output={}
for key,r in sorted(recipes.items(),key=lambda kv:('blasting' not in kv[1]['type'],kv[0])):
    if key.startswith('unpack_'):continue # Acquisition uses raw material, not circular storage unpacking.
    result=r.get('result',{});result={'id':result} if isinstance(result,str) else result
    if result.get('id','').startswith('bunnidogs:'):
        by_output.setdefault(result['id'],(key,r))
for key,r in recipes.items():
    result=r.get('result',{});result={'id':result} if isinstance(result,str) else result
    if result.get('id')=='bunnidogs:'+key:by_output[result['id']]=(key,r)

def ingredient(i):
    if isinstance(i,str):return i
    if isinstance(i,list):return 'choice('+','.join(map(ingredient,i))+')'
    if 'tag' in i:return '#'+i['tag']
    item=i.get('items',i.get('item'));item=','.join(item) if isinstance(item,list) else item
    return str(item)+(' [ground remnant]' if i.get('components',{}).get('bunnidogs:ground_remnant') else '')
def inputs(r):
    if 'pattern' in r:
        return Counter(ingredient(r['key'][c]) for row in r['pattern'] for c in row if c!=' ')
    if 'ingredients' in r:return Counter(map(ingredient,r['ingredients']))
    if 'ingredient' in r:return Counter([ingredient(r['ingredient'])])
    return Counter(ingredient(r[k]) for k in ['base','addition','template'] if k in r)
def project(goals):
    demand=Counter({'bunnidogs:'+item:n for item,n in goals.items()});graph={}
    def visit(item,ancestors=()):
        if item not in by_output:return
        if item in ancestors:raise ValueError(('Cyclic recipe needs an explicit acquisition policy',item))
        if item in graph:return
        recipe=by_output[item][1];children=inputs(recipe);graph[item]=children
        for child in children:visit(child,ancestors+(item,))
    for item in demand:visit(item)
    incoming=Counter(child for parent,children in graph.items() for child in children if child in graph)
    ready=[item for item in graph if incoming[item]==0];raw=Counter();work=Counter();processed=set()
    while ready:
        item=ready.pop();processed.add(item);r=by_output[item][1];result=r['result'];made=result.get('count',1) if isinstance(result,dict) else 1
        batches=math.ceil(demand[item]/made)
        if 'cookingtime' in r:work['furnace_ticks']+=r['cookingtime']*batches
        for material,n in graph[item].items():
            demand[material]+=batches*n
            if material in graph:
                incoming[material]-=1
                if incoming[material]==0:ready.append(material)
    assert len(processed)==len(graph),'Recipe graph did not resolve'
    raw={item:n for item,n in demand.items() if item not in graph}
    return {'raw':dict(sorted(raw.items())),'work':dict(work)}
parts=['helmet','chestplate','leggings','boots']
goals={'mining_foothold':{'astralite_pickaxe':1},'astralite_armour':{'astralite_'+p:1 for p in parts},'heartbound_armour':{'heartbound_'+p:1 for p in parts},'sovereign_armour':{'sovereign_'+p:1 for p in parts}}
for item in ['netherite_guava','beacon_guava','dragon_egg_guava','astralite_bunnidog_barding','fireball_launcher','prism_lance','sovereign_lance','concord_standard','abyssal_flask','genetic_vacuum','cataclysm_totem','pack_armoury','resonance_bench','bastion_heart','launch_aerie','nursery_hearth','kennel_wagon','hearth_tree','grafting_frame','feeding_trough','nursery_cradle','apex_crucible','hearth_incense','warren_anchor','resonant_eye','warren_ward']:
    if 'bunnidogs:'+item in by_output:goals[item]={item:1}
goals.update({'64_volatile_charges':{'volatile_charge':64},'64_prism_cells':{'prism_cell':64}})
projects={name:project(goal) for name,goal in goals.items()}

def bounds(n):
    if isinstance(n,(int,float)):return n,n
    return n['min'],n['max']
def loot(p):
    result=defaultdict(lambda:[0,0])
    for pool in json.loads(p.read_text())['pools']:
        # These authored tables use one guaranteed item per pool. Fail on a future unsupported format.
        assert pool['rolls']==1 and len(pool['entries'])==1 and not pool.get('conditions'),p
        e=pool['entries'][0];assert e['type']=='minecraft:item' and not e.get('conditions'),p
        count=next((f['count'] for f in e.get('functions',[]) if f['function']=='minecraft:set_count'),1)
        lo,hi=bounds(count);result[e['name']][0]+=lo;result[e['name']][1]+=hi
    return dict(result)
tables={p.stem:loot(p) for p in (DATA/'loot_table/chests/crown_stores').glob('*.json')}
for name in ['crown_wing_0','crown_wing_1','crown_wing_2','crown_vault']:tables[name]=loot(DATA/f'loot_table/chests/{name}.json')
budget=defaultdict(lambda:[0,0])
for table,stock in tables.items():
    for item,(lo,hi) in stock.items():budget[item][0]+=lo;budget[item][1]+=hi
log=Path(sys.argv[1]) if len(sys.argv)>1 else ROOT/'build/scale-full.log'
native=next(json.loads(line.split('PROGRESSION_SCALE_AUDIT ',1)[1]) for line in log.read_text().splitlines() if 'PROGRESSION_SCALE_AUDIT ' in line)
report={'basis':'shipped recipes and finite V2 cupboard/wing/vault tables; native scale_audit','projects':projects,'whole_site_chests':dict(sorted(budget.items())),'tables':tables,'native':native}
(OUT/'progression-data.json').write_text(json.dumps(report,indent=2)+'\n')
lines=['# Generated progression quantities','','Reproduce with `python3 tools/progression_audit.py <passing GameTest log>`.','All amounts are acquisition units. Vanilla gear is assumed already owned; fuels,','travel, failed attempts, enchantments and teaching time are additional. Recipe','batches round upward. Tags/alternative ingredients stay explicit; no ore, XP or','Netherite equivalence is invented. Furnace ticks are serial work, not a mandatory','wall-clock wait: multiple furnaces run in parallel. See ../PROGRESSION_SCALE_AUDIT.md.','']
for name,section in projects.items():
    lines+=['## '+name.replace('_',' '),'','| Raw input | Count |','| --- | ---: |']
    lines += [f'| {item} | {n} |' for item,n in section['raw'].items()]
    lines+=['',f"Serial furnace work: {section['work'].get('furnace_ticks',0)/20:.0f} seconds.",'']
lines+=['## Whole-site physical chest budget','','One of every authored V2 stock table, three earned wings and the boss vault.','This excludes archives, creature drops and repeat rewards. Decorative cupboards','divide their group budget; they do not each receive the entire group.','','| Material | Minimum | Maximum |','| --- | ---: | ---: |']
lines += [f'| {item} | {lo} | {hi} |' for item,(lo,hi) in sorted(budget.items())]
lines+=['','## Native juice scaling','','Actual healing over 500 native effect ticks on an injured actor.','','| Quality | Strength level | Regeneration level | Healing HP/s | Duration s |','| ---: | ---: | ---: | ---: | ---: |']
for e in native['effects']:lines+=[f"| {e['quality']:g} | {e['strength_level']} | {e['regen_level']} | {e['native_healing_hp_per_second']:.2f} | {e['duration_ticks']/20:g} |"]
lines+=['','## Matched-line native enemies','','Fixed ordinary genome, 40 points in four physical stats / 20 Bloom.','This isolates the equipment difference, rather than comparing unrelated random','founders. HP, attack attributes, armour and toughness come from actual actors.','Special attacks retain their own multipliers, armour rules and cooldowns.','','| Role | Flying | Barding | HP | Attack attribute | Armour | Toughness |','| --- | --- | --- | ---: | ---: | ---: | ---: |']
for e in native['fighters']:lines+=[f"| {e['role']} | {e['flying']} | {e['barding']} | {e['health']:.1f} | {e['attack']:.2f} | {e['armor']} | {e['toughness']:g} |"]
(OUT/'progression-quantities.md').write_text('\n'.join(lines)+'\n')
print(f'Audited {len(projects)} equipment/project bills and {len(tables)} finite loot tables; native effects and matched enemies included.')
