"""Apex crafting parts and generous generic ruin stock. Run after gen_realm.py."""
from pathlib import Path
import json,zipfile,io
from PIL import Image,ImageDraw
ROOT=Path(__file__).resolve().parents[1];RES=ROOT/'src/main/resources';A=RES/'assets/bunnidogs';D=RES/'data/bunnidogs'
def write(root,path,obj):
 p=root/Path(path);p.parent.mkdir(parents=True,exist_ok=True);p.write_text(json.dumps(obj,indent=2)+'\n')
def shaped(name,pattern,key,out,count=1):
 write(D,'recipe/'+name+'.json',{'type':'minecraft:crafting_shaped','category':'equipment','pattern':pattern,'key':key,'result':{'id':'bunnidogs:'+out,'count':count}})
 write(D,'advancement/recipes/great_warren/'+name+'.json',{'parent':'minecraft:recipes/root','criteria':{'part':{'trigger':'minecraft:inventory_changed','conditions':{'items':[{'items':next(iter(key.values()))}]}}},'requirements':[['part']],'rewards':{'recipes':['bunnidogs:'+name]}})
def smith(name,base,addition):
 write(D,'recipe/'+name+'.json',{'type':'minecraft:smithing_transform','base':base,'addition':'bunnidogs:'+addition,'result':{'id':'bunnidogs:'+name}})
 write(D,'advancement/recipes/great_warren/'+name+'.json',{'parent':'minecraft:recipes/root','criteria':{'artifact':{'trigger':'minecraft:inventory_changed','conditions':{'items':[{'items':'bunnidogs:'+addition}]}}},'requirements':[['artifact']],'rewards':{'recipes':['bunnidogs:'+name]}})
shaped('vitality_artifact',['SGS','MHM','SGS'],{'S':'bunnidogs:heart_spool','G':'minecraft:enchanted_golden_apple','M':'bunnidogs:apex_marrow','H':'bunnidogs:chorus_heart'},'vitality_artifact')
shaped('aerial_artifact',['MLM','HNH','MVM'],{'M':'bunnidogs:aerial_membrane','L':'bunnidogs:prism_lens','H':'bunnidogs:heart_spool','N':'minecraft:netherite_block','V':'bunnidogs:volatile_heart'},'aerial_artifact')
for part in ['helmet','chestplate','leggings','boots']:smith('heartbound_'+part,'bunnidogs:astralite_'+part,'vitality_artifact')
smith('apex_wing_harness','minecraft:elytra','aerial_artifact')
shaped('fireball_launcher',['VVF','MNB','VVF'],{'V':'bunnidogs:volatile_heart','F':'bunnidogs:apex_fang','M':'bunnidogs:apex_marrow','N':'minecraft:netherite_ingot','B':'minecraft:blaze_rod'},'fireball_launcher')
shaped('volatile_charge',['GFG','GHG','GFG'],{'G':'minecraft:gunpowder','F':'minecraft:fire_charge','H':'bunnidogs:volatile_heart'},'volatile_charge',16)
shaped('genetic_vacuum',['HLS','MNB','HLS'],{'H':'bunnidogs:chorus_heart','L':'bunnidogs:prism_lens','S':'bunnidogs:heart_spool','M':'bunnidogs:apex_marrow','N':'minecraft:netherite_ingot','B':'minecraft:bundle'},'genetic_vacuum')
shaped('abyssal_flask',['HLH','SBS','HMH'],{'H':'bunnidogs:chorus_heart','L':'bunnidogs:prism_lens','S':'bunnidogs:heart_spool','B':'minecraft:glass_bottle','M':'bunnidogs:aerial_membrane'},'abyssal_flask')
shaped('prism_lance',[' LL','SFL','NM '],{'L':'bunnidogs:prism_lens','S':'bunnidogs:heart_spool','F':'bunnidogs:apex_fang','N':'minecraft:netherite_ingot','M':'bunnidogs:aerial_membrane'},'prism_lance')
shaped('cataclysm_core',['VMV','SFS','VMV'],{'V':'bunnidogs:volatile_heart','M':'bunnidogs:apex_marrow','S':'bunnidogs:heart_spool','F':'bunnidogs:apex_fang'},'cataclysm_core')
smith('cataclysm_totem','minecraft:totem_of_undying','cataclysm_core')
def pool(item,lo,hi):return {'rolls':1,'entries':[{'type':'minecraft:item','name':item,'functions':[{'function':'minecraft:set_count','count':{'type':'minecraft:uniform','min':lo,'max':hi}}]}]}
wealth=[('diamond',48,64),('netherite_ingot',12,24),('enchanted_golden_apple',8,16),('gold_block',16,32),('emerald_block',16,32),('totem_of_undying',2,4),('elytra',1,1),('blaze_rod',32,64)]
bulk=[('amethyst_shard',128,192),('copper_block',32,64),('gunpowder',128,192),('ender_pearl',32,48),('crying_obsidian',32,64),('golden_carrot',32,64),('glass_bottle',32,64),('fire_charge',32,64)]
for name,items in [('great_ruin',wealth),('warren_supplies',bulk)]:write(D,'loot_table/chests/'+name+'.json',{'type':'minecraft:chest','random_sequence':'bunnidogs:chests/'+name,'pools':[pool('minecraft:'+item,lo,hi) for item,lo,hi in items]})
# Durability/armor tags retain ordinary anvil enchantment compatibility.
for tag,ids in [('durability',['fireball_launcher','genetic_vacuum']+['heartbound_'+p for p in ['helmet','chestplate','leggings','boots']]),('armor',['apex_wing_harness']+['heartbound_'+p for p in ['helmet','chestplate','leggings','boots']]),('head_armor',['heartbound_helmet']),('chest_armor',['heartbound_chestplate','apex_wing_harness']),('leg_armor',['heartbound_leggings']),('foot_armor',['heartbound_boots'])]:
 p=RES/'data/minecraft/tags/item/enchantable'/f'{tag}.json';old=json.loads(p.read_text()) if p.exists() else {'replace':False,'values':[]};old['values']=list(dict.fromkeys(old['values']+['bunnidogs:'+n for n in ids]));p.parent.mkdir(parents=True,exist_ok=True);p.write_text(json.dumps(old,indent=2)+'\n')
