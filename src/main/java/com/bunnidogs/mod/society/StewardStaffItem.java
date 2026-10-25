package com.bunnidogs.mod.society;

import com.bunnidogs.mod.entity.Bunnidog;
import com.bunnidogs.mod.registry.ModItems;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;

/** Explicit permissions: bind a hearth, invite animals, link work sites and survey/authorize a small plot. */
public final class StewardStaffItem extends Item {
    public StewardStaffItem(Properties p) {super(p);}
    public static HearthTreeBlockEntity hearth(ItemStack stack,Player p) {
        var at=stack.get(ModItems.HEARTH_LINK.get());
        return at!=null && p.level() instanceof ServerLevel l && at.dimension().equals(l.dimension()) && l.hasChunkAt(at.pos()) && l.getBlockEntity(at.pos()) instanceof HearthTreeBlockEntity h && h.permitted(p) ? h : null;
    }
    @Override public InteractionResult useOn(UseOnContext c) {
        var p=c.getPlayer();if(p==null || !p.mayBuild() || !c.getLevel().mayInteract(p,c.getClickedPos()))return InteractionResult.FAIL;
        if(c.getLevel().isClientSide())return InteractionResult.SUCCESS;
        if(c.getLevel().getBlockEntity(c.getClickedPos()) instanceof HearthTreeBlockEntity h && h.permitted(p)) {
            if(p.isShiftKeyDown())h.survey(p);else {c.getItemInHand().set(ModItems.HEARTH_LINK.get(),GlobalPos.of(p.level().dimension(),c.getClickedPos()));p.sendOverlayMessage(Component.translatable("message.bunnidogs.hearth.selected"));}
            return InteractionResult.SUCCESS;
        }
        var h=hearth(c.getItemInHand(),p);if(h==null) {p.sendOverlayMessage(Component.translatable("message.bunnidogs.hearth.select"));return InteractionResult.FAIL;}
        boolean linked=h.link(c.getClickedPos());p.sendOverlayMessage(Component.translatable("message.bunnidogs.hearth."+(linked ? "linked" : "site_refused")));return linked ? InteractionResult.SUCCESS : InteractionResult.FAIL;
    }
    @Override public InteractionResult interactLivingEntity(ItemStack stack,Player p,LivingEntity target,InteractionHand hand) {
        if(!(target instanceof Bunnidog dog) || !dog.acceptsCare() || p.isSpectator() || !p.mayBuild() || p.distanceToSqr(dog)>36)return InteractionResult.PASS;
        if(p.level().isClientSide())return InteractionResult.SUCCESS;
        var h=hearth(stack,p);boolean done=h!=null && dog.society().invite(p,h,p.isShiftKeyDown());
        p.sendOverlayMessage(Component.translatable("message.bunnidogs.hearth."+(done ? p.isShiftKeyDown() ? "left" : "joined" : "invite_refused")));return InteractionResult.SUCCESS;
    }
}
