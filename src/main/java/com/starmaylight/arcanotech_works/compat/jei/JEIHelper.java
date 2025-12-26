package com.starmaylight.arcanotech_works.compat.jei;

import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.ModList;

import javax.annotation.Nullable;
import java.util.List;

/**
 * JEI連携ヘルパークラス
 */
public class JEIHelper {

    @Nullable
    private static IJeiRuntime jeiRuntime;

    /**
     * JEIランタイムを設定（JEIプラグインから呼び出される）
     */
    public static void setJeiRuntime(@Nullable IJeiRuntime runtime) {
        jeiRuntime = runtime;
    }

    /**
     * JEIが読み込まれているかチェック
     */
    public static boolean isJeiLoaded() {
        return ModList.get().isLoaded("jei");
    }

    /**
     * 指定したレシピタイプのレシピ一覧を表示
     */
    public static <T> void showRecipes(RecipeType<T> recipeType) {
        if (jeiRuntime != null) {
            jeiRuntime.getRecipesGui().showTypes(List.of(recipeType));
        }
    }

    /**
     * 粉砕機レシピを表示
     */
    public static void showCrusherRecipes() {
        if (isJeiLoaded() && jeiRuntime != null) {
            showRecipes(CrusherRecipeCategory.RECIPE_TYPE);
        }
    }

    /**
     * 圧縮機レシピを表示
     */
    public static void showCompressorRecipes() {
        if (isJeiLoaded() && jeiRuntime != null) {
            showRecipes(CompressorRecipeCategory.RECIPE_TYPE);
        }
    }

    /**
     * 圧延機レシピを表示
     */
    public static void showRollingMillRecipes() {
        if (isJeiLoaded() && jeiRuntime != null) {
            showRecipes(RollingMillRecipeCategory.RECIPE_TYPE);
        }
    }

    /**
     * 混合機レシピを表示
     */
    public static void showMixerRecipes() {
        if (isJeiLoaded() && jeiRuntime != null) {
            showRecipes(MixerRecipeCategory.RECIPE_TYPE);
        }
    }
}
