package com.github.theredbrain.scriptblocks.block.entity;

import com.github.theredbrain.scriptblocks.block.RotatedBlockWithEntity;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

public abstract class RotatedBlockEntity extends BlockEntity {
	protected int rotated = 0;
	protected boolean x_mirrored = false;
	protected boolean z_mirrored = false;

	public RotatedBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	@Override
	protected void writeData(WriteView view) {
		super.writeData(view);

		view.putInt("rotated", this.rotated);
		view.putBoolean("x_mirrored", this.x_mirrored);
		view.putBoolean("z_mirrored", this.z_mirrored);
	}

	/**
	 * Has to be called last when overridden to avoid rotated data being overridden by saved data
	 */
	@Override
	protected void readData(ReadView view) {
		super.readData(view);

		this.rotated = MathHelper.clamp(view.getInt("rotated", 0), 0, 3);
		this.x_mirrored = view.getBoolean("x_mirrored", false);
		this.z_mirrored = view.getBoolean("z_mirrored", false);
		if (this.getCachedState().getBlock() instanceof RotatedBlockWithEntity) {
			this.onRotate(this.getCachedState());
		}
	}

	protected abstract void onRotate(BlockState state);
}
