package com.khopan.blockimage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BlockImage {
	private BlockImage() {}

	public static final String MOD_NAME = "Block Image";
	public static final String MOD_ID = "blockimage";

	public static final Logger LOGGER = LoggerFactory.getLogger(BlockImage.MOD_NAME);

	public static void initialize() {
		BlockImage.LOGGER.info("Initializing {}", BlockImage.MOD_NAME);
	}
}
