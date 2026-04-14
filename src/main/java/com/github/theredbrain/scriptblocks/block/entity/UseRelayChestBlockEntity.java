package com.github.theredbrain.scriptblocks.block.entity;

import com.github.theredbrain.scriptblocks.ScriptBlocks;
import com.github.theredbrain.scriptblocks.block.Resetable;
import com.github.theredbrain.scriptblocks.block.RotatedBlockWithEntity;
import com.github.theredbrain.scriptblocks.block.Triggerable;
import com.github.theredbrain.scriptblocks.block.UseRelayChestBlock;
import com.github.theredbrain.scriptblocks.component.type.InteractiveKeyComponent;
import com.github.theredbrain.scriptblocks.registry.BlockRegistry;
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

public class UseRelayChestBlockEntity extends RotatedBlockEntity implements Resetable {

	private BlockPos relayBlockPositionOffset = BlockPos.ORIGIN;
	private MutablePair<BlockPos, Boolean> triggeredBlock = new MutablePair<>(BlockPos.ORIGIN, false);
	private String keyIdentifierString = "";
	private String lockedMessage = "";
	private String lockedSound = "";
	private String unlockedMessage = "";
	private String unlockedSound = "";

	public UseRelayChestBlockEntity(BlockPos pos, BlockState state) {
		super(EntityRegistry.USE_RELAY_CHEST_BLOCK_ENTITY, pos, state);
	}

	@Override
	protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {

		nbt.putInt("relay_block_position_offset_x", this.relayBlockPositionOffset.getX());
		nbt.putInt("relay_block_position_offset_y", this.relayBlockPositionOffset.getY());
		nbt.putInt("relay_block_position_offset_z", this.relayBlockPositionOffset.getZ());

		nbt.putInt("triggered_block_position_offset_x", this.triggeredBlock.getLeft().getX());
		nbt.putInt("triggered_block_position_offset_y", this.triggeredBlock.getLeft().getY());
		nbt.putInt("triggered_block_position_offset_z", this.triggeredBlock.getLeft().getZ());
		nbt.putBoolean("triggered_block_resets", this.triggeredBlock.getRight());

		nbt.putString("key_identifier_string", this.keyIdentifierString);

		nbt.putString("locked_message", this.lockedMessage);

		nbt.putString("locked_sound", this.lockedSound);

		nbt.putString("unlocked_message", this.unlockedMessage);

		nbt.putString("unlocked_sound", this.unlockedSound);

		if (this.world != null && this.world.getBlockState(this.pos).isOf(BlockRegistry.LOCKED_USE_RELAY_CHEST)) {
			ScriptBlocks.sendDeprecatedFeatureInfo("Deprecated 'Locked Use Relay Chest' detected at: " + this.pos.toString() + ". This block will be removed in the future and should be replaced with a 'Trapped Use Relay Chest'.", this.world != null ? this.world.getServer() : null);
		}

		super.writeNbt(nbt, registryLookup);
	}

