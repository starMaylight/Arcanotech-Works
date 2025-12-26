package com.starmaylight.arcanotech_works.world.manapool;

import com.starmaylight.arcanotech_works.Arcanotech_works;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.event.level.ChunkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Random;

/**
 * チャンクロード時に魔力だまりを生成するイベントハンドラ
 */
@Mod.EventBusSubscriber(modid = Arcanotech_works.MODID)
public class ManaPoolGenerator {

    // 魔力だまり生成確率（1/RARITY チャンクに1つ）
    // 2000 = 約0.05%、非常にレア
    private static final int RARITY = 500;
    
    // シード混合用の定数
    private static final long SEED_MODIFIER = 0xA8C4A0_B00FL;
    
    @SubscribeEvent
    public static void onChunkLoad(ChunkEvent.Load event) {
        if (event.getLevel() instanceof ServerLevel serverLevel) {
            if (!(event.getChunk() instanceof LevelChunk chunk)) {
                return;
            }
            
            ChunkPos chunkPos = chunk.getPos();
            ManaPoolSavedData data = ManaPoolSavedData.get(serverLevel);
            
            // 既にチェック済みのチャンクはスキップ
            // 新規チャンクかどうかを判定するため、座標ベースのシードで判定
            if (shouldGenerateManaPool(serverLevel, chunkPos)) {
                if (!data.isManaPool(chunkPos)) {
                    data.addManaPool(chunkPos);
                    Arcanotech_works.LOGGER.debug("Mana Pool generated at chunk [{}, {}]", 
                            chunkPos.x, chunkPos.z);
                }
            }
        }
    }
    
    /**
     * 指定チャンクに魔力だまりを生成すべきかを決定論的に判定
     * 同じチャンク座標とワールドシードなら常に同じ結果を返す
     */
    private static boolean shouldGenerateManaPool(ServerLevel level, ChunkPos pos) {
        // ワールドシードとチャンク座標から決定論的に乱数を生成
        long worldSeed = level.getSeed();
        long chunkSeed = getChunkSeed(worldSeed, pos.x, pos.z);
        
        Random random = new Random(chunkSeed);
        
        // RARITY分の1の確率で生成
        return random.nextInt(RARITY) == 0;
    }
    
    /**
     * チャンク固有のシードを計算
     */
    private static long getChunkSeed(long worldSeed, int chunkX, int chunkZ) {
        return worldSeed ^ 
               ((long) chunkX * 341873128712L) ^ 
               ((long) chunkZ * 132897987541L) ^ 
               SEED_MODIFIER;
    }
    
    /**
     * 指定位置が魔力だまり内かどうかをチェック（ユーティリティメソッド）
     */
    public static boolean isInManaPool(ServerLevel level, ChunkPos pos) {
        return ManaPoolSavedData.get(level).isManaPool(pos);
    }
}
