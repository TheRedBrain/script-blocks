package com.github.theredbrain.scriptblocks.block;

import com.github.theredbrain.scriptblocks.block.entity.TriggeredDispenserBlockEntity;
import com.github.theredbrain.scriptblocks.registry.ScriptBlocksEntities;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.DispenserBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import org.slf4j.Logger;

public class TriggeredDispenserBlock extends DispenserBlock {
	private static final Logger LOGGER = LogUtils.getLogger();
	public static final MapCodec<TriggeredDispenserBlock> CODEC = simpleCodec(TriggeredDispenserBlock::new);

	@Override
	public MapCodec<TriggeredDispenserBlock> codec() {
		return CODEC;
	}

	public TriggeredDispenserBlock(BlockBehaviour.Properties settings) {
		super(settings);
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new TriggeredDispenserBlockEntity(pos, state);
	}

	@Override
	protected void onStateReplaced(BlockState state, Level world, BlockPos pos, BlockState newState, boolean moved) {
		if (state.hasBlockEntity() && !state.is(newState.getBlock())) {
			world.removeBlockEntity(pos);
		}
	}

	@Override
	public InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit) {
		if (player.isCreative()) {
			if (world.isClientSide) {
				return InteractionResult.SUCCESS;
			} else {
				BlockEntity blockEntity = world.getBlockEntity(pos);
				if (blockEntity instanceof TriggeredDispenserBlockEntity triggeredDispenserBlockEntity) {
					player.openMenu(triggeredDispenserBlockEntity);
				}
				return InteractionResult.CONSUME;
			}
		} else {
			return InteractionResult.PASS;
		}
	}

	@Override
	protected void dispenseFrom(ServerLevel world, BlockState state, BlockPos pos) {
		DispenserBlockEntity dispenserBlockEntity = world.getBlockEntity(pos, ScriptBlocksEntities.TRIGGERED_DISPENSER_BLOCK_ENTITY).orElse(null);
		if (dispenserBlockEntity == null) {
			LOGGER.warn("Ignoring dispensing attempt for TriggeredDispenserBlock without matching block entity at {}", pos);
		} else {
			BlockSource blockPointer = new BlockSource(world, pos, state, dispenserBlockEntity);
			int i = dispenserBlockEntity.getRandomSlot(world.random);
			if (i < 0) {
				world.levelEvent(LevelEvent.SOUND_DISPENSER_FAIL, pos, 0);
				world.gameEvent(GameEvent.BLOCK_ACTIVATE, pos, GameEvent.Context.of(dispenserBlockEntity.getBlockState()));
			} else {
				ItemStack itemStack = dispenserBlockEntity.getItem(i);
				ItemStack itemStack1 = itemStack.copy();
				DispenseItemBehavior dispenserBehavior = this.getDispenseMethod(world, itemStack);
				if (dispenserBehavior != DispenseItemBehavior.NOOP) {
					ItemStack newItemStack = dispenserBehavior.dispense(blockPointer, itemStack);
					if (ItemStack.isSameItemSameComponents(itemStack, newItemStack)) {
						dispenserBlockEntity.setItem(i, itemStack1);
					} else {
						dispenserBlockEntity.setItem(i, newItemStack);
					}
				}
			}
		}
	}
}
