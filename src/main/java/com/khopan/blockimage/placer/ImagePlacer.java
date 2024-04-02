package com.khopan.blockimage.placer;

import java.awt.image.BufferedImage;
import java.util.List;

import com.khopan.blockimage.placer.score.ColorScore;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;

public class ImagePlacer {
	private ImagePlacer() {}

	private static final SimpleCommandExceptionType ERROR_NO_BLOCK_AVAILABLE = new SimpleCommandExceptionType(Component.literal("Error: No blocks available"));

	public static void place(BufferedImage image, BlockPos position, ServerLevel level) throws CommandSyntaxException {
		List<BlockEntry> blockList = BlockList.get();

		if(blockList.isEmpty()) {
			throw ImagePlacer.ERROR_NO_BLOCK_AVAILABLE.create();
		}

		int width = image.getWidth();
		int height = image.getHeight();

		for(int y = 0; y < height; y++) {
			for(int x = 0; x < width; x++) {
				int pixel = image.getRGB(x, y);
				BlockEntry entry = BlockList.findClosest(blockList, pixel, ColorScore.SIMPLE);
				BlockPos location = position.offset(x, -y, 0);
				level.setBlock(location, entry.block.defaultBlockState(), 11);
			}
		}
	}
}
