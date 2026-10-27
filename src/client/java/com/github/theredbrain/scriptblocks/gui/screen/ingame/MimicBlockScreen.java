package com.github.theredbrain.scriptblocks.gui.screen.ingame;

import com.github.theredbrain.scriptblocks.block.entity.MimicBlockEntity;
import com.github.theredbrain.scriptblocks.network.packet.UpdateMimicBlockPacket;
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
public class MimicBlockScreen extends AbstractCreativeBlockSettingsScreen {
	private static final Component ACTIVE_MIMIC_BLOCK_POSITION_LABEL_TEXT = Component.translatable("gui.mimic_block.active_mimic_block_position_offset");
	private static final Component INACTIVE_MIMIC_BLOCK_POSITION_LABEL_TEXT = Component.translatable("gui.mimic_block.inactive_mimic_block_position_offset");
	private final MimicBlockEntity mimicBlock;
	private EditBox activeMimicBlockPositionOffsetXField;
	private EditBox activeMimicBlockPositionOffsetYField;
	private EditBox activeMimicBlockPositionOffsetZField;
	private EditBox inactiveMimicBlockPositionOffsetXField;
	private EditBox inactiveMimicBlockPositionOffsetYField;
	private EditBox inactiveMimicBlockPositionOffsetZField;

	public MimicBlockScreen(MimicBlockEntity mimicBlock) {
		super(GameNarrator.NO_TITLE);
		this.mimicBlock = mimicBlock;
	}

	@Override
	protected void init() {
		this.activeMimicBlockPositionOffsetXField = new EditBox(this.font, this.width / 2 - 154, 80, 100, 20, Component.translatable(""));
		this.activeMimicBlockPositionOffsetXField.setValue(Integer.toString(this.mimicBlock.getActiveMimicBlockPositionOffset().getX()));
		this.addWidget(this.activeMimicBlockPositionOffsetXField);
		this.activeMimicBlockPositionOffsetYField = new EditBox(this.font, this.width / 2 - 50, 80, 100, 20, Component.translatable(""));
		this.activeMimicBlockPositionOffsetYField.setValue(Integer.toString(this.mimicBlock.getActiveMimicBlockPositionOffset().getY()));
		this.addWidget(this.activeMimicBlockPositionOffsetYField);
		this.activeMimicBlockPositionOffsetZField = new EditBox(this.font, this.width / 2 + 54, 80, 100, 20, Component.translatable(""));
		this.activeMimicBlockPositionOffsetZField.setValue(Integer.toString(this.mimicBlock.getActiveMimicBlockPositionOffset().getZ()));
		this.addWidget(this.activeMimicBlockPositionOffsetZField);

		this.inactiveMimicBlockPositionOffsetXField = new EditBox(this.font, this.width / 2 - 154, 115, 100, 20, Component.translatable(""));
		this.inactiveMimicBlockPositionOffsetXField.setValue(Integer.toString(this.mimicBlock.getInactiveMimicBlockPositionOffset().getX()));
		this.addWidget(this.inactiveMimicBlockPositionOffsetXField);
		this.inactiveMimicBlockPositionOffsetYField = new EditBox(this.font, this.width / 2 - 50, 115, 100, 20, Component.translatable(""));
		this.inactiveMimicBlockPositionOffsetYField.setValue(Integer.toString(this.mimicBlock.getInactiveMimicBlockPositionOffset().getY()));
		this.addWidget(this.inactiveMimicBlockPositionOffsetYField);
		this.inactiveMimicBlockPositionOffsetZField = new EditBox(this.font, this.width / 2 + 54, 115, 100, 20, Component.translatable(""));
		this.inactiveMimicBlockPositionOffsetZField.setValue(Integer.toString(this.mimicBlock.getInactiveMimicBlockPositionOffset().getZ()));
		this.addWidget(this.inactiveMimicBlockPositionOffsetZField);

		this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> this.done()).bounds(this.width / 2 - 4 - 150, 139, 150, 20).build());
		this.addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL, button -> this.cancel()).bounds(this.width / 2 + 4, 139, 150, 20).build());
	}

	@Override
	protected void setInitialFocus() {
		this.setInitialFocus(this.activeMimicBlockPositionOffsetXField);
	}

	@Override
	public void resize(Minecraft client, int width, int height) {
		String string = this.activeMimicBlockPositionOffsetXField.getValue();
		String string1 = this.activeMimicBlockPositionOffsetYField.getValue();
		String string2 = this.activeMimicBlockPositionOffsetZField.getValue();
		String string3 = this.inactiveMimicBlockPositionOffsetXField.getValue();
		String string4 = this.inactiveMimicBlockPositionOffsetYField.getValue();
		String string5 = this.inactiveMimicBlockPositionOffsetZField.getValue();
		this.init(client, width, height);
		this.activeMimicBlockPositionOffsetXField.setValue(string);
		this.activeMimicBlockPositionOffsetYField.setValue(string1);
		this.activeMimicBlockPositionOffsetZField.setValue(string2);
		this.inactiveMimicBlockPositionOffsetXField.setValue(string3);
		this.inactiveMimicBlockPositionOffsetYField.setValue(string4);
		this.inactiveMimicBlockPositionOffsetZField.setValue(string5);
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

		context.drawString(this.font, ACTIVE_MIMIC_BLOCK_POSITION_LABEL_TEXT, this.width / 2 - 153, 71, 0xA0A0A0);
		this.activeMimicBlockPositionOffsetXField.render(context, mouseX, mouseY, delta);
		this.activeMimicBlockPositionOffsetYField.render(context, mouseX, mouseY, delta);
		this.activeMimicBlockPositionOffsetZField.render(context, mouseX, mouseY, delta);

		context.drawString(this.font, INACTIVE_MIMIC_BLOCK_POSITION_LABEL_TEXT, this.width / 2 - 153, 104, 0xA0A0A0);
		this.inactiveMimicBlockPositionOffsetXField.render(context, mouseX, mouseY, delta);
		this.inactiveMimicBlockPositionOffsetYField.render(context, mouseX, mouseY, delta);
		this.inactiveMimicBlockPositionOffsetZField.render(context, mouseX, mouseY, delta);
	}

	@Override
	protected boolean updateBlock() {
		ClientPlayNetworking.send(new UpdateMimicBlockPacket(
				this.mimicBlock.getPos(),
				new BlockPos(
						ItemUtils.parseInt(this.activeMimicBlockPositionOffsetXField.getValue()),
						ItemUtils.parseInt(this.activeMimicBlockPositionOffsetYField.getValue()),
						ItemUtils.parseInt(this.activeMimicBlockPositionOffsetZField.getValue())
				),
				new BlockPos(
						ItemUtils.parseInt(this.inactiveMimicBlockPositionOffsetXField.getValue()),
						ItemUtils.parseInt(this.inactiveMimicBlockPositionOffsetYField.getValue()),
						ItemUtils.parseInt(this.inactiveMimicBlockPositionOffsetZField.getValue())
				)
		));
		return true;
	}
}
