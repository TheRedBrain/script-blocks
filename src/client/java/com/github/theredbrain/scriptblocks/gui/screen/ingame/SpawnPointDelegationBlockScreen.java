package com.github.theredbrain.scriptblocks.gui.screen.ingame;

import com.github.theredbrain.scriptblocks.ScriptBlocks;
import com.github.theredbrain.scriptblocks.block.entity.SpawnPointDelegationBlockEntity;
import com.github.theredbrain.scriptblocks.network.packet.UpdateSpawnPointDelegationBlockPacket;
import com.github.theredbrain.scriptblocks.util.ItemUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ButtonTextures;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.TexturedButtonWidget;
import net.minecraft.client.util.NarratorManager;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import org.apache.commons.lang3.tuple.MutablePair;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

@Environment(value = EnvType.CLIENT)
public class SpawnPointDelegationBlockScreen extends Screen {
	private static final Text ADD_NEW_DELEGATED_SPAWN_POINT_BUTTON_LABEL_TEXT = Text.translatable("gui.list_entry.add");
	private static final Text NEW_DELEGATED_SPAWN_POINT_POSITION_OFFET_LABEL_TEXT = Text.translatable("gui.spawn_point_delegation_block.spawn_point_position_offset");
	private static final Text NEW_DELEGATED_SPAWN_POINT_ORIENTATION_LABEL_TEXT = Text.translatable("gui.spawn_point_delegation_block.spawn_point_orientation");
	private static final Identifier SCROLL_BAR_BACKGROUND_8_92_TEXTURE = ScriptBlocks.identifier("scroll_bar/scroll_bar_background_8_92");
	private static final Identifier SCROLLER_TEXTURE = ScriptBlocks.identifier("scroll_bar/scroller_vertical_6_7");
	public static final ButtonTextures REMOVE_ENTRY_BUTTON_TEXTURES = new ButtonTextures(
			Identifier.of(ScriptBlocks.MOD_ID, "widgets/remove_entry_button"), Identifier.of(ScriptBlocks.MOD_ID, "widgets/remove_entry_button_highlighted")
	);
	private static final int VISIBLE_LIST_ELEMENTS = 4;
	private final SpawnPointDelegationBlockEntity spawnPointDelegationBlockEntity;
	private ButtonWidget removeListEntryButton0;
	private ButtonWidget removeListEntryButton1;
	private ButtonWidget removeListEntryButton2;
	private ButtonWidget removeListEntryButton3;
	private TextFieldWidget newDelegatedSpawnPointPositionOffsetXField;
	private TextFieldWidget newDelegatedSpawnPointPositionOffsetYField;
	private TextFieldWidget newDelegatedSpawnPointPositionOffsetZField;
	private TextFieldWidget newDelegatedSpawnPointOrientationYawField;
	private TextFieldWidget newDelegatedSpawnPointOrientationPitchField;
	private final List<MutablePair<BlockPos, MutablePair<Double, Double>>> delegatedSpawnPoints = new ArrayList<>(List.of());
	private int scrollPosition = 0;
	private float scrollAmount = 0.0f;
	private boolean mouseClicked = false;

	public SpawnPointDelegationBlockScreen(SpawnPointDelegationBlockEntity spawnPointDelegationBlockEntity) {
		super(NarratorManager.EMPTY);
		this.spawnPointDelegationBlockEntity = spawnPointDelegationBlockEntity;
	}

