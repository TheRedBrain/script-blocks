package com.github.theredbrain.scriptblocks.block.entity;

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
	public static final String TARGET_KEY = "target";
	public static final String POOL_KEY = "pool";
	public static final String JOINT_KEY = "joint";
	private Identifier target = Identifier.of("empty");
	private RegistryKey<StructurePool> pool = RegistryKey.of(RegistryKeys.TEMPLATE_POOL, Identifier.of("empty"));
	private JigsawBlockEntity.Joint joint = JigsawBlockEntity.Joint.ROLLABLE;
	private MutablePair<BlockPos, Boolean> triggeredBlock = new MutablePair<>(new BlockPos(0, 0, 0), false);

	public JigsawPlacerBlockEntity(BlockPos pos, BlockState state) {
		super(EntityRegistry.STRUCTURE_PLACER_BLOCK_ENTITY, pos, state);
	}

	@Override
	protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
		nbt.putString(TARGET_KEY, this.target.toString());
		nbt.putString(POOL_KEY, this.pool.getValue().toString());
		nbt.putString(JOINT_KEY, this.joint.asString());

		nbt.putInt("triggeredBlockPositionOffsetX", this.triggeredBlock.getLeft().getX());
		nbt.putInt("triggeredBlockPositionOffsetY", this.triggeredBlock.getLeft().getY());
		nbt.putInt("triggeredBlockPositionOffsetZ", this.triggeredBlock.getLeft().getZ());
		nbt.putBoolean("triggeredBlockResets", this.triggeredBlock.getRight());

		super.writeNbt(nbt, registryLookup);
	}

	@Override
	protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
		this.target = Identifier.of(nbt.getString(TARGET_KEY));
		this.pool = RegistryKey.of(RegistryKeys.TEMPLATE_POOL, Identifier.of(nbt.getString(POOL_KEY)));
		this.joint = JigsawBlockEntity.Joint.byName(nbt.getString(JOINT_KEY)).orElseGet(() -> JigsawBlock.getFacing(this.getCachedState()).getAxis().isHorizontal() ? JigsawBlockEntity.Joint.ALIGNED : JigsawBlockEntity.Joint.ROLLABLE);

		int x = MathHelper.clamp(nbt.getInt("triggeredBlockPositionOffsetX"), -48, 48);
		int y = MathHelper.clamp(nbt.getInt("triggeredBlockPositionOffsetY"), -48, 48);
		int z = MathHelper.clamp(nbt.getInt("triggeredBlockPositionOffsetZ"), -48, 48);
		this.triggeredBlock = new MutablePair<>(new BlockPos(x, y, z), nbt.getBoolean("triggeredBlockResets"));

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

	public RegistryKey<StructurePool> getPool() {
		return this.pool;
	}

	public boolean setPool(String pool) {
		Identifier identifier = Identifier.tryParse(pool);
		if (identifier != null) {
			this.pool = RegistryKey.of(RegistryKeys.TEMPLATE_POOL, identifier);
			return true;
		}
		return false;
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

	@Override
	public void trigger() {
		if (this.world != null) {
			if (this.world instanceof ServerWorld serverWorld) {
				BlockPos blockPos = this.getPos().offset(this.getCachedState().get(JigsawBlock.ORIENTATION).getFacing());
				Registry<StructurePool> registry = world.getRegistryManager().get(RegistryKeys.TEMPLATE_POOL);
				RegistryEntry.Reference<StructurePool> registryEntry = registry.entryOf(this.pool);

				Direction rotation = this.getCachedState().get(JigsawBlock.ORIENTATION).getRotation();
				Direction facing = this.getCachedState().get(JigsawBlock.ORIENTATION).getFacing();

				FixedRotationStructurePoolBasedGenerator.generate(serverWorld, registryEntry, this.target, 7, /*blockPos*/new BlockPos(blockPos.getX(), blockPos.getY() + 1, blockPos.getZ()), false, facing == Direction.EAST ? BlockRotation.CLOCKWISE_90 : facing == Direction.SOUTH ? BlockRotation.CLOCKWISE_180 : facing == Direction.WEST ? BlockRotation.COUNTERCLOCKWISE_90 : facing == Direction.NORTH ? BlockRotation.NONE : rotation == Direction.EAST ? BlockRotation.CLOCKWISE_90 : rotation == Direction.SOUTH ? BlockRotation.CLOCKWISE_180 : rotation == Direction.WEST ? BlockRotation.COUNTERCLOCKWISE_90 : BlockRotation.NONE);
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
}
