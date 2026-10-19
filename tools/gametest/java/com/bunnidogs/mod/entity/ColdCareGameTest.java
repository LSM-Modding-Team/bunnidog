package com.bunnidogs.mod.entity;

import com.bunnidogs.mod.genetics.Gene;
import com.bunnidogs.mod.genetics.Genome;
import com.bunnidogs.mod.genetics.Grooming;
import com.bunnidogs.mod.genetics.Stats;
import com.bunnidogs.mod.registry.ModEntities;
import com.bunnidogs.mod.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.entity.DispenserBlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/** Live climate, inherited expression and ordinary care tools share their real recoveries. */
public final class ColdCareGameTest {
    private ColdCareGameTest() {}
    private static int count(GameTestHelper h, Item item) {
        var low = h.absolutePos(BlockPos.ZERO); var high = h.absolutePos(new BlockPos(8,8,8));
        return h.getLevel().getEntitiesOfClass(ItemEntity.class,new AABB(low.getX(),low.getY(),low.getZ(),high.getX(),high.getY(),high.getZ()),e -> e.getItem().is(item))
                .stream().mapToInt(e -> e.getItem().getCount()).sum();
    }
    private static void pulse(GameTestHelper h, BlockPos pos, Runnable after) {
        h.getLevel().setBlock(pos.west(),Blocks.REDSTONE_BLOCK.defaultBlockState(),3);
        h.runAfterDelay(6,() -> { h.getLevel().setBlock(pos.west(),Blocks.AIR.defaultBlockState(),3); after.run(); });
    }
    public static void run(GameTestHelper h) {
        var level = h.getLevel();
        for (int x = 0; x < 8; x++) for (int z = 0; z < 8; z++) {
            h.setBlock(x,0,z,Blocks.STONE);
            for (int y = 1; y <= 5; y++) h.setBlock(x,y,z,x == 0 || x == 7 || z == 0 || z == 7 ? Blocks.STONE : Blocks.AIR);
        }
        var dog = h.spawnWithNoFreeWill(ModEntities.BUNNIDOG.get(),2.5F,1,3.5F);
        int snow = Gene.COAT.alleleNamed("snow").id(); var genome = Genome.DEFAULT.with(Gene.COAT,0,snow).with(Gene.COAT,1,snow);
        dog.setGenome(genome); dog.setAge(0); dog.setStats(Stats.of(0,0,0,0,10)); dog.setHealth(dog.getMaxHealth());
        h.setBiome(Biomes.PLAINS);
        h.assertTrue(!BunnidogCare.canGroom(dog),"Snow coats must not create frost in warm climates");
        h.setBiome(Biomes.SNOWY_PLAINS); dog.setStats(Stats.of(0,0,0,0,9));
        h.assertTrue(!BunnidogCare.canGroom(dog),"Cold snow coats below Bloom ten must refuse without recovery");
        dog.setStats(Stats.of(0,0,0,0,10)); dog.setGenome(genome.with(Gene.COAT,1,Gene.COAT.alleleNamed("spotted").id()));
        h.assertTrue(!BunnidogCare.canGroom(dog),"Hidden snow must not yield frost"); dog.setGenome(genome);
        var player = h.makeMockPlayer(GameType.SURVIVAL); var brush = new ItemStack(Items.BRUSH); player.setItemInHand(InteractionHand.OFF_HAND,brush);
        float health = dog.getHealth(); player.interactOn(dog,InteractionHand.OFF_HAND,Vec3.ZERO);
        int snowballs = 0;
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) if (player.getInventory().getItem(slot).is(Items.SNOWBALL)) snowballs += player.getInventory().getItem(slot).getCount();
        h.assertTrue(snowballs == 1 && brush.getDamageValue() == 1 && dog.groomingWait() == Grooming.RECOVERY_TICKS
                && dog.getHealth() == health && dog.genome().equals(genome),"Native off-hand brushing must give one frost product for one wear/shared rest without harm");
        player.interactOn(dog,InteractionHand.OFF_HAND,Vec3.ZERO);
        h.assertTrue(brush.getDamageValue() == 1 && !BunnidogCare.canGroom(dog),"Repeated frost care must not bypass recovery");
        var worker = h.spawnWithNoFreeWill(ModEntities.BUNNIDOG.get(),5.5F,1,3.5F);
        int woolly = Gene.BUILD.alleleNamed("woolly").id(), star = Gene.FERTILITY.alleleNamed("starborn").id();
        worker.setGenome(genome.with(Gene.BUILD,0,woolly).with(Gene.BUILD,1,woolly).with(Gene.FERTILITY,0,star).with(Gene.FERTILITY,1,star));
        worker.setAge(0); worker.setStats(Stats.of(0,0,0,10,20)); worker.setHealth(worker.getMaxHealth());
        var source = h.absolutePos(new BlockPos(4,1,3)); level.setBlock(source,Blocks.DISPENSER.defaultBlockState().setValue(DispenserBlock.FACING,Direction.EAST),3);
        var dispenser = (DispenserBlockEntity)level.getBlockEntity(source); var stationBrush = new ItemStack(Items.BRUSH); dispenser.setItem(0,stationBrush);
        pulse(h,source,() -> {
            h.assertTrue(count(h,Items.SNOWBALL) == 1 && count(h,ModItems.WHISKER.get()) == 1 && count(h,ModItems.STARLIT_DUST.get()) == 1
                    && stationBrush.getDamageValue() == 1 && worker.groomingWait() > 0 && worker.fleeceWait() == 0,
                    "Native dispenser frost, whiskers and Starlit dust must share one brush cost while keeping fleece readiness");
            var shears = new ItemStack(Items.SHEARS); dispenser.setItem(0,shears);
            pulse(h,source,() -> {
                h.assertTrue(count(h,Items.WOOL.pick(DyeColor.WHITE)) == 2 && shears.getDamageValue() == 1
                        && worker.fleeceWait() > 0 && worker.groomingWait() > 0,"Native IShearable dispenser path must shear two coat-colored wool independently of grooming rest");
                pulse(h,source,() -> {
                    h.assertTrue(count(h,Items.WOOL.pick(DyeColor.WHITE)) == 2 && shears.getDamageValue() == 1,"Repeat native shearing must keep tool and products unchanged");
                    h.succeed();
                });
            });
        });
    }
}
