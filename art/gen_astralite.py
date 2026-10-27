"""Astralite's pre-dungeon economy and original moon/gear art. Run last after gen_apex_rewards."""
from pathlib import Path
import json,random,zipfile,io
from PIL import Image,ImageDraw
ROOT=Path(__file__).resolve().parents[1];RES=ROOT/'src/main/resources';A=RES/'assets/bunnidogs';D=RES/'data/bunnidogs'
def write(root,path,obj):
 p=root/path;p.parent.mkdir(parents=True,exist_ok=True);p.write_text(json.dumps(obj,indent=2,ensure_ascii=False)+'\n')
def tag(path,ids):
 p=RES/'data/minecraft/tags'/path;obj=json.loads(p.read_text()) if p.exists() else {'replace':False,'values':[]};obj['values']=list(dict.fromkeys(obj['values']+['bunnidogs:'+i for i in ids]));write(RES/'data/minecraft/tags',path,obj)
def unlock(name,item):write(D,Path('advancement/recipes/astralite')/(name+'.json'),{'parent':'minecraft:recipes/root','criteria':{'material':{'trigger':'minecraft:inventory_changed','conditions':{'items':[{'items':item if ':' in item else 'bunnidogs:'+item}]}}},'requirements':[['material']],'rewards':{'recipes':['bunnidogs:'+name]}})
def shaped(name,pattern,key,count=1):
 write(D,Path('recipe')/(name+'.json'),{'type':'minecraft:crafting_shaped','category':'equipment','pattern':pattern,'key':key,'result':{'id':'bunnidogs:'+name,'count':count}});unlock(name,next(iter(key.values())))
shaped('astralite_ingot',['SCS','CMC','SCS'],{'S':'bunnidogs:astralite_shard','C':'bunnidogs:cinderite_ingot','M':'bunnidogs:apex_marrow'})
shaped('astralite_upgrade',['DAD','PNP','DAD'],{'D':'minecraft:diamond','A':'bunnidogs:aerial_membrane','P':'bunnidogs:prism_lens','N':'minecraft:netherite_ingot'})
for kind,time in [('smelting',200),('blasting',100)]:
 name='cinderite_ingot_from_'+kind;write(D,Path('recipe')/(name+'.json'),{'type':'minecraft:'+kind,'category':'misc','ingredient':'bunnidogs:raw_cinderite','result':{'id':'bunnidogs:cinderite_ingot'},'experience':2,'cookingtime':time});unlock(name,'raw_cinderite')
parts=['helmet','chestplate','leggings','boots'];tools=['sword','pickaxe','axe','shovel','hoe']
for part in parts+tools:
 out='astralite_'+part;write(D,Path('recipe')/(out+'.json'),{'type':'minecraft:smithing_transform','template':'bunnidogs:astralite_upgrade','base':'minecraft:netherite_'+part,'addition':'bunnidogs:astralite_ingot','result':{'id':'bunnidogs:'+out}});unlock(out,'astralite_ingot')
for part in parts:
 write(D,Path('recipe')/('heartbound_'+part+'.json'),{'type':'minecraft:smithing_transform','base':'bunnidogs:astralite_'+part,'addition':'bunnidogs:vitality_artifact','result':{'id':'bunnidogs:heartbound_'+part}})
write(D,Path('tags/item/astralite_repair.json'),{'replace':False,'values':['bunnidogs:astralite_ingot']})
allgear=['astralite_'+p for p in parts+tools]
tag(Path('item/enchantable/durability.json'),allgear)
for part in parts:
 for name in ['armor',{'helmet':'head_armor','chestplate':'chest_armor','leggings':'leg_armor','boots':'foot_armor'}[part]]:tag(Path('item/enchantable')/(name+'.json'),['astralite_'+part])
# Inherit every native Netherite enchantment/mining/tool tag, including NeoForge tool abilities.
z=zipfile.ZipFile(ROOT/'build/moddev/artifacts/minecraft-patched-26.2.0.88.jar')
for path in z.namelist():
 if path.startswith('data/minecraft/tags/item/') and path.endswith('.json'):
  obj=json.loads(z.read(path));extra=[]
  for p in parts+tools:
   if 'minecraft:netherite_'+p in obj.get('values',[]):extra.append('astralite_'+p)
  if extra:tag(Path(path.removeprefix('data/minecraft/tags/')),extra)
