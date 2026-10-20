"""Short contextual controls and a native first-litter journey; no additional guide pages."""
import json
from gen_syringe import RES, write

TEXT = {
    'screen.bunnidogs.care.title': 'Bunnidog care',
    'care.bunnidogs.loading': 'Checking nearby care…',
    'care.bunnidogs.empty': 'empty',
    'care.bunnidogs.stock': '%s × %s',
    'care.bunnidogs.on': 'enabled',
    'care.bunnidogs.off': 'disabled',
    'care.bunnidogs.animal.health': 'Health: %s / %s',
    'care.bunnidogs.animal.food': 'Healing food: %s',
    'care.bunnidogs.animal.no_home': 'Home: no Nest claimed',
    'care.bunnidogs.animal.home': 'Home: Nest at %s, %s, %s',
    'care.bunnidogs.animal.breeding': 'Courtship: %s',
    'care.bunnidogs.animal.young': 'still a pup',
    'care.bunnidogs.animal.courting': 'looking for a partner',
    'care.bunnidogs.animal.rest': 'recovering for %s seconds',
    'care.bunnidogs.animal.ready': 'ready for a guava',
    'care.bunnidogs.animal.work': 'Home crop work: %s',
    'care.bunnidogs.animal.traits': 'Temperament: %s · Build: %s',
    'care.bunnidogs.animal.cargo': 'Bundles: %s / %s',
    'care.bunnidogs.animal.trail': 'Trail: %s · %s',
    'care.bunnidogs.animal.provisions': 'Bundle meals: %s',
    'care.bunnidogs.animal.training': 'Loyal/stoic adults can train as partners. Keep a Whistle and a plain guava in your inventory.',
    'care.bunnidogs.animal.trail_changed': 'Travel needs an expressed loyal/stoic temperament. The bond remains; its keeper can release it below.',
    'care.bunnidogs.animal.grooming': 'Brush yield: %s · recovery: %ss',
    'care.bunnidogs.animal.next.safety': 'Let this animal reach safety before care or pairing.',
    'care.bunnidogs.animal.next.heal': 'Needs healing. Care uses one plain guava; suitable hand-fed foods are listed below.',
    'care.bunnidogs.animal.next.grow': 'Let this pup grow, or feed it guavas to help. It can claim a Nest as an adult.',
    'care.bunnidogs.animal.next.trail': 'The trained keeper can choose an order below. Travel retains the bond and any existing Nest.',
    'care.bunnidogs.animal.next.transport': 'Release leads or dismount before pairing. Care and inventory inspection remain available.',
    'care.bunnidogs.animal.next.home': 'Lead this adult to an empty Nest. Open the Nest and choose Assign led adult.',
    'care.bunnidogs.animal.next.courting': 'Bring another guava-fed adult close. Their courtship drops one litter seed.',
    'care.bunnidogs.animal.next.rest': 'This parent is resting. Breeding food is kept until it is ready again.',
    'care.bunnidogs.animal.next.breed': 'Feed a guava normally to each of two adults. Keep them close to breed a litter seed.',
    'care.bunnidogs.button.genetics': 'Genetics (Calipers)',
    'care.bunnidogs.button.care': 'Care (1 guava)',
    'care.bunnidogs.button.courtship': 'Courtship (1 guava)',
    'care.bunnidogs.button.train': 'Train (Whistle + guava)',
    'care.bunnidogs.button.groom': 'Groom (Brush)',
    'care.bunnidogs.button.release': 'Release training',
    'care.bunnidogs.button.work_on': 'Resume home work',
    'care.bunnidogs.button.work_off': 'Pause home work',
    'care.bunnidogs.button.follow': 'Follow me',
    'care.bunnidogs.button.wait': 'Wait here',
    'care.bunnidogs.button.resident': 'Return home',
    'care.bunnidogs.button.resident_roam': 'Resume roaming',
    'care.bunnidogs.button.provisions_on': 'Allow bundle meals',
    'care.bunnidogs.button.provisions_off': 'Stop bundle meals',
    'care.bunnidogs.button.take_bundle': 'Retrieve last bundle',
    'care.bunnidogs.button.cargo': 'Inspect cargo',
    'care.bunnidogs.button.back': 'Back to care',
    'care.bunnidogs.cargo.title': '%s · Cargo',
    'care.bunnidogs.cargo.next': 'The last bundle is retrieved first. Inspecting keeps every item; retrieve a bag to open its full native contents.',
    'care.bunnidogs.cargo.bag': 'Bundle %s: %s',
    'care.bunnidogs.cargo.more': '… and %s more stacks',
    'care.bunnidogs.cargo.more_bags': '… and %s more bundles; retrieve the last to inspect them.',
    'care.bunnidogs.cargo.unsupported': 'Contents are kept unchanged. Retrieve this bundle to inspect it natively.',
    'care.bunnidogs.button.tending_on': 'Enable crop work',
    'care.bunnidogs.button.tending_off': 'Disable crop work',
    'care.bunnidogs.button.take_food': 'Withdraw food',
    'care.bunnidogs.button.clear_filter': 'Clear food filter',
    'care.bunnidogs.button.add_food': 'Add guava (1)',
    'care.bunnidogs.button.pair': 'Feed both parents',
    'care.bunnidogs.button.take_left': 'Withdraw manger A',
    'care.bunnidogs.button.take_right': 'Withdraw manger B',
    'care.bunnidogs.button.add_left': 'Add guava to A (1)',
    'care.bunnidogs.button.add_right': 'Add guava to B (1)',
    'care.bunnidogs.button.hatch': 'Hatch litter',
    'care.bunnidogs.button.take_seed': 'Withdraw seed',
    'care.bunnidogs.button.add_seed': 'Insert seed (1)',
    'care.bunnidogs.button.grow': 'Grow (1 bone meal)',
    'care.bunnidogs.button.auto_on': 'Allow redstone hatch',
    'care.bunnidogs.button.auto_off': 'Manual hatching only',
    'care.bunnidogs.button.take_juice': 'Collect juice',
    'care.bunnidogs.button.take_fruit': 'Withdraw fruit',
    'care.bunnidogs.button.take_carrier': 'Withdraw carriers',
    'care.bunnidogs.button.add_fruit': 'Add guava (1)',
    'care.bunnidogs.button.add_bottle': 'Add bottle (1)',
    'care.bunnidogs.button.rehome': 'Assign led adult',
    'care.bunnidogs.button.submit': 'Submit (1 paper)',
    'care.bunnidogs.trough.next.power': 'Redstone power pauses resident meals and work. Remove the signal to resume.',
    'care.bunnidogs.trough.next.rich': 'Upgraded guavas are kept for breeding. Withdraw this saved stock, then add ordinary meal food.',
    'care.bunnidogs.trough.rich': 'Upgraded guavas are breeding food. Kept; stock plain guavas or other ordinary meals.',
    'care.bunnidogs.trough.next.food': 'Add plain guavas below, or use another suitable food on the Trough. Plain guavas suit every resident.',
    'care.bunnidogs.trough.next.ready': 'Injured nearby residents can eat. Crop work is optional; choose its purpose below.',
    'care.bunnidogs.trough.stock': 'Food: %s',
    'care.bunnidogs.trough.filter': 'Reserved food: %s',
    'care.bunnidogs.trough.work': 'Work: %s · %s',
    'care.bunnidogs.trough.mode.mixed': 'Mixed: harvest, tend, gather and compost surplus. Does not spend food planting new crops.',
    'care.bunnidogs.trough.mode.garden': 'Garden: harvest and restore crops, spending suitable stock for new plantings.',
    'care.bunnidogs.trough.mode.gather': 'Gather: carry nearby suitable dropped food into this Trough.',
    'care.bunnidogs.trough.mode.compost': 'Compost: turn stock above the eight-item reserve into compost with Bloom +10.',
    'care.bunnidogs.trough.mode.nursery': 'Nursery: Bloom +20 adults tend nearby Cradle stages with plain guavas, keeping eight in reserve.',
    'care.bunnidogs.trough.mode.supply': 'Supply: Senses +20 adults move ordinary surplus to nearby compatible Troughs, keeping eight at source.',
    'care.bunnidogs.trough.meals': 'Trough meals heal; they never start courtship. A hoe remains a quick work control.',
    'care.bunnidogs.pen.left': 'Manger A: %s',
    'care.bunnidogs.pen.right': 'Manger B: %s',
    'care.bunnidogs.pen.parent': 'Parent %s: %s · %s',
    'care.bunnidogs.pen.seed': 'This station feeds the pair. Normal courtship then drops a seed; plant it on farmland.',
    'care.bunnidogs.pen.next.ready': 'Both parents are ready. Feed both parents spends one guava from each manger.',
    'care.bunnidogs.pen.next.pair': 'Bring exactly two adult residents; move other adults at least 12 blocks away.',
    'care.bunnidogs.pen.next.too_far': 'Bring both parents within four blocks of the Pen and less than three blocks apart.',
    'care.bunnidogs.pen.next.unavailable': 'Parents need full health, no breeding rest or danger, and freedom from leads, riders and water.',
    'care.bunnidogs.pen.next.home': 'Give each parent its own Nest. Keep both parents within their home area.',
    'care.bunnidogs.pen.next.unsafe': 'Clear a safe, dry floor and a visible path between the parents.',
    'care.bunnidogs.pen.next.unloaded': 'Keep the whole pairing area nearby and loaded.',
    'care.bunnidogs.pen.next.protected': 'Protection is preventing this automated pairing.',
    'care.bunnidogs.pen.next.empty': 'Add a guava to each manger below. Using a guava on each side also works, including richer tiers.',
    'care.bunnidogs.pen.next.changed': 'The pairing area changed. Check both parents and reopen the station.',
    'care.bunnidogs.pen.next.busy': 'The station is already feeding a pair.',
    'care.bunnidogs.pen.next.fed': 'The pair has been fed. Keep them together and collect their litter seed.',
    'care.bunnidogs.cradle.next.seed': 'Insert a seed from your inventory below, or use a chosen seed on the Cradle. Farmland also works.',
    'care.bunnidogs.cradle.next.grow': 'Let the seed grow, or use bone meal. Starlit Dust ripens it immediately.',
    'care.bunnidogs.cradle.next.hatch': 'The litter is ripe and its landing pad is clear. Choose Hatch litter.',
    'care.bunnidogs.cradle.next.space': 'Clear the dry 3 × 3 pad two blocks in front, with two air blocks above it. Keep animals out of the pad.',
    'care.bunnidogs.cradle.seed': 'Seed: %s',
    'care.bunnidogs.cradle.growth': 'Growth: %s / %s · next stage in %ss',
    'care.bunnidogs.cradle.pad': 'Landing pad: %s',
    'care.bunnidogs.cradle.blocked_at': 'Obstruction at %s, %s, %s',
    'care.bunnidogs.cradle.auto': 'Redstone hatching: %s',
    'care.bunnidogs.cradle.grow': 'A loaded minute grows one stage. Withdrawing a seed resets its growth.',
    'care.bunnidogs.juicer.next.power': 'Redstone power pauses the press. Remove the signal to continue.',
    'care.bunnidogs.juicer.next.output': 'Collect the output to make room for another bottle.',
    'care.bunnidogs.juicer.next.inputs': 'Add a guava and a glass bottle below, or use your chosen ingredients on the press. One of each makes juice.',
    'care.bunnidogs.juicer.fruit': 'Fruit or litter seed: %s',
    'care.bunnidogs.juicer.carrier': 'Bottles or plain juice: %s',
    'care.bunnidogs.juicer.output': 'Output: %s',
    'care.bunnidogs.juicer.progress': 'Pressing: %s%%',
    'care.bunnidogs.juicer.infuse': 'Optional infusion: one litter seed + uninfused juice. This spends the seed instead of hatching it.',
    'care.bunnidogs.nest.claimed': 'Nest: claimed',
    'care.bunnidogs.nest.free': 'Nest: available',
    'care.bunnidogs.nest.resident': 'Nearby resident: %s',
    'care.bunnidogs.nest.one': 'One adult per Nest. Nearby adults can claim a free Nest naturally.',
    'care.bunnidogs.nest.range': 'Residents return home at night and normally work within 16 blocks.',
    'care.bunnidogs.nest.compass': 'Use a compass on this Nest to mark the way back.',
    'care.bunnidogs.nest.next.claimed': 'Keep this resident’s floor and walking route clear. Leads and baskets take priority over home routines.',
    'care.bunnidogs.nest.next.lead': 'Lead an adult within eight blocks, then choose Assign led adult. A lead used on the Nest also works.',
    'care.bunnidogs.board.request': 'Request: %s',
    'care.bunnidogs.board.progress': 'Your completed requests: %s / %s',
    'care.bunnidogs.board.candidate': 'Candidate: %s',
    'care.bunnidogs.board.choice': '%s',
    'care.bunnidogs.board.done': '✓ %s',
    'care.bunnidogs.board.check': '%s: %s / %s',
    'care.bunnidogs.board.fixed': 'Fixed genes mean two matching copies. Stats are inherited through breeding, not earned by exercise.',
    'care.bunnidogs.board.setup': 'A villager takes the Breeder job at a Breeding Pen. Keep the Breeder within eight blocks and one calm bred adult on the clear front pad.',
    'care.bunnidogs.board.next.done': 'You have completed this request. Choose another target below; your animal stays with you.',
    'care.bunnidogs.board.next.submit': 'This candidate qualifies. Submit one paper from your inventory to claim the reward once.',
    'care.bunnidogs.board.next.paper': 'The candidate qualifies. Bring one paper to submit; your animal and equipment stay.',
    'care.bunnidogs.check.gene.coat': 'Fixed %s coat',
    'care.bunnidogs.check.gene.ears': 'Fixed %s ears',
    'care.bunnidogs.check.gene.legs': 'Fixed %s legs',
    'care.bunnidogs.check.gene.build': 'Fixed %s build',
    'care.bunnidogs.check.gene.diet': 'Fixed %s diet',
    'care.bunnidogs.check.gene.fertility': 'Fixed %s fertility',
    'care.bunnidogs.check.stat': 'Bred %s',
    'care.bunnidogs.check.build': 'Fixed Build’s bundle capacity',
    'care.bunnidogs.check.fixed': 'Fixed gene pairs',
    'care.bunnidogs.check.origins': 'Expressed wild origins',
    'care.bunnidogs.check.cargo': 'Equipped bundles',
    'care.bunnidogs.feedback.courtship': 'Ready to pair. Feed another adult a guava and keep them close.',
    'care.bunnidogs.feedback.courting': 'Already courting. Bring another guava-fed adult close.',
    'care.bunnidogs.feedback.rest': 'Breeding rest: %s seconds. Your food is kept.',
    'care.bunnidogs.feedback.seed': 'A litter seed! Plant it on farmland; break the ripe crop to hatch pups.',
    'care.bunnidogs.feedback.planted': 'Let the litter crop ripen, then break it to hatch pups. Bone meal speeds growth.',
    'care.bunnidogs.feedback.changed': 'This target changed. Review the updated stock and controls, then try again.',
    'care.bunnidogs.feedback.incubating': 'The litter is incubating. When ripe, open the Cradle and choose Hatch litter.',
    'care.bunnidogs.choose.title': 'Choose a commission',
    'care.bunnidogs.choose.hint': 'Choose a breeding goal. The Board will show the candidate’s progress toward it.',
    'care.bunnidogs.choose.open': 'Choose request',
    'care.bunnidogs.choose.back': 'Back to candidate',
    'care.bunnidogs.guava.use': 'Feed two adults to breed a litter seed.',
    'care.bunnidogs.seed.use': 'Plant on farmland; break the ripe crop to hatch pups.',
    'care.bunnidogs.seed.details': 'Calipers in the other hand: inspect the litter.',
    'care.bunnidogs.seed.treated': 'Treated with Mutagen',
    'hint.bunnidogs.animal': '%1$s: Care & breeding',
    'hint.bunnidogs.unleash': '%1$s: Release lead · Use the Nest to assign a home',
    'hint.bunnidogs.bundle': '%1$s: Retrieve last bundle',
    'hint.bunnidogs.grow': '%1$s: Help this pup grow',
    'hint.bunnidogs.breed': '%1$s: Courtship · %2$s + %1$s: Plain-guava care',
    'hint.bunnidogs.heal': '%1$s: Heal with a plain guava',
    'hint.bunnidogs.whistle': '%1$s: Train or give an order · Guava in other hand to train',
    'hint.bunnidogs.brush': '%1$s: Groom',
    'hint.bunnidogs.calipers': '%1$s: Genetics · %2$s + %1$s: Detailed care',
    'hint.bunnidogs.book': '%1$s: Record this animal',
    'hint.bunnidogs.bowl': '%1$s: Toggle your partner’s bundle meals',
    'hint.bunnidogs.pack': '%1$s: Equip this bundle as a pack · Empty hands: Cargo controls',
    'hint.bunnidogs.feed': '%1$s: Feed for healing or pup growth',
    'hint.bunnidogs.rich': 'Upgraded guavas are kept for breeding · Stock ordinary meals',
    'hint.bunnidogs.compass': '%1$s: Mark home · %2$s + %1$s: Mineral survey',
    'hint.bunnidogs.station': '%1$s: Status & controls',
    'hint.bunnidogs.station_shortcut': 'Release %2$s for status & controls',
    'hint.bunnidogs.nest': '%1$s: Assign a led adult',
    'hint.bunnidogs.submit': '%1$s: Submit 1 paper · Empty hands: Choose request',
    'hint.bunnidogs.stock': '%1$s: Stock food · Empty hands: Meals & work controls',
    'hint.bunnidogs.manger': '%1$s: Stock this side · Empty hands: Pairing controls',
    'hint.bunnidogs.cradle_seed': '%1$s: Insert litter seed · Empty hands: Nursery controls',
    'hint.bunnidogs.plant': '%1$s: Plant litter seed',
    'hint.bunnidogs.crop_ripe': '%3$s: Hatch the ripe litter',
    'hint.bunnidogs.crop_growing': 'Litter growing · Bone meal speeds it up · Wait until ripe',
    'hint.bunnidogs.fruit_ripe': '%1$s: Pick a ripe guava · The hanging fruit regrows',
    'hint.bunnidogs.fruit_growing': 'Guava ripening · Wait or use bone meal · Pick when ripe',
    'hint.bunnidogs.fruit_bonemeal': '%1$s: Help this guava ripen',
    'hint.bunnidogs.fruit_release': 'Release %2$s, then %1$s: Pick this ripe guava',
    'tooltip.bunnidogs.guide.optional': 'Optional reference for genetics and advanced systems.',
    'tooltip.bunnidogs.guide.controls': 'Empty hands on animals and care stations: needs and controls.',
    'care.bunnidogs.button.read_table': 'Read breeding plans',
    'care.bunnidogs.button.add_sample': 'Place next sample',
    'care.bunnidogs.button.take_a': 'Retrieve sample A',
    'care.bunnidogs.button.take_b': 'Retrieve sample B',
    'care.bunnidogs.button.award': 'Claim eligible ribbon',
    'care.bunnidogs.table.slot': '%s: %s',
    'care.bunnidogs.table.look': '%s traits: %s',
    'care.bunnidogs.table.unreadable': 'unreadable saved sample; retrieve it intact',
    'care.bunnidogs.table.next_sample': 'Next inventory sample: %s',
    'care.bunnidogs.table.obtain': 'Use a Sampling Syringe on a healthy bunnidog to collect a reusable sample; pups work too. Use a held sample here to choose it directly.',
    'care.bunnidogs.table.cues': 'Plans use plain guavas by default. Use this Table with guavas, Mutagen or a Fortune tool in hand to preview those choices without spending them.',
    'care.bunnidogs.table.next.collect': 'Collect a sample from each parent. One sample shows its genes; two reveal litter forecasts and inheritance odds.',
    'care.bunnidogs.table.next.place': 'Place next sample fills A, then B. The exact next inventory sample is shown below; occupied slots are kept.',
    'care.bunnidogs.table.next.compare': 'Compare these parents before breeding. The plans show pup traits, bred stats, inherited copies and optional splices; samples stay reusable.',
    'care.bunnidogs.table.next.unreadable': 'A saved sample cannot be read. Retrieve it intact to free a slot; readable samples can still be inspected.',
    'care.bunnidogs.show.entrant': 'Entrant: %s',
    'care.bunnidogs.show.tier': 'Current stat tier: %s',
    'care.bunnidogs.show.parents': 'Birth parents: %s',
    'care.bunnidogs.show.recorded': 'recorded',
    'care.bunnidogs.show.missing': 'not recorded; breed a representative',
    'care.bunnidogs.show.fixed': 'Matching gene pairs: %s / %s (including Diet)',
    'care.bunnidogs.show.mixed': 'Mixed pairs: %s',
    'care.bunnidogs.show.stat': '%s: %s / +%s',
    'care.bunnidogs.show.target': 'Stat target below: %s',
    'care.bunnidogs.show.once': 'Each animal earns a tier once. Ribbons keep its winning record; display one on a Trophy Pedestal.',
    'care.bunnidogs.show.lead': 'Lead one adult within six blocks of this Stand. The closest adult on your lead is inspected.',
    'care.bunnidogs.show.thresholds': 'Every bred stat needs +5 for bronze, +20 for silver or +50 for gold. Stable genes, recorded birth parents and at least half health are also required.',
    'care.bunnidogs.show.next.lead': 'Bring a led adult to see its scorecard. Inspection never issues an award.',
    'care.bunnidogs.show.next.parents': 'This animal needs recorded birth parents. Breed a representative of the line, then return with that adult.',
    'care.bunnidogs.show.next.mixed': 'Fix both copies of every gene, including Diet. The Genetics Table compares parents and shows inherited-copy odds.',
    'care.bunnidogs.show.next.health': 'Let this entrant recover to at least half health. Feed suitable food or stock its Trough.',
    'care.bunnidogs.show.next.stats': 'Breed toward +5 in all five stats for bronze. The Table forecasts points; a single strong specialty does not qualify.',
    'care.bunnidogs.show.next.already': 'This entrant already earned its current tier. A stronger bred representative can work toward the next ribbon.',
    'care.bunnidogs.show.next.complete': 'Gold target met; this animal has no higher ribbon to claim. Display its winning ribbon on a Trophy Pedestal, or try a specialized commission.',
    'care.bunnidogs.show.next.ready': 'This entrant qualifies. Claim its highest eligible ribbon below; the animal and lead stay intact.',
    'hint.bunnidogs.table': '%1$s: Samples & breeding plans',
    'hint.bunnidogs.table_take': '%1$s: Retrieve last sample · Release %2$s for controls',
    'hint.bunnidogs.sample': '%1$s: Place sample (swaps B when full) · Empty hands: Plans & retrieval',
    'hint.bunnidogs.table_read': '%1$s: Read plans using held cues · Nothing is spent',
    'hint.bunnidogs.show': '%1$s: Entrant scorecard & ribbon controls',
    'hint.bunnidogs.show_preview': '%1$s: Quick scorecard · Release %2$s for controls',
    'hint.bunnidogs.judge': '%1$s: Judge led adult · Empty hands: Preview first',
    'tooltip.bunnidogs.tissue.table': 'Place on a Genetics Table to compare parents. Samples are reusable.',
    'message.bunnidogs.cradle.ready_manual': 'Ripe litter: open the Cradle and choose Hatch litter. Redstone hatching is disabled.',
}

