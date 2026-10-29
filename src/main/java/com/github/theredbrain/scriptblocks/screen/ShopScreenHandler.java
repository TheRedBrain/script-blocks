package com.github.theredbrain.scriptblocks.screen;

import com.github.theredbrain.scriptblocks.ScriptBlocks;
import com.github.theredbrain.scriptblocks.block.entity.ShopBlockEntity;
import com.github.theredbrain.scriptblocks.data.Shop;
import com.github.theredbrain.scriptblocks.network.packet.TradeWithShopPacket;
import com.github.theredbrain.scriptblocks.registry.ScriptBlocksDynamicRegistries;
import com.github.theredbrain.scriptblocks.registry.ScriptBlocksScreenHandlerTypes;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.ServerAdvancementManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ShopScreenHandler extends AbstractContainerMenu {

	private Inventory playerInventory;
	private final Level world;
	private ShopBlockEntity shopBlockEntity;
	private @Nullable Shop shop;
	private List<Shop.Deal> dealsList = new ArrayList<>(List.of());
	private List<Shop.Deal> unlockedDealsList = new ArrayList<>(List.of());
	private List<Shop.Deal> stockedDealsList = new ArrayList<>(List.of());
	private int unlockedDealsCounter = 0;
	private int stockedDealsCounter = 0;
	Runnable contentsChangedListener = () -> {
	};
	public final Container inventory = new SimpleContainer(9) {
		@Override
		public int getMaxStackSize() {
			return 999;
		}

		@Override
		public void setChanged() {
			super.setChanged();
			ShopScreenHandler.this.slotsChanged(this);
			ShopScreenHandler.this.contentsChangedListener.run();
		}
	};

	private BlockPos blockPos;

	public ShopScreenHandler(int syncId, Inventory playerInventory, ShopBlockData data) {
		this(syncId, playerInventory, data.blockPos());
	}

	public ShopScreenHandler(int syncId, Inventory playerInventory, BlockPos blockPos) {
		super(ScriptBlocksScreenHandlerTypes.SHOP_BLOCK_SCREEN_HANDLER, syncId);
		this.playerInventory = playerInventory;
		this.world = playerInventory.player.level();
		this.blockPos = blockPos;
		BlockEntity blockEntity = this.world.getBlockEntity(this.blockPos);
		if (blockEntity instanceof ShopBlockEntity) {
			this.shopBlockEntity = (ShopBlockEntity) blockEntity;
			Shop shop = null;
			String shopIdentifier = this.shopBlockEntity.getShopIdentifier();
			if (!shopIdentifier.isEmpty()) {
				Optional<Holder.Reference<Shop>> optionalShopReference = this.world.registryAccess().get(ScriptBlocksDynamicRegistries.SHOP_REGISTRY_KEY).get().value().get(Identifier.parse(shopIdentifier));
				if (optionalShopReference.isPresent()) {
					shop = optionalShopReference.get().value();
				}
			}
			this.shop = shop;
			if (shop != null) {
				this.dealsList = shop.dealList();
			}
		}
		int i;
		// hotbar
		for (i = 0; i < 9; ++i) {
			this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 188));
		}
		// main inventory
		for (i = 0; i < 3; ++i) {
			for (int j = 0; j < 9; ++j) {
				this.addSlot(new Slot(playerInventory, j + (i + 1) * 9, 8 + j * 18, 130 + i * 18));
			}
		}
		// player offers
		for (i = 0; i < 9; ++i) {
			this.addSlot(new Slot(inventory, i, 8 + i * 18, 98));
		}
	}

	public BlockPos getBlockPos() {
		return this.blockPos;
	}

	public ShopBlockEntity getShopBlockEntity() {
		return this.shopBlockEntity;
	}

	@Nullable
	public Shop getShop() {
		return this.shop;
	}

	public List<Shop.Deal> getDealsList() {
		return this.dealsList;
	}

	public List<Shop.Deal> getUnlockedDealsList() {
		return this.unlockedDealsList;
	}

	public List<Shop.Deal> getStockedDealsList() {
		return this.stockedDealsList;
	}

	public int getUnlockedDealsCounter() {
		return this.unlockedDealsCounter;
	}

	public int getStockedDealsCounter() {
		return this.stockedDealsCounter;
	}

	public Container getInventory() {
		return this.inventory;
	}

	public void setContentsChangedListener(Runnable contentsChangedListener) {
		this.contentsChangedListener = contentsChangedListener;
	}

	@Override
	public ItemStack quickMoveStack(Player player, int slot) {
		ItemStack itemStack = ItemStack.EMPTY;
		// TODO
//        Slot slot2 = this.slots.get(slot);
//        if (slot2 != null && slot2.hasStack()) {
//            ItemStack itemStack2 = slot2.getStack();
//            itemStack = itemStack2.copy();
//            if (slot == 0) {
//                this.context.run((world, pos) -> itemStack2.getItem().onCraftByPlayer(itemStack2, world, player));
//                if (!this.insertItem(itemStack2, 10, 46, true)) {
//                    return ItemStack.EMPTY;
//                }
//
//                slot2.onQuickTransfer(itemStack2, itemStack);
//            } else if (slot >= 10 && slot < 46) {
//                if (!this.insertItem(itemStack2, 1, 10, false)) {
//                    if (slot < 37) {
//                        if (!this.insertItem(itemStack2, 37, 46, false)) {
//                            return ItemStack.EMPTY;
//                        }
//                    } else if (!this.insertItem(itemStack2, 10, 37, false)) {
//                        return ItemStack.EMPTY;
//                    }
//                }
//            } else if (!this.insertItem(itemStack2, 10, 46, false)) {
//                return ItemStack.EMPTY;
//            }
//
//            if (itemStack2.isEmpty()) {
//                slot2.setStack(ItemStack.EMPTY);
//            } else {
//                slot2.markDirty();
//            }
//
//            if (itemStack2.getCount() == itemStack.getCount()) {
//                return ItemStack.EMPTY;
//            }
//
//            slot2.onTakeItem(player, itemStack2);
//            if (slot == 0) {
//                player.dropItem(itemStack2, false);
//            }
//        }
//
		return itemStack;
	}

	@Override
	public boolean stillValid(Player player) {
		return true;
	}

	@Override
	public boolean clickMenuButton(Player player, int id) {
		if (id < this.stockedDealsList.size()) {
			Shop.Deal currentDeal = this.stockedDealsList.get(id);
			if (currentDeal != null && player instanceof ServerPlayer serverPlayerEntity) {
				ServerPlayNetworking.send(serverPlayerEntity,
						new TradeWithShopPacket(
								this.shopBlockEntity.getShopIdentifier(),
								id
						)
				);
			}
		}
		return true;
	}

	@Override
	public void removed(Player player) {
		super.removed(player);
		if (player instanceof ServerPlayer) {
			this.clearContainer(player, this.inventory);
		}
	}

	public void calculateUnlockedAndStockedDeals() {

		ScriptBlocks.info("calculateUnlockedAndStockedDeals");

//        ClientAdvancementManager advancementHandler = null;
		PlayerAdvancements playerAdvancementTracker = null;
		ServerAdvancementManager serverAdvancementLoader = null;

		if (this.playerInventory.player instanceof ServerPlayer serverPlayerEntity) {
			playerAdvancementTracker = serverPlayerEntity.getAdvancements();
			MinecraftServer minecraftServer = serverPlayerEntity.level().getServer();
			if (minecraftServer != null) {
				serverAdvancementLoader = minecraftServer.getAdvancements();
			}
		}
		String lockAdvancement;
		String unlockAdvancement;

//        if (this.world.isClient && this.playerInventory.player instanceof ClientPlayerEntity clientPlayerEntity) {
//
//            ScriptBlocksMod.info("this.world.isClient && this.playerInventory.player instanceof ClientPlayerEntity clientPlayerEntity");
//
//            advancementHandler = clientPlayerEntity.networkHandler.getAdvancementHandler();
//        }

		if (playerAdvancementTracker != null && serverAdvancementLoader != null) {
			this.unlockedDealsList.clear();
			this.unlockedDealsCounter = 0;

			ScriptBlocks.info("advancementHandler != null");

			ScriptBlocks.info("this.dealsList.size(): " + this.dealsList.size());

			for (int i = 0; i < this.dealsList.size(); i++) {

				Shop.Deal deal = this.dealsList.get(i);
				lockAdvancement = deal.lockAdvancement();
				unlockAdvancement = deal.unlockAdvancement();

				AdvancementHolder lockAdvancementEntry = null;
				lockAdvancementEntry = serverAdvancementLoader.get(Identifier.parse(lockAdvancement));

				AdvancementHolder unlockAdvancementEntry = null;
				unlockAdvancementEntry = serverAdvancementLoader.get(Identifier.parse(unlockAdvancement));

				if ((lockAdvancementEntry != null && !playerAdvancementTracker.getOrStartProgress(lockAdvancementEntry).isDone()) &&
						(unlockAdvancementEntry != null && playerAdvancementTracker.getOrStartProgress(unlockAdvancementEntry).isDone())) {
					this.unlockedDealsList.add(deal);
					this.unlockedDealsCounter++;
				} else {
					this.unlockedDealsList.add(null);
				}
			}
		}

		this.stockedDealsList.clear();
		this.stockedDealsCounter = 0;
		for (Shop.Deal deal : this.unlockedDealsList) {
			if (deal != null) {
				// TODO keep track of times traded, per player or per block, depending on deal.
//                int dealMaxStockCount = deal.getMaxStockCount();
//                if (dealMaxStockCount == -1) {
//                  this.stockedDealsList.add(deal);
//                }
				this.stockedDealsList.add(deal);
				this.stockedDealsCounter++;
			} else {
				this.stockedDealsList.add(null);
			}
		}
	}

	public record ShopBlockData(
			BlockPos blockPos
	) {

		public static final StreamCodec<RegistryFriendlyByteBuf, ShopBlockData> PACKET_CODEC = StreamCodec.ofMember(ShopBlockData::write, ShopBlockData::new);

		public ShopBlockData(RegistryFriendlyByteBuf registryByteBuf) {
			this(registryByteBuf.readBlockPos());
		}

		private void write(RegistryFriendlyByteBuf registryByteBuf) {
			registryByteBuf.writeBlockPos(blockPos);
		}
	}
}
