package com.github.theredbrain.scriptblocks.block;

import com.github.theredbrain.scriptblocks.block.entity.LootableVaultBlockEntity;
import com.github.theredbrain.scriptblocks.block.lootable_vault.LootableVaultState;
import com.github.theredbrain.scriptblocks.registry.ScriptBlocksEntities;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

@Deprecated
public class LootableVaultBlock extends BaseEntityBlock {
	public static final MapCodec<LootableVaultBlock> CODEC = simpleCodec(LootableVaultBlock::new);
	public static final Property<LootableVaultState> LOOTABLE_VAULT_STATE = EnumProperty.create("lootable_vault_state", LootableVaultState.class);

	@Override
	public MapCodec<LootableVaultBlock> codec() {
		return CODEC;
	}

	public LootableVaultBlock(BlockBehaviour.Properties settings) {
		super(settings);
		this.registerDefaultState(
				this.stateDefinition.any().setValue(HorizontalDirectionalBlock.FACING, Direction.NORTH).setValue(LOOTABLE_VAULT_STATE, LootableVaultState.INACTIVE).setValue(BlockStateProperties.OMINOUS, Boolean.FALSE)
		);
	}

	@Override
	public InteractionResult useItemOn(ItemStack stack, BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
		if (stack.isEmpty() || state.getValue(LOOTABLE_VAULT_STATE) != LootableVaultState.ACTIVE) {
			return InteractionResult.TRY_WITH_EMPTY_HAND;
		} else if (world instanceof ServerLevel serverWorld) {
			if (serverWorld.getBlockEntity(pos) instanceof LootableVaultBlockEntity lootableVaultBlockEntity) {
				LootableVaultBlockEntity.Server.tryUnlock(
						serverWorld, pos, state, lootableVaultBlockEntity.getConfig(world), lootableVaultBlockEntity.getServerData(), lootableVaultBlockEntity.getSharedData(), player, stack
				);
				return InteractionResult.SUCCESS;
			} else {
				return InteractionResult.TRY_WITH_EMPTY_HAND;
			}
		} else {
			return InteractionResult.CONSUME;
		}
	}

	@Nullable
	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new LootableVaultBlockEntity(pos, state);
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(HorizontalDirectionalBlock.FACING, LOOTABLE_VAULT_STATE, BlockStateProperties.OMINOUS);
	}

	@Nullable
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type) {
		return world instanceof ServerLevel serverWorld
				? createTickerHelper(
				type,
				ScriptBlocksEntities.LOOTABLE_VAULT_BLOCK_ENTITY,
				(worldx, pos, statex, blockEntity) -> LootableVaultBlockEntity.Server.tick(
						serverWorld, pos, statex, blockEntity.getConfig(world), blockEntity.getServerData(), blockEntity.getSharedData()
				)
		)
				: createTickerHelper(
				type,
				ScriptBlocksEntities.LOOTABLE_VAULT_BLOCK_ENTITY,
				(worldx, pos, statex, blockEntity) -> LootableVaultBlockEntity.Client.tick(worldx, pos, statex, blockEntity.getClientData(), blockEntity.getSharedData())
		);
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext ctx) {
		return this.defaultBlockState().setValue(HorizontalDirectionalBlock.FACING, ctx.getHorizontalDirection().getOpposite());
	}

	@Override
	public BlockState rotate(BlockState state, Rotation rotation) {
		return state.setValue(HorizontalDirectionalBlock.FACING, rotation.rotate(state.getValue(HorizontalDirectionalBlock.FACING)));
	}

	@Override
	public BlockState mirror(BlockState state, Mirror mirror) {
		return state.rotate(mirror.getRotation(state.getValue(HorizontalDirectionalBlock.FACING)));
	}

	@Override
	public RenderShape getRenderShape(BlockState state) {
		return RenderShape.MODEL;
	}
}
