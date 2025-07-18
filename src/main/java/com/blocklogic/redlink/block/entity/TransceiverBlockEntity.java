package com.blocklogic.redlink.block.entity;

import com.blocklogic.redlink.Config;
import com.blocklogic.redlink.block.custom.TransceiverBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Vector3f;

public class TransceiverBlockEntity extends BlockEntity {
    private int channel = -1;
    private BlockPos boundHubPos = null;
    private boolean isToggleMode = true;
    private boolean isActive = false;
    private int pulseTicksRemaining = 0;

    public TransceiverBlockEntity(BlockPos pos, BlockState blockState) {
        super(RLBlockEntities.TRANSCEIVER_BLOCK_ENTITY.get(), pos, blockState);
    }

    public int getChannel() {
        return channel;
    }

    public void setChannel(int channel) {
        if (channel >= -1 && channel < 8) {
            this.channel = channel;
            setChanged();
            forceClientSync();
        }
    }

    public boolean isLinked() {
        return channel >= 0 && channel < 8 && boundHubPos != null;
    }

    public void unlink() {
        TransceiverHubBlockEntity oldHub = null;

        if (boundHubPos != null && level != null) {
            BlockEntity blockEntity = level.getBlockEntity(boundHubPos);
            if (blockEntity instanceof TransceiverHubBlockEntity hubEntity) {
                oldHub = hubEntity;
            }
        }

        this.channel = -1;
        this.boundHubPos = null;
        this.isActive = false;
        this.pulseTicksRemaining = 0;
        setChanged();
        forceClientSync();

        if (level != null && !level.isClientSide() && oldHub != null) {
            oldHub.invalidateCountCache();
        }
    }

    public BlockPos getBoundHubPos() {
        return boundHubPos;
    }

    public void setBoundHubPos(BlockPos hubPos) {
        this.boundHubPos = hubPos;
        setChanged();
        forceClientSync();
    }

    public TransceiverHubBlockEntity getBoundHub() {
        if (boundHubPos == null || level == null) {
            return null;
        }

        BlockEntity blockEntity = level.getBlockEntity(boundHubPos);
        if (!(blockEntity instanceof TransceiverHubBlockEntity hubEntity)) {
            unlink();
            return null;
        }

        return hubEntity;
    }

    public boolean isHubAccessible() {
        TransceiverHubBlockEntity hub = getBoundHub();
        if (hub == null) {
            return false;
        }

        return true;
    }

    public boolean linkToHub(BlockPos hubPos, int channel) {
        if (level == null || hubPos == null) {
            return false;
        }

        BlockEntity blockEntity = level.getBlockEntity(hubPos);
        if (!(blockEntity instanceof TransceiverHubBlockEntity hub)) {
            return false;
        }

        if (channel < 0 || channel >= 8) {
            return false;
        }

        if (this.boundHubPos != null && this.boundHubPos.equals(hubPos) && this.channel == channel) {
            return true;
        }

        if (!hub.canAddTransceiverToChannel(channel)) {
            return false;
        }

        TransceiverHubBlockEntity oldHub = getBoundHub();

        if (this.boundHubPos != null || this.channel != -1) {
            unlink();
        }

        this.boundHubPos = hubPos;
        this.channel = channel;
        this.isActive = false;
        this.pulseTicksRemaining = 0;
        setChanged();

        if (level != null && !level.isClientSide()) {
            if (level.getBlockState(getBlockPos()).getBlock() instanceof TransceiverBlock transceiverBlock) {
                transceiverBlock.updateBlockState(level, getBlockPos(), this);
            }

            forceClientSync();

            hub.invalidateCountCache();
            if (oldHub != null && !oldHub.equals(hub)) {
                oldHub.invalidateCountCache();
            }
        }

        return true;
    }

    public boolean isToggleMode() {
        return isToggleMode;
    }

    public boolean isPulseMode() {
        return !isToggleMode;
    }

