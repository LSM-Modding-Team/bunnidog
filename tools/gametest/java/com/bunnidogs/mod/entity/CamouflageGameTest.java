package com.bunnidogs.mod.entity;

import com.bunnidogs.mod.genetics.Camouflage;
import com.bunnidogs.mod.genetics.Gene;
import com.bunnidogs.mod.genetics.Genome;
import com.bunnidogs.mod.genetics.Stats;
import com.bunnidogs.mod.item.CalipersItem;
import com.bunnidogs.mod.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SculkSensorBlock;
import net.minecraft.world.level.block.entity.SculkSensorBlockEntity;
import net.minecraft.world.level.gameevent.BlockPositionSource;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.GameEventListener;
import net.minecraft.world.level.gameevent.PositionSource;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.phys.Vec3;
import net.minecraft.server.level.ServerLevel;

/** Real native perception, support changes and sculk dispatch, rather than an event-only mock. */
public final class CamouflageGameTest {
    private CamouflageGameTest() {}
    private static Genome coat(String name) {
        int id = Gene.COAT.alleleNamed(name).id();
        return Genome.DEFAULT.with(Gene.COAT,0,id).with(Gene.COAT,1,id);
    }
    private static Bunnidog dog(GameTestHelper h, BlockPos pos, String coat) {
        var dog = h.spawnWithNoFreeWill(ModEntities.BUNNIDOG.get(),pos);
        dog.setGenome(coat(coat)); dog.setStats(Stats.ZERO); dog.setHealth(dog.getMaxHealth()); dog.setOnGround(true);
        return dog;
    }
    public static void rules(GameTestHelper h) {
        var level = h.getLevel();
        var dog = dog(h,new BlockPos(2,1,2),"pale");
        var support = dog.getOnPos();
        String[] coats = {"tan","cream","brown","spotted","snow","sand","moss","pale","slate","ember","void","glow"};
        var floors = new net.minecraft.world.level.block.Block[]{Blocks.SAND,Blocks.SAND,Blocks.DIRT,Blocks.DIRT,Blocks.SNOW_BLOCK,
                Blocks.SAND,Blocks.MOSS_BLOCK,Blocks.CALCITE,Blocks.DEEPSLATE,Blocks.NETHERRACK,Blocks.OBSIDIAN,Blocks.MOSS_BLOCK};
        for (int i = 0; i < coats.length; i++) {
            dog.setGenome(coat(coats[i])); level.setBlockAndUpdate(support,floors[i].defaultBlockState());
            h.assertTrue(dog.camouflageStatus() == (coats[i].equals("glow") ? Camouflage.Status.LUMINOUS : Camouflage.Status.BLENDED),
                    "Each expressed coat must use its actual shipped native ground tag: "+coats[i]);
        }
        dog.setGenome(coat("pale")); level.setBlockAndUpdate(support,Blocks.STONE.defaultBlockState());
        h.assertTrue(dog.camouflageStatus() == Camouflage.Status.BLENDED,"A pale coat blends on ordinary cave stone");
        dog.setBaby(true); h.assertTrue(dog.camouflageStatus() == Camouflage.Status.BLENDED,"Pups inherit the same expressed coat benefit"); dog.setAge(0);
        dog.setHealth(dog.getMaxHealth()-1); h.assertTrue(dog.camouflageStatus() == Camouflage.Status.DISTURBED,"Even slight injury breaks cover"); dog.setHealth(dog.getMaxHealth());
        dog.setOnGround(false); h.assertTrue(dog.camouflageStatus() == Camouflage.Status.DISTURBED,"Jumping/falling breaks cover"); dog.setOnGround(true);
        dog.setInLove(null); h.assertTrue(dog.camouflageStatus() == Camouflage.Status.DISTURBED,"Courtship remains exposed"); dog.resetLove();
        dog.addEffect(new MobEffectInstance(MobEffects.GLOWING,100)); h.assertTrue(dog.camouflageStatus() == Camouflage.Status.DISTURBED,"Tracking glow defeats cover"); dog.removeEffect(MobEffects.GLOWING);
        var player = h.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL); dog.setLeashedTo(player,false);
        h.assertTrue(dog.camouflageStatus() == Camouflage.Status.DISTURBED,"A lead breaks cover"); dog.removeLeash();
        dog.setTarget(player); h.assertTrue(dog.camouflageStatus() == Camouflage.Status.DISTURBED && dog.getTarget() == player,"Cover never clears an existing combat target"); dog.setTarget(null);
        int glow = Gene.COAT.alleleNamed("glow").id(), pale = Gene.COAT.alleleNamed("pale").id();
        dog.setGenome(Genome.DEFAULT.with(Gene.COAT,0,glow).with(Gene.COAT,1,pale));
        h.assertTrue(dog.camouflageStatus() == Camouflage.Status.BLENDED,"A hidden glow copy does not illuminate or block the expressed pale coat");
        level.setBlockAndUpdate(support,Blocks.SAND.defaultBlockState());
        h.assertTrue(dog.camouflageStatus() == Camouflage.Status.EXPOSED,"A changed floor immediately removes cover without stale cached visibility");
        level.setBlockAndUpdate(support,Blocks.STONE.defaultBlockState());
        var saved = TagValueOutput.createWithContext(ProblemReporter.DISCARDING,level.registryAccess()); dog.saveWithoutId(saved);
        var restored = ModEntities.BUNNIDOG.get().create(level,EntitySpawnReason.LOAD);
        restored.load(TagValueInput.create(ProblemReporter.DISCARDING,level.registryAccess(),saved.buildResult())); restored.setOnGround(true);
        h.assertTrue(restored.camouflageStatus() == Camouflage.Status.BLENDED && restored.genome().equals(dog.genome()),
                "Old/native animal saves derive cover directly without a timer or component migration");
        var before = TagValueOutput.createWithContext(ProblemReporter.DISCARDING,level.registryAccess()); dog.saveWithoutId(before);
        CalipersItem.showCare(player,dog);
        var after = TagValueOutput.createWithContext(ProblemReporter.DISCARDING,level.registryAccess()); dog.saveWithoutId(after);
        h.assertTrue(before.buildResult().equals(after.buildResult()),"Calipers cover inspection must remain read-only");
        var observer = h.spawnWithNoFreeWill(EntityTypes.ZOMBIE,2,1,12);
        var conditions = TargetingConditions.forNonCombat().range(16).ignoreLineOfSight();
        h.assertTrue(dog.getVisibilityPercent(observer) == .5 && !conditions.test(level,observer,dog),"Native target acquisition must halve the visual distance");
        level.setBlockAndUpdate(support,Blocks.SAND.defaultBlockState());
        h.assertTrue(conditions.test(level,observer,dog),"An exposed animal remains visible at the same distance");
        level.setBlockAndUpdate(support,Blocks.STONE.defaultBlockState()); observer.setPos(dog.getX(),dog.getY(),dog.getZ()+1.5);
        h.assertTrue(conditions.test(level,observer,dog),"Native close-range minimum still finds a blended animal");
        dog.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY,100));
        dog.setInvisible(true); // The potion refreshes this native flag on the next entity tick.
        h.assertTrue(dog.getVisibilityPercent(observer) > 0 && dog.getVisibilityPercent(observer) < .5,"Native potion visibility composes with cover without invulnerability");
        dog.removeEffect(MobEffects.INVISIBILITY);
        dog.setInvisible(false);
        dog.setHealth(dog.getMaxHealth());
        h.assertTrue(dog.hurtServer(level,level.damageSources().mobAttack(observer),1) && dog.getHealth() < dog.getMaxHealth()
                && dog.camouflageStatus() == Camouflage.Status.DISTURBED,"Native attacks still hurt and immediately break cover");
    }
    public static void vibrations(GameTestHelper h) {
        var level = h.getLevel();
        for (int x = 0; x < 12; x++) for (int z = 0; z < 8; z++) h.setBlock(new BlockPos(x,124,z),Blocks.DEEPSLATE);
        var sensorPos = h.absolutePos(new BlockPos(7,125,3));
        var dog = dog(h,new BlockPos(2,125,3),"slate");
        var listener = new Counter(sensorPos);
        var chunk = level.getChunkAt(sensorPos);
        chunk.getListenerRegistry(sensorPos.getY()>>4).register(listener);
        h.runAtTickTime(3,() -> {
            level.setBlockAndUpdate(sensorPos,Blocks.SCULK_SENSOR.defaultBlockState());
            step(dog);
        });
        h.runAtTickTime(12,() -> {
            var sensor = (SculkSensorBlockEntity)level.getBlockEntity(sensorPos);
            h.assertTrue(dog.camouflageStatus() == Camouflage.Status.BLENDED && listener.steps == 0
                    && sensor.getLastVibrationFrequency() == 0,"Real blended native walking must reach neither a sculk sensor nor ordinary game-event listeners");
            // A non-step vibration from the same animal still travels normally.
            level.gameEvent(GameEvent.EAT,dog.position(),GameEvent.Context.of(dog));
            h.assertTrue(listener.other == 1,"Blending must not mute eating or other native events");
        });
        h.runAtTickTime(22,() -> {
            h.assertTrue(((SculkSensorBlockEntity)level.getBlockEntity(sensorPos)).getLastVibrationFrequency() == 8,
                    "A real sculk sensor still receives a blended animal's eating vibration");
            // Replace the sensor to clear the ordinary native active/cooldown period.
            level.setBlockAndUpdate(sensorPos,Blocks.AIR.defaultBlockState());
            level.setBlockAndUpdate(sensorPos,Blocks.SCULK_SENSOR.defaultBlockState());
            dog.setGenome(coat("sand")); step(dog);
            h.assertTrue(listener.steps > 0,"Mismatched native walking must resume STEP dispatch");
        });
        h.runAtTickTime(34,() -> {
            h.assertTrue(((SculkSensorBlockEntity)level.getBlockEntity(sensorPos)).getLastVibrationFrequency() == 1,
                    "A real sculk sensor hears exposed walking again");
            chunk.getListenerRegistry(sensorPos.getY()>>4).unregister(listener); h.succeed();
        });
    }
    private static void step(Bunnidog dog) {
        for (int i = 0; i < 6; i++) dog.move(MoverType.SELF,new Vec3(.3,-.01,0));
        dog.setOnGround(true);
    }
    private static final class Counter implements GameEventListener {
        private final PositionSource position;
        int steps, other;
        Counter(BlockPos pos) { this.position = new BlockPositionSource(pos); }
        public PositionSource getListenerSource() { return this.position; }
        public int getListenerRadius() { return 8; }
        public boolean handleGameEvent(ServerLevel level, Holder<GameEvent> event, GameEvent.Context context, Vec3 position) {
            if (context.sourceEntity() instanceof Bunnidog) { if (event.equals(GameEvent.STEP)) this.steps++; else this.other++; }
            return true;
        }
    }
}
