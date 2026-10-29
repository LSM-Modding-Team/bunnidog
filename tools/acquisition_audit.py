#!/usr/bin/env python3
"""Acquisition routes and native observations, separate from the crafting bill appendix.
Run after scale_acquisition: python3 tools/acquisition_audit.py <GameTest log>.
"""
from pathlib import Path
import json,sys,hashlib
ROOT=Path(__file__).resolve().parents[1];DATA=ROOT/'src/main/resources/data/bunnidogs';OUT=ROOT/'docs/audits'
log=Path(sys.argv[1]) if len(sys.argv)>1 else ROOT/'build/acquisition-native.log'
observed=next(json.loads(line.split('ACQUISITION_AUDIT ',1)[1]) for line in log.read_text().splitlines() if 'ACQUISITION_AUDIT ' in line)
observed['method']={'ore_stock':'Three actual generator/seed moon volumes; no claimed harvest or player time.',
 'explosions':'Actual Blastmaw calculator against translated 45x45x45 patches of seed-1379 moon substrate; four independent illustrative samples, native mob griefing and explosion decay enabled. No player, AI opponents, travel, baiting, collection, fuse or self-damage simulation. Blocks are refilled per sample. Engine explosion/loot RNG is not a fixed golden output.',
 'ruins':'16 native random-spread candidates per seed; height admission only, not guaranteed completed structures or a search time.',
 'trades':'Fresh native merchant offers backed by actual initial goods; not an infinite daily restock.',
 'domestic':'Expected drops for a homozygous Starborn/default-other-genes adult, Looting III. Grooming has one shared recovery, can give multiple products and still requires real health/care eligibility.'}
# Explicit guaranteed item pools only; never interpret a weighted pantry as guaranteed raw stock.
def stock(name):
 p=DATA/f'loot_table/chests/{name}.json';r=json.loads(p.read_text());out={}
 for pool in r['pools']:
  assert pool['rolls']==1 and len(pool['entries'])==1 and not pool.get('conditions')
  e=pool['entries'][0];assert e['type']=='minecraft:item' and not e.get('conditions')
  amount=next((f['count'] for f in e.get('functions',[]) if f['function']=='minecraft:set_count'),1)
  lo,hi=(amount['min'],amount['max']) if isinstance(amount,dict) else (amount,amount)
  out[e['name']]=[lo,hi]
 return {'stock':out,'sha256':hashlib.sha256(p.read_bytes()).hexdigest()}
observed['one_open_ruin']={'treasure':stock('great_ruin'),'supplies':stock('warren_supplies'),
 'archive':{'seed_quality':[20,33],'stat_points':[40,70],'starlit_dust':16,'source':'GreatRuinPiece.archive'},
 'layout':'One treasure chest, one supply chest and one frozen archive. No seal, key or mandatory kill wave; terrain and world enemies still matter.'}
observed['crown_archive']={'seed_quality':[36,47],'stat_points':[70,100],'starlit_dust':64,'source':'CrownArchives.fill'}
(OUT/'acquisition-observations.json').write_text(json.dumps(observed,indent=2)+'\n')
rows=['# Acquisition observations','','See ../RAW_MATERIAL_ACQUISITION.md for route selection, stage gates and interpretation.','','## Actual generated moon stock','','Total contained ore, not collected output or a timed mining run.','','| Seed | Radius | Rock blocks | Astralite ore | Cinderite ore | Height-admitted ruin candidates |','| ---: | ---: | ---: | ---: | ---: | ---: |']
for m in observed['moons']:rows+=[f"| {m['seed']} | {m['moon_radius']} | {m['moon_blocks']:,} | {m['astralite_ore']:,} | {m['cinderite_ore']:,} | {m['height_accepted_candidates']}/{m['ruin_candidates']} |"]
rows+=['','## Native Blastmaw payout samples','','Each row refills a translated real generated substrate patch. Default explosion decay is enabled.','These observe block destruction and spawned drops, not the difficulty/time of making the attack happen.','Four samples cannot establish average expedition yield; source and limitations are in the JSON.','','| Placement | Power | Blocks broken | Astralite ore broken | Shards recovered | Cinderite ore broken | Raw Cinderite recovered | Moonstone recovered |','| --- | ---: | ---: | ---: | ---: | ---: | ---: | ---: |']
for e in observed['extraction']:rows+=[f"| {e['placement']} | {e['power']} | {e['blocks_broken']} | {e['astralite_ore_broken']} | {e['astralite_shard_recovered']} | {e['cinderite_ore_broken']} | {e['raw_cinderite_recovered']} | {e['moonstone_recovered']} |"]
rows+=['','## One open ruin: actual chest stock','','One of each chest, not three copies of the treasure table.','','| Chest | Item | Minimum | Maximum |','| --- | --- | ---: | ---: |']
for category in ['treasure','supplies']:
 for item,(lo,hi) in observed['one_open_ruin'][category]['stock'].items():rows+=[f'| {category} | {item} | {lo} | {hi} |']
rows+=['','## Initial physical Lantern offers','','Finite stock and daytime manufacture determine subsequent availability.','Initial max uses is backed by goods, not a claim that a daily refresh creates those goods.','','| Profession | Input A | Input B | Output | Initial uses |','| ---: | --- | --- | --- | ---: |']
def item(s):return '' if s['count']==0 else f"{s['count']} {s['id']}"
for t in observed['lantern_initial_trades']:rows+=[f"| {t['profession']} | {item(t['cost_a'])} | {item(t['cost_b'])} | {item(t['output'])} | {t['initial_max_uses']} |"]
rows+=['','## Domestic routes','','Starborn expressed, other genes default; eligible adults with Looting III on death.','These are yield expectations, not an animals-per-hour farm measurement.','','| Points in each stat | Sinew / adult | Whisker / adult | Dust / adult | Living grooming products per recovery |','| ---: | ---: | ---: | ---: | --- |']
for r in observed['domestic_materials']:
 d={e['id']:e['mean_per_looting3_adult'] for e in r['death_drops']}
 rows+=[f"| {r['bred_points']} | {d.get('bunnidogs:sinew',0):.2f} | {d.get('bunnidogs:whisker',0):.2f} | {d.get('bunnidogs:starlit_dust',0):.2f} | {', '.join(r['grooming_products_per_12000_ticks']) or 'None'} |"]
(OUT/'acquisition-observations.md').write_text('\n'.join(rows)+'\n')
print('Recorded native acquisition samples, actual ruin payouts, stocked trades and domestic harvest routes.')
