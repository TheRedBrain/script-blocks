package com.github.theredbrain.scriptblocks.block.entity;

import com.github.theredbrain.scriptblocks.block.ProvidesData;
import com.github.theredbrain.scriptblocks.block.Resetable;
import com.github.theredbrain.scriptblocks.block.RotatedBlockWithEntity;
import com.github.theredbrain.scriptblocks.block.Triggerable;
import com.github.theredbrain.scriptblocks.data.PVPArenaSettings;
import com.github.theredbrain.scriptblocks.registry.ScriptBlocksDamageTypes;
import com.github.theredbrain.scriptblocks.registry.ScriptBlocksDynamicRegistries;
import com.github.theredbrain.scriptblocks.registry.ScriptBlocksEntities;
import com.github.theredbrain.scriptblocks.util.BlockRotationUtils;
import com.github.theredbrain.scriptblocks.util.ItemUtils;
import com.github.theredbrain.scriptblocks.util.ScriptBlocksBlockProperties;
import net.minecraft.world.level.storage.LevelData;
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
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;

public class PVPControllerBlockEntity extends RotatedBlockEntity implements Resetable, Triggerable {

	private String pvpArenaSettingsIdentifier = "";
	private final HashMap<String, LevelData.RespawnData> respawnPositions = new HashMap<>(Map.of());
	private MutablePair<BlockPos, Boolean> triggeredBlock = new MutablePair<>(BlockPos.ZERO, false);
	private BlockPos dataProvidingBlockPosOffset = BlockPos.ZERO;

	private final Set<String> teamSet = new HashSet<>();
	private final Set<UUID> playerUUIDSet = new HashSet<>();

	private boolean matchIsActive = false;
	private String matchDurationDataIdentifier = "";
	private int matchDuration = 0;
	private int matchTicker = 0;

	public PVPControllerBlockEntity(BlockPos pos, BlockState state) {
		super(ScriptBlocksEntities.PVP_CONTROLLER_BLOCK_ENTITY, pos, state);
	}

	@Override
	protected void saveAdditional(ValueOutput view) {

		view.putString("pvp_arena_settings_identifier", this.pvpArenaSettingsIdentifier);

		ValueOutput.TypedOutputList<CompoundTag> listAppender = view.list("respawn_positions", CompoundTag.CODEC);
		this.respawnPositions.forEach((key, entry) -> {
			CompoundTag nbtCompound = new CompoundTag();
			nbtCompound.putString("key", key);
			nbtCompound.store("position_offset", BlockPos.CODEC, this.respawnPositions.get(key).getLeft());
			nbtCompound.putDouble("yaw", this.respawnPositions.get(key).getRight().getLeft());
			nbtCompound.putDouble("pitch", this.respawnPositions.get(key).getRight().getRight());
			listAppender.add(nbtCompound.copy());
		});

		CompoundTag nbtCompound = new CompoundTag();
		nbtCompound.store("position_offset", BlockPos.CODEC, this.triggeredBlock.getLeft());
		nbtCompound.putBoolean("resets", this.triggeredBlock.getRight());
		view.store("triggered_block", CompoundTag.CODEC, nbtCompound.copy());

		view.store("data_providing_block_position_offset", BlockPos.CODEC, this.dataProvidingBlockPosOffset);

		view.putString("match_duration_data_identifier", this.matchDurationDataIdentifier);

		super.saveAdditional(view);
	}

