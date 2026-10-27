package com.github.theredbrain.scriptblocks.block.entity;

import com.github.theredbrain.scriptblocks.block.InteractiveTriggerBlock;
import com.github.theredbrain.scriptblocks.block.Resetable;
import com.github.theredbrain.scriptblocks.block.RotatedBlockWithEntity;
import com.github.theredbrain.scriptblocks.block.Triggerable;
import com.github.theredbrain.scriptblocks.component.type.InteractiveKeyComponent;
import com.github.theredbrain.scriptblocks.entity.player.PlayerEntityHelper;
import com.github.theredbrain.scriptblocks.registry.ScriptBlocksDataComponents;
import com.github.theredbrain.scriptblocks.registry.ScriptBlocksEntities;
import com.github.theredbrain.scriptblocks.util.BlockRotationUtils;
import com.github.theredbrain.scriptblocks.util.ScriptBlocksBlockProperties;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.apache.commons.lang3.tuple.MutablePair;

public class InteractiveTriggerBlockEntity extends RotatedBlockEntity implements Triggerable, Resetable {
	private MutablePair<BlockPos, Boolean> triggeredBlock = new MutablePair<>(new BlockPos(0, 0, 0), false);
	private String keyIdentifierString = "";
	private String preventInteractionIdentifierString = "";
	private String interactionPreventedMessage = "";
	private String interactionPreventedSound = "";
	private String lockedMessage = "";
	private String lockedSound = "";
	private String unlockedMessage = "";
	private String unlockedSound = "";

	public InteractiveTriggerBlockEntity(BlockPos pos, BlockState state) {
		super(ScriptBlocksEntities.INTERACTIVE_TRIGGER_BLOCK_ENTITY, pos, state);
	}

	@Override
	protected void saveAdditional(ValueOutput view) {

		CompoundTag nbtCompound = new CompoundTag();
		nbtCompound.store("position_offset", BlockPos.CODEC, this.triggeredBlock.getLeft());
		nbtCompound.putBoolean("resets", this.triggeredBlock.getRight());
		view.store("triggered_block", CompoundTag.CODEC, nbtCompound.copy());

		view.putString("prevent_interaction_identifier_string", this.preventInteractionIdentifierString);

		view.putString("interaction_prevented_message", this.interactionPreventedMessage);

		view.putString("interaction_prevented_sound", this.interactionPreventedSound);

		view.putString("key_identifier_string", this.keyIdentifierString);

		view.putString("locked_message", this.lockedMessage);

		view.putString("locked_sound", this.lockedSound);

		view.putString("unlocked_message", this.unlockedMessage);

		view.putString("unlocked_sound", this.unlockedSound);

		super.saveAdditional(view);
	}

	@Override
	protected void loadAdditional(ValueInput view) {

		CompoundTag nbtCompound = view.read("triggered_block", CompoundTag.CODEC).orElseGet(CompoundTag::new);
		this.triggeredBlock = new MutablePair<>(
				BlockPos.max(
						BlockPos.min(
								nbtCompound.read("position_offset", BlockPos.CODEC).orElse(BlockPos.ZERO),
								new BlockPos(48, 48, 48)
						),
						new BlockPos(-48, -48, -48)
				),
				nbtCompound.getBooleanOr("resets", false)
		);

		this.preventInteractionIdentifierString = view.getStringOr("prevent_interaction_identifier_string", "");

		this.interactionPreventedMessage = view.getStringOr("interaction_prevented_message", "");

		this.interactionPreventedSound = view.getStringOr("interaction_prevented_sound", "");

		this.keyIdentifierString = view.getStringOr("key_identifier_string", "");

		this.lockedMessage = view.getStringOr("locked_message", "");

		this.lockedSound = view.getStringOr("locked_sound", "");

		this.unlockedMessage = view.getStringOr("unlocked_message", "");

		this.unlockedSound = view.getStringOr("unlocked_sound", "");

		super.loadAdditional(view);
	}

