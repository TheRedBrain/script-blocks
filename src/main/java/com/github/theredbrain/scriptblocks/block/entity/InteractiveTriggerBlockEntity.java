package com.github.theredbrain.scriptblocks.block.entity;

import com.github.theredbrain.scriptblocks.block.InteractiveTriggerBlock;
import com.github.theredbrain.scriptblocks.block.Resetable;
import com.github.theredbrain.scriptblocks.block.RotatedBlockWithEntity;
import com.github.theredbrain.scriptblocks.block.Triggerable;
import com.github.theredbrain.scriptblocks.component.type.InteractiveKeyComponent;
import com.github.theredbrain.scriptblocks.registry.ScriptBlocksDataComponents;
import com.github.theredbrain.scriptblocks.registry.ScriptBlocksEntities;
import com.github.theredbrain.scriptblocks.util.BlockRotationUtils;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import org.apache.commons.lang3.tuple.MutablePair;

public class InteractiveTriggerBlockEntity extends RotatedBlockEntity implements Resetable {
	private MutablePair<BlockPos, Boolean> triggeredBlock = new MutablePair<>(new BlockPos(0, 0, 0), false);
	private String keyIdentifierString = "";
	private String lockedMessage = "";
	private String lockedSound = "";
	private String unlockedMessage = "";
	private String unlockedSound = "";

	public InteractiveTriggerBlockEntity(BlockPos pos, BlockState state) {
		super(ScriptBlocksEntities.INTERACTIVE_TRIGGER_BLOCK_ENTITY, pos, state);
	}

	@Override
	protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {

		NbtCompound nbtCompound = new NbtCompound();
		nbtCompound.put("position_offset", NbtHelper.fromBlockPos(this.triggeredBlock.getLeft()));
		nbtCompound.putBoolean("resets", this.triggeredBlock.getRight());
		nbt.put("triggered_block", nbtCompound.copy());

		nbt.putString("key_identifier_string", this.keyIdentifierString);

		nbt.putString("locked_message", this.lockedMessage);

		nbt.putString("locked_sound", this.lockedSound);

		nbt.putString("unlocked_message", this.unlockedMessage);

		nbt.putString("unlocked_sound", this.unlockedSound);

		super.writeNbt(nbt, registryLookup);
	}

	@Override
	protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {

		if (nbt.contains("triggeredBlockPositionOffsetX") || nbt.contains("triggeredBlockPositionOffsetY") || nbt.contains("triggeredBlockPositionOffsetZ") || nbt.contains("triggeredBlockResets")) {
			this.triggeredBlock = new MutablePair<>(
					new BlockPos(
							MathHelper.clamp(nbt.getInt("triggeredBlockPositionOffsetX"), -48, 48),
							MathHelper.clamp(nbt.getInt("triggeredBlockPositionOffsetY"), -48, 48),
							MathHelper.clamp(nbt.getInt("triggeredBlockPositionOffsetZ"), -48, 48)
					),
					nbt.getBoolean("triggeredBlockResets")
			);
			nbt.remove("triggeredBlockPositionOffsetX");
			nbt.remove("triggeredBlockPositionOffsetY");
			nbt.remove("triggeredBlockPositionOffsetZ");
			nbt.remove("triggeredBlockResets");
		} else if (nbt.contains("triggered_block_position_offset_x") || nbt.contains("triggered_block_position_offset_y") || nbt.contains("triggered_block_position_offset_z")) {
			this.triggeredBlock = new MutablePair<>(
					new BlockPos(
							MathHelper.clamp(nbt.getInt("triggered_block_position_offset_x"), -48, 48),
							MathHelper.clamp(nbt.getInt("triggered_block_position_offset_y"), -48, 48),
							MathHelper.clamp(nbt.getInt("triggered_block_position_offset_z"), -48, 48)
					),
					nbt.getBoolean("triggered_block_resets")
			);
			nbt.remove("triggered_block_position_offset_x");
			nbt.remove("triggered_block_position_offset_y");
			nbt.remove("triggered_block_position_offset_z");
		} else {
			NbtCompound nbtCompound = nbt.getCompound("triggered_block");
			this.triggeredBlock = new MutablePair<>(
					BlockPos.max(
							BlockPos.min(
									NbtHelper.toBlockPos(nbtCompound, "position_offset").orElse(BlockPos.ORIGIN),
									new BlockPos(48, 48, 48)
							),
							new BlockPos(-48, -48, -48)
					),
					nbtCompound.getBoolean("resets")
			);
		}

		if (nbt.contains("keyIdentifierString")) {
			this.keyIdentifierString = nbt.getString("keyIdentifierString");
			nbt.remove("keyIdentifierString");
		} else {
			this.keyIdentifierString = nbt.getString("key_identifier_string");
		}

		if (nbt.contains("lockedMessage")) {
			this.lockedMessage = nbt.getString("lockedMessage");
			nbt.remove("lockedMessage");
		} else {
			this.lockedMessage = nbt.getString("locked_message");
		}

		if (nbt.contains("lockedSound")) {
			this.lockedSound = nbt.getString("lockedSound");
			nbt.remove("lockedSound");
		} else {
			this.lockedSound = nbt.getString("locked_sound");
		}

		if (nbt.contains("unlockedMessage")) {
			this.unlockedMessage = nbt.getString("unlockedMessage");
			nbt.remove("unlockedMessage");
		} else {
			this.unlockedMessage = nbt.getString("unlocked_message");
		}

		if (nbt.contains("unlockedSound")) {
			this.unlockedSound = nbt.getString("unlockedSound");
			nbt.remove("unlockedSound");
		} else {
			this.unlockedSound = nbt.getString("unlocked_sound");
		}

		super.readNbt(nbt, registryLookup);
	}

