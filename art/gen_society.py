"""First sustainable households, the living ritual heart, and six physical hunt inlays."""
from pathlib import Path
import json,random
from PIL import Image,ImageDraw
ROOT=Path(__file__).resolve().parents[1];R=ROOT/'src/main/resources';A=R/'assets/bunnidogs';D=R/'data/bunnidogs'
def write(root,path,obj):
 p=root/path;p.parent.mkdir(parents=True,exist_ok=True);p.write_text(json.dumps(obj,ensure_ascii=False,indent=2)+'\n')
def recipe(name,pattern,key,count=1):
 write(D,Path('recipe')/(name+'.json'),{'type':'minecraft:crafting_shaped','category':'misc','pattern':pattern,'key':key,'result':{'id':'bunnidogs:'+name,'count':count}})
 item=next(iter(key.values()));item=item if not item.startswith('#') else 'bunnidogs:guava'
 write(D,Path('advancement/recipes/hearth')/(name+'.json'),{'parent':'minecraft:recipes/root','criteria':{'material':{'trigger':'minecraft:inventory_changed','conditions':{'items':[{'items':item}]}}},'requirements':[['material']],'rewards':{'recipes':['bunnidogs:'+name]}})
recipe('hearth_tree',[' L ','CSC','PPP'],{'L':'bunnidogs:guava_sapling','C':'minecraft:copper_ingot','S':'minecraft:amethyst_shard','P':'#minecraft:planks'})
recipe('steward_staff',['  C',' S ','S  '],{'C':'minecraft:copper_ingot','S':'minecraft:stick'})
recipe('guava_coppice',['BS'],{'B':'minecraft:bone_meal','S':'bunnidogs:guava_sapling'},2)
recipe('grafting_frame',['PBP','PBP'],{'P':'#minecraft:planks','B':'minecraft:bone_meal'})
recipe('hearth_incense',['GG','GP'],{'G':'bunnidogs:guava','P':'#minecraft:planks'},4)
recipe('hearth_poultice',[' G ','BPH'],{'G':'bunnidogs:guava','B':'minecraft:bone_meal','P':'minecraft:paper','H':'minecraft:honeycomb'},4)
# Former rare offerings remain valuable optional crafting stock, never a gate or a consumed pet requirement.
for legend in ['titan_haunch','gilded_foot','moon_sinew','listening_shell','everbloom']:
 write(D,Path('recipe')/('incense_from_'+legend+'.json'),{'type':'minecraft:crafting_shapeless','category':'misc','ingredients':['bunnidogs:'+legend],'result':{'id':'bunnidogs:hearth_incense','count':64}})
 # Optional conversion appears naturally in the recipe book when the old rare item is found.
 write(D,Path('advancement/recipes/hearth')/('incense_from_'+legend+'.json'),{'parent':'minecraft:recipes/root','criteria':{'legend':{'trigger':'minecraft:inventory_changed','conditions':{'items':[{'items':'bunnidogs:'+legend}]}}},'requirements':[['legend']],'rewards':{'recipes':['bunnidogs:incense_from_'+legend]}})
