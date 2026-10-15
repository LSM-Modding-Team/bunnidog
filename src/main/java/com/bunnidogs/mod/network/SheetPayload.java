package com.bunnidogs.mod.network;

import com.bunnidogs.mod.BunnidogsMod;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/**
 * Server to client (Phase 8 S6): "open the sheet of THIS animal". The whole payload is the text form of one
 * {@code Pedigree.Entry} ({@code genome#uuid@tag$stats!<mother+father}, about 150 characters; see
 * {@code Pedigree.Entry#toString} and {@code Pedigree.parseEntry}), so the wire format is one UTF-8 string and the client reads
 * it with code that is already tested. The shape follows NeoForge's own payload records ({@code ConfigFilePayload},
 * {@code CommonRegisterPayload}): a record, a {@code Type} made from an {@code Identifier}, a {@code StreamCodec}.
 */
public record SheetPayload(String entry) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<SheetPayload> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(BunnidogsMod.MODID, "sheet"));

    /** One string (the library's default limit of 32,767 characters is far above an entry); {@code map} builds and unpacks the record. */
    public static final StreamCodec<ByteBuf, SheetPayload> STREAM_CODEC =
            ByteBufCodecs.STRING_UTF8.map(SheetPayload::new, SheetPayload::entry);

    @Override
    public CustomPacketPayload.Type<SheetPayload> type() {
        return TYPE;
    }
}
