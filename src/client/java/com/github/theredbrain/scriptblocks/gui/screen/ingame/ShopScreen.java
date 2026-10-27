package com.github.theredbrain.scriptblocks.gui.screen.ingame;

import com.github.theredbrain.scriptblocks.ScriptBlocks;
import com.github.theredbrain.scriptblocks.block.entity.ShopBlockEntity;
import com.github.theredbrain.scriptblocks.data.Shop;
import com.github.theredbrain.scriptblocks.network.packet.UpdateShopBlockPacket;
import com.github.theredbrain.scriptblocks.screen.ShopScreenHandler;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import java.util.List;

@Environment(value = EnvType.CLIENT)
public class ShopScreen extends AbstractContainerScreen<ShopScreenHandler> {
	private static final Component TRADE_BUTTON_LABEL_TEXT = Component.translatable("gui.shop_block.trade_button_label");
	public static final Identifier SHOP_BACKGROUND_TEXTURE = ScriptBlocks.identifier("textures/gui/container/shop_background.png");
	private static final Identifier SCROLL_BAR_BACKGROUND_8_68_TEXTURE = ScriptBlocks.identifier("scroll_bar/scroll_bar_background_8_68");
	private static final Identifier OUT_OF_STOCK_TEXTURE = Identifier.withDefaultNamespace("container/villager/out_of_stock");
	private static final Identifier HAS_STOCK_TEXTURE = ScriptBlocks.identifier("container/shop_screen/has_stock");
	private static final Identifier SCROLLER_VERTICAL_6_7_TEXTURE = ScriptBlocks.identifier("scroll_bar/scroller_vertical_6_7");
	private ShopBlockEntity shopBlockEntity;
	private Button tradeButton0;
	private Button tradeButton1;
	private Button tradeButton2;
	private EditBox shopIdentifierField;
	private Button saveCreativeButton;
	private Button cancelCreativeButton;
	private Component shopTitle = Component.empty();
	private Component playerOffersTitle = Component.empty();
	private int scrollPosition = 0;
	private float scrollAmount = 0.0f;
	private boolean mouseClicked = false;

	public ShopScreen(ShopScreenHandler handler, Inventory inventory, Component title) {
		super(handler, inventory, title);
		handler.setContentsChangedListener(this::onInventoryChange);
		this.shopBlockEntity = handler.getShopBlockEntity();
	}

