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
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.structure.pool.StructurePool;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
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
		super(ScriptBlocksEntities.JIGSAW_PLACER_BLOCK_ENTITY, pos, state);
	}

	@Override
	protected void writeData(WriteView view) {

		view.putString("target", this.target.toString());

		view.putString("structure_pool_string", this.structurePoolString);

		view.putString("joint", this.joint.asString());

		NbtCompound nbtCompound = new NbtCompound();
		nbtCompound.put("position_offset", BlockPos.CODEC, this.triggeredBlock.getLeft());
		nbtCompound.putBoolean("resets", this.triggeredBlock.getRight());
		view.put("triggered_block", NbtCompound.CODEC, nbtCompound.copy());

		WriteView.ListAppender<NbtCompound> listAppender = view.getListAppender("appendices", NbtCompound.CODEC);
		for (MutablePair<BlockPos, MutablePair<String, String>> appendix : this.structurePoolStringAppendices) {
			nbtCompound = new NbtCompound();
			nbtCompound.put("position_offset", BlockPos.CODEC, appendix.getLeft());
			nbtCompound.putString("data_id", appendix.getRight().getLeft());
			nbtCompound.putString("static_appendix", appendix.getRight().getRight());
			listAppender.add(nbtCompound.copy());
		}

		super.writeData(view);
	}

	@Override
	protected void readData(ReadView view) {

		this.target = Identifier.of(view.getString("target", ""));

		this.structurePoolString = view.getString("structure_pool_string", "");

		this.joint = view.read("joint", JigsawBlockEntity.Joint.CODEC).orElseGet(() -> JigsawBlock.getFacing(this.getCachedState()).getAxis().isHorizontal() ? JigsawBlockEntity.Joint.ALIGNED : JigsawBlockEntity.Joint.ROLLABLE);

		NbtCompound nbtCompound = view.read("triggered_block", NbtCompound.CODEC).orElseGet(NbtCompound::new);
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

		this.structurePoolStringAppendices.clear();
		for (NbtCompound nbtCompound1 : view.getTypedListView("appendices", NbtCompound.CODEC)) {
			this.structurePoolStringAppendices.add(
					new MutablePair<>(
							BlockPos.max(
									BlockPos.min(
											nbtCompound1.get("position_offset", BlockPos.CODEC).orElse(BlockPos.ORIGIN),
											new BlockPos(48, 48, 48)
									),
									new BlockPos(-48, -48, -48)
							),
							new MutablePair<>(
									nbtCompound1.getString("appendix_data_id", ""),
									nbtCompound1.getString("static_appendix", "")
							)
					)
			);
		}

		super.readData(view);
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

	@Override
	protected void onRotate(BlockState state) {
		if (state.getBlock() instanceof RotatedBlockWithEntity) {
			if (state.get(ScriptBlocksBlockProperties.ROTATED) != this.rotated) {
				BlockRotation blockRotation = BlockRotationUtils.calculateRotationFromDifferentRotatedStates(state.get(ScriptBlocksBlockProperties.ROTATED), this.rotated);
				this.triggeredBlock.setLeft(BlockRotationUtils.rotateOffsetBlockPos(this.triggeredBlock.getLeft(), blockRotation));

				List<MutablePair<BlockPos, MutablePair<String, String>>> newStructurePoolStringAppendices = new ArrayList<>(List.of());
				for (MutablePair<BlockPos, MutablePair<String, String>> appendix : this.structurePoolStringAppendices) {
					newStructurePoolStringAppendices.add(new MutablePair<>(BlockRotationUtils.rotateOffsetBlockPos(appendix.getLeft(), blockRotation), new MutablePair<>(appendix.getRight().getLeft(), appendix.getRight().getRight())));
				}
				this.structurePoolStringAppendices.clear();
				this.structurePoolStringAppendices.addAll(newStructurePoolStringAppendices);

				this.rotated = state.get(ScriptBlocksBlockProperties.ROTATED);
			}
			if (state.get(ScriptBlocksBlockProperties.X_MIRRORED) != this.x_mirrored) {
				this.triggeredBlock.setLeft(BlockRotationUtils.mirrorOffsetBlockPos(this.triggeredBlock.getLeft(), BlockMirror.FRONT_BACK));

				List<MutablePair<BlockPos, MutablePair<String, String>>> newStructurePoolStringAppendices = new ArrayList<>(List.of());
				for (MutablePair<BlockPos, MutablePair<String, String>> appendix : this.structurePoolStringAppendices) {
					newStructurePoolStringAppendices.add(new MutablePair<>(BlockRotationUtils.mirrorOffsetBlockPos(appendix.getLeft(), BlockMirror.FRONT_BACK), new MutablePair<>(appendix.getRight().getLeft(), appendix.getRight().getRight())));
				}
				this.structurePoolStringAppendices.clear();
				this.structurePoolStringAppendices.addAll(newStructurePoolStringAppendices);

				this.x_mirrored = state.get(ScriptBlocksBlockProperties.X_MIRRORED);
			}
			if (state.get(ScriptBlocksBlockProperties.Z_MIRRORED) != this.z_mirrored) {
				this.triggeredBlock.setLeft(BlockRotationUtils.mirrorOffsetBlockPos(this.triggeredBlock.getLeft(), BlockMirror.LEFT_RIGHT));

				List<MutablePair<BlockPos, MutablePair<String, String>>> newStructurePoolStringAppendices = new ArrayList<>(List.of());
				for (MutablePair<BlockPos, MutablePair<String, String>> appendix : this.structurePoolStringAppendices) {
					newStructurePoolStringAppendices.add(new MutablePair<>(BlockRotationUtils.mirrorOffsetBlockPos(appendix.getLeft(), BlockMirror.LEFT_RIGHT), new MutablePair<>(appendix.getRight().getLeft(), appendix.getRight().getRight())));
				}
				this.structurePoolStringAppendices.clear();
				this.structurePoolStringAppendices.addAll(newStructurePoolStringAppendices);

				this.z_mirrored = state.get(ScriptBlocksBlockProperties.Z_MIRRORED);
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
