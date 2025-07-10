package com.blocklogic.redlink.client.handler;

import com.blocklogic.redlink.client.RLKeyMappings;
import com.blocklogic.redlink.item.custom.RedstoneRemoteItem;
import com.blocklogic.redlink.network.RLNetworkHandler;
import com.blocklogic.redlink.network.RemoteChannelCyclePacket;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.InputEvent;

public class RemoteKeyHandler {

    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;

        if (player == null || mc.screen != null) {
            return;
        }

        boolean channelPrevious = RLKeyMappings.CHANNEL_PREVIOUS.consumeClick();
        boolean channelNext = RLKeyMappings.CHANNEL_NEXT.consumeClick();

        if (!channelPrevious && !channelNext) {
            return;
        }

        ItemStack heldRemote = getHeldRemote(player);
        if (heldRemote.isEmpty() || !(heldRemote.getItem() instanceof RedstoneRemoteItem remoteItem)) {
            return;
        }

        if (!remoteItem.isBoundToHub(heldRemote)) {
            return;
        }

        if (channelNext) {
            RLNetworkHandler.sendToServer(new RemoteChannelCyclePacket(true));
        } else if (channelPrevious) {
            RLNetworkHandler.sendToServer(new RemoteChannelCyclePacket(false));
        }
    }

    private static ItemStack getHeldRemote(Player player) {
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