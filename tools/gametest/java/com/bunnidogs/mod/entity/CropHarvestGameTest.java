package com.bunnidogs.mod.entity;

import java.util.ArrayList;
import java.util.function.Consumer;
import com.bunnidogs.mod.block.SeedCropBlock;
import com.bunnidogs.mod.block.SeedCropBlockEntity;
import com.bunnidogs.mod.genetics.Breeding;
import com.bunnidogs.mod.genetics.Gene;
import com.bunnidogs.mod.genetics.Genome;
import com.bunnidogs.mod.genetics.Seed;
import com.bunnidogs.mod.genetics.Stats;
import com.bunnidogs.mod.item.SeedItem;
import com.bunnidogs.mod.registry.ModBlocks;
import com.bunnidogs.mod.registry.ModEntities;
import com.bunnidogs.mod.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.level.block.BreakBlockEvent;

/** Exercise committed native harvesting rather than posting a synthetic break event. */
public final class CropHarvestGameTest {
    private CropHarvestGameTest() {}
    private static SeedCropBlockEntity plant(GameTestHelper h, BlockPos pos, ItemStack seed, boolean ripe) {
        var level = h.getLevel(); level.setBlock(pos.below(),Blocks.FARMLAND.defaultBlockState(),3);
        level.setBlock(pos,ModBlocks.SEED_CROP.get().defaultBlockState().setValue(SeedCropBlock.AGE,ripe ? 7 : 0),3);
        var crop = (SeedCropBlockEntity)level.getBlockEntity(pos); crop.setSeedStack(seed); return crop;
    }
    public static void run(GameTestHelper h) {
        var level = h.getLevel();
        for (int x = 0; x < 8; x++) for (int z = 0; z < 8; z++) {
            h.setBlock(x,0,z,Blocks.STONE);
            for (int y = 1; y < 7; y++) h.setBlock(x,y,z,Blocks.AIR);
        }
        var pos = h.absolutePos(new BlockPos(3,1,3)); var area = new AABB(pos).inflate(3);
        var player = h.makeMockServerPlayerInLevel(); player.gameMode.changeGameModeForPlayer(GameType.SURVIVAL);
        var parents = Genome.DEFAULT.with(Gene.FERTILITY,0,3).with(Gene.FERTILITY,1,3);
        var seed = SeedItem.create(new Seed(parents,Stats.of(1,2,3,4,5),parents,Stats.of(6,7,8,9,10),2.5,true));
        seed.set(DataComponents.CUSTOM_NAME,Component.literal("Protected line"));
        var crop = plant(h,pos,seed,false);
        Consumer<BreakBlockEvent> protect = event -> { if (event.getLevel() == level && event.getPos().equals(pos)) event.setCanceled(true); };
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST,protect);
        try {
            h.assertTrue(!player.gameMode.destroyBlock(pos) && level.getBlockEntity(pos) == crop && ItemStack.matches(crop.seedStack(),seed)
                    && level.getEntitiesOfClass(ItemEntity.class,area).isEmpty(),
                    "A late protection cancellation must leave an unripe crop intact without prematurely refunding its seed");
            level.setBlock(pos,level.getBlockState(pos).setValue(SeedCropBlock.AGE,7),3);
            h.assertTrue(!player.gameMode.destroyBlock(pos) && level.getBlockEntity(pos) == crop
                    && level.getEntitiesOfClass(Bunnidog.class,area,Bunnidog::isBaby).isEmpty() && ItemStack.matches(crop.seedStack(),seed),
                    "A late protection cancellation must leave a ripe litter untouched with no premature pups");
        } finally { NeoForge.EVENT_BUS.unregister(protect); }
        player.gameMode.changeGameModeForPlayer(GameType.ADVENTURE); player.gameMode.destroyBlock(pos);
        h.assertTrue(level.getBlockEntity(pos) == crop && ItemStack.matches(crop.seedStack(),seed),"Native adventure restrictions must preserve the planted litter");
        player.gameMode.changeGameModeForPlayer(GameType.SURVIVAL); player.gameMode.destroyBlock(pos);
        var born = level.getEntitiesOfClass(Bunnidog.class,area,Bunnidog::isBaby);
        h.assertTrue(level.getBlockState(pos).isAir() && born.size() == 4
                && born.stream().allMatch(pup -> pup.motherGenome().orElseThrow().equals(parents) && pup.fatherGenome().orElseThrow().equals(parents))
                && level.getEntitiesOfClass(ItemEntity.class,area).isEmpty(),"An accepted survival harvest must consume the crop and create its entire actual litter once");
        born.forEach(Bunnidog::discard);
        plant(h,pos,seed,false); player.gameMode.changeGameModeForPlayer(GameType.CREATIVE); player.gameMode.destroyBlock(pos);
        h.assertTrue(level.getBlockState(pos).isAir() && level.getEntitiesOfClass(ItemEntity.class,area).isEmpty(),"Creative unripe removal must retain ordinary no-drop behavior");
        plant(h,pos,seed,true); player.gameMode.destroyBlock(pos);
        born = level.getEntitiesOfClass(Bunnidog.class,area,Bunnidog::isBaby);
        h.assertTrue(born.size() == 4 && level.getBlockState(pos).isAir(),"Creative ripe harvest must retain its established hatching behavior"); born.forEach(Bunnidog::discard);
        crop = plant(h,pos,seed,true); player.gameMode.changeGameModeForPlayer(GameType.SURVIVAL);
        var crowd = new ArrayList<Bunnidog>();
        try {
            for (int i = 0; i < Breeding.CROWD_LIMIT; i++) crowd.add(h.spawnWithNoFreeWill(ModEntities.BUNNIDOG.get(),6,1,6));
            player.gameMode.destroyBlock(pos);
            h.assertTrue(level.getBlockEntity(pos) == crop && SeedCropBlock.isRipe(level.getBlockState(pos)) && ItemStack.matches(crop.seedStack(),seed),
                    "A crowded native harvest must leave the original ripe crop and exact seed in place");
        } finally { crowd.forEach(Bunnidog::discard); }
        int[] attempts = {0}; boolean[] removedBeforeSpawn = {false};
        Consumer<EntityJoinLevelEvent> reject = event -> {
            if (event.getLevel() != level || !(event.getEntity() instanceof Bunnidog pup) || !area.contains(pup.position())) return;
            removedBeforeSpawn[0] = level.getBlockState(pos).isAir();
            if (++attempts[0] == 2) event.setCanceled(true);
        };
        NeoForge.EVENT_BUS.addListener(reject);
        try { player.gameMode.destroyBlock(pos); }
        finally { NeoForge.EVENT_BUS.unregister(reject); }
        var refunded = level.getEntitiesOfClass(ItemEntity.class,area,e -> e.getItem().is(ModItems.SEED.get()));
        h.assertTrue(attempts[0] == 2 && removedBeforeSpawn[0] && level.getEntitiesOfClass(Bunnidog.class,area,Bunnidog::isBaby).isEmpty()
                && refunded.size() == 1 && ItemStack.matches(refunded.getFirst().getItem(),seed),
                "Native spawn refusal after committed crop removal must roll back all siblings and refund exactly the named seed");
        refunded.forEach(ItemEntity::discard);
        plant(h,pos,seed,true); level.setBlock(pos,Blocks.WATER.defaultBlockState(),3);
        refunded = level.getEntitiesOfClass(ItemEntity.class,area,e -> e.getItem().is(ModItems.SEED.get()));
        h.assertTrue(level.getBlockState(pos).is(Blocks.WATER) && refunded.size() == 1 && ItemStack.matches(refunded.getFirst().getItem(),seed)
                && level.getEntitiesOfClass(Bunnidog.class,area,Bunnidog::isBaby).isEmpty(),
                "Environmental crop replacement must preserve the full seed once without spawning a litter or overwriting the new block");
    }
}
