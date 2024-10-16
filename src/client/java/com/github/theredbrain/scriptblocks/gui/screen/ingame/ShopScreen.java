package com.github.theredbrain.scriptblocks.gui.screen.ingame;

import com.github.theredbrain.scriptblocks.ScriptBlocks;
import com.github.theredbrain.scriptblocks.block.entity.ShopBlockEntity;
import com.github.theredbrain.scriptblocks.data.Shop;
import com.github.theredbrain.scriptblocks.network.packet.UpdateShopBlockPacket;
import com.github.theredbrain.scriptblocks.screen.ShopScreenHandler;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

import java.util.List;

@Environment(value = EnvType.CLIENT)
public class ShopScreen extends HandledScreen<ShopScreenHandler> {
	private static final Text TRADE_BUTTON_LABEL_TEXT = Text.translatable("gui.shop_block.trade_button_label");
	public static final Identifier SHOP_BACKGROUND_TEXTURE = ScriptBlocks.identifier("textures/gui/container/shop_background.png");
	private static final Identifier SCROLL_BAR_BACKGROUND_8_68_TEXTURE = ScriptBlocks.identifier("scroll_bar/scroll_bar_background_8_68");
	private static final Identifier OUT_OF_STOCK_TEXTURE = Identifier.ofVanilla("container/villager/out_of_stock");
	private static final Identifier HAS_STOCK_TEXTURE = ScriptBlocks.identifier("container/shop_screen/has_stock");
	private static final Identifier SCROLLER_VERTICAL_6_7_TEXTURE = ScriptBlocks.identifier("scroll_bar/scroller_vertical_6_7");
	private ShopBlockEntity shopBlockEntity;
	private ButtonWidget tradeButton0;
	private ButtonWidget tradeButton1;
	private ButtonWidget tradeButton2;
	private TextFieldWidget shopIdentifierField;
	private ButtonWidget saveCreativeButton;
	private ButtonWidget cancelCreativeButton;
	private Text shopTitle = Text.empty();
	private Text playerOffersTitle = Text.empty();
	private int scrollPosition = 0;
	private float scrollAmount = 0.0f;
	private boolean mouseClicked = false;

	public ShopScreen(ShopScreenHandler handler, PlayerInventory inventory, Text title) {
		super(handler, inventory, title);
		handler.setContentsChangedListener(this::onInventoryChange);
		this.shopBlockEntity = handler.getShopBlockEntity();
	}

