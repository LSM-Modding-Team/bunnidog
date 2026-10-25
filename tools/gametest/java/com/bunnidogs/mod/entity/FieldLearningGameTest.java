package com.bunnidogs.mod.entity;

import com.bunnidogs.mod.block.*;
import com.bunnidogs.mod.genetics.*;
import com.bunnidogs.mod.item.SeedItem;
import com.bunnidogs.mod.menu.*;
import com.bunnidogs.mod.registry.*;
import java.util.function.Consumer;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.storage.*;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.block.BreakBlockEvent;

/** Native player actions teach only after their real outcomes; work still needs inherited capacity. */
public final class FieldLearningGameTest {
    private record Context(GameTestHelper h,Bunnidog dog,ServerPlayer p,BlockPos bed,BlockPos stock,BlockPos plot) {
        void later(int ticks,Runnable action) {h.runAfterDelay(ticks,()->day(this,action));}
        FeedingTroughBlockEntity trough() {return (FeedingTroughBlockEntity)h.getLevel().getBlockEntity(stock);}
        void finish() {dog.discard();p.discard();h.succeed();}
    }
    private static void day(Context c,Runnable action) {
        var level=c.h.getLevel();var clock=level.dimensionType().defaultClock().orElseThrow();long old=level.clockManager().getTotalTicks(clock);
        try {level.clockManager().setTotalTicks(clock,1000);action.run();}finally {level.clockManager().setTotalTicks(clock,old);}
    }
    private static Context setup(GameTestHelper h,boolean field,Consumer<Context> action) {
        var l=h.getLevel();for(int x=25;x<42;x++)for(int z=25;z<41;z++) {h.setBlock(x,259,z,Blocks.STONE);for(int y=260;y<265;y++)h.setBlock(x,y,z,Blocks.AIR);}
        var bed=h.absolutePos(new BlockPos(29,260,28));var stock=h.absolutePos(new BlockPos(30,260,34));var plot=h.absolutePos(new BlockPos(32,260,33));
        l.setBlock(bed,ModBlocks.NEST.get().defaultBlockState(),3);l.setBlock(stock,ModBlocks.FEEDING_TROUGH.get().defaultBlockState().setValue(FeedingTroughBlock.TENDING,true),3);
        l.setBlock(plot.below(),Blocks.FARMLAND.defaultBlockState().setValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.MOISTURE,7),3);
        var dog=ModEntities.BUNNIDOG.get().create(l,EntitySpawnReason.COMMAND);var genes=Genome.DEFAULT;
        for(var gene:new Gene[]{Gene.EARS,Gene.DIET}) {int id=gene.alleleNamed(gene==Gene.EARS ? "bat" : "forager").id();genes=genes.with(gene,0,id).with(gene,1,id);}
        dog.setGenome(genes);dog.setStats(Stats.of(0,0,0,20,20));dog.setAge(0);dog.setHealth(dog.getMaxHealth());dog.setNoAi(true);dog.snapTo(Vec3.atBottomCenterOf(h.absolutePos(new BlockPos(31,260,31))));dog.setOnGround(true);l.addFreshEntity(dog);
        var player=ApexTypesGameTest.player(h,Vec3.atBottomCenterOf(h.absolutePos(new BlockPos(34,260,32))));var c=new Context(h,dog,player,bed,stock,plot);
        c.trough().setWorkMode(TroughWork.GARDEN);
        c.later(2,()->{if(!field)h.assertTrue(dog.moveHomeTo(l,bed)==NestHome.MoveResult.MOVED,"Practical home lessons use a real claimed Nest");action.accept(c);});return c;
    }
    private static void teach(Context c,Learning.Routine routine) {
        c.p.closeContainer();CareMenuGameTest.stow(c.p);c.p.snapTo(c.dog.position().add(1,0,0));CareMenu.open(c.p,c.dog);var m=(CareMenu)c.p.containerMenu;
        c.h.assertTrue(m.clickMenuButton(c.p,m.buttonId(CarePanels.ROLES)) && m.clickMenuButton(c.p,m.buttonId(LearningPanel.OPEN)),"Lessons open through the actual animal conversation");
        c.h.assertTrue(m.clickMenuButton(c.p,m.buttonId(LearningPanel.teachId(routine))) && c.p.containerMenu!=m && c.dog.learning().lesson()==routine,"The real lesson action starts the intended routine and closes conversation");
        c.p.snapTo(Vec3.atBottomCenterOf(c.plot.east(2)));
    }
    private static void use(Context c,BlockPos pos) {c.p.gameMode.useItemOn(c.p,c.h.getLevel(),c.p.getMainHandItem(),InteractionHand.MAIN_HAND,new BlockHitResult(Vec3.atCenterOf(pos),Direction.UP,pos,false));}
    private static void ripe(Context c) {c.h.getLevel().setBlock(c.plot,Blocks.CARROTS.defaultBlockState().setValue(CropBlock.AGE,7),3);}
    public static void harvest(GameTestHelper h) {
        setup(h,false,c->{
            var genes=c.dog.genome();ripe(c);teach(c,Learning.Routine.HARVEST);h.assertTrue(c.p.gameMode.destroyBlock(c.plot),"The player actually harvests ripe native carrots");
            c.later(3,()->{
                h.assertTrue(c.dog.learning().knowledge().progress(Learning.Routine.HARVEST)==5,"A visible real harvest adds normal personal practice");
                ripe(c);h.assertTrue(c.p.gameMode.destroyBlock(c.plot),"A second real harvest completes the practical example");
                c.later(3,()->{h.assertTrue(c.dog.learning().knowledge().independent(c.dog.mind(),Learning.Routine.HARVEST) && c.dog.learning().lesson()==null && c.dog.genome().equals(genes),"Successful examples teach independence without changing inherited capacity");ripe(c);h.assertTrue(new GardenGoal(c.dog).canUse(),"The observed graduate can select the existing real autonomous harvest job");c.finish();});
            });
        });
    }
    public static void garden(GameTestHelper h) {
        setup(h,false,c->{
            int grazer=Gene.DIET.alleleNamed("grazer").id();c.dog.setGenome(c.dog.genome().with(Gene.DIET,0,grazer).with(Gene.DIET,1,grazer));
            teach(c,Learning.Routine.GARDEN);var seeds=new ItemStack(Items.WHEAT_SEEDS,3);c.p.setItemInHand(InteractionHand.MAIN_HAND,seeds);use(c,c.plot.below());
            h.assertTrue(seeds.getCount()==2 && h.getLevel().getBlockState(c.plot).is(Blocks.WHEAT),"Native seed planting spends one actual seed");
            c.later(3,()->{h.assertTrue(c.dog.learning().knowledge().progress(Learning.Routine.GARDEN)==5,"Watching real planting teaches the existing Garden routine");var meal=new ItemStack(Items.BONE_MEAL,3);c.p.setItemInHand(InteractionHand.MAIN_HAND,meal);use(c,c.plot);
                c.later(3,()->{h.assertTrue(meal.getCount()==2 && c.dog.learning().knowledge().knows(Learning.Routine.GARDEN),"Paid bone-meal growth provides a distinct real example and completes learning: meal="+meal.getCount()+", progress="+c.dog.learning().knowledge()+", plant="+h.getLevel().getBlockState(c.plot));c.finish();});
            });
        });
    }
    public static void nursery(GameTestHelper h) {
        setup(h,false,c->{
            h.getLevel().setBlock(c.plot,ModBlocks.NURSERY_CRADLE.get().defaultBlockState(),3);var cradle=(NurseryCradleBlockEntity)h.getLevel().getBlockEntity(c.plot);
            var seed=SeedItem.create(new Seed(Genome.DEFAULT,Stats.ZERO,Genome.DEFAULT,Stats.ZERO,1));h.assertTrue(cradle.insert(seed),"An actual litter is present for instruction");
            teach(c,Learning.Routine.NURSERY);var meals=new ItemStack(Items.BONE_MEAL,3);c.p.setItemInHand(InteractionHand.MAIN_HAND,meals);use(c,c.plot);
            c.later(3,()->{h.assertTrue(meals.getCount()==2 && cradle.age()>0 && c.dog.learning().knowledge().progress(Learning.Routine.NURSERY)==5,"Paid held-item care advances that litter and teaches once");
                var menu=CareMenuGameTest.open(h,c.p,c.plot);h.assertTrue(menu.clickMenuButton(c.p,menu.buttonId(CarePanels.GROW)),"The existing nursery menu also performs actual paid care");c.p.closeContainer();
                c.later(3,()->{h.assertTrue(c.dog.learning().knowledge().knows(Learning.Routine.NURSERY) && ItemStack.matches(cradle.storedSeed(),seed),"World and native-menu examples share the same litter and personal learning, without hatching or replacing it");c.finish();});
            });
        });
    }
    public static void safety(GameTestHelper h) {
        setup(h,false,c->{
            ripe(c);teach(c,Learning.Routine.HARVEST);Consumer<BreakBlockEvent> deny=e->{if(e.getPlayer()==c.p)e.setCanceled(true);};NeoForge.EVENT_BUS.addListener(deny);
            try {h.assertTrue(!c.p.gameMode.destroyBlock(c.plot),"Native protection cancels the attempted harvest");}finally {NeoForge.EVENT_BUS.unregister(deny);}
            h.getLevel().setBlock(c.plot,Blocks.CARROTS.defaultBlockState(),3);c.p.gameMode.destroyBlock(c.plot);
            c.later(3,()->{h.assertTrue(c.dog.learning().knowledge().equals(Learning.EMPTY),"Cancelled work and breaking an immature plant give no experience");
                ripe(c);var wall=c.plot.north();h.getLevel().setBlock(wall,Blocks.STONE.defaultBlockState(),3);h.getLevel().setBlock(wall.above(),Blocks.STONE.defaultBlockState(),3);c.p.gameMode.destroyBlock(c.plot);h.getLevel().removeBlock(wall,false);h.getLevel().removeBlock(wall.above(),false);
                c.later(3,()->{h.assertTrue(c.dog.learning().knowledge().equals(Learning.EMPTY),"An occluded native harvest cannot teach through a wall");ripe(c);c.p.gameMode.destroyBlock(c.plot);
                    var saved=TagValueOutput.createWithContext(ProblemReporter.DISCARDING,h.getLevel().registryAccess());c.dog.saveWithoutId(saved);c.dog.load(TagValueInput.create(ProblemReporter.DISCARDING,h.getLevel().registryAccess(),saved.buildResult()));
                    c.later(3,()->{h.assertTrue(c.dog.learning().lesson()==null && c.dog.learning().knowledge().equals(Learning.EMPTY),"Reload clears unconfirmed examples and the transient teacher without inventing experience");c.finish();});
                });
            });
        });
    }
    public static void scout(GameTestHelper h) {
        setup(h,true,c->{
            h.assertTrue(c.dog.homeNest()==null && c.dog.scoutingStatus()==Scouting.Status.LESSON,"A fresh qualified cave animal needs personal experience, not a home");
            teach(c,Learning.Routine.SCOUT);c.p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(Items.IRON_PICKAXE));h.getLevel().setBlock(c.plot,Blocks.COAL_ORE.defaultBlockState(),3);h.assertTrue(c.p.gameMode.destroyBlock(c.plot),"The field instructor actually mines a visible ore");
            c.later(3,()->{h.assertTrue(c.dog.learning().knowledge().progress(Learning.Routine.SCOUT)==5,"Real ore mining teaches a personal scouting cue");h.getLevel().setBlock(c.plot,Blocks.IRON_ORE.defaultBlockState(),3);c.p.gameMode.destroyBlock(c.plot);
                c.later(3,()->{
                    h.assertTrue(c.dog.learning().knowledge().independent(c.dog.mind(),Learning.Routine.SCOUT) && c.dog.learning().lesson()==null,"A real second example teaches independent surveying without a Nest");
                    h.getLevel().setBlock(c.plot,Blocks.DIAMOND_ORE.defaultBlockState(),3);c.p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(Items.COMPASS));c.p.setShiftKeyDown(true);MineralScouting.use(c.dog,c.p,InteractionHand.MAIN_HAND);
                    h.assertTrue(c.p.getMainHandItem().has(DataComponents.LODESTONE_TRACKER) && c.dog.scoutingWait()==Scouting.RECOVERY_TICKS,"Only the learned survey binds the actual compass and starts ordinary recovery");
                    LooseFoodGameTest.ready(c.dog,"ScoutWait");var body=c.dog.genome();c.dog.setGenome(new Genome(body.packed(),0));h.assertTrue(c.dog.scoutingStatus()==Scouting.Status.LIMITED && !c.dog.learning().begin(c.p,Learning.Routine.SCOUT),"A limiting splice preserves knowledge but prevents advanced surveying");
                    Genome supported=null;for(int code=0;code<256 && supported==null;code++){int word=0;for(int i=0;i<4;i++)word|=(((code>>>(i*2))&3)*17)<<(i*8);var candidate=new Genome(body.packed(),word);if(CapabilityRules.potential(candidate.mind(),CapabilityRules.Task.SCOUTING)==CapabilityRules.Potential.SUPPORTED)supported=candidate;}
                    h.assertTrue(supported!=null,"The actual multilocus allele table contains supported scouts");c.dog.setGenome(supported);teach(c,Learning.Routine.SCOUT);h.assertTrue(c.dog.scoutingStatus()==Scouting.Status.READY,"A knowledgeable supported scout works with its present guide");c.p.snapTo(c.p.position().add(20,0,0));h.assertTrue(c.dog.scoutingStatus()==Scouting.Status.GUIDANCE,"The inherited guide requirement survives learning");c.finish();
                });
            });
        });
    }
    public static void gathering(GameTestHelper h) {
        setup(h,false,c->{
            c.trough().setWorkMode(TroughWork.GATHER);var genes=c.dog.genome();for(var gene:CognitiveGene.values())for(int i=0;i<2;i++)genes=genes.with(gene,i,2);c.dog.setGenome(genes);
            var goods=new ItemStack(Items.CARROT,3);goods.set(DataComponents.CUSTOM_NAME,net.minecraft.network.chat.Component.literal("Lesson carrots"));var at=c.stock.east();var item=new ItemEntity(h.getLevel(),at.getX()+.5,at.getY()+.1,at.getZ()+.5,goods.copy());item.setNoGravity(true);item.setDeltaMovement(Vec3.ZERO);item.setNoPickUpDelay();LooseFoodGameTest.age(item,Scavenging.MIN_ITEM_AGE);h.getLevel().addFreshEntity(item);
            h.assertTrue(!new LooseFoodGoal(c.dog,true).canUse(),"High Senses alone cannot bypass personal Harvest learning through loose-food collection");teach(c,Learning.Routine.HARVEST);
            var goal=new LooseFoodGoal(c.dog,true);h.assertTrue(goal.canUse(),"A compatible pupil can practice collecting the actual loose food with its guide");goal.start();c.dog.snapTo(Vec3.atBottomCenterOf(at));for(int i=0;i<Gardening.WORK_TICKS;i++)goal.tick();goal.stop();
            h.assertTrue(c.trough().food().getCount()==1 && item.getItem().getCount()==2 && ItemStack.isSameItemSameComponents(c.trough().food(),goods) && c.dog.learning().knowledge().knows(Learning.Routine.HARVEST),"One committed collection preserves exact stock and teaches actual work");
            c.dog.setGenome(new Genome(c.dog.genome().packed(),0));c.dog.setHealth(c.dog.getMaxHealth()-4);h.assertTrue(c.dog.canForage(),"Even a limited animal retains instinctive food and recovery");c.finish();
        });
    }
}
