package com.bunnidogs.mod.entity;

import com.bunnidogs.mod.genetics.Gene;
import com.bunnidogs.mod.genetics.Genome;
import com.bunnidogs.mod.genetics.Stats;
import com.bunnidogs.mod.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForgeMod;

/** Native oxygen use, actual water movement, drowning/effects and saved external attribute composition. */
public final class CoastalTraitsGameTest {
    private CoastalTraitsGameTest() {}
    private static Genome island() {
        var genome=Genome.DEFAULT;
        for (var gene:new Gene[]{Gene.COAT,Gene.LEGS,Gene.DIET}) {
            String name=gene==Gene.COAT ? "seal" : gene==Gene.LEGS ? "paddle" : "shore_forager";
            int id=gene.alleleNamed(name).id(); genome=genome.with(gene,0,id).with(gene,1,id);
        }
        return genome;
    }
    private static final class AirProbe extends Bunnidog {
        AirProbe(Level level) { super(ModEntities.BUNNIDOG.get(),level); }
        int nativeAirStep(int air) { return super.decreaseAirSupply(air); }
    }
    private static AirProbe probe(GameTestHelper h,Genome genome,Vec3 at) {
        var dog=new AirProbe(h.getLevel()); dog.setGenome(genome); dog.setStats(Stats.ZERO); dog.setNoAi(true); dog.setNoGravity(true);
        dog.setAge(0); dog.setHealth(dog.getMaxHealth()); dog.setPos(at); h.getLevel().addFreshEntity(dog); return dog;
    }
    private static void loadPool(GameTestHelper h,BlockPos base) {
        for (int x=(base.getX()-2)>>4;x<=(base.getX()+10)>>4;x++)
            for (int z=(base.getZ()-2)>>4;z<=(base.getZ()+10)>>4;z++) h.getLevel().getChunk(x,z);
    }
    public static void run(GameTestHelper h) {
        var level=h.getLevel(); var base=h.absolutePos(new BlockPos(1152,30,10));
        for (int x=0;x<8;x++) for (int z=0;z<8;z++) {
            level.setBlock(base.offset(x,0,z),Blocks.STONE.defaultBlockState(),2);
            for (int y=1;y<6;y++) level.setBlock(base.offset(x,y,z),Blocks.WATER.defaultBlockState(),2);
        }
        var at=Vec3.atBottomCenterOf(base.offset(3,1,3)); var ordinary=probe(h,Genome.DEFAULT,at); var coastal=probe(h,island(),at);
        var hidden=probe(h,island().with(Gene.COAT,1,Gene.COAT.alleleNamed("spotted").id()).with(Gene.LEGS,1,Gene.LEGS.alleleNamed("springy").id()),at);
        coastal.getRandom().setSeed(4711); int spent=0;
        for (int i=0;i<64;i++) {
            h.assertTrue(ordinary.nativeAirStep(100)==99 && hidden.nativeAirStep(100)==99,"Hidden coastal copies cannot change native oxygen use");
            if (coastal.nativeAirStep(100)==99) spent++;
        }
        h.assertTrue(spent>10 && spent<54 && coastal.getAttributeValue(Attributes.OXYGEN_BONUS)==1,
                "The actual native oxygen method must conserve some air with the inherited bonus");
        {
            loadPool(h,base);
            ordinary.setPos(at); coastal.setPos(at); hidden.setPos(at);
            // These deliberately distant fixture chunks are outside GameTest's forced ticking area.
            ordinary.tick(); ordinary.tick(); coastal.tick(); coastal.tick(); hidden.tick(); hidden.tick();
            h.assertTrue(coastal.isUnderWater() && !coastal.canBreatheUnderwater(),"A seal-coated animal still needs air in real water: submerged="+coastal.isUnderWater()
                    +", inWater="+coastal.isInWater()+", breathes="+coastal.canBreatheUnderwater()+", eyes="
                    +level.getBlockState(BlockPos.containing(coastal.getX(),coastal.getEyeY(),coastal.getZ())));
            ordinary.setDeltaMovement(Vec3.ZERO); coastal.setDeltaMovement(Vec3.ZERO); hidden.setDeltaMovement(Vec3.ZERO);
            ordinary.travel(new Vec3(0,0,1)); coastal.travel(new Vec3(0,0,1)); hidden.travel(new Vec3(0,0,1));
            double normal=ordinary.position().subtract(at).horizontalDistance(), swim=coastal.position().subtract(at).horizontalDistance();
            h.assertTrue(normal>0 && Math.abs(swim/normal-2)<.001 && Math.abs(hidden.position().subtract(at).horizontalDistance()-normal)<.0001,
                    "Expressed paddle legs double real native water propulsion; a hidden copy does not");
            h.assertTrue(coastal.canEat(new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.KELP))
                    && coastal.canEat(new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.SEAGRASS))
                    && !ordinary.canEat(new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.KELP)),"The actual shore diet adds its plant foods without feeding ordinary animals");
            coastal.setAirSupply(-20); float health=coastal.getHealth();
            coastal.getFluidInteraction().update(coastal,type->false);
            net.neoforged.neoforge.common.CommonHooks.onLivingBreathe(coastal,level,1,4);
            h.assertTrue(coastal.getHealth()<health,"The actual native breathing/drowning hook still hurts after stored air runs out: air="+coastal.getAirSupply());
            coastal.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING,200)); coastal.setAirSupply(10); float protectedHealth=coastal.getHealth();
            net.neoforged.neoforge.common.CommonHooks.onLivingBreathe(coastal,level,1,4);
            h.assertTrue(coastal.getAirSupply()==10 && coastal.getHealth()==protectedHealth,"Native Water Breathing composes without refilling underwater air");
                    coastal.getAttribute(Attributes.OXYGEN_BONUS).setBaseValue(2); coastal.getAttribute(NeoForgeMod.SWIM_SPEED).setBaseValue(2);
                    coastal.setAirSupply(73);
                    var out=TagValueOutput.createWithContext(ProblemReporter.DISCARDING,level.registryAccess()); coastal.saveWithoutId(out);
                    var restored=ModEntities.BUNNIDOG.get().create(level,EntitySpawnReason.LOAD);
                    restored.load(TagValueInput.create(ProblemReporter.DISCARDING,level.registryAccess(),out.buildResult()));
                    h.assertTrue(restored.genome().equals(island()) && restored.getAirSupply()==73
                            && restored.getAttributeValue(Attributes.OXYGEN_BONUS)==3 && restored.getAttributeValue(NeoForgeMod.SWIM_SPEED)==3,
                            "Native saves conserve actual air and one genetic modifier beside external oxygen/swim bases");
                    restored.setGenome(Genome.DEFAULT);
                    h.assertTrue(restored.getAirSupply()==73 && restored.getAttributeValue(Attributes.OXYGEN_BONUS)==2
                            && restored.getAttributeValue(NeoForgeMod.SWIM_SPEED)==2,"Gene changes remove only inherited modifiers without refilling air");
                    restored.setBaby(true); restored.setGenome(island());
                    h.assertTrue(restored.getAttributeValue(Attributes.OXYGEN_BONUS)==3 && restored.isBaby(),"Coastal pups inherit oxygen without forced growth");
                    ordinary.discard(); coastal.discard(); hidden.discard(); h.succeed();
        }
    }
}
