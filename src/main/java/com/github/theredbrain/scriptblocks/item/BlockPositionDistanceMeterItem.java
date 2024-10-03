package com.github.theredbrain.scriptblocks.item;

import com.github.theredbrain.scriptblocks.component.type.BlockPositionDistanceMeterComponent;
import com.github.theredbrain.scriptblocks.registry.ItemComponentRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;

public class BlockPositionDistanceMeterItem extends Item {

	public BlockPositionDistanceMeterItem(Settings settings) {
		super(settings);
	}

	@Override
	public ActionResult useOnBlock(ItemUsageContext context) {
		ItemStack itemStack = context.getStack();
		BlockPositionDistanceMeterComponent blockPositionDistanceMeterComponent = itemStack.get(ItemComponentRegistry.BLOCK_POSITION_DISTANCE_METER);
		PlayerEntity playerEntity = context.getPlayer();
		BlockPos pos = context.getBlockPos();
		if (playerEntity != null && blockPositionDistanceMeterComponent != null) {
			boolean isRootMode = blockPositionDistanceMeterComponent.is_root_mode();
			BlockPos offset;
			if (playerEntity.isSneaking()) {
				BlockPos root_pos;
				BlockPos offset_pos;
				if (isRootMode) {
					root_pos = pos;
					offset_pos = blockPositionDistanceMeterComponent.offset_pos();
					if (playerEntity.getEntityWorld().isClient()) {
						playerEntity.sendMessage(Text.translatable("item.scriptblocks.block_position_distance_meter.set_root_block", pos.getX(), pos.getY(), pos.getZ()));
					}
				} else {
					root_pos = blockPositionDistanceMeterComponent.root_pos();
					offset_pos = pos;
					if (playerEntity.getEntityWorld().isClient()) {
						playerEntity.sendMessage(Text.translatable("item.scriptblocks.block_position_distance_meter.set_offset_block", pos.getX(), pos.getY(), pos.getZ()));
					}
				}
				if (root_pos.getY() > -64 && offset_pos.getY() > -64) {
					int offset_x = offset_pos.getX() - root_pos.getX();
					int offset_y = offset_pos.getY() - root_pos.getY();
					int offset_z = offset_pos.getZ() - root_pos.getZ();
					offset = new BlockPos(offset_pos.getX() - root_pos.getX(), offset_pos.getY() - root_pos.getY(), offset_pos.getZ() - root_pos.getZ());
					if (playerEntity.getEntityWorld().isClient()) {
						playerEntity.sendMessage(Text.translatable("item.scriptblocks.block_position_distance_meter.info", offset_x, offset_y, offset_z));
					}
				} else {
					offset = BlockPos.ORIGIN;
				}
				itemStack.set(ItemComponentRegistry.BLOCK_POSITION_DISTANCE_METER, new BlockPositionDistanceMeterComponent(isRootMode, root_pos, offset_pos, offset));

			} else if (playerEntity.getEntityWorld().isClient()) {
				playerEntity.sendMessage(isRootMode ? Text.translatable("item.scriptblocks.block_position_distance_meter.root_mode") : Text.translatable("item.scriptblocks.block_position_distance_meter.offset_mode"));

				offset = blockPositionDistanceMeterComponent.offset();
				if (offset.getX() != 0 || offset.getY() != 0 || offset.getZ() != 0) {
					playerEntity.sendMessage(Text.translatable("item.scriptblocks.block_position_distance_meter.info", offset.getX(), offset.getY(), offset.getZ()));
				}
			}
			return ActionResult.SUCCESS;
		}
		return ActionResult.PASS;
	}

	@Override
	public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
		ItemStack itemStack = user.getStackInHand(hand);
		BlockPositionDistanceMeterComponent blockPositionDistanceMeterComponent = itemStack.get(ItemComponentRegistry.BLOCK_POSITION_DISTANCE_METER);
		if (blockPositionDistanceMeterComponent != null) {
			boolean isRootMode = blockPositionDistanceMeterComponent.is_root_mode();
			if (user.isSneaking()) {

				itemStack.set(ItemComponentRegistry.BLOCK_POSITION_DISTANCE_METER, new BlockPositionDistanceMeterComponent(!isRootMode, blockPositionDistanceMeterComponent.root_pos(), blockPositionDistanceMeterComponent.offset_pos(), blockPositionDistanceMeterComponent.offset()));
				if (world.isClient()) {
					user.sendMessage(isRootMode ? Text.translatable("item.scriptblocks.block_position_distance_meter.set_root_mode.false") : Text.translatable("item.scriptblocks.block_position_distance_meter.set_root_mode.true"));
				}
			} else if (user.getEntityWorld().isClient()) {
				user.sendMessage(isRootMode ? Text.translatable("item.scriptblocks.block_position_distance_meter.root_mode") : Text.translatable("item.scriptblocks.block_position_distance_meter.offset_mode"));

				BlockPos offset = blockPositionDistanceMeterComponent.offset();
				if (offset.getX() != 0 || offset.getY() != 0 || offset.getZ() != 0) {
					user.sendMessage(Text.translatable("item.scriptblocks.block_position_distance_meter.info", offset.getX(), offset.getY(), offset.getZ()));
				}
			}
			return TypedActionResult.consume(itemStack);
		}
		return super.use(world, user, hand);
	}

	@Override
	public void appendTooltip(ItemStack stack, Item.TooltipContext context, List<Text> tooltip, TooltipType type) {
		BlockPositionDistanceMeterComponent blockPositionDistanceMeterComponent = stack.get(ItemComponentRegistry.BLOCK_POSITION_DISTANCE_METER);
		if (blockPositionDistanceMeterComponent != null) {
			blockPositionDistanceMeterComponent.appendTooltip(context, tooltip::add, type);
		}
	}
}
