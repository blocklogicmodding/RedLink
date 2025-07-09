package com.blocklogic.redlink.datagen;

import com.blocklogic.redlink.RedLink;
import com.blocklogic.redlink.item.RLItems;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class RLItemModelProvider extends ItemModelProvider {
    public RLItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, RedLink.MODID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        basicItem(RLItems.REDSTONE_REMOTE.get());
    }
}
