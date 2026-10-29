package com.github.theredbrain.scriptblocks.gui.screen.ingame;

import com.github.theredbrain.scriptblocks.ScriptBlocks;
import com.github.theredbrain.scriptblocks.block.entity.TriggeredSpawnerBlockEntity;
import com.github.theredbrain.scriptblocks.network.packet.UpdateTriggeredSpawnerBlockPacket;
import com.github.theredbrain.scriptblocks.util.ItemUtils;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.GameNarrator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.level.storage.LevelData;
import org.apache.commons.lang3.tuple.MutablePair;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Environment(value = EnvType.CLIENT)
public class TriggeredSpawnerBlockScreen extends AbstractCreativeBlockSettingsScreen {
	private static final int VISIBLE_LIST_ENTRIES = 3;
	private static final Component ENTITY_SPAWN_POSITION_OFFSET_LABEL_TEXT = Component.translatable("gui.triggered_spawner_block.entity_spawn_position_offset_label");
	private static final Component ENTITY_SPAWN_ORIENTATION_PITCH_LABEL_TEXT = Component.translatable("gui.triggered_spawner_block.entity_spawn_orientation_pitch_label");
	private static final Component ENTITY_SPAWN_ORIENTATION_YAW_LABEL_TEXT = Component.translatable("gui.triggered_spawner_block.entity_spawn_orientation_yaw_label");
	private static final Component SPAWNING_MODE_LABEL_TEXT = Component.translatable("gui.triggered_spawner_block.spawning_mode_label");
	private static final Component IS_PERSISTENT_LABEL_TEXT = Component.translatable("gui.triggered_spawner_block.is_persistent_label");
	private static final Component ENABLE_SPAWN_EFFECTS_LABEL_TEXT = Component.translatable("gui.triggered_spawner_block.enable_spawn_effects_label");
	private static final Component TOGGLE_ENABLE_SPAWN_EFFECTS_BUTTON_ON_LABEL_TEXT = Component.translatable("gui.triggered_spawner_block.toggle_enable_spawn_effects_button_label.on");
	private static final Component TOGGLE_ENABLE_SPAWN_EFFECTS_BUTTON_OFF_LABEL_TEXT = Component.translatable("gui.triggered_spawner_block.toggle_enable_spawn_effects_button_label.off");
	private static final Component ENTITY_TYPE_LABEL_TEXT = Component.translatable("gui.triggered_spawner_block.entity_type_label");
	private static final Component ENTITY_ATTRIBUTE_IDENTIFIER_FIELD_PLACEHOLDER_TEXT = Component.translatable("gui.triggered_spawner_block.entity_attribute_identifier_label");
	private static final Component ENTITY_ATTRIBUTE_MODIFIER_IDENTIFIER_FIELD_PLACEHOLDER_TEXT = Component.translatable("gui.triggered_spawner_block.entity_attribute_modifier_identifier_label");
	private static final Component ENTITY_ATTRIBUTE_MODIFIER_VALUE_FIELD_PLACEHOLDER_TEXT = Component.translatable("gui.triggered_spawner_block.entity_attribute_modifier_value_label");
	private static final Component USE_RELAY_BLOCK_POSITION_OFFSET_LABEL_TEXT = Component.translatable("gui.use_relay_block.relay_block_position_offset");
	private static final Component TRIGGERED_BLOCK_POSITION_OFFSET_LABEL_TEXT = Component.translatable("gui.triggered_block.triggered_block_position_offset");

	private static final Identifier SCROLL_BAR_BACKGROUND_8_92_TEXTURE = ScriptBlocks.identifier("scroll_bar/scroll_bar_background_8_92");
	private static final Identifier SCROLLER_VERTICAL_6_7_TEXTURE = ScriptBlocks.identifier("scroll_bar/scroller_vertical_6_7");
	public static final WidgetSprites REMOVE_ENTRY_BUTTON_TEXTURES = new WidgetSprites(
			Identifier.fromNamespaceAndPath(ScriptBlocks.MOD_ID, "widgets/remove_entry_button"), Identifier.fromNamespaceAndPath(ScriptBlocks.MOD_ID, "widgets/remove_entry_button_highlighted")
	);

	private final TriggeredSpawnerBlockEntity triggeredSpawnerBlock;