	public ClientboundBlockEntityDataPacket getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}

	@Override
	public CompoundTag getUpdateTag(HolderLookup.Provider registryLookup) {
		return this.saveCustomOnly(registryLookup);
	}

	// region --- getter & setter ---
	public MutablePair<BlockPos, Boolean> getTriggeredBlock() {
		return this.triggeredBlock;
	}

	public void setTriggeredBlock(MutablePair<BlockPos, Boolean> triggeredBlock) {
		this.triggeredBlock = triggeredBlock;
	}

	public String getPreventInteractionIdentifierString() {
		return this.preventInteractionIdentifierString;
	}

	public void setPreventInteractionIdentifierString(String preventInteractionIdentifierString) {
		this.preventInteractionIdentifierString = preventInteractionIdentifierString;
	}

	public String getInteractionPreventedMessage() {
		return this.interactionPreventedMessage;
	}

	public void setInteractionPreventedMessage(String interactionPreventedMessage) {
		this.interactionPreventedMessage = interactionPreventedMessage;
	}

	public String getInteractionPreventedSound() {
		return this.interactionPreventedSound;
	}

	public void setInteractionPreventedSound(String interactionPreventedSound) {
		this.interactionPreventedSound = interactionPreventedSound;
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

	public boolean isInteractionPrevented(Player playerEntity) {
		return this.preventInteractionIdentifierString.isEmpty() || PlayerEntityHelper.allowInteractions(playerEntity, this.preventInteractionIdentifierString);
	}

	public boolean canTrigger(Player playerEntity) {
		if (!this.keyIdentifierString.isEmpty()) {
			InteractiveKeyComponent interactiveKeyComponent = playerEntity.getUseItem().get(ScriptBlocksDataComponents.INTERACTIVE_KEY);
			if (interactiveKeyComponent != null) {
				return interactiveKeyComponent.identifier_list().contains(Identifier.parse(this.keyIdentifierString));
			}
			return false;
		}
		return true;
	}

	public void tryToConsumeKeyItem(Player playerEntity) {
		if (!this.keyIdentifierString.isEmpty()) {
			InteractionHand hand = playerEntity.getUsedItemHand();
			ItemStack stack = playerEntity.getItemInHand(hand);
			InteractiveKeyComponent interactiveKeyComponent = stack.get(ScriptBlocksDataComponents.INTERACTIVE_KEY);
			if (interactiveKeyComponent != null) {
				if (interactiveKeyComponent.is_consumed()) {
					if (stack.getMaxStackSize() > 1) {
						stack.consume(1, playerEntity);
						if (stack.getCount() > 0) {
							playerEntity.setItemInHand(hand, stack);
						} else {
							playerEntity.setItemInHand(hand, ItemStack.EMPTY);
						}
					} else {
						stack.hurtAndBreak(1, playerEntity, hand == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND);
					}
				}
			}
		}
	}

	public void triggeredOnInteraction() {
		if (this.level != null) {
			BlockEntity blockEntity = level.getBlockEntity(new BlockPos(this.worldPosition.getX() + this.triggeredBlock.getLeft().getX(), this.worldPosition.getY() + this.triggeredBlock.getLeft().getY(), this.worldPosition.getZ() + this.triggeredBlock.getLeft().getZ()));
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
			if (state.getValue(ScriptBlocksBlockProperties.ROTATED) != this.rotated) {
				Rotation blockRotation = BlockRotationUtils.calculateRotationFromDifferentRotatedStates(state.getValue(ScriptBlocksBlockProperties.ROTATED), this.rotated);
				this.triggeredBlock.setLeft(BlockRotationUtils.rotateOffsetBlockPos(this.triggeredBlock.getLeft(), blockRotation));
				this.rotated = state.getValue(ScriptBlocksBlockProperties.ROTATED);
			}
			if (state.getValue(ScriptBlocksBlockProperties.X_MIRRORED) != this.x_mirrored) {
				this.triggeredBlock.setLeft(BlockRotationUtils.mirrorOffsetBlockPos(this.triggeredBlock.getLeft(), Mirror.FRONT_BACK));
				this.x_mirrored = state.getValue(ScriptBlocksBlockProperties.X_MIRRORED);
			}
			if (state.getValue(ScriptBlocksBlockProperties.Z_MIRRORED) != this.z_mirrored) {
				this.triggeredBlock.setLeft(BlockRotationUtils.mirrorOffsetBlockPos(this.triggeredBlock.getLeft(), Mirror.LEFT_RIGHT));
				this.z_mirrored = state.getValue(ScriptBlocksBlockProperties.Z_MIRRORED);
			}
		}
	}

	@Override
	public void trigger() {
		if (this.level != null) {
			BlockState state = this.level.getBlockState(this.worldPosition);
			if (state.getBlock() instanceof InteractiveTriggerBlock interactiveTriggerBlock) {
				if (!this.getUnlockedSound().isEmpty() && !this.level.isClientSide()) {
					SoundEvent soundEvent = BuiltInRegistries.SOUND_EVENT.getValue(Identifier.parse(this.getUnlockedSound()));
					if (soundEvent != null) {
						this.level.playSound(null, (double) this.worldPosition.getX() + 0.5, (double) this.worldPosition.getY() + 0.5, (double) this.worldPosition.getZ() + 0.5, soundEvent, SoundSource.BLOCKS, 0.5F, this.level.getRandom().nextFloat() * 0.1F + 0.9F);
					}
				}
				interactiveTriggerBlock.trigger(state, this.level, this.worldPosition);
			}
		}
	}

	@Override
	public void reset() {
		if (this.level != null) {
			BlockState state = this.level.getBlockState(this.worldPosition);
			if (state.getBlock() instanceof InteractiveTriggerBlock interactiveTriggerBlock) {
				interactiveTriggerBlock.reset(state, this.level, this.worldPosition);
			}
		}
	}
}
