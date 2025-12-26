package com.starmaylight.arcanotech_works.recipe;

import com.google.gson.JsonArray;
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
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * 混合機レシピ
 * アイテムと液体を混合して新しいアイテム/液体を生成
 */
public class MixerRecipe implements Recipe<Container> {

    private final ResourceLocation id;
    private final List<Ingredient> ingredients;
    private final FluidStack fluidInput1;
    private final FluidStack fluidInput2;
    private final ItemStack result;
    private final FluidStack fluidOutput1;
    private final FluidStack fluidOutput2;
    private final int processingTime;

    public static final int DEFAULT_PROCESSING_TIME = 200;

    public MixerRecipe(ResourceLocation id, List<Ingredient> ingredients,
                       FluidStack fluidInput1, FluidStack fluidInput2,
                       ItemStack result, FluidStack fluidOutput1, FluidStack fluidOutput2,
                       int processingTime) {
        this.id = id;
        this.ingredients = ingredients;
        this.fluidInput1 = fluidInput1;
        this.fluidInput2 = fluidInput2;
        this.result = result;
        this.fluidOutput1 = fluidOutput1;
        this.fluidOutput2 = fluidOutput2;
        this.processingTime = processingTime;
    }

    /**
     * アイテムのみのマッチング（液体は別途チェック）
     */
    @Override
    public boolean matches(Container container, Level level) {
        // 各スロットのアイテムをチェック
        List<ItemStack> inputs = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            ItemStack stack = container.getItem(i);
            if (!stack.isEmpty()) {
                inputs.add(stack);
            }
        }

        // 全てのレシピ材料が満たされているかチェック
        for (Ingredient ingredient : ingredients) {
            boolean found = false;
            for (ItemStack input : inputs) {
                if (ingredient.test(input)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                return false;
            }
        }

        return true;
    }

    /**
     * 液体を含めた完全なマッチング
     */
    public boolean matchesWithFluids(Container container, FluidStack tank1, FluidStack tank2, Level level) {
        if (!matches(container, level)) {
            return false;
        }

        // 液体入力チェック
        if (!fluidInput1.isEmpty()) {
            if (tank1.isEmpty() || !tank1.isFluidEqual(fluidInput1) || tank1.getAmount() < fluidInput1.getAmount()) {
                // タンク2もチェック
                if (tank2.isEmpty() || !tank2.isFluidEqual(fluidInput1) || tank2.getAmount() < fluidInput1.getAmount()) {
                    return false;
                }
            }
        }

        if (!fluidInput2.isEmpty()) {
            if (tank2.isEmpty() || !tank2.isFluidEqual(fluidInput2) || tank2.getAmount() < fluidInput2.getAmount()) {
                // タンク1もチェック
                if (tank1.isEmpty() || !tank1.isFluidEqual(fluidInput2) || tank1.getAmount() < fluidInput2.getAmount()) {
                    return false;
                }
            }
        }

        return true;
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
        return ModRecipes.MIXER_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipes.MIXER_TYPE.get();
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> list = NonNullList.create();
        list.addAll(ingredients);
        return list;
    }

    @Override
    public ItemStack getToastSymbol() {
        return new ItemStack(ModBlocks.MIXER.get());
    }

    // ==================== ゲッター ====================

    public List<Ingredient> getIngredientList() {
        return ingredients;
    }

    public FluidStack getFluidInput1() {
        return fluidInput1.copy();
    }

    public FluidStack getFluidInput2() {
        return fluidInput2.copy();
    }

    public ItemStack getResult() {
        return result.copy();
    }

    public FluidStack getFluidOutput1() {
        return fluidOutput1.copy();
    }

    public FluidStack getFluidOutput2() {
        return fluidOutput2.copy();
    }

    public int getProcessingTime() {
        return processingTime;
    }

    // ==================== シリアライザー ====================

    public static class Serializer implements RecipeSerializer<MixerRecipe> {

        @Override
        public MixerRecipe fromJson(ResourceLocation id, JsonObject json) {
            // アイテム材料
            List<Ingredient> ingredients = new ArrayList<>();
            if (json.has("ingredients")) {
                JsonArray ingredientsArray = GsonHelper.getAsJsonArray(json, "ingredients");
                for (int i = 0; i < ingredientsArray.size(); i++) {
                    ingredients.add(Ingredient.fromJson(ingredientsArray.get(i)));
                }
            }

            // 液体入力
            FluidStack fluidInput1 = parseFluidStack(json, "fluidInput1");
            FluidStack fluidInput2 = parseFluidStack(json, "fluidInput2");

            // 出力
            ItemStack result = ItemStack.EMPTY;
            if (json.has("result")) {
                result = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(json, "result"));
            }

            FluidStack fluidOutput1 = parseFluidStack(json, "fluidOutput1");
            FluidStack fluidOutput2 = parseFluidStack(json, "fluidOutput2");

            int processingTime = GsonHelper.getAsInt(json, "processingTime", DEFAULT_PROCESSING_TIME);

            return new MixerRecipe(id, ingredients, fluidInput1, fluidInput2, result, fluidOutput1, fluidOutput2, processingTime);
        }

        private FluidStack parseFluidStack(JsonObject json, String key) {
            if (!json.has(key)) {
                return FluidStack.EMPTY;
            }
            JsonObject fluidJson = GsonHelper.getAsJsonObject(json, key);
            String fluidName = GsonHelper.getAsString(fluidJson, "fluid");
            int amount = GsonHelper.getAsInt(fluidJson, "amount", 1000);
            var fluid = ForgeRegistries.FLUIDS.getValue(new ResourceLocation(fluidName));
            if (fluid == null) {
                return FluidStack.EMPTY;
            }
            return new FluidStack(fluid, amount);
        }

        @Override
        public @Nullable MixerRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buf) {
            int ingredientCount = buf.readVarInt();
            List<Ingredient> ingredients = new ArrayList<>();
            for (int i = 0; i < ingredientCount; i++) {
                ingredients.add(Ingredient.fromNetwork(buf));
            }

            FluidStack fluidInput1 = buf.readFluidStack();
            FluidStack fluidInput2 = buf.readFluidStack();
            ItemStack result = buf.readItem();
            FluidStack fluidOutput1 = buf.readFluidStack();
            FluidStack fluidOutput2 = buf.readFluidStack();
            int processingTime = buf.readVarInt();

            return new MixerRecipe(id, ingredients, fluidInput1, fluidInput2, result, fluidOutput1, fluidOutput2, processingTime);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buf, MixerRecipe recipe) {
            buf.writeVarInt(recipe.ingredients.size());
            for (Ingredient ingredient : recipe.ingredients) {
                ingredient.toNetwork(buf);
            }

            buf.writeFluidStack(recipe.fluidInput1);
            buf.writeFluidStack(recipe.fluidInput2);
            buf.writeItem(recipe.result);
            buf.writeFluidStack(recipe.fluidOutput1);
            buf.writeFluidStack(recipe.fluidOutput2);
            buf.writeVarInt(recipe.processingTime);
        }
    }
}
