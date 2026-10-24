package com.bunnidogs.mod.entity;

import com.mojang.serialization.Codec;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.UUIDUtil;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

/** World-owned heart debt survives logout/dimension changes and can release offline victims. */
final class LifebinderMarks extends SavedData {
    private static final Identifier MODIFIER=Identifier.fromNamespaceAndPath("bunnidogs","lifebinder_hearts");
    static final Codec<LifebinderMarks> CODEC=Codec.unboundedMap(UUIDUtil.STRING_CODEC,
            Codec.unboundedMap(UUIDUtil.STRING_CODEC,Codec.intRange(1,1023))).xmap(LifebinderMarks::new,r->r.debts);
    private static final SavedDataType<LifebinderMarks> TYPE=new SavedDataType<>(Identifier.fromNamespaceAndPath("bunnidogs","lifebinder_marks"),LifebinderMarks::new,CODEC);
    private final Map<UUID,Map<UUID,Integer>> debts=new HashMap<>();
    private LifebinderMarks() {}
    private LifebinderMarks(Map<UUID,Map<UUID,Integer>> saved) { saved.forEach((id,marks)->debts.put(id,new HashMap<>(marks))); }
    static LifebinderMarks get(ServerLevel level) { return level.getServer().overworld().getDataStorage().computeIfAbsent(TYPE); }
    int lost(UUID player) { return debts.getOrDefault(player,Map.of()).values().stream().mapToInt(Integer::intValue).sum(); }
    void mark(Apex caster,ServerPlayer player) {
        sync(player);
        // Do not consume the final complete heart container, including on unusual max-health builds.
        if(player.getMaxHealth()<4)return;
        debts.computeIfAbsent(player.getUUID(),id->new HashMap<>()).merge(caster.getUUID(),1,Integer::sum);
        setDirty();sync(player);
    }
    void restore(ServerPlayer player,int hearts) {
        var marks=debts.get(player.getUUID());if(marks==null)return;
        int remaining=hearts;
        for(var id:java.util.List.copyOf(marks.keySet())) {
            int removed=Math.min(remaining,marks.get(id)),count=marks.get(id)-removed;
            if(count==0)marks.remove(id);else marks.put(id,count);
            remaining-=removed;if(remaining<=0)break;
        }
        if(marks.isEmpty())debts.remove(player.getUUID());setDirty();sync(player);
    }
    void clear(ServerPlayer player) { if(debts.remove(player.getUUID())!=null)setDirty();sync(player); }
    void casterDied(Apex caster) {
        // Killing a marking Lifebinder breaks all heart loss for its victims, even while offline.
        var freed=debts.entrySet().stream().filter(e->e.getValue().containsKey(caster.getUUID())).map(Map.Entry::getKey).toList();
        for(var id:freed) { debts.remove(id);var p=caster.level().getServer().getPlayerList().getPlayer(id);if(p!=null)sync(p); }
        if(!freed.isEmpty())setDirty();
        ApexDaze.releaseCaster(caster);
    }
    void sync(ServerPlayer player) {
        var attribute=player.getAttribute(Attributes.MAX_HEALTH);if(attribute==null)return;
        var existing=attribute.getModifier(MODIFIER);int count=lost(player.getUUID());
        if(existing!=null && existing.amount()==-2.0*count)return;
        attribute.removeModifier(MODIFIER);
        double maximumLoss=Math.max(0,attribute.getValue()-2);
        if(count>0)attribute.addTransientModifier(new AttributeModifier(MODIFIER,-Math.min(count*2.0,maximumLoss),AttributeModifier.Operation.ADD_VALUE));
        if(player.getHealth()>player.getMaxHealth())player.setHealth(player.getMaxHealth());
    }
}
