package com.bunnidogs.mod.entity;

import com.bunnidogs.mod.block.GuavaFruitBlock;
import com.bunnidogs.mod.orchard.Orchard;
import com.bunnidogs.mod.registry.ModBlocks;
import com.bunnidogs.mod.registry.ModItems;
import com.bunnidogs.mod.worldgen.GuavaGroveFeature;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.*;
import net.minecraft.util.StaticCache2D;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.chunk.*;
import net.minecraft.world.level.chunk.status.*;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.RightClickBlock;

/** Real native gathering, protected remote targets, root investment and unfinished world generation. */
final class OrchardProgressionGameTest {
    private static BlockPos tree(GameTestHelper h) {
        for(int x=0;x<12;x++)for(int z=0;z<12;z++) {
            h.setBlock(x,0,z,Blocks.DIRT);
            for(int y=1;y<13;y++)h.setBlock(x,y,z,Blocks.AIR);
        }
        var root=h.absolutePos(new BlockPos(5,1,5));
        for(int y=0;y<8;y++)h.getLevel().setBlock(root.above(y),ModBlocks.GUAVA_LOG.get().defaultBlockState(),3);
        return root;
    }
    private static BlockPos branch(GameTestHelper h,BlockPos root,int dx,int dz,int age,int bunch) {
        var fruit=root.offset(dx,6,dz);
        h.getLevel().setBlock(fruit.above(),ModBlocks.GUAVA_LEAVES.get().defaultBlockState().setValue(LeavesBlock.PERSISTENT,true),3);
        h.getLevel().setBlock(fruit,ModBlocks.GUAVA_FRUIT.get().defaultBlockState().setValue(GuavaFruitBlock.AGE,age).setValue(GuavaFruitBlock.BUNCH,bunch),3);
        return fruit;
    }
    private static int loose(GameTestHelper h,BlockPos root) {
        return h.getLevel().getEntitiesOfClass(ItemEntity.class,new AABB(root).inflate(12),e->e.getItem().is(ModItems.GUAVA.get())).stream().mapToInt(e->e.getItem().getCount()).sum();
    }
    private static void click(ServerPlayer p,BlockPos pos) {
        p.gameMode.useItemOn(p,p.level(),p.getMainHandItem(),InteractionHand.MAIN_HAND,new BlockHitResult(Vec3.atCenterOf(pos),Direction.NORTH,pos,false));
    }
    static void shake(GameTestHelper h) {
        var level=h.getLevel();var root=tree(h);
        var ripe=branch(h,root,1,0,2,2);var protectedFruit=branch(h,root,-1,0,2,3);var green=branch(h,root,0,1,1,2);
        var p=h.makeMockServerPlayerInLevel();p.gameMode.changeGameModeForPlayer(GameType.SURVIVAL);p.setPos(Vec3.atBottomCenterOf(root.north(2)));
        Consumer<RightClickBlock> veto=e->{if(e.getEntity()==p && e.getPos().equals(protectedFruit))e.setCanceled(true);};
        NeoForge.EVENT_BUS.addListener(veto);try {click(p,root);}finally {NeoForge.EVENT_BUS.unregister(veto);}
        h.assertTrue(loose(h,root)==2 && level.getBlockState(ripe).getValue(GuavaFruitBlock.AGE)==0
                && GuavaFruitBlock.isRipe(level.getBlockState(protectedFruit)) && level.getBlockState(green).getValue(GuavaFruitBlock.AGE)==1,
                "Native trunk use drops only its allowed ripe bunches and leaves green/protected fruit intact");
        click(p,root);h.assertTrue(loose(h,root)==2,"The shake rest prevents a second payout or callback re-entry");
        p.getPersistentData().remove("OrchardShakeUntil");
        Consumer<RightClickBlock> replace=e->{if(e.getEntity()==p && e.getPos().equals(protectedFruit))level.setBlock(protectedFruit,Blocks.CHEST.defaultBlockState(),3);};
        NeoForge.EVENT_BUS.addListener(replace);try {click(p,root);}finally {NeoForge.EVENT_BUS.unregister(replace);}
        h.assertTrue(level.getBlockEntity(protectedFruit)!=null && loose(h,root)==2,"A protection callback's replacement container survives without a stale harvest");
        var block=ModBlocks.GUAVA_FRUIT.get();
        block.performBonemeal(level,level.getRandom(),ripe,level.getBlockState(ripe));
        h.assertTrue(level.getBlockState(ripe).getValue(GuavaFruitBlock.BUNCH)==2,"A rooted unfertilized tree grows a pair of guavas");
        level.setBlock(root.below(),ModBlocks.ORCHARD_MULCH.get().defaultBlockState(),3);
        level.setBlock(ripe,level.getBlockState(ripe).setValue(GuavaFruitBlock.AGE,0),3);
        block.performBonemeal(level,level.getRandom(),ripe,level.getBlockState(ripe));
        h.assertTrue(level.getBlockState(ripe).getValue(GuavaFruitBlock.BUNCH)==3,"Mulch below the actual trunk grows visible three-fruit bunches");
        level.setBlock(root,Blocks.AIR.defaultBlockState(),3);
        level.setBlock(ripe,level.getBlockState(ripe).setValue(GuavaFruitBlock.AGE,0),3);
        block.performBonemeal(level,level.getRandom(),ripe,level.getBlockState(ripe));
        h.assertTrue(level.getBlockState(ripe).getValue(GuavaFruitBlock.BUNCH)==1,"Disconnected old fruit remains supported but loses the rooted orchard bonus");
        var remote=new BlockPos(25000000,64,25000000);h.assertTrue(Orchard.root(level,remote)==null && !level.hasChunkAt(remote),"Root discovery cannot request missing terrain");
        h.succeed();
    }
    private static void aim(ServerPlayer p,BlockPos at){p.lookAt(EntityAnchorArgument.Anchor.EYES,Vec3.atCenterOf(at));}
    private static int inventory(ServerPlayer p){return p.getInventory().countItem(ModItems.GUAVA.get());}
    static void picker(GameTestHelper h) {
        var level=h.getLevel();var root=tree(h);var target=branch(h,root,0,-1,2,3);
        var ripe=level.getBlockState(target);var p=h.makeMockServerPlayerInLevel();p.gameMode.changeGameModeForPlayer(GameType.SURVIVAL);p.setPos(Vec3.atBottomCenterOf(root.north(4)));
        var tool=new ItemStack(ModItems.FRUIT_PICKER.get());tool.set(DataComponents.CUSTOM_NAME,Component.literal("High branches"));p.setItemInHand(InteractionHand.MAIN_HAND,tool);aim(p,target);
        Consumer<RightClickBlock> veto=e->{if(e.getEntity()==p && e.getPos().equals(target))e.setCanceled(true);};
        NeoForge.EVENT_BUS.addListener(veto);try {p.gameMode.useItem(p,level,tool,InteractionHand.MAIN_HAND);}finally {NeoForge.EVENT_BUS.unregister(veto);}
        h.assertTrue(level.getBlockState(target).equals(ripe) && inventory(p)==0 && tool.getDamageValue()==0,"A ranged picker respects target protection before payment or wear");
        var wall=target.north().below(2);level.setBlock(wall,Blocks.STONE.defaultBlockState(),3);
        p.gameMode.useItem(p,level,tool,InteractionHand.MAIN_HAND);
        h.assertTrue(level.getBlockState(target).equals(ripe) && tool.getDamageValue()==0,"The picker cannot reach through an intervening block");
        level.removeBlock(wall,false);p.gameMode.useItem(p,level,tool,InteractionHand.MAIN_HAND);
        h.assertTrue(level.getBlockState(target).getValue(GuavaFruitBlock.AGE)==0 && inventory(p)==3 && loose(h,root)==0
                && tool.getDamageValue()==1 && tool.getHoverName().getString().equals("High branches"),"An actual air-use ray catches the entire bunch beyond hand reach for one wear");
        level.setBlock(target,ripe,3);p.gameMode.useItem(p,level,tool,InteractionHand.MAIN_HAND);
        h.assertTrue(inventory(p)==3 && tool.getDamageValue()==1,"A second same-tick use cannot harvest around the tool cooldown");
        // Mock players do not receive normal player ticks; advance the native cooldown itself.
        for(int tick=0;tick<6;tick++)p.getCooldowns().tick();
        {
            Consumer<RightClickBlock> swap=e->{if(e.getEntity()==p && e.getPos().equals(target))p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(net.minecraft.world.item.Items.STICK));};
            NeoForge.EVENT_BUS.addListener(swap);try {p.gameMode.useItem(p,level,tool,InteractionHand.MAIN_HAND);}finally {NeoForge.EVENT_BUS.unregister(swap);}
            h.assertTrue(level.getBlockState(target).equals(ripe) && inventory(p)==3 && tool.getDamageValue()==1,"A callback swapping the tool cannot make a stale remote harvest");
            p.setItemInHand(InteractionHand.MAIN_HAND,tool);var drops=level.getGameRules().get(GameRules.BLOCK_DROPS);
            try {level.getGameRules().set(GameRules.BLOCK_DROPS,false,level.getServer());p.gameMode.useItem(p,level,tool,InteractionHand.MAIN_HAND);
                h.assertTrue(inventory(p)==3 && level.getBlockState(target).getValue(GuavaFruitBlock.AGE)==0,"Direct inventory collection still honors the native block-drops rule");
            }finally {level.getGameRules().set(GameRules.BLOCK_DROPS,drops,level.getServer());}
            h.succeed();
        }
    }
    static void grove(GameTestHelper h) {
        var level=h.getLevel();var mid=new ChunkPos(120000,120000);var chunks=new HashMap<ChunkPos,ProtoChunk>();
        for(int x=-2;x<=2;x++)for(int z=-2;z<=2;z++) {
            var cp=new ChunkPos(mid.x()+x,mid.z()+z);var c=new ProtoChunk(cp,UpgradeData.EMPTY,level,PalettedContainerFactory.create(level.registryAccess()),null);
            c.setPersistedStatus(ChunkStatus.CARVERS);chunks.put(cp,c);
            for(int dx=0;dx<16;dx++)for(int dz=0;dz<16;dz++)c.setBlockState(new BlockPos(cp.getMinBlockX()+dx,64,cp.getMinBlockZ()+dz),Blocks.GRASS_BLOCK.defaultBlockState(),0);
        }
        var cache=StaticCache2D.<GenerationChunkHolder>create(mid.x(),mid.z(),2,(x,z)->new GenerationChunkHolder(new ChunkPos(x,z)) {
            @Override public ChunkAccess getChunkIfPresentUnchecked(ChunkStatus status){return chunks.get(new ChunkPos(x,z));}
            @Override protected void addSaveDependency(CompletableFuture<?> p){}
            @Override public int getTicketLevel(){return 0;}
            @Override public int getQueueLevel(){return 0;}
        });
        var region=new WorldGenRegion(level,cache,ChunkPyramid.GENERATION_PYRAMID.getStepTo(ChunkStatus.FEATURES),chunks.get(mid));
        var base=new BlockPos(mid.getMinBlockX()+8,65,mid.getMinBlockZ()+8);
        h.assertTrue(GuavaGroveFeature.grow(region,base,1832),"A grove tree generates safely in an actual unfinished WorldGenRegion");
        int ripe=0,young=0,tall=0;var preview=new com.google.gson.JsonArray();
        for(var c:chunks.values())for(int x=0;x<16;x++)for(int z=0;z<16;z++)for(int y=65;y<82;y++) {
            var p=new BlockPos(c.getPos().getMinBlockX()+x,y,c.getPos().getMinBlockZ()+z);var s=c.getBlockState(p);
            if(!s.isAir()) {var row=new com.google.gson.JsonArray();row.add(p.getX()-base.getX());row.add(p.getY()-base.getY());row.add(p.getZ()-base.getZ());row.add(net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(s.getBlock()).toString());if(s.is(ModBlocks.GUAVA_FRUIT.get())){row.add(s.getValue(GuavaFruitBlock.AGE));row.add(s.getValue(GuavaFruitBlock.BUNCH));}preview.add(row);}
            if(s.is(ModBlocks.GUAVA_LOG.get()))tall++;
            if(s.is(ModBlocks.GUAVA_FRUIT.get())) {
                h.assertTrue(s.getValue(GuavaFruitBlock.BUNCH)==2 && s.canSurvive(region,p) && base.equals(Orchard.root(region,p)),"Generated bunches are attached to their own living root");
                if(GuavaFruitBlock.isRipe(s))ripe++;else young++;
            }
        }
        h.assertTrue(tall>=8 && ripe>0 && young>0,"A tall wild tree supplies a ready first harvest and visibly growing future fruit");
        com.bunnidogs.mod.BunnidogsMod.LOGGER.info("ORCHARD_GROVE_PREVIEW {}",preview);
        var blocked=base.east(12);region.setBlock(blocked.above(3),Blocks.CHEST.defaultBlockState(),2);
        h.assertTrue(!GuavaGroveFeature.grow(region,blocked,1832) && region.getBlockState(blocked).isAir()
                && region.getBlockState(blocked.above(3)).is(Blocks.CHEST),"A blocked trunk preserves the original container and does not leave a partial tree");
        h.assertTrue(!level.hasChunkAt(base),"Detached grove generation never requests a live terrain chunk");h.succeed();
    }
}
