package com.bunnidogs.mod.entity;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.datafixers.util.Either;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

/** A bounded deliberate party, shared across dimensions and unloaded/captured animals. */
final class CombatRoster extends SavedData {
    static final int LIMIT=3;
    private record Legacy(UUID animal,long busyUntil) {
        static final Codec<Legacy> CODEC=RecordCodecBuilder.create(i->i.group(
                UUIDUtil.STRING_CODEC.fieldOf("animal").forGetter(Legacy::animal),
                Codec.LONG.optionalFieldOf("busy_until",0L).forGetter(Legacy::busyUntil)).apply(i,Legacy::new));
    }
    record Sighting(String name,String dimension,BlockPos pos) {
        static final Codec<Sighting> CODEC=RecordCodecBuilder.create(i->i.group(
                Codec.STRING.fieldOf("name").forGetter(Sighting::name),
                Codec.STRING.fieldOf("dimension").forGetter(Sighting::dimension),
                BlockPos.CODEC.fieldOf("pos").forGetter(Sighting::pos)).apply(i,Sighting::new));
    }
    private record Party(List<UUID> members,Optional<UUID> command,long busyUntil,Map<UUID,Sighting> seen) {
        Party {
            members=members.stream().distinct().limit(LIMIT).toList();
            if (command.isEmpty() || !members.contains(command.orElseThrow())) command=members.stream().findFirst();
            var kept=new HashMap<UUID,Sighting>(); for (var member:members) if (seen.containsKey(member)) kept.put(member,seen.get(member));
            seen=Map.copyOf(kept);
        }
        static final Codec<Party> CURRENT=RecordCodecBuilder.create(i->i.group(
                UUIDUtil.STRING_CODEC.listOf().fieldOf("members").forGetter(Party::members),
                UUIDUtil.STRING_CODEC.optionalFieldOf("command").forGetter(Party::command),
                Codec.LONG.optionalFieldOf("busy_until",0L).forGetter(Party::busyUntil),
                Codec.unboundedMap(UUIDUtil.STRING_CODEC,Sighting.CODEC).optionalFieldOf("seen",Map.of()).forGetter(Party::seen)).apply(i,Party::new));
        static final Codec<Party> CODEC=Codec.either(CURRENT,Legacy.CODEC).xmap(
                entry->entry.map(p->p,old->new Party(List.of(old.animal),Optional.of(old.animal),old.busyUntil,Map.of())),Either::left);
    }
    static final Codec<CombatRoster> CODEC=Codec.unboundedMap(UUIDUtil.STRING_CODEC,Party.CODEC)
            .xmap(CombatRoster::new,r->Map.copyOf(r.entries));
    private static final SavedDataType<CombatRoster> TYPE=new SavedDataType<>(
            Identifier.fromNamespaceAndPath("bunnidogs","combat_partners"),CombatRoster::new,CODEC);
    private final Map<UUID,Party> entries;
    private CombatRoster() { this.entries=new HashMap<>(); }
    private CombatRoster(Map<UUID,Party> entries) { this.entries=new HashMap<>(entries); }
    static CombatRoster get(ServerLevel level) { return level.getServer().overworld().getDataStorage().computeIfAbsent(TYPE); }
    boolean chosen(UUID keeper,UUID animal) { return members(keeper).contains(animal); }
    List<UUID> members(UUID keeper) { var e=entries.get(keeper); return e==null ? List.of() : e.members; }
    int count(UUID keeper) { return members(keeper).size(); }
    int slot(UUID keeper,UUID animal) { return members(keeper).indexOf(animal); }
    UUID animal(UUID keeper) { var e=entries.get(keeper); return e==null ? null : e.command.orElse(null); }
    Sighting sighting(UUID keeper,UUID member) { var e=entries.get(keeper); return e==null ? null : e.seen.get(member); }
    boolean busy(UUID keeper,long now) { var e=entries.get(keeper); return e!=null && e.busyUntil>now; }
    boolean choose(UUID keeper,UUID animal,long now) {
        var old=entries.get(keeper);
        var members=new ArrayList<>(members(keeper));
        if (!members.contains(animal)) {
            if (members.size()>=LIMIT || busy(keeper,now)) return false;
            members.add(animal);
        }
        entries.put(keeper,new Party(members,Optional.of(animal),old==null ? 0 : old.busyUntil,old==null ? Map.of() : old.seen)); setDirty(); return true;
    }
    boolean remove(UUID keeper,UUID animal,long now) {
        if (!chosen(keeper,animal) || busy(keeper,now)) return false;
        var old=entries.get(keeper); var members=new ArrayList<>(old.members); members.remove(animal);
        if (members.isEmpty()) entries.remove(keeper);
        else entries.put(keeper,new Party(members,old.command,old.busyUntil,old.seen));
        setDirty(); return true;
    }
    void engaged(UUID keeper,UUID animal,long now) {
        if (!chosen(keeper,animal)) return;
        var old=entries.get(keeper);
        entries.put(keeper,new Party(old.members,old.command,now+100,old.seen)); setDirty();
    }
    void saw(Bunnidog dog) {
        var keeper=dog.trail().keeper(); if (!chosen(keeper,dog.getUUID())) return;
        var old=entries.get(keeper); var seen=new HashMap<>(old.seen);
        var sighting=new Sighting(dog.getName().getString(),dog.level().dimension().identifier().toString(),dog.blockPosition().immutable());
        if (sighting.equals(seen.put(dog.getUUID(),sighting))) return;
        entries.put(keeper,new Party(old.members,old.command,old.busyUntil,seen)); setDirty();
    }
}
