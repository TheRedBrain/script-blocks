package com.github.theredbrain.scriptblocks.block.entity;

import com.github.theredbrain.scriptblocks.block.InteractiveTriggerBlock;
import com.github.theredbrain.scriptblocks.block.Resetable;
import com.github.theredbrain.scriptblocks.block.RotatedBlockWithEntity;
import com.github.theredbrain.scriptblocks.block.Triggerable;
import com.github.theredbrain.scriptblocks.component.type.InteractiveKeyComponent;
import com.github.theredbrain.scriptblocks.registry.EntityRegistry;
import com.github.theredbrain.scriptblocks.registry.ItemComponentRegistry;
import com.github.theredbrain.scriptblocks.util.BlockRotationUtils;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
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
		super(EntityRegistry.INTERACTIVE_TRIGGER_BLOCK_ENTITY, pos, state);
	}

	@Override
	protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {

		nbt.putInt("triggeredBlockPositionOffsetX", this.triggeredBlock.getLeft().getX());
		nbt.putInt("triggeredBlockPositionOffsetY", this.triggeredBlock.getLeft().getY());
		nbt.putInt("triggeredBlockPositionOffsetZ", this.triggeredBlock.getLeft().getZ());
		nbt.putBoolean("triggeredBlockResets", this.triggeredBlock.getRight());

		nbt.putString("keyIdentifierString", this.keyIdentifierString);

		nbt.putString("lockedMessage", this.lockedMessage);

		nbt.putString("lockedSound", this.lockedSound);

		nbt.putString("unlockedMessage", this.unlockedMessage);

		nbt.putString("unlockedSound", this.unlockedSound);

		super.writeNbt(nbt, registryLookup);
	}

	@Override
	protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {

		int x = MathHelper.clamp(nbt.getInt("triggeredBlockPositionOffsetX"), -48, 48);
		int y = MathHelper.clamp(nbt.getInt("triggeredBlockPositionOffsetY"), -48, 48);
		int z = MathHelper.clamp(nbt.getInt("triggeredBlockPositionOffsetZ"), -48, 48);
		this.triggeredBlock = new MutablePair<>(new BlockPos(x, y, z), nbt.getBoolean("triggeredBlockResets"));

		this.keyIdentifierString = nbt.getString("keyIdentifierString");

		this.lockedMessage = nbt.getString("lockedMessage");

		this.lockedSound = nbt.getString("lockedSound");

		this.unlockedMessage = nbt.getString("unlockedMessage");

		this.unlockedSound = nbt.getString("unlockedSound");

		super.readNbt(nbt, registryLookup);
	}

	public BlockEntityUpdateS2CPacket toUpdatePacket() {
		return BlockEntityUpdateS2CPacket.create(this);
	}

	@Override
	public NbtCompound toInitialChunkDataNbt(RegistryWrapper.WrapperLookup registryLookup) {
		return this.createComponentlessNbt(registryLookup);
	}

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

	public boolean canTrigger(PlayerEntity playerEntity) {
		if (!this.keyIdentifierString.isEmpty()) {
			InteractiveKeyComponent interactiveKeyComponent = playerEntity.getActiveItem().get(ItemComponentRegistry.INTERACTIVE_KEY);
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
			InteractiveKeyComponent interactiveKeyComponent = stack.get(ItemComponentRegistry.INTERACTIVE_KEY);
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
