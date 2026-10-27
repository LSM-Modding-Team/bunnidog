"""Current expedition feedback, applied last by the maintained localization chain."""
from pathlib import Path
import json
root=Path(__file__).resolve().parents[1]/'src/main/resources/assets/bunnidogs/lang'
rows={
 'guide.bunnidogs.page.22.body':('Invite residents and link work sites with a bound Steward’s Staff. Authorize the marked shelter and orchard plot. Crops, hauling, shared meals, paid crafting/building and a resident-taught new generation cultivate the Keystone. The five ground apex remnants and an Aerial Membrane craft the Resonant Eye.','Invita residentes y vincula lugares de trabajo con un bastón del cuidador. Autoriza el refugio y el huerto marcados. Los cultivos, los traslados, las comidas compartidas, la fabricación, la construcción y una nueva generación enseñada por los residentes cultivan la piedra angular viva. Los vestigios de los cinco ápex terrestres y una membrana aérea permiten fabricar el ojo resonante.'),
 'message.bunnidogs.hunt.missing':('Unrecorded hunts: %s','Cacerías sin registrar: %s'),
 'message.bunnidogs.hunt.complete':('Historical hunt record complete.','Registro histórico de cacerías completo.'),
 'tooltip.bunnidogs.ground_remnant':('Ground apex remnant','Vestigio de ápex terrestre'),
 'tooltip.bunnidogs.resonant_eye':('Five ground apex remnants and an aerial membrane, bound into one eye.','Cinco vestigios de ápex terrestres y una membrana aérea, unidos en un ojo.'),
 'tooltip.bunnidogs.apex_crest.anchor':('Pairs with a Resonant Eye to awaken a Warren Anchor.','Junto con un ojo resonante, despierta un ancla de madriguera.'),
 'tooltip.bunnidogs.apex_crest.use':('Use to inspect the source sheet.','Úsalo para inspeccionar la ficha de origen.'),
}
for i,locale in enumerate(['en_us','es_pe']):
 p=root/(locale+'.json');data=json.loads(p.read_text());data.update({key:values[i] for key,values in rows.items()});p.write_text(json.dumps(data,ensure_ascii=False,indent=2)+'\n')

# Preserve the latest architectural assets and key language after older art generators.
import runpy
runpy.run_path(str(Path(__file__).resolve().with_name("gen_citadel_details.py")))
