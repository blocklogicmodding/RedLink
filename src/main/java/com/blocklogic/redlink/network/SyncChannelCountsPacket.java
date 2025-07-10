package com.blocklogic.redlink.network;

import com.blocklogic.redlink.client.ClientChannelCountCache;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SyncChannelCountsPacket(BlockPos hubPos, int[] channelCounts) implements CustomPacketPayload {
    public static final Type<SyncChannelCountsPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("redlink", "sync_channel_counts"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SyncChannelCountsPacket> STREAM_CODEC =
            StreamCodec.composite(
                    BlockPos.STREAM_CODEC, SyncChannelCountsPacket::hubPos,
                    StreamCodec.of(SyncChannelCountsPacket::encodeArray, SyncChannelCountsPacket::decodeArray), SyncChannelCountsPacket::channelCounts,
                    SyncChannelCountsPacket::new
            );

    private static void encodeArray(RegistryFriendlyByteBuf buf, int[] counts) {
        for (int i = 0; i < 8; i++) {
            buf.writeVarInt(counts[i]);
        }
    }

    private static int[] decodeArray(RegistryFriendlyByteBuf buf) {
        int[] counts = new int[8];
        for (int i = 0; i < 8; i++) {
            counts[i] = buf.readVarInt();
        }
        return counts;
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SyncChannelCountsPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.flow().isClientbound() && Minecraft.getInstance().level != null) {
                ClientChannelCountCache.updateCounts(packet.hubPos, packet.channelCounts);
            }
        });
    }
}