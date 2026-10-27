package com.github.theredbrain.scriptblocks.block.entity;

import com.github.theredbrain.scriptblocks.block.HandlesUUIDList;
import com.github.theredbrain.scriptblocks.block.RotatedBlockWithEntity;
import com.github.theredbrain.scriptblocks.block.Triggerable;
import com.github.theredbrain.scriptblocks.registry.ScriptBlocksEntities;
import com.github.theredbrain.scriptblocks.util.BlockRotationUtils;
import com.github.theredbrain.scriptblocks.util.DataAttachmentHelper;
import com.github.theredbrain.scriptblocks.util.ScriptBlocksBlockProperties;
import com.github.theredbrain.scriptblocks.util.ScriptBlocksNBTHelper;
import org.apache.commons.lang3.tuple.MutablePair;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Team;

public class TeamControllerBlockEntity extends RotatedBlockEntity implements HandlesUUIDList, Triggerable {

	private boolean enableTicking = true;

	private boolean calculateAreaBox = true;
	private AABB area = null;
	private boolean showArea = false;
	private Vec3i areaDimensions = Vec3i.ZERO;
	private BlockPos areaPositionOffset = new BlockPos(0, 0, 0);
	private BlockPos pvpControllerBlockPositionOffset = new BlockPos(0, 0, 0);

	private String teamIdentifier = "";
	private String displayNameString = "";
	private ChatFormatting teamColor = ChatFormatting.RESET;
	private boolean friendlyFire = false;
	private boolean showFriendlyInvisibles = false;
	private Team.Visibility nametagVisibility = Team.Visibility.ALWAYS;
	private Team.Visibility deathMessageVisibility = Team.Visibility.ALWAYS;
	private Team.CollisionRule collisionRule = Team.CollisionRule.ALWAYS;
	private String prefixString = "";
	private String suffixString = "";

	private PlayerTeam team = null;

	public TeamControllerBlockEntity(BlockPos pos, BlockState state) {
		super(ScriptBlocksEntities.TEAM_CONTROLLER_BLOCK_ENTITY, pos, state);
	}

	@Override
	protected void saveAdditional(ValueOutput view) {

		view.putBoolean("enable_ticking", this.enableTicking);

		CompoundTag nbtCompound = new CompoundTag();
		nbtCompound.putBoolean("visible", this.showArea);
		nbtCompound.store("position_offset", BlockPos.CODEC, this.areaPositionOffset);
		nbtCompound.put("dimensions", ScriptBlocksNBTHelper.fromVec3i(this.areaDimensions));
		view.store("area", CompoundTag.CODEC, nbtCompound.copy());

		view.store("pvp_controller_block_position_offset", BlockPos.CODEC, this.pvpControllerBlockPositionOffset);

		nbtCompound = new CompoundTag();
		nbtCompound.putString("identifier", this.teamIdentifier);

		nbtCompound.putString("display_name_string", this.displayNameString);

		nbtCompound.putInt("color_index", this.getTeamColorIndex());

		nbtCompound.putBoolean("friendly_fire", this.friendlyFire);

		nbtCompound.putBoolean("show_friendly_invisibles", this.showFriendlyInvisibles);

		nbtCompound.store("nametag_visibility", Team.Visibility.CODEC, this.nametagVisibility);

		nbtCompound.store("death_message_visibility", Team.Visibility.CODEC, this.deathMessageVisibility);

		nbtCompound.store("collision_rule", Team.CollisionRule.CODEC, this.collisionRule);

		nbtCompound.putString("prefix_string", this.prefixString);

		nbtCompound.putString("suffix_string", this.suffixString);
		view.store("team_settings", CompoundTag.CODEC, nbtCompound.copy());

		super.saveAdditional(view);

	}