	private CycleButton<ScreenPage> cycleScreenPageButton;
	private EditBox entitySpawnPositionOffsetXField;
	private EditBox entitySpawnPositionOffsetYField;
	private EditBox entitySpawnPositionOffsetZField;
	private EditBox entitySpawnOrientationPitchField;
	private EditBox entitySpawnOrientationYawField;
	private CycleButton<TriggeredSpawnerBlockEntity.SpawningMode> cycleSpawningModeButton;
	private TriggeredSpawnerBlockEntity.SpawningMode spawningMode;

	private CycleButton<Boolean> toggleIsPersistentButton;
	private boolean isPersistent;
	private CycleButton<Boolean> toggleEnableSpawnEffectsButton;
	private boolean enableSpawnEffects;

	private EditBox entityTypeIdField;

	private Button removeEntryButton0;
	private Button removeEntryButton1;
	private Button removeEntryButton2;
	private EditBox entityAttributeIdentifierField;
	private EditBox entityAttributeModifierIdentifierField;
	private EditBox entityAttributeModifierValueField;
	private CycleButton<TriggeredSpawnerBlockEntity.VisualEntityAttributeModifierOperation> cycleEntityAttributeModifierOperationButton;
	private Button addNewEntityAttributeModifierButton;

	private EditBox useRelayBlockPositionOffsetXField;
	private EditBox useRelayBlockPositionOffsetYField;
	private EditBox useRelayBlockPositionOffsetZField;

	private EditBox triggeredBlockPositionOffsetXField;
	private EditBox triggeredBlockPositionOffsetYField;
	private EditBox triggeredBlockPositionOffsetZField;
	private CycleButton<Boolean> toggleTriggeredBlockResetsButton;
	private boolean triggeredBlockResets;

	private final List<MutablePair<Identifier, AttributeModifier>> entityAttributeModifiersList = new ArrayList<>();
	private ScreenPage screenPage;
	private TriggeredSpawnerBlockEntity.VisualEntityAttributeModifierOperation newEntityAttributeModifierOperation;
	private int scrollPosition = 0;
	private float scrollAmount = 0.0f;
	private boolean mouseClicked = false;

	public TriggeredSpawnerBlockScreen(TriggeredSpawnerBlockEntity triggeredSpawnerBlock) {
		super(GameNarrator.NO_TITLE);
		this.triggeredSpawnerBlock = triggeredSpawnerBlock;
		this.screenPage = ScreenPage.MISC;
		this.newEntityAttributeModifierOperation = TriggeredSpawnerBlockEntity.VisualEntityAttributeModifierOperation.ADD_VALUE;
	}

	private void removeEntry(int index) {
		if (index + this.scrollPosition < this.entityAttributeModifiersList.size()) {
			this.entityAttributeModifiersList.remove(index + this.scrollPosition);
		}
		updateWidgets();
	}

	private void addNewEntityAttributeModifier() {
		// TODO check for existing entries and validate fields
		this.entityAttributeModifiersList.add(new MutablePair<>(
				Identifier.parse(this.entityAttributeIdentifierField.getValue()),
				new AttributeModifier(
						Identifier.parse(this.entityAttributeModifierIdentifierField.getValue()),
						ItemUtils.parseDouble(this.entityAttributeModifierValueField.getValue()),
						this.newEntityAttributeModifierOperation.getOperation()
				)
		));
		this.scrollPosition = 0;
		this.scrollAmount = 0.0f;
		updateWidgets();
	}

