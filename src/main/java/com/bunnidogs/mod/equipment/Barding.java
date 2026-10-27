package com.bunnidogs.mod.equipment;

import com.bunnidogs.mod.BunnidogsMod;
import com.bunnidogs.mod.entity.*;
import com.bunnidogs.mod.registry.ModItems;
import java.util.*;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.*;

/** Derived named modifiers cache real equipment; the saved BODY stack owns every benefit. */
@EventBusSubscriber(modid=BunnidogsMod.MODID)
public final class Barding {
    public static final String[] NAMES={"leather","iron","diamond","netherite","astralite"};
    public static final int[] DURABILITY={320,800,1600,2400,7200};
    private static final double[][] VALUES={{2,0,.10,.05,0,0},{6,1,.25,.15,0,.05},{10,2,.50,.30,.02,.10},{12,3,.75,.45,.05,.15},{14,5,1,.60,.08,.20}};
    public static double[] bonuses(int tier){return VALUES[tier].clone();}
    private static final Set<UUID> BUSY=new HashSet<>();
    public static int tier(Bunnidog dog){return dog.getItemBySlot(EquipmentSlot.BODY).getItem() instanceof BardingItem item ? item.tier : -1;}
    public static double damage(Bunnidog dog){int t=tier(dog);return t<0 ? 1 : 1+VALUES[t][3];}
    public static double speed(Bunnidog dog){int t=tier(dog);return t<0 ? 1 : 1+VALUES[t][4];}
    public static void refresh(Bunnidog dog) {
        if(!(dog.level() instanceof ServerLevel))return;
        int t=tier(dog);float hp=dog.getHealth();
        var attributes=List.of(Attributes.ARMOR,Attributes.ARMOR_TOUGHNESS,Attributes.MAX_HEALTH,Attributes.ATTACK_DAMAGE,Attributes.MOVEMENT_SPEED,Attributes.KNOCKBACK_RESISTANCE);
        for(int i=0;i<attributes.size();i++) {
            var attribute=dog.getAttribute(attributes.get(i));if(attribute==null)continue;
            var id=Identifier.fromNamespaceAndPath("bunnidogs","barding_"+i);double value=t<0 ? 0 : VALUES[t][i];
            var old=attribute.getModifier(id);
            if(value==0) {if(old!=null)attribute.removeModifier(id);}
            else if(old==null || old.amount()!=value) {
                attribute.removeModifier(id);
                // Saved derived values keep native load-time health from being clamped before BODY restores.
                attribute.addPermanentModifier(new AttributeModifier(id,value,i>=2 && i<=4 ? AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL : AttributeModifier.Operation.ADD_VALUE));
            }
        }
        dog.setHealth(Math.min(hp,dog.getMaxHealth()));
    }
    public static boolean editable(Player p,Bunnidog dog) {
        return p.mayBuild() && p.level()==dog.level() && p.distanceToSqr(dog)<=36 && dog.isAlive() && dog.acceptsCare() && !dog.isBaby()
            && dog.trail().belongsTo(p) && dog.getTarget()==null && dog.hurtTime==0 && !dog.isOnFire() && !dog.combat().danger() && !dog.isInLove() && !dog.beingTransported() && !dog.combat().recovering();
    }
    public static boolean equip(Player p,Bunnidog dog,ItemStack offered) {
        if(!(dog.level() instanceof ServerLevel))return true;
        if(!editable(p,dog) || dog.combat().downed() || !(offered.getItem() instanceof BardingItem) || !BUSY.add(dog.getUUID()))return false;
        try {
            var old=dog.getItemBySlot(EquipmentSlot.BODY);if(!old.isEmpty() && !(old.getItem() instanceof BardingItem))return false;
            var next=offered.copyWithCount(1);offered.consume(1,p);dog.setItemSlot(EquipmentSlot.BODY,next);dog.setDropChance(EquipmentSlot.BODY,2);refresh(dog);
            if(!old.isEmpty() && !p.getInventory().add(old))p.drop(old,false);
            dog.playSound(net.minecraft.sounds.SoundEvents.ARMOR_EQUIP_IRON.value(),.8F,1);return true;
        }finally{BUSY.remove(dog.getUUID());}
    }
    public static boolean remove(Player p,Bunnidog dog) {
        if(!(dog.level() instanceof ServerLevel) || !editable(p,dog) || tier(dog)<0 || !BUSY.add(dog.getUUID()))return false;
        try {var old=dog.getItemBySlot(EquipmentSlot.BODY);dog.setItemSlot(EquipmentSlot.BODY,ItemStack.EMPTY);refresh(dog);if(!p.getInventory().add(old))p.drop(old,false);return true;}finally{BUSY.remove(dog.getUUID());}
    }
    public static void astralite(Apex dog) {dog.setItemSlot(EquipmentSlot.BODY,new ItemStack(ModItems.BARDING.get("astralite").get()));dog.setDropChance(EquipmentSlot.BODY,.25F);refresh(dog);}
    @SubscribeEvent public static void damage(LivingDamageEvent.Post event) {
        if(event.getEntity() instanceof Bunnidog dog && dog.level() instanceof ServerLevel && tier(dog)>=0 && event.getHealthDamage()>0) {
            if(!event.getSource().is(net.minecraft.tags.DamageTypeTags.BYPASSES_ARMOR))dog.getItemBySlot(EquipmentSlot.BODY).hurtAndBreak(Math.max(1,(int)(event.getHealthDamage()/4)),dog,EquipmentSlot.BODY);
            refresh(dog);
        }
    }
    private Barding(){}
}
