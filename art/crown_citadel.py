"""Crown Warren V2: authored architectural modules, baked into chunk-sized native templates.
Coordinates are relative to the Crown arena floor. No runtime random decoration or hidden loot refill.
Run python art/crown_citadel.py; render_citadel.py consumes the very same exported native templates.
"""
from pathlib import Path
import json, math
from collections import defaultdict
from nbt_template import save, string
ROOT=Path(__file__).resolve().parents[1]
DATA=ROOT/'src/main/resources/data/bunnidogs'
OUT=DATA/'structure/crown_citadel'
C={};ROOMS=[]
AIR='minecraft:air';STONE='minecraft:tuff_bricks';PALE='minecraft:calcite';RIB='bunnidogs:rootstone';DARK='minecraft:deepslate_tiles';WOOD='minecraft:dark_oak_planks'
def put(x,y,z,b,**p): C[int(x),int(y),int(z)]=(b if ':' in b else 'minecraft:'+b,{k:str(v).lower() for k,v in p.items()},None)
def box(x0,y0,z0,x1,y1,z1,b,**p):
 for x in range(x0,x1+1):
  for z in range(z0,z1+1):
   for y in range(y0,y1+1):put(x,y,z,b,**p)
def line(a,b,mat,width=0):
 steps=max(abs(b[i]-a[i]) for i in range(3))
 for t in range(steps+1):
  p=[round(a[i]+(b[i]-a[i])*t/max(1,steps)) for i in range(3)]
  box(p[0]-width,p[1],p[2]-width,p[0]+width,p[1],p[2]+width,mat)
def floor(x0,z0,x1,z1,y,mat=STONE):
 box(x0,y-2 if y<=0 else y,z0,x1,y,z1,mat)
 # Upper floors are supported by authored beams/columns, never roots through the room below.
 if y>0:return
 # Individually tapered foundations, rather than one square platform beneath the castle.
 for x in range(x0+2,x1,10):
  for z in range(z0+2,z1,10):
   box(x-1,-12,z-1,x+1,y-3,z+1,RIB)
def clear(x0,y0,z0,x1,y1,z1):box(x0,y0,z0,x1,y1,z1,AIR)
def light(x,y,z,hanging=False):put(x,y,z,'lantern',hanging=hanging,waterlogged=False)
def chest(x,y,z,table='crown_supplies',face='north',barrel=False):
 b='minecraft:barrel' if barrel else 'minecraft:chest'
 C[x,y,z]=(b,{'facing':face,**({'open':'false'} if barrel else {'type':'single','waterlogged':'false'})},{'LootTable':string('bunnidogs:chests/'+table)})
