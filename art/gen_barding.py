"""Bake approved fitted armour into native animated meshes and original atlases.
The preview below reads the exported binaries, not the in-memory concept.
"""
from pathlib import Path
import struct,json,io
import numpy as np
from PIL import Image,ImageDraw
import preview_barding_design as design
from gen_bunnidog import PARTS
from preview_bunnidog import world_faces,render,font
ROOT=Path(__file__).resolve().parents[1];R=ROOT/'src/main/resources';A=R/'assets/bunnidogs';D=R/'data/bunnidogs'
def write(path,obj):path.parent.mkdir(parents=True,exist_ok=True);path.write_text(json.dumps(obj,indent=2,ensure_ascii=False)+'\n')
def tag(name,ids):
 p=R/'data/minecraft/tags/item/enchantable'/f'{name}.json';v=json.loads(p.read_text()) if p.exists() else {'replace':False,'values':[]};v['values']=list(dict.fromkeys(v['values']+ids));write(p,v)
def utf(f,s):b=s.encode();f.write(struct.pack('>H',len(b))+b)
def readmesh(path):
 f=io.BytesIO(path.read_bytes())
 def unpack(fmt):return struct.unpack(fmt,f.read(struct.calcsize(fmt)))
 def string():return f.read(unpack('>H')[0]).decode()
 magic,n=unpack('>ii');assert magic==0x42444d32
 bones={p['name']:p for p in PARTS};out=[]
 for _ in range(n):
  name=string();parent=string();count=unpack('>i')[0];pivot=np.array(bones[name]['pivot'])
  for i in range(count):
   rows=np.array([unpack('>8f') for _ in range(4)]);norm=np.array(unpack('>3f'))*(1,-1,1)
   points=rows[:,:3]*(1,-1,1)+pivot;uv=rows[:,3:5]*1024;normals=rows[:,5:]*(1,-1,1)
   assert np.isfinite(rows).all();out.append((points,norm,uv,(0,0),normals))
 assert not f.read();return out
