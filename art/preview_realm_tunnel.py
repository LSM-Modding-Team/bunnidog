"""Longitudinal section through actual native ProtoChunks exported by the realm fixture."""
from pathlib import Path
import json
import numpy as np
import matplotlib
matplotlib.use('Agg')
import matplotlib.pyplot as plt
ROOT=Path(__file__).resolve().parents[1]
data=json.loads((ROOT/'build/game-test/realm_tunnel_preview.json').read_text())
colors={'minecraft:air':'#172736','minecraft:end_stone':'#aaa477',
        'minecraft:moss_block':'#678448','minecraft:end_stone_bricks':'#e0d4a2',
        'minecraft:sea_lantern':'#b8efec','bunnidogs:astralite_ore':'#a790d2',
        'bunnidogs:cinderite_ore':'#c48057'}
xs=sorted({x for x,y,block in data['cells']});ys=sorted({y for x,y,block in data['cells']})
pixels=np.zeros((len(ys),len(xs),3));minx=min(xs);miny=min(ys)
for x,y,block in data['cells']:
 color=colors.get(block,'#9a8c84');pixels[y-miny,x-minx]=[int(color[i:i+2],16)/255 for i in (1,3,5)]
fig,ax=plt.subplots(figsize=(16,6.8),facecolor='#111c27',layout='constrained');ax.set_facecolor('#172736')
ax.imshow(pixels,origin='lower',interpolation='nearest',extent=(min(xs)-.5,max(xs)+.5,min(ys)-.5,max(ys)+.5),aspect='equal')
ax.set_ylim(data['floor']-18,max(row[2] for row in data['profile'])+10);ax.tick_params(colors='#bfcad0');ax.set_xlabel('Blocks along route',color='#bfcad0');ax.set_ylabel('World height',color='#bfcad0')
ax.set_title('Great Warren — bridge becomes a tunnel through the island',color='#f0ebda',fontsize=16,pad=14)
for spine in ax.spines.values():spine.set_color('#50606b')
fig.text(.02,.025,'Actual generated rock/roof, five clear blocks, existing lit deck · Native ProtoChunk section, seed '+str(data['seed']),color='#d1d9db',fontsize=10)
fig.text(.02,.005,'Longitudinal cut (side walls outside this plane); not an in-game capture.',color='#9eabb4',fontsize=9)
fig.set_layout_engine('constrained',rect=(0,.08,1,.92));fig.savefig(ROOT/'art/realm_tunnel_preview.png',dpi=140,facecolor=fig.get_facecolor());plt.close(fig)
