package com.github.theredbrain.scriptblocks.block.entity;

import com.github.theredbrain.scriptblocks.block.Resetable;
import com.github.theredbrain.scriptblocks.block.RotatedBlockWithEntity;
import com.github.theredbrain.scriptblocks.block.Triggerable;
import com.github.theredbrain.scriptblocks.entity.player.DuckPlayerEntityMixin;
import com.github.theredbrain.scriptblocks.registry.ScriptBlocksEntities;
import com.github.theredbrain.scriptblocks.registry.ScriptBlocksStatusEffects;
import com.github.theredbrain.scriptblocks.util.BlockRotationUtils;
import com.github.theredbrain.scriptblocks.util.ScriptBlocksNBTHelper;
import com.github.theredbrain.scriptblocks.util.UUIDUtilities;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.scoreboard.Team;
import net.minecraft.text.Text;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.MutablePair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class HousingBlockEntity extends RotatedBlockEntity {
	private String ownerUuid = "";
	private boolean isOwnerSet;
	private final List<String> coOwnerList = new ArrayList<>(List.of());
	private final List<String> trustedList = new ArrayList<>(List.of());
	private final List<String> guestList = new ArrayList<>(List.of());

	private boolean calculateAreaBox = true;
	private Box area = null;
	private boolean showInfluenceArea = false;
	private Vec3i influenceAreaDimensions = Vec3i.ZERO;
	private BlockPos influenceAreaPositionOffset = new BlockPos(0, 1, 0);
	private OwnerMode ownerMode = OwnerMode.DIMENSION_OWNER;
	private MutablePair<BlockPos, Boolean> triggeredBlock = new MutablePair<>(new BlockPos(0, 0, 0), false);

	public HousingBlockEntity(BlockPos pos, BlockState state) {
		super(ScriptBlocksEntities.HOUSING_BLOCK_ENTITY, pos, state);
		this.isOwnerSet = false;
	}

	// TODO
	@Override
	protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {

		nbt.putString("owner_uuid", this.ownerUuid);

		nbt.putBoolean("is_owner_set", isOwnerSet);

		NbtList nbtList = new NbtList();
		for (String entry : this.coOwnerList) {
			nbtList.add(NbtString.of(entry));
		}
		nbt.put("co_owners", nbtList.copy());

		nbtList = new NbtList();
		for (String entry : this.trustedList) {
			nbtList.add(NbtString.of(entry));
		}
		nbt.put("trusted_people", nbtList.copy());

		nbtList = new NbtList();
		for (String entry : this.guestList) {
			nbtList.add(NbtString.of(entry));
		}
		nbt.put("guests", nbtList);

		NbtCompound nbtCompound = new NbtCompound();
		nbtCompound.putBoolean("visible", this.showInfluenceArea);
		nbtCompound.put("position_offset", NbtHelper.fromBlockPos(this.influenceAreaPositionOffset));
		nbtCompound.put("dimensions", ScriptBlocksNBTHelper.fromVec3i(this.influenceAreaDimensions));
		nbt.put("influence_area", nbtCompound.copy());

		nbt.putString("owner_mode", this.ownerMode.asString());

		nbtCompound = new NbtCompound();
		nbtCompound.put("position_offset", NbtHelper.fromBlockPos(this.triggeredBlock.getLeft()));
		nbtCompound.putBoolean("resets", this.triggeredBlock.getRight());
		nbt.put("triggered_block", nbtCompound);

		super.writeNbt(nbt, registryLookup);
	}

	@Override
	protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {

		if (nbt.contains("ownerUuid")) {
			this.ownerUuid = nbt.getString("ownerUuid");
			nbt.remove("ownerUuid");
		} else {
			this.ownerUuid = nbt.getString("owner_uuid");
		}

		if (nbt.contains("isOwnerSet")) {
			this.isOwnerSet = nbt.getBoolean("isOwnerSet");
			nbt.remove("isOwnerSet");
		} else {
			this.isOwnerSet = nbt.getBoolean("is_owner_set");
		}

		this.coOwnerList.clear();
		if (nbt.contains("coOwnerListSize")) {
			int coOwnerListSize = nbt.getInt("coOwnerListSize");
			for (int i = 0; i < coOwnerListSize; i++) {
				this.coOwnerList.add(nbt.getString("coOwnerListEntry" + i));
				nbt.remove("coOwnerListEntry" + i);
			}
			nbt.remove("coOwnerListSize");
		} else {
			NbtList nbtList = nbt.getList("co_owners", NbtElement.STRING_TYPE);
			for (NbtElement nbtElement : nbtList) {
				this.coOwnerList.add(nbtElement.asString());
			}
		}

		this.trustedList.clear();
		if (nbt.contains("trustedListSize")) {
			int trustedListSize = nbt.getInt("trustedListSize");
			for (int i = 0; i < trustedListSize; i++) {
				this.trustedList.add(nbt.getString("trustedListEntry" + i));
				nbt.remove("trustedListEntry" + i);
			}
			nbt.remove("trustedListSize");
		} else {
			NbtList nbtList = nbt.getList("trusted_people", NbtElement.STRING_TYPE);
			for (NbtElement nbtElement : nbtList) {
				this.trustedList.add(nbtElement.asString());
			}
		}

		this.guestList.clear();
		if (nbt.contains("guestListSize")) {
			int guestListSize = nbt.getInt("guestListSize");
			for (int i = 0; i < guestListSize; i++) {
				this.guestList.add(nbt.getString("guestListEntry" + i));
				nbt.remove("guestListEntry" + i);
			}
			nbt.remove("guestListSize");
		} else {
			NbtList nbtList = nbt.getList("guests", NbtElement.STRING_TYPE);
			for (NbtElement nbtElement : nbtList) {
				this.guestList.add(nbtElement.asString());
			}
		}

		if (nbt.contains("areaMinX") || nbt.contains("areaMinY") || nbt.contains("areaMinZ") || nbt.contains("areaMaxX") || nbt.contains("areaMaxY") || nbt.contains("areaMaxZ")) {
			this.area = new Box(nbt.getDouble("areaMinX"), nbt.getDouble("areaMinY"), nbt.getDouble("areaMinZ"), nbt.getDouble("areaMaxX"), nbt.getDouble("areaMaxY"), nbt.getDouble("areaMaxZ"));
			this.calculateAreaBox = true;
			nbt.remove("areaMinX");
			nbt.remove("areaMinY");
			nbt.remove("areaMinZ");
			nbt.remove("areaMaxX");
			nbt.remove("areaMaxY");
			nbt.remove("areaMaxZ");
		}

		NbtCompound nbtCompound = nbt.getCompound("influence_area");
		if (nbt.contains("showInfluenceArea")) {
			this.showInfluenceArea = nbt.getBoolean("showInfluenceArea");
			nbt.remove("showInfluenceArea");
		} else {
			this.showInfluenceArea = nbtCompound.getBoolean("visible");
		}

		if (nbt.contains("influenceAreaDimensionsX") || nbt.contains("influenceAreaDimensionsY") || nbt.contains("influenceAreaDimensionsZ")) {
			this.influenceAreaDimensions = new Vec3i(
					MathHelper.clamp(nbt.getInt("influenceAreaDimensionsX"), 0, 48),
					MathHelper.clamp(nbt.getInt("influenceAreaDimensionsY"), 0, 48),
					MathHelper.clamp(nbt.getInt("influenceAreaDimensionsZ"), 0, 48)
			);
			nbt.remove("influenceAreaDimensionsX");
			nbt.remove("influenceAreaDimensionsY");
			nbt.remove("influenceAreaDimensionsZ");
		} else {
			this.influenceAreaDimensions = ScriptBlocksNBTHelper.toVec3i(nbtCompound, "dimensions").orElse(new Vec3i(0, 0, 0));
		}

		if (nbt.contains("influenceAreaPositionOffsetX") || nbt.contains("influenceAreaPositionOffsetY") || nbt.contains("influenceAreaPositionOffsetZ")) {
			this.influenceAreaPositionOffset = new BlockPos(
					MathHelper.clamp(nbt.getInt("influenceAreaPositionOffsetX"), -48, 48),
					MathHelper.clamp(nbt.getInt("influenceAreaPositionOffsetY"), -48, 48),
					MathHelper.clamp(nbt.getInt("influenceAreaPositionOffsetZ"), -48, 48)
			);
			nbt.remove("influenceAreaPositionOffsetX");
			nbt.remove("influenceAreaPositionOffsetY");
			nbt.remove("influenceAreaPositionOffsetZ");
		} else {
			this.influenceAreaPositionOffset = BlockPos.max(
					BlockPos.min(
							NbtHelper.toBlockPos(nbtCompound, "position_offset").orElse(BlockPos.ORIGIN),
							new BlockPos(48, 48, 48)
					),
					new BlockPos(-48, -48, -48)
			);
		}

		if (nbt.contains("ownerMode")) {
			this.ownerMode = OwnerMode.byName(nbt.getString("ownerMode")).orElseGet(() -> OwnerMode.DIMENSION_OWNER);
			nbt.remove("ownerMode");
		} else {
			this.ownerMode = OwnerMode.byName(nbt.getString("owner_mode")).orElseGet(() -> OwnerMode.DIMENSION_OWNER);
		}

		if (nbt.contains("triggeredBlockPositionOffsetX") || nbt.contains("triggeredBlockPositionOffsetY") || nbt.contains("triggeredBlockPositionOffsetZ") || nbt.contains("triggeredBlockResets")) {
			this.triggeredBlock = new MutablePair<>(
					new BlockPos(
							MathHelper.clamp(nbt.getInt("triggeredBlockPositionOffsetX"), -48, 48),
							MathHelper.clamp(nbt.getInt("triggeredBlockPositionOffsetY"), -48, 48),
							MathHelper.clamp(nbt.getInt("triggeredBlockPositionOffsetZ"), -48, 48)
					),
					nbt.getBoolean("triggeredBlockResets")
			);
			nbt.remove("triggeredBlockPositionOffsetX");
			nbt.remove("triggeredBlockPositionOffsetY");
			nbt.remove("triggeredBlockPositionOffsetZ");
			nbt.remove("triggeredBlockResets");
		} else if (nbt.contains("triggered_block_position_offset_x") || nbt.contains("triggered_block_position_offset_y") || nbt.contains("triggered_block_position_offset_z")) {
			this.triggeredBlock = new MutablePair<>(
					new BlockPos(
							MathHelper.clamp(nbt.getInt("triggered_block_position_offset_x"), -48, 48),
							MathHelper.clamp(nbt.getInt("triggered_block_position_offset_y"), -48, 48),
							MathHelper.clamp(nbt.getInt("triggered_block_position_offset_z"), -48, 48)
					),
					nbt.getBoolean("triggered_block_resets")
			);
			nbt.remove("triggered_block_position_offset_x");
			nbt.remove("triggered_block_position_offset_y");
			nbt.remove("triggered_block_position_offset_z");
			nbt.remove("triggered_block_resets");
		} else {
			nbtCompound = nbt.getCompound("triggered_block");
			this.triggeredBlock = new MutablePair<>(
					BlockPos.max(
							BlockPos.min(
									NbtHelper.toBlockPos(nbtCompound, "position_offset").orElse(BlockPos.ORIGIN),
									new BlockPos(48, 48, 48)
							),
							new BlockPos(-48, -48, -48)
					),
					nbtCompound.getBoolean("resets")
			);
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

	public static void tick(World world, BlockPos pos, BlockState state, HousingBlockEntity housingBlockEntity) {
		if (!world.isClient && world.getTime() % 20L == 0L) {
			Team ownerTeam = null;
			if (housingBlockEntity.hasWorld() && !housingBlockEntity.isOwnerSet && housingBlockEntity.ownerMode == OwnerMode.DIMENSION_OWNER) {
				housingBlockEntity.ownerUuid = initOwner(housingBlockEntity.world);
				if (UUIDUtilities.isStringValidUUID(housingBlockEntity.ownerUuid)) {
					housingBlockEntity.isOwnerSet = true;
					PlayerEntity owner = world.getPlayerByUuid(UUID.fromString(housingBlockEntity.ownerUuid));
					if (owner != null) {
						ownerTeam = owner.getScoreboardTeam();
					}
				}
				housingBlockEntity.markDirty();
			}

			if (housingBlockEntity.calculateAreaBox || housingBlockEntity.area == null) {

				housingBlockEntity.area = new Box(
						housingBlockEntity.pos.getX() + housingBlockEntity.influenceAreaPositionOffset.getX(),
						housingBlockEntity.pos.getY() + housingBlockEntity.influenceAreaPositionOffset.getY(),
						housingBlockEntity.pos.getZ() + housingBlockEntity.influenceAreaPositionOffset.getZ(),
						housingBlockEntity.pos.getX() + housingBlockEntity.influenceAreaPositionOffset.getX() + housingBlockEntity.influenceAreaDimensions.getX(),
						housingBlockEntity.pos.getY() + housingBlockEntity.influenceAreaPositionOffset.getY() + housingBlockEntity.influenceAreaDimensions.getY(),
						housingBlockEntity.pos.getZ() + housingBlockEntity.influenceAreaPositionOffset.getZ() + housingBlockEntity.influenceAreaDimensions.getZ()
				);
				housingBlockEntity.calculateAreaBox = false;
				housingBlockEntity.markDirty();
			}
			List<PlayerEntity> list = world.getNonSpectatingEntities(PlayerEntity.class, housingBlockEntity.area);
			Iterator<PlayerEntity> var11 = list.iterator();

			PlayerEntity playerEntity;
			while (var11.hasNext()) {
				playerEntity = var11.next();

				String playerName = playerEntity.getName().getString();
				String playerUuid = playerEntity.getUuidAsString();
				if (Objects.equals(playerUuid, housingBlockEntity.getOwnerUuid())) {
					playerEntity.addStatusEffect(new StatusEffectInstance(ScriptBlocksStatusEffects.HOUSING_OWNER_EFFECT, 100, 0, true, false, false));
				} else if (housingBlockEntity.getCoOwnerList().contains(playerName)) {
					playerEntity.addStatusEffect(new StatusEffectInstance(ScriptBlocksStatusEffects.HOUSING_CO_OWNER_EFFECT, 100, 0, true, false, false));
				} else if (housingBlockEntity.getTrustedList().contains(playerName)) {
					playerEntity.addStatusEffect(new StatusEffectInstance(ScriptBlocksStatusEffects.HOUSING_TRUSTED_EFFECT, 100, 0, true, false, false));
				} else if (housingBlockEntity.getGuestList().contains(playerName) || (ownerTeam != null && ownerTeam.getPlayerList().contains(playerName))) {
					playerEntity.addStatusEffect(new StatusEffectInstance(ScriptBlocksStatusEffects.HOUSING_GUEST_EFFECT, 100, 0, true, false, false));
				} else {
					playerEntity.addStatusEffect(new StatusEffectInstance(ScriptBlocksStatusEffects.HOUSING_STRANGER_EFFECT, 100, 0, true, false, false));
				}
				((DuckPlayerEntityMixin) playerEntity).scriptblocks$setCurrentHousingBlockPosition(Optional.of(housingBlockEntity.pos));
			}
		}
	}

	// region --- getter & setter ---
	public String getOwnerUuid() {
		return this.ownerUuid;
	}

	public boolean setOwnerUuid(String ownerUuid) {
		if (ownerUuid.isEmpty()) {
			this.ownerUuid = ownerUuid;
			return true;
		}
		if (UUIDUtilities.isStringValidUUID(ownerUuid)) {
			this.ownerUuid = ownerUuid;
			return true;
		}
		return false;
	}

	public List<String> getCoOwnerList() {
		return this.coOwnerList;
	}

	public void setCoOwnerList(List<String> coOwnerList) {
		this.coOwnerList.clear();
		this.coOwnerList.addAll(coOwnerList);
	}

	public List<String> getTrustedList() {
		return this.trustedList;
	}

	public void setTrustedList(List<String> trustedList) {
		this.trustedList.clear();
		this.trustedList.addAll(trustedList);
	}

	public List<String> getGuestList() {
		return this.guestList;
	}

	public void setGuestList(List<String> guestList) {
		this.guestList.clear();
		this.guestList.addAll(guestList);
	}

	public boolean getShowInfluenceArea() {
		return this.showInfluenceArea;
	}

	public void setShowInfluenceArea(boolean showInfluenceArea) {
		this.showInfluenceArea = showInfluenceArea;
	}

	public Vec3i getInfluenceAreaDimensions() {
		return this.influenceAreaDimensions;
	}

	public void setInfluenceAreaDimensions(Vec3i influenceAreaDimensions) {
		this.influenceAreaDimensions = new BlockPos(influenceAreaDimensions);
	}

	public BlockPos getRestrictBlockBreakingAreaPositionOffset() {
		return this.influenceAreaPositionOffset;
	}

	public void setRestrictBlockBreakingAreaPositionOffset(BlockPos influenceAreaPositionOffset) {
		this.influenceAreaPositionOffset = new BlockPos(influenceAreaPositionOffset);
	}

	public MutablePair<BlockPos, Boolean> getTriggeredBlock() {
		return this.triggeredBlock;
	}

	public void setTriggeredBlock(MutablePair<BlockPos, Boolean> triggeredBlock) {
		this.triggeredBlock = triggeredBlock;
	}

	public OwnerMode getOwnerMode() {
		return this.ownerMode;
	}

	public void setOwnerMode(OwnerMode ownerMode) {
		this.ownerMode = ownerMode;
	}

	public boolean isOwnerSet() {
		return this.isOwnerSet;
	}

	public void setIsOwnerSet(boolean isOwnerSet) {
		this.isOwnerSet = isOwnerSet;
	}
	// endregion --- getter & setter ---

	private static String initOwner(World world) {
		if (world != null) {
			String worldRegistryKey = world.getRegistryKey().getValue().getPath();
			String[] parts = worldRegistryKey.split("_");
			String uuidString = parts[0];
			if (UUIDUtilities.isStringValidUUID(uuidString)) {
				return uuidString;
			}
		}
		return "";
	}

	public boolean influenceAreaContains(BlockPos pos) {
		return (double) (pos.getX() + 1) > (this.pos.getX() + this.influenceAreaPositionOffset.getX())
				&& (double) pos.getX() < (this.pos.getX() + this.influenceAreaPositionOffset.getX() + this.influenceAreaDimensions.getX())
				&& (double) (pos.getY() + 1) > (this.pos.getY() + this.influenceAreaPositionOffset.getY())
				&& (double) pos.getY() < (this.pos.getY() + this.influenceAreaPositionOffset.getY() + this.influenceAreaDimensions.getY())
				&& (double) (pos.getZ() + 1) > (this.pos.getZ() + this.influenceAreaPositionOffset.getZ())
				&& (double) pos.getZ() < (this.pos.getZ() + this.influenceAreaPositionOffset.getZ() + this.influenceAreaDimensions.getZ());
	}

	public void trigger() {
		if (this.world != null) {
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
	}

	@Override
	protected void onRotate(BlockState state) {
		if (state.getBlock() instanceof RotatedBlockWithEntity) {
			if (state.get(RotatedBlockWithEntity.ROTATED) != this.rotated) {
				BlockRotation blockRotation = BlockRotationUtils.calculateRotationFromDifferentRotatedStates(state.get(RotatedBlockWithEntity.ROTATED), this.rotated);
				this.triggeredBlock.setLeft(BlockRotationUtils.rotateOffsetBlockPos(this.triggeredBlock.getLeft(), blockRotation));
				MutablePair<BlockPos, Vec3i> offsetArea = BlockRotationUtils.rotateOffsetArea(this.influenceAreaPositionOffset, this.influenceAreaDimensions, blockRotation);
				this.influenceAreaPositionOffset = offsetArea.getLeft();
				this.influenceAreaDimensions = offsetArea.getRight();
				this.rotated = state.get(RotatedBlockWithEntity.ROTATED);
			}
			if (state.get(RotatedBlockWithEntity.X_MIRRORED) != this.x_mirrored) {
				this.triggeredBlock.setLeft(BlockRotationUtils.mirrorOffsetBlockPos(this.triggeredBlock.getLeft(), BlockMirror.FRONT_BACK));
				MutablePair<BlockPos, Vec3i> offsetArea = BlockRotationUtils.mirrorOffsetArea(this.influenceAreaPositionOffset, this.influenceAreaDimensions, BlockMirror.FRONT_BACK);
				this.influenceAreaPositionOffset = offsetArea.getLeft();
				this.influenceAreaDimensions = offsetArea.getRight();
				this.x_mirrored = state.get(RotatedBlockWithEntity.X_MIRRORED);
			}
			if (state.get(RotatedBlockWithEntity.Z_MIRRORED) != this.z_mirrored) {
				this.triggeredBlock.setLeft(BlockRotationUtils.mirrorOffsetBlockPos(this.triggeredBlock.getLeft(), BlockMirror.LEFT_RIGHT));
				MutablePair<BlockPos, Vec3i> offsetArea = BlockRotationUtils.mirrorOffsetArea(this.influenceAreaPositionOffset, this.influenceAreaDimensions, BlockMirror.LEFT_RIGHT);
				this.influenceAreaPositionOffset = offsetArea.getLeft();
				this.influenceAreaDimensions = offsetArea.getRight();
				this.z_mirrored = state.get(RotatedBlockWithEntity.Z_MIRRORED);
			}
		}
	}

	public enum OwnerMode implements StringIdentifiable {
		DIMENSION_OWNER("dimension_owner"),
		INTERACTION("interaction");

		private final String name;

		OwnerMode(String name) {
			this.name = name;
		}

		@Override
		public String asString() {
			return this.name;
		}

		public static Optional<OwnerMode> byName(String name) {
			return Arrays.stream(OwnerMode.values()).filter(ownerMode -> ownerMode.asString().equals(name)).findFirst();
		}

		public Text asText() {
			return Text.translatable("gui.housing_block.ownerMode." + this.name);
		}
	}
}
