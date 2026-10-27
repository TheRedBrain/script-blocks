package com.github.theredbrain.scriptblocks.gui.screen.ingame;

import com.github.theredbrain.scriptblocks.block.entity.UseRelayBlockEntity;
import com.github.theredbrain.scriptblocks.network.packet.UpdateUseRelayBlockPacket;
import com.github.theredbrain.scriptblocks.util.ItemUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.GameNarrator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

@Environment(value = EnvType.CLIENT)
public class UseRelayBlockScreen extends AbstractCreativeBlockSettingsScreen {
	private static final Component RELAY_BLOCK_POSITION_OFFSET_LABEL_TEXT = Component.translatable("gui.use_relay_block.relay_block_position_offset");
	private final UseRelayBlockEntity useRelayBlock;
	private EditBox relayBlockPositionOffsetXField;
	private EditBox relayBlockPositionOffsetYField;
	private EditBox relayBlockPositionOffsetZField;

	public UseRelayBlockScreen(UseRelayBlockEntity useRelayBlock) {
		super(GameNarrator.NO_TITLE);
		this.useRelayBlock = useRelayBlock;
	}

	@Override
	protected void init() {
		this.relayBlockPositionOffsetXField = new EditBox(this.font, this.width / 2 - 154, 80, 100, 20, Component.translatable(""));
		this.relayBlockPositionOffsetXField.setMaxLength(128);
		this.relayBlockPositionOffsetXField.setValue(Integer.toString(this.useRelayBlock.getRelayBlockPositionOffset().getX()));
		this.addWidget(this.relayBlockPositionOffsetXField);
		this.relayBlockPositionOffsetYField = new EditBox(this.font, this.width / 2 - 50, 80, 100, 20, Component.translatable(""));
		this.relayBlockPositionOffsetYField.setMaxLength(128);
		this.relayBlockPositionOffsetYField.setValue(Integer.toString(this.useRelayBlock.getRelayBlockPositionOffset().getY()));
		this.addWidget(this.relayBlockPositionOffsetYField);
		this.relayBlockPositionOffsetZField = new EditBox(this.font, this.width / 2 + 54, 80, 100, 20, Component.translatable(""));
		this.relayBlockPositionOffsetZField.setMaxLength(128);
		this.relayBlockPositionOffsetZField.setValue(Integer.toString(this.useRelayBlock.getRelayBlockPositionOffset().getZ()));
		this.addWidget(this.relayBlockPositionOffsetZField);
		this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> this.done()).bounds(this.width / 2 - 4 - 150, 145, 150, 20).build());
		this.addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL, button -> this.cancel()).bounds(this.width / 2 + 4, 145, 150, 20).build());
	}

	@Override
	protected void setInitialFocus() {
		this.setInitialFocus(this.relayBlockPositionOffsetXField);
	}

	@Override
	public void resize(Minecraft client, int width, int height) {
		String string = this.relayBlockPositionOffsetXField.getValue();
		String string1 = this.relayBlockPositionOffsetYField.getValue();
		String string2 = this.relayBlockPositionOffsetZField.getValue();
		this.init(client, width, height);
		this.relayBlockPositionOffsetXField.setValue(string);
		this.relayBlockPositionOffsetYField.setValue(string1);
		this.relayBlockPositionOffsetZField.setValue(string2);
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

		context.drawString(this.font, RELAY_BLOCK_POSITION_OFFSET_LABEL_TEXT, this.width / 2 - 153, 70, 0xA0A0A0);
		this.relayBlockPositionOffsetXField.render(context, mouseX, mouseY, delta);
		this.relayBlockPositionOffsetYField.render(context, mouseX, mouseY, delta);
		this.relayBlockPositionOffsetZField.render(context, mouseX, mouseY, delta);
	}

	@Override
	protected boolean updateBlock() {
		ClientPlayNetworking.send(new UpdateUseRelayBlockPacket(
				this.useRelayBlock.getPos(),
				new BlockPos(
						ItemUtils.parseInt(this.relayBlockPositionOffsetXField.getValue()),
						ItemUtils.parseInt(this.relayBlockPositionOffsetYField.getValue()),
						ItemUtils.parseInt(this.relayBlockPositionOffsetZField.getValue())
				)
		));
		return true;
	}
}
