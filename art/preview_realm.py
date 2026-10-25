"""Inspect native fixture snapshots. Run runGameTestServer first; no synthetic terrain."""
from pathlib import Path
import json,zipfile
from PIL import Image,ImageDraw,ImageStat
ROOT=Path(__file__).resolve().parents[1]
mc_assets=zipfile.ZipFile(ROOT/'build/moddev/artifacts/minecraft-patched-26.2.0.88.jar')
colors={}
def color(name):
 if name in colors:return colors[name]
 ns,path=name.split(':');result=(128,125,116)
 try:
  if ns=='bunnidogs':im=Image.open(ROOT/f'src/main/resources/assets/{ns}/textures/block/{path}.png')
  else:
   import io
   im=Image.open(io.BytesIO(mc_assets.read(f'assets/{ns}/textures/block/{path}.png')))
  result=tuple(int(c) for c in ImageStat.Stat(im.convert('RGBA').convert('RGB')).mean)
 except (KeyError,FileNotFoundError):
  result={'minecraft:chest':(174,126,53),'minecraft:tuff_brick_wall':(104,108,94),'minecraft:guava_leaves':(137,154,86)}.get(name,result)
 colors[name]=result;return result
def shade(c,f):return tuple(min(255,int(v*f)) for v in c)
im=Image.new('RGB',(1200,520),(34,39,46));d=ImageDraw.Draw(im)
d.text((24,16),'Great Warren - native generated arrival and ruin geometry',fill='white')
for label,file,ox,oy,sx,sy,yscale in [('Arrival: actual loaded realm terrain','realm_arrival_preview.json',300,210,6,3,3),('Ruin: actual generated room cells','realm_ruin_preview.json',900,260,9,4.5,9)]:
 data=json.loads((ROOT/'build/game-test'/file).read_text());base=min(c[1] for c in data) if 'arrival' in file else 0
 d.text((ox-260,50),label,fill='white')
 for x,y,z,block in sorted(data,key=lambda c:(c[0]+c[2],c[1])):
  px=ox+(x-z)*sx;py=oy+(x+z)*sy-(y-base)*yscale;c=color(block)
  a=(px,py);b=(px+sx,py+sy);c1=(px,py+2*sy);e=(px-sx,py+sy)
  d.polygon([e,c1,(c1[0],c1[1]+yscale),(e[0],e[1]+yscale)],fill=shade(c,.65))
  d.polygon([b,c1,(c1[0],c1[1]+yscale),(b[0],b[1]+yscale)],fill=shade(c,.8))
  d.polygon([a,b,c1,e],fill=c)
d.text((24,480),'Headless geometry/texture study; not an in-game lighting or performance capture.',fill='#cac7c0')
im.save(ROOT/'art/realm_world_preview.png')
