"""Shared English/neutral Spanish for actual household care and personal group tactics."""
from pathlib import Path
import hashlib,json
root=Path(__file__).resolve().parents[1]/'src/main/resources/assets/bunnidogs/lang'
rows={
 'message.bunnidogs.clinic.treated':('%s is recovering.','%s se está recuperando.'),
 'message.bunnidogs.clinic.wait':('Needs quiet, dry recovery and time between dressings.','Necesita recuperarse en un lugar seco y tranquilo, y esperar entre curaciones.'),
 'care.bunnidogs.clinic.take':('Take supplies','Recuperar suministros'),
 'care.bunnidogs.clinic.supplies':('Dressing','Apósito'),
 'care.bunnidogs.clinic.carrying':('Carrying care supplies','Llevando suministros de curación'),
 'message.bunnidogs.tactics.learned':('%s has learned to coordinate in combat.','%s aprendió a coordinarse en combate.'),
 'care.bunnidogs.tactics.instinct':('Fights by instinct','Combate por instinto'),
 'care.bunnidogs.tactics.learning':('Learning group combat','Aprendiendo combate en grupo'),
 'care.bunnidogs.tactics.guided':('Coordinates with guidance','Se coordina con ayuda'),
 'care.bunnidogs.tactics.coordinated':('Coordinates the group','Coordina el grupo'),
}
for en,es in [('Group tactics','Tácticas de grupo'),('Family care','Cuidados familiares')]:rows['text.bunnidogs.'+hashlib.sha256(en.encode()).hexdigest()[:16]]=(en,es)
for index,locale in enumerate(['en_us','es_pe']):
 p=root/(locale+'.json');data=json.loads(p.read_text());data.update({key:pair[index] for key,pair in rows.items()});p.write_text(json.dumps(data,indent=2,ensure_ascii=False)+'\n')
