package com.khopan.blockimage.command.argument;

import com.khopan.blockimage.HandSide;
import com.mojang.brigadier.context.CommandContext;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.StringRepresentableArgument;

public class HandSideArgumentType extends StringRepresentableArgument<HandSide> {
	private HandSideArgumentType() {
		super(HandSide.CODEC, HandSide :: values);
	}

	public static HandSideArgumentType handSide() {
		return new HandSideArgumentType();
	}

	public static HandSide getHandSide(CommandContext<CommandSourceStack> context, String name) {
		return context.getArgument(name, HandSide.class);
	}
}
