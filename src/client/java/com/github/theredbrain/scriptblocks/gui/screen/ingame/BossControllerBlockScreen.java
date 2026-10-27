package com.github.theredbrain.scriptblocks.gui.screen.ingame;

import com.github.theredbrain.scriptblocks.ScriptBlocks;
import com.github.theredbrain.scriptblocks.block.entity.BossControllerBlockEntity;
import com.github.theredbrain.scriptblocks.network.packet.UpdateBossControllerBlockPacket;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Environment(value = EnvType.CLIENT)
public class BossControllerBlockScreen extends AbstractCreativeBlockSettingsScreen {
	private static final Component UUID_LIST_PROVIDER_POSITION_OFFSET_LABEL_TEXT = Component.translatable("gui.uuid_list_provider.uuid_list_provider_position_offset");
	private static final Component NO_PLAYERS_AROUND_TRIGGERED_BLOCK_POSITION_OFFET_LABEL_TEXT = Component.translatable("gui.boss_controller_block.no_players_around_triggered_block_position_offset_label");
	private static final Component ADD_ENTRY_BUTTON_LABEL_TEXT = Component.translatable("gui.list_entry.add");
	private static final Component REMOVE_ENTRY_BUTTON_LABEL_TEXT = Component.translatable("gui.list_entry.remove");
	private static final Component ENTRY_ALREADY_IN_LIST_TEXT = Component.translatable("gui.dialogue_block.entry_already_in_list");
	private static final Component BOSS_IDENTIFIER_LABEL_TEXT = Component.translatable("gui.boss_controller_block.boss_identifier_label");
	private static final Component ENTITY_SPAWN_POSITION_OFFSET_LABEL_TEXT = Component.translatable("gui.boss_controller_block.entity_spawn_position_offset_label");
	private static final Component ENTITY_SPAWN_ORIENTATION_PITCH_LABEL_TEXT = Component.translatable("gui.boss_controller_block.entity_spawn_orientation_pitch_label");
	private static final Component ENTITY_SPAWN_ORIENTATION_YAW_LABEL_TEXT = Component.translatable("gui.boss_controller_block.entity_spawn_orientation_yaw_label");
	private static final Component IS_PERSISTENT_LABEL_TEXT = Component.translatable("gui.boss_controller_block.is_persistent_label");
	private static final Component ENABLE_SPAWN_EFFECTS_LABEL_TEXT = Component.translatable("gui.boss_controller_block.enable_spawn_effects_label");
	private static final Component TOGGLE_ENABLE_SPAWN_EFFECTS_BUTTON_ON_LABEL_TEXT = Component.translatable("gui.boss_controller_block.toggle_enable_spawn_effects_button_label.on");
	private static final Component TOGGLE_ENABLE_SPAWN_EFFECTS_BUTTON_OFF_LABEL_TEXT = Component.translatable("gui.boss_controller_block.toggle_enable_spawn_effects_button_label.off");

	private static final Identifier SCROLL_BAR_BACKGROUND_8_96_TEXTURE = ScriptBlocks.identifier("scroll_bar/scroll_bar_background_8_96");
	private static final Identifier SCROLLER_VERTICAL_6_7_TEXTURE = ScriptBlocks.identifier("scroll_bar/scroller_vertical_6_7");

	private final BossControllerBlockEntity bossControllerBlock;

	private CycleButton<ScreenPage> cycleScreenPageButton;

	private EditBox uuidListHandlerBlockPositionOffsetXField;
	private EditBox uuidListHandlerBlockPositionOffsetYField;
	private EditBox uuidListHandlerBlockPositionOffsetZField;

	private EditBox noPlayersAroundTriggeredBlockPositionOffsetXField;
	private EditBox noPlayersAroundTriggeredBlockPositionOffsetYField;
	private EditBox noPlayersAroundTriggeredBlockPositionOffsetZField;
	private boolean noPlayersAroundTriggeredBlockResets;
	private CycleButton<Boolean> toggleNoPlayersAroundTriggeredBlockResetsButton;

	private EditBox bossIdentifierField;
	private EditBox entitySpawnPositionOffsetXField;
	private EditBox entitySpawnPositionOffsetYField;
	private EditBox entitySpawnPositionOffsetZField;
	private EditBox entitySpawnOrientationPitchField;
	private EditBox entitySpawnOrientationYawField;

