"""Architecture checks on final shipped NBT: closed-gate walking routes and clear native pads.
This is a conservative voxel navigation check, not a replacement for the native GameTests.
"""
from pathlib import Path
import json,gzip,sys
from collections import deque,defaultdict
from render_citadel import nbt,ROOT
D=ROOT/'src/main/resources/data/bunnidogs/structure'
def load():
 meta=json.loads((D/'crown_citadel.json').read_text());cells={}
 for ox,oy,oz,name in meta['tiles']:
  t=nbt(gzip.decompress((D/(name+'.nbt')).read_bytes()));pal=t['palette']
  for b in t['blocks']:
   x,y,z=b['pos'];cells[x+ox,y+oy,z+oz]=pal[b['state']]
 return meta,cells
def main():
 m,c=load();empty={'minecraft:air','minecraft:chain','minecraft:lantern','minecraft:torch','minecraft:wall_torch'}
 def passable(x,y,z):return c.get((x,y,z),{}).get('Name','minecraft:air') in empty
 def walkable(x,y,z):return not passable(x,y-1,z) and passable(x,y,z) and passable(x,y+1,z)
 q=deque([(0,1,118)]);seen=set(q)
 while q:
  x,y,z=q.popleft()
  for dx,dz in [(1,0),(-1,0),(0,1),(0,-1)]:
   for dy in [0,1,-1]:
    p=x+dx,y+dy,z+dz
    if p in seen or abs(p[0])>120 or abs(p[2])>120 or p[1]<-9 or p[1]>30:continue
    # The boss combat floor is explicitly forbidden: wings must be reachable independently.
    if abs(p[0])<=42 and -42<=p[2]<=42:continue
    if walkable(*p) and (dy<=0 or passable(x,y+2,z)):
     seen.add(p);q.append(p)
 found=[]
 for e in m['encounters']:
  x,y,z=e['seal'];near=[p for p in seen if abs(p[0]-x)+abs(p[2]-z)<=2 and abs(p[1]-y)<=1]
  found.append(bool(near));print('Wing',e['wing'],'reachable',bool(near),'seal',e['seal'])
  assert walkable(*e["patrol"]),f'Blocked approach guard pad {e["wing"]}: {e["patrol"]}'
  for p in e['pads']:
   assert walkable(*p),f'Blocked pad {e["wing"]}: {p}: {[c.get((p[0],p[1]+dy,p[2])) for dy in [-1,0,1]]}'
 # Save the graph only when diagnosing an actual missing connection.
 if not all(found):
  Path('/tmp/citadel-reachable.json').write_text(json.dumps(sorted(seen)))
  for e in m['encounters']:
   p=e['seal'];nearest=sorted(seen,key=lambda a:sum(abs(a[i]-p[i]) for i in range(3)))[:1];print('nearest',e['wing'],nearest)
  raise AssertionError('Independent closed-gate walking route is blocked')
 # Gates must completely fill their authored low openings, including the side jambs.
 for g in m['gates']:
  x0,y0,z0,x1,y1,z1=g['box']
  for x in range(x0,x1+1):
   assert c[x,2,z0]['Name']=='bunnidogs:crown_gate',f'Missing gate cell {g}'
  for x in [x0-1,x1+1]:
   assert not passable(x,1,z0) and not passable(x,2,z0),f'Walkable gap beside gate {g["kind"]}: {x}'
 # Accepted arena/flight envelope, aside from the native-floor-only protected center.
 for x in range(-41,42):
  for z in range(-41,42):
   for y in range(1,33):
    assert passable(x,y,z),f'Boss flight envelope obstruction {(x,y,z)}'
 print(f'{len(seen):,} reachable standing cells; all four controls, legal pads and Crown flight volume clear')
if __name__=='__main__':main()