	private void addNewDelegatedSpawnPoint() {
		boolean bl = false;
		int x = ItemUtils.parseInt(this.newDelegatedSpawnPointPositionOffsetXField.getText());
		if (x < -48 || x > 48) {
			x = Math.max(-48, Math.min(x, 48));
			this.newDelegatedSpawnPointPositionOffsetXField.setText(Integer.toString(x));
			bl = true;
		}
		int y = ItemUtils.parseInt(this.newDelegatedSpawnPointPositionOffsetYField.getText());
		if (y < -48 || y > 48) {
			y = Math.max(-48, Math.min(y, 48));
			this.newDelegatedSpawnPointPositionOffsetYField.setText(Integer.toString(y));
			bl = true;
		}
		int z = ItemUtils.parseInt(this.newDelegatedSpawnPointPositionOffsetZField.getText());
		if (z < -48 || z > 48) {
			z = Math.max(-48, Math.min(z, 48));
			this.newDelegatedSpawnPointPositionOffsetZField.setText(Integer.toString(z));
			bl = true;
		}
		int yaw = ItemUtils.parseInt(this.newDelegatedSpawnPointOrientationYawField.getText());
		if (yaw < -180 || yaw > 180) {
			yaw = Math.max(-180, Math.min(yaw, 180));
			this.newDelegatedSpawnPointOrientationYawField.setText(Integer.toString(yaw));
			bl = true;
		}
		int pitch = ItemUtils.parseInt(this.newDelegatedSpawnPointOrientationPitchField.getText());
		if (pitch < -90 || pitch > 90) {
			pitch = Math.max(-90, Math.min(pitch, 90));
			this.newDelegatedSpawnPointOrientationPitchField.setText(Integer.toString(pitch));
			bl = true;
		}
		if (bl && this.client != null && this.client.player != null) {
			this.client.player.sendMessage(Text.translatable("gui.spawn_point_delegation_block.invalid_entry_was_corrected"));
			return;
		}
		MutablePair<BlockPos, MutablePair<Double, Double>> newDelegatedSpawnPoint = new MutablePair<>(
				new BlockPos(
						ItemUtils.parseInt(this.newDelegatedSpawnPointPositionOffsetXField.getText()),
						ItemUtils.parseInt(this.newDelegatedSpawnPointPositionOffsetYField.getText()),
						ItemUtils.parseInt(this.newDelegatedSpawnPointPositionOffsetZField.getText())
				),
				new MutablePair<>(
						ItemUtils.parseDouble(this.newDelegatedSpawnPointOrientationYawField.getText()),
						ItemUtils.parseDouble(this.newDelegatedSpawnPointOrientationPitchField.getText())
				)
		);
		bl = false;
		for (MutablePair<BlockPos, MutablePair<Double, Double>> delegatedSpawnPoint : this.delegatedSpawnPoints) {
			if (delegatedSpawnPoint.equals(newDelegatedSpawnPoint)) {
				bl = true;
				break;
			}
		}
		if (bl && this.client != null && this.client.player != null) {
			this.client.player.sendMessage(Text.translatable("gui.spawn_point_delegation_block.spawn_point_already_in_list"));
		} else {
			this.delegatedSpawnPoints.add(newDelegatedSpawnPoint);
			this.scrollPosition = 0;
			this.scrollAmount = 0.0f;
			this.updateWidgets();
		}
	}

