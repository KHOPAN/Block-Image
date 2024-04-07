package com.khopan.minecraft.common.networking;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.EntityGetter;

public interface MultiplatformPacketHandler {
	void sendToClient(ServerPacket packet, ServerPlayer player);
	void sendToServer(ClientPacket packet);

	default void sendToClients(ServerPacket packet, EntityGetter level) {
		level.players().forEach(player -> this.sendToClient(packet, (ServerPlayer) player));
	}

	public static class PacketClassEntry {
		public boolean isClient;
		public Class<? extends Packet> packetClass;
	}
}
