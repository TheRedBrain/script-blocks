package com.github.theredbrain.scriptblocks.block.entity;

import com.github.theredbrain.scriptblocks.block.ProvidesData;
import com.github.theredbrain.scriptblocks.block.Resetable;
import com.github.theredbrain.scriptblocks.block.RotatedBlockWithEntity;
import com.github.theredbrain.scriptblocks.block.Triggerable;
import com.github.theredbrain.scriptblocks.registry.EntityRegistry;
import com.github.theredbrain.scriptblocks.util.BlockRotationUtils;
import com.github.theredbrain.scriptblocks.util.UUIDUtilities;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import org.apache.commons.lang3.tuple.MutablePair;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class TriggeredRNGBlockEntity extends RotatedBlockEntity implements Triggerable {
	private static final BlockPos OVERRIDE_TRIGGERED_BLOCK_POS_DEFAULT = new BlockPos(0, -1, 0);
	private static final BlockPos FALLBACK_TRIGGERED_BLOCK_POS_DEFAULT = new BlockPos(0, 1, 0);

	private BlockPos dataProvidingBlockPosOffset = BlockPos.ORIGIN;
	private String overrideDataIdentifier = "";
	private String overrideDataValue = "";
	private MutablePair<BlockPos, Boolean> overrideTriggeredBlock = new MutablePair<>(OVERRIDE_TRIGGERED_BLOCK_POS_DEFAULT, false);

	private String influencingAttributeIdentifierString = "";
	private boolean checksTeamAttributes = true;
	private boolean isAffectedByLuck = true;

	private int randomMinValue = 0;
	private int randomMaxValue = 1;
	MutablePair<BlockPos, Boolean> fallbackTriggeredBlock = new MutablePair<>(FALLBACK_TRIGGERED_BLOCK_POS_DEFAULT, false);
	private List<MutablePair<MutablePair<BlockPos, Boolean>, Integer>> triggeredBlocks = new ArrayList<>();

	public TriggeredRNGBlockEntity(BlockPos pos, BlockState state) {
		super(EntityRegistry.TRIGGERED_RNG_BLOCK_ENTITY, pos, state);
	}

	@Override
	protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {

		if (this.dataProvidingBlockPosOffset != BlockPos.ORIGIN) {
			nbt.putInt("dataProvidingBlockPosOffsetX", this.dataProvidingBlockPosOffset.getX());
			nbt.putInt("dataProvidingBlockPosOffsetY", this.dataProvidingBlockPosOffset.getY());
			nbt.putInt("dataProvidingBlockPosOffsetZ", this.dataProvidingBlockPosOffset.getZ());
		} else {
			nbt.remove("dataProvidingBlockPosOffsetX");
			nbt.remove("dataProvidingBlockPosOffsetY");
			nbt.remove("dataProvidingBlockPosOffsetZ");
		}

		if (!this.overrideDataIdentifier.isEmpty()) {
			nbt.putString("overrideDataIdentifier", this.overrideDataIdentifier);
		} else {
			nbt.remove("overrideDataIdentifier");
		}

		if (!this.overrideDataValue.isEmpty()) {
			nbt.putString("overrideDataValue", this.overrideDataValue);
		} else {
			nbt.remove("overrideDataValue");
		}

		BlockPos overrideTriggeredBlockPositionOffset = this.overrideTriggeredBlock.getLeft();
		if (!overrideTriggeredBlockPositionOffset.equals(OVERRIDE_TRIGGERED_BLOCK_POS_DEFAULT)) {
			nbt.putInt("overrideTriggeredBlockPositionOffsetX", overrideTriggeredBlockPositionOffset.getX());
			nbt.putInt("overrideTriggeredBlockPositionOffsetY", overrideTriggeredBlockPositionOffset.getY());
			nbt.putInt("overrideTriggeredBlockPositionOffsetZ", overrideTriggeredBlockPositionOffset.getZ());
			nbt.putBoolean("overrideTriggeredBlockResets", this.overrideTriggeredBlock.getRight());
		} else {
			nbt.remove("overrideTriggeredBlockPositionOffsetX");
			nbt.remove("overrideTriggeredBlockPositionOffsetY");
			nbt.remove("overrideTriggeredBlockPositionOffsetZ");
			nbt.remove("overrideTriggeredBlockResets");
		}

		if (!this.influencingAttributeIdentifierString.isEmpty()) {
			nbt.putString("influencingAttributeIdentifierString", this.influencingAttributeIdentifierString);
		} else {
			nbt.remove("influencingAttributeIdentifierString");
		}

		if (!this.checksTeamAttributes) {
			nbt.putBoolean("checksTeamAttributes", false);
		} else {
			nbt.remove("checksTeamAttributes");
		}

		if (!this.isAffectedByLuck) {
			nbt.putBoolean("isAffectedByLuck", false);
		} else {
			nbt.remove("isAffectedByLuck");
		}

		if (this.randomMinValue != 0) {
			nbt.putInt("randomMinValue", this.randomMinValue);
		} else {
			nbt.remove("randomMinValue");
		}

		if (this.randomMaxValue != 1) {
			nbt.putInt("randomMaxValue", this.randomMaxValue);
		} else {
			nbt.remove("randomMaxValue");
		}

		BlockPos fallbackTriggeredBlockPositionOffset = this.fallbackTriggeredBlock.getLeft();
		if (!fallbackTriggeredBlockPositionOffset.equals(OVERRIDE_TRIGGERED_BLOCK_POS_DEFAULT)) {
			nbt.putInt("fallbackTriggeredBlockPositionOffsetX", fallbackTriggeredBlockPositionOffset.getX());
			nbt.putInt("fallbackTriggeredBlockPositionOffsetY", fallbackTriggeredBlockPositionOffset.getY());
			nbt.putInt("fallbackTriggeredBlockPositionOffsetZ", fallbackTriggeredBlockPositionOffset.getZ());
			nbt.putBoolean("fallbackTriggeredBlockResets", this.fallbackTriggeredBlock.getRight());
		} else {
			nbt.remove("fallbackTriggeredBlockPositionOffsetX");
			nbt.remove("fallbackTriggeredBlockPositionOffsetY");
			nbt.remove("fallbackTriggeredBlockPositionOffsetZ");
			nbt.remove("fallbackTriggeredBlockResets");
		}

		if (!this.triggeredBlocks.isEmpty()) {

			nbt.putInt("triggeredBlocksSize", triggeredBlocks.size());
			for (int i = 0; i < this.triggeredBlocks.size(); i++) {
				BlockPos triggeredBlock = this.triggeredBlocks.get(i).left.left;
				nbt.putInt("triggeredBlockPositionOffsetX_" + i, triggeredBlock.getX());
				nbt.putInt("triggeredBlockPositionOffsetY_" + i, triggeredBlock.getY());
				nbt.putInt("triggeredBlockPositionOffsetZ_" + i, triggeredBlock.getZ());
				nbt.putBoolean("triggeredBlockResets_" + i, this.triggeredBlocks.get(i).left.right);
				nbt.putInt("triggeredBlockThreshold_" + i, this.triggeredBlocks.get(i).right);
			}

		}

		super.writeNbt(nbt, registryLookup);
	}

	@Override
	protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {

		if (nbt.contains("dataProvidingBlockPosOffsetX", NbtElement.INT_TYPE) && nbt.contains("dataProvidingBlockPosOffsetY", NbtElement.INT_TYPE) && nbt.contains("dataProvidingBlockPosOffsetZ", NbtElement.INT_TYPE)) {
			this.dataProvidingBlockPosOffset = new BlockPos(
					MathHelper.clamp(nbt.getInt("dataProvidingBlockPosOffsetX"), -48, 48),
					MathHelper.clamp(nbt.getInt("dataProvidingBlockPosOffsetY"), -48, 48),
					MathHelper.clamp(nbt.getInt("dataProvidingBlockPosOffsetZ"), -48, 48)
			);
		} else {
			this.dataProvidingBlockPosOffset = BlockPos.ORIGIN;
		}

		if (nbt.contains("overrideDataIdentifier", NbtElement.STRING_TYPE)) {
			this.overrideDataIdentifier = nbt.getString("overrideDataIdentifier");
		} else {
			this.overrideDataIdentifier = "";
		}

		if (nbt.contains("overrideDataValue", NbtElement.STRING_TYPE)) {
			this.overrideDataValue = nbt.getString("overrideDataValue");
		} else {
			this.overrideDataValue = "";
		}

		if (nbt.contains("overrideTriggeredBlockPositionOffsetX", NbtElement.INT_TYPE) || nbt.contains("overrideTriggeredBlockPositionOffsetY", NbtElement.INT_TYPE) || nbt.contains("overrideTriggeredBlockPositionOffsetZ", NbtElement.INT_TYPE) || nbt.contains("overrideTriggeredBlockResets", NbtElement.BYTE_TYPE)) {
			this.overrideTriggeredBlock = new MutablePair<>(
					new BlockPos(
							MathHelper.clamp(nbt.getInt("overrideTriggeredBlockPositionOffsetX"), -48, 48),
							MathHelper.clamp(nbt.getInt("overrideTriggeredBlockPositionOffsetY"), -48, 48),
							MathHelper.clamp(nbt.getInt("overrideTriggeredBlockPositionOffsetZ"), -48, 48)
					),
					nbt.getBoolean("overrideTriggeredBlockResets")
			);
		} else {
			this.overrideTriggeredBlock = new MutablePair<>(OVERRIDE_TRIGGERED_BLOCK_POS_DEFAULT, false);
		}

		if (nbt.contains("influencingAttributeIdentifierString", NbtElement.STRING_TYPE)) {
			this.influencingAttributeIdentifierString = nbt.getString("influencingAttributeIdentifierString");
		} else {
			this.influencingAttributeIdentifierString = "";
		}

		if (nbt.contains("checksTeamAttributes", NbtElement.BYTE_TYPE)) {
			this.checksTeamAttributes = nbt.getBoolean("checksTeamAttributes");
		} else {
			this.checksTeamAttributes = true;
		}

		if (nbt.contains("isAffectedByLuck", NbtElement.BYTE_TYPE)) {
			this.isAffectedByLuck = nbt.getBoolean("isAffectedByLuck");
		} else {
			this.isAffectedByLuck = true;
		}

		if (nbt.contains("randomMinValue", NbtElement.INT_TYPE)) {
			this.randomMinValue = nbt.getInt("randomMinValue");
		} else {
			this.randomMinValue = 0;
		}

		if (nbt.contains("randomMaxValue", NbtElement.INT_TYPE)) {
			this.randomMaxValue = nbt.getInt("randomMaxValue");
		} else {
			this.randomMaxValue = 1;
		}

		if (nbt.contains("fallbackTriggeredBlockPositionOffsetX", NbtElement.INT_TYPE) || nbt.contains("fallbackTriggeredBlockPositionOffsetY", NbtElement.INT_TYPE) || nbt.contains("fallbackTriggeredBlockPositionOffsetZ", NbtElement.INT_TYPE) || nbt.contains("fallbackTriggeredBlockResets", NbtElement.BYTE_TYPE)) {
			this.fallbackTriggeredBlock = new MutablePair<>(
					new BlockPos(
							MathHelper.clamp(nbt.getInt("fallbackTriggeredBlockPositionOffsetX"), -48, 48),
							MathHelper.clamp(nbt.getInt("fallbackTriggeredBlockPositionOffsetY"), -48, 48),
							MathHelper.clamp(nbt.getInt("fallbackTriggeredBlockPositionOffsetZ"), -48, 48)
					),
					nbt.getBoolean("fallbackTriggeredBlockResets")
			);
		} else {
			this.fallbackTriggeredBlock = new MutablePair<>(FALLBACK_TRIGGERED_BLOCK_POS_DEFAULT, false);
		}

		int triggeredBlocksSize = nbt.getInt("triggeredBlocksSize");
		this.triggeredBlocks = new ArrayList<>(List.of());
		for (int i = 0; i < triggeredBlocksSize; i++) {
			this.triggeredBlocks.add(
					new MutablePair<>(
							new MutablePair<>(
									new BlockPos(
											MathHelper.clamp(nbt.getInt("triggeredBlockPositionOffsetX_" + i), -48, 48),
											MathHelper.clamp(nbt.getInt("triggeredBlockPositionOffsetY_" + i), -48, 48),
											MathHelper.clamp(nbt.getInt("triggeredBlockPositionOffsetZ_" + i), -48, 48)
									),
									nbt.getBoolean("triggeredBlockResets_" + i)
							),
							MathHelper.clamp(nbt.getInt("triggeredBlockThreshold_" + i), 0, 100)
					)
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

	public BlockPos getDataProvidingBlockPosOffset() {
		return this.dataProvidingBlockPosOffset;
	}

	public void setDataProvidingBlockPosOffset(BlockPos dataProvidingBlockPosOffset) {
		this.dataProvidingBlockPosOffset = dataProvidingBlockPosOffset;
	}

	public String getOverrideDataIdentifier() {
		return this.overrideDataIdentifier;
	}

	public void setOverrideDataIdentifier(String overrideDataIdentifier) {
		this.overrideDataIdentifier = overrideDataIdentifier;
	}

	public String getOverrideDataValue() {
		return this.overrideDataValue;
	}

	public void setOverrideDataValue(String overrideDataValue) {
		this.overrideDataValue = overrideDataValue;
	}

	public MutablePair<BlockPos, Boolean> getOverrideTriggeredBlock() {
		return this.overrideTriggeredBlock;
	}

	public void setOverrideTriggeredBlock(MutablePair<BlockPos, Boolean> overrideTriggeredBlock) {
		this.overrideTriggeredBlock = overrideTriggeredBlock;
	}

	public String getInfluencingAttributeIdentifierString() {
		return this.influencingAttributeIdentifierString;
	}

	public void setInfluencingAttributeIdentifierString(String influencingAttributeIdentifierString) {
		this.influencingAttributeIdentifierString = influencingAttributeIdentifierString;
	}

	public boolean checksTeamAttributes() {
		return this.checksTeamAttributes;
	}

	public void setChecksTeamAttributes(boolean checksTeamAttributes) {
		this.checksTeamAttributes = checksTeamAttributes;
	}

	public boolean isAffectedByLuck() {
		return this.isAffectedByLuck;
	}

	public void setIsAffectedByLuck(boolean isAffectedByLuck) {
		this.isAffectedByLuck = isAffectedByLuck;
	}

	public int getRandomMinValue() {
		return this.randomMinValue;
	}

	public void setRandomMinValue(int randomMinValue) {
		this.randomMinValue = randomMinValue;
	}

	public int getRandomMaxValue() {
		return this.randomMaxValue;
	}

	public void setRandomMaxValue(int randomMaxValue) {
		this.randomMaxValue = randomMaxValue;
	}

	public MutablePair<BlockPos, Boolean> getFallbackTriggeredBlock() {
		return this.fallbackTriggeredBlock;
	}

	public void setFallbackTriggeredBlock(MutablePair<BlockPos, Boolean> fallbackTriggeredBlock) {
		this.fallbackTriggeredBlock = fallbackTriggeredBlock;
	}

	public List<MutablePair<MutablePair<BlockPos, Boolean>, Integer>> getTriggeredBlocks() {
		return this.triggeredBlocks;
	}

	public void setTriggeredBlocks(List<MutablePair<MutablePair<BlockPos, Boolean>, Integer>> triggeredBlocks) {
		this.triggeredBlocks = triggeredBlocks;
	}

	@Override
	public void trigger() {
		if (this.world != null) {
			String worldName = this.world.getRegistryKey().getValue().getPath();
			MinecraftServer server = this.world.getServer();

			boolean overrideIsActive = false;

			// check if override is active
			if (this.dataProvidingBlockPosOffset != BlockPos.ORIGIN && !this.overrideDataIdentifier.isEmpty() && !this.overrideDataValue.isEmpty()) {
				BlockPos dataProviderBlockPos = new BlockPos(this.pos.getX() + this.dataProvidingBlockPosOffset.getX(), this.pos.getY() + this.dataProvidingBlockPosOffset.getY(), this.pos.getZ() + this.dataProvidingBlockPosOffset.getZ());
				BlockEntity blockEntity = world.getBlockEntity(dataProviderBlockPos);
				if (blockEntity instanceof ProvidesData providesDataEntity) {
					overrideIsActive = Objects.equals(providesDataEntity.getData(this.overrideDataIdentifier), this.overrideDataValue);
				}
			}

			if (overrideIsActive) {
				BlockEntity blockEntity = world.getBlockEntity(new BlockPos(this.pos.getX() + this.overrideTriggeredBlock.getLeft().getX(), this.pos.getY() + this.overrideTriggeredBlock.getLeft().getY(), this.pos.getZ() + this.overrideTriggeredBlock.getLeft().getZ()));
				if (blockEntity != this) {
					boolean overrideTriggeredBlockResets = this.overrideTriggeredBlock.getRight();
					if (overrideTriggeredBlockResets && blockEntity instanceof Resetable resetable) {
						resetable.reset();
					} else if (!overrideTriggeredBlockResets && blockEntity instanceof Triggerable triggerable) {
						triggerable.trigger();
					}
				}
			} else {
				float luck = 0.0F;
				float attributeValue = 0.0F;
				String attributeString = this.influencingAttributeIdentifierString;

				Optional<RegistryEntry.Reference<EntityAttribute>> attributeEntry = Registries.ATTRIBUTE.getEntry(Identifier.tryParse(attributeString));

				ServerPlayerEntity serverPlayerEntity = null;
				if (server != null && UUIDUtilities.isStringValidUUID(worldName)) {
					serverPlayerEntity = server.getPlayerManager().getPlayer(UUID.fromString(worldName));
				}
				if (serverPlayerEntity != null) {
					luck += serverPlayerEntity.getLuck();
					if (attributeEntry.isPresent()) {
						attributeValue += (float) serverPlayerEntity.getAttributeValue(attributeEntry.get());
					}
					if (this.checksTeamAttributes) {
						Team team = serverPlayerEntity.getScoreboardTeam();
						if (team != null) {
							for (String playerString : team.getPlayerList()) {
								ServerPlayerEntity teamServerPlayerEntity = server.getPlayerManager().getPlayer(playerString);
								if (teamServerPlayerEntity != null && teamServerPlayerEntity != serverPlayerEntity) {
									luck += teamServerPlayerEntity.getLuck();
									if (attributeEntry.isPresent()) {
										attributeValue += (float) serverPlayerEntity.getAttributeValue(attributeEntry.get());
									}
								}
							}
						}
					}
				}

				int randomNumber = this.world.random.nextBetween(this.randomMinValue, this.randomMaxValue);
				if (this.isAffectedByLuck) {
					boolean rollWithAdvantage = luck > 0;
					int additionalRollAmount = (int) Math.floor(Math.abs(luck));
					for (int i = 0; i < additionalRollAmount; i++) {
						int newRandomNumber = this.world.random.nextBetween(this.randomMinValue, this.randomMaxValue);
						if (rollWithAdvantage) {
							if (newRandomNumber > randomNumber) {
								randomNumber = newRandomNumber;
							}
						} else {
							if (newRandomNumber < randomNumber) {
								randomNumber = newRandomNumber;
							}
						}
					}
				}
				randomNumber += (int) Math.floor(Math.abs(attributeValue));

				MutablePair<BlockPos, Boolean> triggeredBlock = new MutablePair<>(BlockPos.ORIGIN, false);

				// determine highest reached threshold
				int currentThreshold = this.randomMinValue;
				for (MutablePair<MutablePair<BlockPos, Boolean>, Integer> entry : this.triggeredBlocks) {
					int f = entry.right;
					if (randomNumber >= f && f >= currentThreshold) {
						triggeredBlock = entry.left;
						currentThreshold = f;
					}
				}

				// trigger chosen triggeredBlock
				if (!Objects.equals(triggeredBlock, new MutablePair<>(BlockPos.ORIGIN, false))) {
					BlockEntity blockEntity = world.getBlockEntity(triggeredBlock.left);
					if (blockEntity != this) {
						if (triggeredBlock.right && blockEntity instanceof Resetable resetable) {
							resetable.reset();
						} else if (!triggeredBlock.right && blockEntity instanceof Triggerable triggerable) {
							triggerable.trigger();
						}
					}
				}
			}
		}
	}

	@Override
	protected void onRotate(BlockState state) {
		if (state.getBlock() instanceof RotatedBlockWithEntity) {
			if (state.get(RotatedBlockWithEntity.ROTATED) != this.rotated) {
				BlockRotation blockRotation = BlockRotationUtils.calculateRotationFromDifferentRotatedStates(state.get(RotatedBlockWithEntity.ROTATED), this.rotated);

				this.dataProvidingBlockPosOffset = BlockRotationUtils.rotateOffsetBlockPos(this.dataProvidingBlockPosOffset, blockRotation);

				this.overrideTriggeredBlock.setLeft(BlockRotationUtils.rotateOffsetBlockPos(this.overrideTriggeredBlock.getLeft(), blockRotation));

				List<MutablePair<MutablePair<BlockPos, Boolean>, Integer>> newTriggeredBlocks = new ArrayList<>(List.of());
				for (MutablePair<MutablePair<BlockPos, Boolean>, Integer> triggeredBlock : this.triggeredBlocks) {
					newTriggeredBlocks.add(new MutablePair<>(new MutablePair<>(BlockRotationUtils.rotateOffsetBlockPos(triggeredBlock.getLeft().getLeft(), blockRotation), triggeredBlock.getLeft().getRight()), triggeredBlock.getRight()));
				}
				this.triggeredBlocks = newTriggeredBlocks;

				this.rotated = state.get(RotatedBlockWithEntity.ROTATED);
			}
			if (state.get(RotatedBlockWithEntity.X_MIRRORED) != this.x_mirrored) {

				this.dataProvidingBlockPosOffset = BlockRotationUtils.mirrorOffsetBlockPos(this.dataProvidingBlockPosOffset, BlockMirror.FRONT_BACK);

				this.overrideTriggeredBlock.setLeft(BlockRotationUtils.mirrorOffsetBlockPos(this.overrideTriggeredBlock.getLeft(), BlockMirror.FRONT_BACK));

				List<MutablePair<MutablePair<BlockPos, Boolean>, Integer>> newTriggeredBlocks = new ArrayList<>(List.of());
				for (MutablePair<MutablePair<BlockPos, Boolean>, Integer> triggeredBlock : this.triggeredBlocks) {
					newTriggeredBlocks.add(new MutablePair<>(new MutablePair<>(BlockRotationUtils.mirrorOffsetBlockPos(triggeredBlock.getLeft().getLeft(), BlockMirror.FRONT_BACK), triggeredBlock.getLeft().getRight()), triggeredBlock.getRight()));
				}
				this.triggeredBlocks = newTriggeredBlocks;

				this.x_mirrored = state.get(RotatedBlockWithEntity.X_MIRRORED);
			}
			if (state.get(RotatedBlockWithEntity.Z_MIRRORED) != this.z_mirrored) {

				this.dataProvidingBlockPosOffset = BlockRotationUtils.mirrorOffsetBlockPos(this.dataProvidingBlockPosOffset, BlockMirror.LEFT_RIGHT);

				this.overrideTriggeredBlock.setLeft(BlockRotationUtils.mirrorOffsetBlockPos(this.overrideTriggeredBlock.getLeft(), BlockMirror.LEFT_RIGHT));

				List<MutablePair<MutablePair<BlockPos, Boolean>, Integer>> newTriggeredBlocks = new ArrayList<>(List.of());
				for (MutablePair<MutablePair<BlockPos, Boolean>, Integer> triggeredBlock : this.triggeredBlocks) {
					newTriggeredBlocks.add(new MutablePair<>(new MutablePair<>(BlockRotationUtils.mirrorOffsetBlockPos(triggeredBlock.getLeft().getLeft(), BlockMirror.LEFT_RIGHT), triggeredBlock.getLeft().getRight()), triggeredBlock.getRight()));
				}
				this.triggeredBlocks = newTriggeredBlocks;

				this.z_mirrored = state.get(RotatedBlockWithEntity.Z_MIRRORED);
			}
		}
	}
}
