package com.blocklogic.redlink.screen.cusom;

import com.blocklogic.redlink.block.entity.TransceiverHubBlockEntity;
import com.blocklogic.redlink.screen.RLMenuTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class TranceiverHubMenu extends AbstractContainerMenu {
    public TransceiverHubBlockEntity blockEntity;

    public TranceiverHubMenu(int containerId, Inventory inv, FriendlyByteBuf data) {
        this(containerId, inv.player.level(), data.readBlockPos());
    }

    public TranceiverHubMenu(int containerId, Level level, BlockPos pos) {
        super(RLMenuTypes.TRANSCEIVER_HUB_MENU.get(), containerId);
        if (level.getBlockEntity(pos) instanceof TransceiverHubBlockEntity transceiverHubBlockEntity) {
            this.blockEntity = transceiverHubBlockEntity;
        } else {
            throw new IllegalStateException("Expected TransceiverHubBlockEntity at " + pos + " but found something else.");
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int i) {
        return null;
    }

    @Override
    public boolean stillValid(Player player) {
        return blockEntity != null && player.distanceToSqr(blockEntity.getBlockPos().getCenter()) < 64;
    }
}
