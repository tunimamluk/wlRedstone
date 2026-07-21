package com.tuni.wlredstone.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import org.jetbrains.annotations.Nullable;

import com.tuni.wlredstone.ModBlockEntities;

public class TransceiverBlockEntity extends BlockEntity {
	@Nullable
	private GlobalPos link;

	public TransceiverBlockEntity(BlockPos pos, BlockState state) {
		super(ModBlockEntities.TRANSCEIVER, pos, state);
	}

	@Nullable
	public GlobalPos getLink() {
		return this.link;
	}

	public void setLink(@Nullable GlobalPos link) {
		this.link = link;
		this.setChanged();
	}

	@Override
	protected void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		output.storeNullable("link", GlobalPos.CODEC, this.link);
	}

	@Override
	protected void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		this.link = input.read("link", GlobalPos.CODEC).orElse(null);
	}
}
