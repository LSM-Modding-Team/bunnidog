package com.bunnidogs.mod.entity;

import com.bunnidogs.mod.realm.*;
import com.bunnidogs.mod.registry.*;
import com.bunnidogs.mod.genetics.*;
import com.bunnidogs.mod.item.ApexCrestItem;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.phys.Vec3;

/** Actual realm transfer, native entity persistence/loot and a paid player weapon. */
final class RealmGameTest {
    static void travel(GameTestHelper h) {
        try {
            var fixture=new RealmFixture(h.getLevel());
            BlockPos refuge;
            try {refuge=travelMounted(h);}
            catch(Throwable error) {fixture.close();throw error;}
            h.runAfterDelay(2,()->{
                try {
                    var realm=fixture.realm;
                    h.assertTrue(GreatWarren.warded(realm,refuge),"The actual generated return refuge enters the native ward POI index");
                    var enderman=net.minecraft.world.entity.EntityTypes.ENDERMAN.create(realm,EntitySpawnReason.NATURAL);enderman.setPos(Vec3.atBottomCenterOf(refuge.east(5)));
                    var natural=new net.neoforged.neoforge.event.entity.living.MobSpawnEvent.PositionCheck(enderman,realm,EntitySpawnReason.NATURAL,null);
                    net.neoforged.neoforge.common.NeoForge.EVENT_BUS.post(natural);
                    h.assertTrue(natural.getResult()==net.neoforged.neoforge.event.entity.living.MobSpawnEvent.PositionCheck.Result.FAIL,"The refuge blocks all new natural monsters, including ordinary Endermen");
                    var scripted=new net.neoforged.neoforge.event.entity.living.MobSpawnEvent.PositionCheck(enderman,realm,EntitySpawnReason.TRIGGERED,null);net.neoforged.neoforge.common.NeoForge.EVENT_BUS.post(scripted);
                    h.assertTrue(scripted.getResult()==net.neoforged.neoforge.event.entity.living.MobSpawnEvent.PositionCheck.Result.DEFAULT,"Wards cannot suppress scripted encounters or summoned brood");enderman.discard();
                    var apex=ModEntities.FLYING_APEX.getFirst().get().create(realm,EntitySpawnReason.NATURAL);apex.finalizeSpawn(realm,realm.getCurrentDifficultyAt(refuge),EntitySpawnReason.NATURAL,null);
                    h.assertTrue(apex.wild() && !apex.isPersistenceRequired(),"Actual native natural initialization selects the wild Apex profile");apex.discard();
                } finally {try {fixture.close();}catch(Exception error) {throw new RuntimeException(error);}}
                h.succeed();
            });
        } catch(Exception error) {throw new RuntimeException(error);}
    }
    private static BlockPos travelMounted(GameTestHelper h) {
        var source=h.getLevel();var pos=h.absolutePos(new BlockPos(3,2,3));
        for(int x=-4;x<=4;x++)for(int z=-4;z<=4;z++)h.setBlock(3+x,1,3+z,Blocks.STONE);
        source.setBlock(pos,ModBlocks.WARREN_ANCHOR.get().defaultBlockState(),3);
        var anchor=(WarrenAnchorBlockEntity)source.getBlockEntity(pos);
        var p=ApexTypesGameTest.player(h,Vec3.atBottomCenterOf(pos.east()));
        p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(ModItems.RESONANT_EYE.get(),2));
        anchor.attune(p,InteractionHand.MAIN_HAND);
        h.assertTrue(!anchor.linked() && p.getMainHandItem().getCount()==2,"A source Crest must be shown before any activation payment or realm changes");
        var crest=new ItemStack(ModItems.APEX_CREST.get());p.setItemInHand(InteractionHand.OFF_HAND,crest);
        var original=crest.copy();anchor.attune(p,InteractionHand.MAIN_HAND);
        var realm=source.getServer().getLevel(GreatWarren.LEVEL);
        h.assertTrue(realm!=null && p.level()==realm && anchor.linked() && p.getMainHandItem().getCount()==1 && ItemStack.matches(original,p.getOffhandItem()),"Real paid entry preserves the source Crest and crosses into the registered realm: realm="+realm+", player="+p.level().dimension()+", linked="+anchor.linked()+", eye="+p.getMainHandItem()+", endpoint="+anchor.endpoint());
        naturalRuin(h,realm);
        com.bunnidogs.mod.worldgen.CrownWorldgenGameTest.natural(h,realm);
        var back=(WarrenAnchorBlockEntity)realm.getBlockEntity(anchor.endpoint());
        h.assertTrue(back!=null && back.endpoint().equals(pos) && back.destination().equals(source.dimension()) && back.getBlockState().getValue(WarrenAnchorBlock.RETURN),"The created return anchor must preserve the actual source dimension and position");
        h.assertTrue(!realm.getBlockState(back.getBlockPos().below()).canEntityDestroy(realm,back.getBlockPos().below(),p),"Refuge foundations withstand destructive Apex jumps as well as explosions");
        boolean ladder=false;
        for(var direction:net.minecraft.core.Direction.Plane.HORIZONTAL)if(realm.getBlockState(back.getBlockPos().below().relative(direction,4)).is(Blocks.LADDER))ladder=true;
        h.assertTrue(ladder,"A new refuge has a physical climb down to terrain and back up");
        var otherPos=pos.east(10);
        for(int dx=-2;dx<=2;dx++)for(int dz=-2;dz<=2;dz++)source.setBlock(otherPos.offset(dx,-1,dz),Blocks.STONE.defaultBlockState(),3);
        source.setBlock(otherPos,ModBlocks.WARREN_ANCHOR.get().defaultBlockState(),3);
        var other=(WarrenAnchorBlockEntity)source.getBlockEntity(otherPos);var guest=ApexTypesGameTest.player(h,Vec3.atBottomCenterOf(otherPos.east()));
        guest.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(ModItems.RESONANT_EYE.get()));guest.setItemInHand(InteractionHand.OFF_HAND,ApexCrestItem.create(new Tissue(Genome.DEFAULT,Stats.ZERO)));other.attune(guest,InteractionHand.MAIN_HAND);
        h.assertTrue(guest.level()==realm && other.linked() && !other.endpoint().equals(anchor.endpoint()),"Separate entry anchors create distinct paired returns for multiple players");
        var guestReturn=(WarrenAnchorBlockEntity)realm.getBlockEntity(other.endpoint());guest.getInventory().clearContent();guest.getPersistentData().remove("WarrenCrossUntil");guestReturn.cross(guest);
        h.assertTrue(guest.level()==source && guestReturn.endpoint().equals(otherPos) && guest.position().distanceToSqr(Vec3.atBottomCenterOf(otherPos))<9,"A second player returns to their own source rather than another player's anchor");guest.discard();
        var data=back.saveWithFullMetadata(realm.registryAccess());
        var copy=(WarrenAnchorBlockEntity)net.minecraft.world.level.block.entity.BlockEntity.loadStatic(back.getBlockPos(),back.getBlockState(),data,realm.registryAccess());
        h.assertTrue(copy!=null && copy.endpoint().equals(pos) && copy.destination().equals(source.dimension()),"Paired endpoints survive native block-entity saves");
        h.assertTrue(back.getBlockState().getDestroyProgress(p,realm,back.getBlockPos())==0 && !back.getBlockState().canEntityDestroy(realm,back.getBlockPos(),p),"The generated return machinery resists mining and enemy destruction");
        h.assertTrue(WarrenAnchorBlockEntity.safeLanding(realm,back.getBlockPos(),p)!=null,"The generated refuge has a safe neighboring arrival");
        var terrain=new com.google.gson.JsonArray();var at=back.getBlockPos();
        for(int dx=-20;dx<=20;dx++)for(int dz=-20;dz<=20;dz++) {
            int x=at.getX()+dx,z=at.getZ()+dz;if(!realm.hasChunkAt(new BlockPos(x,80,z)))continue;
            int y=realm.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.WORLD_SURFACE,x,z)-1;
            var cell=new com.google.gson.JsonArray();cell.add(dx);cell.add(y);cell.add(dz);cell.add(net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(realm.getBlockState(new BlockPos(x,y,z)).getBlock()).toString());terrain.add(cell);
        }
        try {java.nio.file.Files.writeString(java.nio.file.Path.of("realm_arrival_preview.json"),terrain.toString());}
        catch(java.io.IOException error) {throw new RuntimeException(error);}
        // No inventory item is required to return, and source-anchor loss does not erase the saved destination.
        source.removeBlock(pos,false);p.getInventory().clearContent();p.getPersistentData().remove("WarrenCrossUntil");
        back.cross(p);h.assertTrue(p.level()==source && p.position().distanceToSqr(Vec3.atBottomCenterOf(pos))<100,"An empty-handed player returns to the saved source even after its anchor is removed");
        p.discard();return back.getBlockPos();
    }
    private static void naturalRuin(GameTestHelper h,net.minecraft.server.level.ServerLevel realm) {
        var registries=realm.registryAccess();var generator=realm.getChunkSource().getGenerator();var random=realm.getChunkSource().randomState();
        var placement=(net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement)registries.lookupOrThrow(net.minecraft.core.registries.Registries.STRUCTURE_SET)
                .getOrThrow(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.STRUCTURE_SET,net.minecraft.resources.Identifier.fromNamespaceAndPath("bunnidogs","great_ruins"))).value().placement();
        var structure=(com.bunnidogs.mod.realm.GreatRuinStructure)registries.lookupOrThrow(net.minecraft.core.registries.Registries.STRUCTURE)
                .getOrThrow(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.STRUCTURE,net.minecraft.resources.Identifier.fromNamespaceAndPath("bunnidogs","great_ruin"))).value();
        var state=generator.createState(registries.lookupOrThrow(net.minecraft.core.registries.Registries.STRUCTURE_SET),random,realm.getSeed());
        for(int x=-2;x<=2;x++)for(int z=-2;z<=2;z++) {
            var chunk=placement.getPotentialStructureChunk(realm.getSeed(),x*20,z*20);
            if(!placement.isStructureChunk(state,chunk.x(),chunk.z()))continue;
            var context=new net.minecraft.world.level.levelgen.structure.Structure.GenerationContext(registries,generator,generator.getBiomeSource(),random,realm.getServer().getStructureManager(),realm.getSeed(),chunk,realm,biome->true);
            var point=structure.findGenerationPoint(context);
            if(point.isPresent()) {
                h.assertTrue(point.get().getPiecesBuilder().build().pieces().size()==1,"A native realm random-spread candidate produces a fitted ruin piece");
                com.bunnidogs.mod.BunnidogsMod.LOGGER.info("Great Warren natural ruin fixture: seed {}, candidate {}",realm.getSeed(),chunk);return;
            }
        }
        h.assertTrue(false,"The shipped dimension's native terrain must admit at least one ruin in a bounded set of actual placement candidates");
    }
    static void ecology(GameTestHelper h) {
        var level=h.getLevel();var pos=ApexTypesGameTest.center(h);var p=ApexTypesGameTest.player(h,pos.add(7,0,0));
        for(var kind:ApexKind.values())for(boolean flying:new boolean[]{false,true}) {
            var a=ApexTypesGameTest.apex(h,kind,flying,pos);a.makeWild();
            h.assertTrue(a.wild() && !a.isPersistenceRequired() && a.removeWhenFarAway(20000),"All ten wild forms use normal despawning instead of ritual persistence");
            if(flying) {
                var flight=new ApexFlightGoal(a);for(int tick=0;tick<12;tick++)flight.tick();
                h.assertTrue(a.getDeltaMovement().y>0,"Wild flyers take off and cruise even before acquiring a combat target");flight.stop();
            }
            var out=TagValueOutput.createWithContext(ProblemReporter.DISCARDING,level.registryAccess());a.saveWithoutId(out);
            var restored=ModEntities.apex(kind,flying).create(level,EntitySpawnReason.LOAD);restored.load(TagValueInput.create(ProblemReporter.DISCARDING,level.registryAccess(),out.buildResult()));
            h.assertTrue(restored.wild() && !restored.isPersistenceRequired() && restored.genome().equals(a.genome()) && restored.stats().equals(a.stats()),"A natural Apex keeps its frozen genome/profile through native saves");restored.discard();
            var drops=new java.util.ArrayList<net.minecraft.world.entity.item.ItemEntity>();
            var event=new net.neoforged.neoforge.event.entity.living.LivingDropsEvent(a,level.damageSources().playerAttack(p),drops,true);
            RealmLoot.drops(event,a,level);
            h.assertTrue(drops.size()>=2 && drops.stream().noneMatch(e->e.getItem().is(ModItems.TITAN_HAUNCH.get())),"Wild rewards provide role materials and supplies without domestic legends");
            a.setCustomName(net.minecraft.network.chat.Component.literal("Saved wild Apex"));h.assertTrue(a.isPersistenceRequired(),"A named wild Apex may still be kept deliberately");a.discard();
        }
        var ritual=ApexTypesGameTest.apex(h,ApexKind.BRUTE,false,pos);h.assertTrue(!ritual.wild() && ritual.isPersistenceRequired() && !ritual.removeWhenFarAway(20000),"Existing ritual Apexes keep their original persistence");ritual.discard();p.discard();
    }
    static void ward(GameTestHelper h) {
        var level=h.getLevel();var pos=h.absolutePos(new BlockPos(3,2,3));
        level.setBlock(pos,ModBlocks.WARREN_WARD.get().defaultBlockState(),3);
        // POI insertion follows the real native block-state notification.
        h.runAfterDelay(2,()->{
            h.assertTrue(GreatWarren.warded(level,pos.offset(10,0,0)) && !GreatWarren.warded(level,pos.offset(25,0,0)),"A real loaded ward protects only its twenty-four-block neighborhood");
            var a=ApexTypesGameTest.apex(h,ApexKind.BLAST,false,Vec3.atBottomCenterOf(pos.east(3)));a.makeWild();
            h.assertTrue(a.isAlive(),"Wards do not erase existing enemies or cancel their fights");a.discard();
            level.removeBlock(pos,false);
            h.runAfterDelay(2,()->{h.assertTrue(!GreatWarren.warded(level,pos),"Removing an outpost ward stops suppressing new natural spawns");h.succeed();});
        });
    }
    static void harness(GameTestHelper h) {
        var level=h.getLevel();var p=ApexTypesGameTest.player(h,ApexTypesGameTest.center(h).add(0,8,0));
        var harness=new ItemStack(ModItems.APEX_WING_HARNESS.get());p.setItemSlot(net.minecraft.world.entity.EquipmentSlot.CHEST,harness);
        p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(ModItems.PRISM_CELL.get()));
        h.assertTrue(harness.has(net.minecraft.core.component.DataComponents.GLIDER) && harness.get(net.minecraft.core.component.DataComponents.EQUIPPABLE).slot()==net.minecraft.world.entity.EquipmentSlot.CHEST,"The harness uses native gliding and the chest equipment slot without armor stats");
        var cell=ModItems.PRISM_CELL.get();
        h.assertTrue(cell.use(level,p,InteractionHand.MAIN_HAND)==net.minecraft.world.InteractionResult.PASS && p.getMainHandItem().getCount()==1,"A cell cannot produce free ground flight");
        p.setOnGround(false);p.startFallFlying();p.setXRot(0);p.setYRot(0);
        h.assertTrue(cell.use(level,p,InteractionHand.MAIN_HAND)==net.minecraft.world.InteractionResult.SUCCESS && p.getMainHandItem().isEmpty() && harness.getDamageValue()==4 && p.getDeltaMovement().y>.5,"A real glide burst pays its last cell and durability while gaining forward and climbing motion");
        p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(ModItems.PRISM_CELL.get(),2));
        h.assertTrue(cell.use(level,p,InteractionHand.MAIN_HAND)==net.minecraft.world.InteractionResult.FAIL && p.getMainHandItem().getCount()==2,"Consuming the last cell must still set the shared cooldown on replacement fuel");
        p.getCooldowns().removeCooldown(ModItems.PRISM_CELL.getId());harness.setDamageValue(harness.getMaxDamage()-4);
        h.assertTrue(cell.use(level,p,InteractionHand.MAIN_HAND)==net.minecraft.world.InteractionResult.FAIL && p.getMainHandItem().getCount()==2,"A nearly broken harness cannot spend more fuel or grant another boost");p.discard();
    }
    static void lance(GameTestHelper h) {
        var level=h.getLevel();ApexTypesGameTest.floor(h);var pos=ApexTypesGameTest.center(h);var p=ApexTypesGameTest.player(h,pos);p.setYRot(-90);p.setXRot(0);
        var a=ApexTypesGameTest.apex(h,ApexKind.BRUTE,false,pos.add(8,0,0));var b=ApexTypesGameTest.apex(h,ApexKind.PRISM,false,pos.add(12,0,0));
        var item=ModItems.PRISM_LANCE.get();var lance=new ItemStack(item);p.setItemInHand(InteractionHand.MAIN_HAND,lance);
        h.assertTrue(item.use(level,p,InteractionHand.MAIN_HAND)==net.minecraft.world.InteractionResult.FAIL,"A survival lance cannot start without real cells");
        p.setItemInHand(InteractionHand.OFF_HAND,new ItemStack(ModItems.PRISM_CELL.get(),2));
        item.use(level,p,InteractionHand.MAIN_HAND);p.stopUsingItem();h.assertTrue(p.getOffhandItem().getCount()==2,"Interrupted charges do not consume ammunition");
        item.use(level,p,InteractionHand.MAIN_HAND);float ah=a.getHealth(),bh=b.getHealth();
        item.finishUsingItem(lance,level,p);
        h.assertTrue(a.getHealth()<ah && b.getHealth()<bh && p.getOffhandItem().getCount()==1 && lance.getDamageValue()==1,"The actual paid beam pierces a line of enemies and pays one cell and one durability");
        item.finishUsingItem(lance,level,p);h.assertTrue(p.getOffhandItem().getCount()==1,"Native item cooldown prevents duplicate finish callbacks from firing twice");
        a.discard();b.discard();p.discard();
    }
}
