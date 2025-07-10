package com.blocklogic.redlink.client;

import net.minecraft.core.BlockPos;
import java.util.HashMap;
import java.util.Map;

public class ClientChannelCountCache {
    private static final Map<BlockPos, int[]> channelCounts = new HashMap<>();

    public static void updateCounts(BlockPos hubPos, int[] counts) {
        channelCounts.put(hubPos, counts.clone());
    }

    public static int getChannelCount(BlockPos hubPos, int channel) {
        int[] counts = channelCounts.get(hubPos);
        if (counts != null && channel >= 0 && channel < 8) {
            return counts[channel];
        }
        return 0;
    }

    public static void clearCache() {
        channelCounts.clear();
    }

    public static void removeHub(BlockPos hubPos) {
        channelCounts.remove(hubPos);
    }

    public static boolean hasCachedData(BlockPos hubPos) {
        return channelCounts.containsKey(hubPos);
    }
}