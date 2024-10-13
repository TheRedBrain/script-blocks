package com.github.theredbrain.scriptblocks.block.entity;

import com.github.theredbrain.scriptblocks.ScriptBlocks;
import com.github.theredbrain.scriptblocks.block.Resetable;
import com.github.theredbrain.scriptblocks.block.RotatedBlockWithEntity;
import com.github.theredbrain.scriptblocks.block.Triggerable;
import com.github.theredbrain.scriptblocks.entity.player.DuckPlayerEntityMixin;
import com.github.theredbrain.scriptblocks.registry.EntityRegistry;
import com.github.theredbrain.scriptblocks.registry.StatusEffectsRegistry;
import com.github.theredbrain.scriptblocks.util.BlockRotationUtils;
import com.github.theredbrain.scriptblocks.util.UUIDUtilities;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryWrapper;
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

public class HousingBlockEntity extends RotatedBlockEntity {
	private String ownerUuid = "";
	private boolean isOwnerSet;
	private List<String> coOwnerList = new ArrayList<>(List.of());
	private List<String> trustedList = new ArrayList<>(List.of());
	private List<String> guestList = new ArrayList<>(List.of());
	private boolean showInfluenceArea = false;
	private Vec3i influenceAreaDimensions = Vec3i.ZERO;
	private BlockPos influenceAreaPositionOffset = new BlockPos(0, 1, 0);
	private OwnerMode ownerMode = OwnerMode.DIMENSION_OWNER;
	private MutablePair<BlockPos, Boolean> triggeredBlock = new MutablePair<>(new BlockPos(0, 0, 0), false);

	public HousingBlockEntity(BlockPos pos, BlockState state) {
		super(EntityRegistry.HOUSING_BLOCK_ENTITY, pos, state);
		this.isOwnerSet = false;
	}

	@Override
	protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
		nbt.putString("ownerUuid", this.ownerUuid);
		nbt.putBoolean("isOwnerSet", isOwnerSet);

		int coOwnerListSize = coOwnerList.size();
		nbt.putInt("coOwnerListSize", coOwnerListSize);
		for (int i = 0; i < coOwnerListSize; i++) {
			nbt.putString("coOwnerListEntry" + i, this.coOwnerList.get(i));
		}

		int trustedListSize = trustedList.size();
		nbt.putInt("trustedListSize", trustedListSize);
		for (int i = 0; i < trustedListSize; i++) {
			nbt.putString("trustedListEntry" + i, this.trustedList.get(i));
		}

		int guestListSize = guestList.size();
		nbt.putInt("guestListSize", guestListSize);
		for (int i = 0; i < guestListSize; i++) {
			nbt.putString("guestListEntry" + i, this.guestList.get(i));
		}

		nbt.putBoolean("showInfluenceArea", this.showInfluenceArea);

		nbt.putInt("influenceAreaDimensionsX", this.influenceAreaDimensions.getX());
		nbt.putInt("influenceAreaDimensionsY", this.influenceAreaDimensions.getY());
		nbt.putInt("influenceAreaDimensionsZ", this.influenceAreaDimensions.getZ());

		nbt.putInt("influenceAreaPositionOffsetX", this.influenceAreaPositionOffset.getX());
		nbt.putInt("influenceAreaPositionOffsetY", this.influenceAreaPositionOffset.getY());
		nbt.putInt("influenceAreaPositionOffsetZ", this.influenceAreaPositionOffset.getZ());

		nbt.putString("ownerMode", this.ownerMode.asString());

		nbt.putInt("triggeredBlockPositionOffsetX", this.triggeredBlock.getLeft().getX());
		nbt.putInt("triggeredBlockPositionOffsetY", this.triggeredBlock.getLeft().getY());
		nbt.putInt("triggeredBlockPositionOffsetZ", this.triggeredBlock.getLeft().getZ());
		nbt.putBoolean("triggeredBlockResets", this.triggeredBlock.getRight());

