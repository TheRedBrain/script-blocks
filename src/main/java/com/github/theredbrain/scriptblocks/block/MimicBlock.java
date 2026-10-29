package com.github.theredbrain.scriptblocks.block;

import com.github.theredbrain.scriptblocks.block.entity.MimicBlockEntity;
import com.github.theredbrain.scriptblocks.entity.player.DuckPlayerEntityMixin;
import com.github.theredbrain.scriptblocks.registry.ScriptBlocksBlocks;
import com.github.theredbrain.scriptblocks.registry.ScriptBlocksConfigs;
import com.github.theredbrain.scriptblocks.util.ScriptBlocksBlockProperties;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class MimicBlock extends RotatedBlockWithEntity {
	public static final MapCodec<MimicBlock> CODEC = simpleCodec(MimicBlock::new);

	public MimicBlock(Properties settings) {
		super(settings);
		this.registerDefaultState(this.stateDefinition.any()
				.setValue(ScriptBlocksBlockProperties.ROTATED, 0)
				.setValue(ScriptBlocksBlockProperties.X_MIRRORED, false)
				.setValue(ScriptBlocksBlockProperties.Z_MIRRORED, false)
				.setValue(BlockStateProperties.TRIGGERED, false)
		);
	}

	public MapCodec<MimicBlock> codec() {
		return CODEC;
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder);
		builder.add(BlockStateProperties.TRIGGERED);
	}

	@Nullable
	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new MimicBlockEntity(pos, state);
	}

	@Override
	public RenderShape getRenderShape(BlockState state) {
		return RenderShape.MODEL;
	}

	@Override
	public InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit) {
		BlockEntity blockEntity = world.getBlockEntity(pos);
		if (blockEntity instanceof MimicBlockEntity mimicBlockEntity && player.canUseGameMasterBlocks()) {
			((DuckPlayerEntityMixin) player).scriptblocks$openMimicBlockScreen(mimicBlockEntity);
			return InteractionResult.SUCCESS;
		}
		return InteractionResult.PASS;
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
		BlockPos activeMimicBlockPos = new BlockPos(0, 1, 0);
		BlockPos inactiveMimicBlockPos = new BlockPos(0, -1, 0);
		if (world.getBlockEntity(pos) instanceof MimicBlockEntity mimicBlockEntity) {
			activeMimicBlockPos = mimicBlockEntity.getActiveMimicBlockPositionOffset();
			inactiveMimicBlockPos = mimicBlockEntity.getInactiveMimicBlockPositionOffset();
		}
		BlockState mimicBlockState = ScriptBlocksBlocks.MIMIC_FALLBACK_BLOCK.defaultBlockState();
		BlockPos mimicBlockPos;
		if (state.getValue(BlockStateProperties.TRIGGERED)) {
			mimicBlockPos = pos.offset(activeMimicBlockPos.getX(), activeMimicBlockPos.getY(), activeMimicBlockPos.getZ());
		} else {
			mimicBlockPos = pos.offset(inactiveMimicBlockPos.getX(), inactiveMimicBlockPos.getY(), inactiveMimicBlockPos.getZ());
		}
		BlockState blockState = world.getBlockState(mimicBlockPos);
		if (!blockState.is(this) && !(ScriptBlocksConfigs.SERVER_CONFIG.enable_mimic_block_debug_mode || context.isHoldingItem(ScriptBlocksBlocks.MIMIC_BLOCK.asItem()))) {
			mimicBlockState = blockState;
		}
		return mimicBlockState.getBlock().getShape(mimicBlockState, world, mimicBlockPos, context);
	}

	@Deprecated
	public VoxelShape getCollisionShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
		BlockPos activeMimicBlockPos = new BlockPos(0, 1, 0);
		BlockPos inactiveMimicBlockPos = new BlockPos(0, -1, 0);
		if (world.getBlockEntity(pos) instanceof MimicBlockEntity mimicBlockEntity) {
			activeMimicBlockPos = mimicBlockEntity.getActiveMimicBlockPositionOffset();
			inactiveMimicBlockPos = mimicBlockEntity.getInactiveMimicBlockPositionOffset();
		}
		BlockState mimicBlockState = ScriptBlocksBlocks.MIMIC_FALLBACK_BLOCK.defaultBlockState();
		BlockPos mimicBlockPos;
		if (state.getValue(BlockStateProperties.TRIGGERED)) {
			mimicBlockPos = pos.offset(activeMimicBlockPos.getX(), activeMimicBlockPos.getY(), activeMimicBlockPos.getZ());
		} else {
			mimicBlockPos = pos.offset(inactiveMimicBlockPos.getX(), inactiveMimicBlockPos.getY(), inactiveMimicBlockPos.getZ());
		}
		BlockState blockState = world.getBlockState(mimicBlockPos);
		if (!blockState.is(this) && !(ScriptBlocksConfigs.SERVER_CONFIG.enable_mimic_block_debug_mode || context.isHoldingItem(ScriptBlocksBlocks.MIMIC_BLOCK.asItem()))) {
			mimicBlockState = blockState;
		}
		return mimicBlockState.getBlock().getCollisionShape(mimicBlockState, world, mimicBlockPos, context);
	}