STEPS = {
    'root': ('Bunnidogs', 'Discover guavas, animals or a care station. Empty hands open needs and controls.', 'bunnidogs:guava', None,
             ['nest','feeding_trough','calipers','pedigree_book']),
    'home': ('A Place to Call Home', 'Lead an adult to a Nest and assign it a home. Each adult needs its own Nest.', 'bunnidogs:nest','root', ['feeding_trough','travel_basket']),
    'courtship': ('A Guava for Two', 'Feed a guava normally to each of two adults. Keep them close; their courtship makes a litter seed.', 'bunnidogs:guava','root', []),
    'seed': ('A Litter in Your Hand', 'Collect the pair’s seed. Plant it on farmland; advanced nursery stations are optional.', 'bunnidogs:seed','courtship', ['nursery_cradle']),
    'plant': ('Growing a Family', 'Plant a litter seed on farmland or insert it in a Cradle. Wait or use bone meal; hatch when ripe.', 'minecraft:wheat_seeds','seed', []),
    'litter': ('The First Litter', 'Hatch real pups from a ripe crop or Cradle. Give them time, food and room to grow.', 'bunnidogs:seed','plant', ['breeding_pen','commission_board','syringe','genetics_table','ear_tag']),
}

def main():
    for step,(title,description,icon,parent,recipes) in STEPS.items():
        display={'icon':{'id':icon},'title':{'translate':f'journey.bunnidogs.{step}.title'},
                 'description':{'translate':f'journey.bunnidogs.{step}.description'},'frame':'goal' if step=='litter' else 'task',
                 'show_toast':True,'announce_to_chat':False,'hidden':False}
        if parent is None: display['background']='minecraft:block/hay_block_side'
        value={'display':display,'criteria':{'done':{'trigger':'minecraft:impossible'}},'requirements':[['done']]}
        if parent is not None: value['parent']='bunnidogs:journey/'+parent
        else:
            value['criteria']={'met':{'trigger':'minecraft:impossible'},'food':{'trigger':'minecraft:inventory_changed',
                'conditions':{'items':[{'items':'#bunnidogs:bunnidog_food'}]}}}
            value['requirements']=[['met','food']]
        if recipes: value['rewards']={'recipes':['bunnidogs:'+r for r in recipes]}
        write('data/bunnidogs/advancement/journey/'+step+'.json',value)
        TEXT[f'journey.bunnidogs.{step}.title']=title
        TEXT[f'journey.bunnidogs.{step}.description']=description
    # Keep the starter home usable away from guava timber; all native wood works.
    path=RES/'data/bunnidogs/recipe/nest.json'
    nest=json.loads(path.read_text()); nest['key']['P']='#minecraft:planks'; write('data/bunnidogs/recipe/nest.json',nest)
    for path in (RES/'assets/bunnidogs/lang').glob('*.json'):
        lang=json.loads(path.read_text()); lang.update(TEXT); path.write_text(json.dumps(lang,indent=2)+'\n')
    print('Generated six native first-litter milestones, starter recipe and concise contextual text.')

