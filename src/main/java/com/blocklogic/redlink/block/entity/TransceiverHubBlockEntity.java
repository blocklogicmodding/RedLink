package com.blocklogic.redlink.block.entity;

import com.blocklogic.redlink.Config;
import com.blocklogic.redlink.component.ChannelData;
import com.blocklogic.redlink.network.RLNetworkHandler;
import com.blocklogic.redlink.screen.cusom.TransceiverHubMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class TransceiverHubBlockEntity extends BlockEntity implements MenuProvider {
    private ChannelData channelData = ChannelData.DEFAULT;
    private String hubName = "Transceiver Hub";

    private int[] cachedCounts = null;
    private long lastCountUpdate = 0;
    private static final long COUNT_CACHE_TIME = 20;

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

    public String getHubName() {
        return hubName;
    }

    public void setHubName(String name) {
        this.hubName = Config.sanitizeChannelName(name);
        setChanged();
        syncToClients();
    }

    public boolean isValidHubName(String name) {
        return Config.isValidChannelName(name);
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
        this.hubName = "Transceiver Hub";
        setChannelData(ChannelData.DEFAULT);
    }

    public void resetChannel(int channel) {
        setChannelData(channelData.resetChannel(channel));
    }

    public int getTransceiverCount(int channel) {
        if (!isValidChannel(channel) || level == null) {
            return 0;
        }

        long currentTime = level.getGameTime();
        if (cachedCounts != null && (currentTime - lastCountUpdate) < COUNT_CACHE_TIME) {
            return cachedCounts[channel];
        }

        cachedCounts = new int[8];
        int searchRange = Config.getRemoteRange();
        BlockPos hubPos = getBlockPos();

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
                        int transceiverChannel = transceiverEntity.getChannel();
                        if (transceiverChannel >= 0 && transceiverChannel < 8 &&
                                transceiverEntity.getBoundHubPos() != null &&
                                transceiverEntity.getBoundHubPos().equals(hubPos)) {
                            cachedCounts[transceiverChannel]++;
                        }
                    }
                }
            }
        }

        lastCountUpdate = currentTime;
        return cachedCounts[channel];
    }

    public boolean canAddTransceiverToChannel(int channel) {
        if (!isValidChannel(channel)) {
            return false;
        }

        int currentCount = getTransceiverCount(channel);
        int maxTransceivers = Config.getMaxTransceiversPerChannel();

        return currentCount < maxTransceivers;
    }

    public List<TransceiverBlockEntity> getChannelTransceivers(int channel) {
        List<TransceiverBlockEntity> transceivers = new ArrayList<>();

        if (!isValidChannel(channel) || level == null) {
            return transceivers;
        }

        int searchRange = Config.getRemoteRange();
        BlockPos hubPos = getBlockPos();

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
                            transceivers.add(transceiverEntity);
                        }
                    }
                }
            }
        }

        return transceivers;
    }

    public int[] getAllChannelCounts() {
        int[] counts = new int[8];
        for (int i = 0; i < 8; i++) {
            counts[i] = getTransceiverCount(i);
        }
        return counts;
    }

    public void invalidateCountCache() {
        cachedCounts = null;
        if (level != null && !level.isClientSide()) {
            syncChannelCountsToNearbyPlayers();
        }
    }

    public void syncChannelCountsToNearbyPlayers() {
        if (level != null && !level.isClientSide()) {
            int[] counts = getAllChannelCounts();
            RLNetworkHandler.syncChannelCountsToNearbyPlayers((ServerLevel) level, getBlockPos(), counts);
        }
    }

    public void syncChannelCountsToPlayer(ServerPlayer player) {
        if (level != null && !level.isClientSide()) {
            int[] counts = getAllChannelCounts();
            RLNetworkHandler.syncChannelCountsToPlayer(player, getBlockPos(), counts);
        }
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
        tag.putString("HubName", hubName);
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

        if (tag.contains("HubName")) {
            this.hubName = Config.sanitizeChannelName(tag.getString("HubName"));
        } else {
            this.hubName = "Transceiver Hub";
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

    public boolean isValidChannelName(String name) {
        return Config.isValidChannelName(name);
    }

    public boolean isValidPulseFrequency(int frequency) {
        return Config.isValidPulseFrequency(frequency);
    }
}