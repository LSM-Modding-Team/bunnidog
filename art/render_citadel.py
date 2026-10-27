"""Headless architectural inspection of the shipped native Crown templates, through Blender.
No concept-image generation. Records exact template hashes. Native light propagation/terrain are
not simulated; block shapes below are geometric approximations for architectural review.
"""
from pathlib import Path
import gzip,struct,json,hashlib,zipfile,pickle,subprocess,sys,math,os,tempfile,shutil
ROOT=Path(__file__).resolve().parents[1];TMP=Path(os.environ.get('CITADEL_RENDER_CACHE') or tempfile.mkdtemp(prefix='bunnidog-citadel-render-'));TMP.mkdir(exist_ok=True)
def nbt(data):
 p=0
 def read(n):
  nonlocal p
  v=data[p:p+n];p+=n;return v
 def u16():return struct.unpack('>H',read(2))[0]
 def utf():return read(u16()).decode()
 def val(t):
  if t==3:return struct.unpack('>i',read(4))[0]
  if t==8:return utf()
  if t==9:
   k=read(1)[0];return [val(k) for _ in range(val(3))]
  if t==10:
   d={}
   while (k:=read(1)[0]):
    name=utf();d[name]=val(k)
   return d
  raise ValueError(t)
 assert read(1)==b'\x0a';utf();return val(10)
