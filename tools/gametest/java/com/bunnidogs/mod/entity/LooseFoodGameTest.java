package com.bunnidogs.mod.entity;

import java.util.UUID;
import java.util.function.Consumer;
import com.bunnidogs.mod.genetics.Foraging;
import com.bunnidogs.mod.genetics.Gene;
import com.bunnidogs.mod.genetics.Genome;
import com.bunnidogs.mod.genetics.Scavenging;
import com.bunnidogs.mod.registry.ModBlocks;
import com.bunnidogs.mod.registry.ModEntities;
import com.bunnidogs.mod.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityMobGriefingEvent;

/** Real item stacks, reservations and callback changes; the animal never invents or equips food. */
public final class LooseFoodGameTest {
    private LooseFoodGameTest() {}
    static void age(ItemEntity item, int age) {
        var level = item.level(); var out = TagValueOutput.createWithContext(ProblemReporter.DISCARDING,level.registryAccess());
        item.saveWithoutId(out); var saved = out.buildResult(); saved.putShort("Age",(short)age);
        item.load(TagValueInput.create(ProblemReporter.DISCARDING,level.registryAccess(),saved));
    }
    static ItemEntity food(GameTestHelper h, ItemStack stack) {
        var pos = h.absolutePos(new BlockPos(5,1,4));
        var item = new ItemEntity(h.getLevel(),pos.getX()+.5,pos.getY()+.1,pos.getZ()+.5,stack);
        item.setNoGravity(true); item.setDeltaMovement(Vec3.ZERO); item.setNoPickUpDelay(); age(item,Scavenging.MIN_ITEM_AGE);
        h.getLevel().addFreshEntity(item); return item;
    }
    static void ready(Bunnidog dog, String key) {
        var level = dog.level(); var out = TagValueOutput.createWithContext(ProblemReporter.DISCARDING,level.registryAccess());
        dog.saveWithoutId(out); var saved = out.buildResult(); saved.remove(key);
        dog.load(TagValueInput.create(ProblemReporter.DISCARDING,level.registryAccess(),saved));
    }
    static void work(GameTestHelper h, Bunnidog dog, LooseFoodGoal goal, int ticks) {
        goal.start(); h.assertTrue(HomeRoutes.safe(dog,dog.getNavigation().getPath(),null),"Loose-food work needs a complete loaded home-bounded safe path");
        dog.setPos(Vec3.atBottomCenterOf(h.absolutePos(new BlockPos(4,1,4))));
        for (int i = 0; i < ticks; i++) goal.tick(); goal.stop();
    }
    static void clear(GameTestHelper h) {
        for (int x = 0; x < 8; x++) for (int z = 0; z < 8; z++) {
            h.setBlock(x,0,z,Blocks.STONE); for (int y = 1; y <= 5; y++) h.setBlock(x,y,z,Blocks.AIR);
        }
        h.setBlock(1,1,1,ModBlocks.NEST.get());
    }
    public static void run(GameTestHelper h) {
        clear(h); var level = h.getLevel();
        var dog = h.spawnWithNoFreeWill(ModEntities.BUNNIDOG.get(),3.5F,1,4.5F); dog.setAge(0); dog.setGenome(Genome.DEFAULT); dog.setHealth(dog.getMaxHealth()-4);
        var original = new ItemStack(ModItems.GUAVA.get(),3); original.set(DataComponents.CUSTOM_NAME,Component.literal("Windfall guavas"));
        var item = food(h,original.copy());
        h.runAfterDelay(2,() -> {
            var clock = level.dimensionType().defaultClock().orElseThrow(); long time = level.clockManager().getTotalTicks(clock);
            boolean griefing = level.getGameRules().get(GameRules.MOB_GRIEFING);
            try {
                level.clockManager().setTotalTicks(clock,1000); level.getGameRules().set(GameRules.MOB_GRIEFING,true,level.getServer());
                h.assertTrue(dog.moveHomeTo(level,h.absolutePos(new BlockPos(1,1,1))) == NestHome.MoveResult.MOVED,"Loose meals require a real home");
                age(item,Scavenging.MIN_ITEM_AGE-1); h.assertTrue(!new LooseFoodGoal(dog,false).canUse(),"Fresh drops must retain their full grace period"); age(item,Scavenging.MIN_ITEM_AGE);
                item.setDefaultPickUpDelay(); h.assertTrue(!new LooseFoodGoal(dog,false).canUse(),"Native pickup delays must be respected"); item.setNoPickUpDelay();
                item.setTarget(UUID.randomUUID()); h.assertTrue(!new LooseFoodGoal(dog,false).canUse(),"Owner-reserved drops must be left for their player"); item.setTarget(null);
                item.setItem(new ItemStack(ModItems.GOLDEN_GUAVA.get(),3)); h.assertTrue(!new LooseFoodGoal(dog,false).canUse(),"Upgraded breeding guavas must not be scavenged"); item.setItem(original.copy());
                level.clockManager().setTotalTicks(clock,13000); h.assertTrue(!new LooseFoodGoal(dog,false).canUse(),"Outdoor loose meals remain daytime only"); level.clockManager().setTotalTicks(clock,1000);
                level.getGameRules().set(GameRules.MOB_GRIEFING,false,level.getServer()); h.assertTrue(!new LooseFoodGoal(dog,false).canUse(),"Loose meals obey native griefing"); level.getGameRules().set(GameRules.MOB_GRIEFING,true,level.getServer());
                var interrupted = new LooseFoodGoal(dog,false); h.assertTrue(interrupted.canUse(),"An old plain guava must make a valid meal");
                Consumer<EntityMobGriefingEvent> reserveAtCommit = event -> { if (event.getEntity() == dog) item.setTarget(UUID.randomUUID()); };
                float health = dog.getHealth(); NeoForge.EVENT_BUS.addListener(reserveAtCommit);
                try { work(h,dog,interrupted,Foraging.GRAZE_TICKS); } finally { NeoForge.EVENT_BUS.unregister(reserveAtCommit); }
                h.assertTrue(ItemStack.matches(item.getItem(),original) && dog.getHealth() == health && dog.forageWait() == 0,"A reservation added in protection callbacks must cancel without food/healing/rest"); item.setTarget(null);
                var meal = new LooseFoodGoal(dog,false); h.assertTrue(meal.canUse(),"Released food must be available again"); work(h,dog,meal,Foraging.GRAZE_TICKS);
                h.assertTrue(ItemStack.matches(item.getItem(),original.copyWithCount(2)) && dog.getHealth() == health+Foraging.HEAL
                        && dog.forageWait() == Foraging.MEAL_COOLDOWN && !new LooseFoodGoal(dog,false).canUse(),"One real meal must preserve remaining names/components, heal two HP and share normal food rest");
                ready(dog,"ForageWait"); dog.setHealth(dog.getMaxHealth()-4);
                var stale = new LooseFoodGoal(dog,false); h.assertTrue(stale.canUse(),"An old remaining stack may be planned after recovery"); stale.start(); item.discard();
                for (int i = 0; i < Foraging.GRAZE_TICKS; i++) stale.tick(); stale.stop();
                h.assertTrue(dog.getHealth() == dog.getMaxHealth()-4 && dog.forageWait() == 0,"Disappearing food must never heal or recreate a meal");
                var carrot = food(h,new ItemStack(Items.CARROT)); h.assertTrue(!new LooseFoodGoal(dog,false).canUse(),"Plain Diet must refuse loose roots");
                int forager = Gene.DIET.alleleNamed("forager").id(); dog.setGenome(Genome.DEFAULT.with(Gene.DIET,0,forager).with(Gene.DIET,1,forager));
                var root = new LooseFoodGoal(dog,false); h.assertTrue(root.canUse(),"Matching Diets may take loose root meals"); work(h,dog,root,Foraging.GRAZE_TICKS);
                h.assertTrue(carrot.isRemoved() && dog.forageWait() == Foraging.MEAL_COOLDOWN,"The final consumed item must be discarded exactly once");
                h.succeed();
            } finally { level.clockManager().setTotalTicks(clock,time); level.getGameRules().set(GameRules.MOB_GRIEFING,griefing,level.getServer()); }
        });
    }
}