def preview_from_log(log_path):
    """Draw production view data in the screen's layout, with an explicitly substitute font."""
    import re
    from pathlib import Path
    from PIL import Image, ImageDraw, ImageFont
    from gen_syringe import ROOT
    views={}
    for line in Path(log_path).read_text().splitlines():
        if 'CARE_PREVIEW ' in line:
            value=json.loads(line.split('CARE_PREVIEW ',1)[1]); views[value['name']]=value
    core_names=('Injured companion','Trained partner','Food and garden work','Juicer inputs','Blocked nursery','Commission progress')
    core={name:views.pop(name) for name in core_names}
    workshop=views; views=core
    lang=json.loads((RES/'assets/bunnidogs/lang/en_us.json').read_text())
    def text(value):
        if isinstance(value,list): return ''.join(text(v) for v in value)
        if not isinstance(value,dict): return str(value)
        result=value.get('text','')
        if 'translate' in value:
            key=value['translate']; result=lang.get(key,key.split('.')[-1].replace('_',' ').title())
            args=value.get('with',[]); cursor=0
            def substitute(match):
                nonlocal cursor
                index=int(match[1])-1 if match[1] else cursor
                if not match[1]: cursor+=1
                return text(args[index]) if index<len(args) else match[0]
            result=re.sub(r'%(?:(\d+)\$)?s',substitute,result).replace('%%','%')
        return result+''.join(text(v) for v in value.get('extra',[]))
    font=ImageFont.truetype('DejaVuSans.ttf',9); bold=ImageFont.truetype('DejaVuSans-Bold.ttf',10)
    def lines(value,width=384):
        result=[]
        for paragraph in text(value).split('\n'):
            line=''
            for word in paragraph.split():
                proposed=(line+' '+word).strip()
                if line and font.getlength(proposed)>width: result.append(line); line=word
                else: line=proposed
            result.append(line)
        return result
    def panel(view,choosing=False):
        im=Image.new('RGB',(420,360),(34,28,22)); d=ImageDraw.Draw(im); d.rectangle((0,0,419,359),outline=(225,199,144))
        d.text((12,9),text({'translate':'care.bunnidogs.choose.title'}) if choosing else text(view['title']),font=bold,fill=(244,231,201))
        body=Image.new('RGB',(396,1200),(34,28,22)); draw=ImageDraw.Draw(body); y=0
        def paragraph(value):
            nonlocal y
            color={'green':(85,255,85),'red':(255,85,85)}.get(value.get('color') if isinstance(value,dict) else None,(230,225,214))
            for line in lines(value): draw.text((0,y),line,font=font,fill=color); y+=10
            y+=7
        def buttons(section):
            nonlocal y
            group=[c for c in view['controls'] if c.get('section',0)==section]
            for i,control in enumerate(group):
                x=(i%2)*196; yy=y+(i//2)*27
                draw.rectangle((x,yy,x+187,yy+19),fill=(79,76,70) if control['enabled'] else (48,46,42),outline=(135,130,116))
                label=text(control['label']); draw.text((x+94-font.getlength(label)/2,yy+4),label,font=font,fill=(255,255,255) if control['enabled'] else (136,132,123))
            y+=((len(group)+1)//2)*27
        paragraph({'translate':'care.bunnidogs.choose.hint'} if choosing else view['next'])
        if choosing: buttons(2)
        else:
            for row in view['rows'][:2]: paragraph(row)
            buttons(0)
            for row in view['rows'][2:]: paragraph(row)
            buttons(1)
        im.paste(body.crop((0,0,396,290)),(12,30))
        if y>290:
            d.rectangle((402,30,407,319),fill=(12,10,8)); d.rectangle((402,30,407,30+max(14,int(290*290/y))),fill=(163,155,133))
        choices=any(c.get('section')==2 for c in view['controls'])
        for x,width,label in ([(12,194,'Done'),(214,194,text({'translate':'care.bunnidogs.choose.back' if choosing else 'care.bunnidogs.choose.open'}))] if choices else [(12,396,'Done')]):
            d.rectangle((x,332,x+width-1,351),fill=(79,76,70),outline=(135,130,116)); d.text((x+(width-font.getlength(label))/2,336),label,font=font,fill='white')
        return im
    canvas=Image.new('RGB',(1320,820),(39,44,39)); d=ImageDraw.Draw(canvas)
    d.text((12,9),'Care panel layout | native server snapshots | substitute font; client rendering pending',font=bold,fill=(235,225,194))
    for i,(name,view) in enumerate(views.items()):
        x=12+(i%3)*436; y=35+(i//3)*390
        d.text((x,y),name,font=bold,fill=(235,225,194)); canvas.paste(panel(view),(x,y+17))
    canvas.resize((2640,1640),Image.Resampling.NEAREST).save(ROOT/'art/cohesion_preview.png')
    panel(views['Commission progress'],True).resize((840,720),Image.Resampling.NEAREST).save(ROOT/'art/commission_choices_preview.png')
    family_names=('Useful first roles','Chosen breeding goal')
    family={name:workshop.pop(name) for name in family_names}
    family_canvas=Image.new('RGB',(884,430),(39,44,39)); fd=ImageDraw.Draw(family_canvas)
    fd.text((12,9),'Useful family roles and breeding goals | native snapshots | substitute font',font=bold,fill=(235,225,194))
    for i,(name,view) in enumerate(family.items()):
        x=12+i*440; fd.text((x,35),name,font=bold,fill=(235,225,194)); family_canvas.paste(panel(view),(x,52))
    family_canvas.resize((1768,860),Image.Resampling.NEAREST).save(ROOT/'art/family_goals_preview.png')
    progression_names=('Actual litter against goal','Living sampled family','Promising measured pup')
    progression={name:workshop.pop(name) for name in progression_names}
    progress=Image.new('RGB',(1320,430),(39,44,39)); pd=ImageDraw.Draw(progress)
    pd.text((12,9),'Plan, actual seed and living offspring | native snapshots | substitute font',font=bold,fill=(235,225,194))
    for i,(name,view) in enumerate(progression.items()):
        x=12+i*436; pd.text((x,35),name,font=bold,fill=(235,225,194)); progress.paste(panel(view),(x,52))
    progress.resize((2640,860),Image.Resampling.NEAREST).save(ROOT/'art/family_progression_preview.png')
    work_rows=(len(workshop)+1)//2
    work=Image.new('RGB',(884,40+390*work_rows),(39,44,39)); wd=ImageDraw.Draw(work)
    wd.text((12,9),'Workshop controls | native server snapshots | substitute font',font=bold,fill=(235,225,194))
    for i,(name,view) in enumerate(workshop.items()):
        x=12+(i%2)*440; y=35+(i//2)*390
        wd.text((x,y),name,font=bold,fill=(235,225,194)); work.paste(panel(view),(x,y+17))
    work.resize((1768,work.height*2),Image.Resampling.NEAREST).save(ROOT/'art/workshop_controls_preview.png')
    print(f'Rendered {6+len(workshop)+len(family)+len(progression)} native care/workshop snapshots and the request selector layout (substitute font).')

if __name__=='__main__':
    import argparse
    parser=argparse.ArgumentParser(); parser.add_argument('--preview-from'); args=parser.parse_args()
    main()
    if args.preview_from: preview_from_log(args.preview_from)