	public BlockEntityUpdateS2CPacket toUpdatePacket() {
		return BlockEntityUpdateS2CPacket.create(this);
	}

	@Override
	public NbtCompound toInitialChunkDataNbt(RegistryWrapper.WrapperLookup registryLookup) {
		return this.createComponentlessNbt(registryLookup);
	}

	// region --- getter & setter ---
	public MutablePair<BlockPos, Boolean> getTriggeredBlock() {
		return this.triggeredBlock;
	}

	public void setTriggeredBlock(MutablePair<BlockPos, Boolean> triggeredBlock) {
		this.triggeredBlock = triggeredBlock;
	}

	public String getKeyIdentifierString() {
		return this.keyIdentifierString;
	}

	public void setKeyIdentifierString(String keyIdentifierString) {
		this.keyIdentifierString = keyIdentifierString;
	}

	public String getLockedMessage() {
		return this.lockedMessage;
	}

	public void setLockedMessage(String lockedMessage) {
		this.lockedMessage = lockedMessage;
	}

	public String getLockedSound() {
		return this.lockedSound;
	}

	public void setLockedSound(String lockedSound) {
		this.lockedSound = lockedSound;
	}

	public String getUnlockedMessage() {
		return this.unlockedMessage;
	}

	public void setUnlockedMessage(String unlockedMessage) {
		this.unlockedMessage = unlockedMessage;
	}

	public String getUnlockedSound() {
		return this.unlockedSound;
	}

	public void setUnlockedSound(String unlockedSound) {
		this.unlockedSound = unlockedSound;
	}
	// endregion --- getter & setter ---

	public boolean canTrigger(PlayerEntity playerEntity) {
		if (!this.keyIdentifierString.isEmpty()) {
			InteractiveKeyComponent interactiveKeyComponent = playerEntity.getActiveItem().get(ScriptBlocksDataComponents.INTERACTIVE_KEY);
			if (interactiveKeyComponent != null) {
				return interactiveKeyComponent.identifier_list().contains(Identifier.of(this.keyIdentifierString));
			}
			return false;
		}
		return true;
	}

	public void tryToConsumeKeyItem(PlayerEntity playerEntity) {
		if (!this.keyIdentifierString.isEmpty()) {
			Hand hand = playerEntity.getActiveHand();
			ItemStack stack = playerEntity.getStackInHand(hand);
			InteractiveKeyComponent interactiveKeyComponent = stack.get(ScriptBlocksDataComponents.INTERACTIVE_KEY);
			if (interactiveKeyComponent != null) {
				if (interactiveKeyComponent.is_consumed()) {
					if (stack.getMaxCount() > 1) {
						stack.decrementUnlessCreative(1, playerEntity);
						if (stack.getCount() > 0) {
							playerEntity.setStackInHand(hand, stack);
						} else {
							playerEntity.setStackInHand(hand, ItemStack.EMPTY);
						}
					} else {
						stack.damage(1, playerEntity, hand == Hand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND);
					}
				}
			}
		}
	}

	public void trigger() {
		if (this.world != null) {
			BlockEntity blockEntity = world.getBlockEntity(new BlockPos(this.pos.getX() + this.triggeredBlock.getLeft().getX(), this.pos.getY() + this.triggeredBlock.getLeft().getY(), this.pos.getZ() + this.triggeredBlock.getLeft().getZ()));
			if (blockEntity != this) {
				boolean triggeredBlockResets = this.triggeredBlock.getRight();
				if (triggeredBlockResets && blockEntity instanceof Resetable resetable) {
					resetable.reset();
				} else if (!triggeredBlockResets && blockEntity instanceof Triggerable triggerable) {
					triggerable.trigger();
				}
			}
		}
	}

	@Override
	protected void onRotate(BlockState state) {
		if (state.getBlock() instanceof RotatedBlockWithEntity) {
			if (state.get(RotatedBlockWithEntity.ROTATED) != this.rotated) {
				BlockRotation blockRotation = BlockRotationUtils.calculateRotationFromDifferentRotatedStates(state.get(RotatedBlockWithEntity.ROTATED), this.rotated);
				this.triggeredBlock.setLeft(BlockRotationUtils.rotateOffsetBlockPos(this.triggeredBlock.getLeft(), blockRotation));
				this.rotated = state.get(RotatedBlockWithEntity.ROTATED);
			}
			if (state.get(RotatedBlockWithEntity.X_MIRRORED) != this.x_mirrored) {
				this.triggeredBlock.setLeft(BlockRotationUtils.mirrorOffsetBlockPos(this.triggeredBlock.getLeft(), BlockMirror.FRONT_BACK));
				this.x_mirrored = state.get(RotatedBlockWithEntity.X_MIRRORED);
			}
			if (state.get(RotatedBlockWithEntity.Z_MIRRORED) != this.z_mirrored) {
				this.triggeredBlock.setLeft(BlockRotationUtils.mirrorOffsetBlockPos(this.triggeredBlock.getLeft(), BlockMirror.LEFT_RIGHT));
				this.z_mirrored = state.get(RotatedBlockWithEntity.Z_MIRRORED);
			}
		}
	}

	@Override
	public void reset() {
		if (this.world != null) {
			BlockState state = this.world.getBlockState(this.pos);
			if (state.getBlock() instanceof InteractiveTriggerBlock interactiveTriggerBlock) {
				interactiveTriggerBlock.reset(state, this.world, this.pos);
			}
		}
	}
}
