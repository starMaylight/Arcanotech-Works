package com.starmaylight.arcanotech_works.recipe;

import com.google.gson.JsonObject;
import com.starmaylight.arcanotech_works.registry.ModBlocks;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

/**
 * 粉砕機レシピ
 * 1つの入力から1つの出力を生成
 */
public class CrusherRecipe implements Recipe<Container> {

    private final ResourceLocation id;
    private final Ingredient ingredient;
    private final ItemStack result;
    private final int processingTime;

    public static final int DEFAULT_PROCESSING_TIME = 200;

    public CrusherRecipe(ResourceLocation id, Ingredient ingredient, ItemStack result, int processingTime) {
        this.id = id;
        this.ingredient = ingredient;
        this.result = result;
        this.processingTime = processingTime;
    }

    @Override
    public boolean matches(Container container, Level level) {
        return ingredient.test(container.getItem(0));
    }

    @Override
    public ItemStack assemble(Container container, RegistryAccess registryAccess) {
        return result.copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResultItem(RegistryAccess registryAccess) {
        return result.copy();
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.CRUSHER_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipes.CRUSHER_TYPE.get();
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> list = NonNullList.create();
        list.add(ingredient);
        return list;
    }

    @Override
    public ItemStack getToastSymbol() {
        return new ItemStack(ModBlocks.CRUSHER.get());
    }

    // ==================== ゲッター ====================

    public Ingredient getIngredient() {
        return ingredient;
    }

    public ItemStack getResult() {
        return result.copy();
    }

    public int getProcessingTime() {
        return processingTime;
    }

    // ==================== シリアライザー ====================

    public static class Serializer implements RecipeSerializer<CrusherRecipe> {

        @Override
        public CrusherRecipe fromJson(ResourceLocation id, JsonObject json) {
            Ingredient ingredient = Ingredient.fromJson(GsonHelper.getAsJsonObject(json, "ingredient"));
            
            JsonObject resultJson = GsonHelper.getAsJsonObject(json, "result");
            ItemStack result = ShapedRecipe.itemStackFromJson(resultJson);
            
            int processingTime = GsonHelper.getAsInt(json, "processingTime", DEFAULT_PROCESSING_TIME);

            return new CrusherRecipe(id, ingredient, result, processingTime);
        }

        @Override
        public @Nullable CrusherRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buf) {
            Ingredient ingredient = Ingredient.fromNetwork(buf);
            ItemStack result = buf.readItem();
            int processingTime = buf.readVarInt();

            return new CrusherRecipe(id, ingredient, result, processingTime);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buf, CrusherRecipe recipe) {
            recipe.ingredient.toNetwork(buf);
            buf.writeItem(recipe.result);
            buf.writeVarInt(recipe.processingTime);
        }
    }
}
