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
            for(String tier:new String[]{"early","mid","late","endgame"}) {
                var stage=command(family,tier,"crystal",tier);
                if(tier.equals("endgame"))for(String style:new String[]{"crystal","mace"})stage.then(command(family,tier,style,tier+" "+style));
                branch.then(stage);
            }
            root.then(branch);
        }
        event.getDispatcher().register(root);
    }
    private static com.mojang.brigadier.builder.LiteralArgumentBuilder<net.minecraft.commands.CommandSourceStack> command(String family,String tier,String style,String label) {
        return Commands.literal(label.contains(" ") ? style : tier)
            .then(Commands.literal("replace").executes(c->{apply(c.getSource().getPlayerOrException(),family,tier,style);return 1;}))
            .executes(c->{c.getSource().sendSuccess(()->Component.translatable("message.bunnidogs.kit.replace",family,label),false);return 0;});
    }
    private static ItemStack gear(net.minecraft.server.level.ServerPlayer p,Item item,boolean late,boolean mid) {
        if(late || !mid)return enchanted(p,item,late);
        var stack=new ItemStack(item);var registry=p.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
        var keys=java.util.List.of(net.minecraft.world.item.enchantment.Enchantments.PROTECTION,net.minecraft.world.item.enchantment.Enchantments.SHARPNESS,net.minecraft.world.item.enchantment.Enchantments.POWER,net.minecraft.world.item.enchantment.Enchantments.EFFICIENCY,net.minecraft.world.item.enchantment.Enchantments.UNBREAKING);
        for(var key:keys) {var entry=registry.getOrThrow(key);if(entry.value().isSupportedItem(stack))stack.enchant(entry,key.equals(net.minecraft.world.item.enchantment.Enchantments.EFFICIENCY) ? 3 : 2);}
        return stack;
    }
    private static ItemStack breachMace(net.minecraft.server.level.ServerPlayer p) {
        var mace=enchanted(p,Items.MACE,true);var registry=p.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
        var enchants=new net.minecraft.world.item.enchantment.ItemEnchantments.Mutable(mace.get(DataComponents.ENCHANTMENTS));
        enchants.set(registry.getOrThrow(net.minecraft.world.item.enchantment.Enchantments.DENSITY),0);
        enchants.set(registry.getOrThrow(net.minecraft.world.item.enchantment.Enchantments.BREACH),4);
        mace.set(DataComponents.ENCHANTMENTS,enchants.toImmutable());return mace;
    }
    private static ItemStack healing() {
        var stack=new ItemStack(Items.SPLASH_POTION);stack.set(DataComponents.POTION_CONTENTS,new net.minecraft.world.item.alchemy.PotionContents(net.minecraft.world.item.alchemy.Potions.STRONG_HEALING));return stack;
    }
    /** Move real stacks; never duplicate an item to populate a preferred hotbar. */
    private static void organize(java.util.List<ItemStack> items,java.util.List<Item> hotbar) {
        var ordered=new java.util.ArrayList<ItemStack>();
        for(var item:hotbar) {
            int found=-1;for(int i=0;i<items.size();i++)if(items.get(i).is(item)){found=i;break;}
            if(found<0)throw new IllegalStateException("Missing hotbar item: "+item);
            ordered.add(items.remove(found));
        }
        ordered.addAll(items);items.clear();items.addAll(ordered);
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
        apply(p,family,tier,"crystal");
    }
    public static void apply(net.minecraft.server.level.ServerPlayer p,String family,String tier,String style) {
        if(!java.util.Set.of("vanilla","bunnidog").contains(family) || !java.util.Set.of("early","mid","late","endgame").contains(tier) || !java.util.Set.of("crystal","mace").contains(style))throw new IllegalArgumentException("Unknown kit");
        boolean mod=family.equals("bunnidog"),end=tier.equals("endgame"),late=end||tier.equals("late"),mid=late||tier.equals("mid");
        boolean mace=end && style.equals("mace");
        var items=new java.util.ArrayList<ItemStack>();var reserve=new java.util.ArrayList<ItemStack>();String material=late ? "netherite" : mid ? "diamond" : "iron";
        var armor=new java.util.EnumMap<EquipmentSlot,ItemStack>(EquipmentSlot.class);
        // Close native containers first so their carried stack has an owner before the explicit replacement.
        p.closeContainer();
        for(var slot:new EquipmentSlot[]{EquipmentSlot.HEAD,EquipmentSlot.CHEST,EquipmentSlot.LEGS,EquipmentSlot.FEET}) {
            String part=switch(slot){case HEAD->"helmet";case CHEST->"chestplate";case LEGS->"leggings";default->"boots";};
            Item item=mod&&late ? (end ? ModItems.SOVEREIGN_ARMOR : ModItems.HEARTBOUND_ARMOR).get(part).get() : net.minecraft.core.registries.BuiltInRegistries.ITEM.getValue(Identifier.withDefaultNamespace(material+"_"+part));
            armor.put(slot,gear(p,item,late,mid));
        }
        for(String tool:new String[]{"sword","pickaxe","axe","shovel","hoe"}) {
            var stack=gear(p,mod&&late ? ModItems.ASTRALITE_TOOLS.get(tool).get() : net.minecraft.core.registries.BuiltInRegistries.ITEM.getValue(Identifier.withDefaultNamespace(material+"_"+tool)),late,mid);
            (late && (tool.equals("hoe") || tool.equals("shovel") || end && tool.equals("axe")) ? reserve : items).add(stack);
        }
        items.add(gear(p,Items.BOW,late,mid));items.add(new ItemStack(Items.ARROW,64));items.add(new ItemStack(end ? Items.EXPERIENCE_BOTTLE : Items.COOKED_BEEF,64));
        if(late)items.add(enchanted(p,Items.SHIELD,true)); // Earlier kits already carry their shield in the offhand.
        if(mid) {items.add(new ItemStack(Items.GOLDEN_APPLE,late ? 64 : 32));for(int i=0;i<4;i++)items.add(new ItemStack(Items.ENDER_PEARL,16));}
        if(late) {
            items.add(enchanted(p,mod ? end ? ModItems.SOVEREIGN_ELYTRA.get() : ModItems.APEX_WING_HARNESS.get() : Items.ELYTRA,true));
            var rockets=new ItemStack(Items.FIREWORK_ROCKET,64);rockets.set(DataComponents.FIREWORKS,new net.minecraft.world.item.component.Fireworks(1,java.util.List.of()));items.add(rockets);
            items.add(new ItemStack(Items.ENCHANTED_GOLDEN_APPLE,end ? 20 : 5));
            reserve.add(enchanted(p,Items.CROSSBOW,true));
            if(end) {reserve.add(enchanted(p,Items.TRIDENT,true));(mace ? items : reserve).add(enchanted(p,Items.MACE,true));if(mace)reserve.add(breachMace(p));}
            if(end)for(int i=0;i<3;i++)reserve.add(new ItemStack(Items.GOLDEN_APPLE,64));
            for(int i=0;i<2;i++)reserve.add(rockets.copy());
            for(int i=0;i<4;i++)reserve.add(new ItemStack(Items.ENDER_PEARL,16));
            for(int i=0;i<(mace ? 0 : end ? 2 : 3);i++)reserve.add(new ItemStack(Items.OBSIDIAN,64));
            reserve.add(new ItemStack(Items.EXPERIENCE_BOTTLE,64));
            if(mod)for(int i=0;i<4;i++) {reserve.add(new ItemStack(ModItems.PRISM_CELL.get(),64));reserve.add(new ItemStack(ModItems.VOLATILE_CHARGE.get(),64));}
            if(reserve.size()>27)throw new IllegalStateException("Kit reserve exceeds its Shulker: "+family+" "+tier);
            while(reserve.size()<27)reserve.add(new ItemStack(mod ? ModItems.CATACLYSM_TOTEM.get() : Items.TOTEM_OF_UNDYING));
            var box=new ItemStack(Items.SHULKER_BOX);box.set(DataComponents.CONTAINER,ItemContainerContents.fromItems(reserve));items.add(box);
        }
        items.add(new ItemStack(Items.WATER_BUCKET));
        if(end) {
            if(mace)items.add(new ItemStack(Items.COBBLESTONE,64));
            else {for(int i=0;i<2;i++)items.add(new ItemStack(Items.END_CRYSTAL,64));items.add(new ItemStack(Items.OBSIDIAN,64));items.add(new ItemStack(Items.RESPAWN_ANCHOR,64));items.add(new ItemStack(Items.GLOWSTONE,64));}
            items.add(new ItemStack(Items.COBWEB,64));for(int i=0;i<2;i++)items.add(new ItemStack(Items.WIND_CHARGE,64));
        } else {items.add(new ItemStack(Items.COBBLESTONE,64));items.add(new ItemStack(Items.OAK_PLANKS,64));items.add(new ItemStack(Items.LAVA_BUCKET));if(mid)items.add(new ItemStack(Items.COBWEB,late ? 16 : 8));}
        if(mid && (!mod || !late))for(int i=0;i<2;i++)items.add(healing());
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
        var totem=mod ? ModItems.CATACLYSM_TOTEM.get() : Items.TOTEM_OF_UNDYING;
        if(late)while(items.size()<36)items.add(new ItemStack(totem));
        Item sword=mod&&late ? ModItems.ASTRALITE_TOOLS.get("sword").get() : net.minecraft.core.registries.BuiltInRegistries.ITEM.getValue(Identifier.withDefaultNamespace(material+"_sword"));
        Item pick=mod&&late ? ModItems.ASTRALITE_TOOLS.get("pickaxe").get() : net.minecraft.core.registries.BuiltInRegistries.ITEM.getValue(Identifier.withDefaultNamespace(material+"_pickaxe"));
        Item axe=mod&&late ? ModItems.ASTRALITE_TOOLS.get("axe").get() : net.minecraft.core.registries.BuiltInRegistries.ITEM.getValue(Identifier.withDefaultNamespace(material+"_axe"));
        Item primary=mod&&late ? (end ? ModItems.SOVEREIGN_LANCE.get() : ModItems.PRISM_LANCE.get()) : sword;
        Item sustain=mod&&late ? ModItems.ABYSSAL_FLASK.get() : Items.GOLDEN_APPLE;
        if(end)organize(items,mace ? java.util.List.of(primary,Items.MACE,Items.WIND_CHARGE,Items.ENDER_PEARL,Items.WATER_BUCKET,sustain,Items.COBWEB,Items.COBBLESTONE,totem)
            : java.util.List.of(primary,Items.END_CRYSTAL,Items.OBSIDIAN,pick,Items.ENDER_PEARL,sustain,Items.WIND_CHARGE,Items.WATER_BUCKET,totem));
        else if(late)organize(items,java.util.List.of(primary,axe,Items.BOW,pick,Items.WATER_BUCKET,mod ? ModItems.ABYSSAL_FLASK.get() : Items.LAVA_BUCKET,Items.GOLDEN_APPLE,Items.ENDER_PEARL,totem));
        else organize(items,java.util.List.of(sword,axe,Items.BOW,pick,Items.WATER_BUCKET,Items.LAVA_BUCKET,mid ? Items.GOLDEN_APPLE : Items.COOKED_BEEF,mid ? Items.ENDER_PEARL : Items.OAK_PLANKS,Items.COBBLESTONE));
        p.getInventory().clearContent();armor.forEach(p::setItemSlot);
        for(int i=0;i<items.size();i++)p.getInventory().setItem(i,items.get(i));
        p.setItemSlot(EquipmentSlot.OFFHAND,late ? new ItemStack(totem) : gear(p,Items.SHIELD,false,mid));
        com.bunnidogs.mod.expedition.ExpeditionGear.armor(p);p.setHealth(p.getMaxHealth());p.getFoodData().setFoodLevel(20);p.inventoryMenu.broadcastChanges();
    }
}
