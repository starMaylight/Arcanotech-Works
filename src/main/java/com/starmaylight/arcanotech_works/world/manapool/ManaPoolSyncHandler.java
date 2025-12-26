package com.starmaylight.arcanotech_works.world.manapool;

import com.starmaylight.arcanotech_works.Arcanotech_works;
import com.starmaylight.arcanotech_works.network.ManaPoolSyncPacket;
import com.starmaylight.arcanotech_works.network.ModNetwork;
import com.starmaylight.arcanotech_works.registry.ModItems;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashSet;
import java.util.Set;

/**
 * サーバー側で魔力だまり位置をプレイヤーに同期
 */
@Mod.EventBusSubscriber(modid = Arcanotech_works.MODID)
public class ManaPoolSyncHandler {

    // 同期間隔（tick）- 1秒に1回
    private static final int SYNC_INTERVAL = 20;
    
    // 同期範囲（チャンク数）
    private static final int SYNC_RADIUS = 8;
    
    private static int tickCounter = 0;
    
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!(event.player instanceof ServerPlayer player)) return;
        
        // 魔力感知器を持っているか確認
        boolean holdingSensor = player.getMainHandItem().is(ModItems.MANA_SENSOR.get()) ||
                               player.getOffhandItem().is(ModItems.MANA_SENSOR.get());
        
        if (!holdingSensor) return;
        
        tickCounter++;
        if (tickCounter < SYNC_INTERVAL) return;
        tickCounter = 0;
        
        // プレイヤー周辺の魔力だまりを収集
        ServerLevel level = player.serverLevel();
        ManaPoolSavedData data = ManaPoolSavedData.get(level);
        ChunkPos playerChunk = new ChunkPos(player.blockPosition());
        
        Set<ChunkPos> nearbyPools = new HashSet<>();
        for (ChunkPos pool : data.getAllManaPools()) {
            int dx = Math.abs(pool.x - playerChunk.x);
            int dz = Math.abs(pool.z - playerChunk.z);
            if (dx <= SYNC_RADIUS && dz <= SYNC_RADIUS) {
                nearbyPools.add(pool);
            }
        }
        
        // パケット送信
        ModNetwork.sendToPlayer(new ManaPoolSyncPacket(nearbyPools), player);
    }
}
