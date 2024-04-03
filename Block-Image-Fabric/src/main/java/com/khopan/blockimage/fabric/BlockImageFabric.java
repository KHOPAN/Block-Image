package com.khopan.blockimage.fabric;

import com.khopan.blockimage.BlockImage;

import net.fabricmc.api.ModInitializer;

public class BlockImageFabric implements ModInitializer {
	@Override
	public void onInitialize() {
		BlockImage.initialize();
	}
}
