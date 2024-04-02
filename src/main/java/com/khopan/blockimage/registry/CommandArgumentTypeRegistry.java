package com.khopan.blockimage.registry;

import com.khopan.blockimage.BlockImage;
import com.khopan.blockimage.command.argument.HandSideArgumentType;

import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import net.minecraft.commands.synchronization.SingletonArgumentInfo;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class CommandArgumentTypeRegistry {
	private CommandArgumentTypeRegistry() {}

	public static final DeferredRegister<ArgumentTypeInfo<?, ?>> ARGUMENT_TYPE_REGISTRY = DeferredRegister.create(ForgeRegistries.COMMAND_ARGUMENT_TYPES, BlockImage.MOD_ID);

	public static final RegistryObject<SingletonArgumentInfo<HandSideArgumentType>> HAND_SIDE_ARGUMENT_TYPE = CommandArgumentTypeRegistry.ARGUMENT_TYPE_REGISTRY.register("handside_argument", () -> ArgumentTypeInfos.registerByClass(HandSideArgumentType.class, SingletonArgumentInfo.contextFree(HandSideArgumentType :: handSide)));

	public static void register(IEventBus bus) {
		CommandArgumentTypeRegistry.ARGUMENT_TYPE_REGISTRY.register(bus);
	}
}
