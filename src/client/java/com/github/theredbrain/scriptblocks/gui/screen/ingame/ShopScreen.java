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
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
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
import net.minecraft.world.item.ItemStackTemplate;
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
		super(handler, inventory, title, 176, 212);
		handler.setContentsChangedListener(this::onInventoryChange);
		this.shopBlockEntity = handler.getShopBlockEntity();
	}

	private void trade(int index) {
		if (index + this.scrollPosition < this.menu.getStockedDealsList().size()) {
			Shop.Deal currentDeal = this.menu.getStockedDealsList().get(index + this.scrollPosition);
			if (currentDeal != null && this.minecraft.player != null && this.minecraft.gameMode != null) {
				if (this.menu.clickMenuButton(this.minecraft.player, index + this.scrollPosition)) {
					Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_STONECUTTER_SELECT_RECIPE, 1.0F));
					this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, index + this.scrollPosition);
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
						ItemStack itemStack = this.menu.getInventory().getItem(k);
						inventory.setItem(k, itemStack.copy());
					}
					boolean bl = true;
					for (ItemStackTemplate priceStackTemplate : deal.price()) {
						ItemStack priceStack = priceStackTemplate.create();
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
		if (this.minecraft.level != null) {
			BlockEntity blockEntity = this.minecraft.level.getBlockEntity(this.menu.getBlockPos());
			if (blockEntity instanceof ShopBlockEntity) {
				this.shopBlockEntity = (ShopBlockEntity) blockEntity;
			}
		}
		if (this.menu.getShop() == null) {
			this.minecraft.setScreen(null);
			return;
		}

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
	public void resize(final int width, final int height) {
		String string = this.shopIdentifierField.getValue();
		this.init(width, height);
		this.shopIdentifierField.setValue(string);
		this.updateWidgets();
	}

	@Override
	public boolean mouseClicked(final MouseButtonEvent event, final boolean doubleClick) {
		this.mouseClicked = false;
		if (this.menu.getUnlockedDealsCounter() > 3) {
			int i = this.leftPos + this.imageWidth - 14;
			int j = this.topPos + 99;
			if (event.x() >= (double) i && event.x() < (double) (i + 6) && event.y() >= (double) j && event.y() < (double) (j + 90)) {
				this.mouseClicked = true;
			}
		}
		return super.mouseClicked(event, doubleClick);
	}

	@Override
	public boolean mouseDragged(final MouseButtonEvent event, final double dx, final double dy) {
		if (this.menu.getUnlockedDealsCounter() > 3
				&& this.mouseClicked) {
			int i = this.menu.getUnlockedDealsCounter() - 3;
			float f = (float) dy / (float) i;
			this.scrollAmount = Mth.clamp(this.scrollAmount + f, 0.0f, 1.0f);
			this.scrollPosition = (int) ((double) (this.scrollAmount * (float) i));
		}
		return super.mouseDragged(event, dx, dy);
	}

	@Override
	public boolean mouseScrolled(final double x, final double y, final double scrollX, final double scrollY) {
		if (this.menu.getUnlockedDealsCounter() > 3
				&& x >= (double) (this.leftPos + 7) && x <= (double) (this.leftPos + this.imageWidth - 7)
				&& y >= (double) (this.topPos + 98) && y <= (double) (this.topPos + 190)) {
			int i = this.menu.getUnlockedDealsCounter() - 3;
			float f = (float) scrollY / (float) i;
			this.scrollAmount = Mth.clamp(this.scrollAmount - f, 0.0f, 1.0f);
			this.scrollPosition = (int) ((double) (this.scrollAmount * (float) i));
		}
		return super.mouseScrolled(x, y, scrollX, scrollY);
	}

	@Override
	public void extractRenderState(final GuiGraphicsExtractor graphics, final int mouseX, final int mouseY, final float a) {

		super.extractRenderState(graphics, mouseX, mouseY, a);

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
					ItemStack offerStack = deal.offer().get(j).create();
					x = this.leftPos + 85 + (j * 18);
					y = this.topPos + 18 + (index * 24);
					k = x + y * this.imageWidth;
					graphics.fakeItem(offerStack, x, y, k);
					graphics.itemDecorations(this.font, offerStack, x, y, String.valueOf(offerStack.count()));
				}
				for (int j = 0; j < deal.price().size(); j++) {
					ItemStack priceStack = deal.price().get(j).create();
					x = this.leftPos + 8 + (j * 18);
					y = this.topPos + 18 + (index * 24);
					k = x + y * this.imageWidth;
					graphics.fakeItem(priceStack, x, y, k);
					graphics.itemDecorations(this.font, priceStack, x, y, String.valueOf(priceStack.count()));
				}
				graphics.blitSprite(RenderPipelines.GUI_TEXTURED, this.menu.getStockedDealsList().get(i) != null ? HAS_STOCK_TEXTURE : OUT_OF_STOCK_TEXTURE, this.leftPos + 55, this.topPos + 16 + (index * 24), 28, 21);
				index++;
			}
			if (this.menu.getUnlockedDealsCounter() > 3) {
				graphics.blitSprite(RenderPipelines.GUI_TEXTURED, SCROLL_BAR_BACKGROUND_8_68_TEXTURE, this.leftPos + 161, this.topPos + 16, 8, 68);
				k = (int) (59.0f * this.scrollAmount);
				graphics.blitSprite(RenderPipelines.GUI_TEXTURED, SCROLLER_VERTICAL_6_7_TEXTURE, this.leftPos + 161 + 1, this.topPos + 16 + 1 + k, 6, 7);
			}
		}
	}

	@Override
	protected void extractLabels(final GuiGraphicsExtractor graphics, final int xm, final int ym) {
		graphics.text(this.font, this.shopTitle, this.titleLabelX, this.titleLabelY, 4210752, false);
		graphics.text(this.font, this.playerOffersTitle, this.titleLabelX, this.titleLabelY + 80, 4210752, false);
		graphics.text(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, 4210752, false);
	}

	@Override
	public void extractBackground(final GuiGraphicsExtractor graphics, final int mouseX, final int mouseY, final float a) {
		super.extractBackground(graphics, mouseX, mouseY, a);
		graphics.blit(RenderPipelines.GUI_TEXTURED, SHOP_BACKGROUND_TEXTURE, this.leftPos, this.topPos, 0.0F, 0.0F, this.imageWidth, this.imageHeight, this.imageWidth, this.imageHeight);
	}

	private boolean updateShopBlockCreative() {
		ClientPlayNetworking.send(new UpdateShopBlockPacket(
				this.shopBlockEntity.getBlockPos(),
				this.shopIdentifierField.getValue()
		));
		return true;
	}

	private void onInventoryChange() {
		this.calculateActiveDeals();
	}
}
