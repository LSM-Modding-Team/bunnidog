package com.bunnidogs.mod.network;

import com.bunnidogs.mod.client.ClientScreens;
import com.bunnidogs.mod.genetics.Pedigree;
import com.bunnidogs.mod.genetics.Seed;
import com.bunnidogs.mod.genetics.Tissue;
import com.bunnidogs.mod.layout.TableSheet;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

/**
 * The mod's one network channel (Phase 8 S6; S8 adds the seed and Tissue Sample windows, Genetics Table T2 the table window). Registered from {@code BunnidogsMod} with
 * {@code modEventBus.addListener(ModNetwork::register)}: {@link RegisterPayloadHandlersEvent} is a mod-bus event. The handler runs
 * on the client's main thread (the registrar's default), so it may open a window directly. It is a LAMBDA on purpose: the
 * client-only door {@link ClientScreens} is then touched only when a packet really arrives, never on a dedicated server.
 */
public final class ModNetwork {
    private ModNetwork() {}

    /** Bump for payload shape or required client content/allele semantics. */
    public static final String VERSION = "22";

    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(VERSION);
        registrar.playToClient(CareViewPayload.TYPE,CareViewPayload.STREAM_CODEC,
                (payload,context) -> ClientScreens.careView(payload));
        registrar.playToServer(LineageEditPayload.TYPE,LineageEditPayload.STREAM_CODEC,
                (payload,context) -> PacketDistributor.sendToPlayer((ServerPlayer)context.player(),
                        com.bunnidogs.mod.item.LineageEditing.apply(context.player(),payload)));
        registrar.playToClient(LineageResultPayload.TYPE,LineageResultPayload.STREAM_CODEC,
                (payload,context) -> ClientScreens.lineageResult(payload));
        registrar.playToClient(SheetPayload.TYPE, SheetPayload.STREAM_CODEC,
                (payload, context) -> Pedigree.parseEntry(payload.entry()).ifPresent(ClientScreens::openSheet));
        registrar.playToClient(SeedPayload.TYPE, SeedPayload.STREAM_CODEC,
                (payload, context) -> Seed.parse(payload.seed()).ifPresent(ClientScreens::openSeed));
        registrar.playToClient(SamplePayload.TYPE, SamplePayload.STREAM_CODEC,
                (payload, context) -> Tissue.parse(payload.sample()).ifPresent(ClientScreens::openSample));
        registrar.playToClient(TablePayload.TYPE, TablePayload.STREAM_CODEC,
                (payload, context) -> TableSheet.parse(payload.table()).ifPresent(ClientScreens::openTable));
    }

    /** Tells one player's client to open the sheet of this animal. Call on the server. */
    public static void sendSheet(ServerPlayer player, Pedigree.Entry entry) {
        PacketDistributor.sendToPlayer(player, new SheetPayload(entry.toString()));
    }

    /** Tells one player's client to open the window of this seed (Phase 8 S8). Call on the server. */
    public static void sendSeed(ServerPlayer player, Seed seed) {
        PacketDistributor.sendToPlayer(player, new SeedPayload(seed.toString()));
    }

    /** Tells one player's client to open the window of this Tissue Sample (Phase 8 S8). Call on the server. */
    public static void sendSample(ServerPlayer player, Tissue sample) {
        PacketDistributor.sendToPlayer(player, new SamplePayload(sample.toString()));
    }

    /** Tells one player's client to open the window of a Genetics Table's samples (T2). Call on the server. */
    public static void sendTable(ServerPlayer player, TableSheet table) {
        PacketDistributor.sendToPlayer(player, new TablePayload(table.toString()));
    }
}
