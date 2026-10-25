package com.bunnidogs.mod.entity;

import com.bunnidogs.mod.registry.*;
import com.bunnidogs.mod.expedition.PowerModes;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.*;
import net.minecraft.core.component.DataComponents;

final class EquipmentModesGameTest {
    static void weapons(GameTestHelper h) {
        ApexTypesGameTest.floor(h);var level=h.getLevel();var p=ApexTypesGameTest.player(h,ApexTypesGameTest.center(h));p.setYRot(-90);p.setXRot(0);
        var target=net.minecraft.world.entity.EntityTypes.COW.create(level,EntitySpawnReason.TRIGGERED);target.setNoAi(true);target.getAttribute(Attributes.MAX_HEALTH).setBaseValue(1024);target.setHealth(1024);target.snapTo(p.position().add(6,0,0));level.addFreshEntity(target);
        for(int power=1;power<=5;power++) {
            p.getInventory().clearContent();var weapon=new ItemStack(ModItems.SOVEREIGN_LANCE.get());weapon.set(ModItems.TOOL_POWER.get(),power);p.setItemInHand(InteractionHand.MAIN_HAND,weapon);
            p.setItemInHand(InteractionHand.OFF_HAND,new ItemStack(ModItems.PRISM_CELL.get(),1));if(power>1)p.getInventory().setItem(9,new ItemStack(ModItems.PRISM_CELL.get(),power-1));
            for(int i=0;i<30;i++)p.getCooldowns().tick();target.invulnerableTime=0;float hp=target.getHealth();
            ModItems.SOVEREIGN_LANCE.get().use(level,p,InteractionHand.MAIN_HAND);ModItems.SOVEREIGN_LANCE.get().finishUsingItem(weapon,level,p);p.stopUsingItem();
            h.assertTrue(Math.abs(hp-target.getHealth()-45*power)<.01 && weapon.getDamageValue()==power && !PowerModes.available(p,ModItems.PRISM_CELL.get(),1),"Each actual Sovereign shot pays split-stack ammo/wear and deals its selected linear damage: "+power);
        }
        target.discard();p.getInventory().clearContent();var launcher=new ItemStack(ModItems.FIREBALL_LAUNCHER.get());launcher.set(ModItems.TOOL_POWER.get(),5);p.setItemInHand(InteractionHand.MAIN_HAND,launcher);p.setItemInHand(InteractionHand.OFF_HAND,new ItemStack(ModItems.VOLATILE_CHARGE.get(),2));p.getInventory().setItem(9,new ItemStack(ModItems.VOLATILE_CHARGE.get(),3));
        ModItems.FIREBALL_LAUNCHER.get().use(level,p,InteractionHand.MAIN_HAND);
        var ball=level.getEntitiesOfClass(net.minecraft.world.entity.projectile.hurtingprojectile.LargeFireball.class,p.getBoundingBox().inflate(4),e->e.getOwner()==p).getFirst();
        var out=net.minecraft.world.level.storage.TagValueOutput.createWithContext(net.minecraft.util.ProblemReporter.DISCARDING,level.registryAccess());ball.saveWithoutId(out);
        h.assertTrue(out.buildResult().getByteOr("ExplosionPower",(byte)0)==20 && launcher.getDamageValue()==5 && !PowerModes.available(p,ModItems.VOLATILE_CHARGE.get(),1),"Power-five launcher creates a real power-20 fireball and spends five fragmented charges");ball.discard();
        p.setShiftKeyDown(true);ModItems.FIREBALL_LAUNCHER.get().use(level,p,InteractionHand.MAIN_HAND);
        h.assertTrue(PowerModes.get(launcher)==1 && launcher.getDamageValue()==5,"Sneak-use wraps power without firing or consuming resources, even during recovery");p.setShiftKeyDown(false);p.discard();
    }
    static void support(GameTestHelper h) {
        var level=h.getLevel();var p=ApexTypesGameTest.player(h,ApexTypesGameTest.center(h));p.getAttribute(Attributes.MAX_HEALTH).setBaseValue(100);
        for(int power=1;power<=5;power++) {
            p.getInventory().clearContent();p.removeAllEffects();p.setHealth(1);var standard=new ItemStack(ModItems.CONCORD_STANDARD.get());standard.set(ModItems.TOOL_POWER.get(),power);p.setItemInHand(InteractionHand.MAIN_HAND,standard);p.setItemInHand(InteractionHand.OFF_HAND,new ItemStack(ModItems.HEART_SPOOL.get(),power));for(int i=0;i<110;i++)p.getCooldowns().tick();
            ModItems.CONCORD_STANDARD.get().use(level,p,InteractionHand.MAIN_HAND);ModItems.CONCORD_STANDARD.get().finishUsingItem(standard,level,p);p.stopUsingItem();
            var effect=p.getEffect(net.minecraft.world.effect.MobEffects.RESISTANCE);
            h.assertTrue(p.getHealth()==1+10*power && effect.getAmplifier()==power-1 && effect.getDuration()==80 && p.getOffhandItem().isEmpty(),"Standard power scales actual hearts, Resistance and paid spools: "+power);
        }
        var vacuum=new ItemStack(ModItems.GENETIC_VACUUM.get());p.setItemInHand(InteractionHand.MAIN_HAND,vacuum);var item=ModItems.GENETIC_VACUUM.get();var slot=new net.minecraft.world.inventory.Slot(p.getInventory(),0,0,0);var cursor=SlotAccess.of(()->p.getInventory().getItem(1),s->p.getInventory().setItem(1,s));var diamonds=new ItemStack(Items.DIAMOND,5);
        item.overrideOtherStackedOnMe(vacuum,diamonds,slot,net.minecraft.world.inventory.ClickAction.PRIMARY,p,cursor);
        h.assertTrue(diamonds.getCount()==5 && vacuum.get(DataComponents.BUNDLE_CONTENTS).isEmpty(),"Gene Siphon rejects non-seed inventory contents without consuming them");
        var seed=com.bunnidogs.mod.item.SeedItem.create(com.bunnidogs.mod.genetics.Seed.captured(com.bunnidogs.mod.genetics.Genome.DEFAULT,com.bunnidogs.mod.genetics.Stats.ZERO));item.overrideOtherStackedOnMe(vacuum,seed,slot,net.minecraft.world.inventory.ClickAction.PRIMARY,p,cursor);
        h.assertTrue(!vacuum.get(DataComponents.BUNDLE_CONTENTS).isEmpty(),"The same native bundle click still accepts a real Bunnidog Seed");p.discard();
    }
}
