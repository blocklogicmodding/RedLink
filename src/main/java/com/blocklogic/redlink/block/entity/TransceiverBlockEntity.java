package com.blocklogic.redlink.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class TransceiverBlockEntity extends BlockEntity {
    public TransceiverBlockEntity(BlockPos pos, BlockState blockState) {
        super(RLBlockEntities.TRANSCEIVER_BLOCK_ENTITY.get(), pos, blockState);
    }
}
