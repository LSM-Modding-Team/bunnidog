"""Player-facing pale-coat cave guidance and its earned native advancement."""
import json
from gen_syringe import RES, write


def main():
    text={'message.bunnidogs.cave_sight.unavailable':'This animal cannot guide your sight here.',
        'message.bunnidogs.cave_sight.busy':'This guide is already sharing its sight.',
        'message.bunnidogs.cave_sight.adult':'Only adult guides can share cave sight.',
        'message.bunnidogs.cave_sight.health':'The guide needs at least half health.',
        'message.bunnidogs.cave_sight.traits':'Needs an expressed pale coat and Senses +20.',
        'message.bunnidogs.cave_sight.combat':'The guide must be calm and out of courtship.',
        'message.bunnidogs.cave_sight.resting':'Cave sight recovers in %s seconds.',
        'message.bunnidogs.cave_sight.bright':'Find dim light first (light level 7 or lower).',
        'message.bunnidogs.cave_sight.already':'You already have Night Vision; keep the berry.',
        'message.bunnidogs.cave_sight.ready':'Ready: sneak-feed a glow berry in dim light.',
        'message.bunnidogs.cave_sight.denied':'Cave sight was blocked; your berry is returned.',
        'message.bunnidogs.cave_sight.given':'Your pale companion guides your sight for %s seconds.',
        'message.bunnidogs.care.cave_sight':'Cave sight: %s',
        'advancements.bunnidogs.pale_guide.title':'By Pale Lantern',
        'advancements.bunnidogs.pale_guide.description':'Receive cave sight from a trained pale-coated companion'}
    path=RES/'assets/bunnidogs/lang/en_us.json'; language=json.loads(path.read_text()); language.update(text)
    path.write_text(json.dumps(language,indent=2)+'\n')
    write('data/bunnidogs/advancement/pale_guide.json',{'parent':'bunnidogs:discerning_ear','display':{
        'icon':{'id':'minecraft:glow_berries'},'title':{'translate':'advancements.bunnidogs.pale_guide.title'},
        'description':{'translate':'advancements.bunnidogs.pale_guide.description'},'frame':'goal','show_toast':True,'announce_to_chat':False,'hidden':False},
        'criteria':{'guided':{'trigger':'minecraft:impossible'}},'requirements':[['guided']],'rewards':{'experience':25}})
    print('Generated pale-coat cave guidance feedback and its native advancement.')


if __name__=='__main__': main()
