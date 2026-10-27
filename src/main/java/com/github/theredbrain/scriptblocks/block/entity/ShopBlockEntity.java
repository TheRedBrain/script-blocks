package com.github.theredbrain.scriptblocks.block.entity;

import com.github.theredbrain.scriptblocks.data.Shop;
import com.github.theredbrain.scriptblocks.registry.ScriptBlocksDynamicRegistries;
import com.github.theredbrain.scriptblocks.registry.ScriptBlocksEntities;
import com.github.theredbrain.scriptblocks.screen.ShopScreenHandler;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ShopBlockEntity extends BlockEntity implements ExtendedScreenHandlerFactory {
	private String shopIdentifier = "";

	// TODO ShopBlockEntity should implement ShopAnchor instead, which saves shopOffer uses, which are compared to maxOfferAmount
	private final List<Integer> stockCountList = new ArrayList<>();

	public ShopBlockEntity(BlockPos pos, BlockState state) {
		super(ScriptBlocksEntities.SHOP_BLOCK_ENTITY, pos, state);
	}

	@Override
	protected void saveAdditional(ValueOutput view) {

		view.putString("shop_identifier", this.shopIdentifier);

		super.saveAdditional(view);
	}

	@Override
	protected void loadAdditional(ValueInput view) {

		this.shopIdentifier = view.getStringOr("shop_identifier", "");

		super.loadAdditional(view);
	}

	public ClientboundBlockEntityDataPacket getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}

	@Override
	public CompoundTag getUpdateTag(HolderLookup.Provider registryLookup) {
		return this.saveCustomOnly(registryLookup);
	}

	public String getShopIdentifier() {
		return shopIdentifier;
	}

	public boolean setShopIdentifier(String newShopIdentifier) {
		Shop shop = null;
		Identifier identifier = Identifier.tryParse(newShopIdentifier);
		if (identifier != null && this.level != null) {
			Optional<Holder.Reference<Shop>> optionalShopReference = this.level.registryAccess().get(ScriptBlocksDynamicRegistries.SHOP_REGISTRY_KEY).getEntry(identifier);
			if (optionalShopReference.isPresent()) {
				shop = optionalShopReference.get().value();
			}
		}
		if (newShopIdentifier.isEmpty() || shop != null) {
			this.shopIdentifier = newShopIdentifier;
			this.stockCountList.clear();

			if (shop != null) {
				List<Shop.Deal> dealList = shop.dealList();
				for (int i = 0; i < dealList.size(); i++) {
					this.stockCountList.add(dealList.get(i).maxStockCount());
				}
			}
			return true;
		}
		return false;
	}

	@Nullable
	@Override
	public AbstractContainerMenu createMenu(int syncId, Inventory playerInventory, Player player) {
		return new ShopScreenHandler(syncId, playerInventory, this.worldPosition);
	}

	@Override
	public Component getDisplayName() {
		return Component.empty();
	}

	@Override
	public Object getScreenOpeningData(ServerPlayer player) {
		return new ShopScreenHandler.ShopBlockData(this.worldPosition);
	}
}