		super.writeNbt(nbt, registryLookup);
	}

	@Override
	protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
		this.ownerUuid = nbt.getString("ownerUuid");
		this.isOwnerSet = nbt.getBoolean("isOwnerSet");

		this.coOwnerList = new ArrayList<>(List.of());
		this.trustedList = new ArrayList<>(List.of());
		this.guestList = new ArrayList<>(List.of());

		int coOwnerListSize = nbt.getInt("coOwnerListSize");
		for (int i = 0; i < coOwnerListSize; i++) {
			this.coOwnerList.add(nbt.getString("coOwnerListEntry" + i));
		}

		int trustedListSize = nbt.getInt("trustedListSize");
		for (int i = 0; i < trustedListSize; i++) {
			this.trustedList.add(nbt.getString("trustedListEntry" + i));
		}

		int guestListSize = nbt.getInt("guestListSize");
		for (int i = 0; i < guestListSize; i++) {
			this.guestList.add(nbt.getString("guestListEntry" + i));
		}

		this.showInfluenceArea = nbt.getBoolean("showInfluenceArea");

		int i = MathHelper.clamp(nbt.getInt("influenceAreaDimensionsX"), 0, 48);
		int j = MathHelper.clamp(nbt.getInt("influenceAreaDimensionsY"), 0, 48);
		int k = MathHelper.clamp(nbt.getInt("influenceAreaDimensionsZ"), 0, 48);
		this.influenceAreaDimensions = new Vec3i(i, j, k);

		int l = MathHelper.clamp(nbt.getInt("influenceAreaPositionOffsetX"), -48, 48);
		int m = MathHelper.clamp(nbt.getInt("influenceAreaPositionOffsetY"), -48, 48);
		int n = MathHelper.clamp(nbt.getInt("influenceAreaPositionOffsetZ"), -48, 48);
		this.influenceAreaPositionOffset = new BlockPos(l, m, n);

		this.ownerMode = OwnerMode.byName(nbt.getString("ownerMode")).orElseGet(() -> OwnerMode.DIMENSION_OWNER);

		int x = MathHelper.clamp(nbt.getInt("triggeredBlockPositionOffsetX"), -48, 48);
		int y = MathHelper.clamp(nbt.getInt("triggeredBlockPositionOffsetY"), -48, 48);
		int z = MathHelper.clamp(nbt.getInt("triggeredBlockPositionOffsetZ"), -48, 48);
		this.triggeredBlock = new MutablePair<>(new BlockPos(x, y, z), nbt.getBoolean("triggeredBlockResets"));

		super.readNbt(nbt, registryLookup);
	}

	public BlockEntityUpdateS2CPacket toUpdatePacket() {
		return BlockEntityUpdateS2CPacket.create(this);
	}

	@Override
	public NbtCompound toInitialChunkDataNbt(RegistryWrapper.WrapperLookup registryLookup) {
		return this.createComponentlessNbt(registryLookup);
	}

	public static void tick(World world, BlockPos pos, BlockState state, HousingBlockEntity blockEntity) {
		if (!world.isClient && world.getTime() % 20L == 0L) {
			if (blockEntity.hasWorld() && !blockEntity.isOwnerSet && blockEntity.ownerMode == OwnerMode.DIMENSION_OWNER) {
				blockEntity.ownerUuid = initOwner(blockEntity.world);
				if (UUIDUtilities.isStringValidUUID(blockEntity.ownerUuid)) {
					ScriptBlocks.info(blockEntity.ownerUuid);
					blockEntity.isOwnerSet = true;
				}
			}

			Box box = new Box(
					blockEntity.pos.getX() + blockEntity.influenceAreaPositionOffset.getX(),
					blockEntity.pos.getY() + blockEntity.influenceAreaPositionOffset.getY(),
					blockEntity.pos.getZ() + blockEntity.influenceAreaPositionOffset.getZ(),
					blockEntity.pos.getX() + blockEntity.influenceAreaPositionOffset.getX() + blockEntity.influenceAreaDimensions.getX(),
					blockEntity.pos.getY() + blockEntity.influenceAreaPositionOffset.getY() + blockEntity.influenceAreaDimensions.getY(),
					blockEntity.pos.getZ() + blockEntity.influenceAreaPositionOffset.getZ() + blockEntity.influenceAreaDimensions.getZ()
			);
			List<PlayerEntity> list = world.getNonSpectatingEntities(PlayerEntity.class, box);
			Iterator var11 = list.iterator();

			PlayerEntity playerEntity;
			while (var11.hasNext()) {
				playerEntity = (PlayerEntity) var11.next();

				String playerName = playerEntity.getName().getString();
				String playerUuid = playerEntity.getUuidAsString();
				if (Objects.equals(playerUuid, blockEntity.getOwnerUuid())) {
					playerEntity.addStatusEffect(new StatusEffectInstance(Registries.STATUS_EFFECT.getEntry(StatusEffectsRegistry.HOUSING_OWNER_EFFECT), 100, 0, true, false, false));
				} else if (blockEntity.getCoOwnerList().contains(playerName)) {
					playerEntity.addStatusEffect(new StatusEffectInstance(Registries.STATUS_EFFECT.getEntry(StatusEffectsRegistry.HOUSING_CO_OWNER_EFFECT), 100, 0, true, false, false));
				} else if (blockEntity.getTrustedList().contains(playerName)) {
					playerEntity.addStatusEffect(new StatusEffectInstance(Registries.STATUS_EFFECT.getEntry(StatusEffectsRegistry.HOUSING_TRUSTED_EFFECT), 100, 0, true, false, false));
				} else if (blockEntity.getGuestList().contains(playerName)) {
					playerEntity.addStatusEffect(new StatusEffectInstance(Registries.STATUS_EFFECT.getEntry(StatusEffectsRegistry.HOUSING_GUEST_EFFECT), 100, 0, true, false, false));
				} else {
					playerEntity.addStatusEffect(new StatusEffectInstance(Registries.STATUS_EFFECT.getEntry(StatusEffectsRegistry.HOUSING_STRANGER_EFFECT), 100, 0, true, false, false));
				}
				((DuckPlayerEntityMixin) playerEntity).scriptblocks$setCurrentHousingBlockPosition(blockEntity.pos);
			}
		}
	}

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

	public boolean setCoOwnerList(List<String> coOwnerList) {
		this.coOwnerList = coOwnerList;
		return true;
	}

	public List<String> getTrustedList() {
		return this.trustedList;
	}

	public boolean setTrustedList(List<String> trustedList) {
		this.trustedList = trustedList;
		return true;
	}

	public List<String> getGuestList() {
		return this.guestList;
	}

	public boolean setGuestList(List<String> guestList) {
		this.guestList = guestList;
		return true;
	}

	public boolean getShowInfluenceArea() {
		return this.showInfluenceArea;
	}

	public boolean setShowInfluenceArea(boolean showInfluenceArea) {
		this.showInfluenceArea = showInfluenceArea;
		return true;
	}

	public Vec3i getInfluenceAreaDimensions() {
		return this.influenceAreaDimensions;
	}

	// TODO check if input is valid
	public boolean setInfluenceAreaDimensions(Vec3i influenceAreaDimensions) {
		this.influenceAreaDimensions = influenceAreaDimensions;
		return true;
	}

	public BlockPos getRestrictBlockBreakingAreaPositionOffset() {
		return this.influenceAreaPositionOffset;
	}

	// TODO check if input is valid
	public boolean setRestrictBlockBreakingAreaPositionOffset(BlockPos influenceAreaPositionOffset) {
		this.influenceAreaPositionOffset = influenceAreaPositionOffset;
		return true;
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

	public boolean setOwnerMode(OwnerMode ownerMode) {
		this.ownerMode = ownerMode;
		return true;
	}

	public boolean isOwnerSet() {
		return this.isOwnerSet;
	}

	public void setIsOwnerSet(boolean isOwnerSet) {
		this.isOwnerSet = isOwnerSet;
	}

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

	public static enum OwnerMode implements StringIdentifiable {
		DIMENSION_OWNER("dimension_owner"),
		INTERACTION("interaction");

		private final String name;

		private OwnerMode(String name) {
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
