package com.blocklogic.redlink.block.entity;

import com.blocklogic.redlink.RedLink;
import com.blocklogic.redlink.block.RLBlocks;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class RLBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, RedLink.MODID);

    public static final Supplier<BlockEntityType<TransceiverHubBlockEntity>> TRANSCEIVER_HUB_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("transceiver_hub_block_entity", () -> BlockEntityType.Builder.of(
                    TransceiverHubBlockEntity::new,
                    RLBlocks.TRANSCEIVER_HUB.get()
            ).build(null));

    public static final Supplier<BlockEntityType<TransceiverBlockEntity>> TRANSCEIVER_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("transceiver_block_entity", () -> BlockEntityType.Builder.of(
                    TransceiverBlockEntity::new,
                    RLBlocks.TRANSCEIVER.get()
            ).build(null));


    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}
