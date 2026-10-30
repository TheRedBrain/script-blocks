package com.github.theredbrain.scriptblocks.gui.screen.ingame;

import com.github.theredbrain.scriptblocks.block.entity.EntranceDelegationBlockEntity;
import com.github.theredbrain.scriptblocks.network.packet.UpdateEntranceDelegationBlockPacket;
import com.github.theredbrain.scriptblocks.util.ItemUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.GameNarrator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.storage.LevelData;

@Deprecated
@Environment(value = EnvType.CLIENT)
public class EntranceDelegationBlockScreen extends Screen {
	private static final Component DELEGATED_ENTRANCE_POSITION_OFFET_LABEL_TEXT = Component.translatable("gui.entrance_delegation_block.entrance_position_offset");
	private static final Component DELEGATED_ENTRANCE_ORIENTATION_LABEL_TEXT = Component.translatable("gui.entrance_delegation_block.entrance_orientation");
	private final EntranceDelegationBlockEntity entranceDelegationBlock;
	private EditBox mainEntrancePositionOffsetXField;
	private EditBox mainEntrancePositionOffsetYField;
	private EditBox mainEntrancePositionOffsetZField;
	private EditBox mainEntranceOrientationYawField;
	private EditBox mainEntranceOrientationPitchField;

	public EntranceDelegationBlockScreen(EntranceDelegationBlockEntity entranceDelegationBlock) {
		super(GameNarrator.NO_TITLE);
		this.entranceDelegationBlock = entranceDelegationBlock;
	}

	private void done() {
		if (this.updateEntranceDelegationBlock()) {
			this.onClose();
		}
	}

	private void cancel() {
		this.onClose();
	}

	@Override
	protected void init() {
		this.mainEntrancePositionOffsetXField = new EditBox(this.font, this.width / 2 - 154, 80, 100, 20, Component.empty());
		this.mainEntrancePositionOffsetXField.setMaxLength(128);
		this.mainEntrancePositionOffsetXField.setValue(Integer.toString(this.entranceDelegationBlock.getDelegatedEntrance().getLeft().getX()));
		this.addWidget(this.mainEntrancePositionOffsetXField);

		this.mainEntrancePositionOffsetYField = new EditBox(this.font, this.width / 2 - 50, 80, 100, 20, Component.empty());
		this.mainEntrancePositionOffsetYField.setMaxLength(128);
		this.mainEntrancePositionOffsetYField.setValue(Integer.toString(this.entranceDelegationBlock.getDelegatedEntrance().getLeft().getY()));
		this.addWidget(this.mainEntrancePositionOffsetYField);

		this.mainEntrancePositionOffsetZField = new EditBox(this.font, this.width / 2 + 54, 80, 100, 20, Component.empty());
		this.mainEntrancePositionOffsetZField.setMaxLength(128);
		this.mainEntrancePositionOffsetZField.setValue(Integer.toString(this.entranceDelegationBlock.getDelegatedEntrance().getLeft().getZ()));
		this.addWidget(this.mainEntrancePositionOffsetZField);

		this.mainEntranceOrientationYawField = new EditBox(this.font, this.width / 2 - 154, 115, 100, 20, Component.empty());
		this.mainEntranceOrientationYawField.setMaxLength(128);
		this.mainEntranceOrientationYawField.setValue(Double.toString(this.entranceDelegationBlock.getDelegatedEntrance().getRight().getLeft()));
		this.addWidget(this.mainEntranceOrientationYawField);

		this.mainEntranceOrientationPitchField = new EditBox(this.font, this.width / 2 - 50, 115, 100, 20, Component.empty());
		this.mainEntranceOrientationPitchField.setMaxLength(128);
		this.mainEntranceOrientationPitchField.setValue(Double.toString(this.entranceDelegationBlock.getDelegatedEntrance().getRight().getRight()));
		this.addWidget(this.mainEntranceOrientationPitchField);

		this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> this.done()).bounds(this.width / 2 - 4 - 150, 210, 150, 20).build());
		this.addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL, button -> this.cancel()).bounds(this.width / 2 + 4, 210, 150, 20).build());
	}

	@Override
	public void resize(Minecraft client, int width, int height) {
		String string = this.mainEntrancePositionOffsetXField.getValue();
		String string1 = this.mainEntrancePositionOffsetYField.getValue();
		String string2 = this.mainEntrancePositionOffsetZField.getValue();
		String string3 = this.mainEntranceOrientationYawField.getValue();
		String string4 = this.mainEntranceOrientationPitchField.getValue();
		this.init(client, width, height);
		this.mainEntrancePositionOffsetXField.setValue(string);
		this.mainEntrancePositionOffsetYField.setValue(string1);
		this.mainEntrancePositionOffsetZField.setValue(string2);
		this.mainEntranceOrientationYawField.setValue(string3);
		this.mainEntranceOrientationPitchField.setValue(string4);
	}

	@Override
	public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {

		super.render(context, mouseX, mouseY, delta);

		context.drawString(this.font, DELEGATED_ENTRANCE_POSITION_OFFET_LABEL_TEXT, this.width / 2 - 153, 70, 0xA0A0A0);
		this.mainEntrancePositionOffsetXField.render(context, mouseX, mouseY, delta);
		this.mainEntrancePositionOffsetYField.render(context, mouseX, mouseY, delta);
		this.mainEntrancePositionOffsetZField.render(context, mouseX, mouseY, delta);
		context.drawString(this.font, DELEGATED_ENTRANCE_ORIENTATION_LABEL_TEXT, this.width / 2 - 153, 105, 0xA0A0A0);
		this.mainEntranceOrientationYawField.render(context, mouseX, mouseY, delta);
		this.mainEntranceOrientationPitchField.render(context, mouseX, mouseY, delta);
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	private boolean updateEntranceDelegationBlock() {
		if (this.entranceDelegationBlock.getLevel() == null) {
			return false;
		}
		ClientPlayNetworking.send(new UpdateEntranceDelegationBlockPacket(
				this.entranceDelegationBlock.getBlockPos(),
				LevelData.RespawnData.of(
						this.entranceDelegationBlock.getLevel().dimension(),
						new BlockPos(
								ItemUtils.parseInt(this.mainEntrancePositionOffsetXField.getValue()),
								ItemUtils.parseInt(this.mainEntrancePositionOffsetYField.getValue()),
								ItemUtils.parseInt(this.mainEntrancePositionOffsetZField.getValue())
						),
						ItemUtils.parseFloat(this.mainEntranceOrientationYawField.getValue()),
						ItemUtils.parseFloat(this.mainEntranceOrientationPitchField.getValue())
				)
		));
		return true;
	}
}
