package com.khopan.blockimage.command.placer;

import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;

import javax.imageio.ImageIO;

import com.khopan.blockimage.BlockImage;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CoralBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;

public class BlockList {
	private BlockList() {}

	private static final int PARTITION_SIZE = 15;

	public static List<BlockEntry> get() {
		BlockImage.LOGGER.info("Getting Minecraft block list");
		long time = System.currentTimeMillis();
		Minecraft minecraft = Minecraft.getInstance();
		ResourceManager manager = minecraft.getResourceManager();
		Iterator<Entry<ResourceKey<Block>, Block>> iterator = BuiltInRegistries.BLOCK.entrySet().iterator();
		time = System.currentTimeMillis() - time;
		BlockImage.LOGGER.info("Block list query took {}ms", time);
		List<BlockState> blockList = new ArrayList<>();
		time = System.currentTimeMillis();

		while(iterator.hasNext()) {
			Entry<ResourceKey<Block>, Block> entry = iterator.next();
			Block block = entry.getValue();
			BlockState state = block.defaultBlockState();
			VoxelShape shape = state.getShape(EmptyBlockGetter.INSTANCE, BlockPos.ZERO);

			if(block instanceof CoralBlock || block instanceof LeavesBlock || !Block.isShapeFullBlock(shape) || !state.isSolidRender(EmptyBlockGetter.INSTANCE, BlockPos.ZERO) || block.hasDynamicShape() || state.getLightEmission() > 0) {
				continue;
			}

			blockList.add(state);
		}

		int blockLength = blockList.size();
		List<Thread> threadList = new ArrayList<>();
		int remaining = blockLength;
		int index = -1;
		int threadNumber = 0;
		List<BlockEntry> list = new ArrayList<>();

		while(true) {
			int count = Math.min(Math.max(remaining, 0), BlockList.PARTITION_SIZE);

			if(count == 0) {
				break;
			}

			remaining -= BlockList.PARTITION_SIZE;
			int start = index + 1;
			index += count;
			List<BlockState> subList = blockList.subList(start, index);
			Thread thread = new Thread(() -> BlockList.worker(subList, manager, list));
			threadNumber++;
			thread.setName("Block Image Query Thread #" + threadNumber);
			thread.setPriority(6);
			threadList.add(thread);
		}

		int threadListSize = threadList.size();

		for(int i = 0; i < threadListSize; i++) {
			threadList.get(i).start();
		}

		BlockImage.LOGGER.info("Started {} worker thread{}", threadNumber, threadNumber == 1 ? "" : "s");

		for(int i = 0; i < threadListSize; i++) {
			try {
				threadList.get(i).join();
			} catch(Throwable Errors) {

			}
		}

		time = System.currentTimeMillis() - time;
		list.sort((x, y) -> x.name.compareTo(y.name));
		BlockImage.LOGGER.info("Block texture averaging took {}ms", time);
		return list;
	}

	private static void worker(List<BlockState> blockList, ResourceManager manager, List<BlockEntry> list) {
		for(int i = 0; i < blockList.size(); i++) {
			BlockState state = blockList.get(i);
			Block block = state.getBlock();
			ModelResourceLocation modelLocation = BlockModelShaper.stateToModelLocation(state);
			String name = modelLocation.getPath();
			ResourceLocation location = new ResourceLocation(modelLocation.getNamespace(), "textures/block/" + name + ".png");
			Optional<Resource> optional = manager.getResource(location);

			if(optional.isPresent()) {
				Resource resource = optional.get();

				try {
					InputStream stream = resource.open();
					BufferedImage image = ImageIO.read(stream);
					stream.close();
					list.add(new BlockEntry(block, name, image));
				} catch(Throwable Errors) {
					Errors.printStackTrace();
				}
			}
		}
	}

	public static BlockEntry findClosest(List<BlockEntry> list, int color) {
		int alpha = (color >> 24) & 0xFF;
		int red = (color >> 16) & 0xFF;
		int green = (color >> 8) & 0xFF;
		int blue = color & 0xFF;

		if(alpha == 0) {
			alpha = 255;
			red = 255;
			green = 255;
			blue = 255;
		}

		double minimumScore = Double.MAX_VALUE;
		BlockEntry closest = null;

		for(int i = 0; i < list.size(); i++) {
			BlockEntry entry = list.get(i);
			double factor = ((double) Math.min(alpha, entry.alpha)) / 255.0d;
			double score = (1.0d - factor) * Math.abs(alpha - entry.alpha) * 3.0d + factor * (Math.abs(red - entry.red) + Math.abs(green - entry.green) + Math.abs(blue - entry.blue));

			if(score < minimumScore) {
				minimumScore = score;
				closest = entry;
			}
		}

		if(closest == null) {
			closest = list.get(0);
		}

		return closest;
	}
}
