package com.bunnidogs.mod.entity;

import com.bunnidogs.mod.genetics.Gene;
import com.bunnidogs.mod.genetics.Genome;
import com.bunnidogs.mod.genetics.Stats;
import com.bunnidogs.mod.item.TissueSampleItem;
import com.bunnidogs.mod.registry.ModEntities;
import com.bunnidogs.mod.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.entity.DispenserBlockEntity;
import net.minecraft.world.phys.AABB;

public final class CareDispenserGameTest {
    private CareDispenserGameTest() {}
    private static AABB catchment(GameTestHelper h) {
        var low = h.absolutePos(BlockPos.ZERO); var high = h.absolutePos(new BlockPos(8,6,8));
        return new AABB(low.getX(),low.getY(),low.getZ(),high.getX(),high.getY(),high.getZ());
    }
    private static int dropped(GameTestHelper h, BlockPos pos, Item item) {
        return h.getLevel().getEntitiesOfClass(ItemEntity.class,catchment(h),e -> e.getItem().is(item))
                .stream().mapToInt(e -> e.getItem().getCount()).sum();
    }
    private static void pulse(GameTestHelper h, BlockPos pos, Runnable after) {
        h.getLevel().setBlock(pos.west(),Blocks.REDSTONE_BLOCK.defaultBlockState(),3);
        h.runAfterDelay(6,() -> {
            h.getLevel().setBlock(pos.west(),Blocks.AIR.defaultBlockState(),3);
            after.run();
        });
    }
    public static void run(GameTestHelper h) {
        var level = h.getLevel();
        for (int x = 0; x < 8; x++) for (int z = 0; z < 8; z++) {
            h.setBlock(x,0,z,Blocks.STONE);
            // Native output has randomized velocity. Keep it inside this fixture, not a tiny moving search window.
            for (int y = 1; y <= 4; y++) h.setBlock(x,y,z,x == 0 || x == 7 || z == 0 || z == 7 ? Blocks.STONE : Blocks.AIR);
        }
        h.setBlock(new BlockPos(2,1,3),Blocks.DISPENSER.defaultBlockState().setValue(DispenserBlock.FACING,Direction.EAST));
        var pos = h.absolutePos(new BlockPos(2,1,3));
        var dispenser = (DispenserBlockEntity)level.getBlockEntity(pos);
        var dog = h.spawnWithNoFreeWill(ModEntities.BUNNIDOG.get(),3.5F,1,3.5F);
        int glow = Gene.COAT.alleleNamed("glow").id(), starborn = Gene.FERTILITY.alleleNamed("starborn").id();
        dog.setGenome(Genome.DEFAULT.with(Gene.COAT,0,glow).with(Gene.COAT,1,glow)
                .with(Gene.FERTILITY,0,starborn).with(Gene.FERTILITY,1,starborn));
        dog.setStats(Stats.of(5,6,7,10,20)); dog.setAge(0); dog.setHealth(dog.getMaxHealth());
        var brush = new ItemStack(Items.BRUSH);
        dispenser.setItem(0,brush);
        float health = dog.getHealth();
        pulse(h,pos,() -> {
            h.assertTrue(dropped(h,pos,ModItems.WHISKER.get()) == 1 && dropped(h,pos,Items.GLOWSTONE_DUST) == 1
                    && dropped(h,pos,ModItems.STARLIT_DUST.get()) == 1 && brush.getDamageValue() == 1 && dog.groomingWait() > 0,
                    "One real powered dispenser pulse must gather all qualified products for one wear and shared recovery");
            pulse(h,pos,() -> {
                h.assertTrue(dropped(h,pos,ModItems.STARLIT_DUST.get()) == 1 && brush.getDamageValue() == 1 && dispenser.getItem(0).is(Items.BRUSH),
                        "Repeated redstone pulses must retain an unready brush without duplicate products or wear");
                dog.setAge(-1000);
                var syringe = new ItemStack(ModItems.SYRINGE.get());
                syringe.setDamageValue(syringe.getMaxDamage()-1);
                dispenser.setItem(0,syringe);
                pulse(h,pos,() -> {
                    var samples = level.getEntitiesOfClass(ItemEntity.class,catchment(h),e -> e.getItem().is(ModItems.TISSUE_SAMPLE.get()));
                    var tissue = TissueSampleItem.read(samples.getFirst().getItem()).orElseThrow();
                    h.assertTrue(samples.size() == 1 && tissue.genome().equals(dog.genome()) && tissue.stats().equals(dog.stats())
                            && dog.getHealth() == health && dog.samplingWait() > 0 && dispenser.getItem(0).isEmpty(),
                            "A last-charge dispenser syringe must sample a pup exactly, preserve health and break normally");
                    var apex = ModEntities.APEX.get().create(level,net.minecraft.world.entity.EntitySpawnReason.COMMAND);
                    h.assertTrue(!BunnidogCare.canSample(apex) && !BunnidogCare.canGroom(apex),"Shared care transactions must exclude the Apex");
                    var nativeBrush = new ItemStack(Items.BRUSH);
                    dispenser.setItem(0,nativeBrush);
                    h.spawnWithNoFreeWill(EntityTypes.ARMADILLO,3.5F,1,3.5F);
                    pulse(h,pos,() -> {
                        h.assertTrue(dropped(h,pos,Items.ARMADILLO_SCUTE) == 1 && nativeBrush.getDamageValue() == 16,
                                "The original native armadillo brushing behavior and its tool cost must still work");
                        h.succeed();
                    });
                });
            });
        });
    }
}
