package com.khopan.minecraft.common.forge;

public class KHOPANCommonForge {
	private KHOPANCommonForge() {}

	private static boolean Initialized = false;

	public static void commonSetup() {
		if(KHOPANCommonForge.Initialized) {
			return;
		}

		KHOPANCommonForge.Initialized = true;
		ForgePacketHandler.initialize();
	}
}
