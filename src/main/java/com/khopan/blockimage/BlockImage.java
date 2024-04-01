package com.khopan.blockimage;

import java.util.Iterator;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

@Mod(BlockImage.MOD_ID)
public class BlockImage {
	public static final String MOD_NAME = "Block Image";
	public static final String MOD_ID = "blockimage";

	public static final Logger LOGGER = LoggerFactory.getLogger(BlockImage.MOD_NAME);

	public BlockImage() {
		BlockImage.LOGGER.info("Initializing {}", BlockImage.MOD_NAME);
		BlockImage.printAllBlocks();
	}

	private static void printAllBlocks() {
		Iterator<Entry<ResourceKey<Block>, Block>> iterator = ForgeRegistries.BLOCKS.getEntries().iterator();

		while(iterator.hasNext()) {
			Entry<ResourceKey<Block>, Block> entry = iterator.next();
			ResourceKey<Block> key = entry.getKey();
			System.out.println(key.toString());
		}
	}
}
