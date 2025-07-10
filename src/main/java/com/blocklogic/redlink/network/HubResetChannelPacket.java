package com.blocklogic.redlink.network;

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

public record HubResetChannelPacket(BlockPos hubPos, int channel) implements CustomPacketPayload {
    public static final Type<HubResetChannelPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("redlink", "hub_reset_channel"));

    public static final StreamCodec<FriendlyByteBuf, HubResetChannelPacket> STREAM_CODEC =
            StreamCodec.composite(
                    BlockPos.STREAM_CODEC, HubResetChannelPacket::hubPos,
                    StreamCodec.of(FriendlyByteBuf::writeVarInt, FriendlyByteBuf::readVarInt), HubResetChannelPacket::channel,
                    HubResetChannelPacket::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(HubResetChannelPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer serverPlayer) {
                BlockEntity blockEntity = serverPlayer.level().getBlockEntity(packet.hubPos);
                if (blockEntity instanceof TransceiverHubBlockEntity hubEntity) {

                    if (!hubEntity.isValidChannel(packet.channel)) {
                        serverPlayer.sendSystemMessage(Component.translatable("redlink.hub.invalid_channel", packet.channel));
                        return;
                    }

                    hubEntity.resetChannel(packet.channel);

                    Component channelNameComponent = Component.literal(hubEntity.getChannelName(packet.channel))
                            .withStyle(style -> style.withColor(hubEntity.getChannelColor(packet.channel)));

                    serverPlayer.displayClientMessage(Component.translatable("redlink.hub.channel_reset", channelNameComponent), true);

                    RLNetworkHandler.sendToPlayer(serverPlayer, new SyncHubDataPacket(packet.hubPos, hubEntity.getChannelData()));
                }
            }
        });
    }
}