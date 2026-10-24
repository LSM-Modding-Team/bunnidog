package com.bunnidogs.mod.entity;

import com.bunnidogs.mod.network.ApexDazePayload;
import com.bunnidogs.mod.registry.ModEffects;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.neoforged.neoforge.network.PacketDistributor;

/** Persistent movement stun, freed by a bounded amount of actual client look movement. */
public final class ApexDaze {
    private ApexDaze() {}
    public static final int REQUIRED=360,PER_TICK=18;
    private static final String DATA="BunnidogsAnvilDaze";
    private static final Map<UUID,Long> LAST_INPUT=new HashMap<>();
    public static void hit(Apex caster,ServerPlayer player) {
        if(player.hasEffect(ModEffects.ANVIL_DAZE))return;
        if(!player.addEffect(new MobEffectInstance(ModEffects.ANVIL_DAZE,-1,0,false,true,true)))return;
        int nonce=caster.getRandom().nextInt(Integer.MAX_VALUE-1)+1;
        var tag=new net.minecraft.nbt.CompoundTag();tag.putInt("Nonce",nonce);tag.putInt("Progress",0);tag.putString("Caster",caster.getUUID().toString());
        player.getPersistentData().put(DATA,tag);LAST_INPUT.put(player.getUUID(),player.level().getGameTime());
        player.stopRiding();player.setDeltaMovement(0,0,0);player.hurtMarked=true;send(player);
    }
    public static void struggle(ServerPlayer player,int nonce,int movement) {
        var tag=player.getPersistentData().getCompoundOrEmpty(DATA);
        if(!player.isAlive() || !player.hasEffect(ModEffects.ANVIL_DAZE) || tag.getIntOr("Nonce",0)!=nonce || nonce==0)return;
        long now=player.level().getGameTime(),last=LAST_INPUT.getOrDefault(player.getUUID(),now);
        int elapsed=(int)Math.clamp(now-last,0L,4L),accepted=Math.clamp(movement,0,elapsed*PER_TICK);
        if(accepted==0)return;
        LAST_INPUT.put(player.getUUID(),now);
        int progress=Math.min(REQUIRED,tag.getIntOr("Progress",0)+accepted);tag.putInt("Progress",progress);
        if(progress==REQUIRED)clear(player);else send(player);
    }
    static void tick(ServerPlayer player) {
        var tag=player.getPersistentData().getCompoundOrEmpty(DATA);int nonce=tag.getIntOr("Nonce",0);
        if(nonce==0) { if(player.hasEffect(ModEffects.ANVIL_DAZE))player.removeEffect(ModEffects.ANVIL_DAZE);return; }
        if(!player.isAlive() || !player.hasEffect(ModEffects.ANVIL_DAZE)) { clear(player);return; }
        LAST_INPUT.putIfAbsent(player.getUUID(),player.level().getGameTime());
        if(player.tickCount%10==0)send(player);
    }
    public static void clear(ServerPlayer player) {
        boolean active=player.getPersistentData().contains(DATA) || player.hasEffect(ModEffects.ANVIL_DAZE);
        player.getPersistentData().remove(DATA);LAST_INPUT.remove(player.getUUID());player.removeEffect(ModEffects.ANVIL_DAZE);
        if(active)PacketDistributor.sendToPlayer(player,new ApexDazePayload(0,0));
    }
    private static void send(ServerPlayer player) {
        var tag=player.getPersistentData().getCompoundOrEmpty(DATA);
        PacketDistributor.sendToPlayer(player,new ApexDazePayload(tag.getIntOr("Nonce",0),tag.getIntOr("Progress",0)));
    }
    static void releaseCaster(Apex caster) {
        for(var p:caster.level().getServer().getPlayerList().getPlayers())
            if(p.getPersistentData().getCompoundOrEmpty(DATA).getStringOr("Caster","").equals(caster.getUUID().toString()))clear(p);
    }
}
