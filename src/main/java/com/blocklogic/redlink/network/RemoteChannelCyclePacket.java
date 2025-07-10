package com.blocklogic.redlink.network;

import com.blocklogic.redlink.item.custom.RedstoneRemoteItem;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record RemoteChannelCyclePacket(boolean forward) implements CustomPacketPayload {
    public static final Type<RemoteChannelCyclePacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("redlink", "remote_channel_cycle"));

    public static final StreamCodec<FriendlyByteBuf, RemoteChannelCyclePacket> STREAM_CODEC =
            StreamCodec.composite(
                    StreamCodec.of(FriendlyByteBuf::writeBoolean, FriendlyByteBuf::readBoolean), RemoteChannelCyclePacket::forward,
                    RemoteChannelCyclePacket::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(RemoteChannelCyclePacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer serverPlayer) {
                ItemStack heldRemote = getHeldRemote(serverPlayer);
                if (heldRemote.isEmpty() || !(heldRemote.getItem() instanceof RedstoneRemoteItem remoteItem)) {
                    return;
                }

                if (packet.forward) {
                    remoteItem.handleChannelCycling(serverPlayer.level(), serverPlayer, heldRemote);
                } else {
                    remoteItem.handleChannelCyclingBackward(serverPlayer.level(), serverPlayer, heldRemote);
                }
            }
        });
    }

    private static ItemStack getHeldRemote(ServerPlayer player) {
        ItemStack mainHand = player.getMainHandItem();
        if (mainHand.getItem() instanceof RedstoneRemoteItem) {
            return mainHand;
        }

        ItemStack offHand = player.getOffhandItem();
        if (offHand.getItem() instanceof RedstoneRemoteItem) {
            return offHand;
        }

        return ItemStack.EMPTY;
    }
}