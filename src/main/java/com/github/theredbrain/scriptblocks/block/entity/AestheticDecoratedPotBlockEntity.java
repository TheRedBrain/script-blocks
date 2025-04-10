package com.github.theredbrain.scriptblocks.block.entity;

import com.github.theredbrain.scriptblocks.registry.EntityRegistry;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.DecoratedPotBlockEntity;
import net.minecraft.block.entity.Sherds;
import net.minecraft.component.ComponentMap;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.Nullable;

public class AestheticDecoratedPotBlockEntity extends BlockEntity {
	public static final String SHERDS_NBT_KEY = "sherds";
	public static final String ITEM_NBT_KEY = "item";
	public static final int field_46660 = 1;
	public long lastWobbleTime;
	@Nullable
	public DecoratedPotBlockEntity.WobbleType lastWobbleType;
	private Sherds sherds;

	public AestheticDecoratedPotBlockEntity(BlockPos pos, BlockState state) {
		super(EntityRegistry.AESTHETIC_DECORATED_POT_BLOCK_ENTITY, pos, state);
		this.sherds = Sherds.DEFAULT;
	}

	@Override
	protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
		super.writeNbt(nbt, registryLookup);
		this.sherds.toNbt(nbt);
	}

	@Override
	protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
		super.readNbt(nbt, registryLookup);
		this.sherds = Sherds.fromNbt(nbt);
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
		ItemStack itemStack = Items.DECORATED_POT.getDefaultStack();
		itemStack.applyComponentsFrom(this.createComponentMap());
		return itemStack;
	}

	public static ItemStack getStackWith(Sherds sherds) {
		ItemStack itemStack = Items.DECORATED_POT.getDefaultStack();
		itemStack.set(DataComponentTypes.POT_DECORATIONS, sherds);
		return itemStack;
	}

	@Override
	protected void addComponents(ComponentMap.Builder componentMapBuilder) {
		super.addComponents(componentMapBuilder);
		componentMapBuilder.add(DataComponentTypes.POT_DECORATIONS, this.sherds);
	}

	@Override
	protected void readComponents(BlockEntity.ComponentsAccess components) {
		super.readComponents(components);
		this.sherds = components.getOrDefault(DataComponentTypes.POT_DECORATIONS, Sherds.DEFAULT);
	}

	@Override
	public void removeFromCopiedStackNbt(NbtCompound nbt) {
		super.removeFromCopiedStackNbt(nbt);
		nbt.remove("sherds");
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