for name in ['moonstone','astralite_ore','cinderite_ore']:
 tag(Path('block/mineable/pickaxe.json'),[name]);tag(Path('block/needs_diamond_tool.json'),[name] if name!='moonstone' else [])
 write(A,Path('blockstates')/(name+'.json'),{'variants':{'':{'model':'bunnidogs:block/'+name}}});write(A,Path('models/block')/(name+'.json'),{'parent':'minecraft:block/cube_all','textures':{'all':'bunnidogs:block/'+name}})
 write(A,Path('models/item')/(name+'.json'),{'parent':'bunnidogs:block/'+name});write(A,Path('items')/(name+'.json'),{'model':{'type':'minecraft:model','model':'bunnidogs:item/'+name}})
 rng=random.Random(651);im=Image.new('RGB',(16,16))
 for x in range(16):
  for y in range(16):
   c=rng.randint(-13,13);im.putpixel((x,y),(89+c,91+c,117+c))
 d=ImageDraw.Draw(im)
 if name!='moonstone':
  color='#9fe9e6' if name=='astralite_ore' else '#efaa6b'
  for x,y in [(3,4),(10,3),(7,10),(12,13),(2,13)]:d.line((x,y,x+2,y-1),fill=color,width=2);d.point((x,y-1),fill='#f7e9c9')
 im.save(A/'textures/block'/f'{name}.png')
 if name=='moonstone':pools=[{'rolls':1,'entries':[{'type':'minecraft:item','name':'bunnidogs:moonstone'}],'conditions':[{'condition':'minecraft:survives_explosion'}]}]
 else:
  drop='astralite_shard' if name=='astralite_ore' else 'raw_cinderite'
  pools=[{'rolls':1,'entries':[{'type':'minecraft:alternatives','children':[{'type':'minecraft:item','name':'bunnidogs:'+name,'conditions':[{'condition':'minecraft:match_tool','predicate':{'predicates':{'minecraft:enchantments':[{'enchantments':'minecraft:silk_touch','levels':{'min':1}}]}}}]},{'type':'minecraft:item','name':'bunnidogs:'+drop,'functions':[{'function':'minecraft:apply_bonus','enchantment':'minecraft:fortune','formula':'minecraft:ore_drops'},{'function':'minecraft:explosion_decay'}]}]}]}]
 write(D,Path('loot_table/blocks')/(name+'.json'),{'type':'minecraft:block','pools':pools})
# Moon mining: native tool gates and dropped-item resistance, shared with Blastmaw's narrow calculator exception.
write(D,Path('tags/block/needs_astralite_tool.json'),{'values':['bunnidogs:'+i for i in ['moonstone','astralite_ore','cinderite_ore']]})
write(D,Path('tags/block/incorrect_for_astralite_tool.json'),{'values':[]})
write(D,Path('tags/damage_type/moon_material_hazards.json'),{'values':['#minecraft:is_fire','#minecraft:is_explosion']})
for tier in ['wooden','gold','stone','iron','diamond','netherite']:
 path=RES/'data/minecraft/tags/block'/('incorrect_for_'+tier+'_tool.json');obj=json.loads(path.read_text()) if path.exists() else {'replace':False,'values':[]};obj['values']=list(dict.fromkeys(obj['values']+['#bunnidogs:needs_astralite_tool']));write(RES,path.relative_to(RES),obj)
# Blue-black metallic surfaces with copper seams and pale crystalline studs; native armor UVs.
for layer in ['humanoid','humanoid_leggings']:
 base=Image.open(io.BytesIO(z.read('assets/minecraft/textures/entity/equipment/'+layer+'/netherite.png'))).convert('RGBA')
 for x in range(base.width):
  for y in range(base.height):
   r,g,b,a=base.getpixel((x,y))
   if a:base.putpixel((x,y),(min(255,int(r*.75)+12),min(255,int(g*.9)+18),min(255,int(b*1.15)+25),a))
 d=ImageDraw.Draw(base);d.line((20,20,27,20),fill='#c59a73');d.line((20,27,27,27),fill='#c59a73');d.polygon([(24,21),(26,24),(24,26),(22,24)],fill='#aff0e6')
 path=A/'textures/entity/equipment'/layer/'astralite.png';path.parent.mkdir(parents=True,exist_ok=True);base.save(path)
write(A,Path('equipment/astralite.json'),{'layers':{layer:[{'texture':'bunnidogs:astralite'}] for layer in ['humanoid','humanoid_leggings']}})
for variant in ['heartbound','sovereign']:write(A,Path('equipment')/(variant+'.json'),{'layers':{layer:[{'texture':'bunnidogs:astralite'},{'texture':'bunnidogs:'+variant}] for layer in ['humanoid','humanoid_leggings']}})
names=allgear+['astralite_shard','raw_cinderite','cinderite_ingot','astralite_ingot','astralite_upgrade'];sheet=Image.new('RGB',(1000,450),'#252c39');sd=ImageDraw.Draw(sheet)
for i,name in enumerate(names):
 im=Image.new('RGBA',(16,16));d=ImageDraw.Draw(im)
 if name in allgear:
  part=name.removeprefix('astralite_');im=Image.open(io.BytesIO(z.read('assets/minecraft/textures/item/netherite_'+part+'.png'))).convert('RGBA')
  for x in range(16):
   for y in range(16):
    r,g,b,a=im.getpixel((x,y))
    if a:im.putpixel((x,y),(min(255,int(r*.75)+12),min(255,int(g*.9)+18),min(255,int(b*1.15)+25),a))
  d=ImageDraw.Draw(im);d.line((6,6,9,6),fill='#c49a73');d.point((8,7),fill='#aff0e6')
 elif name=='astralite_upgrade':d.rectangle((3,1,12,14),fill='#595478',outline='#c49a73');d.polygon([(8,3),(11,7),(8,12),(5,7)],fill='#aff0e6')
 elif name.endswith('ingot'):d.polygon([(2,6),(10,3),(14,6),(14,10),(6,13),(2,10)],fill='#83c6cb' if name=='astralite_ingot' else '#d59468',outline='#f1dab2');d.line((2,6,6,9,14,6),fill='#fff3ce')
 else:d.polygon([(8,1),(13,5),(11,12),(5,15),(2,10),(4,4)],fill='#aff0e6' if name=='astralite_shard' else '#cc885f',outline='#f1dab2')
 im.save(A/'textures/item'/f'{name}.png');write(A,Path('models/item')/(name+'.json'),{'parent':'minecraft:item/handheld' if name in ['astralite_'+t for t in tools] else 'minecraft:item/generated','textures':{'layer0':'bunnidogs:item/'+name}});write(A,Path('items')/(name+'.json'),{'model':{'type':'minecraft:model','model':'bunnidogs:item/'+name}})
 x=20+i%7*140;y=20+i//7*145;big=im.resize((96,96),Image.Resampling.NEAREST);sheet.paste(big,(x,y),big);sd.text((x,y+100),name.replace('_',' '),fill='white')
