package com.github.theredbrain.scriptblocks.gui.screen.ingame;

import com.github.theredbrain.scriptblocks.block.entity.MimicBlockEntity;
import com.github.theredbrain.scriptblocks.network.packet.UpdateMimicBlockPacket;
import com.github.theredbrain.scriptblocks.util.ItemUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.GameNarrator;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

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
		this.activeMimicBlockPositionOffsetXField = new EditBox(this.font, this.width / 2 - 154, 80, 100, 20, Component.empty());
		this.activeMimicBlockPositionOffsetXField.setValue(Integer.toString(this.mimicBlock.getActiveMimicBlockPositionOffset().getX()));
		this.addWidget(this.activeMimicBlockPositionOffsetXField);
		this.activeMimicBlockPositionOffsetYField = new EditBox(this.font, this.width / 2 - 50, 80, 100, 20, Component.empty());
		this.activeMimicBlockPositionOffsetYField.setValue(Integer.toString(this.mimicBlock.getActiveMimicBlockPositionOffset().getY()));
		this.addWidget(this.activeMimicBlockPositionOffsetYField);
		this.activeMimicBlockPositionOffsetZField = new EditBox(this.font, this.width / 2 + 54, 80, 100, 20, Component.empty());
		this.activeMimicBlockPositionOffsetZField.setValue(Integer.toString(this.mimicBlock.getActiveMimicBlockPositionOffset().getZ()));
		this.addWidget(this.activeMimicBlockPositionOffsetZField);

		this.inactiveMimicBlockPositionOffsetXField = new EditBox(this.font, this.width / 2 - 154, 115, 100, 20, Component.empty());
		this.inactiveMimicBlockPositionOffsetXField.setValue(Integer.toString(this.mimicBlock.getInactiveMimicBlockPositionOffset().getX()));
		this.addWidget(this.inactiveMimicBlockPositionOffsetXField);
		this.inactiveMimicBlockPositionOffsetYField = new EditBox(this.font, this.width / 2 - 50, 115, 100, 20, Component.empty());
		this.inactiveMimicBlockPositionOffsetYField.setValue(Integer.toString(this.mimicBlock.getInactiveMimicBlockPositionOffset().getY()));
		this.addWidget(this.inactiveMimicBlockPositionOffsetYField);
		this.inactiveMimicBlockPositionOffsetZField = new EditBox(this.font, this.width / 2 + 54, 115, 100, 20, Component.empty());
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
	public void resize(final int width, final int height) {
		String string = this.activeMimicBlockPositionOffsetXField.getValue();
		String string1 = this.activeMimicBlockPositionOffsetYField.getValue();
		String string2 = this.activeMimicBlockPositionOffsetZField.getValue();
		String string3 = this.inactiveMimicBlockPositionOffsetXField.getValue();
		String string4 = this.inactiveMimicBlockPositionOffsetYField.getValue();
		String string5 = this.inactiveMimicBlockPositionOffsetZField.getValue();
		this.init(width, height);
		this.activeMimicBlockPositionOffsetXField.setValue(string);
		this.activeMimicBlockPositionOffsetYField.setValue(string1);
		this.activeMimicBlockPositionOffsetZField.setValue(string2);
		this.inactiveMimicBlockPositionOffsetXField.setValue(string3);
		this.inactiveMimicBlockPositionOffsetYField.setValue(string4);
		this.inactiveMimicBlockPositionOffsetZField.setValue(string5);
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

		graphics.text(this.font, ACTIVE_MIMIC_BLOCK_POSITION_LABEL_TEXT, this.width / 2 - 153, 71, 0xA0A0A0);
		this.activeMimicBlockPositionOffsetXField.extractRenderState(graphics, mouseX, mouseY, a);
		this.activeMimicBlockPositionOffsetYField.extractRenderState(graphics, mouseX, mouseY, a);
		this.activeMimicBlockPositionOffsetZField.extractRenderState(graphics, mouseX, mouseY, a);

		graphics.text(this.font, INACTIVE_MIMIC_BLOCK_POSITION_LABEL_TEXT, this.width / 2 - 153, 104, 0xA0A0A0);
		this.inactiveMimicBlockPositionOffsetXField.extractRenderState(graphics, mouseX, mouseY, a);
		this.inactiveMimicBlockPositionOffsetYField.extractRenderState(graphics, mouseX, mouseY, a);
		this.inactiveMimicBlockPositionOffsetZField.extractRenderState(graphics, mouseX, mouseY, a);
	}

	@Override
	protected boolean updateBlock() {
		ClientPlayNetworking.send(new UpdateMimicBlockPacket(
				this.mimicBlock.getBlockPos(),
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
