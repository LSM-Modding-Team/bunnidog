package com.bunnidogs.mod.entity;

import com.bunnidogs.mod.block.FeedingTroughBlock;
import com.bunnidogs.mod.block.FeedingTroughBlockEntity;
import com.bunnidogs.mod.genetics.Foraging;
import com.bunnidogs.mod.genetics.Gardening;
import com.bunnidogs.mod.genetics.Gene;
import com.bunnidogs.mod.genetics.Genome;
import com.bunnidogs.mod.genetics.Stats;
import com.bunnidogs.mod.registry.ModBlocks;
import com.bunnidogs.mod.registry.ModEntities;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingDestroyBlockEvent;
import net.neoforged.neoforge.event.level.BlockEvent;

/** Real dry-bank work and meals: preserve a living root/source water, conserve sowing costs and respect callbacks. */
public final class ShoreCareGameTest {
    private ShoreCareGameTest() {}
    private record Context(ServerLevel level,BlockPos base,BlockPos top,BlockPos root,FeedingTroughBlockEntity trough,Bunnidog dog) {}
    private static Context setup(GameTestHelper h,int offset) {
        var level=h.getLevel(); var base=h.absolutePos(new BlockPos(1280+offset,40,10));
        for (int x=(base.getX()-16)>>4;x<=(base.getX()+24)>>4;x++) for (int z=(base.getZ()-16)>>4;z<=(base.getZ()+24)>>4;z++) level.getChunk(x,z);
        for (int x=0;x<9;x++) for (int z=0;z<9;z++) {
            for (int y=-2;y<=0;y++) level.setBlock(base.offset(x,y,z),Blocks.SAND.defaultBlockState(),2);
            for (int y=1;y<=4;y++) level.setBlock(base.offset(x,y,z),Blocks.AIR.defaultBlockState(),2);
        }
        var nest=base.offset(1,0,2); level.setBlock(nest,ModBlocks.NEST.get().defaultBlockState(),3);
        var trough=base.offset(3,1,4); level.setBlock(trough,ModBlocks.FEEDING_TROUGH.get().defaultBlockState().setValue(FeedingTroughBlock.TENDING,true),3);
        var top=base.offset(5,0,4); var root=top.below();
        level.setBlock(top,Blocks.WATER.defaultBlockState(),3); level.setBlock(root,Blocks.KELP_PLANT.defaultBlockState(),3); level.setBlock(top,Blocks.KELP.defaultBlockState(),3);
        var dog=ModEntities.BUNNIDOG.get().create(level,EntitySpawnReason.COMMAND);
        int diet=Gene.DIET.alleleNamed("shore_forager").id(); dog.setGenome(Genome.DEFAULT.with(Gene.DIET,0,diet).with(Gene.DIET,1,diet));
        dog.setAge(0); dog.setNoAi(true); dog.setHealth(dog.getMaxHealth()); dog.setPos(Vec3.atBottomCenterOf(base.offset(4,1,4))); dog.setOnGround(true);
        level.addFreshEntity(dog);
        h.assertTrue(dog.moveHomeTo(level,nest)==NestHome.MoveResult.MOVED,"A shore worker claims its real dry home");
        return new Context(level,base,top,root,(FeedingTroughBlockEntity)level.getBlockEntity(trough),dog);
    }
    private static void ready(Context c) {
        var out=TagValueOutput.createWithContext(ProblemReporter.DISCARDING,c.level.registryAccess()); c.dog.saveWithoutId(out);
        var saved=out.buildResult(); saved.remove("GardenWait"); saved.remove("ForageWait");
        c.dog.load(TagValueInput.create(ProblemReporter.DISCARDING,c.level.registryAccess(),saved));
        c.dog.setPos(Vec3.atBottomCenterOf(c.base.offset(4,1,4))); c.dog.setOnGround(true);
    }
    private static void work(Context c,GardenGoal goal) {
        goal.start(); c.dog.setPos(Vec3.atBottomCenterOf(c.base.offset(4,1,4))); c.dog.setOnGround(true);
        for (int i=0;i<Gardening.WORK_TICKS;i++) goal.tick(); goal.stop();
    }
    private static void eat(Context c,GrazeGoal goal) {
        goal.start(); c.dog.setPos(Vec3.atBottomCenterOf(c.base.offset(4,1,4))); c.dog.setOnGround(true);
        for (int i=0;i<Foraging.GRAZE_TICKS;i++) goal.tick(); goal.stop();
    }
    public static void garden(GameTestHelper h) {
        var level=h.getLevel(); var clock=level.dimensionType().defaultClock().orElseThrow(); long time=level.clockManager().getTotalTicks(clock);
        boolean grief=level.getGameRules().get(GameRules.MOB_GRIEFING);
        try {
            level.clockManager().setTotalTicks(clock,1000); level.getGameRules().set(GameRules.MOB_GRIEFING,true,level.getServer());
            var c=setup(h,0); var goal=new GardenGoal(c.dog);
            h.assertTrue(goal.canUse(),"A healthy shore resident finds the actual upper kelp from a dry bank");
            Consumer<LivingDestroyBlockEvent> deny=e->{ if(e.getEntity()==c.dog&&e.getPos().equals(c.top)) e.setCanceled(true); };
            NeoForge.EVENT_BUS.addListener(deny);
            try { work(c,goal); } finally { NeoForge.EVENT_BUS.unregister(deny); }
            h.assertTrue(level.getBlockState(c.top).is(Blocks.KELP)&&c.trough.food().isEmpty(),"Native protection retains plant and stock");
            ready(c); goal=new GardenGoal(c.dog); h.assertTrue(goal.canUse(),"The retained plant remains available after protection is lifted"); work(c,goal);
            h.assertTrue(c.trough.food().is(Items.KELP)&&c.trough.food().getCount()==1&&level.getBlockState(c.top).is(Blocks.WATER)
                    &&level.getFluidState(c.top).isSource()&&level.getBlockState(c.root).is(Blocks.KELP),
                    "One actual upper plant becomes one stored kelp while its living native root and source water remain");
            ready(c); h.assertTrue(!new GardenGoal(c.dog).canUse(),"The final rooted plant is never harvested");
            c.trough.takeAll(); c.trough.insert(new ItemStack(Items.KELP,9)); c.dog.setStats(Stats.of(0,0,0,0,10));
            c.trough.setWorkMode(com.bunnidogs.mod.genetics.TroughWork.GARDEN);
            level.setBlock(c.root,Blocks.WATER.defaultBlockState(),3); level.setBlock(c.top,Blocks.WATER.defaultBlockState(),3); ready(c);
            goal=new GardenGoal(c.dog); h.assertTrue(goal.canUse(),"A developed resident finds a genuinely bare two-source planting cell");
            Consumer<BlockEvent.EntityPlaceEvent> denyPlant=e->{ if(e.getEntity()==c.dog&&e.getPos().equals(c.root)) e.setCanceled(true); };
            NeoForge.EVENT_BUS.addListener(denyPlant);
            try { work(c,goal); } finally { NeoForge.EVENT_BUS.unregister(denyPlant); }
            h.assertTrue(c.trough.food().getCount()==9&&level.getBlockState(c.root).is(Blocks.WATER),"Denied native water planting refunds its exact real stock");
            ready(c); goal=new GardenGoal(c.dog); h.assertTrue(goal.canUse(),"Allowed native planting resumes at the bare bed"); work(c,goal);
            h.assertTrue(c.trough.food().getCount()==8&&level.getBlockState(c.root).is(Blocks.KELP)&&level.getBlockState(c.top).is(Blocks.WATER)
                    &&ResidentPlanting.plan(level,c.top,new ItemStack(Items.KELP))==null,
                    "One kelp pays for one root, retains eight reserve and never sows above an already living stem");
            c.dog.discard();
        } finally { level.clockManager().setTotalTicks(clock,time); level.getGameRules().set(GameRules.MOB_GRIEFING,grief,level.getServer()); }
    }
    public static void meals(GameTestHelper h) {
        var level=h.getLevel(); var clock=level.dimensionType().defaultClock().orElseThrow(); long time=level.clockManager().getTotalTicks(clock);
        boolean grief=level.getGameRules().get(GameRules.MOB_GRIEFING);
        try {
            level.clockManager().setTotalTicks(clock,1000); level.getGameRules().set(GameRules.MOB_GRIEFING,true,level.getServer());
            var c=setup(h,128); c.dog.setHealth(c.dog.getMaxHealth()-2); float before=c.dog.getHealth();
            var goal=new GrazeGoal(c.dog); h.assertTrue(goal.canUse(),"An injured shore forager finds a renewable meal from its actual dry bank");
            eat(c,goal);
            h.assertTrue(c.dog.getHealth()==before+Foraging.HEAL&&c.dog.forageWait()>0&&c.trough.food().isEmpty()
                    &&level.getBlockState(c.top).is(Blocks.WATER)&&level.getBlockState(c.root).is(Blocks.KELP),
                    "A native shore meal heals once without duplicating produce or consuming the root");
            ready(c); c.dog.setHealth(before); h.assertTrue(!new GrazeGoal(c.dog).canUse(),"A hungry-for-care resident still preserves the final root");
            level.setBlock(c.root,Blocks.KELP_PLANT.defaultBlockState(),3); level.setBlock(c.top,Blocks.KELP.defaultBlockState(),3);
            level.getGameRules().set(GameRules.MOB_GRIEFING,false,level.getServer());
            h.assertTrue(!new GrazeGoal(c.dog).canUse(),"Native mob-griefing prevents kelp meals as it does crop meals");
            c.dog.discard();
        } finally { level.clockManager().setTotalTicks(clock,time); level.getGameRules().set(GameRules.MOB_GRIEFING,grief,level.getServer()); }
    }
}
