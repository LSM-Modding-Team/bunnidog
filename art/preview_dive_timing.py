"""Headless timing schematic; not an in-game rendering preview."""
from PIL import Image, ImageDraw
im=Image.new('RGB',(720,240),(38,42,48));d=ImageDraw.Draw(im)
d.text((24,16),'Aerial melee: orbit -> 8 tick warning -> dive -> ascend',fill='white')
for x,title,scale in [(24,'Apex: 1.35 blocks/tick, 4 crit particles',1),(380,'Brood: genetic speed x1.6, 2 crit particles',.65)]:
    d.text((x,47),title,fill='white')
    cx=x+60;cy=100;r=int(12*scale)
    d.ellipse((cx-r,cy-r,cx+r,cy+r),fill='#bbaa99')
    for dx,dy in [(-8,-28),(8,-24)]+([(-18,-20),(18,-30)] if scale==1 else []):
        d.line((cx+dx-2,cy+dy,cx+dx+2,cy+dy),fill='#d6b897');d.line((cx+dx,cy+dy-2,cx+dx,cy+dy+2),fill='#d6b897')
    d.line([(cx+18,cy+8),(cx+108,185),(cx+190,95)],fill='#e2b77b',width=2)
    d.text((x,208),'Brief pause       one strike       climb',fill='white')
im.save('art/dive_timing_preview.png')
