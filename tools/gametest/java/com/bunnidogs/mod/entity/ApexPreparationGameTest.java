package com.bunnidogs.mod.entity;

import java.util.function.Consumer;
import java.util.concurrent.atomic.AtomicReference;
import com.bunnidogs.mod.genetics.*;
import com.bunnidogs.mod.item.TissueSampleItem;
import com.bunnidogs.mod.menu.*;
import com.bunnidogs.mod.registry.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Difficulty;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;

/** Native explicit preparation, source/fuel/heart replacement, cancellation and reentry for the new ritual. */
public final class ApexPreparationGameTest {
    private static CareMenu arm(GameTestHelper h,net.minecraft.server.level.ServerPlayer p,BlockPos pos) {var menu=CareMenuGameTest.open(h,p,pos);h.assertTrue(menu.clickMenuButton(p,menu.buttonId(ApexPanel.PREPARE)),"A rooted Crucible and incense permit deliberate preparation");return menu;}
    public static void run(GameTestHelper h) {
        var l=h.getLevel();var previous=l.getDifficulty();l.getServer().setDifficulty(Difficulty.NORMAL,true);var pos=h.absolutePos(new BlockPos(23,375,5));
        try {
            for(int x=17;x<=29;x++)for(int z=0;z<=11;z++) {h.setBlock(x,374,z,Blocks.STONE);for(int y=375;y<380;y++)h.setBlock(x,y,z,Blocks.AIR);}
            l.setBlock(pos,ModBlocks.APEX_CRUCIBLE.get().defaultBlockState(),3);var p=ApexTypesGameTest.player(h,Vec3.atBottomCenterOf(pos.east(2)));SocietyGameTest.root(h,p,pos);
            var source=TissueSampleItem.create(Genome.DEFAULT,Stats.of(7,8,9,10,11));source.setCount(2);p.getInventory().setItem(9,source);var fuel=p.getInventory().getItem(7);int initial=fuel.getCount();
            var menu=arm(h,p,pos);h.assertTrue(source.getCount()==2 && fuel.getCount()==initial,"Reviewing spends no source, heart or incense");menu.clickMenuButton(p,menu.buttonId(ApexPanel.CANCEL));
            menu=arm(h,p,pos);int stale=menu.buttonId(ApexPanel.SUMMON);source.set(DataComponents.CUSTOM_NAME,Component.literal("Changed source"));h.assertTrue(!menu.clickMenuButton(p,stale) && source.getCount()==2,"Changing the whole source disarms old confirmation");
            l.setBlock(pos.above(2),Blocks.STONE.defaultBlockState(),3);menu=arm(h,p,pos);menu.clickMenuButton(p,menu.buttonId(ApexPanel.SUMMON));h.assertTrue(source.getCount()==2 && fuel.getCount()==initial,"Blocked spawning preserves both costs");l.removeBlock(pos.above(2),false);
            var nested=new AtomicReference<ApexRitual.Status>();Consumer<EntityJoinLevelEvent> reject=e->{if(e.getEntity() instanceof Apex && e.getLevel()==l) {nested.set(ApexRitual.summon(p,pos,source));e.setCanceled(true);}};
            menu=arm(h,p,pos);NeoForge.EVENT_BUS.addListener(reject);try {menu.clickMenuButton(p,menu.buttonId(ApexPanel.SUMMON));}finally {NeoForge.EVENT_BUS.unregister(reject);}
            h.assertTrue(nested.get()==ApexRitual.Status.BUSY && source.getCount()==2 && fuel.getCount()==initial,"Canceled/reentrant spawning cannot charge or create a free boss");
            Consumer<EntityJoinLevelEvent> steal=e->{if(e.getEntity() instanceof Apex && e.getLevel()==l)p.getInventory().setItem(7,ItemStack.EMPTY);};
            menu=arm(h,p,pos);NeoForge.EVENT_BUS.addListener(steal);try {menu.clickMenuButton(p,menu.buttonId(ApexPanel.SUMMON));}finally {NeoForge.EVENT_BUS.unregister(steal);}
            h.assertTrue(source.getCount()==2 && l.getEntitiesOfClass(Apex.class,new AABB(pos).inflate(5)).isEmpty(),"Fuel removed by a spawn callback removes the unpaid Apex");p.getInventory().setItem(7,fuel);
            menu=arm(h,p,pos);h.assertTrue(menu.clickMenuButton(p,menu.buttonId(ApexPanel.SUMMON)),"A revalidated deliberate summon commits");var bosses=l.getEntitiesOfClass(Apex.class,new AABB(pos).inflate(5));h.assertTrue(bosses.size()==1 && source.getCount()==1 && fuel.getCount()==initial-1 && ((com.bunnidogs.mod.society.CrucibleHeartBlockEntity)l.getBlockEntity(pos)).ready(),"A successful ritual spends one source and incense and retains its actual Keystone");bosses.forEach(Apex::discard);p.discard();
        }finally {l.getServer().setDifficulty(previous,true);}
    }
}
