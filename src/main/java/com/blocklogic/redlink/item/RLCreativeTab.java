package com.blocklogic.redlink.item;

import com.blocklogic.redlink.RedLink;
import com.blocklogic.redlink.block.RLBlocks;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class RLCreativeTab {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TAB = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, RedLink.MODID);

    public static final Supplier<CreativeModeTab> REDLINK = CREATIVE_MODE_TAB.register("redlink",
            () -> CreativeModeTab.builder().icon(() -> new ItemStack(RLItems.REDSTONE_REMOTE.get()))
                    .title(Component.translatable("creativetab.redlink"))
                    .displayItems((ItemDisplayParameters, output) -> {
                        output.accept(RLItems.REDSTONE_REMOTE);

                        output.accept(RLBlocks.TRANSCEIVER_HUB);
                        output.accept(RLBlocks.TRANSCEIVER);
                    }).build());

    public static void register (IEventBus eventBus) {
        CREATIVE_MODE_TAB.register(eventBus);
    }
}
