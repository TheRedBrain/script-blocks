package com.github.theredbrain.scriptblocks.block.entity;

import com.github.theredbrain.scriptblocks.block.ProvidesData;
import com.github.theredbrain.scriptblocks.block.Resetable;
import com.github.theredbrain.scriptblocks.block.RotatedBlockWithEntity;
import com.github.theredbrain.scriptblocks.block.Triggerable;
import com.github.theredbrain.scriptblocks.registry.ScriptBlocksEntities;
import com.github.theredbrain.scriptblocks.util.BlockRotationUtils;
import com.github.theredbrain.scriptblocks.util.UUIDUtilities;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtList;
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
	private MutablePair<BlockPos, Boolean> fallbackTriggeredBlock = new MutablePair<>(FALLBACK_TRIGGERED_BLOCK_POS_DEFAULT, false);
	private final List<MutablePair<MutablePair<BlockPos, Boolean>, Integer>> triggeredBlocks = new ArrayList<>();

	public TriggeredRNGBlockEntity(BlockPos pos, BlockState state) {
		super(ScriptBlocksEntities.TRIGGERED_RNG_BLOCK_ENTITY, pos, state);
	}

	@Override
	protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {

		nbt.put("data_providing_block_position_offset", NbtHelper.fromBlockPos(this.dataProvidingBlockPosOffset));

		nbt.putString("override_data_identifier", this.overrideDataIdentifier);

		nbt.putString("override_data_value", this.overrideDataValue);

		NbtCompound nbtCompound = new NbtCompound();
		nbtCompound.put("position_offset", NbtHelper.fromBlockPos(this.overrideTriggeredBlock.getLeft()));
		nbtCompound.putBoolean("resets", this.overrideTriggeredBlock.getRight());
		nbt.put("override_triggered_block", nbtCompound.copy());

		nbt.putString("influencing_attribute_identifier_string", this.influencingAttributeIdentifierString);

		nbt.putBoolean("checks_team_attributes", false);

		nbt.putBoolean("is_affected_by_luck", false);

		nbt.putInt("random_min_value", this.randomMinValue);

		nbt.putInt("random_max_value", this.randomMaxValue);

		nbtCompound = new NbtCompound();
		nbtCompound.put("position_offset", NbtHelper.fromBlockPos(this.fallbackTriggeredBlock.getLeft()));
		nbtCompound.putBoolean("resets", this.fallbackTriggeredBlock.getRight());
		nbt.put("fallback_triggered_block", nbtCompound.copy());

		NbtList nbtList = new NbtList();
		for (MutablePair<MutablePair<BlockPos, Boolean>, Integer> triggeredBlock : this.triggeredBlocks) {
			nbtCompound = new NbtCompound();
			nbtCompound.put("position_offset", NbtHelper.fromBlockPos(triggeredBlock.getLeft().getLeft()));
			nbtCompound.putBoolean("resets", triggeredBlock.getLeft().getRight());
			nbtCompound.putInt("threshold", triggeredBlock.getRight());
			nbtList.add(nbtCompound.copy());
		}
		nbt.put("triggered_blocks", nbtList);

		super.writeNbt(nbt, registryLookup);
	}

	@Override
	protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {

		if (nbt.contains("dataProvidingBlockPosOffsetX") || nbt.contains("dataProvidingBlockPosOffsetY") || nbt.contains("dataProvidingBlockPosOffsetZ")) {
			this.dataProvidingBlockPosOffset = new BlockPos(
					MathHelper.clamp(nbt.getInt("dataProvidingBlockPosOffsetX"), -48, 48),
					MathHelper.clamp(nbt.getInt("dataProvidingBlockPosOffsetY"), -48, 48),
					MathHelper.clamp(nbt.getInt("dataProvidingBlockPosOffsetZ"), -48, 48)
			);
			nbt.remove("dataProvidingBlockPosOffsetX");
			nbt.remove("dataProvidingBlockPosOffsetY");
			nbt.remove("dataProvidingBlockPosOffsetZ");
		} else {
			this.dataProvidingBlockPosOffset = BlockPos.max(
					BlockPos.min(
							NbtHelper.toBlockPos(nbt, "data_providing_block_pos_offset").orElse(BlockPos.ORIGIN),
							new BlockPos(48, 48, 48)
					),
					new BlockPos(-48, -48, -48)
			);
		}

		if (nbt.contains("overrideDataIdentifier")) {
			this.overrideDataIdentifier = nbt.getString("overrideDataIdentifier");
			nbt.remove("overrideDataIdentifier");
		} else {
			this.overrideDataIdentifier = nbt.getString("override_data_identifier");
		}

		if (nbt.contains("overrideDataValue")) {
			this.overrideDataValue = nbt.getString("overrideDataValue");
			nbt.remove("overrideDataValue");
		} else {
			this.overrideDataValue = nbt.getString("override_data_value");
		}

		if (nbt.contains("overrideTriggeredBlockPositionOffsetX") || nbt.contains("overrideTriggeredBlockPositionOffsetY") || nbt.contains("overrideTriggeredBlockPositionOffsetZ") || nbt.contains("overrideTriggeredBlockResets")) {
			this.overrideTriggeredBlock = new MutablePair<>(
					new BlockPos(
							MathHelper.clamp(nbt.getInt("overrideTriggeredBlockPositionOffsetX"), -48, 48),
							MathHelper.clamp(nbt.getInt("overrideTriggeredBlockPositionOffsetY"), -48, 48),
							MathHelper.clamp(nbt.getInt("overrideTriggeredBlockPositionOffsetZ"), -48, 48)
					),
					nbt.getBoolean("overrideTriggeredBlockResets")
			);
			nbt.remove("overrideTriggeredBlockPositionOffsetX");
			nbt.remove("overrideTriggeredBlockPositionOffsetY");
			nbt.remove("overrideTriggeredBlockPositionOffsetZ");
			nbt.remove("overrideTriggeredBlockResets");
		} else {
			NbtCompound nbtCompound = nbt.getCompound("override_triggered_block");
			this.overrideTriggeredBlock = new MutablePair<>(
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

		if (nbt.contains("influencingAttributeIdentifierString")) {
			this.influencingAttributeIdentifierString = nbt.getString("influencingAttributeIdentifierString");
			nbt.remove("influencingAttributeIdentifierString");
		} else {
			this.influencingAttributeIdentifierString = nbt.getString("influencing_attribute_identifier_string");
		}

		if (nbt.contains("checksTeamAttributes")) {
			this.checksTeamAttributes = nbt.getBoolean("checksTeamAttributes");
			nbt.remove("checksTeamAttributes");
		} else {
			this.checksTeamAttributes = nbt.getBoolean("checks_team_attributes");
		}

		if (nbt.contains("isAffectedByLuck")) {
			this.isAffectedByLuck = nbt.getBoolean("isAffectedByLuck");
			nbt.remove("isAffectedByLuck");
		} else {
			this.isAffectedByLuck = nbt.getBoolean("is_affected_by_luck");
		}

		if (nbt.contains("randomMinValue")) {
			this.randomMinValue = nbt.getInt("randomMinValue");
			nbt.remove("randomMinValue");
		} else {
			this.randomMinValue = nbt.getInt("random_min_value");
		}

		if (nbt.contains("randomMaxValue")) {
			this.randomMaxValue = nbt.getInt("randomMaxValue");
			nbt.remove("randomMaxValue");
		} else {
			this.randomMaxValue = nbt.getInt("random_max_value");
		}

		if (nbt.contains("fallbackTriggeredBlockPositionOffsetX") || nbt.contains("fallbackTriggeredBlockPositionOffsetY") || nbt.contains("fallbackTriggeredBlockPositionOffsetZ") || nbt.contains("fallbackTriggeredBlockResets")) {
			this.fallbackTriggeredBlock = new MutablePair<>(
					new BlockPos(
							MathHelper.clamp(nbt.getInt("fallbackTriggeredBlockPositionOffsetX"), -48, 48),
							MathHelper.clamp(nbt.getInt("fallbackTriggeredBlockPositionOffsetY"), -48, 48),
							MathHelper.clamp(nbt.getInt("fallbackTriggeredBlockPositionOffsetZ"), -48, 48)
					),
					nbt.getBoolean("fallbackTriggeredBlockResets")
			);
			nbt.remove("fallbackTriggeredBlockPositionOffsetX");
			nbt.remove("fallbackTriggeredBlockPositionOffsetY");
			nbt.remove("fallbackTriggeredBlockPositionOffsetZ");
			nbt.remove("fallbackTriggeredBlockResets");
		} else {
			NbtCompound nbtCompound = nbt.getCompound("fallback_triggered_block");
			this.fallbackTriggeredBlock = new MutablePair<>(
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

		this.triggeredBlocks.clear();
		if (nbt.contains("triggeredBlocksSize")) {
			int triggeredBlocksSize = nbt.getInt("triggeredBlocksSize");
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
				nbt.remove("triggeredBlockPositionOffsetX_" + i);
				nbt.remove("triggeredBlockPositionOffsetY_" + i);
				nbt.remove("triggeredBlockPositionOffsetZ_" + i);
				nbt.remove("triggeredBlockResets_" + i);
				nbt.remove("triggeredBlockThreshold_" + i);
			}
			nbt.remove("triggeredBlocksSize");
		} else {
			NbtList nbtList = nbt.getList("triggered_blocks", NbtElement.COMPOUND_TYPE);
			for (int i = 0; i < nbtList.size(); i++) {
				NbtCompound nbtCompound = nbtList.getCompound(i);
				this.triggeredBlocks.add(
						new MutablePair<>(
								new MutablePair<>(
										NbtHelper.toBlockPos(nbtCompound, "position_offset").orElse(BlockPos.ORIGIN),
										nbtCompound.getBoolean("resets")
								),
								MathHelper.clamp(nbtCompound.getInt("threshold"), 0, 100)
						)
				);
			}
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

	// region --- getter & setter ---
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
		this.triggeredBlocks.clear();
		this.triggeredBlocks.addAll(triggeredBlocks);
	}
	// endregion --- getter & setter ---

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

				// fallback
				if (Objects.equals(triggeredBlock, new MutablePair<>(BlockPos.ORIGIN, false))) {
					triggeredBlock = this.fallbackTriggeredBlock;
				}

				// trigger chosen triggeredBlock
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

	@Override
	protected void onRotate(BlockState state) {
		if (state.getBlock() instanceof RotatedBlockWithEntity) {
			if (state.get(RotatedBlockWithEntity.ROTATED) != this.rotated) {
				BlockRotation blockRotation = BlockRotationUtils.calculateRotationFromDifferentRotatedStates(state.get(RotatedBlockWithEntity.ROTATED), this.rotated);

				this.dataProvidingBlockPosOffset = BlockRotationUtils.rotateOffsetBlockPos(this.dataProvidingBlockPosOffset, blockRotation);

				this.overrideTriggeredBlock.setLeft(BlockRotationUtils.rotateOffsetBlockPos(this.overrideTriggeredBlock.getLeft(), blockRotation));

				this.fallbackTriggeredBlock.setLeft(BlockRotationUtils.rotateOffsetBlockPos(this.fallbackTriggeredBlock.getLeft(), blockRotation));

				List<MutablePair<MutablePair<BlockPos, Boolean>, Integer>> newTriggeredBlocks = new ArrayList<>(List.of());
				for (MutablePair<MutablePair<BlockPos, Boolean>, Integer> triggeredBlock : this.triggeredBlocks) {
					newTriggeredBlocks.add(new MutablePair<>(new MutablePair<>(BlockRotationUtils.rotateOffsetBlockPos(triggeredBlock.getLeft().getLeft(), blockRotation), triggeredBlock.getLeft().getRight()), triggeredBlock.getRight()));
				}
				this.triggeredBlocks.clear();
				this.triggeredBlocks.addAll(newTriggeredBlocks);

				this.rotated = state.get(RotatedBlockWithEntity.ROTATED);
			}
			if (state.get(RotatedBlockWithEntity.X_MIRRORED) != this.x_mirrored) {

				this.dataProvidingBlockPosOffset = BlockRotationUtils.mirrorOffsetBlockPos(this.dataProvidingBlockPosOffset, BlockMirror.FRONT_BACK);

				this.overrideTriggeredBlock.setLeft(BlockRotationUtils.mirrorOffsetBlockPos(this.overrideTriggeredBlock.getLeft(), BlockMirror.FRONT_BACK));

				this.fallbackTriggeredBlock.setLeft(BlockRotationUtils.mirrorOffsetBlockPos(this.fallbackTriggeredBlock.getLeft(), BlockMirror.FRONT_BACK));

				List<MutablePair<MutablePair<BlockPos, Boolean>, Integer>> newTriggeredBlocks = new ArrayList<>(List.of());
				for (MutablePair<MutablePair<BlockPos, Boolean>, Integer> triggeredBlock : this.triggeredBlocks) {
					newTriggeredBlocks.add(new MutablePair<>(new MutablePair<>(BlockRotationUtils.mirrorOffsetBlockPos(triggeredBlock.getLeft().getLeft(), BlockMirror.FRONT_BACK), triggeredBlock.getLeft().getRight()), triggeredBlock.getRight()));
				}
				this.triggeredBlocks.clear();
				this.triggeredBlocks.addAll(newTriggeredBlocks);

				this.x_mirrored = state.get(RotatedBlockWithEntity.X_MIRRORED);
			}
			if (state.get(RotatedBlockWithEntity.Z_MIRRORED) != this.z_mirrored) {

				this.dataProvidingBlockPosOffset = BlockRotationUtils.mirrorOffsetBlockPos(this.dataProvidingBlockPosOffset, BlockMirror.LEFT_RIGHT);

				this.overrideTriggeredBlock.setLeft(BlockRotationUtils.mirrorOffsetBlockPos(this.overrideTriggeredBlock.getLeft(), BlockMirror.LEFT_RIGHT));

				this.fallbackTriggeredBlock.setLeft(BlockRotationUtils.mirrorOffsetBlockPos(this.fallbackTriggeredBlock.getLeft(), BlockMirror.LEFT_RIGHT));

				List<MutablePair<MutablePair<BlockPos, Boolean>, Integer>> newTriggeredBlocks = new ArrayList<>(List.of());
				for (MutablePair<MutablePair<BlockPos, Boolean>, Integer> triggeredBlock : this.triggeredBlocks) {
					newTriggeredBlocks.add(new MutablePair<>(new MutablePair<>(BlockRotationUtils.mirrorOffsetBlockPos(triggeredBlock.getLeft().getLeft(), BlockMirror.LEFT_RIGHT), triggeredBlock.getLeft().getRight()), triggeredBlock.getRight()));
				}
				this.triggeredBlocks.clear();
				this.triggeredBlocks.addAll(newTriggeredBlocks);

				this.z_mirrored = state.get(RotatedBlockWithEntity.Z_MIRRORED);
			}
		}
	}
}
