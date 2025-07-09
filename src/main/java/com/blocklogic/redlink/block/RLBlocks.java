package com.blocklogic.redlink.block;

import com.blocklogic.redlink.RedLink;
import com.blocklogic.redlink.block.custom.TransceiverBlock;
import com.blocklogic.redlink.block.custom.TransceiverHubBlock;
import com.blocklogic.redlink.item.RLItems;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class RLBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(RedLink.MODID);

    public static final DeferredBlock<Block> TRANSCEIVER_HUB = registerBlock("transceiver_hub",
            () -> new TransceiverHubBlock(BlockBehaviour.Properties.of()
                    .strength(3.0F)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.STONE)
                    .noOcclusion()
            ));

    public static final DeferredBlock<Block> TRANSCEIVER = registerBlock("transceiver",
            () -> new TransceiverBlock(BlockBehaviour.Properties.of()
                    .strength(3.0F)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.STONE)
                    .noOcclusion()
            ));

    private static <T extends Block> DeferredBlock<T> registerBlock(String name, Supplier<T> block) {
        DeferredBlock<T> toReturn = BLOCKS.register(name, block);
        registerBlockItem(name, toReturn);
        return toReturn;
    }

    private static <T extends Block> void registerBlockItem(String name, DeferredBlock<T> block) {
        RLItems.ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
    }

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}