fur=Image.open(A/'textures/entity/bunnidog_tan.png').convert('RGB')
board=Image.new('RGB',(1530,1120),design.BG);dr=ImageDraw.Draw(board);dr.text((24,14),'BUNNIDOG ARMOUR — SHIPPED MESH / TEXTURE CHECK',font=font(25),fill='#28282d')
material_labels={'leather':('Leather Bunnidog Barding','Armadura de cuero para bunnidog'),'iron':('Iron Bunnidog Barding','Armadura de hierro para bunnidog'),'diamond':('Diamond Bunnidog Barding','Armadura de diamante para bunnidog'),'netherite':('Netherite Bunnidog Barding','Armadura de netherita para bunnidog'),'astralite':('Astralite Bunnidog Barding','Armadura de astralita para bunnidog')}
allids=[]
for tier,(name,*_) in enumerate(design.TIERS):
 name=name.lower();id=name+'_bunnidog_barding';allids.append('bunnidogs:'+id)
 design.EXPORT_FACES.clear();design.study(tier)
 geometry=list(design.EXPORT_FACES);path=A/'models/entity'/f'barding_{name}.mesh';path.parent.mkdir(parents=True,exist_ok=True)
 with path.open('wb') as f:
  f.write(struct.pack('>ii',0x42444d32,len(PARTS)))
  for bone in PARTS:
   utf(f,bone['name']);utf(f,bone['parent'] or 'root');faces=[face for parent,face in geometry if parent==bone['name']];f.write(struct.pack('>i',len(faces)))
   for points,normal,uv,size,normals in faces:
    for point,tex,n in zip(points,uv,normals):
     local=(point-np.array(bone['pivot']))*(1,-1,1);n=n*(1,-1,1);f.write(struct.pack('>8f',*local,*(np.array(tex)/1024),*n))
    f.write(struct.pack('>3f',*(normal*(1,-1,1))))
 atlas=design.material_atlas(tier,fur);atlas.save(A/'textures/entity'/f'barding_{name}.png')
 # A proper miniature suit icon, not a player chestplate recolour.
 icon=Image.new('RGBA',(32,32));d=ImageDraw.Draw(icon);base=tuple(design.TIERS[tier][1]);edge=tuple(design.TIERS[tier][2]);d.polygon([(7,5),(13,2),(20,4),(24,9),(27,20),(21,27),(9,26),(4,19)],fill=base,outline=edge);d.line((6,12,25,12),fill='#273344',width=2);d.line((10,16,23,17,20,23,11,22,10,16),fill=edge,width=2);d.rectangle((9,8,12,10),fill='#182433');d.rectangle((18,8,21,10),fill='#182433');icon.save(A/'textures/item'/f'{id}.png')
 write(A/'models/item'/f'{id}.json',{'parent':'minecraft:item/generated','textures':{'layer0':'bunnidogs:item/'+id}});write(A/'items'/f'{id}.json',{'model':{'type':'minecraft:model','model':'bunnidogs:item/'+id}})
 if tier==0:rec={'type':'minecraft:crafting_shaped','category':'equipment','pattern':['L L','LNL','LSL'],'key':{'L':'minecraft:leather','N':'minecraft:iron_nugget','S':'minecraft:string'},'result':{'id':'bunnidogs:'+id}}
 else:
  additions=['','minecraft:iron_block','minecraft:diamond_block','minecraft:netherite_ingot','bunnidogs:astralite_plating'];rec={'type':'minecraft:smithing_transform','base':'bunnidogs:'+list(material_labels)[tier-1]+'_bunnidog_barding','addition':additions[tier],'result':{'id':'bunnidogs:'+id}}
  if tier>=3:rec['template']='minecraft:netherite_upgrade_smithing_template' if tier==3 else 'bunnidogs:astralite_upgrade'
 write(D/'recipe'/f'{id}.json',rec)
 write(D/'advancement/recipes/barding'/f'{id}.json',{'parent':'minecraft:recipes/root','criteria':{'material':{'trigger':'minecraft:inventory_changed','conditions':{'items':[{'items':'minecraft:leather' if tier==0 else rec['base']}]}}},'requirements':[['material']],'rewards':{'recipes':['bunnidogs:'+id]}})
 mesh=readmesh(path);image=render(world_faces()+mesh,np.asarray(atlas,dtype=float),yaw=-55,elevation=16,width=510,height=470,zoom=23);x=(tier%3)*510;y=45+(tier//3)*525;board.paste(image,(x,y));dr.text((x+20,y+472),material_labels[name][0],font=font(18),fill='#292b30')
 if tier==4:
  image=render(world_faces()+mesh,np.asarray(atlas,dtype=float),yaw=0,elevation=16,width=510,height=470,zoom=23);board.paste(image,(1020,570));dr.text((1040,1044),'Astralite — actual exported visor',font=font(18),fill='#292b30')
for t in ['durability','armor']:tag(t,allids)
for locale,index in [('en_us',0),('es_pe',1)]:
 p=A/'lang'/f'{locale}.json';v=json.loads(p.read_text())
 for name,labels in material_labels.items():v['item.bunnidogs.'+name+'_bunnidog_barding']=labels[index]
 v['message.bunnidogs.barding.refused']='Only calm, bonded adults can change armour.' if index==0 else 'Solo los adultos vinculados y tranquilos pueden cambiar de armadura.'
 v['tooltip.bunnidogs.barding.defense']='Armour +%s · Toughness +%s' if index==0 else 'Armadura +%s · Dureza +%s'
 v['tooltip.bunnidogs.barding.power']='Max health +%s%% · Damage +%s%%' if index==0 else 'Salud máxima +%s%% · Daño +%s%%'
 v['care.bunnidogs.barding.remove']='Remove armour' if index==0 else 'Quitar armadura'
 write(p,v)
board.save(ROOT/'art/barding_shipped_preview.png');print('Exported five native animated armour meshes and atlases; preview reads those binaries.')