	private void removeDelegatedSpawnPoint(int index) {
		if (index + this.scrollPosition < this.delegatedSpawnPoints.size()) {
			this.delegatedSpawnPoints.remove(index + this.scrollPosition);
		}
		this.scrollPosition = 0;
		this.scrollAmount = 0.0f;
		this.updateWidgets();
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

		this.delegatedSpawnPoints.clear();
		this.delegatedSpawnPoints.addAll(this.spawnPointDelegationBlockEntity.getDelegatedSpawnPoints());

		this.removeListEntryButton0 = this.addDrawableChild(new TexturedButtonWidget(this.width / 2 - 141, 20, 20, 20, REMOVE_ENTRY_BUTTON_TEXTURES, button -> this.removeDelegatedSpawnPoint(0)));
		this.removeListEntryButton1 = this.addDrawableChild(new TexturedButtonWidget(this.width / 2 - 141, 44, 20, 20, REMOVE_ENTRY_BUTTON_TEXTURES, button -> this.removeDelegatedSpawnPoint(1)));
		this.removeListEntryButton2 = this.addDrawableChild(new TexturedButtonWidget(this.width / 2 - 141, 68, 20, 20, REMOVE_ENTRY_BUTTON_TEXTURES, button -> this.removeDelegatedSpawnPoint(2)));
		this.removeListEntryButton3 = this.addDrawableChild(new TexturedButtonWidget(this.width / 2 - 141, 92, 20, 20, REMOVE_ENTRY_BUTTON_TEXTURES, button -> this.removeDelegatedSpawnPoint(3)));

		this.addDrawableChild(ButtonWidget.builder(ADD_NEW_DELEGATED_SPAWN_POINT_BUTTON_LABEL_TEXT, button -> this.addNewDelegatedSpawnPoint()).dimensions(this.width / 2 - 154, 116, 300, 20).build());

		this.newDelegatedSpawnPointPositionOffsetXField = new TextFieldWidget(this.textRenderer, this.width / 2 - 154, 151, 100, 20, Text.empty());
		this.newDelegatedSpawnPointPositionOffsetXField.setMaxLength(128);
		this.addSelectableChild(this.newDelegatedSpawnPointPositionOffsetXField);

		this.newDelegatedSpawnPointPositionOffsetYField = new TextFieldWidget(this.textRenderer, this.width / 2 - 50, 151, 100, 20, Text.empty());
		this.newDelegatedSpawnPointPositionOffsetYField.setMaxLength(128);
		this.addSelectableChild(this.newDelegatedSpawnPointPositionOffsetYField);

		this.newDelegatedSpawnPointPositionOffsetZField = new TextFieldWidget(this.textRenderer, this.width / 2 + 54, 151, 100, 20, Text.empty());
		this.newDelegatedSpawnPointPositionOffsetZField.setMaxLength(128);
		this.addSelectableChild(this.newDelegatedSpawnPointPositionOffsetZField);

		this.newDelegatedSpawnPointOrientationYawField = new TextFieldWidget(this.textRenderer, this.width / 2 - 154, 186, 100, 20, Text.empty());
		this.newDelegatedSpawnPointOrientationYawField.setMaxLength(128);
		this.addSelectableChild(this.newDelegatedSpawnPointOrientationYawField);

		this.newDelegatedSpawnPointOrientationPitchField = new TextFieldWidget(this.textRenderer, this.width / 2 - 50, 186, 100, 20, Text.empty());
		this.newDelegatedSpawnPointOrientationPitchField.setMaxLength(128);
		this.addSelectableChild(this.newDelegatedSpawnPointOrientationPitchField);

		this.addDrawableChild(ButtonWidget.builder(ScreenTexts.DONE, button -> this.done()).dimensions(this.width / 2 - 4 - 150, 210, 150, 20).build());
		this.addDrawableChild(ButtonWidget.builder(ScreenTexts.CANCEL, button -> this.cancel()).dimensions(this.width / 2 + 4, 210, 150, 20).build());

		this.updateWidgets();
	}

	@Override
	protected void setInitialFocus() {
		this.setInitialFocus(this.newDelegatedSpawnPointPositionOffsetXField);
	}