def prepare():
 meta=json.loads((ROOT/'src/main/resources/data/bunnidogs/structure/crown_citadel.json').read_text());cells={};states=[];ids={};hashes={}
 for ox,oy,oz,name in meta['tiles']:
  path=ROOT/'src/main/resources/data/bunnidogs/structure'/f'{name}.nbt';raw=path.read_bytes();hashes[name]=hashlib.sha256(raw).hexdigest();t=nbt(gzip.decompress(raw))
  palette=[]
  for s in t['palette']:
   key=json.dumps(s,sort_keys=True)
   if key not in ids:ids[key]=len(states);states.append(s)
   palette.append(ids[key])
  for b in t['blocks']:
   x,y,z=b['pos'];sid=palette[b['state']]
   if states[sid]['Name']!='minecraft:air':cells[x+ox,y+oy,z+oz]=sid
 # Textures from the resolved client and mod resources. Use the native texture dictionary,
 # with explicit face choices for common architectural blocks; nearest sampling preserves pixels.
 candidates=[Path(os.environ.get('BUNNIDOG_CLIENT_JAR','/nonexistent')),ROOT.parent/'.bunnidog-setup/gradle/caches/neoformruntime/artifacts/minecraft_26.2_client.jar',Path(os.environ.get('GRADLE_USER_HOME',str(Path.home()/'.gradle')))/'caches/neoformruntime/artifacts/minecraft_26.2_client.jar']
 client_path=next((p for p in candidates if p.is_file()),None)
 if client_path is None:raise FileNotFoundError('Resolve the Minecraft client through Gradle, or set BUNNIDOG_CLIENT_JAR to its client JAR.')
 client=zipfile.ZipFile(client_path)
 mats={};verts={};faces={};uvs={};lights=[]
 def texture(name,face):
  namespace,b=name.split(':');suffix=''
  if b in ['grass_block','hay_block','bookshelf','barrel','lectern','crafting_table','smithing_table','furnace','blast_furnace','chest','ender_chest']:
   if b=='hay_block':b+='_'+('top' if face in [4,5] else 'side')
   elif b=='bookshelf':b='dark_oak_planks' if face in [4,5] else b
   elif b in ['chest','ender_chest']:b='dark_oak_planks' if b=='chest' else 'obsidian'
   elif b=='lectern':b='oak_planks'
   else:b+='_'+('top' if face==5 else 'bottom' if face==4 else 'side')
  for end in ['_stairs','_slab','_fence']:
   if b.endswith(end):
    b=b[:-len(end)];b=({'tuff_brick':'tuff_bricks','dark_oak':'dark_oak_planks'}).get(b,b)
  if b=='chain':b='iron_block'
  if b=='lantern':b='glowstone'
  if b=='anvil':b='anvil_top' if face==5 else 'anvil'
  key=f'{namespace}:{b}'
  if key in mats:return key
  rel=f'assets/{namespace}/textures/block/{b}.png';dest=TMP/f'{namespace}_{b}.png'
  if namespace=='bunnidogs':
   p=ROOT/'src/main/resources'/rel
   if not p.exists():p=ROOT/'src/main/resources/assets/bunnidogs/textures/block/rootstone.png'
   dest.write_bytes(p.read_bytes())
  else:
   try:dest.write_bytes(client.read(rel))
   except KeyError:dest.write_bytes(client.read('assets/minecraft/textures/block/tuff_bricks.png'))
  mats[key]={'texture':str(dest),'glass':'glass' in name,'emission':any(s in name for s in ['lantern','froglight','shroomlight','rootlight'])}
  return key
 # Minecraft coordinates converted to Blender x,-z,y; UVs consistently one native block.
 facepoints=[[(0,0,0),(0,1,0),(0,1,1),(0,0,1)],[(1,0,1),(1,1,1),(1,1,0),(1,0,0)],[(0,0,0),(0,0,1),(1,0,1),(1,0,0)],[(1,1,0),(1,1,1),(0,1,1),(0,1,0)],[(0,1,0),(0,0,0),(1,0,0),(1,1,0)],[(0,0,1),(0,1,1),(1,1,1),(1,0,1)]]
 # Here facepoints use x,z,y; neighbors match native x,y,z.
 neighbors=[(-1,0,0),(1,0,0),(0,0,-1),(0,0,1),(0,-1,0),(0,1,0)]
 def solid(s):return not any(k in s['Name'] for k in ['fence','stairs','slab','chain','lantern','anvil','lectern','glass'])
 def cube(pos,state,lo=(0,0,0),hi=(1,1,1),cull=True):
  x,y,z=pos;name=state['Name']
  for f,fp in enumerate(facepoints):
   dx,dy,dz=neighbors[f];other=cells.get((x+dx,y+dy,z+dz))
   if cull and other is not None and (solid(states[other]) or states[other]['Name']==name):continue
   key=texture(name,f);vv=verts.setdefault(key,[]);ff=faces.setdefault(key,[]);base=len(vv)
   for a,b,c in fp:vv.append((x+lo[0]+a*(hi[0]-lo[0]),-(z+lo[2]+b*(hi[2]-lo[2])),y+lo[1]+c*(hi[1]-lo[1])))
   ff.append((base,base+1,base+2,base+3))
 for pos,sid in cells.items():
  state=states[sid];name=state['Name'];props=state.get('Properties',{})
  if 'fence' in name:
   cube(pos,state,(.375,0,.375),(.625,1,.625),False)
   if props.get('north')=='true' or props.get('south')=='true':cube(pos,state,(.43,.4,0),(.57,.875,1),False)
   if props.get('east')=='true' or props.get('west')=='true':cube(pos,state,(0,.4,.43),(1,.875,.57),False)
  elif name.endswith('chain'):cube(pos,state,(.46,0,.46),(.54,1,.54),False)
  elif name.endswith('lantern'):
   cube(pos,state,(.3,0,.3),(.7,.65,.7),False);lights.append((pos[0]+.5,-pos[2]-.5,pos[1]+.3))
  elif name.endswith('_trapdoor'):
   cube(pos,state,(0,0,0),(1,.1875,1),False)
  elif name.endswith('_stairs'):
   cube(pos,state,(0,0,0),(1,.5,1),False);lo=[0,.5,0];hi=[1,1,1];f=props.get('facing')
   if f=='north':hi[2]=.5
   elif f=='south':lo[2]=.5
   elif f=='east':lo[0]=.5
   else:hi[0]=.5
   cube(pos,state,lo,hi,False)
  else:cube(pos,state)
 payload={'materials':mats,'vertices':verts,'faces':faces,'lights':lights}
 with (TMP/'mesh.pickle').open('wb') as f:pickle.dump(payload,f,protocol=4)
 (TMP/'provenance.json').write_text(json.dumps({'renderer':'Blender Cycles CPU; native template cells and source textures; approximate minor block shapes, neutral ambient material fill for readable geometry; no terrain or client light simulation','template_sha256':hashes,'solid_cells':len(cells),'faces':sum(map(len,faces.values()))},indent=2)+'\n')
 print(f'Prepared {len(cells):,} cells / {sum(map(len,faces.values())):,} faces',flush=True)
