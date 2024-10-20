package com.github.theredbrain.scriptblocks.block.entity;

import com.github.theredbrain.scriptblocks.block.ProvidesData;
import com.github.theredbrain.scriptblocks.block.Resetable;
import com.github.theredbrain.scriptblocks.block.RotatedBlockWithEntity;
import com.github.theredbrain.scriptblocks.block.Triggerable;
import com.github.theredbrain.scriptblocks.registry.EntityRegistry;
import com.github.theredbrain.scriptblocks.structure.pool.FixedRotationStructurePoolBasedGenerator;
import com.github.theredbrain.scriptblocks.util.BlockRotationUtils;
import net.minecraft.block.BlockState;
import net.minecraft.block.JigsawBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.JigsawBlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
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
	private static final BlockPos DATA_PROVIDING_BLOCK_POS_DEFAULT = new BlockPos(0, -1, 0);
	private static final RegistryKey<StructurePool> POOL_DEFAULT = RegistryKey.of(RegistryKeys.TEMPLATE_POOL, Identifier.of("empty"));
	private static final String CHECKED_DATA_ID_DEFAULT = "";
	public static final String TARGET_KEY = "target";
	public static final String JOINT_KEY = "joint";
	public static final String CHECKED_DATA_ID_KEY = "checked_data_id";
	private Identifier target = Identifier.of("empty");
	private List<String> structurePoolList = new ArrayList<>();
	private JigsawBlockEntity.Joint joint = JigsawBlockEntity.Joint.ROLLABLE;
	private MutablePair<BlockPos, Boolean> triggeredBlock = new MutablePair<>(new BlockPos(0, 0, 0), false);
	private BlockPos dataProvidingBlockPosOffset = DATA_PROVIDING_BLOCK_POS_DEFAULT;
	private String checkedDataId = CHECKED_DATA_ID_DEFAULT;

	public JigsawPlacerBlockEntity(BlockPos pos, BlockState state) {
		super(EntityRegistry.STRUCTURE_PLACER_BLOCK_ENTITY, pos, state);
	}

	@Override
	protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
		nbt.putString(TARGET_KEY, this.target.toString());

		nbt.putInt("structurePoolListSize", this.structurePoolList.size());

		for (int i = 0; i < this.structurePoolList.size(); i++) {
			nbt.putString("structurePoolList_" + i, this.structurePoolList.get(i));
		}

		nbt.putString(JOINT_KEY, this.joint.asString());

		nbt.putInt("triggeredBlockPositionOffsetX", this.triggeredBlock.getLeft().getX());
		nbt.putInt("triggeredBlockPositionOffsetY", this.triggeredBlock.getLeft().getY());
		nbt.putInt("triggeredBlockPositionOffsetZ", this.triggeredBlock.getLeft().getZ());
		nbt.putBoolean("triggeredBlockResets", this.triggeredBlock.getRight());

		if (this.dataProvidingBlockPosOffset != DATA_PROVIDING_BLOCK_POS_DEFAULT) {
			nbt.putInt("dataProvidingBlockPosOffsetX", this.dataProvidingBlockPosOffset.getX());
			nbt.putInt("dataProvidingBlockPosOffsetY", this.dataProvidingBlockPosOffset.getY());
			nbt.putInt("dataProvidingBlockPosOffsetZ", this.dataProvidingBlockPosOffset.getZ());
		} else {
			nbt.remove("dataProvidingBlockPosOffsetX");
			nbt.remove("dataProvidingBlockPosOffsetY");
			nbt.remove("dataProvidingBlockPosOffsetZ");
		}

		if (this.checkedDataId.equals(CHECKED_DATA_ID_DEFAULT)) {
			nbt.remove(CHECKED_DATA_ID_KEY);
		} else {
			nbt.putString(CHECKED_DATA_ID_KEY, this.checkedDataId);
		}

		super.writeNbt(nbt, registryLookup);
	}

	@Override
	protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
		this.target = Identifier.of(nbt.getString(TARGET_KEY));

		int listSize = nbt.getInt("structurePoolListSize");
		this.structurePoolList.clear();
		for (int i = 0; i < listSize; i++) {
			this.structurePoolList.add(nbt.getString("structurePoolList_" + i));
		}

		this.joint = JigsawBlockEntity.Joint.byName(nbt.getString(JOINT_KEY)).orElseGet(() -> JigsawBlock.getFacing(this.getCachedState()).getAxis().isHorizontal() ? JigsawBlockEntity.Joint.ALIGNED : JigsawBlockEntity.Joint.ROLLABLE);

		int x = MathHelper.clamp(nbt.getInt("triggeredBlockPositionOffsetX"), -48, 48);
		int y = MathHelper.clamp(nbt.getInt("triggeredBlockPositionOffsetY"), -48, 48);
		int z = MathHelper.clamp(nbt.getInt("triggeredBlockPositionOffsetZ"), -48, 48);
		this.triggeredBlock = new MutablePair<>(new BlockPos(x, y, z), nbt.getBoolean("triggeredBlockResets"));

		if (nbt.contains("dataProvidingBlockPosOffsetX", NbtElement.INT_TYPE) && nbt.contains("dataProvidingBlockPosOffsetY", NbtElement.INT_TYPE) && nbt.contains("dataProvidingBlockPosOffsetZ", NbtElement.INT_TYPE)) {
			this.dataProvidingBlockPosOffset = new BlockPos(
					MathHelper.clamp(nbt.getInt("dataProvidingBlockPosOffsetX"), -48, 48),
					MathHelper.clamp(nbt.getInt("dataProvidingBlockPosOffsetY"), -48, 48),
					MathHelper.clamp(nbt.getInt("dataProvidingBlockPosOffsetZ"), -48, 48)
			);
		}

		if (nbt.contains(CHECKED_DATA_ID_KEY)) {
			this.checkedDataId = nbt.getString(CHECKED_DATA_ID_KEY);
		} else {
			this.checkedDataId = CHECKED_DATA_ID_DEFAULT;
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

	public List<String> getStructurePoolList() {
		return structurePoolList;
	}

	public void setStructurePoolList(List<String> structurePoolList) {
		this.structurePoolList = structurePoolList;
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

	public BlockPos getDataProvidingBlockPosOffset() {
		return dataProvidingBlockPosOffset;
	}

	public void setDataProvidingBlockPosOffset(BlockPos dataProvidingBlockPosOffset) {
		this.dataProvidingBlockPosOffset = dataProvidingBlockPosOffset;
	}

	public String getCheckedDataId() {
		return checkedDataId;
	}

	public void setCheckedDataId(String checkedDataId) {
		this.checkedDataId = checkedDataId;
	}

	@Override
	public void trigger() {
		if (this.world != null) {
			if (this.world instanceof ServerWorld serverWorld) {
				BlockPos blockPos = this.getPos().offset(this.getCachedState().get(JigsawBlock.ORIENTATION).getFacing());
				Registry<StructurePool> registry = world.getRegistryManager().get(RegistryKeys.TEMPLATE_POOL);
				RegistryEntry.Reference<StructurePool> registryEntry = registry.entryOf(this.getCurrentPool(serverWorld));

				Direction rotation = this.getCachedState().get(JigsawBlock.ORIENTATION).getRotation();
				Direction facing = this.getCachedState().get(JigsawBlock.ORIENTATION).getFacing();

				FixedRotationStructurePoolBasedGenerator.generate(
						serverWorld,
						registryEntry,
						this.target,
						20,
						/*blockPos*/new BlockPos(blockPos.getX(), blockPos.getY() + 1, blockPos.getZ()), // offset y by 1 to fix vanilla bug
						false,
						facing == Direction.EAST ? BlockRotation.CLOCKWISE_90 : facing == Direction.SOUTH ? BlockRotation.CLOCKWISE_180 : facing == Direction.WEST ? BlockRotation.COUNTERCLOCKWISE_90 : facing == Direction.NORTH ? BlockRotation.NONE : rotation == Direction.EAST ? BlockRotation.CLOCKWISE_90 : rotation == Direction.SOUTH ? BlockRotation.CLOCKWISE_180 : rotation == Direction.WEST ? BlockRotation.COUNTERCLOCKWISE_90 : BlockRotation.NONE);
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
			if (state.get(RotatedBlockWithEntity.ROTATED) != this.rotated) {
				BlockRotation blockRotation = BlockRotationUtils.calculateRotationFromDifferentRotatedStates(state.get(RotatedBlockWithEntity.ROTATED), this.rotated);
				this.triggeredBlock.setLeft(BlockRotationUtils.rotateOffsetBlockPos(this.triggeredBlock.getLeft(), blockRotation));
				this.rotated = state.get(RotatedBlockWithEntity.ROTATED);
			}
			if (state.get(RotatedBlockWithEntity.X_MIRRORED) != this.x_mirrored) {
				this.triggeredBlock.setLeft(BlockRotationUtils.mirrorOffsetBlockPos(this.triggeredBlock.getLeft(), BlockMirror.FRONT_BACK));
				this.x_mirrored = state.get(RotatedBlockWithEntity.X_MIRRORED);
			}
			if (state.get(RotatedBlockWithEntity.Z_MIRRORED) != this.z_mirrored) {
				this.triggeredBlock.setLeft(BlockRotationUtils.mirrorOffsetBlockPos(this.triggeredBlock.getLeft(), BlockMirror.LEFT_RIGHT));
				this.z_mirrored = state.get(RotatedBlockWithEntity.Z_MIRRORED);
			}
		}
	}

	private RegistryKey<StructurePool> getCurrentPool(ServerWorld serverWorld) {
		RegistryKey<StructurePool> currentPool = POOL_DEFAULT;

		BlockPos dataBlockPos = this.dataProvidingBlockPosOffset;
		if (dataBlockPos != BlockPos.ORIGIN) {
			BlockEntity blockEntity1 = serverWorld.getBlockEntity(this.getPos().add(dataBlockPos.getX(), dataBlockPos.getY(), dataBlockPos.getZ()));
			if (blockEntity1 instanceof ProvidesData providesDataBlockEntity) {
				int data = providesDataBlockEntity.getData(this.checkedDataId);
				if (data < this.structurePoolList.size() && data >= 0) {

					currentPool = RegistryKey.of(RegistryKeys.TEMPLATE_POOL, Identifier.tryParse(this.structurePoolList.get(data)));
				}
			}
		} else if (!this.structurePoolList.isEmpty()) {
			currentPool = RegistryKey.of(RegistryKeys.TEMPLATE_POOL, Identifier.tryParse(this.structurePoolList.getFirst()));
		}
		return currentPool;
	}
}
