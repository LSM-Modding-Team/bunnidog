#!/usr/bin/env python3
"""Actual tail-tip phenotypes for all five expressed fertility alleles."""
from pathlib import Path
import numpy as np
from PIL import Image, ImageDraw, ImageFont
from preview_bunnidog import ROOT, world_faces, render

def main():
    root=Path(ROOT); texture=np.asarray(Image.open(root/'src/main/resources/assets/bunnidogs/textures/entity/bunnidog_tan.png'),dtype=float)
    sheet=Image.new('RGB',(1500,465),(238,234,226)); draw=ImageDraw.Draw(sheet)
    font=ImageFont.truetype('DejaVuSans.ttf',18); small=ImageFont.truetype('DejaVuSans.ttf',14)
    labels={'average':'Unmarked tip','prolific':'One open rosette','shy':'Closed green bud','swarm':'Three rosettes','starborn':'Violet star'}
    for i,(allele,label) in enumerate(labels.items()):
        faces=world_faces(fertility=allele,parts={'tail','fertility_'+allele})
        shifted=[(c+np.array((0,3.25,-7.8)),n,uv,size,vn) for c,n,uv,size,vn in faces]
        image=render(shifted,texture,-35,38,300,310,135,plain=True)
        sheet.paste(image,(i*300,65)); draw.text((i*300+20,22),allele.title(),font=font,fill=(45,50,39))
        draw.text((i*300+20,384),label,font=small,fill=(65,72,55))
    draw.text((25,430),'Actual exported geometry/atlas; tail-tip closeups. Expressed fertility only, independent of breeding readiness or Bloom points.',font=small,fill=(65,72,55))
    path=root/'art/fertility_preview.png'; sheet.save(path); print('Wrote',path.name)

if __name__=='__main__':main()
