package com.khopan.blockimage.command;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

import com.khopan.blockimage.command.argument.HandSide;
import com.khopan.blockimage.command.argument.HandSideArgumentType;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
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

	// Temporary (literal)
	private static final SimpleCommandExceptionType ERROR_FILE_NOT_FOUND = new SimpleCommandExceptionType(Component.literal("Cannot find the path specified"));
	private static final SimpleCommandExceptionType ERROR_INVALID_FILE = new SimpleCommandExceptionType(Component.literal("Invalid file type"));
	private static final SimpleCommandExceptionType ERROR_INVALID_DIMENSION = new SimpleCommandExceptionType(Component.literal("Invalid image dimension: Image too small"));

	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		dispatcher.register(Commands.literal("blockimage").requires(source -> source.hasPermission(2) && source.isPlayer())
				.then(Commands.argument("position", BlockPosArgument.blockPos())
						.then(Commands.argument("imageFile", StringArgumentType.string())
								.executes(context -> BlockImageCommand.placeImage(context, HandSide.RIGHT, 0, 0))
								.then(Commands.argument("side", HandSideArgumentType.handSide())
										.executes(context -> BlockImageCommand.placeImage(context, HandSideArgumentType.getHandSide(context, "side"), 0, 0))
										.then(Commands.argument("width", IntegerArgumentType.integer(0))
												.executes(context -> BlockImageCommand.placeImage(context, HandSideArgumentType.getHandSide(context, "side"), IntegerArgumentType.getInteger(context, "width"), 0))
												.then(Commands.argument("height", IntegerArgumentType.integer(0))
														.executes(context -> BlockImageCommand.placeImage(context, HandSideArgumentType.getHandSide(context, "side"), IntegerArgumentType.getInteger(context, "width"), IntegerArgumentType.getInteger(context, "height")))
														)
												)
										)
								)
						)
				);
	}

	private static int placeImage(CommandContext<CommandSourceStack> context, HandSide side, int width, int height) throws CommandSyntaxException {
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

		Image scaledImage = image.getScaledInstance(resultWidth, resultHeight, BufferedImage.SCALE_SMOOTH);
		StringBuilder builder = new StringBuilder();
		builder.append("Image size: ");
		builder.append(image.getWidth());
		builder.append('x');
		builder.append(image.getHeight());
		builder.append("\nFacing: ");
		builder.append(direction.toString());
		builder.append("\nHeight Limit: ");
		builder.append(verticalLimit);
		builder.append("\nCalculated size: ");
		builder.append(scaledImage.getWidth(null));
		builder.append('x');
		builder.append(scaledImage.getHeight(null));
		source.sendSystemMessage(Component.literal(builder.toString()));

		/*try {
			Minecraft minecraft = Minecraft.getInstance();
			ResourceManager manager = minecraft.getResourceManager();
			Optional<Resource> optional = manager.getResource(new ResourceLocation("minecraft", "textures/block/diamond_block.png"));

			if(optional.isPresent()) {
				Resource resource = optional.get();
				InputStream stream = resource.open();

				if(stream != null) {
					BufferedImage imageResource = ImageIO.read(stream);
					stream.close();

					if(imageResource != null) {
						ImageIO.write(imageResource, "png", new File("C:\\Users\\puthi\\Downloads\\texture.png"));
						System.out.println(imageResource.getWidth() + "x" + imageResource.getHeight());
					}
				}
			}
		} catch(Throwable Errors) {
			Errors.printStackTrace();
		}*/

		return Command.SINGLE_SUCCESS;
	}
}
