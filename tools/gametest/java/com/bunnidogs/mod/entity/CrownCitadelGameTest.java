package com.bunnidogs.mod.entity;

import com.bunnidogs.mod.crown.*;
import com.bunnidogs.mod.registry.*;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.phys.*;

/** Exercises the final native architecture, not a surrogate empty arena. */
final class CrownCitadelGameTest {
    static void run(GameTestHelper h) {
        var level=h.getLevel();var c=h.absolutePos(new BlockPos(128,179,128));
        var piece=CrownWarrenPiece.citadel(c.getX(),c.getY(),c.getZ(),12984912);
        var b=piece.getBoundingBox();
        for(int x=b.minX()>>4;x<=b.maxX()>>4;x++)for(int z=b.minZ()>>4;z<=b.maxZ()>>4;z++) {
            var cp=new ChunkPos(x,z);piece.postProcess(level,level.structureManager(),level.getChunkSource().getGenerator(),RandomSource.create(1),new BoundingBox(cp.getMinBlockX(),level.getMinY(),cp.getMinBlockZ(),cp.getMaxBlockX(),level.getMaxY()-1,cp.getMaxBlockZ()),cp,c);
        }
        h.assertTrue(new CrownWarrenPiece(piece.createTag(StructurePieceSerializationContext.fromLevel(level))).layoutVersion()==2,"New sites persist their chosen layout revision");
        for(int wing=0;wing<4;wing++) {
            var at=CrownSitePlan.seal(c,wing);var seal=(CrownSealBlockEntity)level.getBlockEntity(at);
            h.assertTrue(seal!=null && seal.citadel() && seal.site().equals(piece.site()),"Every actual shipped seal configures its own native geometry");
            var restored=(CrownSealBlockEntity)BlockEntity.loadStatic(at,seal.getBlockState(),seal.saveWithFullMetadata(level.registryAccess()),level.registryAccess());
            h.assertTrue(restored!=null && restored.citadel() && restored.encounterCenter().equals(seal.encounterCenter()) && restored.leaseBounds().equals(seal.leaseBounds()),"Native saves freeze elevated centers and room bounds");
        }
        var supplies=(BarrelBlockEntity)level.getBlockEntity(c.offset(-73,1,82));supplies.unpackLootTable(null);
        boolean stockPresent=false;for(int i=0;i<supplies.getContainerSize();i++)if(!supplies.getItem(i).isEmpty())stockPresent=true;
        h.assertTrue(stockPresent,"The actual authored supply barrel resolves its finite native expedition loot");supplies.clearContent();
        // Explicit old-palette conversion only visits matching native structural positions.
        // Pick a real planned masonry cell, restore its former material, then upgrade it.
        BlockPos structural=null;
        outer:for(int x=-20;x<=20;x++)for(int z=-20;z<=20;z++)if(level.getBlockState(c.offset(x,0,z)).is(com.bunnidogs.mod.crown.LunarMasonry.BLOCKS.get("lunar_rootstone").get())){structural=c.offset(x,0,z);break outer;}
        h.assertTrue(structural!=null,"The actual castle floor uses fortified masonry");level.setBlock(structural,ModBlocks.ROOTSTONE.get().defaultBlockState(),2);
        h.assertTrue(CrownCitadelPlacement.fortify(level,piece)==1 && level.getBlockState(structural).is(com.bunnidogs.mod.crown.LunarMasonry.BLOCKS.get("lunar_rootstone").get()) && supplies.isEmpty(),"Explicit fortification upgrades the legacy matching cell without refilling an opened barrel");
        // Controller and pad locations are checked by real collision/spawn attempts in all three finished wings.
        var p=ApexTypesGameTest.player(h,Vec3.atBottomCenterOf(CrownSitePlan.seal(c,0).east()));
        p.setInvulnerable(true);guards(h,c,piece,p,0);
    }
    private static void guards(GameTestHelper h,BlockPos c,CrownWarrenPiece piece,ServerPlayer p,int wing) {
        if(wing==4) {exercise(h,c,piece,p);return;}
        p.snapTo(Vec3.atBottomCenterOf(CrownSitePlan.geometry(c,wing).patrol().south(3)));
        h.runAfterDelay(12,()->{
            var seal=(CrownSealBlockEntity)h.getLevel().getBlockEntity(CrownSitePlan.seal(c,wing));
            h.assertTrue(seal.patrol() && seal.enemies().size()==2 && seal.patrolSpent(),"The actual furnished approach admits its saved guard pair: "+wing);
            for(var id:seal.enemies())CrownDungeonGameTest.kill(h,(LivingEntity)h.getLevel().getEntity(id),p);
            seal=CrownDungeonGameTest.copy(h,seal);
            h.assertTrue(!seal.active() && !seal.cleared() && seal.wave()==0 && seal.patrolSpent(),"Patrol death/reload pays no wing key/cache and never rearms: "+wing);
            guards(h,c,piece,p,wing+1);
        });
    }
    private static void exercise(GameTestHelper h,BlockPos c,CrownWarrenPiece piece,ServerPlayer p) {
        var level=h.getLevel();
        for(int wing=0;wing<3;wing++) {
            var seal=(CrownSealBlockEntity)level.getBlockEntity(CrownSitePlan.seal(c,wing));p.snapTo(Vec3.atBottomCenterOf(seal.getBlockPos().south()));
            h.assertTrue(seal.begin(p,InteractionHand.MAIN_HAND),"The authored wing admits its real Apex actors: "+wing);
            for(int wave=0;wave<2;wave++)for(var id:seal.enemies())CrownDungeonGameTest.kill(h,(LivingEntity)level.getEntity(id),p);
            h.assertTrue(seal.cleared() && !seal.active(),"Actual confirmed waves pay a site-bound key in the revised room: "+wing);
        }
        var main=(CrownSealBlockEntity)level.getBlockEntity(CrownSitePlan.seal(c,3));var socket=c.offset(-6,4,46);
        p.snapTo(Vec3.atBottomCenterOf(socket.south()));p.setItemInHand(InteractionHand.MAIN_HAND,CrownDungeonGameTest.key(0,piece.site()));
        // Native block use, beyond six blocks from the controller: authority is the touched protected seat.
        touch(h,p,socket);
        h.assertTrue(main.sockets()==1 && p.getMainHandItem().isEmpty() && level.getBlockState(c.offset(0,2,46)).is(ModBlocks.CROWN_GATE.get()),"The first remote physical socket consumes exactly one matching key");
        p.setItemInHand(InteractionHand.MAIN_HAND,CrownDungeonGameTest.key(2,piece.site()));touch(h,p,socket);
        h.assertTrue(Integer.bitCount(main.sockets())==2 && level.getBlockState(c.offset(0,2,46)).isAir() && level.getBlockState(c.offset(31,2,67)).is(ModBlocks.CROWN_GATE.get()),"Two distinct keys open only the throne gate; the independent archive stays closed");
        p.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);touch(h,p,socket);
        h.assertTrue(!main.active(),"An already filled key seat never starts a boss when inspected empty-handed");
        var archiveSeat=c.offset(31,3,68);p.snapTo(Vec3.atBottomCenterOf(archiveSeat.south()));p.setItemInHand(InteractionHand.MAIN_HAND,CrownDungeonGameTest.key(1,piece.site()));touch(h,p,archiveSeat);
        h.assertTrue(main.sockets()==7 && level.getBlockState(c.offset(31,2,67)).isAir() && !main.active(),"The optional third key opens its separate archive without accidentally starting the boss");
        h.assertTrue(level.getBlockState(c.offset(6,4,46)).getValue(CrownSocketBlock.KEY)==3 && level.getBlockState(archiveSeat).getValue(CrownSocketBlock.KEY)==2,"Physical seats preserve actual insertion order, including after an out-of-order optional key");
        var archive=(ChestBlockEntity)level.getBlockEntity(CrownSitePlan.geometry(c,3).archives().getFirst());h.assertTrue(archive!=null && !archive.isEmpty(),"The frozen archive is physically inside the unlocked archive room");
        var original=archive.getItem(10).copy();main=CrownDungeonGameTest.copy(h,main);p.setItemInHand(InteractionHand.MAIN_HAND,CrownDungeonGameTest.key(1,piece.site()));touch(h,p,archiveSeat);
        h.assertTrue(ItemStack.matches(original,archive.getItem(10)) && p.getMainHandItem().getCount()==1,"Saved/repeated keys do not refill or reroll the independent archive");
        p.snapTo(Vec3.atBottomCenterOf(main.getBlockPos().east()));p.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);
        h.assertTrue(main.begin(p,InteractionHand.MAIN_HAND),"The actual vaulted Crown hall admits the unchanged finale");
        var boss=(CrownedApex)level.getEntity(main.enemies().iterator().next());boss.setNoAi(true);CrownDungeonGameTest.kill(h,boss,p);
        h.assertTrue(main.pending(p.getUUID())==1 && level.getBlockState(c.offset(-31,2,67)).isAir(),"Final victory retains personal rewards and opens the separate treasury");
        // Persisted generation replay and explicit repair must preserve used stock and progress.
        var entry=CrownSitePlan.geometry(c,3).rewards().getFirst();var chest=(ChestBlockEntity)level.getBlockEntity(entry);chest.unpackLootTable(null);chest.clearContent();
        var replay=new CrownWarrenPiece(piece.createTag(StructurePieceSerializationContext.fromLevel(level)));var cp=ChunkPos.containing(entry);
        replay.postProcess(level,level.structureManager(),level.getChunkSource().getGenerator(),RandomSource.create(8),new BoundingBox(cp.getMinBlockX(),level.getMinY(),cp.getMinBlockZ(),cp.getMaxBlockX(),level.getMaxY()-1,cp.getMaxBlockZ()),cp,c);
        CrownRepair.repair(level,replay);
        h.assertTrue(chest.isEmpty() && ((BarrelBlockEntity)level.getBlockEntity(c.offset(-73,1,82))).isEmpty() && main.sockets()==7 && main.cleared(),"Replay/loaded repair never repastes old stock, closes paid doors or resets the ledger");p.discard();h.succeed();
    }
    private static void touch(GameTestHelper h,ServerPlayer p,BlockPos at) {p.gameMode.useItemOn(p,h.getLevel(),p.getMainHandItem(),InteractionHand.MAIN_HAND,new BlockHitResult(Vec3.atCenterOf(at),Direction.SOUTH,at,false));}
}
