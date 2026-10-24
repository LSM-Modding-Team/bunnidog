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
    /** Select an actual visible inventory stack, place one into a real slot, and return the cursor remainder. */
    public static void transfer(GameTestHelper h,ServerPlayer player,CareMenu menu,int action) {
        int target=switch (action) {
            case CarePanels.ADD_RIGHT,CarePanels.TAKE_RIGHT,CarePanels.ADD_BOTTLE,CarePanels.TAKE_CARRIER,com.bunnidogs.mod.menu.WorkshopPanels.TAKE_B -> 1;
            case CarePanels.TAKE_JUICE -> 2;
            case com.bunnidogs.mod.menu.WorkshopPanels.ADD_SAMPLE -> menu.slots.get(0).hasItem() ? 1 : 0;
            default -> 0;
        };
        boolean add=java.util.Set.of(CarePanels.ADD_FOOD,CarePanels.ADD_LEFT,CarePanels.ADD_RIGHT,CarePanels.ADD_SEED,CarePanels.ADD_FRUIT,CarePanels.ADD_BOTTLE,
                com.bunnidogs.mod.menu.WorkshopPanels.ADD_SAMPLE,com.bunnidogs.mod.menu.SignalPanels.JAR_ADD).contains(action);
        h.assertTrue(menu.getCarried().isEmpty(),"A transfer gesture begins with an empty cursor");
        if (add) {
            int source=-1;
            for (int i=menu.inventoryKind().slots;i<menu.slots.size();i++) {
                var candidate=menu.slots.get(i).getItem();
                boolean type=switch (action) {
                    case CarePanels.ADD_FOOD,CarePanels.ADD_LEFT,CarePanels.ADD_RIGHT,CarePanels.ADD_FRUIT -> candidate.is(com.bunnidogs.mod.registry.ModItems.GUAVA.get());
                    case CarePanels.ADD_BOTTLE -> candidate.is(net.minecraft.world.item.Items.GLASS_BOTTLE);
                    case CarePanels.ADD_SEED -> com.bunnidogs.mod.item.SeedItem.read(candidate).isPresent();
                    default -> com.bunnidogs.mod.item.TissueSampleItem.read(candidate).isPresent();
                };
                var stored=menu.slots.get(target).getItem();
                if (type && menu.slots.get(target).mayPlace(candidate) && (stored.isEmpty() || ItemStack.isSameItemSameComponents(candidate,stored)
                        && stored.getCount()<menu.slots.get(target).getMaxStackSize(candidate))) { source=i; break; }
            }
            h.assertTrue(source>=0,"A real inventory input can enter the requested native slot: "+action);
            menu.clicked(source,1,net.minecraft.world.inventory.ContainerInput.PICKUP,player);
            menu.clicked(target,1,net.minecraft.world.inventory.ContainerInput.PICKUP,player);
            if (!menu.getCarried().isEmpty()) menu.clicked(source,0,net.minecraft.world.inventory.ContainerInput.PICKUP,player);
            h.assertTrue(menu.getCarried().isEmpty(),"The exact remainder returns to the player's inventory");
        } else menu.clicked(target,0,net.minecraft.world.inventory.ContainerInput.QUICK_MOVE,player);
    }
    public static void click(GameTestHelper h,ServerPlayer player,BlockPos pos,int action) {
        var menu=open(h,player,pos);
        h.assertTrue(menu.clickMenuButton(player,menu.buttonId(action)),"The explicit live station action must be available: "+action);
    }
}