	private void trade(int index) {
		if (index + this.scrollPosition < this.menu.getStockedDealsList().size()) {
			Shop.Deal currentDeal = this.menu.getStockedDealsList().get(index + this.scrollPosition);
			if (currentDeal != null && this.minecraft != null && this.minecraft.gameMode != null) {
				if (this.menu.onButtonClick(this.minecraft.player, index + this.scrollPosition)) {
					Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.master(SoundEvents.UI_STONECUTTER_SELECT_RECIPE, 1.0F));
					this.minecraft.gameMode.handleInventoryButtonClick(this.menu.syncId, index + this.scrollPosition);
				}
			}
		}
	}

	private void calculateActiveDeals() {

		if (this.tradeButton0 != null && this.tradeButton1 != null && this.tradeButton2 != null) {
			this.tradeButton0.active = false;
			this.tradeButton1.active = false;
			this.tradeButton2.active = false;

			Container inventory = new SimpleContainer(9);

			int index = 0;
			List<Shop.Deal> stockedDealsList = this.menu.getStockedDealsList();
			for (int i = this.scrollPosition; i < Math.min(this.scrollPosition + 3, this.menu.getStockedDealsCounter()); i++) {
				if (i > stockedDealsList.size()) {
					break;
				}
				Shop.Deal deal = stockedDealsList.get(i);
				if (deal != null) {
					for (int k = 0; k < 9; k++) {
						ItemStack itemStack = this.menu.getInventory().getStack(k);
						inventory.setItem(k, itemStack.copy());
					}
					boolean bl = true;
					List<ItemStack> priceList = deal.price();
					for (ItemStack priceStack : priceList) {
						int priceCount = priceStack.getCount();
						for (int j = 0; j < inventory.getContainerSize(); j++) {
							if (ItemStack.isSameItemSameComponents(inventory.getItem(j), priceStack)) {
								int stackCount = inventory.getItem(j).getCount();
								if (stackCount >= priceCount) {
									inventory.removeItem(j, priceCount);
									priceCount = 0;
									break;
								} else {
									inventory.setItem(j, ItemStack.EMPTY);
									priceCount = priceCount - stackCount;
								}
							}
						}
						if (priceCount > 0) {
							bl = false;
						}
					}
					if (bl) {
						if (index == 0) {
							this.tradeButton0.active = true;
						} else if (index == 1) {
							this.tradeButton1.active = true;
						} else if (index == 2) {
							this.tradeButton2.active = true;
						}
					}
					index++;
				}
			}
			this.updateWidgets();
		}
	}

	private void saveCreative() {
		if (this.updateShopBlockCreative()) {
			this.onClose();
		}
	}

	private void cancel() {
		this.onClose();
	}

	@Override
	protected void init() {
		if (this.minecraft != null && this.minecraft.level != null) {
			BlockEntity blockEntity = this.minecraft.level.getBlockEntity(this.menu.getBlockPos());
			if (blockEntity instanceof ShopBlockEntity) {
				this.shopBlockEntity = (ShopBlockEntity) blockEntity;
			}
		}
		if (this.menu.getShop() == null && this.minecraft != null) {
			this.minecraft.setScreen(null);
			return;
		}

		this.imageWidth = 176;
		this.imageHeight = 212;
//        this.x = (this.width - this.backgroundWidth) / 2;
//        this.y = (this.height - this.backgroundHeight) / 2;

		this.inventoryLabelY = 118;
		super.init();

		this.tradeButton0 = this.addRenderableWidget(Button.builder(TRADE_BUTTON_LABEL_TEXT, button -> this.trade(0)).bounds(this.leftPos + 107, this.topPos + 16, 50, 20).build());
		this.tradeButton1 = this.addRenderableWidget(Button.builder(TRADE_BUTTON_LABEL_TEXT, button -> this.trade(1)).bounds(this.leftPos + 107, this.topPos + 40, 50, 20).build());
		this.tradeButton2 = this.addRenderableWidget(Button.builder(TRADE_BUTTON_LABEL_TEXT, button -> this.trade(2)).bounds(this.leftPos + 107, this.topPos + 64, 50, 20).build());

		this.shopIdentifierField = new EditBox(this.font, this.width / 2 - 154, 114, 300, 20, Component.empty());
		this.shopIdentifierField.setMaxLength(128);
		this.shopIdentifierField.setValue(this.shopBlockEntity.getShopIdentifier());
		this.addWidget(this.shopIdentifierField);

		this.saveCreativeButton = this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> this.saveCreative()).bounds(this.width / 2 - 4 - 150, 210, 150, 20).build());
		this.cancelCreativeButton = this.addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL, button -> this.cancel()).bounds(this.width / 2 + 4, 210, 150, 20).build());

		Shop shop = this.menu.getShop();
		if (shop != null) {
			this.menu.calculateUnlockedAndStockedDeals();
			this.calculateActiveDeals();
			this.shopTitle = Component.translatable(shop.shopTitle());
			this.playerOffersTitle = Component.translatable(shop.offersTitle());
		} else {
			this.updateWidgets();
		}
	}

	private void updateWidgets() {

		this.tradeButton0.visible = false;
		this.tradeButton1.visible = false;
		this.tradeButton2.visible = false;

		this.shopIdentifierField.setVisible(false);

		this.saveCreativeButton.visible = false;
		this.cancelCreativeButton.visible = false;

		int index = 0;
		for (int i = 0; i < Math.min(3, this.menu.getUnlockedDealsCounter()); i++) {
			if (index == 0) {
				this.tradeButton0.visible = true;
			} else if (index == 1) {
				this.tradeButton1.visible = true;
			} else if (index == 2) {
				this.tradeButton2.visible = true;
			}
			index++;
		}
		this.scrollPosition = 0;
		this.scrollAmount = 0.0f;
	}

	@Override
	public void resize(Minecraft client, int width, int height) {
		String string = this.shopIdentifierField.getValue();
		this.init(client, width, height);
		this.shopIdentifierField.setValue(string);
		this.updateWidgets();
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		this.mouseClicked = false;
		if (this.menu.getUnlockedDealsCounter() > 3) {
			int i = this.leftPos + this.imageWidth - 14;
			int j = this.topPos + 99;
			if (mouseX >= (double) i && mouseX < (double) (i + 6) && mouseY >= (double) j && mouseY < (double) (j + 90)) {
				this.mouseClicked = true;
			}
		}
		return super.mouseClicked(mouseX, mouseY, button);
	}

	@Override
	public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
		if (this.menu.getUnlockedDealsCounter() > 3
				&& this.mouseClicked) {
			int i = this.menu.getUnlockedDealsCounter() - 3;
			float f = (float) deltaY / (float) i;
			this.scrollAmount = Mth.clamp(this.scrollAmount + f, 0.0f, 1.0f);
			this.scrollPosition = (int) ((double) (this.scrollAmount * (float) i));
		}
		return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
		if (this.menu.getUnlockedDealsCounter() > 3
				&& mouseX >= (double) (this.leftPos + 7) && mouseX <= (double) (this.leftPos + this.imageWidth - 7)
				&& mouseY >= (double) (this.topPos + 98) && mouseY <= (double) (this.topPos + 190)) {
			int i = this.menu.getUnlockedDealsCounter() - 3;
			float f = (float) verticalAmount / (float) i;
			this.scrollAmount = Mth.clamp(this.scrollAmount - f, 0.0f, 1.0f);
			this.scrollPosition = (int) ((double) (this.scrollAmount * (float) i));
		}
		return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
	}

	@Override
	public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {

		super.render(context, mouseX, mouseY, delta);

		int x;
		int y;
		int k;
		int index = 0;
		for (int i = this.scrollPosition; i < Math.min(this.scrollPosition + 3, this.menu.getUnlockedDealsCounter()); i++) {
			if (i > this.menu.getUnlockedDealsList().size()) {
				break;
			}
			Shop.Deal deal = this.menu.getUnlockedDealsList().get(i);
			if (deal != null) {
				for (int j = 0; j < deal.offer().size(); j++) {
					ItemStack offerStack = deal.offer().get(j);
					x = this.leftPos + 85 + (j * 18);
					y = this.topPos + 18 + (index * 24);
					k = x + y * this.imageWidth;
					context.renderFakeItem(offerStack, x, y, k);
					context.drawItemInSlot(this.font, offerStack, x, y);
				}
				for (int j = 0; j < deal.price().size(); j++) {
					ItemStack priceStack = deal.price().get(j);
					x = this.leftPos + 8 + (j * 18);
					y = this.topPos + 18 + (index * 24);
					k = x + y * this.imageWidth;
					context.renderFakeItem(priceStack, x, y, k);
					context.drawItemInSlot(this.font, priceStack, x, y);
				}
				context.blitSprite(this.menu.getStockedDealsList().get(i) != null ? HAS_STOCK_TEXTURE : OUT_OF_STOCK_TEXTURE, this.leftPos + 55, this.topPos + 16 + (index * 24), 28, 21);
				index++;
			}
			if (this.menu.getUnlockedDealsCounter() > 3) {
				context.blitSprite(SCROLL_BAR_BACKGROUND_8_68_TEXTURE, this.leftPos + 161, this.topPos + 16, 8, 68);
				k = (int) (59.0f * this.scrollAmount);
				context.blitSprite(SCROLLER_VERTICAL_6_7_TEXTURE, this.leftPos + 161 + 1, this.topPos + 16 + 1 + k, 6, 7);
			}
		}
	}

	@Override
	protected void renderLabels(GuiGraphics context, int mouseX, int mouseY) {
		context.drawString(this.font, this.shopTitle, this.titleLabelX, this.titleLabelY, 4210752, false);
		context.drawString(this.font, this.playerOffersTitle, this.titleLabelX, this.titleLabelY + 80, 4210752, false);
		context.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, 4210752, false);
	}

	@Override
	public void renderBg(GuiGraphics context, float delta, int mouseX, int mouseY) {
		int i = this.leftPos;
		int j = this.topPos;
		context.blit(SHOP_BACKGROUND_TEXTURE, i, j, 0, 0, this.imageWidth, this.imageHeight, this.imageWidth, this.imageHeight);

	}

	private boolean updateShopBlockCreative() {
		ClientPlayNetworking.send(new UpdateShopBlockPacket(
				this.shopBlockEntity.getPos(),
				this.shopIdentifierField.getValue()
		));
		return true;
	}

	private void onInventoryChange() {
		this.calculateActiveDeals();
	}
}
