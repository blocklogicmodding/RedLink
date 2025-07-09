package com.blocklogic.redlink.block.custom;

import com.blocklogic.redlink.block.entity.TransceiverHubBlockEntity;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

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
}
