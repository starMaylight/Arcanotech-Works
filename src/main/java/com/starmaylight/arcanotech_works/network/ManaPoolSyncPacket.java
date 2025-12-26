package com.starmaylight.arcanotech_works.network;

import com.starmaylight.arcanotech_works.client.ManaPoolClientData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.ChunkPos;
import net.minecraftforge.network.NetworkEvent;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

/**
 * 魔力だまり位置同期パケット（S2C）
 * プレイヤー周辺の魔力だまりチャンク位置をクライアントに送信
 */
public class ManaPoolSyncPacket {

    private final Set<ChunkPos> manaPoolChunks;
    
    public ManaPoolSyncPacket(Set<ChunkPos> chunks) {
        this.manaPoolChunks = chunks;
    }
    
    public static void encode(ManaPoolSyncPacket packet, FriendlyByteBuf buf) {
        buf.writeInt(packet.manaPoolChunks.size());
        for (ChunkPos pos : packet.manaPoolChunks) {
            buf.writeInt(pos.x);
            buf.writeInt(pos.z);
        }
    }
    
    public static ManaPoolSyncPacket decode(FriendlyByteBuf buf) {
        int size = buf.readInt();
        Set<ChunkPos> chunks = new HashSet<>();
        for (int i = 0; i < size; i++) {
            int x = buf.readInt();
            int z = buf.readInt();
            chunks.add(new ChunkPos(x, z));
        }
        return new ManaPoolSyncPacket(chunks);
    }
    
    public static void handle(ManaPoolSyncPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            // クライアント側で魔力だまり情報を更新
            ManaPoolClientData.setNearbyPools(packet.manaPoolChunks);
        });
        ctx.get().setPacketHandled(true);
    }
}
