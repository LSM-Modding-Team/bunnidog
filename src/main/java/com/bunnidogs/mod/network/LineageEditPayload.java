package com.bunnidogs.mod.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/** Bounded edit of one existing genotype in the exact held book snapshot. */
public record LineageEditPayload(int hand, String records, String previous, String genome, String name) implements CustomPacketPayload {
    public static final Type<LineageEditPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath("bunnidogs","lineage_edit"));
    public static final StreamCodec<ByteBuf,LineageEditPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT,LineageEditPayload::hand,ByteBufCodecs.stringUtf8(16384),LineageEditPayload::records,
            ByteBufCodecs.stringUtf8(8192),LineageEditPayload::previous,ByteBufCodecs.stringUtf8(64),LineageEditPayload::genome,
            ByteBufCodecs.stringUtf8(256),LineageEditPayload::name,LineageEditPayload::new);
    @Override public Type<LineageEditPayload> type() { return TYPE; }
}
