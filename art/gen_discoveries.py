"""The original mutation-observation goal and a saved-book collection spanning every wild genetic origin."""
import json
from gen_syringe import RES, write


def main():
    for name,icon,criterion,xp in [('nature_finds_a_way','bunnidogs:mutagen','observed',50),
                                  ('wild_gene_library','bunnidogs:pedigree_book','catalogued',100)]:
        write(f'data/bunnidogs/advancement/{name}.json',{'parent':'bunnidogs:enter_warren','display':{
            'icon':{'id':icon},'title':{'translate':f'advancements.bunnidogs.{name}.title'},
            'description':{'translate':f'advancements.bunnidogs.{name}.description'},'frame':'goal',
            'show_toast':True,'announce_to_chat':False,'hidden':False},'criteria':{criterion:{'trigger':'minecraft:impossible'}},
            'requirements':[[criterion]],'rewards':{'experience':xp}})
    path=RES/'assets/bunnidogs/lang/en_us.json'; language=json.loads(path.read_text())
    language.update({'advancements.bunnidogs.nature_finds_a_way.title':'Nature Finds a Way',
        'advancements.bunnidogs.nature_finds_a_way.description':'Record a bred animal carrying a mutation-only allele',
        'advancements.bunnidogs.wild_gene_library.title':'Wild Gene Library',
        'advancements.bunnidogs.wild_gene_library.description':'Record genes from every wild lineage in one Pedigree Book',
        'tooltip.bunnidogs.records.wild':'Wild lineage signatures: %s / %s'})
    path.write_text(json.dumps(language,indent=2)+'\n')
    print('Generated mutation observation and wild-gene library goals; native one-time XP, no new currency or save component.')


if __name__=='__main__': main()
