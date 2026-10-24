package com.bunnidogs.mod.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import com.bunnidogs.mod.block.ApexCrucibleBlock;
import com.bunnidogs.mod.block.StationUse;
import com.bunnidogs.mod.block.TrophyPedestalBlockEntity;
import com.bunnidogs.mod.item.TissueSampleItem;
import com.bunnidogs.mod.registry.ModBlocks;
import com.bunnidogs.mod.registry.ModEntities;
import com.bunnidogs.mod.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;

/** Read the loaded ring once; recheck that exact stock after native spawn callbacks, then pay together. */
public final class ApexRitual {
    private ApexRitual() {}
    public enum Status { READY, SAMPLE, PEACEFUL, LOADED, ACTIVE, OFFERINGS, PERMISSION, BUSY, CHANGED, BLOCKED, SUMMONED }
    public record Slot(BlockPos pos,TrophyPedestalBlockEntity pedestal,ItemStack stock) {
        public Slot { pos=pos.immutable(); stock=stock.copy(); }
        boolean current(ServerLevel level) { return level.hasChunkAt(pos) && level.getBlockEntity(pos)==pedestal
                && pedestal!=null && !pedestal.isRemoved() && ItemStack.matches(stock,pedestal.item()); }
    }
    public record Reading(Status status,List<Slot> slots,List<Slot> chosen,List<Item> missing) {
        public Reading { slots=List.copyOf(slots); chosen=List.copyOf(chosen); missing=List.copyOf(missing); }
    }
    private record Key(ServerLevel level,BlockPos pos) {}
    private static final Set<Key> ACTIVE=ConcurrentHashMap.newKeySet();
    private static final Set<TrophyPedestalBlockEntity> RESERVED=ConcurrentHashMap.newKeySet();
    public static List<Item> legends() { return List.of(ModItems.TITAN_HAUNCH.get(),ModItems.GILDED_FOOT.get(),ModItems.MOON_SINEW.get(),ModItems.LISTENING_SHELL.get(),ModItems.EVERBLOOM.get()); }
    public static Reading inspect(Player player,BlockPos pos,ItemStack source) {
        var level=player.level(); var slots=new ArrayList<Slot>(); var chosen=new ArrayList<Slot>(); var missing=new ArrayList<Item>();
        for (var offset:ApexCrucibleBlock.RING) {
            var at=pos.offset(offset);
            var pedestal=level.hasChunkAt(at) && level.getBlockEntity(at) instanceof TrophyPedestalBlockEntity p ? p : null;
            slots.add(new Slot(at,pedestal,pedestal==null ? ItemStack.EMPTY : pedestal.item()));
        }
        for (var legend:legends()) {
            var found=slots.stream().filter(s->s.stock().is(legend)).findFirst();
            if (found.isPresent()) chosen.add(found.orElseThrow()); else missing.add(legend);
        }
        Status status=!level.hasChunksAt(pos.offset(-4,0,-4),pos.offset(4,4,4)) ? Status.LOADED
                : level.getDifficulty()==Difficulty.PEACEFUL ? Status.PEACEFUL
                : level instanceof ServerLevel server && ACTIVE.contains(new Key(server,pos)) || chosen.stream().anyMatch(s->RESERVED.contains(s.pedestal())) ? Status.BUSY
                : !level.getEntitiesOfClass(Apex.class,new AABB(pos).inflate(48),Apex::isAlive).isEmpty() ? Status.ACTIVE
                : TissueSampleItem.read(source).isEmpty() ? Status.SAMPLE : !missing.isEmpty() ? Status.OFFERINGS : Status.READY;
        return new Reading(status,slots,chosen,missing);
    }
    public static ItemStack heldSigil(Player player) {
        if (player.getMainHandItem().getItem() instanceof com.bunnidogs.mod.item.ApexSigilItem) return player.getMainHandItem();
        if (player.getOffhandItem().getItem() instanceof com.bunnidogs.mod.item.ApexSigilItem) return player.getOffhandItem();
        return ItemStack.EMPTY;
    }
    public static Status summon(Player player,BlockPos pos,ItemStack actual) { return summon(player,pos,actual,ItemStack.EMPTY); }
    public static Status summon(Player player,BlockPos pos,ItemStack actual,ItemStack sigil) {
        if (!(player.level() instanceof ServerLevel level) || !StationUse.editable(player,pos)
                || !level.getBlockState(pos).is(ModBlocks.APEX_CRUCIBLE.get())) return Status.PERMISSION;
        if (!StationUse.owns(player,actual)) return Status.SAMPLE;
        if (!sigil.isEmpty() && (!(sigil.getItem() instanceof com.bunnidogs.mod.item.ApexSigilItem) || !StationUse.owns(player,sigil))) return Status.CHANGED;
        var reading=inspect(player,pos,actual); if (reading.status()!=Status.READY) return reading.status();
        for (var slot:reading.chosen()) if (!level.mayInteract(player,slot.pos())) return Status.PERMISSION;
        var key=new Key(level,pos.immutable()); if (!ACTIVE.add(key)) return Status.BUSY;
        var claimed=new ArrayList<TrophyPedestalBlockEntity>(); Apex apex=null; boolean paid=false;
        try {
            for (var slot:reading.chosen()) {
                if (!RESERVED.add(slot.pedestal())) return Status.BUSY;
                claimed.add(slot.pedestal());
            }
            var expected=actual.copy(); var expectedSigil=sigil.copy(); boolean creative=player.hasInfiniteMaterials();
            var source=TissueSampleItem.read(expected).orElseThrow();
            var focus=sigil.getItem() instanceof com.bunnidogs.mod.item.ApexSigilItem item ? item : null;
            apex=ModEntities.apex(focus==null ? ApexKind.BRUTE : focus.kind,focus!=null && focus.flying).create(level,EntitySpawnReason.TRIGGERED);
            if (apex==null) return Status.BLOCKED;
            apex.setGenome(source.genome()); apex.setStats(source.stats()); apex.setHealth(apex.getMaxHealth());
            apex.snapTo(pos.getX()+.5,pos.getY()+1.01,pos.getZ()+.5,player.getYRot()+180,0);
            var space=apex.getBoundingBox().inflate(.2,0,.2);
            if (!level.noCollision(apex,space) || level.containsAnyLiquid(space)) return Status.BLOCKED;
            if (!creative) apex.setTarget(player);
            if (!level.addFreshEntity(apex) || !apex.isAlive() || apex.isRemoved()) return Status.BLOCKED;
            // EntityJoinLevelEvent can cancel, replace stock, move the keeper or reenter another crucible.
            if (!StationUse.editable(player,pos) || !level.getBlockState(pos).is(ModBlocks.APEX_CRUCIBLE.get())
                    || !ItemStack.matches(sigil,expectedSigil) || !expectedSigil.isEmpty() && !StationUse.owns(player,sigil)
                    || creative!=player.hasInfiniteMaterials() || !StationUse.owns(player,actual) || !ItemStack.matches(actual,expected)
                    || reading.chosen().stream().anyMatch(s->!s.current(level) || !level.mayInteract(player,s.pos()))
                    || !level.hasChunksAt(pos.offset(-4,0,-4),pos.offset(4,4,4)) || level.getDifficulty()==Difficulty.PEACEFUL) return Status.CHANGED;
            if (!level.noCollision(apex,space) || level.containsAnyLiquid(space)) return Status.BLOCKED;
            Apex summoned=apex;
            if (!level.getEntitiesOfClass(Apex.class,new AABB(pos).inflate(48),other->other!=summoned && other.isAlive()).isEmpty()) return Status.ACTIVE;
            if (!creative) try (var tx=Transaction.openRoot()) {
                for (var slot:reading.chosen()) if (slot.pedestal().ritualStock().extract(ItemResource.of(slot.stock()),1,tx)!=1) return Status.CHANGED;
                actual.consume(1,player); sigil.consume(1,player); tx.commit();
            }
            paid=true;
            apex.playSound(SoundEvents.RAVAGER_ROAR,1,.6F);
            level.sendParticles(ParticleTypes.WITCH,apex.getX(),apex.getY()+.5,apex.getZ(),60,1,.5,1,.04);
            return Status.SUMMONED;
        } finally {
            if (!paid && apex!=null) apex.discard();
            for (var pedestal:claimed) RESERVED.remove(pedestal);
            ACTIVE.remove(key);
        }
    }
}
