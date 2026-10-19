package com.bunnidogs.mod.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/** An acknowledged edit; the window only updates its labels after this server reply. */
public record LineageResultPayload(String records, String previous, String names, boolean saved) implements CustomPacketPayload {
    public static final Type<LineageResultPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath("bunnidogs","lineage_result"));
    public static final StreamCodec<ByteBuf,LineageResultPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.stringUtf8(16384),LineageResultPayload::records,ByteBufCodecs.stringUtf8(8192),LineageResultPayload::previous,
            ByteBufCodecs.stringUtf8(8192),LineageResultPayload::names,ByteBufCodecs.BOOL,LineageResultPayload::saved,LineageResultPayload::new);
    @Override public Type<LineageResultPayload> type() { return TYPE; }
}
