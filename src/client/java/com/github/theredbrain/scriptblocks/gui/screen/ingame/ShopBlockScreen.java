package com.github.theredbrain.scriptblocks.gui.screen.ingame;

import com.github.theredbrain.scriptblocks.block.entity.ShopBlockEntity;
import com.github.theredbrain.scriptblocks.network.packet.UpdateShopBlockPacket;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.GameNarrator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

@Environment(value = EnvType.CLIENT)
public class ShopBlockScreen extends AbstractCreativeBlockSettingsScreen {
	private final ShopBlockEntity shopBlockEntity;
	private EditBox shopIdentifierField;

	public ShopBlockScreen(ShopBlockEntity shopBlockEntity) {
		super(GameNarrator.NO_TITLE);
		this.shopBlockEntity = shopBlockEntity;
	}

	@Override
	protected void init() {
		super.init();

		this.shopIdentifierField = new EditBox(this.font, this.width / 2 - 154, 114, 300, 20, Component.empty());
		this.shopIdentifierField.setMaxLength(128);
		this.shopIdentifierField.setValue(this.shopBlockEntity.getShopIdentifier());
		this.addWidget(this.shopIdentifierField);

		this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> this.done()).bounds(this.width / 2 - 4 - 150, 210, 150, 20).build());
		this.addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL, button -> this.cancel()).bounds(this.width / 2 + 4, 210, 150, 20).build());
	}

	@Override
	protected void setInitialFocus() {
		this.setInitialFocus(this.shopIdentifierField);
	}

	@Override
	public void resize(Minecraft client, int width, int height) {
		String string = this.shopIdentifierField.getValue();
		this.init(client, width, height);
		this.shopIdentifierField.setValue(string);
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
	public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {

		super.render(context, mouseX, mouseY, delta);

		this.shopIdentifierField.render(context, mouseX, mouseY, delta);
	}

	@Override
	protected boolean updateBlock() {
		ClientPlayNetworking.send(new UpdateShopBlockPacket(
				this.shopBlockEntity.getPos(),
				this.shopIdentifierField.getValue()
		));
		return true;
	}
}
