package com.github.theredbrain.scriptblocks.block.entity;

import com.github.theredbrain.scriptblocks.registry.ScriptBlocksBlocks;
import com.github.theredbrain.scriptblocks.registry.ScriptBlocksEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.DecoratedPotBlockEntity;
import net.minecraft.world.level.block.entity.PotDecorations;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;

public class AestheticDecoratedPotBlockEntity extends BlockEntity {
	public long lastWobbleTime;
	@Nullable
	public DecoratedPotBlockEntity.WobbleStyle lastWobbleType;
	private PotDecorations sherds;

	public AestheticDecoratedPotBlockEntity(BlockPos pos, BlockState state) {
		super(ScriptBlocksEntities.AESTHETIC_DECORATED_POT_BLOCK_ENTITY, pos, state);
		this.sherds = PotDecorations.EMPTY;
	}

	@Override
	protected void saveAdditional(ValueOutput view) {
		super.saveAdditional(view);
		view.store("sherds", PotDecorations.CODEC, this.sherds);
	}

	@Override
	protected void loadAdditional(ValueInput view) {
		super.loadAdditional(view);
		this.sherds = view.read("sherds", PotDecorations.CODEC).orElse(PotDecorations.EMPTY);
	}

	public ClientboundBlockEntityDataPacket getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}

	@Override
	public CompoundTag getUpdateTag(HolderLookup.Provider registryLookup) {
		return this.saveCustomOnly(registryLookup);
	}

	public Direction getHorizontalFacing() {
		return this.getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);
	}

	public PotDecorations getSherds() {
		return this.sherds;
	}

	public void readFrom(ItemStack stack) {
		this.applyComponentsFromItemStack(stack);
	}

	public ItemStack asStack() {
		ItemStack itemStack = ScriptBlocksBlocks.AESTHETIC_DECORATED_POT.asItem().getDefaultInstance();
		itemStack.applyComponents(this.collectComponents());
		return itemStack;
	}

	public static ItemStack getStackWith(PotDecorations sherds) {
		ItemStack itemStack = ScriptBlocksBlocks.AESTHETIC_DECORATED_POT.asItem().getDefaultInstance();
		itemStack.set(DataComponents.POT_DECORATIONS, sherds);
		return itemStack;
	}

	@Override
	protected void collectImplicitComponents(DataComponentMap.Builder componentMapBuilder) {
		super.collectImplicitComponents(componentMapBuilder);
		componentMapBuilder.set(DataComponents.POT_DECORATIONS, this.sherds);
	}

	@Override
	protected void applyImplicitComponents(DataComponentGetter components) {
		super.applyImplicitComponents(components);
		this.sherds = components.getOrDefault(DataComponents.POT_DECORATIONS, PotDecorations.EMPTY);
	}

	@Override
	public void removeComponentsFromTag(ValueOutput view) {
		super.removeComponentsFromTag(view);
		view.discard("sherds");
	}

	public void wobble(DecoratedPotBlockEntity.WobbleStyle wobbleType) {
		if (this.level != null && !this.level.isClientSide()) {
			this.level.blockEvent(this.getBlockPos(), this.getBlockState().getBlock(), 1, wobbleType.ordinal());
		}
	}

	@Override
	public boolean triggerEvent(int type, int data) {
		if (this.level != null && type == 1 && data >= 0 && data < DecoratedPotBlockEntity.WobbleStyle.values().length) {
			this.lastWobbleTime = this.level.getGameTime();
			this.lastWobbleType = DecoratedPotBlockEntity.WobbleStyle.values()[data];
			return true;
		} else {
			return super.triggerEvent(type, data);
		}
	}
}
