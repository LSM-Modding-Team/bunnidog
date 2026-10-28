package com.bunnidogs.mod.guild;
import com.bunnidogs.mod.registry.ModEntities;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.InteractionResult;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.phys.Vec3;
public final class WagonItem extends Item {
    public WagonItem(Properties p){super(p);}
    @Override public InteractionResult useOn(UseOnContext c) {
        if(!(c.getLevel() instanceof ServerLevel server) || c.getPlayer()==null)return InteractionResult.SUCCESS;
        var p=c.getPlayer();var at=c.getClickedPos().relative(c.getClickedFace());if(!server.mayInteract(p,at) || !p.mayUseItemAt(at,c.getClickedFace(),c.getItemInHand()))return InteractionResult.FAIL;
        var wagon=ModEntities.KENNEL_WAGON.get().create(server,EntitySpawnReason.TRIGGERED);if(wagon==null)return InteractionResult.FAIL;
        wagon.owner(p.getUUID());wagon.snapTo(Vec3.atBottomCenterOf(at));
        if(!server.noCollision(wagon) || server.containsAnyLiquid(wagon.getBoundingBox()) || !server.addFreshEntity(wagon)) {wagon.discard();return InteractionResult.FAIL;}
        c.getItemInHand().consume(1,p);return InteractionResult.SUCCESS;
    }
}
