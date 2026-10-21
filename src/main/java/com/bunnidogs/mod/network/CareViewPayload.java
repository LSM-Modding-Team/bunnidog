package com.bunnidogs.mod.network;

import com.bunnidogs.mod.menu.CareView;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record CareViewPayload(int containerId,CareView view) implements CustomPacketPayload {
    public static final Type<CareViewPayload> TYPE=new Type<>(Identifier.fromNamespaceAndPath("bunnidogs","care_view"));
    private static final StreamCodec<RegistryFriendlyByteBuf,CareView.Control> CONTROL=StreamCodec.composite(
            ByteBufCodecs.VAR_INT,CareView.Control::id,ComponentSerialization.STREAM_CODEC,CareView.Control::label,
            ByteBufCodecs.BOOL,CareView.Control::enabled,ByteBufCodecs.VAR_INT,CareView.Control::section,
            ComponentSerialization.STREAM_CODEC,CareView.Control::hint,CareView.Control::new);
    private static final StreamCodec<RegistryFriendlyByteBuf,CareView> VIEW=StreamCodec.composite(
            ComponentSerialization.STREAM_CODEC,CareView::title,ComponentSerialization.STREAM_CODEC,CareView::next,
            ComponentSerialization.STREAM_CODEC.apply(ByteBufCodecs.list(12)),CareView::rows,
            CONTROL.apply(ByteBufCodecs.list(12)),CareView::controls,ByteBufCodecs.VAR_INT,CareView::subjectId,CareView::new);
    public static final StreamCodec<RegistryFriendlyByteBuf,CareViewPayload> STREAM_CODEC=StreamCodec.composite(
            ByteBufCodecs.VAR_INT,CareViewPayload::containerId,VIEW,CareViewPayload::view,CareViewPayload::new);
    @Override public Type<CareViewPayload> type() { return TYPE; }
}