def room(name,x0,z0,x1,z1,y,h=8,wall=STONE,openings=(),roof=True):
 ROOMS.append({'name':name,'box':[x0,y,z0,x1,y+h,z1]})
 floor(x0,z0,x1,z1,y)
 clear(x0,y+1,z0,x1,y+h,z1)
 for inset,mat in [(0,RIB),(1,wall)]:
  box(x0+inset,y+1,z0+inset,x1-inset,y+h,z0+inset,mat)
  box(x0+inset,y+1,z1-inset,x1-inset,y+h,z1-inset,mat)
  box(x0+inset,y+1,z0+inset,x0+inset,y+h,z1-inset,mat)
  box(x1-inset,y+1,z0+inset,x1-inset,y+h,z1-inset,mat)
 # Recessed windows and repeated projecting structural bays.
 for x in range(x0+5,x1-3,9):
  for z in [z0,z1]:
   box(x-1,y+3,z,x+1,y+h-2,z,'light_gray_stained_glass')
   box(x-1,y+3,z+(1 if z==z0 else -1),x+1,y+h-2,z+(1 if z==z0 else -1),AIR)
   box(x-2,y+2,z,x+2,y+2,z,PALE)
 for z in range(z0+5,z1-3,9):
  for x in [x0,x1]:
   box(x,y+3,z-1,x,y+h-2,z+1,'light_gray_stained_glass')
   box(x+(1 if x==x0 else -1),y+3,z-1,x+(1 if x==x0 else -1),y+h-2,z+1,AIR)
 for x in [x0,x1]:
  for z in range(z0,z1+1,9):
   box(x-1,y,z-1,x+1,y+h+1,z+1,RIB)
   box(x-2,y+h-1,z-2,x+2,y+h,z+2,PALE)
 if roof:
  # Steep pitched roofs, extended eaves and ridge caps.
  for x in range(x0-2,x1+3):
   ry=y+h+2+min(x-(x0-2),x1+2-x)//2
   if x0+2<=x<=x1-2:clear(x,y+h+1,z0+2,x,ry-1,z1-2)
   if x0<=x<=x1:
    for z in [z0,z1]:box(x,y+h+1,z,x,ry-1,z,'light_gray_stained_glass' if abs(x-(x0+x1)//2)<=1 else WOOD)
    if x in [x0,x1]:box(x,y+h+1,z0,x,ry-1,z1,wall)
   box(x,ry,z0-2,x,ry,z1+2,DARK)
   if (x-x0)%8==0:box(x,ry+1,z0-2,x,ry+1,z1+2,RIB)
 for edge,center,width,height in openings:door(x0,z0,x1,z1,y,edge,center,width,height)
 for x in range(x0+4,x1-2,10):
  for z in range(z0+4,z1-2,10):light(x,y+h-1,z,True)
def door(x0,z0,x1,z1,y,edge,c,w=5,h=6):
 # Broad arched opening carved through both wall skins.
 for u in range(-(w//2),w//2+1):
  cap=h-max(0,abs(u)-(w//2-1))
  if edge in ['n','s']:
   z=z0 if edge=='n' else z1
   clear(c+u,y+1,z-2,c+u,y+cap,z+2)
   put(c+u,y+cap+1,z,PALE)
  else:
   x=x0 if edge=='w' else x1
   clear(x-2,y+1,c+u,x+2,y+cap,c+u)
   put(x,y+cap+1,c+u,PALE)
def arch_x(x,y,z,span,height,depth=1):
 for dx in range(-span,span+1):
  rise=round(height*math.sqrt(max(0,1-(dx/(span+1))**2)))
  box(x+dx,y+rise,z-depth,x+dx,y+rise+1,z+depth,RIB)
 for dx in [-span,span]:box(x+dx,y,z-depth,x+dx,y+height//3,z+depth,RIB)
def stairs(name,x,z,y,steps,width=5,axis='z',direction=1):
 # Half-speed rise: tread and landing per block of ascent. Real native stair shapes.
 for n in range(steps*2):
  rise=(n+1)//2;xx=x+(n*direction if axis=='x' else 0);zz=z+(n*direction if axis=='z' else 0)
  xa,xb=(xx,xx) if axis=='x' else (xx-width//2,xx+width//2)
  za,zb=(zz,zz) if axis=='z' else (zz-width//2,zz+width//2)
  clear(xa,y+rise+1,za,xb,y+rise+6,zb)
  box(xa,y-2,za,xb,y+rise,zb,STONE)
  if n%2:box(xa,y+rise,za,xb,y+rise,zb,'tuff_brick_stairs',facing=('east' if direction>0 else 'west') if axis=='x' else ('south' if direction>0 else 'north'),half='bottom',shape='straight',waterlogged=False)
 endx=x+(steps*2-1)*direction if axis=='x' else x
 endz=z+(steps*2-1)*direction if axis=='z' else z
 wx=width//2 if axis=='z' else 0;wz=width//2 if axis=='x' else 0
 ROOMS.append({'name':name,'box':[min(x,endx)-wx,y,min(z,endz)-wz,max(x,endx)+wx,y+steps+6,max(z,endz)+wz]})
def table(x,y,z,length=5):
 box(x,y,z,x+length-1,y,z,'dark_oak_fence',east=True,west=True,north=False,south=False,waterlogged=False)
 box(x,y+1,z,x+length-1,y+1,z,'dark_oak_trapdoor',facing='north',half='bottom',open=False,powered=False,waterlogged=False)
 for xx in range(x,x+length,2):put(xx,y,z+2,'dark_oak_stairs',facing='north',half='bottom',shape='straight',waterlogged=False)
 light(x+length//2,y+1,z)
def shelves(x0,z0,x1,z1,y):
 for x in range(x0,x1+1):
  for z in range(z0,z1+1):
   if (x-x0)%7<5:box(x,y,z,x,y+3,z,'bookshelf')
def tower(cx,cz,r,body,peak):
 for x in range(cx-r-2,cx+r+3):
  for z in range(cz-r-2,cz+r+3):
   d=max(abs(x-cx),abs(z-cz))+.42*min(abs(x-cx),abs(z-cz))
   if d>r:continue
   box(x,-8,z,x,0,z,RIB)
   clear(x,1,z,x,peak+1,z)
   if d>=r-2:
    box(x,1,z,x,body,z,STONE if (x+z)%9 else PALE)
    for y in [11,23,body]:box(x,y,z,x,y+1,z,PALE)
   else:
    for y in [12,24,body]:put(x,y,z,WOOD)
   roofy=body+2+int((r-d)*(peak-body-2)/r)
   box(x,roofy,z,x,roofy+1,z,DARK)
 # Square helical flight with broad treads and carved headroom through landings.
 ring=[];q=r-4
 for x in range(-q,q):ring.append((x,-q))
 for z in range(-q,q):ring.append((q,z))
 for x in range(q,-q,-1):ring.append((x,q))
 for z in range(q,-q,-1):ring.append((-q,z))
 for n in range(body*2):
  dx,dz=ring[n%len(ring)];y=n//2
  for v in range(-1,2):
   xx=cx+dx+(v if abs(dz)==q else 0);zz=cz+dz+(v if abs(dx)==q else 0)
   clear(xx,y+1,zz,xx,y+4,zz)
   nx,nz=ring[(n+1)%len(ring)];facing='east' if nx>dx else 'west' if nx<dx else 'south' if nz>dz else 'north'
   if n%2==0 and n>0:put(xx,y,zz,'tuff_brick_stairs',facing=facing,half='bottom',shape='straight',waterlogged=False)
   else:put(xx,y,zz,STONE)
 for y in [4,16,27]:
  clear(cx-2,y,cz-r-1,cx+2,y+4,cz-r+2)
  box(cx-2,y-1,cz-r-1,cx+2,y-1,cz-r+2,PALE)
 for x,z in [(cx,cz-r+3),(cx,cz+r-3)]:light(x,8,z,True)
 ROOMS.append({'name':'Gate tower stair and watch room','box':[cx-r,0,cz-r,cx+r,body,cz+r]})
def court(x0,z0,x1,z1,y):
 floor(x0,z0,x1,z1,y)
 clear(x0,y+1,z0,x1,y+10,z1)
 for x in range(x0+4,x1,12):
  for z in [z0+2,z1-2]:
   box(x,y+1,z,x,y+2,z,PALE);light(x,y+3,z)
def seal(x,y,z,wing):put(x,y,z,'bunnidogs:crown_seal',wing=wing,state=0,sockets=0)

def build():
 # Crown hall: 85x85 unobstructed combat floor, aisles outside, visible crossing vaults.
 floor(-55,-55,55,48,0,RIB);clear(-55,1,-55,55,67,48)
 for x in range(-54,55):
  for z in range(-54,49):
   if abs(x)<=42 and abs(z)<=42:put(x,0,z,RIB)
 for x in [-49,49]:
  box(x-1,1,-53,x+1,28,47,STONE)
  for z in range(-48,49,12):
   box(x-2,1,z-2,x+2,32,z+2,RIB)
   box(x-3,18,z-3,x+3,20,z+3,PALE)
   # Deep lancet bay, set into the outer skin.
   if z<43:
    box(x,6,z+4,x,23,z+8,'light_gray_stained_glass')
    clear(x-1,6,z+4,x-1,23,z+8)
    clear(x+1,6,z+4,x+1,23,z+8)
   outward=-1 if x<0 else 1
   line((x+outward*8,1,z),(x+outward*3,24,z),RIB,1)
   line((x+outward*3,24,z),(x,30,z),PALE,1)
 for z in [-53,46]:
  box(-49,1,z-1,49,29,z+1,STONE)
  for x in range(-48,49,12):
   box(x-2,1,z-2,x+2,32,z+2,RIB)
   if abs(x)>7:box(x+4,9,z,x+8,23,z,'light_gray_stained_glass')
 # Barrel vault shell; transverse ribs at each structural bay.
 for x in range(-53,54):
  roofy=29+round(15*math.sqrt(max(0,1-(x/54)**2)))
  box(x,roofy,-55,x,roofy,48,DARK)
  if abs(x)>44:box(x,29,-55,x,29,48,DARK)
 # Complete glazed clerestories/gables connect the vault to its wall bays.
 for x in range(-49,50):
  yy=29+round(15*math.sqrt(max(0,1-(x/54)**2)))
  for z in [-53,46]:box(x,30,z,x,yy-1,z,'light_gray_stained_glass' if x%12 in range(4,9) else STONE)
 for x in [-49,49]:
  yy=29+round(15*math.sqrt(max(0,1-(x/54)**2)))
  for z in range(-53,47):box(x,29,z,x,yy-1,z,RIB if z%12 in [0,1,11] else 'light_gray_stained_glass')
 for z in range(-48,49,12):arch_x(0,29,z,49,14,1)
 # True upper side galleries outside the combat envelope, accessible from the library roofwalk.
 for x0,x1 in [(-54,-44),(44,54)]:
  box(x0,14,-51,x1,14,43,WOOD)
  for x in [x1 if x1<0 else x0]:
   box(x,15,-51,x,15,43,'dark_oak_fence',north=True,south=True,east=False,west=False,waterlogged=False)
  for z in range(-46,43,12):light((x0+x1)//2,21,z,True)
 # Stairs to the keep's two galleries start inside the sealed hall, not on a wing bypass.
 for x in [-46,46]:stairs('Crown side gallery stair',x,40,0,14,3,'z',-1)
 # Crown lantern rises above the long roof: open ribs and skylight, no low combat obstructions.
 for x in [-13,13]:
  for z in [-13,13]:box(x-1,43,z-1,x+1,57,z+1,RIB)
 for y in [44,54]:
  box(-14,y,-14,14,y,-12,PALE);box(-14,y,12,14,y,14,PALE)
  box(-14,y,-14,-12,y,14,PALE);box(12,y,-14,14,y,14,PALE)
 for x in range(-14,15):
  yy=57+(14-abs(x))//2
  box(x,yy,-14,x,yy,14,'light_gray_stained_glass')
 for x in [-13,0,13]:
  for z in [-13,13]:
   box(x,55,z,x,63+int(x==0)*3,z,PALE)
   put(x,64+int(x==0)*3,z,'gold_block')
 # A glazed octagonal crown drum closes the lantern, with a steep ribbed cap.
 for x in range(-14,15):
  for z in range(-14,15):
   d=max(abs(x),abs(z))+.42*min(abs(x),abs(z))
   if d>15:continue
   if d>=13:box(x,45,z,x,56,z,'light_gray_stained_glass' if x%5 and z%5 else RIB)
   yy=57+round((15-d)*.5)
   put(x,yy,z,RIB if abs(x)==abs(z) or x==0 or z==0 else DARK)
 # A rose bay and deep archivolts break the monumental front into readable human-scale thresholds.
 for x in range(-10,11):
  for y in range(10,31):
   d=math.hypot(x,y-21)
   if d<=9:
    box(x,y,45,x,y,47,PALE if d>7.8 else RIB if abs(x)<1 or abs(y-21)<1 or abs(abs(x)-abs(y-21))<.8 else 'light_gray_stained_glass')
 for zz,w,h in [(48,8,11),(49,9,12),(50,10,13)]:arch_x(0,0,zz,w,h)
 # Rear petrified Hearth feature above playable volume.
 for dx in [-18,-9,0,9,18]:line((dx,0,-51),(dx//3,26,-49),RIB,1)
 box(-7,16,-51,7,27,-49,'polished_basalt');box(-4,19,-48,4,24,-48,'ochre_froglight')
 # Main threshold, two distinct physical seats.
 door(-49,-53,49,46,0,'s',0,7,9)
 box(-3,1,46,3,8,46,'bunnidogs:crown_gate',gate_kind=0)
 for x in [-6,6]:put(x,4,46,'bunnidogs:crown_socket',gate_kind=0,key=0)
 court(-55,49,55,91,0)
 # Recessed archive and treasury behind their own gates.
 for name,x0,x1,kind in [('Treasury',-45,-20,2),('Genetic archive',20,45,1)]:
  mid=-31 if kind==2 else 31
  room(name,x0,49,x1,67,0,8,openings=[('s',mid,5,5)])
  box(mid-2,1,67,mid+2,5,67,'bunnidogs:crown_gate',gate_kind=kind)
  if kind==1:put(mid,3,68,'bunnidogs:crown_socket',gate_kind=1,key=0)
  shelves(x0+3,51,x1-3,51,1)
  for x in [x0+4,x1-4]:table(x,1,57,4)
 # Raised controller plinth remains reachable, never presented as a screen.
 box(-1,0,49,1,0,51,PALE);seal(0,1,50,3)
 # Quiet courtyard fittings leave broad independent wing approaches on both sides.
 for x in range(-7,8):
  for z in range(71,84):
   d=math.hypot(x,z-77)
   if d<6:put(x,0,z,'water' if d<4 else PALE)
 for x in [-4,0,4]:line((x,1,77),(x//2,8,77),RIB,1)
 box(-2,8,75,2,9,79,'moss_block')
 for x in [-18,18]:
  box(x,1,70,x,1,81,'dark_oak_stairs',facing='east' if x<0 else 'west',half='bottom',shape='straight',waterlogged=False)
 for x in [-51,51]:
  for z in range(53,91,12):box(x,1,z,x,7,z,RIB);light(x,6,z,True)
 for x0,x1 in [(-54,-47),(47,54)]:box(x0,8,49,x1,8,89,WOOD)
 # Gate and asymmetric defensive foreground.
 for x in [-22,22]:tower(x,104,11,32 if x<0 else 38,45 if x<0 else 52)
 room('Gatehouse passage',-12,93,12,116,0,11,openings=[('n',0,9,8),('s',0,9,8)],roof=False)
 box(-14,12,94,14,12,114,WOOD)
 room('Gatehouse watch gallery',-12,94,12,114,12,7,openings=[('w',104,5,5),('e',104,5,5)])
 clear(-5,1,91,5,9,119)
 court(-8,117,8,119,0)
 floor(-6,90,6,118,0)
 # Low curtain walls are interrupted by the wing facades, not an enclosing square box.
 for a,b in [((-108,0,75),(-108,0,-82)),((-108,0,75),(-35,0,109)),((108,0,75),(108,0,-76)),((108,0,75),(35,0,109)),((-35,0,109),(-34,0,109)),((35,0,109),(34,0,109))]:
  dx,dz=b[0]-a[0],b[2]-a[2];n=max(abs(dx),abs(dz))
  for i in range(n+1):
   x=round(a[0]+dx*i/max(1,n));z=round(a[2]+dz*i/max(1,n))
   box(x,-7,z,x,8,z,STONE);box(x,9,z,x,9+(i%4<2),z,RIB)
 # Wayhouse and food infrastructure: quiet, furnished, real preparation utility.
 room('Warded wayhouse',-102,60,-62,85,0,8,openings=[('e',73,5,6),('s',-83,5,6)])
 table(-95,1,65,8)
 for x,b in [(-99,'crafting_table'),(-96,'smithing_table'),(-93,'ender_chest')]:put(x,1,81,b,**({'facing':'north','waterlogged':False} if b=='ender_chest' else {}))
 put(-99,1,64,'bunnidogs:warren_ward')
 for x in [-73,-68]:chest(x,1,82,barrel=True)
 room('Granary',63,60,102,84,0,8,wall='mud_bricks',openings=[('w',73,5,6),('n',83,5,6)])
 for x in [67,74,90,97]:
  box(x,1,64,x+2,3,67,'hay_block',axis='y');chest(x,1,80,barrel=True)
 table(80,1,66,7)
 # Court approach branches reach all wings without passing the Crown gate.
 for a,b in [((-55,0,73),(-57,0,19)),((55,0,73),(57,0,10)),((55,0,12),(55,0,-67)),((55,0,-67),(37,0,-67))]:
  line(a,b,STONE,4)
  for n in range(0,max(abs(a[0]-b[0]),abs(a[2]-b[2]))+1,12):
   m=max(abs(a[0]-b[0]),abs(a[2]-b[2]));x=round(a[0]+(b[0]-a[0])*n/max(1,m));z=round(a[2]+(b[2]-a[2])*n/max(1,m));light(x+4,2,z)
 # Glass Gallery: library, actual upper gallery and stair, then octagonal observatory.
 room('Lower library',-103,8,-63,48,0,8,openings=[('e',28,5,6),('n',-83,7,6)],roof=False)
 room('Upper library gallery',-103,8,-63,48,8,8,openings=[('e',12,5,5),('n',-83,7,6)])
 box(-103,8,8,-63,8,48,WOOD)
 clear(-94,6,21,-72,8,38)
 for x in [-95,-71]:box(x,9,21,x,9,38,'dark_oak_fence',north=True,south=True,east=False,west=False,waterlogged=False)
 for z in [20,39]:
  box(-95,9,z,-71,9,z,'dark_oak_fence',north=False,south=False,east=True,west=True,waterlogged=False)
  box(-100,7,z,-66,7,z,'dark_oak_log',axis='x')
  for x in [-95,-71]:box(x,1,z,x,7,z,RIB)
 shelves(-93,14,-90,15,1);shelves(-81,26,-78,27,1)
 for y in [1,9]:
  shelves(-100,11,-66,11,y);shelves(-100,44,-66,44,y)
  table(-96,y,17,7);table(-78,y,39,7)
  for z in [19,30,41]:box(-100,y,z,-100,y+4,z+3,'bookshelf')
 chest(-98,1,39);put(-68,1,15,'lectern',facing='south',has_book=False,powered=False)
 # Enclosed stair east of library, tread direction rises north.
 room('Library stair',-63,9,-54,44,0,14,openings=[('w',40,5,6),('e',40,5,6),('n',-58,5,6)],roof=False)
 stairs('Library stair flight',-58,40,0,8,5,'z',-1)
 box(-62,8,8,-54,8,24,STONE);clear(-61,9,7,-55,14,25)
 clear(-65,9,10,-61,14,15)
 # Octagonal observatory, 35x35 central fighting footprint and radial peripheral bays.
 ox,oz=-83,-28
 for x in range(ox-25,ox+26):
  for z in range(oz-25,oz+26):
   dx,dz=abs(x-ox),abs(z-oz)
   if max(dx,dz)>24 or dx+dz>36:continue
   floor(x,z,x,z,8)
   clear(x,9,z,x,55,z)
   edge=max(dx,dz)>=23 or dx+dz>=35
   if edge:
    box(x,9,z,x,28,z,PALE if (x+z)%12==0 else STONE)
    if (dx<3 or dz<3 or abs(dx-dz)<2) and 13<=dx+dz:box(x,13,z,x,24,z,'cyan_stained_glass')
   r=math.sqrt(dx*dx+dz*dz)
   yy=28+round(16*math.sqrt(max(0,1-(r/30)**2)))
   put(x,yy,z,RIB if dx%12==0 or dz%12==0 or abs(dx-dz)<1 else 'light_blue_stained_glass')
   if edge:box(x,29,z,x,yy-1,z,RIB if dx%6==0 or dz%6==0 else 'cyan_stained_glass')
   if max(dx,dz)<=22 and dx+dz<=33 and (max(dx,dz)>=19 or dx+dz>=28):put(x,20,z,WOOD)
 for x in range(ox-19,ox+20):
  for z in range(oz-19,oz+20):
   if max(abs(x-ox),abs(z-oz))<=19 and abs(x-ox)+abs(z-oz)<=28 and (max(abs(x-ox),abs(z-oz))==19 or abs(x-ox)+abs(z-oz)==28):put(x,21,z,'dark_oak_fence',north=True,south=True,east=True,west=True,waterlogged=False)
 stairs('Observatory gallery stair',ox+19,oz+12,8,12,3,'z',-1)
 for xx in [-102,-64]:
  for zz in [-47,-9]:box(xx,9,zz,xx,31,zz,RIB)
 door(ox-24,oz-24,ox+24,oz+24,8,'s',ox,7,7)
 floor(-88,-5,-78,8,8);clear(-88,9,-5,-78,16,9)
 # A broad stair at the south library wall supplies a second genuine upper-level route.
 stairs('Library return stair',-98,41,0,8,5,'x',1)
 # Eight authored root piers hold the raised observatory above the island.
 for dx,dz in [(-22,0),(22,0),(0,-22),(0,22),(-14,-14),(14,-14),(-14,14),(14,14)]:
  box(ox+dx-1,-12,oz+dz-1,ox+dx+1,7,oz+dz+1,RIB)
 # Telescope sits in a side bay, never on an encounter pad.
 line((-102,10,-28),(-99,14,-28),'copper_block',1);light(-83,30,-28,True)
 for x in [-98,-68]:chest(x,9,-44)
 seal(-83,9,-8,0)
 # Roofwalk shortcut to keep side gallery at level14, outside the arena gate.
 floor(-58,-10,-52,6,14);clear(-58,15,-10,-52,20,6)
 stairs('Observatory roofwalk stair',-58,-4,8,6,5,'z',1)
 # The roofwalk ends at the outside buttress; it never opens a pre-key route into the hall.
 box(-52,15,-10,-52,15,7,PALE)
 # Nursery wing. Stores lead into an open nesting court, then terraces and individual nurseries.
 room('Seed kitchen',64,35,103,55,0,7,wall='mud_bricks',openings=[('w',44,5,6),('n',83,7,6),('s',83,5,6)])
 for x in [68,73,93,98]:chest(x,1,51,barrel=True)
 table(77,1,46,8);box(68,1,39,73,2,41,'hay_block',axis='y')
 court(61,-13,105,33,0)
 for z in range(-9,32,10):
  for x in [62,104]:box(x,1,z,x,9,z,RIB);light(x,8,z,True)
 for x in [61,100]:box(x,10,-13,x+4,10,33,WOOD)
 for z in [-12,32]:
  box(61,10,z,105,10,z+2,WOOD)
  for x in [74,92]:box(x,1,z,x,9,z,RIB)
  arch_x(83,3,z,9,7)
 # Small nesting alcoves and a pitched cloister edge frame the court's clear fighting floor.
 for x in [62,102]:
  for z in [0,12,24]:
   box(x,1,z,x+2,1,z+4,'moss_block');box(x+1,2,z+1,x+1,2,z+3,'hay_block',axis='y')
 for x in list(range(60,67))+list(range(99,106)):
  yy=11+min(x-60,66-x) if x<70 else 11+min(x-99,105-x)
  box(x,yy,-14,x,yy,34,DARK)
 # Nest alcoves are substantial rooms with a central open court.
 for x0,x1 in [(64,80),(86,102)]:
  for z0,z1 in [(-61,-43),(-42,-24)]:
   box(x0,-6,z0,x1,3,z1,RIB)
   room('Terraced nursery',x0,z0,x1,z1,4,7,wall='mud_bricks',openings=[('s',(x0+x1)//2,5,5),('n',(x0+x1)//2,5,5)])
   box(x0+3,5,z0+3,x1-3,5,z0+6,'moss_block')
   box(x0+4,6,z0+4,x1-4,6,z0+5,'hay_block',axis='y')
   chest(x1-3,5,z1-3,barrel=True)
 box(82,-6,-62,84,3,-21,RIB)
 floor(80,-62,86,-21,4);clear(81,5,-62,85,12,-21)
 stairs('Nursery terrace steps',83,-14,0,4,7,'z',-1)
 # Side exit loops along an unroofed service walk to the seed kitchen.
 floor(57,-53,60,50,0);clear(57,1,-53,60,6,50)
 for z in range(-48,49,12):light(58,2,z)
 seal(83,1,29,1)
 # Lower furnace: loading/workshop above, then freight stairs, cooling gallery and heart chamber.
 room('Furnace loading hall',29,-107,48,-65,0,10,openings=[('s',37,7,7),('w',-94,5,6)])
 box(32,1,-103,36,3,-100,'raw_copper_block');box(41,1,-99,44,2,-95,'iron_block')
 for z in [-102,-88,-69]:chest(45,1,z,barrel=True)
 for z in [-103,-95]:put(39,1,z,'anvil',facing='north')
 room('Lower cooling gallery',27,-106,48,-80,-8,7,wall='polished_basalt',openings=[('w',-87,7,6),('s',37,7,6)],roof=False)
 # Native stair rises east; lower landing at x28 and upper at44.
 stairs('Freight stair',28,-74,-8,8,7,'x',1)
 floor(28,-80,48,-78,-8);clear(29,-7,-80,46,-1,-77)
 clear(27,-7,-77,31,4,-71)
 # Upper walkway around stair mouth, floor openings are deliberate.
 clear(28,-7,-78,44,8,-71)
 stairs('Freight stair final',28,-74,-8,8,7,'x',1)
 room('Chained heart chamber',-24,-111,24,-63,-8,22,wall='polished_basalt',openings=[('e',-87,7,7)],roof=False)
 for x in range(-25,26):
  yy=14+round(9*math.sqrt(max(0,1-(x/26)**2)))
  if -23<=x<=23:clear(x,15,-110,x,yy-1,-64)
  box(x,yy,-113,x,yy,-61,DARK)
 for x in range(-24,25):
  yy=14+round(9*math.sqrt(max(0,1-(x/26)**2)))
  for z in [-111,-63]:box(x,15,z,x,yy-1,z,'polished_basalt' if x%8<3 else 'orange_stained_glass')
  if abs(x)==24:box(x,15,-111,x,yy-1,-63,'polished_basalt')
 for z in [-105,-93,-81,-69]:arch_x(0,12,z,23,10)
 # Cooling water/channels, furnaces and hoist are outside all encounter pads.
 for z in range(-103,-68,5):
  put(-20,-7,z,'blast_furnace',facing='east',lit=False)
  put(20,-7,z,'blast_furnace',facing='west',lit=False)
 box(-19,-7,-108,19,-7,-107,'copper_grate',waterlogged=False)
 box(-12,22,-97,12,22,-95,RIB)
 for x in [-3,3]:box(x,11,-96,x,21,-96,'chain',axis='y',waterlogged=False)
 for yy,r in enumerate([1,2,3,3,3,2,1],4):
  for x in range(-r,r+1):
   for dz in range(-r,r+1):
    if abs(x)+abs(dz)<=r+1:put(x,yy,-96+dz,'raw_copper_block')
 box(-1,6,-93,1,8,-93,'shroomlight')
 for x in [-20,20]:
  for z in [-105,-69]:light(x,9,z,True)
 seal(19,-7,-87,2)
 # Broken rear foundations terminate independently rather than a square perimeter slab.
 for x in [-42,39]:
  line((x,-10,-108),(x+4,14,-57),RIB,2)
 floor(22,-91,30,-83,-8)
 # Protected fighting floors carry small warm root lights, not a color-coded route.
 for fx,fy,fz,r in [(0,0,0,41),(-83,8,-28,17),(83,0,10,17),(0,-8,-87,17)]:
  for x in range(fx-r,fx+r+1):
   for z in range(fz-r,fz+r+1):
    put(x,fy,z,'bunnidogs:crown_rootlight' if (x-fx)%12==0 and (z-fz)%12==0 else RIB)
 # Deliberate store thresholds link kitchen, granary and service return without jumps.
 floor(54,40,68,48,0);floor(78,30,88,37,0);floor(78,54,88,61,0)
 # Two copper-banded furnace vents break the low industrial roof silhouette.
 for xx in [-19,19]:
  box(xx-2,17,-110,xx+2,31,-106,'polished_basalt')
  box(xx-2,26,-110,xx+2,27,-106,'cut_copper')
  clear(xx-1,18,-109,xx+1,32,-107)
  box(xx-3,32,-111,xx+3,32,-105,'deepslate_bricks')
  clear(xx-1,32,-109,xx+1,33,-107)
 # Site-specific control and encounter records are exported, not inferred from decorative blocks.

ENCOUNTERS=[
 {'wing':0,'seal':[-83,9,-8],'floor':[-83,8,-28],'bounds':[-108,7,-54,-55,56,49], 'patrol':[-83,1,33],'patrolBounds':[-104,0,7,-54,30,49]},
 {'wing':1,'seal':[83,1,29],'floor':[83,0,10],'bounds':[56,-1,-64,109,31,56], 'patrol':[83,1,40],'patrolBounds':[62,0,34,105,22,57]},
 {'wing':2,'seal':[19,-7,-87],'floor':[0,-8,-87],'bounds':[-27,-10,-114,51,34,-58], 'patrol':[37,1,-94],'patrolBounds':[27,-9,-109,50,25,-62]},
 {'wing':3,'seal':[0,1,50],'floor':[0,0,0],'bounds':[-43,-4,-43,43,33,43], 'patrol':[0,1,100],'patrolBounds':[-35,0,91,35,55,119]}]
GATES=[{'kind':0,'box':[-3,1,46,3,8,46]},{'kind':1,'box':[29,1,67,33,5,67]},{'kind':2,'box':[-33,1,67,-29,5,67]}]
SOCKETS=[[-6,4,46],[6,4,46],[31,3,68]]
def main():
 build()
 # Six reward niches per controller, reserved empty. Counters populate them only after victory.
 for e in ENCOUNTERS:
  x,y,z=e['seal'];w=e['wing']
  slots=([[-41+i*3,1,54] for i in range(6)] if w==3 else [[x-8+i*3,y,z-10] for i in range(6)])
  if w==0:slots=[[-99+i*3,9,-46] for i in range(6)]
  if w==2:slots=[[-17+i*6,-7,-105] for i in range(6)]
  e['rewards']=slots
  e['archives']=([[25+i*3,1,53] for i in range(6)] if w==3 else slots)
  for xx,yy,zz in slots+(e['archives'] if w==3 else []):
   put(xx,yy-1,zz,PALE);clear(xx,yy,zz,xx,yy+2,zz)
  # Explicit floor-relative legal actor feet; flight offsets remain the accepted native behavior.
  fx,fy,fz=e['floor'];e['pads']=[[fx+dx,fy+1,fz+dz] for dx,dz in [(-7,7),(7,-7),(7,7),(-7,-7),(0,-10),(10,0)]]
 # Preserve authored geometry while replacing every structural palette entry.
 from gen_lunar_forge import MAPPING
 for pos,(name,props,tag) in list(C.items()):
  if name in MAPPING:
   out='bunnidogs:'+MAPPING[name]
   if not out.endswith(('_stairs','_fence','_slab','_wall')):props={}
   C[pos]=(out,props,tag)
 tiles=defaultdict(dict)
 for (x,y,z),state in C.items():
  if not (-120<=x<120 and -120<=z<120 and -12<=y<=70):raise ValueError((x,y,z))
  tx,tz=(x+120)//16,(z+120)//16
  tiles[tx,tz][x+120-tx*16,y+12,z+120-tz*16]=state
 OUT.mkdir(parents=True,exist_ok=True)
 for old in OUT.glob('*.nbt'):old.unlink()
 for (tx,tz),cells in sorted(tiles.items()):save(OUT/f'tile_{tx}_{tz}.nbt',cells,(16,83,16))
 meta={'revision':2,'minimum':[-120,-12,-120],'maximum':[119,70,119], 'tiles':[[tx*16-120,-12,tz*16-120,f'crown_citadel/tile_{tx}_{tz}'] for tx,tz in sorted(tiles)], 'encounters':ENCOUNTERS,'gates':GATES,'sockets':SOCKETS,'rooms':ROOMS}
 (DATA/'structure/crown_citadel.json').write_text(json.dumps(meta,indent=2)+'\n')
 # Finite supply cupboards contain generic expedition stock, never wing keys/exclusive boss parts.
 from gen_lunar_forge import supplies
 supplies()
 print(f'{len(C):,} authored cells; {len(tiles)} native tiles; {len(ROOMS)} purposeful rooms/circulation spaces')
if __name__=='__main__':main()
