package com.github.theredbrain.scriptblocks.gui.screen.ingame;

import com.github.theredbrain.scriptblocks.block.entity.ShopBlockEntity;
import com.github.theredbrain.scriptblocks.network.packet.UpdateShopBlockPacket;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.GameNarrator;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

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
	public void resize(final int width, final int height) {
		String string = this.shopIdentifierField.getValue();
		this.init(width, height);
		this.shopIdentifierField.setValue(string);
	}

	@Override
	public boolean keyPressed(final KeyEvent event) {
		if (event.isConfirmation()) {
			this.done();
			return true;
		}
		return super.keyPressed(event);
	}

	@Override
	public void extractRenderState(final GuiGraphicsExtractor graphics, final int mouseX, final int mouseY, final float a) {

		super.extractRenderState(graphics, mouseX, mouseY, a);

		this.shopIdentifierField.extractRenderState(graphics, mouseX, mouseY, a);
	}

	@Override
	protected boolean updateBlock() {
		ClientPlayNetworking.send(new UpdateShopBlockPacket(
				this.shopBlockEntity.getBlockPos(),
				this.shopIdentifierField.getValue()
		));
		return true;
	}
}
