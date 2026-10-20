package com.bunnidogs.mod.entity;

import java.util.function.Consumer;
import com.bunnidogs.mod.block.NurseryCradleBlock;
import com.bunnidogs.mod.block.NurseryCradleBlockEntity;
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
import net.minecraft.util.TriState;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

/** Native off-hand/creative controls and real queued/rising-edge redstone keep the exact litter. */
public final class CradleControlsGameTest {
    private CradleControlsGameTest() {}
    public static void run(GameTestHelper h) {
        var level = h.getLevel();
        for (int x = 0; x < 8; x++) for (int z = 0; z < 8; z++) {
            h.setBlock(x,0,z,Blocks.STONE); for (int y = 1; y < 6; y++) h.setBlock(x,y,z,Blocks.AIR);
        }
        var pos = h.absolutePos(new BlockPos(3,1,4));
        level.setBlock(pos,ModBlocks.NURSERY_CRADLE.get().defaultBlockState().setValue(NurseryCradleBlock.FACING,Direction.NORTH),3);
        var cradle = (NurseryCradleBlockEntity)level.getBlockEntity(pos);
        var seed = SeedItem.create(new Seed(Genome.DEFAULT,Stats.of(1,2,3,4,5),Genome.DEFAULT,Stats.ZERO,1).withMutagen());
        seed.set(DataComponents.CUSTOM_NAME,Component.literal("Keep this litter")); cradle.insert(seed); cradle.grow(false);
        int age = cradle.age(), wait = cradle.growthWait(); var area = new AABB(pos).inflate(5);
        var player = h.makeMockServerPlayerInLevel(); player.gameMode.changeGameModeForPlayer(GameType.CREATIVE);
        var hoe = new ItemStack(Items.IRON_HOE); hoe.setDamageValue(hoe.getMaxDamage()-1); player.setItemInHand(InteractionHand.OFF_HAND,hoe); player.setShiftKeyDown(true);
        var hit = new BlockHitResult(Vec3.atCenterOf(pos),Direction.UP,pos,false);
        h.assertTrue(!cradle.manualHatchOnly(),"New Cradles preserve ordinary redstone hatching");
        player.gameMode.useItemOn(player,level,hoe,InteractionHand.OFF_HAND,hit);
        h.assertTrue(cradle.manualHatchOnly() && ItemStack.matches(cradle.storedSeed(),seed) && cradle.age() == age && cradle.growthWait() == wait
                && player.getOffhandItem() == hoe && hoe.getDamageValue() == hoe.getMaxDamage()-1,"Actual crouched creative off-hand hoe must change only the preference, without wear");
        var saved = cradle.saveWithFullMetadata(level.registryAccess());
        var restored = (NurseryCradleBlockEntity)BlockEntity.loadStatic(pos,cradle.getBlockState(),saved,level.registryAccess());
        h.assertTrue(restored.manualHatchOnly() && ItemStack.matches(restored.storedSeed(),seed) && restored.age() == age && restored.growthWait() == wait,"Native saves preserve manual mode and the whole named treated litter/incubation");
        saved.remove("ManualHatchOnly"); var legacy = (NurseryCradleBlockEntity)BlockEntity.loadStatic(pos,cradle.getBlockState(),saved,level.registryAccess());
        h.assertTrue(!legacy.manualHatchOnly(),"Legacy Cradles retain their redstone behavior");
        player.gameMode.changeGameModeForPlayer(GameType.ADVENTURE); player.gameMode.useItemOn(player,level,hoe,InteractionHand.OFF_HAND,hit);
        h.assertTrue(cradle.manualHatchOnly(),"Adventure cannot change the Cradle preference"); player.gameMode.changeGameModeForPlayer(GameType.SURVIVAL);
        Consumer<PlayerInteractEvent.RightClickBlock> protection = event -> { if (event.getEntity() == player && event.getPos().equals(pos)) event.setUseBlock(TriState.FALSE); };
        NeoForge.EVENT_BUS.addListener(protection);
        try { player.gameMode.useItemOn(player,level,hoe,InteractionHand.OFF_HAND,hit); } finally { NeoForge.EVENT_BUS.unregister(protection); }
        h.assertTrue(cradle.manualHatchOnly(),"Native block-use protection must remain stronger than the crouched control");
        var calipers = new ItemStack(ModItems.CALIPERS.get()); player.setItemInHand(InteractionHand.OFF_HAND,calipers); player.gameMode.useItemOn(player,level,calipers,InteractionHand.OFF_HAND,hit);
        h.assertTrue(cradle.manualHatchOnly() && ItemStack.matches(cradle.storedSeed(),seed) && cradle.age() == age && cradle.growthWait() == wait,"Sneak-Calipers readout must not change the mode, litter or incubation");
        player.setItemInHand(InteractionHand.OFF_HAND,hoe); cradle.serverTick(level);
        h.assertTrue(cradle.growthWait() == wait-1 && cradle.manualHatchOnly(),"Incubation keeps advancing in manual-only mode"); cradle.grow(true);
        level.setBlock(pos.west(),Blocks.REDSTONE_BLOCK.defaultBlockState(),3);
        h.runAfterDelay(3,() -> {
            h.assertTrue(ItemStack.matches(cradle.storedSeed(),seed) && cradle.age() == 7 && cradle.stockSignal() == 15
                    && level.getEntitiesOfClass(Bunnidog.class,area,Bunnidog::isBaby).isEmpty(),"A powered manual-only Cradle must retain a ripe litter and comparator signal");
            player.gameMode.useItemOn(player,level,hoe,InteractionHand.OFF_HAND,hit); h.assertTrue(!cradle.manualHatchOnly(),"The same gesture re-enables redstone");
            h.runAfterDelay(2,() -> {
                h.assertTrue(ItemStack.matches(cradle.storedSeed(),seed),"Re-enabling under steady power must not synthesize a fresh edge");
                level.removeBlock(pos.west(),false); level.setBlock(pos.west(),Blocks.REDSTONE_BLOCK.defaultBlockState(),3);
                player.gameMode.useItemOn(player,level,hoe,InteractionHand.OFF_HAND,hit);
                h.runAfterDelay(3,() -> {
                    h.assertTrue(cradle.manualHatchOnly() && ItemStack.matches(cradle.storedSeed(),seed)
                            && level.getEntitiesOfClass(Bunnidog.class,area,Bunnidog::isBaby).isEmpty(),"Switching to manual-only must cancel an already queued redstone hatch");
                    level.removeBlock(pos.west(),false); player.setShiftKeyDown(false); player.setItemInHand(InteractionHand.OFF_HAND,ItemStack.EMPTY);
                    CareMenuGameTest.click(h,player,pos,com.bunnidogs.mod.menu.CarePanels.HATCH);
                    var babies = level.getEntitiesOfClass(Bunnidog.class,area,Bunnidog::isBaby);
                    h.assertTrue(cradle.storedSeed().isEmpty() && cradle.manualHatchOnly() && !babies.isEmpty() && babies.stream().noneMatch(Bunnidog::workPaused),"Deliberate menu use still hatches safely; keeper preferences are not inherited");
                    babies.forEach(Bunnidog::discard); cradle.insert(seed); cradle.grow(true); player.setShiftKeyDown(true); player.setItemInHand(InteractionHand.OFF_HAND,hoe);
                    player.gameMode.useItemOn(player,level,hoe,InteractionHand.OFF_HAND,hit); level.setBlock(pos.west(),Blocks.REDSTONE_BLOCK.defaultBlockState(),3);
                    h.runAfterDelay(3,() -> {
                        h.assertTrue(!cradle.manualHatchOnly() && cradle.storedSeed().isEmpty() && !level.getEntitiesOfClass(Bunnidog.class,area,Bunnidog::isBaby).isEmpty(),"After re-enabling, a fresh real edge resumes the original redstone hatch path"); h.succeed();
                    });
                });
            });
        });
    }
}
