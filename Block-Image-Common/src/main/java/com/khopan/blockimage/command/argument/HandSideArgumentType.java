package com.khopan.blockimage.command.argument;

import com.khopan.blockimage.command.argument.HandSideArgumentType.HandSide;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.serialization.Codec;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.StringRepresentableArgument;
import net.minecraft.util.StringRepresentable;

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

	public static enum HandSide implements StringRepresentable {
		LEFT("left"),
		RIGHT("right");

		public static final Codec<HandSide> CODEC = StringRepresentable.fromEnum(HandSide :: values);

		private final String name;

		HandSide(String name) {
			this.name = name;
		}

		@Override
		public String getSerializedName() {
			return this.name;
		}

		@Override
		public String toString() {
			return this.name;
		}
	}
}
