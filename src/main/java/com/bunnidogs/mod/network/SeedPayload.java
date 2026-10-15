package com.bunnidogs.mod.network;

import com.bunnidogs.mod.BunnidogsMod;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/**
 * Server to client (Phase 8 S8): "open the window of THIS seed". The whole payload is the text form of
 * the Seed ({@code Seed#toString} / {@code Seed.parse}), one UTF-8 string, so the client reads it with code that is already tested.
 * The shape is exactly that of {@link SheetPayload} (a record, a {@code Type} made from an {@code Identifier}, a one-string codec).
 */
public record SeedPayload(String seed) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<SeedPayload> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(BunnidogsMod.MODID, "seed"));

    public static final StreamCodec<ByteBuf, SeedPayload> STREAM_CODEC =
            ByteBufCodecs.STRING_UTF8.map(SeedPayload::new, SeedPayload::seed);

    @Override
    public CustomPacketPayload.Type<SeedPayload> type() {
        return TYPE;
    }
}
