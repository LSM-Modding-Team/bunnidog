package com.bunnidogs.mod.entity;

import com.bunnidogs.mod.genetics.*;
import com.bunnidogs.mod.registry.*;
import java.util.function.Consumer;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityMobGriefingEvent;
import net.neoforged.neoforge.event.level.BlockEvent.NeighborNotifyEvent;

/** Native permission callbacks and one original finite timber permission across reentrant block notifications. */
final class WildHarvestGameTest {
    static void capacity(GameTestHelper h) {
        var c=WildFamilyGameTest.setup(h,true);var worker=c.dogs().getFirst();var original=worker.genome();var home=c.home();
        var targets=java.util.List.of(new SocietyWork.Job(SocietyWork.Kind.CLIP_WOOD,home.wild().timber().getFirst(),-1,h.getLevel().getBlockState(home.wild().timber().getFirst())),new SocietyWork.Job(SocietyWork.Kind.PICK_FRUIT,home.wild().fruit().getFirst(),-1,h.getLevel().getBlockState(home.wild().fruit().getFirst())),new SocietyWork.Job(SocietyWork.Kind.HARVEST_FIELD,home.wild().fields().getFirst(),-1,h.getLevel().getBlockState(home.wild().fields().getFirst())));
        for(var target:targets)for(Runnable invalidate:new Runnable[]{()->worker.setGenome(new Genome(original.packed(),0)),()->worker.snapTo(net.minecraft.world.phys.Vec3.atBottomCenterOf(home.getBlockPos().south(5)))}) {
            boolean[] called={false};Consumer<EntityMobGriefingEvent> limit=e->{if(e.getEntity()==worker) {called[0]=true;invalidate.run();}};NeoForge.EVENT_BUS.addListener(limit);
            try {h.assertTrue(!WildFamilyGameTest.act(c,worker,target.kind(),target.target()) && called[0] && h.getLevel().getBlockState(target.target()).equals(target.expected()) && home.wild().timberAvailable(home.wild().timber().getFirst()) && worker.learning().knowledge().equals(Learning.EMPTY) && h.getLevel().getEntitiesOfClass(ItemEntity.class,new AABB(home.getBlockPos()).inflate(24)).isEmpty(),"A real protection callback removing harvest capacity or reach preserves the actual "+target.kind()+" source, finite permission, material and learning");}finally {NeoForge.EVENT_BUS.unregister(limit);worker.setGenome(original);}
        }
        h.assertTrue(WildFamilyGameTest.act(c,worker,SocietyWork.Kind.HARVEST_FIELD,home.wild().fields().getFirst()),"Restoring the real qualified worker permits the same original crop after refused callbacks");WildFamilyGameTest.clean(c);h.succeed();
    }
    static void receipt(GameTestHelper h) {
        var c=WildFamilyGameTest.setup(h,true);var home=c.home();var worker=c.dogs().getFirst();var helper=c.dogs().get(1);var log=home.wild().timber().getFirst();boolean[] called={false},paid={false},reentered={false};
        Consumer<NeighborNotifyEvent> replace=e->{if(!called[0] && e.getLevel()==h.getLevel() && e.getPos().equals(log) && h.getLevel().getBlockState(log).isAir()) {called[0]=true;paid[0]=!home.wild().timberAvailable(log);h.getLevel().setBlock(log,ModBlocks.GUAVA_LOG.get().defaultBlockState(),3);reentered[0]=WildFamilyGameTest.act(c,helper,SocietyWork.Kind.CLIP_WOOD,log);}};NeoForge.EVENT_BUS.addListener(replace);
        try {h.assertTrue(WildFamilyGameTest.act(c,worker,SocietyWork.Kind.CLIP_WOOD,log) && called[0] && paid[0] && !reentered[0],"Native removal notifications see the original timber permission already spent; replacing the block cannot let a second real worker spend it again");}finally {NeoForge.EVENT_BUS.unregister(replace);}
        var saved=home.saveWithFullMetadata(h.getLevel().registryAccess());home.loadWithComponents(TagValueInput.create(ProblemReporter.DISCARDING,h.getLevel().registryAccess(),saved));
        int logs=h.getLevel().getEntitiesOfClass(ItemEntity.class,new AABB(home.getBlockPos()).inflate(24),item->item.getItem().is(ModItems.GUAVA_LOG.get()) && home.site().toString().equals(item.getItem().get(ModItems.FARM_ORIGIN.get()))).stream().mapToInt(item->item.getItem().getCount()).sum();
        h.assertTrue(logs==1 && !home.wild().timberAvailable(log) && h.getLevel().getBlockState(log).is(ModBlocks.GUAVA_LOG.get()) && !WildFamilyGameTest.act(c,helper,SocietyWork.Kind.CLIP_WOOD,log),"One original log yields one actual marked item; native reload preserves the spent receipt and the replacement block remains protected");WildFamilyGameTest.clean(c);h.succeed();
    }
}
