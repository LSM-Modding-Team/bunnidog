#!/usr/bin/env python3
"""Compare actual ordinary/webbed paw geometry and the expressed swamp variant."""
from pathlib import Path
import numpy as np
from PIL import Image, ImageDraw, ImageFont
from preview_bunnidog import ROOT, world_faces, render

def main():
    root=Path(ROOT); texture=np.asarray(Image.open(root/'src/main/resources/assets/bunnidogs/textures/entity/bunnidog_moss.png'),dtype=float)
    sheet=Image.new('RGB',(1240,760),(238,234,226)); draw=ImageDraw.Draw(sheet)
    font=ImageFont.truetype('DejaVuSans.ttf',19)
    small=ImageFont.truetype('DejaVuSans.ttf',15)
    for i,webbed in enumerate((False,True)):
        faces=world_faces(webbed=webbed,parts={'leg_fr','leg_fr_web'})
        shifted=[(c+np.array((-4.35,9,3.4)),n,uv,size,vn) for c,n,uv,size,vn in faces]
        image=render(shifted,texture,-15,42,410,530,180,plain=True)
        sheet.paste(image,(i*410,95)); draw.text((i*410+30,35),'Expressed webbed paw' if webbed else 'Approved ordinary paw',font=font,fill=(45,50,39))
    whole=render(world_faces(leg_scale=.95,webbed=True),texture,-28,14,420,600,24)
    sheet.paste(whole,(820,70));draw.text((850,35),'Swamp swimming line',font=font,fill=(45,50,39))
    draw.text((30,678),'Actual exported geometry/atlas. Only expressed webbed legs show the membranes.',font=small,fill=(65,72,55))
    draw.text((30,708),'All 2,172 approved base faces retain exact coordinates/normals; the optional four-paw overlay adds 72.',font=small,fill=(65,72,55))
    path=root/'art/webbed_paws_preview.png';sheet.save(path);print('Wrote',path.name)

if __name__=='__main__':main()
