package com.starmaylight.arcanotech_works.recipe;

import com.starmaylight.arcanotech_works.Arcanotech_works;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * カスタムレシピタイプとシリアライザーの登録
 */
public class ModRecipes {

    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES =
            DeferredRegister.create(ForgeRegistries.RECIPE_TYPES, Arcanotech_works.MODID);

    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS =
            DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, Arcanotech_works.MODID);

    // ==================== レシピタイプ ====================

    public static final RegistryObject<RecipeType<CrusherRecipe>> CRUSHER_TYPE =
            RECIPE_TYPES.register("crushing", () -> new RecipeType<>() {
                @Override
                public String toString() {
                    return "crushing";
                }
            });

    public static final RegistryObject<RecipeType<CompressorRecipe>> COMPRESSOR_TYPE =
            RECIPE_TYPES.register("compressing", () -> new RecipeType<>() {
                @Override
                public String toString() {
                    return "compressing";
                }
            });

    public static final RegistryObject<RecipeType<RollingMillRecipe>> ROLLING_MILL_TYPE =
            RECIPE_TYPES.register("rolling", () -> new RecipeType<>() {
                @Override
                public String toString() {
                    return "rolling";
                }
            });

    public static final RegistryObject<RecipeType<MixerRecipe>> MIXER_TYPE =
            RECIPE_TYPES.register("mixing", () -> new RecipeType<>() {
                @Override
                public String toString() {
                    return "mixing";
                }
            });

    // ==================== シリアライザー ====================

    public static final RegistryObject<RecipeSerializer<CrusherRecipe>> CRUSHER_SERIALIZER =
            RECIPE_SERIALIZERS.register("crushing", CrusherRecipe.Serializer::new);

    public static final RegistryObject<RecipeSerializer<CompressorRecipe>> COMPRESSOR_SERIALIZER =
            RECIPE_SERIALIZERS.register("compressing", CompressorRecipe.Serializer::new);

    public static final RegistryObject<RecipeSerializer<RollingMillRecipe>> ROLLING_MILL_SERIALIZER =
            RECIPE_SERIALIZERS.register("rolling", RollingMillRecipe.Serializer::new);

    public static final RegistryObject<RecipeSerializer<MixerRecipe>> MIXER_SERIALIZER =
            RECIPE_SERIALIZERS.register("mixing", MixerRecipe.Serializer::new);

    public static void register(IEventBus eventBus) {
        RECIPE_TYPES.register(eventBus);
        RECIPE_SERIALIZERS.register(eventBus);
    }
}
