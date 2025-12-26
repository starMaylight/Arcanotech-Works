package com.starmaylight.arcanotech_works.event;

import com.starmaylight.arcanotech_works.Arcanotech_works;
import com.starmaylight.arcanotech_works.api.concept.ConceptData;
import com.starmaylight.arcanotech_works.api.concept.ConceptDomain;
import com.starmaylight.arcanotech_works.api.concept.ConceptType;
import com.starmaylight.arcanotech_works.capability.ConceptCapability;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

/**
 * アイテムのツールチップに概念情報を表示するハンドラ。
 * Shiftキーで詳細表示。
 */
@Mod.EventBusSubscriber(modid = Arcanotech_works.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ConceptTooltipHandler {

    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        List<Component> tooltip = event.getToolTip();

        stack.getCapability(ConceptCapability.CONCEPT).ifPresent(conceptData -> {
            if (conceptData.isEmpty()) return;

            tooltip.add(Component.empty());
            tooltip.add(Component.translatable("tooltip.arcanotech_works.concepts")
                    .withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD));

            if (Screen.hasShiftDown()) {
                addDetailedConceptInfo(tooltip, conceptData);
            } else {
                addSimpleConceptInfo(tooltip, conceptData);
                tooltip.add(Component.translatable("tooltip.arcanotech_works.shift_for_details")
                        .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
            }
        });
    }

    private static void addSimpleConceptInfo(List<Component> tooltip, ConceptData data) {
        int count = data.getConceptCount();
        if (count <= 3) {
            for (ConceptType type : data.getConcepts()) {
                tooltip.add(formatConceptSimple(type, data.getIntensity(type)));
            }
        } else {
            tooltip.add(Component.translatable("tooltip.arcanotech_works.concept_summary",
                    count, data.getDomainCount()).withStyle(ChatFormatting.GRAY));
        }
    }

    private static void addDetailedConceptInfo(List<Component> tooltip, ConceptData data) {
        for (ConceptDomain domain : ConceptDomain.values()) {
            List<ConceptType> domainConcepts = data.getConceptsByDomain(domain);
            if (!domainConcepts.isEmpty()) {
                tooltip.add(Component.literal("  ").append(domain.getDisplayName())
                        .append(Component.literal(":").withStyle(ChatFormatting.GRAY)));
                for (ConceptType type : domainConcepts) {
                    tooltip.add(Component.literal("    ")
                            .append(formatConceptDetailed(type, data.getIntensity(type))));
                }
            }
        }
        tooltip.add(Component.empty());
        tooltip.add(Component.translatable("tooltip.arcanotech_works.total_intensity",
                data.getTotalIntensity()).withStyle(ChatFormatting.DARK_GRAY));
    }

    private static Component formatConceptSimple(ConceptType type, int intensity) {
        return Component.literal("  ").append(type.getDisplayName())
                .append(Component.literal(" ")).append(createIntensityBar(intensity));
    }

    private static Component formatConceptDetailed(ConceptType type, int intensity) {
        return type.getDisplayName().copy()
                .append(Component.literal(" [" + intensity + "] ").withStyle(ChatFormatting.DARK_GRAY))
                .append(createIntensityBar(intensity));
    }

    private static MutableComponent createIntensityBar(int intensity) {
        int filled = intensity / 10;
        int empty = 10 - filled;
        ChatFormatting color = intensity >= 80 ? ChatFormatting.AQUA :
                intensity >= 60 ? ChatFormatting.GREEN :
                intensity >= 40 ? ChatFormatting.YELLOW :
                intensity >= 20 ? ChatFormatting.GOLD : ChatFormatting.RED;

        return Component.literal("│".repeat(filled)).withStyle(color)
                .append(Component.literal("│".repeat(empty)).withStyle(ChatFormatting.DARK_GRAY));
    }
}
