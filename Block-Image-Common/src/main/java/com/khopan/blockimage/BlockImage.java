package com.khopan.blockimage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.resources.ResourceLocation;

public class BlockImage {
	private BlockImage() {}

	public static final String MOD_NAME = "Block Image";
	public static final String MOD_ID = "blockimage";

	public static final Logger LOGGER = LoggerFactory.getLogger(BlockImage.MOD_NAME);

	public static void initialize() {
		BlockImage.LOGGER.info("Initializing {}", BlockImage.MOD_NAME);
	}

	public static ResourceLocation location(String path) {
		return new ResourceLocation(BlockImage.MOD_ID, path);
	}
}
