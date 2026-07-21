package com.tuni.wlredstone;

import java.util.function.Function;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;

import com.tuni.wlredstone.item.LinkerItem;

public final class ModItems {
	public static final Item LINKER = register(
			"linker",
			LinkerItem::new,
			new Item.Properties().stacksTo(1));

	private static Item register(String name, Function<Item.Properties, Item> factory, Item.Properties properties) {
		ResourceKey<Item> itemKey = ResourceKey.create(Registries.ITEM, WlRedstone.id(name));
		return Registry.register(BuiltInRegistries.ITEM, itemKey, factory.apply(properties.setId(itemKey)));
	}

	public static void initialize() {
	}

	private ModItems() {
	}
}
