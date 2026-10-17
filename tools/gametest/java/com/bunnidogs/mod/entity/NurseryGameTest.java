package com.bunnidogs.mod.entity;

import com.bunnidogs.mod.block.SeedCropBlock;
import com.bunnidogs.mod.block.SeedCropBlockEntity;
import com.bunnidogs.mod.genetics.Gene;
import com.bunnidogs.mod.genetics.Genome;
import com.bunnidogs.mod.genetics.Seed;
import com.bunnidogs.mod.genetics.Stats;
import com.bunnidogs.mod.item.SeedItem;
import com.bunnidogs.mod.registry.ModBlocks;
import com.bunnidogs.mod.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.DispenserBlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public final class NurseryGameTest {
    private NurseryGameTest() {}
    private static void pulse(GameTestHelper h, BlockPos dispenser, Direction back, Runnable after) {
        h.getLevel().setBlock(dispenser.relative(back),Blocks.REDSTONE_BLOCK.defaultBlockState(),3);
        h.runAfterDelay(6,() -> {
            h.getLevel().setBlock(dispenser.relative(back),Blocks.AIR.defaultBlockState(),3);
            after.run();
        });
    }
    private static void use(ServerPlayer player, BlockPos pos) {
        player.gameMode.useItemOn(player,player.level(),player.getMainHandItem(),InteractionHand.MAIN_HAND,
                new BlockHitResult(Vec3.atCenterOf(pos),Direction.UP,pos,false));
    }
    private static SeedCropBlockEntity crop(GameTestHelper h, BlockPos pos) {
        return (SeedCropBlockEntity)h.getLevel().getBlockEntity(pos);
    }
    public static void run(GameTestHelper h) {
        var level = h.getLevel();
        for (int x = 0; x < 8; x++) for (int z = 0; z < 8; z++) {
            h.setBlock(x,0,z,Blocks.STONE);
            for (int y = 1; y < 5; y++) h.setBlock(x,y,z,Blocks.AIR);
        }
        h.setBlock(new BlockPos(2,1,3),Blocks.DISPENSER.defaultBlockState().setValue(DispenserBlock.FACING,Direction.EAST));
        h.setBlock(new BlockPos(3,1,4),Blocks.DISPENSER.defaultBlockState().setValue(DispenserBlock.FACING,Direction.NORTH));
        h.setBlock(4,0,3,Blocks.WATER);
        var planterPos = h.absolutePos(new BlockPos(2,1,3)); var dusterPos = h.absolutePos(new BlockPos(3,1,4));
        var target = h.absolutePos(new BlockPos(3,1,3));
        var planter = (DispenserBlockEntity)level.getBlockEntity(planterPos);
        var duster = (DispenserBlockEntity)level.getBlockEntity(dusterPos);
        var seed = new Seed(Genome.DEFAULT.with(Gene.COAT,0,1),Stats.of(10,11,12,13,14),
                Genome.DEFAULT.with(Gene.DIET,1,1),Stats.of(15,16,17,18,19),3.5,true);
        var empty = new ItemStack(ModItems.SEED.get(),2); planter.setItem(0,empty);
        level.setBlock(target.below(),Blocks.FARMLAND.defaultBlockState(),3);
        pulse(h,planterPos,Direction.WEST,() -> {
            h.assertTrue(empty.getCount() == 2 && level.getBlockState(target).isAir(),"A seed with no saved litter must remain in the dispenser");
            var source = SeedItem.create(seed); source.setCount(2); planter.setItem(0,source);
            level.setBlock(target,Blocks.STONE.defaultBlockState(),3);
            pulse(h,planterPos,Direction.WEST,() -> {
                h.assertTrue(source.getCount() == 2 && level.getBlockState(target).is(Blocks.STONE),"An obstructed nursery must not spend or eject the seed");
                level.setBlock(target,Blocks.AIR.defaultBlockState(),3); level.setBlock(target.below(),Blocks.DIRT.defaultBlockState(),3);
                pulse(h,planterPos,Direction.WEST,() -> {
                    h.assertTrue(source.getCount() == 2 && level.getBlockState(target).isAir(),"Dispenser planting must require farmland, just like hand planting");
                    level.setBlock(target.below(),Blocks.FARMLAND.defaultBlockState(),3);
                    pulse(h,planterPos,Direction.WEST,() -> {
                        h.assertTrue(source.getCount() == 1 && level.getBlockState(target).is(ModBlocks.SEED_CROP.get())
                                && !SeedCropBlock.isRipe(level.getBlockState(target)) && crop(h,target).seed().orElseThrow().equals(seed),
                                "One real pulse must plant exactly one unrolled litter with both genomes/stats, quality and Mutagen intact");
                        pulse(h,planterPos,Direction.WEST,() -> {
                            h.assertTrue(source.getCount() == 1 && crop(h,target).seed().orElseThrow().equals(seed),"Another planting pulse must preserve the occupied crop and remaining seed");
                            var dust = new ItemStack(ModItems.STARLIT_DUST.get(),2); duster.setItem(0,dust);
                            pulse(h,dusterPos,Direction.SOUTH,() -> {
                                h.assertTrue(dust.getCount() == 1 && SeedCropBlock.isRipe(level.getBlockState(target)) && crop(h,target).seed().orElseThrow().equals(seed),
                                        "One dust pulse must ripen the existing litter for exactly one dust");
                                var saved = crop(h,target).saveWithFullMetadata(level.registryAccess());
                                var restored = (SeedCropBlockEntity)BlockEntity.loadStatic(target,level.getBlockState(target),saved,level.registryAccess());
                                h.assertTrue(restored.seed().orElseThrow().equals(seed),"Native crop saves must retain the whole functional litter after automation");
                                pulse(h,dusterPos,Direction.SOUTH,() -> {
                                    h.assertTrue(dust.getCount() == 1,"Already-ripe crops must retain dispenser dust");
                                    level.setBlock(target,Blocks.AIR.defaultBlockState(),3);
                                    level.setBlock(target,ModBlocks.SEED_CROP.get().defaultBlockState(),3);
                                    pulse(h,dusterPos,Direction.SOUTH,() -> {
                                        h.assertTrue(dust.getCount() == 1 && !SeedCropBlock.isRipe(level.getBlockState(target)),"An empty commanded crop must not waste dust");
                                        level.setBlock(target,Blocks.WHEAT.defaultBlockState(),3);
                                        pulse(h,dusterPos,Direction.SOUTH,() -> {
                                            h.assertTrue(dust.getCount() == 1 && level.getBlockState(target).equals(Blocks.WHEAT.defaultBlockState()),"Other plants must be left intact");
                                            var player = h.makeMockServerPlayerInLevel(); player.gameMode.changeGameModeForPlayer(GameType.SURVIVAL);
                                            var handSoil = h.absolutePos(new BlockPos(5,0,2)); level.setBlock(handSoil,Blocks.FARMLAND.defaultBlockState(),3);
                                            player.setItemInHand(InteractionHand.MAIN_HAND,SeedItem.create(seed)); use(player,handSoil);
                                            h.assertTrue(player.getMainHandItem().isEmpty() && crop(h,handSoil.above()).seed().orElseThrow().equals(seed),"Native hand planting must share exact data and one-seed cost");
                                            h.setBlock(4,0,3,Blocks.STONE);
                                            level.setBlock(handSoil,Blocks.FARMLAND.defaultBlockState(),3);
                                            level.getBlockState(handSoil).randomTick(level,handSoil,level.getRandom());
                                            h.assertTrue(level.getBlockState(handSoil).is(Blocks.FARMLAND),"A planted Seed Crop must maintain dry farmland under the native random tick");
                                            var handDust = new ItemStack(ModItems.STARLIT_DUST.get(),2); player.setItemInHand(InteractionHand.MAIN_HAND,handDust); use(player,handSoil.above());
                                            h.assertTrue(handDust.getCount() == 1 && SeedCropBlock.isRipe(level.getBlockState(handSoil.above()))
                                                    && crop(h,handSoil.above()).seed().orElseThrow().equals(seed),"Native hand dust must share ripening, cost and litter preservation");
                                            use(player,handSoil.above()); h.assertTrue(handDust.getCount() == 1,"Repeated hand dust must refuse an already-ripe crop");
                                            player.gameMode.changeGameModeForPlayer(GameType.CREATIVE);
                                            level.setBlock(handSoil.above(),level.getBlockState(handSoil.above()).setValue(SeedCropBlock.AGE,0),3);
                                            use(player,handSoil.above());
                                            h.assertTrue(handDust.getCount() == 1 && SeedCropBlock.isRipe(level.getBlockState(handSoil.above())),"Creative hand dust must retain its existing free-use behavior");
                                            h.assertTrue(level.getEntitiesOfClass(ItemEntity.class,new AABB(target).inflate(3),
                                                    entity -> entity.getItem().is(ModItems.SEED.get()) || entity.getItem().is(ModItems.STARLIT_DUST.get())).isEmpty(),"Refused dispensers must not eject nursery consumables");
                                            h.succeed();
                                        });
                                    });
                                });
                            });
                        });
                    });
                });
            });
        });
    }
}
