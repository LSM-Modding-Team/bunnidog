package com.bunnidogs.mod.entity;

import java.util.Comparator;
import java.util.List;
import com.bunnidogs.mod.item.EarTagItem;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/** A deliberate identity search through the loaded entity index, never a chunk request or AI command. */
public final class ResidentFinding {
    public static final int RADIUS = 24;
    public static final int OUTLINE_TICKS = 200;
    public static final int COOLDOWN_TICKS = 100;
    private static final int MAX_OUTLINES = 16;
    private ResidentFinding() {}

    static List<Bunnidog> nearby(ServerLevel level, Vec3 center, String code) {
        var matches = level.getEntitiesOfClass(Bunnidog.class,new AABB(center,center).inflate(RADIUS),
                dog -> dog.isAlive() && dog.acceptsCare() && dog.distanceToSqr(center) <= RADIUS * RADIUS
                        && level.getWorldBorder().isWithinBounds(dog.blockPosition())
                        && dog.earTag().filter(code::equals).isPresent());
        matches.sort(Comparator.comparingDouble((Bunnidog dog) -> dog.distanceToSqr(center)).thenComparing(Bunnidog::getUUID));
        return matches;
    }

    public static InteractionResult use(Player player, InteractionHand hand, ItemStack tag) {
        if (!player.mayBuild()) return InteractionResult.PASS;
        if (!(player.level() instanceof ServerLevel level)) return InteractionResult.SUCCESS;
        var calipers = player.getItemInHand(hand);
        if (player.getCooldowns().isOnCooldown(calipers)) return InteractionResult.SUCCESS;
        var code = EarTagItem.finderCode(tag);
        if (code.isEmpty()) {
            player.sendOverlayMessage(Component.translatable("message.bunnidogs.finder.name"));
            return InteractionResult.SUCCESS;
        }
        var matches = nearby(level,player.position(),code.get());
        // Even an empty search rests. Native effect merging preserves stronger/longer existing effects and mod hooks.
        player.getCooldowns().addCooldown(calipers,COOLDOWN_TICKS);
        if (matches.isEmpty()) {
            player.sendSystemMessage(Component.translatable("message.bunnidogs.finder.none",code.get(),RADIUS));
        } else {
            for (var dog : matches.subList(0,Math.min(MAX_OUTLINES,matches.size())))
                dog.addEffect(new MobEffectInstance(MobEffects.GLOWING,OUTLINE_TICKS,0,false,false,true),player);
            var nearest = matches.getFirst();
            var pos = nearest.blockPosition();
            player.sendSystemMessage(Component.translatable("message.bunnidogs.finder.found",code.get(),matches.size(),
                    pos.getX(),pos.getY(),pos.getZ()));
        }
        level.playSound(null,player.blockPosition(),SoundEvents.AMETHYST_BLOCK_CHIME,SoundSource.PLAYERS,.4F,1.4F);
        return InteractionResult.SUCCESS;
    }
}
