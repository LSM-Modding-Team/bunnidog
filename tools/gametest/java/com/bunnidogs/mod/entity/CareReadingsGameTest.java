package com.bunnidogs.mod.entity;

import com.bunnidogs.mod.item.CalipersItem;
import com.bunnidogs.mod.menu.CareMenu;
import com.bunnidogs.mod.menu.CarePanels;
import com.bunnidogs.mod.menu.CareReadingPanel;
import com.bunnidogs.mod.registry.ModEntities;
import com.bunnidogs.mod.registry.ModItems;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.storage.TagValueOutput;

/** Requested measurement views remain bounded, tool-gated and read-only for the actual living target. */
public final class CareReadingsGameTest {
    private CareReadingsGameTest() {}
    public static void run(GameTestHelper h) {
        var dog=h.spawnWithNoFreeWill(ModEntities.BUNNIDOG.get(),2.5F,160,2.5F);
        dog.setAge(0); dog.setHealth(dog.getMaxHealth()-1);
        var player=h.makeMockServerPlayerInLevel(); CareMenuGameTest.prepare(player);
        player.gameMode.changeGameModeForPlayer(GameType.SURVIVAL); player.snapTo(dog.position().add(1,0,0));
        CalipersItem.showCare(player,dog);
        h.assertTrue(!(player.containerMenu instanceof CareMenu),"Calling inspection without actual Calipers reveals no measurements");
        var calipers=new ItemStack(ModItems.CALIPERS.get()); player.setItemInHand(InteractionHand.MAIN_HAND,calipers);
        var before=TagValueOutput.createWithContext(ProblemReporter.DISCARDING,h.getLevel().registryAccess()); dog.saveWithoutId(before);
        player.setShiftKeyDown(true); player.interactOn(dog,InteractionHand.MAIN_HAND,Vec3.ZERO);
        h.assertTrue(player.containerMenu instanceof CareMenu,"Real crouch-use Calipers open a bounded inspection instead of a chat report");
        var menu=(CareMenu)player.containerMenu;
        h.assertTrue(menu.view().rows().isEmpty() && menu.view().subjectId()<0 && menu.buttonId(CareReadingPanel.FIRST)>=0,
                "The tool first offers purposes, without dumping every reading");
        CohesionGameTest.preview("Calipers choices",menu.view());
        for (int page=0;page<5;page++) {
            h.assertTrue(menu.clickMenuButton(player,menu.buttonId(CareReadingPanel.FIRST+page)),"An explicit reading group opens");
            h.assertTrue(!menu.view().rows().isEmpty() && menu.view().rows().size()<=12 && menu.view().controls().size()==1,
                    "Each requested group is bounded and uses a single consistent Back control");
            for (var row:menu.view().rows()) if (row.getContents() instanceof net.minecraft.network.chat.contents.TranslatableContents translated)
                for (var argument:translated.getArgs()) if (argument instanceof net.minecraft.network.chat.Component component && !component.getString().isEmpty())
                    h.assertTrue(row.getString().contains(component.getString()),"A measured label must actually display its status, rather than silently discarding the value");
            if (page==0) CohesionGameTest.preview("Measured health",menu.view());
            if (page==2) CohesionGameTest.preview("Measured work",menu.view());
            h.assertTrue(menu.clickMenuButton(player,menu.buttonId(CarePanels.BACK)),"Reading Back returns to purpose choices");
        }
        var after=TagValueOutput.createWithContext(ProblemReporter.DISCARDING,h.getLevel().registryAccess()); dog.saveWithoutId(after);
        h.assertTrue(before.buildResult().equals(after.buildResult()) && calipers.getCount()==1,"Reading every group changes neither the full saved animal nor its instrument");
        h.assertTrue(menu.clickMenuButton(player,menu.buttonId(CareReadingPanel.FIRST)),"An actual measured page is open before losing its tool");
        int old=menu.buttonId(CarePanels.BACK); player.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);
        h.assertTrue(!menu.clickMenuButton(player,old) && menu.view().rows().isEmpty() && menu.view().subjectId()==dog.getId()
                && menu.buttonId(CareReadingPanel.FIRST)<0 && menu.buttonId(CarePanels.GENETICS)<0,
                "Losing Calipers retires measured pages and old controls, returning to unmeasured interaction");
        player.closeContainer(); player.setItemInHand(InteractionHand.MAIN_HAND,calipers);
        player.gameMode.changeGameModeForPlayer(GameType.ADVENTURE); CalipersItem.showCare(player,dog); menu=(CareMenu)player.containerMenu;
        h.assertTrue(menu.clickMenuButton(player,menu.buttonId(CareReadingPanel.FIRST+4)),"Adventure retains private read-only instrument use");
        player.closeContainer(); player.gameMode.changeGameModeForPlayer(GameType.SURVIVAL);
        player.setShiftKeyDown(false); player.interactOn(dog,InteractionHand.MAIN_HAND,Vec3.ZERO);
        h.assertTrue(!(player.containerMenu instanceof CareMenu),"Normal Calipers use keeps the original detailed genetics reader");
    }
}