    public void setMode(boolean toggleMode) {
        this.isToggleMode = toggleMode;

        this.isActive = false;
        this.pulseTicksRemaining = 0;

        setChanged();
        forceClientSync();
    }

    public void switchMode() {
        setMode(!isToggleMode);
    }

    public Component getModeDisplayName() {
        return Component.translatable(isToggleMode ?
                "redlink.transceiver.mode.toggle" :
                "redlink.transceiver.mode.pulse");
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        boolean wasActive = this.isActive;

        if (isToggleMode) {
            this.isActive = active;
            this.pulseTicksRemaining = 0;
        } else {
            if (active) {
                this.isActive = true;
                TransceiverHubBlockEntity hub = getBoundHub();
                int pulseFrequency = hub != null ? hub.getPulseFrequency(channel) : Config.getDefaultPulseFrequency();
                this.pulseTicksRemaining = pulseFrequency;
            } else {
                this.isActive = false;
                this.pulseTicksRemaining = 0;
            }
        }

        if (wasActive != this.isActive) {
            setChanged();
            forceClientSync();

            if (level != null && !level.isClientSide()) {
                if (level.getBlockState(getBlockPos()).getBlock() instanceof TransceiverBlock transceiverBlock) {
                    transceiverBlock.updateBlockState(level, getBlockPos(), this);
                }
            }
        }
    }

    public int getRedstoneSignal() {
        return isActive ? 15 : 0;
    }

    public void tick() {
        if (level == null) {
            return;
        }

        if (!level.isClientSide()) {
            serverTick();
        } else {
            clientTick();
        }
    }

    private void serverTick() {
        boolean wasActive = this.isActive;

        if (isPulseMode() && pulseTicksRemaining > 0) {
            pulseTicksRemaining--;

            if (pulseTicksRemaining <= 0) {
                this.isActive = !this.isActive;

                TransceiverHubBlockEntity hub = getBoundHub();
                int pulseFrequency = hub != null ? hub.getPulseFrequency(channel) : Config.getDefaultPulseFrequency();
                this.pulseTicksRemaining = pulseFrequency;

                setChanged();
                forceClientSync();
            }
        }

        if (wasActive != this.isActive) {
            if (level.getBlockState(getBlockPos()).getBlock() instanceof TransceiverBlock transceiverBlock) {
                transceiverBlock.updateBlockState(level, getBlockPos(), this);
            }
        }

        if (level.getGameTime() % 20 == 0 && isLinked()) {
            if (!isHubAccessible()) {
                unlink();
            }
        }
    }

    private void clientTick() {
        if (isActive && isLinked()) {
            spawnParticles();
        }
    }

