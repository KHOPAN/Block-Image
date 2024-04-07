package com.khopan.blockimage.packet.client;

import com.khopan.blockimage.BlockImage;
import com.khopan.blockimage.command.BlockImageCommand;
import com.khopan.minecraft.common.networking.ClientPacket;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

public class ImagePacket implements ClientPacket {
	private static final ResourceLocation PACKET_IDENTIFIER = BlockImage.location("image_packet");

	private boolean hasError;
	private int errorCode;
	private byte[] imageData;
	private long sessionId;

	@Override
	public ResourceLocation getPacketIdentifier() {
		return ImagePacket.PACKET_IDENTIFIER;
	}

	@Override
	public void encode(FriendlyByteBuf buffer) {
		buffer.writeBoolean(this.hasError);

		if(this.hasError) {
			buffer.writeInt(this.errorCode);
		} else {
			buffer.writeByteArray(this.imageData);			
		}

		buffer.writeLong(this.sessionId);
	}

	@Override
	public void decode(FriendlyByteBuf buffer) {
		this.hasError = buffer.readBoolean();

		if(this.hasError) {
			this.errorCode = buffer.readInt();
		} else {
			this.imageData = buffer.readByteArray();
		}

		this.sessionId = buffer.readLong();
	}

	@Override
	public void executeOnServer(MinecraftServer server, ServerPlayer player, ServerLevel level) {
		BlockImageCommand.place(this.hasError, this.errorCode, this.imageData, this.sessionId);
	}

	public static ImagePacket create(boolean hasError, int errorCode, byte[] imageData, long sessionId) {
		ImagePacket packet = new ImagePacket();
		packet.hasError = hasError;
		packet.errorCode = errorCode;
		packet.imageData = imageData;
		packet.sessionId = sessionId;
		return packet;
	}
}