p=R/'data/minecraft/tags/block/mineable/axe.json';obj=json.loads(p.read_text()) if p.exists() else {'replace':False,'values':[]};obj['values']=list(dict.fromkeys(obj['values']+['bunnidogs:hearth_tree','bunnidogs:guava_coppice']));write(R/'data/minecraft/tags/block/mineable',Path('axe.json'),obj)
# Original pixel art; the tree uses its physical roots, graft frame and fruit to show development.
colors=['#e8c77c','#a6d6ec','#a6c171','#ee9071','#d3a1df','#cce9e4']
for name in ['living_keystone','grafting_frame','hearth_incense','steward_staff','hearth_poultice']:
 im=Image.new('RGBA',(32,32));d=ImageDraw.Draw(im)
 if name=='living_keystone':
  d.polygon([(16,2),(28,11),(25,26),(16,31),(7,26),(4,11)],fill='#b0c982',outline='#fff2bd');d.line((16,27,16,10,10,5),fill='#745943',width=3);d.line((16,19,23,11),fill='#745943',width=3)
  for x,y in [(10,12),(19,7),(20,19)]:d.ellipse((x,y,x+6,y+6),fill='#f1d6a5',outline='#fff5d1')
 elif name=='grafting_frame':
  d.rectangle((4,3,27,27),outline='#c49261',width=3);d.line((4,5,27,26),fill='#b8bd8b',width=2);d.line((27,5,4,26),fill='#b8bd8b',width=2)
 elif name=='hearth_incense':
  for x in [7,14,21]:d.line((x,28,x+3,10),fill='#c19364',width=3);d.ellipse((x,6,x+5,12),fill='#bed39a');d.arc((x-2,0,x+6,7),0,230,fill='#dac6b3')
 elif name=='steward_staff':d.line((5,30,22,6),fill='#bb976d',width=4);d.arc((13,1,29,17),160,440,fill='#d5a473',width=3);d.ellipse((18,5,25,12),fill='#accd92')
 else:d.polygon([(6,5),(25,5),(29,15),(24,28),(5,27),(2,15)],fill='#d7c9a7',outline='#f3e6c8');d.line((4,11,26,21),fill='#b1a187',width=3);d.ellipse((12,9,22,21),fill='#a6c171')
 im.save(A/'textures/item'/f'{name}.png');write(A,Path('models/item')/(name+'.json'),{'parent':'minecraft:item/handheld' if name=='steward_staff' else 'minecraft:item/generated','textures':{'layer0':'bunnidogs:item/'+name}});write(A,Path('items')/(name+'.json'),{'model':{'type':'minecraft:model','model':'bunnidogs:item/'+name}})
def box(a,b,tex):return {'from':a,'to':b,'faces':{f:{'texture':'#'+tex,'uv':[0,0,16,16]} for f in ['up','down','north','south','east','west']}}
for stage in range(5):
 elements=[box([0,0,0],[16,5,16],'store'),box([6,5,6],[10,24,10],'wood'),box([3,18,3],[13,27,13],'leaf')]
 if stage>=1:elements += [box([3,5,3],[5,19,5],'frame'),box([11,5,11],[13,19,13],'frame'),box([3,17,3],[13,19,5],'frame')]
 if stage>=2:elements += [box([1,21,4],[7,29,12],'leaf'),box([9,21,4],[15,29,12],'leaf')]
 if stage>=3:elements += [box([4,4,4],[6,10,6],'glow'),box([10,4,10],[12,10,12],'glow')]
 if stage==4:elements += [box([11,17,5],[15,22,9],'fruit'),box([1,17,7],[5,22,11],'fruit')]
 write(A,Path('models/block')/f'hearth_tree_{stage}.json',{'parent':'minecraft:block/block','textures':{'particle':'bunnidogs:block/guava_log','wood':'bunnidogs:block/guava_log','leaf':'bunnidogs:block/hearth_leaf','store':'bunnidogs:block/hearth_store','frame':'bunnidogs:block/hearth_frame','glow':'bunnidogs:block/hearth_glow','fruit':'bunnidogs:block/hearth_fruit'},'elements':elements})
for name,color in [('leaf','#91ad75'),('store','#8b684d'),('frame','#b6ad80'),('glow','#d2e8ab'),('fruit','#eed5a0')]:
 im=Image.new('RGB',(16,16),color);d=ImageDraw.Draw(im);d.rectangle((0,0,15,15),outline='#536c4e' if name=='leaf' else '#c2a77e');rng=random.Random(513)
 for i in range(30):x=rng.randrange(16);y=rng.randrange(16);d.point((x,y),fill='#adc38b' if name=='leaf' else '#e0c897')
 im.save(A/'textures/block'/f'hearth_{name}.png')
write(A,Path('blockstates/hearth_tree.json'),{'variants':{f'stage={n}':{'model':f'bunnidogs:block/hearth_tree_{n}'} for n in range(5)}})
write(A,Path('models/item/hearth_tree.json'),{'parent':'bunnidogs:block/hearth_tree_0'});write(A,Path('items/hearth_tree.json'),{'model':{'type':'minecraft:model','model':'bunnidogs:item/hearth_tree'}})
for age in range(8):
 h=5+age*2
 write(A,Path('models/block')/f'guava_coppice_{age}.json',{'parent':'minecraft:block/block','textures':{'particle':'bunnidogs:block/guava_log','wood':'bunnidogs:block/guava_log','leaf':'bunnidogs:block/hearth_leaf'},'elements':[box([6,0,6],[10,h,10],'wood'),box([2,h-4,2],[14,h+3,14],'leaf')]})
