package com.bunnidogs.mod.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModEffects {
    private ModEffects() {}
    public static final DeferredRegister<MobEffect> EFFECTS=DeferredRegister.create(Registries.MOB_EFFECT,"bunnidogs");
    public static final net.neoforged.neoforge.registries.DeferredHolder<MobEffect,MobEffect> STAGGER=EFFECTS.register("apex_stagger",()->
            new MobEffect(MobEffectCategory.HARMFUL,0xD881AF) {}
                    .addAttributeModifier(Attributes.MOVEMENT_SPEED,Identifier.fromNamespaceAndPath("bunnidogs","stagger_walk"),-1,AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL)
                    .addAttributeModifier(Attributes.JUMP_STRENGTH,Identifier.fromNamespaceAndPath("bunnidogs","stagger_jump"),-1,AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
}
