package com.github.theredbrain.scriptblocks.block.entity;

import com.github.theredbrain.scriptblocks.ScriptBlocks;
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

public class JigsawPlacerBlockEntity extends RotatedBlockEntity implements Triggerable {
	private static final BlockPos DATA_PROVIDING_BLOCK_POS_DEFAULT = new BlockPos(0, 0, 0);
	private static final RegistryKey<StructurePool> POOL_DEFAULT = RegistryKey.of(RegistryKeys.TEMPLATE_POOL, Identifier.of("empty"));
	private static final String CHECKED_DATA_ID_DEFAULT = "";
	public static final String TARGET_KEY = "target";
	public static final String JOINT_KEY = "joint";
	public static final String FIRST_CHECKED_DATA_ID_KEY = "first_checked_data_id";
	public static final String SECOND_CHECKED_DATA_ID_KEY = "second_checked_data_id";
	private Identifier target = Identifier.of("empty");
	private String firstStructurePoolString = "";
	private String secondStructurePoolString = "";
	private JigsawBlockEntity.Joint joint = JigsawBlockEntity.Joint.ROLLABLE;
	private MutablePair<BlockPos, Boolean> triggeredBlock = new MutablePair<>(new BlockPos(0, 0, 0), false);
	private BlockPos firstDataProvidingBlockPosOffset = DATA_PROVIDING_BLOCK_POS_DEFAULT;
	private BlockPos secondDataProvidingBlockPosOffset = DATA_PROVIDING_BLOCK_POS_DEFAULT;
	private String firstCheckedDataId = CHECKED_DATA_ID_DEFAULT;
	private String secondCheckedDataId = CHECKED_DATA_ID_DEFAULT;

	public JigsawPlacerBlockEntity(BlockPos pos, BlockState state) {
		super(EntityRegistry.STRUCTURE_PLACER_BLOCK_ENTITY, pos, state);
	}

	@Override
	protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
		nbt.putString(TARGET_KEY, this.target.toString());

		nbt.putString("firstStructurePoolString", this.firstStructurePoolString);

		nbt.putString("secondStructurePoolString", this.secondStructurePoolString);

		nbt.putString(JOINT_KEY, this.joint.asString());

		nbt.putInt("triggeredBlockPositionOffsetX", this.triggeredBlock.getLeft().getX());
		nbt.putInt("triggeredBlockPositionOffsetY", this.triggeredBlock.getLeft().getY());
		nbt.putInt("triggeredBlockPositionOffsetZ", this.triggeredBlock.getLeft().getZ());
		nbt.putBoolean("triggeredBlockResets", this.triggeredBlock.getRight());

		if (this.firstDataProvidingBlockPosOffset != DATA_PROVIDING_BLOCK_POS_DEFAULT) {
			nbt.putInt("firstDataProvidingBlockPosOffsetX", this.firstDataProvidingBlockPosOffset.getX());
			nbt.putInt("firstDataProvidingBlockPosOffsetY", this.firstDataProvidingBlockPosOffset.getY());
			nbt.putInt("firstDataProvidingBlockPosOffsetZ", this.firstDataProvidingBlockPosOffset.getZ());
		} else {
			nbt.remove("firstDataProvidingBlockPosOffsetX");
			nbt.remove("firstDataProvidingBlockPosOffsetY");
			nbt.remove("firstDataProvidingBlockPosOffsetZ");
		}

		if (this.secondDataProvidingBlockPosOffset != DATA_PROVIDING_BLOCK_POS_DEFAULT) {
			nbt.putInt("secondDataProvidingBlockPosOffsetX", this.secondDataProvidingBlockPosOffset.getX());
			nbt.putInt("secondDataProvidingBlockPosOffsetY", this.secondDataProvidingBlockPosOffset.getY());
			nbt.putInt("secondDataProvidingBlockPosOffsetZ", this.secondDataProvidingBlockPosOffset.getZ());
		} else {
			nbt.remove("secondDataProvidingBlockPosOffsetX");
			nbt.remove("secondDataProvidingBlockPosOffsetY");
			nbt.remove("secondDataProvidingBlockPosOffsetZ");
		}

