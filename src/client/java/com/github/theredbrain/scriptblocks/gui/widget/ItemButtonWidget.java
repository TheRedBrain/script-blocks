package com.github.theredbrain.scriptblocks.gui.widget;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

@Environment(value = EnvType.CLIENT)
public class ItemButtonWidget extends Button {

	protected final ItemStack itemStack;

	ItemButtonWidget(int x, int y, int width, int height, ItemStack itemStack, Button.OnPress onPress) {
		super(x, y, width, height, Plain.empty(), onPress, DEFAULT_NARRATION);
		this.itemStack = itemStack;
	}

	public static Builder builder(ItemStack itemStack, Button.OnPress onPress) {
		return new Builder(itemStack, onPress);
	}

	public void renderWidget(GuiGraphics context, int mouseX, int mouseY, float delta) {
		super.renderWidget(context, mouseX, mouseY, delta);
		if (this.visible) {
			int i = this.getX() + this.getWidth() / 2 - 8;
			int j = this.getY() + this.getHeight() / 2 - 8;
			context.renderFakeItem(this.itemStack, i, j);
		}
	}

	@Environment(EnvType.CLIENT)
	public static class Builder {
		private final ItemStack itemStack;
		private final Button.OnPress onPress;
		@Nullable
		private Tooltip tooltip;
		private int x;
		private int y;
		private int width = 150;
		private int height = 20;

		public Builder(ItemStack itemStack, Button.OnPress onPress) {
			this.itemStack = itemStack;
			this.onPress = onPress;
		}

		public Builder position(int x, int y) {
			this.x = x;
			this.y = y;
			return this;
		}

		public Builder width(int width) {
			this.width = width;
			return this;
		}

		public Builder size(int width, int height) {
			this.width = width;
			this.height = height;
			return this;
		}

		public Builder dimensions(int x, int y, int width, int height) {
			return this.position(x, y).size(width, height);
		}

		public ItemButtonWidget build() {
			ItemButtonWidget itemButtonWidget = new ItemButtonWidget(this.x, this.y, this.width, this.height, this.itemStack, this.onPress);
			itemButtonWidget.setTooltip(this.tooltip);
			return itemButtonWidget;
		}
	}
}