	private void trade(int index) {
		if (index + this.scrollPosition < this.handler.getStockedDealsList().size()) {
			Shop.Deal currentDeal = this.handler.getStockedDealsList().get(index + this.scrollPosition);
			if (currentDeal != null && this.client != null && this.client.interactionManager != null) {
				if (this.handler.onButtonClick(this.client.player, index + this.scrollPosition)) {
					MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_STONECUTTER_SELECT_RECIPE, 1.0F));
					this.client.interactionManager.clickButton(this.handler.syncId, index + this.scrollPosition);
				}
			}
		}
	}

	private void calculateActiveDeals() {

		if (this.tradeButton0 != null && this.tradeButton1 != null && this.tradeButton2 != null) {
			this.tradeButton0.active = false;
			this.tradeButton1.active = false;
			this.tradeButton2.active = false;

			Inventory inventory = new SimpleInventory(9);

			int index = 0;
			List<Shop.Deal> stockedDealsList = this.handler.getStockedDealsList();
			for (int i = this.scrollPosition; i < Math.min(this.scrollPosition + 3, this.handler.getStockedDealsCounter()); i++) {
				if (i > stockedDealsList.size()) {
					break;
				}
				Shop.Deal deal = stockedDealsList.get(i);
				if (deal != null) {
					for (int k = 0; k < 9; k++) {
						ItemStack itemStack = this.handler.getInventory().getStack(k);
						inventory.setStack(k, itemStack.copy());
					}
					boolean bl = true;
					List<ItemStack> priceList = deal.price();
					for (ItemStack price : priceList) {
						Item virtualItem = price.getItem();
						int priceCount = price.getCount();
						for (int j = 0; j < inventory.size(); j++) {
							if (inventory.getStack(j).isOf(virtualItem)) {
								int stackCount = inventory.getStack(j).getCount();
								if (stackCount >= priceCount) {
									inventory.removeStack(j, priceCount);
									priceCount = 0;
									break;
								} else {
									inventory.setStack(j, ItemStack.EMPTY);
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
			this.close();
		}
	}

	private void cancel() {
		this.close();
	}

	@Override
	protected void init() {
		if (this.client != null && this.client.world != null) {
			BlockEntity blockEntity = this.client.world.getBlockEntity(this.handler.getBlockPos());
			if (blockEntity instanceof ShopBlockEntity) {
				this.shopBlockEntity = (ShopBlockEntity) blockEntity;
			}
		}
		if (this.handler.getShop() == null && this.client != null) {
			this.client.setScreen(null);
			return;
		}

		this.backgroundWidth = 176;
		this.backgroundHeight = 212;
//        this.x = (this.width - this.backgroundWidth) / 2;
//        this.y = (this.height - this.backgroundHeight) / 2;

		this.playerInventoryTitleY = 118;
		super.init();

		this.tradeButton0 = this.addDrawableChild(ButtonWidget.builder(TRADE_BUTTON_LABEL_TEXT, button -> this.trade(0)).dimensions(this.x + 107, this.y + 16, 50, 20).build());
		this.tradeButton1 = this.addDrawableChild(ButtonWidget.builder(TRADE_BUTTON_LABEL_TEXT, button -> this.trade(1)).dimensions(this.x + 107, this.y + 40, 50, 20).build());
		this.tradeButton2 = this.addDrawableChild(ButtonWidget.builder(TRADE_BUTTON_LABEL_TEXT, button -> this.trade(2)).dimensions(this.x + 107, this.y + 64, 50, 20).build());

		this.shopIdentifierField = new TextFieldWidget(this.textRenderer, this.width / 2 - 154, 114, 300, 20, Text.empty());
		this.shopIdentifierField.setMaxLength(128);
		this.shopIdentifierField.setText(this.shopBlockEntity.getShopIdentifier());
		this.addSelectableChild(this.shopIdentifierField);

		this.saveCreativeButton = this.addDrawableChild(ButtonWidget.builder(ScreenTexts.DONE, button -> this.saveCreative()).dimensions(this.width / 2 - 4 - 150, 210, 150, 20).build());
		this.cancelCreativeButton = this.addDrawableChild(ButtonWidget.builder(ScreenTexts.CANCEL, button -> this.cancel()).dimensions(this.width / 2 + 4, 210, 150, 20).build());

		Shop shop = this.handler.getShop();
		if (shop != null) {
			this.handler.calculateUnlockedAndStockedDeals();
			this.calculateActiveDeals();
			this.shopTitle = Text.translatable(shop.shopTitle());
			this.playerOffersTitle = Text.translatable(shop.offersTitle());
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
		for (int i = 0; i < Math.min(3, this.handler.getUnlockedDealsCounter()); i++) {
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
	public void resize(MinecraftClient client, int width, int height) {
		String string = this.shopIdentifierField.getText();
		this.init(client, width, height);
		this.shopIdentifierField.setText(string);
		this.updateWidgets();
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		this.mouseClicked = false;
		if (this.handler.getUnlockedDealsCounter() > 3) {
			int i = this.x + this.backgroundWidth - 14;
			int j = this.y + 99;
			if (mouseX >= (double) i && mouseX < (double) (i + 6) && mouseY >= (double) j && mouseY < (double) (j + 90)) {
				this.mouseClicked = true;
			}
		}
		return super.mouseClicked(mouseX, mouseY, button);
	}

	@Override
	public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
		if (this.handler.getUnlockedDealsCounter() > 3
				&& this.mouseClicked) {
			int i = this.handler.getUnlockedDealsCounter() - 3;
			float f = (float) deltaY / (float) i;
			this.scrollAmount = MathHelper.clamp(this.scrollAmount + f, 0.0f, 1.0f);
			this.scrollPosition = (int) ((double) (this.scrollAmount * (float) i));
		}
		return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
		if (this.handler.getUnlockedDealsCounter() > 3
				&& mouseX >= (double) (this.x + 7) && mouseX <= (double) (this.x + this.backgroundWidth - 7)
				&& mouseY >= (double) (this.y + 98) && mouseY <= (double) (this.y + 190)) {
			int i = this.handler.getUnlockedDealsCounter() - 3;
			float f = (float) verticalAmount / (float) i;
			this.scrollAmount = MathHelper.clamp(this.scrollAmount - f, 0.0f, 1.0f);
			this.scrollPosition = (int) ((double) (this.scrollAmount * (float) i));
		}
		return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {

		super.render(context, mouseX, mouseY, delta);

		int x;
		int y;
		int k;
		int index = 0;
		for (int i = this.scrollPosition; i < Math.min(this.scrollPosition + 3, this.handler.getUnlockedDealsCounter()); i++) {
			if (i > this.handler.getUnlockedDealsList().size()) {
				break;
			}
			Shop.Deal deal = this.handler.getUnlockedDealsList().get(i);
			if (deal != null) {
				ItemStack offerItemStack = deal.offer();
				x = this.x + 85;
				y = this.y + 18 + (index * 24);
				k = x + y * this.backgroundWidth;
				context.drawItemWithoutEntity(offerItemStack, x, y/*, k*/);
				context.drawItemInSlot(this.textRenderer, offerItemStack, x, y);
				for (int j = 0; j < deal.price().size(); j++) {
					ItemStack priceItemStack = deal.price().get(j);
					x = this.x + 8 + (j * 18);
					y = this.y + 18 + (index * 24);
					k = x + y * this.backgroundWidth;
					context.drawItemWithoutEntity(priceItemStack, x, y/*, k*/);
					context.drawItemInSlot(this.textRenderer, priceItemStack, x, y);
				}
				context.drawGuiTexture(this.handler.getStockedDealsList().get(i) != null ? HAS_STOCK_TEXTURE : OUT_OF_STOCK_TEXTURE, this.x + 55, this.y + 16 + (index * 24), 28, 21);
				index++;
			}
			if (this.handler.getUnlockedDealsCounter() > 3) {
				context.drawGuiTexture(SCROLL_BAR_BACKGROUND_8_68_TEXTURE, this.x + 161, this.y + 16, 8, 68);
				k = (int) (59.0f * this.scrollAmount);
				context.drawGuiTexture(SCROLLER_VERTICAL_6_7_TEXTURE, this.x + 161 + 1, this.y + 16 + 1 + k, 6, 7);
			}
		}
	}

	@Override
	protected void drawForeground(DrawContext context, int mouseX, int mouseY) {
		context.drawText(this.textRenderer, this.shopTitle, this.titleX, this.titleY, 4210752, false);
		context.drawText(this.textRenderer, this.playerOffersTitle, this.titleX, this.titleY + 80, 4210752, false);
		context.drawText(this.textRenderer, this.playerInventoryTitle, this.playerInventoryTitleX, this.playerInventoryTitleY, 4210752, false);
	}

	@Override
	public void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
		int i = this.x;
		int j = this.y;
		context.drawTexture(SHOP_BACKGROUND_TEXTURE, i, j, 0, 0, this.backgroundWidth, this.backgroundHeight, this.backgroundWidth, this.backgroundHeight);

	}

	private boolean updateShopBlockCreative() {
		ClientPlayNetworking.send(new UpdateShopBlockPacket(
				this.shopBlockEntity.getPos(),
				this.shopIdentifierField.getText()
		));
		return true;
	}

	private void onInventoryChange() {
		this.calculateActiveDeals();
	}
}
