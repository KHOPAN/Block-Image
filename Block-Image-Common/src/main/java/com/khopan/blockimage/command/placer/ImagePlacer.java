package com.khopan.blockimage.command.placer;

import java.awt.image.BufferedImage;
import java.util.List;

import com.khopan.blockimage.BlockImage;
import com.khopan.blockimage.command.argument.HandSideArgumentType.HandSide;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

public class ImagePlacer {
	private ImagePlacer() {}

	public static void place(BufferedImage image, ServerPlayer player, ServerLevel level, BlockPos position, Direction direction, HandSide side) {
		List<BlockEntry> blockList = BlockList.get();

		if(blockList.isEmpty()) {
			player.sendSystemMessage(Component.empty().append(Component.translatable("error.command.blockimage.no_blocks_available")).withStyle(ChatFormatting.RED));
			return;
		}

		long time = System.currentTimeMillis();
		int width = image.getWidth();
		int height = image.getHeight();
		BlockRegion region = new BlockRegion(width, height);
		region.start(image, side, blockList);
		int blocks = region.placeInLevel(level, position, direction, side);
		player.sendSystemMessage(blocks == 1 ? Component.translatable("success.command.blockimage.filled.one") : Component.translatable("success.command.blockimage.filled.multiple", Integer.toString(blocks)));
		time = System.currentTimeMillis() - time;
		BlockImage.LOGGER.info("Block placing took {}ms", time);
	}
}