	private CycleButton<Boolean> toggleIsPersistentButton;
	private CycleButton<Boolean> toggleEnableSpawnEffectsButton;
	private boolean isPersistent;
	private boolean enableSpawnEffects;

	private Button removeBossTriggeredBlockEntryButton0;
	private Button removeBossTriggeredBlockEntryButton1;
	private Button removeBossTriggeredBlockEntryButton2;
	private Button removeBossTriggeredBlockEntryButton3;
	private EditBox newBossTriggeredBlockIdentifierField;
	private EditBox newBossTriggeredBlockPositionOffsetXField;
	private EditBox newBossTriggeredBlockPositionOffsetYField;
	private EditBox newBossTriggeredBlockPositionOffsetZField;
	private CycleButton<Boolean> toggleNewBossTriggeredBlockResetsButton;
	private boolean newBossTriggeredBlockResets;
	private Button addBossTriggeredBlockButton;

	private final List<MutablePair<String, MutablePair<BlockPos, Boolean>>> bossTriggeredBlocksList = new ArrayList<>(List.of());

	private ScreenPage screenPage;

	private int scrollPosition = 0;
	private float scrollAmount = 0.0f;
	private boolean mouseClicked = false;

	public BossControllerBlockScreen(BossControllerBlockEntity bossControllerBlock) {
		super(GameNarrator.NO_TITLE);
		this.bossControllerBlock = bossControllerBlock;
		this.screenPage = ScreenPage.SPAWN_POSITION;
	}

	private void removeBossTriggeredBlockEntry(int index) {
		if (index + this.scrollPosition < this.bossTriggeredBlocksList.size()) {
			this.bossTriggeredBlocksList.remove(index + this.scrollPosition);
		}
		this.scrollPosition = 0;
		this.scrollAmount = 0.0f;
		this.updateWidgets();
	}

	private void addBossTriggeredBlockEntry() {
		String newBossTriggeredBlockIdentifier = this.newBossTriggeredBlockIdentifierField.getValue();
		for (MutablePair<String, MutablePair<BlockPos, Boolean>> entry : this.bossTriggeredBlocksList) {
			if (entry.getLeft().equals(newBossTriggeredBlockIdentifier)) {
				if (this.minecraft != null && this.minecraft.player != null) {
					this.minecraft.player.displayClientMessage(ENTRY_ALREADY_IN_LIST_TEXT);
				}
				return;
			}
		}
		this.bossTriggeredBlocksList.add(
				new MutablePair<>(newBossTriggeredBlockIdentifier,
						new MutablePair<>(
								new BlockPos(
										ItemUtils.parseInt(this.newBossTriggeredBlockPositionOffsetXField.getValue()),
										ItemUtils.parseInt(this.newBossTriggeredBlockPositionOffsetYField.getValue()),
										ItemUtils.parseInt(this.newBossTriggeredBlockPositionOffsetZField.getValue())
								),
								this.newBossTriggeredBlockResets
						)
				)
		);
		this.scrollPosition = 0;
		this.scrollAmount = 0.0f;
		this.updateWidgets();
	}

