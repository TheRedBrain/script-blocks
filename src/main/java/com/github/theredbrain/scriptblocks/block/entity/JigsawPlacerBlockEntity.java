package com.github.theredbrain.scriptblocks.block.entity;

import com.github.theredbrain.scriptblocks.block.ProvidesData;
import com.github.theredbrain.scriptblocks.block.Resetable;
import com.github.theredbrain.scriptblocks.block.RotatedBlockWithEntity;
import com.github.theredbrain.scriptblocks.block.Triggerable;
import com.github.theredbrain.scriptblocks.registry.EntityRegistry;
import com.github.theredbrain.scriptblocks.structure.pool.FixedRotationStructurePoolBasedGenerator;
import com.github.theredbrain.scriptblocks.util.BlockRotationUtils;
import com.github.theredbrain.scriptblocks.util.DebuggingHelper;
import net.minecraft.block.BlockState;
import net.minecraft.block.JigsawBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.JigsawBlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.pool.StructurePool;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import org.apache.commons.lang3.tuple.MutablePair;

import java.util.ArrayList;
import java.util.List;

public class JigsawPlacerBlockEntity extends RotatedBlockEntity implements Triggerable {
	private static final RegistryKey<StructurePool> POOL_DEFAULT = RegistryKey.of(RegistryKeys.TEMPLATE_POOL, Identifier.of("empty"));
	private Identifier target = Identifier.of("empty");
	private String structurePoolString = "";
	private JigsawBlockEntity.Joint joint = JigsawBlockEntity.Joint.ROLLABLE;
	private MutablePair<BlockPos, Boolean> triggeredBlock = new MutablePair<>(BlockPos.ORIGIN, false);
	private final List<MutablePair<BlockPos, MutablePair<String, String>>> structurePoolStringAppendices = new ArrayList<>(List.of());

	public JigsawPlacerBlockEntity(BlockPos pos, BlockState state) {
		super(EntityRegistry.STRUCTURE_PLACER_BLOCK_ENTITY, pos, state);
	}

	@Override
	protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
		nbt.putString("target", this.target.toString());

		nbt.putString("joint", this.joint.asString());

		nbt.putInt("triggered_block_position_offset_x", this.triggeredBlock.getLeft().getX());
		nbt.putInt("triggered_block_position_offset_y", this.triggeredBlock.getLeft().getY());
		nbt.putInt("triggered_block_position_offset_z", this.triggeredBlock.getLeft().getZ());
		nbt.putBoolean("triggered_block_resets", this.triggeredBlock.getRight());

		nbt.putString("structure_pool_string", this.structurePoolString);

		nbt.putInt("appendices_size", structurePoolStringAppendices.size());
		for (int i = 0; i < this.structurePoolStringAppendices.size(); i++) {
			BlockPos appendixPositionOffset = this.structurePoolStringAppendices.get(i).getLeft();
			nbt.putInt("appendix_position_offset_x_" + i, appendixPositionOffset.getX());
			nbt.putInt("appendix_position_offset_y_" + i, appendixPositionOffset.getY());
			nbt.putInt("appendix_position_offset_z_" + i, appendixPositionOffset.getZ());
			nbt.putString("appendix_data_id_" + i, this.structurePoolStringAppendices.get(i).getRight().getLeft());
			nbt.putString("static_appendix_" + i, this.structurePoolStringAppendices.get(i).getRight().getRight());
		}

