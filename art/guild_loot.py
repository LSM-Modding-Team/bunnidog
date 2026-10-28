"""Whole-site stock budgets, distributed over real cupboards instead of multiplied per decorative barrel."""
from gen_lunar_forge import write,D
BUDGETS={
'entry':[('bunnidogs:moonstone',128,256),('bunnidogs:root_fibre',64,128),('bunnidogs:raw_cinderite',64,64),('minecraft:golden_apple',8,16),('minecraft:enchanted_golden_apple',1,2),('minecraft:totem_of_undying',1,3),('minecraft:diamond',8,16),('minecraft:netherite_ingot',1,3),('minecraft:experience_bottle',32,64),('minecraft:ender_pearl',16,32),('minecraft:firework_rocket',32,64)],
'glass':[('bunnidogs:starlit_dust',32,64),('bunnidogs:whisker',16,32),('bunnidogs:cinderite_ingot',64,64)],
'brood':[('bunnidogs:root_fibre',64,128),('bunnidogs:sinew',32,64),('bunnidogs:moonstone',128,256),('bunnidogs:cinderite_ingot',64,64)],
'furnace':[('bunnidogs:raw_cinderite',128,192),('bunnidogs:moonstone',128,256),('bunnidogs:starlit_dust',32,64)]}
def pool(item,lo,hi):
 f=[{'function':'minecraft:set_count','count':{'type':'minecraft:uniform','min':lo,'max':hi}}]
 if item=='minecraft:firework_rocket':f.insert(0,{'function':'minecraft:set_components','components':{'minecraft:fireworks':{'flight_duration':1,'explosions':[]}}})
 return {'rolls':1,'entries':[{'type':'minecraft:item','name':item,'functions':f}]}
def table(name,stock):
 slots=sum((hi+(16 if item=='minecraft:ender_pearl' else 1 if item=='minecraft:totem_of_undying' else 64)-1)//(16 if item=='minecraft:ender_pearl' else 1 if item=='minecraft:totem_of_undying' else 64) for item,lo,hi in stock)
 assert slots<=27,(name,slots)
 write(D,'loot_table/chests/'+name+'.json',{'type':'minecraft:chest','random_sequence':'bunnidogs:chests/'+name,'pools':[pool(*s) for s in stock]})
def supply_tables():
 table('crown_supplies',[('bunnidogs:moonstone',128,128),('bunnidogs:root_fibre',64,64),('bunnidogs:cinderite_ingot',64,64),('bunnidogs:starlit_dust',32,32),('minecraft:experience_bottle',32,32)])
 for wing in range(3):
  rewards=[('minecraft:golden_apple',8,16),('minecraft:enchanted_golden_apple',1,2),('minecraft:totem_of_undying',1,1),('minecraft:experience_bottle',16,16)]
  if wing!=1:rewards.append(('bunnidogs:astralite_shard',32 if wing==0 else 64,64 if wing==0 else 128))
  else:rewards += [('bunnidogs:field_dressing',4,4),('bunnidogs:nursery_bedding',16,16)]
  table('crown_wing_'+str(wing),rewards)
 table('crown_wing',[('bunnidogs:astralite_shard',32,64),('bunnidogs:root_fibre',64,128),('bunnidogs:cinderite_ingot',64,64)])
 table('crown_vault',[('bunnidogs:astralite_shard',192,256),('bunnidogs:raw_cinderite',256,512),('bunnidogs:moonstone',128,128),('bunnidogs:starlit_dust',128,128),('bunnidogs:root_fibre',256,512),('bunnidogs:sinew',64,64)])
def distribute(cells,string):
 groups={k:[] for k in BUDGETS}
 for pos,(name,props,tag) in sorted(cells.items()):
  if not tag or tag.get('LootTable')!=(8,'bunnidogs:chests/crown_supplies'):continue
  x,y,z=pos;group='furnace' if z<-58 else 'glass' if x<-50 and z<52 else 'brood' if x>50 and z<62 else 'entry';groups[group].append(pos)
 manifest={}
 for group,positions in groups.items():
  if not positions:raise ValueError(('Missing stock room',group))
  for i,pos in enumerate(positions):
   stock=[]
   for item,lo,hi in BUDGETS[group]:
    a=lo//len(positions)+(i<lo%len(positions));b=hi//len(positions)+(i<hi%len(positions))
    if b:stock.append((item,a,b))
   name='crown_stores/'+group+'_'+str(i);table(name,stock)
   block,props,tag=cells[pos];tag=dict(tag);tag['LootTable']=string('bunnidogs:chests/'+name);cells[pos]=(block,props,tag)
  manifest[group]={'containers':len(positions),'combined_budget':BUDGETS[group]}
 write(D,'structure/crown_store_budget.json',manifest)
