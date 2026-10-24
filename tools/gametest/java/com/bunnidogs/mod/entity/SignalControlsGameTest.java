package com.bunnidogs.mod.entity;

import com.bunnidogs.mod.block.*;
import com.bunnidogs.mod.genetics.*;
import com.bunnidogs.mod.item.TissueSampleItem;
import com.bunnidogs.mod.menu.*;
import com.bunnidogs.mod.registry.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundContainerButtonClickPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public final class SignalControlsGameTest {
    private SignalControlsGameTest() {}
    private static ServerPlayer player(GameTestHelper h) {
        var p=h.makeMockServerPlayerInLevel(); p.gameMode.changeGameModeForPlayer(GameType.SURVIVAL); CareMenuGameTest.prepare(p); return p;
    }
    private static void press(GameTestHelper h,ServerPlayer player,CareMenu menu,int action) {
        if (com.bunnidogs.mod.menu.StationInventory.transferButton(action)) { CareMenuGameTest.transfer(h,player,menu,action); return; }
        int button=menu.buttonId(action); h.assertTrue(menu.view().permits(button),"The displayed signal control is enabled: "+action);
        player.connection.handleContainerButtonClick(new ServerboundContainerButtonClickPacket(menu.containerId,button));
    }
    private static boolean owns(ServerPlayer player,ItemStack item) {
        for (int i=0;i<player.getInventory().getContainerSize();i++) if (ItemStack.matches(player.getInventory().getItem(i),item)) return true;
        return false;
    }
    public static void run(GameTestHelper h) {
        int y=374; var level=h.getLevel();
        for (int x=0;x<14;x++) for (int z=0;z<10;z++) { h.setBlock(x,y,z,Blocks.STONE); for (int up=1;up<=4;up++) h.setBlock(x,y+up,z,Blocks.AIR); }
        var p=player(h); var pos=h.absolutePos(new BlockPos(3,y+1,3)); level.setBlock(pos,ModBlocks.SPECIMEN_JAR.get().defaultBlockState(),3);
        var jar=(SpecimenJarBlockEntity)level.getBlockEntity(pos); var source=TissueSampleItem.create(Genome.DEFAULT,Stats.of(1,2,3,4,5));
        source.set(DataComponents.CUSTOM_NAME,Component.literal("Family museum original")); var original=source.copy(); p.getInventory().setItem(9,source);
        var saved=jar.saveWithFullMetadata(level.registryAccess()); var menu=CareMenuGameTest.open(h,p,pos);
        h.assertTrue(saved.equals(jar.saveWithFullMetadata(level.registryAccess())) && owns(p,original),"Opening the Jar must preserve both empty storage and real inventory");
        press(h,p,menu,SignalPanels.JAR_ADD);
        h.assertTrue(ItemStack.matches(jar.sample(),original) && !owns(p,original) && jar.fixedSignal()==15,"Preserve-next pays the actual named sample once");
        CohesionGameTest.preview("Preserved sample",menu); saved=jar.saveWithFullMetadata(level.registryAccess());
        p.gameMode.changeGameModeForPlayer(GameType.ADVENTURE); menu=CareMenuGameTest.open(h,p,pos);
        menu.clicked(0,0,net.minecraft.world.inventory.ContainerInput.QUICK_MOVE,p);
        h.assertTrue(menu.getCarried().isEmpty() && ItemStack.matches(jar.sample(),original),"Adventure cannot retrieve a preserved sample");
        press(h,p,menu,SignalPanels.JAR_READ);
        h.assertTrue(p.containerMenu!=menu && saved.equals(jar.saveWithFullMetadata(level.registryAccess())),"A read-only native sample reader leaves storage intact");
        p.gameMode.changeGameModeForPlayer(GameType.SURVIVAL); menu=CareMenuGameTest.open(h,p,pos); press(h,p,menu,SignalPanels.JAR_TAKE);
        h.assertTrue(jar.sample().isEmpty() && owns(p,original) && !menu.clickMenuButton(p,menu.buttonId(SignalPanels.JAR_TAKE)),"Retrieval returns the complete sample exactly once"); p.closeContainer();

        p=player(h); var at=pos.east(5); level.setBlock(at,ModBlocks.PEDIGREE_COMPARATOR.get().defaultBlockState(),3);
        var detector=(PedigreeComparatorBlockEntity)level.getBlockEntity(at);
        var wanted=Genome.DEFAULT.with(Gene.EARS,1,Gene.EARS.alleleNamed("long").id());
        var reference=TissueSampleItem.create(wanted,Stats.ZERO); reference.set(DataComponents.CUSTOM_NAME,Component.literal("Hidden-copy target")); var expected=reference.copy();
        p.getInventory().setItem(9,reference); var animal=h.spawnWithNoFreeWill(ModEntities.BUNNIDOG.get(),8,y+1,2); animal.setGenome(Genome.DEFAULT); animal.setAge(0);
        var before=detector.saveWithFullMetadata(level.registryAccess()); menu=CareMenuGameTest.open(h,p,at);
        h.assertTrue(before.equals(detector.saveWithFullMetadata(level.registryAccess())) && !detector.getBlockState().getValue(PedigreeComparatorBlock.ANALOG),"Opening a gene detector no longer silently toggles its circuit");
        press(h,p,menu,SignalPanels.COPY); press(h,p,menu,SignalPanels.ANALOG);
        h.assertTrue(owns(p,expected) && detector.reference().orElseThrow().genome().equals(wanted)
                && detector.getBlockState().getValue(PedigreeComparatorBlock.POWER)==13,"Explicit free copying and named analog mode use the real hidden-copy signal");
        CohesionGameTest.preview("Live gene signal",menu);
        int exact=menu.buttonId(SignalPanels.EXACT); animal.snapTo(animal.position().add(3,0,0)); detector.refresh();
        h.assertTrue(menu.clickMenuButton(p,exact),"An ordinary signal poll cannot invalidate an unrelated mode choice");
        int oldClear=menu.buttonId(SignalPanels.CLEAR); detector.setReference(new Tissue(Genome.DEFAULT,Stats.ZERO));
        h.assertTrue(!menu.clickMenuButton(p,oldClear) && detector.reference().isPresent(),"An old clear intent cannot remove a newly copied shared reference");
        p.gameMode.changeGameModeForPlayer(GameType.ADVENTURE); menu=CareMenuGameTest.open(h,p,at);
        h.assertTrue(!menu.clickMenuButton(p,menu.buttonId(SignalPanels.COPY)) && !menu.clickMenuButton(p,menu.buttonId(SignalPanels.ANALOG)),"Adventure cannot reprogram a live circuit");
        before=detector.saveWithFullMetadata(level.registryAccess()); press(h,p,menu,SignalPanels.READ_REFERENCE);
        h.assertTrue(before.equals(detector.saveWithFullMetadata(level.registryAccess())) && owns(p,expected),"Reading a copied reference changes neither sample nor detector"); animal.discard(); p.closeContainer();

        p=player(h); var display=pos.east(9); level.setBlock(display,ModBlocks.TROPHY_PEDESTAL.get().defaultBlockState(),3);
        var pedestal=(TrophyPedestalBlockEntity)level.getBlockEntity(display); var trophy=original.copy();
        p.snapTo(Vec3.atCenterOf(display).add(0,0,2));
        p.gameMode.useItemOn(p,level,ItemStack.EMPTY,InteractionHand.MAIN_HAND,new BlockHitResult(Vec3.atCenterOf(display),Direction.UP,display,false));
        h.assertTrue(!(p.containerMenu instanceof CareMenu) && pedestal.isEmpty(),"An empty display has no useless inspection menu");
        p.setItemInHand(InteractionHand.OFF_HAND,trophy);
        p.gameMode.useItemOn(p,level,trophy,InteractionHand.OFF_HAND,new BlockHitResult(Vec3.atCenterOf(display),Direction.UP,display,false));
        h.assertTrue(trophy.isEmpty() && ItemStack.matches(pedestal.item(),original),"Actual off-hand display placement pays only that item and preserves its complete record");
        menu=CareMenuGameTest.open(h,p,display); h.assertTrue(ItemStack.matches(pedestal.item(),original),"Opening a display cannot take its trophy");
        CohesionGameTest.preview("Trophy display",menu); press(h,p,menu,SignalPanels.TROPHY_READ);
        h.assertTrue(p.containerMenu!=menu && ItemStack.matches(pedestal.item(),original),"The existing native record reader is inspection only");
        menu=CareMenuGameTest.open(h,p,display); press(h,p,menu,SignalPanels.TROPHY_TAKE);
        h.assertTrue(pedestal.isEmpty() && owns(p,original),"Named trophy retrieval retains the full original sample"); p.closeContainer();
    }
}
