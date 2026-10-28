"""Native project-menu snapshots and source textures; substitute font, not client screenshots."""
import json
from pathlib import Path
from PIL import Image,ImageDraw
from preview_stations import panel,container_slot,icon,translator,FONT,PANEL_INK

ROOT=Path(__file__).resolve().parents[1]

def item(image,draw,stack,x,y):
    if stack['count']<=0:return
    sprite=icon(stack['item'])
    if sprite:
        sprite=sprite.resize((16,16),Image.Resampling.NEAREST);image.paste(sprite,(x,y),sprite)
    if stack['count']>1:
        value=str(stack['count']);draw.text((x+17-FONT.getlength(value),y+8),value,font=FONT,fill='white',stroke_width=1,stroke_fill='black')

def frame(view,locale):
    text,messages=translator(locale);image=Image.new('RGB',(320,268),'#30372e');draw=ImageDraw.Draw(image)
    w,h=view['width'],view['height'];x,y=(320-w)//2,5
    panel(image,x,y,w,h)
    draw.text((x+8,y+6),messages['block.bunnidogs.'+view['kind']],font=FONT,fill=PANEL_INK)
    draw.text((x+26,y+136),'Inventario' if locale=='es_pe' else 'Inventory',font=FONT,fill=PANEL_INK)
    state=messages['project.bunnidogs.'+view['state']];draw.text((x+(w-FONT.getlength(state))/2,y+66),state,font=FONT,fill=PANEL_INK)
    for slot in view['slots']:
        sx,sy=x+slot['x'],y+slot['y'];container_slot(image,sx,sy);item(image,draw,slot,sx,sy)
    if view['kind'] in ['pack_armoury','resonance_bench']:
        draw.text((x+112,y+28),'→',font=FONT,fill=PANEL_INK)
        if view['kind']=='resonance_bench':draw.text((x+57,y+28),'+',font=FONT,fill=PANEL_INK)
        for i,cost in enumerate(view['costs']):item(image,draw,cost,x+28+i*58,y+46)
    elif view['kind']=='bastion_heart':
        for i,name in enumerate(['cinderite_ingot','starlit_dust']):
            sx=x+27+i*92;item(image,draw,{'item':'bunnidogs:'+name,'count':1},sx,y+29)
            draw.rectangle((sx+21,y+34,sx+66,y+39),fill='#555555');fuel=view['fuel'][i];width=44*fuel//[1200,12000][i]
            if width:draw.rectangle((sx+22,y+35,sx+21+width,y+38),fill='#78a265')
    else:
        names=[('cinderite_ingot',2),('starlit_dust',1)] if view['kind']=='launch_aerie' else [('nursery_bedding',1),('guava',1)]
        for i,(name,n) in enumerate(names):item(image,draw,{'item':'bunnidogs:'+name,'count':n},x+47+i*76,y+(24 if view['kind']=='launch_aerie' else 30))
        if view['kind']=='launch_aerie':
            draw.rectangle((x+68,y+44,x+143,y+63),fill='#777777',outline='#555555');label=messages['project.bunnidogs.launch'];draw.text((x+106-FONT.getlength(label)/2,y+49),label,font=FONT,fill='#b8b8b8')
        if view['kind']=='nursery_hearth':
            draw.rectangle((x+28,y+53,x+183,y+56),fill='#555555');width=154*view['fuel'][2]//600
            if width:draw.rectangle((x+29,y+54,x+28+width,y+55),fill='#78a265')
    draw.text((8,246),locale+' / '+view['kind'],font=FONT,fill='#dae2d0')
    return image

if __name__=='__main__':
    views=json.loads((ROOT/'build/game-test/project_workshops_preview.json').read_text())
    names=['armoury','binding','bastion_heart','nursery_hearth','launch_aerie']
    sheet=Image.new('RGB',(len(names)*320,574),'#20291e');draw=ImageDraw.Draw(sheet)
    for row,locale in enumerate(['en_us','es_pe']):
        for column,name in enumerate(names):sheet.paste(frame(views[name],locale),(column*320,row*268))
    draw.text((12,550),'Native inventory snapshots / 320×240 viewport / source textures / substitute font / no client capture',font=FONT,fill='#d1d7cb')
    sheet.save(ROOT/'art/project_workshops_preview.png')
    from gen_guild_projects import NAMES
    names=[(name,label[0]) for name,label in NAMES.items() if (ROOT/f'src/main/resources/assets/bunnidogs/textures/item/{name}.png').exists()]
    sheet=Image.new('RGB',(720,222),'#20291e');draw=ImageDraw.Draw(sheet)
    for i,(name,label) in enumerate(names):
        x=(i%6)*120;y=(i//6)*100
        sprite=icon('bunnidogs:'+name).resize((48,48),Image.Resampling.NEAREST);sheet.paste(sprite,(x+36,y+12),sprite)
        draw.text((x+(120-FONT.getlength(label))/2,y+68),label,font=FONT,fill='#e5dec7')
    draw.text((12,205),'Shipped 16px inventory sprites / source textures',font=FONT,fill='#d1d7cb');sheet.save(ROOT/'art/project_materials_preview.png')
