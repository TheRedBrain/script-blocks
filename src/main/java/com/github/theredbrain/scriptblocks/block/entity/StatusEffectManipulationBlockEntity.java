package com.github.theredbrain.scriptblocks.block.entity;

import com.github.theredbrain.scriptblocks.block.HandlesUUIDList;
import com.github.theredbrain.scriptblocks.block.Triggerable;
import com.github.theredbrain.scriptblocks.registry.ScriptBlocksEntities;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class StatusEffectManipulationBlockEntity extends BlockEntity implements HandlesUUIDList, Triggerable {

	private ManipulationMode manipulationMode = ManipulationMode.APPLY;
	private String effectIdentifier = "";
	private int effectAmplifier = 0;
	private int effectDuration = 100;
	private boolean effectIsAmbient = false;
	private boolean effectShowsParticles = false;
	private boolean effectShowsIcon = false;
	private String manipulatedEffectTagIdentifierString = "";
	private int amplifierModification = 0;
	private BlockPos uuidListHandlerPositionOffset = new BlockPos(0, 1, 0);

	public StatusEffectManipulationBlockEntity(BlockPos pos, BlockState state) {
		super(ScriptBlocksEntities.STATUS_EFFECT_MANIPULATION_BLOCK_ENTITY, pos, state);
	}

	@Override
	protected void saveAdditional(ValueOutput view) {

		view.putString("manipulation_mode", this.manipulationMode.getSerializedName());

		CompoundTag nbtCompound = new CompoundTag();
		nbtCompound.putString("identifier", this.effectIdentifier);
		nbtCompound.putInt("amplifier", this.effectAmplifier);
		nbtCompound.putInt("duration", this.effectDuration);
		nbtCompound.putBoolean("is_ambient", this.effectIsAmbient);
		nbtCompound.putBoolean("show_particles", this.effectShowsParticles);
		nbtCompound.putBoolean("show_icon", this.effectShowsIcon);
		view.store("applied_status_effect", CompoundTag.CODEC, nbtCompound.copy());

		view.putString("manipulated_effect_tag_id", this.manipulatedEffectTagIdentifierString);

		view.putInt("amplifier_modification", this.amplifierModification);

		view.store("uuid_list_handler_position_offset", BlockPos.CODEC, this.uuidListHandlerPositionOffset);

		super.saveAdditional(view);
	}

	@Override
	protected void loadAdditional(ValueInput view) {

		this.manipulationMode = ManipulationMode.byName(view.getStringOr("manipulation_mode", "")).orElse(ManipulationMode.APPLY);

		CompoundTag nbtCompound = view.read("applied_status_effect", CompoundTag.CODEC).orElseGet(CompoundTag::new);
		this.effectIdentifier = nbtCompound.getStringOr("identifier", "");

		this.effectAmplifier = nbtCompound.getIntOr("amplifier", 0);

		this.effectDuration = nbtCompound.getIntOr("duration", 100);

		this.effectIsAmbient = nbtCompound.getBooleanOr("is_ambient", false);

		this.effectShowsParticles = nbtCompound.getBooleanOr("show_particles", false);

		this.effectShowsIcon = nbtCompound.getBooleanOr("show_icon", false);

		this.manipulatedEffectTagIdentifierString = view.getStringOr("manipulated_effect_tag_id", "");

		this.amplifierModification = view.getIntOr("amplifier_modification", 0);

		this.uuidListHandlerPositionOffset = BlockPos.max(
				BlockPos.min(
						view.read("uuid_list_handler_position_offset", BlockPos.CODEC).orElse(BlockPos.ZERO),
						new BlockPos(48, 48, 48)
				),
				new BlockPos(-48, -48, -48)
		);

		super.loadAdditional(view);
	}

	@Override
	public ClientboundBlockEntityDataPacket getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}

	@Override
	public CompoundTag getUpdateTag(HolderLookup.Provider registryLookup) {
		return this.saveCustomOnly(registryLookup);
	}

	// region --- getter & setter ---

	public ManipulationMode getManipulationMode() {
		return this.manipulationMode;
	}

	public void setManipulationMode(ManipulationMode manipulationMode) {
		this.manipulationMode = manipulationMode;
	}

	public String getEffectIdentifier() {
		return this.effectIdentifier;
	}

	public boolean setEffectIdentifier(String effectIdentifier) {
		if (BuiltInRegistries.MOB_EFFECT.getValue(Identifier.tryParse(effectIdentifier)) != null || effectIdentifier.isEmpty()) {
			this.effectIdentifier = effectIdentifier;
			return true;
		}
		return false;
	}

	public int getEffectAmplifier() {
		return this.effectAmplifier;
	}

	public boolean setEffectAmplifier(int effectAmplifier) {
		if (effectAmplifier >= 0 && effectAmplifier < 127) {
			this.effectAmplifier = effectAmplifier;
			return true;
		}
		return false;
	}

	public int getEffectDuration() {
		return this.effectDuration;
	}

	public void setEffectDuration(int effectDuration) {
		if (effectDuration < -1) {
			effectDuration = 100;
		}
		this.effectDuration = effectDuration;
	}

	public boolean getEffectIsAmbient() {
		return this.effectIsAmbient;
	}

	public void setEffectIsAmbient(boolean effectIsAmbient) {
		this.effectIsAmbient = effectIsAmbient;
	}

	public boolean getEffectShowsParticles() {
		return this.effectShowsParticles;
	}

	public void setEffectShowsParticles(boolean effectShowsParticles) {
		this.effectShowsParticles = effectShowsParticles;
	}

	public boolean getEffectShowsIcon() {
		return this.effectShowsIcon;
	}

	public void setEffectShowsIcon(boolean effectShowsIcon) {
		this.effectShowsIcon = effectShowsIcon;
	}

	public String getManipulatedEffectTagIdentifierString() {
		return this.manipulatedEffectTagIdentifierString;
	}

	public void setManipulatedEffectTagIdentifierString(String manipulatedEffectTagIdentifierString) {
		this.manipulatedEffectTagIdentifierString = manipulatedEffectTagIdentifierString;
	}

	public int getAmplifierModification() {
		return this.amplifierModification;
	}

	public void setAmplifierModification(int amplifierModification) {
		this.amplifierModification = effectAmplifier;
	}

	public BlockPos getUuidListHandlerPositionOffset() {
		return this.uuidListHandlerPositionOffset;
	}

	public void setUuidListHandlerPositionOffset(BlockPos uuidListHandlerPositionOffset) {
		this.uuidListHandlerPositionOffset = uuidListHandlerPositionOffset;
	}
	// endregion --- getter & setter ---

	public BlockPos getActualUUIDListHandlerPosition() {
		return new BlockPos(this.worldPosition.getX() + this.uuidListHandlerPositionOffset.getX(), this.worldPosition.getY() + this.uuidListHandlerPositionOffset.getY(), this.worldPosition.getZ() + this.uuidListHandlerPositionOffset.getZ());
	}

	private void manipulateStatusEffects(Level world, List<UUID> list, Holder.Reference<MobEffect> statusEffectEntry) {

		Iterator<UUID> playerListIterator = list.iterator();

		Player playerEntity;
		UUID uuid;

		while (playerListIterator.hasNext()) {
			uuid = playerListIterator.next();
			playerEntity = world.getPlayerByUUID(uuid);

			if (playerEntity == null) {
				continue;
			}
			if (this.manipulationMode == ManipulationMode.APPLY) {

				playerEntity.addEffect(
						new MobEffectInstance(
								statusEffectEntry,
								this.effectDuration,
								this.effectAmplifier,
								this.effectIsAmbient,
								this.effectShowsParticles,
								this.effectShowsIcon
						)
				);

			} else {

				TagKey<MobEffect> tagKey = TagKey.create(Registries.MOB_EFFECT, Identifier.parse(this.manipulatedEffectTagIdentifierString));
				for (MobEffectInstance statusEffectInstance : playerEntity.getActiveEffects().stream().toList()) {
					Holder<MobEffect> statusEffectRegistryEntry = statusEffectInstance.getEffect();
					if (statusEffectRegistryEntry.is(tagKey)) {
						if (this.manipulationMode == ManipulationMode.REMOVE) {
							playerEntity.removeEffect(statusEffectRegistryEntry);
						} else if (this.manipulationMode == ManipulationMode.DECREMENT_AMPLIFIER) {
							int oldAmplifier = statusEffectInstance.getAmplifier();
							if (oldAmplifier <= 0) {
								playerEntity.removeEffect(statusEffectRegistryEntry);
							} else {
								MobEffectInstance newStatusEffectInstance = new MobEffectInstance(statusEffectRegistryEntry, statusEffectInstance.getDuration(), statusEffectInstance.getAmplifier() - 1, statusEffectInstance.isAmbient(), statusEffectInstance.isVisible(), statusEffectInstance.showIcon());
								playerEntity.removeEffect(statusEffectRegistryEntry);
								playerEntity.addEffect(newStatusEffectInstance);
							}
						}
					}
				}
			}
		}
	}

	@Override
	public List<UUID> supplyUUIDList(boolean remove) {
		return new ArrayList<>();
	}

	@Override
	public void handleUUIDList(List<UUID> list, boolean remove) {

		Optional<Holder.Reference<MobEffect>> statusEffect = BuiltInRegistries.MOB_EFFECT.get(Identifier.tryParse(this.effectIdentifier));
		if (statusEffect.isEmpty()) {
			return;
		}
		if (this.getLevel() == null) {
			return;
		}

		this.manipulateStatusEffects(this.getLevel(), list, statusEffect.get());
	}

	@Override
	public void reset() {
	}

	@Override
	public void trigger() {
		Optional<Holder.Reference<MobEffect>> statusEffect = BuiltInRegistries.MOB_EFFECT.get(Identifier.tryParse(this.effectIdentifier));
		if (statusEffect.isEmpty()) {
			return;
		}
		if (this.getLevel() == null) {
			return;
		}
		BlockEntity blockEntity = this.getLevel().getBlockEntity(this.getActualUUIDListHandlerPosition());
		if (blockEntity == this) {
			return;
		}

		if (blockEntity instanceof HandlesUUIDList handlesUUIDList) {
			this.manipulateStatusEffects(this.getLevel(), handlesUUIDList.supplyUUIDList(false), statusEffect.get());
		}
	}

	public enum ManipulationMode implements StringRepresentable {
		APPLY("apply"),
		REMOVE("remove"),
		DECREMENT_AMPLIFIER("decrement_amplifier");

		private final String name;

		private ManipulationMode(String name) {
			this.name = name;
		}

		@Override
		public String getSerializedName() {
			return this.name;
		}

		public static Optional<ManipulationMode> byName(String name) {
			return Arrays.stream(ManipulationMode.values()).filter(manipulationMode -> manipulationMode.getSerializedName().equals(name)).findFirst();
		}

		public Component asText() {
			return Component.translatable("gui.status_effect_manipulation_block.mode." + this.name);
		}
	}
}
