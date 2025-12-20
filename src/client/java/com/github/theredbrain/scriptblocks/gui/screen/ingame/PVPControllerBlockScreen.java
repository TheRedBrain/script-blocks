package com.github.theredbrain.scriptblocks.gui.screen.ingame;

import com.github.theredbrain.scriptblocks.ScriptBlocks;
import com.github.theredbrain.scriptblocks.block.entity.PVPControllerBlockEntity;
import com.github.theredbrain.scriptblocks.network.packet.UpdatePVPControllerBlockPacket;
import com.github.theredbrain.scriptblocks.util.ItemUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.CyclingButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.NarratorManager;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import org.apache.commons.lang3.tuple.MutablePair;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Environment(value = EnvType.CLIENT)
public class PVPControllerBlockScreen extends Screen {
	private static final Text PVP_ARENA_SETTINGS_ID_LABEL_TEXT = Text.translatable("gui.pvp_controller_block.pvp_arena_settings_id_label");
	private static final Text TRIGGERED_BLOCK_POSITION_TEXT = Text.translatable("gui.pvp_controller_block.match_end_triggered_block_position_offset");
	private static final Text DATA_PROVIDING_BLOCK_POSITION_TEXT = Text.translatable("gui.pvp_controller_block.data_providing_block_position_offset");
	private static final Text MATCH_DURATION_DATA_IDENTIFIER_TEXT = Text.translatable("gui.pvp_controller_block.match_duration_data_identifier");
	private static final Text REMOVE_LIST_ENTRY_BUTTON_LABEL_TEXT = Text.translatable("gui.pvp_controller_block.remove_list_entry_button_label");
	private static final Text NEW_RESPAWN_POSITION_OFFSET_LABEL_TEXT = Text.translatable("gui.pvp_controller_block.new_respawn_position_offset_label");
	private static final Text NEW_RESPAWN_POSITION_NAME_LABEL_TEXT = Text.translatable("gui.pvp_controller_block.new_respawn_position_name_label");
	private static final Text NEW_RESPAWN_ORIENTATION_LABEL_TEXT = Text.translatable("gui.pvp_controller_block.new_respawn_orientation_label");
	private static final Identifier SCROLL_BAR_BACKGROUND_8_70_TEXTURE = ScriptBlocks.identifier("scroll_bar/scroll_bar_background_8_70");
	private static final Identifier SCROLLER_TEXTURE = ScriptBlocks.identifier("scroll_bar/scroller_vertical_6_7");
	private final PVPControllerBlockEntity pvpControllerBlockEntity;
	private CyclingButtonWidget<ScreenPage> creativeScreenPageButton;
	private TextFieldWidget pvpArenaSettingsIdentifierField;
	private TextFieldWidget triggeredBlockPositionOffsetXField;
	private TextFieldWidget triggeredBlockPositionOffsetYField;
	private TextFieldWidget triggeredBlockPositionOffsetZField;
	private CyclingButtonWidget<Boolean> toggleTriggeredBlockResetsButton;
	private boolean triggeredBlockResets;
	private TextFieldWidget dataProvidingBlockPositionOffsetXField;
	private TextFieldWidget dataProvidingBlockPositionOffsetYField;
	private TextFieldWidget dataProvidingBlockPositionOffsetZField;
	private TextFieldWidget matchDurationDataIdentifierField;

	private ButtonWidget removeRespawnPositionButton0;
	private ButtonWidget removeRespawnPositionButton1;
	private ButtonWidget removeRespawnPositionButton2;
	private TextFieldWidget newRespawnPositionOffsetXField;
	private TextFieldWidget newRespawnPositionOffsetYField;
	private TextFieldWidget newRespawnPositionOffsetZField;
	private TextFieldWidget newRespawnOrientationYawField;
	private TextFieldWidget newRespawnOrientationPitchField;
	private TextFieldWidget newRespawnPositionNameField;
	private ButtonWidget addNewRespawnPositionButton;
	private ButtonWidget saveButton;
	private ButtonWidget cancelButton;
	private ScreenPage screenPage;
	private List<MutablePair<String, MutablePair<BlockPos, MutablePair<Double, Double>>>> respawnPositionsList = new ArrayList<>();
	private int scrollPosition = 0;
	private float scrollAmount = 0.0f;
	private boolean mouseClicked = false;

