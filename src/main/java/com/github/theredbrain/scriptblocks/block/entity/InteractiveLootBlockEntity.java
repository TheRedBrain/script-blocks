package com.github.theredbrain.scriptblocks.block.entity;

import com.github.theredbrain.scriptblocks.block.Resetable;
import com.github.theredbrain.scriptblocks.registry.EntityRegistry;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.text.Text;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.math.BlockPos;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public class InteractiveLootBlockEntity extends BlockEntity implements Resetable {
	private Set<UUID> playerSet = new HashSet<>();
	private String lootTableIdentifierString = "";
	private Mode mode = Mode.VANILLA;
	private int rolls = 3;
	private int choices = 1;

	public InteractiveLootBlockEntity(BlockPos pos, BlockState state) {
		super(EntityRegistry.INTERACTIVE_LOOT_BLOCK_ENTITY, pos, state);
	}

	@Override
	protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {

		List<UUID> list = this.playerSet.stream().toList();
		int listSize = list.size();
		nbt.putInt("listSize", listSize);
		for (int i = 0; i < listSize; i++) {
			nbt.putUuid("listEntry_" + i, list.get(i));
		}

		if (!this.lootTableIdentifierString.isEmpty()) {
			nbt.putString("lootTableIdentifierString", this.lootTableIdentifierString);
		} else {
			nbt.remove("lootTableIdentifierString");
		}

		if (this.mode != Mode.VANILLA) {
			nbt.putString("mode", this.mode.asString());
		} else {
			nbt.remove("mode");
		}

		if (this.rolls != 3) {
			nbt.putInt("rolls", this.rolls);
		} else {
			nbt.remove("rolls");
		}

		if (this.choices != 1) {
			nbt.putInt("choices", this.choices);
		} else {
			nbt.remove("choices");
		}

		super.writeNbt(nbt, registryLookup);
	}

	@Override
	protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {

		this.playerSet.clear();
		int listSize = nbt.getInt("listSize");
		for (int i = 0; i < listSize; i++) {
			if (nbt.containsUuid("listEntry_" + i)) {
				this.playerSet.add(nbt.getUuid("listEntry_" + i));
			}
		}

		if (nbt.contains("lootTableIdentifierString")) {
			this.lootTableIdentifierString = nbt.getString("lootTableIdentifierString");
		} else {
			this.lootTableIdentifierString = "";
		}

		this.mode = Mode.byName(nbt.getString("mode")).orElse(Mode.VANILLA);

		if (nbt.contains("rolls")) {
			this.rolls = nbt.getInt("rolls");
		} else {
			this.rolls = 3;
		}

		if (nbt.contains("choices")) {
			this.choices = nbt.getInt("choices");
		} else {
			this.choices = 1;
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
