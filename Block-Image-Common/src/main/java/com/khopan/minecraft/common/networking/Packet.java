package com.khopan.minecraft.common.networking;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public interface Packet {
	ResourceLocation getPacketIdentifier();
	void encode(FriendlyByteBuf buffer);
	void decode(FriendlyByteBuf buffer);

	default void handle(PacketDirection direction, ServerPlayer player) {

	}
}
