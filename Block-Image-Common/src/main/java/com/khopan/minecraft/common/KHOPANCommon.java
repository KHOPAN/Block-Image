package com.khopan.minecraft.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.resources.ResourceLocation;

public class KHOPANCommon {
	private KHOPANCommon() {}

	public static final String NAME = "KHOPAN Common";
	public static final String IDENTIFIER = "khopancommon";
	public static final String VERSION = "1.0.0";
	public static final String NETWORK_PROTOCOL_VERSION = "1.0.0";

	public static final Logger LOGGER = LoggerFactory.getLogger(KHOPANCommon.NAME);

	public static ResourceLocation location(String path) {
		if(path == null) {
			throw new NullPointerException("Path cannot be null");
		}

		return new ResourceLocation(KHOPANCommon.IDENTIFIER, path);
	}
}
