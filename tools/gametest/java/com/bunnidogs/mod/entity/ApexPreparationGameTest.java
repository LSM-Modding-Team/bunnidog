package com.bunnidogs.mod.entity;

import java.util.ArrayList;
import java.util.function.Consumer;
import java.util.concurrent.atomic.AtomicReference;
import com.bunnidogs.mod.block.*;
import com.bunnidogs.mod.genetics.*;
import com.bunnidogs.mod.item.TissueSampleItem;
import com.bunnidogs.mod.menu.*;
import com.bunnidogs.mod.registry.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundContainerButtonClickPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;

/** Real native preparation, cancellation, changed stock, spawn rejection and recursive callbacks. */
public final class ApexPreparationGameTest {
    private ApexPreparationGameTest() {}
    private static void press(GameTestHelper h,ServerPlayer p,CareMenu menu,int action) {
        int button=menu.buttonId(action); h.assertTrue(menu.view().permits(button),"The displayed ritual action is enabled: "+action);
        p.connection.handleContainerButtonClick(new ServerboundContainerButtonClickPacket(menu.containerId,button));
    }
    private static CareMenu arm(GameTestHelper h,ServerPlayer p,BlockPos pos) {
        var menu=CareMenuGameTest.open(h,p,pos); press(h,p,menu,ApexPanel.PREPARE); return menu;
    }
    public static void run(GameTestHelper h) {
        var level=h.getLevel(); int y=374; var pos=h.absolutePos(new BlockPos(23,y+1,5)); var previousDifficulty=level.getDifficulty();
        level.getServer().setDifficulty(Difficulty.NORMAL,true);
        try {
            for (int x=17;x<=29;x++) for (int z=0;z<=11;z++) { h.setBlock(x,y,z,Blocks.STONE); for (int up=1;up<=4;up++) h.setBlock(x,y+up,z,Blocks.AIR); }
            level.setBlock(pos,ModBlocks.APEX_CRUCIBLE.get().defaultBlockState(),3);
            var p=h.makeMockServerPlayerInLevel(); p.gameMode.changeGameModeForPlayer(GameType.SURVIVAL); CareMenuGameTest.prepare(p);
            var source=TissueSampleItem.create(Genome.DEFAULT,Stats.of(7,8,9,10,11)); source.setCount(2);
            source.set(DataComponents.CUSTOM_NAME,Component.literal("Chosen family challenge")); p.getInventory().setItem(9,source);
            var pedestals=new ArrayList<TrophyPedestalBlockEntity>();
            for (int i=0;i<5;i++) {
                var at=pos.offset(ApexCrucibleBlock.RING.get(i)); level.setBlock(at,ModBlocks.TROPHY_PEDESTAL.get().defaultBlockState(),3);
                var pedestal=(TrophyPedestalBlockEntity)level.getBlockEntity(at); pedestal.setItem(new ItemStack(ApexRitual.legends().get(i),2)); pedestals.add(pedestal);
            }
            var menu=CareMenuGameTest.open(h,p,pos); press(h,p,menu,ApexPanel.PREPARE);
            CohesionGameTest.preview("Apex preparation",menu.view());
            h.assertTrue(source.getCount()==2 && pedestals.stream().allMatch(be->be.item().getCount()==2)
                    && level.getEntitiesOfClass(Apex.class,new AABB(pos).inflate(5)).isEmpty(),"Opening/reviewing a ready ritual never spends materials or creates a boss");
            press(h,p,menu,ApexPanel.CANCEL); press(h,p,menu,ApexPanel.PREPARE); int stale=menu.buttonId(ApexPanel.SUMMON);
            var renamed=pedestals.getFirst().item().copy(); renamed.set(DataComponents.CUSTOM_NAME,Component.literal("New displayed offering")); pedestals.getFirst().setItem(renamed);
            h.assertTrue(!menu.clickMenuButton(p,stale) && source.getCount()==2 && !menu.view().permits(menu.buttonId(ApexPanel.SUMMON)),
                    "A changed but still qualifying offering disarms old confirmation before spending");
            menu=arm(h,p,pos); stale=menu.buttonId(ApexPanel.SUMMON); source.set(DataComponents.CUSTOM_NAME,Component.literal("Renamed chosen sample"));
            h.assertTrue(!menu.clickMenuButton(p,stale) && source.getCount()==2,"A changed whole sample also disarms the shown confirmation");
            p.gameMode.changeGameModeForPlayer(GameType.ADVENTURE); menu=CareMenuGameTest.open(h,p,pos);
            h.assertTrue(!menu.clickMenuButton(p,menu.buttonId(ApexPanel.PREPARE)),"Adventure inspects forecasts without starting an encounter");
            press(h,p,menu,ApexPanel.SHOW_RING); h.assertTrue(p.containerMenu!=menu,"Showing ring positions returns to the world without changing its materials");
            p.gameMode.changeGameModeForPlayer(GameType.SURVIVAL); level.setBlock(pos.above(2),Blocks.STONE.defaultBlockState(),3);
            menu=arm(h,p,pos); press(h,p,menu,ApexPanel.SUMMON);
            h.assertTrue(source.getCount()==2 && pedestals.stream().allMatch(be->be.item().getCount()==2)
                    && menu.view().next().equals(Component.translatable("care.bunnidogs.apex.next.blocked")),"Blocked spawning keeps all actual items and gives a visible local reason");
            level.removeBlock(pos.above(2),false);
            var nested=new AtomicReference<ApexRitual.Status>();
            Consumer<EntityJoinLevelEvent> reject=event->{ if (event.getEntity() instanceof Apex boss && boss.level()==level && boss.distanceToSqr(pos.getX()+.5,pos.getY()+1,pos.getZ()+.5)<4) {
                nested.set(ApexRitual.summon(p,pos,source)); event.setCanceled(true);
            }};
            menu=arm(h,p,pos); NeoForge.EVENT_BUS.addListener(reject);
            try { press(h,p,menu,ApexPanel.SUMMON); } finally { NeoForge.EVENT_BUS.unregister(reject); }
            h.assertTrue(nested.get()==ApexRitual.Status.BUSY && source.getCount()==2 && pedestals.stream().allMatch(be->be.item().getCount()==2)
                    && level.getEntitiesOfClass(Apex.class,new AABB(pos).inflate(5)).isEmpty(),"A cancelled spawn and reentrant ritual cannot produce a free boss or charge any item");
            var removed=new AtomicReference<ItemStack>();
            Consumer<EntityJoinLevelEvent> replace=event->{ if (event.getEntity() instanceof Apex boss && boss.level()==level && boss.distanceToSqr(pos.getX()+.5,pos.getY()+1,pos.getZ()+.5)<4) {
                removed.set(pedestals.getFirst().takeItem()); pedestals.getFirst().setItem(new ItemStack(Items.APPLE,7));
            }};
            menu=arm(h,p,pos); NeoForge.EVENT_BUS.addListener(replace);
            try { press(h,p,menu,ApexPanel.SUMMON); } finally { NeoForge.EVENT_BUS.unregister(replace); }
            h.assertTrue(source.getCount()==2 && removed.get().getCount()==2 && pedestals.getFirst().item().is(Items.APPLE) && pedestals.getFirst().item().getCount()==7
                    && pedestals.stream().skip(1).allMatch(be->be.item().getCount()==2) && level.getEntitiesOfClass(Apex.class,new AABB(pos).inflate(5)).isEmpty(),
                    "Spawn callbacks replacing a legend keep the replacement and other stock, and remove the unpaid Apex");
            pedestals.getFirst().setItem(removed.get());
            Consumer<EntityJoinLevelEvent> moveSource=event->{ if (event.getEntity() instanceof Apex boss && boss.level()==level && boss.distanceToSqr(pos.getX()+.5,pos.getY()+1,pos.getZ()+.5)<4)
                p.getInventory().setItem(9,TissueSampleItem.create(Genome.DEFAULT,Stats.ZERO)); };
            menu=arm(h,p,pos); NeoForge.EVENT_BUS.addListener(moveSource);
            try { press(h,p,menu,ApexPanel.SUMMON); } finally { NeoForge.EVENT_BUS.unregister(moveSource); }
            h.assertTrue(source.getCount()==2 && p.getInventory().getItem(9).getCount()==1 && pedestals.stream().allMatch(be->be.item().getCount()==2)
                    && level.getEntitiesOfClass(Apex.class,new AABB(pos).inflate(5)).isEmpty(),"A replaced inventory source cannot be charged through a detached old stack");
            p.getInventory().setItem(9,source); nested.set(null);
            Consumer<EntityJoinLevelEvent> reenter=event->{ if (event.getEntity() instanceof Apex boss && boss.level()==level && boss.distanceToSqr(pos.getX()+.5,pos.getY()+1,pos.getZ()+.5)<4)
                nested.set(ApexRitual.summon(p,pos,source)); };
            menu=arm(h,p,pos); NeoForge.EVENT_BUS.addListener(reenter);
            try { press(h,p,menu,ApexPanel.SUMMON); } finally { NeoForge.EVENT_BUS.unregister(reenter); }
            var bosses=level.getEntitiesOfClass(Apex.class,new AABB(pos).inflate(5));
            h.assertTrue(bosses.size()==1 && nested.get()==ApexRitual.Status.BUSY && source.getCount()==1 && pedestals.stream().allMatch(be->be.item().getCount()==1),
                    "One successful ritual pays exactly one of each legend even from legacy stacked displays, and blocks recursive double-spawning");
            h.assertTrue(bosses.getFirst().stats().equals(TissueSampleItem.read(source).orElseThrow().stats()),"The summoned boss retains the exact reviewed source stats"); bosses.getFirst().discard();
            p.gameMode.changeGameModeForPlayer(GameType.CREATIVE); menu=arm(h,p,pos); press(h,p,menu,ApexPanel.SUMMON);
            bosses=level.getEntitiesOfClass(Apex.class,new AABB(pos).inflate(5));
            h.assertTrue(bosses.size()==1 && source.getCount()==1 && pedestals.stream().allMatch(be->be.item().getCount()==1),"Creative confirmation retains the exact source and display items"); bosses.getFirst().discard(); p.closeContainer();
        } finally { level.getServer().setDifficulty(previousDifficulty,true); }
    }
}
