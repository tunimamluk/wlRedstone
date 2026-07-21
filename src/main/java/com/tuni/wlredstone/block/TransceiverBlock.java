package com.tuni.wlredstone.block;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RedStoneWireBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.redstone.ExperimentalRedstoneUtils;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import com.tuni.wlredstone.blockentity.TransceiverBlockEntity;

/**
 * Repeater-style directional block: redstone comes in through the back
 * (the side FACING points at, like vanilla DiodeBlock) and the wireless
 * partner's signal comes out the front.
 */
public class TransceiverBlock extends HorizontalDirectionalBlock implements EntityBlock {
	public static final MapCodec<TransceiverBlock> CODEC = simpleCodec(TransceiverBlock::new);
	public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

	private static final VoxelShape SHAPE = Block.box(0.0, 0.0, 0.0, 16.0, 2.0, 16.0);

	public TransceiverBlock(Properties properties) {
		super(properties);
		this.registerDefaultState(this.stateDefinition.any()
				.setValue(FACING, Direction.NORTH)
				.setValue(POWERED, false));
	}

	@Override
	protected MapCodec<? extends TransceiverBlock> codec() {
		return CODEC;
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACING, POWERED);
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return SHAPE;
	}

	@Override
	protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
		return Block.canSupportRigidBlock(level, pos.below());
	}

	@Override
	protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess tickAccess, BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, RandomSource random) {
		if (direction == Direction.DOWN && !state.canSurvive(level, pos)) {
			return Blocks.AIR.defaultBlockState();
		}

		return super.updateShape(state, level, tickAccess, pos, direction, neighborPos, neighborState, random);
	}

	@Override
	protected boolean isSignalSource(BlockState state) {
		return true;
	}

	@Override
	protected int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
		if (state.getValue(FACING) != direction) {
			return 0;
		}

		return level.getBlockEntity(pos) instanceof TransceiverBlockEntity transceiver ? transceiver.getOutputPower() : 0;
	}

	@Override
	protected int getDirectSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
		return this.getSignal(state, level, pos, direction);
	}

	/** Reads the redstone power entering through the back, exactly like vanilla DiodeBlock. */
	public static int getInputSignal(Level level, BlockPos pos, BlockState state) {
		Direction direction = state.getValue(FACING);
		BlockPos inputPos = pos.relative(direction);
		int input = level.getSignal(inputPos, direction);

		if (input >= 15) {
			return input;
		}

		BlockState inputState = level.getBlockState(inputPos);
		return Math.max(input, inputState.is(Blocks.REDSTONE_WIRE) ? inputState.getValue(RedStoneWireBlock.POWER) : 0);
	}

	/** Notifies the block in front (the output side) that our signal changed, like vanilla DiodeBlock. */
	public static void updateNeighborsInFront(Level level, BlockPos pos, BlockState state) {
		Direction direction = state.getValue(FACING);
		BlockPos frontPos = pos.relative(direction.getOpposite());
		Orientation orientation = ExperimentalRedstoneUtils.initialOrientation(level, direction.getOpposite(), Direction.UP);
		level.neighborChanged(frontPos, state.getBlock(), orientation);
		level.updateNeighborsAtExceptFromFacing(frontPos, state.getBlock(), direction, orientation);
	}

	@Override
	protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, Orientation orientation, boolean movedByPiston) {
		super.neighborChanged(state, level, pos, neighborBlock, orientation, movedByPiston);

		if (!level.isClientSide() && level.getBlockEntity(pos) instanceof TransceiverBlockEntity transceiver) {
			transceiver.recomputeInput();
		}
	}

	@Override
	protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
		super.onPlace(state, level, pos, oldState, movedByPiston);
		updateNeighborsInFront(level, pos, state);

		if (!level.isClientSide() && !oldState.is(this) && level.getBlockEntity(pos) instanceof TransceiverBlockEntity transceiver) {
			transceiver.recomputeInput();
		}
	}

	@Override
	protected void affectNeighborsAfterRemoval(BlockState state, net.minecraft.server.level.ServerLevel level, BlockPos pos, boolean movedByPiston) {
		if (!movedByPiston) {
			updateNeighborsInFront(level, pos, state);
		}
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new TransceiverBlockEntity(pos, state);
	}
}
