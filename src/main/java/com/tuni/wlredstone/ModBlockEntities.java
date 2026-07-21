package com.tuni.wlredstone;

import java.util.Set;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;

import com.tuni.wlredstone.blockentity.TransceiverBlockEntity;

public final class ModBlockEntities {
	public static final BlockEntityType<TransceiverBlockEntity> TRANSCEIVER = Registry.register(
			BuiltInRegistries.BLOCK_ENTITY_TYPE,
			WlRedstone.id("transceiver"),
			new BlockEntityType<>(TransceiverBlockEntity::new, Set.of(ModBlocks.WIRELESS_TRANSCEIVER)));

	public static void initialize() {
	}

	private ModBlockEntities() {
	}
}
