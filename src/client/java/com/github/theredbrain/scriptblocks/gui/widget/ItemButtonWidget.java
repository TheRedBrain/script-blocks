package com.github.theredbrain.scriptblocks.gui.widget;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class ItemButtonWidget extends ButtonWidget {

    protected final ItemStack itemStack;

    ItemButtonWidget(int x, int y, int width, int height, ItemStack itemStack, ButtonWidget.PressAction onPress) {
        super(x, y, width, height, Text.empty(), onPress, DEFAULT_NARRATION_SUPPLIER);
        this.itemStack = itemStack;
    }

    public static Builder builder(ItemStack itemStack, ButtonWidget.PressAction onPress) {
        return new Builder(itemStack, onPress);
    }

    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        if (this.visible) {
            int i = this.getX() + this.getWidth() / 2 - 8;
            int j = this.getY() + this.getHeight() / 2 - 8;
            context.drawItemWithoutEntity(this.itemStack, i, j);
        }
    }

    @Environment(EnvType.CLIENT)
    public static class Builder {
        private final ItemStack itemStack;
        private final ButtonWidget.PressAction onPress;
        @Nullable
        private Tooltip tooltip;
        private int x;
        private int y;
        private int width = 150;
        private int height = 20;

        public Builder(ItemStack itemStack, ButtonWidget.PressAction onPress) {
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
