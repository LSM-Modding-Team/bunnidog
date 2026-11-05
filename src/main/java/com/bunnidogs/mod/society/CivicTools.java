package com.bunnidogs.mod.society;

import net.minecraft.world.item.*;

/** Shared native tools may be worn, but named, enchanted and otherwise custom gear remains private. */
public final class CivicTools {
    private CivicTools() {}
    private static boolean usable(ItemStack stack,Item item) {
        if(!stack.is(item) || stack.getCount()!=1 || stack.getDamageValue()>=stack.getMaxDamage())return false;
        var plain=stack.copy();plain.setDamageValue(0);return CivicRecipes.ordinary(plain);
    }
    public static boolean usableAxe(ItemStack stack) {return usable(stack,Items.WOODEN_AXE);}
    public static boolean usablePickaxe(ItemStack stack) {return usable(stack,Items.WOODEN_PICKAXE);}
    /** Only an actual started pickup or original owned walking tool can postpone another paid tool bill. */
    public static boolean recovering(HearthTreeBlockEntity home,Item tool) {return home.residents().stream().anyMatch(d->d.society().recoveringTool(home,tool));}
    public static int axeSlot(HearthTreeBlockEntity home) {
        for(int slot=0;slot<home.getContainerSize();slot++)if(usableAxe(home.getItem(slot)))return slot;return -1;
    }
    public static int pickaxeSlot(HearthTreeBlockEntity home) {
        for(int slot=0;slot<home.getContainerSize();slot++)if(usablePickaxe(home.getItem(slot)))return slot;return -1;
    }
}
