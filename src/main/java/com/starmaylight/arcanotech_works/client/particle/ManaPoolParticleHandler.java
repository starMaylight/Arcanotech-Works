package com.starmaylight.arcanotech_works.client.particle;

import com.starmaylight.arcanotech_works.Arcanotech_works;
import com.starmaylight.arcanotech_works.client.ManaPoolClientData;
import com.starmaylight.arcanotech_works.registry.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Random;

/**
 * クライアント側で魔力だまりの暗い霧を表示
 */
@Mod.EventBusSubscriber(modid = Arcanotech_works.MODID, value = Dist.CLIENT)
public class ManaPoolParticleHandler {

    private static final Random random = new Random();
    private static int tickCounter = 0;
    
    // パーティクル生成間隔（tick）
    private static final int PARTICLE_INTERVAL = 1;
    
    // 1チャンクあたりのパーティクル数（増加）
    private static final int PARTICLES_PER_CHUNK = 15;
    
    // 霧の高さ範囲
    private static final double FOG_HEIGHT_RANGE = 20.0;
    
    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        Level level = mc.level;
        
        if (player == null || level == null) return;
        
        // 魔力感知器を持っているか確認
        boolean holdingSensor = player.getMainHandItem().is(ModItems.MANA_SENSOR.get()) ||
                               player.getOffhandItem().is(ModItems.MANA_SENSOR.get());
        
        if (!holdingSensor) return;
        
        tickCounter++;
        if (tickCounter < PARTICLE_INTERVAL) return;
        tickCounter = 0;
        
        // 周辺の魔力だまりにパーティクルを生成
        for (ChunkPos chunkPos : ManaPoolClientData.getNearbyPools()) {
            spawnDarkFog(level, player, chunkPos);
        }
    }
    
    /**
     * 魔力だまりチャンクに暗い霧を生成
     */
    private static void spawnDarkFog(Level level, LocalPlayer player, ChunkPos chunkPos) {
        // チャンクの中心座標
        double chunkCenterX = (chunkPos.x * 16) + 8;
        double chunkCenterZ = (chunkPos.z * 16) + 8;
        
        // プレイヤーから遠すぎる場合はスキップ
        double distSq = player.distanceToSqr(chunkCenterX, player.getY(), chunkCenterZ);
        if (distSq > 80 * 80) return; // 80ブロック以内のみ
        
        // 距離に応じてパーティクル量を調整（近いほど多い）
        double dist = Math.sqrt(distSq);
        int particleCount = (int) (PARTICLES_PER_CHUNK * (1.0 - dist / 100.0));
        particleCount = Math.max(3, particleCount);
        
        // パーティクルを生成
        for (int i = 0; i < particleCount; i++) {
            // チャンク内のランダムな位置
            double x = (chunkPos.x * 16) + random.nextDouble() * 16;
            double z = (chunkPos.z * 16) + random.nextDouble() * 16;
            double y = player.getY() + (random.nextDouble() - 0.5) * FOG_HEIGHT_RANGE;
            
            // ゆっくりと漂う動き
            double vx = (random.nextDouble() - 0.5) * 0.01;
            double vy = (random.nextDouble() - 0.3) * 0.005; // 少し上昇傾向
            double vz = (random.nextDouble() - 0.5) * 0.01;
            
            // 暗い霧パーティクルを選択
            float choice = random.nextFloat();
            
            if (choice < 0.5f) {
                // 大きい煙（暗い霧のメイン）
                level.addParticle(ParticleTypes.LARGE_SMOKE, x, y, z, vx, vy, vz);
            } else if (choice < 0.75f) {
                // 焚き火の煙（ゆっくり漂う）
                level.addParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE, x, y, z, vx, vy * 0.5, vz);
            } else if (choice < 0.9f) {
                // 通常の煙
                level.addParticle(ParticleTypes.SMOKE, x, y, z, vx, vy, vz);
            } else {
                // たまに紫の光（魔力の存在を示す）
                level.addParticle(ParticleTypes.WITCH, x, y, z, 0, 0.02, 0);
            }
        }
        
        // チャンク境界付近に濃い霧を追加（境界を強調）
        spawnBoundaryFog(level, player, chunkPos);
    }
    
    /**
     * チャンク境界に濃い霧を生成
     */
    private static void spawnBoundaryFog(Level level, LocalPlayer player, ChunkPos chunkPos) {
        // 境界パーティクルは少なめ
        if (random.nextFloat() > 0.3f) return;
        
        int baseX = chunkPos.x * 16;
        int baseZ = chunkPos.z * 16;
        double y = player.getY() + (random.nextDouble() - 0.5) * 10;
        
        // 4辺のいずれかにパーティクル
        int side = random.nextInt(4);
        double x, z;
        
        switch (side) {
            case 0: // 北
                x = baseX + random.nextDouble() * 16;
                z = baseZ;
                break;
            case 1: // 南
                x = baseX + random.nextDouble() * 16;
                z = baseZ + 16;
                break;
            case 2: // 西
                x = baseX;
                z = baseZ + random.nextDouble() * 16;
                break;
            default: // 東
                x = baseX + 16;
                z = baseZ + random.nextDouble() * 16;
                break;
        }
        
        // 境界には濃い煙
        level.addParticle(ParticleTypes.CAMPFIRE_SIGNAL_SMOKE, x, y, z, 0, 0.01, 0);
    }
}
