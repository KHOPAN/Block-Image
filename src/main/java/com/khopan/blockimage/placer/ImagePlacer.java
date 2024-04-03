package com.khopan.blockimage.placer;

import java.awt.image.BufferedImage;
import java.util.List;

import com.khopan.blockimage.command.argument.HandSide;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;

public class ImagePlacer {
	private ImagePlacer() {}

	private static final SimpleCommandExceptionType ERROR_NO_BLOCK_AVAILABLE = new SimpleCommandExceptionType(Component.translatable("error.command.blockimage.no_blocks_available"));

	public static void place(BufferedImage image, BlockPos position, ServerLevel level, Direction direction, HandSide side) throws CommandSyntaxException {
		List<BlockEntry> blockList = BlockList.get();

		if(blockList.isEmpty()) {
			throw ImagePlacer.ERROR_NO_BLOCK_AVAILABLE.create();
		}

		int width = image.getWidth();
		int height = image.getHeight();
		int stepX = direction.getStepX();
		int stepZ = direction.getStepZ();

		for(int y = 0; y < height; y++) {
			for(int x = 0; x < width; x++) {
				int pixel = image.getRGB(HandSide.LEFT.equals(side) ? width - x - 1 : x, y);
				BlockEntry entry = BlockList.findClosest(blockList, pixel);
				BlockPos location = position.offset(x * stepX, -y, x * stepZ);
				level.setBlockAndUpdate(location, entry.block.defaultBlockState());
			}
		}
	}
}
