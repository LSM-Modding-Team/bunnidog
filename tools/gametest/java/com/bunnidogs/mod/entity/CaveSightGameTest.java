package com.bunnidogs.mod.entity;

import com.bunnidogs.mod.genetics.CaveSight;
import com.bunnidogs.mod.genetics.Gene;
import com.bunnidogs.mod.genetics.Genome;
import com.bunnidogs.mod.genetics.Stats;
import com.bunnidogs.mod.registry.ModEntities;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;

/** Paid native Night Vision, callback refund/reentry, real darkness and conserved saves/creative stock. */
public final class CaveSightGameTest {
    private CaveSightGameTest() {}
    public static void run(GameTestHelper h) {
        var level=h.getLevel(); int floor=286;
        for (int x=0;x<=6;x++) for (int y=0;y<=5;y++) for (int z=0;z<=6;z++)
            h.setBlock(x,floor+y,z,x==0||x==6||y==0||y==5||z==0||z==6 ? Blocks.STONE : Blocks.AIR);
        var dog=h.spawnWithNoFreeWill(ModEntities.BUNNIDOG.get(),3,floor+1,3);
        int pale=Gene.COAT.alleleNamed("pale").id(); dog.setGenome(Genome.DEFAULT.with(Gene.COAT,0,pale).with(Gene.COAT,1,pale));
        dog.setStats(Stats.of(0,0,0,20,0)); dog.setAge(0); dog.setHealth(dog.getMaxHealth()); dog.setOnGround(true);
        var player=h.makeMockServerPlayerInLevel(); player.gameMode.changeGameModeForPlayer(GameType.SURVIVAL);
        player.setPos(Vec3.atBottomCenterOf(h.absolutePos(new BlockPos(3,floor+1,2)))); player.setShiftKeyDown(true);
        var berries=new ItemStack(Items.GLOW_BERRIES,3); berries.set(DataComponents.CUSTOM_NAME,Component.literal("Keeper's exact berries"));
        player.setItemInHand(InteractionHand.MAIN_HAND,berries);
        h.runAfterDelay(5,()->{
            h.assertTrue(dog.caveSightStatus(player)==CaveSight.Status.READY,"Actual dim-room guidance must become ready after native light updates");
            dog.setStats(Stats.of(0,0,0,19,0)); player.interactOn(dog,InteractionHand.MAIN_HAND,Vec3.ZERO);
            h.assertTrue(berries.getCount()==3&&!player.hasEffect(MobEffects.NIGHT_VISION),"Undeveloped sight never spends the offered fruit"); dog.setStats(Stats.of(0,0,0,20,0));
            player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION,2400,1)); player.interactOn(dog,InteractionHand.MAIN_HAND,Vec3.ZERO);
            h.assertTrue(berries.getCount()==3&&player.getEffect(MobEffects.NIGHT_VISION).getDuration()==2400
                    &&dog.caveSightWait()==0,"An existing native effect is not shortened or charged"); player.removeEffect(MobEffects.NIGHT_VISION);
            int[] callbacks={0};
            Consumer<MobEffectEvent.Applicable> deny=e->{
                if(e.getEntity()!=player||!e.getEffectInstance().is(MobEffects.NIGHT_VISION)) return;
                callbacks[0]++;
                h.assertTrue(player.getMainHandItem().getCount()==2&&dog.caveSightStatus(player)==CaveSight.Status.BUSY,
                        "The actual berry cost and reentry guard precede native effect callbacks");
                player.interactOn(dog,InteractionHand.MAIN_HAND,Vec3.ZERO);
                var remaining=player.getMainHandItem(); player.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(Items.APPLE));
                player.getInventory().setItem(9,remaining);
                e.setResult(MobEffectEvent.Applicable.Result.DO_NOT_APPLY);
            };
            NeoForge.EVENT_BUS.addListener(deny);
            try { player.interactOn(dog,InteractionHand.MAIN_HAND,Vec3.ZERO); } finally { NeoForge.EVENT_BUS.unregister(deny); }
            var returned=player.getInventory().getItem(9);
            h.assertTrue(callbacks[0]==1&&!player.hasEffect(MobEffects.NIGHT_VISION)&&dog.caveSightWait()==0
                    &&returned.is(Items.GLOW_BERRIES)&&returned.getCount()==3&&returned.getHoverName().getString().equals("Keeper's exact berries")
                    &&player.getMainHandItem().is(Items.APPLE),"Denied/reentered guidance refunds the exact berry beside a changed hand");
            player.getInventory().setItem(9,ItemStack.EMPTY); player.setItemInHand(InteractionHand.MAIN_HAND,returned);
            player.interactOn(dog,InteractionHand.MAIN_HAND,Vec3.ZERO);
            h.assertTrue(returned.getCount()==2&&player.hasEffect(MobEffects.NIGHT_VISION)
                    &&player.getEffect(MobEffects.NIGHT_VISION).getDuration()==CaveSight.DURATION_TICKS&&dog.caveSightWait()==CaveSight.RECOVERY_TICKS,
                    "A real keeper interaction spends one berry for native vision and a saved guide recovery");
            var advancement=level.getServer().getAdvancements().get(Identifier.fromNamespaceAndPath("bunnidogs","pale_guide"));
            h.assertTrue(advancement!=null&&player.getAdvancements().getOrStartProgress(advancement).isDone(),"The earned native guidance advancement records success");
            var out=TagValueOutput.createWithContext(ProblemReporter.DISCARDING,level.registryAccess()); dog.saveWithoutId(out); var saved=out.buildResult();
            dog.load(TagValueInput.create(ProblemReporter.DISCARDING,level.registryAccess(),saved)); player.removeEffect(MobEffects.NIGHT_VISION);
            player.interactOn(dog,InteractionHand.MAIN_HAND,Vec3.ZERO);
            h.assertTrue(dog.caveSightWait()==CaveSight.RECOVERY_TICKS&&returned.getCount()==2&&!player.hasEffect(MobEffects.NIGHT_VISION),
                    "Native reload retains recovery and cannot buy another effect early");
            saved.remove("CaveSightWait"); dog.load(TagValueInput.create(ProblemReporter.DISCARDING,level.registryAccess(),saved));
            player.gameMode.changeGameModeForPlayer(GameType.CREATIVE); player.interactOn(dog,InteractionHand.MAIN_HAND,Vec3.ZERO);
            h.assertTrue(returned.getCount()==2&&player.hasEffect(MobEffects.NIGHT_VISION)&&dog.caveSightWait()>0,
                    "Creative guidance retains its actual fruit while still recording source recovery");
            player.removeEffect(MobEffects.NIGHT_VISION); dog.load(TagValueInput.create(ProblemReporter.DISCARDING,level.registryAccess(),saved));
            h.setBlock(3,floor+2,4,Blocks.SEA_LANTERN);
            h.succeedWhen(()->{
                h.assertTrue(dog.caveSightStatus(player)==CaveSight.Status.BRIGHT,"A real nearby light prevents redundant cave guidance: "
                        +dog.caveSightStatus(player)+", light="+level.getMaxLocalRawBrightness(player.blockPosition()));
                player.interactOn(dog,InteractionHand.MAIN_HAND,Vec3.ZERO);
                h.assertTrue(!player.hasEffect(MobEffects.NIGHT_VISION)&&dog.caveSightWait()==0,"Bright refusals give no effect or recovery");
            });
        });
    }
}
