package com.khopan.minecraft.common.networking;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import com.khopan.minecraft.common.KHOPANCommon;
import com.khopan.minecraft.common.networking.MultiplatformPacketHandler.PacketClassEntry;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.EntityGetter;

public class PacketRegistry {
	private PacketRegistry() {}

	private static final List<PacketClassEntry> PACKET_LIST = new ArrayList<>();

	private static MultiplatformPacketHandler Handler = null;

	public static void register(Class<? extends Packet> packetClass) {
		if(packetClass == null) {
			throw new NullPointerException("Packet class cannot be null");
		}

		if(PacketRegistry.Handler != null) {
			throw new IllegalStateException("Cannot register at this state");
		}

		int modifiers = packetClass.getModifiers();

		if(Modifier.isInterface(modifiers)) {
			return;
		}

		boolean isClient = false;

		if(ClientPacket.class.isAssignableFrom(packetClass)) {
			isClient = true;
		} else if(ServerPacket.class.isAssignableFrom(packetClass)) {
			isClient = false;
		} else {
			throw new InternalError("Packet class must be subclass of either ClientPacket or ServerPacket, you cannot implements Packet directly");
		}

		KHOPANCommon.LOGGER.info("Registering packet: {}", packetClass.getName());
		PacketClassEntry entry = new PacketClassEntry();
		entry.isClient = isClient;
		entry.packetClass = packetClass;
		PacketRegistry.PACKET_LIST.add(entry);
	}

	public static void client(ServerPacket packet, ServerPlayer player) {
		if(packet == null) {
			throw new NullPointerException("Packet cannot be null");
		}

		if(player == null) {
			throw new NullPointerException("Player cannot be null");
		}

		PacketRegistry.checkHandler();
		PacketRegistry.checkPacket(packet);
		KHOPANCommon.LOGGER.info("Sending server to client packet: " + packet.getClass().getName());
		PacketRegistry.Handler.sendToClient(packet, player);
	}

	public static void clients(ServerPacket packet, EntityGetter level) {
		if(packet == null) {
			throw new NullPointerException("Packet cannot be null");
		}

		if(level == null) {
			throw new NullPointerException("Level cannot be null");
		}

		PacketRegistry.checkHandler();
		PacketRegistry.checkPacket(packet);
		KHOPANCommon.LOGGER.info("Sending server to all clients packet: " + packet.getClass().getName());
		PacketRegistry.Handler.sendToClients(packet, level);
	}

	public static void server(ClientPacket packet) {
		if(packet == null) {
			throw new NullPointerException("Packet cannot be null");
		}

		PacketRegistry.checkHandler();
		PacketRegistry.checkPacket(packet);
		KHOPANCommon.LOGGER.info("Sending client to server packet: " + packet.getClass().getName());
		PacketRegistry.Handler.sendToServer(packet);
	}

	private static void checkHandler() {
		if(PacketRegistry.Handler == null) {
			throw new InternalError("Handler not provided");
		}
	}

	private static void checkPacket(Packet packet) {
		Class<?> packetClass = packet.getClass();

		for(int i = 0; i < PacketRegistry.PACKET_LIST.size(); i++) {
			PacketClassEntry entry = PacketRegistry.PACKET_LIST.get(i);

			if(packetClass.equals(entry.packetClass)) {
				return;
			}
		}

		throw new IllegalArgumentException("Unknown packet: " + packetClass.getName());
	}
}
