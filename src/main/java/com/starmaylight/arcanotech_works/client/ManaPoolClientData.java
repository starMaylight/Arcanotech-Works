package com.starmaylight.arcanotech_works.client;

import net.minecraft.world.level.ChunkPos;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * クライアント側で保持する魔力だまりデータ
 */
public class ManaPoolClientData {

    // 現在プレイヤー周辺にある魔力だまりチャンク
    private static Set<ChunkPos> nearbyPools = new HashSet<>();
    
    /**
     * サーバーから受信した魔力だまり位置を設定
     */
    public static void setNearbyPools(Set<ChunkPos> pools) {
        nearbyPools = new HashSet<>(pools);
    }
    
    /**
     * 周辺の魔力だまりを取得
     */
    public static Set<ChunkPos> getNearbyPools() {
        return Collections.unmodifiableSet(nearbyPools);
    }
    
    /**
     * 指定チャンクが魔力だまりかどうか
     */
    public static boolean isManaPool(ChunkPos pos) {
        return nearbyPools.contains(pos);
    }
    
    /**
     * データをクリア（ディメンション移動時など）
     */
    public static void clear() {
        nearbyPools.clear();
    }
}