# Artifact icons visibly retain the new foundation as well as their old identifying inlays.
for part in parts:
 im=Image.open(A/'textures/item'/f'astralite_{part}.png').convert('RGBA');d=ImageDraw.Draw(im);d.polygon([(8,7),(6,6),(5,8),(8,11),(11,8),(10,6)],fill='#ed9bb2');im.save(A/'textures/item'/f'heartbound_{part}.png')
 im=im.resize((32,32),Image.Resampling.NEAREST);d=ImageDraw.Draw(im);d.polygon([(16,5),(20,10),(16,15),(12,10)],fill='#fff0c1',outline='#b38858');d.line((11,3,16,1,21,3),fill='#f9d68e',width=2);im.save(A/'textures/item'/f'sovereign_{part}.png')
sheet.save(ROOT/'art/astralite_preview.png')
en={'block.bunnidogs.moonstone':'Moonstone','block.bunnidogs.astralite_ore':'Astralite Ore','block.bunnidogs.cinderite_ore':'Cinderite Ore','item.bunnidogs.astralite_shard':'Astralite Shard','item.bunnidogs.raw_cinderite':'Raw Cinderite','item.bunnidogs.cinderite_ingot':'Cinderite Ingot','item.bunnidogs.astralite_ingot':'Astralite Ingot','item.bunnidogs.astralite_upgrade':'Astralite Upgrade Template','tooltip.bunnidogs.astralite_upgrade':'Netherite + Astralite Ingot · Smithing Table','tooltip.bunnidogs.vitality_artifact':'Smithing upgrade for Astralite armor · +1.5 hearts per piece.','tooltip.bunnidogs.heartbound':'Heartbound Astralite · +1.5 hearts · Lifebinder losses recover independently.'}
es={'block.bunnidogs.moonstone':'Roca lunar','block.bunnidogs.astralite_ore':'Mena de astralita','block.bunnidogs.cinderite_ore':'Mena de cinerita','item.bunnidogs.astralite_shard':'Fragmento de astralita','item.bunnidogs.raw_cinderite':'Cinerita en bruto','item.bunnidogs.cinderite_ingot':'Lingote de cinerita','item.bunnidogs.astralite_ingot':'Lingote de astralita','item.bunnidogs.astralite_upgrade':'Plantilla de mejora de astralita','tooltip.bunnidogs.astralite_upgrade':'Netherita + lingote de astralita · Mesa de herrería','tooltip.bunnidogs.vitality_artifact':'Mejora de herrería para armaduras de astralita · +1.5 corazones por pieza.','tooltip.bunnidogs.heartbound':'Astralita vital · +1.5 corazones · Las pérdidas vitales se recuperan por separado.'}
for p,e,s in [('helmet','Helmet','Casco'),('chestplate','Chestplate','Pechera'),('leggings','Leggings','Grebas'),('boots','Boots','Botas'),('sword','Sword','Espada'),('pickaxe','Pickaxe','Pico'),('axe','Axe','Hacha'),('shovel','Shovel','Pala'),('hoe','Hoe','Azada')]:
 en['item.bunnidogs.astralite_'+p]='Astralite '+e;es['item.bunnidogs.astralite_'+p]=s+' de astralita'
for p,e,s in [('helmet','Helmet','Casco vital'),('chestplate','Chestplate','Pechera vital'),('leggings','Leggings','Grebas vitales'),('boots','Boots','Botas vitales')]:
 en['item.bunnidogs.heartbound_'+p]='Heartbound Astralite '+e;es['item.bunnidogs.heartbound_'+p]=s+' de astralita'
for locale,new in [('en_us',en),('es_pe',es)]:
 p=A/'lang'/f'{locale}.json';obj=json.loads(p.read_text());obj.update(new);p.write_text(json.dumps(obj,indent=2,ensure_ascii=False)+'\n')
