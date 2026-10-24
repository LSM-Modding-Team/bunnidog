#!/usr/bin/env python3
"""Actual approved sculpt with equivalent fuse tints; conditional anvil HUD uses substitute fonts."""
import math
from pathlib import Path
import numpy as np
from PIL import Image,ImageDraw
from preview_bunnidog import ROOT,world_faces,render,font
ROOT=Path(ROOT)
texture=np.asarray(Image.open(ROOT/'src/main/resources/assets/bunnidogs/textures/entity/bunnidog_tan.png'),dtype=float)
sheet=Image.new('RGB',(1440,680),(238,234,226));draw=ImageDraw.Draw(sheet)
draw.text((20,10),'Blastmaw normal / final fuse — actual sculpt, equivalent renderer transforms',font=font(18),fill='#292723')
for i,(swelling,red) in enumerate(((.35,False),(.95,False),(.35,True),(.95,True))):
    wobble=1+math.sin(swelling*100)*swelling*.01
    scale=np.diag(((1+swelling**4*.4)*wobble,(1+swelling**4*.1)/wobble,(1+swelling**4*.4)*wobble));faces=[]
    for corners,normal,uv,size,normals in world_faces():
        n=normal@np.linalg.inv(scale);n/=np.linalg.norm(n);ns=normals@np.linalg.inv(scale);ns/=np.linalg.norm(ns,axis=1)[:,None]
        faces.append((corners@scale,n,uv,size,ns))
    flash=int(swelling*10)%2!=0
    shown=texture*np.array((1,.188,.188,1)) if red and flash else texture*(1-swelling)+255*swelling if flash else texture
    sheet.paste(render(faces,shown,-45,10,360,400,16),(i*360,40))
    draw.text((20+i*360,440),('Final: red' if red else 'Normal: white')+f' / {swelling:.0%}',font=font(16),fill='#292723')
draw.text((20,490),'Anvil escape HUD — visible only while dazed; actual 110 × 6 bar, substitute font',font=font(18),fill='#292723')
for i,progress in enumerate((0,180,360)):
    ox=40+i*465;oy=534
    draw.rectangle((ox,oy,ox+320,oy+120),fill='#34403b')
    x=ox+105;y=oy+46
    draw.rectangle((x-1,y-1,x+111,y+7),fill='#202020');draw.rectangle((x,y,x+110,y+6),fill='#5b5b5b')
    if progress:draw.rectangle((x,y,x+110*progress//360,y+6),fill='#e0b775')
    text='Shake your view' if i!=1 else 'Sacude la vista'
    draw.text((ox+160-draw.textlength(text,font=font(10))/2,y+12),text,font=font(10),fill='white')
sheet.save(ROOT/'art/apex_review_preview.png')
