package com.bunnidogs.mod.entity;

import java.util.function.Consumer;
import com.bunnidogs.mod.genetics.Gene;
import com.bunnidogs.mod.genetics.Genome;
import com.bunnidogs.mod.genetics.Stats;
import com.bunnidogs.mod.item.TravelBasketItem;
import com.bunnidogs.mod.registry.ModBlocks;
import com.bunnidogs.mod.registry.ModEntities;
import com.bunnidogs.mod.registry.ModItems;
import com.bunnidogs.mod.registry.ModPoi;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.entity.DispenserBlockEntity;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;

/** Real redstone pulses exercise whole-animal transport and native reentrant source changes. */
public final class BasketDispenserGameTest {
    private BasketDispenserGameTest() {}
    private static BlockPos setup(GameTestHelper h) {
        for (int x = 0; x < 8; x++) for (int z = 0; z < 8; z++) {
            h.setBlock(x,0,z,Blocks.STONE);
            for (int y = 1; y < 6; y++) h.setBlock(x,y,z,x == 0 || x == 7 || z == 0 || z == 7 ? Blocks.STONE : Blocks.AIR);
        }
        var pos = h.absolutePos(new BlockPos(3,1,3)); machine(h,pos); return pos;
    }
    private static DispenserBlockEntity machine(GameTestHelper h, BlockPos pos) {
        h.getLevel().setBlock(pos,Blocks.DISPENSER.defaultBlockState().setValue(DispenserBlock.FACING,Direction.EAST),3);
        return (DispenserBlockEntity)h.getLevel().getBlockEntity(pos);
    }
    private static AABB area(BlockPos pos) { return new AABB(pos).inflate(6); }
    private static void pulse(GameTestHelper h, BlockPos pos, Runnable after) {
        h.getLevel().setBlock(pos.west(),Blocks.REDSTONE_BLOCK.defaultBlockState(),3);
        h.runAfterDelay(6,() -> { h.getLevel().removeBlock(pos.west(),false); after.run(); });
    }
    public static void run(GameTestHelper h) {
        var level = h.getLevel(); var pos = setup(h); var front = pos.east();
        var inventory = (DispenserBlockEntity)level.getBlockEntity(pos);
        h.setBlock(1,1,1,ModBlocks.NEST.get());
        var dog = h.spawnWithNoFreeWill(ModEntities.BUNNIDOG.get(),4.5F,1,3.5F); dog.setAge(1200);
        int woolly = Gene.BUILD.alleleNamed("woolly").id(), glow = Gene.COAT.alleleNamed("glow").id();
        var genome = Genome.DEFAULT.with(Gene.BUILD,0,woolly).with(Gene.BUILD,1,woolly).with(Gene.COAT,0,glow).with(Gene.COAT,1,glow);
        dog.setGenome(genome); dog.setStats(Stats.of(7,8,9,10,20)); dog.setParents(Genome.DEFAULT,Genome.DEFAULT);
        dog.setCustomName(Component.literal("Juniper station")); dog.applyEarTag(); dog.setHealth(dog.getMaxHealth()-1);
        var basket = new ItemStack(ModItems.TRAVEL_BASKET.get()); basket.set(DataComponents.CUSTOM_NAME,Component.literal("Named carrier")); inventory.setItem(0,basket);
        var identity = dog.getUUID(); var stats = dog.stats(); float health = dog.getHealth();
        h.runAfterDelay(2,() -> {
            var home = h.absolutePos(new BlockPos(1,1,1));
            h.assertTrue(dog.moveHomeTo(level,home) == NestHome.MoveResult.MOVED,"Dispenser transport needs a real old home");
            var player = h.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL); player.setPos(dog.position()); dog.setLeashedTo(player,false);
            pulse(h,pos,() -> {
                h.assertTrue(!TravelBasketItem.occupied(inventory.getItem(0)) && !dog.isRemoved(),"A led resident must refuse machine capture");
                dog.removeLeash(); dog.beginSamplingRecovery(); dog.beginGroomingRecovery(); dog.beginSplicingRecovery(); dog.finishedGardening(); dog.ateForage(0);
                pulse(h,pos,() -> {
                    var packed = inventory.getItem(0); var payload = packed.get(DataComponents.BUCKET_ENTITY_DATA); var duplicate = packed.copy();
                    h.assertTrue(dog.isRemoved() && TravelBasketItem.occupied(packed) && packed.getCount() == 1
                            && packed.getHoverName().getString().equals("Named carrier") && TravelBasketItem.readEntry(packed).orElseThrow().id().orElseThrow().equals(identity)
                            && level.getPoiManager().getCountInRange(holder -> holder.is(ModPoi.NEST.getKey()),home,0,PoiManager.Occupancy.HAS_SPACE) == 1,
                            "One pulse must pack the exact named identity and free its old Nest");
                    level.setBlock(front,Blocks.STONE.defaultBlockState(),3);
                    pulse(h,pos,() -> {
                        h.assertTrue(payload.equals(inventory.getItem(0).get(DataComponents.BUCKET_ENTITY_DATA)),"Blocked release must retain the whole occupied basket");
                        for (var direction : new Direction[]{Direction.NORTH,Direction.SOUTH,Direction.EAST}) level.setBlock(front.relative(direction),Blocks.STONE.defaultBlockState(),3);
                        level.setBlock(front,Blocks.WATER.defaultBlockState(),3);
                        pulse(h,pos,() -> {
                            h.assertTrue(payload.equals(inventory.getItem(0).get(DataComponents.BUCKET_ENTITY_DATA)),"Wet release must retain the animal and every paused timer");
                            level.setBlock(front,Blocks.AIR.defaultBlockState(),3); for (var direction : new Direction[]{Direction.NORTH,Direction.SOUTH,Direction.EAST}) level.removeBlock(front.relative(direction),false);
                            pulse(h,pos,() -> {
                                var restored = (Bunnidog)level.getEntityInAnyDimension(identity);
                                h.assertTrue(restored != null && !TravelBasketItem.occupied(inventory.getItem(0)) && inventory.getItem(0).getCount() == 1
                                        && restored.genome().equals(genome) && restored.stats().equals(stats) && restored.getHealth() == health
                                        && restored.getName().getString().equals("Juniper station") && restored.homeNest() == null
                                        && restored.samplingWait() > 5900 && restored.groomingWait() > 11900 && restored.splicingWait() > 1190
                                        && restored.gardeningWait() > 1190 && restored.forageWait() > 590,
                                        "One pulse must restore the same cared-for resident without resetting its line, health or paused recoveries: restored="+restored+", basket="+inventory.getItem(0)+", health="+(restored == null ? -1 : restored.getHealth())+"/"+health+", home="+(restored == null ? null : restored.homeNest())+", waits="+(restored == null ? "missing" : restored.samplingWait()+","+restored.groomingWait()+","+restored.splicingWait()+","+restored.gardeningWait()+","+restored.forageWait()));
                                inventory.setItem(0,duplicate);
                                pulse(h,pos,() -> {
                                    h.assertTrue(payload.equals(inventory.getItem(0).get(DataComponents.BUCKET_ENTITY_DATA))
                                            && level.getEntitiesOfClass(Bunnidog.class,area(pos),e -> e.getUUID().equals(identity)).size() == 1,
                                            "A copied full basket must retain its data and refuse an existing live identity");
                                    var invalid = new CompoundTag(); invalid.putInt("BunnidogsBasket",1); var malformed = new ItemStack(ModItems.TRAVEL_BASKET.get());
                                    malformed.set(DataComponents.BUCKET_ENTITY_DATA,CustomData.of(invalid)); inventory.setItem(0,malformed);
                                    pulse(h,pos,() -> {
                                        h.assertTrue(inventory.getItem(0).get(DataComponents.BUCKET_ENTITY_DATA).copyTag().equals(invalid)
                                                && level.getEntitiesOfClass(ItemEntity.class,area(pos),e -> e.getItem().is(ModItems.TRAVEL_BASKET.get())).isEmpty(),
                                                "Malformed machine stock must be retained without ejection or occupant loss"); h.succeed();
                                    });
                                });
                            });
                        });
                    });
                });
            });
        });
    }
    public static void callbacks(GameTestHelper h) {
        var level = h.getLevel(); var pos = setup(h); var inventory = (DispenserBlockEntity)level.getBlockEntity(pos);
        var dog = h.spawnWithNoFreeWill(ModEntities.BUNNIDOG.get(),4.5F,1,3.5F); dog.setAge(0); dog.setGenome(Genome.DEFAULT); dog.setHealth(dog.getMaxHealth());
        var identity = dog.getUUID(); var basket = new ItemStack(ModItems.TRAVEL_BASKET.get());
        h.assertTrue(TravelBasketItem.capture(level,basket,dog) == TravelBasketItem.Result.CAPTURED,"Callback fixture must hold one real animal");
        var payload = basket.get(DataComponents.BUCKET_ENTITY_DATA); var reentrant = basket.copy(); inventory.setItem(0,basket);
        boolean[] guarded = {false};
        Consumer<EntityJoinLevelEvent> reject = event -> {
            if (event.getLevel() != level || !(event.getEntity() instanceof Bunnidog animal) || !animal.getUUID().equals(identity)) return;
            guarded[0] = TravelBasketItem.release(level,pos.south(2),reentrant) == TravelBasketItem.Result.DUPLICATE && inventory.getItem(0).isEmpty();
            level.removeBlock(pos,false); event.setCanceled(true);
        };
        NeoForge.EVENT_BUS.addListener(reject);
        pulse(h,pos,() -> {
            NeoForge.EVENT_BUS.unregister(reject);
            var drops = level.getEntitiesOfClass(ItemEntity.class,area(pos),e -> e.getItem().is(ModItems.TRAVEL_BASKET.get()));
            h.assertTrue(guarded[0] && level.getEntityInAnyDimension(identity) == null && drops.size() == 1 && drops.getFirst().getItem().getCount() == 1
                    && payload.equals(drops.getFirst().getItem().get(DataComponents.BUCKET_ENTITY_DATA)),
                    "Removing the source during a refused spawn must refund exactly one full basket and prevent reentrant duplicate release");
            var retry = drops.getFirst().getItem().copy(); drops.forEach(ItemEntity::discard); var replacement = machine(h,pos); replacement.setItem(0,retry);
            Consumer<EntityJoinLevelEvent> refill = event -> {
                if (event.getLevel() == level && event.getEntity() instanceof Bunnidog animal && animal.getUUID().equals(identity)) replacement.setItem(0,new ItemStack(Items.DIAMOND,3));
            };
            NeoForge.EVENT_BUS.addListener(refill);
            pulse(h,pos,() -> {
                NeoForge.EVENT_BUS.unregister(refill);
                var empty = level.getEntitiesOfClass(ItemEntity.class,area(pos),e -> e.getItem().is(ModItems.TRAVEL_BASKET.get()));
                h.assertTrue(replacement.getItem(0).is(Items.DIAMOND) && replacement.getItem(0).getCount() == 3
                        && empty.size() == 1 && empty.getFirst().getItem().getCount() == 1 && !TravelBasketItem.occupied(empty.getFirst().getItem())
                        && level.getEntitiesOfClass(Bunnidog.class,area(pos),e -> e.getUUID().equals(identity)).size() == 1,
                        "A successful spawn callback refilling the source must retain new stock, return one empty basket and spawn exactly once"); h.succeed();
            });
        });
    }
}
