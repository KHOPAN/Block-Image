package com.khopan.blockimage.command.placer;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import com.khopan.blockimage.BlockImage;
import com.khopan.blockimage.command.argument.HandSideArgumentType.HandSide;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;

public class BlockRegion {
	private static final int PARTITION_SIZE = 3000;

	private final int width;
	private final int height;
	private final BlockState[] blockList;

	public BlockRegion(int width, int height) {
		this.width = width;
		this.height = height;
		this.blockList = new BlockState[this.width * this.height];
	}

	public int placeInLevel(ServerLevel level, BlockPos origin, Direction direction, HandSide side) {
		int stepX = direction.getStepX();
		int stepZ = direction.getStepZ();
		int count = 0;

		for(int y = 0; y < this.height; y++) {
			for(int x = 0; x < this.width; x++) {
				BlockState state = this.blockList[this.width * y + x];

				if(state == null) {
					continue;
				}

				BlockPos position = origin.offset(x * stepX, -y, x * stepZ);

				if(level.setBlock(position, state, 2)) {
					count++;
				}
			}
		}

		return count;
	}

	public void start(BufferedImage image, HandSide side, List<BlockEntry> blockList) {
		List<Thread> threadList = new ArrayList<>();
		int remaining = this.blockList.length;
		int index = -1;
		int threadNumber = 0;

		while(true) {
			int count = Math.min(Math.max(remaining, 0), BlockRegion.PARTITION_SIZE);

			if(count == 0) {
				break;
			}

			remaining -= BlockRegion.PARTITION_SIZE;
			int start = index + 1;
			index += count;
			int end = index;
			Thread thread = new Thread(() -> this.worker(image, side, blockList, start, end));
			threadNumber++;
			thread.setName("Block Image Builder Thread #" + threadNumber);
			thread.setPriority(6);
			threadList.add(thread);
			thread.start();
		}

		BlockImage.LOGGER.info("Started {} worker thread{}", threadNumber, threadNumber == 1 ? "" : "s");

		for(int i = 0; i < threadList.size(); i++) {
			try {
				threadList.get(i).join();
			} catch(Throwable Errors) {

			}
		}
	}

	private void worker(BufferedImage image, HandSide side, List<BlockEntry> blockList, int start, int end) {
		for(int i = start; i <= end; i++) {
			try {
				int x = i % this.width;
				int y = i / this.width;
				int pixel = image.getRGB(HandSide.LEFT.equals(side) ? this.width - x - 1 : x, y);
				BlockEntry entry = BlockList.findClosest(blockList, pixel);
				this.blockList[i] = entry.block.defaultBlockState();
			} catch(Throwable Errors) {
				Errors.printStackTrace();;
			}
		}
	}
}
