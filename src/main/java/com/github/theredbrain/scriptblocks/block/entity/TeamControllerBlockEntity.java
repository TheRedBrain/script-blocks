package com.github.theredbrain.scriptblocks.block.entity;

import com.github.theredbrain.scriptblocks.block.Resetable;
import com.github.theredbrain.scriptblocks.block.RotatedBlockWithEntity;
import com.github.theredbrain.scriptblocks.block.Triggerable;
import com.github.theredbrain.scriptblocks.entity.player.DuckPlayerEntityMixin;
import com.github.theredbrain.scriptblocks.registry.EntityRegistry;
import com.github.theredbrain.scriptblocks.util.BlockRotationUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.scoreboard.AbstractTeam;
import net.minecraft.scoreboard.Team;
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

public class TeamControllerBlockEntity extends RotatedBlockEntity implements Triggerable, Resetable {

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
	private String nametagVisibility = AbstractTeam.VisibilityRule.ALWAYS.name;
	private String deathMessageVisibility = AbstractTeam.VisibilityRule.ALWAYS.name;
	private String collisionRule = AbstractTeam.CollisionRule.ALWAYS.name;
	private String prefixString = "";
	private String suffixString = "";

	private Team team = null;

	public TeamControllerBlockEntity(BlockPos pos, BlockState state) {
		super(EntityRegistry.TEAM_CONTROLLER_BLOCK_ENTITY, pos, state);
	}

	@Override
	protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {

		nbt.putBoolean("show_area", this.showArea);

		if (this.area != null) {
			nbt.putDouble("area_min_x", this.area.minX);
			nbt.putDouble("area_min_y", this.area.minY);
			nbt.putDouble("area_min_z", this.area.minZ);
			nbt.putDouble("area_max_x", this.area.maxX);
			nbt.putDouble("area_max_y", this.area.maxY);
			nbt.putDouble("area_max_z", this.area.maxZ);
		}

		nbt.putInt("area_dimensions_x", this.areaDimensions.getX());
		nbt.putInt("area_dimensions_y", this.areaDimensions.getY());
		nbt.putInt("area_dimensions_z", this.areaDimensions.getZ());

		nbt.putInt("area_position_offset_x", this.areaPositionOffset.getX());
		nbt.putInt("area_position_offset_y", this.areaPositionOffset.getY());
		nbt.putInt("area_position_offset_z", this.areaPositionOffset.getZ());

		nbt.putInt("pvp_controller_block_position_offset_x", this.pvpControllerBlockPositionOffset.getX());
		nbt.putInt("pvp_controller_block_position_offset_y", this.pvpControllerBlockPositionOffset.getY());
		nbt.putInt("pvp_controller_block_position_offset_z", this.pvpControllerBlockPositionOffset.getZ());

		nbt.putString("team_identifier", this.teamIdentifier);

		nbt.putString("display_name_string", this.displayNameString);

		nbt.putInt("team_color_index", this.getTeamColorIndex());

		nbt.putBoolean("friendly_fire", this.friendlyFire);

		nbt.putBoolean("show_friendly_invisibles", this.showFriendlyInvisibles);

		nbt.putString("nametag_visibility", this.nametagVisibility);

		nbt.putString("death_message_visibility", this.deathMessageVisibility);

		nbt.putString("collision_rule", this.collisionRule);

		nbt.putString("prefix_string", this.prefixString);

		nbt.putString("suffix_string", this.suffixString);

		super.writeNbt(nbt, registryLookup);

	}

