package com.github.theredbrain.scriptblocks.item;

import com.github.theredbrain.scriptblocks.component.type.BlockPositionDistanceMeterComponent;
import com.github.theredbrain.scriptblocks.registry.ScriptBlocksDataComponents;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

public class BlockPositionDistanceMeterItem extends Item {

	public BlockPositionDistanceMeterItem(Properties settings) {
		super(settings);
	}

	@Override
	public InteractionResult useOn(UseOnContext context) {
		ItemStack itemStack = context.getItemInHand();
		BlockPositionDistanceMeterComponent blockPositionDistanceMeterComponent = itemStack.get(ScriptBlocksDataComponents.BLOCK_POSITION_DISTANCE_METER);
		Player playerEntity = context.getPlayer();
		BlockPos pos = context.getClickedPos();
		if (playerEntity != null && blockPositionDistanceMeterComponent != null) {
			boolean isRootMode = blockPositionDistanceMeterComponent.is_root_mode();
			BlockPos offset;
			if (playerEntity.isShiftKeyDown()) {
				BlockPos root_pos;
				BlockPos offset_pos;
				if (isRootMode) {
					root_pos = pos;
					offset_pos = blockPositionDistanceMeterComponent.offset_pos();
					if (playerEntity.level().isClientSide()) {
						playerEntity.sendSystemMessage(Component.translatable("item.scriptblocks.block_position_distance_meter.set_root_block", pos.getX(), pos.getY(), pos.getZ()));
					}
				} else {
					root_pos = blockPositionDistanceMeterComponent.root_pos();
					offset_pos = pos;
					if (playerEntity.level().isClientSide()) {
						playerEntity.sendSystemMessage(Component.translatable("item.scriptblocks.block_position_distance_meter.set_offset_block", pos.getX(), pos.getY(), pos.getZ()));
					}
				}
				if (root_pos.getY() > -64 && offset_pos.getY() > -64) {
					int offset_x = offset_pos.getX() - root_pos.getX();
					int offset_y = offset_pos.getY() - root_pos.getY();
					int offset_z = offset_pos.getZ() - root_pos.getZ();
					offset = new BlockPos(offset_pos.getX() - root_pos.getX(), offset_pos.getY() - root_pos.getY(), offset_pos.getZ() - root_pos.getZ());
					if (playerEntity.level().isClientSide()) {
						playerEntity.sendSystemMessage(Component.translatable("item.scriptblocks.block_position_distance_meter.info", offset_x, offset_y, offset_z));
					}
				} else {
					offset = BlockPos.ZERO;
				}
				itemStack.set(ScriptBlocksDataComponents.BLOCK_POSITION_DISTANCE_METER, new BlockPositionDistanceMeterComponent(isRootMode, root_pos, offset_pos, offset));

			} else if (playerEntity.level().isClientSide()) {
				playerEntity.sendSystemMessage(isRootMode ? Component.translatable("item.scriptblocks.block_position_distance_meter.root_mode") : Component.translatable("item.scriptblocks.block_position_distance_meter.offset_mode"));

				offset = blockPositionDistanceMeterComponent.offset();
				if (offset.getX() != 0 || offset.getY() != 0 || offset.getZ() != 0) {
					playerEntity.sendSystemMessage(Component.translatable("item.scriptblocks.block_position_distance_meter.info", offset.getX(), offset.getY(), offset.getZ()));
				}
			}
			return InteractionResult.SUCCESS;
		}
		return InteractionResult.PASS;
	}

	@Override
	public InteractionResult use(Level world, Player user, InteractionHand hand) {
		ItemStack itemStack = user.getItemInHand(hand);
		BlockPositionDistanceMeterComponent blockPositionDistanceMeterComponent = itemStack.get(ScriptBlocksDataComponents.BLOCK_POSITION_DISTANCE_METER);
		if (blockPositionDistanceMeterComponent != null) {
			boolean isRootMode = blockPositionDistanceMeterComponent.is_root_mode();
			if (user.isShiftKeyDown()) {

				itemStack.set(ScriptBlocksDataComponents.BLOCK_POSITION_DISTANCE_METER, new BlockPositionDistanceMeterComponent(!isRootMode, blockPositionDistanceMeterComponent.root_pos(), blockPositionDistanceMeterComponent.offset_pos(), blockPositionDistanceMeterComponent.offset()));
				if (world.isClientSide()) {
					user.sendSystemMessage(isRootMode ? Component.translatable("item.scriptblocks.block_position_distance_meter.set_root_mode.false") : Component.translatable("item.scriptblocks.block_position_distance_meter.set_root_mode.true"));
				}
			} else if (user.level().isClientSide()) {
				user.sendSystemMessage(isRootMode ? Component.translatable("item.scriptblocks.block_position_distance_meter.root_mode") : Component.translatable("item.scriptblocks.block_position_distance_meter.offset_mode"));

				BlockPos offset = blockPositionDistanceMeterComponent.offset();
				if (offset.getX() != 0 || offset.getY() != 0 || offset.getZ() != 0) {
					user.sendSystemMessage(Component.translatable("item.scriptblocks.block_position_distance_meter.info", offset.getX(), offset.getY(), offset.getZ()));
				}
			}
			return InteractionResult.CONSUME;
		}
		return super.use(world, user, hand);
	}

	@Deprecated
	public void appendHoverText(
			ItemStack stack, Item.TooltipContext context, TooltipDisplay displayComponent, Consumer<Component> textConsumer, TooltipFlag type
	) {
		BlockPositionDistanceMeterComponent blockPositionDistanceMeterComponent = stack.get(ScriptBlocksDataComponents.BLOCK_POSITION_DISTANCE_METER);
		if (blockPositionDistanceMeterComponent != null) {
			blockPositionDistanceMeterComponent.addToTooltip(context, textConsumer, type, stack.getComponents());
		}
	}

}
