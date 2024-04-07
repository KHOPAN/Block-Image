package com.khopan.minecraft.common.networking;

import net.minecraft.client.Minecraft;
import net.minecraft.server.level.ServerPlayer;

public interface ServerPacket extends Packet {
	default void executeOnClient(Minecraft minecraft) {

	}

	@Override
	default void handle(PacketDirection direction, ServerPlayer player) {
		this.executeOnClient(Minecraft.getInstance());
	}
}
