#!/usr/bin/env python3
"""Approved sculpt with the renderer's creeper swelling/white-overlay transforms, not a client capture."""
import math
from pathlib import Path
import numpy as np
from PIL import Image, ImageDraw
from preview_bunnidog import ROOT, world_faces, render, font

ROOT=Path(ROOT)
sheet=Image.new('RGB',(1440,500),(238,234,226))
draw=ImageDraw.Draw(sheet)
draw.text((18,12),'Blastmaw fuse — equivalent renderer transforms / no wing geometry added',font=font(18),fill=(43,42,38))
texture=np.asarray(Image.open(ROOT/'src/main/resources/assets/bunnidogs/textures/entity/bunnidog_tan.png'),dtype=float)
for i,swelling in enumerate((0,.35,.65,.95)):
    wobble=1+math.sin(swelling*100)*swelling*.01
    transform=np.diag(((1+swelling**4*.4)*wobble,(1+swelling**4*.1)/wobble,(1+swelling**4*.4)*wobble))
    faces=[]
    for corners,normal,uv,size,normals in world_faces():
        n=normal@np.linalg.inv(transform);n/=np.linalg.norm(n)
        ns=normals@np.linalg.inv(transform);ns/=np.linalg.norm(ns,axis=1)[:,None]
        faces.append((corners@transform,n,uv,size,ns))
    overlay=0 if int(swelling*10)%2==0 else max(.5,swelling)
    shown=texture*(1-overlay)+255*overlay
    sheet.paste(render(faces,shown,-45,10,360,410,16),(i*360,45))
    draw.text((18+i*360,470),f'{swelling*1.5:.2f} s / white {overlay:.0%}',font=font(16),fill=(43,42,38))
sheet.save(ROOT/'art/apex_fuse_preview.png')
