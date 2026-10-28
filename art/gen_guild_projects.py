"""Paid guild project recipes, textured native models and one shared Spanish source."""
from pathlib import Path
import json,random,math
from PIL import Image,ImageDraw
from gen_lunar_forge import write,recipe,unlock,tag
ROOT=Path(__file__).resolve().parents[1];R=ROOT/'src/main/resources';D=R/'data/bunnidogs';A=R/'assets/bunnidogs'
NAMES={
'root_fibre':('Root Fibre','Fibra de raíz'),'fibre_roll':('Root Fibre Roll','Rollo de fibra de raíz'),'dust_pouch':('Pouch of Starlit Dust','Bolsa de polvo estelar'),'sinew_bale':('Sinew Bale','Fardo de tendones'),'moonstone_crate':('Crate of Moonstone','Cajón de roca lunar'),'structural_frame':('Cinderroot Frame','Armazón de raíz de cinerita'),
'field_dressing':('Concord Field Dressing','Vendaje de concordia'),'nursery_bedding':('Nursery Bedding','Lecho de crianza'),'kennel_wagon':('Kennel Wagon','Carreta de la manada'),'prism_battery':('Prism Battery','Batería prismática'),'volatile_magazine':('Volatile Magazine','Cargador volátil'),'homeward_totem':('Homeward Totem','Tótem de regreso'),
'pack_armoury':('Pack Armoury','Armería de la manada'),'resonance_bench':('Resonance Binding Bench','Mesa de vínculo resonante'),'nursery_hearth':('Nursery Hearth','Hogar de crianza'),'bastion_heart':('Bastion Heart','Corazón del bastión'),'launch_aerie':('Launch Aerie','Nidal de despegue'),'guild_heart':('Lantern Warren Heart','Corazón de la madriguera de los faroles'),'guild_gate':('Lantern Gate','Portal de los faroles')}
BLOCKS=['pack_armoury','resonance_bench','nursery_hearth','bastion_heart','launch_aerie','guild_heart','guild_gate']
def simple(out,ingredients,count=1):
 write(D,'recipe/'+out+'.json',{'type':'minecraft:crafting_shapeless','category':'misc','ingredients':ingredients,'result':{'id':'bunnidogs:'+out,'count':count}});unlock(out,ingredients[0])
