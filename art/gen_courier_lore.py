"""Native written-book assets and shared language for real household route knowledge."""
from pathlib import Path
import json
root=Path(__file__).resolve().parents[1]/'src/main/resources/assets/bunnidogs'
def write(path,data):
 p=root/path;p.parent.mkdir(parents=True,exist_ok=True);p.write_text(json.dumps(data,ensure_ascii=False,indent=2)+'\n')
write(Path('models/item/route_notes.json'),{'parent':'minecraft:item/generated','textures':{'layer0':'minecraft:item/written_book'}})
write(Path('items/route_notes.json'),{'model':{'type':'minecraft:model','model':'bunnidogs:item/route_notes'}})
rows={
 'item.bunnidogs.route_notes':('Route Notes','Notas de recorrido'),
 'item.bunnidogs.route_notes.named':('Route: %s','Recorrido: %s'),
 'book.bunnidogs.route_notes.route':('%s\n\nFrom: %s, %s, %s\nTo: %s, %s, %s','%s\n\nOrigen: %s, %s, %s\nDestino: %s, %s, %s'),
 'message.bunnidogs.courier.taught':('%s learned a delivery from %s.','%s aprendió una entrega de %s.'),
 'care.bunnidogs.courier.apprentice':('Learning a courier route','Aprendiendo un recorrido de entrega'),
}
for index,locale in enumerate(['en_us','es_pe']):
 p=root/'lang'/(locale+'.json');data=json.loads(p.read_text());data.update({key:pair[index] for key,pair in rows.items()});p.write_text(json.dumps(data,ensure_ascii=False,indent=2)+'\n')
