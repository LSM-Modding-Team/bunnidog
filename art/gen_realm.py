"""Great Warren blocks/items/recipes and original pixel art. Locale prose is maintained separately."""
from pathlib import Path
import json,random,math
from PIL import Image,ImageDraw
ROOT=Path(__file__).resolve().parents[1];A=ROOT/'src/main/resources/assets/bunnidogs';D=ROOT/'src/main/resources/data/bunnidogs'
def write(root,path,obj):
 p=root/path;p.parent.mkdir(parents=True,exist_ok=True);p.write_text(json.dumps(obj,indent=2)+'\n')
def tex(name,kind):
 rng=random.Random(name);im=Image.new('RGBA',(32,32));d=ImageDraw.Draw(im)
 if kind=='eye':
  d.ellipse((3,8,29,25),fill='#8264b7',outline='#dac892',width=2);d.ellipse((9,9,24,24),fill='#3e9d9d',outline='#b6e6c8',width=2);d.ellipse((14,10,19,23),fill='#232033');d.point((12,12),fill='white')
 elif kind=='lance':
  d.line((5,29,23,11),fill='#453a55',width=5);d.line((6,28,23,11),fill='#d0b978',width=2);d.polygon([(13,11),(23,1),(31,1),(30,10),(21,20)],fill='#79c1c6',outline='#e1f4d5');d.line((17,15,28,4),fill='#f4fff0',width=2);d.polygon([(10,18),(14,13),(19,18),(15,22)],fill='#b88ecb')
 elif kind=='harness':
  d.polygon([(15,10),(8,3),(2,5),(2,24),(7,29),(13,22),(15,15)],fill='#e0d1af',outline='#826959');d.polygon([(17,10),(24,3),(30,5),(30,24),(25,29),(19,22),(17,15)],fill='#e0d1af',outline='#826959')
  for x in [4,8,22,26]:d.line((x,10,x,23),fill='#a99075')
  d.rectangle((13,10,19,21),fill='#665269',outline='#bd9873');d.ellipse((14,12,18,16),fill='#91d6d7')
 elif kind=='cell':
  d.polygon([(10,5),(22,5),(26,10),(26,24),(22,29),(10,29),(6,24),(6,10)],fill='#a376cc',outline='#e8cda0');d.rectangle((11,8,21,25),fill='#75ced2');d.line((13,10,13,22),fill='#ebffdf',width=2)
 else:
  colors={'apex_marrow':('#eadbb4','#866852'),'prism_lens':('#8fd4d7','#426d95'),'chorus_heart':('#c68ace','#67436e'),'volatile_heart':('#ed9e4c','#8d3a3a'),'heart_spool':('#c97587','#673b65'),'aerial_membrane':('#d8ccb3','#826c61')}
  c,b=colors[name]
  if name=='apex_marrow':
   d.line((7,25,24,7),fill=c,width=8)
   for x,y in [(7,25),(24,7)]:d.ellipse((x-5,y-5,x+5,y+5),fill=c,outline=b)
  elif name=='prism_lens':d.polygon([(16,2),(28,10),(26,23),(16,30),(4,22),(3,9)],fill=c,outline=b);d.line((6,10,16,5,24,11,16,26,6,20,6,10),fill='#e0f6e6',width=2)
  elif name=='aerial_membrane':d.polygon([(3,5),(17,10),(29,3),(26,24),(17,29),(6,24)],fill=c,outline=b);d.line((3,5,17,29,17,10,29,3),fill=b,width=2)
  elif name=='heart_spool':
   d.rectangle((7,7,25,25),fill=b);d.ellipse((5,3,27,11),fill=c,outline='#ebc6a7');d.ellipse((5,22,27,30),fill=c,outline='#ebc6a7')
   for y in range(12,23,3):d.line((7,y,25,y-2),fill=c,width=2)
  else:
   d.polygon([(16,29),(3,15),(4,7),(11,4),(16,9),(22,4),(29,8),(29,16)],fill=c,outline=b);d.line((8,10,12,9,16,15,24,11),fill='#fbe4ae',width=2)
 p=A/'textures/item'/f'{name}.png';p.parent.mkdir(parents=True,exist_ok=True);im.save(p)
 return im
