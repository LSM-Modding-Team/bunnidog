"""Living offspring and commission fit reuse the Table's goal and the Board's actual checks."""
import json
from gen_syringe import RES, write
from gen_workshop import unlock

TEXT = {
    'care.bunnidogs.button.litter': 'Compare next real seed',
    'care.bunnidogs.button.read_seed': 'Read litter forecast',
    'care.bunnidogs.button.seed_family': 'Seed offspring (Calipers)',
    'care.bunnidogs.button.back_litter': 'Back to actual seed',
    'care.bunnidogs.litter.title': 'Actual litter seed',
    'care.bunnidogs.litter.source': 'Next inventory seed: %s',
    'care.bunnidogs.litter.none': 'No readable litter seed in your inventory.',
    'care.bunnidogs.litter.matches': 'This seed’s parent genomes and points match the Table samples, in either order.',
    'care.bunnidogs.litter.different': 'Different from the sampled parents. These figures use the actual seed’s saved parents and points.',
    'care.bunnidogs.litter.quality': 'Saved quality: %s · Mutation: %s per pup',
    'care.bunnidogs.litter.treated': 'This actual seed already carries Mutagen treatment.',
    'care.bunnidogs.litter.odds': 'Before mutation: matching pup %s · At least one in an uncrowded litter %s',
    'care.bunnidogs.litter.points': '%s: expected %s · target +%s',
    'care.bunnidogs.litter.genes': '%s · target %s',
    'care.bunnidogs.litter.before': 'Odds before mutation, at saved quality. Held guavas and tools do not change this seed. Fortune helps only when cutting a planted crop; Cradles add none.',
    'care.bunnidogs.litter.next.none': 'Bring a seed from actual courtship. Compare next real seed uses it without spending, treating or planting it.',
    'care.bunnidogs.litter.next.ready': 'Plant on farmland or use a Cradle, then compare the real pups. Quality is already saved; food and care still matter after hatching. Your seed stays in your inventory.',
    'care.bunnidogs.litter.next.genes': 'Before mutation, this seed cannot inherit all requested genes. Keep it for another purpose or plan a different pair; growing it longer will not change the parent genes.',
    'care.bunnidogs.litter.next.points': 'At saved quality these point targets are out of reach. A ripe planted crop can benefit from Fortune; Cradles add none. For a later litter, improve the parents or feed plan.',
    'care.bunnidogs.button.compare_goal': 'Compare commission goals (Calipers)',
    'care.bunnidogs.button.other_goal': 'Compare another goal',
    'care.bunnidogs.button.family': 'Check nearby offspring (Calipers)',
    'care.bunnidogs.button.family_prev': 'Previous animals',
    'care.bunnidogs.button.family_next': 'Next animals',
    'care.bunnidogs.button.show_family': 'Show these animals',
    'care.bunnidogs.family.title': 'Living offspring',
    'care.bunnidogs.family.fit_title': '%s · Commission fit',
    'care.bunnidogs.family.choose': 'Choose a purpose for this animal. Comparison is read-only; it changes no Table, Board or animal.',
    'care.bunnidogs.family.measure': 'Calipers measure actual inherited copies and points. Pups can be measured before you spend time raising them.',
    'care.bunnidogs.family.need_calipers': 'Keep Calipers in your inventory to measure living animals. Inspection never uses up the tool.',
    'care.bunnidogs.family.growth': 'about %sm %ss of loaded growth left',
    'care.bunnidogs.family.growth_locked': 'growth paused by a golden dandelion',
    'care.bunnidogs.family.next.age_locked': 'Growth is paused. Use a golden dandelion on this pup to resume natural growth; food cannot grow it while paused.',
    'hint.bunnidogs.growth_locked': 'Growth paused · Use a golden dandelion to resume',
    'care.bunnidogs.family.pup': 'Pup · %s. Plain guavas help it grow.',
    'care.bunnidogs.family.pup_locked': 'Pup · %s. Resume growth when you want an adult.',
    'care.bunnidogs.family.adult': 'Adult · ready to check its living conditions',
    'care.bunnidogs.family.adult_short': 'adult',
    'care.bunnidogs.family.parents': 'Both birth parents: %s',
    'care.bunnidogs.family.inherited': 'Genes and bred points are inherited. Growing up, feeding and exercise do not improve these targets.',
    'care.bunnidogs.family.at_board': 'Submission needs a calm adult with recorded parents, at least half health, a clear Board pad, a Breeder nearby and one paper. Your animal stays.',
    'care.bunnidogs.family.next.genes': 'This animal misses inherited gene targets. It can still have useful traits or be a parent; compare another mate or goal rather than waiting for its genes to change.',
    'care.bunnidogs.family.next.stats': 'This animal misses inherited point targets. Raise it for its current role or breed the next generation; food and exercise will not add these points.',
    'care.bunnidogs.family.next.adult': 'The inherited targets fit this pup. Help it grow, then check adult health, calm and equipment before taking it to the Board.',
    'care.bunnidogs.family.next.parents': 'This founder has no recorded birth parents. Use it as a useful resident or parent; the Board needs an offspring hatched from a litter seed.',
    'care.bunnidogs.family.next.health': 'The inherited targets fit. Let this adult heal to at least half health; ordinary suitable food helps.',
    'care.bunnidogs.family.next.transported': 'The inherited targets fit. Dismount the animal and remove its passengers before submission. A lead is allowed.',
    'care.bunnidogs.family.next.danger': 'The inherited targets fit. Let the animal calm down, leave danger and finish courtship before submission.',
    'care.bunnidogs.family.next.cargo': 'The inherited targets fit. Use a real native bundle on this adult to equip its courier pack; it stays equipped after submission.',
    'care.bunnidogs.family.next.ready': 'This animal fits the request. Lead it onto that Board’s clear front pad, keep a Breeder within eight blocks, choose the same request and submit one paper.',
    'care.bunnidogs.family.next.unavailable': 'This living animal is no longer available for ordinary care or commissions.',
    'care.bunnidogs.family.source': 'Within %s blocks of the Table, with the two sampled birth-parent genomes in either order. Several litters may match; samples do not identify individual parents.',
    'care.bunnidogs.family.seed_source': 'Within %s blocks of the Table, with the actual inventory seed’s two birth-parent genomes in either order. Several litters may match; birth records identify parent genomes, not individuals.',
    'care.bunnidogs.family.no_goal': 'Choose a breeding goal at this Table first.',
    'care.bunnidogs.family.entry': '#%s · %s · %s · Gene/point targets: %s / %s',
    'care.bunnidogs.family.position': 'At %s, %s, %s',
    'care.bunnidogs.family.inspect': 'Compare animal #%s',
    'care.bunnidogs.family.none': 'No matching offspring in the loaded nearby area. Hatch the pair’s real seed here, or compare another animal through Useful roles & work.',
    'care.bunnidogs.family.page': 'Page %s / %s · %s matching animals nearby',
    'care.bunnidogs.family.next': 'Compare the actual pups against this Table’s goal. Move within six blocks to open an animal’s measurements, or show brief glints above this page’s animals.',
}

def main():
    write('data/bunnidogs/tags/item/workshop_quills.json',{'replace':False,'values':['minecraft:feather','bunnidogs:whisker']})
    # Table recipes predate the workshop generators; keep their current starter alternative here.
    path=RES/'data/bunnidogs/recipe/genetics_table.json'
    recipe=json.loads(path.read_text()); recipe['key']['W']='#bunnidogs:workshop_quills'
    write('data/bunnidogs/recipe/genetics_table.json',recipe)
    unlock('genetics_table','#bunnidogs:workshop_quills')
    for path in (RES/'assets/bunnidogs/lang').glob('*.json'):
        value=json.loads(path.read_text()); value.update(TEXT); path.write_text(json.dumps(value,indent=2)+'\n')
    print('Generated live family and commission comparison text.')

if __name__ == '__main__':
    main()