p=RES/'data/minecraft/tags/damage_type/bypasses_armor.json';obj=json.loads(p.read_text());obj['values']=list(dict.fromkeys(obj['values']+['bunnidogs:prism_lance']));p.write_text(json.dumps(obj,indent=2)+'\n')
# Original equipment/ingredient pixel art.
names=['apex_fang','vitality_artifact','aerial_artifact','cataclysm_core','cataclysm_totem','volatile_charge','fireball_launcher','genetic_vacuum','abyssal_flask']
contact=Image.new('RGB',(900,480),(36,40,47));cd=ImageDraw.Draw(contact)
for i,name in enumerate(names):
 im=Image.new('RGBA',(32,32));d=ImageDraw.Draw(im)
 if name=='apex_fang':d.polygon([(7,3),(24,3),(23,14),(16,27),(5,30),(12,17)],fill='#e8dec3',outline='#917a61')
 elif name in ['fireball_launcher','genetic_vacuum']:
  col='#d78149' if name=='fireball_launcher' else '#88c5ce';d.rectangle((5,8,29,20),fill='#55495e',outline='#c9b58e');d.rectangle((23,5,30,23),fill=col,outline='#d8e7c6');d.polygon([(10,19),(17,19),(14,29),(6,27)],fill='#a17c5e');d.ellipse((9,10,18,18),fill=col)
 elif name=='abyssal_flask':
  d.polygon([(11,3),(22,3),(22,11),(27,17),(27,28),(6,28),(6,17),(11,11)],fill='#4b355f',outline='#e4b98c');d.rectangle((9,18,24,25),fill='#9d6ccd');d.ellipse((13,16,20,23),outline='#f4d8a1',width=2);d.rectangle((11,2,22,5),fill='#9f8663')
 elif name=='cataclysm_core':
  d.polygon([(9,2),(23,2),(30,9),(30,23),(23,30),(9,30),(2,23),(2,9)],fill='#623f52',outline='#edbb73');d.ellipse((7,7,25,25),fill='#dc7747',outline='#ffe0a0',width=2);d.polygon([(16,7),(20,17),(16,25),(12,17)],fill='#fff1ac')
 elif name=='cataclysm_totem':
  d.ellipse((10,2,23,14),fill='#e8aa61',outline='#6a3d44');d.polygon([(7,11),(25,11),(23,21),(20,22),(20,30),(13,30),(13,22),(9,21)],fill='#ac6c4c',outline='#eec98a');d.ellipse((13,13,20,21),fill='#ffdf65');d.point((13,7),fill='#8c3846');d.point((20,7),fill='#8c3846')
 elif name=='volatile_charge':d.ellipse((3,4,28,29),fill='#e88349',outline='#7b3945',width=2);d.line((10,7,15,23,23,11),fill='#ffe999',width=3)
 else:
  col='#db889e' if name=='vitality_artifact' else '#8bc4d1';d.polygon([(16,1),(30,16),(16,31),(1,16)],fill='#685168',outline='#e2c68c');d.ellipse((7,7,25,25),fill=col,outline='#fff1c9',width=2);d.line((12,17,15,21,22,12),fill='#fff6cf',width=2)
 write(A,'models/item/'+name+'.json',{'parent':'minecraft:item/handheld' if name in ['fireball_launcher','genetic_vacuum'] else 'minecraft:item/generated','textures':{'layer0':'bunnidogs:item/'+name}})
 write(A,'items/'+name+'.json',{'model':{'type':'minecraft:model','model':'bunnidogs:item/'+name}})
 im.save(A/'textures/item'/f'{name}.png');x=20+(i%5)*175;y=20+(i//5)*145;contact.paste(im.resize((96,96),Image.Resampling.NEAREST),(x,y),im.resize((96,96),Image.Resampling.NEAREST));cd.text((x,y+100),name.replace('_',' '),fill='white')
# Armor icons retain familiar Netherite silhouettes with a new heart inlay.
z=zipfile.ZipFile(ROOT/'build/moddev/artifacts/minecraft-patched-26.2.0.88.jar')
for part in ['helmet','chestplate','leggings','boots']:
 im=Image.open(io.BytesIO(z.read('assets/minecraft/textures/item/netherite_'+part+'.png'))).convert('RGBA');d=ImageDraw.Draw(im);d.polygon([(8,7),(6,6),(5,8),(8,11),(11,8),(10,6)],fill='#ed9bb2');im.save(A/'textures/item'/('heartbound_'+part+'.png'))
 write(A,'models/item/heartbound_'+part+'.json',{'parent':'minecraft:item/generated','textures':{'layer0':'bunnidogs:item/heartbound_'+part}});write(A,'items/heartbound_'+part+'.json',{'model':{'type':'minecraft:model','model':'bunnidogs:item/heartbound_'+part}})
contact.save(ROOT/'art/apex_rewards_preview.png')
# A subtle heart inlay over vanilla Netherite, using the native armor model/UVs.
for layer in ['humanoid','humanoid_leggings']:
 overlay=Image.new('RGBA',(64,32));od=ImageDraw.Draw(overlay)
 od.polygon([(24,22),(22,20),(20,21),(20,24),(24,28),(28,24),(28,21),(26,20)],fill='#e88fa9',outline='#ffe1d3')
 path=A/'textures/entity/equipment'/layer/'heartbound.png';path.parent.mkdir(parents=True,exist_ok=True);overlay.save(path)
write(A,'equipment/heartbound.json',{'layers':{layer:[{'texture':'bunnidogs:astralite'},{'texture':'bunnidogs:heartbound'}] for layer in ['humanoid','humanoid_leggings']}})
status=Image.new('RGBA',(18,18));sd=ImageDraw.Draw(status);sd.ellipse((3,3,14,14),outline='#f4cb7f',width=2)
for x,y in [(2,2),(14,3),(3,14)]:sd.line((x-1,y,x+1,y),fill='#fff2cc');sd.line((x,y-1,x,y+1),fill='#fff2cc')
statuspath=A/'textures/mob_effect/resonant_stun.png';statuspath.parent.mkdir(parents=True,exist_ok=True);status.save(statuspath)

for i,part in enumerate(['helmet','chestplate','leggings','boots']):
 icon=Image.open(A/'textures/item'/('heartbound_'+part+'.png')).convert('RGBA').resize((80,80),Image.Resampling.NEAREST)
 contact.paste(icon,(20+i*175,330),icon);cd.text((20+i*175,420),'Heartbound '+part,fill='white')
contact.save(ROOT/'art/apex_rewards_preview.png')

# Crown finale assets extend these rewards without changing the accepted ordinary Apex gear.
import runpy
runpy.run_path(str(ROOT/"art/gen_crown.py"))
