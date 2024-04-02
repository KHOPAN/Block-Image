package com.khopan.blockimage.placer;

import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import javax.imageio.ImageIO;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CoralBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.registries.ForgeRegistries;

public class BlockList {
	private BlockList() {}

	public static List<BlockEntry> get() {
		Minecraft minecraft = Minecraft.getInstance();
		ResourceManager manager = minecraft.getResourceManager();
		Iterator<Block> iterator = ForgeRegistries.BLOCKS.getValues().iterator();
		List<BlockEntry> list = new ArrayList<>();

		while(iterator.hasNext()) {
			Block block = iterator.next();
			BlockState state = block.defaultBlockState();
			VoxelShape shape = state.getShape(EmptyBlockGetter.INSTANCE, BlockPos.ZERO);

			if(!Block.isShapeFullBlock(shape) || !state.isSolidRender(EmptyBlockGetter.INSTANCE, BlockPos.ZERO) || block.hasDynamicShape() || block.getLightEmission(state, EmptyBlockGetter.INSTANCE, BlockPos.ZERO) > 0 || block instanceof CoralBlock || block instanceof LeavesBlock) {
				continue;
			}

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

		list.sort((x, y) -> x.name.compareTo(y.name));
		return list;
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
