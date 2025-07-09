package com.blocklogic.redlink;

import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.slf4j.Logger;

public class Config {
    public static final Logger LOGGER = LogUtils.getLogger();

    public static final ModConfigSpec.Builder COMMON_BUILDER = new ModConfigSpec.Builder();
    public static ModConfigSpec COMMON_CONFIG;
    public static ModConfigSpec SPEC;

    // ========================================
    // CATEGORY CONSTANTS
    // ========================================

    public static final String CATEGORY_REMOTE = "remote";
    public static final String CATEGORY_TRANSCEIVERS = "transceivers";
    public static final String CATEGORY_CHANNELS = "channels";

    // ========================================
    // REMOTE CONFIGURATION
    // ========================================

    public static ModConfigSpec.IntValue REMOTE_RANGE;
    public static ModConfigSpec.BooleanValue REMOTE_SHOW_OVERLAY;
    public static ModConfigSpec.BooleanValue REMOTE_SHOW_PARTICLES;

    // ========================================
    // TRANSCEIVER CONFIGURATION
    // ========================================

    public static ModConfigSpec.IntValue MAX_TRANSCEIVERS_PER_CHANNEL;
    public static ModConfigSpec.IntValue DEFAULT_PULSE_FREQUENCY;
    public static ModConfigSpec.IntValue MIN_PULSE_FREQUENCY;
    public static ModConfigSpec.IntValue MAX_PULSE_FREQUENCY;
    public static ModConfigSpec.BooleanValue TRANSCEIVERS_SHOW_PARTICLES;

    // ========================================
    // CHANNEL CONFIGURATION
    // ========================================

    public static ModConfigSpec.IntValue MAX_CHANNEL_NAME_LENGTH;
    public static ModConfigSpec.BooleanValue CROSS_DIMENSIONAL_OPERATION;
    public static ModConfigSpec.BooleanValue REQUIRE_CHUNK_LOADED;

    public static void register(ModContainer container) {
        registerCommonConfigs(container);
    }

    private static void registerCommonConfigs(ModContainer container) {
        remoteConfig();
        transceiverConfig();
        channelConfig();
        COMMON_CONFIG = COMMON_BUILDER.build();
        SPEC = COMMON_CONFIG; // Legacy compatibility
        container.registerConfig(ModConfig.Type.COMMON, COMMON_CONFIG);
    }

    // ========================================
    // CONFIGURATION CATEGORY METHODS
    // ========================================

    private static void remoteConfig() {
        COMMON_BUILDER.comment("Remote Control Settings").push(CATEGORY_REMOTE);

        REMOTE_RANGE = COMMON_BUILDER.comment("Range of the Redstone Remote in blocks")
                .defineInRange("range", 16, 4, 128);

        REMOTE_SHOW_OVERLAY = COMMON_BUILDER.comment("Show the channel overlay HUD when holding the remote")
                .define("show_overlay", true);

        REMOTE_SHOW_PARTICLES = COMMON_BUILDER.comment("Show particles when remote activates transceivers")
                .define("show_particles", true);

        COMMON_BUILDER.pop();
    }

    private static void transceiverConfig() {
        COMMON_BUILDER.comment("Transceiver Settings").push(CATEGORY_TRANSCEIVERS);

        MAX_TRANSCEIVERS_PER_CHANNEL = COMMON_BUILDER.comment("Maximum number of transceivers per channel")
                .defineInRange("max_per_channel", 16, 1, 64);

        DEFAULT_PULSE_FREQUENCY = COMMON_BUILDER.comment("Default pulse frequency in ticks (20 ticks = 1 second)")
                .defineInRange("default_pulse_frequency", 20, 1, 200);

        MIN_PULSE_FREQUENCY = COMMON_BUILDER.comment("Minimum pulse frequency in ticks")
                .defineInRange("min_pulse_frequency", 1, 1, 20);

        MAX_PULSE_FREQUENCY = COMMON_BUILDER.comment("Maximum pulse frequency in ticks")
                .defineInRange("max_pulse_frequency", 200, 20, 1200);

        TRANSCEIVERS_SHOW_PARTICLES = COMMON_BUILDER.comment("Show particles from active transceivers")
                .define("show_particles", true);

        COMMON_BUILDER.pop();
    }

