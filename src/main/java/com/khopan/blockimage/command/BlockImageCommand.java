package com.khopan.blockimage.command;

import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

import com.khopan.blockimage.HandSide;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;

public class BlockImageCommand {
	private BlockImageCommand() {}

	// Temporary (literal)
	private static final SimpleCommandExceptionType ERROR_FILE_NOT_FOUND = new SimpleCommandExceptionType(Component.literal("Cannot find the path specified"));
	private static final SimpleCommandExceptionType ERROR_INVALID_FILE = new SimpleCommandExceptionType(Component.literal("Invalid file type"));

	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		dispatcher.register(Commands.literal("blockimage").requires(source -> source.hasPermission(2))
				.then(Commands.argument("imageFile", StringArgumentType.string())
						.then(Commands.argument("position", BlockPosArgument.blockPos())
								.executes(BlockImageCommand :: placeImageImageFile)
								)
						)
				);
	}

	private static int placeImageImageFile(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
		return BlockImageCommand.placeImage(context, HandSide.RIGHT);
	}

	private static int placeImage(CommandContext<CommandSourceStack> context, HandSide side) throws CommandSyntaxException {
		BlockPos position = BlockPosArgument.getLoadedBlockPos(context, "position");
		String imageFile = StringArgumentType.getString(context, "imageFile");
		File file = new File(imageFile);

		if(!file.exists()) {
			throw BlockImageCommand.ERROR_FILE_NOT_FOUND.create();
		}

		BufferedImage image;

		try {
			image = ImageIO.read(file);
		} catch(Throwable Errors) {
			throw BlockImageCommand.ERROR_INVALID_FILE.create();
		}

		CommandSourceStack source = context.getSource();
		ServerLevel level = source.getLevel();
		level.setBlock(position, Blocks.STONE.defaultBlockState(), 0b00001011);
		source.sendSystemMessage(Component.literal("Block Image executed successfully! Image: " + image.getWidth() + "x" + image.getHeight()));
		return Command.SINGLE_SUCCESS;
	}
}