	public PVPControllerBlockScreen(PVPControllerBlockEntity pvpControllerBlockEntity) {
		super(NarratorManager.EMPTY);
		this.pvpControllerBlockEntity = pvpControllerBlockEntity;
		this.screenPage = ScreenPage.SETTINGS_ID;
	}

	private void addNewSideEntrance() {
		String newRespawnPositionName = this.newRespawnPositionNameField.getText();
		if (newRespawnPositionName.isEmpty()) {
			return;
		}
		boolean bl = true;
		for (MutablePair<String, MutablePair<BlockPos, MutablePair<Double, Double>>> stringMutablePairMutablePair : this.respawnPositionsList) {
			if (stringMutablePairMutablePair.getLeft().equals(newRespawnPositionName)) {
				bl = false;
				break;
			}
		}
		if (bl) {
			this.respawnPositionsList.add(new MutablePair<>(
					this.newRespawnPositionNameField.getText(),
					new MutablePair<>(
							new BlockPos(
									ItemUtils.parseInt(this.newRespawnPositionOffsetXField.getText()),
									ItemUtils.parseInt(this.newRespawnPositionOffsetYField.getText()),
									ItemUtils.parseInt(this.newRespawnPositionOffsetZField.getText())
							),
							new MutablePair<>(
									ItemUtils.parseDouble(this.newRespawnOrientationYawField.getText()),
									ItemUtils.parseDouble(this.newRespawnOrientationPitchField.getText())
							)
					)
			));
			this.scrollPosition = 0;
			this.scrollAmount = 0.0f;
			this.updateWidgets();
		} else if (this.client != null && this.client.player != null) {
			this.client.player.sendMessage(Text.translatable("gui.teleporter_block.location_already_in_list"));
		}
	}

	private void removeSideEntrance(int index) {
		this.respawnPositionsList.remove(index + this.scrollPosition);
		this.scrollPosition = 0;
		this.scrollAmount = 0.0f;
		this.updateWidgets();
	}

	private void done() {
		if (this.updatePVPControllerBlock()) {
			this.close();
		}
	}

	private void cancel() {
		this.close();
	}

