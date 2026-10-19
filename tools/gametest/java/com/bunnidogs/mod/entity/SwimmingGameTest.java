package com.bunnidogs.mod.entity;

import com.bunnidogs.mod.genetics.Adaptations;
import com.bunnidogs.mod.genetics.Gene;
import com.bunnidogs.mod.genetics.Genome;
import com.bunnidogs.mod.genetics.Stats;
import com.bunnidogs.mod.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForgeMod;

/** Native water propulsion, drowning and named-modifier save/gene changes. */
public final class SwimmingGameTest {
    private SwimmingGameTest() {}
    private static Genome webbed() {
        int id = Gene.LEGS.alleleNamed("webbed").id();
        return Genome.DEFAULT.with(Gene.LEGS,0,id).with(Gene.LEGS,1,id);
    }
    private static Bunnidog dog(GameTestHelper h, Genome genome, Vec3 position) {
        var dog = ModEntities.BUNNIDOG.get().create(h.getLevel(),EntitySpawnReason.COMMAND);
        dog.setNoAi(true); dog.setAge(0); dog.setGenome(genome); dog.setStats(Stats.ZERO); dog.setHealth(dog.getMaxHealth()); dog.setPos(position);
        h.getLevel().addFreshEntity(dog); return dog;
    }
    public static void run(GameTestHelper h) {
        var level = h.getLevel();
        for (int x = 0; x < 8; x++) for (int z = 0; z < 8; z++) {
            h.setBlock(x,0,z,Blocks.STONE);
            for (int y = 1; y < 6; y++) h.setBlock(x,y,z,x >= 1 && x <= 6 && z >= 1 && z <= 6 && y <= 3 ? Blocks.WATER : Blocks.AIR);
        }
        var at = Vec3.atBottomCenterOf(h.absolutePos(new BlockPos(3,1,3)));
        var ordinary = dog(h,Genome.DEFAULT,at); var swimmer = dog(h,webbed(),at);
        var hidden = dog(h,webbed().with(Gene.LEGS,1,Gene.LEGS.alleleNamed("springy").id()),at);
        h.runAfterDelay(2,() -> {
            ordinary.setPos(at); swimmer.setPos(at); hidden.setPos(at);
            h.assertTrue(ordinary.isInWater() && swimmer.isInWater() && swimmer.isUnderWater(),"The fixture must be in actual native water");
            ordinary.setDeltaMovement(Vec3.ZERO); swimmer.setDeltaMovement(Vec3.ZERO); hidden.setDeltaMovement(Vec3.ZERO);
            ordinary.setOnGround(false); swimmer.setOnGround(false); hidden.setOnGround(false);
            ordinary.travel(new Vec3(0,0,1)); swimmer.travel(new Vec3(0,0,1)); hidden.travel(new Vec3(0,0,1));
            double plainMove = ordinary.position().subtract(at).horizontalDistance(), swimMove = swimmer.position().subtract(at).horizontalDistance();
            h.assertTrue(plainMove > 0 && Math.abs(swimMove/plainMove-1.5) < .001
                    && Math.abs(hidden.position().subtract(at).horizontalDistance()-plainMove) < .0001,
                    "Expressed webbed legs must increase actual native swim propulsion by fifty percent; hidden copies do not");
            h.assertTrue(!swimmer.canBreatheUnderwater() && !HomeRoutes.dryCell(swimmer,BlockPos.containing(at)),
                    "Swimmers must still need air and retain domestic dry-route hazards");
            swimmer.setAirSupply(-19); float health = swimmer.getHealth();
            h.runAfterDelay(1,() -> {
                h.assertTrue(swimmer.getHealth() < health && swimmer.getAirSupply() <= 0,"Native drowning must still hurt a webbed animal");
                swimmer.getAttribute(NeoForgeMod.SWIM_SPEED).setBaseValue(2);
                h.assertTrue(swimmer.getAttributeValue(NeoForgeMod.SWIM_SPEED) == 2.5,"External swim base values must combine with the named genetic bonus");
                var savedDog = swimmer;
                for (int i = 0; i < 2; i++) {
                    var out = TagValueOutput.createWithContext(ProblemReporter.DISCARDING,level.registryAccess()); savedDog.saveWithoutId(out);
                    var restored = ModEntities.BUNNIDOG.get().create(level,EntitySpawnReason.LOAD);
                    restored.load(TagValueInput.create(ProblemReporter.DISCARDING,level.registryAccess(),out.buildResult()));
                    h.assertTrue(restored.genome().equals(webbed()) && restored.getAttributeValue(NeoForgeMod.SWIM_SPEED) == 2.5,
                            "Native reload must restore one genetic swim bonus alongside the saved external base");
                    savedDog = restored;
                }
                savedDog.setGenome(Genome.DEFAULT);
                h.assertTrue(savedDog.getAttributeValue(NeoForgeMod.SWIM_SPEED) == 2 && !Adaptations.webbed(savedDog.genome()),
                        "Changing expression must remove only the genetic modifier");
                savedDog.setAge(-1000); savedDog.setGenome(webbed());
                h.assertTrue(savedDog.getAttributeValue(NeoForgeMod.SWIM_SPEED) == 2.5 && savedDog.isBaby(),"Pups inherit swimming feet without forced growth");
                h.succeed();
            });
        });
    }
}
