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

public record HubNameUpdatePacket(BlockPos hubPos, String hubName) implements CustomPacketPayload {
    public static final Type<HubNameUpdatePacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("redlink", "hub_name_update"));

    public static final StreamCodec<FriendlyByteBuf, HubNameUpdatePacket> STREAM_CODEC =
            StreamCodec.composite(
                    BlockPos.STREAM_CODEC, HubNameUpdatePacket::hubPos,
                    StreamCodec.of(FriendlyByteBuf::writeUtf, FriendlyByteBuf::readUtf), HubNameUpdatePacket::hubName,
                    HubNameUpdatePacket::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(HubNameUpdatePacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer serverPlayer) {
                BlockEntity blockEntity = serverPlayer.level().getBlockEntity(packet.hubPos);
                if (blockEntity instanceof TransceiverHubBlockEntity hubEntity) {

                    if (!hubEntity.isValidHubName(packet.hubName)) {
                        serverPlayer.displayClientMessage(Component.translatable("redlink.hub.name_too_long",
                                Config.getMaxChannelNameLength()), true);
                        return;
                    }

                    hubEntity.setHubName(packet.hubName);

                    RLNetworkHandler.syncHubDataToPlayer(serverPlayer, packet.hubPos, hubEntity.getChannelData());
                }
            }
        });
    }
}