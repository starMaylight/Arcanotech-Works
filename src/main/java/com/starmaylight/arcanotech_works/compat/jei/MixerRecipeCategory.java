package com.starmaylight.arcanotech_works.compat.jei;

import com.starmaylight.arcanotech_works.Arcanotech_works;
import com.starmaylight.arcanotech_works.recipe.MixerRecipe;
import com.starmaylight.arcanotech_works.registry.ModBlocks;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.forge.ForgeTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableAnimated;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.fluids.FluidStack;

import java.util.List;

/**
 * JEI用混合機レシピカテゴリ
 */
public class MixerRecipeCategory implements IRecipeCategory<MixerRecipe> {

    public static final ResourceLocation UID = new ResourceLocation(Arcanotech_works.MODID, "mixing");
    public static final ResourceLocation TEXTURE = new ResourceLocation(Arcanotech_works.MODID, "textures/jei/mixer.png");
    public static final RecipeType<MixerRecipe> RECIPE_TYPE = RecipeType.create(Arcanotech_works.MODID, "mixing", MixerRecipe.class);

    private final IDrawable background;
    private final IDrawable icon;
    private final IDrawableAnimated arrow;

    public MixerRecipeCategory(IGuiHelper guiHelper) {
        // 背景: 122x36 (2x2スロット + 液体入力x2 + 矢印 + 液体出力x2 + アイテム出力)

        this.background = guiHelper.createDrawable(TEXTURE, 0, 0, 122, 36);
        this.icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(ModBlocks.MIXER.get()));
        
        // 矢印アニメーション（テクスチャの右端）
        IDrawableStatic arrowStatic = guiHelper.createDrawable(TEXTURE, 122, 0, 24, 17);
        this.arrow = guiHelper.createAnimatedDrawable(arrowStatic, 200, IDrawableAnimated.StartDirection.LEFT, false);
    }

    @Override
    public RecipeType<MixerRecipe> getRecipeType() {
        return RECIPE_TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("jei.arcanotech_works.mixing");
    }

    @Override
    public IDrawable getBackground() {
        return background;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, MixerRecipe recipe, IFocusGroup focuses) {
        List<Ingredient> ingredients = recipe.getIngredientList();
        
        // アイテム入力スロット（2x2グリッド）
        int[] slotX = {1, 19, 1, 19};
        int[] slotY = {1, 1, 19, 19};
        
        for (int i = 0; i < Math.min(ingredients.size(), 4); i++) {
            builder.addSlot(RecipeIngredientRole.INPUT, slotX[i], slotY[i])
                    .addIngredients(ingredients.get(i));
        }

        // 液体入力スロット（18x18 正方形、2つ縦に並ぶ）
        FluidStack fluidInput1 = recipe.getFluidInput1();
        FluidStack fluidInput2 = recipe.getFluidInput2();
        
        if (!fluidInput1.isEmpty()) {
            builder.addSlot(RecipeIngredientRole.INPUT, 38, 1)
                    .setFluidRenderer(4000, false, 16, 16)
                    .addIngredient(ForgeTypes.FLUID_STACK, fluidInput1);
        }
        
        if (!fluidInput2.isEmpty()) {
            builder.addSlot(RecipeIngredientRole.INPUT, 38, 19)
                    .setFluidRenderer(4000, false, 16, 16)
                    .addIngredient(ForgeTypes.FLUID_STACK, fluidInput2);
        }

        // 液体出力スロット（18x18 正方形、2つ縦に並ぶ）
        FluidStack fluidOutput1 = recipe.getFluidOutput1();
        FluidStack fluidOutput2 = recipe.getFluidOutput2();
        
        if (!fluidOutput1.isEmpty()) {
            builder.addSlot(RecipeIngredientRole.OUTPUT, 86, 1)
                    .setFluidRenderer(4000, false, 16, 16)
                    .addIngredient(ForgeTypes.FLUID_STACK, fluidOutput1);
        }
        
        if (!fluidOutput2.isEmpty()) {
            builder.addSlot(RecipeIngredientRole.OUTPUT, 86, 19)
                    .setFluidRenderer(4000, false, 16, 16)
                    .addIngredient(ForgeTypes.FLUID_STACK, fluidOutput2);
        }

        // アイテム出力
        ItemStack result = recipe.getResult();
        if (!result.isEmpty()) {
            builder.addSlot(RecipeIngredientRole.OUTPUT, 105, 10)
                    .addItemStack(result);
        }
    }

    @Override
    public void draw(MixerRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        // 矢印を描画（背景内の適切な位置）
        arrow.draw(guiGraphics, 58, 9);
    }
}
