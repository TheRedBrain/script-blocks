package com.github.theredbrain.scriptblocks.block;

import com.github.theredbrain.scriptblocks.block.entity.LootableVaultBlockEntity;
import com.github.theredbrain.scriptblocks.block.lootable_vault.LootableVaultState;
import com.github.theredbrain.scriptblocks.entity.player.DuckPlayerEntityMixin;
import com.github.theredbrain.scriptblocks.registry.EntityRegistry;
import com.mojang.serialization.MapCodec;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.state.property.Property;
import net.minecraft.util.ActionResult;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class LootableVaultBlock extends BlockWithEntity {
	public static final MapCodec<LootableVaultBlock> CODEC = createCodec(LootableVaultBlock::new);
	public static final Property<LootableVaultState> LOOTABLE_VAULT_STATE = EnumProperty.of("lootable_vault_state", LootableVaultState.class);
	public static final DirectionProperty FACING = HorizontalFacingBlock.FACING;
	public static final BooleanProperty OMINOUS = Properties.OMINOUS;

	@Override
	public MapCodec<LootableVaultBlock> getCodec() {
		return CODEC;
	}

	public LootableVaultBlock(AbstractBlock.Settings settings) {
		super(settings);
		this.setDefaultState(
				this.stateManager.getDefaultState().with(FACING, Direction.NORTH).with(LOOTABLE_VAULT_STATE, LootableVaultState.INACTIVE).with(OMINOUS, Boolean.FALSE)
		);
	}

//	@Override
//	public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
//		BlockEntity blockEntity = world.getBlockEntity(pos);
//		if (blockEntity instanceof LootableVaultBlockEntity lootableVaultBlockEntity && player.isCreativeLevelTwoOp()) {
//			((DuckPlayerEntityMixin) player).scriptblocks$openLootableVaultBlockScreen(lootableVaultBlockEntity);
//			return ActionResult.success(world.isClient);
//		}
//		return ActionResult.PASS;
//	}

	@Override
	public ItemActionResult onUseWithItem(ItemStack stack, BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
		if (stack.isEmpty() || state.get(LOOTABLE_VAULT_STATE) != LootableVaultState.ACTIVE) {
			return ItemActionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
		} else if (world instanceof ServerWorld serverWorld) {
			if (serverWorld.getBlockEntity(pos) instanceof LootableVaultBlockEntity lootableVaultBlockEntity) {
				LootableVaultBlockEntity.Server.tryUnlock(
						serverWorld, pos, state, lootableVaultBlockEntity.getConfig(world), lootableVaultBlockEntity.getServerData(), lootableVaultBlockEntity.getSharedData(), player, stack
				);
				return ItemActionResult.SUCCESS;
			} else {
				return ItemActionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
			}
		} else {
			return ItemActionResult.CONSUME;
		}
	}

	@Nullable
	@Override
	public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
		return new LootableVaultBlockEntity(pos, state);
	}

	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
		builder.add(FACING, LOOTABLE_VAULT_STATE, OMINOUS);
	}

	@Nullable
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
		return world instanceof ServerWorld serverWorld
				? validateTicker(
				type,
				EntityRegistry.LOOTABLE_VAULT_BLOCK_ENTITY,
				(worldx, pos, statex, blockEntity) -> LootableVaultBlockEntity.Server.tick(
						serverWorld, pos, statex, blockEntity.getConfig(world), blockEntity.getServerData(), blockEntity.getSharedData()
				)
		)
				: validateTicker(
				type,
				EntityRegistry.LOOTABLE_VAULT_BLOCK_ENTITY,
				(worldx, pos, statex, blockEntity) -> LootableVaultBlockEntity.Client.tick(worldx, pos, statex, blockEntity.getClientData(), blockEntity.getSharedData())
		);
	}

	@Override
	public BlockState getPlacementState(ItemPlacementContext ctx) {
		return this.getDefaultState().with(FACING, ctx.getHorizontalPlayerFacing().getOpposite());
	}

	@Override
	public BlockState rotate(BlockState state, BlockRotation rotation) {
		return state.with(FACING, rotation.rotate(state.get(FACING)));
	}

	@Override
	public BlockState mirror(BlockState state, BlockMirror mirror) {
		return state.rotate(mirror.getRotation(state.get(FACING)));
	}

	@Override
	public BlockRenderType getRenderType(BlockState state) {
		return BlockRenderType.MODEL;
	}
}
