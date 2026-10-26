package com.github.theredbrain.scriptblocks.block.entity;

import com.github.theredbrain.scriptblocks.registry.ScriptBlocksBlocks;
import com.github.theredbrain.scriptblocks.registry.ScriptBlocksEntities;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.DecoratedPotBlockEntity;
import net.minecraft.block.entity.Sherds;
import net.minecraft.component.ComponentMap;
import net.minecraft.component.ComponentsAccess;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.state.property.Properties;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.Nullable;

public class AestheticDecoratedPotBlockEntity extends BlockEntity {
	public long lastWobbleTime;
	@Nullable
	public DecoratedPotBlockEntity.WobbleType lastWobbleType;
	private Sherds sherds;

	public AestheticDecoratedPotBlockEntity(BlockPos pos, BlockState state) {
		super(ScriptBlocksEntities.AESTHETIC_DECORATED_POT_BLOCK_ENTITY, pos, state);
		this.sherds = Sherds.DEFAULT;
	}

	@Override
	protected void writeData(WriteView view) {
		super.writeData(view);
		view.put("sherds", Sherds.CODEC, this.sherds);
	}

	@Override
	protected void readData(ReadView view) {
		super.readData(view);
		this.sherds = view.read("sherds", Sherds.CODEC).orElse(Sherds.DEFAULT);
	}

	public BlockEntityUpdateS2CPacket toUpdatePacket() {
		return BlockEntityUpdateS2CPacket.create(this);
	}

	@Override
	public NbtCompound toInitialChunkDataNbt(RegistryWrapper.WrapperLookup registryLookup) {
		return this.createComponentlessNbt(registryLookup);
	}

	public Direction getHorizontalFacing() {
		return this.getCachedState().get(Properties.HORIZONTAL_FACING);
	}

	public Sherds getSherds() {
		return this.sherds;
	}

	public void readFrom(ItemStack stack) {
		this.readComponents(stack);
	}

	public ItemStack asStack() {
		ItemStack itemStack = ScriptBlocksBlocks.AESTHETIC_DECORATED_POT.asItem().getDefaultStack();
		itemStack.applyComponentsFrom(this.createComponentMap());
		return itemStack;
	}

	public static ItemStack getStackWith(Sherds sherds) {
		ItemStack itemStack = ScriptBlocksBlocks.AESTHETIC_DECORATED_POT.asItem().getDefaultStack();
		itemStack.set(DataComponentTypes.POT_DECORATIONS, sherds);
		return itemStack;
	}

	@Override
	protected void addComponents(ComponentMap.Builder componentMapBuilder) {
		super.addComponents(componentMapBuilder);
		componentMapBuilder.add(DataComponentTypes.POT_DECORATIONS, this.sherds);
	}

	@Override
	protected void readComponents(ComponentsAccess components) {
		super.readComponents(components);
		this.sherds = components.getOrDefault(DataComponentTypes.POT_DECORATIONS, Sherds.DEFAULT);
	}

	@Override
	public void removeFromCopiedStackData(WriteView view) {
		super.removeFromCopiedStackData(view);
		view.remove("sherds");
	}

	public void wobble(DecoratedPotBlockEntity.WobbleType wobbleType) {
		if (this.world != null && !this.world.isClient()) {
			this.world.addSyncedBlockEvent(this.getPos(), this.getCachedState().getBlock(), 1, wobbleType.ordinal());
		}
	}

	@Override
	public boolean onSyncedBlockEvent(int type, int data) {
		if (this.world != null && type == 1 && data >= 0 && data < DecoratedPotBlockEntity.WobbleType.values().length) {
			this.lastWobbleTime = this.world.getTime();
			this.lastWobbleType = DecoratedPotBlockEntity.WobbleType.values()[data];
			return true;
		} else {
			return super.onSyncedBlockEvent(type, data);
		}
	}
}
