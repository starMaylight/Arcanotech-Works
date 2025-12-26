package com.starmaylight.arcanotech_works.item;

import com.starmaylight.arcanotech_works.world.manapool.ManaPoolSavedData;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * 魔力感知器 - 魔力だまりを可視化するアイテム
 * 手に持っているとき、魔力だまりチャンクにパーティクル（靄）が表示される
 */
public class ManaSensorItem extends Item {

    public ManaSensorItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            ServerLevel serverLevel = serverPlayer.serverLevel();
            ChunkPos playerChunk = new ChunkPos(player.blockPosition());
            ManaPoolSavedData data = ManaPoolSavedData.get(serverLevel);
            
            // 現在地が魔力だまりかどうか
            if (data.isManaPool(playerChunk)) {
                player.displayClientMessage(
                        Component.translatable("message.arcanotech_works.in_mana_pool")
                                .withStyle(ChatFormatting.AQUA), true);
            } else {
                // 近くの魔力だまりを探す
                ChunkPos nearestPool = findNearestManaPool(data, playerChunk, 16);
                if (nearestPool != null) {
                    int distance = Math.abs(nearestPool.x - playerChunk.x) + 
                                   Math.abs(nearestPool.z - playerChunk.z);
                    player.displayClientMessage(
                            Component.translatable("message.arcanotech_works.mana_pool_nearby", distance)
                                    .withStyle(ChatFormatting.DARK_AQUA), true);
                } else {
                    player.displayClientMessage(
                            Component.translatable("message.arcanotech_works.no_mana_pool")
                                    .withStyle(ChatFormatting.GRAY), true);
                }
            }
        }
        
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }
    
    /**
     * 最寄りの魔力だまりを探す
     */
    @Nullable
    private ChunkPos findNearestManaPool(ManaPoolSavedData data, ChunkPos center, int radius) {
        ChunkPos nearest = null;
        int nearestDist = Integer.MAX_VALUE;
        
        for (ChunkPos pool : data.getAllManaPools()) {
            int dist = Math.abs(pool.x - center.x) + Math.abs(pool.z - center.z);
            if (dist <= radius && dist < nearestDist) {
                nearest = pool;
                nearestDist = dist;
            }
        }
        
        return nearest;
    }
    
    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.arcanotech_works.mana_sensor.desc")
                .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.arcanotech_works.mana_sensor.usage")
                .withStyle(ChatFormatting.DARK_GRAY));
    }
    
    @Override
    public boolean isFoil(ItemStack stack) {
        // 常に輝くエンチャント効果
        return true;
    }
}