icons={}
for name,kind in [('resonant_eye','eye'),('prism_lance','lance'),('prism_cell','cell'),('apex_wing_harness','harness')]+[(n,'material') for n in ['apex_marrow','prism_lens','chorus_heart','volatile_heart','heart_spool','aerial_membrane']]:
 icons[name]=tex(name,kind);write(A,Path('models/item')/(name+'.json'),{'parent':'minecraft:item/handheld' if kind=='lance' else 'minecraft:item/generated','textures':{'layer0':'bunnidogs:item/'+name}});write(A,Path('items')/(name+'.json'),{'model':{'type':'minecraft:model','model':'bunnidogs:item/'+name}})
for name in ['warren_anchor','warren_anchor_active','warren_ward','rootstone']:
 rng=random.Random(name);im=Image.new('RGB',(16,16));d=ImageDraw.Draw(im)
 for y in range(16):
  for x in range(16):
   n=rng.randint(-9,9);base=(48,38,62) if 'anchor' in name else (65,78,75);im.putpixel((x,y),tuple(c+n for c in base))
 d.rectangle((1,1,14,14),outline='#aa7958');d.rectangle((3,3,12,12),outline='#e0bb77');d.ellipse((5,5,10,10),fill='#91e9ce' if 'active' in name or name=='warren_ward' else '#44344f',outline='#e0d5a4')
 if name=='rootstone':
  d.rectangle((0,0,15,15),fill='#425954');d.line((0,0,15,0),fill='#293f3d');d.line((0,8,15,8),fill='#293f3d');d.line((8,0,8,8),fill='#879879');d.line((4,8,4,15),fill='#879879');d.line((0,15,15,15),fill='#9ca188')
 if name=='warren_ward':d.line((8,4,8,12),fill='#e4dabb');d.line((4,8,12,8),fill='#e4dabb')
 p=A/'textures/block'/f'{name}.png';p.parent.mkdir(parents=True,exist_ok=True);im.save(p);icons[name]=im
 write(A,Path('models/block')/(name+'.json'),{'parent':'minecraft:block/cube_all','textures':{'all':'bunnidogs:block/'+name}})
for name in ['warren_anchor','warren_ward','rootstone']:
 variants={'':{'model':'bunnidogs:block/'+name}} if name!='warren_anchor' else {'active=false':{'model':'bunnidogs:block/warren_anchor'},'active=true':{'model':'bunnidogs:block/warren_anchor_active'}}
 write(A,Path('blockstates')/(name+'.json'),{'variants':variants});write(A,Path('models/item')/(name+'.json'),{'parent':'bunnidogs:block/'+name});write(A,Path('items')/(name+'.json'),{'model':{'type':'minecraft:model','model':'bunnidogs:item/'+name}})
 write(D,Path('loot_table/blocks')/(name+'.json'),{'type':'minecraft:block','pools':[{'rolls':1,'entries':[{'type':'minecraft:item','name':'bunnidogs:'+name}],'conditions':[{'condition':'minecraft:survives_explosion'}]}]})
def recipe(name,pattern,key,result,count=1):
 write(D,Path('recipe')/(name+'.json'),{'type':'minecraft:crafting_shaped','category':'equipment','pattern':pattern,'key':key,'result':{'id':result,'count':count}})
 ingredients=[v if isinstance(v,str) else v['items'] for v in key.values() if not isinstance(v,str) or not v.startswith('#')]
 write(D,Path('advancement/recipes/great_warren')/(name+'.json'),{'parent':'minecraft:recipes/root','criteria':{'has_material':{'trigger':'minecraft:inventory_changed','conditions':{'items':[{'items':ingredients[0]}]}}},'requirements':[['has_material']],'rewards':{'recipes':['bunnidogs:'+name]}})
