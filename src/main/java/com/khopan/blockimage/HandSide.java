package com.khopan.blockimage;

import com.mojang.serialization.Codec;

import net.minecraft.util.StringRepresentable;

public enum HandSide implements StringRepresentable {
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
