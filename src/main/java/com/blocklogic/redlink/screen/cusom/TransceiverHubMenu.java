package com.blocklogic.redlink.screen.cusom;

import com.blocklogic.redlink.block.entity.TransceiverHubBlockEntity;
import com.blocklogic.redlink.screen.RLMenuTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class TransceiverHubMenu extends AbstractContainerMenu {
    public TransceiverHubBlockEntity blockEntity;

    public TransceiverHubMenu(int containerId, Inventory inv, FriendlyByteBuf data) {
        this(containerId, inv, inv.player.level(), data.readBlockPos());
    }

    public TransceiverHubMenu(int containerId, Inventory playerInventory, Level level, BlockPos pos) {
        super(RLMenuTypes.TRANSCEIVER_HUB_MENU.get(), containerId);
        if (level.getBlockEntity(pos) instanceof TransceiverHubBlockEntity transceiverHubBlockEntity) {
            this.blockEntity = transceiverHubBlockEntity;
        } else {
            throw new IllegalStateException("Expected TransceiverHubBlockEntity at " + pos + " but found something else.");
        }

        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 158 + row * 18));
            }
        }

        for (int col = 0; col < 9; ++col) {
            this.addSlot(new Slot(playerInventory, col, 8 + col * 18, 217));
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
