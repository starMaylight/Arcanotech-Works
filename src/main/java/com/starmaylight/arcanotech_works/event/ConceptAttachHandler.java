package com.starmaylight.arcanotech_works.event;

import com.starmaylight.arcanotech_works.Arcanotech_works;
import com.starmaylight.arcanotech_works.api.concept.ConceptData;
import com.starmaylight.arcanotech_works.api.concept.ConceptDefinition;
import com.starmaylight.arcanotech_works.api.concept.ConceptRegistry;
import com.starmaylight.arcanotech_works.capability.ConceptCapability;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * ItemStackに概念Capabilityをアタッチするイベントハンドラ。
 * JSONで定義された概念を持つアイテムに自動的に概念データを付与する。
 */
@Mod.EventBusSubscriber(modid = Arcanotech_works.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ConceptAttachHandler {

    @SubscribeEvent
    public static void onAttachCapabilities(AttachCapabilitiesEvent<ItemStack> event) {
        ItemStack stack = event.getObject();
        if (stack.isEmpty()) return;

        ConceptRegistry registry = ConceptRegistry.getInstance();
        if (registry == null) {
            // サーバー起動前 - 空のCapabilityをアタッチ
            event.addCapability(ConceptCapability.CONCEPT_CAP_ID,
                    new ConceptCapability.ItemConceptProvider());
            return;
        }

        ConceptDefinition definition = registry.getDefinition(stack);
        if (definition != null) {
            // 定義あり - 概念データで初期化
            ConceptData initialData = definition.toConceptData();
            event.addCapability(ConceptCapability.CONCEPT_CAP_ID,
                    new ConceptCapability.ItemConceptProvider(initialData));
        } else {
            // 定義なし - 空のCapability（レシピで付与される可能性あり）
            event.addCapability(ConceptCapability.CONCEPT_CAP_ID,
                    new ConceptCapability.ItemConceptProvider());
        }
    }
}
