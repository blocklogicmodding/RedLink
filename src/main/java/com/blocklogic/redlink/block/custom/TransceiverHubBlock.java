package com.blocklogic.redlink.block.custom;

import com.blocklogic.redlink.Config;
import com.blocklogic.redlink.block.entity.TransceiverHubBlockEntity;
import com.mojang.serialization.MapCodec;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class TransceiverHubBlock extends BaseEntityBlock {
    public static final MapCodec<TransceiverHubBlock> CODEC = simpleCodec(TransceiverHubBlock::new);

    public TransceiverHubBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new TransceiverHubBlockEntity(blockPos, blockState);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!level.isClientSide()) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof TransceiverHubBlockEntity hubEntity && player instanceof ServerPlayer serverPlayer) {
                serverPlayer.openMenu(hubEntity, pos);
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);

        tooltipComponents.add(Component.translatable("block.redlink.transceiver_hub.tooltip.description")
                .withStyle(ChatFormatting.GRAY));

        tooltipComponents.add(Component.empty());

        tooltipComponents.add(Component.translatable("block.redlink.transceiver_hub.tooltip.usage.bind")
                .withStyle(ChatFormatting.AQUA));

        tooltipComponents.add(Component.translatable("block.redlink.transceiver_hub.tooltip.usage.configure")
                .withStyle(ChatFormatting.AQUA));

        tooltipComponents.add(Component.empty());

        int maxTransceivers = Config.getMaxTransceiversPerChannel();
        int range = Config.getRemoteRange();

        tooltipComponents.add(Component.translatable("block.redlink.transceiver_hub.tooltip.info.channels")
                .withStyle(ChatFormatting.YELLOW));

        tooltipComponents.add(Component.translatable("block.redlink.transceiver_hub.tooltip.info.capacity", maxTransceivers)
                .withStyle(ChatFormatting.YELLOW));

        tooltipComponents.add(Component.translatable("block.redlink.transceiver_hub.tooltip.info.range", range)
                .withStyle(ChatFormatting.YELLOW));
    }
}