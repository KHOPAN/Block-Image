package com.khopan.minecraft.common.fabric;

public class KHOPANCommonFabric {
	private KHOPANCommonFabric() {}

	private static boolean Initialized = false;

	public static void initialize() {
		if(KHOPANCommonFabric.Initialized) {
			return;
		}

		KHOPANCommonFabric.Initialized = true;
		FabricPacketHandler.initialize();
	}
}
