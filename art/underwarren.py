"""Authored Lantern Warrens: vaulted inhabited market, craft quarter, homes and a surface gatehouse.
Source of truth for native chunk templates and Blender inspections. No decorative stock refill.
"""
from pathlib import Path
import math,json
from collections import defaultdict
from nbt_template import save,string,integer
ROOT=Path(__file__).resolve().parents[1];DATA=ROOT/'src/main/resources/data/bunnidogs';C={}
AIR='minecraft:air';BRICK='bunnidogs:lunar_bricks';PALE='bunnidogs:pale_lunar_tiles';RIB='bunnidogs:cinderroot_beams';WOOD='bunnidogs:guava_planks';DARK='bunnidogs:lunar_tiles'
def put(x,y,z,b,**p):C[x,y,z]=(b if ':' in b else 'minecraft:'+b,{k:str(v).lower() for k,v in p.items()},None)
def box(x0,y0,z0,x1,y1,z1,b,**p):
 for x in range(x0,x1+1):
  for z in range(z0,z1+1):
   for y in range(y0,y1+1):put(x,y,z,b,**p)
def vault(x0,z0,x1,z1,y,h):
 # Rounded vault, two masonry shells, projecting ribs and patterned floor.
 mid=(x0+x1)/2;radius=(x1-x0)/2
 for x in range(x0,x1+1):
  rise=round(h*.55*math.sqrt(max(0,1-((x-mid)/max(1,radius))**2)));roof=y+h//2+rise
  for z in range(z0,z1+1):
   for yy in range(y-2,roof+2):
    edge=x in [x0,x1] or z in [z0,z1]
    mat=BRICK if yy<=y or edge or yy>=roof else AIR
    if yy==y:mat=PALE if (x%8==0 or z%8==0) else DARK
    if edge and (x%8==0 or z%8==0):mat=RIB
    if yy>=roof:mat=RIB if z%8==0 else BRICK
    put(x,yy,z,mat)
   if z%8==0 and x in [x0+2,x1-2]:
    box(x,y+1,z,x,y+3,z,PALE);box(x,y+4,z,x,roof-1,z,RIB)
    put(x,y+4,z,'bunnidogs:lunar_rootlight')
def opening(x0,z0,x1,z1,y,h=5):box(x0,y+1,z0,x1,y+h,z1,AIR)
def counter(x,z,profession):
 box(x-2,1,z,x+2,1,z,WOOD);put(x-2,2,z,'lantern',hanging=False,waterlogged=False)
 put(x,1,z-2,profession)
def hang(x,y,z,roof):
 for yy in range(y+1,roof):put(x,yy,z,'iron_chain',axis='y',waterlogged=False)
 put(x,roof,z,RIB);put(x,y,z,'lantern',hanging=True,waterlogged=False)