	@Override
	protected void init() {
		this.respawnPositionsList.clear();
		List<String> keyList = new ArrayList<>(this.pvpControllerBlockEntity.getRespawnPositions().keySet());
		for (String key : keyList) {
			this.respawnPositionsList.add(new MutablePair<>(key, this.pvpControllerBlockEntity.getRespawnPositions().get(key)));
		}
		super.init();

		this.creativeScreenPageButton = this.addDrawableChild(CyclingButtonWidget.builder(ScreenPage::asText).values((ScreenPage[]) ScreenPage.values()).initially(this.screenPage).omitKeyText().build(this.width / 2 - 154, 18, 308, 20, Text.empty(), (button, screenPage) -> {
			this.screenPage = screenPage;
			this.updateWidgets();
		}));

		// --- settings id page ---

		this.pvpArenaSettingsIdentifierField = new TextFieldWidget(this.textRenderer, this.width / 2 - 154, 80, 300, 20, Text.empty());
		this.pvpArenaSettingsIdentifierField.setMaxLength(128);
		this.pvpArenaSettingsIdentifierField.setText(this.pvpControllerBlockEntity.getPVPArenaSettingsIdentifier());
		this.addSelectableChild(this.pvpArenaSettingsIdentifierField);

		this.triggeredBlockPositionOffsetXField = new TextFieldWidget(this.textRenderer, this.width / 2 - 154, 115, 50, 20, Text.empty());
		this.triggeredBlockPositionOffsetXField.setMaxLength(128);
		this.triggeredBlockPositionOffsetXField.setText(Integer.toString(this.pvpControllerBlockEntity.getTriggeredBlock().getLeft().getX()));
		this.addSelectableChild(this.triggeredBlockPositionOffsetXField);
		this.triggeredBlockPositionOffsetYField = new TextFieldWidget(this.textRenderer, this.width / 2 - 100, 115, 50, 20, Text.empty());
		this.triggeredBlockPositionOffsetYField.setMaxLength(128);
		this.triggeredBlockPositionOffsetYField.setText(Integer.toString(this.pvpControllerBlockEntity.getTriggeredBlock().getLeft().getY()));
		this.addSelectableChild(this.triggeredBlockPositionOffsetYField);
		this.triggeredBlockPositionOffsetZField = new TextFieldWidget(this.textRenderer, this.width / 2 - 46, 115, 50, 20, Text.empty());
		this.triggeredBlockPositionOffsetZField.setMaxLength(128);
		this.triggeredBlockPositionOffsetZField.setText(Integer.toString(this.pvpControllerBlockEntity.getTriggeredBlock().getLeft().getZ()));
		this.addSelectableChild(this.triggeredBlockPositionOffsetZField);
		this.triggeredBlockResets = this.pvpControllerBlockEntity.getTriggeredBlock().getRight();
		this.toggleTriggeredBlockResetsButton = this.addDrawableChild(CyclingButtonWidget.onOffBuilder(Text.translatable("gui.triggered_block.toggle_triggered_block_resets_button_label.on"), Text.translatable("gui.triggered_block.toggle_triggered_block_resets_button_label.off")).initially(this.triggeredBlockResets).omitKeyText().build(this.width / 2 + 8, 115, 150, 20, Text.empty(), (button, triggeredBlockResets) -> {
			this.triggeredBlockResets = triggeredBlockResets;
		}));

		this.dataProvidingBlockPositionOffsetXField = new TextFieldWidget(this.textRenderer, this.width / 2 - 154, 150, 50, 20, Text.empty());
		this.dataProvidingBlockPositionOffsetXField.setMaxLength(128);
		this.dataProvidingBlockPositionOffsetXField.setText(Integer.toString(this.pvpControllerBlockEntity.getDataProvidingBlockPosOffset().getX()));
		this.addSelectableChild(this.dataProvidingBlockPositionOffsetXField);
		this.dataProvidingBlockPositionOffsetYField = new TextFieldWidget(this.textRenderer, this.width / 2 - 100, 150, 50, 20, Text.empty());
		this.dataProvidingBlockPositionOffsetYField.setMaxLength(128);
		this.dataProvidingBlockPositionOffsetYField.setText(Integer.toString(this.pvpControllerBlockEntity.getDataProvidingBlockPosOffset().getY()));
		this.addSelectableChild(this.dataProvidingBlockPositionOffsetYField);
		this.dataProvidingBlockPositionOffsetZField = new TextFieldWidget(this.textRenderer, this.width / 2 - 46, 150, 50, 20, Text.empty());
		this.dataProvidingBlockPositionOffsetZField.setMaxLength(128);
		this.dataProvidingBlockPositionOffsetZField.setText(Integer.toString(this.pvpControllerBlockEntity.getDataProvidingBlockPosOffset().getZ()));
		this.addSelectableChild(this.dataProvidingBlockPositionOffsetZField);
		this.dataProvidingBlockPositionOffsetZField = new TextFieldWidget(this.textRenderer, this.width / 2 - 46, 150, 50, 20, Text.empty());
		this.dataProvidingBlockPositionOffsetZField.setMaxLength(128);
		this.dataProvidingBlockPositionOffsetZField.setText(Integer.toString(this.pvpControllerBlockEntity.getDataProvidingBlockPosOffset().getZ()));
		this.addSelectableChild(this.dataProvidingBlockPositionOffsetZField);

		this.matchDurationDataIdentifierField = new TextFieldWidget(this.textRenderer, this.width / 2 + 8, 150, 150, 20, Text.empty());
		this.matchDurationDataIdentifierField.setMaxLength(128);
		this.matchDurationDataIdentifierField.setText(this.pvpControllerBlockEntity.getMatchDurationDataIdentifier());
		this.addSelectableChild(this.matchDurationDataIdentifierField);

		// --- respawn positions page ---

		this.removeRespawnPositionButton0 = this.addDrawableChild(ButtonWidget.builder(REMOVE_LIST_ENTRY_BUTTON_LABEL_TEXT, button -> this.removeSideEntrance(0)).dimensions(this.width / 2 + 104, 44, 50, 20).build());
		this.removeRespawnPositionButton1 = this.addDrawableChild(ButtonWidget.builder(REMOVE_LIST_ENTRY_BUTTON_LABEL_TEXT, button -> this.removeSideEntrance(1)).dimensions(this.width / 2 + 104, 68, 50, 20).build());
		this.removeRespawnPositionButton2 = this.addDrawableChild(ButtonWidget.builder(REMOVE_LIST_ENTRY_BUTTON_LABEL_TEXT, button -> this.removeSideEntrance(2)).dimensions(this.width / 2 + 104, 92, 50, 20).build());

		this.newRespawnPositionOffsetXField = new TextFieldWidget(this.textRenderer, this.width / 2 - 154, 127, 100, 20, Text.empty());
		this.newRespawnPositionOffsetXField.setMaxLength(128);
		this.addSelectableChild(this.newRespawnPositionOffsetXField);

		this.newRespawnPositionOffsetYField = new TextFieldWidget(this.textRenderer, this.width / 2 - 50, 127, 100, 20, Text.empty());
		this.newRespawnPositionOffsetYField.setMaxLength(128);
		this.addSelectableChild(this.newRespawnPositionOffsetYField);

		this.newRespawnPositionOffsetZField = new TextFieldWidget(this.textRenderer, this.width / 2 + 54, 127, 100, 20, Text.empty());
		this.newRespawnPositionOffsetZField.setMaxLength(128);
		this.addSelectableChild(this.newRespawnPositionOffsetZField);

		this.newRespawnPositionNameField = new TextFieldWidget(this.textRenderer, this.width / 2 - 154, 162, 204, 20, Text.empty());
		this.newRespawnPositionNameField.setMaxLength(128);
		this.addSelectableChild(this.newRespawnPositionNameField);

		this.newRespawnOrientationYawField = new TextFieldWidget(this.textRenderer, this.width / 2 + 54, 162, 48, 20, Text.empty());
		this.newRespawnOrientationYawField.setMaxLength(128);
		this.addSelectableChild(this.newRespawnOrientationYawField);

		this.newRespawnOrientationPitchField = new TextFieldWidget(this.textRenderer, this.width / 2 + 106, 162, 48, 20, Text.empty());
		this.newRespawnOrientationPitchField.setMaxLength(128);
		this.addSelectableChild(this.newRespawnOrientationPitchField);

		this.addNewRespawnPositionButton = this.addDrawableChild(ButtonWidget.builder(ScreenTexts.DONE, button -> this.addNewSideEntrance()).dimensions(this.width / 2 - 4 - 150, 186, 308, 20).build());


		this.saveButton = this.addDrawableChild(ButtonWidget.builder(ScreenTexts.DONE, button -> this.done()).dimensions(this.width / 2 - 4 - 150, 210, 150, 20).build());
		this.cancelButton = this.addDrawableChild(ButtonWidget.builder(ScreenTexts.CANCEL, button -> this.cancel()).dimensions(this.width / 2 + 4, 210, 150, 20).build());

		this.updateWidgets();
	}