	@Override
	protected void init() {
		this.bossTriggeredBlocksList.clear();
		List<String> keyList = new ArrayList<>(this.bossControllerBlock.getBossTriggeredBlocks().keySet());
		for (String key : keyList) {
			this.bossTriggeredBlocksList.add(new MutablePair<>(key, this.bossControllerBlock.getBossTriggeredBlocks().get(key)));
		}

		super.init();

		this.cycleScreenPageButton = this.addRenderableWidget(CycleButton.builder(ScreenPage::asText).values((ScreenPage[]) ScreenPage.values()).initially(this.screenPage).omitKeyText().build(this.width / 2 - 154, 20, 300, 20, Component.empty(), (button, screenPage) -> {
			this.screenPage = screenPage;
			this.updateWidgets();
		}));

		// --- players page ---

		this.uuidListHandlerBlockPositionOffsetXField = new EditBox(this.font, this.width / 2 - 154, 124, 100, 20, Component.empty());
		this.uuidListHandlerBlockPositionOffsetXField.setMaxLength(128);
		this.uuidListHandlerBlockPositionOffsetXField.setValue(Integer.toString(this.bossControllerBlock.getUUIDListHandlerBlockPositionOffset().getX()));
		this.addWidget(this.uuidListHandlerBlockPositionOffsetXField);

		this.uuidListHandlerBlockPositionOffsetYField = new EditBox(this.font, this.width / 2 - 50, 124, 100, 20, Component.empty());
		this.uuidListHandlerBlockPositionOffsetYField.setMaxLength(128);
		this.uuidListHandlerBlockPositionOffsetYField.setValue(Integer.toString(this.bossControllerBlock.getUUIDListHandlerBlockPositionOffset().getY()));
		this.addWidget(this.uuidListHandlerBlockPositionOffsetYField);

		this.uuidListHandlerBlockPositionOffsetZField = new EditBox(this.font, this.width / 2 + 54, 124, 100, 20, Component.empty());
		this.uuidListHandlerBlockPositionOffsetZField.setMaxLength(128);
		this.uuidListHandlerBlockPositionOffsetZField.setValue(Integer.toString(this.bossControllerBlock.getUUIDListHandlerBlockPositionOffset().getZ()));
		this.addWidget(this.uuidListHandlerBlockPositionOffsetZField);

		MutablePair<BlockPos, Boolean> noPlayersAroundTriggeredBlock = this.bossControllerBlock.getNoPlayersAroundTriggeredBlock();
		this.noPlayersAroundTriggeredBlockPositionOffsetXField = new EditBox(this.font, this.width / 2 - 154, 159, 50, 20, Component.empty());
		this.noPlayersAroundTriggeredBlockPositionOffsetXField.setMaxLength(128);
		this.noPlayersAroundTriggeredBlockPositionOffsetXField.setValue(Integer.toString(noPlayersAroundTriggeredBlock.getLeft().getX()));
		this.addWidget(this.noPlayersAroundTriggeredBlockPositionOffsetXField);

		this.noPlayersAroundTriggeredBlockPositionOffsetYField = new EditBox(this.font, this.width / 2 - 100, 159, 50, 20, Component.empty());
		this.noPlayersAroundTriggeredBlockPositionOffsetYField.setMaxLength(128);
		this.noPlayersAroundTriggeredBlockPositionOffsetYField.setValue(Integer.toString(noPlayersAroundTriggeredBlock.getLeft().getY()));
		this.addWidget(this.noPlayersAroundTriggeredBlockPositionOffsetYField);

		this.noPlayersAroundTriggeredBlockPositionOffsetZField = new EditBox(this.font, this.width / 2 - 46, 159, 50, 20, Component.empty());
		this.noPlayersAroundTriggeredBlockPositionOffsetZField.setMaxLength(128);
		this.noPlayersAroundTriggeredBlockPositionOffsetZField.setValue(Integer.toString(noPlayersAroundTriggeredBlock.getLeft().getZ()));
		this.addWidget(this.noPlayersAroundTriggeredBlockPositionOffsetZField);

		this.noPlayersAroundTriggeredBlockResets = noPlayersAroundTriggeredBlock.getRight();
		this.toggleNoPlayersAroundTriggeredBlockResetsButton = this.addRenderableWidget(CycleButton.booleanBuilder(Component.translatable("gui.triggered_block.toggle_triggered_block_resets_button_label.on"), Component.translatable("gui.triggered_block.toggle_triggered_block_resets_button_label.off")).initially(this.noPlayersAroundTriggeredBlockResets).omitKeyText().build(this.width / 2 + 8, 159, 150, 20, Component.empty(), (button, noPlayersAroundTriggeredBlockResets) -> {
			this.noPlayersAroundTriggeredBlockResets = noPlayersAroundTriggeredBlockResets;
		}));

		// --- spawning position page ---

		this.bossIdentifierField = new EditBox(this.font, this.width / 2 - 154, 55, 300, 20, Component.empty());
		this.bossIdentifierField.setMaxLength(128);
		this.bossIdentifierField.setValue(this.bossControllerBlock.getBossIdentifier().toString());
		this.addWidget(this.bossIdentifierField);

		this.entitySpawnPositionOffsetXField = new EditBox(this.font, this.width / 2 - 154, 90, 100, 20, Component.empty());
		this.entitySpawnPositionOffsetXField.setValue(Integer.toString(this.bossControllerBlock.getBossSpawnPositionOffset().getX()));
		this.addWidget(this.entitySpawnPositionOffsetXField);
		this.entitySpawnPositionOffsetYField = new EditBox(this.font, this.width / 2 - 50, 90, 100, 20, Component.empty());
		this.entitySpawnPositionOffsetYField.setValue(Integer.toString(this.bossControllerBlock.getBossSpawnPositionOffset().getY()));
		this.addWidget(this.entitySpawnPositionOffsetYField);
		this.entitySpawnPositionOffsetZField = new EditBox(this.font, this.width / 2 + 54, 90, 100, 20, Component.empty());
		this.entitySpawnPositionOffsetZField.setValue(Integer.toString(this.bossControllerBlock.getBossSpawnPositionOffset().getZ()));
		this.addWidget(this.entitySpawnPositionOffsetZField);

		this.entitySpawnOrientationPitchField = new EditBox(this.font, this.width / 2 - 154, 125, 150, 20, Component.empty());
		this.entitySpawnOrientationPitchField.setValue(Double.toString(this.bossControllerBlock.getBossSpawnOrientationPitch()));
		this.addWidget(this.entitySpawnOrientationPitchField);
		this.entitySpawnOrientationYawField = new EditBox(this.font, this.width / 2 + 4, 125, 150, 20, Component.empty());
		this.entitySpawnOrientationYawField.setValue(Double.toString(this.bossControllerBlock.getBossSpawnOrientationYaw()));
		this.addWidget(this.entitySpawnOrientationYawField);

		this.isPersistent = this.bossControllerBlock.isPersistent();
		this.toggleIsPersistentButton = this.addRenderableWidget(CycleButton.onOffBuilder().initially(this.isPersistent).omitKeyText().build(this.width / 2 - 154, 160, 150, 20, Component.empty(), (button, isPersistent) -> {
			this.isPersistent = isPersistent;
		}));

		this.enableSpawnEffects = this.bossControllerBlock.enableSpawnEffects();
		this.toggleEnableSpawnEffectsButton = this.addRenderableWidget(CycleButton.booleanBuilder(TOGGLE_ENABLE_SPAWN_EFFECTS_BUTTON_ON_LABEL_TEXT, TOGGLE_ENABLE_SPAWN_EFFECTS_BUTTON_OFF_LABEL_TEXT).initially(this.enableSpawnEffects).omitKeyText().build(this.width / 2 + 4, 160, 150, 20, Component.empty(), (button, enableSpawnEffects) -> {
			this.enableSpawnEffects = enableSpawnEffects;
		}));

		// --- boss triggered blocks page ---

		this.removeBossTriggeredBlockEntryButton0 = this.addRenderableWidget(Button.builder(REMOVE_ENTRY_BUTTON_LABEL_TEXT, button -> this.removeBossTriggeredBlockEntry(0)).bounds(this.width / 2 + 54, 42, 100, 20).build());
		this.removeBossTriggeredBlockEntryButton1 = this.addRenderableWidget(Button.builder(REMOVE_ENTRY_BUTTON_LABEL_TEXT, button -> this.removeBossTriggeredBlockEntry(1)).bounds(this.width / 2 + 54, 66, 100, 20).build());
		this.removeBossTriggeredBlockEntryButton2 = this.addRenderableWidget(Button.builder(REMOVE_ENTRY_BUTTON_LABEL_TEXT, button -> this.removeBossTriggeredBlockEntry(2)).bounds(this.width / 2 + 54, 90, 100, 20).build());
		this.removeBossTriggeredBlockEntryButton3 = this.addRenderableWidget(Button.builder(REMOVE_ENTRY_BUTTON_LABEL_TEXT, button -> this.removeBossTriggeredBlockEntry(3)).bounds(this.width / 2 + 54, 114, 100, 20).build());

		this.newBossTriggeredBlockIdentifierField = new EditBox(this.font, this.width / 2 - 154, 138, 300, 20, Component.empty());
		this.newBossTriggeredBlockIdentifierField.setMaxLength(128);
		this.addWidget(this.newBossTriggeredBlockIdentifierField);

		this.newBossTriggeredBlockPositionOffsetXField = new EditBox(this.font, this.width / 2 - 154, 162, 50, 20, Component.empty());
		this.newBossTriggeredBlockPositionOffsetXField.setMaxLength(128);
		this.addWidget(this.newBossTriggeredBlockPositionOffsetXField);

		this.newBossTriggeredBlockPositionOffsetYField = new EditBox(this.font, this.width / 2 - 100, 162, 50, 20, Component.empty());
		this.newBossTriggeredBlockPositionOffsetYField.setMaxLength(128);
		this.addWidget(this.newBossTriggeredBlockPositionOffsetYField);

		this.newBossTriggeredBlockPositionOffsetZField = new EditBox(this.font, this.width / 2 - 46, 162, 50, 20, Component.empty());
		this.newBossTriggeredBlockPositionOffsetZField.setMaxLength(128);
		this.addWidget(this.newBossTriggeredBlockPositionOffsetZField);

		this.newBossTriggeredBlockResets = false;
		this.toggleNewBossTriggeredBlockResetsButton = this.addRenderableWidget(CycleButton.booleanBuilder(Component.translatable("gui.triggered_block.toggle_triggered_block_resets_button_label.on"), Component.translatable("gui.triggered_block.toggle_triggered_block_resets_button_label.off")).initially(this.newBossTriggeredBlockResets).omitKeyText().build(this.width / 2 + 8, 162, 150, 20, Component.empty(), (button, triggeredBlockResets) -> {
			this.newBossTriggeredBlockResets = triggeredBlockResets;
		}));

		this.addBossTriggeredBlockButton = this.addRenderableWidget(Button.builder(ADD_ENTRY_BUTTON_LABEL_TEXT, button -> this.addBossTriggeredBlockEntry()).bounds(this.width / 2 - 4 - 150, 186, 300, 20).build());

		this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> this.done()).bounds(this.width / 2 - 4 - 150, 212, 150, 20).build());
		this.addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL, button -> this.cancel()).bounds(this.width / 2 + 4, 212, 150, 20).build());
		this.setInitialFocus(this.entitySpawnPositionOffsetXField);
		this.updateWidgets();
	}

	@Override
	protected void setInitialFocus() {
		this.setInitialFocus(this.cycleScreenPageButton);
	}

	@Override
	protected void updateWidgets() {

		this.uuidListHandlerBlockPositionOffsetXField.setVisible(false);
		this.uuidListHandlerBlockPositionOffsetYField.setVisible(false);
		this.uuidListHandlerBlockPositionOffsetZField.setVisible(false);

		this.noPlayersAroundTriggeredBlockPositionOffsetXField.setVisible(false);
		this.noPlayersAroundTriggeredBlockPositionOffsetYField.setVisible(false);
		this.noPlayersAroundTriggeredBlockPositionOffsetZField.setVisible(false);
		this.toggleNoPlayersAroundTriggeredBlockResetsButton.visible = false;


		this.bossIdentifierField.setVisible(false);
		this.entitySpawnPositionOffsetXField.setVisible(false);
		this.entitySpawnPositionOffsetYField.setVisible(false);
		this.entitySpawnPositionOffsetZField.setVisible(false);
		this.entitySpawnOrientationPitchField.setVisible(false);
		this.entitySpawnOrientationYawField.setVisible(false);

		this.toggleIsPersistentButton.visible = false;
		this.toggleEnableSpawnEffectsButton.visible = false;


		this.removeBossTriggeredBlockEntryButton0.visible = false;
		this.removeBossTriggeredBlockEntryButton1.visible = false;
		this.removeBossTriggeredBlockEntryButton2.visible = false;
		this.removeBossTriggeredBlockEntryButton3.visible = false;

		this.newBossTriggeredBlockIdentifierField.setVisible(false);
		this.newBossTriggeredBlockPositionOffsetXField.setVisible(false);
		this.newBossTriggeredBlockPositionOffsetYField.setVisible(false);
		this.newBossTriggeredBlockPositionOffsetZField.setVisible(false);
		this.toggleNewBossTriggeredBlockResetsButton.visible = false;

		this.addBossTriggeredBlockButton.visible = false;

		if (this.screenPage == ScreenPage.PLAYERS) {

			this.uuidListHandlerBlockPositionOffsetXField.setVisible(true);
			this.uuidListHandlerBlockPositionOffsetYField.setVisible(true);
			this.uuidListHandlerBlockPositionOffsetZField.setVisible(true);

			this.noPlayersAroundTriggeredBlockPositionOffsetXField.setVisible(true);
			this.noPlayersAroundTriggeredBlockPositionOffsetYField.setVisible(true);
			this.noPlayersAroundTriggeredBlockPositionOffsetZField.setVisible(true);
			this.toggleNoPlayersAroundTriggeredBlockResetsButton.visible = true;


		} else if (this.screenPage == ScreenPage.BOSS_TRIGGERED_BLOCKS) {

			int index = 0;
			for (int i = 0; i < Math.min(4, this.bossTriggeredBlocksList.size()); i++) {
				if (index == 0) {
					this.removeBossTriggeredBlockEntryButton0.visible = true;
				} else if (index == 1) {
					this.removeBossTriggeredBlockEntryButton1.visible = true;
				} else if (index == 2) {
					this.removeBossTriggeredBlockEntryButton2.visible = true;
				} else if (index == 3) {
					this.removeBossTriggeredBlockEntryButton3.visible = true;
				}
				index++;
			}

			this.newBossTriggeredBlockIdentifierField.setVisible(true);
			this.newBossTriggeredBlockPositionOffsetXField.setVisible(true);
			this.newBossTriggeredBlockPositionOffsetYField.setVisible(true);
			this.newBossTriggeredBlockPositionOffsetZField.setVisible(true);
			this.toggleNewBossTriggeredBlockResetsButton.visible = true;

			this.addBossTriggeredBlockButton.visible = true;

		} else if (this.screenPage == ScreenPage.SPAWN_POSITION) {

			this.bossIdentifierField.setVisible(true);
			this.entitySpawnPositionOffsetXField.setVisible(true);
			this.entitySpawnPositionOffsetYField.setVisible(true);
			this.entitySpawnPositionOffsetZField.setVisible(true);
			this.entitySpawnOrientationPitchField.setVisible(true);
			this.entitySpawnOrientationYawField.setVisible(true);

			this.toggleIsPersistentButton.visible = true;
			this.toggleEnableSpawnEffectsButton.visible = true;

		}

		this.scrollPosition = 0;
		this.scrollAmount = 0.0f;
	}

	@Override
	public void resize(Minecraft client, int width, int height) {
		List<MutablePair<String, MutablePair<BlockPos, Boolean>>> list1 = new ArrayList<>(this.bossTriggeredBlocksList);
		ScreenPage var = this.screenPage;
		boolean bool = this.noPlayersAroundTriggeredBlockResets;
		boolean bool1 = this.newBossTriggeredBlockResets;
		boolean bool2 = this.isPersistent;
		boolean bool3 = this.enableSpawnEffects;

		String string3 = this.uuidListHandlerBlockPositionOffsetXField.getValue();
		String string4 = this.uuidListHandlerBlockPositionOffsetYField.getValue();
		String string5 = this.uuidListHandlerBlockPositionOffsetZField.getValue();

		String string6 = this.bossIdentifierField.getValue();
		String string7 = this.entitySpawnPositionOffsetXField.getValue();
		String string8 = this.entitySpawnPositionOffsetYField.getValue();
		String string9 = this.entitySpawnPositionOffsetZField.getValue();
		String string10 = this.entitySpawnOrientationPitchField.getValue();
		String string11 = this.entitySpawnOrientationYawField.getValue();

		String string12 = this.newBossTriggeredBlockIdentifierField.getValue();
		String string13 = this.newBossTriggeredBlockPositionOffsetXField.getValue();
		String string14 = this.newBossTriggeredBlockPositionOffsetYField.getValue();
		String string15 = this.newBossTriggeredBlockPositionOffsetZField.getValue();

		String string16 = this.noPlayersAroundTriggeredBlockPositionOffsetXField.getValue();
		String string17 = this.noPlayersAroundTriggeredBlockPositionOffsetYField.getValue();
		String string18 = this.noPlayersAroundTriggeredBlockPositionOffsetZField.getValue();

		this.init(client, width, height);

		this.bossTriggeredBlocksList.clear();
		this.bossTriggeredBlocksList.addAll(list1);

		this.screenPage = var;
		this.cycleScreenPageButton.setValue(this.screenPage);
		this.noPlayersAroundTriggeredBlockResets = bool;
		this.toggleNoPlayersAroundTriggeredBlockResetsButton.setValue(this.noPlayersAroundTriggeredBlockResets);
		this.newBossTriggeredBlockResets = bool1;
		this.toggleNewBossTriggeredBlockResetsButton.setValue(this.newBossTriggeredBlockResets);
		this.isPersistent = bool2;
		this.toggleIsPersistentButton.setValue(this.isPersistent);
		this.enableSpawnEffects = bool3;
		this.toggleEnableSpawnEffectsButton.setValue(this.enableSpawnEffects);

		this.uuidListHandlerBlockPositionOffsetXField.setValue(string3);
		this.uuidListHandlerBlockPositionOffsetYField.setValue(string4);
		this.uuidListHandlerBlockPositionOffsetZField.setValue(string5);

		this.bossIdentifierField.setValue(string6);
		this.entitySpawnPositionOffsetXField.setValue(string7);
		this.entitySpawnPositionOffsetYField.setValue(string8);
		this.entitySpawnPositionOffsetZField.setValue(string9);
		this.entitySpawnOrientationPitchField.setValue(string10);
		this.entitySpawnOrientationYawField.setValue(string11);

		this.newBossTriggeredBlockIdentifierField.setValue(string12);
		this.newBossTriggeredBlockPositionOffsetXField.setValue(string13);
		this.newBossTriggeredBlockPositionOffsetYField.setValue(string14);
		this.newBossTriggeredBlockPositionOffsetZField.setValue(string15);

		this.noPlayersAroundTriggeredBlockPositionOffsetXField.setValue(string16);
		this.noPlayersAroundTriggeredBlockPositionOffsetYField.setValue(string17);
		this.noPlayersAroundTriggeredBlockPositionOffsetZField.setValue(string18);

		this.updateWidgets();
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (this.screenPage == ScreenPage.BOSS_TRIGGERED_BLOCKS
				&& this.bossTriggeredBlocksList.size() > 4) {
			int i = this.width / 2 - 153;
			int j = 43;
			if (mouseX >= (double) i && mouseX < (double) (i + 6) && mouseY >= (double) j && mouseY < (double) (j + 94)) {
				this.mouseClicked = true;
			}
		}
		return super.mouseClicked(mouseX, mouseY, button);
	}

	@Override
	public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
		if (this.screenPage == ScreenPage.BOSS_TRIGGERED_BLOCKS
				&& this.bossTriggeredBlocksList.size() > 4
				&& this.mouseClicked) {
			int i = this.bossTriggeredBlocksList.size() - 4;
			float f = (float) deltaY / (float) i;
			this.scrollAmount = Mth.clamp(this.scrollAmount + f, 0.0f, 1.0f);
			this.scrollPosition = (int) ((double) (this.scrollAmount * (float) i));
		}
		return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
		if (this.screenPage == ScreenPage.BOSS_TRIGGERED_BLOCKS
				&& this.bossTriggeredBlocksList.size() > 4
				&& mouseX >= (double) (this.width / 2 - 154) && mouseX <= (double) (this.width / 2 + 50)
				&& mouseY >= (double) (42) && mouseY <= (double) (138)) {
			int i = this.bossTriggeredBlocksList.size() - 4;
			float f = (float) verticalAmount / (float) i;
			this.scrollAmount = Mth.clamp(this.scrollAmount - f, 0.0f, 1.0f);
			this.scrollPosition = (int) ((double) (this.scrollAmount * (float) i));
		}
		return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
	}

	@Override
	public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {

		super.render(context, mouseX, mouseY, delta);

		if (this.screenPage == ScreenPage.PLAYERS) {
			context.drawString(this.font, UUID_LIST_PROVIDER_POSITION_OFFSET_LABEL_TEXT, this.width / 2 - 153, 114, 0xA0A0A0);
			this.uuidListHandlerBlockPositionOffsetXField.render(context, mouseX, mouseY, delta);
			this.uuidListHandlerBlockPositionOffsetYField.render(context, mouseX, mouseY, delta);
			this.uuidListHandlerBlockPositionOffsetZField.render(context, mouseX, mouseY, delta);
			context.drawString(this.font, NO_PLAYERS_AROUND_TRIGGERED_BLOCK_POSITION_OFFET_LABEL_TEXT, this.width / 2 - 153, 149, 0xA0A0A0);
			this.noPlayersAroundTriggeredBlockPositionOffsetXField.render(context, mouseX, mouseY, delta);
			this.noPlayersAroundTriggeredBlockPositionOffsetYField.render(context, mouseX, mouseY, delta);
			this.noPlayersAroundTriggeredBlockPositionOffsetZField.render(context, mouseX, mouseY, delta);
		} else if (this.screenPage == ScreenPage.SPAWN_POSITION) {
			context.drawString(this.font, BOSS_IDENTIFIER_LABEL_TEXT, this.width / 2 - 153, 45, 0xA0A0A0);
			this.bossIdentifierField.render(context, mouseX, mouseY, delta);

			context.drawString(this.font, ENTITY_SPAWN_POSITION_OFFSET_LABEL_TEXT, this.width / 2 - 153, 80, 0xA0A0A0);
			this.entitySpawnPositionOffsetXField.render(context, mouseX, mouseY, delta);
			this.entitySpawnPositionOffsetYField.render(context, mouseX, mouseY, delta);
			this.entitySpawnPositionOffsetZField.render(context, mouseX, mouseY, delta);

			context.drawString(this.font, ENTITY_SPAWN_ORIENTATION_PITCH_LABEL_TEXT, this.width / 2 - 153, 115, 0xA0A0A0);
			this.entitySpawnOrientationPitchField.render(context, mouseX, mouseY, delta);
			context.drawString(this.font, ENTITY_SPAWN_ORIENTATION_YAW_LABEL_TEXT, this.width / 2 + 5, 115, 0xA0A0A0);
			this.entitySpawnOrientationYawField.render(context, mouseX, mouseY, delta);
			context.drawString(this.font, IS_PERSISTENT_LABEL_TEXT, this.width / 2 - 152, 150, 0xA0A0A0);
			context.drawString(this.font, ENABLE_SPAWN_EFFECTS_LABEL_TEXT, this.width / 2 + 6, 150, 0xA0A0A0);
		} else if (this.screenPage == ScreenPage.BOSS_TRIGGERED_BLOCKS) {
			int x = this.bossTriggeredBlocksList.size() > 4 ? this.width / 2 - 142 : this.width / 2 - 153;
			for (int i = this.scrollPosition; i < Math.min(this.scrollPosition + 4, this.bossTriggeredBlocksList.size()); i++) {
				context.drawString(this.font, this.bossTriggeredBlocksList.get(i).getLeft() + ": " + this.bossTriggeredBlocksList.get(i).getRight().toString(), x, 48 + ((i - this.scrollPosition) * 24), 0xA0A0A0);
			}
			if (this.bossTriggeredBlocksList.size() > 4) {
				context.blit(SCROLL_BAR_BACKGROUND_8_96_TEXTURE, this.width / 2 - 154, 42, 0, 0, 8, 96);
				int k = (int) (85.0f * this.scrollAmount);
				context.blit(SCROLLER_VERTICAL_6_7_TEXTURE, this.width / 2 - 153, 42 + 1 + k, 0, 0, 6, 7);
			}
			this.newBossTriggeredBlockIdentifierField.render(context, mouseX, mouseY, delta);
			this.newBossTriggeredBlockPositionOffsetXField.render(context, mouseX, mouseY, delta);
			this.newBossTriggeredBlockPositionOffsetYField.render(context, mouseX, mouseY, delta);
			this.newBossTriggeredBlockPositionOffsetZField.render(context, mouseX, mouseY, delta);
		}
	}

	@Override
	protected boolean updateBlock() {
		ClientPlayNetworking.send(new UpdateBossControllerBlockPacket(
				this.bossControllerBlock.getPos(),
				new BlockPos(
						ItemUtils.parseInt(this.uuidListHandlerBlockPositionOffsetXField.getValue()),
						ItemUtils.parseInt(this.uuidListHandlerBlockPositionOffsetYField.getValue()),
						ItemUtils.parseInt(this.uuidListHandlerBlockPositionOffsetZField.getValue())
				),
				new BlockPos(
						ItemUtils.parseInt(this.noPlayersAroundTriggeredBlockPositionOffsetXField.getValue()),
						ItemUtils.parseInt(this.noPlayersAroundTriggeredBlockPositionOffsetYField.getValue()),
						ItemUtils.parseInt(this.noPlayersAroundTriggeredBlockPositionOffsetZField.getValue())
				),
				this.noPlayersAroundTriggeredBlockResets,
				this.bossIdentifierField.getValue(),
				new BlockPos(
						ItemUtils.parseInt(this.entitySpawnPositionOffsetXField.getValue()),
						ItemUtils.parseInt(this.entitySpawnPositionOffsetYField.getValue()),
						ItemUtils.parseInt(this.entitySpawnPositionOffsetZField.getValue())
				),
				ItemUtils.parseDouble(this.entitySpawnOrientationPitchField.getValue()),
				ItemUtils.parseDouble(this.entitySpawnOrientationYawField.getValue()),
				this.isPersistent,
				this.enableSpawnEffects,
				this.bossTriggeredBlocksList
		));
		return true;
	}

	public enum ScreenPage implements StringRepresentable {
		PLAYERS("players"),
		BOSS_TRIGGERED_BLOCKS("boss_triggered_blocks"),
		SPAWN_POSITION("spawn_position");

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
			return Component.translatable("gui.boss_controller_block.screen_page." + this.name);
		}
	}
}