		super.writeNbt(nbt, registryLookup);
	}

	@Override
	protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {

		if (nbt.contains("firstStructurePoolString")) {
			this.structurePoolString = nbt.getString("firstStructurePoolString");
			nbt.remove("firstStructurePoolString");
		} else {
			this.structurePoolString = nbt.getString("structure_pool_string");
		}

		this.structurePoolStringAppendices.clear();

		if (nbt.contains("firstDataProvidingBlockPosOffsetX") || nbt.contains("firstDataProvidingBlockPosOffsetY") || nbt.contains("firstDataProvidingBlockPosOffsetZ") || nbt.contains("first_checked_data_id") || nbt.contains("secondStructurePoolString")) {
			this.structurePoolStringAppendices.add(
					new MutablePair<>(new BlockPos(
							MathHelper.clamp(nbt.getInt("firstDataProvidingBlockPosOffsetX"), -48, 48),
							MathHelper.clamp(nbt.getInt("firstDataProvidingBlockPosOffsetY"), -48, 48),
							MathHelper.clamp(nbt.getInt("firstDataProvidingBlockPosOffsetZ"), -48, 48)
					),
							new MutablePair<>(
									nbt.getString("first_checked_data_id"),
									nbt.getString("secondStructurePoolString")
							)
					)
			);
			nbt.remove("firstDataProvidingBlockPosOffsetX");
			nbt.remove("firstDataProvidingBlockPosOffsetY");
			nbt.remove("firstDataProvidingBlockPosOffsetZ");
			nbt.remove("first_checked_data_id");
			nbt.remove("secondStructurePoolString");
		}

		if (nbt.contains("secondDataProvidingBlockPosOffsetX") || nbt.contains("secondDataProvidingBlockPosOffsetY") || nbt.contains("secondDataProvidingBlockPosOffsetZ") || nbt.contains("first_checked_data_id") || nbt.contains("secondStructurePoolString")) {
			if (nbt.getInt("secondDataProvidingBlockPosOffsetX") != 0 || nbt.getInt("secondDataProvidingBlockPosOffsetY") != 0 || nbt.getInt("secondDataProvidingBlockPosOffsetZ") != 0) {
				this.structurePoolStringAppendices.add(
						new MutablePair<>(new BlockPos(
								MathHelper.clamp(nbt.getInt("secondDataProvidingBlockPosOffsetX"), -48, 48),
								MathHelper.clamp(nbt.getInt("secondDataProvidingBlockPosOffsetY"), -48, 48),
								MathHelper.clamp(nbt.getInt("secondDataProvidingBlockPosOffsetZ"), -48, 48)
						),
								new MutablePair<>(
										nbt.getString("second_checked_data_id"),
										""
								)
						)
				);
			}
			nbt.remove("secondDataProvidingBlockPosOffsetX");
			nbt.remove("secondDataProvidingBlockPosOffsetY");
			nbt.remove("secondDataProvidingBlockPosOffsetZ");
			nbt.remove("second_checked_data_id");
		}

		int appendicesSize = nbt.getInt("appendices_size");
		for (int i = 0; i < appendicesSize; i++) {
			this.structurePoolStringAppendices.add(
					new MutablePair<>(
							new BlockPos(
									MathHelper.clamp(nbt.getInt("appendix_position_offset_x_" + i), -48, 48),
									MathHelper.clamp(nbt.getInt("appendix_position_offset_y_" + i), -48, 48),
									MathHelper.clamp(nbt.getInt("appendix_position_offset_z_" + i), -48, 48)
							),
							new MutablePair<>(
									nbt.getString("appendix_data_id_" + i),
									nbt.getString("static_appendix_" + i)
							)
					)
			);
		}

		this.target = Identifier.of(nbt.getString("target"));

		this.joint = JigsawBlockEntity.Joint.byName(nbt.getString("joint")).orElseGet(() -> JigsawBlock.getFacing(this.getCachedState()).getAxis().isHorizontal() ? JigsawBlockEntity.Joint.ALIGNED : JigsawBlockEntity.Joint.ROLLABLE);

		if (nbt.contains("triggeredBlockPositionOffsetX") || nbt.contains("triggeredBlockPositionOffsetY") || nbt.contains("triggeredBlockPositionOffsetZ") || nbt.contains("triggeredBlockResets")) {
			int x = MathHelper.clamp(nbt.getInt("triggeredBlockPositionOffsetX"), -48, 48);
			int y = MathHelper.clamp(nbt.getInt("triggeredBlockPositionOffsetY"), -48, 48);
			int z = MathHelper.clamp(nbt.getInt("triggeredBlockPositionOffsetZ"), -48, 48);
			this.triggeredBlock = new MutablePair<>(new BlockPos(x, y, z), nbt.getBoolean("triggeredBlockResets"));
			nbt.remove("triggeredBlockPositionOffsetX");
			nbt.remove("triggeredBlockPositionOffsetY");
			nbt.remove("triggeredBlockPositionOffsetZ");
			nbt.remove("triggeredBlockResets");
		} else {
			int x = MathHelper.clamp(nbt.getInt("triggered_block_position_offset_x"), -48, 48);
			int y = MathHelper.clamp(nbt.getInt("triggered_block_position_offset_y"), -48, 48);
			int z = MathHelper.clamp(nbt.getInt("triggered_block_position_offset_z"), -48, 48);
			this.triggeredBlock = new MutablePair<>(new BlockPos(x, y, z), nbt.getBoolean("triggered_block_resets"));
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
	public Identifier getTarget() {
		return this.target;
	}

	public boolean setTarget(String target) {
		Identifier identifier = Identifier.tryParse(target);
		if (identifier != null) {
			this.target = identifier;
			return true;
		}
		return false;
	}

	public String getStructurePoolString() {
		return this.structurePoolString;
	}

	public void setStructurePoolString(String structurePoolString) {
		this.structurePoolString = structurePoolString;
	}

	public List<MutablePair<BlockPos, MutablePair<String, String>>> getStructurePoolStringAppendices() {
		return this.structurePoolStringAppendices;
	}

	public void setStructurePoolStringAppendices(List<MutablePair<BlockPos, MutablePair<String, String>>> structurePoolStringAppendices) {
		this.structurePoolStringAppendices.clear();
		this.structurePoolStringAppendices.addAll(structurePoolStringAppendices);
	}

	public JigsawBlockEntity.Joint getJoint() {
		return this.joint;
	}

	public void setJoint(JigsawBlockEntity.Joint joint) {
		this.joint = joint;
	}

	public MutablePair<BlockPos, Boolean> getTriggeredBlock() {
		return this.triggeredBlock;
	}

	public void setTriggeredBlock(MutablePair<BlockPos, Boolean> triggeredBlock) {
		this.triggeredBlock = triggeredBlock;
	}
	// endregion --- getter & setter ---

	@Override
	public void trigger() {
		if (this.world != null) {
			if (this.world instanceof ServerWorld serverWorld) {
				BlockPos blockPos = this.getPos().offset(this.getCachedState().get(JigsawBlock.ORIENTATION).getFacing());
				Registry<StructurePool> registry = world.getRegistryManager().get(RegistryKeys.TEMPLATE_POOL);
				try {
					RegistryEntry.Reference<StructurePool> registryEntry = registry.entryOf(this.getCurrentPool(serverWorld));

					Direction rotation = this.getCachedState().get(JigsawBlock.ORIENTATION).getRotation();
					Direction facing = this.getCachedState().get(JigsawBlock.ORIENTATION).getFacing();

					if (registryEntry.hasKeyAndValue()) {
						FixedRotationStructurePoolBasedGenerator.generate(
								serverWorld,
								registryEntry,
								this.target,
								20,
								new BlockPos(blockPos.getX(), blockPos.getY() + 1, blockPos.getZ()), // offset to fix vanilla bug
								false,
								facing == Direction.EAST ? BlockRotation.COUNTERCLOCKWISE_90 : facing == Direction.SOUTH ? BlockRotation.NONE : facing == Direction.WEST ? BlockRotation.CLOCKWISE_90 : facing == Direction.NORTH ? BlockRotation.CLOCKWISE_180 : this.joint == JigsawBlockEntity.Joint.ROLLABLE ? BlockRotation.random(serverWorld.getRandom()) : rotation == Direction.EAST ? BlockRotation.CLOCKWISE_90 : rotation == Direction.SOUTH ? BlockRotation.CLOCKWISE_180 : rotation == Direction.WEST ? BlockRotation.COUNTERCLOCKWISE_90 : BlockRotation.NONE
						);
					}
				} catch (IllegalStateException illegalStateException) {
					DebuggingHelper.sendJigsawPlacerLogMessage("JigsawPlacerBlock couldn't find a structure pool: " + illegalStateException.getMessage(), null);
					return;
				}
			}
			// trigger next block
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

	// TODO rework for list
	@Override
	protected void onRotate(BlockState state) {
		if (state.getBlock() instanceof RotatedBlockWithEntity) {
			if (state.get(RotatedBlockWithEntity.ROTATED) != this.rotated) {
				BlockRotation blockRotation = BlockRotationUtils.calculateRotationFromDifferentRotatedStates(state.get(RotatedBlockWithEntity.ROTATED), this.rotated);
				this.triggeredBlock.setLeft(BlockRotationUtils.rotateOffsetBlockPos(this.triggeredBlock.getLeft(), blockRotation));

				List<MutablePair<BlockPos, MutablePair<String, String>>> newStructurePoolStringAppendices = new ArrayList<>(List.of());
				for (MutablePair<BlockPos, MutablePair<String, String>> appendix : this.structurePoolStringAppendices) {
					newStructurePoolStringAppendices.add(new MutablePair<>(BlockRotationUtils.rotateOffsetBlockPos(appendix.getLeft(), blockRotation), new MutablePair<>(appendix.getRight().getLeft(), appendix.getRight().getLeft())));
				}
				this.structurePoolStringAppendices.clear();
				this.structurePoolStringAppendices.addAll(newStructurePoolStringAppendices);

				this.rotated = state.get(RotatedBlockWithEntity.ROTATED);
			}
			if (state.get(RotatedBlockWithEntity.X_MIRRORED) != this.x_mirrored) {
				this.triggeredBlock.setLeft(BlockRotationUtils.mirrorOffsetBlockPos(this.triggeredBlock.getLeft(), BlockMirror.FRONT_BACK));

				List<MutablePair<BlockPos, MutablePair<String, String>>> newStructurePoolStringAppendices = new ArrayList<>(List.of());
				for (MutablePair<BlockPos, MutablePair<String, String>> appendix : this.structurePoolStringAppendices) {
					newStructurePoolStringAppendices.add(new MutablePair<>(BlockRotationUtils.mirrorOffsetBlockPos(appendix.getLeft(), BlockMirror.FRONT_BACK), new MutablePair<>(appendix.getRight().getLeft(), appendix.getRight().getLeft())));
				}
				this.structurePoolStringAppendices.clear();
				this.structurePoolStringAppendices.addAll(newStructurePoolStringAppendices);

				this.x_mirrored = state.get(RotatedBlockWithEntity.X_MIRRORED);
			}
			if (state.get(RotatedBlockWithEntity.Z_MIRRORED) != this.z_mirrored) {
				this.triggeredBlock.setLeft(BlockRotationUtils.mirrorOffsetBlockPos(this.triggeredBlock.getLeft(), BlockMirror.LEFT_RIGHT));

				List<MutablePair<BlockPos, MutablePair<String, String>>> newStructurePoolStringAppendices = new ArrayList<>(List.of());
				for (MutablePair<BlockPos, MutablePair<String, String>> appendix : this.structurePoolStringAppendices) {
					newStructurePoolStringAppendices.add(new MutablePair<>(BlockRotationUtils.mirrorOffsetBlockPos(appendix.getLeft(), BlockMirror.LEFT_RIGHT), new MutablePair<>(appendix.getRight().getLeft(), appendix.getRight().getLeft())));
				}
				this.structurePoolStringAppendices.clear();
				this.structurePoolStringAppendices.addAll(newStructurePoolStringAppendices);

				this.z_mirrored = state.get(RotatedBlockWithEntity.Z_MIRRORED);
			}
		}
	}

	private RegistryKey<StructurePool> getCurrentPool(ServerWorld serverWorld) {
		RegistryKey<StructurePool> currentPool = POOL_DEFAULT;

		StringBuilder currentPoolIdentifierStringBuilder = new StringBuilder(this.structurePoolString);
		for (MutablePair<BlockPos, MutablePair<String, String>> listEntry : this.structurePoolStringAppendices) {
			if (!listEntry.getLeft().equals(BlockPos.ORIGIN)) {
				BlockEntity blockEntity1 = serverWorld.getBlockEntity(this.getPos().add(listEntry.getLeft().getX(), listEntry.getLeft().getY(), listEntry.getLeft().getZ()));
				if (blockEntity1 instanceof ProvidesData providesDataBlockEntity) {
					currentPoolIdentifierStringBuilder.append(providesDataBlockEntity.getData(listEntry.getRight().getLeft()));
				}
			}
			currentPoolIdentifierStringBuilder.append(listEntry.getRight().getRight());
		}
		if (!currentPoolIdentifierStringBuilder.isEmpty()) {
			currentPool = RegistryKey.of(RegistryKeys.TEMPLATE_POOL, Identifier.tryParse(currentPoolIdentifierStringBuilder.toString()));
		}
		return currentPool;
	}
}
