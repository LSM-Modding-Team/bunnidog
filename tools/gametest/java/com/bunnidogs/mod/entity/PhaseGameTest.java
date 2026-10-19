package com.bunnidogs.mod.entity;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import com.bunnidogs.mod.genetics.Gene;
import com.bunnidogs.mod.genetics.Genome;
import com.bunnidogs.mod.genetics.Phasing;
import com.bunnidogs.mod.genetics.Stats;
import com.bunnidogs.mod.registry.ModBlocks;
import com.bunnidogs.mod.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityTeleportEvent;

/** Real incoming attacks, native teleport protection and whole-animal recovery persistence. */
public final class PhaseGameTest {
    private PhaseGameTest() {}
    private static Genome phase() {
        int id = Gene.LEGS.alleleNamed("phase").id();
        return Genome.DEFAULT.with(Gene.LEGS,0,id).with(Gene.LEGS,1,id);
    }
    private static Bunnidog fresh(GameTestHelper h) {
        var dog = ModEntities.BUNNIDOG.get().create(h.getLevel(),EntitySpawnReason.COMMAND);
        dog.setNoAi(true); dog.setAge(0); dog.setGenome(phase()); dog.setStats(Stats.ZERO);
        dog.setHealth(dog.getMaxHealth()); dog.setPos(Vec3.atBottomCenterOf(h.absolutePos(new BlockPos(2,1,4)))); dog.setOnGround(true);
        return dog;
    }
    public static void run(GameTestHelper h) {
        var level = h.getLevel();
        for (int x = 0; x < 8; x++) for (int z = 0; z < 8; z++) {
            h.setBlock(x,0,z,Blocks.STONE);
            for (int y = 1; y < 6; y++) h.setBlock(x,y,z,Blocks.AIR);
        }
        var home = h.absolutePos(new BlockPos(1,1,1)); level.setBlock(home,ModBlocks.NEST.get().defaultBlockState(),3);
        var attacker = h.spawnWithNoFreeWill(EntityTypes.PIG,1.5F,1,4.5F);
        var dog = fresh(h); level.addFreshEntity(dog);
        h.runAfterDelay(2,() -> {
            dog.setOnGround(true);
            h.assertTrue(dog.moveHomeTo(level,home) == NestHome.MoveResult.MOVED,"The Phase fixture must claim a real Nest");
            var origin = dog.position(); float health = dog.getHealth();
            h.assertTrue(dog.hurtServer(level,level.damageSources().mobAttack(attacker),1)
                    && dog.getHealth() == health-1 && Phasing.inRange(dog.position().subtract(origin).horizontalDistanceSqr(),dog.getY()-origin.y)
                    && Phasing.farther(origin.distanceToSqr(attacker.position()),dog.distanceToSqr(attacker))
                    && dog.phaseWait() == Phasing.RECOVERY_TICKS && dog.homeNest().equals(home),
                    "A real attack must land, then blink farther away to dry ground without moving the Nest");
            var landed = dog.position(); dog.invulnerableTime = 0; dog.setOnGround(true);
            dog.hurtServer(level,level.damageSources().mobAttack(attacker),1);
            h.assertTrue(dog.position().equals(landed) && dog.getHealth() == health-2,"Recovery must prevent a second blink, without damage immunity");
            var out = TagValueOutput.createWithContext(ProblemReporter.DISCARDING,level.registryAccess()); dog.saveWithoutId(out);
            var saved = out.buildResult(); var restored = fresh(h);
            restored.load(TagValueInput.create(ProblemReporter.DISCARDING,level.registryAccess(),saved));
            h.assertTrue(restored.phaseWait() == Phasing.RECOVERY_TICKS && restored.genome().equals(phase()) && restored.getUUID().equals(dog.getUUID()),
                    "Native whole-animal saves must retain identity, the appended allele and paused recovery");
            saved.remove("PhaseWait"); restored.load(TagValueInput.create(ProblemReporter.DISCARDING,level.registryAccess(),saved));
            h.assertTrue(restored.phaseWait() == 0,"Old saves and reused entities default to ready");
            saved.putInt("PhaseWait",Integer.MAX_VALUE); restored.load(TagValueInput.create(ProblemReporter.DISCARDING,level.registryAccess(),saved));
            h.assertTrue(restored.phaseWait() == Phasing.RECOVERY_TICKS,"Malformed long recovery must remain bounded");

            var player = h.makeMockPlayer(GameType.SURVIVAL);
            for (int mode = 0; mode < 6; mode++) {
                var held = fresh(h); var before = held.position();
                switch (mode) {
                    case 0 -> held.setGenome(Genome.DEFAULT);
                    case 1 -> held.setGenome(phase().with(Gene.LEGS,1,Gene.LEGS.alleleNamed("springy").id()));
                    case 2 -> held.setAge(-1000);
                    case 3 -> held.setLeashedTo(player,false);
                    case 4 -> held.setOnGround(false);
                    case 5 -> held.setHealth(1);
                }
                held.hurtServer(level,level.damageSources().mobAttack(attacker),1);
                h.assertTrue(held.position().equals(before) && held.phaseWait() == 0,
                        "Ordinary/hidden genes, pups, leads, airborne and lethal hits must not blink: "+mode);
                if (held.getLeashData() != null) held.removeLeash();
            }
            var boat = h.spawn(ModEntities.GUAVA_BOAT.get(),3,1,1);
            var rider = fresh(h); h.assertTrue(rider.startRiding(boat),"The transport fixture must actually board");
            var ridingAt = rider.position(); rider.setOnGround(true); rider.hurtServer(level,level.damageSources().mobAttack(attacker),1);
            h.assertTrue(rider.position().equals(ridingAt) && rider.phaseWait() == 0 && rider.isPassenger(),"Native rides must retain control");
            rider.stopRiding(); boat.discard();
            var environmental = fresh(h); var dryAt = environmental.position();
            environmental.hurtServer(level,level.damageSources().fall(),1);
            h.assertTrue(environmental.position().equals(dryAt) && environmental.phaseWait() == 0,"Environmental damage must not become a fall escape");
            var apex = ModEntities.APEX.get().create(level,EntitySpawnReason.COMMAND); apex.setGenome(phase()); apex.setPos(dryAt); apex.setOnGround(true);
            apex.hurtServer(level,level.damageSources().mobAttack(attacker),1);
            h.assertTrue(apex.position().equals(dryAt) && apex.phaseWait() == 0,"The ritual boss retains its own encounter movement");

            var probe = fresh(h); var start = probe.position(); var remote = start.add(100000,0,100000);
            h.assertTrue(!level.hasChunkAt(BlockPos.containing(remote)),"The redirected terrain must begin unloaded");
            var mode = new AtomicInteger(); var events = new AtomicInteger();
            Consumer<EntityTeleportEvent.EnderEntity> protection = event -> {
                if (event.getEntity() != probe) return;
                events.incrementAndGet();
                switch (mode.get()) {
                    case 0 -> event.setCanceled(true);
                    case 1 -> { event.setTargetX(remote.x); event.setTargetZ(remote.z); }
                    case 2 -> { event.setTargetX(start.x+4); event.setTargetY(start.y-1); event.setTargetZ(start.z); }
                    case 3 -> probe.setGenome(Genome.DEFAULT);
                    case 4 -> probe.setPos(start.add(0,0,1));
                }
            };
            NeoForge.EVENT_BUS.addListener(protection);
            try {
                for (int i = 0; i < 5; i++) {
                    mode.set(i); probe.setGenome(phase()); probe.setPos(start); probe.setOnGround(true); probe.setHealth(probe.getMaxHealth()); probe.invulnerableTime = 0;
                    probe.hurtServer(level,level.damageSources().mobAttack(attacker),1);
                    h.assertTrue(probe.position().equals(i == 4 ? start.add(0,0,1) : start) && probe.phaseWait() == 0 && events.get() == i+1,
                            "Cancelled/unsafe redirects and changed genes/position must preserve callback state without recovery: "+i);
                }
            } finally { NeoForge.EVENT_BUS.unregister(protection); }
            h.assertTrue(!level.hasChunkAt(BlockPos.containing(remote)),"A rejected redirect must never load remote terrain");
            // No supported landing anywhere in range: the animal keeps the original attack and pays no recovery.
            for (int x = 0; x < 8; x++) for (int z = 0; z < 8; z++) h.setBlock(x,0,z,Blocks.MAGMA_BLOCK);
            var blocked = fresh(h); var blockedAt = blocked.position(); blocked.hurtServer(level,level.damageSources().mobAttack(attacker),1);
            h.assertTrue(blocked.position().equals(blockedAt) && blocked.phaseWait() == 0,"Native hazardous floors must refuse every candidate");
            h.succeed();
        });
    }
}
