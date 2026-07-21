package com.tuni.wlredstone.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import org.jetbrains.annotations.Nullable;

import com.tuni.wlredstone.ModBlockEntities;
import com.tuni.wlredstone.block.TransceiverBlock;

public class TransceiverBlockEntity extends BlockEntity {
	@Nullable
	private GlobalPos link;
	private int inputPower;
	private int outputPower;

	public TransceiverBlockEntity(BlockPos pos, BlockState state) {
		super(ModBlockEntities.TRANSCEIVER, pos, state);
	}

	@Nullable
	public GlobalPos getLink() {
		return this.link;
	}

	public int getInputPower() {
		return this.inputPower;
	}

	public int getOutputPower() {
		return this.outputPower;
	}

	public void linkTo(TransceiverBlockEntity other) {
		this.link = GlobalPos.of(other.level.dimension(), other.getBlockPos());
		other.link = GlobalPos.of(this.level.dimension(), this.getBlockPos());
		this.setChanged();
		other.setChanged();
		this.recomputeInput();
		other.recomputeInput();
		this.setOutput(other.inputPower);
		other.setOutput(this.inputPower);
	}

	public void clearLink() {
		this.link = null;
		this.setChanged();
		this.setOutput(0);
	}

	/** Re-reads the redstone power entering the back and pushes it to the linked transceiver. */
	public void recomputeInput() {
		if (!(this.level instanceof ServerLevel serverLevel)) {
			return;
		}

		int newInput = TransceiverBlock.getInputSignal(serverLevel, this.worldPosition, this.getBlockState());

		if (newInput != this.inputPower) {
			this.inputPower = newInput;
			this.setChanged();
			this.updatePoweredState();
			TransceiverBlockEntity partner = this.getPartner(serverLevel);

			if (partner != null) {
				partner.setOutput(newInput);
			}
		}
	}

	public void setOutput(int power) {
		if (power == this.outputPower || this.level == null) {
			return;
		}

		this.outputPower = power;
		this.setChanged();
		this.updatePoweredState();
		TransceiverBlock.updateNeighborsInFront(this.level, this.worldPosition, this.getBlockState());
	}

	/** Called when this transceiver is destroyed: unlink the partner so it stops outputting. */
	public void notifyPartnerOfRemoval() {
		if (this.level instanceof ServerLevel serverLevel) {
			TransceiverBlockEntity partner = this.getPartner(serverLevel);

			if (partner != null && partner.link != null && this.getBlockPos().equals(partner.link.pos())) {
				partner.clearLink();
			}
		}
	}

	@Nullable
	private TransceiverBlockEntity getPartner(ServerLevel serverLevel) {
		if (this.link == null) {
			return null;
		}

		ServerLevel targetLevel = serverLevel.getServer().getLevel(this.link.dimension());

		if (targetLevel == null || !targetLevel.isLoaded(this.link.pos())) {
			return null;
		}

		return targetLevel.getBlockEntity(this.link.pos()) instanceof TransceiverBlockEntity partner ? partner : null;
	}

	private void updatePoweredState() {
		BlockState state = this.getBlockState();

		if (state.hasProperty(TransceiverBlock.POWERED)) {
			boolean powered = this.inputPower > 0 || this.outputPower > 0;

			if (state.getValue(TransceiverBlock.POWERED) != powered) {
				this.level.setBlockAndUpdate(this.worldPosition, state.setValue(TransceiverBlock.POWERED, powered));
			}
		}
	}

	@Override
	public void preRemoveSideEffects(BlockPos pos, BlockState state) {
		super.preRemoveSideEffects(pos, state);
		this.notifyPartnerOfRemoval();
	}

	@Override
	protected void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		output.storeNullable("link", GlobalPos.CODEC, this.link);
		output.putInt("input_power", this.inputPower);
		output.putInt("output_power", this.outputPower);
	}

	@Override
	protected void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		this.link = input.read("link", GlobalPos.CODEC).orElse(null);
		this.inputPower = input.getIntOr("input_power", 0);
		this.outputPower = input.getIntOr("output_power", 0);
	}
}
