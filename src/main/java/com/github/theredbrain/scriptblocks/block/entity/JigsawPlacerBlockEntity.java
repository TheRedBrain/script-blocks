package com.github.theredbrain.scriptblocks.block.entity;

import com.github.theredbrain.scriptblocks.block.ProvidesData;
import com.github.theredbrain.scriptblocks.block.Resetable;
import com.github.theredbrain.scriptblocks.block.RotatedBlockWithEntity;
import com.github.theredbrain.scriptblocks.block.Triggerable;
import com.github.theredbrain.scriptblocks.registry.ScriptBlocksEntities;
import com.github.theredbrain.scriptblocks.structure.pool.FixedRotationStructurePoolBasedGenerator;
import com.github.theredbrain.scriptblocks.util.BlockRotationUtils;
import com.github.theredbrain.scriptblocks.util.DebuggingHelper;
import com.github.theredbrain.scriptblocks.util.ScriptBlocksBlockProperties;
import org.apache.commons.lang3.tuple.MutablePair;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.JigsawBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.JigsawBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class JigsawPlacerBlockEntity extends RotatedBlockEntity implements Triggerable {
	private static final ResourceKey<StructureTemplatePool> POOL_DEFAULT = ResourceKey.create(Registries.TEMPLATE_POOL, Identifier.parse("empty"));
	private Identifier target = Identifier.parse("empty");
	private String structurePoolString = "";
	private JigsawBlockEntity.JointType joint = JigsawBlockEntity.JointType.ROLLABLE;
	private MutablePair<BlockPos, Boolean> triggeredBlock = new MutablePair<>(BlockPos.ZERO, false);
	private final List<MutablePair<BlockPos, MutablePair<String, String>>> structurePoolStringAppendices = new ArrayList<>(List.of());

	public JigsawPlacerBlockEntity(BlockPos pos, BlockState state) {
		super(ScriptBlocksEntities.JIGSAW_PLACER_BLOCK_ENTITY, pos, state);
	}

	@Override
	protected void saveAdditional(ValueOutput view) {

		view.putString("target", this.target.toString());

		view.putString("structure_pool_string", this.structurePoolString);

		view.putString("joint", this.joint.getSerializedName());

		CompoundTag nbtCompound = new CompoundTag();
		nbtCompound.store("position_offset", BlockPos.CODEC, this.triggeredBlock.getLeft());
		nbtCompound.putBoolean("resets", this.triggeredBlock.getRight());
		view.store("triggered_block", CompoundTag.CODEC, nbtCompound.copy());

		ValueOutput.TypedOutputList<CompoundTag> listAppender = view.list("appendices", CompoundTag.CODEC);
		for (MutablePair<BlockPos, MutablePair<String, String>> appendix : this.structurePoolStringAppendices) {
			nbtCompound = new CompoundTag();
			nbtCompound.store("position_offset", BlockPos.CODEC, appendix.getLeft());
			nbtCompound.putString("data_id", appendix.getRight().getLeft());
			nbtCompound.putString("static_appendix", appendix.getRight().getRight());
			listAppender.add(nbtCompound.copy());
		}

		super.saveAdditional(view);
	}

	@Override
	protected void loadAdditional(ValueInput view) {

		this.target = Identifier.parse(view.getStringOr("target", ""));

		this.structurePoolString = view.getStringOr("structure_pool_string", "");

		this.joint = view.read("joint", JigsawBlockEntity.JointType.CODEC).orElseGet(() -> JigsawBlock.getFrontFacing(this.getBlockState()).getAxis().isHorizontal() ? JigsawBlockEntity.JointType.ALIGNED : JigsawBlockEntity.JointType.ROLLABLE);

		CompoundTag nbtCompound = view.read("triggered_block", CompoundTag.CODEC).orElseGet(CompoundTag::new);
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

		this.structurePoolStringAppendices.clear();
		for (CompoundTag nbtCompound1 : view.listOrEmpty("appendices", CompoundTag.CODEC)) {
			this.structurePoolStringAppendices.add(
					new MutablePair<>(
							BlockPos.max(
									BlockPos.min(
											nbtCompound1.read("position_offset", BlockPos.CODEC).orElse(BlockPos.ZERO),
											new BlockPos(48, 48, 48)
									),
									new BlockPos(-48, -48, -48)
							),
							new MutablePair<>(
									nbtCompound1.getStringOr("appendix_data_id", ""),
									nbtCompound1.getStringOr("static_appendix", "")
							)
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

	public JigsawBlockEntity.JointType getJoint() {
		return this.joint;
	}

	public void setJoint(JigsawBlockEntity.JointType joint) {
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
		if (this.level != null) {
			if (this.level instanceof ServerLevel serverWorld) {
				BlockPos blockPos = this.getBlockPos().relative(this.getBlockState().getValue(JigsawBlock.ORIENTATION).front());
				Registry<StructureTemplatePool> registry = level.registryAccess().lookupOrThrow(Registries.TEMPLATE_POOL);
				try {
					Holder.Reference<StructureTemplatePool> registryEntry = registry.getOrThrow(this.getCurrentPool(serverWorld));

					Direction rotation = this.getBlockState().getValue(JigsawBlock.ORIENTATION).top();
					Direction facing = this.getBlockState().getValue(JigsawBlock.ORIENTATION).front();

					if (registryEntry.isBound()) {
						FixedRotationStructurePoolBasedGenerator.generate(
								serverWorld,
								registryEntry,
								this.target,
								20,
								new BlockPos(blockPos.getX(), blockPos.getY() + 1, blockPos.getZ()), // offset to fix vanilla bug
								false,
								facing == Direction.EAST ? Rotation.COUNTERCLOCKWISE_90 : facing == Direction.SOUTH ? Rotation.NONE : facing == Direction.WEST ? Rotation.CLOCKWISE_90 : facing == Direction.NORTH ? Rotation.CLOCKWISE_180 : this.joint == JigsawBlockEntity.JointType.ROLLABLE ? Rotation.getRandom(serverWorld.getRandom()) : rotation == Direction.EAST ? Rotation.CLOCKWISE_90 : rotation == Direction.SOUTH ? Rotation.CLOCKWISE_180 : rotation == Direction.WEST ? Rotation.COUNTERCLOCKWISE_90 : Rotation.NONE
						);
					}
				} catch (IllegalStateException illegalStateException) {
					DebuggingHelper.sendJigsawPlacerLogMessage("JigsawPlacerBlock couldn't find a structure pool: " + illegalStateException.getMessage(), null);
					return;
				}
			}
			// trigger next block
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

				List<MutablePair<BlockPos, MutablePair<String, String>>> newStructurePoolStringAppendices = new ArrayList<>(List.of());
				for (MutablePair<BlockPos, MutablePair<String, String>> appendix : this.structurePoolStringAppendices) {
					newStructurePoolStringAppendices.add(new MutablePair<>(BlockRotationUtils.rotateOffsetBlockPos(appendix.getLeft(), blockRotation), new MutablePair<>(appendix.getRight().getLeft(), appendix.getRight().getRight())));
				}
				this.structurePoolStringAppendices.clear();
				this.structurePoolStringAppendices.addAll(newStructurePoolStringAppendices);

				this.rotated = state.getValue(ScriptBlocksBlockProperties.ROTATED);
			}
			if (state.getValue(ScriptBlocksBlockProperties.X_MIRRORED) != this.x_mirrored) {
				this.triggeredBlock.setLeft(BlockRotationUtils.mirrorOffsetBlockPos(this.triggeredBlock.getLeft(), Mirror.FRONT_BACK));

				List<MutablePair<BlockPos, MutablePair<String, String>>> newStructurePoolStringAppendices = new ArrayList<>(List.of());
				for (MutablePair<BlockPos, MutablePair<String, String>> appendix : this.structurePoolStringAppendices) {
					newStructurePoolStringAppendices.add(new MutablePair<>(BlockRotationUtils.mirrorOffsetBlockPos(appendix.getLeft(), Mirror.FRONT_BACK), new MutablePair<>(appendix.getRight().getLeft(), appendix.getRight().getRight())));
				}
				this.structurePoolStringAppendices.clear();
				this.structurePoolStringAppendices.addAll(newStructurePoolStringAppendices);

				this.x_mirrored = state.getValue(ScriptBlocksBlockProperties.X_MIRRORED);
			}
			if (state.getValue(ScriptBlocksBlockProperties.Z_MIRRORED) != this.z_mirrored) {
				this.triggeredBlock.setLeft(BlockRotationUtils.mirrorOffsetBlockPos(this.triggeredBlock.getLeft(), Mirror.LEFT_RIGHT));

				List<MutablePair<BlockPos, MutablePair<String, String>>> newStructurePoolStringAppendices = new ArrayList<>(List.of());
				for (MutablePair<BlockPos, MutablePair<String, String>> appendix : this.structurePoolStringAppendices) {
					newStructurePoolStringAppendices.add(new MutablePair<>(BlockRotationUtils.mirrorOffsetBlockPos(appendix.getLeft(), Mirror.LEFT_RIGHT), new MutablePair<>(appendix.getRight().getLeft(), appendix.getRight().getRight())));
				}
				this.structurePoolStringAppendices.clear();
				this.structurePoolStringAppendices.addAll(newStructurePoolStringAppendices);

				this.z_mirrored = state.getValue(ScriptBlocksBlockProperties.Z_MIRRORED);
			}
		}
	}

	private ResourceKey<StructureTemplatePool> getCurrentPool(ServerLevel serverWorld) {
		ResourceKey<StructureTemplatePool> currentPool = POOL_DEFAULT;

		StringBuilder currentPoolIdentifierStringBuilder = new StringBuilder(this.structurePoolString);
		for (MutablePair<BlockPos, MutablePair<String, String>> listEntry : this.structurePoolStringAppendices) {
			if (!listEntry.getLeft().equals(BlockPos.ZERO)) {
				BlockEntity blockEntity1 = serverWorld.getBlockEntity(this.getBlockPos().offset(listEntry.getLeft().getX(), listEntry.getLeft().getY(), listEntry.getLeft().getZ()));
				if (blockEntity1 instanceof ProvidesData providesDataBlockEntity) {
					currentPoolIdentifierStringBuilder.append(providesDataBlockEntity.getData(listEntry.getRight().getLeft()));
				}
			}
			currentPoolIdentifierStringBuilder.append(listEntry.getRight().getRight());
		}
		if (!currentPoolIdentifierStringBuilder.isEmpty()) {
			currentPool = ResourceKey.create(Registries.TEMPLATE_POOL, Identifier.tryParse(currentPoolIdentifierStringBuilder.toString()));
		}
		return currentPool;
	}
}
