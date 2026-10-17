package com.bunnidogs.mod.entity;

import java.util.Arrays;
import com.bunnidogs.mod.genetics.Gene;
import com.bunnidogs.mod.genetics.Genome;
import com.bunnidogs.mod.genetics.Stats;
import com.bunnidogs.mod.item.TravelBasketItem;
import com.bunnidogs.mod.registry.ModBlocks;
import com.bunnidogs.mod.registry.ModEntities;
import com.bunnidogs.mod.registry.ModItems;
import com.bunnidogs.mod.registry.ModPoi;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.BlockHitResult;

public final class BasketGameTest {
    private BasketGameTest() {}
    private static int[] waits(Bunnidog dog) {
        return new int[]{dog.samplingWait(),dog.groomingWait(),dog.fleeceWait(),dog.splicingWait(),dog.forageWait(),dog.gardeningWait()};
    }
    private static void release(Player player, BlockPos floor) {
        player.getMainHandItem().useOn(new UseOnContext(player,InteractionHand.MAIN_HAND,
                new BlockHitResult(Vec3.atCenterOf(floor),Direction.UP,floor,false)));
    }
    public static void run(GameTestHelper h) {
        var level = h.getLevel();
        for (int x = 0; x < 8; x++) for (int z = 0; z < 8; z++) {
            h.setBlock(x,0,z,Blocks.STONE);
            for (int y = 1; y <= 8; y++) h.setBlock(x,y,z,Blocks.AIR);
        }
        h.setBlock(1,1,1,ModBlocks.NEST.get());
        var dog = h.spawnWithNoFreeWill(ModEntities.BUNNIDOG.get(),3.5F,1,3.5F);
        int woolly = Gene.BUILD.alleleNamed("woolly").id(), glow = Gene.COAT.alleleNamed("glow").id();
        dog.setGenome(Genome.DEFAULT.with(Gene.BUILD,0,woolly).with(Gene.BUILD,1,woolly).with(Gene.COAT,0,glow).with(Gene.COAT,1,glow));
        dog.setStats(Stats.of(7,8,9,10,20)); dog.setAge(1234); dog.setHealth(dog.getMaxHealth()-1);
        dog.setParents(Genome.DEFAULT,Genome.DEFAULT);
        dog.setCustomName(Component.literal("Juniper")); dog.applyEarTag();
        var player = h.makeMockServerPlayerInLevel();
        GameType.CREATIVE.updatePlayerAbilities(player.getAbilities());
        var basket = new ItemStack(ModItems.TRAVEL_BASKET.get());
        basket.set(DataComponents.CUSTOM_NAME,Component.literal("Juniper carrier"));
        player.setItemInHand(InteractionHand.MAIN_HAND,basket);
        h.runAfterDelay(2,() -> {
            var home = h.absolutePos(new BlockPos(1,1,1));
            h.assertTrue(dog.moveHomeTo(level,home) == NestHome.MoveResult.MOVED,"A basket test needs a real occupied Nest");
            dog.setLeashedTo(player,false);
            player.interactOn(dog,InteractionHand.MAIN_HAND,Vec3.ZERO);
            h.assertTrue(!TravelBasketItem.occupied(basket) && !dog.isRemoved(),"A led animal must remain in the world with the basket empty");
            dog.removeLeash();
            var apex = ModEntities.APEX.get().create(level,EntitySpawnReason.COMMAND);
            player.interactOn(apex,InteractionHand.MAIN_HAND,Vec3.ZERO);
            h.assertTrue(!TravelBasketItem.occupied(basket) && !apex.isRemoved(),"The Apex must never enter a basket");
            dog.beginSamplingRecovery(); dog.beginGroomingRecovery(); dog.beginSplicingRecovery();
            dog.ateForage(0); dog.finishedGardening();
            dog.onSheared(player,new ItemStack(Items.SHEARS),level,dog.blockPosition());
            int[] recovery = waits(dog);
            var identity = dog.getUUID(); var genome = dog.genome(); var stats = dog.stats();
            int age = dog.getAge(); float health = dog.getHealth();
            player.interactOn(dog,InteractionHand.MAIN_HAND,Vec3.ZERO);
            h.assertTrue(dog.isRemoved() && TravelBasketItem.occupied(player.getMainHandItem()) && basket.getCount() == 1
                    && basket.getHoverName().getString().equals("Juniper carrier"),"Native creative capture must persist on the actual named held basket and remove one animal");
            h.assertTrue(level.getPoiManager().getCountInRange(holder -> holder.is(ModPoi.NEST.getKey()),home,0,PoiManager.Occupancy.HAS_SPACE) == 1,
                    "Packing must immediately return the exact old Nest ticket");
            // A fresh mob cannot pathfind until native physics has put it on the floor.
            var neighbour = h.spawnWithNoFreeWill(ModEntities.BUNNIDOG.get(),3.5F,1,3.5F);
            neighbour.setAge(0); neighbour.setGenome(Genome.DEFAULT);
            player.interactOn(neighbour,InteractionHand.MAIN_HAND,Vec3.ZERO);
            h.assertTrue(!neighbour.isRemoved(),"An occupied basket must refuse a second animal");
            var itemOutput = TagValueOutput.createWithContext(ProblemReporter.DISCARDING,level.registryAccess());
            itemOutput.store("Basket",ItemStack.CODEC,basket);
            var savedBasket = TagValueInput.create(ProblemReporter.DISCARDING,level.registryAccess(),itemOutput.buildResult())
                    .read("Basket",ItemStack.CODEC).orElseThrow();
            player.setItemInHand(InteractionHand.MAIN_HAND,savedBasket);
            var sheet = TravelBasketItem.readLine(savedBasket).orElseThrow();
            h.assertTrue(sheet.genome().equals(genome) && sheet.stats().equals(stats),"A natively saved basket must retain the exact line read by Calipers");
            var entry = TravelBasketItem.readEntry(savedBasket).orElseThrow();
            h.assertTrue(entry.id().orElseThrow().equals(identity) && entry.mother().orElseThrow().equals(Genome.DEFAULT)
                    && entry.tag().isPresent() && !entry.pup(),"Carried Calipers must preserve identity, ancestry and age class on the ordinary animal sheet");
            var snapshot = savedBasket.get(DataComponents.BUCKET_ENTITY_DATA);
            var duplicate = savedBasket.copy();
            var landing = h.absolutePos(new BlockPos(5,1,5));
            level.setBlock(landing,Blocks.STONE.defaultBlockState(),3);
            release(player,landing.below());
            h.assertTrue(snapshot.equals(savedBasket.get(DataComponents.BUCKET_ENTITY_DATA)),"Blocked placement must preserve the exact full basket");
            level.setBlock(landing,Blocks.AIR.defaultBlockState(),3);
            h.runAfterDelay(3,() -> {
                h.assertTrue(neighbour.moveHomeTo(level,home) == NestHome.MoveResult.MOVED,
                        "Another grounded resident must be able to claim the freed Nest");
                release(player,landing.below());
                var restored = (Bunnidog)level.getEntityInAnyDimension(identity);
                h.assertTrue(restored != null && !TravelBasketItem.occupied(savedBasket) && restored.getUUID().equals(identity)
                        && restored.genome().equals(genome) && restored.stats().equals(stats) && restored.getAge() == age
                        && restored.getHealth() == health && restored.getName().getString().equals("Juniper")
                        && Arrays.equals(recovery,waits(restored)),"Release must restore the same named animal and all paused care/work/meal recoveries without healing or growth");
                h.assertTrue(restored.homeNest() == null && neighbour.homeNest().equals(home),"Release must leave the new owner of the old Nest intact");
                var restoredOutput = TagValueOutput.createWithContext(ProblemReporter.DISCARDING,level.registryAccess());
                restored.saveWithoutId(restoredOutput);
                h.assertTrue(restoredOutput.buildResult().getStringOr("Mother","").equals(Genome.DEFAULT.toString())
                        && !restoredOutput.buildResult().getStringOr("EarTag","").isEmpty(),"Pedigree and permanent registration must survive travel");
                player.setItemInHand(InteractionHand.MAIN_HAND,duplicate);
                release(player,h.absolutePos(new BlockPos(5,0,1)));
                h.assertTrue(TravelBasketItem.occupied(duplicate) && level.getEntityInAnyDimension(identity) == restored,
                        "A copied filled basket must not replace or duplicate an existing identity");
                var malformed = duplicate.copy();
                var invalidData = new CompoundTag(); invalidData.putInt("BunnidogsBasket",1);
                malformed.set(DataComponents.BUCKET_ENTITY_DATA,CustomData.of(invalidData));
                player.setItemInHand(InteractionHand.MAIN_HAND,malformed);
                release(player,h.absolutePos(new BlockPos(5,0,1)));
                h.assertTrue(TravelBasketItem.occupied(malformed),"Malformed occupied data must be retained instead of silently consumed");
                var survival = h.makeMockPlayer(GameType.SURVIVAL);
                var pup = h.spawnWithNoFreeWill(ModEntities.BUNNIDOG.get(),4,1,1);
                pup.setGenome(Genome.DEFAULT); pup.setAge(-1000);
                var pupId = pup.getUUID();
                survival.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(ModItems.TRAVEL_BASKET.get()));
                survival.interactOn(pup,InteractionHand.MAIN_HAND,Vec3.ZERO);
                h.assertTrue(pup.isRemoved() && TravelBasketItem.occupied(survival.getMainHandItem()),"Survival players must also be able to carry a pup");
                h.runAfterDelay(2,() -> {
                    release(survival,h.absolutePos(new BlockPos(3,0,6)));
                    var releasedPup = (Bunnidog)level.getEntityInAnyDimension(pupId);
                    h.assertTrue(releasedPup != null && releasedPup.isBaby() && !TravelBasketItem.occupied(survival.getMainHandItem()),
                            "A released survival pup must retain its identity and age class");
                    h.succeed();
                });
            });
        });
    }
}
