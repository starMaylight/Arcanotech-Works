package com.starmaylight.arcanotech_works.world.manapool;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashSet;
import java.util.Set;

/**
 * 魔力だまりの位置を保存するSavedData
 * ワールドごとに魔力だまりチャンクの座標を記録
 */
public class ManaPoolSavedData extends SavedData {

    private static final String DATA_NAME = "arcanotech_mana_pools";
    
    // 魔力だまりが存在するチャンク座標のセット
    private final Set<ChunkPos> manaPoolChunks = new HashSet<>();
    
    public ManaPoolSavedData() {
    }
    
    public ManaPoolSavedData(CompoundTag tag) {
        ListTag list = tag.getList("ManaPools", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag chunkTag = list.getCompound(i);
            int x = chunkTag.getInt("x");
            int z = chunkTag.getInt("z");
            manaPoolChunks.add(new ChunkPos(x, z));
        }
    }
    
    @Override
    public CompoundTag save(CompoundTag tag) {
        ListTag list = new ListTag();
        for (ChunkPos pos : manaPoolChunks) {
            CompoundTag chunkTag = new CompoundTag();
            chunkTag.putInt("x", pos.x);
            chunkTag.putInt("z", pos.z);
            list.add(chunkTag);
        }
        tag.put("ManaPools", list);
        return tag;
    }
    
    /**
     * チャンクを魔力だまりとしてマーク
     */
    public void addManaPool(ChunkPos pos) {
        if (manaPoolChunks.add(pos)) {
            setDirty();
        }
    }
    
    /**
     * 魔力だまりを削除
     */
    public void removeManaPool(ChunkPos pos) {
        if (manaPoolChunks.remove(pos)) {
            setDirty();
        }
    }
    
    /**
     * 指定チャンクが魔力だまりかどうか
     */
    public boolean isManaPool(ChunkPos pos) {
        return manaPoolChunks.contains(pos);
    }
    
    /**
     * 指定ブロック位置が魔力だまり内かどうか
     */
    public boolean isInManaPool(BlockPos pos) {
        return isManaPool(new ChunkPos(pos));
    }
    
    /**
     * すべての魔力だまりチャンクを取得
     */
    public Set<ChunkPos> getAllManaPools() {
        return new HashSet<>(manaPoolChunks);
    }
    
    /**
     * 魔力だまりの数を取得
     */
    public int getManaPoolCount() {
        return manaPoolChunks.size();
    }
    
    /**
     * ServerLevelからSavedDataを取得
     */
    public static ManaPoolSavedData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
                ManaPoolSavedData::new,
                ManaPoolSavedData::new,
                DATA_NAME
        );
    }
}
