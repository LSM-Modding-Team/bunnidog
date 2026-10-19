"""Two labeled guava mangers for the planned Breeding Pen, with real per-slot fill states."""
import json
from PIL import Image, ImageDraw, ImageFont
from gen_workshop import ROOT, RES, write, cube, drop, unlock, block_item, tag, draw_model


def main():
    straw = Image.new('RGBA',(16,16),(178,157,76)); d = ImageDraw.Draw(straw)
    for y in range(16):
        for x in range(16):
            if (x*7+y*13)%11 < 4: straw.putpixel((x,y),(217,193,103,255))
    d.ellipse((5,5,11,11),fill=(152,187,77)); d.line((7,5,9,4),fill=(78,122,47))
    straw.save(RES/'assets/bunnidogs/textures/block/pen_food.png')
    for letter in ('A','B'):
        label = Image.new('RGBA',(16,16),(213,190,131)); d = ImageDraw.Draw(label)
        rows = ['.XX.','X..X','XXXX','X..X','X..X'] if letter == 'A' else ['XXX.','X..X','XXX.','X..X','XXX.']
        for y,row in enumerate(rows):
            for x,c in enumerate(row):
                if c == 'X': d.rectangle((4+x*2,3+y*2,5+x*2,4+y*2),fill=(75,60,42))
        label.save(RES/f'assets/bunnidogs/textures/block/pen_label_{letter.lower()}.png')
    for fill in range(4):
        elements = [cube([1,0,2],[15,2,14],'wood'),cube([1,12,11],[15,15,13],'wood')]
        for x in (1,13):
            for z in (2,12): elements.append(cube([x,2,z],[x+2,12,z+2],'wood'))
        for slot,x in ((0,9),(1,2)):
            elements += [cube([x,3,3],[x+5,4,11],'wood'),cube([x,4,3],[x+5,6,4],'wood'),
                cube([x,4,10],[x+5,6,11],'wood'),cube([x,4,4],[x+1,6,10],'wood'),cube([x+4,4,4],[x+5,6,10],'wood'),
                cube([x+1,4,2.95],[x+4,6.5,3.05],'label_a' if slot == 0 else 'label_b',[0,0,16,16])]
            if fill & (1<<slot): elements.append(cube([x+1,4,4],[x+4,5.5,10],'food',[0,0,16,16]))
        write(f'assets/bunnidogs/models/block/breeding_pen_{fill}.json',{'parent':'minecraft:block/block',
            'textures':{'particle':'bunnidogs:block/guava_planks','wood':'bunnidogs:block/guava_planks',
                'food':'bunnidogs:block/pen_food','label_a':'bunnidogs:block/pen_label_a','label_b':'bunnidogs:block/pen_label_b'},'elements':elements})
    write('assets/bunnidogs/blockstates/breeding_pen.json',{'variants':{
        f'facing={facing},fill={fill}':{'model':f'bunnidogs:block/breeding_pen_{fill}','y':rotation}
        for fill in range(4) for facing,rotation in [('north',0),('east',90),('south',180),('west',270)]}})
    block_item('breeding_pen','breeding_pen_0'); drop('breeding_pen')
    write('data/bunnidogs/recipe/breeding_pen.json',{'type':'minecraft:crafting_shaped','pattern':['FHF','PWP','CCC'],
        'key':{'F':'#minecraft:wooden_fences','H':'minecraft:hay_block','P':'#minecraft:planks','W':'bunnidogs:whisker','C':'minecraft:copper_ingot'},
        'result':{'id':'bunnidogs:breeding_pen'}})
    unlock('breeding_pen','bunnidogs:whisker'); tag('data/minecraft/tags/block/mineable/axe.json',['bunnidogs:breeding_pen'])
    text = {'block.bunnidogs.breeding_pen':'Breeding Pen',
        'tooltip.bunnidogs.pen.ports':'Hoppers: top feeds A; sides feed B. Collect litter seeds on the floor.',
        'message.bunnidogs.pen.ready':'Ready. Empty-hand use or a redstone pulse feeds the pair.',
        'message.bunnidogs.pen.pair':'A: %s, %s x%s. B: %s, %s x%s.',
        'message.bunnidogs.pen.too_far':'Bring both adults within four blocks, and less than three blocks apart.',
        'message.bunnidogs.pen.unavailable':'Both adults need full health, no love/rest, no danger, leads or rides.',
        'message.bunnidogs.pen.home':'Each parent needs a real nearby claimed Nest.',
        'message.bunnidogs.pen.unsafe':'Keep both parents on safe dry ground with a clear view of each other.',
        'message.bunnidogs.pen.unloaded':'Keep the twelve-block surrounding area loaded.',
        'message.bunnidogs.pen.protected':'Automatic feeding was refused by mob-griefing protection.',
        'message.bunnidogs.pen.empty':'Load guavas into A (front left) and B (front right).',
        'message.bunnidogs.pen.changed':'The pair or pen changed; no new feeding started.',
        'message.bunnidogs.pen.busy':'The pen is already feeding.',
        'message.bunnidogs.pen.fed':'Fed one guava each. Normal courtship produces the litter seed.',
        'message.bunnidogs.pen.pair_count':'Keep exactly two ordinary adults in the twelve-block pen area.'}
    # 'pair' is the two-parent stock readout; status PAIR has its own readable key.
    text['message.bunnidogs.pen.need_pair'] = text.pop('message.bunnidogs.pen.pair_count')
    path = RES/'assets/bunnidogs/lang/en_us.json'; language = json.loads(path.read_text()); language.update(text); path.write_text(json.dumps(language,indent=2)+'\n')
    preview = Image.new('RGB',(1000,340),(43,42,40)); d = ImageDraw.Draw(preview); font = ImageFont.truetype('DejaVuSans.ttf',16)
    d.text((22,16),'Breeding Pen — A/B guava mangers, native courtship, one feeding per pulse',font=font,fill=(239,225,191))
    for index,fill in enumerate((0,1,2,3)):
        draw_model(preview,f'breeding_pen_{fill}',(125+index*250,190),front=True)
        d.text((28+index*250,276),['Empty','A stocked','B stocked','Both stocked'][index],font=font,fill=(226,217,188))
    preview.save(ROOT/'art/breeding_pen_preview.png')
    print('Generated Breeding Pen models/states, textures, recipe, text and preview.')


if __name__ == '__main__': main()
