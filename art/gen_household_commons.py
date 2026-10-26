"""Shared translations for concrete household jobs and requested construction surveys."""
import json
from pathlib import Path
root=Path(__file__).resolve().parents[1]/'src/main/resources/assets/bunnidogs/lang'
rows={
 'message.bunnidogs.construction.repair_allowed':('Repairs authorized.','Reparaciones autorizadas.'),
 'message.bunnidogs.construction.confirm_repair':('Repair %s missing blocks? Confirm this survey.','¿Reparar %s bloques faltantes? Confirma este terreno.'),
 'message.bunnidogs.construction.annex_allowed':('New room authorized.','Nueva habitación autorizada.'),
 'message.bunnidogs.construction.confirm_annex':('New room marked. Confirm this survey.','Nueva habitación marcada. Confirma este terreno.'),
 'message.bunnidogs.construction.no_plot':('No clear room site nearby.','No hay espacio libre para otra habitación.'),
 'care.bunnidogs.commons.supplies':('Household supplies','Suministros del hogar'),
 'care.bunnidogs.commons.take':('Recover supplies','Recuperar suministros'),
 'care.bunnidogs.commons.carrying':('Carrying household supplies','Llevando suministros del hogar'),
}
jobs={
 'plan':('Planning household work','Organizando el trabajo del hogar'),
 'fetch_seed':('Collecting crop seed','Recogiendo semillas de cultivo'),
 'sow_field':('Sowing a field','Sembrando un terreno'),
 'harvest_field':('Harvesting a field','Cosechando un terreno'),
 'gather_field':('Collecting a harvest','Recogiendo una cosecha'),
 'meal':('Eating a ration','Comiendo una ración'),
 'craft':('Making supplies','Preparando suministros'),
 'build':('Building a room','Construyendo una habitación'),
 'fetch_build':('Collecting building materials','Recogiendo materiales de construcción'),
 'plant':('Planting','Plantando'),
 'prune':('Pruning a coppice','Podando un guayabo'),
 'haul':('Collecting surplus food','Recogiendo comida sobrante'),
 'supply':('Restocking a feeder','Reabasteciendo un comedero'),
 'deliver':('Delivering supplies','Entregando suministros'),
 'compost':('Making compost','Preparando compost'),
 'court':('Tending the family','Cuidando a la familia'),
 'seed':('Collecting a litter seed','Recogiendo una semilla de camada'),
 'nurture':('Tending a litter','Cuidando una camada'),
 'hatch':('Helping a litter hatch','Ayudando a nacer a una camada'),
 'tend':('Tending the Hearth','Cuidando el hogar'),
 'teach':('Teaching a housemate','Enseñando a un compañero'),
 'medicine':('Collecting dressings','Recogiendo vendajes'),
 'care':('Treating a housemate','Atendiendo a un compañero'),
 'record_route':('Writing a route','Anotando un recorrido'),
 'study_route':('Studying a route','Estudiando un recorrido'),
}
rows.update({'care.bunnidogs.commons.working.'+key:value for key,value in jobs.items()})
for index,locale in enumerate(['en_us','es_pe']):
 p=root/(locale+'.json');data=json.loads(p.read_text());data.update({key:values[index] for key,values in rows.items()});p.write_text(json.dumps(data,ensure_ascii=False,indent=2)+'\n')
