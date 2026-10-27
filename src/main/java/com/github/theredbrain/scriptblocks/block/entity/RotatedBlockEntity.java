package com.github.theredbrain.scriptblocks.block.entity;

import com.github.theredbrain.scriptblocks.block.RotatedBlockWithEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public abstract class RotatedBlockEntity extends BlockEntity {
	protected int rotated = 0;
	protected boolean x_mirrored = false;
	protected boolean z_mirrored = false;

	public RotatedBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	@Override
	protected void saveAdditional(ValueOutput view) {
		super.saveAdditional(view);

		view.putInt("rotated", this.rotated);
		view.putBoolean("x_mirrored", this.x_mirrored);
		view.putBoolean("z_mirrored", this.z_mirrored);
	}

	/**
	 * Has to be called last when overridden to avoid rotated data being overridden by saved data
	 */
	@Override
	protected void loadAdditional(ValueInput view) {
		super.loadAdditional(view);

		this.rotated = Mth.clamp(view.getIntOr("rotated", 0), 0, 3);
		this.x_mirrored = view.getBooleanOr("x_mirrored", false);
		this.z_mirrored = view.getBooleanOr("z_mirrored", false);
		if (this.getBlockState().getBlock() instanceof RotatedBlockWithEntity) {
			this.onRotate(this.getBlockState());
		}
	}

	protected abstract void onRotate(BlockState state);
}
