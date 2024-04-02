package com.khopan.blockimage.placer;

import java.awt.image.BufferedImage;

import net.minecraft.world.level.block.Block;

public class BlockEntry {
	public final Block block;
	public final String name;
	public final int alpha;
	public final int red;
	public final int green;
	public final int blue;

	public BlockEntry(Block block, String name, BufferedImage image) {
		this.block = block;
		this.name = name == null ? "" : name;
		int width = image.getWidth();
		int height = image.getHeight();
		long alphaTotal = 0L;
		long redTotal = 0L;
		long greenTotal = 0L;
		long blueTotal = 0L;

		for(int y = 0; y < height; y++) {
			for(int x = 0; x < width; x++) {
				int color = image.getRGB(x, y);
				int alpha = (color >> 24) & 0xFF;
				int red = (color >> 16) & 0xFF;
				int green = (color >> 8) & 0xFF;
				int blue = color & 0xFF;
				alphaTotal += alpha;
				redTotal += red;
				greenTotal += green;
				blueTotal += blue;
			}
		}

		long pixels = width * height;
		this.alpha = (int) Math.round(((double) alphaTotal) / ((double) pixels));
		this.red = (int) Math.round(((double) redTotal) / ((double) pixels));
		this.green = (int) Math.round(((double) greenTotal) / ((double) pixels));
		this.blue = (int) Math.round(((double) blueTotal) / ((double) pixels));
	}
}
