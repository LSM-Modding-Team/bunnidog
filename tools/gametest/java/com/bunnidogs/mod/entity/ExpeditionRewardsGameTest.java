package com.bunnidogs.mod.entity;

import com.bunnidogs.mod.registry.*;
import com.bunnidogs.mod.expedition.*;
import com.bunnidogs.mod.genetics.*;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.BundleContents;
import net.minecraft.world.level.block.Blocks;

final class ExpeditionRewardsGameTest {
    static void armor(GameTestHelper h) {
        var level=h.getLevel();var p=ApexTypesGameTest.player(h,ApexTypesGameTest.center(h));var caster=ApexTypesGameTest.apex(h,ApexKind.SUPPORT,false,p.position().add(8,0,0));
        p.setItemSlot(EquipmentSlot.HEAD,new ItemStack(ModItems.HEARTBOUND_ARMOR.get("helmet").get()));ExpeditionGear.armor(p);
        h.assertTrue(p.getMaxHealth()==23,"One actual equipped upgrade supplies three permanent max HP");
        var marks=LifebinderMarks.get(level);long now=level.getGameTime();marks.mark(caster,p);
        h.assertTrue(marks.lost(p.getUUID())==1 && p.getMaxHealth()==21,"Protected hooks still remove a real container before it recovers");
        h.runAfterDelay(20,()->{
            marks.mark(caster,p);marks.expire(p,now+1200);
            h.assertTrue(marks.lost(p.getUUID())==1,"The first hit expires independently while the later hook remains");
            marks.expire(p,now+1220);h.assertTrue(marks.lost(p.getUUID())==0 && p.getMaxHealth()==23,"The second protected hook recovers at its own deadline");
            p.setItemSlot(EquipmentSlot.CHEST,new ItemStack(ModItems.HEARTBOUND_ARMOR.get("chestplate").get()));p.setItemSlot(EquipmentSlot.LEGS,new ItemStack(ModItems.HEARTBOUND_ARMOR.get("leggings").get()));p.setItemSlot(EquipmentSlot.FEET,new ItemStack(ModItems.HEARTBOUND_ARMOR.get("boots").get()));ExpeditionGear.armor(p);
            h.assertTrue(p.getMaxHealth()==32,"Four upgraded armor pieces provide six extra containers total");marks.mark(caster,p);long second=level.getGameTime();
            marks.expire(p,second+299);h.assertTrue(marks.lost(p.getUUID())==1,"A four-piece mark lasts the full fifteen seconds");marks.expire(p,second+300);h.assertTrue(marks.lost(p.getUUID())==0,"Additional pieces reduce recovery by fifteen seconds each");
            marks.clear(p);caster.discard();p.discard();h.succeed();
        });
    }
    static void smith(GameTestHelper h) {
        var level=h.getLevel();
        for(var part:new String[]{"helmet","chestplate","leggings","boots"}) {
            var key=net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.RECIPE,net.minecraft.resources.Identifier.fromNamespaceAndPath("bunnidogs","heartbound_"+part));
            var recipe=(net.minecraft.world.item.crafting.SmithingTransformRecipe)level.getServer().getRecipeManager().byKey(key).orElseThrow().value();
            var item=net.minecraft.core.registries.BuiltInRegistries.ITEM.getValue(net.minecraft.resources.Identifier.withDefaultNamespace("netherite_"+part));var input=new ItemStack(item);input.setDamageValue(27);input.set(DataComponents.CUSTOM_NAME,net.minecraft.network.chat.Component.literal("Keep my gear"));
            var enchants=new net.minecraft.world.item.enchantment.ItemEnchantments.Mutable(net.minecraft.world.item.enchantment.ItemEnchantments.EMPTY);enchants.set(level.registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT).getOrThrow(net.minecraft.world.item.enchantment.Enchantments.PROTECTION),4);input.set(DataComponents.ENCHANTMENTS,enchants.toImmutable());
            var output=recipe.assemble(new net.minecraft.world.item.crafting.SmithingRecipeInput(ItemStack.EMPTY,input,new ItemStack(ModItems.VITALITY_ARTIFACT.get())));
            h.assertTrue(output.is(ModItems.HEARTBOUND_ARMOR.get(part).get()) && output.getDamageValue()==27 && output.getHoverName().equals(input.getHoverName()) && output.get(DataComponents.ENCHANTMENTS).equals(input.get(DataComponents.ENCHANTMENTS)),"Actual smithing preserves Protection IV, wear and custom identity on "+part);
        }
        var key=net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.RECIPE,net.minecraft.resources.Identifier.fromNamespaceAndPath("bunnidogs","apex_wing_harness"));
        var recipe=(net.minecraft.world.item.crafting.SmithingTransformRecipe)level.getServer().getRecipeManager().byKey(key).orElseThrow().value();var wings=new ItemStack(Items.ELYTRA);wings.setDamageValue(31);
        var upgraded=recipe.assemble(new net.minecraft.world.item.crafting.SmithingRecipeInput(ItemStack.EMPTY,wings,new ItemStack(ModItems.AERIAL_ARTIFACT.get())));
        h.assertTrue(upgraded.has(DataComponents.GLIDER) && upgraded.getDamageValue()==31 && upgraded.get(DataComponents.ATTRIBUTE_MODIFIERS).modifiers().stream().anyMatch(m->m.attribute().equals(Attributes.ARMOR) && m.modifier().amount()==8),"Elytra upgrades retain wear, actual native gliding and chestplate-grade armor");
        h.assertTrue(level.registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT).getOrThrow(net.minecraft.world.item.enchantment.Enchantments.PROTECTION).value().isSupportedItem(upgraded),"Armored Elytra supports real Protection enchantments as well as durability enchantments");
    }
    static void vacuum(GameTestHelper h) {
        var level=h.getLevel();ApexTypesGameTest.floor(h);var p=ApexTypesGameTest.player(h,ApexTypesGameTest.center(h));p.setYRot(-90);p.setXRot(0);
        var caller=ApexTypesGameTest.apex(h,ApexKind.BROOD,true,p.position().add(0,0,12));caller.setTarget(p);
        var dog=ModEntities.APEX_MINION.get().create(level,EntitySpawnReason.TRIGGERED);dog.caller(caller);dog.setPos(p.position().add(5,1,0));dog.setNoAi(true);level.addFreshEntity(dog);var genome=dog.genome();var stats=dog.stats();
        var vacuum=new ItemStack(ModItems.GENETIC_VACUUM.get());p.setItemInHand(InteractionHand.MAIN_HAND,vacuum);p.setItemInHand(InteractionHand.OFF_HAND,new ItemStack(ModItems.PRISM_CELL.get(),2));
        var ordinary=ModEntities.BUNNIDOG.get().create(level,EntitySpawnReason.TRIGGERED);ordinary.setGenome(Genome.DEFAULT);ordinary.setAge(0);
        h.assertTrue(ordinary.mobInteract(p,InteractionHand.MAIN_HAND)==net.minecraft.world.InteractionResult.PASS && p.getMainHandItem()==vacuum && ordinary.carriedBundleCount()==0,"Aiming the vacuum at a normal bunnidog must use the weapon, never equip it as a cargo bundle");ordinary.discard();
        ModItems.GENETIC_VACUUM.get().use(level,p,InteractionHand.MAIN_HAND);ModItems.GENETIC_VACUUM.get().onUseTick(level,p,vacuum,16);
        h.assertTrue(dog.getDeltaMovement().x<0,"A real suction pulse pulls an elite spawn toward the keeper");ModItems.GENETIC_VACUUM.get().finishUsingItem(vacuum,level,p);
        h.assertTrue(dog.isRemoved() && p.getOffhandItem().getCount()==1,"Capturing a live hostile brood spends one cell without a death drop");
        var contents=vacuum.get(DataComponents.BUNDLE_CONTENTS);var seedItem=contents.items().iterator().next().create();var seed=com.bunnidogs.mod.item.SeedItem.read(seedItem).orElseThrow();
        var saved=Seed.parse(seed.toString()).orElseThrow();
        var cross=new Cross(saved);h.assertTrue(cross.litter()==1 && cross.looks(Gene.BUILD).size()==1 && new CapabilityForecast(cross).outcomes().size()==1,"Existing body and mind instruments forecast the exact captured pup rather than a Mendelian cross");
        var pups=saved.hatch(99,0,new java.util.Random(2));
        h.assertTrue(pups.size()==1 && pups.getFirst().genome().equals(genome) && pups.getFirst().stats().equals(stats),"Captured seeds preserve exact elite genes and points without litter multiplication or drift");
        var frozen=Infusion.roll(saved,new java.util.Random(3));h.assertTrue(frozen.genome().equals(genome) && frozen.stats().equals(stats),"Infusing a captured seed preserves the same frozen genetic record");caller.discard();p.discard();
    }
    static void flask(GameTestHelper h) {
        var level=h.getLevel();var p=ApexTypesGameTest.player(h,ApexTypesGameTest.center(h));var flask=new ItemStack(ModItems.ABYSSAL_FLASK.get());p.setItemInHand(InteractionHand.MAIN_HAND,flask);
        var slot=new net.minecraft.world.inventory.Slot(p.getInventory(),0,0,0);var cursor=SlotAccess.of(()->p.getInventory().getItem(1),itemStack->p.getInventory().setItem(1,itemStack));
        var potion=new ItemStack(Items.POTION);potion.set(DataComponents.POTION_CONTENTS,new net.minecraft.world.item.alchemy.PotionContents(net.minecraft.world.item.alchemy.Potions.STRONG_HEALING));potion.set(DataComponents.CUSTOM_NAME,net.minecraft.network.chat.Component.literal("Exact potion"));
        var juice=com.bunnidogs.mod.item.JuiceItem.create(new Infusion(Genome.DEFAULT,Stats.of(4,3,2,1,0),4));var exact=juice.copy();var item=ModItems.ABYSSAL_FLASK.get();
        item.overrideOtherStackedOnMe(flask,potion,slot,net.minecraft.world.inventory.ClickAction.PRIMARY,p,cursor);item.overrideOtherStackedOnMe(flask,juice,slot,net.minecraft.world.inventory.ClickAction.PRIMARY,p,cursor);
        h.assertTrue(potion.isEmpty() && juice.isEmpty() && flask.get(ModItems.FLASK_COUNT.get())==2,"Depositing drinks transfers real items into the flask");
        item.overrideOtherStackedOnMe(flask,ItemStack.EMPTY,slot,net.minecraft.world.inventory.ClickAction.SECONDARY,p,cursor);h.assertTrue(ItemStack.matches(p.getInventory().getItem(1),exact),"The newest drink comes back with exact infusion components");
        item.overrideOtherStackedOnMe(flask,p.getInventory().getItem(1),slot,net.minecraft.world.inventory.ClickAction.PRIMARY,p,cursor);
        var id=flask.get(ModItems.FLASK_ID.get());var ops=level.registryAccess().createSerializationContext(com.mojang.serialization.JsonOps.INSTANCE);var encoded=FlaskStorage.CODEC.encodeStart(ops,FlaskStorage.get(level)).getOrThrow();var copied=FlaskStorage.CODEC.parse(ops,encoded).getOrThrow();
        h.assertTrue(copied.size(id)==2 && ItemStack.matches(copied.top(id),exact),"Native registry-aware storage codec preserves order and full drink components");
        item.use(level,p,InteractionHand.MAIN_HAND);item.finishUsingItem(flask,level,p);p.stopUsingItem();h.assertTrue(flask.get(ModItems.FLASK_COUNT.get())==1,"One sip consumes exactly the top drink");
        p.setHealth(5);item.use(level,p,InteractionHand.MAIN_HAND);item.finishUsingItem(flask,level,p);p.stopUsingItem();
        h.assertTrue(p.getHealth()>5 && flask.get(ModItems.FLASK_COUNT.get())==0,"The next sip applies the actual stored healing potion");
        for(int i=0;i<300;i++)FlaskStorage.get(level).push(id,exact);item.refresh(flask,level);
        h.assertTrue(flask.get(ModItems.FLASK_COUNT.get())==300 && flask.get(ModItems.FLASK_TOP.get()).count()==1,"Storage exceeds ordinary container limits while the synced item retains only one read-only top preview");p.discard();
    }
    static void launcher(GameTestHelper h) {
        var level=h.getLevel();var p=ApexTypesGameTest.player(h,ApexTypesGameTest.center(h));var weapon=new ItemStack(ModItems.FIREBALL_LAUNCHER.get());p.setItemInHand(InteractionHand.MAIN_HAND,weapon);p.setItemInHand(InteractionHand.OFF_HAND,new ItemStack(ModItems.VOLATILE_CHARGE.get(),2));
        var item=ModItems.FIREBALL_LAUNCHER.get();h.assertTrue(item.use(level,p,InteractionHand.MAIN_HAND)==net.minecraft.world.InteractionResult.SUCCESS,"A paid launcher produces a real projectile");
        var shots=level.getEntitiesOfClass(net.minecraft.world.entity.projectile.hurtingprojectile.LargeFireball.class,p.getBoundingBox().inflate(4));
        h.assertTrue(shots.size()==1 && shots.getFirst().getOwner()==p && p.getOffhandItem().getCount()==1 && weapon.getDamageValue()==1,"The fireball retains player ownership and exact ammo/wear costs");
        h.assertTrue(item.use(level,p,InteractionHand.MAIN_HAND)==net.minecraft.world.InteractionResult.FAIL,"The launcher respects its repeat delay");shots.forEach(Entity::discard);p.discard();
    }
    static void totem(GameTestHelper h) {
        var level=h.getLevel();var pos=ApexTypesGameTest.center(h);var p=ApexTypesGameTest.player(h,pos);p.setItemInHand(InteractionHand.OFF_HAND,new ItemStack(ModItems.CATACLYSM_TOTEM.get()));
        var mob=net.minecraft.world.entity.EntityTypes.ZOMBIE.create(level,EntitySpawnReason.TRIGGERED);mob.setPos(pos.add(5,0,0));mob.getAttribute(Attributes.MAX_HEALTH).setBaseValue(4096);mob.setHealth(4096);level.addFreshEntity(mob);
        var block=net.minecraft.core.BlockPos.containing(pos).east(2);level.setBlock(block,Blocks.STONE.defaultBlockState(),3);
        p.hurtServer(level,level.damageSources().generic(),10000);ApexPlayerEvents.tick(new net.neoforged.neoforge.event.tick.PlayerTickEvent.Post(p));
        h.runAfterDelay(5,()->{
            h.assertTrue(p.isAlive() && p.getOffhandItem().isEmpty() && mob.getHealth()<4096 && mob.hasEffect(ModEffects.RESONANT_STUN),"A real successful upgraded totem pop spares its owner, damages the swarm and applies stun: alive="+p.isAlive()+", offhand="+p.getOffhandItem()+", health="+mob.getHealth()+", stun="+mob.hasEffect(ModEffects.RESONANT_STUN));
            h.assertTrue(level.getBlockState(block).isAir(),"The power-twenty rescue is genuinely dangerous to nearby construction");
            h.runAfterDelay(103,()->{h.assertTrue(!mob.hasEffect(ModEffects.RESONANT_STUN) && !mob.isNoAi(),"The stun expires and releases the mob's original AI");mob.discard();p.discard();h.succeed();});
        });
    }
}
