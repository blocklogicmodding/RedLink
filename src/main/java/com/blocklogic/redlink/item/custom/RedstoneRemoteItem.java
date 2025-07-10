package com.blocklogic.redlink.item.custom;

import com.blocklogic.redlink.Config;
import com.blocklogic.redlink.block.custom.TransceiverHubBlock;
import com.blocklogic.redlink.block.entity.TransceiverBlockEntity;
import com.blocklogic.redlink.block.entity.TransceiverHubBlockEntity;
import com.blocklogic.redlink.client.ClientChannelCountCache;
import com.blocklogic.redlink.component.RLDataComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

public class RedstoneRemoteItem extends Item {
    public RedstoneRemoteItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Player player = context.getPlayer();
        ItemStack stack = context.getItemInHand();

        if (player == null) return InteractionResult.FAIL;


        checkAndNotifyAutoUnbind(stack, level, player);

        BlockState blockState = level.getBlockState(pos);
        if (blockState.getBlock() instanceof TransceiverHubBlock) {
            if (player.isShiftKeyDown()) {
                return handleHubLinking(level, pos, player, stack);
            }
        }

        return InteractionResult.PASS;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        checkAndNotifyAutoUnbind(stack, level, player);

        if (player.isShiftKeyDown()) {
            return handleChannelCycling(level, player, stack);
        }

