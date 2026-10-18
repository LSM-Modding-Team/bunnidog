package com.bunnidogs.mod.entity;

import com.bunnidogs.mod.item.EarTagItem;
import com.bunnidogs.mod.registry.ModEntities;
import com.bunnidogs.mod.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;

/** Exercises the real two-hand gesture, entity identity and native effect/cooldown rules. */
public final class ResidentFinderGameTest {
    private ResidentFinderGameTest() {}
    public static void run(GameTestHelper h) {
        var level = h.getLevel();
        var player = h.makeMockServerPlayerInLevel();
        player.gameMode.changeGameModeForPlayer(GameType.SURVIVAL);
        var center = Vec3.atBottomCenterOf(h.absolutePos(new BlockPos(3,2,3)));
        player.setPos(center); player.setShiftKeyDown(true);
        var dog = h.spawnWithNoFreeWill(ModEntities.BUNNIDOG.get(),4,2,3);
        var other = h.spawnWithNoFreeWill(ModEntities.BUNNIDOG.get(),4,2,4);
        dog.applyEarTag(); other.applyEarTag();
        var code = dog.earTag().orElseThrow();
        var tag = new ItemStack(ModItems.EAR_TAG.get(),3);
        var tool = new ItemStack(ModItems.CALIPERS.get());
        player.setItemInHand(InteractionHand.MAIN_HAND,tag);
        player.setItemInHand(InteractionHand.OFF_HAND,tool);
        tool.getItem().use(level,player,InteractionHand.OFF_HAND);
        h.assertTrue(!player.getCooldowns().isOnCooldown(tool) && !dog.hasEffect(MobEffects.GLOWING),
                "An unnamed spare tag must explain the gesture without an effect or cooldown");
        tag.set(DataComponents.CUSTOM_NAME,Component.literal("  "+code.toLowerCase(java.util.Locale.ROOT)+"  "));
        h.assertTrue(EarTagItem.finderCode(tag).orElseThrow().equals(code),"Only explicit valid names become normalized finder cues");
        var tagBefore = tag.copy(); var toolBefore = tool.copy();
        var genes = dog.genome(); var stats = dog.stats(); float health = dog.getHealth(); int age = dog.getAge();
        player.gameMode.changeGameModeForPlayer(GameType.ADVENTURE);
        tool.getItem().use(level,player,InteractionHand.OFF_HAND);
        h.assertTrue(!player.getCooldowns().isOnCooldown(tool) && !dog.hasEffect(MobEffects.GLOWING),
                "Adventure permission must refuse a valid finder without starting recovery");
        player.gameMode.changeGameModeForPlayer(GameType.SURVIVAL);
        tool.getItem().use(level,player,InteractionHand.OFF_HAND);
        h.assertTrue(dog.getEffect(MobEffects.GLOWING) != null
                && dog.getEffect(MobEffects.GLOWING).getDuration() == ResidentFinding.OUTLINE_TICKS
                && !other.hasEffect(MobEffects.GLOWING) && player.getCooldowns().isOnCooldown(tool),
                "An off-hand Calipers search must outline the tagged identity, not an otherwise identical neighbor");
        h.assertTrue(ItemStack.matches(tag,tagBefore) && ItemStack.matches(tool,toolBefore)
                && dog.genome().equals(genes) && dog.stats().equals(stats) && dog.getHealth() == health
                && dog.getAge() == age && dog.homeNest() == null && dog.samplingWait() == 0 && dog.scoutingWait() == 0,
                "Finding must preserve whole items, biology, homes and animal recoveries");
        dog.removeEffect(MobEffects.GLOWING);
        tool.getItem().use(level,player,InteractionHand.OFF_HAND);
        h.assertTrue(!dog.hasEffect(MobEffects.GLOWING),"An immediate repeated finder gesture must respect native cooldown");
        player.getCooldowns().removeCooldown(player.getCooldowns().getCooldownGroup(tool));
        dog.setPos(center.add(ResidentFinding.RADIUS+.01,0,0));
        tool.getItem().use(level,player,InteractionHand.OFF_HAND);
        h.assertTrue(!dog.hasEffect(MobEffects.GLOWING) && player.getCooldowns().isOnCooldown(tool),
                "An empty out-of-sphere search must still cool down without outlining a distant match");
        player.getCooldowns().removeCooldown(player.getCooldowns().getCooldownGroup(tool));
        dog.setPos(center.add(ResidentFinding.RADIUS,0,0));
        dog.addEffect(new MobEffectInstance(MobEffects.GLOWING,1000));
        tool.getItem().use(level,player,InteractionHand.OFF_HAND);
        h.assertTrue(dog.getEffect(MobEffects.GLOWING).getDuration() == 1000,
                "A search at the boundary must preserve a longer native outline");
        player.getCooldowns().removeCooldown(player.getCooldowns().getCooldownGroup(tool));
        dog.removeEffect(MobEffects.GLOWING);
        tool.getItem().use(level,player,InteractionHand.OFF_HAND);
        h.assertTrue(dog.hasEffect(MobEffects.GLOWING),"Exactly twenty-four blocks remains inside the finder sphere");
        var remote = h.absolutePos(new BlockPos(4096,2,4096));
        h.assertTrue(!level.hasChunkAt(remote),"Remote search fixture must start unloaded");
        // Native NeoForge Entity.setPos deliberately loads an added entity's destination chunk.
        // Test the actual search at a remote origin without moving the player there.
        h.assertTrue(ResidentFinding.nearby(level,Vec3.atBottomCenterOf(remote),code).isEmpty() && !level.hasChunkAt(remote),
                "Searching an unloaded area through the entity index must request no chunks");
    }
}
