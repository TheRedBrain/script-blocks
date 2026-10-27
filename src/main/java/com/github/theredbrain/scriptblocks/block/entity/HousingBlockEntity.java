package com.github.theredbrain.scriptblocks.block.entity;

import com.github.theredbrain.scriptblocks.block.Resetable;
import com.github.theredbrain.scriptblocks.block.RotatedBlockWithEntity;
import com.github.theredbrain.scriptblocks.block.Triggerable;
import com.github.theredbrain.scriptblocks.registry.ScriptBlocksEntities;
import com.github.theredbrain.scriptblocks.registry.ScriptBlocksStatusEffects;
import com.github.theredbrain.scriptblocks.util.BlockRotationUtils;
import com.github.theredbrain.scriptblocks.util.DataAttachmentHelper;
import com.github.theredbrain.scriptblocks.util.ScriptBlocksBlockProperties;
import com.github.theredbrain.scriptblocks.util.ScriptBlocksNBTHelper;
import com.github.theredbrain.scriptblocks.util.UUIDUtilities;
import com.mojang.serialization.Codec;
import org.apache.commons.lang3.tuple.MutablePair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.scores.PlayerTeam;

public class HousingBlockEntity extends RotatedBlockEntity {
	private String ownerUuid = "";
	private boolean isOwnerSet;
	private final List<String> coOwnerList = new ArrayList<>(List.of());
	private final List<String> trustedList = new ArrayList<>(List.of());
	private final List<String> guestList = new ArrayList<>(List.of());

	private boolean calculateAreaBox = true;
	private AABB area = null;
	private boolean showInfluenceArea = false;
	private Vec3i influenceAreaDimensions = Vec3i.ZERO;
	private BlockPos influenceAreaPositionOffset = new BlockPos(0, 1, 0);
	private OwnerMode ownerMode = OwnerMode.DIMENSION_OWNER;
	private MutablePair<BlockPos, Boolean> triggeredBlock = new MutablePair<>(new BlockPos(0, 0, 0), false);

	public HousingBlockEntity(BlockPos pos, BlockState state) {
		super(ScriptBlocksEntities.HOUSING_BLOCK_ENTITY, pos, state);
		this.isOwnerSet = false;
	}

	@Override
	protected void saveAdditional(ValueOutput view) {

		view.putString("owner_uuid", this.ownerUuid);

		view.putBoolean("is_owner_set", isOwnerSet);

		ValueOutput.TypedOutputList<String> listAppender = view.list("co_owners", Codec.STRING);
		for (String string : this.coOwnerList) {
			listAppender.add(string);
		}

		ValueOutput.TypedOutputList<String> listAppender1 = view.list("trusted_people", Codec.STRING);
		for (String string : this.trustedList) {
			listAppender1.add(string);
		}

		ValueOutput.TypedOutputList<String> listAppender2 = view.list("guests", Codec.STRING);
		for (String string : this.guestList) {
			listAppender2.add(string);
		}

		CompoundTag nbtCompound = new CompoundTag();
		nbtCompound.putBoolean("visible", this.showInfluenceArea);
		nbtCompound.store("position_offset", BlockPos.CODEC, this.influenceAreaPositionOffset);
		nbtCompound.put("dimensions", ScriptBlocksNBTHelper.fromVec3i(this.influenceAreaDimensions));
		view.store("influence_area", CompoundTag.CODEC, nbtCompound.copy());

		view.putString("owner_mode", this.ownerMode.getSerializedName());

		nbtCompound = new CompoundTag();
		nbtCompound.store("position_offset", BlockPos.CODEC, this.triggeredBlock.getLeft());
		nbtCompound.putBoolean("resets", this.triggeredBlock.getRight());
		view.store("triggered_block", CompoundTag.CODEC, nbtCompound.copy());

		super.saveAdditional(view);
	}

	@Override
	protected void loadAdditional(ValueInput view) {

		this.ownerUuid = view.getStringOr("owner_uuid", "");

		this.isOwnerSet = view.getBooleanOr("is_owner_set", false);

		this.coOwnerList.clear();
		for (String string : view.listOrEmpty("co_owners", Codec.STRING)) {
			this.coOwnerList.add(string);
		}

		this.trustedList.clear();
		for (String string : view.listOrEmpty("trusted_people", Codec.STRING)) {
			this.trustedList.add(string);
		}

		this.guestList.clear();
		for (String string : view.listOrEmpty("guests", Codec.STRING)) {
			this.guestList.add(string);
		}

		CompoundTag nbtCompound = view.read("influence_area", CompoundTag.CODEC).orElseGet(CompoundTag::new);
		this.showInfluenceArea = nbtCompound.getBooleanOr("visible", false);

		this.influenceAreaDimensions = ScriptBlocksNBTHelper.toVec3i(nbtCompound, "dimensions").orElse(new Vec3i(0, 0, 0));

		this.influenceAreaPositionOffset = BlockPos.max(
				BlockPos.min(
						nbtCompound.read("position_offset", BlockPos.CODEC).orElse(BlockPos.ZERO),
						new BlockPos(48, 48, 48)
				),
				new BlockPos(-48, -48, -48)
		);

		this.ownerMode = OwnerMode.byName(view.getStringOr("owner_mode", "")).orElseGet(() -> OwnerMode.DIMENSION_OWNER);

		nbtCompound = view.read("triggered_block", CompoundTag.CODEC).orElseGet(CompoundTag::new);
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

		super.loadAdditional(view);
	}

