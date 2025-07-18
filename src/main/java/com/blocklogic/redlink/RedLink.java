package com.blocklogic.redlink;

import com.blocklogic.redlink.block.RLBlocks;
import com.blocklogic.redlink.block.entity.RLBlockEntities;
import com.blocklogic.redlink.client.RLItemProperties;
import com.blocklogic.redlink.client.RLKeyMappings;
import com.blocklogic.redlink.client.handler.RemoteHudHandler;
import com.blocklogic.redlink.client.handler.RemoteKeyHandler;
import com.blocklogic.redlink.component.RLDataComponents;
import com.blocklogic.redlink.item.RLCreativeTab;
import com.blocklogic.redlink.item.RLItems;
import com.blocklogic.redlink.screen.RLMenuTypes;
import com.blocklogic.redlink.screen.cusom.TransceiverHubScreen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

@Mod(RedLink.MODID)
public class RedLink {

    public static final String MODID = "redlink";

    public static final Logger LOGGER = LogUtils.getLogger();

    public RedLink(IEventBus modEventBus, ModContainer modContainer) {

        NeoForge.EVENT_BUS.register(this);

        RLDataComponents.register(modEventBus);
        RLBlocks.register(modEventBus);
        RLItems.register(modEventBus);
        RLBlockEntities.register(modEventBus);
        RLMenuTypes.register(modEventBus);
        RLCreativeTab.register(modEventBus);

        modEventBus.addListener(this::addCreative);

        Config.register(modContainer);
    }

    private void commonSetup(FMLCommonSetupEvent event) {

    }


    private void addCreative(BuildCreativeModeTabContentsEvent event) {

    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {

    }

    @EventBusSubscriber(modid = MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            NeoForge.EVENT_BUS.register(RemoteHudHandler.class);
            NeoForge.EVENT_BUS.register(RemoteKeyHandler.class);
            RLItemProperties.addCustomItemProperties();
        }

        @SubscribeEvent
        public static void registerScreens(RegisterMenuScreensEvent event) {
            event.register(RLMenuTypes.TRANSCEIVER_HUB_MENU.get(), TransceiverHubScreen::new);
        }

        @SubscribeEvent
        public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
            event.register(RLKeyMappings.CHANNEL_PREVIOUS);
            event.register(RLKeyMappings.CHANNEL_NEXT);
        }
    }
}
