package com.khopan.blockimage.forge;

import com.khopan.blockimage.BlockImage;
import com.khopan.blockimage.command.BlockImageCommand;
import com.khopan.blockimage.command.argument.HandSideArgumentType;
import com.khopan.minecraft.common.command.argument.FileArgumentType;
import com.khopan.minecraft.common.forge.KHOPANCommonForge;
import com.mojang.brigadier.CommandDispatcher;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import net.minecraft.commands.synchronization.SingletonArgumentInfo;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@Mod(BlockImage.MOD_ID)
public class BlockImageForge {
	public static final DeferredRegister<ArgumentTypeInfo<?, ?>> ARGUMENT_TYPE_REGISTRY = DeferredRegister.create(ForgeRegistries.COMMAND_ARGUMENT_TYPES, BlockImage.MOD_ID);

	public static final RegistryObject<SingletonArgumentInfo<HandSideArgumentType>> HAND_SIDE_ARGUMENT_TYPE = BlockImageForge.ARGUMENT_TYPE_REGISTRY.register("handside_argument", () -> ArgumentTypeInfos.registerByClass(HandSideArgumentType.class, SingletonArgumentInfo.contextFree(HandSideArgumentType :: handSide)));
	public static final RegistryObject<SingletonArgumentInfo<FileArgumentType>> FILE_ARGUMENT_TYPE = BlockImageForge.ARGUMENT_TYPE_REGISTRY.register("file_argument", () -> ArgumentTypeInfos.registerByClass(FileArgumentType.class, SingletonArgumentInfo.contextFree(FileArgumentType :: file)));

	public BlockImageForge() {
		BlockImage.initialize();
		FMLJavaModLoadingContext context = FMLJavaModLoadingContext.get();
		IEventBus bus = context.getModEventBus();
		BlockImageForge.ARGUMENT_TYPE_REGISTRY.register(bus);
		bus.addListener(this :: onCommonSetup);
		MinecraftForge.EVENT_BUS.addListener(this :: onRegisterCommand);
	}

	private void onCommonSetup(FMLCommonSetupEvent Event) {
		KHOPANCommonForge.commonSetup();
	}

	private void onRegisterCommand(RegisterCommandsEvent Event) {
		CommandDispatcher<CommandSourceStack> dispatcher = Event.getDispatcher();
		BlockImageCommand.register(dispatcher);
	}
}
