package com.starmaylight.arcanotech_works.compat.jei;

import com.starmaylight.arcanotech_works.Arcanotech_works;
import com.starmaylight.arcanotech_works.recipe.CompressorRecipe;
import com.starmaylight.arcanotech_works.recipe.CrusherRecipe;
import com.starmaylight.arcanotech_works.recipe.MixerRecipe;
import com.starmaylight.arcanotech_works.recipe.ModRecipes;
import com.starmaylight.arcanotech_works.recipe.RollingMillRecipe;
import com.starmaylight.arcanotech_works.registry.ModBlocks;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeManager;

import java.util.List;

/**
 * JEI連携プラグイン
 */
@JeiPlugin
public class ArcanotechJEIPlugin implements IModPlugin {

    public static final ResourceLocation PLUGIN_ID = new ResourceLocation(Arcanotech_works.MODID, "jei_plugin");

    @Override
    public ResourceLocation getPluginUid() {
        return PLUGIN_ID;
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(
                new CrusherRecipeCategory(registration.getJeiHelpers().getGuiHelper()),
                new CompressorRecipeCategory(registration.getJeiHelpers().getGuiHelper()),
                new RollingMillRecipeCategory(registration.getJeiHelpers().getGuiHelper()),
                new MixerRecipeCategory(registration.getJeiHelpers().getGuiHelper())
        );
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        RecipeManager recipeManager = Minecraft.getInstance().level.getRecipeManager();

        // 粉砕機レシピ
        List<CrusherRecipe> crusherRecipes = recipeManager.getAllRecipesFor(ModRecipes.CRUSHER_TYPE.get());
        registration.addRecipes(CrusherRecipeCategory.RECIPE_TYPE, crusherRecipes);

        // 圧縮機レシピ
        List<CompressorRecipe> compressorRecipes = recipeManager.getAllRecipesFor(ModRecipes.COMPRESSOR_TYPE.get());
        registration.addRecipes(CompressorRecipeCategory.RECIPE_TYPE, compressorRecipes);

        // 圧延機レシピ
        List<RollingMillRecipe> rollingMillRecipes = recipeManager.getAllRecipesFor(ModRecipes.ROLLING_MILL_TYPE.get());
        registration.addRecipes(RollingMillRecipeCategory.RECIPE_TYPE, rollingMillRecipes);

        // 混合機レシピ
        List<MixerRecipe> mixerRecipes = recipeManager.getAllRecipesFor(ModRecipes.MIXER_TYPE.get());
        registration.addRecipes(MixerRecipeCategory.RECIPE_TYPE, mixerRecipes);
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.CRUSHER.get()), CrusherRecipeCategory.RECIPE_TYPE);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.COMPRESSOR.get()), CompressorRecipeCategory.RECIPE_TYPE);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.ROLLING_MILL.get()), RollingMillRecipeCategory.RECIPE_TYPE);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.MIXER.get()), MixerRecipeCategory.RECIPE_TYPE);
    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {
        JEIHelper.setJeiRuntime(jeiRuntime);
    }
}
