package com.starmaylight.arcanotech_works.compat.jei;

import com.starmaylight.arcanotech_works.Arcanotech_works;
import com.starmaylight.arcanotech_works.recipe.CompressorRecipe;
import com.starmaylight.arcanotech_works.registry.ModBlocks;
import mezz.jei.api.constants.VanillaTypes;
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

import java.util.Arrays;

/**
 * JEI用圧縮機レシピカテゴリ
 */
public class CompressorRecipeCategory implements IRecipeCategory<CompressorRecipe> {

    public static final ResourceLocation UID = new ResourceLocation(Arcanotech_works.MODID, "compressing");
    public static final ResourceLocation TEXTURE = new ResourceLocation(Arcanotech_works.MODID, "textures/jei/compressor.png");
    public static final RecipeType<CompressorRecipe> RECIPE_TYPE = RecipeType.create(Arcanotech_works.MODID, "compressing", CompressorRecipe.class);

    private final IDrawable background;
    private final IDrawable icon;
    private final IDrawableAnimated arrow;

    public CompressorRecipeCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.createDrawable(TEXTURE, 0, 0, 82, 34);
        this.icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(ModBlocks.COMPRESSOR.get()));
        
        IDrawableStatic arrowStatic = guiHelper.createDrawable(TEXTURE, 82, 0, 24, 17);
        this.arrow = guiHelper.createAnimatedDrawable(arrowStatic, 160, IDrawableAnimated.StartDirection.LEFT, false);
    }

    @Override
    public RecipeType<CompressorRecipe> getRecipeType() {
        return RECIPE_TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("jei.arcanotech_works.compressing");
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
    public void setRecipe(IRecipeLayoutBuilder builder, CompressorRecipe recipe, IFocusGroup focuses) {
        // 入力（個数表示付き）
        builder.addSlot(RecipeIngredientRole.INPUT, 1, 9)
                .addIngredients(recipe.getIngredient())
                .addTooltipCallback((recipeSlotView, tooltip) -> {
                    tooltip.add(Component.translatable("jei.arcanotech_works.input_count", recipe.getInputCount()));
                });

        builder.addSlot(RecipeIngredientRole.OUTPUT, 61, 9)
                .addItemStack(recipe.getResult());
    }

    @Override
    public void draw(CompressorRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        arrow.draw(guiGraphics, 26, 8);
        
        // 入力個数を表示
        String countText = "x" + recipe.getInputCount();
        guiGraphics.drawString(
                net.minecraft.client.Minecraft.getInstance().font,
                countText,
                1, 0, 0x404040, false
        );
    }
}
