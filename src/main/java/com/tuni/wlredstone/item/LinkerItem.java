package com.tuni.wlredstone.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

import com.tuni.wlredstone.ModBlocks;
import com.tuni.wlredstone.ModDataComponents;
import com.tuni.wlredstone.blockentity.TransceiverBlockEntity;

public class LinkerItem extends Item {
	public LinkerItem(Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResult useOn(UseOnContext context) {
		Level level = context.getLevel();
		BlockPos pos = context.getClickedPos();

		if (!level.getBlockState(pos).is(ModBlocks.WIRELESS_TRANSCEIVER)) {
			return InteractionResult.PASS;
		}

		if (!(level instanceof ServerLevel serverLevel)) {
			return InteractionResult.SUCCESS;
		}

		Player player = context.getPlayer();
		ItemStack stack = context.getItemInHand();
		GlobalPos clicked = GlobalPos.of(serverLevel.dimension(), pos);
		GlobalPos stored = stack.get(ModDataComponents.LINKING_FROM);

		if (stored == null) {
			stack.set(ModDataComponents.LINKING_FROM, clicked);
			message(player, "item.wlredstone.linker.selected", pos.getX(), pos.getY(), pos.getZ());
			return InteractionResult.SUCCESS;
		}

		if (stored.equals(clicked)) {
			message(player, "item.wlredstone.linker.same_block");
			return InteractionResult.SUCCESS;
		}

		ServerLevel storedLevel = serverLevel.getServer().getLevel(stored.dimension());

		if (storedLevel == null
				|| !(storedLevel.getBlockEntity(stored.pos()) instanceof TransceiverBlockEntity first)
				|| !(serverLevel.getBlockEntity(pos) instanceof TransceiverBlockEntity second)) {
			stack.remove(ModDataComponents.LINKING_FROM);
			message(player, "item.wlredstone.linker.lost");
			return InteractionResult.SUCCESS;
		}

		first.setLink(clicked);
		second.setLink(stored);
		stack.remove(ModDataComponents.LINKING_FROM);
		message(player, "item.wlredstone.linker.linked");
		return InteractionResult.SUCCESS;
	}

	@Override
	public InteractionResult use(Level level, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);

		if (player.isShiftKeyDown() && stack.get(ModDataComponents.LINKING_FROM) != null) {
			if (!level.isClientSide()) {
				stack.remove(ModDataComponents.LINKING_FROM);
				message(player, "item.wlredstone.linker.cleared");
			}

			return InteractionResult.SUCCESS;
		}

		return InteractionResult.PASS;
	}

	private static void message(Player player, String key, Object... args) {
		if (player != null) {
			player.sendOverlayMessage(Component.translatable(key, args));
		}
	}
}
