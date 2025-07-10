package com.blocklogic.redlink.network;

import com.blocklogic.redlink.Config;
import com.blocklogic.redlink.block.entity.TransceiverHubBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record HubChannelUpdatePacket(BlockPos hubPos, int channel, String channelName, int pulseFrequency) implements CustomPacketPayload {
    public static final Type<HubChannelUpdatePacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("redlink", "hub_channel_update"));

    public static final StreamCodec<FriendlyByteBuf, HubChannelUpdatePacket> STREAM_CODEC =
            StreamCodec.composite(
                    BlockPos.STREAM_CODEC, HubChannelUpdatePacket::hubPos,
                    StreamCodec.of(FriendlyByteBuf::writeVarInt, FriendlyByteBuf::readVarInt), HubChannelUpdatePacket::channel,
                    StreamCodec.of(FriendlyByteBuf::writeUtf, FriendlyByteBuf::readUtf), HubChannelUpdatePacket::channelName,
                    StreamCodec.of(FriendlyByteBuf::writeVarInt, FriendlyByteBuf::readVarInt), HubChannelUpdatePacket::pulseFrequency,
                    HubChannelUpdatePacket::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(HubChannelUpdatePacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer serverPlayer) {
                BlockEntity blockEntity = serverPlayer.level().getBlockEntity(packet.hubPos);
                if (blockEntity instanceof TransceiverHubBlockEntity hubEntity) {

                    if (!hubEntity.isValidChannel(packet.channel)) {
                        serverPlayer.displayClientMessage(Component.translatable("redlink.hub.invalid_channel", packet.channel), true);
                        return;
                    }

                    if (!hubEntity.isValidChannelName(packet.channelName)) {
                        serverPlayer.displayClientMessage(Component.translatable("redlink.channel.name_too_long",
                                Config.getMaxChannelNameLength()), true);
                        return;
                    }

                    if (!hubEntity.isValidPulseFrequency(packet.pulseFrequency)) {
                        serverPlayer.displayClientMessage(Component.translatable("redlink.channel.frequency_invalid",
                                Config.getMinPulseFrequency(),
                                Config.getMaxPulseFrequency()), true);
                        return;
                    }

                    hubEntity.setChannelName(packet.channel, packet.channelName);
                    hubEntity.setPulseFrequency(packet.channel, packet.pulseFrequency);

                    RLNetworkHandler.sendToPlayer(serverPlayer, new SyncHubDataPacket(packet.hubPos, hubEntity.getChannelData()));
                }
            }
        });
    }
}