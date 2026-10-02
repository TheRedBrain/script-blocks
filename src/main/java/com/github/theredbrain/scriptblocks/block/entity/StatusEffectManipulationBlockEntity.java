package com.github.theredbrain.scriptblocks.block.entity;

import com.github.theredbrain.scriptblocks.block.HandlesUUIDList;
import com.github.theredbrain.scriptblocks.block.Triggerable;
import com.github.theredbrain.scriptblocks.registry.ScriptBlocksEntities;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

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
	protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {

		nbt.putString("manipulation_mode", this.manipulationMode.asString());

		nbt.putString("effect_identifier", this.effectIdentifier);

		nbt.putInt("effect_amplifier", this.effectAmplifier);

		nbt.putInt("effect_duration", this.effectDuration);

		nbt.putBoolean("effect_is_ambient", true);

		nbt.putBoolean("effect_shows_particles", true);

		nbt.putBoolean("effect_shows_icon", true);

		nbt.putString("manipulated_effect_tag_id", this.manipulatedEffectTagIdentifierString);

		nbt.putInt("amplifier_modification", this.amplifierModification);

		nbt.putInt("uuid_list_handler_position_offset_x", this.uuidListHandlerPositionOffset.getX());
		nbt.putInt("uuid_list_handler_position_offset_y", this.uuidListHandlerPositionOffset.getY());
		nbt.putInt("uuid_list_handler_position_offset_z", this.uuidListHandlerPositionOffset.getZ());

		super.writeNbt(nbt, registryLookup);
	}

	@Override
	protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {

		this.manipulationMode = ManipulationMode.byName(nbt.getString("manipulation_mode")).orElse(ManipulationMode.APPLY);

		this.effectIdentifier = nbt.getString("effect_identifier");

		this.effectAmplifier = nbt.getInt("effect_amplifier");

		if (nbt.contains("effect_duration")) {
			this.effectDuration = nbt.getInt("effect_duration");
		} else {
			this.effectDuration = 100;
		}

		this.effectIsAmbient = nbt.getBoolean("effect_is_ambient");

		this.effectShowsParticles = nbt.getBoolean("effect_shows_particles");

		this.effectShowsIcon = nbt.getBoolean("effect_shows_icon");

		this.manipulatedEffectTagIdentifierString = nbt.getString("manipulated_effect_tag_id");

		this.amplifierModification = nbt.getInt("amplifier_modification");

		this.uuidListHandlerPositionOffset = new BlockPos(
				MathHelper.clamp(nbt.getInt("uuid_list_handler_position_offset_x"), -48, 48),
				MathHelper.clamp(nbt.getInt("uuid_list_handler_position_offset_y"), -48, 48),
				MathHelper.clamp(nbt.getInt("uuid_list_handler_position_offset_z"), -48, 48)
		);

		super.readNbt(nbt, registryLookup);
	}

	@Override
	public BlockEntityUpdateS2CPacket toUpdatePacket() {
		return BlockEntityUpdateS2CPacket.create(this);
	}

	@Override
	public NbtCompound toInitialChunkDataNbt(RegistryWrapper.WrapperLookup registryLookup) {
		return this.createComponentlessNbt(registryLookup);
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
		if (Registries.STATUS_EFFECT.get(Identifier.tryParse(effectIdentifier)) != null || effectIdentifier.isEmpty()) {
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
		return new BlockPos(this.pos.getX() + this.uuidListHandlerPositionOffset.getX(), this.pos.getY() + this.uuidListHandlerPositionOffset.getY(), this.pos.getZ() + this.uuidListHandlerPositionOffset.getZ());
	}

	private void manipulateStatusEffects(World world, List<UUID> list, RegistryEntry.Reference<StatusEffect> statusEffectEntry) {

		Iterator<UUID> playerListIterator = list.iterator();

		PlayerEntity playerEntity;
		UUID uuid;

		while (playerListIterator.hasNext()) {
			uuid = playerListIterator.next();
			playerEntity = world.getPlayerByUuid(uuid);

			if (playerEntity == null) {
				continue;
			}
			if (this.manipulationMode == ManipulationMode.APPLY) {

				playerEntity.addStatusEffect(
						new StatusEffectInstance(
								statusEffectEntry,
								this.effectDuration,
								this.effectAmplifier,
								this.effectIsAmbient,
								this.effectShowsParticles,
								this.effectShowsIcon
						)
				);

			} else {

				TagKey<StatusEffect> tagKey = TagKey.of(RegistryKeys.STATUS_EFFECT, Identifier.of(this.manipulatedEffectTagIdentifierString));
				for (StatusEffectInstance statusEffectInstance : playerEntity.getStatusEffects().stream().toList()) {
					RegistryEntry<StatusEffect> statusEffectRegistryEntry = statusEffectInstance.getEffectType();
					if (statusEffectRegistryEntry.isIn(tagKey)) {
						if (this.manipulationMode == ManipulationMode.REMOVE) {
							playerEntity.removeStatusEffect(statusEffectRegistryEntry);
						} else if (this.manipulationMode == ManipulationMode.DECREMENT_AMPLIFIER) {
							int oldAmplifier = statusEffectInstance.getAmplifier();
							if (oldAmplifier <= 0) {
								playerEntity.removeStatusEffect(statusEffectRegistryEntry);
							} else {
								StatusEffectInstance newStatusEffectInstance = new StatusEffectInstance(statusEffectRegistryEntry, statusEffectInstance.getDuration(), statusEffectInstance.getAmplifier() - 1, statusEffectInstance.isAmbient(), statusEffectInstance.shouldShowParticles(), statusEffectInstance.shouldShowIcon());
								playerEntity.removeStatusEffect(statusEffectRegistryEntry);
								playerEntity.addStatusEffect(newStatusEffectInstance);
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

		Optional<RegistryEntry.Reference<StatusEffect>> statusEffect = Registries.STATUS_EFFECT.getEntry(Identifier.tryParse(this.effectIdentifier));
		if (statusEffect.isEmpty()) {
			return;
		}
		if (this.getWorld() == null) {
			return;
		}

		this.manipulateStatusEffects(this.getWorld(), list, statusEffect.get());
	}

	@Override
	public void reset() {
	}

	@Override
	public void trigger() {
		Optional<RegistryEntry.Reference<StatusEffect>> statusEffect = Registries.STATUS_EFFECT.getEntry(Identifier.tryParse(this.effectIdentifier));
		if (statusEffect.isEmpty()) {
			return;
		}
		if (this.getWorld() == null) {
			return;
		}
		BlockEntity blockEntity = this.getWorld().getBlockEntity(this.getActualUUIDListHandlerPosition());
		if (blockEntity == this) {
			return;
		}

		if (blockEntity instanceof HandlesUUIDList handlesUUIDList) {
			this.manipulateStatusEffects(this.getWorld(), handlesUUIDList.supplyUUIDList(false), statusEffect.get());
		}
	}

	public enum ManipulationMode implements StringIdentifiable {
		APPLY("apply"),
		REMOVE("remove"),
		DECREMENT_AMPLIFIER("decrement_amplifier");

		private final String name;

		private ManipulationMode(String name) {
			this.name = name;
		}

		@Override
		public String asString() {
			return this.name;
		}

		public static Optional<ManipulationMode> byName(String name) {
			return Arrays.stream(ManipulationMode.values()).filter(manipulationMode -> manipulationMode.asString().equals(name)).findFirst();
		}

		public Text asText() {
			return Text.translatable("gui.status_effect_manipulation_block.mode." + this.name);
		}
	}
}
