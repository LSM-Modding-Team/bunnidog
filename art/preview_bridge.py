"""Render the ACTUAL native ProtoChunk bridge exported by realm_travel; never a substitute concept mesh."""
from pathlib import Path
import json,math,subprocess,os,sys
from nbt_template import save
ROOT=Path(__file__).resolve().parents[1];OUT=ROOT/'art/bridges';OUT.mkdir(exist_ok=True)
rows=json.loads((ROOT/'build/game-test/guild_bridge_preview.json').read_text());cells={tuple(r['pos']):(r['state']['Name'],r['state'].get('Properties',{}),None) for r in rows}
minimum=[min(p[i] for p in cells) for i in range(3)];maximum=[max(p[i] for p in cells) for i in range(3)]
local={tuple(p[i]-minimum[i] for i in range(3)):state for p,state in cells.items()};save(OUT/'native_bridge.nbt',local,tuple(maximum[i]-minimum[i]+1 for i in range(3)))
(OUT/'native_bridge.json').write_text(json.dumps({'tiles':[[*minimum,'native_bridge']]},indent=2)+'\n')
points=[p for p,s in cells.items() if s[0]=='bunnidogs:pale_lunar_tiles' and abs(p[1])<4];sx=sum(p[0] for p in points)/len(points);sz=sum(p[2] for p in points)/len(points)
xx=sum((p[0]-sx)**2 for p in points);zz=sum((p[2]-sz)**2 for p in points);xz=sum((p[0]-sx)*(p[2]-sz) for p in points);angle=.5*math.atan2(2*xz,xx-zz);dx,dz=math.cos(angle),math.sin(angle)
views=[['masonry_span',[dx*50-dz*45,-(dz*50+dx*45),32],[0,0,-3],38,1280,840],['carriageway',[0,0,2.7],[dx*20,-dz*20,3.2],22,1200,800]]
(OUT/'views.json').write_text(json.dumps(views,indent=2)+'\n')
env=dict(os.environ,STRUCTURE_META=str(OUT/'native_bridge.json'),STRUCTURE_TEMPLATE_ROOT=str(OUT),STRUCTURE_RENDER_DIR=str(OUT),STRUCTURE_VIEWS=str(OUT/'views.json'))
subprocess.run([sys.executable,str(ROOT/'art/render_citadel.py')],check=True,env=env)