//	@Override
//	public boolean canPathfindThrough(BlockState state, NavigationType type) {
//		BlockPos activeMimicBlockPos = new BlockPos(0, 1, 0);
//		BlockPos inactiveMimicBlockPos = new BlockPos(0, -1, 0);
//		boolean debugMode = false;
//		if (world.getBlockEntity(pos) instanceof MimicBlockEntity mimicBlockEntity) {
//			activeMimicBlockPos = mimicBlockEntity.getActiveMimicBlockPositionOffset();
//			inactiveMimicBlockPos = mimicBlockEntity.getInactiveMimicBlockPositionOffset();
//			debugMode = mimicBlockEntity.isDebugModeActive();
//		}
//		BlockState mimicBlockState = BlockRegistry.MIMIC_FALLBACK_BLOCK.getDefaultState();
//		BlockPos mimicBlockPos;
//		if (state.get(Properties.TRIGGERED)) {
//			mimicBlockPos = pos.add(activeMimicBlockPos.getX(), activeMimicBlockPos.getY(), activeMimicBlockPos.getZ());
//		} else {
//			mimicBlockPos = pos.add(inactiveMimicBlockPos.getX(), inactiveMimicBlockPos.getY(), inactiveMimicBlockPos.getZ());
//		}
//		BlockState blockState = world.getBlockState(mimicBlockPos);
//		if (!blockState.isOf(this) && !debugMode) {
//			mimicBlockState = blockState;
//		}
//		return mimicBlockState.getBlock().canPathfindThrough(mimicBlockState, type);
//	}

//	@Override
//	public boolean isTransparent(BlockState state, BlockGetter world, BlockPos pos) {
//		BlockPos activeMimicBlockPos = new BlockPos(0, 1, 0);
//		BlockPos inactiveMimicBlockPos = new BlockPos(0, -1, 0);
//		if (world.getBlockEntity(pos) instanceof MimicBlockEntity mimicBlockEntity) {
//			activeMimicBlockPos = mimicBlockEntity.getActiveMimicBlockPositionOffset();
//			inactiveMimicBlockPos = mimicBlockEntity.getInactiveMimicBlockPositionOffset();
//		}
//		BlockState mimicBlockState = ScriptBlocksBlocks.MIMIC_FALLBACK_BLOCK.defaultBlockState();
//		BlockPos mimicBlockPos;
//		if (state.getValue(BlockStateProperties.TRIGGERED)) {
//			mimicBlockPos = pos.offset(activeMimicBlockPos.getX(), activeMimicBlockPos.getY(), activeMimicBlockPos.getZ());
//		} else {
//			mimicBlockPos = pos.offset(inactiveMimicBlockPos.getX(), inactiveMimicBlockPos.getY(), inactiveMimicBlockPos.getZ());
//		}
//		BlockState blockState = world.getBlockState(mimicBlockPos);
//		if (!blockState.is(this) && !ScriptBlocksConfigs.SERVER_CONFIG.enable_mimic_block_debug_mode) {
//			mimicBlockState = blockState;
//		}
//		return mimicBlockState.getBlock().propagatesSkylightDown(mimicBlockState, world, mimicBlockPos);
//	}
}
