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
            if(java.util.Set.of("thorns","blast_protection","fire_protection","projectile_protection","breach","binding_curse","vanishing_curse","silk_touch","frost_walker","smite","bane_of_arthropods","infinity","multishot","riptide").contains(id))continue;
            if(entry.value().isSupportedItem(stack))stack.enchant(entry,entry.value().getMaxLevel());
        }
        return stack;
    }
    public static void apply(net.minecraft.server.level.ServerPlayer p,String family,String tier) {
        boolean mod=family.equals("bunnidog"),end=tier.equals("endgame"),late=end||tier.equals("late"),mid=late||tier.equals("mid");
        var items=new java.util.ArrayList<ItemStack>();String material=late ? "netherite" : mid ? "diamond" : "iron";
        // Close native containers first so their carried stack has an owner before the explicit replacement.
        p.closeContainer();p.getInventory().clearContent();
        for(var slot:new EquipmentSlot[]{EquipmentSlot.HEAD,EquipmentSlot.CHEST,EquipmentSlot.LEGS,EquipmentSlot.FEET}) {
            String part=switch(slot){case HEAD->"helmet";case CHEST->"chestplate";case LEGS->"leggings";default->"boots";};
            Item item=mod&&late ? (end ? ModItems.SOVEREIGN_ARMOR : ModItems.HEARTBOUND_ARMOR).get(part).get() : net.minecraft.core.registries.BuiltInRegistries.ITEM.getValue(Identifier.withDefaultNamespace(material+"_"+part));
            p.setItemSlot(slot,enchanted(p,item,late));
        }
        for(String tool:new String[]{"sword","pickaxe","axe","shovel","hoe"})items.add(enchanted(p,net.minecraft.core.registries.BuiltInRegistries.ITEM.getValue(Identifier.withDefaultNamespace(material+"_"+tool)),late));
        items.add(enchanted(p,Items.BOW,late));items.add(new ItemStack(Items.ARROW,64));items.add(new ItemStack(Items.COOKED_BEEF,64));items.add(enchanted(p,Items.SHIELD,late));
        if(mid) {items.add(new ItemStack(Items.GOLDEN_APPLE,32));for(int i=0;i<4;i++)items.add(new ItemStack(Items.ENDER_PEARL,16));}
        if(late) {
            items.add(enchanted(p,mod ? end ? ModItems.SOVEREIGN_ELYTRA.get() : ModItems.APEX_WING_HARNESS.get() : Items.ELYTRA,true));
            var rockets=new ItemStack(Items.FIREWORK_ROCKET,64);rockets.set(DataComponents.FIREWORKS,new net.minecraft.world.item.component.Fireworks(3,java.util.List.of()));items.add(rockets);
            items.add(new ItemStack(Items.ENCHANTED_GOLDEN_APPLE,32));
            for(var weapon:end ? java.util.List.of(Items.CROSSBOW,Items.TRIDENT,Items.MACE) : java.util.List.of(Items.CROSSBOW))items.add(enchanted(p,weapon,true));
            var reserve=new java.util.ArrayList<ItemStack>();
            for(int i=0;i<27;i++)reserve.add(mod && i<4 ? new ItemStack(ModItems.PRISM_CELL.get(),64) : mod && i<8 ? new ItemStack(ModItems.VOLATILE_CHARGE.get(),64) : i<8 ? rockets.copy() : i<12 ? new ItemStack(Items.ENDER_PEARL,16) : i<16 ? new ItemStack(Items.OBSIDIAN,64) : i<20 ? new ItemStack(Items.ENCHANTED_GOLDEN_APPLE,64) : new ItemStack(Items.TOTEM_OF_UNDYING));
            var box=new ItemStack(Items.SHULKER_BOX);box.set(DataComponents.CONTAINER,ItemContainerContents.fromItems(reserve));items.add(box);
        }
        items.add(new ItemStack(Items.COBBLESTONE,64));items.add(new ItemStack(Items.OAK_PLANKS,64));items.add(new ItemStack(Items.WATER_BUCKET));
        if(mod) {
            items.add(new ItemStack(ModItems.CALIPERS.get()));items.add(new ItemStack(ModItems.GUAVA.get(),64));
            if(late) {
                items.add(enchanted(p,end ? ModItems.SOVEREIGN_LANCE.get() : ModItems.PRISM_LANCE.get(),true));items.add(new ItemStack(ModItems.PRISM_CELL.get(),64));
                items.add(new ItemStack(ModItems.FIREBALL_LAUNCHER.get()));items.add(new ItemStack(ModItems.VOLATILE_CHARGE.get(),64));
                var flask=new ItemStack(ModItems.ABYSSAL_FLASK.get());String id=java.util.UUID.randomUUID().toString();flask.set(ModItems.FLASK_ID.get(),id);
                var genes=Genome.DEFAULT;
                for(var pair:java.util.List.of(new String[]{"BUILD","woolly"},new String[]{"COAT","ember"},new String[]{"TEMPERAMENT","feral"})) {
                    var gene=Gene.valueOf(pair[0]);for(int n=0;n<2;n++)genes=genes.with(gene,n,gene.alleleNamed(pair[1]).id());
                }
                var recovery=genes;for(int n=0;n<2;n++) {recovery=recovery.with(Gene.TEMPERAMENT,n,Gene.TEMPERAMENT.alleleNamed("loyal").id());recovery=recovery.with(Gene.BUILD,n,Gene.BUILD.alleleNamed("average").id());recovery=recovery.with(Gene.COAT,n,Gene.COAT.alleleNamed("tan").id());}
                int drinks=end ? 96 : 32;
                for(int i=0;i<drinks;i++)com.bunnidogs.mod.expedition.FlaskStorage.get(p.level()).push(id,com.bunnidogs.mod.item.JuiceItem.create(new Infusion((drinks-1-i)%3==0 ? genes : recovery,Stats.of(80,70,75,80,60),end ? 16 : 8)));
                if(end) {items.add(enchanted(p,ModItems.GENETIC_VACUUM.get(),true));items.add(enchanted(p,ModItems.CONCORD_STANDARD.get(),true));items.add(new ItemStack(ModItems.HEART_SPOOL.get(),64));}
                ModItems.ABYSSAL_FLASK.get().refresh(flask,p.level());items.add(flask);
            }
        } else if(late)for(int i=0;i<3;i++) {var potion=new ItemStack(Items.POTION);potion.set(DataComponents.POTION_CONTENTS,new net.minecraft.world.item.alchemy.PotionContents(net.minecraft.world.item.alchemy.Potions.STRONG_STRENGTH));items.add(potion);}
        for(int i=0;i<Math.min(36,items.size());i++)p.getInventory().setItem(i,items.get(i));
        if(late)for(int i=items.size();i<36;i++)p.getInventory().setItem(i,new ItemStack(mod ? ModItems.CATACLYSM_TOTEM.get() : Items.TOTEM_OF_UNDYING));
        p.setItemSlot(EquipmentSlot.OFFHAND,new ItemStack(late ? mod ? ModItems.CATACLYSM_TOTEM.get() : Items.TOTEM_OF_UNDYING : Items.SHIELD));
        com.bunnidogs.mod.expedition.ExpeditionGear.armor(p);p.setHealth(p.getMaxHealth());p.getFoodData().setFoodLevel(20);p.inventoryMenu.broadcastChanges();
    }
}
