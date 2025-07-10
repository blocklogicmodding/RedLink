package com.blocklogic.redlink.client.renderer;

import com.blocklogic.redlink.Config;
import com.blocklogic.redlink.block.entity.TransceiverBlockEntity;
import com.blocklogic.redlink.block.entity.TransceiverHubBlockEntity;
import com.blocklogic.redlink.item.custom.RedstoneRemoteItem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

@OnlyIn(Dist.CLIENT)
@EventBusSubscriber(value = Dist.CLIENT)
public class WireframeRenderer {

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        Level level = mc.level;

        if (player == null || level == null || !isWireframeVisible(mc)) {
            return;
        }

        ItemStack heldRemote = getHeldRemote(player);
        if (heldRemote.isEmpty() || !(heldRemote.getItem() instanceof RedstoneRemoteItem remoteItem)) {
            return;
        }

        if (!remoteItem.isBoundToHub(heldRemote)) {
            return;
        }

        TransceiverHubBlockEntity hub = remoteItem.getBoundHub(heldRemote, level);
        if (hub == null || !remoteItem.isHubAccessible(heldRemote, level, player)) {
            return;
        }

        PoseStack poseStack = event.getPoseStack();
        Vec3 cameraPos = event.getCamera().getPosition();

        renderHubWireframe(poseStack, cameraPos, hub.getBlockPos(), level);

        int currentChannel = remoteItem.getCurrentChannel(heldRemote);
        renderChannelTransceivers(poseStack, cameraPos, hub, currentChannel, level);
    }

    private static void renderHubWireframe(PoseStack poseStack, Vec3 cameraPos, BlockPos hubPos, Level level) {
        if (!(level.getBlockEntity(hubPos) instanceof TransceiverHubBlockEntity)) {
            return;
        }

        AABB aabb = new AABB(hubPos);
        renderWireframeBox(poseStack, cameraPos, aabb, 1.0f, 1.0f, 1.0f, 0.8f);
    }

    private static void renderChannelTransceivers(PoseStack poseStack, Vec3 cameraPos, TransceiverHubBlockEntity hub, int channel, Level level) {
        // Get channel color
        int channelColorInt = hub.getChannelColor(channel);
        float red = ((channelColorInt >> 16) & 0xFF) / 255.0f;
        float green = ((channelColorInt >> 8) & 0xFF) / 255.0f;
        float blue = (channelColorInt & 0xFF) / 255.0f;

        BlockPos hubPos = hub.getBlockPos();
        int searchRange = Config.getRemoteRange();

        BlockPos.MutableBlockPos searchPos = new BlockPos.MutableBlockPos();
        for (int x = hubPos.getX() - searchRange; x <= hubPos.getX() + searchRange; x++) {
            for (int y = hubPos.getY() - searchRange; y <= hubPos.getY() + searchRange; y++) {
                for (int z = hubPos.getZ() - searchRange; z <= hubPos.getZ() + searchRange; z++) {
                    searchPos.set(x, y, z);

                    double distanceToHub = hubPos.distSqr(searchPos);
                    if (distanceToHub > searchRange * searchRange) {
                        continue;
                    }

                    BlockEntity blockEntity = level.getBlockEntity(searchPos);
                    if (blockEntity instanceof TransceiverBlockEntity transceiverEntity) {
                        if (transceiverEntity.getChannel() == channel &&
                                transceiverEntity.getBoundHubPos() != null &&
                                transceiverEntity.getBoundHubPos().equals(hubPos)) {

                            var blockState = level.getBlockState(searchPos);
                            var shape = blockState.getShape(level, searchPos);
                            AABB aabb = shape.bounds().move(searchPos);

                            renderWireframeBox(poseStack, cameraPos, aabb, red, green, blue, 0.8f);
                        }
                    }
                }
            }
        }
    }

    private static void renderWireframeBox(PoseStack poseStack, Vec3 cameraPos, AABB aabb,
                                           float red, float green, float blue, float alpha) {
        poseStack.pushPose();

        poseStack.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);

        Minecraft minecraft = Minecraft.getInstance();
        MultiBufferSource.BufferSource bufferSource = minecraft.renderBuffers().bufferSource();
        VertexConsumer buffer = bufferSource.getBuffer(RenderType.lines());

        LevelRenderer.renderLineBox(poseStack, buffer, aabb, red, green, blue, alpha);

        bufferSource.endBatch(RenderType.lines());
        poseStack.popPose();
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

    private static boolean isWireframeVisible(Minecraft mc) {
        return Config.isHudEnabled()
                && Config.shouldShowRemoteOverlay()
                && !mc.options.hideGui
                && !mc.getDebugOverlay().showDebugScreen();
    }
}