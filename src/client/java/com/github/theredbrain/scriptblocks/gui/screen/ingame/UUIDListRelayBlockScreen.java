package com.github.theredbrain.scriptblocks.gui.screen.ingame;

import com.github.theredbrain.scriptblocks.block.entity.UUIDListRelayBlockEntity;
import com.github.theredbrain.scriptblocks.network.packet.UpdateUUIDListRelayBlockPacket;
import com.github.theredbrain.scriptblocks.util.ItemUtils;
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
import net.minecraft.util.math.BlockPos;
import org.lwjgl.glfw.GLFW;

@Environment(value = EnvType.CLIENT)
public class UUIDListRelayBlockScreen extends Screen {
	private static final Text UUID_LIST_PROVIDER_POSITION_OFFSET_LABEL_TEXT = Text.translatable("gui.uuid_list_provider.uuid_list_provider_position_offset");
	private final UUIDListRelayBlockEntity uuidListRelayBlock;
	private TextFieldWidget uuidListProviderPositionOffsetXField;
	private TextFieldWidget uuidListProviderPositionOffsetYField;
	private TextFieldWidget uuidListProviderPositionOffsetZField;

	public UUIDListRelayBlockScreen(UUIDListRelayBlockEntity uuidListRelayBlockEntity) {
		super(NarratorManager.EMPTY);
		this.uuidListRelayBlock = uuidListRelayBlockEntity;
	}

	private void done() {
		if (this.updateDelayTriggerBlock()) {
			this.close();
		}
	}

	private void cancel() {
		this.close();
	}

	@Override
	protected void init() {
		this.uuidListProviderPositionOffsetXField = new TextFieldWidget(this.textRenderer, this.width / 2 - 154, 80, 100, 20, Text.empty());
		this.uuidListProviderPositionOffsetXField.setMaxLength(128);
		this.uuidListProviderPositionOffsetXField.setText(Integer.toString(this.uuidListRelayBlock.getUuidListHandlerPositionOffset().getX()));
		this.addSelectableChild(this.uuidListProviderPositionOffsetXField);
		this.uuidListProviderPositionOffsetYField = new TextFieldWidget(this.textRenderer, this.width / 2 - 50, 80, 100, 20, Text.empty());
		this.uuidListProviderPositionOffsetYField.setMaxLength(128);
		this.uuidListProviderPositionOffsetYField.setText(Integer.toString(this.uuidListRelayBlock.getUuidListHandlerPositionOffset().getY()));
		this.addSelectableChild(this.uuidListProviderPositionOffsetYField);
		this.uuidListProviderPositionOffsetZField = new TextFieldWidget(this.textRenderer, this.width / 2 + 54, 80, 100, 20, Text.empty());
		this.uuidListProviderPositionOffsetZField.setMaxLength(128);
		this.uuidListProviderPositionOffsetZField.setText(Integer.toString(this.uuidListRelayBlock.getUuidListHandlerPositionOffset().getZ()));
		this.addSelectableChild(this.uuidListProviderPositionOffsetZField);

		this.addDrawableChild(ButtonWidget.builder(ScreenTexts.DONE, button -> this.done()).dimensions(this.width / 2 - 4 - 150, 145, 150, 20).build());
		this.addDrawableChild(ButtonWidget.builder(ScreenTexts.CANCEL, button -> this.cancel()).dimensions(this.width / 2 + 4, 145, 150, 20).build());
		this.setInitialFocus(this.uuidListProviderPositionOffsetXField);
	}

	@Override
	public void resize(MinecraftClient client, int width, int height) {
		String string = this.uuidListProviderPositionOffsetXField.getText();
		String string1 = this.uuidListProviderPositionOffsetYField.getText();
		String string2 = this.uuidListProviderPositionOffsetZField.getText();
		this.init(client, width, height);
		this.uuidListProviderPositionOffsetXField.setText(string);
		this.uuidListProviderPositionOffsetYField.setText(string1);
		this.uuidListProviderPositionOffsetZField.setText(string2);
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
			this.done();
			return true;
		}
		return super.keyPressed(keyCode, scanCode, modifiers);
	}

	private boolean updateDelayTriggerBlock() {
		ClientPlayNetworking.send(new UpdateUUIDListRelayBlockPacket(
				this.uuidListRelayBlock.getPos(),
				new BlockPos(
						ItemUtils.parseInt(this.uuidListProviderPositionOffsetXField.getText()),
						ItemUtils.parseInt(this.uuidListProviderPositionOffsetYField.getText()),
						ItemUtils.parseInt(this.uuidListProviderPositionOffsetZField.getText())
				)
		));
		return true;
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {

		super.render(context, mouseX, mouseY, delta);

		context.drawTextWithShadow(this.textRenderer, UUID_LIST_PROVIDER_POSITION_OFFSET_LABEL_TEXT, this.width / 2 - 153, 70, 0xA0A0A0);
		this.uuidListProviderPositionOffsetXField.render(context, mouseX, mouseY, delta);
		this.uuidListProviderPositionOffsetYField.render(context, mouseX, mouseY, delta);
		this.uuidListProviderPositionOffsetZField.render(context, mouseX, mouseY, delta);
	}

	@Override
	public boolean shouldPause() {
		return false;
	}
}
