package com.github.theredbrain.scriptblocks.block.entity;

import com.github.theredbrain.scriptblocks.block.ProvidesData;
import com.github.theredbrain.scriptblocks.block.Resetable;
import com.github.theredbrain.scriptblocks.block.RotatedBlockWithEntity;
import com.github.theredbrain.scriptblocks.block.Triggerable;
import com.github.theredbrain.scriptblocks.registry.ScriptBlocksEntities;
import com.github.theredbrain.scriptblocks.util.BlockRotationUtils;
import com.github.theredbrain.scriptblocks.util.ScriptBlocksBlockProperties;
import com.github.theredbrain.scriptblocks.util.UUIDUtilities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.scores.PlayerTeam;
import org.apache.commons.lang3.tuple.MutablePair;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class TriggeredRNGBlockEntity extends RotatedBlockEntity implements Triggerable {
	private static final BlockPos OVERRIDE_TRIGGERED_BLOCK_POS_DEFAULT = new BlockPos(0, -1, 0);
	private static final BlockPos FALLBACK_TRIGGERED_BLOCK_POS_DEFAULT = new BlockPos(0, 1, 0);

	private BlockPos dataProvidingBlockPosOffset = BlockPos.ZERO;
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
	protected void saveAdditional(ValueOutput view) {

		view.store("data_providing_block_position_offset", BlockPos.CODEC, this.dataProvidingBlockPosOffset);

		view.putString("override_data_identifier", this.overrideDataIdentifier);

		view.putString("override_data_value", this.overrideDataValue);

		CompoundTag nbtCompound = new CompoundTag();
		nbtCompound.store("position_offset", BlockPos.CODEC, this.overrideTriggeredBlock.getLeft());
		nbtCompound.putBoolean("resets", this.overrideTriggeredBlock.getRight());
		view.store("override_triggered_block", CompoundTag.CODEC, nbtCompound.copy());

		view.putString("influencing_attribute_identifier_string", this.influencingAttributeIdentifierString);

		view.putBoolean("checks_team_attributes", false);

		view.putBoolean("is_affected_by_luck", false);

		view.putInt("random_min_value", this.randomMinValue);

		view.putInt("random_max_value", this.randomMaxValue);

		nbtCompound = new CompoundTag();
		nbtCompound.store("position_offset", BlockPos.CODEC, this.fallbackTriggeredBlock.getLeft());
		nbtCompound.putBoolean("resets", this.fallbackTriggeredBlock.getRight());
		view.store("fallback_triggered_block", CompoundTag.CODEC, nbtCompound.copy());

		ValueOutput.TypedOutputList<CompoundTag> listAppender = view.list("triggered_blocks", CompoundTag.CODEC);
		for (MutablePair<MutablePair<BlockPos, Boolean>, Integer> triggeredBlock : this.triggeredBlocks) {
			nbtCompound = new CompoundTag();
			nbtCompound.store("position_offset", BlockPos.CODEC, triggeredBlock.getLeft().getLeft());
			nbtCompound.putBoolean("resets", triggeredBlock.getLeft().getRight());
			nbtCompound.putInt("threshold", triggeredBlock.getRight());
			listAppender.add(nbtCompound.copy());
		}

		super.saveAdditional(view);
	}

	@Override
	protected void loadAdditional(ValueInput view) {

		this.dataProvidingBlockPosOffset = BlockPos.max(
				BlockPos.min(
						view.read("data_providing_block_pos_offset", BlockPos.CODEC).orElse(BlockPos.ZERO),
						new BlockPos(48, 48, 48)
				),
				new BlockPos(-48, -48, -48)
		);

		this.overrideDataIdentifier = view.getStringOr("override_data_identifier", "");

		this.overrideDataValue = view.getStringOr("override_data_value", "");

		CompoundTag nbtCompound = view.read("override_triggered_block", CompoundTag.CODEC).orElseGet(CompoundTag::new);
		this.overrideTriggeredBlock = new MutablePair<>(
				BlockPos.max(
						BlockPos.min(
								nbtCompound.read("position_offset", BlockPos.CODEC).orElse(BlockPos.ZERO),
								new BlockPos(48, 48, 48)
						),
						new BlockPos(-48, -48, -48)
				),
				nbtCompound.getBooleanOr("resets", false)
		);

		this.influencingAttributeIdentifierString = view.getStringOr("influencing_attribute_identifier_string", "");

		this.checksTeamAttributes = view.getBooleanOr("checks_team_attributes", true);

		this.isAffectedByLuck = view.getBooleanOr("is_affected_by_luck", true);

		this.randomMinValue = view.getIntOr("random_min_value", 0);

		this.randomMaxValue = view.getIntOr("random_max_value", 1);

		nbtCompound = view.read("fallback_triggered_block", CompoundTag.CODEC).orElseGet(CompoundTag::new);
		this.fallbackTriggeredBlock = new MutablePair<>(
				BlockPos.max(
						BlockPos.min(
								nbtCompound.read("position_offset", BlockPos.CODEC).orElse(BlockPos.ZERO),
								new BlockPos(48, 48, 48)
						),
						new BlockPos(-48, -48, -48)
				),
				nbtCompound.getBooleanOr("resets", false)
		);

		this.triggeredBlocks.clear();
		for (CompoundTag nbtCompound1 : view.listOrEmpty("triggered_blocks", CompoundTag.CODEC)) {
			this.triggeredBlocks.add(
					new MutablePair<>(
							new MutablePair<>(
									nbtCompound1.read("position_offset", BlockPos.CODEC).orElse(BlockPos.ZERO),
									nbtCompound1.getBooleanOr("resets", false)
							),
							Mth.clamp(nbtCompound1.getIntOr("threshold", 0), 0, 100)
					)
			);
		}

		super.loadAdditional(view);
	}

	public ClientboundBlockEntityDataPacket getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}

	@Override
	public CompoundTag getUpdateTag(HolderLookup.Provider registryLookup) {
		return this.saveCustomOnly(registryLookup);
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
		if (this.level != null) {
			String worldName = this.level.dimension().identifier().getPath();
			MinecraftServer server = this.level.getServer();

			boolean overrideIsActive = false;

			// check if override is active
			if (this.dataProvidingBlockPosOffset != BlockPos.ZERO && !this.overrideDataIdentifier.isEmpty() && !this.overrideDataValue.isEmpty()) {
				BlockPos dataProviderBlockPos = new BlockPos(this.worldPosition.getX() + this.dataProvidingBlockPosOffset.getX(), this.worldPosition.getY() + this.dataProvidingBlockPosOffset.getY(), this.worldPosition.getZ() + this.dataProvidingBlockPosOffset.getZ());
				BlockEntity blockEntity = level.getBlockEntity(dataProviderBlockPos);
				if (blockEntity instanceof ProvidesData providesDataEntity) {
					overrideIsActive = Objects.equals(providesDataEntity.getData(this.overrideDataIdentifier), this.overrideDataValue);
				}
			}

			if (overrideIsActive) {
				BlockEntity blockEntity = level.getBlockEntity(new BlockPos(this.worldPosition.getX() + this.overrideTriggeredBlock.getLeft().getX(), this.worldPosition.getY() + this.overrideTriggeredBlock.getLeft().getY(), this.worldPosition.getZ() + this.overrideTriggeredBlock.getLeft().getZ()));
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

				Optional<Holder.Reference<Attribute>> attributeEntry = BuiltInRegistries.ATTRIBUTE.get(Identifier.tryParse(attributeString));

				ServerPlayer serverPlayerEntity = null;
				if (server != null && UUIDUtilities.isStringValidUUID(worldName)) {
					serverPlayerEntity = server.getPlayerList().getPlayer(UUID.fromString(worldName));
				}
				if (serverPlayerEntity != null) {
					luck += serverPlayerEntity.getLuck();
					if (attributeEntry.isPresent()) {
						attributeValue += (float) serverPlayerEntity.getAttributeValue(attributeEntry.get());
					}
					if (this.checksTeamAttributes) {
						PlayerTeam team = serverPlayerEntity.getTeam();
						if (team != null) {
							for (String playerString : team.getPlayers()) {
								ServerPlayer teamServerPlayerEntity = server.getPlayerList().getPlayerByName(playerString);
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

				int randomNumber = this.level.getRandom().nextIntBetweenInclusive(this.randomMinValue, this.randomMaxValue);
				if (this.isAffectedByLuck) {
					boolean rollWithAdvantage = luck > 0;
					int additionalRollAmount = (int) Math.floor(Math.abs(luck));
					for (int i = 0; i < additionalRollAmount; i++) {
						int newRandomNumber = this.level.getRandom().nextIntBetweenInclusive(this.randomMinValue, this.randomMaxValue);
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

				MutablePair<BlockPos, Boolean> triggeredBlock = new MutablePair<>(BlockPos.ZERO, false);

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
				if (Objects.equals(triggeredBlock, new MutablePair<>(BlockPos.ZERO, false))) {
					triggeredBlock = this.fallbackTriggeredBlock;
				}

				// trigger chosen triggeredBlock
				BlockEntity blockEntity = level.getBlockEntity(triggeredBlock.left);
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
			if (state.getValue(ScriptBlocksBlockProperties.ROTATED) != this.rotated) {
				Rotation blockRotation = BlockRotationUtils.calculateRotationFromDifferentRotatedStates(state.getValue(ScriptBlocksBlockProperties.ROTATED), this.rotated);

				this.dataProvidingBlockPosOffset = BlockRotationUtils.rotateOffsetBlockPos(this.dataProvidingBlockPosOffset, blockRotation);

				this.overrideTriggeredBlock.setLeft(BlockRotationUtils.rotateOffsetBlockPos(this.overrideTriggeredBlock.getLeft(), blockRotation));

				this.fallbackTriggeredBlock.setLeft(BlockRotationUtils.rotateOffsetBlockPos(this.fallbackTriggeredBlock.getLeft(), blockRotation));

				List<MutablePair<MutablePair<BlockPos, Boolean>, Integer>> newTriggeredBlocks = new ArrayList<>(List.of());
				for (MutablePair<MutablePair<BlockPos, Boolean>, Integer> triggeredBlock : this.triggeredBlocks) {
					newTriggeredBlocks.add(new MutablePair<>(new MutablePair<>(BlockRotationUtils.rotateOffsetBlockPos(triggeredBlock.getLeft().getLeft(), blockRotation), triggeredBlock.getLeft().getRight()), triggeredBlock.getRight()));
				}
				this.triggeredBlocks.clear();
				this.triggeredBlocks.addAll(newTriggeredBlocks);

				this.rotated = state.getValue(ScriptBlocksBlockProperties.ROTATED);
			}
			if (state.getValue(ScriptBlocksBlockProperties.X_MIRRORED) != this.x_mirrored) {

				this.dataProvidingBlockPosOffset = BlockRotationUtils.mirrorOffsetBlockPos(this.dataProvidingBlockPosOffset, Mirror.FRONT_BACK);

				this.overrideTriggeredBlock.setLeft(BlockRotationUtils.mirrorOffsetBlockPos(this.overrideTriggeredBlock.getLeft(), Mirror.FRONT_BACK));

				this.fallbackTriggeredBlock.setLeft(BlockRotationUtils.mirrorOffsetBlockPos(this.fallbackTriggeredBlock.getLeft(), Mirror.FRONT_BACK));

				List<MutablePair<MutablePair<BlockPos, Boolean>, Integer>> newTriggeredBlocks = new ArrayList<>(List.of());
				for (MutablePair<MutablePair<BlockPos, Boolean>, Integer> triggeredBlock : this.triggeredBlocks) {
					newTriggeredBlocks.add(new MutablePair<>(new MutablePair<>(BlockRotationUtils.mirrorOffsetBlockPos(triggeredBlock.getLeft().getLeft(), Mirror.FRONT_BACK), triggeredBlock.getLeft().getRight()), triggeredBlock.getRight()));
				}
				this.triggeredBlocks.clear();
				this.triggeredBlocks.addAll(newTriggeredBlocks);

				this.x_mirrored = state.getValue(ScriptBlocksBlockProperties.X_MIRRORED);
			}
			if (state.getValue(ScriptBlocksBlockProperties.Z_MIRRORED) != this.z_mirrored) {

				this.dataProvidingBlockPosOffset = BlockRotationUtils.mirrorOffsetBlockPos(this.dataProvidingBlockPosOffset, Mirror.LEFT_RIGHT);

				this.overrideTriggeredBlock.setLeft(BlockRotationUtils.mirrorOffsetBlockPos(this.overrideTriggeredBlock.getLeft(), Mirror.LEFT_RIGHT));

				this.fallbackTriggeredBlock.setLeft(BlockRotationUtils.mirrorOffsetBlockPos(this.fallbackTriggeredBlock.getLeft(), Mirror.LEFT_RIGHT));

				List<MutablePair<MutablePair<BlockPos, Boolean>, Integer>> newTriggeredBlocks = new ArrayList<>(List.of());
				for (MutablePair<MutablePair<BlockPos, Boolean>, Integer> triggeredBlock : this.triggeredBlocks) {
					newTriggeredBlocks.add(new MutablePair<>(new MutablePair<>(BlockRotationUtils.mirrorOffsetBlockPos(triggeredBlock.getLeft().getLeft(), Mirror.LEFT_RIGHT), triggeredBlock.getLeft().getRight()), triggeredBlock.getRight()));
				}
				this.triggeredBlocks.clear();
				this.triggeredBlocks.addAll(newTriggeredBlocks);

				this.z_mirrored = state.getValue(ScriptBlocksBlockProperties.Z_MIRRORED);
			}
		}
	}
}
