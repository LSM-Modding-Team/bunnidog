package com.bunnidogs.mod.entity;

import com.bunnidogs.mod.block.NestBlock;
import com.bunnidogs.mod.item.TravelBasketItem;
import com.bunnidogs.mod.registry.ModBlocks;
import com.bunnidogs.mod.registry.ModItems;
import com.bunnidogs.mod.society.CivicRecipes;
import com.bunnidogs.mod.society.HearthTreeBlockEntity;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;

/** Bounded original-household authority and safe recovery places; never moves goods or loads terrain. */
public final class CivicRescue {
    private CivicRescue() {}
    static boolean member(Bunnidog patient,HearthTreeBlockEntity home) {
        return home!=null && home.attached() && home.owner()!=null && patient.acceptsCare() && !patient.isBaby()
                && patient.society().belongs(home) && !patient.trail().ordered()
                && (patient.trail().keeper()==null || Objects.equals(patient.trail().keeper(),home.owner()))
                && patient.distanceToSqr(Vec3.atCenterOf(home.getBlockPos()))<24*24;
    }
    static boolean bed(HearthTreeBlockEntity home,BlockPos bed) {
        return home.attached() && bed!=null && bed.distSqr(home.getBlockPos())<=24*24
                && home.getLevel().hasChunkAt(bed) && home.getLevel().getWorldBorder().isWithinBounds(bed)
                && home.getLevel().getBlockState(bed).is(ModBlocks.NEST.get());
    }
    /** Registered adults with a real local Nest can survive combat defeat. Terrain and player harm remain separate. */
    static boolean citizen(Bunnidog patient) {
        var home=patient.society().hearth();var bed=patient.homeNest();
        return member(patient,home) && bed(home,bed) && NestBlock.claimed((ServerLevel)home.getLevel(),bed);
    }
    static boolean patient(HearthTreeBlockEntity home,Bunnidog patient,BlockPos originalBed) {
        return member(patient,home) && patient.isAlive() && !patient.isRemoved() && Objects.equals(originalBed,patient.homeNest())
                && bed(home,originalBed) && NestBlock.claimed((ServerLevel)home.getLevel(),originalBed)
                && patient.combat().canResidentAid() && TravelBasketItem.captureStatus(patient)==TravelBasketItem.Result.READY;
    }
    static Bunnidog patient(Bunnidog carer,HearthTreeBlockEntity home,SocietyWork.Patient ref) {
        if(ref==null || !(carer.level() instanceof ServerLevel level) || !(level.getEntity(ref.id()) instanceof Bunnidog patient)
                || patient==carer || !patient(home,patient,ref.bed()) || patient.distanceToSqr(Vec3.atCenterOf(ref.bed()))<=9)return null;
        return patient;
    }
    static BlockPos recoverySpot(Bunnidog carer,HearthTreeBlockEntity home,BlockPos bed) {
        if(!bed(home,bed))return null;
        for(var spot:HomeRoutes.nearHome(bed))if(spot.distSqr(home.getBlockPos())<24*24
                && carer.level().getWorldBorder().isWithinBounds(spot) && HomeRoutes.trailStanding(carer,spot))return spot;
        return null;
    }
    public static boolean needsBasket(HearthTreeBlockEntity home) {
        if(!home.attached() || CivicRecipes.count(home,ModItems.TRAVEL_BASKET.get())>0)return false;
        var residents=home.residents();
        return residents.stream().anyMatch(patient->patient(home,patient,patient.homeNest())
                && patient.distanceToSqr(Vec3.atCenterOf(patient.homeNest()))>9
                && residents.stream().noneMatch(carer->patient.getUUID().equals(carer.society().treatingPatient())));
    }
    /** Local intent only: automatic housemates leave an occupied basket's destination free. */
    static Set<BlockPos> reservedNests(Bunnidog seeker) {
        var home=seeker.society().hearth();
        return home==null || !home.attached() ? Set.of() : home.residents().stream().map(dog->dog.society().rescueBed())
                .filter(Objects::nonNull).collect(Collectors.toUnmodifiableSet());
    }
}
