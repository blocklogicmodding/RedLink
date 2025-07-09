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
                .pattern("RIR")
                .pattern("GBG")
                .pattern("RGR")
                .define('R', Items.REDSTONE_BLOCK)
                .define('I', Items.SMOOTH_STONE)
                .define('B', Items.BOOK)
                .define('G', Tags.Items.GLASS_BLOCKS)
                .unlockedBy("has_redstone", has(Items.REDSTONE))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, RLBlocks.TRANSCEIVER.get())
                .pattern("   ")
                .pattern("RLR")
                .pattern("SSS")
                .define('R', Items.REDSTONE)
                .define('L', Items.LEVER)
                .define('S', Tags.Items.STONES)
                .unlockedBy("has_redstone", has(Items.REDSTONE))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, RLItems.REDSTONE_REMOTE.get())
                .pattern(" I ")
                .pattern("RDR")
                .pattern(" S ")
                .define('R', Items.REDSTONE)
                .define('D', Items.DIAMOND)
                .define('S', Items.STICK)
                .define('I', Items.IRON_INGOT)
                .unlockedBy("has_iron_ingot", has(Items.IRON_INGOT))
                .save(recipeOutput);
    }
}
