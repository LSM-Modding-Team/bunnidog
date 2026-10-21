package com.bunnidogs.mod.entity;

import java.util.Map;
import com.bunnidogs.mod.menu.CareMenu;
import com.bunnidogs.mod.menu.CarePanels;
import com.bunnidogs.mod.network.CareViewPayload;
import com.bunnidogs.mod.network.ModNetwork;
import com.bunnidogs.mod.network.SheetPayload;
import com.bunnidogs.mod.network.TablePayload;
import com.bunnidogs.mod.network.SamplePayload;
import com.bunnidogs.mod.network.SeedPayload;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.registration.ChannelAttributes;
import net.neoforged.neoforge.network.registration.NetworkChannel;
import net.neoforged.neoforge.network.registration.NetworkPayloadSetup;

/** Advertise real mod payloads on the native embedded mock connection. */
public final class CareMenuGameTest {
    private CareMenuGameTest() {}
    public static void prepare(ServerPlayer player) {
        var care=CareViewPayload.TYPE.id(); var sheet=SheetPayload.TYPE.id(); var table=TablePayload.TYPE.id(); var sample=SamplePayload.TYPE.id(); var seed=SeedPayload.TYPE.id();
        var opening=net.neoforged.neoforge.network.payload.AdvancedOpenScreenPayload.TYPE.id();
        ChannelAttributes.setPayloadSetup(player.connection.getConnection(),new NetworkPayloadSetup(Map.of(ConnectionProtocol.PLAY,
                Map.of(care,new NetworkChannel(care,ModNetwork.VERSION),sheet,new NetworkChannel(sheet,ModNetwork.VERSION),table,new NetworkChannel(table,ModNetwork.VERSION),sample,new NetworkChannel(sample,ModNetwork.VERSION),seed,new NetworkChannel(seed,ModNetwork.VERSION),opening,new NetworkChannel(opening,"1")))));
    }
    public static CareMenu open(GameTestHelper h,ServerPlayer player,BlockPos pos) {
        prepare(player); player.setShiftKeyDown(false); stow(player);
        player.snapTo(Vec3.atCenterOf(pos).add(0,0,2));
        player.gameMode.useItemOn(player,h.getLevel(),ItemStack.EMPTY,InteractionHand.MAIN_HAND,new BlockHitResult(Vec3.atCenterOf(pos),Direction.UP,pos,false));
        h.assertTrue(player.containerMenu instanceof CareMenu,"Empty-hand station use must open the native care menu");
        var menu=(CareMenu)player.containerMenu;
        h.assertTrue(menu.view()!=null && menu.stillValid(player),"The live menu must immediately supply its authoritative snapshot");
        return menu;
    }
    public static void stow(ServerPlayer player) {
        for (var hand:InteractionHand.values()) {
            var stack=player.getItemInHand(hand); player.setItemInHand(hand,ItemStack.EMPTY);
            if (stack.isEmpty()) continue;
            boolean stored=false;
            for (int i=9;i<36;i++) if (player.getInventory().getItem(i).isEmpty()) { player.getInventory().setItem(i,stack); stored=true; break; }
            if (!stored) player.drop(stack,false);
        }
    }
    public static void assignHome(GameTestHelper h,ServerPlayer player,BlockPos pos) {
        player.closeContainer(); player.setShiftKeyDown(false); stow(player);
        player.snapTo(Vec3.atCenterOf(pos).add(0,0,2));
        player.gameMode.useItemOn(player,h.getLevel(),ItemStack.EMPTY,InteractionHand.MAIN_HAND,new BlockHitResult(Vec3.atCenterOf(pos),Direction.UP,pos,false));
        h.assertTrue(!(player.containerMenu instanceof CareMenu),"Nest use must act in the world, without a tutorial menu");
    }
    public static boolean chooseGuava(GameTestHelper h,ServerPlayer player,Bunnidog dog,InteractionHand hand,int action) {
        prepare(player); player.closeContainer(); player.snapTo(dog.position().add(1,0,0));
        var stock=player.getItemInHand(hand).copy(); int love=dog.getInLoveTime(); float health=dog.getHealth();
        player.interactOn(dog,hand,Vec3.ZERO);
        h.assertTrue(player.containerMenu instanceof CareMenu,"A guava opens explicit food choices");
        var menu=(CareMenu)player.containerMenu;
        h.assertTrue(ItemStack.matches(stock,player.getItemInHand(hand)) && love==dog.getInLoveTime() && health==dog.getHealth(),
                "Opening food choices must not spend, heal or start courtship");
        int button=menu.buttonId(action);
        if (!menu.view().permits(button)) { player.closeContainer(); return false; }
        return menu.clickMenuButton(player,button);
    }
    public static void click(GameTestHelper h,ServerPlayer player,BlockPos pos,int action) {
        var menu=open(h,player,pos);
        h.assertTrue(menu.clickMenuButton(player,menu.buttonId(action)),"The explicit live station action must be available: "+action);
    }
}
