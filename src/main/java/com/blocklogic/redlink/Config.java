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
    public static final String CATEGORY_HUD = "hud";

    // ========================================
    // REMOTE CONFIGURATION
    // ========================================

    public static ModConfigSpec.IntValue REMOTE_RANGE;
    public static ModConfigSpec.BooleanValue REMOTE_SHOW_OVERLAY;

    // ========================================
    // TRANSCEIVER CONFIGURATION
    // ========================================

    public static ModConfigSpec.IntValue MAX_TRANSCEIVERS_PER_CHANNEL;
    public static ModConfigSpec.IntValue DEFAULT_PULSE_FREQUENCY;
    public static ModConfigSpec.IntValue MIN_PULSE_FREQUENCY;
    public static ModConfigSpec.IntValue MAX_PULSE_FREQUENCY;

    // ========================================
    // CHANNEL CONFIGURATION
    // ========================================

    public static ModConfigSpec.IntValue MAX_CHANNEL_NAME_LENGTH;

    // ========================================
    // HUD CONFIGURATION
    // ========================================

    public static ModConfigSpec.BooleanValue HUD_ENABLED;
    public static ModConfigSpec.BooleanValue HUD_SHOW_OVER_CHAT;
    public static ModConfigSpec.DoubleValue HUD_ANIMATION_SPEED;
    public static ModConfigSpec.IntValue HUD_OFFSET_X;
    public static ModConfigSpec.IntValue HUD_OFFSET_Y;

    public static void register(ModContainer container) {
        registerCommonConfigs(container);
    }

    private static void registerCommonConfigs(ModContainer container) {
        remoteConfig();
        transceiverConfig();
        channelConfig();
        hudConfig();
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

        COMMON_BUILDER.pop();
    }

    private static void channelConfig() {
        COMMON_BUILDER.comment("Channel and Network Settings").push(CATEGORY_CHANNELS);

        MAX_CHANNEL_NAME_LENGTH = COMMON_BUILDER.comment("Maximum length for channel names")
                .defineInRange("max_name_length", 32, 8, 128);

        COMMON_BUILDER.pop();
    }

    private static void hudConfig() {
        COMMON_BUILDER.comment("HUD Settings").push(CATEGORY_HUD);

        HUD_ENABLED = COMMON_BUILDER.comment("Enable the Remote HUD overlay")
                .define("enabled", true);

        HUD_SHOW_OVER_CHAT = COMMON_BUILDER.comment("Show HUD even when chat is open")
                .define("show_over_chat", false);

        HUD_ANIMATION_SPEED = COMMON_BUILDER.comment("HUD slide animation speed (higher = faster)")
                .defineInRange("animation_speed", 0.1, 0.01, 0.5);

        HUD_OFFSET_X = COMMON_BUILDER.comment("Horizontal offset for HUD position")
                .defineInRange("offset_x", 0, -100, 100);

        HUD_OFFSET_Y = COMMON_BUILDER.comment("Vertical offset for HUD position")
                .defineInRange("offset_y", 0, -100, 100);

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

    // ========================================
    // GETTER METHODS FOR CHANNEL SETTINGS
    // ========================================

    public static int getMaxChannelNameLength() {
        return MAX_CHANNEL_NAME_LENGTH.get();
    }

    // ========================================
    // GETTER METHODS FOR HUD SETTINGS
    // ========================================

    public static boolean isHudEnabled() {
        return HUD_ENABLED.get();
    }

    public static boolean shouldShowHudOverChat() {
        return HUD_SHOW_OVER_CHAT.get();
    }

    public static double getHudAnimationSpeed() {
        return HUD_ANIMATION_SPEED.get();
    }

    public static int getHudOffsetX() {
        return HUD_OFFSET_X.get();
    }

    public static int getHudOffsetY() {
        return HUD_OFFSET_Y.get();
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

        LOGGER.info("Transceiver Configuration:");
        LOGGER.info("  Max per Channel: {}", getMaxTransceiversPerChannel());
        LOGGER.info("  Default Pulse Frequency: {} ticks", getDefaultPulseFrequency());
        LOGGER.info("  Pulse Frequency Range: {}-{} ticks", getMinPulseFrequency(), getMaxPulseFrequency());

        LOGGER.info("Channel Configuration:");
        LOGGER.info("  Max Channel Name Length: {} characters", getMaxChannelNameLength());

        LOGGER.info("HUD Configuration:");
        LOGGER.info("  HUD Enabled: {}", isHudEnabled());
        LOGGER.info("  Show Over Chat: {}", shouldShowHudOverChat());
        LOGGER.info("  Animation Speed: {}", getHudAnimationSpeed());
        LOGGER.info("  Position Offset: X={}, Y={}", getHudOffsetX(), getHudOffsetY());
    }
}