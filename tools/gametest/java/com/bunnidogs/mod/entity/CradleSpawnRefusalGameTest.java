package com.bunnidogs.mod.entity;

import java.util.function.Consumer;
import com.bunnidogs.mod.block.NurseryCradleBlockEntity;
import com.bunnidogs.mod.genetics.Gene;
import com.bunnidogs.mod.genetics.Genome;
import com.bunnidogs.mod.genetics.Seed;
import com.bunnidogs.mod.genetics.Stats;
import com.bunnidogs.mod.item.SeedItem;
import com.bunnidogs.mod.registry.ModBlocks;
import com.bunnidogs.mod.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;

/** Real external spawn cancellation and reentrant block removal must conserve one litter. */
public final class CradleSpawnRefusalGameTest {
    private CradleSpawnRefusalGameTest() {}
    public static void run(GameTestHelper h) {
        var level = h.getLevel();
        for (int x = 0; x < 8; x++) for (int z = 0; z < 8; z++) {
            h.setBlock(x,0,z,Blocks.STONE);
            for (int y = 1; y < 6; y++) h.setBlock(x,y,z,Blocks.AIR);
        }
        var pos = h.absolutePos(new BlockPos(3,1,4));
        level.setBlock(pos,ModBlocks.NURSERY_CRADLE.get().defaultBlockState(),3);
        var cradle = (NurseryCradleBlockEntity)level.getBlockEntity(pos);
        var parents = Genome.DEFAULT.with(Gene.FERTILITY,0,3).with(Gene.FERTILITY,1,3);
        var seed = SeedItem.create(new Seed(parents,Stats.ZERO,parents,Stats.ZERO,0));
        seed.set(DataComponents.CUSTOM_NAME,Component.literal("Refused litter"));
        cradle.insert(seed); cradle.grow(true);
        var area = new AABB(pos).inflate(5);
        int[] attempts = {0}; boolean[] reserved = {false};
        Consumer<EntityJoinLevelEvent> reject = event -> {
            if (event.getLevel() != level || !(event.getEntity() instanceof Bunnidog pup) || !pup.isBaby()
                    || !area.contains(pup.position())) return;
            if (++attempts[0] == 1) {
                reserved[0] = cradle.takeSeed().isEmpty() && cradle.hatch().status() == NurseryHatching.Status.EMPTY;
                level.removeBlock(pos,false);
            } else event.setCanceled(true);
        };
        NurseryHatching.Outcome result;
        NeoForge.EVENT_BUS.addListener(reject);
        try { result = cradle.hatch(); }
        finally { NeoForge.EVENT_BUS.unregister(reject); }
        var drops = level.getEntitiesOfClass(ItemEntity.class,area,e -> e.getItem().is(ModItems.SEED.get()));
        h.assertTrue(attempts[0] == 2 && reserved[0] && result.status() == NurseryHatching.Status.REFUSED,
                "A native spawn refusal must roll back after reserving stock against reentrant extraction and hatching");
        h.assertTrue(level.getEntitiesOfClass(Bunnidog.class,area,Bunnidog::isBaby).isEmpty()
                && drops.stream().mapToInt(e -> e.getItem().getCount()).sum() == 1
                && ItemStack.matches(drops.getFirst().getItem(),seed),
                "Removing the cradle during a refused litter must leave zero siblings and exactly the original named seed");
        drops.forEach(ItemEntity::discard);
        level.setBlock(pos,ModBlocks.NURSERY_CRADLE.get().defaultBlockState(),3);
        var retry = (NurseryCradleBlockEntity)level.getBlockEntity(pos); retry.insert(seed); retry.grow(true);
        h.assertTrue(retry.hatch().status() == NurseryHatching.Status.HATCHED && retry.storedSeed().isEmpty(),
                "The returned litter must hatch normally after the external cancellation is gone");
    }
}
