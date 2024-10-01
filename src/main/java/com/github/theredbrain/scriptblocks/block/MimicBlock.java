package com.github.theredbrain.scriptblocks.block;

import com.github.theredbrain.scriptblocks.block.entity.MimicBlockEntity;
import com.github.theredbrain.scriptblocks.entity.player.DuckPlayerEntityMixin;
import com.github.theredbrain.scriptblocks.registry.BlockRegistry;
import com.mojang.serialization.MapCodec;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class MimicBlock extends RotatedBlockWithEntity {

	public static final BooleanProperty TRIGGERED = Properties.TRIGGERED;

	public MimicBlock(Settings settings) {
		super(settings);
		this.setDefaultState(this.stateManager.getDefaultState().with(ROTATED, 0).with(X_MIRRORED, false).with(Z_MIRRORED, false).with(TRIGGERED, false));
	}

	// TODO Block Codecs
	public MapCodec<MimicBlock> getCodec() {
		return null;
	}

	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
		super.appendProperties(builder);
		builder.add(TRIGGERED);
	}

	@Nullable
	@Override
	public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
		return new MimicBlockEntity(pos, state);
	}

	@Override
	public BlockRenderType getRenderType(BlockState state) {
		return BlockRenderType.MODEL;
	}

	@Override
	public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
		BlockEntity blockEntity = world.getBlockEntity(pos);
		if (blockEntity instanceof MimicBlockEntity mimicBlockEntity && player.isCreativeLevelTwoOp()) {
			((DuckPlayerEntityMixin) player).scriptblocks$openMimicBlockScreen(mimicBlockEntity);
			return ActionResult.success(world.isClient);
		}
		return ActionResult.PASS;
	}

	@Override
	public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
		BlockPos activeMimicBlockPos = new BlockPos(0, 1, 0);
		BlockPos inactiveMimicBlockPos = new BlockPos(0, -1, 0);
		boolean debugMode = false;
		if (world.getBlockEntity(pos) instanceof MimicBlockEntity mimicBlockEntity) {
			activeMimicBlockPos = mimicBlockEntity.getActiveMimicBlockPositionOffset();
			inactiveMimicBlockPos = mimicBlockEntity.getInactiveMimicBlockPositionOffset();
			debugMode = mimicBlockEntity.isDebugModeActive();
		}
		BlockState mimicBlockState = BlockRegistry.MIMIC_FALLBACK_BLOCK.getDefaultState();
		BlockPos mimicBlockPos;
		if (state.get(TRIGGERED)) {
			mimicBlockPos = pos.add(activeMimicBlockPos.getX(), activeMimicBlockPos.getY(), activeMimicBlockPos.getZ());
		} else {
			mimicBlockPos = pos.add(inactiveMimicBlockPos.getX(), inactiveMimicBlockPos.getY(), inactiveMimicBlockPos.getZ());
		}
		BlockState blockState = world.getBlockState(mimicBlockPos);
		if (!blockState.isOf(this) && !debugMode) {
			mimicBlockState = blockState;
		}
		return mimicBlockState.getBlock().getOutlineShape(mimicBlockState, world, mimicBlockPos, context);
	}

	@Deprecated
	public VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
		BlockPos activeMimicBlockPos = new BlockPos(0, 1, 0);
		BlockPos inactiveMimicBlockPos = new BlockPos(0, -1, 0);
		boolean debugMode = false;
		if (world.getBlockEntity(pos) instanceof MimicBlockEntity mimicBlockEntity) {
			activeMimicBlockPos = mimicBlockEntity.getActiveMimicBlockPositionOffset();
			inactiveMimicBlockPos = mimicBlockEntity.getInactiveMimicBlockPositionOffset();
			debugMode = mimicBlockEntity.isDebugModeActive();
		}
		BlockState mimicBlockState = BlockRegistry.MIMIC_FALLBACK_BLOCK.getDefaultState();
		BlockPos mimicBlockPos;
		if (state.get(TRIGGERED)) {
			mimicBlockPos = pos.add(activeMimicBlockPos.getX(), activeMimicBlockPos.getY(), activeMimicBlockPos.getZ());
		} else {
			mimicBlockPos = pos.add(inactiveMimicBlockPos.getX(), inactiveMimicBlockPos.getY(), inactiveMimicBlockPos.getZ());
		}
		BlockState blockState = world.getBlockState(mimicBlockPos);
		if (!blockState.isOf(this) && !debugMode) {
			mimicBlockState = blockState;
		}
		return mimicBlockState.getBlock().getCollisionShape(mimicBlockState, world, mimicBlockPos, context);
	}

	@Override
	protected boolean canPathfindThrough(BlockState state, NavigationType type) {
		BlockPos activeMimicBlockPos = new BlockPos(0, 1, 0);
		BlockPos inactiveMimicBlockPos = new BlockPos(0, -1, 0);
		boolean debugMode = false;
		if (world.getBlockEntity(pos) instanceof MimicBlockEntity mimicBlockEntity) {
			activeMimicBlockPos = mimicBlockEntity.getActiveMimicBlockPositionOffset();
			inactiveMimicBlockPos = mimicBlockEntity.getInactiveMimicBlockPositionOffset();
			debugMode = mimicBlockEntity.isDebugModeActive();
		}
		BlockState mimicBlockState = BlockRegistry.MIMIC_FALLBACK_BLOCK.getDefaultState();
		BlockPos mimicBlockPos;
		if (state.get(TRIGGERED)) {
			mimicBlockPos = pos.add(activeMimicBlockPos.getX(), activeMimicBlockPos.getY(), activeMimicBlockPos.getZ());
		} else {
			mimicBlockPos = pos.add(inactiveMimicBlockPos.getX(), inactiveMimicBlockPos.getY(), inactiveMimicBlockPos.getZ());
		}
		BlockState blockState = world.getBlockState(mimicBlockPos);
		if (!blockState.isOf(this) && !debugMode) {
			mimicBlockState = blockState;
		}
		return mimicBlockState.getBlock().canPathfindThrough(mimicBlockState, type);
	}

	@Override
	public boolean isTransparent(BlockState state, BlockView world, BlockPos pos) {
		BlockPos activeMimicBlockPos = new BlockPos(0, 1, 0);
		BlockPos inactiveMimicBlockPos = new BlockPos(0, -1, 0);
		boolean debugMode = false;
		if (world.getBlockEntity(pos) instanceof MimicBlockEntity mimicBlockEntity) {
			activeMimicBlockPos = mimicBlockEntity.getActiveMimicBlockPositionOffset();
			inactiveMimicBlockPos = mimicBlockEntity.getInactiveMimicBlockPositionOffset();
			debugMode = mimicBlockEntity.isDebugModeActive();
		}
		BlockState mimicBlockState = BlockRegistry.MIMIC_FALLBACK_BLOCK.getDefaultState();
		BlockPos mimicBlockPos;
		if (state.get(TRIGGERED)) {
			mimicBlockPos = pos.add(activeMimicBlockPos.getX(), activeMimicBlockPos.getY(), activeMimicBlockPos.getZ());
		} else {
			mimicBlockPos = pos.add(inactiveMimicBlockPos.getX(), inactiveMimicBlockPos.getY(), inactiveMimicBlockPos.getZ());
		}
		BlockState blockState = world.getBlockState(mimicBlockPos);
		if (!blockState.isOf(this) && !debugMode) {
			mimicBlockState = blockState;
		}
		return mimicBlockState.getBlock().isTransparent(mimicBlockState, world, mimicBlockPos);
	}
}
