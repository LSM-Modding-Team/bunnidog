package com.bunnidogs.mod.command;

import com.bunnidogs.mod.BunnidogsMod;
import com.bunnidogs.mod.registry.ModItems;
import com.bunnidogs.mod.genetics.*;
import net.minecraft.commands.Commands;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.ItemContainerContents;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

/** Explicit operator inventory replacement, with ordinary native items and genuinely bred infusions. */
@EventBusSubscriber(modid=BunnidogsMod.MODID)
public final class KitCommands {
    @SubscribeEvent public static void register(RegisterCommandsEvent event) {
        var root=Commands.literal("kit").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS));
        for(String family:new String[]{"vanilla","bunnidog"}) {
            var branch=Commands.literal(family);
            for(String tier:new String[]{"early","mid","late","endgame"})branch.then(Commands.literal(tier).then(Commands.literal("replace").executes(c->{apply(c.getSource().getPlayerOrException(),family,tier);return 1;})).executes(c->{c.getSource().sendSuccess(()->Component.translatable("message.bunnidogs.kit.replace",family,tier),false);return 0;}));
            root.then(branch);
        }
        event.getDispatcher().register(root);
    }
    private static ItemStack enchanted(net.minecraft.server.level.ServerPlayer p,Item item,boolean end) {
        var stack=new ItemStack(item);if(!end)return stack;
        var registry=p.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
        for(var entry:registry.listElements().toList()) {
            String id=entry.key().identifier().getPath();
            if(item==Items.BOW && id.equals("mending") || item==Items.TRIDENT && java.util.Set.of("loyalty","channeling").contains(id))continue;
            if(java.util.Set.of("thorns","blast_protection","fire_protection","projectile_protection","breach","binding_curse","vanishing_curse","silk_touch","frost_walker","smite","bane_of_arthropods","infinity","multishot","riptide").contains(id))continue;
            if(entry.value().isSupportedItem(stack))stack.enchant(entry,entry.value().getMaxLevel());
        }
        if(stack.isDamageableItem()) {
            stack.enchant(registry.getOrThrow(net.minecraft.world.item.enchantment.Enchantments.UNBREAKING),3);
            stack.enchant(registry.getOrThrow(item==Items.BOW ? net.minecraft.world.item.enchantment.Enchantments.INFINITY : net.minecraft.world.item.enchantment.Enchantments.MENDING),1);
        }
        if(item==Items.TRIDENT)stack.enchant(registry.getOrThrow(net.minecraft.world.item.enchantment.Enchantments.RIPTIDE),3);
        return stack;
    }
    public static void apply(net.minecraft.server.level.ServerPlayer p,String family,String tier) {
        boolean mod=family.equals("bunnidog"),end=tier.equals("endgame"),late=end||tier.equals("late"),mid=late||tier.equals("mid");
        var items=new java.util.ArrayList<ItemStack>();var reserve=new java.util.ArrayList<ItemStack>();String material=late ? "netherite" : mid ? "diamond" : "iron";
        var armor=new java.util.EnumMap<EquipmentSlot,ItemStack>(EquipmentSlot.class);
        // Close native containers first so their carried stack has an owner before the explicit replacement.
        p.closeContainer();
        for(var slot:new EquipmentSlot[]{EquipmentSlot.HEAD,EquipmentSlot.CHEST,EquipmentSlot.LEGS,EquipmentSlot.FEET}) {
            String part=switch(slot){case HEAD->"helmet";case CHEST->"chestplate";case LEGS->"leggings";default->"boots";};
            Item item=mod&&late ? (end ? ModItems.SOVEREIGN_ARMOR : ModItems.HEARTBOUND_ARMOR).get(part).get() : net.minecraft.core.registries.BuiltInRegistries.ITEM.getValue(Identifier.withDefaultNamespace(material+"_"+part));
            armor.put(slot,enchanted(p,item,late));
        }
        for(String tool:new String[]{"sword","pickaxe","axe","shovel","hoe"}) {
            var stack=enchanted(p,mod&&late ? ModItems.ASTRALITE_TOOLS.get(tool).get() : net.minecraft.core.registries.BuiltInRegistries.ITEM.getValue(Identifier.withDefaultNamespace(material+"_"+tool)),late);
            (late && (tool.equals("hoe") || tool.equals("shovel") || end && tool.equals("axe")) ? reserve : items).add(stack);
        }
        items.add(enchanted(p,Items.BOW,late));items.add(new ItemStack(Items.ARROW,64));items.add(new ItemStack(end ? Items.EXPERIENCE_BOTTLE : Items.COOKED_BEEF,64));
        if(late)items.add(enchanted(p,Items.SHIELD,true)); // Earlier kits already carry their shield in the offhand.
        if(mid) {items.add(new ItemStack(Items.GOLDEN_APPLE,late ? 64 : 32));for(int i=0;i<4;i++)items.add(new ItemStack(Items.ENDER_PEARL,16));}
        if(late) {
            items.add(enchanted(p,mod ? end ? ModItems.SOVEREIGN_ELYTRA.get() : ModItems.APEX_WING_HARNESS.get() : Items.ELYTRA,true));
            var rockets=new ItemStack(Items.FIREWORK_ROCKET,64);rockets.set(DataComponents.FIREWORKS,new net.minecraft.world.item.component.Fireworks(1,java.util.List.of()));items.add(rockets);
            items.add(new ItemStack(Items.ENCHANTED_GOLDEN_APPLE,end ? 20 : 5));
            reserve.add(enchanted(p,Items.CROSSBOW,true));
            if(end) {reserve.add(enchanted(p,Items.TRIDENT,true));items.add(enchanted(p,Items.MACE,true));}
            if(end)for(int i=0;i<3;i++)reserve.add(new ItemStack(Items.GOLDEN_APPLE,64));
            for(int i=0;i<2;i++)reserve.add(rockets.copy());
            for(int i=0;i<4;i++)reserve.add(new ItemStack(Items.ENDER_PEARL,16));
            for(int i=0;i<(end ? 2 : 3);i++)reserve.add(new ItemStack(Items.OBSIDIAN,64));
            reserve.add(new ItemStack(end ? Items.EXPERIENCE_BOTTLE : Items.COOKED_BEEF,64));
            if(mod)for(int i=0;i<4;i++) {reserve.add(new ItemStack(ModItems.PRISM_CELL.get(),64));reserve.add(new ItemStack(ModItems.VOLATILE_CHARGE.get(),64));}
            if(reserve.size()>27)throw new IllegalStateException("Kit reserve exceeds its Shulker: "+family+" "+tier);
            while(reserve.size()<27)reserve.add(new ItemStack(mod ? ModItems.CATACLYSM_TOTEM.get() : Items.TOTEM_OF_UNDYING));
            var box=new ItemStack(Items.SHULKER_BOX);box.set(DataComponents.CONTAINER,ItemContainerContents.fromItems(reserve));items.add(box);
        }
        items.add(new ItemStack(end ? Items.END_CRYSTAL : Items.COBBLESTONE,64));items.add(new ItemStack(end ? Items.END_CRYSTAL : Items.OAK_PLANKS,64));items.add(new ItemStack(Items.WATER_BUCKET));
        if(end) {items.add(new ItemStack(Items.OBSIDIAN,64));for(int i=0;i<2;i++)items.add(new ItemStack(Items.WIND_CHARGE,64));}
        else {items.add(new ItemStack(Items.LAVA_BUCKET));if(mid)items.add(new ItemStack(Items.COBWEB,late ? 16 : 8));}
        if(mod) {
            items.add(new ItemStack(ModItems.CALIPERS.get()));items.add(new ItemStack(ModItems.GUAVA.get(),64));
            if(late) {
                items.add(enchanted(p,end ? ModItems.SOVEREIGN_LANCE.get() : ModItems.PRISM_LANCE.get(),true));items.add(new ItemStack(ModItems.PRISM_CELL.get(),64));
                items.add(enchanted(p,ModItems.FIREBALL_LAUNCHER.get(),true));items.add(new ItemStack(ModItems.VOLATILE_CHARGE.get(),64));
                var flask=new ItemStack(ModItems.ABYSSAL_FLASK.get());String id=java.util.UUID.randomUUID().toString();flask.set(ModItems.FLASK_ID.get(),id);
                var genes=Genome.DEFAULT;
                for(var pair:java.util.List.of(new String[]{"BUILD","woolly"},new String[]{"COAT","ember"},new String[]{"TEMPERAMENT","feral"})) {
                    var gene=Gene.valueOf(pair[0]);for(int n=0;n<2;n++)genes=genes.with(gene,n,gene.alleleNamed(pair[1]).id());
                }
                var recovery=genes;for(int n=0;n<2;n++) {recovery=recovery.with(Gene.TEMPERAMENT,n,Gene.TEMPERAMENT.alleleNamed("loyal").id());recovery=recovery.with(Gene.BUILD,n,Gene.BUILD.alleleNamed("average").id());recovery=recovery.with(Gene.COAT,n,Gene.COAT.alleleNamed("tan").id());}
                int drinks=end ? 96 : 32;
                for(int i=0;i<drinks;i++)com.bunnidogs.mod.expedition.FlaskStorage.get(p.level()).push(id,com.bunnidogs.mod.item.JuiceItem.create(new Infusion((drinks-1-i)%3==0 ? genes : recovery,Stats.of(80,70,75,80,60),end ? 116 : 24)));
                if(end) {items.add(enchanted(p,ModItems.GENETIC_VACUUM.get(),true));items.add(enchanted(p,ModItems.CONCORD_STANDARD.get(),true));items.add(new ItemStack(ModItems.HEART_SPOOL.get(),64));}
                ModItems.ABYSSAL_FLASK.get().refresh(flask,p.level());items.add(flask);
            }
        } else if(late) {
            for(var kind:java.util.List.of(net.minecraft.world.item.alchemy.Potions.STRONG_STRENGTH,net.minecraft.world.item.alchemy.Potions.STRONG_STRENGTH,net.minecraft.world.item.alchemy.Potions.STRONG_STRENGTH,net.minecraft.world.item.alchemy.Potions.STRONG_SWIFTNESS,net.minecraft.world.item.alchemy.Potions.LONG_FIRE_RESISTANCE)) {
                var potion=new ItemStack(Items.POTION);potion.set(DataComponents.POTION_CONTENTS,new net.minecraft.world.item.alchemy.PotionContents(kind));items.add(potion);
            }
        }
        for(var item:items)if(item.is(ModItems.SOVEREIGN_LANCE.get()) || item.is(ModItems.FIREBALL_LAUNCHER.get()) || item.is(ModItems.CONCORD_STANDARD.get()))item.set(ModItems.TOOL_POWER.get(),end ? 5 : 1);
        if(items.size()>36)throw new IllegalStateException("Kit exceeds main inventory: "+family+" "+tier);
        p.getInventory().clearContent();armor.forEach(p::setItemSlot);
        for(int i=0;i<items.size();i++)p.getInventory().setItem(i,items.get(i));
        if(late)for(int i=items.size();i<36;i++)p.getInventory().setItem(i,new ItemStack(mod ? ModItems.CATACLYSM_TOTEM.get() : Items.TOTEM_OF_UNDYING));
        p.setItemSlot(EquipmentSlot.OFFHAND,new ItemStack(late ? mod ? ModItems.CATACLYSM_TOTEM.get() : Items.TOTEM_OF_UNDYING : Items.SHIELD));
        com.bunnidogs.mod.expedition.ExpeditionGear.armor(p);p.setHealth(p.getMaxHealth());p.getFoodData().setFoodLevel(20);p.inventoryMenu.broadcastChanges();
    }
}
