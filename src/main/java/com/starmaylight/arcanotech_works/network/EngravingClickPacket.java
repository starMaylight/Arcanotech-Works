package com.starmaylight.arcanotech_works.network;

import com.starmaylight.arcanotech_works.block.engraving.EngravingTableBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * 刻印台のグリッドクリックパケット（C2S）
 */
public class EngravingClickPacket {

    private final BlockPos pos;
    private final int gridX;
    private final int gridY;

    public EngravingClickPacket(BlockPos pos, int gridX, int gridY) {
        this.pos = pos;
        this.gridX = gridX;
        this.gridY = gridY;
    }

    public static void encode(EngravingClickPacket packet, FriendlyByteBuf buf) {
        buf.writeBlockPos(packet.pos);
        buf.writeInt(packet.gridX);
        buf.writeInt(packet.gridY);
    }

    public static EngravingClickPacket decode(FriendlyByteBuf buf) {
        return new EngravingClickPacket(
                buf.readBlockPos(),
                buf.readInt(),
                buf.readInt()
        );
    }

    public static void handle(EngravingClickPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;

            // プレイヤーの範囲内かチェック
            if (player.distanceToSqr(
                    packet.pos.getX() + 0.5,
                    packet.pos.getY() + 0.5,
                    packet.pos.getZ() + 0.5) > 64) {
                return;
            }

            BlockEntity entity = player.level().getBlockEntity(packet.pos);
            if (entity instanceof EngravingTableBlockEntity engravingTable) {
                engravingTable.engrave(packet.gridX, packet.gridY, player);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