        return handleChannelActivation(level, player, stack);
    }

    private InteractionResult handleHubLinking(Level level, BlockPos hubPos, Player player, ItemStack stack) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        BlockEntity blockEntity = level.getBlockEntity(hubPos);
        if (!(blockEntity instanceof TransceiverHubBlockEntity hubEntity)) {
            player.displayClientMessage(Component.translatable("redlink.remote.hub_not_found"), true);
            return InteractionResult.FAIL;
        }

        if (!hubEntity.canBindRemote(player)) {
            player.displayClientMessage(Component.translatable("redlink.remote.hub_access_denied"), true);
            return InteractionResult.FAIL;
        }

        double distance = player.distanceToSqr(hubPos.getX() + 0.5, hubPos.getY() + 0.5, hubPos.getZ() + 0.5);
        int maxRange = Config.getRemoteRange();
        if (distance > maxRange * maxRange) {
            player.displayClientMessage(Component.translatable("redlink.remote.hub_too_far", maxRange), true);
            return InteractionResult.FAIL;
        }

        stack.set(RLDataComponents.REMOTE_HUB_POS.get(), hubPos);

        if (!stack.has(RLDataComponents.REMOTE_CHANNEL.get())) {
            stack.set(RLDataComponents.REMOTE_CHANNEL.get(), 0);
        }

        player.displayClientMessage(Component.translatable("redlink.remote.hub_linked",
                hubPos.getX(), hubPos.getY(), hubPos.getZ()), true);

        return InteractionResult.SUCCESS;
    }

    public InteractionResultHolder<ItemStack> handleChannelCycling(Level level, Player player, ItemStack stack) {
        if (level.isClientSide()) {
            return InteractionResultHolder.success(stack);
        }

        if (!isBoundToHub(stack)) {
            player.displayClientMessage(Component.translatable("redlink.remote.not_bound"), true);
            return InteractionResultHolder.fail(stack);
        }

        TransceiverHubBlockEntity hub = getBoundHub(stack, level);
        if (hub == null) {
            player.displayClientMessage(Component.translatable("redlink.remote.hub_removed_during_use"), true);
            return InteractionResultHolder.fail(stack);
        }

        if (!isHubAccessible(stack, level, player)) {
            BlockPos hubPos = getBoundHubPos(stack);
            int maxRange = Config.getRemoteRange();
            player.displayClientMessage(Component.translatable("redlink.remote.hub_out_of_range",
                    hubPos.getX(), hubPos.getY(), hubPos.getZ(), maxRange), true);
            return InteractionResultHolder.fail(stack);
        }

        int currentChannel = getCurrentChannel(stack);
        int nextChannel = (currentChannel + 1) % 8;
        setCurrentChannel(stack, nextChannel);

        String channelName = hub.getChannelName(nextChannel);
        int channelColor = hub.getChannelColor(nextChannel);

        Component channelNameComponent = Component.literal(channelName)
                .withStyle(style -> style.withColor(channelColor));

        player.displayClientMessage(Component.translatable("redlink.remote.channel_switched",
                nextChannel + 1, channelNameComponent), true);

        return InteractionResultHolder.success(stack);
    }

    public InteractionResultHolder<ItemStack> handleChannelCyclingBackward(Level level, Player player, ItemStack stack) {
        if (level.isClientSide()) {
            return InteractionResultHolder.success(stack);
        }

        if (!isBoundToHub(stack)) {
            player.displayClientMessage(Component.translatable("redlink.remote.not_bound"), true);
            return InteractionResultHolder.fail(stack);
        }

        TransceiverHubBlockEntity hub = getBoundHub(stack, level);
        if (hub == null) {
            player.displayClientMessage(Component.translatable("redlink.remote.hub_removed_during_use"), true);
            return InteractionResultHolder.fail(stack);
        }

        if (!isHubAccessible(stack, level, player)) {
            BlockPos hubPos = getBoundHubPos(stack);
            int maxRange = Config.getRemoteRange();
            player.displayClientMessage(Component.translatable("redlink.remote.hub_out_of_range",
                    hubPos.getX(), hubPos.getY(), hubPos.getZ(), maxRange), true);
            return InteractionResultHolder.fail(stack);
        }

        int currentChannel = getCurrentChannel(stack);
        int previousChannel = (currentChannel - 1 + 8) % 8;
        setCurrentChannel(stack, previousChannel);

        String channelName = hub.getChannelName(previousChannel);
        int channelColor = hub.getChannelColor(previousChannel);

        Component channelNameComponent = Component.literal(channelName)
                .withStyle(style -> style.withColor(channelColor));

        player.displayClientMessage(Component.translatable("redlink.remote.channel_switched",
                previousChannel + 1, channelNameComponent), true);

        return InteractionResultHolder.success(stack);
    }

    private InteractionResultHolder<ItemStack> handleChannelActivation(Level level, Player player, ItemStack stack) {
        if (level.isClientSide()) {
            return InteractionResultHolder.success(stack);
        }

        if (!isBoundToHub(stack)) {
            player.displayClientMessage(Component.translatable("redlink.remote.not_bound"), true);
            return InteractionResultHolder.fail(stack);
        }

        TransceiverHubBlockEntity hub = getBoundHub(stack, level);
        if (hub == null) {
            player.displayClientMessage(Component.translatable("redlink.remote.hub_removed_during_use"), true);
            return InteractionResultHolder.fail(stack);
        }

        if (!isHubAccessible(stack, level, player)) {
            BlockPos hubPos = getBoundHubPos(stack);
            int maxRange = Config.getRemoteRange();
            player.displayClientMessage(Component.translatable("redlink.remote.hub_out_of_range",
                    hubPos.getX(), hubPos.getY(), hubPos.getZ(), maxRange), true);
            return InteractionResultHolder.fail(stack);
        }

        int currentChannel = getCurrentChannel(stack);
        String channelName = hub.getChannelName(currentChannel);
        int channelColor = hub.getChannelColor(currentChannel);

        Component channelNameComponent = Component.literal(channelName)
                .withStyle(style -> style.withColor(channelColor));

        BlockPos hubPos = getBoundHubPos(stack);
        int searchRange = Config.getRemoteRange();

        List<TransceiverBlockEntity> channelTransceivers = new ArrayList<>();

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
                        if (transceiverEntity.getChannel() == currentChannel &&
                                transceiverEntity.getBoundHubPos() != null &&
                                transceiverEntity.getBoundHubPos().equals(hubPos)) {
                            channelTransceivers.add(transceiverEntity);
                        }
                    }
                }
            }
        }

        if (channelTransceivers.isEmpty()) {
            player.displayClientMessage(Component.translatable("redlink.remote.no_transceivers_found",
                    channelNameComponent), true);
            return InteractionResultHolder.success(stack);
        }

        int toggledCount = 0;
        for (TransceiverBlockEntity transceiver : channelTransceivers) {
            boolean wasActive = transceiver.isActive();
            transceiver.setActive(!wasActive);
            toggledCount++;
        }

        player.displayClientMessage(Component.translatable("redlink.remote.channel_toggled",
                channelNameComponent, toggledCount), true);

        return InteractionResultHolder.success(stack);
    }

    public boolean isBoundToHub(ItemStack stack) {
        return stack.has(RLDataComponents.REMOTE_HUB_POS.get());
    }

    public BlockPos getBoundHubPos(ItemStack stack) {
        return stack.get(RLDataComponents.REMOTE_HUB_POS.get());
    }

    public TransceiverHubBlockEntity getBoundHub(ItemStack stack, Level level) {
        if (!isBoundToHub(stack)) {
            return null;
        }

        BlockPos hubPos = getBoundHubPos(stack);
        if (hubPos == null) {
            unbindHub(stack);
            return null;
        }

        BlockEntity blockEntity = level.getBlockEntity(hubPos);
        if (!(blockEntity instanceof TransceiverHubBlockEntity hubEntity)) {
            unbindHub(stack);
            return null;
        }

        return hubEntity;
    }

    public boolean isHubAccessible(ItemStack stack, Level level, Player player) {
        TransceiverHubBlockEntity hub = getBoundHub(stack, level);
        if (hub == null) {
            return false;
        }

        BlockPos hubPos = getBoundHubPos(stack);
        double distance = player.distanceToSqr(hubPos.getX() + 0.5, hubPos.getY() + 0.5, hubPos.getZ() + 0.5);
        int maxRange = Config.getRemoteRange();

        boolean accessible = distance <= maxRange * maxRange && hub.canBindRemote(player);

        return accessible;
    }

    public int getCurrentChannel(ItemStack stack) {
        return stack.getOrDefault(RLDataComponents.REMOTE_CHANNEL.get(), 0);
    }

    public void setCurrentChannel(ItemStack stack, int channel) {
        if (channel >= 0 && channel < 8) {
            stack.set(RLDataComponents.REMOTE_CHANNEL.get(), channel);
        }
    }

    public void unbindHub(ItemStack stack) {
        stack.remove(RLDataComponents.REMOTE_HUB_POS.get());
        stack.remove(RLDataComponents.REMOTE_CHANNEL.get());
    }

    public boolean checkAndNotifyAutoUnbind(ItemStack stack, Level level, Player player) {
        if (!level.isClientSide() && isBoundToHub(stack)) {
            BlockPos hubPos = getBoundHubPos(stack);
            if (hubPos != null && level.getBlockEntity(hubPos) == null) {
                player.displayClientMessage(Component.translatable("redlink.remote.hub_auto_unbound"), true);
                unbindHub(stack);
                return true;
            }
        }
        return false;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);

        if (isBoundToHub(stack)) {
            BlockPos hubPos = getBoundHubPos(stack);
            if (hubPos != null) {
                Level level = context.level();
                if (level != null) {
                    TransceiverHubBlockEntity hub = getBoundHub(stack, level);
                    if (hub != null) {
                        tooltipComponents.add(Component.translatable("redlink.remote.tooltip.bound_hub_named",
                                        hub.getHubName(), hubPos.getX(), hubPos.getY(), hubPos.getZ())
                                .withStyle(ChatFormatting.DARK_GREEN).withStyle(ChatFormatting.BOLD));

                        tooltipComponents.add(Component.empty());
                        int channel = getCurrentChannel(stack);
                        tooltipComponents.add(Component.translatable("redlink.remote.tooltip.current_channel",
                                channel + 1));

                        String channelName = hub.getChannelName(channel);
                        int channelColor = hub.getChannelColor(channel);

                        Component channelComponent = Component.literal(channelName)
                                .withStyle(style -> style.withColor(channelColor));

                        tooltipComponents.add(Component.translatable("redlink.remote.tooltip.channel_name")
                                .append(": ").append(channelComponent));

                        int transceiverCount;
                        int maxTransceivers = Config.getMaxTransceiversPerChannel();

                        if (level.isClientSide() && ClientChannelCountCache.hasCachedData(hubPos)) {
                            transceiverCount = ClientChannelCountCache.getChannelCount(hubPos, channel);
                        } else {
                            transceiverCount = hub.getTransceiverCount(channel);

                            if (level.isClientSide() && !ClientChannelCountCache.hasCachedData(hubPos)) {
                                tooltipComponents.add(Component.translatable("redlink.remote.tooltip.loading_count")
                                        .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
                            }
                        }

                        ChatFormatting countColor = ChatFormatting.GREEN;
                        if (transceiverCount >= maxTransceivers) {
                            countColor = ChatFormatting.RED;
                        } else if (transceiverCount >= maxTransceivers * 0.8) {
                            countColor = ChatFormatting.YELLOW;
                        }

                        Component countComponent = Component.literal(transceiverCount + "/" + maxTransceivers)
                                .withStyle(countColor);

                        tooltipComponents.add(Component.translatable("redlink.remote.tooltip.transceiver_count")
                                .append(": ").append(countComponent));

                        if (transceiverCount >= maxTransceivers) {
                            tooltipComponents.add(Component.translatable("redlink.remote.tooltip.channel_full")
                                    .withStyle(ChatFormatting.RED, ChatFormatting.ITALIC));
                        }

                        int range = Config.getRemoteRange();
                        tooltipComponents.add(Component.translatable("redlink.remote.tooltip.range", range)
                                .withStyle(ChatFormatting.GRAY));

                    } else {
                        tooltipComponents.add(Component.translatable("redlink.remote.tooltip.bound_hub",
                                        hubPos.getX(), hubPos.getY(), hubPos.getZ())
                                .withStyle(ChatFormatting.DARK_GREEN).withStyle(ChatFormatting.BOLD));
                        tooltipComponents.add(Component.translatable("redlink.remote.tooltip.hub_unreachable")
                                .withStyle(ChatFormatting.RED));
                    }
                } else {
                    tooltipComponents.add(Component.translatable("redlink.remote.tooltip.bound_hub",
                                    hubPos.getX(), hubPos.getY(), hubPos.getZ())
                            .withStyle(ChatFormatting.DARK_GREEN).withStyle(ChatFormatting.BOLD));
                }
            }
        } else {
            tooltipComponents.add(Component.translatable("redlink.remote.tooltip.unbound")
                    .withStyle(ChatFormatting.GRAY));
            tooltipComponents.add(Component.translatable("redlink.remote.tooltip.bind_instructions")
                    .withStyle(ChatFormatting.DARK_GRAY));
        }

        tooltipComponents.add(Component.empty());
        tooltipComponents.add(Component.translatable("redlink.remote.tooltip.usage_toggle")
                .withStyle(ChatFormatting.AQUA));
        tooltipComponents.add(Component.translatable("redlink.remote.tooltip.usage_cycle")
                .withStyle(ChatFormatting.AQUA));
        tooltipComponents.add(Component.translatable("redlink.remote.tooltip.usage_keybinds")
                .withStyle(ChatFormatting.AQUA));
    }
}