def generate():
 simple('root_fibre',['bunnidogs:guava_log'],4)
 for a,b in [('fibre_roll','root_fibre'),('dust_pouch','starlit_dust'),('sinew_bale','sinew'),('moonstone_crate','compressed_moonstone')]:
  recipe(a,['XXX','XXX','XXX'],{'X':'bunnidogs:'+b})
  write(D,'recipe/unpack_'+a+'.json',{'type':'minecraft:crafting_shapeless','category':'misc','ingredients':['bunnidogs:'+a],'result':{'id':'bunnidogs:'+b,'count':9}})
  unlock('unpack_'+a,'bunnidogs:'+a)
 recipe('structural_frame',['BCB','R R','BCB'],{'B':'bunnidogs:cinderite_billet','C':'bunnidogs:compressed_moonstone','R':'bunnidogs:fibre_roll'})
 recipe('field_dressing',['BBB','SDS','BHD'],{'B':'bunnidogs:cinderite_billet','S':'bunnidogs:sinew_bale','D':'bunnidogs:dust_pouch','H':'bunnidogs:heart_spool'},4)
 simple('nursery_bedding',['bunnidogs:root_fibre']*4+['bunnidogs:sinew']*2+['bunnidogs:starlit_dust'],4)
 recipe('prism_battery',['CMC','RLR','AWS'],{'C':'bunnidogs:compressed_moonstone','M':'bunnidogs:moonstone_crate','R':'bunnidogs:structural_frame','L':'bunnidogs:prism_lens','A':'bunnidogs:fibre_roll','W':'bunnidogs:whisker','S':'bunnidogs:astralite_concentrate'})
 recipe('volatile_magazine',['BRB','FHF','MMM'],{'B':'bunnidogs:cinderite_billet','R':'bunnidogs:structural_frame','F':'bunnidogs:fibre_roll','H':'bunnidogs:volatile_heart','M':'bunnidogs:compressed_moonstone'})
 for name,core in [('pack_armoury','apex_marrow'),('resonance_bench','prism_lens'),('nursery_hearth','chorus_heart'),('launch_aerie','aerial_membrane')]:
  recipe(name,['RMR','FCF',' C '],{'R':'bunnidogs:structural_frame','M':'bunnidogs:moonstone_crate','F':'bunnidogs:fibre_roll','C':'bunnidogs:'+core})
 recipe('bastion_heart',['RMR','AWA','RMR'],{'R':'bunnidogs:structural_frame','M':'bunnidogs:moonstone_crate','A':'bunnidogs:astralite_concentrate','W':'bunnidogs:warren_ward'})
 recipe('kennel_wagon',['RFR','SCS','RMR'],{'R':'bunnidogs:structural_frame','F':'bunnidogs:fibre_roll','S':'bunnidogs:sinew_bale','C':'bunnidogs:chorus_heart','M':'bunnidogs:aerial_membrane'})
 # No craft for guild hearts/gates or rescue totems: these belong to the actual towns/traders.
 for name in NAMES:
  color=(111,169,175) if any(k in name for k in ['prism','resonance','guild']) else (181,130,71) if name!='homeward_totem' else (147,192,161)
  rng=random.Random(name);im=Image.new('RGBA',(16,16),(0,0,0,0));dr=ImageDraw.Draw(im)
  if name in BLOCKS:
   tex=Image.new('RGB',(16,16));px=tex.load()
   for x in range(16):
    for y in range(16):
     n=rng.randint(-18,18);base=(62,66,72) if x in [0,1,14,15] or y in [0,1,14,15] else color;px[x,y]=tuple(max(0,min(255,c+n)) for c in base)
   t=ImageDraw.Draw(tex);t.rectangle((3,3,12,12),outline=(33,39,48));t.rectangle((5,5,10,10),outline=(215,214,164));t.line((6,8,9,8),fill=(170,230,225));tex.save(A/'textures/block'/f'{name}.png')
   textures={'all':'bunnidogs:block/'+name,'wood':'bunnidogs:block/guava_planks','metal':'bunnidogs:block/cinderroot_beams','particle':'bunnidogs:block/'+name}
   elements=[]
   def part(a,b,t='all'):
    elements.append({'from':a,'to':b,'faces':{f:{'texture':'#'+t} for f in ['up','down','north','south','east','west']}})
   if name in ['pack_armoury','resonance_bench','launch_aerie']:
    part([0,0,0],[16,3,16],'metal');part([1,10,1],[15,13,15]);
    for x in [1,12]:
     for z in [1,12]:part([x,3,z],[x+3,10,z+3],'wood')
    if name=='pack_armoury':part([1,13,13],[15,16,16],'metal')
    if name=='resonance_bench':part([4,13,4],[12,16,12])
   else:
    part([0,0,0],[16,3,16],'metal');part([2,3,2],[14,13,14]);part([0,13,0],[16,16,16],'metal')
   write(A,'models/block/'+name+'.json',{'parent':'minecraft:block/block','textures':textures,'elements':elements})
   write(A,'blockstates/'+name+'.json',{'variants':{'':{'model':'bunnidogs:block/'+name}}})
   write(A,'models/item/'+name+'.json',{'parent':'bunnidogs:block/'+name})
   write(A,'items/'+name+'.json',{'model':{'type':'minecraft:model','model':'bunnidogs:item/'+name}})
   write(D,'loot_table/blocks/'+name+'.json',{'type':'minecraft:block','pools':[{'rolls':1,'entries':[{'type':'minecraft:item','name':'bunnidogs:'+name}],'conditions':[{'condition':'minecraft:survives_explosion'}]}]})
   continue
  # Distinct inventory silhouettes, rather than recolouring the same square for every project.
  if name=='root_fibre':
   for x,c in [(2,'#a27942'),(6,'#d3b675'),(10,'#c0975c')]:dr.line((x,2,x+2,5,x,9,x+2,13),fill=c,width=2)
   dr.line((4,1,6,4,5,8,7,14),fill='#edd39a')
  elif name=='fibre_roll':
   dr.rounded_rectangle((2,4,12,13),2,fill='#b58b52',outline='#493724');dr.ellipse((7,2,14,10),fill='#d4b273',outline='#59422b');dr.ellipse((9,4,12,8),outline='#745634');dr.line((3,6,3,11),fill='#e9cf94');dr.line((2,10,7,14),fill='#7c5d39')
  elif name=='dust_pouch':
   dr.polygon([(4,4),(11,4),(14,11),(12,14),(3,14),(1,11)],fill='#817490',outline='#302b40');dr.line((5,2,11,2,10,5,5,5,5,2),fill='#b7b1bc',width=2);dr.line((4,5,12,5),fill='#d2b77e');dr.line((7,8,7,12),fill='#f5d76e');dr.line((5,10,9,10),fill='#f5d76e');dr.point((11,8),fill='#ded5ff')
  elif name=='sinew_bale':
   for y in [3,6,9]:dr.rounded_rectangle((1,y,14,y+3),1,fill='#bb8d88',outline='#654b4d');dr.line((3,y+1,11,y+1),fill='#edc3ad')
   for x in [4,10]:dr.rectangle((x,2,x+1,13),fill='#6b6246');dr.point((x,3),fill='#bca977')
  elif name=='moonstone_crate':
   dr.rectangle((1,4,14,14),fill='#755638',outline='#322b27')
   for x in [3,7,11]:dr.rectangle((x,5,x+1,12),fill='#a48255')
   dr.polygon([(3,4),(5,1),(9,2),(11,1),(13,5)],fill='#d6dbba',outline='#899878');dr.line((1,6,14,6),fill='#ceaa70');dr.line((1,12,14,12),fill='#ceaa70')
  elif name=='structural_frame':
   dr.rectangle((1,1,14,14),outline='#333b44',width=3);dr.rectangle((3,3,12,12),outline='#95653e',width=2)
   for x in [1,12]:
    for y in [1,12]:dr.rectangle((x,y,x+2,y+2),fill='#8b9b9f');dr.point((x,y),fill='#d6ddc6')
  elif name=='field_dressing':
   dr.polygon([(1,6),(6,1),(14,9),(9,14)],fill='#ddd4b7',outline='#665b4c');dr.line((4,5,11,12),fill='#a8997f',width=2);dr.line((8,5,11,8),fill='#588b80',width=2);dr.line((5,7,8,10),fill='#588b80',width=2)
  elif name=='nursery_bedding':
   dr.rounded_rectangle((1,5,14,14),2,fill='#c8ad77',outline='#59482f');dr.rectangle((2,3,13,9),fill='#e0cb96',outline='#92754c');dr.line((3,6,12,6),fill='#8c6450');dr.line((3,11,12,11),fill='#8c6450');dr.line((4,3,4,8),fill='#f1e5b7')
  elif name in ['prism_battery','volatile_magazine']:
   prism=name=='prism_battery';base='#4a6870' if prism else '#6d4e3c';bright='#95e6e4' if prism else '#ecaa52'
   dr.rounded_rectangle((4,2,11,14),2,fill=base,outline='#29343d');dr.rectangle((6,0,9,2),fill='#bec2ab');dr.rectangle((3,5,12,11),outline='#aca58a');dr.rectangle((6,4,9,11),fill=bright);dr.line((5,3,5,12),fill='#a3b6af');dr.line((7,5,7,9),fill='#f4efd5')
   if not prism:dr.rectangle((2,6,3,12),fill='#b77c42');dr.rectangle((12,6,13,12),fill='#b77c42')
  elif name=='kennel_wagon':
   dr.rectangle((2,5,12,11),fill='#906442',outline='#493927');dr.polygon([(1,5),(4,1),(10,1),(13,5)],fill='#d1bc8b',outline='#5b4b38');dr.line((2,8,12,8),fill='#c49a61');dr.line((12,10,15,10),fill='#956f43',width=2)
   for x in [3,10]:dr.ellipse((x-2,10,x+2,14),fill='#424c50',outline='#aeb5a5');dr.point((x,12),fill='#d8bf7b')
  else:
   dr.rounded_rectangle((2,2,13,13),2,fill=(*color,255),outline=(38,42,49,255));dr.line((4,3,11,3),fill=(229,218,167,255));dr.line((3,5,3,11),fill=(219,201,153,255))
   for y in range(5,12,3):dr.line((5,y,10,y),fill=(49,60,70,255))
  if name=='homeward_totem':
   dr.rectangle((6,0,9,5),fill=(214,178,83,255));dr.rectangle((0,6,15,8),fill=(214,178,83,255));dr.rectangle((5,4,6,5),fill=(92,227,207,255));dr.rectangle((9,4,10,5),fill=(92,227,207,255))
  im.save(A/'textures/item'/f'{name}.png');write(A,'models/item/'+name+'.json',{'parent':'minecraft:item/generated','textures':{'layer0':'bunnidogs:item/'+name}});write(A,'items/'+name+'.json',{'model':{'type':'minecraft:model','model':'bunnidogs:item/'+name}})
 im=Image.new('RGBA',(16,16));p=im.load()
 for x in range(16):
  for y in range(16):
   n=int(15*math.sin((x+y)/3))+random.Random(x*77+y).randint(-9,9);p[x,y]=(max(0,55+n),max(0,145+n),max(0,157+n),210)
 im.save(A/'textures/block/guild_veil.png')
 (A/'blockstates/guild_veil.json').write_text(json.dumps({'variants':{'':{'model':'bunnidogs:block/guild_veil'}}},indent=2)+'\n')
 (A/'models/block/guild_veil.json').write_text(json.dumps({'parent':'minecraft:block/block','render_type':'minecraft:translucent','textures':{'portal':'bunnidogs:block/guild_veil','particle':'bunnidogs:block/guild_veil'},'elements':[{'from':[0,0,7.95],'to':[16,16,8.05],'faces':{'north':{'texture':'#portal'},'south':{'texture':'#portal'}}}]},indent=2)+'\n')
 # wagon atlas: varied timber grain, iron/copper rims and warm fabric, not a flat recolour.
 atlas=Image.new('RGB',(256,256));px=atlas.load();rng=random.Random(7127)
 for x in range(256):
  for y in range(256):
   base=(84,63,44) if x<80 else (87,99,105);n=rng.randint(-15,15)+(8 if y%7==0 else 0);px[x,y]=tuple(max(0,min(255,c+n)) for c in base)
 atlas.save(A/'textures/entity/kennel_wagon.png')
 messages={
 'project.bunnidogs.occupied':('Result occupied','Resultado ocupado'),
 'project.bunnidogs.launch':('Launch','Despegar'),
 'project.bunnidogs.workpiece':('No workpiece','Sin pieza'),
 'project.bunnidogs.barding':('No barding','Sin armadura animal'),
 'project.bunnidogs.repaired':('Fully repaired','Reparación completa'),
 'project.bunnidogs.equipment':('No equipment','Sin equipo'),
 'project.bunnidogs.book':('No enchanted book','Sin libro encantado'),
 'project.bunnidogs.incompatible':('No compatible upgrade','Sin mejoras compatibles'),
 'project.bunnidogs.working':('Working','En proceso'),
 'project.bunnidogs.materials':('Missing materials','Faltan materiales'),
 'project.bunnidogs.ready':('Ready','Listo'),
 'project.bunnidogs.paused':('Paused by redstone','Pausado por redstone'),
 'project.bunnidogs.fuel':('Missing fuel','Falta combustible'),
 'project.bunnidogs.active':('Active','Activo'),
 'project.bunnidogs.shelter':('No sheltered pups nearby','Sin crías bajo techo cerca'),
 'project.bunnidogs.wings':('Glider required','Se necesita un planeador'),
 'message.bunnidogs.wagon.hitched':('Puller hitched.','Animal enganchado.'),
 'message.bunnidogs.wagon.unhitched':('Puller released.','Animal desenganchado.'),
 'message.bunnidogs.wagon.puller':('No available strong companion nearby.','No hay un compañero fuerte disponible cerca.'),
 'message.bunnidogs.guild.riding':('Leave the vehicle first.','Baja del vehículo primero.'),
 'message.bunnidogs.guild.arrived':('Lantern Warren: %s','Madriguera de los faroles: %s'),
 'message.bunnidogs.guild.renovated':('Updated %s bridge blocks.','Se renovaron %s bloques del puente.'),
 'tooltip.bunnidogs.homeward_totem':('Returns you to its Warren when it saves your life.','Te devuelve a su madriguera al salvarte la vida.'),
 'message.bunnidogs.guild.full':('There is not enough room for a full refill.','No queda espacio para una recarga completa.'),
 'message.bunnidogs.guild.reload':('Missing refill materials.','Faltan materiales para recargar.'),
 'message.bunnidogs.guild.units':('%s / %s units','%s / %s unidades'),
 'message.bunnidogs.guild.bound':('Bound to this Warren.','Vinculado a esta madriguera.'),
 'message.bunnidogs.guild.no_route':('No other Warren has joined the network yet.','Aún no hay otra madriguera en la red.'),
 'message.bunnidogs.guild.destination':('Destination: %s','Destino: %s'),
 'message.bunnidogs.guild.blocked':('The Warren landing is obstructed.','La llegada a la madriguera está obstruida.'),
 'tooltip.bunnidogs.prism_battery':('Sneak-use: 18 Cinderite + 36 Dust → 256 units.','Usar agachado: 18 cineritas + 36 polvos → 256 unidades.'),
 'tooltip.bunnidogs.volatile_magazine':('Sneak-use: 18 Cinderite, Fibre and Dust + one Volatile Heart → 64 units.','Usar agachado: 18 cineritas, fibras y polvos + un corazón volátil → 64 unidades.'),
 'block.bunnidogs.guild_veil':('Lantern Veil','Velo de los faroles'),
 'entity.bunnidogs.warren_citizen':('Lantern Warren Elder','Mayor de la madriguera de los faroles'),
 'entity.bunnidogs.kennel_wagon':('Kennel Wagon','Carreta de la manada')}
 for i,names in enumerate([('Rootmason','Maestro de raíces'),('Armourer','Armero'),('Quartermaster','Intendente'),('Waykeeper','Guardián de caminos')]):messages['entity.bunnidogs.guild_job_'+str(i)]=names
 for locale,idx in [('en_us',0),('es_pe',1)]:
  path=A/'lang'/f'{locale}.json';lang=json.loads(path.read_text())
  for name,names in NAMES.items():lang[('block' if name in BLOCKS else 'item')+'.bunnidogs.'+name]=names[idx]
  lang.update({key:value[idx] for key,value in messages.items()});path.write_text(json.dumps(lang,indent=2,ensure_ascii=False)+'\n')
 tag('minecraft','block/mineable/pickaxe.json',['bunnidogs:'+b for b in BLOCKS])
 write(D,'worldgen/structure/lantern_warren.json',{'type':'bunnidogs:lantern_warren','biomes':'#bunnidogs:great_warren','step':'underground_structures','spawn_overrides':{},'terrain_adaptation':'none'})
 write(D,'worldgen/structure_set/lantern_warrens.json',{'structures':[{'structure':'bunnidogs:lantern_warren','weight':1}],'placement':{'type':'minecraft:random_spread','spacing':32,'separation':16,'salt':827136045}})
if __name__=='__main__':generate()
