package com.khopan.minecraft.common.fabric;

import java.lang.reflect.Field;
import java.util.List;

import com.khopan.minecraft.common.networking.ClientPacket;
import com.khopan.minecraft.common.networking.MultiplatformPacketHandler;
import com.khopan.minecraft.common.networking.Packet;
import com.khopan.minecraft.common.networking.PacketDirection;
import com.khopan.minecraft.common.networking.PacketRegistry;
import com.khopan.minecraft.common.networking.ServerPacket;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public class FabricPacketHandler implements MultiplatformPacketHandler {
	private static boolean Initialized = false;

	private FabricPacketHandler() {}

	@Override
	public void sendToClient(ServerPacket packet, ServerPlayer player) {
		ResourceLocation identifier = packet.getPacketIdentifier();

		if(identifier == null) {
			throw new NullPointerException("The provided packet has null identifier");
		}

		FriendlyByteBuf buffer = PacketByteBufs.create();
		packet.encode(buffer);
		ServerPlayNetworking.send(player, identifier, buffer);
	}

	@Override
	public void sendToServer(ClientPacket packet) {
		ResourceLocation identifier = packet.getPacketIdentifier();

		if(identifier == null) {
			throw new NullPointerException("The provided packet has null identifier");
		}

		FriendlyByteBuf buffer = PacketByteBufs.create();
		packet.encode(buffer);
		ClientPlayNetworking.send(identifier, buffer);
	}

	@SuppressWarnings("unchecked")
	public static <T extends Packet> void initialize() {
		if(FabricPacketHandler.Initialized) {
			return;
		}

		FabricPacketHandler.Initialized = true;

		try {
			Field field = PacketRegistry.class.getDeclaredField("Handler");
			field.setAccessible(true);
			field.set(null, new FabricPacketHandler());
		} catch(Throwable Errors) {
			Errors.printStackTrace();
		}

		try {
			Field field = PacketRegistry.class.getDeclaredField("PACKET_LIST");
			field.setAccessible(true);
			List<PacketClassEntry> list = (List<PacketClassEntry>) field.get(null);

			for(int i = 0; i < list.size(); i++) {
				PacketClassEntry entry = list.get(i);
				Packet packet = FabricPacketHandler.constructPacket(entry.packetClass);
				ResourceLocation identifier = packet.getPacketIdentifier();

				if(entry.isClient) {
					ServerPlayNetworking.registerGlobalReceiver(identifier, (server, player, listener, buffer, sender) -> server.execute(() -> FabricPacketHandler.receive(buffer, PacketDirection.CLIENT_TO_SERVER, player, entry.packetClass)));
				} else {
					ClientPlayNetworking.registerGlobalReceiver(identifier, (minecraft, listener, buffer, sender) -> minecraft.execute(() -> FabricPacketHandler.receive(buffer, PacketDirection.SERVER_TO_CLIENT, null, entry.packetClass)));
				}
			}
		} catch(Throwable Errors) {
			Errors.printStackTrace();
		}
	}

	private static void receive(FriendlyByteBuf buffer, PacketDirection direction, ServerPlayer player, Class<? extends Packet> packetClass) {
		Packet packet = FabricPacketHandler.constructPacket(packetClass);
		packet.decode(buffer);
		packet.handle(direction, player);
	}

	private static <T extends Packet> T constructPacket(Class<T> packetClass) {
		T packet;

		try {
			packet = (T) packetClass.getConstructor().newInstance();
		} catch(Throwable Errors) {
			throw new InternalError("Error while constructing new packet", Errors);
		}

		return packet;
	}
}
