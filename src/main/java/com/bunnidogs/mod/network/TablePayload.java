package com.bunnidogs.mod.network;

import com.bunnidogs.mod.BunnidogsMod;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/**
 * Server to client (Genetics Table T2): "open the window of THIS table". The whole payload is the text form of the table's two samples
 * ({@code TableSheet#toString} / {@code TableSheet.parse}, {@code A|B}), one UTF-8 string, so the client reads it with code that is
 * already tested. The shape is exactly that of {@link SamplePayload} (a record, a {@code Type} made from an {@code Identifier}, a one-string codec).
 */
public record TablePayload(String table) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<TablePayload> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(BunnidogsMod.MODID, "table"));

    public static final StreamCodec<ByteBuf, TablePayload> STREAM_CODEC =
            ByteBufCodecs.STRING_UTF8.map(TablePayload::new, TablePayload::table);

    @Override
    public CustomPacketPayload.Type<TablePayload> type() {
        return TYPE;
    }
}
