package com.github.theredbrain.scriptblocks.block.entity;

import com.github.theredbrain.scriptblocks.block.Resetable;
import com.github.theredbrain.scriptblocks.block.RotatedBlockWithEntity;
import com.github.theredbrain.scriptblocks.block.Triggerable;
import com.github.theredbrain.scriptblocks.registry.ScriptBlocksEntities;
import com.github.theredbrain.scriptblocks.util.BlockRotationUtils;
import com.github.theredbrain.scriptblocks.util.ScriptBlocksBlockProperties;
import com.github.theredbrain.scriptblocks.util.UUIDUtilities;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.apache.commons.lang3.tuple.MutablePair;

import java.util.UUID;

public class TriggeredAdvancementCheckerBlockEntity extends RotatedBlockEntity implements Triggerable {
	// TODO rework
	//  contains a list of triggered blocks with corresponding advancements
	private MutablePair<BlockPos, Boolean> firstTriggeredBlock = new MutablePair<>(new BlockPos(0, 1, 0), false);
	private MutablePair<BlockPos, Boolean> secondTriggeredBlock = new MutablePair<>(new BlockPos(0, -1, 0), false);
	private String checkedAdvancementIdentifier = "";

	public TriggeredAdvancementCheckerBlockEntity(BlockPos pos, BlockState state) {
		super(ScriptBlocksEntities.TRIGGERED_ADVANCEMENT_CHECKER_BLOCK_ENTITY, pos, state);
	}

	@Override
	protected void saveAdditional(ValueOutput view) {

		view.putInt("firstTriggeredBlockPositionOffsetX", this.firstTriggeredBlock.getLeft().getX());
		view.putInt("firstTriggeredBlockPositionOffsetY", this.firstTriggeredBlock.getLeft().getY());
		view.putInt("firstTriggeredBlockPositionOffsetZ", this.firstTriggeredBlock.getLeft().getZ());
		view.putBoolean("firstTriggeredBlockResets", this.firstTriggeredBlock.getRight());

		view.putInt("secondTriggeredBlockPositionOffsetX", this.secondTriggeredBlock.getLeft().getX());
		view.putInt("secondTriggeredBlockPositionOffsetY", this.secondTriggeredBlock.getLeft().getY());
		view.putInt("secondTriggeredBlockPositionOffsetZ", this.secondTriggeredBlock.getLeft().getZ());
		view.putBoolean("secondTriggeredBlockResets", this.secondTriggeredBlock.getRight());

		view.putString("checkedAdvancementIdentifier", this.checkedAdvancementIdentifier);

		super.saveAdditional(view);
	}

