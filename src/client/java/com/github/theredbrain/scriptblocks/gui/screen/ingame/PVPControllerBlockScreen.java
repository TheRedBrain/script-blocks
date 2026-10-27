package com.github.theredbrain.scriptblocks.gui.screen.ingame;

import com.github.theredbrain.scriptblocks.ScriptBlocks;
import com.github.theredbrain.scriptblocks.block.entity.PVPControllerBlockEntity;
import com.github.theredbrain.scriptblocks.network.packet.UpdatePVPControllerBlockPacket;
import com.github.theredbrain.scriptblocks.util.ItemUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.GameNarrator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.util.StringRepresentable;
import org.apache.commons.lang3.tuple.MutablePair;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Environment(value = EnvType.CLIENT)
public class PVPControllerBlockScreen extends AbstractCreativeBlockSettingsScreen {
	private static final Component PVP_ARENA_SETTINGS_ID_LABEL_TEXT = Component.translatable("gui.pvp_controller_block.pvp_arena_settings_id_label");
	private static final Component TRIGGERED_BLOCK_POSITION_TEXT = Component.translatable("gui.pvp_controller_block.match_end_triggered_block_position_offset");
	private static final Component DATA_PROVIDING_BLOCK_POSITION_TEXT = Component.translatable("gui.pvp_controller_block.data_providing_block_position_offset");
	private static final Component MATCH_DURATION_DATA_IDENTIFIER_TEXT = Component.translatable("gui.pvp_controller_block.match_duration_data_identifier");
	private static final Component REMOVE_LIST_ENTRY_BUTTON_LABEL_TEXT = Component.translatable("gui.pvp_controller_block.remove_list_entry_button_label");
	private static final Component NEW_RESPAWN_POSITION_OFFSET_LABEL_TEXT = Component.translatable("gui.pvp_controller_block.new_respawn_position_offset_label");
	private static final Component NEW_RESPAWN_POSITION_NAME_LABEL_TEXT = Component.translatable("gui.pvp_controller_block.new_respawn_position_name_label");
	private static final Component NEW_RESPAWN_ORIENTATION_LABEL_TEXT = Component.translatable("gui.pvp_controller_block.new_respawn_orientation_label");
	private static final Identifier SCROLL_BAR_BACKGROUND_8_70_TEXTURE = ScriptBlocks.identifier("scroll_bar/scroll_bar_background_8_70");
	private static final Identifier SCROLLER_TEXTURE = ScriptBlocks.identifier("scroll_bar/scroller_vertical_6_7");
	private final PVPControllerBlockEntity pvpControllerBlockEntity;
	private CycleButton<ScreenPage> cycleScreenPageButton;
	private EditBox pvpArenaSettingsIdentifierField;
	private EditBox triggeredBlockPositionOffsetXField;
	private EditBox triggeredBlockPositionOffsetYField;
	private EditBox triggeredBlockPositionOffsetZField;
	private CycleButton<Boolean> toggleTriggeredBlockResetsButton;
	private boolean triggeredBlockResets;
	private EditBox dataProvidingBlockPositionOffsetXField;
	private EditBox dataProvidingBlockPositionOffsetYField;
	private EditBox dataProvidingBlockPositionOffsetZField;
	private EditBox matchDurationDataIdentifierField;

	private Button removeRespawnPositionButton0;
	private Button removeRespawnPositionButton1;
	private Button removeRespawnPositionButton2;
	private EditBox newRespawnPositionOffsetXField;
	private EditBox newRespawnPositionOffsetYField;
	private EditBox newRespawnPositionOffsetZField;
	private EditBox newRespawnOrientationYawField;
	private EditBox newRespawnOrientationPitchField;
	private EditBox newRespawnPositionNameField;
	private Button addNewRespawnPositionButton;
	private ScreenPage screenPage;
	private final List<MutablePair<String, MutablePair<BlockPos, MutablePair<Double, Double>>>> respawnPositionsList = new ArrayList<>();
	private int scrollPosition = 0;
	private float scrollAmount = 0.0f;
	private boolean mouseClicked = false;

	public PVPControllerBlockScreen(PVPControllerBlockEntity pvpControllerBlockEntity) {
		super(GameNarrator.NO_TITLE);
		this.pvpControllerBlockEntity = pvpControllerBlockEntity;
		this.screenPage = ScreenPage.SETTINGS_ID;
	}