	@Override
	protected void loadAdditional(ValueInput view) {

		this.enableTicking = view.getBooleanOr("enable_ticking", true);

		CompoundTag nbtCompound = view.read("area", CompoundTag.CODEC).orElseGet(CompoundTag::new);
		this.showArea = nbtCompound.getBooleanOr("visible", false);

		this.areaDimensions = ScriptBlocksNBTHelper.toVec3i(nbtCompound, "dimensions").orElse(new Vec3i(0, 0, 0));

			this.areaPositionOffset = BlockPos.max(
					BlockPos.min(
							nbtCompound.read("position_offset", BlockPos.CODEC).orElse(BlockPos.ZERO),
							new BlockPos(48, 48, 48)
					),
					new BlockPos(-48, -48, -48)
			);

			this.pvpControllerBlockPositionOffset = BlockPos.max(
					BlockPos.min(
							nbtCompound.read("pvp_controller_block_position_offset", BlockPos.CODEC).orElse(BlockPos.ZERO),
							new BlockPos(48, 48, 48)
					),
					new BlockPos(-48, -48, -48)
			);

		nbtCompound = view.read("team_settings", CompoundTag.CODEC).orElseGet(CompoundTag::new);
		this.teamIdentifier = nbtCompound.getStringOr("identifier", "");

		this.displayNameString = nbtCompound.getStringOr("display_name_string", "");


		this.setTeamColor(nbtCompound.getIntOr("color_index", -1));

		this.friendlyFire = nbtCompound.getBooleanOr("friendly_fire", false);

		this.showFriendlyInvisibles = nbtCompound.getBooleanOr("show_friendly_invisibles", false);

		this.nametagVisibility = nbtCompound.read("nametag_visibility", Team.Visibility.CODEC).orElse(Team.Visibility.ALWAYS);

		this.deathMessageVisibility = nbtCompound.read("death_message_visibility", Team.Visibility.CODEC).orElse(Team.Visibility.ALWAYS);

		this.collisionRule = nbtCompound.read("collision_rule", Team.CollisionRule.CODEC).orElse(Team.CollisionRule.ALWAYS);

		this.prefixString = nbtCompound.getStringOr("prefix_string", "");

		this.suffixString = nbtCompound.getStringOr("suffix_string", "");

		super.loadAdditional(view);

	}

