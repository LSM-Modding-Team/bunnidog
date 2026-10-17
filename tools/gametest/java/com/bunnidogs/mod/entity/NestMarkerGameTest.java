package com.bunnidogs.mod.entity;

import java.util.Optional;
import com.bunnidogs.mod.genetics.Genome;
import com.bunnidogs.mod.registry.ModBlocks;
import com.bunnidogs.mod.registry.ModEntities;
import com.bunnidogs.mod.registry.ModPoi;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.LodestoneTracker;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public final class NestMarkerGameTest {
    private NestMarkerGameTest() {}
    private static void use(ServerPlayer player, BlockPos pos, InteractionHand hand) {
        player.gameMode.useItemOn(player,player.level(),player.getItemInHand(hand),hand,
                new BlockHitResult(Vec3.atCenterOf(pos),Direction.UP,pos,false));
    }
    public static void run(GameTestHelper h) {
        var level = h.getLevel();
        for (int x = 0; x < 8; x++) for (int z = 0; z < 8; z++) h.setBlock(x,0,z,Blocks.STONE);
        h.setBlock(1,1,1,ModBlocks.NEST.get()); h.setBlock(6,1,1,ModBlocks.NEST.get());
        var first = h.absolutePos(new BlockPos(1,1,1)); var second = h.absolutePos(new BlockPos(6,1,1));
        var player = h.makeMockServerPlayerInLevel();
        GameType.SURVIVAL.updatePlayerAbilities(player.getAbilities());
        var dog = h.spawnWithNoFreeWill(ModEntities.BUNNIDOG.get(),3.5F,1,3.5F);
        dog.setGenome(Genome.DEFAULT); dog.setAge(0);
        h.runAfterDelay(2,() -> {
            var pois = level.getPoiManager();
            use(player,first,InteractionHand.MAIN_HAND);
            h.assertTrue(pois.getCountInRange(holder -> holder.is(ModPoi.NEST.getKey()),first,0,PoiManager.Occupancy.HAS_SPACE) == 1,
                    "Empty Nest inspection must leave its native ticket free");
            h.assertTrue(dog.moveHomeTo(level,first) == NestHome.MoveResult.MOVED,"The inspected Nest must still accept its first resident");
            use(player,first,InteractionHand.MAIN_HAND);
            h.assertTrue(first.equals(dog.homeNest()) && pois.getCountInRange(holder -> holder.is(ModPoi.NEST.getKey()),first,0,PoiManager.Occupancy.IS_OCCUPIED) == 1,
                    "Occupied inspection must preserve its resident and exact reservation");
            var compasses = new ItemStack(Items.COMPASS,3);
            compasses.set(DataComponents.CUSTOM_NAME,Component.literal("Warren route"));
            player.setItemInHand(InteractionHand.MAIN_HAND,compasses);
            use(player,first,InteractionHand.MAIN_HAND);
            var expected = new LodestoneTracker(Optional.of(GlobalPos.of(level.dimension(),first)),false);
            int markedSlot = -1;
            for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++)
                if (expected.equals(player.getInventory().getItem(slot).get(DataComponents.LODESTONE_TRACKER))) markedSlot = slot;
            h.assertTrue(markedSlot >= 0 && compasses.getCount() == 2 && !compasses.has(DataComponents.LODESTONE_TRACKER)
                    && player.getInventory().getItem(markedSlot).getCount() == 1,"A survival stack must spend exactly one compass to make one marker");
            var marked = player.getInventory().removeItem(markedSlot,1);
            player.setItemInHand(InteractionHand.OFF_HAND,marked);
            use(player,second,InteractionHand.OFF_HAND);
            var rebound = new LodestoneTracker(Optional.of(GlobalPos.of(level.dimension(),second)),false);
            h.assertTrue(rebound.equals(marked.get(DataComponents.LODESTONE_TRACKER)) && marked.getHoverName().getString().equals("Warren route")
                    && compasses.getCount() == 2,"Rebinding the actual off-hand compass must preserve its name and other hand");
            var output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING,level.registryAccess());
            output.store("Compass",ItemStack.CODEC,marked);
            var restored = TagValueInput.create(ProblemReporter.DISCARDING,level.registryAccess(),output.buildResult()).read("Compass",ItemStack.CODEC).orElseThrow();
            level.setBlock(second,Blocks.AIR.defaultBlockState(),3);
            restored.getItem().inventoryTick(restored,level,player,EquipmentSlot.OFFHAND);
            h.assertTrue(rebound.equals(restored.get(DataComponents.LODESTONE_TRACKER)),"Native saved markers must keep their coordinates when the Nest is removed");
            var creative = h.makeMockServerPlayerInLevel(); GameType.CREATIVE.updatePlayerAbilities(creative.getAbilities());
            var single = new ItemStack(Items.COMPASS); creative.setItemInHand(InteractionHand.MAIN_HAND,single);
            use(creative,first,InteractionHand.MAIN_HAND);
            h.assertTrue(expected.equals(creative.getMainHandItem().get(DataComponents.LODESTONE_TRACKER))
                    && creative.getInventory().countItem(Items.COMPASS) == 1,"A single creative compass must bind on the actual held stack without a spare");
            var denied = h.makeMockServerPlayerInLevel(); GameType.ADVENTURE.updatePlayerAbilities(denied.getAbilities());
            var unchanged = new ItemStack(Items.COMPASS); denied.setItemInHand(InteractionHand.MAIN_HAND,unchanged);
            use(denied,first,InteractionHand.MAIN_HAND);
            h.assertTrue(!unchanged.has(DataComponents.LODESTONE_TRACKER),"Denied build permission must preserve an unbound compass");
            var full = h.makeMockServerPlayerInLevel(); GameType.SURVIVAL.updatePlayerAbilities(full.getAbilities());
            full.setPos(Vec3.atBottomCenterOf(h.absolutePos(new BlockPos(3,1,4))));
            for (int slot = 0; slot < 36; slot++) full.getInventory().setItem(slot,new ItemStack(Items.STONE,64));
            var extra = new ItemStack(Items.COMPASS,2); full.setItemInHand(InteractionHand.MAIN_HAND,extra);
            use(full,first,InteractionHand.MAIN_HAND);
            var drops = level.getEntitiesOfClass(ItemEntity.class,new AABB(full.blockPosition()).inflate(2),
                    item -> expected.equals(item.getItem().get(DataComponents.LODESTONE_TRACKER)));
            h.assertTrue(extra.getCount() == 1 && drops.stream().mapToInt(item -> item.getItem().getCount()).sum() == 1,
                    "A full survival inventory must drop exactly the one marked compass");
            h.assertTrue(first.equals(dog.homeNest()),"Marking, re-marking and overflow must not move the resident");
            h.succeed();
        });
    }
}