    private void spawnParticles() {
        if (level == null || !level.isClientSide()) {
            return;
        }

        TransceiverHubBlockEntity hub = getBoundHub();
        if (hub == null) {
            return;
        }

        int channelColorInt = hub.getChannelColor(channel);
        float red = ((channelColorInt >> 16) & 0xFF) / 255.0f;
        float green = ((channelColorInt >> 8) & 0xFF) / 255.0f;
        float blue = (channelColorInt & 0xFF) / 255.0f;

        Vector3f color = new Vector3f(red, green, blue);
        DustParticleOptions dustOptions = new DustParticleOptions(color, 0.5f);

        RandomSource random = level.getRandom();
        BlockPos pos = getBlockPos();
        BlockState state = level.getBlockState(pos);
        Direction facing = state.getValue(TransceiverBlock.FACING);

        double baseX = pos.getX() + 0.5;
        double baseY = pos.getY() + 0.5;
        double baseZ = pos.getZ() + 0.5;

        double offsetX = 0;
        double offsetY = 0;
        double offsetZ = 0;

        double outward = -0.3;
        switch (facing) {
            case UP -> offsetY = -outward;
            case DOWN -> offsetY = outward;
            case NORTH -> offsetZ = outward;
            case SOUTH -> offsetZ = -outward;
            case WEST -> offsetX = outward;
            case EAST -> offsetX = -outward;
        }

        double spawnX = baseX + offsetX;
        double spawnY = baseY + offsetY;
        double spawnZ = baseZ + offsetZ;

        double jitterRange = 0.15;

        if (isToggleMode) {
            if (random.nextFloat() < 0.15f) {
                double x = spawnX + (random.nextDouble() - 0.5) * jitterRange;
                double y = spawnY + (random.nextDouble() - 0.5) * jitterRange;
                double z = spawnZ + (random.nextDouble() - 0.5) * jitterRange;

                double motionX = (random.nextDouble() - 0.5) * 0.01;
                double motionY = random.nextDouble() * 0.005;
                double motionZ = (random.nextDouble() - 0.5) * 0.01;

                level.addParticle(dustOptions, x, y, z, motionX, motionY, motionZ);
            }
        } else {
            int pulseFrequency = hub.getPulseFrequency(channel);
            float pulseProgress = (float) pulseTicksRemaining / pulseFrequency;
            int particleCount = pulseProgress > 0.8f ? 1 + random.nextInt(2) : (random.nextFloat() < 0.05f ? 1 : 0);

            for (int i = 0; i < particleCount; i++) {
                double x = spawnX + (random.nextDouble() - 0.5) * jitterRange;
                double y = spawnY + (random.nextDouble() - 0.5) * jitterRange;
                double z = spawnZ + (random.nextDouble() - 0.5) * jitterRange;

                double motionX = (random.nextDouble() - 0.5) * 0.01;
                double motionY = random.nextDouble() * 0.005;
                double motionZ = (random.nextDouble() - 0.5) * 0.01;

                level.addParticle(dustOptions, x, y, z, motionX, motionY, motionZ);
            }
        }
    }


    private void forceClientSync() {
        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);

            if (level instanceof ServerLevel serverLevel) {
                serverLevel.getChunkSource().blockChanged(getBlockPos());
            }
        }
    }

    public Component getChannelInfo() {
        if (!isLinked()) {
            return Component.translatable("redlink.transceiver.unlinked");
        }

        TransceiverHubBlockEntity hub = getBoundHub();
        if (hub == null) {
            return Component.translatable("redlink.transceiver.hub_missing");
        }

        String channelName = hub.getChannelName(channel);
        int channelColor = hub.getChannelColor(channel);

        Component channelComponent = Component.literal(channelName)
                .withStyle(style -> style.withColor(channelColor));

        return Component.translatable("redlink.transceiver.linked_to", channelComponent);
    }

    public Component getStatusInfo() {
        if (!isLinked()) {
            return Component.translatable("redlink.transceiver.status.unlinked");
        }

        String modeKey = isToggleMode ? "toggle" : "pulse";
        String activeKey = isActive ? "active" : "inactive";

        return Component.translatable("redlink.transceiver.status.format",
                Component.translatable("redlink.transceiver.mode." + modeKey),
                Component.translatable("redlink.transceiver.state." + activeKey));
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);

        tag.putInt("Channel", channel);
        tag.putBoolean("IsToggleMode", isToggleMode);
        tag.putBoolean("IsActive", isActive);
        tag.putInt("PulseTicksRemaining", pulseTicksRemaining);

        if (boundHubPos != null) {
            tag.putLong("BoundHubPos", boundHubPos.asLong());
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);

        this.channel = tag.getInt("Channel");
        this.isToggleMode = tag.getBoolean("IsToggleMode");
        this.isActive = tag.getBoolean("IsActive");
        this.pulseTicksRemaining = tag.getInt("PulseTicksRemaining");

        if (tag.contains("BoundHubPos")) {
            this.boundHubPos = BlockPos.of(tag.getLong("BoundHubPos"));
        } else {
            this.boundHubPos = null;
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

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}