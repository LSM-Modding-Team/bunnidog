package com.bunnidogs.mod.entity;

import com.bunnidogs.mod.block.*;
import com.bunnidogs.mod.genetics.*;
import com.bunnidogs.mod.item.SeedItem;
import com.bunnidogs.mod.menu.*;
import com.bunnidogs.mod.network.SeedPayload;
import com.bunnidogs.mod.registry.ModBlocks;
import com.bunnidogs.mod.registry.ModItems;
import com.bunnidogs.mod.layout.Metrics;
import com.bunnidogs.mod.layout.SeedSheet;
import com.bunnidogs.mod.BunnidogsMod;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.netty.buffer.Unpooled;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundContainerButtonClickPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;

/** Reuses a real family's Table to distinguish a produced seed from its prospective held-feed plan. */
public final class LitterPlanningGameTest {
    private LitterPlanningGameTest() {}
    private static void press(GameTestHelper h,ServerPlayer player,CareMenu menu,int action) {
        var button=menu.buttonId(action); h.assertTrue(menu.view().permits(button),"Actual-seed control must be shown: "+action);
        player.connection.handleContainerButtonClick(new ServerboundContainerButtonClickPacket(menu.containerId,button));
    }
    public static void run(GameTestHelper h,ServerPlayer player,BlockPos pos,GeneticsTableBlockEntity table,Seed parents) {
        var level=h.getLevel();
        var actual=new Seed(parents.mother(),parents.motherStats(),parents.father(),parents.fatherStats(),2.75,true);
        var stack=SeedItem.create(actual); stack.set(DataComponents.CUSTOM_NAME,Component.literal("The actual treated litter"));
        player.getInventory().setItem(15,stack); var before=stack.copy(); var saved=table.saveWithFullMetadata(level.registryAccess());
        var menu=CareMenuGameTest.open(h,player,pos); press(h,player,menu,LitterPanel.LITTER);
        CohesionGameTest.preview("Actual litter against goal",menu.view()); export(actual);
        player.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(ModItems.DIAMOND_GUAVA.get(),4));
        player.setItemInHand(InteractionHand.OFF_HAND,new ItemStack(ModItems.MUTAGEN.get(),3));
        h.assertTrue(LitterPanel.view(player,table).rows().stream().anyMatch(row->row.getString().contains("2.75"))
                && ItemStack.matches(stack,before) && saved.equals(table.saveWithFullMetadata(level.registryAccess())),
                "Actual saved quality and treatment ignore rich held preview cues and preserve the complete seed and Table");
        var reverse=new Seed(actual.father(),actual.fatherStats(),actual.mother(),actual.motherStats(),actual.quality(),actual.mutagen());
        h.assertTrue(LitterPanel.matches(table,actual) && LitterPanel.matches(table,reverse)
                && !LitterPanel.matches(table,new Seed(actual.mother(),Stats.ZERO,actual.father(),actual.fatherStats(),actual.quality(),true)),
                "Actual-seed matching keeps both parent points as well as genes, accepting either order");
        for (var goal:Commissions.Request.values()) {
            table.setGoalFromPlayer(player,goal);
            var view=LitterPanel.view(player,table);
            h.assertTrue(view.rows().size()<=12 && view.controls().size()==3 && ItemStack.matches(stack,before),
                    "Every actual-seed target fits the native view, including a treated End Nursery's complete checklist");
        }
        table.setGoalFromPlayer(player,Commissions.Request.NIGHT_GUIDE);
        player.closeContainer(); menu=CareMenuGameTest.open(h,player,pos); press(h,player,menu,LitterPanel.LITTER);
        int stale=menu.buttonId(LitterPanel.READ_SEED); player.getInventory().setItem(16,stack);
        var replacement=SeedItem.create(new Seed(Genome.DEFAULT,Stats.ZERO,Genome.DEFAULT,Stats.ZERO,.25));
        replacement.set(DataComponents.CUSTOM_NAME,Component.literal("A different litter")); player.getInventory().setItem(15,replacement);
        h.assertTrue(!menu.clickMenuButton(player,stale) && player.containerMenu==menu && ItemStack.matches(stack,before),
                "A replaced inventory seed refreshes before an old reader action can silently inspect a different litter");
        press(h,player,menu,LitterPanel.READ_SEED);
        h.assertTrue(player.containerMenu!=menu && replacement.getCount()==1 && ItemStack.matches(stack,before),"The native reader closes its container and keeps both real seed items");
        var buffer=Unpooled.buffer();
        try {
            SeedPayload.STREAM_CODEC.encode(buffer,new SeedPayload(actual.toString()));
            h.assertTrue(Seed.parse(SeedPayload.STREAM_CODEC.decode(buffer).seed()).orElseThrow().equals(actual),"The existing native seed payload retains exact saved quality and treatment");
        } finally { buffer.release(); }
        var cradlePos=pos.offset(-3,0,7); level.setBlock(cradlePos,ModBlocks.NURSERY_CRADLE.get().defaultBlockState(),3);
        var cradle=(NurseryCradleBlockEntity)level.getBlockEntity(cradlePos); cradle.insert(before);
        var cradleSaved=cradle.saveWithFullMetadata(level.registryAccess());
        player.gameMode.changeGameModeForPlayer(GameType.ADVENTURE); menu=CareMenuGameTest.open(h,player,cradlePos);
        press(h,player,menu,LitterPanel.READ_SEED);
        h.assertTrue(player.containerMenu!=menu && cradleSaved.equals(cradle.saveWithFullMetadata(level.registryAccess())) && ItemStack.matches(stack,before),
                "Adventure reads the actual incubating seed without withdrawing it, resetting growth or spending held inputs");
        player.gameMode.changeGameModeForPlayer(GameType.SURVIVAL); CareMenuGameTest.stow(player);
    }
    private static void export(Seed seed) {
        var spread=SeedSheet.spread(seed,SeedSheet.FORECAST,Metrics.ESTIMATE); var output=new JsonObject();
        for (var name:java.util.List.of("left","right")) {
            var rows=new JsonArray(); var page=name.equals("left") ? spread.left() : spread.right();
            for (var text:page.texts()) {
                var row=new JsonObject(); row.addProperty("x",text.x()); row.addProperty("y",text.y()); row.addProperty("text",text.text());
                row.addProperty("right",text.right()); row.addProperty("tone",text.tone().name()); rows.add(row);
            } output.add(name,rows);
        }
        BunnidogsMod.LOGGER.info("SEED_FORECAST_PREVIEW {}",output);
    }
}