	private void updateWidgets() {

		this.creativeScreenPageButton.visible = false;

		this.pvpArenaSettingsIdentifierField.setVisible(false);

		this.triggeredBlockPositionOffsetXField.setVisible(false);
		this.triggeredBlockPositionOffsetYField.setVisible(false);
		this.triggeredBlockPositionOffsetZField.setVisible(false);

		this.toggleTriggeredBlockResetsButton.visible = false;

		this.dataProvidingBlockPositionOffsetXField.setVisible(false);
		this.dataProvidingBlockPositionOffsetYField.setVisible(false);
		this.dataProvidingBlockPositionOffsetZField.setVisible(false);

		this.matchDurationDataIdentifierField.setVisible(false);

		this.removeRespawnPositionButton0.visible = false;
		this.removeRespawnPositionButton1.visible = false;
		this.removeRespawnPositionButton2.visible = false;

		this.newRespawnPositionOffsetXField.setVisible(false);
		this.newRespawnPositionOffsetYField.setVisible(false);
		this.newRespawnPositionOffsetZField.setVisible(false);
		this.newRespawnOrientationYawField.setVisible(false);
		this.newRespawnOrientationPitchField.setVisible(false);
		this.newRespawnPositionNameField.setVisible(false);
		this.addNewRespawnPositionButton.visible = false;

		this.saveButton.visible = false;
		this.cancelButton.visible = false;

		this.creativeScreenPageButton.visible = true;

		if (this.screenPage == ScreenPage.SETTINGS_ID) {

			this.pvpArenaSettingsIdentifierField.setVisible(true);

			this.triggeredBlockPositionOffsetXField.setVisible(true);
			this.triggeredBlockPositionOffsetYField.setVisible(true);
			this.triggeredBlockPositionOffsetZField.setVisible(true);

			this.toggleTriggeredBlockResetsButton.visible = true;

			this.dataProvidingBlockPositionOffsetXField.setVisible(true);
			this.dataProvidingBlockPositionOffsetYField.setVisible(true);
			this.dataProvidingBlockPositionOffsetZField.setVisible(true);

			this.matchDurationDataIdentifierField.setVisible(true);

		} else if (this.screenPage == ScreenPage.RESPAWN_POSITIONS) {

			int index = 0;
			for (int i = 0; i < Math.min(3, this.respawnPositionsList.size()); i++) {
				if (index == 0) {
					this.removeRespawnPositionButton0.visible = true;
				} else if (index == 1) {
					this.removeRespawnPositionButton1.visible = true;
				} else if (index == 2) {
					this.removeRespawnPositionButton2.visible = true;
				}
				index++;
			}

			this.newRespawnPositionOffsetXField.setVisible(true);
			this.newRespawnPositionOffsetYField.setVisible(true);
			this.newRespawnPositionOffsetZField.setVisible(true);
			this.newRespawnOrientationYawField.setVisible(true);
			this.newRespawnOrientationPitchField.setVisible(true);
			this.newRespawnPositionNameField.setVisible(true);
			this.addNewRespawnPositionButton.visible = true;

		}

		this.saveButton.visible = true;
		this.cancelButton.visible = true;

	}

