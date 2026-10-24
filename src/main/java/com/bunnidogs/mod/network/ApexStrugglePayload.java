package com.bunnidogs.mod.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record ApexStrugglePayload(int nonce,int movement) implements CustomPacketPayload {
    public static final Type<ApexStrugglePayload> TYPE=new Type<>(Identifier.fromNamespaceAndPath("bunnidogs","anvil_struggle"));
    public static final StreamCodec<RegistryFriendlyByteBuf,ApexStrugglePayload> CODEC=StreamCodec.composite(ByteBufCodecs.VAR_INT,ApexStrugglePayload::nonce,ByteBufCodecs.VAR_INT,ApexStrugglePayload::movement,ApexStrugglePayload::new);
    @Override public Type<ApexStrugglePayload> type() { return TYPE; }
}