	private void addNewSideEntrance() {
		String newRespawnPositionName = this.newRespawnPositionNameField.getValue();
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
					this.newRespawnPositionNameField.getValue(),
					new MutablePair<>(
							new BlockPos(
									ItemUtils.parseInt(this.newRespawnPositionOffsetXField.getValue()),
									ItemUtils.parseInt(this.newRespawnPositionOffsetYField.getValue()),
									ItemUtils.parseInt(this.newRespawnPositionOffsetZField.getValue())
							),
							new MutablePair<>(
									ItemUtils.parseDouble(this.newRespawnOrientationYawField.getValue()),
									ItemUtils.parseDouble(this.newRespawnOrientationPitchField.getValue())
							)
					)
			));
			this.scrollPosition = 0;
			this.scrollAmount = 0.0f;
			this.updateWidgets();
		} else if (this.minecraft != null && this.minecraft.player != null) {
			this.minecraft.player.displayClientMessage(Component.translatable("gui.teleporter_block.location_already_in_list"));
		}
	}

	private void removeSideEntrance(int index) {
		this.respawnPositionsList.remove(index + this.scrollPosition);
		this.scrollPosition = 0;
		this.scrollAmount = 0.0f;
		this.updateWidgets();
	}

	@Override
	protected void init() {
		this.respawnPositionsList.clear();
		List<String> keyList = new ArrayList<>(this.pvpControllerBlockEntity.getRespawnPositions().keySet());
		for (String key : keyList) {
			this.respawnPositionsList.add(new MutablePair<>(key, this.pvpControllerBlockEntity.getRespawnPositions().get(key)));
		}
		super.init();

		this.cycleScreenPageButton = this.addRenderableWidget(CycleButton.builder(ScreenPage::asText).values((ScreenPage[]) ScreenPage.values()).initially(this.screenPage).omitKeyText().build(this.width / 2 - 154, 18, 308, 20, Component.empty(), (button, screenPage) -> {
			this.screenPage = screenPage;
			this.updateWidgets();
		}));

		// --- settings id page ---

		this.pvpArenaSettingsIdentifierField = new EditBox(this.font, this.width / 2 - 154, 80, 300, 20, Component.empty());
		this.pvpArenaSettingsIdentifierField.setMaxLength(128);
		this.pvpArenaSettingsIdentifierField.setValue(this.pvpControllerBlockEntity.getPVPArenaSettingsIdentifier());
		this.addWidget(this.pvpArenaSettingsIdentifierField);

		this.triggeredBlockPositionOffsetXField = new EditBox(this.font, this.width / 2 - 154, 115, 50, 20, Component.empty());
		this.triggeredBlockPositionOffsetXField.setMaxLength(128);
		this.triggeredBlockPositionOffsetXField.setValue(Integer.toString(this.pvpControllerBlockEntity.getTriggeredBlock().getLeft().getX()));
		this.addWidget(this.triggeredBlockPositionOffsetXField);
		this.triggeredBlockPositionOffsetYField = new EditBox(this.font, this.width / 2 - 100, 115, 50, 20, Component.empty());
		this.triggeredBlockPositionOffsetYField.setMaxLength(128);
		this.triggeredBlockPositionOffsetYField.setValue(Integer.toString(this.pvpControllerBlockEntity.getTriggeredBlock().getLeft().getY()));
		this.addWidget(this.triggeredBlockPositionOffsetYField);
		this.triggeredBlockPositionOffsetZField = new EditBox(this.font, this.width / 2 - 46, 115, 50, 20, Component.empty());
		this.triggeredBlockPositionOffsetZField.setMaxLength(128);
		this.triggeredBlockPositionOffsetZField.setValue(Integer.toString(this.pvpControllerBlockEntity.getTriggeredBlock().getLeft().getZ()));
		this.addWidget(this.triggeredBlockPositionOffsetZField);
		this.triggeredBlockResets = this.pvpControllerBlockEntity.getTriggeredBlock().getRight();
		this.toggleTriggeredBlockResetsButton = this.addRenderableWidget(CycleButton.booleanBuilder(Component.translatable("gui.triggered_block.toggle_triggered_block_resets_button_label.on"), Component.translatable("gui.triggered_block.toggle_triggered_block_resets_button_label.off")).initially(this.triggeredBlockResets).omitKeyText().build(this.width / 2 + 8, 115, 150, 20, Component.empty(), (button, triggeredBlockResets) -> {
			this.triggeredBlockResets = triggeredBlockResets;
		}));

		this.dataProvidingBlockPositionOffsetXField = new EditBox(this.font, this.width / 2 - 154, 150, 50, 20, Component.empty());
		this.dataProvidingBlockPositionOffsetXField.setMaxLength(128);
		this.dataProvidingBlockPositionOffsetXField.setValue(Integer.toString(this.pvpControllerBlockEntity.getDataProvidingBlockPosOffset().getX()));
		this.addWidget(this.dataProvidingBlockPositionOffsetXField);
		this.dataProvidingBlockPositionOffsetYField = new EditBox(this.font, this.width / 2 - 100, 150, 50, 20, Component.empty());
		this.dataProvidingBlockPositionOffsetYField.setMaxLength(128);
		this.dataProvidingBlockPositionOffsetYField.setValue(Integer.toString(this.pvpControllerBlockEntity.getDataProvidingBlockPosOffset().getY()));
		this.addWidget(this.dataProvidingBlockPositionOffsetYField);
		this.dataProvidingBlockPositionOffsetZField = new EditBox(this.font, this.width / 2 - 46, 150, 50, 20, Component.empty());
		this.dataProvidingBlockPositionOffsetZField.setMaxLength(128);
		this.dataProvidingBlockPositionOffsetZField.setValue(Integer.toString(this.pvpControllerBlockEntity.getDataProvidingBlockPosOffset().getZ()));
		this.addWidget(this.dataProvidingBlockPositionOffsetZField);
		this.dataProvidingBlockPositionOffsetZField = new EditBox(this.font, this.width / 2 - 46, 150, 50, 20, Component.empty());
		this.dataProvidingBlockPositionOffsetZField.setMaxLength(128);
		this.dataProvidingBlockPositionOffsetZField.setValue(Integer.toString(this.pvpControllerBlockEntity.getDataProvidingBlockPosOffset().getZ()));
		this.addWidget(this.dataProvidingBlockPositionOffsetZField);

		this.matchDurationDataIdentifierField = new EditBox(this.font, this.width / 2 + 8, 150, 150, 20, Component.empty());
		this.matchDurationDataIdentifierField.setMaxLength(128);
		this.matchDurationDataIdentifierField.setValue(this.pvpControllerBlockEntity.getMatchDurationDataIdentifier());
		this.addWidget(this.matchDurationDataIdentifierField);

		// --- respawn positions page ---

		this.removeRespawnPositionButton0 = this.addRenderableWidget(Button.builder(REMOVE_LIST_ENTRY_BUTTON_LABEL_TEXT, button -> this.removeSideEntrance(0)).bounds(this.width / 2 + 104, 44, 50, 20).build());
		this.removeRespawnPositionButton1 = this.addRenderableWidget(Button.builder(REMOVE_LIST_ENTRY_BUTTON_LABEL_TEXT, button -> this.removeSideEntrance(1)).bounds(this.width / 2 + 104, 68, 50, 20).build());
		this.removeRespawnPositionButton2 = this.addRenderableWidget(Button.builder(REMOVE_LIST_ENTRY_BUTTON_LABEL_TEXT, button -> this.removeSideEntrance(2)).bounds(this.width / 2 + 104, 92, 50, 20).build());

		this.newRespawnPositionOffsetXField = new EditBox(this.font, this.width / 2 - 154, 127, 100, 20, Component.empty());
		this.newRespawnPositionOffsetXField.setMaxLength(128);
		this.addWidget(this.newRespawnPositionOffsetXField);

		this.newRespawnPositionOffsetYField = new EditBox(this.font, this.width / 2 - 50, 127, 100, 20, Component.empty());
		this.newRespawnPositionOffsetYField.setMaxLength(128);
		this.addWidget(this.newRespawnPositionOffsetYField);

		this.newRespawnPositionOffsetZField = new EditBox(this.font, this.width / 2 + 54, 127, 100, 20, Component.empty());
		this.newRespawnPositionOffsetZField.setMaxLength(128);
		this.addWidget(this.newRespawnPositionOffsetZField);

		this.newRespawnPositionNameField = new EditBox(this.font, this.width / 2 - 154, 162, 204, 20, Component.empty());
		this.newRespawnPositionNameField.setMaxLength(128);
		this.addWidget(this.newRespawnPositionNameField);

		this.newRespawnOrientationYawField = new EditBox(this.font, this.width / 2 + 54, 162, 48, 20, Component.empty());
		this.newRespawnOrientationYawField.setMaxLength(128);
		this.addWidget(this.newRespawnOrientationYawField);

		this.newRespawnOrientationPitchField = new EditBox(this.font, this.width / 2 + 106, 162, 48, 20, Component.empty());
		this.newRespawnOrientationPitchField.setMaxLength(128);
		this.addWidget(this.newRespawnOrientationPitchField);

		this.addNewRespawnPositionButton = this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> this.addNewSideEntrance()).bounds(this.width / 2 - 4 - 150, 186, 308, 20).build());


		this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> this.done()).bounds(this.width / 2 - 4 - 150, 210, 150, 20).build());
		this.addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL, button -> this.cancel()).bounds(this.width / 2 + 4, 210, 150, 20).build());

		this.updateWidgets();
	}

	@Override
	protected void setInitialFocus() {
		this.setInitialFocus(this.cycleScreenPageButton);
	}

	@Override
	protected void updateWidgets() {

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

	}

	@Override
	public void resize(Minecraft client, int width, int height) {
		List<MutablePair<String, MutablePair<BlockPos, MutablePair<Double, Double>>>> list = new ArrayList<>(this.respawnPositionsList);
		ScreenPage var = this.screenPage;
		boolean bl = this.triggeredBlockResets;
		int number = this.scrollPosition;
		float number1 = this.scrollAmount;
		String string = this.pvpArenaSettingsIdentifierField.getValue();
		String string5 = this.newRespawnPositionOffsetXField.getValue();
		String string6 = this.newRespawnPositionOffsetYField.getValue();
		String string7 = this.newRespawnPositionOffsetZField.getValue();
		String string8 = this.newRespawnOrientationYawField.getValue();
		String string9 = this.newRespawnOrientationPitchField.getValue();
		String string10 = this.newRespawnPositionNameField.getValue();
		String string11 = this.triggeredBlockPositionOffsetXField.getValue();
		String string12 = this.triggeredBlockPositionOffsetYField.getValue();
		String string13 = this.triggeredBlockPositionOffsetZField.getValue();
		String string14 = this.dataProvidingBlockPositionOffsetXField.getValue();
		String string15 = this.dataProvidingBlockPositionOffsetYField.getValue();
		String string16 = this.dataProvidingBlockPositionOffsetZField.getValue();
		String string17 = this.matchDurationDataIdentifierField.getValue();
		this.init(client, width, height);
		this.respawnPositionsList.clear();
		this.respawnPositionsList.addAll(list);
		this.screenPage = var;
		this.cycleScreenPageButton.setValue(this.screenPage);
		this.triggeredBlockResets = bl;
		this.toggleTriggeredBlockResetsButton.setValue(this.triggeredBlockResets);
		this.scrollPosition = number;
		this.scrollAmount = number1;
		this.pvpArenaSettingsIdentifierField.setValue(string);
		this.newRespawnPositionOffsetXField.setValue(string5);
		this.newRespawnPositionOffsetYField.setValue(string6);
		this.newRespawnPositionOffsetZField.setValue(string7);
		this.newRespawnOrientationYawField.setValue(string8);
		this.newRespawnOrientationPitchField.setValue(string9);
		this.newRespawnPositionNameField.setValue(string10);
		this.triggeredBlockPositionOffsetXField.setValue(string11);
		this.triggeredBlockPositionOffsetYField.setValue(string12);
		this.triggeredBlockPositionOffsetZField.setValue(string13);
		this.dataProvidingBlockPositionOffsetXField.setValue(string14);
		this.dataProvidingBlockPositionOffsetYField.setValue(string15);
		this.dataProvidingBlockPositionOffsetZField.setValue(string16);
		this.matchDurationDataIdentifierField.setValue(string17);
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
			this.scrollAmount = Mth.clamp(this.scrollAmount + f, 0.0f, 1.0f);
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
			this.scrollAmount = Mth.clamp(this.scrollAmount - f, 0.0f, 1.0f);
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
	public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {

		super.render(context, mouseX, mouseY, delta);

		if (this.screenPage == ScreenPage.SETTINGS_ID) {
			context.drawString(this.font, PVP_ARENA_SETTINGS_ID_LABEL_TEXT, this.width / 2 - 153, 70, 0xA0A0A0);
			this.pvpArenaSettingsIdentifierField.render(context, mouseX, mouseY, delta);

			context.drawString(this.font, TRIGGERED_BLOCK_POSITION_TEXT, this.width / 2 - 153, 105, 0xA0A0A0);
			this.triggeredBlockPositionOffsetXField.render(context, mouseX, mouseY, delta);
			this.triggeredBlockPositionOffsetYField.render(context, mouseX, mouseY, delta);
			this.triggeredBlockPositionOffsetZField.render(context, mouseX, mouseY, delta);

			context.drawString(this.font, DATA_PROVIDING_BLOCK_POSITION_TEXT, this.width / 2 - 153, 140, 0xA0A0A0);
			this.dataProvidingBlockPositionOffsetXField.render(context, mouseX, mouseY, delta);
			this.dataProvidingBlockPositionOffsetYField.render(context, mouseX, mouseY, delta);
			this.dataProvidingBlockPositionOffsetZField.render(context, mouseX, mouseY, delta);

			context.drawString(this.font, MATCH_DURATION_DATA_IDENTIFIER_TEXT, this.width / 2 + 9, 140, 0xA0A0A0);
			this.matchDurationDataIdentifierField.render(context, mouseX, mouseY, delta);
		} else if (this.screenPage == ScreenPage.RESPAWN_POSITIONS) {
			for (int i = this.scrollPosition; i < Math.min(this.scrollPosition + 3, this.respawnPositionsList.size()); i++) {
				String text = this.respawnPositionsList.get(i).getLeft();
				if (!this.respawnPositionsList.get(i).getLeft().isEmpty()) {
					text = this.respawnPositionsList.get(i).getLeft() + ", " + this.respawnPositionsList.get(i).getRight();
				}
				context.drawString(this.font, text, this.width / 2 - 141, 50 + ((i - this.scrollPosition) * 24), 0xA0A0A0);
			}
			if (this.respawnPositionsList.size() > 3) {
				context.blitSprite(SCROLL_BAR_BACKGROUND_8_70_TEXTURE, this.width / 2 - 153, 44, 8, 70);
				int k = (int) (61.0f * this.scrollAmount);
				context.blitSprite(SCROLLER_TEXTURE, this.width / 2 - 152, 44 + 1 + k, 6, 7);
			}
			context.drawString(this.font, NEW_RESPAWN_POSITION_OFFSET_LABEL_TEXT, this.width / 2 - 153, 116, 0xA0A0A0);
			this.newRespawnPositionOffsetXField.render(context, mouseX, mouseY, delta);
			this.newRespawnPositionOffsetYField.render(context, mouseX, mouseY, delta);
			this.newRespawnPositionOffsetZField.render(context, mouseX, mouseY, delta);
			context.drawString(this.font, NEW_RESPAWN_POSITION_NAME_LABEL_TEXT, this.width / 2 - 153, 151, 0xA0A0A0);
			context.drawString(this.font, NEW_RESPAWN_ORIENTATION_LABEL_TEXT, this.width / 2 + 55, 151, 0xA0A0A0);
			this.newRespawnOrientationYawField.render(context, mouseX, mouseY, delta);
			this.newRespawnOrientationPitchField.render(context, mouseX, mouseY, delta);
			this.newRespawnPositionNameField.render(context, mouseX, mouseY, delta);
		}
	}

	@Override
	protected boolean updateBlock() {
		ClientPlayNetworking.send(new UpdatePVPControllerBlockPacket(
				this.pvpControllerBlockEntity.getPos(),
				this.pvpArenaSettingsIdentifierField.getValue(),
				this.respawnPositionsList,
				new BlockPos(
						ItemUtils.parseInt(this.triggeredBlockPositionOffsetXField.getValue()),
						ItemUtils.parseInt(this.triggeredBlockPositionOffsetYField.getValue()),
						ItemUtils.parseInt(this.triggeredBlockPositionOffsetZField.getValue())
				),
				this.triggeredBlockResets,
				new BlockPos(
						ItemUtils.parseInt(this.dataProvidingBlockPositionOffsetXField.getValue()),
						ItemUtils.parseInt(this.dataProvidingBlockPositionOffsetYField.getValue()),
						ItemUtils.parseInt(this.dataProvidingBlockPositionOffsetZField.getValue())
				),
				this.matchDurationDataIdentifierField.getValue()
		));
		return true;
	}

	public enum ScreenPage implements StringRepresentable {
		SETTINGS_ID("settings_id"),
		RESPAWN_POSITIONS("respawn_positions");

		private final String name;

		ScreenPage(String name) {
			this.name = name;
		}

		@Override
		public String getSerializedName() {
			return this.name;
		}

		public static Optional<ScreenPage> byName(String name) {
			return Arrays.stream(ScreenPage.values()).filter(screenPage -> screenPage.getSerializedName().equals(name)).findFirst();
		}

		public Component asText() {
			return Component.translatable("gui.pvp_controller_block.screen_page." + this.name);
		}
	}
}
