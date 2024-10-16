package com.github.theredbrain.scriptblocks.block.entity;

import com.github.theredbrain.scriptblocks.block.Resetable;
import com.github.theredbrain.scriptblocks.block.RotatedBlockWithEntity;
import com.github.theredbrain.scriptblocks.block.Triggerable;
import com.github.theredbrain.scriptblocks.registry.EntityRegistry;
import com.github.theredbrain.scriptblocks.util.BlockRotationUtils;
import com.github.theredbrain.scriptblocks.util.UUIDUtilities;
import net.minecraft.advancement.AdvancementEntry;
import net.minecraft.advancement.PlayerAdvancementTracker;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import org.apache.commons.lang3.tuple.MutablePair;

import java.util.UUID;

public class TriggeredAdvancementCheckerBlockEntity extends RotatedBlockEntity implements Triggerable {
	// TODO rework
	//  contains a list of triggered blocks with corresponding advancements
	private MutablePair<BlockPos, Boolean> firstTriggeredBlock = new MutablePair<>(new BlockPos(0, 1, 0), false);
	private MutablePair<BlockPos, Boolean> secondTriggeredBlock = new MutablePair<>(new BlockPos(0, -1, 0), false);
	private String checkedAdvancementIdentifier = "";

	public TriggeredAdvancementCheckerBlockEntity(BlockPos pos, BlockState state) {
		super(EntityRegistry.TRIGGERED_ADVANCEMENT_CHECKER_BLOCK_ENTITY, pos, state);
	}

	@Override
	protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {

		nbt.putInt("firstTriggeredBlockPositionOffsetX", this.firstTriggeredBlock.getLeft().getX());
		nbt.putInt("firstTriggeredBlockPositionOffsetY", this.firstTriggeredBlock.getLeft().getY());
		nbt.putInt("firstTriggeredBlockPositionOffsetZ", this.firstTriggeredBlock.getLeft().getZ());
		nbt.putBoolean("firstTriggeredBlockResets", this.firstTriggeredBlock.getRight());

		nbt.putInt("secondTriggeredBlockPositionOffsetX", this.secondTriggeredBlock.getLeft().getX());
		nbt.putInt("secondTriggeredBlockPositionOffsetY", this.secondTriggeredBlock.getLeft().getY());
		nbt.putInt("secondTriggeredBlockPositionOffsetZ", this.secondTriggeredBlock.getLeft().getZ());
		nbt.putBoolean("secondTriggeredBlockResets", this.secondTriggeredBlock.getRight());

		nbt.putString("checkedAdvancementIdentifier", this.checkedAdvancementIdentifier);

		super.writeNbt(nbt, registryLookup);
	}

	@Override
	protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {

		int x = MathHelper.clamp(nbt.getInt("firstTriggeredBlockPositionOffsetX"), -48, 48);
		int y = MathHelper.clamp(nbt.getInt("firstTriggeredBlockPositionOffsetY"), -48, 48);
		int z = MathHelper.clamp(nbt.getInt("firstTriggeredBlockPositionOffsetZ"), -48, 48);
		this.firstTriggeredBlock = new MutablePair<>(new BlockPos(x, y, z), nbt.getBoolean("firstTriggeredBlockResets"));

		x = MathHelper.clamp(nbt.getInt("secondTriggeredBlockPositionOffsetX"), -48, 48);
		y = MathHelper.clamp(nbt.getInt("secondTriggeredBlockPositionOffsetY"), -48, 48);
		z = MathHelper.clamp(nbt.getInt("secondTriggeredBlockPositionOffsetZ"), -48, 48);
		this.secondTriggeredBlock = new MutablePair<>(new BlockPos(x, y, z), nbt.getBoolean("secondTriggeredBlockResets"));

		this.checkedAdvancementIdentifier = nbt.getString("checkedAdvancementIdentifier");

		super.readNbt(nbt, registryLookup);
	}

