"""Render the native unfinished-ProtoChunk grove fixture and the actual fruit textures.
No concept mesh or substitute tree plan; source cells come from orchard_grove.
"""
from pathlib import Path
import hashlib,json,os,subprocess,sys
from PIL import Image,ImageDraw,ImageFont,ImageOps
from nbt_template import save

ROOT=Path(__file__).resolve().parents[1]
OUT=ROOT/'art/orchard';OUT.mkdir(exist_ok=True)
log=Path(sys.argv[1]) if len(sys.argv)>1 else ROOT/'build/orchard-full.log'
rows=next(json.loads(line.split('ORCHARD_GROVE_PREVIEW ',1)[1]) for line in log.read_text().splitlines() if 'ORCHARD_GROVE_PREVIEW ' in line)
(OUT/'native_grove_cells.json').write_text(json.dumps(rows,indent=2)+'\n')
cells={}
for x,y,z,name,*fruit in rows:
    props={'age':str(fruit[0]),'bunch':str(fruit[1])} if fruit else {}
    cells[x,y,z]=(name,props,None)
minimum=[min(p[i] for p in cells) for i in range(3)]
maximum=[max(p[i] for p in cells) for i in range(3)]
local={tuple(p[i]-minimum[i] for i in range(3)):s for p,s in cells.items()}
save(OUT/'native_grove.nbt',local,tuple(maximum[i]-minimum[i]+1 for i in range(3)))
(OUT/'native_grove.json').write_text(json.dumps({'tiles':[[*minimum,'native_grove']]},indent=2)+'\n')
views=[['tree',[13,-19,10],[0,0,5.8],42,940,940],['branches',[5,-8,5.5],[0,0,8],38,940,680]]
(OUT/'views.json').write_text(json.dumps(views,indent=2)+'\n')
env=dict(os.environ,STRUCTURE_META=str(OUT/'native_grove.json'),STRUCTURE_TEMPLATE_ROOT=str(OUT),STRUCTURE_RENDER_DIR=str(OUT),STRUCTURE_VIEWS=str(OUT/'views.json'))
subprocess.run([sys.executable,str(ROOT/'art/render_citadel.py')],check=True,env=env)
sheet=Image.new('RGB',(1580,1060),'#25302a');d=ImageDraw.Draw(sheet)
font=ImageFont.truetype('DejaVuSans.ttf',23);small=ImageFont.truetype('DejaVuSans.ttf',17)
d.text((24,18),'Native grove / flowering, green and ripe bunches',font=font,fill='#f1e7cc')
sheet.paste(Image.open(OUT/'tree.png').convert('RGB'),(20,65))
branches=ImageOps.contain(Image.open(OUT/'branches.png').convert('RGB'),(590,500));sheet.paste(branches,(970,65))
textures=ImageOps.contain(Image.open(ROOT/'art/guava_tree_preview.png').convert('RGB'),(365,480));sheet.paste(textures,(1080,555))
d.text((24,1022),'Actual native tree cells and resource textures; headless geometry/material check, not a client capture.',font=small,fill='#c1cbb8')
sheet.save(OUT/'overview.png')
provenance=json.loads((OUT/'provenance.json').read_text());provenance['source']='orchard_grove native WorldGenRegion / seed 1832';provenance['cell_sha256']=hashlib.sha256((OUT/'native_grove_cells.json').read_bytes()).hexdigest()
(OUT/'provenance.json').write_text(json.dumps(provenance,indent=2)+'\n')
