package com.starmaylight.arcanotech_works.capability;

import com.starmaylight.arcanotech_works.Arcanotech_works;
import com.starmaylight.arcanotech_works.api.concept.ConceptData;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.*;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * 概念Capabilityの登録とアタッチを管理するクラス。
 */
public class ConceptCapability {

    public static final Capability<ConceptData> CONCEPT = CapabilityManager.get(new CapabilityToken<>() {});
    public static final ResourceLocation CONCEPT_CAP_ID = new ResourceLocation(Arcanotech_works.MODID, "concept");

    public static void register(RegisterCapabilitiesEvent event) {
        event.register(ConceptData.class);
    }

    public static class ItemConceptProvider implements ICapabilitySerializable<CompoundTag> {
        private final ConceptData conceptData;
        private final LazyOptional<ConceptData> optional;

        public ItemConceptProvider() {
            this.conceptData = new ConceptData();
            this.optional = LazyOptional.of(() -> conceptData);
        }

        public ItemConceptProvider(ConceptData initialData) {
            this.conceptData = initialData.copy();
            this.optional = LazyOptional.of(() -> conceptData);
        }

        @Nonnull
        @Override
        public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
            return cap == CONCEPT ? optional.cast() : LazyOptional.empty();
        }

        @Override
        public CompoundTag serializeNBT() { return conceptData.serializeNBT(); }

        @Override
        public void deserializeNBT(CompoundTag nbt) { conceptData.deserializeNBT(nbt); }

        public void invalidate() { optional.invalidate(); }
        public ConceptData getConceptData() { return conceptData; }
    }

    public static LazyOptional<ConceptData> getConcept(ItemStack stack) {
        return stack.getCapability(CONCEPT);
    }

    public static boolean hasConcept(ItemStack stack) {
        return stack.getCapability(CONCEPT)
                .map(data -> !data.isEmpty())
                .orElse(false);
    }

    public static ConceptData copyConceptData(ItemStack stack) {
        return stack.getCapability(CONCEPT)
                .map(ConceptData::copy)
                .orElse(new ConceptData());
    }
}
