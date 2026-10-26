package com.github.theredbrain.scriptblocks.block.entity;

import com.github.theredbrain.scriptblocks.block.HandlesUUIDList;
import com.github.theredbrain.scriptblocks.block.RotatedBlockWithEntity;
import com.github.theredbrain.scriptblocks.block.Triggerable;
import com.github.theredbrain.scriptblocks.registry.ScriptBlocksEntities;
import com.github.theredbrain.scriptblocks.util.BlockRotationUtils;
import com.github.theredbrain.scriptblocks.util.DataAttachmentHelper;
import com.github.theredbrain.scriptblocks.util.ScriptBlocksBlockProperties;
import com.github.theredbrain.scriptblocks.util.ScriptBlocksNBTHelper;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.scoreboard.AbstractTeam;
import net.minecraft.scoreboard.Team;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.text.Text;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.MutablePair;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class TeamControllerBlockEntity extends RotatedBlockEntity implements HandlesUUIDList, Triggerable {

	private boolean enableTicking = true;

	private boolean calculateAreaBox = true;
	private Box area = null;
	private boolean showArea = false;
	private Vec3i areaDimensions = Vec3i.ZERO;
	private BlockPos areaPositionOffset = new BlockPos(0, 0, 0);
	private BlockPos pvpControllerBlockPositionOffset = new BlockPos(0, 0, 0);

	private String teamIdentifier = "";
	private String displayNameString = "";
	private Formatting teamColor = Formatting.RESET;
	private boolean friendlyFire = false;
	private boolean showFriendlyInvisibles = false;
	private AbstractTeam.VisibilityRule nametagVisibility = AbstractTeam.VisibilityRule.ALWAYS;
	private AbstractTeam.VisibilityRule deathMessageVisibility = AbstractTeam.VisibilityRule.ALWAYS;
	private AbstractTeam.CollisionRule collisionRule = AbstractTeam.CollisionRule.ALWAYS;
	private String prefixString = "";
	private String suffixString = "";

	private Team team = null;

	public TeamControllerBlockEntity(BlockPos pos, BlockState state) {
		super(ScriptBlocksEntities.TEAM_CONTROLLER_BLOCK_ENTITY, pos, state);
	}

	@Override
	protected void writeData(WriteView view) {

		view.putBoolean("enable_ticking", this.enableTicking);

		NbtCompound nbtCompound = new NbtCompound();
		nbtCompound.putBoolean("visible", this.showArea);
		nbtCompound.put("position_offset", BlockPos.CODEC, this.areaPositionOffset);
		nbtCompound.put("dimensions", ScriptBlocksNBTHelper.fromVec3i(this.areaDimensions));
		view.put("area", NbtCompound.CODEC, nbtCompound.copy());

		view.put("pvp_controller_block_position_offset", BlockPos.CODEC, this.pvpControllerBlockPositionOffset);

		nbtCompound = new NbtCompound();
		nbtCompound.putString("identifier", this.teamIdentifier);

		nbtCompound.putString("display_name_string", this.displayNameString);

		nbtCompound.putInt("color_index", this.getTeamColorIndex());

		nbtCompound.putBoolean("friendly_fire", this.friendlyFire);

		nbtCompound.putBoolean("show_friendly_invisibles", this.showFriendlyInvisibles);

		nbtCompound.put("nametag_visibility", AbstractTeam.VisibilityRule.CODEC, this.nametagVisibility);

		nbtCompound.put("death_message_visibility", AbstractTeam.VisibilityRule.CODEC, this.deathMessageVisibility);

		nbtCompound.put("collision_rule", AbstractTeam.CollisionRule.CODEC, this.collisionRule);

		nbtCompound.putString("prefix_string", this.prefixString);

		nbtCompound.putString("suffix_string", this.suffixString);
		view.put("team_settings", NbtCompound.CODEC, nbtCompound.copy());

		super.writeData(view);

	}

	@Override
	protected void readData(ReadView view) {

		this.enableTicking = view.getBoolean("enable_ticking", true);

		NbtCompound nbtCompound = view.read("area", NbtCompound.CODEC).orElseGet(NbtCompound::new);
		this.showArea = nbtCompound.getBoolean("visible", false);

		this.areaDimensions = ScriptBlocksNBTHelper.toVec3i(nbtCompound, "dimensions").orElse(new Vec3i(0, 0, 0));

			this.areaPositionOffset = BlockPos.max(
					BlockPos.min(
							nbtCompound.get("position_offset", BlockPos.CODEC).orElse(BlockPos.ORIGIN),
							new BlockPos(48, 48, 48)
					),
					new BlockPos(-48, -48, -48)
			);

			this.pvpControllerBlockPositionOffset = BlockPos.max(
					BlockPos.min(
							nbtCompound.get("pvp_controller_block_position_offset", BlockPos.CODEC).orElse(BlockPos.ORIGIN),
							new BlockPos(48, 48, 48)
					),
					new BlockPos(-48, -48, -48)
			);

		nbtCompound = view.read("team_settings", NbtCompound.CODEC).orElseGet(NbtCompound::new);
		this.teamIdentifier = nbtCompound.getString("identifier", "");

		this.displayNameString = nbtCompound.getString("display_name_string", "");


		this.setTeamColor(nbtCompound.getInt("color_index", -1));

		this.friendlyFire = nbtCompound.getBoolean("friendly_fire", false);

		this.showFriendlyInvisibles = nbtCompound.getBoolean("show_friendly_invisibles", false);

		this.nametagVisibility = nbtCompound.get("nametag_visibility", AbstractTeam.VisibilityRule.CODEC).orElse(AbstractTeam.VisibilityRule.ALWAYS);

		this.deathMessageVisibility = nbtCompound.get("death_message_visibility", AbstractTeam.VisibilityRule.CODEC).orElse(AbstractTeam.VisibilityRule.ALWAYS);

		this.collisionRule = nbtCompound.get("collision_rule", AbstractTeam.CollisionRule.CODEC).orElse(AbstractTeam.CollisionRule.ALWAYS);

		this.prefixString = nbtCompound.getString("prefix_string", "");

		this.suffixString = nbtCompound.getString("suffix_string", "");

		super.readData(view);

	}

	public BlockEntityUpdateS2CPacket toUpdatePacket() {
		return BlockEntityUpdateS2CPacket.create(this);
	}

	@Override
	public NbtCompound toInitialChunkDataNbt(RegistryWrapper.WrapperLookup registryLookup) {
		return this.createComponentlessNbt(registryLookup);
	}

	public static void tick(World world, BlockPos pos, BlockState state, TeamControllerBlockEntity teamControllerBlockEntity) {
		if (!world.isClient && teamControllerBlockEntity.enableTicking && world.getTime() % 80L == 0L) {
			if (teamControllerBlockEntity.calculateAreaBox || teamControllerBlockEntity.area == null) {
				BlockPos areaPositionOffset = teamControllerBlockEntity.areaPositionOffset;
				Vec3i areaDimensions = teamControllerBlockEntity.areaDimensions;
				Vec3d areaStart = new Vec3d(pos.getX() + areaPositionOffset.getX(), pos.getY() + areaPositionOffset.getY(), pos.getZ() + areaPositionOffset.getZ());
				Vec3d areaEnd = new Vec3d(areaStart.getX() + areaDimensions.getX(), areaStart.getY() + areaDimensions.getY(), areaStart.getZ() + areaDimensions.getZ());
				teamControllerBlockEntity.area = new Box(areaStart, areaEnd);
				teamControllerBlockEntity.calculateAreaBox = false;
				teamControllerBlockEntity.markDirty();
				world.updateListeners(pos, state, state, Block.NOTIFY_ALL);
			}
			if (teamControllerBlockEntity.team != null && teamControllerBlockEntity.world != null && !teamControllerBlockEntity.world.isClient) {
				BlockPos pvpControllerBlockPos = null;
				PVPControllerBlockEntity pvpControllerBlockEntity = null;
				if (teamControllerBlockEntity.pvpControllerBlockPositionOffset != BlockPos.ORIGIN) {
					BlockPos pvpControllerBlockPositionOffset = teamControllerBlockEntity.pvpControllerBlockPositionOffset;
					pvpControllerBlockPos = teamControllerBlockEntity.pos.add(pvpControllerBlockPositionOffset.getX(), pvpControllerBlockPositionOffset.getY(), pvpControllerBlockPositionOffset.getZ());
					BlockEntity blockEntity = teamControllerBlockEntity.world.getBlockEntity(pvpControllerBlockPos);
					if (blockEntity instanceof PVPControllerBlockEntity) {
						pvpControllerBlockEntity = (PVPControllerBlockEntity) blockEntity;
					}
				}
				List<LivingEntity> livingEntityList = world.getNonSpectatingEntities(LivingEntity.class, teamControllerBlockEntity.area);
				for (LivingEntity livingEntity : livingEntityList) {
					teamControllerBlockEntity.world.getScoreboard().addScoreHolderToTeam(livingEntity.getNameForScoreboard(), teamControllerBlockEntity.team);
					if (livingEntity instanceof PlayerEntity playerEntity && pvpControllerBlockPos != null && pvpControllerBlockEntity != null) {
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

	public Text getDisplayName(World world) {
		return Text.Serialization.fromLenientJson(this.displayNameString, world.getRegistryManager());
	}

	public String getDisplayNameString() {
		return this.displayNameString;
	}

	public void setDisplayNameString(String displayNameString) {
		this.displayNameString = displayNameString;
	}

	public Formatting getTeamColor() {
		return this.teamColor;
	}

	public int getTeamColorIndex() {
		return this.teamColor.getColorIndex();
	}

	public void setTeamColor(int teamColorIndex) {
		Formatting teamColor = Formatting.byColorIndex(teamColorIndex);
		this.teamColor = teamColor != null ? teamColor : Formatting.RESET;
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

	public AbstractTeam.VisibilityRule getNametagVisibility() {
		return this.nametagVisibility;
	}

	public void setNametagVisibility(AbstractTeam.VisibilityRule nametagVisibility) {
		this.nametagVisibility = nametagVisibility;
	}

	public AbstractTeam.VisibilityRule getDeathMessageVisibility() {
		return this.deathMessageVisibility;
	}

	public void setDeathMessageVisibility(AbstractTeam.VisibilityRule deathMessageVisibility) {
		this.deathMessageVisibility = deathMessageVisibility;
	}

	public AbstractTeam.CollisionRule getCollisionRule() {
		return this.collisionRule;
	}

	public void setCollisionRule(AbstractTeam.CollisionRule collisionRule) {
		this.collisionRule = collisionRule;
	}

	public Text getPrefix(World world) {
		return Text.Serialization.fromLenientJson(this.prefixString, world.getRegistryManager());
	}

	public String getPrefixString() {
		return this.prefixString;
	}

	public void setPrefixString(String prefixString) {
		this.prefixString = prefixString;
	}

	public Text getSuffix(World world) {
		return Text.Serialization.fromLenientJson(this.suffixString, world.getRegistryManager());
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
		if (this.world != null && !this.world.isClient() && this.team != null) {
			list.forEach((uuid) -> {
				PlayerEntity playerEntity = this.world.getPlayerByUuid(uuid);
				if (playerEntity != null) {
					BlockPos pvpControllerBlockPos = null;
					PVPControllerBlockEntity pvpControllerBlockEntity = null;
					if (this.pvpControllerBlockPositionOffset != BlockPos.ORIGIN) {
						BlockPos pvpControllerBlockPositionOffset = this.pvpControllerBlockPositionOffset;
						pvpControllerBlockPos = this.pos.add(pvpControllerBlockPositionOffset.getX(), pvpControllerBlockPositionOffset.getY(), pvpControllerBlockPositionOffset.getZ());
						BlockEntity blockEntity = this.world.getBlockEntity(pvpControllerBlockPos);
						if (blockEntity instanceof PVPControllerBlockEntity) {
							pvpControllerBlockEntity = (PVPControllerBlockEntity) blockEntity;
						}
					}
					this.world.getScoreboard().addScoreHolderToTeam(playerEntity.getNameForScoreboard(), this.team);
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
			this.team.getScoreboard().removeTeam(this.team);
			this.team = null;
		}
		this.markDirty();
		if (this.world != null) {
			BlockState blockState = this.world.getBlockState(this.pos);
			this.world.updateListeners(this.pos, blockState, blockState, Block.NOTIFY_ALL);
		}
	}

	@Override
	public void trigger() {
		if (this.team == null && this.world != null && !this.world.isClient) {
			if (this.world.getScoreboard().getTeam(this.teamIdentifier) == null) {
				this.world.getScoreboard().addTeam(this.teamIdentifier);
			}

			this.team = this.world.getScoreboard().getTeam(this.teamIdentifier);
			if (this.team != null) {
				if (!this.displayNameString.isEmpty()) {
					this.team.setDisplayName(this.getDisplayName(this.world));
				}
				if (!this.prefixString.isEmpty()) {
					this.team.setPrefix(this.getPrefix(this.world));
				}
				if (!this.suffixString.isEmpty()) {
					this.team.setSuffix(this.getSuffix(this.world));
				}
				this.team.setFriendlyFireAllowed(this.friendlyFire);
				this.team.setShowFriendlyInvisibles(this.showFriendlyInvisibles);
				AbstractTeam.VisibilityRule nametagVisibility = AbstractTeam.VisibilityRule.getRule(this.nametagVisibility);
				this.team.setNameTagVisibilityRule(nametagVisibility != null ? nametagVisibility : AbstractTeam.VisibilityRule.ALWAYS);
				AbstractTeam.VisibilityRule deathMessageVisibility = AbstractTeam.VisibilityRule.getRule(this.deathMessageVisibility);
				this.team.setDeathMessageVisibilityRule(deathMessageVisibility != null ? deathMessageVisibility : AbstractTeam.VisibilityRule.ALWAYS);
				AbstractTeam.CollisionRule collisionRule = AbstractTeam.CollisionRule.getRule(this.collisionRule);
				this.team.setCollisionRule(collisionRule != null ? collisionRule : AbstractTeam.CollisionRule.ALWAYS);
				this.team.setColor(this.teamColor);
			}
		}
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

				MutablePair<BlockPos, Vec3i> offsetArea = BlockRotationUtils.rotateOffsetArea(this.areaPositionOffset, this.areaDimensions, blockRotation);
				this.areaPositionOffset = offsetArea.getLeft();
				this.areaDimensions = offsetArea.getRight();

				this.rotated = state.get(ScriptBlocksBlockProperties.ROTATED);
			}
			if (state.get(ScriptBlocksBlockProperties.X_MIRRORED) != this.x_mirrored) {

				MutablePair<BlockPos, Vec3i> offsetArea = BlockRotationUtils.mirrorOffsetArea(this.areaPositionOffset, this.areaDimensions, BlockMirror.FRONT_BACK);
				this.areaPositionOffset = offsetArea.getLeft();
				this.areaDimensions = offsetArea.getRight();

				this.x_mirrored = state.get(ScriptBlocksBlockProperties.X_MIRRORED);
			}
			if (state.get(ScriptBlocksBlockProperties.Z_MIRRORED) != this.z_mirrored) {

				MutablePair<BlockPos, Vec3i> offsetArea = BlockRotationUtils.mirrorOffsetArea(this.areaPositionOffset, this.areaDimensions, BlockMirror.LEFT_RIGHT);
				this.areaPositionOffset = offsetArea.getLeft();
				this.areaDimensions = offsetArea.getRight();

				this.z_mirrored = state.get(ScriptBlocksBlockProperties.Z_MIRRORED);
			}
		}
	}
}
