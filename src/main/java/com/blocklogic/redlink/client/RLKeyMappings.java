package com.blocklogic.redlink.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.neoforged.neoforge.client.settings.KeyConflictContext;

public class RLKeyMappings {
    public static final String KEY_CATEGORY_REDLINK = "key.categories.redlink";

    public static final KeyMapping CHANNEL_PREVIOUS = new KeyMapping(
            "key.redlink.channel_previous",
            KeyConflictContext.IN_GAME,
            InputConstants.getKey(InputConstants.KEY_LBRACKET, -1),
            KEY_CATEGORY_REDLINK
    );

    public static final KeyMapping CHANNEL_NEXT = new KeyMapping(
            "key.redlink.channel_next",
            KeyConflictContext.IN_GAME,
            InputConstants.getKey(InputConstants.KEY_RBRACKET, -1),
            KEY_CATEGORY_REDLINK
    );
}