	@Override
	protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {

		if (nbt.contains("showArea")) {
			this.showArea = nbt.getBoolean("showArea");
			nbt.remove("showArea");
		} else {
			this.showArea = nbt.getBoolean("show_area");
		}

		if (nbt.contains("areaMinX") || nbt.contains("areaMinY") || nbt.contains("areaMinZ") || nbt.contains("areaMaxX") || nbt.contains("areaMaxY") || nbt.contains("areaMaxZ")) {
			this.area = new Box(
					nbt.getDouble("areaMinX"),
					nbt.getDouble("areaMinY"),
					nbt.getDouble("areaMinZ"),
					nbt.getDouble("areaMaxX"),
					nbt.getDouble("areaMaxY"),
					nbt.getDouble("areaMaxZ")
			);
			nbt.remove("areaMinX");
			nbt.remove("areaMinY");
			nbt.remove("areaMinZ");
			nbt.remove("areaMaxX");
			nbt.remove("areaMaxY");
			nbt.remove("areaMaxZ");
		} else {
			this.area = new Box(
					nbt.getDouble("area_min_x"),
					nbt.getDouble("area_min_y"),
					nbt.getDouble("area_min_z"),
					nbt.getDouble("area_max_x"),
					nbt.getDouble("area_max_y"),
					nbt.getDouble("area_max_z")
			);
		}
		this.calculateAreaBox = true;

		if (nbt.contains("areaDimensionsX") || nbt.contains("areaDimensionsY") || nbt.contains("areaDimensionsZ")) {
			this.areaDimensions = new Vec3i(
					MathHelper.clamp(nbt.getInt("areaDimensionsX"), 0, 48),
					MathHelper.clamp(nbt.getInt("areaDimensionsY"), 0, 48),
					MathHelper.clamp(nbt.getInt("areaDimensionsZ"), 0, 48)
			);
			nbt.remove("areaDimensionsX");
			nbt.remove("areaDimensionsY");
			nbt.remove("areaDimensionsZ");
		} else {
			this.areaDimensions = new Vec3i(
					MathHelper.clamp(nbt.getInt("area_dimensions_x"), 0, 48),
					MathHelper.clamp(nbt.getInt("area_dimensions_y"), 0, 48),
					MathHelper.clamp(nbt.getInt("area_dimensions_z"), 0, 48)
			);
		}

		if (nbt.contains("areaPositionOffsetX") || nbt.contains("areaPositionOffsetY") || nbt.contains("areaPositionOffsetZ")) {
			this.areaPositionOffset = new BlockPos(
					MathHelper.clamp(nbt.getInt("areaPositionOffsetX"), -48, 48),
					MathHelper.clamp(nbt.getInt("areaPositionOffsetY"), -48, 48),
					MathHelper.clamp(nbt.getInt("areaPositionOffsetZ"), -48, 48)
			);
			nbt.remove("areaPositionOffsetX");
			nbt.remove("areaPositionOffsetY");
			nbt.remove("areaPositionOffsetZ");
		} else {
			this.areaPositionOffset = new BlockPos(
					MathHelper.clamp(nbt.getInt("area_position_offset_x"), -48, 48),
					MathHelper.clamp(nbt.getInt("area_position_offset_y"), -48, 48),
					MathHelper.clamp(nbt.getInt("area_position_offset_z"), -48, 48)
			);
		}

		if (nbt.contains("pvpControllerBlockPositionOffsetX") || nbt.contains("pvpControllerBlockPositionOffsetY") || nbt.contains("pvpControllerBlockPositionOffsetZ")) {
			this.pvpControllerBlockPositionOffset = new BlockPos(
					MathHelper.clamp(nbt.getInt("pvpControllerBlockPositionOffsetX"), -48, 48),
					MathHelper.clamp(nbt.getInt("pvpControllerBlockPositionOffsetY"), -48, 48),
					MathHelper.clamp(nbt.getInt("pvpControllerBlockPositionOffsetZ"), -48, 48)
			);
			nbt.remove("pvpControllerBlockPositionOffsetX");
			nbt.remove("pvpControllerBlockPositionOffsetY");
			nbt.remove("pvpControllerBlockPositionOffsetZ");
		} else {
			this.pvpControllerBlockPositionOffset = new BlockPos(
					MathHelper.clamp(nbt.getInt("pvp_controller_block_position_offset_x"), -48, 48),
					MathHelper.clamp(nbt.getInt("pvp_controller_block_position_offset_y"), -48, 48),
					MathHelper.clamp(nbt.getInt("pvp_controller_block_position_offset_z"), -48, 48)
			);
		}

		if (nbt.contains("teamIdentifier")) {
			this.teamIdentifier = nbt.getString("teamIdentifier");
			nbt.remove("teamIdentifier");
		} else {
			this.teamIdentifier = nbt.getString("team_identifier");
		}

		if (nbt.contains("displayNameString")) {
			this.displayNameString = nbt.getString("displayNameString");
			nbt.remove("displayNameString");
		} else {
			this.displayNameString = nbt.getString("display_name_string");
		}


		if (nbt.contains("teamColorIndex")) {
			this.setTeamColor(nbt.getInt("teamColorIndex"));
			nbt.remove("teamColorIndex");
		} else {
			this.setTeamColor(nbt.getInt("team_color_index"));
		}

		if (nbt.contains("friendlyFire")) {
			this.friendlyFire = nbt.getBoolean("friendlyFire");
			nbt.remove("friendlyFire");
		} else {
			this.friendlyFire = nbt.getBoolean("friendly_fire");
		}

		if (nbt.contains("showFriendlyInvisibles")) {
			this.showFriendlyInvisibles = nbt.getBoolean("showFriendlyInvisibles");
			nbt.remove("showFriendlyInvisibles");
		} else {
			this.showFriendlyInvisibles = nbt.getBoolean("show_friendly_invisibles");
		}

		if (nbt.contains("nametagVisibility")) {
			this.nametagVisibility = nbt.getString("nametagVisibility");
			nbt.remove("nametagVisibility");
		} else {
			this.nametagVisibility = nbt.getString("nametag_visibility");
		}

		if (nbt.contains("deathMessageVisibility")) {
			this.deathMessageVisibility = nbt.getString("deathMessageVisibility");
			nbt.remove("deathMessageVisibility");
		} else {
			this.deathMessageVisibility = nbt.getString("death_message_visibility");
		}

		if (nbt.contains("collisionRule")) {
			this.collisionRule = nbt.getString("collisionRule");
			nbt.remove("collisionRule");
		} else {
			this.collisionRule = nbt.getString("collision_rule");
		}

		if (nbt.contains("prefixString")) {
			this.prefixString = nbt.getString("prefixString");
			nbt.remove("prefixString");
		} else {
			this.prefixString = nbt.getString("prefix_string");
		}

		if (nbt.contains("suffixString")) {
			this.suffixString = nbt.getString("suffixString");
			nbt.remove("suffixString");
		} else {
			this.suffixString = nbt.getString("suffix_string");
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

	public static void tick(World world, BlockPos pos, BlockState state, TeamControllerBlockEntity teamControllerBlockEntity) {
		if (!world.isClient && world.getTime() % 80L == 0L) {
			if (teamControllerBlockEntity.calculateAreaBox || teamControllerBlockEntity.area == null) {
				BlockPos areaPositionOffset = teamControllerBlockEntity.areaPositionOffset;
				Vec3i areaDimensions = teamControllerBlockEntity.areaDimensions;
				Vec3d areaStart = new Vec3d(pos.getX() + areaPositionOffset.getX(), pos.getY() + areaPositionOffset.getY(), pos.getZ() + areaPositionOffset.getZ());
				Vec3d areaEnd = new Vec3d(areaStart.getX() + areaDimensions.getX(), areaStart.getY() + areaDimensions.getY(), areaStart.getZ() + areaDimensions.getZ());
				teamControllerBlockEntity.area = new Box(areaStart, areaEnd);
				teamControllerBlockEntity.calculateAreaBox = false;
				teamControllerBlockEntity.markDirty();
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
						((DuckPlayerEntityMixin) playerEntity).scriptblocks$setCurrentPVPControllerBlockPosition(Optional.of(pvpControllerBlockPos));
						pvpControllerBlockEntity.addPlayerAndTeam(teamControllerBlockEntity.team, playerEntity);
					}
				}
			}
		}
	}

