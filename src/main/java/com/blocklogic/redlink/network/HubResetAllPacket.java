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

public record HubResetAllPacket(BlockPos hubPos) implements CustomPacketPayload {
    public static final Type<HubResetAllPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("redlink", "hub_reset_all"));

    public static final StreamCodec<FriendlyByteBuf, HubResetAllPacket> STREAM_CODEC =
            StreamCodec.composite(
                    BlockPos.STREAM_CODEC, HubResetAllPacket::hubPos,
                    HubResetAllPacket::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(HubResetAllPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer serverPlayer) {
                BlockEntity blockEntity = serverPlayer.level().getBlockEntity(packet.hubPos);
                if (blockEntity instanceof TransceiverHubBlockEntity hubEntity) {

                    hubEntity.resetChannelData();

                    serverPlayer.displayClientMessage(Component.translatable("redlink.hub.all_channels_reset"), true);

                    RLNetworkHandler.sendToPlayer(serverPlayer, new SyncHubDataPacket(packet.hubPos, hubEntity.getChannelData()));
                }
            }
        });
    }
}