    private static void channelConfig() {
        COMMON_BUILDER.comment("Channel and Network Settings").push(CATEGORY_CHANNELS);

        MAX_CHANNEL_NAME_LENGTH = COMMON_BUILDER.comment("Maximum length for channel names")
                .defineInRange("max_name_length", 32, 8, 128);

        CROSS_DIMENSIONAL_OPERATION = COMMON_BUILDER.comment("Allow transceivers to work across dimensions (NOT RECOMMENDED)")
                .define("cross_dimensional", false);

        REQUIRE_CHUNK_LOADED = COMMON_BUILDER.comment("Require chunks to be loaded for transceivers to function")
                .define("require_chunk_loaded", true);

        COMMON_BUILDER.pop();
    }

    // ========================================
    // GETTER METHODS FOR REMOTE SETTINGS
    // ========================================

    public static int getRemoteRange() {
        return REMOTE_RANGE.get();
    }

    public static boolean shouldShowRemoteOverlay() {
        return REMOTE_SHOW_OVERLAY.get();
    }

    public static boolean shouldShowRemoteParticles() {
        return REMOTE_SHOW_PARTICLES.get();
    }

    // ========================================
    // GETTER METHODS FOR TRANSCEIVER SETTINGS
    // ========================================

    public static int getMaxTransceiversPerChannel() {
        return MAX_TRANSCEIVERS_PER_CHANNEL.get();
    }

    public static int getDefaultPulseFrequency() {
        return DEFAULT_PULSE_FREQUENCY.get();
    }

    public static int getMinPulseFrequency() {
        return MIN_PULSE_FREQUENCY.get();
    }

    public static int getMaxPulseFrequency() {
        return MAX_PULSE_FREQUENCY.get();
    }

    public static boolean shouldShowTransceiverParticles() {
        return TRANSCEIVERS_SHOW_PARTICLES.get();
    }

    // ========================================
    // GETTER METHODS FOR CHANNEL SETTINGS
    // ========================================

    public static int getMaxChannelNameLength() {
        return MAX_CHANNEL_NAME_LENGTH.get();
    }

    public static boolean allowCrossDimensionalOperation() {
        return CROSS_DIMENSIONAL_OPERATION.get();
    }

    public static boolean requireChunkLoaded() {
        return REQUIRE_CHUNK_LOADED.get();
    }

    // ========================================
    // VALIDATION METHODS
    // ========================================

    public static boolean isValidChannelName(String name) {
        return name != null && !name.trim().isEmpty() && name.length() <= getMaxChannelNameLength();
    }

    public static boolean isValidPulseFrequency(int frequency) {
        return frequency >= getMinPulseFrequency() && frequency <= getMaxPulseFrequency();
    }

    public static String sanitizeChannelName(String name) {
        if (name == null) return "Channel";
        String sanitized = name.trim();
        if (sanitized.isEmpty()) return "Channel";
        if (sanitized.length() > getMaxChannelNameLength()) {
            sanitized = sanitized.substring(0, getMaxChannelNameLength());
        }
        return sanitized;
    }

    public static int clampPulseFrequency(int frequency) {
        return Math.max(getMinPulseFrequency(), Math.min(getMaxPulseFrequency(), frequency));
    }

    // ========================================
    // CONFIG LOADING AND LOGGING
    // ========================================

    public static void loadConfig() {
        LOGGER.info("RedLink configs reloaded");
        logConfigValues();
    }

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        LOGGER.info("RedLink configuration loaded");
        logConfigValues();
    }

    private static void logConfigValues() {
        LOGGER.info("Remote Configuration:");
        LOGGER.info("  Range: {} blocks", getRemoteRange());
        LOGGER.info("  Show Overlay: {}", shouldShowRemoteOverlay());
        LOGGER.info("  Show Particles: {}", shouldShowRemoteParticles());

        LOGGER.info("Transceiver Configuration:");
        LOGGER.info("  Max per Channel: {}", getMaxTransceiversPerChannel());
        LOGGER.info("  Default Pulse Frequency: {} ticks", getDefaultPulseFrequency());
        LOGGER.info("  Pulse Frequency Range: {}-{} ticks", getMinPulseFrequency(), getMaxPulseFrequency());
        LOGGER.info("  Show Particles: {}", shouldShowTransceiverParticles());

        LOGGER.info("Channel Configuration:");
        LOGGER.info("  Max Channel Name Length: {} characters", getMaxChannelNameLength());
        LOGGER.info("  Cross Dimensional: {}", allowCrossDimensionalOperation());
        LOGGER.info("  Require Chunk Loaded: {}", requireChunkLoaded());
    }
}