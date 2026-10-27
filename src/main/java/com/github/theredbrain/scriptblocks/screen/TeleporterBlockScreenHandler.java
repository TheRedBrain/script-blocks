package com.github.theredbrain.scriptblocks.screen;

import com.github.theredbrain.scriptblocks.ScriptBlocks;
import com.github.theredbrain.scriptblocks.data.VisibleLocation;
import com.github.theredbrain.scriptblocks.registry.ScriptBlocksScreenHandlerTypes;
import com.github.theredbrain.scriptblocks.util.LocationUtils;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class TeleporterBlockScreenHandler extends AbstractContainerMenu {

	private final ContainerLevelAccess context;
	private Inventory playerInventory;
	private Container inventory = new SimpleContainer(9) {
		@Override
		public int getMaxStackSize() {
			return 999;
		}

		@Override
		public void setChanged() {
			super.setChanged();
//            ShopBlockScreenHandler.this.onContentChanged(this);
//            ShopBlockScreenHandler.this.contentsChangedListener.run();
		}
	};
//    private TeleporterBlockBlockEntity teleporterBlockBlockEntity;

	private final BlockPos blockPos;

	private final List<VisibleLocation> visibleLocationsList;

	private final int requiredKeyItemStackSlotX = 66;
	private final int requiredKeyItemStackSlotY = 148;
	private final DataSlot currentLocationListIndex = DataSlot.standalone();
	private final DataSlot currentLocationCooldown = DataSlot.standalone();

	public TeleporterBlockScreenHandler(int syncId, Inventory playerInventory, TeleporterBlockData data) {
		this(syncId, playerInventory, ContainerLevelAccess.NULL, data.blockPos(), data.visibleLocationsList);
	}

	public TeleporterBlockScreenHandler(int syncId, Inventory playerInventory, ContainerLevelAccess context, BlockPos blockPos, List<VisibleLocation> visibleLocationsList) {
		super(ScriptBlocksScreenHandlerTypes.TELEPORTER_BLOCK_SCREEN_HANDLER, syncId);
		this.playerInventory = playerInventory;
		this.blockPos = blockPos;
		this.visibleLocationsList = new ArrayList<>(visibleLocationsList);
		this.context = context;

		int i;
		// hotbar
		for (i = 0; i < 9; ++i) {
			this.addSlot(new Slot(playerInventory, i, 8 + i * 18, /*showCreativeTab ? 273 : */273/*TODO*/)/* {

                @Override
                public boolean canInsert(ItemStack stack) {
                    return stack.isIn(Tags.ADVENTURE_HOTBAR_ITEMS);
                }
            }*/);
		}
		this.addDataSlot(this.currentLocationListIndex).set(0);
		this.addDataSlot(this.currentLocationCooldown).set(0);
//		this.addProperty(this.currentCooldownMax).set(0);
		// main inventory
		for (i = 0; i < 3; ++i) {
			for (int j = 0; j < 9; ++j) {
				this.addSlot(new Slot(playerInventory, j + (i + 1) * 9, 8 + j * 18, (/*showCreativeTab ? 215 : */215)/*TODO*/ + i * 18));
			}
		}
		// keyItemStackSlot
		this.addSlot(new Slot(new SimpleContainer(1), 0, 77, 80) {
//            @Override
//            public boolean isEnabled() {
//                return !showCreativeTab && !(inventory.getStack(0).isEmpty());
//            }
		});

//        // requiredKeyItemStackSlot
//        this.addSlot(new Slot(inventory, 0, this.requiredKeyItemStackSlotX, this.requiredKeyItemStackSlotY) {
////            @Override
////            public boolean isEnabled() {
////                return showCreativeTab;
////            }
////
////            @Override
////            public void setStack(ItemStack stack) {
////                super.setStack(stack);
////                this.inventory.setStack(0, stack);
////            }
//        });

		// requiredKeyItemStackSlot in AdventureScreen
//        this.addSlot(new Slot(inventory, 0, 55, 80) {
////            @Override
////            public boolean isEnabled() {
////                return !showCreativeTab && !(inventory.getStack(0).isEmpty());
////            }
//            @Override
//            public boolean canTakeItems(PlayerEntity playerEntity) {
//                return false;
//            }
//            @Override
//            public boolean canInsert(ItemStack stack) {
//                return false;
//            }
//        });
	}

	public BlockPos getBlockPos() {
		return blockPos;
	}

	public int getRequiredKeyItemStackSlotX() {
		return requiredKeyItemStackSlotX;
	}

	public int getRequiredKeyItemStackSlotY() {
		return requiredKeyItemStackSlotY;
	}

	public Inventory getPlayerInventory() {
		return playerInventory;
	}

	public int getCurrentLocationListIndex() {
		return this.currentLocationListIndex.get();
	}

	public int getCurrentLocationCooldown() {
		return this.currentLocationCooldown.get();
	}


	public List<VisibleLocation> getVisibleLocationsList() {
		return this.visibleLocationsList;
	}

	@Override
	public boolean clickMenuButton(Player player, int id) {
		if (id == -1) {
			this.updateRemainingCooldown(player);
			return true;
		} else if (id >= 0 && id < this.visibleLocationsList.size()) {
			this.context.execute((world, pos) -> {
			this.setData(0, id);
			this.broadcastChanges();
			});
			return true;
		}
		return false;
	}

	private void updateRemainingCooldown(Player player) {
		this.context.execute((world, pos) -> {
			if (this.currentLocationListIndex.get() < this.visibleLocationsList.size() && player instanceof ServerPlayer serverPlayerEntity) {
				VisibleLocation currentLocation = this.visibleLocationsList.get(this.currentLocationListIndex.get());
				int remainingCooldown = (int) LocationUtils.getRemainingCooldownForEntrance(serverPlayerEntity, currentLocation.identifier(), currentLocation.entrance());
				this.setData(1, remainingCooldown);
				this.broadcastChanges();
			}
		});
	}

	@Override
	public ItemStack quickMoveStack(Player player, int slot) {
		return ItemStack.EMPTY;
	}

	@Override
	public boolean stillValid(Player player) {
		return this.inventory.stillValid(player);
	}

	@Override
	public void removed(Player player) {
		super.removed(player);
		ItemStack itemStack;
		if (player instanceof ServerPlayer && !(itemStack = this.getSlot(36).getItem()).isEmpty()) {
			if (!player.isAlive() || ((ServerPlayer) player).hasDisconnected()) {
				player.drop(itemStack, false);
			} else {
				player.getInventory().placeItemBackInInventory(itemStack);
			}
		}
	}

	public record TeleporterBlockData(
			BlockPos blockPos,
			List<VisibleLocation> visibleLocationsList
	) {

		public static final StreamCodec<RegistryFriendlyByteBuf, TeleporterBlockData> PACKET_CODEC = StreamCodec.ofMember(TeleporterBlockData::write, TeleporterBlockData::new);

		public TeleporterBlockData(RegistryFriendlyByteBuf registryByteBuf) {
			this(
					registryByteBuf.readBlockPos(),
					registryByteBuf.readList(VisibleLocation.PACKET_CODEC)
			);
		}

		private void write(RegistryFriendlyByteBuf registryByteBuf) {
			registryByteBuf.writeBlockPos(blockPos);
			registryByteBuf.writeCollection(this.visibleLocationsList, VisibleLocation.PACKET_CODEC);
		}
	}

}
