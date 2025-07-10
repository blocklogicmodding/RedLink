package com.blocklogic.redlink.screen.cusom;

import com.blocklogic.redlink.Config;
import com.blocklogic.redlink.block.entity.TransceiverHubBlockEntity;
import com.blocklogic.redlink.component.ChannelData;
import com.blocklogic.redlink.network.RLNetworkHandler;
import com.blocklogic.redlink.screen.RLMenuTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class TransceiverHubMenu extends AbstractContainerMenu {
    private final TransceiverHubBlockEntity hubEntity;
    private final Level level;
    private final BlockPos pos;

    public TransceiverHubMenu(int containerId, Inventory inv, FriendlyByteBuf data) {
        this(containerId, inv, inv.player.level(), data.readBlockPos());
    }

    public TransceiverHubMenu(int containerId, Inventory playerInventory, Level level, BlockPos pos) {
        super(RLMenuTypes.TRANSCEIVER_HUB_MENU.get(), containerId);
        this.level = level;
        this.pos = pos;

        if (level.getBlockEntity(pos) instanceof TransceiverHubBlockEntity hubBlockEntity) {
            this.hubEntity = hubBlockEntity;
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

        if (!level.isClientSide() && playerInventory.player instanceof ServerPlayer serverPlayer) {
            RLNetworkHandler.syncHubDataToPlayer(serverPlayer, pos, hubEntity.getChannelData());
        }
    }

    public TransceiverHubMenu(int containerId, Inventory playerInventory, TransceiverHubBlockEntity hubEntity) {
        this(containerId, playerInventory, hubEntity.getLevel(), hubEntity.getBlockPos());
    }

    public TransceiverHubBlockEntity getHubEntity() {
        return hubEntity;
    }

    public ChannelData getChannelData() {
        return hubEntity.getChannelData();
    }

    public String getChannelName(int channel) {
        return hubEntity.getChannelName(channel);
    }

    public int getPulseFrequency(int channel) {
        return hubEntity.getPulseFrequency(channel);
    }

    public int getChannelColor(int channel) {
        return hubEntity.getChannelColor(channel);
    }

    public boolean isValidChannel(int channel) {
        return hubEntity.isValidChannel(channel);
    }

    public void updateChannel(int channel, String channelName, int pulseFrequency) {
        if (level.isClientSide()) {
            RLNetworkHandler.sendHubChannelUpdate(pos, channel, channelName, pulseFrequency);
        } else {
            hubEntity.setChannelName(channel, channelName);
            hubEntity.setPulseFrequency(channel, pulseFrequency);
        }
    }

    public void resetAllChannels() {
        if (level.isClientSide()) {
            RLNetworkHandler.sendHubResetAll(pos);
        } else {
            hubEntity.resetChannelData();
        }
    }

    public boolean isValidChannelName(String name) {
        return hubEntity.isValidChannelName(name);
    }

    public boolean isValidPulseFrequency(int frequency) {
        return hubEntity.isValidPulseFrequency(frequency);
    }

    public String sanitizeChannelName(String name) {
        return Config.sanitizeChannelName(name);
    }

    public int clampPulseFrequency(int frequency) {
        return Config.clampPulseFrequency(frequency);
    }

    public String getDefaultChannelName(int channel) {
        return "Channel " + (channel + 1);
    }

    public int getMaxChannelNameLength() {
        return Config.getMaxChannelNameLength();
    }

    public int getDefaultPulseFrequency() {
        return Config.getDefaultPulseFrequency();
    }

    public int getMinPulseFrequency() {
        return Config.getMinPulseFrequency();
    }

    public int getMaxPulseFrequency() {
        return Config.getMaxPulseFrequency();
    }

    public boolean isHubAccessible() {
        return hubEntity != null && !hubEntity.isRemoved() &&
                level.getBlockEntity(pos) == hubEntity;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return null;
    }

    @Override
    public boolean stillValid(Player player) {
        return hubEntity != null && !hubEntity.isRemoved() &&
                player.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) <= 64.0;
    }
}