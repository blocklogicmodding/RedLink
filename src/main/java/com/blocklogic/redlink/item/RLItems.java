package com.blocklogic.redlink.item;

import com.blocklogic.redlink.RedLink;
import com.blocklogic.redlink.item.custom.RedstoneRemoteItem;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class RLItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(RedLink.MODID);

    public static final DeferredItem<Item> REDSTONE_REMOTE = ITEMS.register("redstone_remote",
            () -> new RedstoneRemoteItem(new Item.Properties()));

    public static void register (IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
