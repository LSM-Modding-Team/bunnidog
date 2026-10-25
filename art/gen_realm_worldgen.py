"""Generate the Great Warren biome, terrain and placement data from resolved native defaults."""
from pathlib import Path
import json,zipfile,copy
root=Path(__file__).resolve().parents[1];data=root/'src/main/resources/data/bunnidogs'
def write(path,obj):
 p=data/path;p.parent.mkdir(parents=True,exist_ok=True);p.write_text(json.dumps(obj,indent=2)+'\n')
z=zipfile.ZipFile(root/'build/moddev/artifacts/minecraft-patched-26.2.0.88.jar')
base=json.loads(z.read('data/minecraft/worldgen/biome/forest.json'))
regions=[('crownroot_groves','#c7a3da','#a8b887',[("apex",24),("prismmaw",18),("broodcaller",12),("blastmaw",10),("lifebinder",8),("flying_apex",8),("flying_prismmaw",4),("flying_broodcaller",2),("flying_blastmaw",2),("flying_lifebinder",2)]),('glassbloom_flats','#b5c9e8','#d3ccb4',[("apex",14),("prismmaw",30),("broodcaller",6),("blastmaw",6),("lifebinder",8),("flying_apex",6),("flying_prismmaw",12),("flying_broodcaller",2),("flying_blastmaw",2),("flying_lifebinder",4)]),('thunder_hollows','#d7a994','#97877a',[("apex",20),("prismmaw",8),("broodcaller",10),("blastmaw",26),("lifebinder",8),("flying_apex",6),("flying_prismmaw",2),("flying_broodcaller",2),("flying_blastmaw",6),("flying_lifebinder",2)])]
for i,(name,sky,grass,spawns) in enumerate(regions):
 b=copy.deepcopy(base);b['attributes']['minecraft:visual/sky_color']=sky;b['attributes']['minecraft:visual/fog_color']='#9884a6';b['effects'].update({'grass_color':grass,'foliage_color':'#b5bc80','water_color':'#719c9a'});b['has_precipitation']=False;b['temperature']=.7;b['downfall']=.4
 b['features']=[[],[],[],[],[],[],[],[],[],['bunnidogs:realm_landmarks'],[]];b['carvers']=[];b['spawners']={'monster':[{'type':'bunnidogs:'+mob,'weight':weight,'minCount':1 if mob.startswith('flying') else 2,'maxCount':1 if mob.startswith('flying') else 3} for mob,weight in spawns]+[{'type':'minecraft:enderman','weight':10,'minCount':1,'maxCount':2}]};b['spawn_costs']={}
 write(Path('worldgen/biome')/(name+'.json'),b)
noise=json.loads(z.read('data/minecraft/worldgen/noise_settings/overworld.json'));noise['sea_level']=40
# The custom generator owns solid columns, moons and bridges. Native noise is used only for biome climate.
custom=[]
for name,top in [('glassbloom_flats','minecraft:calcite'),('thunder_hollows','minecraft:polished_basalt'),('crownroot_groves','minecraft:moss_block')]:
 custom.append({'type':'minecraft:condition','if_true':{'type':'minecraft:biome','biome_is':['bunnidogs:'+name]},'then_run':{'type':'minecraft:condition','if_true':{'type':'minecraft:above_preliminary_surface'},'then_run':{'type':'minecraft:condition','if_true':{'type':'minecraft:stone_depth','offset':0,'surface_type':'floor','add_surface_depth':False,'secondary_depth_range':0},'then_run':{'type':'minecraft:block','result_state':{'Name':top}}}}})
noise['surface_rule']={'type':'minecraft:sequence','sequence':custom+[noise['surface_rule']]};write(Path('worldgen/noise_settings/great_warren.json'),noise)
dimtype=json.loads(z.read('data/minecraft/dimension_type/overworld.json'));dimtype['ambient_light']=.12;dimtype['monster_spawn_light_level']={'type':'minecraft:uniform','min_inclusive':0,'max_inclusive':15};dimtype['attributes']['minecraft:gameplay/nether_portal_spawns_piglin']=False;dimtype['attributes']['minecraft:visual/fog_color']='#9884a6';write(Path('dimension_type/great_warren.json'),dimtype)
write(Path('dimension/great_warren.json'),{'type':'bunnidogs:great_warren','generator':{'type':'bunnidogs:realm_islands','settings':'bunnidogs:great_warren','biome_source':{'type':'minecraft:multi_noise','biomes':[{'biome':'bunnidogs:'+region[0],'parameters':{'temperature':bounds,'humidity':[-1,1],'continentalness':[-1,1],'erosion':[-1,1],'depth':[-1,1],'weirdness':[-1,1],'offset':0}} for region,bounds in zip(regions,[[-1,-.25],[-.25,.25],[.25,1]])]}}})
write(Path('worldgen/configured_feature/realm_landmarks.json'),{'type':'bunnidogs:realm_landmarks','config':{}})
write(Path('worldgen/placed_feature/realm_landmarks.json'),{'feature':'bunnidogs:realm_landmarks','placement':[{'type':'minecraft:count','count':1},{'type':'minecraft:in_square'},{'type':'minecraft:heightmap','heightmap':'WORLD_SURFACE_WG'},{'type':'minecraft:biome'}]})
write(Path('tags/worldgen/biome/great_warren.json'),{'replace':False,'values':['bunnidogs:'+r[0] for r in regions]})
