package com.tuni.wlredstone;

import net.minecraft.core.GlobalPos;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;

public final class ModDataComponents {
	public static final DataComponentType<GlobalPos> LINKING_FROM = Registry.register(
			BuiltInRegistries.DATA_COMPONENT_TYPE,
			WlRedstone.id("linking_from"),
			DataComponentType.<GlobalPos>builder()
					.persistent(GlobalPos.CODEC)
					.networkSynchronized(GlobalPos.STREAM_CODEC)
					.build());

	public static void initialize() {
	}

	private ModDataComponents() {
	}
}
