package com.bunnidogs.mod.entity;

import com.bunnidogs.mod.block.*;
import com.bunnidogs.mod.genetics.*;
import com.bunnidogs.mod.item.*;
import com.bunnidogs.mod.menu.*;
import com.bunnidogs.mod.registry.*;
import io.netty.buffer.Unpooled;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;

/** Real vanilla slot operations over the actual station journals, including cursor ownership and callbacks. */
public final class StationInventoryGameTest {
    private StationInventoryGameTest() {}
    private static ServerPlayer player(GameTestHelper h) {
        var player=h.makeMockServerPlayerInLevel(); player.gameMode.changeGameModeForPlayer(GameType.SURVIVAL); CareMenuGameTest.prepare(player); return player;
    }
    private static int inventorySlot(CareMenu menu,int slot) {
        for (int i=menu.inventoryKind().slots;i<menu.slots.size();i++) if (menu.slots.get(i).getSlotIndex()==slot) return i;
        throw new IllegalArgumentException("Missing player inventory slot");
    }
    private static boolean owns(ServerPlayer player,ItemStack expected) {
        for (int i=0;i<player.getInventory().getContainerSize();i++) if (ItemStack.matches(expected,player.getInventory().getItem(i))) return true;
        return false;
    }
    public static void selection(GameTestHelper h) {
        var level=h.getLevel(); var player=player(h); var pos=h.absolutePos(new BlockPos(2,165,2));
        level.setBlock(pos,ModBlocks.GENETICS_TABLE.get().defaultBlockState(),3);
        var table=(GeneticsTableBlockEntity)level.getBlockEntity(pos);
        var a=TissueSampleItem.create(Genome.DEFAULT,Stats.ZERO); a.set(DataComponents.CUSTOM_NAME,Component.literal("First inventory sample"));
        var b=TissueSampleItem.create(Genome.DEFAULT.with(Gene.COAT,0,2).with(Gene.COAT,1,2),Stats.of(1,2,3,4,5)); b.set(DataComponents.CUSTOM_NAME,Component.literal("Chosen second sample"));
        var expectedA=a.copy(); var expectedB=b.copy(); player.getInventory().setItem(9,a); player.getInventory().setItem(10,b);
        var menu=CareMenuGameTest.open(h,player,pos);
        menu.clicked(inventorySlot(menu,10),0,ContainerInput.PICKUP,player); menu.clicked(0,0,ContainerInput.PICKUP,player);
        h.assertTrue(ItemStack.matches(table.item(0),expectedB) && owns(player,expectedA) && !owns(player,expectedB),"Clicking a specific visible sample places that exact record, not the first inventory match");
        menu.clicked(inventorySlot(menu,9),0,ContainerInput.PICKUP,player); menu.clicked(0,0,ContainerInput.PICKUP,player);
        h.assertTrue(ItemStack.matches(table.item(0),expectedA) && ItemStack.matches(menu.getCarried(),expectedB),"An intentional native swap returns the complete previous sample to the cursor");
        menu.clicked(1,0,ContainerInput.PICKUP,player);
        var bytes=new RegistryFriendlyByteBuf(Unpooled.buffer(),level.registryAccess());
        try {
            menu.writeOpeningData(bytes); var client=ModMenus.CARE.get().create(menu.containerId,player.getInventory(),bytes);
            h.assertTrue(client.slots.size()==38 && client.inventoryKind()==StationInventory.Kind.TABLE
                    && ItemStack.matches(client.slots.get(0).getItem(),expectedA) && ItemStack.matches(client.slots.get(1).getItem(),expectedB),
                    "The registered native factory creates matching station/player slots and actual initial stock before its first frame");
        } finally { bytes.release(); }
        CohesionGameTest.preview("Chosen native samples",menu);
        h.assertTrue(menu.view().controls().stream().noneMatch(c->StationInventory.transferButton(c.id()&255)),"Native slots replace the automatic Add/Withdraw button bank");
        var other=pos.east(3); level.setBlock(other,ModBlocks.FEEDING_TROUGH.get().defaultBlockState(),3);
        final var active=menu; boolean[] armed={false}; int[] calls={0};
        menu.addSlotListener(new ContainerListener() {
            @Override public void dataChanged(AbstractContainerMenu container,int id,int value) {}
            @Override public void slotChanged(AbstractContainerMenu container,int slot,ItemStack stack) {
                if (!armed[0] || slot!=0) return;
                armed[0]=false; calls[0]++;
                h.assertTrue(table.isEmpty(0) && ItemStack.matches(active.getCarried(),expectedA),"Actual cursor ownership and station debit precede native slot callbacks");
                active.clicked(1,0,ContainerInput.PICKUP,player);
                h.assertTrue(ItemStack.matches(table.item(1),expectedB),"A recursive callback cannot run a second inventory gesture");
                CareMenu.open(player,other);
            }
        });
        armed[0]=true; menu.clicked(0,0,ContainerInput.PICKUP,player);
        h.assertTrue(calls[0]==1 && player.containerMenu!=menu && player.containerMenu instanceof CareMenu
                && menu.getCarried().isEmpty() && owns(player,expectedA) && ItemStack.matches(table.item(1),expectedB),
                "A replacement menu survives the callback and closing returns the complete cursor item exactly once");
        menu.clicked(1,0,ContainerInput.PICKUP,player); h.assertTrue(ItemStack.matches(table.item(1),expectedB),"A closed menu cannot operate its old station");
        player.closeContainer(); player.gameMode.changeGameModeForPlayer(GameType.ADVENTURE); menu=CareMenuGameTest.open(h,player,pos);
        menu.clicked(1,0,ContainerInput.QUICK_MOVE,player);
        h.assertTrue(menu.getCarried().isEmpty() && ItemStack.matches(table.item(1),expectedB),"Adventure inventory gestures keep station contents read-only");
        player.closeContainer(); player.gameMode.changeGameModeForPlayer(GameType.SURVIVAL); menu=CareMenuGameTest.open(h,player,pos);
        h.assertTrue(menu.clickMenuButton(player,menu.buttonId(WorkshopPanels.GOALS)) && menu.slots.stream().noneMatch(slot->slot.isActive()),"Choice pages hide their fixed native slot set");
        menu.clicked(1,0,ContainerInput.PICKUP,player); h.assertTrue(ItemStack.matches(table.item(1),expectedB),"A forged click on a hidden station slot cannot mutate it");
        h.assertTrue(menu.clickMenuButton(player,menu.buttonId(WorkshopPanels.BACK_TABLE)) && menu.slots.get(1).isActive(),"Back restores the same inventory slots");
        player.snapTo(player.position().add(20,0,0)); menu.clicked(1,0,ContainerInput.QUICK_MOVE,player);
        h.assertTrue(ItemStack.matches(table.item(1),expectedB),"Moving out of reach invalidates all native inventory mutations"); player.closeContainer();
        var trough=(FeedingTroughBlockEntity)level.getBlockEntity(other); var food=new ItemStack(ModItems.GUAVA.get());
        food.set(DataComponents.CUSTOM_NAME,Component.literal("Native notification reserve")); trough.insert(food);
        var notified=CareMenuGameTest.open(h,player,other); boolean[] observed={false};
        java.util.function.Consumer<net.neoforged.neoforge.event.level.BlockEvent.NeighborNotifyEvent> listener=event->{
            if (event.getLevel()!=level || !event.getPos().equals(other) || observed[0]) return;
            observed[0]=true;
            h.assertTrue(trough.food().isEmpty() && ItemStack.matches(notified.getCarried(),food),"Native neighbor callbacks observe the paid station debit and completed cursor ownership");
            notified.clicked(0,0,ContainerInput.PICKUP,player);
            CareMenu.open(player,pos);
        };
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(listener);
        try { notified.clicked(0,0,ContainerInput.PICKUP,player); }
        finally { net.neoforged.neoforge.common.NeoForge.EVENT_BUS.unregister(listener); }
        h.assertTrue(observed[0] && player.containerMenu!=notified && notified.getCarried().isEmpty() && owns(player,food) && trough.food().isEmpty(),
                "Replacing a menu during its actual neighbor update returns the completed cursor exactly once"); player.closeContainer();
    }
    public static void machines(GameTestHelper h) {
        var level=h.getLevel(); var player=player(h); var pos=h.absolutePos(new BlockPos(2,155,2));
        level.setBlock(pos,ModBlocks.NURSERY_CRADLE.get().defaultBlockState(),3);
        var cradle=(NurseryCradleBlockEntity)level.getBlockEntity(pos);
        var seed=SeedItem.create(Seed.of(Genome.DEFAULT,Stats.ZERO,Genome.DEFAULT,Stats.ZERO,1)); seed.set(DataComponents.CUSTOM_NAME,Component.literal("Chosen incubator seed"));
        var expected=seed.copy(); player.getInventory().setItem(9,seed); var menu=CareMenuGameTest.open(h,player,pos);
        menu.clicked(inventorySlot(menu,9),0,ContainerInput.QUICK_MOVE,player);
        var planted=level.getServer().getAdvancements().get(Identifier.fromNamespaceAndPath("bunnidogs","journey/plant"));
        h.assertTrue(ItemStack.matches(cradle.storedSeed(),expected) && player.getAdvancements().getOrStartProgress(planted).isDone(),"Native seed placement pays the actual item and retains the existing incubation milestone");
        for (int i=0;i<4;i++) cradle.grow(false);
        for (int i=0;i<11;i++) menu.broadcastChanges();
        h.assertTrue(cradle.age()==SeedCropBlock.MAX_AGE && menu.slots.get(0).mayPickup(player) && cradle.age()==SeedCropBlock.MAX_AGE,
                "Inventory synchronization and simulated pickup checks never reset incubation");
        menu.clicked(0,1,ContainerInput.PICKUP,player);
        h.assertTrue(cradle.storedSeed().isEmpty() && cradle.age()==0 && ItemStack.matches(menu.getCarried(),expected),"Withdrawing the actual seed resets growth without losing its record");
        player.closeContainer(); h.assertTrue(owns(player,expected),"Closing with a seed on the cursor safely returns it");
        var troughPos=pos.east(3); level.setBlock(troughPos,ModBlocks.FEEDING_TROUGH.get().defaultBlockState(),3);
        var trough=(FeedingTroughBlockEntity)level.getBlockEntity(troughPos); var legacy=new ItemStack(ModItems.GOLDEN_GUAVA.get(),40);
        legacy.set(DataComponents.CUSTOM_NAME,Component.literal("Legacy breeding reserve"));
        var saved=trough.saveWithFullMetadata(level.registryAccess());
        var encoded=net.minecraft.world.level.storage.TagValueOutput.createWithContext(net.minecraft.util.ProblemReporter.DISCARDING,level.registryAccess());
        encoded.store("Food",ItemStack.CODEC,legacy); saved.put("Food",encoded.buildResult().get("Food").copy());
        trough=(FeedingTroughBlockEntity)net.minecraft.world.level.block.entity.BlockEntity.loadStatic(troughPos,trough.getBlockState(),saved,level.registryAccess());
        level.setBlockEntity(trough); menu=CareMenuGameTest.open(h,player,troughPos);
        menu.clicked(0,0,ContainerInput.QUICK_MOVE,player);
        h.assertTrue(trough.food().isEmpty() && owns(player,legacy),"Legacy over-capacity breeding stock remains retrievable even though new meal insertion rejects it"); player.closeContainer();
        var pressPos=pos.south(3); level.setBlock(pressPos,ModBlocks.JUICER.get().defaultBlockState(),3);
        var press=(JuicerBlockEntity)level.getBlockEntity(pressPos); player.getInventory().setItem(11,new ItemStack(ModItems.GUAVA.get(),2)); player.getInventory().setItem(12,new ItemStack(Items.GLASS_BOTTLE,2));
        menu=CareMenuGameTest.open(h,player,pressPos); CareMenuGameTest.transfer(h,player,menu,CarePanels.ADD_FRUIT); CareMenuGameTest.transfer(h,player,menu,CarePanels.ADD_BOTTLE);
        for (int i=0;i<JuicerBlockEntity.PROCESS_TICKS;i++) press.serverTick(level);
        h.assertTrue(press.item(2).getItem() instanceof JuiceItem && !menu.slots.get(2).mayPlace(new ItemStack(ModItems.GUAVA.get())),"A real processing cycle produces juice and its native output slot rejects insertion");
        menu.broadcastChanges(); CohesionGameTest.preview("Juicer output",menu);
        var output=press.item(2); menu.clicked(2,0,ContainerInput.QUICK_MOVE,player);
        h.assertTrue(press.item(2).isEmpty() && owns(player,output),"Shift-clicking output returns the complete actual juice"); player.closeContainer();
        var penPos=pos.offset(3,0,3); level.setBlock(penPos,ModBlocks.BREEDING_PEN.get().defaultBlockState(),3);
        var pen=(BreedingPenBlockEntity)level.getBlockEntity(penPos);
        var left=new ItemStack(ModItems.GOLDEN_GUAVA.get(),6); left.set(DataComponents.CUSTOM_NAME,Component.literal("Chosen left breeding stock"));
        var right=new ItemStack(ModItems.DIAMOND_GUAVA.get(),5); right.set(DataComponents.CUSTOM_NAME,Component.literal("Chosen right breeding stock"));
        var expectedLeft=left.copy(); var expectedRight=right.copy(); player.getInventory().setItem(13,left); player.getInventory().setItem(14,right);
        menu=CareMenuGameTest.open(h,player,penPos);
        menu.clicked(inventorySlot(menu,14),0,ContainerInput.PICKUP,player); menu.clicked(1,0,ContainerInput.PICKUP,player);
        menu.clicked(inventorySlot(menu,13),0,ContainerInput.PICKUP,player); menu.clicked(0,0,ContainerInput.PICKUP,player);
        h.assertTrue(ItemStack.matches(pen.food(0),expectedLeft) && ItemStack.matches(pen.food(1),expectedRight),"Native manger placement preserves the actual chosen tier, name and count for each parent");
        CohesionGameTest.preview("Breeding mangers",menu);
        menu.clicked(1,1,ContainerInput.PICKUP,player);
        h.assertTrue(pen.food(1).getCount()==2 && ItemStack.matches(menu.getCarried(),expectedRight.copyWithCount(3)),"Right-click takes half the actual manger without applying its input-only hopper restriction to the player");
        menu.clicked(inventorySlot(menu,14),0,ContainerInput.PICKUP,player); menu.clicked(0,0,ContainerInput.QUICK_MOVE,player);
        h.assertTrue(pen.food(0).isEmpty() && owns(player,expectedLeft),"Shift-click recovers the complete other manger"); player.closeContainer();
    }
}
