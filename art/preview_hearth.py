"""Orthographic study of the actual shipped block-model boxes; not a client capture."""
from pathlib import Path
import json
from PIL import Image,ImageDraw,ImageStat
root=Path(__file__).resolve().parents[1];a=root/'src/main/resources/assets/bunnidogs'
im=Image.new('RGB',(1150,600),'#26332e');d=ImageDraw.Draw(im);d.text((24,15),'Hearth Tree - five stages from the shipped block model geometry',fill='white')
def draw(name,ox,oy,scale):
 model=json.loads((a/'models/block'/f'{name}.json').read_text());textures=model['textures']
 def project(p):x,y,z=p;return (ox+(x-z)*scale,oy+(x+z)*scale*.5-y*scale)
 for el in sorted(model['elements'],key=lambda e:(sum(e['from'][::2]),e['from'][1])):
  x,y,z=el['from'];xx,yy,zz=el['to'];key=next(iter(el['faces'].values()))['texture'][1:];tex=textures[key].split(':')[1]
  color=tuple(int(c) for c in ImageStat.Stat(Image.open(a/'textures'/f'{tex}.png').convert('RGB')).mean)
  for points,f in [([(x,y,zz),(xx,y,zz),(xx,yy,zz),(x,yy,zz)],.72),([(xx,y,z),(xx,y,zz),(xx,yy,zz),(xx,yy,z)],.86),([(x,yy,z),(xx,yy,z),(xx,yy,zz),(x,yy,zz)],1.12)]:d.polygon([project(p) for p in points],fill=tuple(min(255,int(c*f)) for c in color))
for stage in range(5):draw(f'hearth_tree_{stage}',120+stage*225,220,4);d.text((80+stage*225,260),f'Stage {stage+1}',fill='#e4dbc1')
d.text((24,320),'Renewable guava coppice - growing, young and mature',fill='white')
for i,age in enumerate([0,3,7]):draw(f'guava_coppice_{age}',180+i*300,515,4)
d.text((24,570),'Headless geometry/texture study. In-game lighting and player review remain separate.',fill='#c3cec5');im.save(root/'art/hearth_blocks_preview.png')
