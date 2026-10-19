package com.bunnidogs.mod.entity;

import java.util.function.Consumer;
import com.bunnidogs.mod.block.BreedingPenBlock;
import com.bunnidogs.mod.block.BreedingPenBlockEntity;
import com.bunnidogs.mod.genetics.*;
import com.bunnidogs.mod.item.SeedItem;
import com.bunnidogs.mod.registry.ModBlocks;
import com.bunnidogs.mod.registry.ModEntities;
import com.bunnidogs.mod.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HopperBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityMobGriefingEvent;
import net.neoforged.neoforge.event.level.BlockEvent.NeighborNotifyEvent;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;

/** Elevated fixtures isolate the pen's twelve-block adult census from neighboring GameTest animals. */
public final class BreedingPenGameTest {
    private record Context(ServerLevel level, BlockPos pos, BreedingPenBlockEntity pen, Bunnidog a, Bunnidog b, int height) {}
    private static Context setup(GameTestHelper h, boolean natural, int height) {
        var level = h.getLevel();
        for (int x = 0; x < 8; x++) for (int z = 0; z < 8; z++) {
            h.setBlock(x,height-1,z,Blocks.STONE);
            for (int y = height; y < height+4; y++) h.setBlock(x,y,z,x == 0 || x == 7 || z == 0 || z == 7 ? Blocks.STONE : Blocks.AIR);
        }
        h.setBlock(3,height,2,ModBlocks.BREEDING_PEN.get()); h.setBlock(4,height,5,ModBlocks.NEST.get()); h.setBlock(3,height,5,ModBlocks.NEST.get());
        var a = natural ? h.spawn(ModEntities.BUNNIDOG.get(),4.5F,height,4.5F) : h.spawnWithNoFreeWill(ModEntities.BUNNIDOG.get(),4.5F,height,4.5F);
        var b = natural ? h.spawn(ModEntities.BUNNIDOG.get(),2.5F,height,4.5F) : h.spawnWithNoFreeWill(ModEntities.BUNNIDOG.get(),2.5F,height,4.5F);
        a.setGenome(Genome.DEFAULT); b.setGenome(Genome.DEFAULT.with(Gene.COAT,0,2).with(Gene.COAT,1,2));
        a.setAge(0); b.setAge(0); a.setStats(Stats.of(5,0,0,0,0)); b.setStats(Stats.of(0,5,0,0,0));
        a.setHealth(a.getMaxHealth()); b.setHealth(b.getMaxHealth()); a.setOnGround(true); b.setOnGround(true);
        var pos = h.absolutePos(new BlockPos(3,height,2));
        return new Context(level,pos,(BreedingPenBlockEntity)level.getBlockEntity(pos),a,b,height);
    }
    private static void claim(GameTestHelper h, Context c) {
        h.assertTrue(c.a.moveHomeTo(c.level,h.absolutePos(new BlockPos(4,c.height,5))) == NestHome.MoveResult.MOVED
                && c.b.moveHomeTo(c.level,h.absolutePos(new BlockPos(3,c.height,5))) == NestHome.MoveResult.MOVED,"The actual two parents claim their own reachable Nests");
        var check = PenBreeding.inspect(c.level,c.pos,Direction.NORTH);
        h.assertTrue(check.status() == Pen.Status.READY,"Expected an isolated housed pair, found "+check.status()+" with "+check.parents().size()+" adults");
    }
    private static void stock(GameTestHelper h, Context c, int count) {
        var player = h.makeMockServerPlayerInLevel(); player.gameMode.changeGameModeForPlayer(GameType.SURVIVAL);
        var a = new ItemStack(ModItems.GOLDEN_GUAVA.get(),count); a.set(DataComponents.CUSTOM_NAME,Component.literal("A's exact stock"));
        var b = new ItemStack(ModItems.DIAMOND_GUAVA.get(),count); b.set(DataComponents.CUSTOM_NAME,Component.literal("B's exact stock"));
        player.setItemInHand(InteractionHand.OFF_HAND,a); c.pen.insertFromHand(player,a,0);
        player.setItemInHand(InteractionHand.OFF_HAND,b); c.pen.insertFromHand(player,b,1);
    }
    public static void courtship(GameTestHelper h) {
        var c = setup(h,true,25);
        h.runAfterDelay(3,() -> {
            claim(h,c);
            c.level.setBlock(c.pos.above(),Blocks.HOPPER.defaultBlockState(),3);
            c.level.setBlock(c.pos.east(),Blocks.HOPPER.defaultBlockState().setValue(HopperBlock.FACING,Direction.WEST),3);
            var upper = (HopperBlockEntity)c.level.getBlockEntity(c.pos.above());
            var side = (HopperBlockEntity)c.level.getBlockEntity(c.pos.east());
            upper.setItem(0,new ItemStack(ModItems.GOLDEN_GUAVA.get())); side.setItem(0,new ItemStack(ModItems.DIAMOND_GUAVA.get()));
            HopperBlockEntity.pushItemsTick(c.level,c.pos.above(),upper.getBlockState(),upper);
            HopperBlockEntity.pushItemsTick(c.level,c.pos.east(),side.getBlockState(),side);
            h.assertTrue(c.pen.food(0).is(ModItems.GOLDEN_GUAVA.get()) && c.pen.food(1).is(ModItems.DIAMOND_GUAVA.get())
                    && upper.getItem(0).isEmpty() && side.getItem(0).isEmpty(),"Native top/side hoppers fill the two separate parent mangers");
            h.assertTrue(PenBreeding.inspect(c.level,c.pos,Direction.NORTH).parents().get(0) == c.a,"A is on the left when viewing the actual front");
            c.level.setBlock(c.pos.west(),Blocks.REDSTONE_BLOCK.defaultBlockState(),3);
            h.runAfterDelay(3,() -> {
                h.assertTrue(c.a.isInLove() && c.b.isInLove() && c.pen.food(0).isEmpty() && c.pen.food(1).isEmpty(),"A genuine redstone edge pays one guava each and starts native love");
                h.runAfterDelay(80,() -> {
                    var seeds = c.level.getEntitiesOfClass(ItemEntity.class,new AABB(c.pos).inflate(6),e -> SeedItem.read(e.getItem()).isPresent());
                    h.assertTrue(seeds.size() == 1,"The actual native BreedGoal produces exactly one seed after courtship");
                    var seed = SeedItem.read(seeds.getFirst().getItem()).orElseThrow();
                    h.assertTrue(seed.quality() == 1.5 && (seed.mother().equals(c.a.genome()) && seed.father().equals(c.b.genome())
                            || seed.mother().equals(c.b.genome()) && seed.father().equals(c.a.genome()))
                            && c.a.getAge() > 0 && c.b.getAge() > 0,"Mating preserves both real genomes, averaged paid tiers and ordinary parental rest");
                    c.a.setAge(0); c.b.setAge(0); stock(h,c,1);
                    h.runAfterDelay(3,() -> {
                        h.assertTrue(!c.a.isInLove() && !c.b.isInLove() && c.pen.food(0).getCount() == 1 && c.pen.food(1).getCount() == 1,
                                "Held power cannot repeatedly feed a newly ready pair");
                        h.succeed();
                    });
                });
            });
        });
    }
    public static void safeguards(GameTestHelper h) {
        var c = setup(h,false,55);
        h.runAfterDelay(3,() -> {
            claim(h,c); stock(h,c,16);
            var originalA = c.pen.food(0); var originalB = c.pen.food(1);
            var save = c.pen.saveWithFullMetadata(c.level.registryAccess());
            var restored = (BreedingPenBlockEntity)BlockEntity.loadStatic(c.pos,c.pen.getBlockState(),save,c.level.registryAccess());
            h.assertTrue(ItemStack.matches(restored.food(0),originalA) && ItemStack.matches(restored.food(1),originalB) && c.pen.stockSignal() == 15,"Native saves retain both whole named guava stacks and full stock signal");
            try (var tx = Transaction.openRoot()) { h.assertTrue(c.pen.automation(Direction.UP).insert(ItemResource.of(ModItems.GOLDEN_GUAVA.get()),1,tx) == 0
                    && c.pen.automation(Direction.DOWN).size() == 0,"Full input refuses further food and the bottom cannot drain guavas"); }
            c.a.setHealth(c.a.getMaxHealth()-1); h.assertTrue(c.pen.activate(false) == Pen.Status.UNAVAILABLE,"Injured adults refuse"); c.a.setHealth(c.a.getMaxHealth());
            c.a.setAge(100); h.assertTrue(c.pen.activate(false) == Pen.Status.UNAVAILABLE,"Breeding rest is respected"); c.a.setAge(0);
            var player = h.makeMockServerPlayerInLevel(); c.a.setLeashedTo(player,false);
            h.assertTrue(c.pen.activate(false) == Pen.Status.UNAVAILABLE,"Leads keep player control"); c.a.removeLeash();
            var third = h.spawnWithNoFreeWill(ModEntities.BUNNIDOG.get(),5.5F,c.height,3.5F); third.setAge(0);
            h.assertTrue(c.pen.activate(false) == Pen.Status.PAIR,"A third adult prevents accidental pairing"); third.setBaby(true);
            h.assertTrue(c.pen.status() == Pen.Status.READY,"Existing pups do not obstruct their two parents' feed"); third.discard();
            c.level.setBlock(c.a.blockPosition().below(),Blocks.MAGMA_BLOCK.defaultBlockState(),3);
            h.assertTrue(c.pen.activate(false) == Pen.Status.UNSAFE,"Native floor hazards refuse"); c.level.setBlock(c.a.blockPosition().below(),Blocks.STONE.defaultBlockState(),3);
            c.level.setBlock(h.absolutePos(new BlockPos(3,c.height,4)),Blocks.STONE.defaultBlockState(),3); c.level.setBlock(h.absolutePos(new BlockPos(3,c.height+1,4)),Blocks.STONE.defaultBlockState(),3);
            h.assertTrue(c.pen.activate(false) == Pen.Status.UNSAFE,"Walls block a supposedly ready pair"); c.level.setBlock(h.absolutePos(new BlockPos(3,c.height,4)),Blocks.AIR.defaultBlockState(),3); c.level.setBlock(h.absolutePos(new BlockPos(3,c.height+1,4)),Blocks.AIR.defaultBlockState(),3);
            var remote = c.pos.offset(4096,0,4096);
            h.assertTrue(!c.level.hasChunkAt(remote) && PenBreeding.inspect(c.level,remote,Direction.NORTH).status() == Pen.Status.UNLOADED
                    && !c.level.hasChunkAt(remote),"Inspection must not request remote terrain");
            h.assertTrue(ItemStack.matches(c.pen.food(0),originalA) && ItemStack.matches(c.pen.food(1),originalB),"All refused attempts retain the exact two full stocks");
            Consumer<EntityMobGriefingEvent> refuse = event -> { if (event.getEntity() == c.a || event.getEntity() == c.b) event.setCanGrief(false); };
            NeoForge.EVENT_BUS.addListener(refuse);
            try { h.assertTrue(c.pen.activate(true) == Pen.Status.PROTECTED,"Native protection refuses automatic feeding without spending stock"); }
            finally { NeoForge.EVENT_BUS.unregister(refuse); }
            boolean[] changed = {false};
            Consumer<EntityMobGriefingEvent> move = event -> { if (!changed[0] && event.getEntity() == c.a) { changed[0] = true; c.b.setPos(c.b.getX(),c.b.getY()+4,c.b.getZ()); } };
            NeoForge.EVENT_BUS.addListener(move);
            try { h.assertTrue(c.pen.activate(true) == Pen.Status.CHANGED,"Protection callbacks changing a parent cancel the old pair"); }
            finally { NeoForge.EVENT_BUS.unregister(move); }
            var back = h.absolutePos(new BlockPos(2,c.height,4)); c.b.setPos(back.getX()+.5,back.getY(),back.getZ()+.5);
            h.assertTrue(changed[0] && ItemStack.matches(c.pen.food(0),originalA) && !c.a.isInLove(),"Interrupted automation consumes no guava or love state");
            var handA = new BlockHitResult(Vec3.atCenterOf(c.pos).add(.3,0,0),Direction.NORTH,c.pos,false);
            player.gameMode.changeGameModeForPlayer(GameType.ADVENTURE); player.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY); player.setItemInHand(InteractionHand.OFF_HAND,ItemStack.EMPTY);
            c.pen.getBlockState().useWithoutItem(c.level,player,handA);
            h.assertTrue(!c.a.isInLove() && ItemStack.matches(c.pen.food(0),originalA),"Adventure reads cannot feed or consume");
            player.gameMode.changeGameModeForPlayer(GameType.SURVIVAL); player.setShiftKeyDown(true); c.pen.getBlockState().useWithoutItem(c.level,player,handA); player.setShiftKeyDown(false);
            h.assertTrue(c.pen.food(0).isEmpty() && c.pen.food(1).isEmpty(),"Deliberate empty-hand recovery withdraws both mangers");
            player.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);
            var left = new ItemStack(ModItems.GOLDEN_GUAVA.get(),3); player.setItemInHand(InteractionHand.OFF_HAND,left);
            player.gameMode.useItemOn(player,c.level,left,InteractionHand.OFF_HAND,handA);
            h.assertTrue(left.isEmpty() && c.pen.food(0).getCount() == 3 && c.pen.food(1).isEmpty(),"Actual off-hand front-left use fills A and pays survival stock");
            player.gameMode.changeGameModeForPlayer(GameType.CREATIVE);
            var right = new ItemStack(ModItems.DIAMOND_GUAVA.get(),2); player.setItemInHand(InteractionHand.OFF_HAND,right);
            var handB = new BlockHitResult(Vec3.atCenterOf(c.pos).add(-.3,0,0),Direction.NORTH,c.pos,false);
            player.gameMode.useItemOn(player,c.level,right,InteractionHand.OFF_HAND,handB);
            h.assertTrue(right.getCount() == 2 && c.pen.food(1).getCount() == 2 && c.pen.getBlockState().getValue(BreedingPenBlock.FILL) == 3,"Creative front-right use preserves the actual hand and separately fills B");
            var calipers = new ItemStack(ModItems.CALIPERS.get()); player.setItemInHand(InteractionHand.MAIN_HAND,calipers); player.setItemInHand(InteractionHand.OFF_HAND,ItemStack.EMPTY);
            player.setShiftKeyDown(true); player.gameMode.useItemOn(player,c.level,calipers,InteractionHand.MAIN_HAND,handA); player.setShiftKeyDown(false);
            h.assertTrue(!c.a.isInLove() && c.pen.food(0).getCount() == 3 && c.pen.food(1).getCount() == 2,"Sneak-Calipers inspection reads the pair without food or love mutations");
            player.gameMode.changeGameModeForPlayer(GameType.SURVIVAL); player.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);
            NeoForge.EVENT_BUS.addListener(refuse);
            try { c.pen.getBlockState().useWithoutItem(c.level,player,handA); }
            finally { NeoForge.EVENT_BUS.unregister(refuse); }
            h.assertTrue(c.a.isInLove() && c.b.isInLove() && c.pen.food(0).getCount() == 2 && c.pen.food(1).getCount() == 1,
                    "A deliberate player action pays from the pen and can breed when automatic mob-griefing permission refuses");
            h.succeed();
        });
    }
    public static void callbacks(GameTestHelper h) {
        var c = setup(h,false,85);
        h.runAfterDelay(3,() -> {
            claim(h,c); stock(h,c,1); boolean[] busy = {false}, removed = {false};
            Consumer<EntityMobGriefingEvent> reenter = event -> { if (event.getEntity() == c.a) busy[0] = c.pen.activate(true) == Pen.Status.BUSY; };
            Consumer<NeighborNotifyEvent> remove = event -> {
                if (!removed[0] && event.getLevel() == c.level && event.getPos().equals(c.pos) && c.a.isInLove() && c.b.isInLove()) {
                    removed[0] = true; c.level.removeBlock(c.pos,false);
                }
            };
            NeoForge.EVENT_BUS.addListener(reenter); NeoForge.EVENT_BUS.addListener(remove);
            try { h.assertTrue(c.pen.activate(true) == Pen.Status.FED,"Committed feeding survives source removal in stock notifications"); }
            finally { NeoForge.EVENT_BUS.unregister(reenter); NeoForge.EVENT_BUS.unregister(remove); }
            int food = c.level.getEntitiesOfClass(ItemEntity.class,new AABB(c.pos).inflate(2),e -> e.getItem().getItem() instanceof com.bunnidogs.mod.item.GuavaItem)
                    .stream().mapToInt(e -> e.getItem().getCount()).sum();
            h.assertTrue(busy[0] && removed[0] && food == 0 && c.pen.food(0).isEmpty() && c.pen.food(1).isEmpty()
                    && c.a.isInLove() && c.b.isInLove(),"Recursive feeding is refused; removing committed stock cannot duplicate paid food or either love state");
            h.succeed();
        });
    }
}