	public BlockEntityUpdateS2CPacket toUpdatePacket() {
		return BlockEntityUpdateS2CPacket.create(this);
	}

	@Override
	public NbtCompound toInitialChunkDataNbt(RegistryWrapper.WrapperLookup registryLookup) {
		return this.createComponentlessNbt(registryLookup);
	}

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

	@Override
	public void trigger() {
		if (this.world != null) {
			String worldName = this.world.getRegistryKey().getValue().getPath();
			MinecraftServer server = this.world.getServer();
			BlockPos triggeredBlockPos = new BlockPos(this.pos.getX() + this.secondTriggeredBlock.getLeft().getX(), this.pos.getY() + this.secondTriggeredBlock.getLeft().getY(), this.pos.getZ() + this.secondTriggeredBlock.getLeft().getZ());
			boolean triggeredBlockResets = this.secondTriggeredBlock.getRight();
			if (server != null && UUIDUtilities.isStringValidUUID(worldName)) {
				ServerPlayerEntity serverPlayerEntity = server.getPlayerManager().getPlayer(UUID.fromString(worldName));
				if (serverPlayerEntity != null) {
					PlayerAdvancementTracker advancementTracker = server.getPlayerManager().getAdvancementTracker(serverPlayerEntity);
					if (advancementTracker != null) {
						AdvancementEntry checkedAdvancementEntry = server.getAdvancementLoader().get(Identifier.tryParse(this.checkedAdvancementIdentifier));
						if (checkedAdvancementEntry != null && advancementTracker.getProgress(checkedAdvancementEntry).isDone()) {
							triggeredBlockPos = new BlockPos(this.pos.getX() + this.firstTriggeredBlock.getLeft().getX(), this.pos.getY() + this.firstTriggeredBlock.getLeft().getY(), this.pos.getZ() + this.firstTriggeredBlock.getLeft().getZ());
							triggeredBlockResets = this.firstTriggeredBlock.getRight();
						}
					}
				}
			}
			BlockEntity blockEntity = world.getBlockEntity(triggeredBlockPos);
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
			if (state.get(RotatedBlockWithEntity.ROTATED) != this.rotated) {
				BlockRotation blockRotation = BlockRotationUtils.calculateRotationFromDifferentRotatedStates(state.get(RotatedBlockWithEntity.ROTATED), this.rotated);

				this.firstTriggeredBlock.setLeft(BlockRotationUtils.rotateOffsetBlockPos(this.firstTriggeredBlock.getLeft(), blockRotation));

				this.secondTriggeredBlock.setLeft(BlockRotationUtils.rotateOffsetBlockPos(this.secondTriggeredBlock.getLeft(), blockRotation));

				this.rotated = state.get(RotatedBlockWithEntity.ROTATED);
			}
			if (state.get(RotatedBlockWithEntity.X_MIRRORED) != this.x_mirrored) {

				this.firstTriggeredBlock.setLeft(BlockRotationUtils.mirrorOffsetBlockPos(this.firstTriggeredBlock.getLeft(), BlockMirror.FRONT_BACK));

				this.secondTriggeredBlock.setLeft(BlockRotationUtils.mirrorOffsetBlockPos(this.secondTriggeredBlock.getLeft(), BlockMirror.FRONT_BACK));

				this.x_mirrored = state.get(RotatedBlockWithEntity.X_MIRRORED);
			}
			if (state.get(RotatedBlockWithEntity.Z_MIRRORED) != this.z_mirrored) {

				this.firstTriggeredBlock.setLeft(BlockRotationUtils.mirrorOffsetBlockPos(this.firstTriggeredBlock.getLeft(), BlockMirror.LEFT_RIGHT));

				this.secondTriggeredBlock.setLeft(BlockRotationUtils.mirrorOffsetBlockPos(this.secondTriggeredBlock.getLeft(), BlockMirror.LEFT_RIGHT));

				this.z_mirrored = state.get(RotatedBlockWithEntity.Z_MIRRORED);
			}
		}
	}
}
