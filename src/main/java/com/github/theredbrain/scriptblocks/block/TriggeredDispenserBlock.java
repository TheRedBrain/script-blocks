package com.github.theredbrain.scriptblocks.block;

import com.github.theredbrain.scriptblocks.block.entity.TriggeredDispenserBlockEntity;
import com.github.theredbrain.scriptblocks.registry.EntityRegistry;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.MapCodec;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.DispenserBlock;
import net.minecraft.block.dispenser.DispenserBehavior;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.DispenserBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPointer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldEvents;
import net.minecraft.world.event.GameEvent;
import org.slf4j.Logger;

public class TriggeredDispenserBlock extends DispenserBlock {
	private static final Logger LOGGER = LogUtils.getLogger();
	public static final MapCodec<TriggeredDispenserBlock> CODEC = createCodec(TriggeredDispenserBlock::new);

	@Override
	public MapCodec<TriggeredDispenserBlock> getCodec() {
		return CODEC;
	}

	public TriggeredDispenserBlock(AbstractBlock.Settings settings) {
		super(settings);
	}

	@Override
	public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
		return new TriggeredDispenserBlockEntity(pos, state);
	}

	@Override
	protected void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
		if (!state.isOf(newState.getBlock())) {
			if (world.getBlockEntity(pos) instanceof Inventory inventory) {
				inventory.clear();
			}
		}
		super.onStateReplaced(state, world, pos, newState, moved);
	}

	@Override
	public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
		if (player.isCreative()) {
			if (world.isClient) {
				return ActionResult.SUCCESS;
			} else {
				BlockEntity blockEntity = world.getBlockEntity(pos);
				if (blockEntity instanceof TriggeredDispenserBlockEntity triggeredDispenserBlockEntity) {
					player.openHandledScreen(triggeredDispenserBlockEntity);
				}
				return ActionResult.CONSUME;
			}
		} else {
			return ActionResult.PASS;
		}
	}

	@Override
	protected void dispense(ServerWorld world, BlockState state, BlockPos pos) {
		DispenserBlockEntity dispenserBlockEntity = (DispenserBlockEntity) world.getBlockEntity(pos, EntityRegistry.TRIGGERED_DISPENSER_BLOCK_ENTITY).orElse(null);
		if (dispenserBlockEntity == null) {
			LOGGER.warn("Ignoring dispensing attempt for Dispenser without matching block entity at {}", pos);
		} else {
			BlockPointer blockPointer = new BlockPointer(world, pos, state, dispenserBlockEntity);
			int i = dispenserBlockEntity.chooseNonEmptySlot(world.random);
			if (i < 0) {
				world.syncWorldEvent(WorldEvents.DISPENSER_FAILS, pos, 0);
				world.emitGameEvent(GameEvent.BLOCK_ACTIVATE, pos, GameEvent.Emitter.of(dispenserBlockEntity.getCachedState()));
			} else {
				ItemStack itemStack = dispenserBlockEntity.getStack(i);
				ItemStack itemStack1 = itemStack.copy();
				DispenserBehavior dispenserBehavior = this.getBehaviorForItem(world, itemStack);
				if (dispenserBehavior != DispenserBehavior.NOOP) {
					ItemStack newItemStack = dispenserBehavior.dispense(blockPointer, itemStack);
					if (ItemStack.areItemsAndComponentsEqual(itemStack, newItemStack)) {
						dispenserBlockEntity.setStack(i, itemStack1);
					} else {
						dispenserBlockEntity.setStack(i, newItemStack);
					}
				}
			}
		}
	}
}
