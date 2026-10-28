"""Render actual native-generator samples; headless geometry evidence, not a client screenshot."""
from pathlib import Path
import json
import numpy as np
import matplotlib
matplotlib.use('Agg')
import matplotlib.pyplot as plt
from matplotlib.colors import LightSource
from matplotlib.patches import Rectangle
root=Path(__file__).resolve().parents[1]
data=json.loads((root/'build/game-test/realm_islands_preview.json').read_text())
rows=np.asarray(data['cells']);xs=np.unique(rows[:,0]);zs=np.unique(rows[:,1]);height=rows[:,2].reshape(len(xs),len(zs)).T;sky=rows[:,3].reshape(len(xs),len(zs)).T
fig=plt.figure(figsize=(17,8),facecolor='#151e2a',layout='constrained');ax=fig.add_subplot(121);ax.set_facecolor('#151e2a')
land=np.ma.masked_where(height<0,height);ax.imshow(land,origin='lower',extent=[xs.min(),xs.max(),zs.min(),zs.max()],cmap='gist_earth',vmin=30,vmax=155,interpolation='nearest')
for x,z,bx,bz in data.get('routes',[]):ax.plot([x,bx],[z,bz],color='#eed986',lw=.65,alpha=.85)
ax.contour(xs,zs,sky>0,levels=[.5],colors=['#ada7e9'],linewidths=.7)
for x,z in data['crowns']:
 if xs.min()<x<xs.max() and zs.min()<z<zs.max():ax.add_patch(Rectangle((x-90,z-90),180,180,facecolor='none',edgecolor='#ffcf99',lw=1.4));ax.text(x,z+155,'Crown',ha='center',color='#fff0cb',fontsize=7)
ax.set_title('Sparse Crown routes through a varied archipelago',color='white',pad=16);ax.set_xlabel('Blocks east / west',color='#c4ced9');ax.set_ylabel('Blocks north / south',color='#c4ced9');ax.tick_params(colors='#c4ced9');ax.set_aspect('equal')
close=np.asarray(data['detail']);xs=np.unique(close[:,0]);zs=np.unique(close[:,1]);xx,zz=np.meshgrid(xs,zs);hh=close[:,2].reshape(len(xs),len(zs)).T.astype(float);hh[hh<0]=np.nan
view=fig.add_subplot(122,projection='3d');view.set_facecolor('#151e2a');view.plot_surface(xx,zz,hh,cmap='gist_earth',vmin=30,vmax=155,linewidth=0,antialiased=True,rcount=130,ccount=130);view.view_init(38,-48);view.set_box_aspect((1,1,.4));view.set_zlim(0,165);view.set_title('One Crown island: relief beyond the dungeon footprint',color='white',pad=12);view.tick_params(colors='#c4ced9',labelsize=8);view.set_xlabel('Blocks',color='#c4ced9');view.set_zlabel('Height',color='#c4ced9')
for axis in [view.xaxis,view.yaxis,view.zaxis]:axis.pane.fill=False;axis.line.set_color('#596674')
fig.set_layout_engine('constrained',rect=(0,.065,1,.935))
fig.suptitle('Great Warren · native seeded terrain',color='#fff0cc',fontsize=18)
fig.text(.04,.02,'Gold: island routes · Violet: moons · Crowns ~7.1× rarer (512-chunk spacing)',color='#c4ced9',fontsize=9)
fig.text(.55,.02,'Generated sample; not an in-game screenshot or a gameplay review.',color='#c4ced9',fontsize=9)
fig.savefig(root/'art/realm_islands_preview.png',dpi=140,facecolor=fig.get_facecolor());plt.close(fig)
