package com.github.theredbrain.scriptblocks.block.entity;

import com.github.theredbrain.scriptblocks.block.Resetable;
import com.github.theredbrain.scriptblocks.block.RotatedBlockWithEntity;
import com.github.theredbrain.scriptblocks.block.Triggerable;
import com.github.theredbrain.scriptblocks.entity.player.DuckPlayerEntityMixin;
import com.github.theredbrain.scriptblocks.registry.EntityRegistry;
import com.github.theredbrain.scriptblocks.util.BlockRotationUtils;
import net.minecraft.block.BlockState;
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

		if (this.showArea) {
			nbt.putBoolean("showArea", true);
		} else {
			nbt.remove("showArea");
		}

		if (this.area != null) {
			nbt.putDouble("areaMinX", this.area.minX);
			nbt.putDouble("areaMaxX", this.area.maxX);
			nbt.putDouble("areaMinY", this.area.minY);
			nbt.putDouble("areaMaxY", this.area.maxY);
			nbt.putDouble("areaMinZ", this.area.minZ);
			nbt.putDouble("areaMaxZ", this.area.maxZ);
		} else {
			nbt.remove("areaMinX");
			nbt.remove("areaMaxX");
			nbt.remove("areaMinY");
			nbt.remove("areaMaxY");
			nbt.remove("areaMinZ");
			nbt.remove("areaMaxZ");
		}

		if (this.areaDimensions.getX() != 0) {
			nbt.putInt("areaDimensionsX", this.areaDimensions.getX());
		} else {
			nbt.remove("areaDimensionsX");
		}

		if (this.areaDimensions.getY() != 0) {
			nbt.putInt("areaDimensionsY", this.areaDimensions.getY());
		} else {
			nbt.remove("areaDimensionsY");
		}

		if (this.areaDimensions.getZ() != 0) {
			nbt.putInt("areaDimensionsZ", this.areaDimensions.getZ());
		} else {
			nbt.remove("areaDimensionsZ");
		}

		if (this.areaPositionOffset.getX() != 0) {
			nbt.putInt("areaPositionOffsetX", this.areaPositionOffset.getX());
		} else {
			nbt.remove("areaPositionOffsetX");
		}

		if (this.areaPositionOffset.getY() != 0) {
			nbt.putInt("areaPositionOffsetY", this.areaPositionOffset.getY());
		} else {
			nbt.remove("areaPositionOffsetY");
		}

		if (this.areaPositionOffset.getZ() != 0) {
			nbt.putInt("areaPositionOffsetZ", this.areaPositionOffset.getZ());
		} else {
			nbt.remove("areaPositionOffsetZ");
		}

		if (this.pvpControllerBlockPositionOffset.getX() != 0) {
			nbt.putInt("pvpControllerBlockPositionOffsetX", this.pvpControllerBlockPositionOffset.getX());
		} else {
			nbt.remove("pvpControllerBlockPositionOffsetX");
		}

		if (this.pvpControllerBlockPositionOffset.getY() != 0) {
			nbt.putInt("pvpControllerBlockPositionOffsetY", this.pvpControllerBlockPositionOffset.getY());
		} else {
			nbt.remove("pvpControllerBlockPositionOffsetY");
		}

		if (this.pvpControllerBlockPositionOffset.getZ() != 0) {
			nbt.putInt("pvpControllerBlockPositionOffsetZ", this.pvpControllerBlockPositionOffset.getZ());
		} else {
			nbt.remove("pvpControllerBlockPositionOffsetZ");
		}

		nbt.putString("teamIdentifier", this.teamIdentifier);

		nbt.putString("displayNameString", this.displayNameString);

		nbt.putInt("teamColorIndex", this.getTeamColorIndex());

		if (this.friendlyFire) {
			nbt.putBoolean("friendlyFire", true);
		} else {
			nbt.remove("friendlyFire");
		}

		if (this.showFriendlyInvisibles) {
			nbt.putBoolean("showFriendlyInvisibles", true);
		} else {
			nbt.remove("showFriendlyInvisibles");
		}

		nbt.putString("nametagVisibility", this.nametagVisibility);

		nbt.putString("deathMessageVisibility", this.deathMessageVisibility);

		nbt.putString("collisionRule", this.collisionRule);

		nbt.putString("prefixString", this.prefixString);

		nbt.putString("suffixString", this.suffixString);

		super.writeNbt(nbt, registryLookup);

	}

	@Override
	protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {

		this.showArea = nbt.getBoolean("showArea");

		if (nbt.contains("areaMinX") && nbt.contains("areaMinY") && nbt.contains("areaMinZ") && nbt.contains("areaMaxX") && nbt.contains("areaMaxY") && nbt.contains("areaMaxZ")) {
			this.area = new Box(nbt.getDouble("areaMinX"), nbt.getDouble("areaMinY"), nbt.getDouble("areaMinZ"), nbt.getDouble("areaMaxX"), nbt.getDouble("areaMaxY"), nbt.getDouble("areaMaxZ"));
			this.calculateAreaBox = true;
		}

		int i = MathHelper.clamp(nbt.getInt("areaDimensionsX"), 0, 48);
		int j = MathHelper.clamp(nbt.getInt("areaDimensionsY"), 0, 48);
		int k = MathHelper.clamp(nbt.getInt("areaDimensionsZ"), 0, 48);
		this.areaDimensions = new Vec3i(i, j, k);

		int l = MathHelper.clamp(nbt.getInt("areaPositionOffsetX"), -48, 48);
		int m = MathHelper.clamp(nbt.getInt("areaPositionOffsetY"), -48, 48);
		int n = MathHelper.clamp(nbt.getInt("areaPositionOffsetZ"), -48, 48);
		this.areaPositionOffset = new BlockPos(l, m, n);

		this.pvpControllerBlockPositionOffset = new BlockPos(
				MathHelper.clamp(nbt.getInt("pvpControllerBlockPositionOffsetX"), -48, 48),
				MathHelper.clamp(nbt.getInt("pvpControllerBlockPositionOffsetY"), -48, 48),
				MathHelper.clamp(nbt.getInt("pvpControllerBlockPositionOffsetZ"), -48, 48)
		);

		this.teamIdentifier = nbt.getString("teamIdentifier");

		this.displayNameString = nbt.getString("displayNameString");

		this.setTeamColor(nbt.getInt("teamColorIndex"));

		this.friendlyFire = nbt.getBoolean("friendlyFire");

		this.showFriendlyInvisibles = nbt.getBoolean("showFriendlyInvisibles");

		this.nametagVisibility = nbt.getString("nametagVisibility");

		this.deathMessageVisibility = nbt.getString("deathMessageVisibility");

		this.collisionRule = nbt.getString("collisionRule");

		this.prefixString = nbt.getString("prefixString");

		this.suffixString = nbt.getString("suffixString");

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
			}
			if (teamControllerBlockEntity.team != null && teamControllerBlockEntity.world != null && !teamControllerBlockEntity.world.isClient) {
				BlockPos pvpControllerBlockPos = null;
				PVPControllerBlockEntity pvpControllerBlockEntity = null;
				if (teamControllerBlockEntity.pvpControllerBlockPositionOffset != BlockPos.ORIGIN) {
					BlockPos pvpControllerBlockPositionOffset = teamControllerBlockEntity.pvpControllerBlockPositionOffset;
					pvpControllerBlockPos = teamControllerBlockEntity.pos.add(pvpControllerBlockPositionOffset.getX(), pvpControllerBlockPositionOffset.getY(), pvpControllerBlockPositionOffset.getZ());
					if (teamControllerBlockEntity.world.getBlockEntity(pvpControllerBlockPos) instanceof PVPControllerBlockEntity pvpControllerBlockEntity1) {
						pvpControllerBlockEntity = pvpControllerBlockEntity1;
					}
				}
				List<LivingEntity> livingEntityList = world.getNonSpectatingEntities(LivingEntity.class, teamControllerBlockEntity.area);
				for (LivingEntity livingEntity : livingEntityList) {
					teamControllerBlockEntity.world.getScoreboard().addScoreHolderToTeam(livingEntity.getNameForScoreboard(), teamControllerBlockEntity.team);
					if (livingEntity instanceof PlayerEntity playerEntity && pvpControllerBlockPos != null && pvpControllerBlockEntity != null) {
						((DuckPlayerEntityMixin)playerEntity).scriptblocks$setCurrentPVPControllerBlockPosition(Optional.of(pvpControllerBlockPos));
						pvpControllerBlockEntity.addPlayerAndTeam(teamControllerBlockEntity.team, playerEntity);
					}
				}
			}
		}
	}

	//region --- getter & setter ---
	public boolean showArea() {
		return showArea;
	}

	public void setShowArea(boolean showArea) {
		this.showArea = showArea;
	}

	public Vec3i getAreaDimensions() {
		return areaDimensions;
	}

	// TODO check if input is valid
	public boolean setAreaDimensions(Vec3i areaDimensions) {
		this.areaDimensions = areaDimensions;
		this.calculateAreaBox = true;
		return true;
	}

	public BlockPos getAreaPositionOffset() {
		return areaPositionOffset;
	}

	// TODO check if input is valid
	public boolean setAreaPositionOffset(BlockPos areaPositionOffset) {
		this.areaPositionOffset = areaPositionOffset;
		this.calculateAreaBox = true;
		return true;
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
	//endregion --- getter & setter ---

	@Override
	public void reset() {
		if (this.team != null) {
			this.team.getScoreboard().removeTeam(this.team);
			this.team = null;
		}
	}

	@Override
	public void trigger() {
		if (this.team == null && this.world != null && !this.world.isClient) {
			this.world.getScoreboard().addTeam(this.teamIdentifier);

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
