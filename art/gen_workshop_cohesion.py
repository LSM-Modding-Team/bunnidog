"""Live controls for preserved samples, redstone references and optional Apex preparation."""
import json
from gen_syringe import RES

TEXT = {
    'care.bunnidogs.button.show_hatch_pad': 'Show hatch area',
    'care.bunnidogs.button.show_front_pad': 'Show front pad',
    'care.bunnidogs.area_marked': 'Glints mark the working area. Keep its floor dry and headroom clear; angry particles mark the first hatch obstruction.',
    'care.bunnidogs.button.roles': 'Useful roles & work',
    'care.bunnidogs.roles.title': '%s · Useful roles',
    'care.bunnidogs.roles.next.pup': 'Let this pup grow. Its inherited Build and temperament already suggest a role; adult carrying, training and home work will then be available.',
    'care.bunnidogs.roles.next.heal': 'Heal this animal first. Pack inspection remains available; home crop work needs full health.',
    'care.bunnidogs.roles.next.adult': 'Ordinary adults can carry supplies and harvest suitable ripe crops. No bred-point minimum is needed for those jobs. Develop a specialty when you want one.',
    'care.bunnidogs.roles.packs': 'Pack capacity: %s bundles · Equipped: %s. Use a real bundle on this adult; cargo stays in the bags.',
    'care.bunnidogs.roles.partner': 'Loyal/stoic: train with a Whistle and a plain guava in the care panel. Follow/Wait plus bundles makes a useful first travelling companion.',
    'care.bunnidogs.roles.guard': 'Feral: healthy adults can guard their home boundary. A sword toggles guarding without wear; they still need a Nest and a safe route.',
    'care.bunnidogs.roles.independent': 'This temperament prefers resident life. It can still carry bundles and do suitable home crop work. Loyal/stoic offspring can train as trail partners.',
    'care.bunnidogs.roles.harvest': 'Basic harvest: claim a Nest, enable crop work at a nearby Trough, and grow suitable crops within three blocks of it. Healthy adults harvest by day, even at zero bred points.',
    'care.bunnidogs.roles.cultivation': 'Growing, replanting & compost · Bloom %s / +%s: %s',
    'care.bunnidogs.roles.nursery': 'Tending Cradle stages · Bloom +%s: %s',
    'care.bunnidogs.roles.supply': 'Moving Trough surplus · Senses %s / +%s: %s',
    'care.bunnidogs.roles.available': 'stat ready; needs the right station and care',
    'care.bunnidogs.roles.develop': 'breed toward this specialty when useful',
    'care.bunnidogs.roles.trait': '%s: %s',
    'care.bunnidogs.roles.breeding': 'Points come from parents and breeding quality, not exercise. Pick a commission goal at a Genetics Table to compare a pair toward a useful target.',
    'care.bunnidogs.button.goals': 'Breeding goal',
    'care.bunnidogs.button.back_table': 'Back to samples',
    'care.bunnidogs.button.clear_goal': 'Clear breeding goal',
    'care.bunnidogs.table.goals.title': 'Choose a breeding goal',
    'care.bunnidogs.table.goals.next': 'Choose a commission to breed toward. With two samples, Read breeding plans shows the combined gene and point odds for that target.',
    'care.bunnidogs.table.goals.use': 'Goals belong to this Table. They never spend samples, start breeding or submit a commission. The Board still judges the actual adult.',
    'care.bunnidogs.table.goal': 'Breeding goal: %s',
    'care.bunnidogs.table.goal_unknown': 'Unrecognized saved breeding goal. Kept until you deliberately replace or clear it.',
    'care.bunnidogs.table.next.goal': 'Plan %s: Read breeding plans combines its required genes and points, with the next steps for an actual adult. Your samples remain reusable.',
    'care.bunnidogs.button.jar_add': 'Preserve next sample',
    'care.bunnidogs.button.jar_take': 'Retrieve sample',
    'care.bunnidogs.button.jar_read': 'Read sample',
    'care.bunnidogs.jar.sample': 'Preserved sample: %s',
    'care.bunnidogs.jar.signal': 'Comparator output: %s / 15',
    'care.bunnidogs.jar.use': 'A Jar displays a sampled line and its fixed-pair signal. The Genetics Table compares two samples; the Pedigree Comparator reads a living animal.',
    'care.bunnidogs.jar.next.sample': 'Preserve the shown inventory sample, or use a chosen sample here directly. It stays reusable and can be retrieved intact.',
    'care.bunnidogs.jar.next.ready': 'Read this preserved line, display it or retrieve the complete sample. Inspection spends nothing.',
    'care.bunnidogs.jar.next.unreadable': 'This saved sample cannot be read. Retrieve it intact to free the Jar.',
    'care.bunnidogs.button.copy_reference': 'Copy shown sample',
    'care.bunnidogs.button.exact': 'Exact match',
    'care.bunnidogs.button.analog': 'Matched-pair signal',
    'care.bunnidogs.button.clear_reference': 'Clear reference',
    'care.bunnidogs.button.read_reference': 'Read copied reference',
    'care.bunnidogs.detector.reference': 'Copied reference: %s',
    'care.bunnidogs.detector.mode': '%s · Output: %s / 15',
    'care.bunnidogs.detector.exact': 'Exact match',
    'care.bunnidogs.detector.analog': 'Matched-pair signal',
    'care.bunnidogs.detector.pad': 'Front pad: %s, %s, %s. Keep its block and headroom clear.',
    'care.bunnidogs.detector.copies': 'Copying keeps the sample. Hidden copies count; parent order and bred stats do not. Exact mode gives 15 only for all seven pairs; matched-pair mode rises from 1 to 15.',
    'care.bunnidogs.detector.next.empty': 'Copy a Tissue Sample as the target. This stores information and leaves the original sample in your inventory.',
    'care.bunnidogs.detector.next.reading': 'One animal is being compared with the copied reference. Choose the signal your circuit needs below.',
    'care.bunnidogs.detector.next.absent': 'Lead one animal onto the front block. Its genes will be compared without harming or changing it.',
    'care.bunnidogs.detector.next.crowded': 'More than one animal occupies the front block. Separate them for an unambiguous reading.',
    'care.bunnidogs.detector.next.blocked': 'Clear the front block and its headroom. The detector cannot read through a solid block or fluid.',
    'care.bunnidogs.detector.next.unloaded': 'The front pad is outside the loaded area or world border. Move the station to a usable spot.',
    'care.bunnidogs.detector.next.unavailable': 'This target cannot be read. Use an ordinary living bunnidog on a clear loaded front block.',
    'care.bunnidogs.button.take_trophy': 'Retrieve displayed item',
    'care.bunnidogs.button.read_trophy': 'Read displayed record',
    'care.bunnidogs.trophy.stock': 'On display: %s',
    'care.bunnidogs.trophy.winner': 'Winning record %s: %s',
    'care.bunnidogs.trophy.use': 'Use a held item on an empty Pedestal to display one. Retrieval keeps its complete name, contents and recorded genes.',
    'care.bunnidogs.trophy.offering': 'Legendary offering: place five distinct stat legends around an Apex Crucible to prepare the optional encounter.',
    'care.bunnidogs.trophy.next.place': 'Use a trophy or item here to display it. Empty-hand inspection never changes the display.',
    'care.bunnidogs.trophy.next.ready': 'Keep this item on display, read a genetic record or retrieve it below.',
    'care.bunnidogs.button.prepare_ritual': 'Review summon',
    'care.bunnidogs.button.summon': 'Summon Apex',
    'care.bunnidogs.button.cancel_ritual': 'Keep preparing',
    'care.bunnidogs.button.show_ring': 'Show ring positions',
    'care.bunnidogs.apex.source': 'Chosen sample: %s',
    'care.bunnidogs.apex.offering': '%s: %s',
    'care.bunnidogs.apex.missing': 'missing',
    'care.bunnidogs.apex.ready': 'placed',
    'care.bunnidogs.apex.ring': 'Use any five of eight ring positions, at Crucible height: three blocks north/south/east/west, or two diagonally. Show ring positions marks them in the world.',
    'care.bunnidogs.apex.cost': 'Summoning spends one chosen sample and one of each of five displayed legends. It creates a hostile Apex; the living donor stays safe. Creative keeps materials.',
    'care.bunnidogs.apex.confirm': 'Summon the hostile Apex from this sample? Costs one sample and five distinct legends. Review the forecast and offerings below, or keep preparing.',
    'care.bunnidogs.apex.next.ready': 'The five distinct offerings are placed. Review the chosen sample and combat forecast before choosing Review summon.',
    'care.bunnidogs.apex.next.sample': 'Collect a Tissue Sample from a line you want to challenge. Use it here to choose that exact sample, or keep one in your inventory.',
    'care.bunnidogs.apex.next.offerings': 'Place the missing legends on ring Pedestals. The checklist shows which are still needed; Show ring positions marks the possible spots.',
    'care.bunnidogs.apex.next.peaceful': 'The Apex encounter requires a difficulty above Peaceful. All samples and offerings are kept.',
    'care.bunnidogs.apex.next.loaded': 'Keep the whole ring and the space above it loaded. No sample or offering has been spent.',
    'care.bunnidogs.apex.next.active': 'An Apex is already alive within 48 blocks. Resolve that encounter before starting another here.',
    'care.bunnidogs.apex.next.permission': 'You cannot start a ritual here. The Crucible and each offering must allow your interaction.',
    'care.bunnidogs.apex.next.busy': 'This Crucible or an offering is already part of a ritual. Wait for that action to finish.',
    'care.bunnidogs.apex.next.changed': 'The sample, ring or keeper changed during summoning. The uncommitted Apex was removed; no additional items were spent. Review the current preparation.',
    'care.bunnidogs.apex.next.blocked': 'Clear dry shoulder and head space above the Crucible. The Apex could not spawn; your sample and offerings are kept.',
    'hint.bunnidogs.jar': '%1$s: Preserved sample & controls',
    'hint.bunnidogs.jar_take': '%1$s: Retrieve sample · Release %2$s to inspect',
    'hint.bunnidogs.jar_sample': '%1$s: Preserve sample in an empty Jar · Empty hands: Controls',
    'hint.bunnidogs.detector': '%1$s: Reference, front pad & signal controls',
    'hint.bunnidogs.detector_clear': '%1$s: Clear copied reference · Release %2$s to inspect',
    'hint.bunnidogs.detector_copy': '%1$s: Copy sample without spending it · Empty hands: Signal controls',
    'hint.bunnidogs.trophy': '%1$s: Display controls',
    'hint.bunnidogs.trophy_take': '%1$s: Retrieve displayed item · Release %2$s to inspect',
    'hint.bunnidogs.trophy_place': '%1$s: Display one item when empty · Empty hands: Inspect',
    'hint.bunnidogs.apex': '%1$s: Apex preparation & combat forecast',
    'hint.bunnidogs.syringe': '%1$s: Collect a reusable Tissue Sample · Healthy pups work too',
}

