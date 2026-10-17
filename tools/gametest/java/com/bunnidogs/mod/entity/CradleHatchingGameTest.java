package com.bunnidogs.mod.entity;

import java.util.ArrayList;
import com.bunnidogs.mod.block.NurseryCradleBlock;
import com.bunnidogs.mod.block.NurseryCradleBlockEntity;
import com.bunnidogs.mod.genetics.Breeding;
import com.bunnidogs.mod.genetics.Genome;
import com.bunnidogs.mod.genetics.Seed;
import com.bunnidogs.mod.genetics.Stats;
import com.bunnidogs.mod.item.SeedItem;
import com.bunnidogs.mod.registry.ModBlocks;
import com.bunnidogs.mod.registry.ModEntities;
import com.bunnidogs.mod.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public final class CradleHatchingGameTest {
    private CradleHatchingGameTest() {}
    public static void run(GameTestHelper h) {
        var level = h.getLevel();
        for (int x = 0; x < 8; x++) for (int z = 0; z < 8; z++) {
            h.setBlock(x,0,z,Blocks.STONE);
            for (int y = 1; y < 8; y++) h.setBlock(x,y,z,Blocks.AIR);
        }
        h.setBlock(new BlockPos(3,1,4),ModBlocks.NURSERY_CRADLE.get().defaultBlockState().setValue(NurseryCradleBlock.FACING,Direction.NORTH));
        var pos = h.absolutePos(new BlockPos(3,1,4)); var center = pos.north(2);
        var cradle = (NurseryCradleBlockEntity)level.getBlockEntity(pos);
        var seed = new Seed(Genome.DEFAULT,Stats.ZERO,Genome.DEFAULT,Stats.ZERO,0);
        var source = SeedItem.create(seed); cradle.insert(source);
        h.assertTrue(cradle.hatch().status() == NurseryHatching.Status.NOT_RIPE && ItemStack.matches(cradle.storedSeed(),source),"Unripe hatch attempts must retain the entire seed");
        var player = h.makeMockServerPlayerInLevel(); player.gameMode.changeGameModeForPlayer(GameType.SURVIVAL);
        player.setShiftKeyDown(true); var calipers = new ItemStack(ModItems.CALIPERS.get()); player.setItemInHand(InteractionHand.MAIN_HAND,calipers);
        var hit = new BlockHitResult(Vec3.atCenterOf(pos),Direction.UP,pos,false);
        player.gameMode.useItemOn(player,level,calipers,InteractionHand.MAIN_HAND,hit);
        h.assertTrue(NurseryHatching.inspect(level,pos,Direction.NORTH).status() == NurseryHatching.Status.READY
                && ItemStack.matches(cradle.storedSeed(),source) && cradle.age() == 0 && cradle.growthWait() == NurseryCradleBlockEntity.STAGE_TICKS
                && player.getMainHandItem() == calipers && level.getEntitiesOfClass(Bunnidog.class,new AABB(center).inflate(2),Bunnidog::isBaby).isEmpty(),
                "Native sneak-Calipers must inspect an unripe clear pad without cost, incubation changes or premature pups");
        cradle.grow(true); level.setBlock(center,Blocks.STONE.defaultBlockState(),3);
        player.gameMode.useItemOn(player,level,calipers,InteractionHand.MAIN_HAND,hit);
        h.assertTrue(NurseryHatching.inspect(level,pos,Direction.NORTH).problem().equals(center) && ItemStack.matches(cradle.storedSeed(),source) && cradle.age() == 7,
                "A ripe blocked-pad readout must identify the actual obstruction while retaining the whole seed");
        h.assertTrue(cradle.hatch().status() == NurseryHatching.Status.BLOCKED,"Solid pad obstructions must retain the ripe seed");
        level.setBlock(center,Blocks.WATER.defaultBlockState(),3);
        h.assertTrue(cradle.hatch().status() == NurseryHatching.Status.BLOCKED,"A flooded hatch pad must be refused");
        level.setBlock(center,Blocks.AIR.defaultBlockState(),3); level.setBlock(center.below(),Blocks.MAGMA_BLOCK.defaultBlockState(),3);
        h.assertTrue(cradle.hatch().status() == NurseryHatching.Status.BLOCKED,"Damaging floor must be refused");
        level.setBlock(center.below(),Blocks.STONE.defaultBlockState(),3);
        var cow = h.spawnWithNoFreeWill(EntityTypes.COW,3.5F,1,2.5F);
        h.assertTrue(cradle.hatch().status() == NurseryHatching.Status.BLOCKED,"Living occupants must block the pad"); cow.discard();
        var crowd = new ArrayList<Bunnidog>();
        try {
            for (int i = 0; i < Breeding.CROWD_LIMIT; i++) {
                var dog = h.spawnWithNoFreeWill(ModEntities.BUNNIDOG.get(),3.5F,1,6.5F); dog.setGenome(Genome.DEFAULT); crowd.add(dog);
            }
            h.assertTrue(cradle.hatch().status() == NurseryHatching.Status.CROWDED && cradle.age() == 7
                    && ItemStack.matches(cradle.storedSeed(),source),"The native local crowd limit must preserve the ripe litter");
            h.assertTrue(NurseryHatching.inspect(level,pos,Direction.NORTH).status() == NurseryHatching.Status.CROWDED
                    && NurseryHatching.inspect(level,pos,Direction.NORTH).nearby() >= Breeding.CROWD_LIMIT,
                    "Read-only pad inspection and actual hatching must share native crowd refusal");
        } finally { crowd.forEach(Bunnidog::discard); }
        player.setShiftKeyDown(false); player.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);
        player.gameMode.useItemOn(player,level,ItemStack.EMPTY,InteractionHand.MAIN_HAND,new BlockHitResult(Vec3.atCenterOf(pos),Direction.UP,pos,false));
        var babies = level.getEntitiesOfClass(Bunnidog.class,new AABB(center).inflate(2),Bunnidog::isBaby);
        h.assertTrue(cradle.storedSeed().isEmpty() && babies.size() >= 1 && babies.size() <= 6
                && babies.stream().allMatch(pup -> pup.motherGenome().orElseThrow().equals(seed.mother()) && pup.fatherGenome().orElseThrow().equals(seed.father())
                        && NurseryHatching.pad(pos,Direction.NORTH).contains(pup.blockPosition())),"Actual empty-hand hatching must consume one seed and make safely placed pups with original parents");
        babies.forEach(Bunnidog::discard); cradle.insert(source);
        level.setBlock(pos.west(),Blocks.REDSTONE_BLOCK.defaultBlockState(),3);
        h.runAfterDelay(3,() -> {
            h.assertTrue(cradle.age() < 7 && !cradle.storedSeed().isEmpty(),"An unripe pulse must leave the seed incubating");
            cradle.grow(true);
            h.runAfterDelay(3,() -> {
                h.assertTrue(cradle.age() == 7 && !cradle.storedSeed().isEmpty(),"Ripening under steady power must not silently hatch");
                level.setBlock(pos.west(),Blocks.AIR.defaultBlockState(),3); level.setBlock(pos.west(),Blocks.REDSTONE_BLOCK.defaultBlockState(),3);
                h.runAfterDelay(3,() -> {
                    var born = level.getEntitiesOfClass(Bunnidog.class,new AABB(center).inflate(2),Bunnidog::isBaby);
                    h.assertTrue(cradle.storedSeed().isEmpty() && !born.isEmpty() && cradle.stockSignal() == 0,
                            "A fresh real redstone edge must hatch the ripe litter exactly once");
                    h.succeed();
                });
            });
        });
    }
}
