"""Shared English and neutral Peruvian Spanish for observed field lessons."""
from pathlib import Path
import hashlib,json
root=Path(__file__).resolve().parents[1]/'src/main/resources/assets/bunnidogs/lang'
rows={
 'message.bunnidogs.scouting.limited':('This survey is too complex for this bunnidog.','Esta exploración es demasiado compleja para este bunnidog.'),
 'message.bunnidogs.scouting.lesson':('Still learning to recognize minerals.','Todavía está aprendiendo a reconocer minerales.'),
 'message.bunnidogs.scouting.guidance':('Needs its scouting guide nearby.','Necesita a su guía de exploración cerca.'),
 'message.bunnidogs.scouting.paused':('Scouting is paused.','Exploración pausada.'),
 'message.bunnidogs.scouting.ready':('Ready to survey.','Listo para explorar.'),
 'care.bunnidogs.scouting.senses':('Needs a sharper ear for minerals.','Necesita un oído más fino para los minerales.'),
 'interaction.bunnidogs.practice.watching':('Watching %s','Observando %s'),
}
for en,es in [('Scouting','Exploración'),('%s alone; %s guided','%s solo; %s con guía'),('Teach %s through Work → Lessons.','Enseña %s en Trabajo → Lecciones.')]:
 rows['text.bunnidogs.'+hashlib.sha256(en.encode()).hexdigest()[:16]]=(en,es)
for index,locale in enumerate(['en_us','es_pe']):
 p=root/(locale+'.json');data=json.loads(p.read_text());data.update({key:pair[index] for key,pair in rows.items()});p.write_text(json.dumps(data,indent=2,ensure_ascii=False)+'\n')
