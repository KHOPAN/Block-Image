package com.khopan.blockimage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.khopan.blockimage.packet.client.ImagePacket;
import com.khopan.blockimage.packet.server.RequestImagePacket;
import com.khopan.minecraft.common.networking.PacketRegistry;

import net.minecraft.resources.ResourceLocation;

public class BlockImage {
	private BlockImage() {}

	public static final String MOD_NAME = "Block Image";
	public static final String MOD_ID = "blockimage";

	public static final Logger LOGGER = LoggerFactory.getLogger(BlockImage.MOD_NAME);

	public static void initialize() {
		BlockImage.LOGGER.info("Initializing {}", BlockImage.MOD_NAME);
		PacketRegistry.register(RequestImagePacket.class);
		PacketRegistry.register(ImagePacket.class);
	}

	public static ResourceLocation location(String path) {
		return new ResourceLocation(BlockImage.MOD_ID, path);
	}
}