	@Override
	public void resize(MinecraftClient client, int width, int height) {
		List<MutablePair<String, MutablePair<BlockPos, MutablePair<Double, Double>>>> list = new ArrayList<>(this.respawnPositionsList);
		ScreenPage var = this.screenPage;
		boolean bl = this.triggeredBlockResets;
		int number = this.scrollPosition;
		float number1 = this.scrollAmount;
		String string = this.pvpArenaSettingsIdentifierField.getText();
		String string5 = this.newRespawnPositionOffsetXField.getText();
		String string6 = this.newRespawnPositionOffsetYField.getText();
		String string7 = this.newRespawnPositionOffsetZField.getText();
		String string8 = this.newRespawnOrientationYawField.getText();
		String string9 = this.newRespawnOrientationPitchField.getText();
		String string10 = this.newRespawnPositionNameField.getText();
		String string11 = this.triggeredBlockPositionOffsetXField.getText();
		String string12 = this.triggeredBlockPositionOffsetYField.getText();
		String string13 = this.triggeredBlockPositionOffsetZField.getText();
		String string14 = this.dataProvidingBlockPositionOffsetXField.getText();
		String string15 = this.dataProvidingBlockPositionOffsetYField.getText();
		String string16 = this.dataProvidingBlockPositionOffsetZField.getText();
		String string17 = this.matchDurationDataIdentifierField.getText();
		this.init(client, width, height);
		this.respawnPositionsList.clear();
		this.respawnPositionsList.addAll(list);
		this.screenPage = var;
		this.triggeredBlockResets = bl;
		this.scrollPosition = number;
		this.scrollAmount = number1;
		this.pvpArenaSettingsIdentifierField.setText(string);
		this.newRespawnPositionOffsetXField.setText(string5);
		this.newRespawnPositionOffsetYField.setText(string6);
		this.newRespawnPositionOffsetZField.setText(string7);
		this.newRespawnOrientationYawField.setText(string8);
		this.newRespawnOrientationPitchField.setText(string9);
		this.newRespawnPositionNameField.setText(string10);
		this.triggeredBlockPositionOffsetXField.setText(string11);
		this.triggeredBlockPositionOffsetYField.setText(string12);
		this.triggeredBlockPositionOffsetZField.setText(string13);
		this.dataProvidingBlockPositionOffsetXField.setText(string14);
		this.dataProvidingBlockPositionOffsetYField.setText(string15);
		this.dataProvidingBlockPositionOffsetZField.setText(string16);
		this.matchDurationDataIdentifierField.setText(string17);
		this.updateWidgets();
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		this.mouseClicked = false;
		if (this.screenPage == ScreenPage.RESPAWN_POSITIONS
				&& this.respawnPositionsList.size() > 3) {
			int i = this.width / 2 - 152;
			int j = 45;
			if (mouseX >= (double) i && mouseX < (double) (i + 6) && mouseY >= (double) j && mouseY < (double) (j + 68)) {
				this.mouseClicked = true;
			}
		}
		return super.mouseClicked(mouseX, mouseY, button);
	}