recipe('rootstone',['TTT','TMT','TTT'],{'T':'minecraft:tuff','M':'bunnidogs:apex_marrow'},'bunnidogs:rootstone',8)
recipe('warren_anchor',['OCO','CEC','OCO'],{'O':'minecraft:crying_obsidian','C':'minecraft:copper_block','E':'minecraft:ender_pearl'},'bunnidogs:warren_anchor')
ground_parts={letter:{'neoforge:ingredient_type':'neoforge:components','items':'bunnidogs:'+item,'components':{'bunnidogs:ground_remnant':True}} for letter,item in zip('FPLBH',['apex_fang','prism_lens','chorus_heart','volatile_heart','heart_spool'])}
ground_parts.update({'E':'minecraft:ender_eye','A':'bunnidogs:aerial_membrane','D':'bunnidogs:starlit_dust','S':'minecraft:netherite_scrap'})
recipe('resonant_eye',['FPL','BEH','ADS'],ground_parts,'bunnidogs:resonant_eye')
recipe('warren_ward',['OHO','CMS','OAO'],{'O':'minecraft:crying_obsidian','H':'bunnidogs:chorus_heart','C':'bunnidogs:heart_spool','M':'bunnidogs:apex_marrow','S':'bunnidogs:aerial_membrane','A':'minecraft:amethyst_block'},'bunnidogs:warren_ward')
recipe('apex_wing_harness',['M M','NSN','M M'],{'M':'bunnidogs:aerial_membrane','N':'minecraft:netherite_ingot','S':'bunnidogs:heart_spool'},'bunnidogs:apex_wing_harness')
recipe('prism_lance',['  L',' M ','N  '],{'L':'bunnidogs:prism_lens','M':'bunnidogs:aerial_membrane','N':'minecraft:netherite_ingot'},'bunnidogs:prism_lance')
recipe('prism_cell',[' A ','ACA',' A '],{'A':'minecraft:amethyst_shard','C':'minecraft:copper_ingot'},'bunnidogs:prism_cell',8)
recipe('volatile_prism_cells',[' A ','ACA',' V '],{'A':'minecraft:amethyst_shard','C':'minecraft:copper_ingot','V':'bunnidogs:volatile_heart'},'bunnidogs:prism_cell',24)
write(D,Path('worldgen/structure/great_ruin.json'),{'type':'bunnidogs:great_ruin','biomes':'#bunnidogs:great_warren','step':'surface_structures','spawn_overrides':{},'terrain_adaptation':'none'})
write(D,Path('worldgen/structure_set/great_ruins.json'),{'structures':[{'structure':'bunnidogs:great_ruin','weight':1}],'placement':{'type':'minecraft:random_spread','spacing':20,'separation':8,'salt':541208179}})
write(D,Path('damage_type/prism_lance.json'),{'message_id':'bunnidogs.prism_lance','scaling':'never','exhaustion':.1})
def pool(item,lo,hi):return {'rolls':1,'entries':[{'type':'minecraft:item','name':item,'functions':[{'function':'minecraft:set_count','count':{'type':'minecraft:uniform','min':lo,'max':hi}}]}]}
write(D,Path('loot_table/chests/great_ruin.json'),{'type':'minecraft:chest','random_sequence':'bunnidogs:chests/great_ruin','pools':[pool('minecraft:diamond',16,32),pool('minecraft:netherite_ingot',2,6),pool('minecraft:enchanted_golden_apple',2,4),pool('minecraft:emerald',24,48),pool('bunnidogs:mutant_marrow',12,24),pool('bunnidogs:starlit_dust',16,32),pool('bunnidogs:prism_cell',16,32),pool('bunnidogs:prism_lens',2,4),pool('bunnidogs:aerial_membrane',2,4),pool('minecraft:golden_carrot',16,32)]})
# A pixel-art contact sheet; actual terrain preview is generated by the native fixture.
preview=Image.new('RGB',(840,425),(38,42,49));draw=ImageDraw.Draw(preview);draw.text((20,12),'Great Warren - original textures and equipment',fill='white')
for i,(name,im) in enumerate(icons.items()):
 x=20+(i%6)*138;y=50+(i//6)*120;preview.paste(im.resize((80,80),Image.Resampling.NEAREST),(x,y),im.resize((80,80),Image.Resampling.NEAREST) if im.mode=='RGBA' else None);draw.text((x,y+84),name.replace('_',' '),fill='white')
preview.save(ROOT/'art/realm_items_preview.png')

# Native glider equipment model uses a 64 x 32 atlas. Authored feather bands are original.
wings=Image.new('RGBA',(64,32),(203,187,153,255));wd=ImageDraw.Draw(wings)
for x in range(64):
 for y in range(32):
  n=random.Random(x*192+y).randint(-7,7);base=(216,202,173) if y<22 else (133,109,89)
  wings.putpixel((x,y),tuple(c+n for c in base)+(255,))
for x in range(0,64,3):wd.line((x,2,x+1,22),fill='#9d876e');wd.line((x+1,3,x+2,18),fill='#eee2c5')
wp=A/'textures/entity/equipment/wings/apex_wing_harness.png';wp.parent.mkdir(parents=True,exist_ok=True);wings.save(wp)
write(A,Path('equipment/apex_wing_harness.json'),{'layers':{'wings':[{'texture':'bunnidogs:apex_wing_harness','use_player_texture':False}]}})

# The current rewards extension overrides the initial expedition recipes and stock.
import runpy
runpy.run_path(str(ROOT/"art/gen_apex_rewards.py"))
