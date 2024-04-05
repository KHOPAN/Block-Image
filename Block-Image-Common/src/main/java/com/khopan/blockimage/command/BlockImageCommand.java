package com.khopan.blockimage.command;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

import com.khopan.blockimage.BlockImage;
import com.khopan.blockimage.command.argument.HandSideArgumentType;
import com.khopan.blockimage.command.argument.HandSideArgumentType.HandSide;
import com.khopan.blockimage.command.placer.ImagePlacer;
import com.khopan.minecraft.common.command.argument.FileArgumentType;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

public class BlockImageCommand {
	private BlockImageCommand() {}

	private static final SimpleCommandExceptionType ERROR_INVALID_IMAGE_FILE = new SimpleCommandExceptionType(Component.translatable("error.command.blockimage.invalid_image_file"));
	private static final SimpleCommandExceptionType ERROR_INVALID_DIMENSION = new SimpleCommandExceptionType(Component.translatable("error.command.blockimage.image_too_small"));

	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		dispatcher.register(Commands.literal("blockimage")
				.requires(source -> source.hasPermission(2) && source.isPlayer())
				.then(Commands.argument("position", BlockPosArgument.blockPos())
						.then(Commands.argument("imageFile", FileArgumentType.file())
								.executes(context -> BlockImageCommand.placeImage(context, FileArgumentType.getFile(context, "imageFile"), HandSide.RIGHT, 0, 0))
								.then(Commands.argument("side", HandSideArgumentType.handSide())
										.executes(context -> BlockImageCommand.placeImage(context, FileArgumentType.getFile(context, "imageFile"), HandSideArgumentType.getHandSide(context, "side"), 0, 0))
										.then(Commands.argument("width", IntegerArgumentType.integer(0))
												.executes(context -> BlockImageCommand.placeImage(context, FileArgumentType.getFile(context, "imageFile"), HandSideArgumentType.getHandSide(context, "side"), IntegerArgumentType.getInteger(context, "width"), 0))
												.then(Commands.argument("height", IntegerArgumentType.integer(0))
														.executes(context -> BlockImageCommand.placeImage(context, FileArgumentType.getFile(context, "imageFile"), HandSideArgumentType.getHandSide(context, "side"), IntegerArgumentType.getInteger(context, "width"), IntegerArgumentType.getInteger(context, "height")))
														)
												)
										)
								)
						)
				);
	}

	private static int placeImage(CommandContext<CommandSourceStack> context, File file, HandSide side, int width, int height) throws CommandSyntaxException {
		BlockPos position = BlockPosArgument.getLoadedBlockPos(context, "position");
		BufferedImage image;

		try {
			BlockImage.LOGGER.info("Reading image file: {}", file.getAbsolutePath());
			image = ImageIO.read(file);
		} catch(Throwable Errors) {
			throw BlockImageCommand.ERROR_INVALID_IMAGE_FILE.create();
		}

		int imageWidth = image.getWidth();
		int imageHeight = image.getHeight();

		if(imageWidth < 1 || imageHeight < 1) {
			throw BlockImageCommand.ERROR_INVALID_DIMENSION.create();
		}

		CommandSourceStack source = context.getSource();
		ServerPlayer player = source.getPlayer();
		ServerLevel level = source.getLevel();
		Entity cameraEntity = player.getCamera();
		Direction direction = cameraEntity.getDirection();
		int buildLimit = level.getMaxBuildHeight();
		int y = position.getY();
		int verticalLimit = buildLimit - y;
		boolean hasWidth = width > 0;
		boolean hasHeight = height > 0;
		int resultWidth;
		int resultHeight;

		if(hasWidth) {
			resultWidth = width;

			if(hasHeight) {
				resultHeight = height;
			} else {
				resultHeight = (int) Math.round(((double) imageHeight) / ((double) imageWidth) * ((double) width));
			}
		} else {
			if(hasHeight) {
				resultWidth = (int) Math.round(((double) imageWidth) / ((double) imageHeight) * ((double) height));
				resultHeight = height;
			} else {
				resultWidth = imageWidth;
				resultHeight = imageHeight;
			}
		}

		if(resultHeight > verticalLimit) {
			resultWidth = (int) Math.round(((double) resultWidth) / ((double) resultHeight) * ((double) verticalLimit));
			resultHeight = verticalLimit;
		}

		BufferedImage bufferedImage;

		if(imageWidth == resultWidth && imageHeight == resultHeight) {
			bufferedImage = image;
		} else {
			Image scaledImage = image.getScaledInstance(resultWidth, resultHeight, BufferedImage.SCALE_SMOOTH);
			bufferedImage = new BufferedImage(resultWidth, resultHeight, BufferedImage.TYPE_INT_ARGB);
			Graphics2D Graphics = bufferedImage.createGraphics();
			Graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			Graphics.drawImage(scaledImage, 0, 0, null);
			Graphics.dispose();
		}

		StringBuilder builder = new StringBuilder();
		builder.append("Input size: ");
		builder.append(image.getWidth());
		builder.append('x');
		builder.append(image.getHeight());
		builder.append("\nOutput size: ");
		builder.append(resultWidth);
		builder.append('x');
		builder.append(resultHeight);
		player.sendSystemMessage(Component.literal(builder.toString()));
		ImagePlacer.place(bufferedImage, player, level, position.above(resultHeight - 1), direction, side);
		return Command.SINGLE_SUCCESS;
	}
}
