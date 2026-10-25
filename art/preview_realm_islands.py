"""Visualize the native generator's fixture snapshot; not a client screenshot."""
from pathlib import Path
import json
from PIL import Image,ImageDraw
root=Path(__file__).resolve().parents[1]
data=json.loads((root/'build/game-test/realm_islands_preview.json').read_text())
im=Image.new('RGB',(1300,760),'#202737');d=ImageDraw.Draw(im)
d.text((24,18),'Great Warren - native archipelago terrain (top view)',fill='#f3e8cc')
d.text((24,38),'Seed '+str(data['seed'])+' | shaded islands, lit causeways and ore-rich moons',fill='#c1ccd2')
for x,z,land,moon,bridge in data['cells']:
 px=26+(x+1000)*.25;pz=76+(z+1000)*.25
 if land>0:
  c='#c3af89' if bridge else '#698b7c' if land>=78 else '#52776b';d.rectangle((px,pz,px+3,pz+3),fill=c)
 if moon>0:d.rectangle((px,pz,px+3,pz+3),fill='#ada7db')
d.rectangle((26+1000*.25-22,76+1000*.25-22,26+1000*.25+22,76+1000*.25+22),outline='#edd5a0')
d.text((730,100),'Astralite progression',fill='#b1eee4')
for i,line in enumerate(['Netherite equipment','    + Astralite Ingot + Upgrade Template','    -> Smithing Table','Astralite armor: 3x durability / toughness 5','Astralite tools: +20% durability / faster mining','','Two unique ores in islands and sky moons','Moons: ~14x richer, Blastmaw territory','','Artifact armor now starts from Astralite','No dungeon ingredient required','Existing generated chunks remain intact']):d.text((730,130+i*25),line,fill='#dedccf')
d.text((24,735),'Headless geometry study; not an in-game lighting or gameplay capture.',fill='#b6c0c9')
im.save(root/'art/realm_islands_preview.png')
