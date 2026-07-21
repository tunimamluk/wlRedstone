package com.tuni.wlredstone;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WlRedstone implements ModInitializer {
	public static final String MOD_ID = "wlredstone";

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	private static final ResourceKey<CreativeModeTab> REDSTONE_BLOCKS_TAB = ResourceKey.create(
			Registries.CREATIVE_MODE_TAB, Identifier.fromNamespaceAndPath("minecraft", "redstone_blocks"));

	@Override
	public void onInitialize() {
		ModDataComponents.initialize();
		ModBlocks.initialize();
		ModBlockEntities.initialize();
		ModItems.initialize();

		CreativeModeTabEvents.modifyOutputEvent(REDSTONE_BLOCKS_TAB).register(output -> {
			output.accept(ModBlocks.WIRELESS_TRANSCEIVER);
			output.accept(ModItems.LINKER);
		});

		LOGGER.info("Wireless Redstone initialized!");
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}
