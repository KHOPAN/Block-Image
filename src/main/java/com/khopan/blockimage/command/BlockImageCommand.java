package com.khopan.blockimage.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;

public class BlockImageCommand {
	private BlockImageCommand() {}

	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		dispatcher.register(LiteralArgumentBuilder.<CommandSourceStack>literal("blockimage").requires(source -> source.hasPermission(2)).executes(context -> {
			return BlockImageCommand.placeImage(context);
		}));
	}

	private static int placeImage(CommandContext<CommandSourceStack> context) {
		CommandSourceStack source = context.getSource();
		source.sendSystemMessage(Component.literal("/blockimage command has been executed!"));
		return Command.SINGLE_SUCCESS;
	}
}
