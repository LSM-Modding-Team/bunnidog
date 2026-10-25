"""Preview the actual generated crest boxes on the approved sculpt, plus native dungeon cells."""
from pathlib import Path
import json,sys
import numpy as np
from PIL import Image,ImageDraw
from preview_bunnidog import world_faces,render
ROOT=Path(__file__).resolve().parents[1];A=ROOT/'src/main/resources/assets/bunnidogs'
texture=Image.new('RGBA',(1024,512));texture.paste(Image.open(A/'textures/entity/bunnidog_tan.png'),(0,0));texture.paste(Image.open(A/'textures/entity/crowned_apex.png'),(512,0))
faces=world_faces()
for row in json.loads((ROOT/'art/crown_geometry.json').read_text()):
 x,y,z,w,h,d=row['box'];pivot=(.15,16.3,-1.7) if row['part']=='head' else (0,18.9,1.7)
 lo=np.array((pivot[0]+x,24-(pivot[1]+y+h),pivot[2]+z));hi=lo+np.array((w,h,d))
 for axis in range(3):
  for sign in [-1,1]:
   others=[i for i in range(3) if i!=axis];corners=[]
   for a,b in [(0,0),(1,0),(1,1),(0,1)]:
    p=lo.copy();p[axis]=lo[axis] if sign<0 else hi[axis];p[others[0]]=lo[others[0]] if a==0 else hi[others[0]];p[others[1]]=lo[others[1]] if b==0 else hi[others[1]];corners.append(p)
   normal=np.zeros(3);normal[axis]=sign;normals=np.repeat(normal[None,:],4,axis=0)
   faces.append((np.array(corners),normal,(512+row['uv'],0),(8,8),normals))
img=render(faces,np.asarray(texture,dtype=float),-28,15,680,760,29,background_rgb=(38,47,52));d=ImageDraw.Draw(img);d.text((22,20),'Crowned Apex - generated crests on the actual animal sculpt',fill='white');img.save(ROOT/'art/crowned_apex_preview.png')
p=ROOT/'build/game-test/crown_layout_preview.json'
if p.exists():
 image=Image.new('RGB',(1100,880),'#252f34');d=ImageDraw.Draw(image);scale=4;ox=380;oz=425
 for x,z,name,wall in json.loads(p.read_text()):
  c='#6c9686' if name.endswith('moss_block') else '#d1cbc0' if name.endswith('calcite') else '#626168' if name.endswith('basalt') else '#607772'
  if wall:c='#ad9b78'
  d.rectangle((ox+x*scale,oz+z*scale,ox+(x+1)*scale,oz+(z+1)*scale),fill=c)
 for x,z,name in [(-70,0,'Glass Gallery'),(70,0,'Brood Vaults'),(0,-70,'Heart Furnace'),(0,65,'Preparation court')]:
  d.text((ox+x*scale-40,oz+z*scale+5),name,fill='white');d.ellipse((ox+x*scale-4,oz+z*scale-4,ox+x*scale+4,oz+z*scale+4),fill='#ffe4ab')
 d.text((20,20),'Crown Warren - actual native floor / wall cells',fill='white');d.text((20,45),'85-block central arena / three branching wings / sheltered staging and outer return paths',fill='#d2dbd7')
 d.text((790,200),'The Hunt',fill='#efc38f');d.text((790,224),'Committed pounces and beams',fill='white');d.text((790,300),'The Living Crown',fill='#b4dcaa');d.text((790,324),'Finite pack and vitality links',fill='white');d.text((790,400),'The Broken Crown',fill='#e7a29a');d.text((790,424),'Flight, dives, marked blasts',fill='white')
 d.text((790,540),'Any two matching keystones',fill='#e9d8a8');d.text((790,564),'open the central Crown Gate.',fill='white');image.save(ROOT/'art/crown_warren_preview.png')
