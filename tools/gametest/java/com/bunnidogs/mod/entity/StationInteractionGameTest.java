package com.bunnidogs.mod.entity;

import com.bunnidogs.mod.block.FeedingTroughBlock;
import com.bunnidogs.mod.block.FeedingTroughBlockEntity;
import com.bunnidogs.mod.genetics.TroughWork;
import com.bunnidogs.mod.menu.CarePanels;
import com.bunnidogs.mod.network.CareViewPayload;
import com.bunnidogs.mod.registry.ModBlocks;
import com.bunnidogs.mod.registry.ModItems;
import com.bunnidogs.mod.registry.ModMenus;
import io.netty.buffer.Unpooled;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;

/** Compact station views still identify actual items and bind actions to the page the player saw. */
public final class StationInteractionGameTest {
    private StationInteractionGameTest() {}
    public static void run(GameTestHelper h) {
        var level=h.getLevel(); var player=h.makeMockServerPlayerInLevel();
        player.gameMode.changeGameModeForPlayer(GameType.SURVIVAL);
        var pos=h.absolutePos(new BlockPos(2,170,2));
        level.setBlock(pos,ModBlocks.FEEDING_TROUGH.get().defaultBlockState(),3);
        var trough=(FeedingTroughBlockEntity)level.getBlockEntity(pos);
        var food=new ItemStack(ModItems.GUAVA.get(),7); food.set(DataComponents.CUSTOM_NAME,Component.literal("Actual shared meals"));
        trough.setInitialFood(food);
        var menu=CareMenuGameTest.open(h,player,pos); int root=menu.view().page();
        h.assertTrue(menu.view().next().getString().isEmpty() && menu.view().rows().size()==1 && menu.view().stock().size()==1
                && ItemStack.matches(menu.view().stock().getFirst().item(),food) && menu.buttonId(CarePanels.MODE+1)<0,
                "A working Trough shows actual stock and its work state, without a tutorial or the full mode bank");
        var wire=new RegistryFriendlyByteBuf(Unpooled.buffer(),level.registryAccess());
        try {
            menu.writeOpeningData(wire);
            var client=ModMenus.CARE.get().create(menu.containerId,player.getInventory(),wire);
            h.assertTrue(client.view().equals(menu.view()),"Stock components, counts and page identity exist in the initial client factory view");
            CareViewPayload.STREAM_CODEC.encode(wire,new CareViewPayload(menu.containerId,menu.view()));
            h.assertTrue(CareViewPayload.STREAM_CODEC.decode(wire).view().equals(menu.view()),"Live stock views round-trip without dropping item components");
        } finally { wire.release(); }
        int stale=menu.buttonId(CarePanels.TENDING);
        h.assertTrue(menu.clickMenuButton(player,menu.buttonId(CarePanels.WORK_MODES)),"Work choices open deliberately");
        h.assertTrue(menu.view().page()!=root && trough.workMode()==TroughWork.MIXED && trough.food().getCount()==7,
                "Opening a choice page changes neither work policy nor stock");
        CohesionGameTest.preview("Resident work choices",menu);
        h.assertTrue(menu.clickMenuButton(player,menu.buttonId(CarePanels.BACK)),"Work choices return through the common Back control");
        h.assertTrue(menu.view().page()==root && !menu.clickMenuButton(player,stale)
                && !trough.getBlockState().getValue(FeedingTroughBlock.TENDING),
                "Returning to the same stock must still reject an old button from before the page transition");
        h.assertTrue(menu.clickMenuButton(player,menu.buttonId(CarePanels.WORK_MODES))
                && menu.clickMenuButton(player,menu.buttonId(CarePanels.MODE+TroughWork.GARDEN.ordinal()))
                && menu.view().page()==root && trough.workMode()==TroughWork.GARDEN,
                "Selecting a work policy updates the real station and returns to its main page");
        var displayed=menu.view().stock().getFirst().item().copy(); trough.takeAll().shrink(1);
        h.assertTrue(ItemStack.matches(displayed,menu.view().stock().getFirst().item()),"Published item displays are detached from mutable station stock");
        player.closeContainer();
    }
}