def build():
 vault(-21,-14,21,24,0,18)
 for z in [-8,8,24]:
  for x in range(-18,19):
   y=9+round(9*math.sqrt(max(0,1-(x/19)**2)));put(x,y,z,PALE)
 # Water garden: seats and a rooted lantern-tree well, leaving broad circulation on either side.
 box(-3,0,0,3,0,6,PALE);box(-2,0,1,2,0,5,'water',level=0)
 box(-1,1,2,1,2,4,BRICK);put(0,3,3,'bunnidogs:lunar_rootlight')
 for x in [-5,5]:
  for z in [0,3,6]:put(x,1,z,'bunnidogs:guava_stairs',facing='east' if x<0 else 'west',half='bottom',shape='straight',waterlogged=False)
 for x in [-12,12]:
  for z in [-7,9,20]:hang(x,10,z,16)
 # Market wings: stalls, actual machines, reading benches, upper homes.
 for x0,x1 in [(-44,-22),(22,44)]:
  vault(x0,-14,x1,23,0,10);opening(x0 if x0>0 else x1-1,-8,x0+1 if x0>0 else x1,16,0,6)
  for z,job in [(-8,'smithing_table'),(4,'cartography_table'),(16,'loom')]:counter((x0+x1)//2,z,job)
  # Lived-in upper storey with timber ceiling beams.
  vault(x0,-14,x1,23,9,8)
  for z in [-10,0,10,20]:
   box(x0+3,10,z,x1-3,12,z,WOOD);opening(x0+8,z,x0+11,z,9,3)
   for x in [x0+4,x1-5]:put(x,10,z+2,'red_bed',facing='south',part='foot',occupied=False);put(x,10,z+3,'red_bed',facing='south',part='head',occupied=False)
   put(x0+5,10,z+1,'barrel',facing='up',open=False)
   # A table, reading stool and a lamp fixed to the actual vault in each home.
   if z<20:
    tx=x0+16
    put(tx,10,z+5,'bunnidogs:guava_fence',east=False,west=False,north=False,south=False,waterlogged=False)
    put(tx,11,z+5,'bunnidogs:guava_pressure_plate',powered=False)
    put(tx-2,10,z+5,'bunnidogs:guava_slab',type='bottom',waterlogged=False)
    roof=9+4+round(8*.55*math.sqrt(max(0,1-((tx-(x0+x1)/2)/((x1-x0)/2))**2)))
    hang(tx,12,z+6,roof)
  # Open balcony and continuous stairs from the public hall.
  bx=x1 if x0<0 else x0
  opening(bx-1,-10,bx+1,21,9,5)
  for z in range(-10,22):put(bx+(1 if x0<0 else -1),9,z,WOOD)
  for n in range(18):
   z=21-n;y=(n+1)//2
   for dx in range(3):
    x=bx+(dx+2)*(1 if x0<0 else -1);box(x,y-1,z,x,y,z,BRICK)
    if n%2:put(x,y,z,'bunnidogs:lunar_bricks_stairs',facing='north',half='bottom',shape='straight',waterlogged=False)
 # South workshop and communal kitchen, with stores and planted conservatory.
 vault(-44,24,-15,44,0,10);vault(15,24,44,44,0,10)
 opening(-20,22,-16,25,0,5);opening(16,22,20,25,0,5)
 for x in range(-40,-18,4):put(x,1,40,'smoker',facing='north',lit=False);put(x,1,30,'composter',level=0)
 box(-40,1,34,-19,1,35,WOOD)
 for x in range(-39,-18,3):put(x,2,34,'flower_pot')
 for x in [19,26,33,40]:
  put(x,1,40,'blast_furnace',facing='north',lit=False);put(x,1,30,'anvil',facing='north')
 for x,z,b in [(19,26,'pack_armoury'),(26,26,'resonance_bench'),(-19,26,'nursery_hearth')]:put(x,1,z,'bunnidogs:'+b)
 # Resonance hall, framed portal with geometric inlaid runes and paid travel controller.
 vault(-14,25,14,44,0,14);opening(-5,22,5,26,0,6)
 for x in [-9,9]:box(x-1,1,34,x+1,8,36,RIB);box(x-2,8,33,x+2,9,37,PALE)
 for x in range(-8,9):put(x,10-round(abs(x)/3),35,PALE)
 box(-5,0,31,5,0,39,PALE);put(0,0,35,'bunnidogs:guild_gate')
 for x in range(-3,4):
  for y in range(1,8):put(x,y,35,'bunnidogs:guild_veil')
 for x in [-7,7]:put(x,1,29,'lectern',facing='north',has_book=False,powered=False)
 # Native guild heart owns finite residents and material warehouse.
 put(12,1,19,'bunnidogs:guild_heart')
 # Four flights descend from an unmistakable surface lodge into the public hall.
 vault(-8,-42,8,-15,0,38);box(-6,1,-40,6,37,-17,AIR)
 for flight in range(4):
  y0=32-flight*8;direction=1 if flight%2==0 else -1;x=-3 if flight%2==0 else 3;z0=-37 if direction==1 else -21
  for n in range(16):
   z=z0+direction*n;y=y0-(n+1)//2
   box(x-2,y-1,z,x+2,y,z,BRICK)
   if n%2:box(x-2,y,z,x+2,y,z,'bunnidogs:lunar_bricks_stairs',facing='north' if direction==1 else 'south',half='bottom',shape='straight',waterlogged=False)
  z=z0+direction*15;y=y0-8;box(-5,y-1,z-1,5,y,z+1,BRICK)
  for xx in [-6,6]:put(xx,y+3,z,'bunnidogs:lunar_rootlight')
 opening(-5,-17,5,-12,0,6)
 # Surface gatehouse: deep arch, stepped gable, buttresses, sheltered lamps, planted approach.
 vault(-11,-45,11,-28,32,12);opening(-5,-46,5,-42,32,7);box(-5,33,-41,5,39,-29,AIR)
 for x in [-10,10]:
  box(x-1,30,-46,x+1,41,-44,RIB);box(x-2,41,-47,x+2,42,-43,PALE)
  put(x,43,-45,'bunnidogs:lunar_rootlight')
 for x in range(-8,9):
  put(x,40+round(5*math.sqrt(max(0,1-(x/9)**2))),-45,PALE)
 for z in range(-52,-45):
  box(-6,31,z,6,32,z,BRICK)
  for x in [-6,6]:put(x,33,z,PALE)
 for x in [-8,8]:put(x,33,-49,'bunnidogs:lunar_rootlight')
 # Recut and restore the upper stair flight after the lodge's floor was laid.
 for n in range(16):
  z=-37+n;y=32-(n+1)//2
  box(-5,y+1,z,-1,y+5,z,AIR);box(-5,y-1,z,-1,y,z,BRICK)
  if n%2:box(-5,y,z,-1,y,z,'bunnidogs:lunar_bricks_stairs',facing='north',half='bottom',shape='straight',waterlogged=False)
 # Public market stalls face the common aisle; awnings, stock and counters make the hall inhabited.
 for side in [-1,1]:
  for z in [-5,15]:
   x=side*14;box(x-3,1,z-2,x+3,1,z+2,WOOD)
   for xx in [x-3,x+3]:
    for zz in [z-2,z+2]:box(xx,2,zz,xx,4,zz,'bunnidogs:guava_fence',east=False,west=False,north=False,south=False,waterlogged=False)
   for xx in range(x-3,x+4):
    for zz in range(z-2,z+3):put(xx,5+(abs(xx-x)<2),zz,'green_wool' if z<0 else 'purple_wool')
   for zz in [z-1,z+1]:put(x+side*2,2,zz,'barrel',facing='up',open=False)
   put(x-side*2,2,z,'cake',bites=0)
 # Hall end screens are recessed around a carved arch rather than a blank timber wall.
 for x in [-16,-10,10,16]:
  box(x-1,3,23,x+1,7,23,PALE);box(x,4,23,x,6,23,'bunnidogs:runic_lunar_glass')
  box(x-2,8,23,x+2,8,23,RIB)
 # Quiet reading benches overlooking the fountain.
 for x in [-8,8]:
  for z in [18,19]:put(x,1,z,'bookshelf')
  put(x,2,18,'lantern',hanging=False,waterlogged=False)
 # Clear the surface approach, without flattening the island around the lodge.
 box(-5,33,-52,5,39,-46,AIR)
 # Every descending flight cuts its own opening through the previous landing.
 # Without this final clearance, the landing seals the first two descending treads.
 for flight in range(4):
  y0=32-flight*8;direction=1 if flight%2==0 else -1;x=-3 if flight%2==0 else 3;z0=-37 if direction==1 else -21
  for n in range(16):
   z=z0+direction*n;y=y0-(n+1)//2;box(x-2,y+1,z,x+2,y+5,z,AIR)
   if 2<=n<=12:
    for xx in [x-3,x+3]:
     put(xx,y,z,RIB);put(xx,y+1,z,'iron_bars',north=True,south=True,east=False,west=False,waterlogged=False)
 # Real planted kitchen beds under grow lights.
 for x in range(-40,-19):
  for z in [26,27]:
   put(x,0,z,'farmland',moisture=7);put(x,1,z,'wheat',age=7)
  # A covered water channel hydrates every bed without an open item-trapping shaft.
  put(x,0,28,'bunnidogs:lunar_bricks_slab',type='top',waterlogged=True)
 for x in range(-40,-19,4):
  put(x,3,27,'bunnidogs:lunar_rootlight')
  for y in range(4,8):put(x,y,27,'iron_chain',axis='y',waterlogged=False)
  put(x,8,27,RIB)
 # Every hanging lantern is fixed to its authored beam. Street posts are solid.
 for p,(name,props,tag) in list(C.items()):
  if name=='minecraft:lantern' and props.get('hanging')!='true':
   x,y,z=p
   if C.get((x,y-1,z),(AIR,{},None))[0]==AIR:put(x,y-1,z,WOOD)

def main():
 build();tiles=defaultdict(dict)
 for (x,y,z),s in C.items():
  tx,tz=(x+48)//16,(z+64)//16;tiles[tx,tz][x+48-tx*16,y+3,z+64-tz*16]=s
 out=DATA/'structure/lantern_warren';out.mkdir(parents=True,exist_ok=True)
 for (tx,tz),cells in tiles.items():save(out/f'tile_{tx}_{tz}.nbt',cells,(16,55,16))
 meta={'minimum':[-48,-3,-64],'maximum':[47,51,47],'tiles':[[tx*16-48,-3,tz*16-64,f'lantern_warren/tile_{tx}_{tz}'] for tx,tz in sorted(tiles)]}
 (DATA/'structure/lantern_warren.json').write_text(json.dumps(meta,indent=2)+'\n')
 print(len(C),'authored town cells;',len(tiles),'chunk tiles')
if __name__=='__main__':main()
