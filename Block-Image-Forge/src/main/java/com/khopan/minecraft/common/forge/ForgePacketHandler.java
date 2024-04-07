package com.khopan.minecraft.common.forge;

import java.lang.reflect.Field;
import java.util.List;
import java.util.function.Supplier;

import com.khopan.minecraft.common.KHOPANCommon;
import com.khopan.minecraft.common.networking.ClientPacket;
import com.khopan.minecraft.common.networking.MultiplatformPacketHandler;
import com.khopan.minecraft.common.networking.Packet;
import com.khopan.minecraft.common.networking.PacketDirection;
import com.khopan.minecraft.common.networking.PacketRegistry;
import com.khopan.minecraft.common.networking.ServerPacket;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent.Context;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import net.minecraftforge.network.simple.SimpleChannel.MessageBuilder;

public class ForgePacketHandler implements MultiplatformPacketHandler {
	public static final SimpleChannel CHANNEL = NetworkRegistry.ChannelBuilder.named(KHOPANCommon.location("packets"))
			.networkProtocolVersion(() -> KHOPANCommon.NETWORK_PROTOCOL_VERSION)
			.clientAcceptedVersions(version -> KHOPANCommon.NETWORK_PROTOCOL_VERSION.equals(version))
			.serverAcceptedVersions(version -> KHOPANCommon.NETWORK_PROTOCOL_VERSION.equals(version))
			.simpleChannel();

	private static boolean Initialized = false;
	private static int Identifier;

	private ForgePacketHandler() {}

	@Override
	public void sendToClient(ServerPacket packet, ServerPlayer player) {
		ForgePacketHandler.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), packet);
	}

	@Override
	public void sendToServer(ClientPacket packet) {
		ForgePacketHandler.CHANNEL.sendToServer(packet);
	}

	@SuppressWarnings("unchecked")
	public static <T extends Packet> void initialize() {
		if(ForgePacketHandler.Initialized) {
			return;
		}

		ForgePacketHandler.Initialized = true;

		try {
			Field field = PacketRegistry.class.getDeclaredField("Handler");
			field.setAccessible(true);
			field.set(null, new ForgePacketHandler());
		} catch(Throwable Errors) {
			Errors.printStackTrace();
		}

		try {
			Field field = PacketRegistry.class.getDeclaredField("PACKET_LIST");
			field.setAccessible(true);
			List<PacketClassEntry> list = (List<PacketClassEntry>) field.get(null);

			for(int i = 0; i < list.size(); i++) {
				PacketClassEntry entry = list.get(i);
				Class<T> packetClass = (Class<T>) entry.packetClass;
				MessageBuilder<T> builder = ForgePacketHandler.CHANNEL.messageBuilder(packetClass, ForgePacketHandler.Identifier, entry.isClient ? NetworkDirection.PLAY_TO_SERVER : NetworkDirection.PLAY_TO_CLIENT)
						.decoder(buffer -> ForgePacketHandler.decode(buffer, packetClass))
						.encoder(T :: encode);

				builder.consumerNetworkThread((packet, supplier) -> {
					ForgePacketHandler.consumer(packet, supplier, entry.isClient);
				});

				builder.add();
				ForgePacketHandler.Identifier++;
			}
		} catch(Throwable Errors) {
			Errors.printStackTrace();
		}
	}

	private static <T extends Packet> void consumer(T packet, Supplier<Context> supplier, boolean fromClient) {
		Context context = supplier.get();

		if(!fromClient) {
			ForgePacketHandler.consume(packet, context, false);
			return;
		}

		context.enqueueWork(() -> ForgePacketHandler.consume(packet, context, true));
		context.setPacketHandled(true);
	}

	private static <T extends Packet> void consume(T packet, Context context, boolean fromClient) {
		if(fromClient) {
			MinecraftServer server = context.getSender().server;

			if(!server.isSameThread()) {
				return;
			}
		} else {
			if(!Minecraft.getInstance().isSameThread()) {
				return;
			}
		}

		packet.handle(fromClient ? PacketDirection.CLIENT_TO_SERVER : PacketDirection.SERVER_TO_CLIENT, context.getSender());
	}

	private static <T extends Packet> T decode(FriendlyByteBuf buffer, Class<T> packetClass) {
		T packet;

		try {
			packet = (T) packetClass.getConstructor().newInstance();
		} catch(Throwable Errors) {
			throw new InternalError("Error while constructing new packet", Errors);
		}

		packet.decode(buffer);
		return packet;
	}
}
