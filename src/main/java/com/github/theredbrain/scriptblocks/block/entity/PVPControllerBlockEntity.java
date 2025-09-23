package com.github.theredbrain.scriptblocks.block.entity;

import com.github.theredbrain.scriptblocks.ScriptBlocks;
import com.github.theredbrain.scriptblocks.block.ProvidesData;
import com.github.theredbrain.scriptblocks.block.Resetable;
import com.github.theredbrain.scriptblocks.block.RotatedBlockWithEntity;
import com.github.theredbrain.scriptblocks.block.Triggerable;
import com.github.theredbrain.scriptblocks.data.PVPArenaSettings;
import com.github.theredbrain.scriptblocks.registry.CustomDynamicRegistries;
import com.github.theredbrain.scriptblocks.registry.DamageTypesRegistry;
import com.github.theredbrain.scriptblocks.registry.EntityRegistry;
import com.github.theredbrain.scriptblocks.util.BlockRotationUtils;
import com.github.theredbrain.scriptblocks.util.ItemUtils;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.MutablePair;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public class PVPControllerBlockEntity extends RotatedBlockEntity implements Resetable, Triggerable {

	private String pvpArenaSettingsIdentifier = "";
	private HashMap<String, MutablePair<BlockPos, MutablePair<Double, Double>>> respawnPositions = new HashMap<>(Map.of());
	private MutablePair<BlockPos, Boolean> triggeredBlock = new MutablePair<>(BlockPos.ORIGIN, false);
	private BlockPos dataProvidingBlockPosOffset = BlockPos.ORIGIN;

	private Set<String> teamSet = new HashSet<>();
	private Set<UUID> playerUUIDSet = new HashSet<>();

	private boolean matchIsActive = false;
	private String matchDurationDataIdentifier = "";
	private int matchDuration = 0;
	private int matchTicker = 0;

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

		nbt.putInt("triggeredBlockPositionOffsetX", this.triggeredBlock.getLeft().getX());
		nbt.putInt("triggeredBlockPositionOffsetY", this.triggeredBlock.getLeft().getY());
		nbt.putInt("triggeredBlockPositionOffsetZ", this.triggeredBlock.getLeft().getZ());
		nbt.putBoolean("triggeredBlockResets", this.triggeredBlock.getRight());

		nbt.putInt("dataProvidingBlockPosOffsetX", this.dataProvidingBlockPosOffset.getX());
		nbt.putInt("dataProvidingBlockPosOffsetY", this.dataProvidingBlockPosOffset.getY());
		nbt.putInt("dataProvidingBlockPosOffsetZ", this.dataProvidingBlockPosOffset.getZ());

		if (!this.matchDurationDataIdentifier.isEmpty()) {
			nbt.putString("matchDurationDataIdentifier", this.matchDurationDataIdentifier);
		} else {
			nbt.remove("matchDurationDataIdentifier");
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

		int x = MathHelper.clamp(nbt.getInt("triggeredBlockPositionOffsetX"), -48, 48);
		int y = MathHelper.clamp(nbt.getInt("triggeredBlockPositionOffsetY"), -48, 48);
		int z = MathHelper.clamp(nbt.getInt("triggeredBlockPositionOffsetZ"), -48, 48);
		this.triggeredBlock = new MutablePair<>(new BlockPos(x, y, z), nbt.getBoolean("triggeredBlockResets"));

		this.dataProvidingBlockPosOffset = new BlockPos(
				MathHelper.clamp(nbt.getInt("dataProvidingBlockPosOffsetX"), -48, 48),
				MathHelper.clamp(nbt.getInt("dataProvidingBlockPosOffsetY"), -48, 48),
				MathHelper.clamp(nbt.getInt("dataProvidingBlockPosOffsetZ"), -48, 48)
		);

		if (nbt.contains("matchDurationDataIdentifier")) {
			this.matchDurationDataIdentifier = nbt.getString("matchDurationDataIdentifier");
		} else {
			this.matchDurationDataIdentifier = "";
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
		return this.pvpArenaSettingsIdentifier;
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

	public HashMap<String, MutablePair<BlockPos, MutablePair<Double, Double>>> getRespawnPositions() {
		return respawnPositions;
	}

	public void setRespawnPositions(HashMap<String, MutablePair<BlockPos, MutablePair<Double, Double>>> respawnPositions) {
		this.respawnPositions.clear();
		this.respawnPositions.putAll(respawnPositions);
	}

	public MutablePair<BlockPos, Boolean> getTriggeredBlock() {
		return this.triggeredBlock;
	}

	public void setTriggeredBlock(MutablePair<BlockPos, Boolean> triggeredBlock) {
		this.triggeredBlock = triggeredBlock;
	}

	public BlockPos getDataProvidingBlockPosOffset() {
		return this.dataProvidingBlockPosOffset;
	}

	public void setDataProvidingBlockPosOffset(BlockPos firstDataProvidingBlockPosOffset) {
		this.dataProvidingBlockPosOffset = firstDataProvidingBlockPosOffset;
	}

	public String getMatchDurationDataIdentifier() {
		return this.matchDurationDataIdentifier;
	}

	public void setMatchDurationDataIdentifier(String matchDurationDataIdentifier) {
		this.matchDurationDataIdentifier = matchDurationDataIdentifier;
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

	public void addPlayerAndTeam(Team team, PlayerEntity playerEntity) {
		this.teamSet.add(team.getName());
		this.playerUUIDSet.add(playerEntity.getUuid());
	}

	public void removePlayer(PlayerEntity playerEntity) {
		this.playerUUIDSet.remove(playerEntity.getUuid());
	}

	public void initMatchDuration() {
		if (this.world != null && !this.world.isClient()) {
			if (this.dataProvidingBlockPosOffset != BlockPos.ORIGIN) {
				BlockPos firstDataProviderBlockPos = new BlockPos(this.pos.getX() + this.dataProvidingBlockPosOffset.getX(), this.pos.getY() + this.dataProvidingBlockPosOffset.getY(), this.pos.getZ() + this.dataProvidingBlockPosOffset.getZ());

				if (world.getBlockEntity(firstDataProviderBlockPos) instanceof ProvidesData providesDataEntity) {
					this.matchDuration = ItemUtils.parseInt(providesDataEntity.getData(this.matchDurationDataIdentifier));
					return;
				}
			}
		}
		this.matchDuration = 0;
	}

	public void startMatch() {
		this.initMatchDuration();
		this.matchIsActive = true;
	}

	public void endMatch(boolean forceEnd) {
		if (this.world != null) {
			if (!forceEnd) {
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

			Registry<DamageType> registry = world.getRegistryManager().get(RegistryKeys.DAMAGE_TYPE);

			DamageSource damageSource = new DamageSource(registry.entryOf(DamageTypesRegistry.PVP_WINNING_DAMAGE_TYPE));

			Iterator<UUID> iterator = this.playerUUIDSet.stream().iterator();
			while (iterator.hasNext()) {
				PlayerEntity playerEntity = world.getPlayerByUuid(iterator.next());

				if (playerEntity != null) {

					playerEntity.damage(damageSource, 2048.0F);
					if (!forceEnd && this.world instanceof ServerWorld serverWorld) {
						serverWorld.getServer().getPlayerManager().broadcast(Text.translatable("hud.message.pvp_controller_block.player_won_match", playerEntity.getDisplayName()), false);
					}
				}
			}
		}
		this.matchTicker = 0;
		this.matchIsActive = false;
	}

	public static void tick(World world, BlockPos pos, BlockState state, PVPControllerBlockEntity pvpControllerBlockEntity) {
		if (!world.isClient && pvpControllerBlockEntity.matchIsActive) {
			pvpControllerBlockEntity.matchTicker++;

			if (pvpControllerBlockEntity.matchTicker > pvpControllerBlockEntity.matchDuration) {
				pvpControllerBlockEntity.endMatch(false);
			}

			if (pvpControllerBlockEntity.matchTicker % 20 == 0) {
				Iterator<String> iterator = pvpControllerBlockEntity.teamSet.stream().iterator();
				Scoreboard scoreboard = world.getScoreboard();
				// new list
				Team team;
				int activeTeamCounter = 0;
				while (iterator.hasNext()) {
					team = scoreboard.getTeam(iterator.next());

					if (team != null) {
						if (team.getPlayerList().isEmpty()) {
							pvpControllerBlockEntity.teamSet.remove(team.getName());
						} else {
							activeTeamCounter++;
						}
					}
				}
				if (activeTeamCounter <= 1) {
					pvpControllerBlockEntity.endMatch(false);
				}

				// TODO show scoreboard of teams in pvpArenaSettingsIdentifier with pvp deaths, kills, ctf points
//			PVPArenaSettings pvpArenaSettings = null;
//			Identifier identifier = Identifier.tryParse(this.pvpArenaSettingsIdentifier);
//			if (identifier != null) {
//				Optional<RegistryEntry.Reference<PVPArenaSettings>> optionalPVPArenaSettingsReference = this.world.getRegistryManager().get(CustomDynamicRegistries.PVP_ARENA_SETTINGS_REGISTRY_KEY).getEntry(identifier);
//				if (optionalPVPArenaSettingsReference.isPresent()) {
//					pvpArenaSettings = optionalPVPArenaSettingsReference.get().value();
//				}
//			}
//			if (pvpArenaSettings != null) {
//				Scoreboard scoreboard = this.world.getScoreboard();
//				for (Map.Entry<String, PVPArenaSettings.TeamSettings> entry : pvpArenaSettings.team_settings().entrySet()) {
//					scoreboard.
//				}
//			}
			}
		}
	}

	@Override
	public void trigger() {
		this.startMatch();
//		if (this.world != null) {
//
//		}
	}

	@Override
	public void reset() {
		// TODO no longer show scoreboard
		this.endMatch(true);
		this.teamSet.clear();
		this.playerUUIDSet.clear();
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

				this.triggeredBlock.setLeft(BlockRotationUtils.rotateOffsetBlockPos(this.triggeredBlock.getLeft(), blockRotation));

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

				this.triggeredBlock.setLeft(BlockRotationUtils.mirrorOffsetBlockPos(this.triggeredBlock.getLeft(), BlockMirror.FRONT_BACK));

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

				this.triggeredBlock.setLeft(BlockRotationUtils.mirrorOffsetBlockPos(this.triggeredBlock.getLeft(), BlockMirror.LEFT_RIGHT));

				this.z_mirrored = state.get(RotatedBlockWithEntity.Z_MIRRORED);
			}
		}
	}
}