write(A,Path('blockstates/guava_coppice.json'),{'variants':{f'age={n}':{'model':f'bunnidogs:block/guava_coppice_{n}'} for n in range(8)}})
write(A,Path('models/item/guava_coppice.json'),{'parent':'bunnidogs:block/guava_coppice_0'});write(A,Path('items/guava_coppice.json'),{'model':{'type':'minecraft:model','model':'bunnidogs:item/guava_coppice'}})
for name in ['hearth_tree','guava_coppice']:
 write(D,Path('loot_table/blocks')/(name+'.json'),{'type':'minecraft:block','pools':[{'rolls':1,'entries':[{'type':'minecraft:item','name':'bunnidogs:'+name}],'conditions':[{'condition':'minecraft:survives_explosion'}]}]})
# Roots on the reusable Crucible; all earlier unrooted geometry stays recognizable.
old=json.loads((A/'models/block/apex_crucible.json').read_text());rooted=json.loads(json.dumps(old));rooted.setdefault('textures',{})['heart']='bunnidogs:block/hearth_glow';rooted.setdefault('elements',[]).extend([box([2,13,2],[6,16,6],'heart'),box([10,13,10],[14,16,14],'heart')]);write(A,Path('models/block/apex_crucible_rooted.json'),rooted)
write(A,Path('blockstates/apex_crucible.json'),{'variants':{'rooted=false':{'model':'bunnidogs:block/apex_crucible'},'rooted=true':{'model':'bunnidogs:block/apex_crucible_rooted'}}})
# Each partial hunt record is physically visible in six colored sockets on the Anchor.
for active in [False,True]:
 for mask in range(64):
  base='warren_anchor_active' if active else 'warren_anchor';im=Image.open(A/'textures/block'/f'{base}.png').convert('RGB');d=ImageDraw.Draw(im)
  for i,col in enumerate(colors):
   x=2+i%3*4;y=2+i//3*10;d.rectangle((x,y,x+2,y+2),fill=col if mask&(1<<i) else '#263d3f',outline='#eedbb4' if mask&(1<<i) else '#71817a')
  name=f'anchor_{int(active)}_{mask}';im.save(A/'textures/block'/f'{name}.png');write(A,Path('models/block')/(name+'.json'),{'parent':'minecraft:block/cube_all','textures':{'all':'bunnidogs:block/'+name}})