def main():
    for path in (RES/'assets/bunnidogs/lang').glob('*.json'):
        lang=json.loads(path.read_text()); lang.update(TEXT); path.write_text(json.dumps(lang,indent=2)+'\n')
    print('Generated preserved-sample, redstone, display and Apex preparation text.')

def goal_preview(log_path,marker='GOAL_PREVIEW ',filename='breeding_goal_preview.png',title='Breeding goal'):
    from pathlib import Path
    from PIL import Image, ImageDraw, ImageFont
    from gen_syringe import ROOT
    exported=[json.loads(line.split(marker,1)[1]) for line in Path(log_path).read_text().splitlines() if marker in line]
    if not exported: raise ValueError('No production goal layout in the native log')
    data=exported[-1]
    canvas=Image.new('RGB',(336,242),(43,46,39)); d=ImageDraw.Draw(canvas)
    font=ImageFont.truetype('DejaVuSans.ttf',9)
    label=ImageFont.truetype('DejaVuSans.ttf',9)
    d.text((8,5),title+' | production layout | substitute font',font=label,fill=(235,225,194))
    colors={'HEAD':(80,55,27),'INK':(47,44,29),'MUTED':(110,106,78),'GOOD':(45,100,53),'BAD':(150,48,35)}
    for side,x,frame in [('left',30,12),('right',176,168)]:
        d.rectangle((frame,26,frame+155,226),fill=(230,216,174),outline=(117,90,48))
        for row in data[side]:
            xx=x+row['x']-(font.getlength(row['text']) if row['right'] else 0)
            d.text((xx,48+row['y']),row['text'],font=font,fill=colors[row['tone']])
    canvas.resize((1344,968),Image.Resampling.NEAREST).save(ROOT/'art'/filename)
    print('Rendered the actual native goal spread, with a substitute font.')

if __name__=='__main__':
    import argparse
    parser=argparse.ArgumentParser(); parser.add_argument('--preview-from'); args=parser.parse_args()
    main()
    if args.preview_from:
        goal_preview(args.preview_from)
        goal_preview(args.preview_from,'SEED_FORECAST_PREVIEW ','actual_seed_forecast_preview.png','Actual seed forecast')
