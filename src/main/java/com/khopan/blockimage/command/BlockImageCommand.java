package com.khopan.blockimage.command;

import com.khopan.blockimage.HandSide;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;

public class BlockImageCommand {
	private BlockImageCommand() {}

	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		dispatcher.register(LiteralArgumentBuilder.<CommandSourceStack>literal("blockimage").requires(source -> source.hasPermission(2))
				.then(RequiredArgumentBuilder.<CommandSourceStack, String>argument("imageFile", StringArgumentType.string())
						.executes(BlockImageCommand :: placeImageImageFile)
						)
				);
	}

	private static int placeImageImageFile(CommandContext<CommandSourceStack> context) {
		return BlockImageCommand.placeImage(context, HandSide.RIGHT);
	}

	private static int placeImage(CommandContext<CommandSourceStack> context, HandSide side) {
		CommandSourceStack source = context.getSource();
		source.sendSystemMessage(Component.literal("/blockimage command has been executed!"));
		return Command.SINGLE_SUCCESS;
	}
}
