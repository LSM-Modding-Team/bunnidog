#!/usr/bin/env python3
"""Native model geometry and equivalent native projectile integration; not an in-client capture."""
from pathlib import Path
from zipfile import ZipFile
from io import BytesIO
import json,math
from PIL import Image,ImageDraw,ImageFont,ImageStat
ROOT=Path(__file__).resolve().parents[1]
with ZipFile(ROOT/'build/moddev/artifacts/minecraft-patched-26.2.0.88.jar') as z:
    model=json.loads(z.read('assets/minecraft/models/block/template_anvil.json'))
    color=tuple(round(c) for c in ImageStat.Stat(Image.open(BytesIO(z.read('assets/minecraft/textures/block/anvil.png'))).convert('RGB')).mean)
def font(size):return ImageFont.truetype('DejaVuSans.ttf',size)
img=Image.new('RGB',(1140,620),'#eeeae2');d=ImageDraw.Draw(img)
d.text((20,14),'Anvil follow-up — native block geometry and equivalent native ballistic integration',font=font(18),fill='#292723')
for center,scale,label in [(150,.75,'Previous item scale'),(390,2,'New world model: 2 blocks high')]:
    def project(p):
        x,y,z=p;return (center+(x-z)*60*.866,360-y*60+(x+z)*30)
    faces=[]
    for part in model['elements']:
        a=[(part['from'][i]/16-(.5 if i!=1 else 0))*scale for i in range(3)]
        b=[(part['to'][i]/16-(.5 if i!=1 else 0))*scale for i in range(3)]
        x,y,z=a;X,Y,Z=b
        for points,shade in [([(x,Y,z),(X,Y,z),(X,Y,Z),(x,Y,Z)],1.15),([(X,y,z),(X,y,Z),(X,Y,Z),(X,Y,z)],.65),([(x,y,Z),(X,y,Z),(X,Y,Z),(x,Y,Z)],.9)]:
            faces.append((sum(v[1]*2+v[0]+v[2] for v in points)/4,points,shade))
    for _,points,shade in sorted(faces):
        d.polygon([project(p) for p in points],fill=tuple(min(255,int(c*shade)) for c in color),outline='#43413d')
    d.text((center-95,407),label,font=font(13),fill='#292723')
    d.line((center-80,365,center+80,365),fill='#969087')
ox,oy,px,py=615,360,25,25
d.line((ox,110,ox,oy,1085,oy),fill='#79736c',width=2)
for x in range(0,19,4):
    d.text((ox+x*px-8,oy+8),str(x),font=font(12),fill='#292723')
for start,target,col,label in [(2.166,1.62,'#797061','Ground shot'),(9.166,1.62,'#a64c36','Aerial shot')]:
    ticks=max(16,min(32,math.ceil(16*1.1)));drag=.99;weight=fall=vertical=0;power=1
    for _ in range(ticks):power*=drag;weight+=power;vertical=(vertical-.06)*drag;fall+=vertical
    vx,vy=16/weight,(target-start-fall)/weight;x,y=0,start;points=[(ox,oy-y*py)]
    for _ in range(ticks):vx*=drag;vy=(vy-.06)*drag;x+=vx;y+=vy;points.append((ox+x*px,oy-y*py))
    d.line(points,fill=col,width=3)
    for i,p in enumerate(points):
        if i%3==0:d.ellipse((p[0]-3,p[1]-3,p[0]+3,p[1]+3),fill=col)
    d.text((655,440 if label=='Ground shot' else 465),label,font=font(15),fill=col)
d.text((615,397),'Horizontal distance (blocks)',font=font(13),fill='#292723')
d.text((20,505),'Parabola: gravity 0.06, air drag 0.99; 16–32 ticks to the aimed point.',font=font(17),fill='#292723')
d.text((20,540),'Collision body 2 × 2; damage matches the same line’s flying Brute; 14-second cooldown.',font=font(17),fill='#292723')
d.text((20,577),'Projection, averaged native texture colors and substitute font; live client appearance remains play review.',font=font(13),fill='#67635b')
img.save(ROOT/'art/anvil_arc_preview.png')
