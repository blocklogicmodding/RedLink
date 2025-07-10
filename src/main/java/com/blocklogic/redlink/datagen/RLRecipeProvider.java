package com.blocklogic.redlink.datagen;

import com.blocklogic.redlink.block.RLBlocks;
import com.blocklogic.redlink.item.RLItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.conditions.IConditionBuilder;

import java.util.concurrent.CompletableFuture;

public class RLRecipeProvider extends RecipeProvider implements IConditionBuilder {
    public RLRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    @Override
    protected void buildRecipes(RecipeOutput recipeOutput) {
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, RLBlocks.TRANSCEIVER_HUB.get())
                .pattern("SGS")
                .pattern("GCG")
                .pattern("SRS")
                .define('R', Items.REDSTONE_BLOCK)
                .define('S', Tags.Items.STONES)
                .define('C', Items.COMPARATOR)
                .define('G', Tags.Items.GLASS_BLOCKS)
                .unlockedBy("has_redstone", has(Items.REDSTONE))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, RLBlocks.TRANSCEIVER.get(), 2)
                .pattern("   ")
                .pattern("IRI")
                .pattern("SSS")
                .define('R', Items.REDSTONE)
                .define('I', Items.IRON_INGOT)
                .define('S', Tags.Items.STONES)
                .unlockedBy("has_redstone", has(Items.REDSTONE))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, RLItems.REDSTONE_REMOTE.get())
                .pattern("IXI")
                .pattern("GDG")
                .pattern("RGR")
                .define('R', Items.REDSTONE)
                .define('X', Items.REPEATER)
                .define('D', Items.DIAMOND)
                .define('G', Tags.Items.GLASS_BLOCKS)
                .define('I', Items.IRON_INGOT)
                .unlockedBy("has_iron_ingot", has(Items.IRON_INGOT))
                .save(recipeOutput);
    }
}
