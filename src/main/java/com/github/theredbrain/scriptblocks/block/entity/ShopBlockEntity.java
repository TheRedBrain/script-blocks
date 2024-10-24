package com.github.theredbrain.scriptblocks.block.entity;

import com.github.theredbrain.scriptblocks.data.Shop;
import com.github.theredbrain.scriptblocks.registry.EntityRegistry;
import com.github.theredbrain.scriptblocks.registry.ShopsRegistry;
import com.github.theredbrain.scriptblocks.screen.ShopScreenHandler;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class ShopBlockEntity extends BlockEntity implements ExtendedScreenHandlerFactory {
	private String shopIdentifier = "";

	// TODO ShopBlockEntity should implement ShopAnchor instead, which saves shopOffer uses, which are compared to maxOfferAmount
	private List<Integer> stockCountList = new ArrayList<>();

	public ShopBlockEntity(BlockPos pos, BlockState state) {
		super(EntityRegistry.SHOP_BLOCK_ENTITY, pos, state);
	}

	@Override
	protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {

		if (!this.shopIdentifier.equals("")) {
			nbt.putString("shopName", this.shopIdentifier);
		}

		super.writeNbt(nbt, registryLookup);
	}

	@Override
	protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {

		if (nbt.contains("shopName")) {
			this.shopIdentifier = nbt.getString("shopName");
		}

		super.readNbt(nbt, registryLookup);
	}

	public BlockEntityUpdateS2CPacket toUpdatePacket() {
		return BlockEntityUpdateS2CPacket.create(this);
	}

	@Override
	public NbtCompound toInitialChunkDataNbt(RegistryWrapper.WrapperLookup registryLookup) {
		return this.createComponentlessNbt(registryLookup);
	}

	public String getShopIdentifier() {
		return shopIdentifier;
	}

	public boolean setShopIdentifier(String newShopIdentifier) {
		Shop shop = null;
		Identifier identifier = Identifier.tryParse(newShopIdentifier);
		if (identifier != null) {
			shop = ShopsRegistry.registeredShops.get(identifier);
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
	public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
		return new ShopScreenHandler(syncId, playerInventory, this.pos);
	}

	@Override
	public Text getDisplayName() {
		return Text.empty();
	}

	@Override
	public Object getScreenOpeningData(ServerPlayerEntity player) {
		return new ShopScreenHandler.ShopBlockData(this.pos);
	}
}
