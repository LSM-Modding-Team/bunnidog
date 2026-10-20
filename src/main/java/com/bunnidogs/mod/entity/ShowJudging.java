package com.bunnidogs.mod.entity;

import java.util.Comparator;
import java.util.Optional;
import com.bunnidogs.mod.genetics.Pedigree;
import com.bunnidogs.mod.genetics.Purebreds;
import com.bunnidogs.mod.genetics.Sampling;
import com.bunnidogs.mod.genetics.Show;
import com.bunnidogs.mod.item.ShowRibbonItem;
import com.bunnidogs.mod.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

/** One loaded entrant and the same eligibility rules for the scorecard and actual award. */
public final class ShowJudging {
    private ShowJudging() {}
    public enum Status { LEAD, PARENTS, MIXED, HEALTH, STATS, ALREADY, READY }
    public record Reading(Status status, Optional<Bunnidog> candidate, Optional<Pedigree.Entry> entry, Show.Tier tier) {}

    public static Reading inspect(Level level,BlockPos pos,Player player) {
        var candidate=level.getEntitiesOfClass(Bunnidog.class,new AABB(pos).inflate(Show.RANGE),
                dog->dog.isAlive() && dog.acceptsCare() && !dog.isBaby() && !dog.isPassenger()
                        && dog.isLeashed() && dog.getLeashHolder()==player
                        && dog.distanceToSqr(pos.getX()+.5,pos.getY()+1,pos.getZ()+.5)<=Show.RANGE*Show.RANGE)
                .stream().min(Comparator.comparingDouble((Bunnidog dog)->dog.distanceToSqr(pos.getX()+.5,pos.getY()+1,pos.getZ()+.5))
                        .thenComparing(Bunnidog::getUUID));
        if (candidate.isEmpty()) return new Reading(Status.LEAD,Optional.empty(),Optional.empty(),Show.Tier.NONE);
        var dog=candidate.orElseThrow();
        var entry=new Pedigree.Entry(dog.genome(),dog.motherGenome(),dog.fatherGenome(),Optional.of(dog.getUUID()),
                dog.earTag(),Optional.of(dog.stats()),false);
        var eligibility=Purebreds.eligibility(entry);
        var tier=Show.rank(dog.stats());
        var status=eligibility==Purebreds.Result.PARENTS ? Status.PARENTS : eligibility==Purebreds.Result.MIXED ? Status.MIXED
                : !Sampling.healthy(dog.getHealth(),dog.getMaxHealth()) ? Status.HEALTH : tier==Show.Tier.NONE ? Status.STATS
                : dog.highestShowAward()>=tier.ordinal() ? Status.ALREADY : Status.READY;
        return new Reading(status,candidate,Optional.of(entry),tier);
    }

    /** Commit the monotonic award before inventory/advancement callbacks can reenter. */
    public static boolean award(Level level,BlockPos pos,Player player) {
        if (level.isClientSide() || player.level()!=level || !player.isAlive() || player.isSpectator() || !player.mayBuild()
                || !level.hasChunkAt(pos) || !level.getBlockState(pos).is(ModBlocks.JUDGING_STAND.get())
                || !level.mayInteract(player,pos) || player.distanceToSqr(pos.getX()+.5,pos.getY()+.5,pos.getZ()+.5)>36) return false;
        var reading=inspect(level,pos,player);
        if (reading.status()!=Status.READY) {
            player.sendOverlayMessage(Component.translatable("message.bunnidogs.show."+reading.status().name().toLowerCase(java.util.Locale.ROOT)));
            return false;
        }
        var dog=reading.candidate().orElseThrow(); var tier=reading.tier();
        if (!dog.recordShowAward(tier.ordinal())) return false;
        var ribbon=ShowRibbonItem.create(tier,reading.entry().orElseThrow());
        if (!player.getInventory().add(ribbon)) player.drop(ribbon,false);
        player.sendOverlayMessage(Component.translatable("message.bunnidogs.show.awarded",Component.translatable("item.bunnidogs."+tier.key()+"_show_ribbon")));
        level.playSound(null,pos,SoundEvents.WOODEN_BUTTON_CLICK_ON,SoundSource.BLOCKS,.4F,1.4F);
        if (tier==Show.Tier.GOLD && player instanceof ServerPlayer server) {
            var goal=server.level().getServer().getAdvancements().get(Identifier.fromNamespaceAndPath("bunnidogs","champion_line"));
            if (goal!=null) server.getAdvancements().award(goal,"judged");
        }
        return true;
    }
}