		if (this.firstCheckedDataId.equals(CHECKED_DATA_ID_DEFAULT)) {
			nbt.remove(FIRST_CHECKED_DATA_ID_KEY);
		} else {
			nbt.putString(FIRST_CHECKED_DATA_ID_KEY, this.firstCheckedDataId);
		}

		if (this.secondCheckedDataId.equals(CHECKED_DATA_ID_DEFAULT)) {
			nbt.remove(SECOND_CHECKED_DATA_ID_KEY);
		} else {
			nbt.putString(SECOND_CHECKED_DATA_ID_KEY, this.secondCheckedDataId);
		}

		super.writeNbt(nbt, registryLookup);
	}

	@Override
	protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
		this.target = Identifier.of(nbt.getString(TARGET_KEY));

		this.firstStructurePoolString = nbt.getString("firstStructurePoolString");

		this.secondStructurePoolString = nbt.getString("secondStructurePoolString");

		this.joint = JigsawBlockEntity.Joint.byName(nbt.getString(JOINT_KEY)).orElseGet(() -> JigsawBlock.getFacing(this.getCachedState()).getAxis().isHorizontal() ? JigsawBlockEntity.Joint.ALIGNED : JigsawBlockEntity.Joint.ROLLABLE);

		int x = MathHelper.clamp(nbt.getInt("triggeredBlockPositionOffsetX"), -48, 48);
		int y = MathHelper.clamp(nbt.getInt("triggeredBlockPositionOffsetY"), -48, 48);
		int z = MathHelper.clamp(nbt.getInt("triggeredBlockPositionOffsetZ"), -48, 48);
		this.triggeredBlock = new MutablePair<>(new BlockPos(x, y, z), nbt.getBoolean("triggeredBlockResets"));

		if (nbt.contains("firstDataProvidingBlockPosOffsetX", NbtElement.INT_TYPE) && nbt.contains("firstDataProvidingBlockPosOffsetY", NbtElement.INT_TYPE) && nbt.contains("firstDataProvidingBlockPosOffsetZ", NbtElement.INT_TYPE)) {
			this.firstDataProvidingBlockPosOffset = new BlockPos(
					MathHelper.clamp(nbt.getInt("firstDataProvidingBlockPosOffsetX"), -48, 48),
					MathHelper.clamp(nbt.getInt("firstDataProvidingBlockPosOffsetY"), -48, 48),
					MathHelper.clamp(nbt.getInt("firstDataProvidingBlockPosOffsetZ"), -48, 48)
			);
		}

		if (nbt.contains("secondDataProvidingBlockPosOffsetX", NbtElement.INT_TYPE) && nbt.contains("secondDataProvidingBlockPosOffsetY", NbtElement.INT_TYPE) && nbt.contains("secondDataProvidingBlockPosOffsetZ", NbtElement.INT_TYPE)) {
			this.secondDataProvidingBlockPosOffset = new BlockPos(
					MathHelper.clamp(nbt.getInt("secondDataProvidingBlockPosOffsetX"), -48, 48),
					MathHelper.clamp(nbt.getInt("secondDataProvidingBlockPosOffsetY"), -48, 48),
					MathHelper.clamp(nbt.getInt("secondDataProvidingBlockPosOffsetZ"), -48, 48)
			);
		}

		if (nbt.contains(FIRST_CHECKED_DATA_ID_KEY)) {
			this.firstCheckedDataId = nbt.getString(FIRST_CHECKED_DATA_ID_KEY);
		} else {
			this.firstCheckedDataId = CHECKED_DATA_ID_DEFAULT;
		}

		if (nbt.contains(SECOND_CHECKED_DATA_ID_KEY)) {
			this.secondCheckedDataId = nbt.getString(SECOND_CHECKED_DATA_ID_KEY);
		} else {
			this.secondCheckedDataId = CHECKED_DATA_ID_DEFAULT;
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

	public String getFirstStructurePoolString() {
		return this.firstStructurePoolString;
	}

	public void setFirstStructurePoolString(String firstStructurePoolString) {
		this.firstStructurePoolString = firstStructurePoolString;
	}

	public String getSecondStructurePoolString() {
		return this.secondStructurePoolString;
	}

	public void setSecondStructurePoolString(String secondStructurePoolString) {
		this.secondStructurePoolString = secondStructurePoolString;
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

	public BlockPos getFirstDataProvidingBlockPosOffset() {
		return this.firstDataProvidingBlockPosOffset;
	}

	public void setFirstDataProvidingBlockPosOffset(BlockPos firstDataProvidingBlockPosOffset) {
		this.firstDataProvidingBlockPosOffset = firstDataProvidingBlockPosOffset;
	}

	public BlockPos getSecondDataProvidingBlockPosOffset() {
		return this.secondDataProvidingBlockPosOffset;
	}

	public void setSecondDataProvidingBlockPosOffset(BlockPos secondDataProvidingBlockPosOffset) {
		this.secondDataProvidingBlockPosOffset = secondDataProvidingBlockPosOffset;
	}

	public String getFirstCheckedDataId() {
		return this.firstCheckedDataId;
	}

	public void setFirstCheckedDataId(String firstCheckedDataId) {
		this.firstCheckedDataId = firstCheckedDataId;
	}

	public String getSecondCheckedDataId() {
		return this.secondCheckedDataId;
	}

	public void setSecondCheckedDataId(String secondCheckedDataId) {
		this.secondCheckedDataId = secondCheckedDataId;
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

				ScriptBlocks.info("facing: " + facing + ", rotation: " + rotation);

				if (registryEntry.hasKeyAndValue()) {
					FixedRotationStructurePoolBasedGenerator.generate(
							serverWorld,
							registryEntry,
							this.target,
							20,
							/*blockPos*/(facing == Direction.UP || facing == Direction.DOWN) ? new BlockPos(blockPos.getX(), blockPos.getY() + 1, blockPos.getZ()) : facing == Direction.SOUTH ? new BlockPos(blockPos.getX(), blockPos.getY() + 1, blockPos.getZ() + 2) : facing == Direction.WEST ? new BlockPos(blockPos.getX() - 2, blockPos.getY() + 1, blockPos.getZ()) : facing == Direction.EAST ? new BlockPos(blockPos.getX() + 2, blockPos.getY() + 1, blockPos.getZ()) : new BlockPos(blockPos.getX(), blockPos.getY() + 1, blockPos.getZ() - 2), // offsets to fix vanilla bug
							false,
							facing == Direction.EAST ? BlockRotation.CLOCKWISE_90 : facing == Direction.SOUTH ? BlockRotation.CLOCKWISE_180 : facing == Direction.WEST ? BlockRotation.COUNTERCLOCKWISE_90 : facing == Direction.NORTH ? BlockRotation.NONE : rotation == Direction.EAST ? BlockRotation.CLOCKWISE_90 : rotation == Direction.SOUTH ? BlockRotation.CLOCKWISE_180 : rotation == Direction.WEST ? BlockRotation.COUNTERCLOCKWISE_90 : BlockRotation.NONE
					);
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
			if (state.get(RotatedBlockWithEntity.ROTATED) != this.rotated) {
				BlockRotation blockRotation = BlockRotationUtils.calculateRotationFromDifferentRotatedStates(state.get(RotatedBlockWithEntity.ROTATED), this.rotated);
				this.triggeredBlock.setLeft(BlockRotationUtils.rotateOffsetBlockPos(this.triggeredBlock.getLeft(), blockRotation));
				this.firstDataProvidingBlockPosOffset = BlockRotationUtils.rotateOffsetBlockPos(this.firstDataProvidingBlockPosOffset, blockRotation);
				this.secondDataProvidingBlockPosOffset = BlockRotationUtils.rotateOffsetBlockPos(this.secondDataProvidingBlockPosOffset, blockRotation);
				this.rotated = state.get(RotatedBlockWithEntity.ROTATED);
			}
			if (state.get(RotatedBlockWithEntity.X_MIRRORED) != this.x_mirrored) {
				this.triggeredBlock.setLeft(BlockRotationUtils.mirrorOffsetBlockPos(this.triggeredBlock.getLeft(), BlockMirror.FRONT_BACK));
				this.firstDataProvidingBlockPosOffset = BlockRotationUtils.mirrorOffsetBlockPos(this.firstDataProvidingBlockPosOffset, BlockMirror.FRONT_BACK);
				this.secondDataProvidingBlockPosOffset = BlockRotationUtils.mirrorOffsetBlockPos(this.secondDataProvidingBlockPosOffset, BlockMirror.FRONT_BACK);
				this.x_mirrored = state.get(RotatedBlockWithEntity.X_MIRRORED);
			}
			if (state.get(RotatedBlockWithEntity.Z_MIRRORED) != this.z_mirrored) {
				this.triggeredBlock.setLeft(BlockRotationUtils.mirrorOffsetBlockPos(this.triggeredBlock.getLeft(), BlockMirror.LEFT_RIGHT));
				this.firstDataProvidingBlockPosOffset = BlockRotationUtils.mirrorOffsetBlockPos(this.firstDataProvidingBlockPosOffset, BlockMirror.LEFT_RIGHT);
				this.secondDataProvidingBlockPosOffset = BlockRotationUtils.mirrorOffsetBlockPos(this.secondDataProvidingBlockPosOffset, BlockMirror.LEFT_RIGHT);
				this.z_mirrored = state.get(RotatedBlockWithEntity.Z_MIRRORED);
			}
		}
	}

	private RegistryKey<StructurePool> getCurrentPool(ServerWorld serverWorld) {
		RegistryKey<StructurePool> currentPool = POOL_DEFAULT;

		BlockPos firstDataBlockPos = this.firstDataProvidingBlockPosOffset;
		BlockPos secondDataBlockPos = this.secondDataProvidingBlockPosOffset;
		String currentPoolIdentifierString = this.firstStructurePoolString;
		if (firstDataBlockPos != BlockPos.ORIGIN) {
			BlockEntity blockEntity1 = serverWorld.getBlockEntity(this.getPos().add(firstDataBlockPos.getX(), firstDataBlockPos.getY(), firstDataBlockPos.getZ()));
			if (blockEntity1 instanceof ProvidesData providesDataBlockEntity) {
				currentPoolIdentifierString = currentPoolIdentifierString + providesDataBlockEntity.getData(this.firstCheckedDataId);
			}
		}
		if (!this.secondStructurePoolString.isEmpty()) {
			currentPoolIdentifierString = currentPoolIdentifierString + this.secondStructurePoolString;
		}
		if (secondDataBlockPos != BlockPos.ORIGIN) {
			BlockEntity blockEntity1 = serverWorld.getBlockEntity(this.getPos().add(secondDataBlockPos.getX(), secondDataBlockPos.getY(), secondDataBlockPos.getZ()));
			if (blockEntity1 instanceof ProvidesData providesDataBlockEntity) {
				currentPoolIdentifierString = currentPoolIdentifierString + providesDataBlockEntity.getData(this.secondCheckedDataId);
			}
		}
		if (!currentPoolIdentifierString.isEmpty()) {
			currentPool = RegistryKey.of(RegistryKeys.TEMPLATE_POOL, Identifier.tryParse(currentPoolIdentifierString));
		}
		return currentPool;
	}
}
