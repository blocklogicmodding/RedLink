package com.blocklogic.redlink.network;

import com.blocklogic.redlink.block.entity.TransceiverHubBlockEntity;
import com.blocklogic.redlink.component.ChannelData;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SyncHubDataPacket(BlockPos hubPos, ChannelData channelData, String hubName) implements CustomPacketPayload {
    public static final Type<SyncHubDataPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("redlink", "sync_hub_data"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SyncHubDataPacket> STREAM_CODEC =
            StreamCodec.composite(
                    BlockPos.STREAM_CODEC, SyncHubDataPacket::hubPos,
                    ChannelData.STREAM_CODEC, SyncHubDataPacket::channelData,
                    StreamCodec.of(RegistryFriendlyByteBuf::writeUtf, RegistryFriendlyByteBuf::readUtf), SyncHubDataPacket::hubName,
                    SyncHubDataPacket::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SyncHubDataPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.flow().isClientbound() && Minecraft.getInstance().level != null) {
                BlockEntity blockEntity = Minecraft.getInstance().level.getBlockEntity(packet.hubPos);
                if (blockEntity instanceof TransceiverHubBlockEntity hubEntity) {
                    hubEntity.setChannelData(packet.channelData);
                    hubEntity.setHubName(packet.hubName);
                }
            }
        });
    }
}