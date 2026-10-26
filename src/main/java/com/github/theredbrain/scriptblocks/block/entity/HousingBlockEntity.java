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
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.scoreboard.Team;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.text.Text;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
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

	@Override
	protected void writeData(WriteView view) {

		view.putString("owner_uuid", this.ownerUuid);

		view.putBoolean("is_owner_set", isOwnerSet);

		WriteView.ListAppender<String> listAppender = view.getListAppender("co_owners", Codec.STRING);
		for (String string : this.coOwnerList) {
			listAppender.add(string);
		}

		WriteView.ListAppender<String> listAppender1 = view.getListAppender("trusted_people", Codec.STRING);
		for (String string : this.trustedList) {
			listAppender1.add(string);
		}

		WriteView.ListAppender<String> listAppender2 = view.getListAppender("guests", Codec.STRING);
		for (String string : this.guestList) {
			listAppender2.add(string);
		}

		NbtCompound nbtCompound = new NbtCompound();
		nbtCompound.putBoolean("visible", this.showInfluenceArea);
		nbtCompound.put("position_offset", BlockPos.CODEC, this.influenceAreaPositionOffset);
		nbtCompound.put("dimensions", ScriptBlocksNBTHelper.fromVec3i(this.influenceAreaDimensions));
		view.put("influence_area", NbtCompound.CODEC, nbtCompound.copy());

		view.putString("owner_mode", this.ownerMode.asString());

		nbtCompound = new NbtCompound();
		nbtCompound.put("position_offset", BlockPos.CODEC, this.triggeredBlock.getLeft());
		nbtCompound.putBoolean("resets", this.triggeredBlock.getRight());
		view.put("triggered_block", NbtCompound.CODEC, nbtCompound.copy());

		super.writeData(view);
	}

	@Override
	protected void readData(ReadView view) {

		this.ownerUuid = view.getString("owner_uuid", "");

		this.isOwnerSet = view.getBoolean("is_owner_set", false);

		this.coOwnerList.clear();
		for (String string : view.getTypedListView("co_owners", Codec.STRING)) {
			this.coOwnerList.add(string);
		}

		this.trustedList.clear();
		for (String string : view.getTypedListView("trusted_people", Codec.STRING)) {
			this.trustedList.add(string);
		}

		this.guestList.clear();
		for (String string : view.getTypedListView("guests", Codec.STRING)) {
			this.guestList.add(string);
		}

		NbtCompound nbtCompound = view.read("influence_area", NbtCompound.CODEC).orElseGet(NbtCompound::new);
		this.showInfluenceArea = nbtCompound.getBoolean("visible", false);

		this.influenceAreaDimensions = ScriptBlocksNBTHelper.toVec3i(nbtCompound, "dimensions").orElse(new Vec3i(0, 0, 0));

		this.influenceAreaPositionOffset = BlockPos.max(
				BlockPos.min(
						nbtCompound.get("position_offset", BlockPos.CODEC).orElse(BlockPos.ORIGIN),
						new BlockPos(48, 48, 48)
				),
				new BlockPos(-48, -48, -48)
		);

		this.ownerMode = OwnerMode.byName(view.getString("owner_mode", "")).orElseGet(() -> OwnerMode.DIMENSION_OWNER);

		nbtCompound = view.read("triggered_block", NbtCompound.CODEC).orElseGet(NbtCompound::new);
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

		super.readData(view);
	}

	public BlockEntityUpdateS2CPacket toUpdatePacket() {
		return BlockEntityUpdateS2CPacket.create(this);
	}

	@Override
	public NbtCompound toInitialChunkDataNbt(RegistryWrapper.WrapperLookup registryLookup) {
		return this.createComponentlessNbt(registryLookup);
	}

	public static void tick(World world, BlockPos pos, BlockState state, HousingBlockEntity housingBlockEntity) {
		if (!world.isClient() && world.getTime() % 20L == 0L) {
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
				DataAttachmentHelper.setCurrentHousingBlockPosition(playerEntity, Optional.of(housingBlockEntity.getPos()));
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
			if (state.get(ScriptBlocksBlockProperties.ROTATED) != this.rotated) {
				BlockRotation blockRotation = BlockRotationUtils.calculateRotationFromDifferentRotatedStates(state.get(ScriptBlocksBlockProperties.ROTATED), this.rotated);
				this.triggeredBlock.setLeft(BlockRotationUtils.rotateOffsetBlockPos(this.triggeredBlock.getLeft(), blockRotation));
				MutablePair<BlockPos, Vec3i> offsetArea = BlockRotationUtils.rotateOffsetArea(this.influenceAreaPositionOffset, this.influenceAreaDimensions, blockRotation);
				this.influenceAreaPositionOffset = offsetArea.getLeft();
				this.influenceAreaDimensions = offsetArea.getRight();
				this.rotated = state.get(ScriptBlocksBlockProperties.ROTATED);
			}
			if (state.get(ScriptBlocksBlockProperties.X_MIRRORED) != this.x_mirrored) {
				this.triggeredBlock.setLeft(BlockRotationUtils.mirrorOffsetBlockPos(this.triggeredBlock.getLeft(), BlockMirror.FRONT_BACK));
				MutablePair<BlockPos, Vec3i> offsetArea = BlockRotationUtils.mirrorOffsetArea(this.influenceAreaPositionOffset, this.influenceAreaDimensions, BlockMirror.FRONT_BACK);
				this.influenceAreaPositionOffset = offsetArea.getLeft();
				this.influenceAreaDimensions = offsetArea.getRight();
				this.x_mirrored = state.get(ScriptBlocksBlockProperties.X_MIRRORED);
			}
			if (state.get(ScriptBlocksBlockProperties.Z_MIRRORED) != this.z_mirrored) {
				this.triggeredBlock.setLeft(BlockRotationUtils.mirrorOffsetBlockPos(this.triggeredBlock.getLeft(), BlockMirror.LEFT_RIGHT));
				MutablePair<BlockPos, Vec3i> offsetArea = BlockRotationUtils.mirrorOffsetArea(this.influenceAreaPositionOffset, this.influenceAreaDimensions, BlockMirror.LEFT_RIGHT);
				this.influenceAreaPositionOffset = offsetArea.getLeft();
				this.influenceAreaDimensions = offsetArea.getRight();
				this.z_mirrored = state.get(ScriptBlocksBlockProperties.Z_MIRRORED);
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