def blender():
 import bpy
 from mathutils import Vector
 with (TMP/'mesh.pickle').open('rb') as f:p=pickle.load(f)
 bpy.ops.object.select_all(action='SELECT');bpy.ops.object.delete(use_global=False)
 for key,data in p['materials'].items():
  m=bpy.data.materials.new(key);m.use_nodes=True;n=m.node_tree.nodes;bs=n.get('Principled BSDF');im=n.new('ShaderNodeTexImage');im.image=bpy.data.images.load(data['texture']);im.interpolation='Closest';m.node_tree.links.new(im.outputs['Color'],bs.inputs['Base Color']);bs.inputs['Roughness'].default_value=.8
  if data['glass']:
   bs.inputs['Alpha'].default_value=.24;m.blend_method='BLEND';bs.inputs['Transmission Weight'].default_value=.4
  m.node_tree.links.new(im.outputs['Color'],bs.inputs['Emission Color']);bs.inputs['Emission Strength'].default_value=.65 if data['emission'] else .16
  mesh=bpy.data.meshes.new(key);mesh.from_pydata(p['vertices'][key],[],p['faces'][key]);mesh.materials.append(m);mesh.update()
  uv=mesh.uv_layers.new(name='native_block_uv')
  for poly in mesh.polygons:
   for li,xy in zip(poly.loop_indices,[(0,0),(1,0),(1,1),(0,1)]):uv.data[li].uv=xy
  ob=bpy.data.objects.new(key,mesh);bpy.context.collection.objects.link(ob)
 for i,loc in enumerate(p['lights']):
  data=bpy.data.lights.new('lantern','POINT');data.energy=70;data.color=(1,.77,.49);data.shadow_soft_size=.6;ob=bpy.data.objects.new('lantern',data);bpy.context.collection.objects.link(ob);ob.location=loc
 scene=bpy.context.scene;scene.render.engine='CYCLES';scene.cycles.device='CPU';scene.cycles.samples=8;scene.cycles.max_bounces=4;scene.cycles.use_denoising=False
 scene.world.use_nodes=True;scene.world.node_tree.nodes['Background'].inputs[0].default_value=(.65,.74,.85,1);scene.world.node_tree.nodes['Background'].inputs[1].default_value=.6
 sun=bpy.data.lights.new('daylight','SUN');sun.energy=2.5;ob=bpy.data.objects.new('daylight',sun);bpy.context.collection.objects.link(ob);ob.rotation_euler=(.5,-.5,-.5)
 scene.view_settings.view_transform='Standard';scene.view_settings.look='Medium High Contrast' if bpy.app.version<(4,0,0) else 'Medium High Contrast';scene.view_settings.exposure=0;scene.view_settings.gamma=1
 scene.render.resolution_percentage=100;scene.render.image_settings.file_format='PNG'
 camdata=bpy.data.cameras.new('architectural_review');cam=bpy.data.objects.new('camera',camdata);bpy.context.collection.objects.link(cam);scene.camera=cam
 out=ROOT/'art/citadel';out.mkdir(exist_ok=True)
 views=[('exterior',(200,-260,183),(0,0,15),42,1280,960),('gatehouse',(0,-141,5),(0,-91,12),24,1200,780),('courtyard',(-16,-88,3),(0,-44,16),20,1200,780),('library',(-91,-34,2.62),(-78,-15,9),20,1200,780),('stair_gallery',(-58,-36,4.62),(-61,-11,11),20,1200,780),('observatory',(-83,0,11),(-83,34,23),20,1200,780),('nurseries',(80,-29,2.62),(88,11,8),20,1200,780),('nursery_room',(72,50,6.62),(72,31,8),20,1200,780),('furnace',(17,78,-5),(-8,94,5),20,1200,780),('crown_hall',(-28,-35,2.62),(0,0,19),20,1200,780)]
 requested=[a for a in sys.argv[sys.argv.index('--')+1:] if a!='--blender'] if '--' in sys.argv else []
 for name,pos,target,lens,w,h in views:
  if requested and name not in requested:continue
  cam.location=pos;cam.rotation_euler=(Vector(target)-cam.location).to_track_quat('-Z','Y').to_euler();camdata.lens=lens;camdata.clip_end=700;camdata.clip_start=.08;scene.render.resolution_x=w;scene.render.resolution_y=h;scene.render.filepath=str(out/(name+'.png'));bpy.ops.render.render(write_still=True)
 (out/'provenance.json').write_bytes((TMP/'provenance.json').read_bytes())
def contact_sheet():
 from PIL import Image,ImageDraw,ImageFont,ImageOps
 out=ROOT/'art/citadel';sheet=Image.new('RGB',(2000,2110),'#19262c');draw=ImageDraw.Draw(sheet)
 try:font=ImageFont.truetype('/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf',24)
 except OSError:font=ImageFont.load_default()
 for i,(name,label) in enumerate([('exterior','Crown citadel'),('courtyard','Court and throne threshold'),('library','Library and upper gallery'),('observatory','Glass Gallery'),('nurseries','Brood court and nursery terraces'),('furnace','Heart Furnace')]):
  x=20+(i%2)*1000;y=20+(i//2)*685;draw.text((x,y),label,font=font,fill='#f2e8cc')
  preview=ImageOps.contain(Image.open(out/(name+'.png')).convert('RGB'),(960,630))
  sheet.paste(preview,(x+(960-preview.width)//2,y+38+(630-preview.height)//2))
 draw.text((20,2080),'Native template geometry · headless Blender inspection · terrain/client lighting not simulated',font=font,fill='#c2ced0')
 sheet.save(out/'overview.png')

if __name__=='__main__':
 if '--blender' in sys.argv:blender()
 else:
  prepare()
  binary=shutil.which('blender')
  if binary is None:raise FileNotFoundError('Blender 4.3 or later must be available on PATH.')
  priority=[shutil.which('nice'),'-n','10'] if shutil.which('nice') else []
  subprocess.run([*priority,binary,'-b','-t','4','--python',__file__,'--','--blender',*sys.argv[1:]],check=True,env=dict(os.environ,CITADEL_RENDER_CACHE=str(TMP)))
  contact_sheet()