write(A,Path('blockstates/warren_anchor.json'),{'variants':{f'active={str(a).lower()},marks={m}':{'model':f'bunnidogs:block/anchor_{int(a)}_{m}'} for a in [False,True] for m in range(64)}})
en={'block.bunnidogs.hearth_tree':'Hearth Tree','block.bunnidogs.guava_coppice':'Guava Coppice','item.bunnidogs.living_keystone':'Living Keystone','item.bunnidogs.grafting_frame':'Grafting Frame','item.bunnidogs.hearth_incense':'Hearth Incense','item.bunnidogs.steward_staff':"Steward’s Staff",'item.bunnidogs.hearth_poultice':'Hearth Poultice','tooltip.bunnidogs.living_keystone':'Grown by a self-sustaining household. Installs permanently in an Apex Crucible.','tooltip.bunnidogs.steward_staff':'Bind to a Hearth; link residents and work sites. Sneak-use the Hearth to survey a shelter plot.','tooltip.bunnidogs.hunt_marks':'Ground hunts %s/5 · Flying hunt: %s','message.bunnidogs.hunt.flying':'any flying Apex','message.bunnidogs.hunt.missing':'The Anchor still needs these hunts: %s','message.bunnidogs.hunt.earned':'Resonance earned: %s','care.bunnidogs.apex.hearth':'Living Keystone · %s','care.bunnidogs.apex.next.keystone':'Install a Living Keystone from a Hearth Tree.','care.bunnidogs.apex.next.fuel':'Bring Hearth Incense.','care.bunnidogs.apex.cost':'Consumes one source sample and one Hearth Incense. The Keystone remains.','care.bunnidogs.apex.focus_cost':'Consumes one sigil, one source sample and one Hearth Incense. The Keystone remains.','tooltip.bunnidogs.apex_crest.anchor':'Your five ground hunts and one flying hunt awaken a Warren Anchor.'}
es={'block.bunnidogs.hearth_tree':'Árbol del Hogar','block.bunnidogs.guava_coppice':'Guayabo de rebrote','item.bunnidogs.living_keystone':'Piedra angular viva','item.bunnidogs.grafting_frame':'Soporte de injerto','item.bunnidogs.hearth_incense':'Incienso del hogar','item.bunnidogs.steward_staff':'Bastón del cuidador','item.bunnidogs.hearth_poultice':'Cataplasma del hogar','tooltip.bunnidogs.living_keystone':'Cultivada por una comunidad autosuficiente. Se instala de forma permanente en un crisol de ápex.','tooltip.bunnidogs.steward_staff':'Vincula un árbol, residentes y lugares de trabajo. Usa el bastón agachado en el árbol para trazar un refugio.','tooltip.bunnidogs.hunt_marks':'Cacerías terrestres %s/5 · Cacería aérea: %s','message.bunnidogs.hunt.flying':'cualquier ápex volador','message.bunnidogs.hunt.missing':'Al ancla todavía le faltan estas cacerías: %s','message.bunnidogs.hunt.earned':'Resonancia obtenida: %s','care.bunnidogs.apex.hearth':'Piedra angular viva · %s','care.bunnidogs.apex.next.keystone':'Instala una piedra angular viva de un Árbol del Hogar.','care.bunnidogs.apex.next.fuel':'Necesitas incienso del hogar.','care.bunnidogs.apex.cost':'Consume una muestra de origen y un incienso. La piedra angular permanece.','care.bunnidogs.apex.focus_cost':'Consume un sello, una muestra de origen y un incienso. La piedra angular permanece.','tooltip.bunnidogs.apex_crest.anchor':'Tus cinco cacerías terrestres y una aérea despiertan un ancla de madriguera.'}
msgs={'selected':('Hearth selected.','Árbol seleccionado.'),'select':('Choose a Hearth Tree first.','Selecciona primero un Árbol del Hogar.'),'linked':('Work site linked.','Lugar de trabajo vinculado.'),'site_refused':('Link a nearby trough, cradle or composter.','Vincula un comedero, una cuna o un compostador cercano.'),'joined':('Joined the household.','Se unió a la comunidad.'),'left':('Left the household.','Dejó la comunidad.'),'invite_refused':('Bring an adult you can care for near your Hearth.','Acerca a tu árbol un adulto que puedas cuidar.'),'plot_blocked':('The shelter or orchard plot is obstructed.','El terreno del refugio o del huerto está obstruido.'),'confirm_plot':('Shelter and orchard marked. Sneak-use again to authorize this plot.','Refugio y huerto marcados. Usa otra vez el bastón agachado para autorizar la obra.'),'authorized':('The crew may build and plant within the marked plot.','Los residentes ya pueden construir y plantar en el terreno marcado.')}
needs={'plot':('A shelter plot awaits authorization.','Falta autorizar el terreno del refugio.'),'orchard':('The household needs a coppice grower and a wooden axe.','La comunidad necesita un cultivador de rebrotes y un hacha de madera.'),'craft':('A craftsperson must make a grafting frame.','Un artesano debe fabricar un soporte de injerto.'),'harvest':('The roots await a renewable harvest.','Las raíces esperan una cosecha renovable.'),'haul':('A hauler must bring guavas from a linked trough.','Un transportista debe traer guayabas de un comedero vinculado.'),'meals':('The household is sharing its meals.','La comunidad debe compartir sus alimentos.'),'shelter':('The crew needs a finished shelter and nests.','Los residentes necesitan terminar el refugio y sus nidos.'),'family':('Caretakers must raise the next generation.','Los cuidadores deben criar la siguiente generación.'),'teaching':('An offspring must learn and work independently.','Una cría debe aprender y trabajar de forma independiente.'),'fruit':('The graft needs guavas, bone meal and frames.','El injerto necesita guayabas, polvo de hueso y soportes.')}
for k,(a,b) in msgs.items():en['message.bunnidogs.hearth.'+k]=a;es['message.bunnidogs.hearth.'+k]=b
for k,(a,b) in needs.items():en['message.bunnidogs.hearth.need.'+k]=a;es['message.bunnidogs.hearth.need.'+k]=b
en.update({'message.bunnidogs.hunt.complete':'All six resonances are yours. The Warren Anchor is ready.',
 'tooltip.bunnidogs.apex_crest.use':'Use to read your hunts; sneak-use to inspect the source.',
 'message.bunnidogs.apex.ritual':'A Living Keystone roots the Crucible. Incense and a source sample awaken an Apex.',
 'message.bunnidogs.apex.missing':'The Crucible needs a Living Keystone from a Hearth Tree.',
 'tooltip.bunnidogs.apex_offering.use':'Rare keepsake or crafting stock for Hearth Incense.',
 'guide.bunnidogs.page.21.body':'A self-sustaining Hearth household grows a Living Keystone. Install it once in an Apex Crucible. Offer a source sample and Hearth Incense; a role sigil selects the encounter. Review, then summon. The Keystone stays.',
 'guide.bunnidogs.page.22.body':'Invite residents and link work sites with a bound Steward’s Staff. Authorize the marked shelter and orchard plot. Crops, hauling, shared meals, paid crafting/building and a resident-taught new generation cultivate the Keystone. Five ground Apex hunts plus any flying one awaken the Warren Anchor.',
 'care.bunnidogs.trophy.offering':'A rare keepsake that can also become Hearth Incense.',
 'care.bunnidogs.apex.next.ready':'The rooted Crucible is ready.',
 'message.bunnidogs.hearth.need.haul':'A hauler must supply linked troughs from communal stores.'})
