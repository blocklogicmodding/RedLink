package com.blocklogic.redlink.datagen;

import com.blocklogic.redlink.block.RLBlocks;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;

import java.util.Set;

public class RLLootTableProvider extends BlockLootSubProvider {
    protected RLLootTableProvider(HolderLookup.Provider registries) {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags(), registries);
    }

    @Override
    protected void generate() {
        dropSelf(RLBlocks.TRANSCEIVER_HUB.get());
        dropSelf(RLBlocks.TRANSCEIVER.get());
    }

    @Override
    protected Iterable<Block> getKnownBlocks() {
        return RLBlocks.BLOCKS.getEntries().stream().map(Holder::value)::iterator;
    }
}
