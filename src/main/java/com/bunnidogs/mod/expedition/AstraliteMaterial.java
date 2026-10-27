package com.bunnidogs.mod.expedition;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.*;
import net.minecraft.tags.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.equipment.*;

/** The pre-Crown resource tier; artifact variants keep the same Astralite foundation. */
public final class AstraliteMaterial {
    public static final TagKey<Item> REPAIR=TagKey.create(Registries.ITEM,Identifier.fromNamespaceAndPath("bunnidogs","astralite_repair"));
    public static final ArmorMaterial ARMOR=new ArmorMaterial(111,ArmorMaterials.NETHERITE.defense(),18,ArmorMaterials.NETHERITE.equipSound(),5,.15F,REPAIR,ResourceKey.create(EquipmentAssets.ROOT_ID,Identifier.fromNamespaceAndPath("bunnidogs","astralite")));
    public static final ToolMaterial TOOLS=new ToolMaterial(TagKey.create(Registries.BLOCK,Identifier.fromNamespaceAndPath("bunnidogs","incorrect_for_astralite_tool")),2437,12,6,18,REPAIR);
    private AstraliteMaterial() {}
}
