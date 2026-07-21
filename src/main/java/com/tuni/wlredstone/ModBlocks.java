package com.tuni.wlredstone;

import java.util.function.Function;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;

import com.tuni.wlredstone.block.TransceiverBlock;

public final class ModBlocks {
	public static final Block WIRELESS_TRANSCEIVER = register(
			"wireless_transceiver",
			TransceiverBlock::new,
			BlockBehaviour.Properties.of().instabreak().sound(SoundType.STONE));

	private static Block register(String name, Function<BlockBehaviour.Properties, Block> factory, BlockBehaviour.Properties properties) {
		ResourceKey<Block> blockKey = ResourceKey.create(Registries.BLOCK, WlRedstone.id(name));
		Block block = Registry.register(BuiltInRegistries.BLOCK, blockKey, factory.apply(properties.setId(blockKey)));

		ResourceKey<Item> itemKey = ResourceKey.create(Registries.ITEM, WlRedstone.id(name));
		BlockItem blockItem = new BlockItem(block, new Item.Properties().setId(itemKey).useBlockDescriptionPrefix());
		Registry.register(BuiltInRegistries.ITEM, itemKey, blockItem);

		return block;
	}

	public static void initialize() {
	}

	private ModBlocks() {
	}
}
