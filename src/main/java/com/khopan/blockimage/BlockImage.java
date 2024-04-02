package com.khopan.blockimage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.khopan.blockimage.command.BlockImageCommand;
import com.khopan.blockimage.registry.CommandArgumentTypeRegistry;
import com.mojang.brigadier.CommandDispatcher;

import net.minecraft.commands.CommandSourceStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(BlockImage.MOD_ID)
public class BlockImage {
	public static final String MOD_NAME = "Block Image";
	public static final String MOD_ID = "blockimage";

	public static final Logger LOGGER = LoggerFactory.getLogger(BlockImage.MOD_NAME);

	public BlockImage() {
		BlockImage.LOGGER.info("Initializing {}", BlockImage.MOD_NAME);
		FMLJavaModLoadingContext context = FMLJavaModLoadingContext.get();
		IEventBus bus = context.getModEventBus();
		CommandArgumentTypeRegistry.register(bus);
		MinecraftForge.EVENT_BUS.addListener(this :: onRegisterCommand);
	}

	private void onRegisterCommand(RegisterCommandsEvent Event) {
		CommandDispatcher<CommandSourceStack> dispatcher = Event.getDispatcher();
		BlockImageCommand.register(dispatcher);
	}

	/*private static void printAllBlocks() {
		Iterator<Entry<ResourceKey<Block>, Block>> iterator = ForgeRegistries.BLOCKS.getEntries().iterator();

		while(iterator.hasNext()) {
			Entry<ResourceKey<Block>, Block> entry = iterator.next();
			ResourceKey<Block> key = entry.getKey();
			System.out.println(key.toString());
		}
	}*/
}
