package com.bunnidogs.mod.entity;

import com.bunnidogs.mod.realm.RealmLoot;
import com.bunnidogs.mod.registry.*;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;

final class RealmProgressionGameTest {
    static void recipe(GameTestHelper h) {
        var level=h.getLevel();var at=ApexTypesGameTest.center(h);var parts=new EnumMap<ApexKind,ItemStack>(ApexKind.class);ItemStack wing=ItemStack.EMPTY;
        for(var kind:ApexKind.values())for(boolean flying:new boolean[]{false,true}) {
            var dog=ApexTypesGameTest.apex(h,kind,flying,at);var event=new LivingDropsEvent(dog,level.damageSources().generic(),new ArrayList<>(),true);RealmLoot.parts(event,dog,level);var remnant=event.getDrops().iterator().next().getItem();
            h.assertTrue(remnant.getOrDefault(ModItems.GROUND_REMNANT.get(),false)==!flying,"Only real ground-role loot carries the visible ground-remnant component");
            if(!flying)parts.put(kind,remnant.copyWithCount(1));else for(var drop:event.getDrops())if(drop.getItem().is(ModItems.AERIAL_MEMBRANE.get()))wing=drop.getItem().copyWithCount(1);dog.discard();
        }
        var recipe=(CraftingRecipe)level.recipeAccess().byKey(ResourceKey.create(Registries.RECIPE,Identifier.fromNamespaceAndPath("bunnidogs","resonant_eye"))).orElseThrow().value();
        var grid=new ArrayList<>(List.of(parts.get(ApexKind.BRUTE),parts.get(ApexKind.PRISM),parts.get(ApexKind.BROOD),parts.get(ApexKind.BLAST),new ItemStack(Items.ENDER_EYE),parts.get(ApexKind.SUPPORT),wing,new ItemStack(ModItems.STARLIT_DUST.get()),new ItemStack(Items.NETHERITE_SCRAP)));
        var input=CraftingInput.of(3,3,grid);h.assertTrue(recipe.matches(input,level) && recipe.assemble(input).is(ModItems.RESONANT_EYE.get()),"The real recipe combines all five physical ground remnants and an aerial drop, with ordinary components");
        for(int slot:new int[]{0,1,2,3,5}) {var old=grid.get(slot);var absent=old.copy();absent.remove(ModItems.GROUND_REMNANT.get());grid.set(slot,absent);h.assertTrue(!recipe.matches(CraftingInput.of(3,3,grid),level),"A flying/unmarked substitute cannot bypass a missing ground ingredient");grid.set(slot,old);}
        grid.set(6,ItemStack.EMPTY);h.assertTrue(!recipe.matches(CraftingInput.of(3,3,grid),level),"Missing aerial material refuses the same native recipe");
    }
    static List<ItemStack> contents(net.minecraft.server.level.ServerPlayer p) {
        var result=new ArrayList<ItemStack>();for(int i=0;i<p.getInventory().getContainerSize();i++) {var stack=p.getInventory().getItem(i);if(stack.isEmpty())continue;result.add(stack);var inside=stack.get(DataComponents.CONTAINER);if(inside!=null)inside.nonEmptyItems().forEach(t->result.add(t.create()));}return result;
    }
    static void kits(GameTestHelper h) {
        var p=ApexTypesGameTest.player(h,ApexTypesGameTest.center(h));
        for(String family:new String[]{"vanilla","bunnidog"})for(String tier:new String[]{"early","mid","late","endgame"}) {
            com.bunnidogs.mod.command.KitCommands.apply(p,family,tier);var all=contents(p);boolean end=tier.equals("endgame");
            boolean late=end || tier.equals("late");var ench=p.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
            if(!late) {
                h.assertTrue(count(all,Items.COOKED_BEEF)==64 && count(all,Items.END_CRYSTAL)==0 && count(all,Items.LAVA_BUCKET)==1,"Earlier kits keep normal food/building supplies and gain a real lava bucket");
                h.assertTrue(count(all,Items.COBWEB)==(tier.equals("mid") ? 8 : 0),"Diamond progression adds a modest web supply, without giving early kits endgame stock");
                h.assertTrue(p.getInventory().getItem(1).is(tier.equals("mid") ? Items.DIAMOND_AXE : Items.IRON_AXE) && p.getInventory().getItem(4).is(Items.WATER_BUCKET) && p.getInventory().getItem(5).is(Items.LAVA_BUCKET),"Earlier hotbars have actual axe and bucket stacks in deliberate slots");
                if(tier.equals("mid"))h.assertTrue(p.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.CHEST).getEnchantmentLevel(ench.getOrThrow(net.minecraft.world.item.enchantment.Enchantments.PROTECTION))==2 && p.getInventory().getItem(0).getEnchantmentLevel(ench.getOrThrow(net.minecraft.world.item.enchantment.Enchantments.SHARPNESS))==2 && count(all,Items.SPLASH_POTION)==2,"Midgame has real moderate enchants and two healing potions");
                continue;
            }
            h.assertTrue(all.stream().filter(s->s.is(Items.GOLDEN_APPLE)).mapToInt(ItemStack::getCount).sum()==(end ? 256 : 64) && all.stream().filter(s->s.is(Items.ENCHANTED_GOLDEN_APPLE)).mapToInt(ItemStack::getCount).sum()==(end ? 20 : 5),"The actual inventory and Shulker together have exact requested apple totals: "+family+" "+tier);
            h.assertTrue(all.stream().anyMatch(s->s.is(Items.FIREWORK_ROCKET)) && all.stream().filter(s->s.is(Items.FIREWORK_ROCKET)).allMatch(s->s.get(DataComponents.FIREWORKS).flightDuration()==1),"Every carried and boxed rocket uses native Flight 1");
            h.assertTrue(count(all,Items.FIREWORK_ROCKET)==192 && count(all,Items.ARROW)==64,"Late/endgame kits carry three rocket stacks and one shared arrow stack");
            var bow=all.stream().filter(s->s.is(Items.BOW)).findFirst().orElseThrow();
            h.assertTrue(bow.getEnchantmentLevel(ench.getOrThrow(net.minecraft.world.item.enchantment.Enchantments.INFINITY))==1 && bow.getEnchantmentLevel(ench.getOrThrow(net.minecraft.world.item.enchantment.Enchantments.MENDING))==0,"Infinity bows never also carry incompatible Mending");
            if(end) {
                h.assertTrue(count(all,Items.COOKED_BEEF)==0 && count(all,Items.EXPERIENCE_BOTTLE)==128 && count(all,Items.END_CRYSTAL)==128 && count(all,Items.WIND_CHARGE)==128,"Both endgame families have exact XP/crystal/wind supplies and no steak");
                h.assertTrue(count(all,Items.COBBLESTONE)==0 && count(all,Items.OAK_PLANKS)==0 && count(all,Items.OBSIDIAN)==192,"Endgame replaces ordinary blocks but retains all three stacks of crystal bases");
                var main=new ArrayList<ItemStack>();for(int i=0;i<36;i++)main.add(p.getInventory().getItem(i));
                var trident=all.stream().filter(s->s.is(Items.TRIDENT)).findFirst().orElseThrow();
                h.assertTrue(trident.getEnchantmentLevel(ench.getOrThrow(net.minecraft.world.item.enchantment.Enchantments.RIPTIDE))==3 && trident.getEnchantmentLevel(ench.getOrThrow(net.minecraft.world.item.enchantment.Enchantments.CHANNELING))==0 && trident.getEnchantmentLevel(ench.getOrThrow(net.minecraft.world.item.enchantment.Enchantments.LOYALTY))==0,"Riptide III tridents exclude both incompatible throwing enchantments");
                for(String tool:List.of("axe","shovel","hoe")) {
                    var item=family.equals("bunnidog") ? ModItems.ASTRALITE_TOOLS.get(tool).get() : net.minecraft.core.registries.BuiltInRegistries.ITEM.getValue(Identifier.withDefaultNamespace("netherite_"+tool));
                    h.assertTrue(count(main,item)==0 && count(all,item)==1,"Secondary tools are preserved exactly once inside the real Shulker");
                }
                h.assertTrue(count(main,Items.TRIDENT)==0 && count(main,Items.CROSSBOW)==0 && count(all,Items.TRIDENT)==1 && count(all,Items.CROSSBOW)==1 && count(main,Items.OBSIDIAN)==64,"Optional weapons are boxed and crystal bases are directly accessible");
            }
            h.assertTrue(all.stream().filter(ItemStack::isDamageableItem).allMatch(s->s.getEnchantmentLevel(ench.getOrThrow(net.minecraft.world.item.enchantment.Enchantments.UNBREAKING))==3 && (s.is(Items.BOW) || s.getEnchantmentLevel(ench.getOrThrow(net.minecraft.world.item.enchantment.Enchantments.MENDING))==1)),"Every durable carried or boxed late-game item retains Unbreaking III and Mending, except the deliberate Infinity bow");
            h.assertTrue(p.getInventory().getItem(8).is(family.equals("bunnidog") && !end ? ModItems.CATACLYSM_TOTEM.get() : Items.TOTEM_OF_UNDYING),"Prepared hotbars reserve a real emergency totem; endgame starts with the normal type");
            if(!end)h.assertTrue(count(all,Items.COOKED_BEEF)==64 && count(all,Items.EXPERIENCE_BOTTLE)==64,"Late kits retain main food but replace boxed spare steak with repair XP");
        }
        for(String family:List.of("vanilla","bunnidog"))for(String style:List.of("crystal","mace")) {
            com.bunnidogs.mod.command.KitCommands.apply(p,family,"endgame",style);var all=contents(p);boolean mace=style.equals("mace");
            var main=new ArrayList<ItemStack>();for(int i=0;i<36;i++)main.add(p.getInventory().getItem(i));
            h.assertTrue(count(main,Items.DYED_SHULKER_BOX.blue())==1 && count(main,Items.DYED_SHULKER_BOX.red())==1,"Every explicit endgame variant has two separately accessible Equipment/Supplies Shulkers");
            for(var box:main)if(box.is(Items.DYED_SHULKER_BOX.blue()) || box.is(Items.DYED_SHULKER_BOX.red())) {
                String purpose=box.is(Items.DYED_SHULKER_BOX.blue()) ? "equipment" : "supplies";
                h.assertTrue(net.minecraft.network.chat.Component.translatable("container.bunnidogs.kit."+purpose).equals(box.get(DataComponents.CUSTOM_NAME)),"Native box names preserve the player's language");
                var boxed=new ArrayList<ItemStack>();box.get(DataComponents.CONTAINER).nonEmptyItems().forEach(t->boxed.add(t.create()));
                h.assertTrue(boxed.size()==27 && boxed.stream().allMatch(s->s.getCount()<=s.getMaxStackSize()),"Both real 27-slot containers retain valid stock and reserve totems without truncation");
            }
            int normal=count(main,Items.TOTEM_OF_UNDYING),explosive=count(main,ModItems.CATACLYSM_TOTEM.get());
            h.assertTrue(normal+explosive>=19 && p.getOffhandItem().is(Items.TOTEM_OF_UNDYING),"Most main-inventory slots are Totems, with a normal offhand emergency option: "+family+" "+style+" "+normal+"/"+explosive);
            h.assertTrue(family.equals("bunnidog") ? explosive>=5 && normal>explosive : explosive==0,"Modded endgame mixes both Totem types with a normal majority; vanilla uses only vanilla Totems");
            h.assertTrue(count(main,Items.ENDER_PEARL)==16 && count(all,Items.ENDER_PEARL)==128 && count(main,Items.FIREWORK_ROCKET)==64 && count(main,Items.EXPERIENCE_BOTTLE)==64 && count(main,Items.WIND_CHARGE)==64,"One active stack stays accessible while exact refills move to the two boxes");
            h.assertTrue(count(main,Items.BOW)==0 && count(all,Items.BOW)==1 && count(all,Items.SHIELD)==1,"Backup bow/shield are preserved in equipment rather than crowding out Totems");
            if(family.equals("bunnidog"))h.assertTrue(count(all,ModItems.PRISM_CELL.get())==320 && count(all,ModItems.VOLATILE_CHARGE.get())==320 && count(all,ModItems.HEART_SPOOL.get())==64 && count(all,ModItems.GUAVA.get())==64 && count(all,ModItems.FIREBALL_LAUNCHER.get())==1 && count(all,ModItems.CONCORD_STANDARD.get())==1 && count(main,ModItems.PRISM_CELL.get())==64,"Modded equipment/ammo survive packing exactly once, retaining real active lance ammunition");
            h.assertTrue(count(all,Items.GOLDEN_APPLE)==256 && count(all,Items.ENCHANTED_GOLDEN_APPLE)==20 && count(all,Items.EXPERIENCE_BOTTLE)==128 && count(all,Items.FIREWORK_ROCKET)==192 && count(all,Items.WIND_CHARGE)==128,"Both variants conserve shared endgame supplies");
            h.assertTrue(count(all,Items.END_CRYSTAL)==(mace ? 0 : 128) && count(all,Items.OBSIDIAN)==(mace ? 0 : 192) && count(all,Items.RESPAWN_ANCHOR)==(mace ? 0 : 64) && count(all,Items.GLOWSTONE)==(mace ? 0 : 64),"Crystal explosives and their bases are intentionally absent from the mace variant");
            h.assertTrue(count(main,Items.MACE)==(mace ? 1 : 0) && count(all,Items.MACE)==(mace ? 2 : 1),"Crystal boxes its backup mace; mace keeps a primary and a distinct reserve alternative");
            h.assertTrue(p.getInventory().getItem(1).is(mace ? Items.MACE : Items.END_CRYSTAL) && p.getInventory().getItem(2).is(mace ? Items.WIND_CHARGE : Items.OBSIDIAN),"Each variant's main attack resources occupy the correct hotbar slots");
            h.assertTrue(p.getInventory().getItem(5).is(family.equals("bunnidog") ? ModItems.ABYSSAL_FLASK.get() : Items.GOLDEN_APPLE),"Modded sustain is directly accessible instead of buried in the inventory");
            var ench=p.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
            for(var weapon:all)if(weapon.is(Items.MACE)) {
                int density=weapon.getEnchantmentLevel(ench.getOrThrow(net.minecraft.world.item.enchantment.Enchantments.DENSITY));int breach=weapon.getEnchantmentLevel(ench.getOrThrow(net.minecraft.world.item.enchantment.Enchantments.BREACH));
                h.assertTrue((density==5 && breach==0) || (density==0 && breach==4),"Density and Breach are separate compatible native mace stacks");
            }
            h.assertTrue(count(all,Items.COBWEB)==64 && count(all,Items.TRIDENT)==1 && count(all,Items.CROSSBOW)==1,"Organisation preserves exact equipment and variant web stock without duplication");
        }
        p.discard();
    }
    private static int count(List<ItemStack> stacks,Item item) {return stacks.stream().filter(s->s.is(item)).mapToInt(ItemStack::getCount).sum();}
    static void mining(GameTestHelper h) {
        var level=h.getLevel();var center=ApexTypesGameTest.center(h);var origin=BlockPos.containing(center);var blast=ApexTypesGameTest.apex(h,ApexKind.BLAST,false,center);
        var ore=ModBlocks.ASTRALITE_ORE.get().defaultBlockState();var stone=ModBlocks.MOONSTONE.get().defaultBlockState();var cinder=ModBlocks.CINDERITE_ORE.get().defaultBlockState();
        for(var state:List.of(ore,stone,cinder))h.assertTrue(!new ItemStack(Items.NETHERITE_PICKAXE).isCorrectToolForDrops(state) && new ItemStack(ModItems.ASTRALITE_TOOLS.get("pickaxe").get()).isCorrectToolForDrops(state),"All lunar blocks require the actual Astralite tool tier");
        var shell=new ArrayList<BlockPos>();for(int x=-4;x<=4;x++)for(int y=-2;y<=2;y++)for(int z=-4;z<=4;z++)if(Math.max(Math.abs(x),Math.abs(z))>=2 || y<0) {var pos=origin.offset(x,y,z);level.setBlock(pos,(x+z)%3==0 ? ore : (x+z)%3==1 ? cinder : stone,3);shell.add(pos);}
        var protectedItems=new ArrayList<ItemEntity>();for(var item:List.of(ModItems.MOONSTONE.get(),ModItems.ASTRALITE_ORE.get(),ModItems.CINDERITE_ORE.get(),ModItems.ASTRALITE_SHARD.get(),ModItems.RAW_CINDERITE.get(),ModItems.CINDERITE_INGOT.get(),ModItems.ASTRALITE_INGOT.get())) {
            var drop=new ItemEntity(level,center.x(),center.y()+1,center.z(),new ItemStack(item));level.addFreshEntity(drop);protectedItems.add(drop);
            h.assertTrue(!drop.hurtServer(level,level.damageSources().lava(),100) && !drop.hurtServer(level,level.damageSources().explosion(blast,blast),100) && drop.isAlive(),"Actual lunar material entities survive native fire and explosion damage");
        }
        level.explode(null,null,new ExplosionDamageCalculator(),center,4,false,Level.ExplosionInteraction.TNT);
        h.assertTrue(shell.stream().allMatch(p->!level.getBlockState(p).isAir()),"A native TNT-strength explosion cannot mine lunar rock/ores");
        h.assertTrue(blast.skills().blast(level) && shell.stream().filter(p->level.getBlockState(p).isAir()).count()>100,"The actual Blastmaw self-explosion breaks its surrounding lunar shell");
        h.assertTrue(protectedItems.stream().allMatch(ItemEntity::isAlive),"The resulting Blastmaw blast preserves physical ore items");protectedItems.forEach(ItemEntity::discard);blast.discard();
    }
}
