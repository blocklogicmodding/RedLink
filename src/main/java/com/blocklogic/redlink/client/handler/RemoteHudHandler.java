package com.blocklogic.redlink.client.handler;

import com.blocklogic.redlink.Config;
import com.blocklogic.redlink.RedLink;
import com.blocklogic.redlink.block.entity.TransceiverHubBlockEntity;
import com.blocklogic.redlink.client.ClientChannelCountCache;
import com.blocklogic.redlink.item.custom.RedstoneRemoteItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import org.joml.Math;

public final class RemoteHudHandler {
    private static double animationProgress = 1.0;
    private static boolean wasHidden = true;

    private static final int HUD_WIDTH = 120;
    private static final int HUD_HEIGHT = 164;
    private static final int CHANNEL_HEIGHT = 14;
    private static final int CHANNEL_SPACING = 3;

    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Pre event) {
        var mc = Minecraft.getInstance();
        var player = mc.player;

        if (player == null) {
            return;
        }

        if (!isVisible(mc)) {
            if (animationProgress < 1.0) {
                wasHidden = true;
                animationProgress = Math.clamp(0.0, 1.0, animationProgress + Config.getHudAnimationSpeed());
            }
            return;
        }

        ItemStack heldItem = getHeldRemote(player);
        if (heldItem.isEmpty() || !(heldItem.getItem() instanceof RedstoneRemoteItem remoteItem)) {
            if (animationProgress < 1.0) {
                wasHidden = true;
                animationProgress = Math.clamp(0.0, 1.0, animationProgress + Config.getHudAnimationSpeed());
            }
            return;
        }

        if (!remoteItem.isBoundToHub(heldItem)) {
            if (animationProgress < 1.0) {
                wasHidden = true;
                animationProgress = Math.clamp(0.0, 1.0, animationProgress + Config.getHudAnimationSpeed());
            }
            return;
        }

        TransceiverHubBlockEntity hub = remoteItem.getBoundHub(heldItem, mc.level);
        if (hub == null || !remoteItem.isHubAccessible(heldItem, mc.level, player)) {
            if (animationProgress < 1.0) {
                wasHidden = true;
                animationProgress = Math.clamp(0.0, 1.0, animationProgress + Config.getHudAnimationSpeed());
            }
            return;
        }

        if (animationProgress > 0.0) {
            wasHidden = false;
            animationProgress = Math.clamp(0.0, 1.0, animationProgress - Config.getHudAnimationSpeed());
        }

        renderHud(event.getGuiGraphics(), heldItem, remoteItem, hub, mc);
    }

    private static ItemStack getHeldRemote(net.minecraft.world.entity.player.Player player) {
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

    private static void renderHud(GuiGraphics guiGraphics, ItemStack remoteStack, RedstoneRemoteItem remoteItem,
                                  TransceiverHubBlockEntity hub, Minecraft mc) {

        if (animationProgress >= 1.0) {
            return;
        }

        var hudPos = getHudPosition(mc, animationProgress);

        if (hudPos == null) {
            return;
        }

        int currentChannel = remoteItem.getCurrentChannel(remoteStack);
        int maxTransceivers = Config.getMaxTransceiversPerChannel();

        var matrix = guiGraphics.pose();
        matrix.pushPose();

        renderBackground(guiGraphics, hudPos.x, hudPos.y);

        Component title = Component.literal(hub.getHubName());
        float scale = 0.7f;
        int titleWidth = mc.font.width(title);
        int scaledX = hudPos.x + (HUD_WIDTH - (int)(titleWidth * scale)) / 2;
        int scaledY = hudPos.y + 8;

        var pose = guiGraphics.pose();
        pose.pushPose();
        pose.translate(scaledX, scaledY, 0);
        pose.scale(scale, scale, 1.0f);
        guiGraphics.drawString(mc.font, title, 0, 0, 0xFFFFFF);
        pose.popPose();

        var hubBlockPos = remoteItem.getBoundHubPos(remoteStack);
        if (hubBlockPos != null) {
            Component hubCoords = Component.translatable("redlink.hud.bound_to_hub",
                    hubBlockPos.getX(), hubBlockPos.getY(), hubBlockPos.getZ());
            float coordScale = 0.5f;
            int coordWidth = mc.font.width(hubCoords);
            int coordX = hudPos.x + (HUD_WIDTH - (int)(coordWidth * coordScale)) / 2;
            int coordY = hudPos.y + 18;

            pose.pushPose();
            pose.translate(coordX, coordY, 0);
            pose.scale(coordScale, coordScale, 1.0f);
            guiGraphics.drawString(mc.font, hubCoords, 0, 0, 0xAAAAAA);
            pose.popPose();
        }

        int channelStartY = hudPos.y + 30;

        for (int channel = 0; channel < 8; channel++) {
            int channelY = channelStartY + (channel * (CHANNEL_HEIGHT + CHANNEL_SPACING));

            boolean isSelected = (channel == currentChannel);
            renderChannel(guiGraphics, mc, hudPos.x + 4, channelY, channel, hub, maxTransceivers, isSelected);
        }

        matrix.popPose();
    }

    private static void renderBackground(GuiGraphics guiGraphics, int x, int y) {
        guiGraphics.fill(x, y, x + HUD_WIDTH, y + HUD_HEIGHT, 0xAA000000);

        guiGraphics.fill(x, y, x + HUD_WIDTH, y + 1, 0xFF555555);
        guiGraphics.fill(x, y + HUD_HEIGHT - 1, x + HUD_WIDTH, y + HUD_HEIGHT, 0xFF555555);
        guiGraphics.fill(x, y, x + 1, y + HUD_HEIGHT, 0xFF555555);
        guiGraphics.fill(x + HUD_WIDTH - 1, y, x + HUD_WIDTH, y + HUD_HEIGHT, 0xFF555555);
    }

    private static void renderChannel(GuiGraphics guiGraphics, Minecraft mc, int x, int y, int channel,
                                      TransceiverHubBlockEntity hub, int maxTransceivers, boolean isSelected) {

        String channelName = hub.getChannelName(channel);
        int channelColor = hub.getChannelColor(channel);

        int transceiverCount;
        if (ClientChannelCountCache.hasCachedData(hub.getBlockPos())) {
            transceiverCount = ClientChannelCountCache.getChannelCount(hub.getBlockPos(), channel);
        } else {
            transceiverCount = hub.getTransceiverCount(channel);
        }

        if (isSelected) {
            guiGraphics.fill(x - 2, y - 1, x + HUD_WIDTH - 6, y + CHANNEL_HEIGHT - 1, 0x44FFFFFF);
        }

        guiGraphics.fill(x + 1, y + 1, x + 6, y + 8, 0xFF000000 | channelColor);

        Component channelNum = Component.literal(String.valueOf(channel + 1));
        var pose = guiGraphics.pose();
        pose.pushPose();
        pose.translate(x + 2, y + 2, 0);
        pose.scale(0.6f, 0.6f, 1.0f);
        guiGraphics.drawString(mc.font, channelNum, 0, 0, 0xFFFFFF);
        pose.popPose();

        Component nameComponent = Component.literal(channelName).withStyle(style -> style.withColor(channelColor));
        pose.pushPose();
        pose.translate(x + 10, y + 2, 0);
        pose.scale(0.6f, 0.6f, 1.0f);
        guiGraphics.drawString(mc.font, nameComponent, 0, 0, channelColor);
        pose.popPose();

        String countText = transceiverCount + "/" + maxTransceivers;
        int countColor = 0x55FF55;
        if (transceiverCount >= maxTransceivers) {
            countColor = 0xFF5555;
        } else if (transceiverCount >= maxTransceivers * 0.8) {
            countColor = 0xFFFF55;
        }

        Component countComponent = Component.literal(countText);
        int countWidth = mc.font.width(countComponent);
        pose.pushPose();
        pose.translate(x + HUD_WIDTH - 8 - (int)(countWidth * 0.8f), y + 2, 0);
        pose.scale(0.6f, 0.6f, 1.0f);
        guiGraphics.drawString(mc.font, countComponent, 0, 0, countColor);
        pose.popPose();
    }

    private static HudPos getHudPosition(Minecraft mc, double animationProgress) {
        int windowHeight = mc.getWindow().getGuiScaledHeight();

        int baseX = 10 + Config.getHudOffsetX();
        int baseY = (windowHeight - HUD_HEIGHT) / 2 + Config.getHudOffsetY();

        int animatedX = (int) (baseX - (animationProgress * (HUD_WIDTH + 20)));

        return new HudPos(animatedX, baseY);
    }

    private static boolean isVisible(Minecraft mc) {
        return Config.isHudEnabled()
                && Config.shouldShowRemoteOverlay()
                && (Config.shouldShowHudOverChat() || !(mc.screen instanceof ChatScreen))
                && !mc.options.hideGui
                && !mc.getDebugOverlay().showDebugScreen();
    }

    private record HudPos(int x, int y) {}
}