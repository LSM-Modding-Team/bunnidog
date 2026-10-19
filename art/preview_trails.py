#!/usr/bin/env python3
"""Top views of TrailDump's exact new entrance routes; approximate colors, no client rendering."""
import sys
from pathlib import Path
from PIL import Image, ImageDraw, ImageFont

def main():
    plans = []
    for line in sys.stdin:
        p = line.split()
        if not p: continue
        if p[0] == 'CASE': plans.append((p[1],int(p[2]),[],[]))
        elif p[0] == 'MOUND': plans[-1][2].append((int(p[1]),int(p[2]),int(p[3]),p[4]))
        elif p[0] == 'TRAIL': plans[-1][3].append((int(p[1]),int(p[2])))
    image = Image.new('RGB',(900,910),(34,42,37)); draw = ImageDraw.Draw(image)
    font = ImageFont.truetype('DejaVuSans.ttf',15)
    for i,(name,radial,mound,trail) in enumerate(plans):
        ox,oy=225+(i%2)*450,220+(i//2)*415
        draw.text((ox-175,oy-198),f'{name.title()} entrance / door radius {radial}',font=font,fill=(235,226,198))
        for x in range(-11,12):
            for z in range(-11,12):
                px,py=ox+x*15,oy+z*15
                color=(98,122,74) if x*x+z*z>5 else (135,103,74)
                draw.rectangle((px-7,py-7,px+7,py+7),fill=color,outline=(82,105,63))
        for x,z in trail:
            px,py=ox+x*15,oy+z*15
            draw.rectangle((px-7,py-7,px+7,py+7),fill=(198,164,99),outline=(226,193,125))
        for x,y,z,kind in sorted(mound,key=lambda c:c[1]):
            px,py=ox+x*15,oy+z*15
            color=(90,68,45) if kind=='BEAM' else (88,117,67) if kind=='ROOF' else (125,92,62)
            draw.rectangle((px-7,py-7,px+7,py+7),fill=color,outline=(50,59,41))
        draw.rectangle((ox-7,oy-7,ox+7,oy+7),fill=(90,60,36),outline=(220,190,123))
        draw.ellipse((ox-35,oy-35,ox+35,oy+35),outline=(179,199,123),width=2)
    draw.text((25,860),'Gold: complete fitted trail. Green mound: unchanged entrance. Circle: guava crown.',font=font,fill=(235,226,198))
    draw.text((25,884),'Actual layout columns; native terrain can choose the opposite side or omit an unsafe route.',font=font,fill=(185,197,175))
    path=Path(__file__).with_name('entrance_trails_preview.png'); image.save(path); print('Wrote',path.name)

if __name__ == '__main__': main()
