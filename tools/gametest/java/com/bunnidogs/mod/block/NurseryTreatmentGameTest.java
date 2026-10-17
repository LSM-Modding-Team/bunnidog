package com.bunnidogs.mod.block;

import com.bunnidogs.mod.genetics.Genome;
import com.bunnidogs.mod.genetics.Seed;
import com.bunnidogs.mod.genetics.Stats;
import com.bunnidogs.mod.item.SeedItem;
import com.bunnidogs.mod.registry.ModBlocks;
import com.bunnidogs.mod.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.DispenserBlockEntity;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public final class NurseryTreatmentGameTest {
    private NurseryTreatmentGameTest() {}
    private static void use(ServerPlayer player, BlockPos pos, InteractionHand hand) {
        player.gameMode.useItemOn(player,player.level(),player.getItemInHand(hand),hand,new BlockHitResult(Vec3.atCenterOf(pos),Direction.UP,pos,false));
    }
    private static void pulse(GameTestHelper h, BlockPos pos, Runnable after) {
        h.getLevel().setBlock(pos.west(),Blocks.REDSTONE_BLOCK.defaultBlockState(),3);
        h.runAfterDelay(6,() -> { h.getLevel().setBlock(pos.west(),Blocks.AIR.defaultBlockState(),3); after.run(); });
    }
    private static void remaining(NurseryCradleBlockEntity cradle, int ticks) {
        var level = cradle.getLevel(); var saved = cradle.saveWithFullMetadata(level.registryAccess()); saved.putInt("GrowthWait",ticks);
        cradle.loadAdditional(TagValueInput.create(ProblemReporter.DISCARDING,level.registryAccess(),saved));
    }
    public static void run(GameTestHelper h) {
        var level = h.getLevel();
        for (int x = 0; x < 8; x++) for (int z = 0; z < 8; z++) {
            h.setBlock(x,0,z,Blocks.STONE); for (int y = 1; y < 5; y++) h.setBlock(x,y,z,Blocks.AIR);
        }
        var source = SeedItem.create(new Seed(Genome.DEFAULT,Stats.of(1,2,3,4,5),Genome.DEFAULT,Stats.of(6,7,8,9,10),3.5));
        source.set(DataComponents.CUSTOM_NAME,Component.literal("Treat without uprooting"));
        var treated = source.copy(); treated.set(ModItems.SEED_DATA.get(),SeedItem.read(source).orElseThrow().withMutagen().toString());
        var cropPos = h.absolutePos(new BlockPos(3,1,2)); var cradlePos = h.absolutePos(new BlockPos(3,1,5));
        level.setBlock(cropPos.below(),Blocks.FARMLAND.defaultBlockState(),3); level.setBlock(cropPos,ModBlocks.SEED_CROP.get().defaultBlockState().setValue(SeedCropBlock.AGE,7),3);
        var crop = (SeedCropBlockEntity)level.getBlockEntity(cropPos); crop.setSeedStack(source);
        level.setBlock(cradlePos,ModBlocks.NURSERY_CRADLE.get().defaultBlockState(),3); var cradle = (NurseryCradleBlockEntity)level.getBlockEntity(cradlePos);
        cradle.insert(source); cradle.grow(false); remaining(cradle,137); int age = cradle.age();
        var player = h.makeMockServerPlayerInLevel(); player.gameMode.changeGameModeForPlayer(GameType.SURVIVAL);
        player.setItemInHand(InteractionHand.OFF_HAND,new ItemStack(ModItems.MUTAGEN.get(),2)); use(player,cropPos,InteractionHand.OFF_HAND);
        h.assertTrue(player.getOffhandItem().getCount() == 1 && SeedCropBlock.isRipe(level.getBlockState(cropPos)) && ItemStack.matches(crop.seedStack(),treated),
                "Real off-hand Mutagen use must treat a whole named ripe crop for one vial without changing its stage");
        use(player,cropPos,InteractionHand.OFF_HAND); h.assertTrue(player.getOffhandItem().getCount() == 1,"An already treated crop must keep the vial");
        player.setItemInHand(InteractionHand.OFF_HAND,ItemStack.EMPTY); player.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(ModItems.MUTAGEN.get(),2));
        player.gameMode.changeGameModeForPlayer(GameType.ADVENTURE); use(player,cradlePos,InteractionHand.MAIN_HAND);
        h.assertTrue(player.getMainHandItem().getCount() == 2 && ItemStack.matches(cradle.storedSeed(),source),"Native Adventure/build restrictions must preserve Cradle stock and vials");
        player.gameMode.changeGameModeForPlayer(GameType.CREATIVE); use(player,cradlePos,InteractionHand.MAIN_HAND);
        h.assertTrue(player.getMainHandItem().getCount() == 2 && ItemStack.matches(cradle.storedSeed(),treated) && cradle.age() == age && cradle.growthWait() == 137,
                "Creative native treatment must retain the actual held stack, exact seed, stage and remaining incubation");
        var restored = (NurseryCradleBlockEntity)BlockEntity.loadStatic(cradlePos,cradle.getBlockState(),cradle.saveWithFullMetadata(level.registryAccess()),level.registryAccess());
        h.assertTrue(ItemStack.matches(restored.storedSeed(),treated) && restored.age() == age && restored.growthWait() == 137,
                "Native Cradle saves must retain in-place treatment without restarting incubation");
        var dispenserPos = cradlePos.west(); level.setBlock(dispenserPos,Blocks.DISPENSER.defaultBlockState().setValue(DispenserBlock.FACING,Direction.EAST),3);
        var dispenser = (DispenserBlockEntity)level.getBlockEntity(dispenserPos); var vials = new ItemStack(ModItems.MUTAGEN.get(),4); dispenser.setItem(0,vials);
        pulse(h,dispenserPos,() -> {
            h.assertTrue(vials.getCount() == 4 && ItemStack.matches(cradle.storedSeed(),treated),"A dispenser must retain vials for an already treated Cradle");
            cradle.takeSeed(); cradle.insert(source); cradle.grow(false); remaining(cradle,137); int before = cradle.age();
            pulse(h,dispenserPos,() -> {
                h.assertTrue(vials.getCount() == 3 && ItemStack.matches(cradle.storedSeed(),treated) && cradle.age() == before && cradle.growthWait() <= 137 && cradle.growthWait() > 120,
                        "One real dispenser pulse must treat a Cradle without resetting its stage or loaded timer");
                pulse(h,dispenserPos,() -> {
                    h.assertTrue(vials.getCount() == 3,"Repeat Cradle treatment must cost nothing");
                    cradle.takeSeed(); level.setBlock(cradlePos.below(),Blocks.FARMLAND.defaultBlockState(),3);
                    level.setBlock(cradlePos,ModBlocks.SEED_CROP.get().defaultBlockState().setValue(SeedCropBlock.AGE,7),3);
                    var planted = (SeedCropBlockEntity)level.getBlockEntity(cradlePos); planted.setSeedStack(source);
                    pulse(h,dispenserPos,() -> {
                        h.assertTrue(vials.getCount() == 2 && ItemStack.matches(planted.seedStack(),treated) && SeedCropBlock.isRipe(level.getBlockState(cradlePos)),
                                "A native dispenser must treat the named ripe crop for one vial with parents/stats/quality intact");
                        pulse(h,dispenserPos,() -> {
                            h.assertTrue(vials.getCount() == 2,"Repeat crop treatment must cost nothing"); planted.takeSeed();
                            pulse(h,dispenserPos,() -> {
                                h.assertTrue(vials.getCount() == 2,"An empty crop must retain dispenser Mutagen"); level.setBlock(cradlePos,Blocks.WHEAT.defaultBlockState(),3);
                                pulse(h,dispenserPos,() -> {
                                    h.assertTrue(vials.getCount() == 2 && level.getEntitiesOfClass(ItemEntity.class,new AABB(cradlePos).inflate(3),e -> e.getItem().is(ModItems.MUTAGEN.get())).isEmpty(),
                                            "Wrong targets must retain Mutagen without loose-item ejection"); h.succeed();
                                });
                            });
                        });
                    });
                });
            });
        });
    }
}
