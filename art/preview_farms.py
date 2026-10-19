#!/usr/bin/env python3
"""Draw FarmDump's real regional cells with approximate block colors; no native textures or client proof."""
import sys
from pathlib import Path
from PIL import Image, ImageDraw, ImageFont

COLORS = {'SAND':(213,191,134),'CACTUS':(65,127,65),'SOUL_SAND':(91,67,58),'NETHER_WART':(146,35,45),
          'MYCELIUM':(129,112,136),'RED_MUSHROOM':(206,65,58),'BROWN_MUSHROOM':(155,117,84),
          'END_STONE':(218,218,169),'CHORUS_FLOWER':(162,119,162),'PLANK':(161,111,68),
          'GLOW_VINE':(102,143,65),'FILL':(101,104,93),'TROUGH':(146,98,59),'WATER':(56,114,173),
          'FARMLAND':(106,74,48),'WHEAT':(183,168,65),'CARROTS':(105,162,57),'POTATOES':(122,151,66),'MELON_STEM':(133,170,65)}
PLANTS = {'NETHER_WART','RED_MUSHROOM','BROWN_MUSHROOM','CHORUS_FLOWER','GLOW_VINE','WHEAT','CARROTS','POTATOES','MELON_STEM'}

def main():
    farms=[]
    for line in sys.stdin:
        if line.startswith('FARM '): farms.append((line.split()[1],[]))
        elif line.strip():
            x,y,z,mat=line.split(); farms[-1][1].append((int(x),int(y),int(z),mat))
    img=Image.new('RGB',(990,650),(37,42,39)); d=ImageDraw.Draw(img)
    font=ImageFont.truetype('DejaVuSans.ttf',14)
    for i,(name,cells) in enumerate(farms):
        ox,oy=165+(i%3)*330,207+(i//3)*270
        d.text((ox-95,oy-175),name.title(),font=font,fill=(233,225,193))
        for x,y,z,mat in sorted(cells,key=lambda c:(c[0]+c[2],c[1],c[0])):
            px,py=ox+(x-z)*32,oy+(x+z)*15-y*29
            if mat == 'FRUIT_SPACE':
                d.polygon([(px,py-17),(px+29,py-2),(px,py+13),(px-29,py-2)],outline=(234,197,104),width=2)
                continue
            r=9 if mat in PLANTS else 29
            height=15 if mat in PLANTS else 10 if mat=='TROUGH' else 27
            c=COLORS[mat]; dark=tuple(int(v*.6) for v in c); mid=tuple(int(v*.8) for v in c)
            top=[(px,py-r/2-height),(px+r,py-height),(px,py+r/2-height),(px-r,py-height)]
            d.polygon([(px-r,py-height),(px,py+r/2-height),(px,py+r/2),(px-r,py)],fill=dark)
            d.polygon([(px,py+r/2-height),(px+r,py-height),(px+r,py),(px,py+r/2)],fill=mid)
            d.polygon(top,fill=c,outline=(32,38,29))
            if mat=='GLOW_VINE': d.ellipse((px-4,py-height-4,px+4,py-height+4),fill=(246,176,49))
    d.text((16,579),'Version-six meadow beds / same 3x3 footprint / optional empty corner trough',font=font,fill=(233,225,193))
    d.text((16,606),'Outlines: fruit gaps. Actual cells; approximate colors. Native terrain and client appearance need playtests.',font=font,fill=(190,194,174))
    path=Path(__file__).with_name('regional_farms_preview.png'); img.save(path); print('Wrote',path.name)

if __name__=='__main__': main()
