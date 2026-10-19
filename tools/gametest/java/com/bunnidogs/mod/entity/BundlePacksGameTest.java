package com.bunnidogs.mod.entity;

import com.bunnidogs.mod.genetics.Camouflage;
import com.bunnidogs.mod.genetics.Carrying;
import com.bunnidogs.mod.genetics.Gene;
import com.bunnidogs.mod.genetics.Genome;
import com.bunnidogs.mod.genetics.Stats;
import com.bunnidogs.mod.item.CalipersItem;
import com.bunnidogs.mod.item.TissueSampleItem;
import com.bunnidogs.mod.item.TravelBasketItem;
import com.bunnidogs.mod.registry.ModEntities;
import com.bunnidogs.mod.registry.ModItems;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.BundleContents;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;

/** Whole native bundle components, actual hand controls, build shrink, equipment death and existing basket transport. */
public final class BundlePacksGameTest {
    private BundlePacksGameTest() {}
    private static Genome build(String name) {
        int id = Gene.BUILD.alleleNamed(name).id(); return Genome.DEFAULT.with(Gene.BUILD,0,id).with(Gene.BUILD,1,id);
    }
    private static ItemStack cargo(int index) {
        var contents = new BundleContents.Mutable(BundleContents.EMPTY);
        contents.tryInsert(new ItemStack(Items.DIAMOND,3+index));
        contents.tryInsert(TissueSampleItem.create(build("woolly"),Stats.of(index,2,3,4,5)));
        var bundle = new ItemStack(Items.DYED_BUNDLE.pick(index%2 == 0 ? net.minecraft.world.item.DyeColor.BLUE : net.minecraft.world.item.DyeColor.RED));
        bundle.set(DataComponents.BUNDLE_CONTENTS,contents.toImmutable());
        bundle.set(DataComponents.CUSTOM_NAME,Component.literal("Field bundle "+index)); return bundle;
    }
    private static Bunnidog dog(GameTestHelper h, int y, Genome genome) {
        var dog = h.spawnWithNoFreeWill(ModEntities.BUNNIDOG.get(),3,y,3);
        dog.setGenome(genome); dog.setStats(Stats.ZERO); dog.setHealth(dog.getMaxHealth()); dog.setOnGround(true); return dog;
    }
    private static void floor(GameTestHelper h, int y) {
        for (int x = 0; x < 9; x++) for (int z = 0; z < 8; z++) h.setBlock(new BlockPos(x,y,z),Blocks.SAND);
    }
    private static boolean same(List<ItemStack> a, List<ItemStack> b) {
        if (a.size() != b.size()) return false;
        for (int i = 0; i < a.size(); i++) if (!ItemStack.matches(a.get(i),b.get(i))) return false;
        return true;
    }
    public static void rules(GameTestHelper h) {
        var level = h.getLevel(); floor(h,310);
        var dog = dog(h,311,build("chonker"));
        var player = h.makeMockServerPlayerInLevel(); player.gameMode.changeGameModeForPlayer(GameType.SURVIVAL);
        String[] builds = {"lean","slim","average","stocky","woolly","chonker"}; int[] capacities = {1,1,2,3,3,4};
        for (int i = 0; i < builds.length; i++) {
            dog.setGenome(build(builds[i])); h.assertTrue(dog.bundleCapacity() == capacities[i],"Every expressed build has its native carrying capacity: "+builds[i]);
        }
        dog.setGenome(Genome.DEFAULT.with(Gene.BUILD,0,0).with(Gene.BUILD,1,Gene.BUILD.alleleNamed("slim").id()));
        h.assertTrue(dog.bundleCapacity() == 2,"A hidden slim allele does not replace expressed average carrying room"); dog.setGenome(build("chonker"));
        var originals = java.util.stream.IntStream.range(0,4).mapToObj(BundlePacksGameTest::cargo).toList();
        for (var bundle : originals) {
            player.setItemInHand(InteractionHand.OFF_HAND,bundle.copy()); dog.mobInteract(player,InteractionHand.OFF_HAND);
            h.assertTrue(player.getOffhandItem().isEmpty(),"Actual survival off-hand equipment spends its whole bundle once");
        }
        h.assertTrue(dog.carriedBundleCount() == 4 && same(dog.carriedBundles(),originals) && dog.carryingStatus() == Carrying.Status.FULL,
                "Cargo preserves exact colored/named native bundles and their sample/diamond contents");
        h.assertTrue(dog.camouflageStatus() == Camouflage.Status.DISTURBED,"Visible bundle cargo exposes an otherwise matching calm coat");
        var refused = cargo(8); player.setItemInHand(InteractionHand.OFF_HAND,refused); dog.mobInteract(player,InteractionHand.OFF_HAND);
        h.assertTrue(ItemStack.matches(refused,cargo(8)) && dog.carriedBundleCount() == 4,"A full carrier keeps the offered bundle and its contents without spilling it");
        dog.setGenome(build("lean"));
        h.assertTrue(dog.bundleCapacity() == 1 && dog.carriedBundleCount() == 4 && same(dog.carriedBundles(),originals),
                "A smaller expressed build must preserve every existing excess bundle for retrieval");
        var speed = dog.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED);
        speed.addPermanentModifier(new net.minecraft.world.entity.ai.attributes.AttributeModifier(net.minecraft.resources.Identifier.fromNamespaceAndPath("bunnidogs","test_cargo_external"),.1,
                net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_VALUE));
        h.assertTrue(dog.carryingSlowdown() == 75 && Math.abs(speed.getValue()-(speed.getBaseValue()+.1)*.25) < .00001,
                "Over-capacity movement uses one named multiplier while preserving external speed modifiers");
        var saved = TagValueOutput.createWithContext(ProblemReporter.DISCARDING,level.registryAccess()); dog.saveWithoutId(saved);
        var loaded = ModEntities.BUNNIDOG.get().create(level,EntitySpawnReason.LOAD);
        loaded.load(TagValueInput.create(ProblemReporter.DISCARDING,level.registryAccess(),saved.buildResult()));
        h.assertTrue(loaded.bundleCapacity() == 1 && loaded.carriedBundleCount() == 4 && same(loaded.carriedBundles(),originals),
                "Native animal load restores all over-capacity whole bundle components and its synced gear count");
        h.assertTrue(Math.abs(loaded.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED)-speed.getValue()) < .00001,
                "Native reload restores one overload penalty alongside external speed modifiers");
        var snapshot = dog.carriedBundles(); snapshot.getFirst().shrink(1);
        h.assertTrue(same(dog.carriedBundles(),originals),"Read-only cargo snapshots must not alias the animal's owned item stacks");
        var before = TagValueOutput.createWithContext(ProblemReporter.DISCARDING,level.registryAccess()); dog.saveWithoutId(before);
        CalipersItem.showCare(player,dog);
        var after = TagValueOutput.createWithContext(ProblemReporter.DISCARDING,level.registryAccess()); dog.saveWithoutId(after);
        h.assertTrue(before.buildResult().equals(after.buildResult()),"Calipers cargo inspection keeps all native animal state unchanged");
        player.setItemInHand(InteractionHand.OFF_HAND,ItemStack.EMPTY); player.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY); player.setShiftKeyDown(true);
        player.gameMode.changeGameModeForPlayer(GameType.ADVENTURE); dog.mobInteract(player,InteractionHand.MAIN_HAND);
        h.assertTrue(dog.carriedBundleCount() == 4,"Adventure empty-hand inspection cannot withdraw equipment");
        player.gameMode.changeGameModeForPlayer(GameType.SURVIVAL); player.getInventory().clearContent();
        for (int i = 3; i >= 0; i--) {
            player.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY); dog.mobInteract(player,InteractionHand.MAIN_HAND);
            int selected = i;
            h.assertTrue(dog.carriedBundleCount() == i && java.util.stream.IntStream.range(0,player.getInventory().getContainerSize())
                    .anyMatch(slot -> ItemStack.matches(player.getInventory().getItem(slot),originals.get(selected))),"Empty-hand retrieval returns the exact last bundle before the next one");
            player.getInventory().clearContent();
        }
        dog.setOnGround(true);
        h.assertTrue(dog.carriedBundleCount() == 0 && dog.camouflageStatus() == Camouflage.Status.BLENDED,"Removing all cargo restores matching-ground camouflage");
        h.assertTrue(dog.carryingSlowdown() == 0 && Math.abs(speed.getValue()-speed.getBaseValue()-.1) < .00001,"Retrieving the excess removes only its own speed modifier");
        dog.setBaby(true); player.setShiftKeyDown(false); player.setItemInHand(InteractionHand.MAIN_HAND,cargo(0)); dog.mobInteract(player,InteractionHand.MAIN_HAND);
        h.assertTrue(dog.carriedBundleCount() == 0 && ItemStack.matches(player.getMainHandItem(),cargo(0)),"Pups keep offered bundle stock"); dog.setAge(0);
        player.gameMode.changeGameModeForPlayer(GameType.CREATIVE); var creative = cargo(1); player.setItemInHand(InteractionHand.MAIN_HAND,creative); dog.mobInteract(player,InteractionHand.MAIN_HAND);
        h.assertTrue(dog.carriedBundleCount() == 1 && ItemStack.matches(player.getMainHandItem(),cargo(1)),"Creative preserves the actual bundle and creates one equipped copy");
        boolean[] observed = {false};
        Consumer<LivingDropsEvent> dying = event -> {
            if (event.getEntity() == dog) {
                observed[0] = dog.carriedBundleCount() == 0 && dog.carriedBundles().isEmpty();
                dog.dropEquipment(level);
            }
        };
        NeoForge.EVENT_BUS.addListener(dying); boolean mobDrops = level.getGameRules().get(GameRules.MOB_DROPS);
        try {
            level.getGameRules().set(GameRules.MOB_DROPS,false,level.getServer());
            dog.hurtServer(level,level.damageSources().genericKill(),1000);
        } finally { level.getGameRules().set(GameRules.MOB_DROPS,mobDrops,level.getServer()); NeoForge.EVENT_BUS.unregister(dying); }
        var drops = level.getEntitiesOfClass(ItemEntity.class,new AABB(h.absolutePos(new BlockPos(3,311,3))).inflate(3),e -> e.getItem().getItem() instanceof net.minecraft.world.item.BundleItem);
        h.assertTrue(observed[0] && drops.size() == 1 && ItemStack.matches(drops.getFirst().getItem(),cargo(1)),
                "Actual death drops player cargo once with native equipment even when mob loot is off, reserving before reentrant drop callbacks");
    }
    public static void travel(GameTestHelper h) {
        var level = h.getLevel(); floor(h,280);
        var dog = dog(h,281,build("stocky"));
        var player = h.makeMockServerPlayerInLevel(); player.gameMode.changeGameModeForPlayer(GameType.SURVIVAL);
        for (int i = 0; i < 3; i++) { player.setItemInHand(InteractionHand.MAIN_HAND,cargo(i)); dog.mobInteract(player,InteractionHand.MAIN_HAND); }
        var expected = dog.carriedBundles(); var uuid = dog.getUUID();
        var basket = new ItemStack(ModItems.TRAVEL_BASKET.get());
        h.assertTrue(TravelBasketItem.capture(level,basket,dog) == TravelBasketItem.Result.CAPTURED && dog.isRemoved(),"The existing basket can pack a calm equipped carrier");
        var spot = h.absolutePos(new BlockPos(6,281,3));
        h.assertTrue(TravelBasketItem.release(level,spot,basket) == TravelBasketItem.Result.RELEASED && !TravelBasketItem.occupied(basket),"The existing basket can release the equipped animal onto safe ground");
        var restored = (Bunnidog)level.getEntity(uuid);
        h.assertTrue(restored != null && restored.carriedBundleCount() == 3 && same(restored.carriedBundles(),expected),
                "Native basket identity/whole entity data must retain every bundle and all its native contents");
        h.assertTrue(level.getEntitiesOfClass(ItemEntity.class,new AABB(spot).inflate(4),e -> e.getItem().getItem() instanceof net.minecraft.world.item.BundleItem).isEmpty(),
                "Basket discard and release must not also drop the equipped bundles");
        restored.discard(); h.succeed();
    }
}
