package com.starmaylight.arcanotech_works.compat.jei;

import com.starmaylight.arcanotech_works.Arcanotech_works;
import com.starmaylight.arcanotech_works.recipe.RollingMillRecipe;
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

/**
 * JEI用圧延機レシピカテゴリ
 */
public class RollingMillRecipeCategory implements IRecipeCategory<RollingMillRecipe> {

    public static final ResourceLocation UID = new ResourceLocation(Arcanotech_works.MODID, "rolling");
    public static final ResourceLocation TEXTURE = new ResourceLocation(Arcanotech_works.MODID, "textures/gui/jei/rolling_mill.png");
    public static final RecipeType<RollingMillRecipe> RECIPE_TYPE = RecipeType.create(Arcanotech_works.MODID, "rolling", RollingMillRecipe.class);

    private final IDrawable background;
    private final IDrawable icon;
    private final IDrawableAnimated arrow;

    public RollingMillRecipeCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.createDrawable(TEXTURE, 0, 0, 82, 34);
        this.icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(ModBlocks.ROLLING_MILL.get()));
        
        IDrawableStatic arrowStatic = guiHelper.createDrawable(TEXTURE, 82, 0, 24, 17);
        this.arrow = guiHelper.createAnimatedDrawable(arrowStatic, 100, IDrawableAnimated.StartDirection.LEFT, false);
    }

    @Override
    public RecipeType<RollingMillRecipe> getRecipeType() {
        return RECIPE_TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("jei.arcanotech_works.rolling");
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
    public void setRecipe(IRecipeLayoutBuilder builder, RollingMillRecipe recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 1, 9)
                .addIngredients(recipe.getIngredient());

        builder.addSlot(RecipeIngredientRole.OUTPUT, 61, 9)
                .addItemStack(recipe.getResult());
    }

    @Override
    public void draw(RollingMillRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        arrow.draw(guiGraphics, 24, 9);
    }
}
