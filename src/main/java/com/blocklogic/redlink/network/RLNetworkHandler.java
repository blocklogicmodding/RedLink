package com.blocklogic.redlink.network;

import com.blocklogic.redlink.RedLink;
import com.blocklogic.redlink.block.entity.TransceiverHubBlockEntity;
import com.blocklogic.redlink.component.ChannelData;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = RedLink.MODID, bus = EventBusSubscriber.Bus.MOD)
public class RLNetworkHandler {
    private static final String PROTOCOL_VERSION = "1";

    @SubscribeEvent
    public static void register(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar(RedLink.MODID)
                .versioned(PROTOCOL_VERSION);

        registrar.playToServer(HubChannelUpdatePacket.TYPE, HubChannelUpdatePacket.STREAM_CODEC, HubChannelUpdatePacket::handle);
        registrar.playToServer(HubResetChannelPacket.TYPE, HubResetChannelPacket.STREAM_CODEC, HubResetChannelPacket::handle);
        registrar.playToServer(HubResetAllPacket.TYPE, HubResetAllPacket.STREAM_CODEC, HubResetAllPacket::handle);

        registrar.playToClient(SyncHubDataPacket.TYPE, SyncHubDataPacket.STREAM_CODEC, SyncHubDataPacket::handle);
        registrar.playToClient(SyncChannelCountsPacket.TYPE, SyncChannelCountsPacket.STREAM_CODEC, SyncChannelCountsPacket::handle);
        registrar.playToServer(HubNameUpdatePacket.TYPE, HubNameUpdatePacket.STREAM_CODEC, HubNameUpdatePacket::handle);
    }

    public static void sendToPlayer(ServerPlayer player, CustomPacketPayload payload) {
        PacketDistributor.sendToPlayer(player, payload);
    }

    public static void sendToAllPlayers(CustomPacketPayload payload) {
        PacketDistributor.sendToAllPlayers(payload);
    }

    public static void sendToPlayersNear(ServerLevel level,
                                         BlockPos pos,
                                         double radius,
                                         CustomPacketPayload payload) {
        PacketDistributor.sendToPlayersNear(level, null, pos.getX(), pos.getY(), pos.getZ(), radius, payload);
    }

    public static void sendToServer(CustomPacketPayload payload) {
        PacketDistributor.sendToServer(payload);
    }

    public static void sendHubChannelUpdate(BlockPos hubPos, int channel, String channelName, int pulseFrequency) {
        sendToServer(new HubChannelUpdatePacket(hubPos, channel, channelName, pulseFrequency));
    }

    public static void sendHubChannelReset(BlockPos hubPos, int channel) {
        sendToServer(new HubResetChannelPacket(hubPos, channel));
    }

    public static void sendHubResetAll(BlockPos hubPos) {
        sendToServer(new HubResetAllPacket(hubPos));
    }

    public static void syncHubDataToPlayer(ServerPlayer player, BlockPos hubPos, ChannelData channelData) {
        // Get the hub entity to get the hub name
        if (player.level().getBlockEntity(hubPos) instanceof TransceiverHubBlockEntity hub) {
            sendToPlayer(player, new SyncHubDataPacket(hubPos, channelData, hub.getHubName()));
        }
    }

    public static void syncChannelCountsToPlayer(ServerPlayer player, BlockPos hubPos, int[] channelCounts) {
        sendToPlayer(player, new SyncChannelCountsPacket(hubPos, channelCounts));
    }

    public static void syncChannelCountsToNearbyPlayers(ServerLevel level, BlockPos hubPos, int[] channelCounts) {
        sendToPlayersNear(level, hubPos, 64.0, new SyncChannelCountsPacket(hubPos, channelCounts));
    }

    public static void sendHubNameUpdate(BlockPos hubPos, String hubName) {
        sendToServer(new HubNameUpdatePacket(hubPos, hubName));
    }

    public static void syncHubDataToPlayer(ServerPlayer player, BlockPos hubPos, ChannelData channelData, String hubName) {
        sendToPlayer(player, new SyncHubDataPacket(hubPos, channelData, hubName));
    }

    public static void syncHubDataToNearbyPlayers(ServerLevel level,
                                                  BlockPos hubPos,
                                                  ChannelData channelData) {
        if (level.getBlockEntity(hubPos) instanceof TransceiverHubBlockEntity hub) {
            sendToPlayersNear(level, hubPos, 64.0, new SyncHubDataPacket(hubPos, channelData, hub.getHubName()));
        }
    }
}