package com.bunnidogs.mod.entity;

import com.bunnidogs.mod.genetics.Gene;
import com.bunnidogs.mod.genetics.Genome;
import com.bunnidogs.mod.genetics.Gardening;
import com.bunnidogs.mod.genetics.Stats;
import com.bunnidogs.mod.genetics.Trail;
import com.bunnidogs.mod.item.CalipersItem;
import com.bunnidogs.mod.item.TravelBasketItem;
import com.bunnidogs.mod.registry.ModBlocks;
import com.bunnidogs.mod.registry.ModEntities;
import com.bunnidogs.mod.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.phys.Vec3;

/** Actual held-item training/ownership, native saved travel and real path-driven excursions. */
public final class TrailGameTest {
    private TrailGameTest() {}
    private static Genome temperament(String name) {
        int id=Gene.TEMPERAMENT.alleleNamed(name).id();
        return Genome.DEFAULT.with(Gene.TEMPERAMENT,0,id).with(Gene.TEMPERAMENT,1,id);
    }
    private static void floor(GameTestHelper h,int y,int length) {
        for (int x=-1;x<=length;x++) for (int z=-2;z<=3;z++) {
            h.setBlock(x,y,z,Blocks.STONE);
            for (int up=1;up<=4;up++) h.setBlock(x,y+up,z,Blocks.AIR);
        }
    }
    private static net.minecraft.nbt.CompoundTag saved(Bunnidog dog) {
        var out=TagValueOutput.createWithContext(ProblemReporter.DISCARDING,dog.registryAccess()); dog.saveWithoutId(out); return out.buildResult();
    }
    public static void training(GameTestHelper h) {
        var level=h.getLevel(); int y=302; floor(h,y,8);
        var dog=h.spawnWithNoFreeWill(ModEntities.BUNNIDOG.get(),2,y+1,1); dog.setGenome(Genome.DEFAULT);
        dog.setAge(0); dog.setOnGround(true); dog.setHealth(dog.getMaxHealth());
        var keeper=h.makeMockServerPlayerInLevel(); keeper.gameMode.changeGameModeForPlayer(GameType.SURVIVAL);
        keeper.setPos(dog.position().add(1,0,0));
        var whistle=new ItemStack(ModItems.TRAIL_WHISTLE.get()); keeper.setItemInHand(InteractionHand.MAIN_HAND,whistle);
        var guavas=new ItemStack(ModItems.GUAVA.get(),3); guavas.set(DataComponents.CUSTOM_NAME,Component.literal("Training stock"));
        keeper.setItemInHand(InteractionHand.OFF_HAND,guavas);
        dog.setBaby(true); keeper.interactOn(dog,InteractionHand.MAIN_HAND,Vec3.ZERO);
        h.assertTrue(dog.trail().keeper()==null&&guavas.getCount()==3,"Pups retain training stock"); dog.setAge(0);
        dog.setGenome(temperament("feral")); keeper.interactOn(dog,InteractionHand.MAIN_HAND,Vec3.ZERO);
        h.assertTrue(dog.trail().keeper()==null&&guavas.getCount()==3,"Expressed unsuitable temperament refuses training"); dog.setGenome(Genome.DEFAULT);
        keeper.gameMode.changeGameModeForPlayer(GameType.ADVENTURE); keeper.interactOn(dog,InteractionHand.MAIN_HAND,Vec3.ZERO);
        h.assertTrue(dog.trail().keeper()==null&&guavas.getCount()==3,"Native Adventure permission cannot train"); keeper.gameMode.changeGameModeForPlayer(GameType.SURVIVAL);
        var nest=h.absolutePos(new BlockPos(2,y,1)); level.setBlock(nest,ModBlocks.NEST.get().defaultBlockState(),3);
        h.assertTrue(dog.moveHomeTo(level,nest)==NestHome.MoveResult.MOVED,"The trained resident begins with a real claimed Nest");
        keeper.interactOn(dog,InteractionHand.MAIN_HAND,Vec3.ZERO);
        h.assertTrue(dog.trail().belongsTo(keeper)&&dog.trail().order()==Trail.Order.FOLLOW&&guavas.getCount()==2
                &&dog.homeNest().equals(nest)&&dog.gardeningStatus()==Gardening.Status.TRAIL,"Real training spends one actual guava and retains home while pausing chores");
        var goal=level.getServer().getAdvancements().get(Identifier.fromNamespaceAndPath("bunnidogs","trail_partner"));
        h.assertTrue(keeper.getAdvancements().getOrStartProgress(goal).isDone(),"Native trail training goal is earned");
        var stranger=h.makeMockServerPlayerInLevel(); stranger.gameMode.changeGameModeForPlayer(GameType.SURVIVAL);
        stranger.setPos(keeper.position()); stranger.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(ModItems.TRAIL_WHISTLE.get()));
        stranger.setItemInHand(InteractionHand.OFF_HAND,new ItemStack(ModItems.GUAVA.get(),4)); stranger.setShiftKeyDown(true);
        stranger.interactOn(dog,InteractionHand.MAIN_HAND,Vec3.ZERO);
        h.assertTrue(dog.trail().belongsTo(keeper)&&dog.trail().order()==Trail.Order.FOLLOW&&stranger.getOffhandItem().getCount()==4,
                "Another keeper cannot release or overwrite training");
        keeper.interactOn(dog,InteractionHand.MAIN_HAND,Vec3.ZERO);
        h.assertTrue(dog.trail().order()==Trail.Order.WAIT&&guavas.getCount()==2,"Owner controls cost no more food");
        var before=saved(dog); CalipersItem.showCare(keeper,dog);
        h.assertTrue(saved(dog).equals(before),"Trail care inspection never changes saved animal state");
        dog.load(TagValueInput.create(ProblemReporter.DISCARDING,level.registryAccess(),before));
        h.assertTrue(dog.trail().belongsTo(keeper)&&dog.trail().order()==Trail.Order.WAIT&&dog.homeNest().equals(nest),"Native load preserves training, order and Nest independently");
        var basket=new ItemStack(ModItems.TRAVEL_BASKET.get()); var uuid=dog.getUUID();
        h.assertTrue(TravelBasketItem.capture(level,basket,dog)==TravelBasketItem.Result.CAPTURED,"Ordinary baskets can pack a trained waiting adult");
        var arrival=h.absolutePos(new BlockPos(6,y+1,1));
        h.assertTrue(TravelBasketItem.release(level,arrival,basket)==TravelBasketItem.Result.RELEASED,"Native safe basket release succeeds");
        var restored=(Bunnidog)level.getEntity(uuid);
        h.assertTrue(restored!=null&&restored.trail().belongsTo(keeper)&&restored.trail().order()==Trail.Order.WAIT,
                "Whole-animal basket travel conserves exact training and order");
        keeper.setPos(restored.position().add(1,0,0)); keeper.setShiftKeyDown(true); keeper.interactOn(restored,InteractionHand.MAIN_HAND,Vec3.ZERO);
        h.assertTrue(restored.trail().keeper()==null&&restored.trail().order()==Trail.Order.RESIDENT,"Only the keeper can release training");
        keeper.setShiftKeyDown(false); keeper.gameMode.changeGameModeForPlayer(GameType.CREATIVE);
        keeper.interactOn(restored,InteractionHand.MAIN_HAND,Vec3.ZERO);
        h.assertTrue(restored.trail().belongsTo(keeper)&&guavas.getCount()==2,"Creative training preserves actual off-hand guavas");
        var legacy=saved(restored); legacy.putString("TrailKeeper","invalid uuid"); legacy.putString("TrailOrder","follow");
        restored.load(TagValueInput.create(ProblemReporter.DISCARDING,level.registryAccess(),legacy));
        h.assertTrue(restored.trail().keeper()==null&&!restored.trail().ordered(),"Invalid/legacy training cannot lock an animal in a phantom order");
        restored.discard();
    }
    public static void walking(GameTestHelper h) {
        var level=h.getLevel(); int y=244; floor(h,y,31);
        var dog=h.spawn(ModEntities.BUNNIDOG.get(),2,y+1,1); dog.setGenome(Genome.DEFAULT); dog.setStats(Stats.ZERO);
        dog.setAge(0); dog.setHealth(dog.getMaxHealth()); dog.setOnGround(true);
        var keeper=h.makeMockServerPlayerInLevel(); keeper.gameMode.changeGameModeForPlayer(GameType.SURVIVAL);
        keeper.setPos(dog.position().add(1,0,0)); keeper.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(ModItems.TRAIL_WHISTLE.get()));
        keeper.setItemInHand(InteractionHand.OFF_HAND,new ItemStack(ModItems.GUAVA.get(),3));
        var nest=h.absolutePos(new BlockPos(1,y,1)); level.setBlock(nest,ModBlocks.NEST.get().defaultBlockState(),3);
        h.assertTrue(dog.moveHomeTo(level,nest)==NestHome.MoveResult.MOVED,"The walking partner starts with a real housing ticket");
        keeper.interactOn(dog,InteractionHand.MAIN_HAND,Vec3.ZERO);
        var destination=h.absolutePos(new BlockPos(28,y+1,1)); keeper.setPos(Vec3.atBottomCenterOf(destination));
        double[] stopped={0,0};
        h.startSequence().thenWaitUntil(()->{
            h.assertTrue(dog.blockPosition().getX()-nest.getX()>16,"A real goal must walk outside the retained home boundary: "+dog.position());
        }).thenExecute(()->{
            h.assertTrue(dog.homeNest().equals(nest)&&dog.trail().order()==Trail.Order.FOLLOW,"The excursion never gives up or reclaims housing");
            keeper.setPos(dog.position().add(1,0,0)); keeper.interactOn(dog,InteractionHand.MAIN_HAND,Vec3.ZERO);
            h.assertTrue(dog.trail().order()==Trail.Order.WAIT,"Native held-whistle interaction pauses the real walker");
        }).thenIdle(4).thenExecute(()->{
            stopped[0]=dog.getX(); stopped[1]=dog.getZ();
        }).thenIdle(10).thenExecute(()->{
            h.assertTrue(Math.abs(dog.getX()-stopped[0])<.3&&Math.abs(dog.getZ()-stopped[1])<.3,"Wait holds after native braking instead of home tether or wandering: "
                    +(dog.getX()-stopped[0])+", "+(dog.getZ()-stopped[1])+"; order="+dog.trail().order());
            keeper.setShiftKeyDown(true); ModItems.TRAIL_WHISTLE.get().use(level,keeper,InteractionHand.MAIN_HAND);
            h.assertTrue(dog.trail().order()==Trail.Order.RESIDENT&&dog.trail().belongsTo(keeper),"Sneak-air resumes resident routines without clearing training");
            ModItems.TRAIL_WHISTLE.get().use(level,keeper,InteractionHand.MAIN_HAND);
            h.assertTrue(dog.trail().order()==Trail.Order.RESIDENT,"The native call cooldown prevents repeated commands");
            for(int i=0;i<Trail.CALL_COOLDOWN;i++) keeper.getCooldowns().tick();
            keeper.setShiftKeyDown(false); keeper.setPos(dog.position().add(-20,0,0));
            ModItems.TRAIL_WHISTLE.get().use(level,keeper,InteractionHand.MAIN_HAND);
            h.assertTrue(dog.trail().order()==Trail.Order.RESIDENT,"An ordinary-eared partner cannot hear a twenty-block call");
            dog.setStats(Stats.of(0,10,0,20,0));
            for(int i=0;i<Trail.CALL_COOLDOWN;i++) keeper.getCooldowns().tick();
            ModItems.TRAIL_WHISTLE.get().use(level,keeper,InteractionHand.MAIN_HAND);
            h.assertTrue(dog.trail().order()==Trail.Order.FOLLOW,"Bred Senses extends a real heard call beyond sixteen blocks");
            var route=dog.getNavigation().createPath(keeper.blockPosition().offset(2,0,0),0);
            h.assertTrue(HomeRoutes.trailSafe(dog,route),"The loaded dry excursion path is safe independently of a Nest radius");
            var node=route.getNode(Math.max(1,route.getNodeCount()/2)).asBlockPos();
            level.setBlock(node.below(),Blocks.MAGMA_BLOCK.defaultBlockState(),3);
            h.assertTrue(!HomeRoutes.trailSafe(dog,route),"A new real damaging floor invalidates the cached trail route");
            level.setBlock(node.below(),Blocks.STONE.defaultBlockState(),3);
            int phase=Gene.LEGS.alleleNamed("phase").id(); dog.setGenome(dog.genome().with(Gene.LEGS,0,phase).with(Gene.LEGS,1,phase));
            dog.setOnGround(true); var origin=dog.position(); float health=dog.getHealth();
            var attacker=net.minecraft.world.entity.EntityTypes.PIG.create(level,EntitySpawnReason.COMMAND);
            attacker.setNoAi(true); attacker.setPos(origin.add(-1,0,0)); level.addFreshEntity(attacker);
            h.assertTrue(dog.hurtServer(level,level.damageSources().mobAttack(attacker),1)&&dog.getHealth()==health-1
                    &&dog.getX()>origin.x+3&&dog.homeNest().equals(nest)&&dog.blockPosition().distSqr(nest)>16*16
                    &&dog.phaseWait()>0,"A trained Phase partner retains its real defensive blink outside a preserved home, with the attack and recovery intact");
            attacker.discard();
            dog.setLeashedTo(keeper,true);
            h.assertTrue(!new TrailGoal(dog).canUse()&&dog.trail().order()==Trail.Order.FOLLOW,"Native leads take priority without discarding the keeper order");
            dog.dropLeash(); keeper.setHealth(0);
            for(int i=0;i<Trail.LOST_KEEPER_TICKS;i++) dog.trail().tick();
            h.assertTrue(dog.trail().order()==Trail.Order.RESIDENT&&dog.homeNest().equals(nest),"An unavailable keeper returns the companion to its retained resident routine");
            dog.discard();
        }).thenSucceed();
    }
}
