package com.blocklogic.redlink.client;

import com.blocklogic.redlink.RedLink;
import com.blocklogic.redlink.component.RLDataComponents;
import com.blocklogic.redlink.item.RLItems;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;

public class RLItemProperties {
    public static void addCustomItemProperties () {
        ItemProperties.register(RLItems.REDSTONE_REMOTE.get(), ResourceLocation.fromNamespaceAndPath(RedLink.MODID, "linked"),
                ((itemStack, clientLevel, livingEntity, i) -> itemStack.get(RLDataComponents.REMOTE_HUB_POS) != null ? 1F : 0F));
    }
}
