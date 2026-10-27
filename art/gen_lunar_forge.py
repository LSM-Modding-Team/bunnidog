"""Lunar mining chain, fortified Crown masonry and expedition supply tables."""
from pathlib import Path
import json,random
from PIL import Image,ImageDraw
ROOT=Path(__file__).resolve().parents[1];R=ROOT/'src/main/resources';D=R/'data/bunnidogs';A=R/'assets/bunnidogs'
def write(root,path,obj):
 p=root/path;p.parent.mkdir(parents=True,exist_ok=True);p.write_text(json.dumps(obj,indent=2,ensure_ascii=False)+'\n')
def tag(ns,name,values):
 p=R/'data'/ns/'tags'/name;old=json.loads(p.read_text()) if p.exists() else {'replace':False,'values':[]};old['values']=list(dict.fromkeys(old['values']+values));write(R/'data'/ns/'tags',name,old)
def recipe(name,pattern,key,count=1):
 write(D,'recipe/'+name+'.json',{'type':'minecraft:crafting_shaped','category':'misc','pattern':pattern,'key':key,'result':{'id':'bunnidogs:'+name,'count':count}});unlock(name,next(iter(key.values())))
def unlock(name,item):write(D,'advancement/recipes/lunar/'+name+'.json',{'parent':'minecraft:recipes/root','criteria':{'material':{'trigger':'minecraft:inventory_changed','conditions':{'items':[{'items':item}]}}},'requirements':[['material']],'rewards':{'recipes':['bunnidogs:'+name]}})
MAPPING={'bunnidogs:rootstone':'lunar_rootstone','bunnidogs:crown_rootlight':'lunar_rootlight','minecraft:tuff_bricks':'lunar_bricks','minecraft:deepslate_tiles':'lunar_tiles','minecraft:calcite':'pale_lunar_tiles','minecraft:dark_oak_planks':'cinderroot_beams','minecraft:polished_basalt':'lunar_tiles','minecraft:mud_bricks':'lunar_bricks','minecraft:deepslate_bricks':'lunar_tiles','minecraft:dark_oak_log':'cinderroot_beams','minecraft:raw_copper_block':'cinderroot_beams','minecraft:cut_copper':'cinderroot_beams','minecraft:copper_grate':'runic_lunar_glass','minecraft:tuff_brick_stairs':'lunar_bricks_stairs','minecraft:dark_oak_stairs':'lunar_tiles_stairs','minecraft:dark_oak_fence':'cinderroot_fence'}
for c in ['light_gray','cyan','light_blue','orange']:MAPPING['minecraft:'+c+'_stained_glass']='runic_lunar_glass'
def supplies():
 def pool(item,lo,hi):
  funcs=[{'function':'minecraft:set_count','count':{'type':'minecraft:uniform','min':lo,'max':hi}}]
  if item=='minecraft:firework_rocket':funcs.insert(0,{'function':'minecraft:set_components','components':{'minecraft:fireworks':{'flight_duration':1,'explosions':[]}}})
  return {'rolls':1,'entries':[{'type':'minecraft:item','name':item,'functions':funcs}]}
 tables={
 'crown_supplies':[('minecraft:golden_apple',64,128),('minecraft:enchanted_golden_apple',16,32),('minecraft:experience_bottle',128,256),('minecraft:ender_pearl',32,64),('minecraft:firework_rocket',64,128),('minecraft:diamond_block',4,8),('minecraft:netherite_ingot',4,8),('minecraft:totem_of_undying',2,3),('minecraft:amethyst_block',64,128),('minecraft:gunpowder',128,256),('minecraft:fire_charge',32,64),('minecraft:copper_block',32,64),('minecraft:gold_block',16,32)],
 'crown_wing':[('minecraft:golden_apple',128,128),('minecraft:diamond_block',24,32),('minecraft:netherite_ingot',32,48),('minecraft:enchanted_golden_apple',48,64),('minecraft:totem_of_undying',4,6),('minecraft:experience_bottle',256,256),('minecraft:amethyst_block',128,128),('minecraft:gunpowder',384,384),('minecraft:fire_charge',128,128),('minecraft:copper_block',64,64),('minecraft:gold_block',64,64)],
 'crown_vault':[('minecraft:diamond_block',64,64),('minecraft:netherite_block',32,48),('minecraft:enchanted_golden_apple',128,128),('minecraft:totem_of_undying',6,8),('minecraft:experience_bottle',256,256),('minecraft:amethyst_block',192,192),('minecraft:gunpowder',256,256),('minecraft:copper_block',64,64),('minecraft:gold_block',64,64),('minecraft:fire_charge',128,128)]}
 for name,stock in tables.items():
  slots=sum((hi+63)//64 if item not in ['minecraft:totem_of_undying'] else hi for item,lo,hi in stock)
  # Pearls stack to sixteen, unlike the other bulk materials.
  slots+=sum((hi+15)//16-(hi+63)//64 for item,lo,hi in stock if item=='minecraft:ender_pearl')
  assert slots<=27,(name,slots)
  write(D,'loot_table/chests/'+name+'.json',{'type':'minecraft:chest','random_sequence':'bunnidogs:chests/'+name,'pools':[pool(*s) for s in stock]})
def generate():
 names={'astralite_concentrate':('Astralite Concentrate','Concentrado de astralita'),'cinderite_billet':('Cinderite Billet','Tocho de cinerita'),'compressed_moonstone':('Compressed Moonstone','Roca lunar comprimida'),'lunar_binder':('Lunar Binder','Aglutinante lunar'),'astralite_bloom':('Astralite Bloom','Masa de astralita'),'astralite_plating':('Astralite Plating','Placas de astralita')}
 for out,source in [('astralite_concentrate','astralite_shard'),('cinderite_billet','cinderite_ingot'),('compressed_moonstone','moonstone')]:recipe(out,['XXX','XXX','XXX'],{'X':'bunnidogs:'+source})
 recipe('lunar_binder',[' L ',' M ',' C '],{'L':'bunnidogs:prism_lens','M':'bunnidogs:apex_marrow','C':'bunnidogs:compressed_moonstone'})
 recipe('astralite_bloom',['ABA','BMB','ABA'],{'A':'bunnidogs:astralite_concentrate','B':'bunnidogs:cinderite_billet','M':'bunnidogs:lunar_binder'})
 write(D,'recipe/astralite_ingot.json',{'type':'minecraft:blasting','category':'misc','ingredient':'bunnidogs:astralite_bloom','result':{'id':'bunnidogs:astralite_ingot'},'experience':8,'cookingtime':400});unlock('astralite_ingot','bunnidogs:astralite_bloom')
 recipe('astralite_plating',['ABA','BMB','ABA'],{'A':'bunnidogs:astralite_ingot','B':'bunnidogs:cinderite_billet','M':'bunnidogs:aerial_membrane'})
 for part in ['helmet','chestplate','leggings','boots']:
  p=D/'recipe'/('astralite_'+part+'.json');obj=json.loads(p.read_text());obj['addition']='bunnidogs:astralite_plating';write(D,'recipe/'+p.name,obj)
 base=['lunar_rootstone','lunar_bricks','pale_lunar_tiles','lunar_tiles','cinderroot_beams','runic_lunar_glass','lunar_rootlight','cinderroot_fence']
 english=['Lunar Rootstone','Lunar Rootstone Bricks','Pale Lunar Tiles','Dark Lunar Tiles','Cinderroot Beams','Runic Lunar Glass','Lunar Rootlight','Cinderroot Fence'];spanish=['Raíz pétrea lunar','Ladrillos de raíz lunar','Baldosas lunares claras','Baldosas lunares oscuras','Vigas de raíz de cinerita','Vidrio lunar rúnico','Luz de raíz lunar','Valla de raíz de cinerita']
 recipe('lunar_rootstone',['RMR','MCM','RMR'],{'R':'bunnidogs:rootstone','M':'bunnidogs:moonstone','C':'bunnidogs:cinderite_ingot'},16)
 for name,pattern,keys,count in [('lunar_bricks',['XX','XX'],{'X':'bunnidogs:lunar_rootstone'},4),('pale_lunar_tiles',['XC','CX'],{'X':'bunnidogs:lunar_rootstone','C':'minecraft:quartz'},4),('lunar_tiles',['XC','CX'],{'X':'bunnidogs:lunar_rootstone','C':'minecraft:obsidian'},4),('cinderroot_beams',['XC','XC'],{'X':'bunnidogs:lunar_rootstone','C':'bunnidogs:cinderite_ingot'},8),('runic_lunar_glass',['XGX','GAG','XGX'],{'X':'bunnidogs:lunar_rootstone','G':'minecraft:glass','A':'minecraft:amethyst_shard'},8),('lunar_rootlight',['XAX','AGA','XAX'],{'X':'bunnidogs:lunar_rootstone','A':'minecraft:amethyst_shard','G':'minecraft:glowstone'},8),('cinderroot_fence',['XSX','XSX'],{'X':'bunnidogs:cinderroot_beams','S':'minecraft:iron_nugget'},6)]:recipe(name,pattern,keys,count)
 colours=[(76,86,96),(93,105,115),(184,191,181),(43,52,69),(72,64,61),(87,135,146),(88,115,112),(72,64,61)]
 for idx,name in enumerate(base):
  names[name]=(english[idx],spanish[idx]);rng=random.Random(871+idx);im=Image.new('RGBA',(16,16));dr=ImageDraw.Draw(im)
  for y in range(16):
   for x in range(16):
    noise=rng.randrange(-8,9);im.putpixel((x,y),tuple(max(0,min(255,v+noise)) for v in colours[idx])+(255,))
  if name in ['lunar_bricks','lunar_tiles','pale_lunar_tiles']:
   for y in [0,8]:dr.line((0,y,15,y),fill='#202a35');dr.line((0,y+1,15,y+1),fill='#a8afaa')
   dr.line((7,1,7,7),fill='#283544');dr.line((1,9,1,15),fill='#283544')
  elif 'glass' in name:
   im=Image.new('RGBA',(16,16),(77,127,139,55));dr=ImageDraw.Draw(im);dr.rectangle((0,0,15,15),outline='#718ea2');dr.line((0,8,8,0,15,8,8,15,0,8),fill='#b4e5da',width=1)
  else:
   dr.line((0,3,6,7,10,4,15,10),fill='#222f42',width=2);dr.line((0,2,6,6,10,3,15,9),fill='#899ca7')
  if idx in [0,4,6,7]:dr.line((3,0,3,15),fill='#b8865b');dr.point((4,8),fill='#bef7e1')
  if 'light' in name:dr.polygon([(8,3),(12,8),(8,13),(4,8)],fill='#b2f4df')
  im.save(A/'textures/block'/f'{name}.png')
  write(A,'models/block/'+name+'.json',{'parent':'minecraft:block/cube_all','textures':{'all':'bunnidogs:block/'+name},**({'render_type':'minecraft:translucent'} if 'glass' in name else {})})
  write(A,'blockstates/'+name+'.json',{'variants':{'':{'model':'bunnidogs:block/'+name}}})
  variants=[name]
  if name.endswith(('bricks','tiles')):
   for form in ['stairs','slab','wall']:
    out=name+'_'+form;variants.append(out);names[out]=(english[idx]+' '+form.title(),spanish[idx]+' ('+{'stairs':'escaleras','slab':'losa','wall':'muro'}[form]+')')
    tex={'bottom':'bunnidogs:block/'+name,'top':'bunnidogs:block/'+name,'side':'bunnidogs:block/'+name}
    if form=='stairs':
     for suffix,parent in [('', 'stairs'),('_inner','inner_stairs'),('_outer','outer_stairs')]:write(A,'models/block/'+out+suffix+'.json',{'parent':'minecraft:block/'+parent,'textures':tex})
     import zipfile
     z=zipfile.ZipFile(ROOT/'build/moddev/artifacts/minecraft-patched-26.2.0.88.jar');obj=json.loads(z.read('assets/minecraft/blockstates/stone_brick_stairs.json'));obj=json.loads(json.dumps(obj).replace('minecraft:block/stone_brick_stairs','bunnidogs:block/'+out));write(A,'blockstates/'+out+'.json',obj)
     recipe(out,['X  ','XX ','XXX'],{'X':'bunnidogs:'+name},4)
    elif form=='slab':
     for suffix,parent in [('', 'slab'),('_top','slab_top')]:write(A,'models/block/'+out+suffix+'.json',{'parent':'minecraft:block/'+parent,'textures':tex})
     write(A,'blockstates/'+out+'.json',{'variants':{'type=bottom':{'model':'bunnidogs:block/'+out},'type=top':{'model':'bunnidogs:block/'+out+'_top'},'type=double':{'model':'bunnidogs:block/'+name}}});recipe(out,['XXX'],{'X':'bunnidogs:'+name},6)
    else:
     import zipfile
     z=zipfile.ZipFile(ROOT/'build/moddev/artifacts/minecraft-patched-26.2.0.88.jar');obj=json.loads(z.read('assets/minecraft/blockstates/stone_brick_wall.json'));write(A,'blockstates/'+out+'.json',json.loads(json.dumps(obj).replace('minecraft:block/stone_brick_wall','bunnidogs:block/'+out)))
     for suffix in ['post','side','side_tall','inventory']:write(A,'models/block/'+out+'_'+suffix+'.json',{'parent':'minecraft:block/wall_'+suffix,'textures':{'wall':'bunnidogs:block/'+name}})
     recipe(out,['XXX','XXX'],{'X':'bunnidogs:'+name},6);tag('minecraft','block/walls.json',['bunnidogs:'+out])
  for out in variants:
   parent=out+'_inventory' if out.endswith('_wall') else out
   write(A,'models/item/'+out+'.json',{'parent':'bunnidogs:block/'+parent});write(A,'items/'+out+'.json',{'model':{'type':'minecraft:model','model':'bunnidogs:item/'+out}})
   entry={'type':'minecraft:item','name':'bunnidogs:'+out}
   if out.endswith('_slab'):entry['functions']=[{'function':'minecraft:set_count','count':2,'conditions':[{'condition':'minecraft:block_state_property','block':'bunnidogs:'+out,'properties':{'type':'double'}}]}]
   write(D,'loot_table/blocks/'+out+'.json',{'type':'minecraft:block','pools':[{'rolls':1,'entries':[entry]}]})
   tag('minecraft','block/mineable/pickaxe.json',['bunnidogs:'+out]);tag('bunnidogs','block/needs_astralite_tool.json',['bunnidogs:'+out])
 # Native fence blockstate/model shape, matching vanilla properties.
 import zipfile
 z=zipfile.ZipFile(ROOT/'build/moddev/artifacts/minecraft-patched-26.2.0.88.jar');obj=json.loads(z.read('assets/minecraft/blockstates/oak_fence.json'));write(A,'blockstates/cinderroot_fence.json',json.loads(json.dumps(obj).replace('minecraft:block/oak_fence','bunnidogs:block/cinderroot_fence')))
 for suffix in ['post','side','inventory']:write(A,'models/block/cinderroot_fence_'+suffix+'.json',{'parent':'minecraft:block/fence_'+suffix,'textures':{'texture':'bunnidogs:block/cinderroot_beams'}})
 write(A,'models/item/cinderroot_fence.json',{'parent':'bunnidogs:block/cinderroot_fence_inventory'})
 tag('minecraft','block/fences.json',['bunnidogs:cinderroot_fence'])
 write(D,'lunar_masonry.json',{k:'bunnidogs:'+v for k,v in MAPPING.items()})
 for name in list(names):
  if name in base or any(name.startswith(b+'_') for b in base):continue
  im=Image.new('RGBA',(16,16));dr=ImageDraw.Draw(im)
  if name=='astralite_concentrate':
   dr.polygon([(1,12),(4,6),(8,8),(11,3),(15,11),(12,14),(4,14)],fill='#675080',outline='#302a48');dr.polygon([(4,7),(6,12),(8,9)],fill='#b0a6ea');dr.polygon([(11,4),(13,10),(10,11)],fill='#d5eaf6');dr.line((3,13,11,13),fill='#887caf')
  elif name=='cinderite_billet':
   dr.polygon([(1,6),(10,3),(15,6),(15,11),(5,14),(1,11)],fill='#633c35',outline='#302d32');dr.polygon([(2,6),(10,4),(14,6),(5,9)],fill='#d2a36d');dr.polygon([(5,10),(14,7),(14,10),(5,13)],fill='#a86645');dr.line((3,7,10,5),fill='#f0d49d')
  elif name=='compressed_moonstone':
   dr.polygon([(2,4),(10,1),(14,4),(14,12),(6,15),(2,12)],fill='#536379',outline='#2a3043');dr.polygon([(3,4),(10,2),(13,4),(6,7)],fill='#acb6bd');dr.polygon([(7,8),(13,5),(13,11),(7,14)],fill='#71899b');dr.line((4,7,4,11,6,12),fill='#d1d3c7');dr.line((10,7,9,10,12,10),fill='#3b4c60')
  elif name=='lunar_binder':
   dr.polygon([(3,7),(6,4),(10,4),(13,7),(14,12),(11,14),(4,14),(2,12)],fill='#526565',outline='#283b43');dr.rectangle((5,2,10,4),fill='#a2c7bd',outline='#435a60');dr.line((4,9,12,9),fill='#b7e8d5',width=2);dr.rectangle((7,6,9,12),fill='#e2f5d2');dr.line((3,12,11,13),fill='#809693')
  elif name=='astralite_bloom':
   dr.polygon([(1,8),(4,3),(7,4),(10,2),(14,6),(13,11),(9,14),(4,13)],fill='#5c4861',outline='#2c2c42');dr.polygon([(4,4),(7,6),(11,3),(12,6),(9,8),(5,7)],fill='#c193a3');dr.line((3,8,6,10,9,7,12,9),fill='#e9ba79',width=2);dr.line((4,12,9,13,11,11),fill='#8285ba')
  elif name=='astralite_plating':
   for x,y in [(1,7),(3,4),(5,1)]:
    dr.polygon([(x,y+3),(x+6,y),(x+9,y+3),(x+7,y+7),(x+1,y+9)],fill='#50667f',outline='#263347');dr.polygon([(x+1,y+3),(x+6,y+1),(x+8,y+3),(x+2,y+5)],fill='#c0dce1');dr.line((x+2,y+6,x+7,y+4),fill='#ad8b73')
  im.save(A/'textures/item'/f'{name}.png');write(A,'models/item/'+name+'.json',{'parent':'minecraft:item/generated','textures':{'layer0':'bunnidogs:item/'+name}});write(A,'items/'+name+'.json',{'model':{'type':'minecraft:model','model':'bunnidogs:item/'+name}})
 for locale,index in [('en_us',0),('es_pe',1)]:
  p=A/'lang'/f'{locale}.json';obj=json.loads(p.read_text());
  for name,labels in names.items():obj[('block' if name in base or any(name.startswith(b+'_') for b in base) else 'item')+'.bunnidogs.'+name]=labels[index]
  obj['tooltip.bunnidogs.astralite_upgrade']=('Smithing · Tools: Astralite ingot · Armour: Astralite plating' if index==0 else 'Herrería · Herramientas: lingote de astralita · Armadura: placas de astralita')
  obj['message.bunnidogs.crown.fortified']=('Fortified %s loaded structural blocks. Existing stock and progress retained.' if index==0 else 'Se reforzaron %s bloques estructurales cargados. Se conservaron los recursos y el progreso.');p.write_text(json.dumps(obj,indent=2,ensure_ascii=False)+'\n')
 write(D,'tags/block/blastmaw_mineable_moonstone.json',{'values':['bunnidogs:moonstone','bunnidogs:astralite_ore','bunnidogs:cinderite_ore']})
 supplies()
if __name__=='__main__':generate()
