package com.github.theredbrain.scriptblocks.block.entity;

import com.github.theredbrain.scriptblocks.ScriptBlocks;
import com.github.theredbrain.scriptblocks.block.Resetable;
import com.github.theredbrain.scriptblocks.registry.ScriptBlocksEntities;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.text.Text;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.Uuids;
import net.minecraft.util.math.BlockPos;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

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
	protected void writeData(WriteView view) {

		WriteView.ListAppender<UUID> listAppender = view.getListAppender("uuidList", Uuids.CODEC);
		for (UUID uuid : this.playerSet) {
			listAppender.add(uuid);
		}

		if (!this.lootTableIdentifierString.isEmpty()) {
			view.putString("lootTableIdentifierString", this.lootTableIdentifierString);
		} else {
			view.remove("lootTableIdentifierString");
		}

		if (this.mode != Mode.VANILLA) {
			view.putString("mode", this.mode.asString());
		} else {
			view.remove("mode");
		}

		if (this.rolls != 3) {
			view.putInt("rolls", this.rolls);
		} else {
			view.remove("rolls");
		}

		if (this.choices != 1) {
			view.putInt("choices", this.choices);
		} else {
			view.remove("choices");
		}

		if (this.trackPlayers) {
			view.putBoolean("trackPlayers", true);
		} else {
			view.remove("trackPlayers");
		}

		if (!this.lootAcquiredMessage.isEmpty()) {
			view.putString("lootAcquiredMessage", this.lootAcquiredMessage);
		} else {
			view.remove("lootAcquiredMessage");
		}

		if (!this.lootAcquiredSoundId.isEmpty()) {
			view.putString("lootAcquiredSoundId", this.lootAcquiredSoundId);
		} else {
			view.remove("lootAcquiredSoundId");
		}

		if (!this.alreadyLootedMessage.isEmpty()) {
			view.putString("alreadyLootedMessage", this.alreadyLootedMessage);
		} else {
			view.remove("alreadyLootedMessage");
		}

		if (!this.alreadyLootedSoundId.isEmpty()) {
			view.putString("alreadyLootedSoundId", this.alreadyLootedSoundId);
		} else {
			view.remove("alreadyLootedSoundId");
		}

		ScriptBlocks.sendDeprecatedFeatureInfo("Deprecated Interactive Loot Block detected at: " + this.pos.toString() + ". This block will be removed in the future and should be replaced with the corresponding block from the 'Lootable Blocks' mod.", this.world != null ? this.world.getServer() : null);
		super.writeData(view);
	}

	@Override
	protected void readData(ReadView view) {

		this.playerSet.clear();
		for (UUID uuid : view.getTypedListView("uuidList", Uuids.CODEC)) {
			this.playerSet.add(uuid);
		}

		this.lootTableIdentifierString = view.getString("lootTableIdentifierString", "");

		this.mode = Mode.byName(view.getString("mode", "")).orElse(Mode.VANILLA);

		this.rolls = view.getInt("rolls", 3);

		this.choices = view.getInt("choices", 1);

		this.trackPlayers = view.getBoolean("trackPlayers", false);

		this.lootAcquiredMessage = view.getString("lootAcquiredMessage", "");

		this.lootAcquiredSoundId = view.getString("lootAcquiredSoundId", "");

		this.alreadyLootedMessage = view.getString("alreadyLootedMessage", "");

		this.alreadyLootedSoundId = view.getString("alreadyLootedSoundId", "");

		super.readData(view);
	}

	public BlockEntityUpdateS2CPacket toUpdatePacket() {
		return BlockEntityUpdateS2CPacket.create(this);
	}

	@Override
	public NbtCompound toInitialChunkDataNbt(RegistryWrapper.WrapperLookup registryLookup) {
		return this.createComponentlessNbt(registryLookup);
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

	public boolean isPlayerInSet(PlayerEntity playerEntity) {
		return this.playerSet.contains(playerEntity.getUuid());
	}

	public void addPlayerToSet(PlayerEntity playerEntity) {
		this.playerSet.add(playerEntity.getUuid());
	}

	public void removePlayerFromSet(PlayerEntity playerEntity) {
		this.playerSet.remove(playerEntity.getUuid());
	}

	@Override
	public void reset() {
		this.playerSet.clear();
	}


	public static enum Mode implements StringIdentifiable {
		CHOICE("choice"),
		RANDOM("random"),
		VANILLA("vanilla");

		private final String name;

		private Mode(String name) {
			this.name = name;
		}

		@Override
		public String asString() {
			return this.name;
		}

		public static Optional<Mode> byName(String name) {
			return Arrays.stream(InteractiveLootBlockEntity.Mode.values()).filter(mode -> mode.asString().equals(name)).findFirst();
		}

		public Text asText() {
			return Text.translatable("gui.interactive_loot_block.mode." + this.name);
		}
	}
}