	@Override
	protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {

		if (nbt.contains("relayBlockPositionOffsetX") || nbt.contains("relayBlockPositionOffsetY") || nbt.contains("relayBlockPositionOffsetZ")) {
			this.relayBlockPositionOffset = new BlockPos(
					MathHelper.clamp(nbt.getInt("relayBlockPositionOffsetX"), -48, 48),
					MathHelper.clamp(nbt.getInt("relayBlockPositionOffsetY"), -48, 48),
					MathHelper.clamp(nbt.getInt("relayBlockPositionOffsetZ"), -48, 48)
			);
			nbt.remove("relayBlockPositionOffsetX");
			nbt.remove("relayBlockPositionOffsetY");
			nbt.remove("relayBlockPositionOffsetZ");
		} else {
			this.relayBlockPositionOffset = new BlockPos(
					MathHelper.clamp(nbt.getInt("relay_block_position_offset_x"), -48, 48),
					MathHelper.clamp(nbt.getInt("relay_block_position_offset_y"), -48, 48),
					MathHelper.clamp(nbt.getInt("relay_block_position_offset_z"), -48, 48)
			);
		}

		if (nbt.contains("triggeredBlockPositionOffsetX") || nbt.contains("triggeredBlockPositionOffsetY") || nbt.contains("triggeredBlockPositionOffsetZ") || nbt.contains("triggeredBlockResets")) {
			this.triggeredBlock = new MutablePair<>(new BlockPos(
					MathHelper.clamp(nbt.getInt("triggeredBlockPositionOffsetX"), -48, 48),
					MathHelper.clamp(nbt.getInt("triggeredBlockPositionOffsetY"), -48, 48),
					MathHelper.clamp(nbt.getInt("triggeredBlockPositionOffsetZ"), -48, 48)
			), nbt.getBoolean("triggeredBlockResets"));
			nbt.remove("triggeredBlockPositionOffsetX");
			nbt.remove("triggeredBlockPositionOffsetY");
			nbt.remove("triggeredBlockPositionOffsetZ");
			nbt.remove("triggeredBlockResets");
		} else {
			this.triggeredBlock = new MutablePair<>(new BlockPos(
					MathHelper.clamp(nbt.getInt("triggered_block_position_offset_x"), -48, 48),
					MathHelper.clamp(nbt.getInt("triggered_block_position_offset_y"), -48, 48),
					MathHelper.clamp(nbt.getInt("triggered_block_position_offset_z"), -48, 48)
			), nbt.getBoolean("triggered_block_resets"));
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
	public BlockPos getRelayBlockPositionOffset() {
		return this.relayBlockPositionOffset;
	}

	public void setRelayBlockPositionOffset(BlockPos relayBlockPositionOffset) {
		this.relayBlockPositionOffset = new BlockPos(
				MathHelper.clamp(relayBlockPositionOffset.getX(), -48, 48),
				MathHelper.clamp(relayBlockPositionOffset.getY(), -48, 48),
				MathHelper.clamp(relayBlockPositionOffset.getZ(), -48, 48)
		);
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
	// endregion --- getter & setter ---

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
				this.relayBlockPositionOffset = BlockRotationUtils.rotateOffsetBlockPos(this.relayBlockPositionOffset, blockRotation);
				this.triggeredBlock.setLeft(BlockRotationUtils.rotateOffsetBlockPos(this.triggeredBlock.getLeft(), blockRotation));
				this.rotated = state.get(RotatedBlockWithEntity.ROTATED);
			}
			if (state.get(RotatedBlockWithEntity.X_MIRRORED) != this.x_mirrored) {
				this.relayBlockPositionOffset = BlockRotationUtils.mirrorOffsetBlockPos(this.relayBlockPositionOffset, BlockMirror.FRONT_BACK);
				this.triggeredBlock.setLeft(BlockRotationUtils.mirrorOffsetBlockPos(this.triggeredBlock.getLeft(), BlockMirror.FRONT_BACK));
				this.x_mirrored = state.get(RotatedBlockWithEntity.X_MIRRORED);
			}
			if (state.get(RotatedBlockWithEntity.Z_MIRRORED) != this.z_mirrored) {
				this.relayBlockPositionOffset = BlockRotationUtils.mirrorOffsetBlockPos(this.relayBlockPositionOffset, BlockMirror.LEFT_RIGHT);
				this.triggeredBlock.setLeft(BlockRotationUtils.mirrorOffsetBlockPos(this.triggeredBlock.getLeft(), BlockMirror.LEFT_RIGHT));
				this.z_mirrored = state.get(RotatedBlockWithEntity.Z_MIRRORED);
			}
		}
	}

	@Override
	public void reset() {
		if (this.world != null) {
			BlockEntity blockEntity = this.world.getBlockEntity(this.pos);
			if (blockEntity != null && blockEntity.getCachedState().getBlock() instanceof UseRelayChestBlock) {
				this.world.setBlockState(this.pos, blockEntity.getCachedState().with(UseRelayChestBlock.OPEN, false));
			}
		}
	}
}