	@Override
	protected void init() {
		this.entityAttributeModifiersList.clear();
		Multimap<Holder<Attribute>, AttributeModifier> entityAttributeModifiers = Multimaps.newMultimap(Maps.newLinkedHashMap(), ArrayList::new);
		entityAttributeModifiers.putAll(this.triggeredSpawnerBlock.getEntityAttributeModifiers());
		List<Holder<Attribute>> entityAttributeModifiersKeys = new ArrayList<>(entityAttributeModifiers.keySet());
		for (Holder<Attribute> key : entityAttributeModifiersKeys) {
			Collection<AttributeModifier> modifierCollection = entityAttributeModifiers.get(key);
			List<AttributeModifier> modifierList = modifierCollection.stream().toList();
			for (AttributeModifier entityAttributeModifier : modifierList) {
				this.entityAttributeModifiersList.add(new MutablePair<>(BuiltInRegistries.ATTRIBUTE.getKey(key.value()), entityAttributeModifier));
			}
		}

		this.cycleScreenPageButton = this.addRenderableWidget(CycleButton.builder(ScreenPage::asText).values((ScreenPage[]) ScreenPage.values()).initially(this.screenPage).omitKeyText().build(this.width / 2 - 154, 20, 308, 20, Component.empty(), (button, screenPage) -> {
			this.screenPage = screenPage;
			this.updateWidgets();
		}));

		// --- misc page ---

		this.entitySpawnPositionOffsetXField = new EditBox(this.font, this.width / 2 - 154, 60, 100, 20, Component.empty());
		this.entitySpawnPositionOffsetXField.setValue(Integer.toString(this.triggeredSpawnerBlock.getSpawnPoint().pos().getX()));
		this.addWidget(this.entitySpawnPositionOffsetXField);
		this.entitySpawnPositionOffsetYField = new EditBox(this.font, this.width / 2 - 50, 60, 100, 20, Component.empty());
		this.entitySpawnPositionOffsetYField.setValue(Integer.toString(this.triggeredSpawnerBlock.getSpawnPoint().pos().getY()));
		this.addWidget(this.entitySpawnPositionOffsetYField);
		this.entitySpawnPositionOffsetZField = new EditBox(this.font, this.width / 2 + 54, 60, 100, 20, Component.empty());
		this.entitySpawnPositionOffsetZField.setValue(Integer.toString(this.triggeredSpawnerBlock.getSpawnPoint().pos().getZ()));
		this.addWidget(this.entitySpawnPositionOffsetZField);

		this.entitySpawnOrientationPitchField = new EditBox(this.font, this.width / 2 - 154, 95, 150, 20, Component.empty());
		this.entitySpawnOrientationPitchField.setValue(Double.toString(this.triggeredSpawnerBlock.getSpawnPoint().pitch()));
		this.addWidget(this.entitySpawnOrientationPitchField);
		this.entitySpawnOrientationYawField = new EditBox(this.font, this.width / 2 + 4, 95, 150, 20, Component.empty());
		this.entitySpawnOrientationYawField.setValue(Double.toString(this.triggeredSpawnerBlock.getSpawnPoint().yaw()));
		this.addWidget(this.entitySpawnOrientationYawField);

		this.spawningMode = this.triggeredSpawnerBlock.getSpawningMode();
		this.cycleSpawningModeButton = this.addRenderableWidget(CycleButton.builder(TriggeredSpawnerBlockEntity.SpawningMode::asText).values(TriggeredSpawnerBlockEntity.SpawningMode.values()).initially(this.spawningMode).omitKeyText().build(this.width / 2 - 154, 130, 100, 20, Component.empty(), (button, spawningMode) -> {
			this.spawningMode = spawningMode;
		}));

		this.isPersistent = this.triggeredSpawnerBlock.isPersistent();
		this.toggleIsPersistentButton = this.addRenderableWidget(CycleButton.onOffBuilder().initially(this.isPersistent).omitKeyText().build(this.width / 2 - 50, 130, 100, 20, Component.empty(), (button, isPersistent) -> {
			this.isPersistent = isPersistent;
		}));

		this.enableSpawnEffects = this.triggeredSpawnerBlock.enableSpawnEffects();
		this.toggleEnableSpawnEffectsButton = this.addRenderableWidget(CycleButton.booleanBuilder(TOGGLE_ENABLE_SPAWN_EFFECTS_BUTTON_ON_LABEL_TEXT, TOGGLE_ENABLE_SPAWN_EFFECTS_BUTTON_OFF_LABEL_TEXT).initially(this.enableSpawnEffects).omitKeyText().build(this.width / 2 + 54, 130, 100, 20, Component.empty(), (button, enableSpawnEffects) -> {
			this.enableSpawnEffects = enableSpawnEffects;
		}));

		this.entityTypeIdField = new EditBox(this.font, this.width / 2 - 154, 165, 308, 20, Component.empty());
		this.entityTypeIdField.setMaxLength(128);
		this.entityTypeIdField.setValue(this.triggeredSpawnerBlock.getEntityTypeId());
		this.addWidget(this.entityTypeIdField);

		// --- entity attribute modifier page ---

		this.removeEntryButton0 = this.addRenderableWidget(new ImageButton(this.width / 2 - 141, 47, 20, 20, REMOVE_ENTRY_BUTTON_TEXTURES, button -> this.removeEntry(0)));
		this.removeEntryButton1 = this.addRenderableWidget(new ImageButton(this.width / 2 - 141, 81, 20, 20, REMOVE_ENTRY_BUTTON_TEXTURES, button -> this.removeEntry(1)));
		this.removeEntryButton2 = this.addRenderableWidget(new ImageButton(this.width / 2 - 141, 115, 20, 20, REMOVE_ENTRY_BUTTON_TEXTURES, button -> this.removeEntry(2)));
		this.entityAttributeIdentifierField = new EditBox(this.font, this.width / 2 - 154, 140, 308, 20, Component.empty());
		this.entityAttributeIdentifierField.setHint(ENTITY_ATTRIBUTE_IDENTIFIER_FIELD_PLACEHOLDER_TEXT);
		this.addWidget(this.entityAttributeIdentifierField);
		this.entityAttributeModifierIdentifierField = new EditBox(this.font, this.width / 2 - 154, 164, 308, 20, Component.empty());
		this.entityAttributeModifierIdentifierField.setHint(ENTITY_ATTRIBUTE_MODIFIER_IDENTIFIER_FIELD_PLACEHOLDER_TEXT);
		this.addWidget(this.entityAttributeModifierIdentifierField);
		this.entityAttributeModifierValueField = new EditBox(this.font, this.width / 2 - 154, 188, 100, 20, Component.empty());
		this.entityAttributeModifierValueField.setHint(ENTITY_ATTRIBUTE_MODIFIER_VALUE_FIELD_PLACEHOLDER_TEXT);
		this.addWidget(this.entityAttributeModifierValueField);
		this.cycleEntityAttributeModifierOperationButton = this.addRenderableWidget(CycleButton.builder(TriggeredSpawnerBlockEntity.VisualEntityAttributeModifierOperation::asText).values(TriggeredSpawnerBlockEntity.VisualEntityAttributeModifierOperation.values()).initially(this.newEntityAttributeModifierOperation).omitKeyText().build(this.width / 2 - 50, 188, 100, 20, Component.empty(), (button, newEntityAttributeModifierOperationButton) -> {
			this.newEntityAttributeModifierOperation = newEntityAttributeModifierOperationButton;
			this.updateWidgets();
		}));
		this.addNewEntityAttributeModifierButton = this.addRenderableWidget(Button.builder(Component.translatable("gui.list_entry.add"), button -> this.addNewEntityAttributeModifier()).bounds(this.width / 2 + 54, 188, 100, 20).build());

		// --- triggered block page ---

		this.useRelayBlockPositionOffsetXField = new EditBox(this.font, this.width / 2 - 154, 95, 100, 20, Component.empty());
		this.useRelayBlockPositionOffsetXField.setMaxLength(128);
		this.useRelayBlockPositionOffsetXField.setValue(Integer.toString(this.triggeredSpawnerBlock.getUseRelayBlockPositionOffset().getX()));
		this.addWidget(this.useRelayBlockPositionOffsetXField);
		this.useRelayBlockPositionOffsetYField = new EditBox(this.font, this.width / 2 - 50, 95, 100, 20, Component.empty());
		this.useRelayBlockPositionOffsetYField.setMaxLength(128);
		this.useRelayBlockPositionOffsetYField.setValue(Integer.toString(this.triggeredSpawnerBlock.getUseRelayBlockPositionOffset().getY()));
		this.addWidget(this.useRelayBlockPositionOffsetYField);
		this.useRelayBlockPositionOffsetZField = new EditBox(this.font, this.width / 2 + 54, 95, 100, 20, Component.empty());
		this.useRelayBlockPositionOffsetZField.setMaxLength(128);
		this.useRelayBlockPositionOffsetZField.setValue(Integer.toString(this.triggeredSpawnerBlock.getUseRelayBlockPositionOffset().getZ()));
		this.addWidget(this.useRelayBlockPositionOffsetZField);

		// --- triggered block page ---

		this.triggeredBlockPositionOffsetXField = new EditBox(this.font, this.width / 2 - 154, 95, 50, 20, Component.empty());
		this.triggeredBlockPositionOffsetXField.setMaxLength(128);
		this.triggeredBlockPositionOffsetXField.setValue(Integer.toString(this.triggeredSpawnerBlock.getTriggeredBlock().getLeft().getX()));
		this.addWidget(this.triggeredBlockPositionOffsetXField);
		this.triggeredBlockPositionOffsetYField = new EditBox(this.font, this.width / 2 - 100, 95, 50, 20, Component.empty());
		this.triggeredBlockPositionOffsetYField.setMaxLength(128);
		this.triggeredBlockPositionOffsetYField.setValue(Integer.toString(this.triggeredSpawnerBlock.getTriggeredBlock().getLeft().getY()));
		this.addWidget(this.triggeredBlockPositionOffsetYField);
		this.triggeredBlockPositionOffsetZField = new EditBox(this.font, this.width / 2 - 46, 95, 50, 20, Component.empty());
		this.triggeredBlockPositionOffsetZField.setMaxLength(128);
		this.triggeredBlockPositionOffsetZField.setValue(Integer.toString(this.triggeredSpawnerBlock.getTriggeredBlock().getLeft().getZ()));
		this.addWidget(this.triggeredBlockPositionOffsetZField);
		this.triggeredBlockResets = this.triggeredSpawnerBlock.getTriggeredBlock().getRight();
		this.toggleTriggeredBlockResetsButton = this.addRenderableWidget(CycleButton.booleanBuilder(Component.translatable("gui.triggered_block.toggle_triggered_block_resets_button_label.on"), Component.translatable("gui.triggered_block.toggle_triggered_block_resets_button_label.off")).initially(this.triggeredBlockResets).omitKeyText().build(this.width / 2 + 8, 95, 150, 20, Component.empty(), (button, triggeredBlockResets) -> {
			this.triggeredBlockResets = triggeredBlockResets;
		}));

		this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> this.done()).bounds(this.width / 2 - 4 - 150, 212, 150, 20).build());
		this.addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL, button -> this.cancel()).bounds(this.width / 2 + 4, 212, 150, 20).build());
		this.updateWidgets();
	}

	@Override
	protected void setInitialFocus() {
		this.setInitialFocus(this.cycleScreenPageButton);
	}

	@Override
	protected void updateWidgets() {

		this.entitySpawnPositionOffsetXField.setVisible(false);
		this.entitySpawnPositionOffsetYField.setVisible(false);
		this.entitySpawnPositionOffsetZField.setVisible(false);
		this.entitySpawnOrientationYawField.setVisible(false);
		this.entitySpawnOrientationPitchField.setVisible(false);

		this.cycleSpawningModeButton.visible = false;

		this.toggleIsPersistentButton.visible = false;
		this.toggleEnableSpawnEffectsButton.visible = false;

		this.entityTypeIdField.setVisible(false);

		this.removeEntryButton0.visible = false;
		this.removeEntryButton1.visible = false;
		this.removeEntryButton2.visible = false;
		this.entityAttributeIdentifierField.setVisible(false);
		this.entityAttributeModifierIdentifierField.setVisible(false);
		this.entityAttributeModifierValueField.setVisible(false);
		this.cycleEntityAttributeModifierOperationButton.visible = false;
		this.addNewEntityAttributeModifierButton.visible = false;

		this.useRelayBlockPositionOffsetXField.setVisible(false);
		this.useRelayBlockPositionOffsetYField.setVisible(false);
		this.useRelayBlockPositionOffsetZField.setVisible(false);

		this.triggeredBlockPositionOffsetXField.setVisible(false);
		this.triggeredBlockPositionOffsetYField.setVisible(false);
		this.triggeredBlockPositionOffsetZField.setVisible(false);
		this.toggleTriggeredBlockResetsButton.visible = false;

		if (this.screenPage == ScreenPage.MISC) {

			this.entitySpawnPositionOffsetXField.setVisible(true);
			this.entitySpawnPositionOffsetYField.setVisible(true);
			this.entitySpawnPositionOffsetZField.setVisible(true);
			this.entitySpawnOrientationYawField.setVisible(true);
			this.entitySpawnOrientationPitchField.setVisible(true);

			this.cycleSpawningModeButton.visible = true;

			this.toggleIsPersistentButton.visible = true;
			this.toggleEnableSpawnEffectsButton.visible = true;

			this.entityTypeIdField.setVisible(true);

		} else if (this.screenPage == ScreenPage.ENTITY_ATTRIBUTE_MODIFIER) {

			int index = 0;
			for (int i = 0; i < Math.min(3, this.entityAttributeModifiersList.size()); i++) {
				if (index == 0) {
					this.removeEntryButton0.visible = true;
				} else if (index == 1) {
					this.removeEntryButton1.visible = true;
				} else if (index == 2) {
					this.removeEntryButton2.visible = true;
				}
				index++;
			}

			this.entityAttributeIdentifierField.setVisible(true);
			this.entityAttributeModifierIdentifierField.setVisible(true);
			this.entityAttributeModifierValueField.setVisible(true);
			this.cycleEntityAttributeModifierOperationButton.visible = true;
			this.addNewEntityAttributeModifierButton.visible = true;

		} else if (this.screenPage == ScreenPage.USE_RELAY_BLOCK) {

			this.useRelayBlockPositionOffsetXField.setVisible(true);
			this.useRelayBlockPositionOffsetYField.setVisible(true);
			this.useRelayBlockPositionOffsetZField.setVisible(true);

		} else if (this.screenPage == ScreenPage.TRIGGERED_BLOCK) {

			this.triggeredBlockPositionOffsetXField.setVisible(true);
			this.triggeredBlockPositionOffsetYField.setVisible(true);
			this.triggeredBlockPositionOffsetZField.setVisible(true);
			this.toggleTriggeredBlockResetsButton.visible = true;

		}

	}

	@Override
	public void resize(Minecraft client, int width, int height) {
		ScreenPage var = this.screenPage;
		TriggeredSpawnerBlockEntity.SpawningMode var1 = this.spawningMode;
		TriggeredSpawnerBlockEntity.VisualEntityAttributeModifierOperation var2 = this.newEntityAttributeModifierOperation;
		List<MutablePair<Identifier, AttributeModifier>> list = new ArrayList<>(this.entityAttributeModifiersList);
		String string = this.entitySpawnPositionOffsetXField.getValue();
		String string1 = this.entitySpawnPositionOffsetYField.getValue();
		String string2 = this.entitySpawnPositionOffsetZField.getValue();
		String string3 = this.entitySpawnOrientationPitchField.getValue();
		String string4 = this.entitySpawnOrientationYawField.getValue();
		String string5 = this.entityTypeIdField.getValue();
		String string6 = this.entityAttributeIdentifierField.getValue();
		String string7 = this.entityAttributeModifierIdentifierField.getValue();
		String string8 = this.entityAttributeModifierValueField.getValue();
		String string9 = this.useRelayBlockPositionOffsetXField.getValue();
		String string10 = this.useRelayBlockPositionOffsetYField.getValue();
		String string11 = this.useRelayBlockPositionOffsetZField.getValue();
		String string12 = this.triggeredBlockPositionOffsetXField.getValue();
		String string13 = this.triggeredBlockPositionOffsetYField.getValue();
		String string14 = this.triggeredBlockPositionOffsetZField.getValue();
		boolean bl = this.triggeredBlockResets;
		boolean bl1 = this.isPersistent;
		boolean bl2 = this.enableSpawnEffects;
		this.init(client, width, height);
		this.screenPage = var;
		this.cycleScreenPageButton.setValue(this.screenPage);
		this.spawningMode = var1;
		this.cycleSpawningModeButton.setValue(this.spawningMode);
		this.newEntityAttributeModifierOperation = var2;
		this.cycleEntityAttributeModifierOperationButton.setValue(this.newEntityAttributeModifierOperation);
		this.entityAttributeModifiersList.clear();
		this.entityAttributeModifiersList.addAll(list);
		this.entitySpawnPositionOffsetXField.setValue(string);
		this.entitySpawnPositionOffsetYField.setValue(string1);
		this.entitySpawnPositionOffsetZField.setValue(string2);
		this.entitySpawnOrientationPitchField.setValue(string3);
		this.entitySpawnOrientationYawField.setValue(string4);
		this.entityTypeIdField.setValue(string5);
		this.entityAttributeIdentifierField.setValue(string6);
		this.entityAttributeModifierIdentifierField.setValue(string7);
		this.entityAttributeModifierValueField.setValue(string8);
		this.useRelayBlockPositionOffsetXField.setValue(string9);
		this.useRelayBlockPositionOffsetYField.setValue(string10);
		this.useRelayBlockPositionOffsetZField.setValue(string11);
		this.triggeredBlockPositionOffsetXField.setValue(string12);
		this.triggeredBlockPositionOffsetYField.setValue(string13);
		this.triggeredBlockPositionOffsetZField.setValue(string14);
		this.triggeredBlockResets = bl;
		this.toggleTriggeredBlockResetsButton.setValue(this.triggeredBlockResets);
		this.isPersistent = bl1;
		this.toggleIsPersistentButton.setValue(this.isPersistent);
		this.enableSpawnEffects = bl2;
		this.toggleEnableSpawnEffectsButton.setValue(this.enableSpawnEffects);
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		this.mouseClicked = false;
		if (this.screenPage == ScreenPage.ENTITY_ATTRIBUTE_MODIFIER
				&& this.entityAttributeModifiersList.size() > VISIBLE_LIST_ENTRIES) {
			int i = this.width / 2 - 152;
			int j = 46;
			if (mouseX >= (double) i && mouseX < (double) (i + 6) && mouseY >= (double) j && mouseY < (double) (j + 90)) {
				this.mouseClicked = true;
			}
		}
		return super.mouseClicked(mouseX, mouseY, button);
	}

	@Override
	public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
		if (this.screenPage == ScreenPage.ENTITY_ATTRIBUTE_MODIFIER
				&& this.entityAttributeModifiersList.size() > VISIBLE_LIST_ENTRIES
				&& this.mouseClicked) {
			int i = this.entityAttributeModifiersList.size() - VISIBLE_LIST_ENTRIES;
			float f = (float) deltaY / (float) i;
			this.scrollAmount = Mth.clamp(this.scrollAmount + f, 0.0f, 1.0f);
			this.scrollPosition = (int) ((double) (this.scrollAmount * (float) i));
		}
		return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
		if (this.screenPage == ScreenPage.ENTITY_ATTRIBUTE_MODIFIER
				&& this.entityAttributeModifiersList.size() > VISIBLE_LIST_ENTRIES
				&& mouseX >= (double) (this.width / 2 - 152) && mouseX <= (double) (this.width / 2 + 154)
				&& mouseY >= 45 && mouseY <= 137) {
			int i = this.entityAttributeModifiersList.size() - VISIBLE_LIST_ENTRIES;
			float f = (float) verticalAmount / (float) i;
			this.scrollAmount = Mth.clamp(this.scrollAmount - f, 0.0f, 1.0f);
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
	public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {

		super.render(context, mouseX, mouseY, delta);

		if (this.screenPage == ScreenPage.MISC) {
			context.drawString(this.font, ENTITY_SPAWN_POSITION_OFFSET_LABEL_TEXT, this.width / 2 - 153, 50, 0xA0A0A0);
			this.entitySpawnPositionOffsetXField.render(context, mouseX, mouseY, delta);
			this.entitySpawnPositionOffsetYField.render(context, mouseX, mouseY, delta);
			this.entitySpawnPositionOffsetZField.render(context, mouseX, mouseY, delta);

			context.drawString(this.font, ENTITY_SPAWN_ORIENTATION_PITCH_LABEL_TEXT, this.width / 2 - 153, 85, 0xA0A0A0);
			this.entitySpawnOrientationPitchField.render(context, mouseX, mouseY, delta);
			context.drawString(this.font, ENTITY_SPAWN_ORIENTATION_YAW_LABEL_TEXT, this.width / 2 + 5, 85, 0xA0A0A0);
			this.entitySpawnOrientationYawField.render(context, mouseX, mouseY, delta);

			context.drawString(this.font, SPAWNING_MODE_LABEL_TEXT, this.width / 2 - 153, 120, 0xA0A0A0);
			context.drawString(this.font, IS_PERSISTENT_LABEL_TEXT, this.width / 2 - 49, 120, 0xA0A0A0);
			context.drawString(this.font, ENABLE_SPAWN_EFFECTS_LABEL_TEXT, this.width / 2 + 55, 120, 0xA0A0A0);

			context.drawString(this.font, ENTITY_TYPE_LABEL_TEXT, this.width / 2 - 153, 155, 0xA0A0A0);
			this.entityTypeIdField.render(context, mouseX, mouseY, delta);
		} else if (this.screenPage == ScreenPage.ENTITY_ATTRIBUTE_MODIFIER) {

			for (int i = this.scrollPosition; i < Math.min(this.scrollPosition + VISIBLE_LIST_ENTRIES, this.entityAttributeModifiersList.size()); i++) {
				AttributeModifier entityAttributeModifier = this.entityAttributeModifiersList.get(i).getRight();

				context.drawString(this.font, this.entityAttributeModifiersList.get(i).getLeft().toString(), this.width / 2 - 117, 46 + ((i - this.scrollPosition) * 34), 0xA0A0A0);
				context.drawString(this.font, entityAttributeModifier.amount() + ", " + entityAttributeModifier.operation(), this.width / 2 - 117, 59 + ((i - this.scrollPosition) * 34), 0xA0A0A0);
			}
			if (this.entityAttributeModifiersList.size() > VISIBLE_LIST_ENTRIES) {
				context.blitSprite(SCROLL_BAR_BACKGROUND_8_92_TEXTURE, this.width / 2 - 153, 45, 8, 92);
				int k = (int) (83.0f * this.scrollAmount);
				context.blitSprite(SCROLLER_VERTICAL_6_7_TEXTURE, this.width / 2 - 152, 45 + 1 + k, 6, 7);
			}

			this.entityAttributeIdentifierField.render(context, mouseX, mouseY, delta);
			this.entityAttributeModifierIdentifierField.render(context, mouseX, mouseY, delta);
			this.entityAttributeModifierValueField.render(context, mouseX, mouseY, delta);

		} else if (this.screenPage == ScreenPage.USE_RELAY_BLOCK) {
			context.drawString(this.font, USE_RELAY_BLOCK_POSITION_OFFSET_LABEL_TEXT, this.width / 2 - 153, 85, 0xA0A0A0);
			this.useRelayBlockPositionOffsetXField.render(context, mouseX, mouseY, delta);
			this.useRelayBlockPositionOffsetYField.render(context, mouseX, mouseY, delta);
			this.useRelayBlockPositionOffsetZField.render(context, mouseX, mouseY, delta);
		} else if (this.screenPage == ScreenPage.TRIGGERED_BLOCK) {
			context.drawString(this.font, TRIGGERED_BLOCK_POSITION_OFFSET_LABEL_TEXT, this.width / 2 - 153, 85, 0xA0A0A0);
			this.triggeredBlockPositionOffsetXField.render(context, mouseX, mouseY, delta);
			this.triggeredBlockPositionOffsetYField.render(context, mouseX, mouseY, delta);
			this.triggeredBlockPositionOffsetZField.render(context, mouseX, mouseY, delta);
		}
	}

	@Override
	protected boolean updateBlock() {
		if (this.triggeredSpawnerBlock.getLevel() == null) {
			return false;
		}
		ClientPlayNetworking.send(
				new UpdateTriggeredSpawnerBlockPacket(
						this.triggeredSpawnerBlock.getBlockPos(),
						LevelData.RespawnData.of(
								this.triggeredSpawnerBlock.getLevel().dimension(),
								new BlockPos(
										ItemUtils.parseInt(this.entitySpawnPositionOffsetXField.getValue()),
										ItemUtils.parseInt(this.entitySpawnPositionOffsetYField.getValue()),
										ItemUtils.parseInt(this.entitySpawnPositionOffsetZField.getValue())
								),
								ItemUtils.parseFloat(this.entitySpawnOrientationPitchField.getValue()),
								ItemUtils.parseFloat(this.entitySpawnOrientationYawField.getValue())
						),
						this.spawningMode.asString(),
						this.isPersistent,
						this.enableSpawnEffects,
						this.entityTypeIdField.getValue(),
						new ArrayList<>(this.entityAttributeModifiersList),
						new BlockPos(
								ItemUtils.parseInt(this.triggeredBlockPositionOffsetXField.getValue()),
								ItemUtils.parseInt(this.triggeredBlockPositionOffsetYField.getValue()),
								ItemUtils.parseInt(this.triggeredBlockPositionOffsetZField.getValue())
						),
						this.triggeredBlockResets,
						new BlockPos(
								ItemUtils.parseInt(this.useRelayBlockPositionOffsetXField.getValue()),
								ItemUtils.parseInt(this.useRelayBlockPositionOffsetYField.getValue()),
								ItemUtils.parseInt(this.useRelayBlockPositionOffsetZField.getValue())
						)
				)
		);
		return true;
	}

	public enum ScreenPage implements StringRepresentable {
		MISC("misc"),
		ENTITY_ATTRIBUTE_MODIFIER("entity_attribute_modifier"),
		USE_RELAY_BLOCK("use_relay_block"),
		TRIGGERED_BLOCK("triggered_block");

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
			return Component.translatable("gui.triggered_spawner_block.screen_page." + this.name);
		}
	}
}