	@Override
	public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
		this.renderInGameBackground(context);
	}

	private void updateWidgets() {

		this.removeListEntryButton0.visible = false;
		this.removeListEntryButton1.visible = false;
		this.removeListEntryButton2.visible = false;
		this.removeListEntryButton3.visible = false;

		int index = 0;
		for (int i = 0; i < Math.min(VISIBLE_LIST_ELEMENTS, this.delegatedSpawnPoints.size()); i++) {
			if (index == 0) {
				this.removeListEntryButton0.visible = true;
			} else if (index == 1) {
				this.removeListEntryButton1.visible = true;
			} else if (index == 2) {
				this.removeListEntryButton2.visible = true;
			} else if (index == 3) {
				this.removeListEntryButton3.visible = true;
			}
			index++;
		}

	}

	@Override
	public void resize(MinecraftClient client, int width, int height) {
		List<MutablePair<BlockPos, MutablePair<Double, Double>>> list = new ArrayList<>(this.delegatedSpawnPoints);
		String string = this.newDelegatedSpawnPointPositionOffsetXField.getText();
		String string1 = this.newDelegatedSpawnPointPositionOffsetYField.getText();
		String string2 = this.newDelegatedSpawnPointPositionOffsetZField.getText();
		String string3 = this.newDelegatedSpawnPointOrientationYawField.getText();
		String string4 = this.newDelegatedSpawnPointOrientationPitchField.getText();
		this.init(client, width, height);
		this.delegatedSpawnPoints.clear();
		this.delegatedSpawnPoints.addAll(list);
		this.newDelegatedSpawnPointPositionOffsetXField.setText(string);
		this.newDelegatedSpawnPointPositionOffsetYField.setText(string1);
		this.newDelegatedSpawnPointPositionOffsetZField.setText(string2);
		this.newDelegatedSpawnPointOrientationYawField.setText(string3);
		this.newDelegatedSpawnPointOrientationPitchField.setText(string4);
		this.updateWidgets();
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		this.mouseClicked = false;
		if (this.delegatedSpawnPoints.size() > VISIBLE_LIST_ELEMENTS) {
			int i = this.width / 2 - 152;
			int j = 20;
			if (mouseX >= (double) i && mouseX < (double) (i + 6) && mouseY >= (double) j && mouseY < (double) (j + 92)) {
				this.mouseClicked = true;
			}
		}
		return super.mouseClicked(mouseX, mouseY, button);
	}

	@Override
	public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
		if (this.delegatedSpawnPoints.size() > VISIBLE_LIST_ELEMENTS
				&& this.mouseClicked) {
			int i = this.delegatedSpawnPoints.size() - VISIBLE_LIST_ELEMENTS;
			float f = (float) deltaY / (float) i;
			this.scrollAmount = MathHelper.clamp(this.scrollAmount + f, 0.0f, 1.0f);
			this.scrollPosition = (int) ((double) (this.scrollAmount * (float) i));
		}
		return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
		if (this.delegatedSpawnPoints.size() > VISIBLE_LIST_ELEMENTS
				&& mouseX >= (double) (this.width / 2 - 152) && mouseX <= (double) (this.width / 2 + 154)
				&& mouseY >= 20 && mouseY <= 112) {
			int i = this.delegatedSpawnPoints.size() - VISIBLE_LIST_ELEMENTS;
			float f = (float) verticalAmount / (float) i;
			this.scrollAmount = MathHelper.clamp(this.scrollAmount - f, 0.0f, 1.0f);
			this.scrollPosition = (int) ((double) (this.scrollAmount * (float) i));
		}
		return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
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

		for (int i = this.scrollPosition; i < Math.min(this.scrollPosition + VISIBLE_LIST_ELEMENTS, this.delegatedSpawnPoints.size()); i++) {
			MutablePair<BlockPos, MutablePair<Double, Double>> delegatedSpawnPoint = this.delegatedSpawnPoints.get(i);

			context.drawTextWithShadow(this.textRenderer,
					Text.translatable("gui.spawn_point_delegation_block.list.entry.1", delegatedSpawnPoint.left.getX(), delegatedSpawnPoint.left.getY(), delegatedSpawnPoint.left.getZ()),
					this.width / 2 - 117, 21 + ((i - this.scrollPosition) * 24), 0xA0A0A0);
			context.drawTextWithShadow(this.textRenderer,
					Text.translatable("gui.spawn_point_delegation_block.list.entry.2", delegatedSpawnPoint.right.getLeft(), delegatedSpawnPoint.right.getRight()),
					this.width / 2 - 117, 31 + ((i - this.scrollPosition) * 24), 0xA0A0A0);
		}
		if (this.delegatedSpawnPoints.size() > VISIBLE_LIST_ELEMENTS) {
			context.drawGuiTexture(SCROLL_BAR_BACKGROUND_8_92_TEXTURE, this.width / 2 - 153, 20, 8, 92);
			int k = (int) (83.0f * this.scrollAmount);
			context.drawGuiTexture(SCROLLER_TEXTURE, this.width / 2 - 152, 20 + 1 + k, 6, 7);
		}
		context.drawTextWithShadow(this.textRenderer, NEW_DELEGATED_SPAWN_POINT_POSITION_OFFET_LABEL_TEXT, this.width / 2 - 153, 141, 0xA0A0A0);
		this.newDelegatedSpawnPointPositionOffsetXField.render(context, mouseX, mouseY, delta);
		this.newDelegatedSpawnPointPositionOffsetYField.render(context, mouseX, mouseY, delta);
		this.newDelegatedSpawnPointPositionOffsetZField.render(context, mouseX, mouseY, delta);
		context.drawTextWithShadow(this.textRenderer, NEW_DELEGATED_SPAWN_POINT_ORIENTATION_LABEL_TEXT, this.width / 2 - 153, 176, 0xA0A0A0);
		this.newDelegatedSpawnPointOrientationYawField.render(context, mouseX, mouseY, delta);
		this.newDelegatedSpawnPointOrientationPitchField.render(context, mouseX, mouseY, delta);
	}

	@Override
	public boolean shouldPause() {
		return false;
	}

	private boolean updateSpawnPointDelegationBlock() {
		ClientPlayNetworking.send(new UpdateSpawnPointDelegationBlockPacket(
				this.spawnPointDelegationBlockEntity.getPos(),
				this.delegatedSpawnPoints
		));
		return true;
	}
}