	public ClientboundBlockEntityDataPacket getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}

	@Override
	public CompoundTag getUpdateTag(HolderLookup.Provider registryLookup) {
		return this.saveCustomOnly(registryLookup);
	}

	public static void tick(Level world, BlockPos pos, BlockState state, TeamControllerBlockEntity teamControllerBlockEntity) {
		if (!world.isClientSide() && teamControllerBlockEntity.enableTicking && world.getGameTime() % 80L == 0L) {
			if (teamControllerBlockEntity.calculateAreaBox || teamControllerBlockEntity.area == null) {
				BlockPos areaPositionOffset = teamControllerBlockEntity.areaPositionOffset;
				Vec3i areaDimensions = teamControllerBlockEntity.areaDimensions;
				Vec3 areaStart = new Vec3(pos.getX() + areaPositionOffset.getX(), pos.getY() + areaPositionOffset.getY(), pos.getZ() + areaPositionOffset.getZ());
				Vec3 areaEnd = new Vec3(areaStart.x() + areaDimensions.getX(), areaStart.y() + areaDimensions.getY(), areaStart.z() + areaDimensions.getZ());
				teamControllerBlockEntity.area = new AABB(areaStart, areaEnd);
				teamControllerBlockEntity.calculateAreaBox = false;
				teamControllerBlockEntity.setChanged();
				world.sendBlockUpdated(pos, state, state, Block.UPDATE_ALL);
			}
			if (teamControllerBlockEntity.team != null && teamControllerBlockEntity.level != null && !teamControllerBlockEntity.level.isClientSide()) {
				BlockPos pvpControllerBlockPos = null;
				PVPControllerBlockEntity pvpControllerBlockEntity = null;
				if (teamControllerBlockEntity.pvpControllerBlockPositionOffset != BlockPos.ZERO) {
					BlockPos pvpControllerBlockPositionOffset = teamControllerBlockEntity.pvpControllerBlockPositionOffset;
					pvpControllerBlockPos = teamControllerBlockEntity.worldPosition.offset(pvpControllerBlockPositionOffset.getX(), pvpControllerBlockPositionOffset.getY(), pvpControllerBlockPositionOffset.getZ());
					BlockEntity blockEntity = teamControllerBlockEntity.level.getBlockEntity(pvpControllerBlockPos);
					if (blockEntity instanceof PVPControllerBlockEntity) {
						pvpControllerBlockEntity = (PVPControllerBlockEntity) blockEntity;
					}
				}
				List<LivingEntity> livingEntityList = world.getEntitiesOfClass(LivingEntity.class, teamControllerBlockEntity.area);
				for (LivingEntity livingEntity : livingEntityList) {
					teamControllerBlockEntity.level.getScoreboard().addPlayerToTeam(livingEntity.getScoreboardName(), teamControllerBlockEntity.team);
					if (livingEntity instanceof Player playerEntity && pvpControllerBlockPos != null && pvpControllerBlockEntity != null) {
						DataAttachmentHelper.setCurrentPVPControllerBlockPosition(playerEntity, Optional.of(pvpControllerBlockPos));
						pvpControllerBlockEntity.addPlayerAndTeam(teamControllerBlockEntity.team, playerEntity);
					}
				}
			}
		}
	}

	// region --- getter & setter ---
	public boolean enableTicking() {
		return this.enableTicking;
	}

	public void setEnableTicking(boolean enableTicking) {
		this.enableTicking = enableTicking;
	}

	public boolean showArea() {
		return showArea;
	}

	public void setShowArea(boolean showArea) {
		this.showArea = showArea;
	}

	public Vec3i getAreaDimensions() {
		return areaDimensions;
	}

	public void setAreaDimensions(Vec3i areaDimensions) {
		this.areaDimensions = areaDimensions;
		this.calculateAreaBox = true;
	}

	public BlockPos getAreaPositionOffset() {
		return areaPositionOffset;
	}

	public void setAreaPositionOffset(BlockPos areaPositionOffset) {
		this.areaPositionOffset = areaPositionOffset;
		this.calculateAreaBox = true;
	}

	public BlockPos getPVPControllerBlockPositionOffset() {
		return this.pvpControllerBlockPositionOffset;
	}

	public void setPVPControllerBlockPositionOffset(BlockPos pvpControllerBlockPositionOffset) {
		this.pvpControllerBlockPositionOffset = pvpControllerBlockPositionOffset;
	}

	public String getTeamIdentifier() {
		return this.teamIdentifier;
	}

	public void setTeamIdentifier(String teamIdentifier) {
		this.teamIdentifier = teamIdentifier;
	}

	public Component getDisplayName(Level world) {
		return Text.Serialization.fromLenientJson(this.displayNameString, world.registryAccess());
	}

	public String getDisplayNameString() {
		return this.displayNameString;
	}

	public void setDisplayNameString(String displayNameString) {
		this.displayNameString = displayNameString;
	}

	public ChatFormatting getTeamColor() {
		return this.teamColor;
	}

	public int getTeamColorIndex() {
		return this.teamColor.getId();
	}

	public void setTeamColor(int teamColorIndex) {
		ChatFormatting teamColor = ChatFormatting.getById(teamColorIndex);
		this.teamColor = teamColor != null ? teamColor : ChatFormatting.RESET;
	}

	public boolean friendlyFire() {
		return this.friendlyFire;
	}

	public void setFriendlyFire(boolean friendlyFire) {
		this.friendlyFire = friendlyFire;
	}

	public boolean showFriendlyInvisibles() {
		return this.showFriendlyInvisibles;
	}

	public void setShowFriendlyInvisibles(boolean showFriendlyInvisibles) {
		this.showFriendlyInvisibles = showFriendlyInvisibles;
	}

	public Team.Visibility getNametagVisibility() {
		return this.nametagVisibility;
	}

	public void setNametagVisibility(Team.Visibility nametagVisibility) {
		this.nametagVisibility = nametagVisibility;
	}

	public Team.Visibility getDeathMessageVisibility() {
		return this.deathMessageVisibility;
	}

	public void setDeathMessageVisibility(Team.Visibility deathMessageVisibility) {
		this.deathMessageVisibility = deathMessageVisibility;
	}

	public Team.CollisionRule getCollisionRule() {
		return this.collisionRule;
	}

	public void setCollisionRule(Team.CollisionRule collisionRule) {
		this.collisionRule = collisionRule;
	}

	public Component getPrefix(Level world) {
		return Text.Serialization.fromLenientJson(this.prefixString, world.registryAccess());
	}

	public String getPrefixString() {
		return this.prefixString;
	}

	public void setPrefixString(String prefixString) {
		this.prefixString = prefixString;
	}

	public Component getSuffix(Level world) {
		return Text.Serialization.fromLenientJson(this.suffixString, world.registryAccess());
	}

	public String getSuffixString() {
		return this.suffixString;
	}

	public void setSuffixString(String suffixString) {
		this.suffixString = suffixString;
	}
	// endregion --- getter & setter ---

	@Override
	public List<UUID> supplyUUIDList(boolean remove) {
		return List.of();
	}

	@Override
	public void handleUUIDList(List<UUID> list, boolean remove) {
		if (this.level != null && !this.level.isClientSide() && this.team != null) {
			list.forEach((uuid) -> {
				Player playerEntity = this.level.getPlayerByUUID(uuid);
				if (playerEntity != null) {
					BlockPos pvpControllerBlockPos = null;
					PVPControllerBlockEntity pvpControllerBlockEntity = null;
					if (this.pvpControllerBlockPositionOffset != BlockPos.ZERO) {
						BlockPos pvpControllerBlockPositionOffset = this.pvpControllerBlockPositionOffset;
						pvpControllerBlockPos = this.worldPosition.offset(pvpControllerBlockPositionOffset.getX(), pvpControllerBlockPositionOffset.getY(), pvpControllerBlockPositionOffset.getZ());
						BlockEntity blockEntity = this.level.getBlockEntity(pvpControllerBlockPos);
						if (blockEntity instanceof PVPControllerBlockEntity) {
							pvpControllerBlockEntity = (PVPControllerBlockEntity) blockEntity;
						}
					}
					this.level.getScoreboard().addPlayerToTeam(playerEntity.getScoreboardName(), this.team);
					if (pvpControllerBlockPos != null && pvpControllerBlockEntity != null) {
						DataAttachmentHelper.setCurrentPVPControllerBlockPosition(playerEntity, Optional.of(pvpControllerBlockPos));
						pvpControllerBlockEntity.addPlayerAndTeam(this.team, playerEntity);
					}
				}
			});
		}
	}

	@Override
	public void reset() {
		if (this.team != null) {
			this.team.getScoreboard().removePlayerTeam(this.team);
			this.team = null;
		}
		this.setChanged();
		if (this.level != null) {
			BlockState blockState = this.level.getBlockState(this.worldPosition);
			this.level.sendBlockUpdated(this.worldPosition, blockState, blockState, Block.UPDATE_ALL);
		}
	}

	@Override
	public void trigger() {
		if (this.team == null && this.level != null && !this.level.isClientSide()) {
			if (this.level.getScoreboard().getPlayerTeam(this.teamIdentifier) == null) {
				this.level.getScoreboard().addPlayerTeam(this.teamIdentifier);
			}

			this.team = this.level.getScoreboard().getPlayerTeam(this.teamIdentifier);
			if (this.team != null) {
				if (!this.displayNameString.isEmpty()) {
					this.team.setDisplayName(this.getDisplayName(this.level));
				}
				if (!this.prefixString.isEmpty()) {
					this.team.setPlayerPrefix(this.getPrefix(this.level));
				}
				if (!this.suffixString.isEmpty()) {
					this.team.setPlayerSuffix(this.getSuffix(this.level));
				}
				this.team.setAllowFriendlyFire(this.friendlyFire);
				this.team.setSeeFriendlyInvisibles(this.showFriendlyInvisibles);
				Team.Visibility nametagVisibility = Team.Visibility.getRule(this.nametagVisibility);
				this.team.setNameTagVisibility(nametagVisibility != null ? nametagVisibility : Team.Visibility.ALWAYS);
				Team.Visibility deathMessageVisibility = Team.Visibility.getRule(this.deathMessageVisibility);
				this.team.setDeathMessageVisibility(deathMessageVisibility != null ? deathMessageVisibility : Team.Visibility.ALWAYS);
				Team.CollisionRule collisionRule = Team.CollisionRule.valueOf(this.collisionRule);
				this.team.setCollisionRule(collisionRule != null ? collisionRule : Team.CollisionRule.ALWAYS);
				this.team.setColor(this.teamColor);
			}
		}
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

				MutablePair<BlockPos, Vec3i> offsetArea = BlockRotationUtils.rotateOffsetArea(this.areaPositionOffset, this.areaDimensions, blockRotation);
				this.areaPositionOffset = offsetArea.getLeft();
				this.areaDimensions = offsetArea.getRight();

				this.rotated = state.getValue(ScriptBlocksBlockProperties.ROTATED);
			}
			if (state.getValue(ScriptBlocksBlockProperties.X_MIRRORED) != this.x_mirrored) {

				MutablePair<BlockPos, Vec3i> offsetArea = BlockRotationUtils.mirrorOffsetArea(this.areaPositionOffset, this.areaDimensions, Mirror.FRONT_BACK);
				this.areaPositionOffset = offsetArea.getLeft();
				this.areaDimensions = offsetArea.getRight();

				this.x_mirrored = state.getValue(ScriptBlocksBlockProperties.X_MIRRORED);
			}
			if (state.getValue(ScriptBlocksBlockProperties.Z_MIRRORED) != this.z_mirrored) {

				MutablePair<BlockPos, Vec3i> offsetArea = BlockRotationUtils.mirrorOffsetArea(this.areaPositionOffset, this.areaDimensions, Mirror.LEFT_RIGHT);
				this.areaPositionOffset = offsetArea.getLeft();
				this.areaDimensions = offsetArea.getRight();

				this.z_mirrored = state.getValue(ScriptBlocksBlockProperties.Z_MIRRORED);
			}
		}
	}
}
