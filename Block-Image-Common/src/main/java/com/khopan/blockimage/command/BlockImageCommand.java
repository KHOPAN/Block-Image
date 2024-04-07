package com.khopan.blockimage.command;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.imageio.ImageIO;

import com.khopan.blockimage.BlockImage;
import com.khopan.blockimage.command.argument.HandSideArgumentType;
import com.khopan.blockimage.command.argument.HandSideArgumentType.HandSide;
import com.khopan.blockimage.command.placer.ImagePlacer;
import com.khopan.blockimage.packet.server.RequestImagePacket;
import com.khopan.minecraft.common.command.argument.FileArgumentType;
import com.khopan.minecraft.common.networking.PacketRegistry;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.ChatFormatting;
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

	private static final List<Session> SESSION_LIST = new ArrayList<>();

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
		CommandSourceStack source = context.getSource();
		ServerPlayer player = source.getPlayer();
		Entity camera = player.getCamera();
		Direction direction = camera.getDirection();
		Session session = new Session();
		session.sessionId = Session.generateRandomSessionId();
		session.player = player;
		session.position = position;
		session.direction = direction;
		session.side = side;
		session.width = width;
		session.height = height;
		BlockImageCommand.SESSION_LIST.add(session);
		PacketRegistry.client(RequestImagePacket.create(file, session.sessionId), player);
		return Command.SINGLE_SUCCESS;
	}

	public static void place(boolean hasError, int errorCode, byte[] imageData, long sessionId) {
		Session session = BlockImageCommand.findSession(sessionId);

		if(session == null) {
			BlockImage.LOGGER.warn("Block Image session with session identifier: {} not found", sessionId);
			return;
		}

		BlockImageCommand.SESSION_LIST.remove(session);
		BlockImage.LOGGER.info("Processing Block Image session with session identifier: {}", sessionId);
		BufferedImage image = null;

		try {
			image = ImageIO.read(new ByteArrayInputStream(imageData));
		} catch(Throwable Errors) {
			hasError = true;
			errorCode = 3;
		}

		if(hasError) {
			Component message = null;

			switch(errorCode) {
			case 0:
				message = Component.translatable("error.command.blockimage.invalid_image_file");
				break;
			case 1:
				message = Component.translatable("error.command.blockimage.image_too_small");
				break;
			case 2:
				message = Component.translatable("error.command.blockimage.serialize_image");
				break;
			case 3:
				message = Component.translatable("error.command.blockimage.deserialize_image");
				break;
			}

			if(message != null) {
				session.player.sendSystemMessage(Component.empty().append(message).withStyle(ChatFormatting.RED));
			}

			return;
		}

		int imageWidth = image.getWidth();
		int imageHeight = image.getHeight();
		ServerLevel level = session.player.serverLevel();
		int buildLimit = level.getMaxBuildHeight();
		int y = session.position.getY();
		int verticalLimit = buildLimit - y;
		boolean hasWidth = session.width > 0;
		boolean hasHeight = session.height > 0;
		int resultWidth;
		int resultHeight;

		if(hasWidth) {
			resultWidth = session.width;

			if(hasHeight) {
				resultHeight = session.height;
			} else {
				resultHeight = (int) Math.round(((double) imageHeight) / ((double) imageWidth) * ((double) session.width));
			}
		} else {
			if(hasHeight) {
				resultWidth = (int) Math.round(((double) imageWidth) / ((double) imageHeight) * ((double) session.height));
				resultHeight = session.height;
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

		session.player.sendSystemMessage(Component.translatable("success.command.blockimage.blockimage_info", imageWidth + 'x' + imageHeight, resultWidth + 'x' + resultHeight));
		ImagePlacer.place(bufferedImage, session.player, level, session.position.above(resultHeight - 1), session.direction, session.side);
	}

	private static Session findSession(long sessionId) {
		for(int i = 0; i < BlockImageCommand.SESSION_LIST.size(); i++) {
			Session session = BlockImageCommand.SESSION_LIST.get(i);

			if(session.sessionId == sessionId) {
				return session;
			}
		}

		return null;
	}

	private static class Session {
		private long sessionId;
		private ServerPlayer player;
		private BlockPos position;
		private Direction direction;
		private HandSide side;
		private int width;
		private int height;

		private static long generateRandomSessionId() {
			Random random = new Random(System.nanoTime());
			return Math.round(random.nextDouble() * ((double) Long.MAX_VALUE));
		}
	}
}
