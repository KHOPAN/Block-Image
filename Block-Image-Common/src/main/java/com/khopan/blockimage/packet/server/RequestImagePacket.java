package com.khopan.blockimage.packet.server;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.charset.StandardCharsets;

import javax.imageio.ImageIO;

import com.khopan.blockimage.BlockImage;
import com.khopan.blockimage.packet.client.ImagePacket;
import com.khopan.minecraft.common.networking.PacketRegistry;
import com.khopan.minecraft.common.networking.ServerPacket;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public class RequestImagePacket implements ServerPacket {
	private static final ResourceLocation PACKET_IDENTIFIER = BlockImage.location("request_image_packet");

	private File file;
	private long sessionId;

	@Override
	public ResourceLocation getPacketIdentifier() {
		return RequestImagePacket.PACKET_IDENTIFIER;
	}

	@Override
	public void encode(FriendlyByteBuf buffer) {
		buffer.writeByteArray(this.file.getAbsolutePath().getBytes(StandardCharsets.UTF_8));
		buffer.writeLong(this.sessionId);
	}

	@Override
	public void decode(FriendlyByteBuf buffer) {
		this.file = new File(new String(buffer.readByteArray(), StandardCharsets.UTF_8));
		this.sessionId = buffer.readLong();
	}

	@Override
	public void executeOnClient(Minecraft minecraft) {
		Thread thread = new Thread(() -> {
			boolean hasError = false;
			int errorCode = 0;
			BufferedImage image = null;

			try {
				BlockImage.LOGGER.info("Reading image file: {}", this.file.getAbsolutePath());
				image = ImageIO.read(this.file);
			} catch(Throwable Errors) {
				hasError = true;
				errorCode = 0;
			}

			if(!hasError) {
				int imageWidth = image.getWidth();
				int imageHeight = image.getHeight();

				if(imageWidth < 1 || imageHeight < 1) {
					hasError = true;
					errorCode = 1;
				}
			}

			byte[] imageData = null;

			if(!hasError) {
				try {
					ByteArrayOutputStream stream = new ByteArrayOutputStream();
					ImageIO.write(image, "png", stream);
					imageData = stream.toByteArray();
				} catch(Throwable Errors) {
					hasError = true;
					errorCode = 2;
				}
			}

			PacketRegistry.server(ImagePacket.create(hasError, errorCode, imageData, this.sessionId));
		});

		thread.setName("Block Image Reader Thread");
		thread.setPriority(6);
		thread.start();
	}

	public static RequestImagePacket create(File file, long sessionId) {
		RequestImagePacket packet = new RequestImagePacket();
		packet.file = file;
		packet.sessionId = sessionId;
		return packet;
	}
}