es.update({'message.bunnidogs.hunt.complete':'Ya tienes las seis resonancias. El ancla de madriguera está lista.',
 'tooltip.bunnidogs.apex_crest.use':'Úsalo para ver tus cacerías; úsalo agachado para inspeccionar el origen.',
 'message.bunnidogs.apex.ritual':'Una piedra angular viva arraiga el crisol. El incienso y una muestra de origen despiertan un ápex.',
 'message.bunnidogs.apex.missing':'El crisol necesita una piedra angular viva de un Árbol del Hogar.',
 'tooltip.bunnidogs.apex_offering.use':'Recuerdo poco común o ingrediente para incienso del hogar.',
 'guide.bunnidogs.page.21.body':'Una comunidad autosuficiente cultiva una piedra angular viva. Instálala una vez en un crisol de ápex. Ofrece una muestra y un incienso; un sello elige el tipo de ápex. Revisa e invoca. La piedra angular permanece.',
 'guide.bunnidogs.page.22.body':'Invita residentes y vincula lugares de trabajo con el bastón. Autoriza el refugio y el huerto marcados. La cosecha, el transporte, los alimentos compartidos, la fabricación, la construcción y una generación enseñada por residentes permiten cultivar la piedra angular. Cinco cacerías terrestres y una aérea despiertan el ancla.',
 'care.bunnidogs.trophy.offering':'Un recuerdo poco común que también puede convertirse en incienso del hogar.',
 'care.bunnidogs.apex.next.ready':'El crisol arraigado está listo.',
 'message.bunnidogs.hearth.need.haul':'Un transportista debe abastecer los comederos desde las reservas de la comunidad.'})
for locale,new in [('en_us',en),('es_pe',es)]:
 p=A/'lang'/f'{locale}.json';obj=json.loads(p.read_text());obj.update(new);p.write_text(json.dumps(obj,ensure_ascii=False,indent=2)+'\n')
sheet=Image.new('RGB',(1000,420),'#29332f');d=ImageDraw.Draw(sheet);d.text((20,15),'Hearth communities - original items and six earned hunt sockets',fill='white')
for i,name in enumerate(['living_keystone','grafting_frame','hearth_incense','steward_staff','hearth_poultice']):
 im=Image.open(A/'textures/item'/f'{name}.png').resize((128,128),Image.Resampling.NEAREST);sheet.paste(im,(20+i*190,55),im);d.text((20+i*190,190),name.replace('_',' '),fill='white')
for i,mask in enumerate([0,1,7,31,63]):sheet.paste(Image.open(A/'textures/block'/f'anchor_1_{mask}.png').resize((96,96),Image.Resampling.NEAREST),(35+i*190,250))
sheet.save(ROOT/'art/society_preview.png')

# Personal delivery lessons extend the household without rewriting its original progression.
import runpy
runpy.run_path(str(ROOT/"art/gen_delivery.py"))
