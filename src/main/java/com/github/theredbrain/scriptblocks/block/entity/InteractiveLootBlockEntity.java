package com.github.theredbrain.scriptblocks.block.entity;

import com.github.theredbrain.scriptblocks.ScriptBlocks;
import com.github.theredbrain.scriptblocks.block.Resetable;
import com.github.theredbrain.scriptblocks.registry.ScriptBlocksEntities;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

@Deprecated
public class InteractiveLootBlockEntity extends BlockEntity implements Resetable {
	private final Set<UUID> playerSet = new HashSet<>();
	private String lootTableIdentifierString = "";
	private Mode mode = Mode.VANILLA;
	private int rolls = 3;
	private int choices = 1;
	private boolean trackPlayers = false;
	private String lootAcquiredMessage = "gui.interactive_loot_block.loot_acquired";
	private String lootAcquiredSoundId = "";
	private String alreadyLootedMessage = "gui.interactive_loot_block.already_looted";
	private String alreadyLootedSoundId = "block.chest.locked";

	public InteractiveLootBlockEntity(BlockPos pos, BlockState state) {
		super(ScriptBlocksEntities.INTERACTIVE_LOOT_BLOCK_ENTITY, pos, state);
	}

	@Override
	protected void saveAdditional(ValueOutput view) {

		ValueOutput.TypedOutputList<UUID> listAppender = view.list("uuidList", UUIDUtil.AUTHLIB_CODEC);
		for (UUID uuid : this.playerSet) {
			listAppender.add(uuid);
		}

		if (!this.lootTableIdentifierString.isEmpty()) {
			view.putString("lootTableIdentifierString", this.lootTableIdentifierString);
		} else {
			view.discard("lootTableIdentifierString");
		}

		if (this.mode != Mode.VANILLA) {
			view.putString("mode", this.mode.getSerializedName());
		} else {
			view.discard("mode");
		}

		if (this.rolls != 3) {
			view.putInt("rolls", this.rolls);
		} else {
			view.discard("rolls");
		}

		if (this.choices != 1) {
			view.putInt("choices", this.choices);
		} else {
			view.discard("choices");
		}

		if (this.trackPlayers) {
			view.putBoolean("trackPlayers", true);
		} else {
			view.discard("trackPlayers");
		}

		if (!this.lootAcquiredMessage.isEmpty()) {
			view.putString("lootAcquiredMessage", this.lootAcquiredMessage);
		} else {
			view.discard("lootAcquiredMessage");
		}

		if (!this.lootAcquiredSoundId.isEmpty()) {
			view.putString("lootAcquiredSoundId", this.lootAcquiredSoundId);
		} else {
			view.discard("lootAcquiredSoundId");
		}

		if (!this.alreadyLootedMessage.isEmpty()) {
			view.putString("alreadyLootedMessage", this.alreadyLootedMessage);
		} else {
			view.discard("alreadyLootedMessage");
		}

		if (!this.alreadyLootedSoundId.isEmpty()) {
			view.putString("alreadyLootedSoundId", this.alreadyLootedSoundId);
		} else {
			view.discard("alreadyLootedSoundId");
		}

		ScriptBlocks.sendDeprecatedFeatureInfo("Deprecated Interactive Loot Block detected at: " + this.worldPosition.toString() + ". This block will be removed in the future and should be replaced with the corresponding block from the 'Lootable Blocks' mod.", this.level != null ? this.level.getServer() : null);
		super.saveAdditional(view);
	}

	@Override
	protected void loadAdditional(ValueInput view) {

		this.playerSet.clear();
		for (UUID uuid : view.listOrEmpty("uuidList", UUIDUtil.AUTHLIB_CODEC)) {
			this.playerSet.add(uuid);
		}

		this.lootTableIdentifierString = view.getStringOr("lootTableIdentifierString", "");

		this.mode = Mode.byName(view.getStringOr("mode", "")).orElse(Mode.VANILLA);

		this.rolls = view.getIntOr("rolls", 3);

		this.choices = view.getIntOr("choices", 1);

		this.trackPlayers = view.getBooleanOr("trackPlayers", false);

		this.lootAcquiredMessage = view.getStringOr("lootAcquiredMessage", "");

		this.lootAcquiredSoundId = view.getStringOr("lootAcquiredSoundId", "");

		this.alreadyLootedMessage = view.getStringOr("alreadyLootedMessage", "");

		this.alreadyLootedSoundId = view.getStringOr("alreadyLootedSoundId", "");

		super.loadAdditional(view);
	}

	public ClientboundBlockEntityDataPacket getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}

	@Override
	public CompoundTag getUpdateTag(HolderLookup.Provider registryLookup) {
		return this.saveCustomOnly(registryLookup);
	}

	public String getLootTableIdentifierString() {
		return this.lootTableIdentifierString;
	}

	public void setLootTableIdentifierString(String lootTableIdentifierString) {
		this.lootTableIdentifierString = lootTableIdentifierString;
	}

	public Mode getMode() {
		return this.mode;
	}

	public void setMode(Mode mode) {
		this.mode = mode;
	}

	public int getRolls() {
		return this.rolls;
	}

	public void setRolls(int rolls) {
		this.rolls = rolls;
	}

	public int getChoices() {
		return this.choices;
	}

	public void setChoices(int choices) {
		this.choices = choices;
	}

	public boolean getTrackPlayers() {
		return this.trackPlayers;
	}

	public void setTrackPlayers(boolean trackPlayers) {
		this.trackPlayers = trackPlayers;
	}

	public String getLootAcquiredMessage() {
		return this.lootAcquiredMessage;
	}

	public void setLootAcquiredMessage(String lootAcquiredMessage) {
		this.lootAcquiredMessage = lootAcquiredMessage;
	}

	public String getLootAcquiredSoundId() {
		return this.lootAcquiredSoundId;
	}

	public void setLootAcquiredSoundId(String lootAcquiredSoundId) {
		this.lootAcquiredSoundId = lootAcquiredSoundId;
	}

	public String getAlreadyLootedMessage() {
		return this.alreadyLootedMessage;
	}

	public void setAlreadyLootedMessage(String alreadyLootedMessage) {
		this.alreadyLootedMessage = alreadyLootedMessage;
	}

	public String getAlreadyLootedSoundId() {
		return this.alreadyLootedSoundId;
	}

	public void setAlreadyLootedSoundId(String alreadyLootedSoundId) {
		this.alreadyLootedSoundId = alreadyLootedSoundId;
	}

	public boolean isPlayerInSet(Player playerEntity) {
		return this.playerSet.contains(playerEntity.getUUID());
	}

	public void addPlayerToSet(Player playerEntity) {
		this.playerSet.add(playerEntity.getUUID());
	}

	public void removePlayerFromSet(Player playerEntity) {
		this.playerSet.remove(playerEntity.getUUID());
	}

	@Override
	public void reset() {
		this.playerSet.clear();
	}


	public static enum Mode implements StringRepresentable {
		CHOICE("choice"),
		RANDOM("random"),
		VANILLA("vanilla");

		private final String name;

		private Mode(String name) {
			this.name = name;
		}

		@Override
		public String getSerializedName() {
			return this.name;
		}

		public static Optional<Mode> byName(String name) {
			return Arrays.stream(Mode.values()).filter(mode -> mode.getSerializedName().equals(name)).findFirst();
		}

		public Component asText() {
			return Component.translatable("gui.interactive_loot_block.mode." + this.name);
		}
	}
}
