package com.bunnidogs.mod.entity;

import com.bunnidogs.mod.BunnidogsMod;
import com.bunnidogs.mod.genetics.Gene;
import com.bunnidogs.mod.genetics.Genome;
import com.bunnidogs.mod.registry.ModBlocks;
import com.bunnidogs.mod.registry.ModEntities;
import com.bunnidogs.mod.registry.ModItems;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.*;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SignBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.SignText;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.RegisterEvent;

/** Runs only from the development source set; neither tests nor fixture enter the mod JAR. */
@EventBusSubscriber(modid = BunnidogsMod.MODID)
public final class GuavaGameTest extends GameTestInstance {
    private static final Identifier TYPE = id("guava_integration");
    public static final MapCodec<GuavaGameTest> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            Codec.STRING.fieldOf("scenario").forGetter(t -> t.scenario),
            TestData.CODEC.forGetter(GuavaGameTest::info)).apply(i, GuavaGameTest::new));
    private final String scenario;

    private GuavaGameTest(String scenario, TestData<Holder<TestEnvironmentDefinition<?>>> data) {
        super(data);
        this.scenario = scenario;
    }
    private static Identifier id(String path) { return Identifier.fromNamespaceAndPath(BunnidogsMod.MODID, path); }
    @SubscribeEvent
    public static void registerCodec(RegisterEvent event) {
        event.register(Registries.TEST_INSTANCE_TYPE, helper -> helper.register(TYPE, CODEC));
    }
    @SubscribeEvent
    public static void registerTests(RegisterGameTestsEvent event) {
        var environment = event.registerEnvironment(id("guava_integration"), new TestEnvironmentDefinition.AllOf());
        for (String name : new String[]{"boats", "signs", "transport", "rehoming", "wayfinding", "sampling", "adaptations", "fleece", "splicing"}) {
            event.registerTest(id(name), new GuavaGameTest(name, new TestData<>(environment, id("empty"), 100, 0, true)));
        }
    }
    @Override public MapCodec<GuavaGameTest> codec() { return CODEC; }
    @Override protected MutableComponent typeDescription() { return Component.literal(this.scenario); }
    @Override public void run(GameTestHelper h) {
        switch (this.scenario) {
            case "boats" -> boats(h);
            case "signs" -> signs(h);
            case "wayfinding" -> com.bunnidogs.mod.worldgen.WayfindingGameTest.run(h);
            case "sampling" -> SamplingGameTest.run(h);
            case "adaptations" -> AdaptationsGameTest.run(h);
            case "fleece" -> { FleeceGameTest.run(h); return; }
            case "splicing" -> SplicingGameTest.run(h);
            case "transport" -> transport(h);
            case "rehoming" -> { rehoming(h); return; }
            default -> h.fail("Unknown integration scenario: " + this.scenario);
        }
        h.succeed();
    }
    private static void boats(GameTestHelper h) {
        var level = h.getLevel();
        var boat = ModEntities.GUAVA_BOAT.get().create(level, EntitySpawnReason.COMMAND);
        var chest = ModEntities.GUAVA_CHEST_BOAT.get().create(level, EntitySpawnReason.COMMAND);
        h.assertTrue(boat != null && chest != null, "Vehicle factories must create both guava boat types");
        var boatPos = h.absolutePos(new BlockPos(1, 2, 1));
        var chestPos = h.absolutePos(new BlockPos(4, 2, 4));
        boat.setInitialPos(boatPos.getX() + .5, boatPos.getY(), boatPos.getZ() + .5);
        chest.setInitialPos(chestPos.getX() + .5, chestPos.getY(), chestPos.getZ() + .5);
        level.addFreshEntity(boat);
        level.addFreshEntity(chest);
        h.assertTrue(boat.getPickResult().is(ModItems.GUAVA_BOAT.get()), "Normal boat pick/drop identity");
        h.assertTrue(chest.getPickResult().is(ModItems.GUAVA_CHEST_BOAT.get()), "Chest boat pick/drop identity");
        var dog1 = h.spawnWithNoFreeWill(ModEntities.BUNNIDOG.get(), 1, 2, 1);
        var dog2 = h.spawnWithNoFreeWill(ModEntities.BUNNIDOG.get(), 1, 2, 2);
        var dog3 = h.spawnWithNoFreeWill(ModEntities.BUNNIDOG.get(), 2, 2, 1);
        var dog4 = h.spawnWithNoFreeWill(ModEntities.BUNNIDOG.get(), 2, 2, 2);
        h.assertTrue(dog1.startRiding(boat) && dog2.startRiding(boat) && !dog3.startRiding(boat), "Normal boat must have exactly two seats");
        dog1.stopRiding(); dog2.stopRiding();
        // Fresh passengers avoid vanilla's cooldown after dismounting the other boat.
        h.assertTrue(dog3.startRiding(chest) && !dog4.startRiding(chest), "Chest boat must have exactly one seat");
        dog3.stopRiding();
        chest.setCustomName(Component.literal("Guava expedition"));
        chest.setItem(0, new ItemStack(ModItems.GUAVA.get(), 3));
        var sample = new ItemStack(ModItems.SEED.get());
        sample.set(ModItems.SEED_DATA.get(), Genome.DEFAULT + "|" + Genome.DEFAULT + "|1.5");
        chest.setItem(26, sample.copy());
        var reporter = new ProblemReporter.Collector();
        var output = TagValueOutput.createWithContext(reporter, level.registryAccess());
        chest.saveWithoutId(output);
        var restored = ModEntities.GUAVA_CHEST_BOAT.get().create(level, EntitySpawnReason.LOAD);
        restored.load(TagValueInput.create(reporter, level.registryAccess(), output.buildResult()));
        h.assertTrue(reporter.isEmpty(), "Boat save/load must not lose codec data");
        h.assertTrue(restored.getContainerSize() == 27 && restored.getItem(0).getCount() == 3
                && ItemStack.isSameItemSameComponents(restored.getItem(26), sample), "All 27 chest slots and seed components must survive load");
        h.assertTrue(restored.getCustomName() != null && restored.getCustomName().getString().equals("Guava expedition"), "Boat name must survive load");
        chest.destroy(level, level.damageSources().generic());
        var drops = level.getEntitiesOfClass(ItemEntity.class, new AABB(chest.blockPosition()).inflate(3));
        h.assertTrue(drops.stream().filter(e -> e.getItem().is(ModItems.GUAVA_CHEST_BOAT.get())).mapToInt(e -> e.getItem().getCount()).sum() == 1,
                "Destroying a chest boat must drop exactly its own boat item");
        h.assertTrue(drops.stream().filter(e -> e.getItem().is(ModItems.GUAVA.get())).mapToInt(e -> e.getItem().getCount()).sum() == 3,
                "Chest contents must drop exactly once");
        h.assertTrue(drops.stream().anyMatch(e -> ItemStack.isSameItemSameComponents(e.getItem(), sample)), "Dropped seeds must preserve their genetic data");
    }
    private static void signs(GameTestHelper h) {
        var blocks = new SignBlock[]{ModBlocks.GUAVA_SIGN.get(), ModBlocks.GUAVA_WALL_SIGN.get(),
                ModBlocks.GUAVA_HANGING_SIGN.get(), ModBlocks.GUAVA_WALL_HANGING_SIGN.get()};
        for (int i = 0; i < blocks.length; i++) {
            var pos = new BlockPos(i + 1, 2, 2);
            h.setBlock(pos.below(), Blocks.STONE);
            h.setBlock(pos.above(), Blocks.STONE);
            h.setBlock(pos.south(), Blocks.STONE);
            h.setBlock(pos, blocks[i]);
            var sign = h.getBlockEntity(pos, SignBlockEntity.class);
            var state = h.getBlockState(pos);
            h.assertTrue(sign.getType().isValid(state), "Native block-entity type must accept every guava sign form");
            h.assertTrue(blocks[i].getTicker(h.getLevel(), state, sign.getType()) != null, "Native sign editing-lock ticker must exist");
            sign.setText(new SignText().setMessage(0, Component.literal("Nursery")).setColor(DyeColor.GREEN).setHasGlowingText(true), true);
            sign.setText(new SignText().setMessage(0, Component.literal("Pantry")).setColor(DyeColor.RED), false);
            sign.setWaxed(true);
            var loaded = BlockEntity.loadStatic(h.absolutePos(pos), state, sign.saveWithFullMetadata(h.getLevel().registryAccess()), h.getLevel().registryAccess());
            h.assertTrue(loaded instanceof SignBlockEntity, "Sign must load through the registered native type");
            var copy = (SignBlockEntity) loaded;
            h.assertTrue(copy.isWaxed() && copy.getFrontText().hasGlowingText() && copy.getFrontText().getColor() == DyeColor.GREEN
                    && copy.getBackText().getColor() == DyeColor.RED, "Wax, glow and both colors must survive save/load");
            h.assertTrue(copy.getFrontText().getMessage(0, false).getString().equals("Nursery")
                    && copy.getBackText().getMessage(0, false).getString().equals("Pantry"), "Both text faces must survive save/load");
            h.assertTrue(copy.getMaxTextLineWidth() == (i < 2 ? 90 : 60), "Native text width must match sign geometry");
        }
    }
    private static void transport(GameTestHelper h) {
        var clock = h.getLevel().dimensionType().defaultClock().orElseThrow();
        long previous = h.getLevel().clockManager().getTotalTicks(clock);
        h.getLevel().clockManager().setTotalTicks(clock, 1000);
        try { transportAtDay(h); }
        finally { h.getLevel().clockManager().setTotalTicks(clock, previous); }
    }
    private static void transportAtDay(GameTestHelper h) {
        var level = h.getLevel();
        var dog = h.spawnWithNoFreeWill(ModEntities.BUNNIDOG.get(), 3, 2, 3);
        dog.setAge(0);
        dog.setGenome(Genome.DEFAULT.with(Gene.DIET, 0, 1).with(Gene.DIET, 1, 1));
        dog.setHealth(dog.getMaxHealth() - 2);
        var home = h.absolutePos(new BlockPos(1, 1, 1));
        h.setBlock(1, 1, 1, ModBlocks.NEST.get());
        var output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, level.registryAccess());
        dog.saveWithoutId(output);
        var saved = output.buildResult();
        saved.putInt("NestX", home.getX()); saved.putInt("NestY", home.getY()); saved.putInt("NestZ", home.getZ());
        dog.load(TagValueInput.create(ProblemReporter.DISCARDING, level.registryAccess(), saved));
        h.assertTrue(dog.canForage(), "An injured grazer at its home must be eligible before transport: baby=" + dog.isBaby()
                + ", health=" + dog.getHealth() + "/" + dog.getMaxHealth() + ", night=" + dog.homeNight()
                + ", transport=" + dog.beingTransported() + ", home=" + dog.homeNest());
        var holder = h.makeMockServerPlayerInLevel();
        dog.setLeashedTo(holder, false);
        h.assertTrue(dog.beingTransported() && !dog.canForage(), "A lead must suspend food navigation");
        var clock = level.dimensionType().defaultClock().orElseThrow();
        level.clockManager().setTotalTicks(clock, 13000);
        h.assertTrue(dog.homeNight() && !new NightHomeGoal(dog).canUse() && !new NightHomeGoal(dog).canContinueToUse(), "A lead must suspend home goals at night");
        var before = dog.position();
        dog.setPos(home.getX() + 20, home.getY() + 1, home.getZ());
        h.assertTrue(!new GoHomeGoal(dog).canUse(), "A lead must prevent returning from outside the home radius");
        dog.setPos(before);
        var leashOutput = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, level.registryAccess());
        dog.saveWithoutId(leashOutput);
        var restored = ModEntities.BUNNIDOG.get().create(level, EntitySpawnReason.LOAD);
        restored.load(TagValueInput.create(ProblemReporter.DISCARDING, level.registryAccess(), leashOutput.buildResult()));
        h.assertTrue(restored.beingTransported() && home.equals(restored.homeNest()), "Pending leash restoration must retain and suspend its saved Nest");
        dog.removeLeash();
        h.assertTrue(new NightHomeGoal(dog).canUse(), "At night, unleashing must resume the home goal");
        level.clockManager().setTotalTicks(clock, 1000);
        h.assertTrue(!dog.beingTransported() && home.equals(dog.homeNest()) && dog.canForage(), "Unleashing must restore normal behavior without forgetting home");
        var boat = ModEntities.GUAVA_BOAT.get().create(level, EntitySpawnReason.COMMAND);
        boat.setInitialPos(dog.getX(), dog.getY(), dog.getZ()); level.addFreshEntity(boat);
        h.assertTrue(dog.startRiding(boat), "Bunnidog must fit a guava boat");
        h.assertTrue(dog.beingTransported() && !dog.canForage() && !new NightHomeGoal(dog).canUse(), "Riding must suspend voluntary home/food navigation");
        dog.stopRiding();
        h.assertTrue(!dog.beingTransported() && home.equals(dog.homeNest()), "Disembarking must retain the Nest");
        dog.setPos(home.getX() + 20, home.getY() + 1, home.getZ());
        h.assertTrue(new GoHomeGoal(dog).canUse(), "After transport, a dog outside its home radius must resume returning");
    }
    private static void rehoming(GameTestHelper h) {
        for (int x = 0; x < 8; x++) for (int z = 0; z < 8; z++) h.setBlock(x, 0, z, Blocks.STONE);
        var oldPos = new BlockPos(1, 0, 1);
        var newPos = new BlockPos(5, 0, 5);
        var blockedPos = new BlockPos(1, 0, 6);
        h.setBlock(oldPos, ModBlocks.NEST.get());
        h.setBlock(newPos, ModBlocks.NEST.get());
        h.setBlock(blockedPos, ModBlocks.NEST.get());
        for (int dx = -1; dx <= 1; dx++) for (int dz = -1; dz <= 1; dz++) {
            h.setBlock(blockedPos.offset(dx, 1, dz), Blocks.STONE);
            h.setBlock(blockedPos.offset(dx, 2, dz), Blocks.STONE);
        }
        var dog = h.spawnWithNoFreeWill(ModEntities.BUNNIDOG.get(), 2, 1, 2);
        var other = h.spawnWithNoFreeWill(ModEntities.BUNNIDOG.get(), 4, 1, 4);
        var pup = h.spawnWithNoFreeWill(ModEntities.BUNNIDOG.get(), 5, 1, 4);
        pup.setAge(-24000);
        var player = h.makeMockServerPlayerInLevel();
        var playerPos = h.absolutePos(new BlockPos(4, 1, 3));
        player.setPos(playerPos.getX() + .5, playerPos.getY(), playerPos.getZ() + .5);
        dog.setLeashedTo(player, false);
        other.setLeashedTo(player, false);
        var level = h.getLevel();
        var oldHome = h.absolutePos(oldPos);
        var newHome = h.absolutePos(newPos);
        // Block-to-POI updates can be queued; test after the server has applied them.
        h.runAfterDelay(2, () -> {
            h.assertTrue(dog.moveHomeTo(level, oldHome) == NestHome.MoveResult.MOVED, "First home must acquire a free POI ticket");
            h.assertTrue(dog.moveHomeTo(level, oldHome) == NestHome.MoveResult.ALREADY_HOME, "Same-home requests must not consume another ticket");
            h.assertTrue(other.moveHomeTo(level, oldHome) == NestHome.MoveResult.OCCUPIED, "A resident must not be displaced");
            h.assertTrue(dog.moveHomeTo(level, h.absolutePos(blockedPos)) == NestHome.MoveResult.UNREACHABLE
                    && oldHome.equals(dog.homeNest()), "A failed route must retain the old home");
            h.assertTrue(pup.moveHomeTo(level, newHome) == NestHome.MoveResult.INVALID, "Pups must not claim Nests");
            player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, new ItemStack(net.minecraft.world.item.Items.LEAD));
            h.useBlock(newPos, player);
            h.assertTrue(newHome.equals(other.homeNest()) && oldHome.equals(dog.homeNest()), "One Nest click must rehome only the nearest led adult");
            h.assertTrue(dog.moveHomeTo(level, newHome) == NestHome.MoveResult.OCCUPIED && oldHome.equals(dog.homeNest()), "Occupied destination must keep the source claim");
            other.removeLeash();
            // Empty-hand interaction also works after the last carried lead was attached.
            player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, ItemStack.EMPTY);
            h.useBlock(newPos, player);
            h.assertTrue(oldHome.equals(dog.homeNest()), "An occupied Nest must still refuse an empty-hand move");
            h.assertTrue(other.moveHomeTo(level, h.absolutePos(new BlockPos(7, 0, 7))) == NestHome.MoveResult.INVALID, "Ordinary blocks cannot become a home");
            h.setBlock(6, 0, 2, ModBlocks.NEST.get());
            h.runAfterDelay(2, () -> {
                var thirdHome = h.absolutePos(new BlockPos(6, 0, 2));
                h.useBlock(new BlockPos(6, 0, 2), player);
                h.assertTrue(thirdHome.equals(dog.homeNest()) && dog.isLeashed(), "Empty-hand move must retain the lead and set the new Nest");
                h.assertTrue(other.moveHomeTo(level, oldHome) == NestHome.MoveResult.MOVED, "Successful relocation must free the old Nest ticket");
                h.succeed();
            });
        });
    }
}
