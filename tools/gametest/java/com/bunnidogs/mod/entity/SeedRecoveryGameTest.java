package com.bunnidogs.mod.entity;

import com.bunnidogs.mod.block.SeedCropBlockEntity;
import com.bunnidogs.mod.genetics.Genome;
import com.bunnidogs.mod.genetics.Seed;
import com.bunnidogs.mod.genetics.Stats;
import com.bunnidogs.mod.item.SeedItem;
import com.bunnidogs.mod.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public final class SeedRecoveryGameTest {
    private SeedRecoveryGameTest() {}
    public static void run(GameTestHelper h) {
        var level = h.getLevel();
        var player = h.makeMockServerPlayerInLevel(); player.gameMode.changeGameModeForPlayer(GameType.SURVIVAL);
        var soil = h.absolutePos(new BlockPos(3,0,3)); var pos = soil.above();
        level.setBlock(soil,Blocks.FARMLAND.defaultBlockState(),3);
        var seed = new Seed(Genome.DEFAULT,Stats.of(1,2,3,4,5),Genome.DEFAULT,Stats.of(6,7,8,9,10),2.5);
        var source = SeedItem.create(seed); source.setCount(2);
        source.set(DataComponents.CUSTOM_NAME,Component.literal("Moonstep line"));
        var data = new CompoundTag(); data.putString("BreederNote","keep this line");
        source.set(DataComponents.CUSTOM_DATA,CustomData.of(data));
        var original = source.copyWithCount(1);
        player.setItemInHand(InteractionHand.MAIN_HAND,source);
        player.gameMode.useItemOn(player,level,source,InteractionHand.MAIN_HAND,new BlockHitResult(Vec3.atCenterOf(soil),Direction.UP,soil,false));
        var crop = (SeedCropBlockEntity)level.getBlockEntity(pos);
        h.assertTrue(source.getCount() == 1 && crop != null && ItemStack.matches(crop.seedStack(),original),"Hand planting must store one whole named seed with arbitrary components");
        source.set(DataComponents.CUSTOM_NAME,Component.literal("Changed source"));
        var read = crop.seedStack(); read.remove(DataComponents.CUSTOM_DATA);
        h.assertTrue(ItemStack.matches(crop.seedStack(),original),"Stored seed and returned reads must be independent of source mutations");
        var saved = crop.saveWithFullMetadata(level.registryAccess());
        var restored = (SeedCropBlockEntity)BlockEntity.loadStatic(pos,crop.getBlockState(),saved,level.registryAccess());
        h.assertTrue(ItemStack.matches(restored.seedStack(),original),"Native crop save/load must preserve whole seed components");
        saved.remove("SeedItem");
        var legacy = (SeedCropBlockEntity)BlockEntity.loadStatic(pos,crop.getBlockState(),saved,level.registryAccess());
        h.assertTrue(SeedItem.read(legacy.seedStack()).orElseThrow().equals(seed) && legacy.seedStack().getCount() == 1,
                "Legacy Seed-only crops must still recover one complete functional seed");
        crop.setSeed(seed.withMutagen());
        var treated = original.copy(); treated.set(ModItems.SEED_DATA.get(),seed.withMutagen().toString());
        h.assertTrue(ItemStack.matches(crop.seedStack(),treated),"Updating functional seed data must retain its name and custom components");
        player.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);
        h.assertTrue(player.gameMode.destroyBlock(pos),"A real survival break must remove an unripe crop");
        var drops = level.getEntitiesOfClass(ItemEntity.class,new AABB(pos).inflate(2),item -> item.getItem().is(ModItems.SEED.get()));
        h.assertTrue(drops.size() == 1 && ItemStack.matches(drops.getFirst().getItem(),treated),"Unripe player harvest must return the exact updated whole seed once");
        drops.getFirst().discard();
        level.setBlock(soil,Blocks.FARMLAND.defaultBlockState(),3); player.setItemInHand(InteractionHand.MAIN_HAND,original.copy());
        player.gameMode.useItemOn(player,level,player.getMainHandItem(),InteractionHand.MAIN_HAND,new BlockHitResult(Vec3.atCenterOf(soil),Direction.UP,soil,false));
        level.setBlock(soil,Blocks.AIR.defaultBlockState(),2);
        level.getBlockState(pos).randomTick(level,pos,level.getRandom());
        var recovered = level.getEntitiesOfClass(ItemEntity.class,new AABB(pos).inflate(2),item -> item.getItem().is(ModItems.SEED.get()));
        h.assertTrue(level.getBlockState(pos).isAir() && recovered.size() == 1 && ItemStack.matches(recovered.getFirst().getItem(),original),
                "Lost support must recover the original seed components once");
        var hand = original.copyWithCount(2); player.setItemInHand(InteractionHand.MAIN_HAND,hand);
        var mutagen = new ItemStack(ModItems.MUTAGEN.get(),2); player.setItemInHand(InteractionHand.OFF_HAND,mutagen);
        player.gameMode.useItem(player,level,hand,InteractionHand.MAIN_HAND);
        int matching = 0;
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            var stack = player.getInventory().getItem(slot);
            if (ItemStack.isSameItemSameComponents(stack,treated)) matching += stack.getCount();
        }
        h.assertTrue(hand.getCount() == 1 && mutagen.getCount() == 1 && matching == 1,
                "Native Mutagen use must preserve the treated seed's components and normal split costs");
        h.succeed();
    }
}
