package com.github.theredbrain.scriptblocks.gui.screen.ingame;

import com.github.theredbrain.scriptblocks.block.entity.ShopBlockEntity;
import com.github.theredbrain.scriptblocks.network.packet.UpdateShopBlockPacket;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.NarratorManager;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

@Environment(value = EnvType.CLIENT)
public class ShopBlockScreen extends Screen {
	private final ShopBlockEntity shopBlockEntity;
	private TextFieldWidget shopIdentifierField;

	public ShopBlockScreen(ShopBlockEntity shopBlockEntity) {
		super(NarratorManager.EMPTY);
		this.shopBlockEntity = shopBlockEntity;
	}

	private void done() {
		this.updateShopBlock();
		this.close();
	}

	@Override
	protected void init() {
		super.init();

		this.shopIdentifierField = new TextFieldWidget(this.textRenderer, this.width / 2 - 154, 114, 300, 20, Text.empty());
		this.shopIdentifierField.setMaxLength(128);
		this.shopIdentifierField.setText(this.shopBlockEntity.getShopIdentifier());
		this.addSelectableChild(this.shopIdentifierField);

		this.addDrawableChild(ButtonWidget.builder(ScreenTexts.DONE, button -> this.done()).dimensions(this.width / 2 - 4 - 150, 210, 150, 20).build());
		this.addDrawableChild(ButtonWidget.builder(ScreenTexts.CANCEL, button -> this.close()).dimensions(this.width / 2 + 4, 210, 150, 20).build());
	}

	@Override
	public void resize(MinecraftClient client, int width, int height) {
		String string = this.shopIdentifierField.getText();
		this.init(client, width, height);
		this.shopIdentifierField.setText(string);
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
			this.done();
			return true;
		}
		return super.keyPressed(keyCode, scanCode, modifiers);
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {

		super.render(context, mouseX, mouseY, delta);

		this.shopIdentifierField.render(context, mouseX, mouseY, delta);
	}

	private void updateShopBlock() {
		ClientPlayNetworking.send(new UpdateShopBlockPacket(
				this.shopBlockEntity.getPos(),
				this.shopIdentifierField.getText()
		));
	}
}