	@Override
	public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
		if (this.screenPage == ScreenPage.RESPAWN_POSITIONS
				&& this.respawnPositionsList.size() > 3
				&& this.mouseClicked) {
			int i = this.respawnPositionsList.size() - 3;
			float f = (float) deltaY / (float) i;
			this.scrollAmount = MathHelper.clamp(this.scrollAmount + f, 0.0f, 1.0f);
			this.scrollPosition = (int) ((double) (this.scrollAmount * (float) i));
		}
		return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
		if (this.screenPage == ScreenPage.RESPAWN_POSITIONS
				&& this.respawnPositionsList.size() > 3
				&& mouseX >= (double) (this.width / 2 - 152) && mouseX <= (double) (this.width / 2 + 100)
				&& mouseY >= 44 && mouseY <= 114) {
			int i = this.respawnPositionsList.size() - 3;
			float f = (float) verticalAmount / (float) i;
			this.scrollAmount = MathHelper.clamp(this.scrollAmount - f, 0.0f, 1.0f);
			this.scrollPosition = (int) ((double) (this.scrollAmount * (float) i));
		}
		return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if ((keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER)) {
			this.done();
			return true;
		}
		return super.keyPressed(keyCode, scanCode, modifiers);
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {

		super.render(context, mouseX, mouseY, delta);

		if (this.screenPage == ScreenPage.SETTINGS_ID) {
			context.drawTextWithShadow(this.textRenderer, PVP_ARENA_SETTINGS_ID_LABEL_TEXT, this.width / 2 - 153, 70, 0xA0A0A0);
			this.pvpArenaSettingsIdentifierField.render(context, mouseX, mouseY, delta);

			context.drawTextWithShadow(this.textRenderer, TRIGGERED_BLOCK_POSITION_TEXT, this.width / 2 - 153, 105, 0xA0A0A0);
			this.triggeredBlockPositionOffsetXField.render(context, mouseX, mouseY, delta);
			this.triggeredBlockPositionOffsetYField.render(context, mouseX, mouseY, delta);
			this.triggeredBlockPositionOffsetZField.render(context, mouseX, mouseY, delta);

			context.drawTextWithShadow(this.textRenderer, DATA_PROVIDING_BLOCK_POSITION_TEXT, this.width / 2 - 153, 140, 0xA0A0A0);
			this.dataProvidingBlockPositionOffsetXField.render(context, mouseX, mouseY, delta);
			this.dataProvidingBlockPositionOffsetYField.render(context, mouseX, mouseY, delta);
			this.dataProvidingBlockPositionOffsetZField.render(context, mouseX, mouseY, delta);

			context.drawTextWithShadow(this.textRenderer, MATCH_DURATION_DATA_IDENTIFIER_TEXT, this.width / 2 + 9, 140, 0xA0A0A0);
			this.matchDurationDataIdentifierField.render(context, mouseX, mouseY, delta);
		} else if (this.screenPage == ScreenPage.RESPAWN_POSITIONS) {
			for (int i = this.scrollPosition; i < Math.min(this.scrollPosition + 3, this.respawnPositionsList.size()); i++) {
				String text = this.respawnPositionsList.get(i).getLeft();
				if (!this.respawnPositionsList.get(i).getLeft().isEmpty()) {
					text = this.respawnPositionsList.get(i).getLeft() + ", " + this.respawnPositionsList.get(i).getRight();
				}
				context.drawTextWithShadow(this.textRenderer, text, this.width / 2 - 141, 50 + ((i - this.scrollPosition) * 24), 0xA0A0A0);
			}
			if (this.respawnPositionsList.size() > 3) {
				context.drawGuiTexture(SCROLL_BAR_BACKGROUND_8_70_TEXTURE, this.width / 2 - 153, 44, 8, 70);
				int k = (int) (61.0f * this.scrollAmount);
				context.drawGuiTexture(SCROLLER_TEXTURE, this.width / 2 - 152, 44 + 1 + k, 6, 7);
			}
			context.drawTextWithShadow(this.textRenderer, NEW_RESPAWN_POSITION_OFFSET_LABEL_TEXT, this.width / 2 - 153, 116, 0xA0A0A0);
			this.newRespawnPositionOffsetXField.render(context, mouseX, mouseY, delta);
			this.newRespawnPositionOffsetYField.render(context, mouseX, mouseY, delta);
			this.newRespawnPositionOffsetZField.render(context, mouseX, mouseY, delta);
			context.drawTextWithShadow(this.textRenderer, NEW_RESPAWN_POSITION_NAME_LABEL_TEXT, this.width / 2 - 153, 151, 0xA0A0A0);
			context.drawTextWithShadow(this.textRenderer, NEW_RESPAWN_ORIENTATION_LABEL_TEXT, this.width / 2 + 55, 151, 0xA0A0A0);
			this.newRespawnOrientationYawField.render(context, mouseX, mouseY, delta);
			this.newRespawnOrientationPitchField.render(context, mouseX, mouseY, delta);
			this.newRespawnPositionNameField.render(context, mouseX, mouseY, delta);
		}
	}

