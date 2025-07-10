package com.blocklogic.redlink.block.entity;

import com.blocklogic.redlink.Config;
import com.blocklogic.redlink.component.ChannelData;
import com.blocklogic.redlink.screen.cusom.TransceiverHubMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class TransceiverHubBlockEntity extends BlockEntity implements MenuProvider {
    private ChannelData channelData = ChannelData.DEFAULT;

    public TransceiverHubBlockEntity(BlockPos pos, BlockState blockState) {
        super(RLBlockEntities.TRANSCEIVER_HUB_BLOCK_ENTITY.get(), pos, blockState);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("gui.redlink.transceiver_hub");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new TransceiverHubMenu(containerId, inventory, this);
    }

    public ChannelData getChannelData() {
        return channelData;
    }

    public void setChannelData(ChannelData newData) {
        this.channelData = newData;
        setChanged();
        syncToClients();
    }

    public String getChannelName(int channel) {
        return channelData.getChannelName(channel);
    }

    public void setChannelName(int channel, String name) {
        setChannelData(channelData.withChannelName(channel, name));
    }

    public int getPulseFrequency(int channel) {
        return channelData.getPulseFrequency(channel);
    }

    public void setPulseFrequency(int channel, int frequency) {
        setChannelData(channelData.withPulseFrequency(channel, frequency));
    }

    public int getChannelColor(int channel) {
        return channelData.getChannelColor(channel);
    }

    public boolean isValidChannel(int channel) {
        return channelData.isValidChannel(channel);
    }

    public void resetChannelData() {
        setChannelData(ChannelData.DEFAULT);
    }

    public void resetChannel(int channel) {
        setChannelData(channelData.resetChannel(channel));
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);

        CompoundTag channelTag = new CompoundTag();

        for (int i = 0; i < 8; i++) {
            channelTag.putString("name_" + i, channelData.getChannelName(i));
        }

        for (int i = 0; i < 8; i++) {
            channelTag.putInt("freq_" + i, channelData.getPulseFrequency(i));
        }

        tag.put("ChannelData", channelTag);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);

        if (tag.contains("ChannelData")) {
            CompoundTag channelTag = tag.getCompound("ChannelData");

            String[] names = new String[8];
            int[] frequencies = new int[8];

            for (int i = 0; i < 8; i++) {
                names[i] = channelTag.getString("name_" + i);
                if (names[i].isEmpty()) {
                    names[i] = "Channel " + (i + 1);
                }
            }

            for (int i = 0; i < 8; i++) {
                if (channelTag.contains("freq_" + i)) {
                    frequencies[i] = channelTag.getInt("freq_" + i);
                } else {
                    frequencies[i] = Config.getDefaultPulseFrequency();
                }
            }

            this.channelData = new ChannelData(names, frequencies);
        } else {
            this.channelData = ChannelData.DEFAULT;
        }
    }

    private void syncToClients() {
        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
        }
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
        saveAdditional(tag, registries);
        return tag;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag, HolderLookup.Provider registries) {
        super.handleUpdateTag(tag, registries);
        loadAdditional(tag, registries);
    }

    public boolean canBindRemote(Player player) {
        return true;
    }

    public Component getChannelInfo(int channel) {
        if (!isValidChannel(channel)) {
            return Component.translatable("redlink.channel.invalid");
        }

        String name = getChannelName(channel);
        int frequency = getPulseFrequency(channel);

        return Component.literal(name + " (" + frequency + " ticks)");
    }

    public boolean isValidChannelName(String name) {
        return Config.isValidChannelName(name);
    }

    public boolean isValidPulseFrequency(int frequency) {
        return Config.isValidPulseFrequency(frequency);
    }
}