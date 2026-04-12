package com.github.theredbrain.scriptblocks.gui.screen.ingame;

import com.github.theredbrain.scriptblocks.block.entity.SpawnPointDelegationBlockEntity;
import com.github.theredbrain.scriptblocks.network.packet.UpdateSpawnPointDelegationBlockPacket;
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

@Environment(value = EnvType.CLIENT)
public class SpawnPointDelegationBlockScreen extends Screen {
	private static final Text DELEGATED_SPAWN_POINT_POSITION_OFFET_LABEL_TEXT = Text.translatable("gui.spawn_point_delegation_block.spawn_point_position_offset");
	private static final Text DELEGATED_SPAWN_POINT_ORIENTATION_LABEL_TEXT = Text.translatable("gui.spawn_point_delegation_block.spawn_point_orientation");
	private final SpawnPointDelegationBlockEntity spawnPointDelegationBlockEntity;
	private TextFieldWidget spawnPointPositionOffsetXField;
	private TextFieldWidget spawnPointPositionOffsetYField;
	private TextFieldWidget spawnPointPositionOffsetZField;
	private TextFieldWidget spawnPointOrientationYawField;
	private TextFieldWidget spawnPointOrientationPitchField;

	public SpawnPointDelegationBlockScreen(SpawnPointDelegationBlockEntity spawnPointDelegationBlockEntity) {
		super(NarratorManager.EMPTY);
		this.spawnPointDelegationBlockEntity = spawnPointDelegationBlockEntity;
	}

	private void done() {
		if (this.updateSpawnPointDelegationBlock()) {
			this.close();
		}
	}

	private void cancel() {
		this.close();
	}

	@Override
	protected void init() {
		this.spawnPointPositionOffsetXField = new TextFieldWidget(this.textRenderer, this.width / 2 - 154, 80, 100, 20, Text.empty());
		this.spawnPointPositionOffsetXField.setMaxLength(128);
		this.spawnPointPositionOffsetXField.setText(Integer.toString(this.spawnPointDelegationBlockEntity.getDelegatedSpawnPoint().getLeft().getX()));
		this.addSelectableChild(this.spawnPointPositionOffsetXField);

		this.spawnPointPositionOffsetYField = new TextFieldWidget(this.textRenderer, this.width / 2 - 50, 80, 100, 20, Text.empty());
		this.spawnPointPositionOffsetYField.setMaxLength(128);
		this.spawnPointPositionOffsetYField.setText(Integer.toString(this.spawnPointDelegationBlockEntity.getDelegatedSpawnPoint().getLeft().getY()));
		this.addSelectableChild(this.spawnPointPositionOffsetYField);

		this.spawnPointPositionOffsetZField = new TextFieldWidget(this.textRenderer, this.width / 2 + 54, 80, 100, 20, Text.empty());
		this.spawnPointPositionOffsetZField.setMaxLength(128);
		this.spawnPointPositionOffsetZField.setText(Integer.toString(this.spawnPointDelegationBlockEntity.getDelegatedSpawnPoint().getLeft().getZ()));
		this.addSelectableChild(this.spawnPointPositionOffsetZField);

		this.spawnPointOrientationYawField = new TextFieldWidget(this.textRenderer, this.width / 2 - 154, 115, 100, 20, Text.empty());
		this.spawnPointOrientationYawField.setMaxLength(128);
		this.spawnPointOrientationYawField.setText(Double.toString(this.spawnPointDelegationBlockEntity.getDelegatedSpawnPoint().getRight().getLeft()));
		this.addSelectableChild(this.spawnPointOrientationYawField);

		this.spawnPointOrientationPitchField = new TextFieldWidget(this.textRenderer, this.width / 2 - 50, 115, 100, 20, Text.empty());
		this.spawnPointOrientationPitchField.setMaxLength(128);
		this.spawnPointOrientationPitchField.setText(Double.toString(this.spawnPointDelegationBlockEntity.getDelegatedSpawnPoint().getRight().getRight()));
		this.addSelectableChild(this.spawnPointOrientationPitchField);

		this.addDrawableChild(ButtonWidget.builder(ScreenTexts.DONE, button -> this.done()).dimensions(this.width / 2 - 4 - 150, 210, 150, 20).build());
		this.addDrawableChild(ButtonWidget.builder(ScreenTexts.CANCEL, button -> this.cancel()).dimensions(this.width / 2 + 4, 210, 150, 20).build());
	}

	@Override
	public void resize(MinecraftClient client, int width, int height) {
		String string = this.spawnPointPositionOffsetXField.getText();
		String string1 = this.spawnPointPositionOffsetYField.getText();
		String string2 = this.spawnPointPositionOffsetZField.getText();
		String string3 = this.spawnPointOrientationYawField.getText();
		String string4 = this.spawnPointOrientationPitchField.getText();
		this.init(client, width, height);
		this.spawnPointPositionOffsetXField.setText(string);
		this.spawnPointPositionOffsetYField.setText(string1);
		this.spawnPointPositionOffsetZField.setText(string2);
		this.spawnPointOrientationYawField.setText(string3);
		this.spawnPointOrientationPitchField.setText(string4);
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {

		super.render(context, mouseX, mouseY, delta);

		context.drawTextWithShadow(this.textRenderer, DELEGATED_SPAWN_POINT_POSITION_OFFET_LABEL_TEXT, this.width / 2 - 153, 70, 0xA0A0A0);
		this.spawnPointPositionOffsetXField.render(context, mouseX, mouseY, delta);
		this.spawnPointPositionOffsetYField.render(context, mouseX, mouseY, delta);
		this.spawnPointPositionOffsetZField.render(context, mouseX, mouseY, delta);
		context.drawTextWithShadow(this.textRenderer, DELEGATED_SPAWN_POINT_ORIENTATION_LABEL_TEXT, this.width / 2 - 153, 105, 0xA0A0A0);
		this.spawnPointOrientationYawField.render(context, mouseX, mouseY, delta);
		this.spawnPointOrientationPitchField.render(context, mouseX, mouseY, delta);
	}

	@Override
	public boolean shouldPause() {
		return false;
	}

	private boolean updateSpawnPointDelegationBlock() {
		ClientPlayNetworking.send(new UpdateSpawnPointDelegationBlockPacket(
				this.spawnPointDelegationBlockEntity.getPos(),
				new BlockPos(
						ItemUtils.parseInt(this.spawnPointPositionOffsetXField.getText()),
						ItemUtils.parseInt(this.spawnPointPositionOffsetYField.getText()),
						ItemUtils.parseInt(this.spawnPointPositionOffsetZField.getText())
				),
				ItemUtils.parseDouble(this.spawnPointOrientationYawField.getText()),
				ItemUtils.parseDouble(this.spawnPointOrientationPitchField.getText())
		));
		return true;
	}
}