	@Override
	public boolean shouldPause() {
		return false;
	}

	@Override
	public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
		this.renderInGameBackground(context);
	}

	private boolean updatePVPControllerBlock() {
		ClientPlayNetworking.send(new UpdatePVPControllerBlockPacket(
				this.pvpControllerBlockEntity.getPos(),
				this.pvpArenaSettingsIdentifierField.getText(),
				this.respawnPositionsList,
				new BlockPos(
						ItemUtils.parseInt(this.triggeredBlockPositionOffsetXField.getText()),
						ItemUtils.parseInt(this.triggeredBlockPositionOffsetYField.getText()),
						ItemUtils.parseInt(this.triggeredBlockPositionOffsetZField.getText())
				),
				this.triggeredBlockResets,
				new BlockPos(
						ItemUtils.parseInt(this.dataProvidingBlockPositionOffsetXField.getText()),
						ItemUtils.parseInt(this.dataProvidingBlockPositionOffsetYField.getText()),
						ItemUtils.parseInt(this.dataProvidingBlockPositionOffsetZField.getText())
				),
				this.matchDurationDataIdentifierField.getText()
		));
		return true;
	}

	public static enum ScreenPage implements StringIdentifiable {
		SETTINGS_ID("settings_id"),
		RESPAWN_POSITIONS("respawn_positions");

		private final String name;

		private ScreenPage(String name) {
			this.name = name;
		}

		@Override
		public String asString() {
			return this.name;
		}

		public static Optional<ScreenPage> byName(String name) {
			return Arrays.stream(ScreenPage.values()).filter(screenPage -> screenPage.asString().equals(name)).findFirst();
		}

		public Text asText() {
			return Text.translatable("gui.pvp_controller_block.screenPage." + this.name);
		}
	}
}
