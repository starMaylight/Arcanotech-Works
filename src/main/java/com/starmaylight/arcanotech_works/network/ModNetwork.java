package com.starmaylight.arcanotech_works.network;

import com.starmaylight.arcanotech_works.Arcanotech_works;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

/**
 * ネットワーク通信ハンドラ
 */
public class ModNetwork {

    private static final String PROTOCOL_VERSION = "1";
    
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(Arcanotech_works.MODID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );
    
    private static int packetId = 0;
    
    public static void register() {
        // S2C: 魔力だまり位置同期パケット
        CHANNEL.messageBuilder(ManaPoolSyncPacket.class, packetId++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(ManaPoolSyncPacket::encode)
                .decoder(ManaPoolSyncPacket::decode)
                .consumerMainThread(ManaPoolSyncPacket::handle)
                .add();
        
        // C2S: 刻印台クリックパケット
        CHANNEL.messageBuilder(EngravingClickPacket.class, packetId++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(EngravingClickPacket::encode)
                .decoder(EngravingClickPacket::decode)
                .consumerMainThread(EngravingClickPacket::handle)
                .add();
        
        Arcanotech_works.LOGGER.info("Network packets registered");
    }
    
    /**
     * プレイヤーにパケットを送信
     */
    public static void sendToPlayer(Object packet, ServerPlayer player) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), packet);
    }
    
    /**
     * 全プレイヤーにパケットを送信
     */
    public static void sendToAll(Object packet) {
        CHANNEL.send(PacketDistributor.ALL.noArg(), packet);
    }
}
