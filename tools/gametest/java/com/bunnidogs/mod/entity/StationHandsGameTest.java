package com.bunnidogs.mod.entity;

import com.bunnidogs.mod.block.FeedingTroughBlockEntity;
import com.bunnidogs.mod.block.JuicerBlockEntity;
import com.bunnidogs.mod.block.NurseryCradleBlockEntity;
import com.bunnidogs.mod.genetics.Genome;
import com.bunnidogs.mod.genetics.Seed;
import com.bunnidogs.mod.genetics.Stats;
import com.bunnidogs.mod.item.SeedItem;
import com.bunnidogs.mod.menu.CareMenu;
import com.bunnidogs.mod.registry.ModBlocks;
import com.bunnidogs.mod.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

/** Real block dispatch with ordinary held materials, purposeful off-hand stock and native crouch placement. */
public final class StationHandsGameTest {
    private StationHandsGameTest() {}
    private static void use(GameTestHelper h,ServerPlayer player,BlockPos pos,InteractionHand hand) {
        player.snapTo(Vec3.atCenterOf(pos).add(0,0,2));
        player.gameMode.useItemOn(player,h.getLevel(),player.getItemInHand(hand),hand,new BlockHitResult(Vec3.atCenterOf(pos),Direction.UP,pos,false));
    }
    public static void run(GameTestHelper h) {
        var player=h.makeMockServerPlayerInLevel(); CareMenuGameTest.prepare(player);
        player.gameMode.changeGameModeForPlayer(GameType.SURVIVAL);
        var blocks=java.util.List.of(ModBlocks.FEEDING_TROUGH.get(),ModBlocks.BREEDING_PEN.get(),ModBlocks.NURSERY_CRADLE.get(),
                ModBlocks.JUICER.get(),ModBlocks.GENETICS_TABLE.get(),ModBlocks.COMMISSION_BOARD.get());
        var stone=new ItemStack(Items.STONE,32); player.setItemInHand(InteractionHand.MAIN_HAND,stone);
        for (int i=0;i<blocks.size();i++) {
            var pos=h.absolutePos(new BlockPos(2+i%2*3,165,1+i/2*2));
            h.getLevel().setBlock(pos,blocks.get(i).defaultBlockState(),3);
            use(h,player,pos,InteractionHand.MAIN_HAND);
            h.assertTrue(player.containerMenu instanceof CareMenu && stone.getCount()==32 && h.getLevel().isEmptyBlock(pos.above()),
                    "Ordinary held blocks inspect a station without placing or spending: "+blocks.get(i));
            player.closeContainer();
        }
        var troughPos=h.absolutePos(new BlockPos(2,165,1));
        player.gameMode.changeGameModeForPlayer(GameType.ADVENTURE); use(h,player,troughPos,InteractionHand.MAIN_HAND);
        h.assertTrue(player.containerMenu instanceof CareMenu && stone.getCount()==32,"Adventure may inspect with ordinary held items");
        player.closeContainer(); player.gameMode.changeGameModeForPlayer(GameType.SURVIVAL);
        player.setShiftKeyDown(true); use(h,player,troughPos,InteractionHand.MAIN_HAND); player.setShiftKeyDown(false);
        h.assertTrue(!(player.containerMenu instanceof CareMenu) && stone.getCount()==31 && h.getLevel().getBlockState(troughPos.above()).is(Blocks.STONE),
                "Crouch placement keeps Minecraft's normal item priority");
        h.getLevel().setBlock(troughPos.above(),Blocks.AIR.defaultBlockState(),3);
        player.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);
        var guava=new ItemStack(ModItems.GUAVA.get(),3); player.setItemInHand(InteractionHand.OFF_HAND,guava);
        use(h,player,troughPos,InteractionHand.MAIN_HAND); use(h,player,troughPos,InteractionHand.OFF_HAND);
        h.assertTrue(!(player.containerMenu instanceof CareMenu) && guava.isEmpty()
                && ((FeedingTroughBlockEntity)h.getLevel().getBlockEntity(troughPos)).food().getCount()==3,
                "A real off-hand meal still stocks the Trough directly");
        var cradlePos=h.absolutePos(new BlockPos(2,165,3));
        var seed=SeedItem.create(Seed.of(Genome.DEFAULT,Stats.ZERO,Genome.DEFAULT,Stats.ZERO,1));
        player.setItemInHand(InteractionHand.OFF_HAND,seed); use(h,player,cradlePos,InteractionHand.MAIN_HAND); use(h,player,cradlePos,InteractionHand.OFF_HAND);
        h.assertTrue(!(player.containerMenu instanceof CareMenu) && seed.isEmpty()
                && ((NurseryCradleBlockEntity)h.getLevel().getBlockEntity(cradlePos)).seed().isPresent(),"Off-hand seeds retain direct Cradle insertion");
        var pressPos=h.absolutePos(new BlockPos(5,165,3));
        var bottles=new ItemStack(Items.GLASS_BOTTLE,2); player.setItemInHand(InteractionHand.OFF_HAND,bottles);
        use(h,player,pressPos,InteractionHand.MAIN_HAND); use(h,player,pressPos,InteractionHand.OFF_HAND);
        h.assertTrue(!(player.containerMenu instanceof CareMenu) && bottles.isEmpty()
                && ((JuicerBlockEntity)h.getLevel().getBlockEntity(pressPos)).item(1).getCount()==2,"Ordinary bottle items remain paid ingredients, not inspection clicks");
        player.setItemInHand(InteractionHand.OFF_HAND,ItemStack.EMPTY); player.setItemInHand(InteractionHand.MAIN_HAND,stone);
        var nestPos=h.absolutePos(new BlockPos(2,165,7)); h.getLevel().setBlock(nestPos,ModBlocks.NEST.get().defaultBlockState(),3);
        use(h,player,nestPos,InteractionHand.MAIN_HAND);
        h.assertTrue(!(player.containerMenu instanceof CareMenu) && stone.getCount()==31 && h.getLevel().isEmptyBlock(nestPos.above()),"Nests remain direct world interactions with ordinary held materials");
    }
}
