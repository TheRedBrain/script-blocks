package com.github.theredbrain.scriptblocks.block.entity;

import com.github.theredbrain.scriptblocks.block.Resetable;
import com.github.theredbrain.scriptblocks.block.RotatedBlockWithEntity;
import com.github.theredbrain.scriptblocks.block.Triggerable;
import com.github.theredbrain.scriptblocks.data.PVPArenaSettings;
import com.github.theredbrain.scriptblocks.registry.CustomDynamicRegistries;
import com.github.theredbrain.scriptblocks.registry.EntityRegistry;
import com.github.theredbrain.scriptblocks.util.BlockRotationUtils;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3i;
import org.apache.commons.lang3.tuple.MutablePair;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class PVPControllerBlockEntity extends RotatedBlockEntity implements Resetable, Triggerable {

	private String pvpArenaSettingsIdentifier = "";
	private HashMap<String, MutablePair<BlockPos, MutablePair<Double, Double>>> respawnPositions = new HashMap<>(Map.of());

	public PVPControllerBlockEntity(BlockPos pos, BlockState state) {
		super(EntityRegistry.PVP_CONTROLLER_BLOCK_ENTITY, pos, state);
	}

	@Override
	protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {

		if (!this.pvpArenaSettingsIdentifier.isEmpty()) {
			nbt.putString("pvpArenaSettingsIdentifier", this.pvpArenaSettingsIdentifier);
		} else {
			nbt.remove("pvpArenaSettingsIdentifier");
		}

		List<String> keyList = this.respawnPositions.keySet().stream().toList();
		int respawnPositionsSize = this.respawnPositions.keySet().size();
		nbt.putInt("respawnPositionsSize", respawnPositionsSize);
		for (int i = 0; i < respawnPositionsSize; i++) {
			String key = keyList.get(i);
			nbt.putString("key_" + i, key);
			nbt.putInt("respawnPosition_" + i + "_X", this.respawnPositions.get(key).getLeft().getX());
			nbt.putInt("respawnPosition_" + i + "_Y", this.respawnPositions.get(key).getLeft().getY());
			nbt.putInt("respawnPosition_" + i + "_Z", this.respawnPositions.get(key).getLeft().getZ());
			nbt.putDouble("respawnPosition_" + i + "_Yaw", this.respawnPositions.get(key).getRight().getLeft());
			nbt.putDouble("respawnPosition_" + i + "_Pitch", this.respawnPositions.get(key).getRight().getRight());
		}

		super.writeNbt(nbt, registryLookup);
	}

	@Override
	protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {

		if (nbt.contains("pvpArenaSettingsIdentifier")) {
			this.pvpArenaSettingsIdentifier = nbt.getString("pvpArenaSettingsIdentifier");
		} else {
			this.pvpArenaSettingsIdentifier = "";
		}

		int respawnPositionsSize = nbt.getInt("respawnPositionsSize");
		this.respawnPositions = new HashMap<>(Map.of());
		for (int i = 0; i < respawnPositionsSize; i++) {
			String key = nbt.getString("key_" + i);
			int respawnPositionX = nbt.getInt("respawnPosition_" + i + "_X");
			int respawnPositionY = nbt.getInt("respawnPosition_" + i + "_Y");
			int respawnPositionZ = nbt.getInt("respawnPosition_" + i + "_Z");
			double respawnPositionYaw = nbt.getDouble("respawnPosition_" + i + "_Yaw");
			double respawnPositionPitch = nbt.getDouble("respawnPosition_" + i + "_Pitch");
			this.respawnPositions.put(key, new MutablePair<>(new BlockPos(respawnPositionX, respawnPositionY, respawnPositionZ), new MutablePair<>(respawnPositionYaw, respawnPositionPitch)));
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

	public String getPVPArenaSettingsIdentifier() {
		return pvpArenaSettingsIdentifier;
	}

	public boolean setPVPArenaSettingsIdentifier(String newPVPArenaSettingsIdentifier) {
		PVPArenaSettings pvpArenaSettings = null;
		Identifier identifier = Identifier.tryParse(newPVPArenaSettingsIdentifier);
		if (identifier != null && this.world != null) {
			Optional<RegistryEntry.Reference<PVPArenaSettings>> optionalPVPArenaSettingsReference = this.world.getRegistryManager().get(CustomDynamicRegistries.PVP_ARENA_SETTINGS_REGISTRY_KEY).getEntry(identifier);
			if (optionalPVPArenaSettingsReference.isPresent()) {
				pvpArenaSettings = optionalPVPArenaSettingsReference.get().value();
			}
		}
		if (newPVPArenaSettingsIdentifier.isEmpty() || pvpArenaSettings != null) {
			this.pvpArenaSettingsIdentifier = newPVPArenaSettingsIdentifier;
			return true;
		}
		return false;
	}

	@Nullable
	public MutablePair<BlockPos, MutablePair<Double, Double>> getTeamRespawnPosition(String teamId, boolean endOfBattle) {
		PVPArenaSettings pvpArenaSettings = null;
		Identifier identifier = Identifier.tryParse(this.pvpArenaSettingsIdentifier);
		if (identifier != null && this.world != null) {
			Optional<RegistryEntry.Reference<PVPArenaSettings>> optionalPVPArenaSettingsReference = this.world.getRegistryManager().get(CustomDynamicRegistries.PVP_ARENA_SETTINGS_REGISTRY_KEY).getEntry(identifier);
			if (optionalPVPArenaSettingsReference.isPresent()) {
				pvpArenaSettings = optionalPVPArenaSettingsReference.get().value();
			}
		}
		if (pvpArenaSettings != null) {
			PVPArenaSettings.TeamSettings teamSettings = pvpArenaSettings.team_settings().get(teamId);
			if (teamSettings != null) {
				return this.respawnPositions.get(endOfBattle ? teamSettings.end_of_battle_respawn_pos() : teamSettings.battle_respawn_pos());
			}
			if (!pvpArenaSettings.default_respawn_pos().isEmpty()) {
				return this.respawnPositions.get(pvpArenaSettings.default_respawn_pos());
			}
		}
		return null;
	}

	public HashMap<String, MutablePair<BlockPos, MutablePair<Double, Double>>> getRespawnPositions() {
		return respawnPositions;
	}

	public void setRespawnPositions(HashMap<String, MutablePair<BlockPos, MutablePair<Double, Double>>> respawnPositions) {
		this.respawnPositions.clear();
		this.respawnPositions.putAll(respawnPositions);
	}

	public void trigger() {
		// TODO show scoreboard of teams in pvpArenaSettingsIdentifier with pvp deaths, kills, ctf points
	}

	@Override
	public void reset() {
		// TODO no longer show scoreboard
	}

	@Override
	protected void onRotate(BlockState state) {
		if (state.getBlock() instanceof RotatedBlockWithEntity) {
			if (state.get(RotatedBlockWithEntity.ROTATED) != this.rotated) {
				BlockRotation blockRotation = BlockRotationUtils.calculateRotationFromDifferentRotatedStates(state.get(RotatedBlockWithEntity.ROTATED), this.rotated);

				List<String> keyList = this.respawnPositions.keySet().stream().toList();
				int sideEntrancesSize = this.respawnPositions.keySet().size();
				for (int i = 0; i < sideEntrancesSize; i++) {
					String key = keyList.get(i);
					MutablePair<BlockPos, MutablePair<Double, Double>> rotatedEntrance = BlockRotationUtils.rotateEntrance(this.respawnPositions.get(key), blockRotation);
					this.respawnPositions.put(key, rotatedEntrance);
				}

				this.rotated = state.get(RotatedBlockWithEntity.ROTATED);
			}
			if (state.get(RotatedBlockWithEntity.X_MIRRORED) != this.x_mirrored) {

				List<String> keyList = this.respawnPositions.keySet().stream().toList();
				int sideEntrancesSize = this.respawnPositions.keySet().size();
				for (int i = 0; i < sideEntrancesSize; i++) {
					String key = keyList.get(i);
					MutablePair<BlockPos, MutablePair<Double, Double>> mirroredEntrance = BlockRotationUtils.mirrorEntrance(this.respawnPositions.get(key), BlockMirror.FRONT_BACK);
					this.respawnPositions.put(key, mirroredEntrance);
				}

				this.x_mirrored = state.get(RotatedBlockWithEntity.X_MIRRORED);
			}
			if (state.get(RotatedBlockWithEntity.Z_MIRRORED) != this.z_mirrored) {

				List<String> keyList = this.respawnPositions.keySet().stream().toList();
				int sideEntrancesSize = this.respawnPositions.keySet().size();
				for (int i = 0; i < sideEntrancesSize; i++) {
					String key = keyList.get(i);
					MutablePair<BlockPos, MutablePair<Double, Double>> mirroredEntrance = BlockRotationUtils.mirrorEntrance(this.respawnPositions.get(key), BlockMirror.LEFT_RIGHT);
					this.respawnPositions.put(key, mirroredEntrance);
				}

				this.z_mirrored = state.get(RotatedBlockWithEntity.Z_MIRRORED);
			}
		}
	}
}