	// region --- getter & setter ---
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

	public String getNametagVisibility() {
		return this.nametagVisibility;
	}

	public void setNametagVisibility(String nametagVisibility) {
		AbstractTeam.VisibilityRule var = AbstractTeam.VisibilityRule.getRule(nametagVisibility);
		if (var == null) {
			nametagVisibility = AbstractTeam.VisibilityRule.ALWAYS.name;
		}
		this.nametagVisibility = nametagVisibility;
	}

	public String getDeathMessageVisibility() {
		return this.deathMessageVisibility;
	}

	public void setDeathMessageVisibility(String deathMessageVisibility) {
		AbstractTeam.VisibilityRule var = AbstractTeam.VisibilityRule.getRule(deathMessageVisibility);
		if (var == null) {
			deathMessageVisibility = AbstractTeam.VisibilityRule.ALWAYS.name;
		}
		this.deathMessageVisibility = deathMessageVisibility;
	}

	public String getCollisionRule() {
		return this.collisionRule;
	}

	public void setCollisionRule(String collisionRule) {
		AbstractTeam.CollisionRule var = AbstractTeam.CollisionRule.getRule(collisionRule);
		if (var == null) {
			collisionRule = AbstractTeam.CollisionRule.ALWAYS.name;
		}
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
			if (state.get(RotatedBlockWithEntity.ROTATED) != this.rotated) {
				BlockRotation blockRotation = BlockRotationUtils.calculateRotationFromDifferentRotatedStates(state.get(RotatedBlockWithEntity.ROTATED), this.rotated);

				MutablePair<BlockPos, Vec3i> offsetArea = BlockRotationUtils.rotateOffsetArea(this.areaPositionOffset, this.areaDimensions, blockRotation);
				this.areaPositionOffset = offsetArea.getLeft();
				this.areaDimensions = offsetArea.getRight();

				this.rotated = state.get(RotatedBlockWithEntity.ROTATED);
			}
			if (state.get(RotatedBlockWithEntity.X_MIRRORED) != this.x_mirrored) {

				MutablePair<BlockPos, Vec3i> offsetArea = BlockRotationUtils.mirrorOffsetArea(this.areaPositionOffset, this.areaDimensions, BlockMirror.FRONT_BACK);
				this.areaPositionOffset = offsetArea.getLeft();
				this.areaDimensions = offsetArea.getRight();

				this.x_mirrored = state.get(RotatedBlockWithEntity.X_MIRRORED);
			}
			if (state.get(RotatedBlockWithEntity.Z_MIRRORED) != this.z_mirrored) {

				MutablePair<BlockPos, Vec3i> offsetArea = BlockRotationUtils.mirrorOffsetArea(this.areaPositionOffset, this.areaDimensions, BlockMirror.LEFT_RIGHT);
				this.areaPositionOffset = offsetArea.getLeft();
				this.areaDimensions = offsetArea.getRight();

				this.z_mirrored = state.get(RotatedBlockWithEntity.Z_MIRRORED);
			}
		}
	}
}
