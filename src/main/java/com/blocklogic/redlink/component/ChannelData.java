package com.blocklogic.redlink.component;

import com.blocklogic.redlink.Config;
import com.mojang.serialization.Codec;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

import java.util.Arrays;

public record ChannelData(
        String[] channelNames,
        int[] pulseFrequencies
) {
    public static final int CHANNEL_COUNT = 8;

    public static final int[] CHANNEL_COLORS = {
            0xFF0000,
            0x00FF00,
            0x0000FF,
            0xFFFF00,
            0xFF8000,
            0x8000FF,
            0x00FFFF,
            0xFF0080
    };

    public static final ChannelData DEFAULT = createDefault();

    private static ChannelData createDefault() {
        String[] defaultNames = new String[CHANNEL_COUNT];
        int[] defaultFrequencies = new int[CHANNEL_COUNT];

        for (int i = 0; i < CHANNEL_COUNT; i++) {
            defaultNames[i] = "Channel " + (i + 1);
            defaultFrequencies[i] = Config.getDefaultPulseFrequency();
        }

        return new ChannelData(defaultNames, defaultFrequencies);
    }

    public static final StreamCodec<FriendlyByteBuf, ChannelData> STREAM_CODEC =
            StreamCodec.of(ChannelData::encode, ChannelData::decode);

    private static void encode(FriendlyByteBuf buf, ChannelData data) {
        for (int i = 0; i < CHANNEL_COUNT; i++) {
            buf.writeUtf(data.channelNames[i]);
        }
        for (int i = 0; i < CHANNEL_COUNT; i++) {
            buf.writeVarInt(data.pulseFrequencies[i]);
        }
    }

    private static ChannelData decode(FriendlyByteBuf buf) {
        String[] names = new String[CHANNEL_COUNT];
        int[] frequencies = new int[CHANNEL_COUNT];

        for (int i = 0; i < CHANNEL_COUNT; i++) {
            names[i] = buf.readUtf();
        }
        for (int i = 0; i < CHANNEL_COUNT; i++) {
            frequencies[i] = buf.readVarInt();
        }

        return new ChannelData(names, frequencies);
    }

    public ChannelData {
        if (channelNames == null || channelNames.length != CHANNEL_COUNT) {
            channelNames = createDefault().channelNames;
        }
        if (pulseFrequencies == null || pulseFrequencies.length != CHANNEL_COUNT) {
            pulseFrequencies = createDefault().pulseFrequencies;
        }

        for (int i = 0; i < CHANNEL_COUNT; i++) {
            channelNames[i] = Config.sanitizeChannelName(channelNames[i]);
            pulseFrequencies[i] = Config.clampPulseFrequency(pulseFrequencies[i]);
        }
    }

    public String getChannelName(int channel) {
        if (channel < 0 || channel >= CHANNEL_COUNT) {
            return "Invalid Channel";
        }
        return channelNames[channel];
    }

    public int getPulseFrequency(int channel) {
        if (channel < 0 || channel >= CHANNEL_COUNT) {
            return Config.getDefaultPulseFrequency();
        }
        return pulseFrequencies[channel];
    }

    public int getChannelColor(int channel) {
        if (channel < 0 || channel >= CHANNEL_COUNT) {
            return 0xFFFFFF;
        }
        return CHANNEL_COLORS[channel];
    }

    public ChannelData withChannelName(int channel, String name) {
        if (channel < 0 || channel >= CHANNEL_COUNT) {
            return this;
        }

        String[] newNames = Arrays.copyOf(channelNames, CHANNEL_COUNT);
        newNames[channel] = Config.sanitizeChannelName(name);

        return new ChannelData(newNames, pulseFrequencies);
    }

    public ChannelData withPulseFrequency(int channel, int frequency) {
        if (channel < 0 || channel >= CHANNEL_COUNT) {
            return this;
        }

        int[] newFrequencies = Arrays.copyOf(pulseFrequencies, CHANNEL_COUNT);
        newFrequencies[channel] = Config.clampPulseFrequency(frequency);

        return new ChannelData(channelNames, newFrequencies);
    }

    public boolean isValidChannel(int channel) {
        return channel >= 0 && channel < CHANNEL_COUNT;
    }

    public ChannelData resetChannel(int channel) {
        if (!isValidChannel(channel)) {
            return this;
        }

        return withChannelName(channel, "Channel " + (channel + 1))
                .withPulseFrequency(channel, Config.getDefaultPulseFrequency());
    }
}