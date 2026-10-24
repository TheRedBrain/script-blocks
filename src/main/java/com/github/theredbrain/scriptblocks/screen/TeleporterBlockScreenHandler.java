package com.github.theredbrain.scriptblocks.screen;

import com.github.theredbrain.scriptblocks.ScriptBlocks;
import com.github.theredbrain.scriptblocks.data.VisibleLocation;
import com.github.theredbrain.scriptblocks.registry.ScriptBlocksScreenHandlerTypes;
import com.github.theredbrain.scriptblocks.util.LocationUtils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.screen.Property;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;

public class TeleporterBlockScreenHandler extends ScreenHandler {

	private final ScreenHandlerContext context;
	private PlayerInventory playerInventory;
	private Inventory inventory = new SimpleInventory(9) {
		@Override
		public int getMaxCountPerStack() {
			return 999;
		}

		@Override
		public void markDirty() {
			super.markDirty();
//            ShopBlockScreenHandler.this.onContentChanged(this);
//            ShopBlockScreenHandler.this.contentsChangedListener.run();
		}
	};
//    private TeleporterBlockBlockEntity teleporterBlockBlockEntity;

	private final BlockPos blockPos;

	private final List<VisibleLocation> visibleLocationsList;

	private final int requiredKeyItemStackSlotX = 66;
	private final int requiredKeyItemStackSlotY = 148;
	private final Property currentLocationListIndex = Property.create();
	private final Property currentLocationCooldown = Property.create();

	public TeleporterBlockScreenHandler(int syncId, PlayerInventory playerInventory, TeleporterBlockData data) {
		this(syncId, playerInventory, ScreenHandlerContext.EMPTY, data.blockPos(), data.visibleLocationsList);
	}

	public TeleporterBlockScreenHandler(int syncId, PlayerInventory playerInventory, ScreenHandlerContext context, BlockPos blockPos, List<VisibleLocation> visibleLocationsList) {
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
		this.addProperty(this.currentLocationListIndex).set(0);
		this.addProperty(this.currentLocationCooldown).set(0);
//		this.addProperty(this.currentCooldownMax).set(0);
		// main inventory
		for (i = 0; i < 3; ++i) {
			for (int j = 0; j < 9; ++j) {
				this.addSlot(new Slot(playerInventory, j + (i + 1) * 9, 8 + j * 18, (/*showCreativeTab ? 215 : */215)/*TODO*/ + i * 18));
			}
		}
		// keyItemStackSlot
		this.addSlot(new Slot(new SimpleInventory(1), 0, 77, 80) {
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

	public PlayerInventory getPlayerInventory() {
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
	public boolean onButtonClick(PlayerEntity player, int id) {
		if (id == -1) {
			this.updateRemainingCooldown(player);
			return true;
		} else if (id >= 0 && id < this.visibleLocationsList.size()) {
			this.context.run((world, pos) -> {
			this.setProperty(0, id);
			this.sendContentUpdates();
			});
			return true;
		}
		return false;
	}

	private void updateRemainingCooldown(PlayerEntity player) {
		this.context.run((world, pos) -> {
			if (this.currentLocationListIndex.get() < this.visibleLocationsList.size() && player instanceof ServerPlayerEntity serverPlayerEntity) {
				VisibleLocation currentLocation = this.visibleLocationsList.get(this.currentLocationListIndex.get());
				int remainingCooldown = (int) LocationUtils.getRemainingCooldownForEntrance(serverPlayerEntity, currentLocation.identifier(), currentLocation.entrance());
				this.setProperty(1, remainingCooldown);
				this.sendContentUpdates();
			}
		});
	}

	@Override
	public ItemStack quickMove(PlayerEntity player, int slot) {
		return ItemStack.EMPTY;
	}

	@Override
	public boolean canUse(PlayerEntity player) {
		return this.inventory.canPlayerUse(player);
	}

	@Override
	public void onClosed(PlayerEntity player) {
		super.onClosed(player);
		ItemStack itemStack;
		if (player instanceof ServerPlayerEntity && !(itemStack = this.getSlot(36).getStack()).isEmpty()) {
			if (!player.isAlive() || ((ServerPlayerEntity) player).isDisconnected()) {
				player.dropItem(itemStack, false);
			} else {
				player.getInventory().offerOrDrop(itemStack);
			}
		}
	}

	public record TeleporterBlockData(
			BlockPos blockPos,
			List<VisibleLocation> visibleLocationsList
	) {

		public static final PacketCodec<RegistryByteBuf, TeleporterBlockData> PACKET_CODEC = PacketCodec.of(TeleporterBlockData::write, TeleporterBlockData::new);

		public TeleporterBlockData(RegistryByteBuf registryByteBuf) {
			this(
					registryByteBuf.readBlockPos(),
					registryByteBuf.readList(VisibleLocation.PACKET_CODEC)
			);
		}

		private void write(RegistryByteBuf registryByteBuf) {
			registryByteBuf.writeBlockPos(blockPos);
			registryByteBuf.writeCollection(this.visibleLocationsList, VisibleLocation.PACKET_CODEC);
		}
	}

}
