package com.blocklogic.redlink.block.entity;

import com.blocklogic.redlink.screen.cusom.TransceiverHubMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class TransceiverHubBlockEntity extends BlockEntity implements MenuProvider {
    public TransceiverHubBlockEntity(BlockPos pos, BlockState blockState) {
        super(RLBlockEntities.TRANSCEIVER_HUB_BLOCK_ENTITY.get(), pos, blockState);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("gui.redlink.fluid_collector");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int i, Inventory inventory, Player player) {
        return null;
    }
}
