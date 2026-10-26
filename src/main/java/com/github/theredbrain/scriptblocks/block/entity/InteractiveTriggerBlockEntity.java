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
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
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
	protected void writeData(WriteView view) {

		NbtCompound nbtCompound = new NbtCompound();
		nbtCompound.put("position_offset", BlockPos.CODEC, this.triggeredBlock.getLeft());
		nbtCompound.putBoolean("resets", this.triggeredBlock.getRight());
		view.put("triggered_block", NbtCompound.CODEC, nbtCompound.copy());

		view.putString("prevent_interaction_identifier_string", this.preventInteractionIdentifierString);

		view.putString("interaction_prevented_message", this.interactionPreventedMessage);

		view.putString("interaction_prevented_sound", this.interactionPreventedSound);

		view.putString("key_identifier_string", this.keyIdentifierString);

		view.putString("locked_message", this.lockedMessage);

		view.putString("locked_sound", this.lockedSound);

		view.putString("unlocked_message", this.unlockedMessage);

		view.putString("unlocked_sound", this.unlockedSound);

		super.writeData(view);
	}

	@Override
	protected void readData(ReadView view) {

		NbtCompound nbtCompound = view.read("triggered_block", NbtCompound.CODEC).orElseGet(NbtCompound::new);
		this.triggeredBlock = new MutablePair<>(
				BlockPos.max(
						BlockPos.min(
								nbtCompound.get("position_offset", BlockPos.CODEC).orElse(BlockPos.ORIGIN),
								new BlockPos(48, 48, 48)
						),
						new BlockPos(-48, -48, -48)
				),
				nbtCompound.getBoolean("resets", false)
		);

		this.preventInteractionIdentifierString = view.getString("prevent_interaction_identifier_string", "");

		this.interactionPreventedMessage = view.getString("interaction_prevented_message", "");

		this.interactionPreventedSound = view.getString("interaction_prevented_sound", "");

		this.keyIdentifierString = view.getString("key_identifier_string", "");

		this.lockedMessage = view.getString("locked_message", "");

		this.lockedSound = view.getString("locked_sound", "");

		this.unlockedMessage = view.getString("unlocked_message", "");

		this.unlockedSound = view.getString("unlocked_sound", "");

		super.readData(view);
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

	public boolean isInteractionPrevented(PlayerEntity playerEntity) {
		return this.preventInteractionIdentifierString.isEmpty() || PlayerEntityHelper.allowInteractions(playerEntity, this.preventInteractionIdentifierString);
	}

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

	public void triggeredOnInteraction() {
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
			if (state.get(ScriptBlocksBlockProperties.ROTATED) != this.rotated) {
				BlockRotation blockRotation = BlockRotationUtils.calculateRotationFromDifferentRotatedStates(state.get(ScriptBlocksBlockProperties.ROTATED), this.rotated);
				this.triggeredBlock.setLeft(BlockRotationUtils.rotateOffsetBlockPos(this.triggeredBlock.getLeft(), blockRotation));
				this.rotated = state.get(ScriptBlocksBlockProperties.ROTATED);
			}
			if (state.get(ScriptBlocksBlockProperties.X_MIRRORED) != this.x_mirrored) {
				this.triggeredBlock.setLeft(BlockRotationUtils.mirrorOffsetBlockPos(this.triggeredBlock.getLeft(), BlockMirror.FRONT_BACK));
				this.x_mirrored = state.get(ScriptBlocksBlockProperties.X_MIRRORED);
			}
			if (state.get(ScriptBlocksBlockProperties.Z_MIRRORED) != this.z_mirrored) {
				this.triggeredBlock.setLeft(BlockRotationUtils.mirrorOffsetBlockPos(this.triggeredBlock.getLeft(), BlockMirror.LEFT_RIGHT));
				this.z_mirrored = state.get(ScriptBlocksBlockProperties.Z_MIRRORED);
			}
		}
	}

	@Override
	public void trigger() {
		if (this.world != null) {
			BlockState state = this.world.getBlockState(this.pos);
			if (state.getBlock() instanceof InteractiveTriggerBlock interactiveTriggerBlock) {
				if (!this.getUnlockedSound().isEmpty() && !this.world.isClient()) {
					SoundEvent soundEvent = Registries.SOUND_EVENT.get(Identifier.of(this.getUnlockedSound()));
					if (soundEvent != null) {
						this.world.playSound(null, (double) this.pos.getX() + 0.5, (double) this.pos.getY() + 0.5, (double) this.pos.getZ() + 0.5, soundEvent, SoundCategory.BLOCKS, 0.5F, this.world.random.nextFloat() * 0.1F + 0.9F);
					}
				}
				interactiveTriggerBlock.trigger(state, this.world, this.pos);
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
