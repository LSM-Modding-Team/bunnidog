package com.bunnidogs.mod.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record ApexDazePayload(int nonce,int progress) implements CustomPacketPayload {
    public static final Type<ApexDazePayload> TYPE=new Type<>(Identifier.fromNamespaceAndPath("bunnidogs","anvil_daze"));
    public static final StreamCodec<RegistryFriendlyByteBuf,ApexDazePayload> CODEC=StreamCodec.composite(ByteBufCodecs.VAR_INT,ApexDazePayload::nonce,ByteBufCodecs.VAR_INT,ApexDazePayload::progress,ApexDazePayload::new);
    @Override public Type<ApexDazePayload> type() { return TYPE; }
}