	public ClientboundBlockEntityDataPacket getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}

	@Override
	public CompoundTag getUpdateTag(HolderLookup.Provider registryLookup) {
		return this.saveCustomOnly(registryLookup);
	}

	public static void tick(Level world, BlockPos pos, BlockState state, HousingBlockEntity housingBlockEntity) {
		if (!world.isClientSide() && world.getGameTime() % 20L == 0L) {
			PlayerTeam ownerTeam = null;
			if (housingBlockEntity.hasLevel() && !housingBlockEntity.isOwnerSet && housingBlockEntity.ownerMode == OwnerMode.DIMENSION_OWNER) {
				housingBlockEntity.ownerUuid = initOwner(housingBlockEntity.level);
				if (UUIDUtilities.isStringValidUUID(housingBlockEntity.ownerUuid)) {
					housingBlockEntity.isOwnerSet = true;
					Player owner = world.getPlayerByUUID(UUID.fromString(housingBlockEntity.ownerUuid));
					if (owner != null) {
						ownerTeam = owner.getTeam();
					}
				}
				housingBlockEntity.setChanged();
			}

			if (housingBlockEntity.calculateAreaBox || housingBlockEntity.area == null) {

				housingBlockEntity.area = new AABB(
						housingBlockEntity.worldPosition.getX() + housingBlockEntity.influenceAreaPositionOffset.getX(),
						housingBlockEntity.worldPosition.getY() + housingBlockEntity.influenceAreaPositionOffset.getY(),
						housingBlockEntity.worldPosition.getZ() + housingBlockEntity.influenceAreaPositionOffset.getZ(),
						housingBlockEntity.worldPosition.getX() + housingBlockEntity.influenceAreaPositionOffset.getX() + housingBlockEntity.influenceAreaDimensions.getX(),
						housingBlockEntity.worldPosition.getY() + housingBlockEntity.influenceAreaPositionOffset.getY() + housingBlockEntity.influenceAreaDimensions.getY(),
						housingBlockEntity.worldPosition.getZ() + housingBlockEntity.influenceAreaPositionOffset.getZ() + housingBlockEntity.influenceAreaDimensions.getZ()
				);
				housingBlockEntity.calculateAreaBox = false;
				housingBlockEntity.setChanged();
			}
			List<Player> list = world.getEntitiesOfClass(Player.class, housingBlockEntity.area);
			Iterator<Player> var11 = list.iterator();

			Player playerEntity;
			while (var11.hasNext()) {
				playerEntity = var11.next();

				String playerName = playerEntity.getName().getString();
				String playerUuid = playerEntity.getStringUUID();
				if (Objects.equals(playerUuid, housingBlockEntity.getOwnerUuid())) {
					playerEntity.addEffect(new MobEffectInstance(ScriptBlocksStatusEffects.HOUSING_OWNER_EFFECT, 100, 0, true, false, false));
				} else if (housingBlockEntity.getCoOwnerList().contains(playerName)) {
					playerEntity.addEffect(new MobEffectInstance(ScriptBlocksStatusEffects.HOUSING_CO_OWNER_EFFECT, 100, 0, true, false, false));
				} else if (housingBlockEntity.getTrustedList().contains(playerName)) {
					playerEntity.addEffect(new MobEffectInstance(ScriptBlocksStatusEffects.HOUSING_TRUSTED_EFFECT, 100, 0, true, false, false));
				} else if (housingBlockEntity.getGuestList().contains(playerName) || (ownerTeam != null && ownerTeam.getPlayers().contains(playerName))) {
					playerEntity.addEffect(new MobEffectInstance(ScriptBlocksStatusEffects.HOUSING_GUEST_EFFECT, 100, 0, true, false, false));
				} else {
					playerEntity.addEffect(new MobEffectInstance(ScriptBlocksStatusEffects.HOUSING_STRANGER_EFFECT, 100, 0, true, false, false));
				}
				DataAttachmentHelper.setCurrentHousingBlockPosition(playerEntity, Optional.of(housingBlockEntity.getBlockPos()));
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

	private static String initOwner(Level world) {
		if (world != null) {
			String worldRegistryKey = world.dimension().identifier().getPath();
			String[] parts = worldRegistryKey.split("_");
			String uuidString = parts[0];
			if (UUIDUtilities.isStringValidUUID(uuidString)) {
				return uuidString;
			}
		}
		return "";
	}

	public boolean influenceAreaContains(BlockPos pos) {
		return (double) (pos.getX() + 1) > (this.worldPosition.getX() + this.influenceAreaPositionOffset.getX())
				&& (double) pos.getX() < (this.worldPosition.getX() + this.influenceAreaPositionOffset.getX() + this.influenceAreaDimensions.getX())
				&& (double) (pos.getY() + 1) > (this.worldPosition.getY() + this.influenceAreaPositionOffset.getY())
				&& (double) pos.getY() < (this.worldPosition.getY() + this.influenceAreaPositionOffset.getY() + this.influenceAreaDimensions.getY())
				&& (double) (pos.getZ() + 1) > (this.worldPosition.getZ() + this.influenceAreaPositionOffset.getZ())
				&& (double) pos.getZ() < (this.worldPosition.getZ() + this.influenceAreaPositionOffset.getZ() + this.influenceAreaDimensions.getZ());
	}

	public void trigger() {
		if (this.level != null) {
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
	}

	@Override
	protected void onRotate(BlockState state) {
		if (state.getBlock() instanceof RotatedBlockWithEntity) {
			if (state.getValue(ScriptBlocksBlockProperties.ROTATED) != this.rotated) {
				Rotation blockRotation = BlockRotationUtils.calculateRotationFromDifferentRotatedStates(state.getValue(ScriptBlocksBlockProperties.ROTATED), this.rotated);
				this.triggeredBlock.setLeft(BlockRotationUtils.rotateOffsetBlockPos(this.triggeredBlock.getLeft(), blockRotation));
				MutablePair<BlockPos, Vec3i> offsetArea = BlockRotationUtils.rotateOffsetArea(this.influenceAreaPositionOffset, this.influenceAreaDimensions, blockRotation);
				this.influenceAreaPositionOffset = offsetArea.getLeft();
				this.influenceAreaDimensions = offsetArea.getRight();
				this.rotated = state.getValue(ScriptBlocksBlockProperties.ROTATED);
			}
			if (state.getValue(ScriptBlocksBlockProperties.X_MIRRORED) != this.x_mirrored) {
				this.triggeredBlock.setLeft(BlockRotationUtils.mirrorOffsetBlockPos(this.triggeredBlock.getLeft(), Mirror.FRONT_BACK));
				MutablePair<BlockPos, Vec3i> offsetArea = BlockRotationUtils.mirrorOffsetArea(this.influenceAreaPositionOffset, this.influenceAreaDimensions, Mirror.FRONT_BACK);
				this.influenceAreaPositionOffset = offsetArea.getLeft();
				this.influenceAreaDimensions = offsetArea.getRight();
				this.x_mirrored = state.getValue(ScriptBlocksBlockProperties.X_MIRRORED);
			}
			if (state.getValue(ScriptBlocksBlockProperties.Z_MIRRORED) != this.z_mirrored) {
				this.triggeredBlock.setLeft(BlockRotationUtils.mirrorOffsetBlockPos(this.triggeredBlock.getLeft(), Mirror.LEFT_RIGHT));
				MutablePair<BlockPos, Vec3i> offsetArea = BlockRotationUtils.mirrorOffsetArea(this.influenceAreaPositionOffset, this.influenceAreaDimensions, Mirror.LEFT_RIGHT);
				this.influenceAreaPositionOffset = offsetArea.getLeft();
				this.influenceAreaDimensions = offsetArea.getRight();
				this.z_mirrored = state.getValue(ScriptBlocksBlockProperties.Z_MIRRORED);
			}
		}
	}

	public enum OwnerMode implements StringRepresentable {
		DIMENSION_OWNER("dimension_owner"),
		INTERACTION("interaction");

		private final String name;

		OwnerMode(String name) {
			this.name = name;
		}

		@Override
		public String getSerializedName() {
			return this.name;
		}

		public static Optional<OwnerMode> byName(String name) {
			return Arrays.stream(OwnerMode.values()).filter(ownerMode -> ownerMode.getSerializedName().equals(name)).findFirst();
		}

		public Component asText() {
			return Component.translatable("gui.housing_block.ownerMode." + this.name);
		}
	}
}
