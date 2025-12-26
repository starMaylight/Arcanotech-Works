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
 * 圧縮機レシピ
 * 複数の同一アイテムを1つの出力に圧縮
 */
public class CompressorRecipe implements Recipe<Container> {

    private final ResourceLocation id;
    private final Ingredient ingredient;
    private final int inputCount;
    private final ItemStack result;
    private final int processingTime;

    public static final int DEFAULT_PROCESSING_TIME = 160;

    public CompressorRecipe(ResourceLocation id, Ingredient ingredient, int inputCount, ItemStack result, int processingTime) {
        this.id = id;
        this.ingredient = ingredient;
        this.inputCount = inputCount;
        this.result = result;
        this.processingTime = processingTime;
    }

    @Override
    public boolean matches(Container container, Level level) {
        ItemStack input = container.getItem(0);
        return ingredient.test(input) && input.getCount() >= inputCount;
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
        return ModRecipes.COMPRESSOR_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipes.COMPRESSOR_TYPE.get();
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> list = NonNullList.create();
        list.add(ingredient);
        return list;
    }

    @Override
    public ItemStack getToastSymbol() {
        return new ItemStack(ModBlocks.COMPRESSOR.get());
    }

    // ==================== ゲッター ====================

    public Ingredient getIngredient() {
        return ingredient;
    }

    public int getInputCount() {
        return inputCount;
    }

    public ItemStack getResult() {
        return result.copy();
    }

    public int getProcessingTime() {
        return processingTime;
    }

    // ==================== シリアライザー ====================

    public static class Serializer implements RecipeSerializer<CompressorRecipe> {

        @Override
        public CompressorRecipe fromJson(ResourceLocation id, JsonObject json) {
            Ingredient ingredient = Ingredient.fromJson(GsonHelper.getAsJsonObject(json, "ingredient"));
            int inputCount = GsonHelper.getAsInt(json, "inputCount", 1);
            
            JsonObject resultJson = GsonHelper.getAsJsonObject(json, "result");
            ItemStack result = ShapedRecipe.itemStackFromJson(resultJson);
            
            int processingTime = GsonHelper.getAsInt(json, "processingTime", DEFAULT_PROCESSING_TIME);

            return new CompressorRecipe(id, ingredient, inputCount, result, processingTime);
        }

        @Override
        public @Nullable CompressorRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buf) {
            Ingredient ingredient = Ingredient.fromNetwork(buf);
            int inputCount = buf.readVarInt();
            ItemStack result = buf.readItem();
            int processingTime = buf.readVarInt();

            return new CompressorRecipe(id, ingredient, inputCount, result, processingTime);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buf, CompressorRecipe recipe) {
            recipe.ingredient.toNetwork(buf);
            buf.writeVarInt(recipe.inputCount);
            buf.writeItem(recipe.result);
            buf.writeVarInt(recipe.processingTime);
        }
    }
}
