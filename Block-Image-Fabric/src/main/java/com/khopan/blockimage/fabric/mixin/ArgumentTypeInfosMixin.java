package com.khopan.blockimage.fabric.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.khopan.blockimage.command.argument.HandSideArgumentType;
import com.khopan.minecraft.common.command.argument.FileArgumentType;
import com.mojang.brigadier.arguments.ArgumentType;

import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import net.minecraft.commands.synchronization.SingletonArgumentInfo;
import net.minecraft.core.Registry;

@Mixin(ArgumentTypeInfos.class)
public abstract class ArgumentTypeInfosMixin {
	@Invoker("register")
	private static <A extends ArgumentType<?>, T extends ArgumentTypeInfo.Template<A>> ArgumentTypeInfo<A, T> invokeRegister(Registry<ArgumentTypeInfo<?, ?>> registry, String id, Class<? extends A> argumentClass, ArgumentTypeInfo<A, T> info) {
		throw new AssertionError();
	}

	@Inject(method="bootstrap(Lnet/minecraft/core/Registry;)Lnet/minecraft/commands/synchronization/ArgumentTypeInfo;", at=@At("HEAD"))
	private static void bootstrapMixin(Registry<ArgumentTypeInfo<?, ?>> registry, CallbackInfoReturnable<ArgumentTypeInfo<?, ?>> info) {
		ArgumentTypeInfosMixin.invokeRegister(registry, "handside_argument", HandSideArgumentType.class, SingletonArgumentInfo.contextFree(HandSideArgumentType :: handSide));
		ArgumentTypeInfosMixin.invokeRegister(registry, "file_argument", FileArgumentType.class, SingletonArgumentInfo.contextFree(FileArgumentType :: file));
	}
}
