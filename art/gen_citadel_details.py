"""V2 physical key seats and concise language. Runs last in the maintained art chain."""
from pathlib import Path
import json
from PIL import Image,ImageDraw
ROOT=Path(__file__).resolve().parents[1];A=ROOT/'src/main/resources/assets/bunnidogs'
def write(path,data):
 p=A/path;p.parent.mkdir(parents=True,exist_ok=True);p.write_text(json.dumps(data,indent=2,ensure_ascii=False)+'\n')
variants={}
for key,color in enumerate(['#182526','#a6d6ec','#a6c171','#ee9071']):
 im=Image.new('RGB',(16,16),'#697576');d=ImageDraw.Draw(im)
 d.rectangle((0,0,15,15),outline='#303e40');d.ellipse((2,2,13,13),fill='#b5a277',outline='#e7d6a5');d.ellipse((4,4,11,11),fill='#283637')
 if key:d.polygon([(8,4),(11,8),(8,12),(4,8)],fill=color,outline='#fff0c8')
 else:d.polygon([(8,4),(11,8),(8,12),(4,8)],outline='#111d1f')
 im.save(A/f'textures/block/crown_socket_{key}.png')
 write(Path(f'models/block/crown_socket_{key}.json'),{'parent':'minecraft:block/cube_all','textures':{'all':f'bunnidogs:block/crown_socket_{key}'}})
 for kind in range(3):variants[f'gate_kind={kind},key={key}']={'model':f'bunnidogs:block/crown_socket_{key}'}
write(Path('blockstates/crown_socket.json'),{'variants':variants})
write(Path('blockstates/crown_gate.json'),{'variants':{f'gate_kind={k}':{'model':'bunnidogs:block/crown_gate'} for k in range(3)}})
# Subtle warm grains in Rootstone make protected recessed lighting, rather than route markers.
im=Image.open(A/'textures/block/rootstone.png').convert('RGB');d=ImageDraw.Draw(im)
for x,y in [(3,4),(11,8),(6,12)]:d.rectangle((x,y,x+1,y+1),fill='#f0ce8b')
im.save(A/'textures/block/crown_rootlight.png')
write(Path('models/block/crown_rootlight.json'),{'parent':'minecraft:block/cube_all','textures':{'all':'bunnidogs:block/crown_rootlight'}})
write(Path('blockstates/crown_rootlight.json'),{'variants':{'':{'model':'bunnidogs:block/crown_rootlight'}}})
rows={
 'block.bunnidogs.crown_rootlight':('Luminous Rootstone','Piedra raíz luminosa'),
 'block.bunnidogs.crown_socket':('Keystone Seat','Asiento de piedra llave'),
 'message.bunnidogs.crown.archive_locked':('The archive has one empty keystone seat.','Al archivo le falta una piedra llave.'),
 'message.bunnidogs.crown.vault_locked':('The treasury is sealed by the living crown.','La corona viviente mantiene cerrada la tesorería.'),
 'tooltip.bunnidogs.crown_key':('Two different keystones open this citadel’s Crown Gate. The third opens its archive.','Dos piedras llave distintas abren la puerta de esta ciudadela. La tercera abre su archivo.'),
}
for i,locale in enumerate(['en_us','es_pe']):
 p=A/'lang'/f'{locale}.json';data=json.loads(p.read_text());data.update({k:v[i] for k,v in rows.items()});p.write_text(json.dumps(data,indent=2,ensure_ascii=False)+'\n')
