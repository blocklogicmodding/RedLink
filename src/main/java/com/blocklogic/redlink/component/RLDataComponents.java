package com.blocklogic.redlink.component;

import com.blocklogic.redlink.RedLink;
import com.mojang.serialization.Codec;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.UnaryOperator;

public class RLDataComponents {
    public static final DeferredRegister<DataComponentType<?>> DATA_COMPONENT_TYPES =
            DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, RedLink.MODID);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> REMOTE_CHANNEL =
            register("remote_channel", builder -> builder
                    .persistent(Codec.INT)
                    .networkSynchronized(ByteBufCodecs.VAR_INT)
                    .cacheEncoding()
            );

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> TRANSCEIVER_CHANNEL =
            register("transceiver_channel", builder -> builder
                    .persistent(Codec.INT)
                    .networkSynchronized(ByteBufCodecs.VAR_INT)
                    .cacheEncoding()
            );

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> TRANSCEIVER_MODE =
            register("transceiver_mode", builder -> builder
                    .persistent(Codec.BOOL)
                    .networkSynchronized(ByteBufCodecs.BOOL)
                    .cacheEncoding()
            );

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> TRANSCEIVER_ACTIVE =
            register("transceiver_active", builder -> builder
                    .persistent(Codec.BOOL)
                    .networkSynchronized(ByteBufCodecs.BOOL)
                    .cacheEncoding()
            );

    private static <T> DeferredHolder<DataComponentType<?>, DataComponentType<T>> register(String name, UnaryOperator<DataComponentType.Builder<T>> builderOperator) {
        return DATA_COMPONENT_TYPES.register(name, () -> builderOperator.apply(DataComponentType.builder()).build());
    }

    public static void register(IEventBus eventBus) {
        DATA_COMPONENT_TYPES.register(eventBus);
    }
}