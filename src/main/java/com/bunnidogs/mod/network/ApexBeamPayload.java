package com.bunnidogs.mod.network;

import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;

/** Bounded pulse geometry; clients produce the particles locally rather than receiving a packet per particle. */
public record ApexBeamPayload(Vec3 from,Vec3 to,int style) implements CustomPacketPayload {
    public static final Type<ApexBeamPayload> TYPE=new Type<>(Identifier.fromNamespaceAndPath("bunnidogs","apex_beam"));
    public static final StreamCodec<RegistryFriendlyByteBuf,ApexBeamPayload> CODEC=StreamCodec.composite(
            Vec3.STREAM_CODEC,ApexBeamPayload::from,Vec3.STREAM_CODEC,ApexBeamPayload::to,ByteBufCodecs.VAR_INT,ApexBeamPayload::style,ApexBeamPayload::new);
    @Override public Type<ApexBeamPayload> type() { return TYPE; }
}
