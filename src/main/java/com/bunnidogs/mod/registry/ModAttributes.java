package com.bunnidogs.mod.registry;

import com.bunnidogs.mod.BunnidogsMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.ai.attributes.*;
import net.neoforged.neoforge.registries.*;

/** A truthful native health pool above vanilla's 1024 bound, exclusive to the Crowned Apex. */
public final class ModAttributes {
    public static final DeferredRegister<Attribute> ATTRIBUTES=DeferredRegister.create(Registries.ATTRIBUTE,BunnidogsMod.MODID);
    public static final DeferredHolder<Attribute,Attribute> CROWN_HEALTH=ATTRIBUTES.register("crown_health",()->new RangedAttribute("attribute.bunnidogs.crown_health",6000,1,16000).setSyncable(true));
    private ModAttributes() {}
}
