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
import net.minecraft.block.Block;
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
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
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
	private final HashMap<String, MutablePair<BlockPos, MutablePair<Double, Double>>> respawnPositions = new HashMap<>(Map.of());
	private MutablePair<BlockPos, Boolean> triggeredBlock = new MutablePair<>(BlockPos.ORIGIN, false);
	private BlockPos dataProvidingBlockPosOffset = BlockPos.ORIGIN;

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
	protected void writeData(WriteView view) {

		view.putString("pvp_arena_settings_identifier", this.pvpArenaSettingsIdentifier);

		WriteView.ListAppender<NbtCompound> listAppender = view.getListAppender("respawn_positions", NbtCompound.CODEC);
		this.respawnPositions.forEach((key, entry) -> {
			NbtCompound nbtCompound = new NbtCompound();
			nbtCompound.putString("key", key);
			nbtCompound.put("position_offset", BlockPos.CODEC, this.respawnPositions.get(key).getLeft());
			nbtCompound.putDouble("yaw", this.respawnPositions.get(key).getRight().getLeft());
			nbtCompound.putDouble("pitch", this.respawnPositions.get(key).getRight().getRight());
			listAppender.add(nbtCompound.copy());
		});

		NbtCompound nbtCompound = new NbtCompound();
		nbtCompound.put("position_offset", BlockPos.CODEC, this.triggeredBlock.getLeft());
		nbtCompound.putBoolean("resets", this.triggeredBlock.getRight());
		view.put("triggered_block", NbtCompound.CODEC, nbtCompound.copy());

		view.put("data_providing_block_position_offset", BlockPos.CODEC, this.dataProvidingBlockPosOffset);

		view.putString("match_duration_data_identifier", this.matchDurationDataIdentifier);

		super.writeData(view);
	}

	@Override
	protected void readData(ReadView view) {

		this.pvpArenaSettingsIdentifier = view.getString("pvp_arena_settings_identifier", "");

		this.respawnPositions.clear();
		for (NbtCompound nbtCompound : view.getTypedListView("respawn_positions", NbtCompound.CODEC)) {
			this.respawnPositions.put(
					nbtCompound.getString("key", ""),
					new MutablePair<>(
							BlockPos.max(
									BlockPos.min(
											nbtCompound.get("position_offset", BlockPos.CODEC).orElse(BlockPos.ORIGIN),
											new BlockPos(48, 48, 48)
									),
									new BlockPos(-48, -48, -48)
							),
							new MutablePair<>(
									MathHelper.clamp(nbtCompound.getDouble("yaw", 0), -180, 180),
									MathHelper.clamp(nbtCompound.getDouble("pitch", 0), -90, 90)
							)
					)
			);
		}

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

		this.dataProvidingBlockPosOffset = BlockPos.max(
				BlockPos.min(
						view.read("data_providing_block_pos_offset", BlockPos.CODEC).orElse(BlockPos.ORIGIN),
						new BlockPos(48, 48, 48)
				),
				new BlockPos(-48, -48, -48)
		);

		this.matchDurationDataIdentifier = view.getString("match_duration_data_identifier", "");

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
	public String getPVPArenaSettingsIdentifier() {
		return this.pvpArenaSettingsIdentifier;
	}

	public boolean setPVPArenaSettingsIdentifier(String newPVPArenaSettingsIdentifier) {
		PVPArenaSettings pvpArenaSettings = null;
		Identifier identifier = Identifier.tryParse(newPVPArenaSettingsIdentifier);
		if (identifier != null && this.world != null) {
			Optional<RegistryEntry.Reference<PVPArenaSettings>> optionalPVPArenaSettingsReference = this.world.getRegistryManager().get(ScriptBlocksDynamicRegistries.PVP_ARENA_SETTINGS_REGISTRY_KEY).getEntry(identifier);
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
// endregion --- getter & setter ---

	@Nullable
	public MutablePair<BlockPos, MutablePair<Double, Double>> getTeamRespawnPosition(String teamId, boolean endOfBattle) {
		PVPArenaSettings pvpArenaSettings = null;
		Identifier identifier = Identifier.tryParse(this.pvpArenaSettingsIdentifier);
		if (identifier != null && this.world != null) {
			Optional<RegistryEntry.Reference<PVPArenaSettings>> optionalPVPArenaSettingsReference = this.world.getRegistryManager().get(ScriptBlocksDynamicRegistries.PVP_ARENA_SETTINGS_REGISTRY_KEY).getEntry(identifier);
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
		this.markDirty();
		if (this.world != null) {
			BlockState blockState = this.world.getBlockState(this.pos);
			this.world.updateListeners(this.pos, blockState, blockState, Block.NOTIFY_ALL);
		}
	}

	public void removePlayer(PlayerEntity playerEntity) {
		this.playerUUIDSet.remove(playerEntity.getUuid());
		this.markDirty();
		if (this.world != null) {
			BlockState blockState = this.world.getBlockState(this.pos);
			this.world.updateListeners(this.pos, blockState, blockState, Block.NOTIFY_ALL);
		}
	}

	public void startMatch() {
		this.initMatchDuration();
		this.matchIsActive = true;
	}

	private void initMatchDuration() {
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

			DamageSource damageSource = new DamageSource(registry.entryOf(ScriptBlocksDamageTypes.PVP_WINNING_DAMAGE_TYPE));

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
		this.markDirty();
		if (this.world != null) {
			BlockState blockState = this.world.getBlockState(this.pos);
			this.world.updateListeners(this.pos, blockState, blockState, Block.NOTIFY_ALL);
		}
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
			pvpControllerBlockEntity.markDirty();
			if (pvpControllerBlockEntity.world != null) {
				BlockState blockState = pvpControllerBlockEntity.world.getBlockState(pvpControllerBlockEntity.pos);
				pvpControllerBlockEntity.world.updateListeners(pvpControllerBlockEntity.pos, blockState, blockState, Block.NOTIFY_ALL);
			}
		}
	}

	@Override
	public void trigger() {
		this.startMatch();
		this.markDirty();
		if (this.world != null) {
			BlockState blockState = this.world.getBlockState(this.pos);
			this.world.updateListeners(this.pos, blockState, blockState, Block.NOTIFY_ALL);
		}
	}

	@Override
	public void reset() {
		// TODO no longer show scoreboard
		this.endMatch(true);
		this.teamSet.clear();
		this.playerUUIDSet.clear();
		this.markDirty();
		if (this.world != null) {
			BlockState blockState = this.world.getBlockState(this.pos);
			this.world.updateListeners(this.pos, blockState, blockState, Block.NOTIFY_ALL);
		}
	}

	@Override
	protected void onRotate(BlockState state) {
		if (state.getBlock() instanceof RotatedBlockWithEntity) {
			if (state.get(ScriptBlocksBlockProperties.ROTATED) != this.rotated) {
				BlockRotation blockRotation = BlockRotationUtils.calculateRotationFromDifferentRotatedStates(state.get(ScriptBlocksBlockProperties.ROTATED), this.rotated);

				List<String> keyList = this.respawnPositions.keySet().stream().toList();
				int sideEntrancesSize = this.respawnPositions.keySet().size();
				for (int i = 0; i < sideEntrancesSize; i++) {
					String key = keyList.get(i);
					MutablePair<BlockPos, MutablePair<Double, Double>> rotatedEntrance = BlockRotationUtils.rotateSpawnPoint(this.respawnPositions.get(key), blockRotation);
					this.respawnPositions.put(key, rotatedEntrance);
				}

				this.triggeredBlock.setLeft(BlockRotationUtils.rotateOffsetBlockPos(this.triggeredBlock.getLeft(), blockRotation));

				this.rotated = state.get(ScriptBlocksBlockProperties.ROTATED);
			}
			if (state.get(ScriptBlocksBlockProperties.X_MIRRORED) != this.x_mirrored) {

				List<String> keyList = this.respawnPositions.keySet().stream().toList();
				int sideEntrancesSize = this.respawnPositions.keySet().size();
				for (int i = 0; i < sideEntrancesSize; i++) {
					String key = keyList.get(i);
					MutablePair<BlockPos, MutablePair<Double, Double>> mirroredEntrance = BlockRotationUtils.mirrorSpawnPoint(this.respawnPositions.get(key), BlockMirror.FRONT_BACK);
					this.respawnPositions.put(key, mirroredEntrance);
				}

				this.triggeredBlock.setLeft(BlockRotationUtils.mirrorOffsetBlockPos(this.triggeredBlock.getLeft(), BlockMirror.FRONT_BACK));

				this.x_mirrored = state.get(ScriptBlocksBlockProperties.X_MIRRORED);
			}
			if (state.get(ScriptBlocksBlockProperties.Z_MIRRORED) != this.z_mirrored) {

				List<String> keyList = this.respawnPositions.keySet().stream().toList();
				int sideEntrancesSize = this.respawnPositions.keySet().size();
				for (int i = 0; i < sideEntrancesSize; i++) {
					String key = keyList.get(i);
					MutablePair<BlockPos, MutablePair<Double, Double>> mirroredEntrance = BlockRotationUtils.mirrorSpawnPoint(this.respawnPositions.get(key), BlockMirror.LEFT_RIGHT);
					this.respawnPositions.put(key, mirroredEntrance);
				}

				this.triggeredBlock.setLeft(BlockRotationUtils.mirrorOffsetBlockPos(this.triggeredBlock.getLeft(), BlockMirror.LEFT_RIGHT));

				this.z_mirrored = state.get(ScriptBlocksBlockProperties.Z_MIRRORED);
			}
		}
	}
}