	@Override
	protected void loadAdditional(ValueInput view) {

		this.pvpArenaSettingsIdentifier = view.getStringOr("pvp_arena_settings_identifier", "");

		this.respawnPositions.clear();
		for (CompoundTag nbtCompound : view.listOrEmpty("respawn_positions", CompoundTag.CODEC)) {
			this.respawnPositions.put(
					nbtCompound.getStringOr("key", ""),
					new MutablePair<>(
							BlockPos.max(
									BlockPos.min(
											nbtCompound.read("position_offset", BlockPos.CODEC).orElse(BlockPos.ZERO),
											new BlockPos(48, 48, 48)
									),
									new BlockPos(-48, -48, -48)
							),
							new MutablePair<>(
									Mth.clamp(nbtCompound.getDoubleOr("yaw", 0), -180, 180),
									Mth.clamp(nbtCompound.getDoubleOr("pitch", 0), -90, 90)
							)
					)
			);
		}

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

		this.dataProvidingBlockPosOffset = BlockPos.max(
				BlockPos.min(
						view.read("data_providing_block_pos_offset", BlockPos.CODEC).orElse(BlockPos.ZERO),
						new BlockPos(48, 48, 48)
				),
				new BlockPos(-48, -48, -48)
		);

		this.matchDurationDataIdentifier = view.getStringOr("match_duration_data_identifier", "");

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
	public String getPVPArenaSettingsIdentifier() {
		return this.pvpArenaSettingsIdentifier;
	}

	public boolean setPVPArenaSettingsIdentifier(String newPVPArenaSettingsIdentifier) {
		PVPArenaSettings pvpArenaSettings = null;
		Identifier identifier = Identifier.tryParse(newPVPArenaSettingsIdentifier);
		if (identifier != null && this.level != null) {
			Optional<Holder.Reference<PVPArenaSettings>> optionalPVPArenaSettingsReference = this.level.registryAccess().get(ScriptBlocksDynamicRegistries.PVP_ARENA_SETTINGS_REGISTRY_KEY).get().value().get(identifier);
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

	public HashMap<String, LevelData.RespawnData> getRespawnPositions() {
		return respawnPositions;
	}

	public void setRespawnPositions(HashMap<String, LevelData.RespawnData> respawnPositions) {
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
// endregion --- getter & setter ---

	@Nullable
	public LevelData.RespawnData getTeamRespawnPosition(String teamId, boolean endOfBattle) {
		PVPArenaSettings pvpArenaSettings = null;
		Identifier identifier = Identifier.tryParse(this.pvpArenaSettingsIdentifier);
		if (identifier != null && this.level != null) {
			Optional<Holder.Reference<PVPArenaSettings>> optionalPVPArenaSettingsReference = this.level.registryAccess().get(ScriptBlocksDynamicRegistries.PVP_ARENA_SETTINGS_REGISTRY_KEY).get().value().get(identifier);
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

	public void addPlayerAndTeam(PlayerTeam team, Player playerEntity) {
		this.teamSet.add(team.getName());
		this.playerUUIDSet.add(playerEntity.getUUID());
		this.setChanged();
		if (this.level != null) {
			BlockState blockState = this.level.getBlockState(this.worldPosition);
			this.level.sendBlockUpdated(this.worldPosition, blockState, blockState, Block.UPDATE_ALL);
		}
	}

	public void removePlayer(Player playerEntity) {
		this.playerUUIDSet.remove(playerEntity.getUUID());
		this.setChanged();
		if (this.level != null) {
			BlockState blockState = this.level.getBlockState(this.worldPosition);
			this.level.sendBlockUpdated(this.worldPosition, blockState, blockState, Block.UPDATE_ALL);
		}
	}

	public void startMatch() {
		this.initMatchDuration();
		this.matchIsActive = true;
	}

	private void initMatchDuration() {
		if (this.level != null && !this.level.isClientSide()) {
			if (this.dataProvidingBlockPosOffset != BlockPos.ZERO) {
				BlockPos firstDataProviderBlockPos = new BlockPos(this.worldPosition.getX() + this.dataProvidingBlockPosOffset.getX(), this.worldPosition.getY() + this.dataProvidingBlockPosOffset.getY(), this.worldPosition.getZ() + this.dataProvidingBlockPosOffset.getZ());

				if (level.getBlockEntity(firstDataProviderBlockPos) instanceof ProvidesData providesDataEntity) {
					this.matchDuration = ItemUtils.parseInt(providesDataEntity.getData(this.matchDurationDataIdentifier));
					return;
				}
			}
		}
		this.matchDuration = 0;
	}

	public void endMatch(boolean forceEnd) {
		if (this.level != null) {
			if (!forceEnd) {
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

			Registry<DamageType> registry = level.registryAccess().lookupOrThrow(Registries.DAMAGE_TYPE);

			DamageSource damageSource = new DamageSource(registry.getOrThrow(ScriptBlocksDamageTypes.PVP_WINNING_DAMAGE_TYPE));

			Iterator<UUID> iterator = this.playerUUIDSet.stream().iterator();
			while (iterator.hasNext()) {
				Player playerEntity = level.getPlayerByUUID(iterator.next());

				if (playerEntity != null && this.level instanceof ServerLevel serverWorld) {

					playerEntity.hurtServer(serverWorld, damageSource, 2048.0F);
					if (!forceEnd) {
						serverWorld.getServer().getPlayerList().broadcastSystemMessage(Component.translatable("hud.message.pvp_controller_block.player_won_match", playerEntity.getDisplayName()), false);
					}
				}
			}
		}
		this.matchTicker = 0;
		this.matchIsActive = false;
		this.setChanged();
		if (this.level != null) {
			BlockState blockState = this.level.getBlockState(this.worldPosition);
			this.level.sendBlockUpdated(this.worldPosition, blockState, blockState, Block.UPDATE_ALL);
		}
	}

	public static void tick(Level world, BlockPos pos, BlockState state, PVPControllerBlockEntity pvpControllerBlockEntity) {
		if (!world.isClientSide() && pvpControllerBlockEntity.matchIsActive) {
			pvpControllerBlockEntity.matchTicker++;

			if (pvpControllerBlockEntity.matchTicker > pvpControllerBlockEntity.matchDuration) {
				pvpControllerBlockEntity.endMatch(false);
			}

			if (pvpControllerBlockEntity.matchTicker % 20 == 0) {
				Iterator<String> iterator = pvpControllerBlockEntity.teamSet.stream().iterator();
				Scoreboard scoreboard = world.getScoreboard();
				// new list
				PlayerTeam team;
				int activeTeamCounter = 0;
				while (iterator.hasNext()) {
					team = scoreboard.getPlayerTeam(iterator.next());

					if (team != null) {
						if (team.getPlayers().isEmpty()) {
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
			pvpControllerBlockEntity.setChanged();
			if (pvpControllerBlockEntity.level != null) {
				BlockState blockState = pvpControllerBlockEntity.level.getBlockState(pvpControllerBlockEntity.worldPosition);
				pvpControllerBlockEntity.level.sendBlockUpdated(pvpControllerBlockEntity.worldPosition, blockState, blockState, Block.UPDATE_ALL);
			}
		}
	}

	@Override
	public void trigger() {
		this.startMatch();
		this.setChanged();
		if (this.level != null) {
			BlockState blockState = this.level.getBlockState(this.worldPosition);
			this.level.sendBlockUpdated(this.worldPosition, blockState, blockState, Block.UPDATE_ALL);
		}
	}

	@Override
	public void reset() {
		// TODO no longer show scoreboard
		this.endMatch(true);
		this.teamSet.clear();
		this.playerUUIDSet.clear();
		this.setChanged();
		if (this.level != null) {
			BlockState blockState = this.level.getBlockState(this.worldPosition);
			this.level.sendBlockUpdated(this.worldPosition, blockState, blockState, Block.UPDATE_ALL);
		}
	}

	@Override
	protected void onRotate(BlockState state) {
		if (state.getBlock() instanceof RotatedBlockWithEntity) {
			if (state.getValue(ScriptBlocksBlockProperties.ROTATED) != this.rotated) {
				Rotation blockRotation = BlockRotationUtils.calculateRotationFromDifferentRotatedStates(state.getValue(ScriptBlocksBlockProperties.ROTATED), this.rotated);

				List<String> keyList = this.respawnPositions.keySet().stream().toList();
				int sideEntrancesSize = this.respawnPositions.keySet().size();
				for (int i = 0; i < sideEntrancesSize; i++) {
					String key = keyList.get(i);
					LevelData.RespawnData rotatedRespawnPosition = BlockRotationUtils.rotateSpawnPoint(this.respawnPositions.get(key), blockRotation);
					this.respawnPositions.put(key, rotatedRespawnPosition);
				}

				this.triggeredBlock.setLeft(BlockRotationUtils.rotateOffsetBlockPos(this.triggeredBlock.getLeft(), blockRotation));

				this.rotated = state.getValue(ScriptBlocksBlockProperties.ROTATED);
			}
			if (state.getValue(ScriptBlocksBlockProperties.X_MIRRORED) != this.x_mirrored) {

				List<String> keyList = this.respawnPositions.keySet().stream().toList();
				int sideEntrancesSize = this.respawnPositions.keySet().size();
				for (int i = 0; i < sideEntrancesSize; i++) {
					String key = keyList.get(i);
					LevelData.RespawnData mirroredRespawnPosition = BlockRotationUtils.mirrorSpawnPoint(this.respawnPositions.get(key), Mirror.FRONT_BACK);
					this.respawnPositions.put(key, mirroredRespawnPosition);
				}

				this.triggeredBlock.setLeft(BlockRotationUtils.mirrorOffsetBlockPos(this.triggeredBlock.getLeft(), Mirror.FRONT_BACK));

				this.x_mirrored = state.getValue(ScriptBlocksBlockProperties.X_MIRRORED);
			}
			if (state.getValue(ScriptBlocksBlockProperties.Z_MIRRORED) != this.z_mirrored) {

				List<String> keyList = this.respawnPositions.keySet().stream().toList();
				int sideEntrancesSize = this.respawnPositions.keySet().size();
				for (int i = 0; i < sideEntrancesSize; i++) {
					String key = keyList.get(i);
					LevelData.RespawnData mirroredRespawnPosition = BlockRotationUtils.mirrorSpawnPoint(this.respawnPositions.get(key), Mirror.LEFT_RIGHT);
					this.respawnPositions.put(key, mirroredRespawnPosition);
				}

				this.triggeredBlock.setLeft(BlockRotationUtils.mirrorOffsetBlockPos(this.triggeredBlock.getLeft(), Mirror.LEFT_RIGHT));

				this.z_mirrored = state.getValue(ScriptBlocksBlockProperties.Z_MIRRORED);
			}
		}
	}
}
