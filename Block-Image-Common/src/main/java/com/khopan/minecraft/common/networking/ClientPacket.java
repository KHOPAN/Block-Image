package com.khopan.minecraft.common.networking;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

public interface ClientPacket extends Packet {
	default void executeOnServer(MinecraftServer server, ServerPlayer player, ServerLevel level) {

	}

	@Override
	default void handle(PacketDirection direction, ServerPlayer player) {
		this.executeOnServer(player.server, player, player.serverLevel());
	}
}
