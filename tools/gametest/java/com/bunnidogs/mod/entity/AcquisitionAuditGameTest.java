package com.bunnidogs.mod.entity;

import com.bunnidogs.mod.genetics.*;
import com.bunnidogs.mod.realm.*;
import com.bunnidogs.mod.registry.*;
import com.google.gson.*;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.core.registries.*;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.levelgen.structure.*;
import net.minecraft.world.level.levelgen.structure.placement.*;
import net.minecraft.world.phys.*;

/** Acquisition observations, not a simulated player's travel, combat skill or mining speed. */
final class AcquisitionAuditGameTest {
    static JsonObject stack(ItemStack stack) {var o=new JsonObject();o.addProperty("id",BuiltInRegistries.ITEM.getKey(stack.getItem()).toString());o.addProperty("count",stack.getCount());return o;}
    static void run(GameTestHelper h) throws Exception {
        var output=new JsonObject();var moons=new JsonArray();var extraction=new JsonArray();
        try(var fixture=new RealmFixture(h.getLevel(),"acquisition-audit")) {
            var realm=fixture.realm;var original=(RealmChunkGenerator)realm.getChunkSource().getGenerator();
            var set=realm.registryAccess().lookupOrThrow(Registries.STRUCTURE_SET).getOrThrow(ResourceKey.create(Registries.STRUCTURE_SET,Identifier.fromNamespaceAndPath("bunnidogs","great_ruins"))).value();
            var placement=(RandomSpreadStructurePlacement)set.placement();
            var structure=(GreatRuinStructure)realm.registryAccess().lookupOrThrow(Registries.STRUCTURE).getOrThrow(ResourceKey.create(Registries.STRUCTURE,Identifier.fromNamespaceAndPath("bunnidogs","great_ruin"))).value();
            for(long seed:new long[]{1379,49157,926481503}) {
                var g=new RealmChunkGenerator(original.getBiomeSource(),original.generatorSettings());g.createState(realm.registryAccess().lookupOrThrow(Registries.STRUCTURE_SET),realm.getChunkSource().randomState(),seed);
                var moon=g.terrain().tile(512,512).moons().stream().min(Comparator.comparingDouble(m->Math.hypot(m.x()-512,m.z()-512))).orElseThrow();
                int volume=0,astralite=0,cinderite=0;
                for(int x=moon.x()-moon.radius();x<=moon.x()+moon.radius();x++)for(int z=moon.z()-moon.radius();z<=moon.z()+moon.radius();z++) {
                    var column=g.terrain().column(x,z);
                    for(int y=column.moonBottom();y<=column.moonTop();y++) {
                        var s=g.state(x,y,z,column);if(s.isAir())continue;volume++;
                        if(s.is(ModBlocks.ASTRALITE_ORE.get()))astralite++;if(s.is(ModBlocks.CINDERITE_ORE.get()))cinderite++;
                    }
                }
                int candidateCount=0,admitted=0;
                for(int x=-2;x<2;x++)for(int z=-2;z<2;z++) {
                    var cp=placement.getPotentialStructureChunk(seed,x*placement.spacing(),z*placement.spacing());candidateCount++;
                    var context=new Structure.GenerationContext(realm.registryAccess(),g,g.getBiomeSource(),realm.getChunkSource().randomState(),realm.getServer().getStructureManager(),seed,cp,realm,b->true);
                    if(structure.findGenerationPoint(context).isPresent())admitted++;
                }
                var sample=new JsonObject();sample.addProperty("seed",seed);sample.addProperty("moon_radius",moon.radius());sample.addProperty("moon_blocks",volume);sample.addProperty("astralite_ore",astralite);sample.addProperty("cinderite_ore",cinderite);sample.addProperty("ruin_candidates",candidateCount);sample.addProperty("height_accepted_candidates",admitted);moons.add(sample);
                if(seed==1379) {
                    for(boolean interior:new boolean[]{false,true})for(int power:new int[]{10,20})extraction.add(explode(h,g,moon,interior,power));
                }
            }
        }
        output.add("moons",moons);output.add("extraction",extraction);
        var trades=new JsonArray();
        for(int job=0;job<4;job++) {
            var citizen=ModEntities.WARREN_CITIZEN.get().create(h.getLevel(),EntitySpawnReason.STRUCTURE);citizen.initialize(h.absolutePos(new BlockPos(56,180,56)),job);
            for(var offer:citizen.getOffers()) {var o=new JsonObject();o.addProperty("profession",job);o.add("cost_a",stack(offer.getCostA()));o.add("cost_b",stack(offer.getCostB()));o.add("output",stack(offer.getResult()));o.addProperty("initial_max_uses",offer.getMaxUses());trades.add(o);}citizen.discard();
        }
        output.add("lantern_initial_trades",trades);
        var materials=new JsonArray();
        for(int points:new int[]{0,20,50,100}) {
            var genes=Genome.DEFAULT;for(int slot=0;slot<2;slot++)genes=genes.with(Gene.FERTILITY,slot,Gene.FERTILITY.alleleNamed("starborn").id());
            var stats=Stats.of(points,points,points,points,points);var row=new JsonObject();row.addProperty("bred_points",points);var products=new JsonArray();
            for(var e:Drops.expected(genes,stats,new Drops.Context(Drops.Source.DEATH,false,true,false,3,false))) {var p=new JsonObject();p.addProperty("id",e.itemId());p.addProperty("mean_per_looting3_adult",e.mean());products.add(p);}
            row.add("death_drops",products);var groom=new JsonArray();for(var p:Grooming.yields(genes,stats))groom.add(p.name());row.add("grooming_products_per_12000_ticks",groom);materials.add(row);
        }output.add("domestic_materials",materials);
        h.assertTrue(moons.size()==3 && extraction.size()==4 && !trades.isEmpty(),"Acquisition observations include actual mineral recovery and physical finite merchant stock");
        com.bunnidogs.mod.BunnidogsMod.LOGGER.info("ACQUISITION_AUDIT {}",output);h.succeed();
    }
    static JsonObject explode(GameTestHelper h,RealmChunkGenerator g,RealmTerrain.Moon moon,boolean interior,int power) {
        var level=h.getLevel();var at=h.absolutePos(new BlockPos(56,180,56));int radius=22;
        int sourceY=interior ? moon.y() : g.terrain().column(moon.x(),moon.z()).moonTop()-1;
        var oldGrief=level.getGameRules().get(GameRules.MOB_GRIEFING);var oldDecay=level.getGameRules().get(GameRules.MOB_EXPLOSION_DROP_DECAY);
        var before=new HashMap<BlockPos,net.minecraft.world.level.block.state.BlockState>();int ore=0;
        var box=new AABB(at).inflate(45);level.getEntitiesOfClass(ItemEntity.class,box,e->true).forEach(Entity::discard);
        for(int x=-radius;x<=radius;x++)for(int z=-radius;z<=radius;z++) {
            var column=g.terrain().column(moon.x()+x,moon.z()+z);
            for(int y=-radius;y<=radius;y++) {
                var state=g.state(moon.x()+x,sourceY+y,moon.z()+z,column);var p=at.offset(x,y,z);level.setBlock(p,state,2);if(!state.isAir())before.put(p,state);
                if(state.is(ModBlocks.ASTRALITE_ORE.get()))ore++;
            }
        }
        level.setBlock(at,Blocks.AIR.defaultBlockState(),2);before.remove(at);
        var actor=ApexTypesGameTest.apex(h,ApexKind.BLAST,false,Vec3.atBottomCenterOf(at));
        try {
            level.getGameRules().set(GameRules.MOB_GRIEFING,true,level.getServer());level.getGameRules().set(GameRules.MOB_EXPLOSION_DROP_DECAY,true,level.getServer());
            h.assertTrue(ApexSkills.explosion(level,actor,Vec3.atCenterOf(at),power,false),"The actual Blastmaw calculator executes against native generated moon substrate");
            int broken=0,brokenOre=0,brokenCinder=0;
            for(var e:before.entrySet())if(level.getBlockState(e.getKey()).isAir()) {broken++;if(e.getValue().is(ModBlocks.ASTRALITE_ORE.get()))brokenOre++;if(e.getValue().is(ModBlocks.CINDERITE_ORE.get()))brokenCinder++;}
            var row=new JsonObject();row.addProperty("substrate_seed",1379);row.addProperty("placement",interior ? "interior" : "surface");row.addProperty("power",power);row.addProperty("blocks_broken",broken);row.addProperty("astralite_ore_broken",brokenOre);row.addProperty("cinderite_ore_broken",brokenCinder);
            for(var item:List.of(ModItems.ASTRALITE_SHARD.get(),ModItems.RAW_CINDERITE.get(),ModItems.MOONSTONE.get()))row.addProperty(BuiltInRegistries.ITEM.getKey(item).getPath()+"_recovered",level.getEntitiesOfClass(ItemEntity.class,box,e->e.getItem().is(item)).stream().mapToInt(e->e.getItem().getCount()).sum());
            return row;
        }finally {actor.discard();level.getEntitiesOfClass(ItemEntity.class,box,e->true).forEach(Entity::discard);level.getGameRules().set(GameRules.MOB_GRIEFING,oldGrief,level.getServer());level.getGameRules().set(GameRules.MOB_EXPLOSION_DROP_DECAY,oldDecay,level.getServer());}
    }
}