	@Override
	protected void loadAdditional(ValueInput view) {

		int x = Mth.clamp(view.getIntOr("firstTriggeredBlockPositionOffsetX", 0), -48, 48);
		int y = Mth.clamp(view.getIntOr("firstTriggeredBlockPositionOffsetY", 0), -48, 48);
		int z = Mth.clamp(view.getIntOr("firstTriggeredBlockPositionOffsetZ", 0), -48, 48);
		this.firstTriggeredBlock = new MutablePair<>(new BlockPos(x, y, z), view.getBooleanOr("firstTriggeredBlockResets", false));

		x = Mth.clamp(view.getIntOr("secondTriggeredBlockPositionOffsetX", 0), -48, 48);
		y = Mth.clamp(view.getIntOr("secondTriggeredBlockPositionOffsetY", 0), -48, 48);
		z = Mth.clamp(view.getIntOr("secondTriggeredBlockPositionOffsetZ", 0), -48, 48);
		this.secondTriggeredBlock = new MutablePair<>(new BlockPos(x, y, z), view.getBooleanOr("secondTriggeredBlockResets", false));

		this.checkedAdvancementIdentifier = view.getStringOr("checkedAdvancementIdentifier", "");

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
	public MutablePair<BlockPos, Boolean> getFirstTriggeredBlock() {
		return this.firstTriggeredBlock;
	}

	public void setFirstTriggeredBlock(MutablePair<BlockPos, Boolean> firstTriggeredBlock) {
		this.firstTriggeredBlock = firstTriggeredBlock;
	}

	public MutablePair<BlockPos, Boolean> getSecondTriggeredBlock() {
		return this.secondTriggeredBlock;
	}

	public void setSecondTriggeredBlock(MutablePair<BlockPos, Boolean> secondTriggeredBlock) {
		this.secondTriggeredBlock = secondTriggeredBlock;
	}

	public String getCheckedAdvancementIdentifier() {
		return this.checkedAdvancementIdentifier;
	}

	public boolean setCheckedAdvancementIdentifier(String checkedAdvancementIdentifier) {
		this.checkedAdvancementIdentifier = checkedAdvancementIdentifier;
		return true;
	}
	// endregion --- getter & setter ---

	@Override
	public void trigger() {
		if (this.level != null) {
			String worldName = this.level.dimension().identifier().getPath();
			MinecraftServer server = this.level.getServer();
			BlockPos triggeredBlockPos = new BlockPos(this.worldPosition.getX() + this.secondTriggeredBlock.getLeft().getX(), this.worldPosition.getY() + this.secondTriggeredBlock.getLeft().getY(), this.worldPosition.getZ() + this.secondTriggeredBlock.getLeft().getZ());
			boolean triggeredBlockResets = this.secondTriggeredBlock.getRight();
			if (server != null && UUIDUtilities.isStringValidUUID(worldName)) {
				ServerPlayer serverPlayerEntity = server.getPlayerList().getPlayer(UUID.fromString(worldName));
				if (serverPlayerEntity != null) {
					PlayerAdvancements advancementTracker = server.getPlayerList().getPlayerAdvancements(serverPlayerEntity);
					if (advancementTracker != null) {
						AdvancementHolder checkedAdvancementEntry = server.getAdvancements().get(Identifier.tryParse(this.checkedAdvancementIdentifier));
						if (checkedAdvancementEntry != null && advancementTracker.getOrStartProgress(checkedAdvancementEntry).isDone()) {
							triggeredBlockPos = new BlockPos(this.worldPosition.getX() + this.firstTriggeredBlock.getLeft().getX(), this.worldPosition.getY() + this.firstTriggeredBlock.getLeft().getY(), this.worldPosition.getZ() + this.firstTriggeredBlock.getLeft().getZ());
							triggeredBlockResets = this.firstTriggeredBlock.getRight();
						}
					}
				}
			}
			BlockEntity blockEntity = level.getBlockEntity(triggeredBlockPos);
			if (blockEntity != this) {
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

				this.firstTriggeredBlock.setLeft(BlockRotationUtils.rotateOffsetBlockPos(this.firstTriggeredBlock.getLeft(), blockRotation));

				this.secondTriggeredBlock.setLeft(BlockRotationUtils.rotateOffsetBlockPos(this.secondTriggeredBlock.getLeft(), blockRotation));

				this.rotated = state.getValue(ScriptBlocksBlockProperties.ROTATED);
			}
			if (state.getValue(ScriptBlocksBlockProperties.X_MIRRORED) != this.x_mirrored) {

				this.firstTriggeredBlock.setLeft(BlockRotationUtils.mirrorOffsetBlockPos(this.firstTriggeredBlock.getLeft(), Mirror.FRONT_BACK));

				this.secondTriggeredBlock.setLeft(BlockRotationUtils.mirrorOffsetBlockPos(this.secondTriggeredBlock.getLeft(), Mirror.FRONT_BACK));

				this.x_mirrored = state.getValue(ScriptBlocksBlockProperties.X_MIRRORED);
			}
			if (state.getValue(ScriptBlocksBlockProperties.Z_MIRRORED) != this.z_mirrored) {

				this.firstTriggeredBlock.setLeft(BlockRotationUtils.mirrorOffsetBlockPos(this.firstTriggeredBlock.getLeft(), Mirror.LEFT_RIGHT));

				this.secondTriggeredBlock.setLeft(BlockRotationUtils.mirrorOffsetBlockPos(this.secondTriggeredBlock.getLeft(), Mirror.LEFT_RIGHT));

				this.z_mirrored = state.getValue(ScriptBlocksBlockProperties.Z_MIRRORED);
			}
		}
	}
}
