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
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ButtonTextures;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.CyclingButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.TexturedButtonWidget;
import net.minecraft.client.util.NarratorManager;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
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
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Environment(value = EnvType.CLIENT)
public class TriggeredSpawnerBlockScreen extends AbstractCreativeBlockSettingsScreen {
	private static final int VISIBLE_LIST_ENTRIES = 3;
	private static final Text ENTITY_SPAWN_POSITION_OFFSET_LABEL_TEXT = Text.translatable("gui.triggered_spawner_block.entity_spawn_position_offset_label");
	private static final Text ENTITY_SPAWN_ORIENTATION_PITCH_LABEL_TEXT = Text.translatable("gui.triggered_spawner_block.entity_spawn_orientation_pitch_label");
	private static final Text ENTITY_SPAWN_ORIENTATION_YAW_LABEL_TEXT = Text.translatable("gui.triggered_spawner_block.entity_spawn_orientation_yaw_label");
	private static final Text SPAWNING_MODE_LABEL_TEXT = Text.translatable("gui.triggered_spawner_block.spawning_mode_label");
	private static final Text IS_PERSISTENT_LABEL_TEXT = Text.translatable("gui.triggered_spawner_block.is_persistent_label");
	private static final Text ENABLE_SPAWN_EFFECTS_LABEL_TEXT = Text.translatable("gui.triggered_spawner_block.enable_spawn_effects_label");
	private static final Text TOGGLE_ENABLE_SPAWN_EFFECTS_BUTTON_ON_LABEL_TEXT = Text.translatable("gui.triggered_spawner_block.toggle_enable_spawn_effects_button_label.on");
	private static final Text TOGGLE_ENABLE_SPAWN_EFFECTS_BUTTON_OFF_LABEL_TEXT = Text.translatable("gui.triggered_spawner_block.toggle_enable_spawn_effects_button_label.off");
	private static final Text ENTITY_TYPE_LABEL_TEXT = Text.translatable("gui.triggered_spawner_block.entity_type_label");
	private static final Text ENTITY_ATTRIBUTE_IDENTIFIER_FIELD_PLACEHOLDER_TEXT = Text.translatable("gui.triggered_spawner_block.entity_attribute_identifier_label");
	private static final Text ENTITY_ATTRIBUTE_MODIFIER_IDENTIFIER_FIELD_PLACEHOLDER_TEXT = Text.translatable("gui.triggered_spawner_block.entity_attribute_modifier_identifier_label");
	private static final Text ENTITY_ATTRIBUTE_MODIFIER_VALUE_FIELD_PLACEHOLDER_TEXT = Text.translatable("gui.triggered_spawner_block.entity_attribute_modifier_value_label");
	private static final Text USE_RELAY_BLOCK_POSITION_OFFSET_LABEL_TEXT = Text.translatable("gui.use_relay_block.relay_block_position_offset");
	private static final Text TRIGGERED_BLOCK_POSITION_OFFSET_LABEL_TEXT = Text.translatable("gui.triggered_block.triggered_block_position_offset");

	private static final Identifier SCROLL_BAR_BACKGROUND_8_92_TEXTURE = ScriptBlocks.identifier("scroll_bar/scroll_bar_background_8_92");
	private static final Identifier SCROLLER_VERTICAL_6_7_TEXTURE = ScriptBlocks.identifier("scroll_bar/scroller_vertical_6_7");
	public static final ButtonTextures REMOVE_ENTRY_BUTTON_TEXTURES = new ButtonTextures(
			Identifier.of(ScriptBlocks.MOD_ID, "widgets/remove_entry_button"), Identifier.of(ScriptBlocks.MOD_ID, "widgets/remove_entry_button_highlighted")
	);

	private final TriggeredSpawnerBlockEntity triggeredSpawnerBlock;

	private CyclingButtonWidget<ScreenPage> cycleScreenPageButton;
	private TextFieldWidget entitySpawnPositionOffsetXField;
	private TextFieldWidget entitySpawnPositionOffsetYField;
	private TextFieldWidget entitySpawnPositionOffsetZField;
	private TextFieldWidget entitySpawnOrientationPitchField;
	private TextFieldWidget entitySpawnOrientationYawField;
	private CyclingButtonWidget<TriggeredSpawnerBlockEntity.SpawningMode> cycleSpawningModeButton;
	private TriggeredSpawnerBlockEntity.SpawningMode spawningMode;

	private CyclingButtonWidget<Boolean> toggleIsPersistentButton;
	private boolean isPersistent;
	private CyclingButtonWidget<Boolean> toggleEnableSpawnEffectsButton;
	private boolean enableSpawnEffects;

	private TextFieldWidget entityTypeIdField;

	private ButtonWidget removeEntryButton0;
	private ButtonWidget removeEntryButton1;
	private ButtonWidget removeEntryButton2;
	private TextFieldWidget entityAttributeIdentifierField;
	private TextFieldWidget entityAttributeModifierIdentifierField;
	private TextFieldWidget entityAttributeModifierValueField;
	private CyclingButtonWidget<TriggeredSpawnerBlockEntity.VisualEntityAttributeModifierOperation> cycleEntityAttributeModifierOperationButton;
	private ButtonWidget addNewEntityAttributeModifierButton;

	private TextFieldWidget useRelayBlockPositionOffsetXField;
	private TextFieldWidget useRelayBlockPositionOffsetYField;
	private TextFieldWidget useRelayBlockPositionOffsetZField;

	private TextFieldWidget triggeredBlockPositionOffsetXField;
	private TextFieldWidget triggeredBlockPositionOffsetYField;
	private TextFieldWidget triggeredBlockPositionOffsetZField;
	private CyclingButtonWidget<Boolean> toggleTriggeredBlockResetsButton;
	private boolean triggeredBlockResets;

	private final List<MutablePair<Identifier, EntityAttributeModifier>> entityAttributeModifiersList = new ArrayList<>();
	private ScreenPage screenPage;
	private TriggeredSpawnerBlockEntity.VisualEntityAttributeModifierOperation newEntityAttributeModifierOperation;
	private int scrollPosition = 0;
	private float scrollAmount = 0.0f;
	private boolean mouseClicked = false;

	public TriggeredSpawnerBlockScreen(TriggeredSpawnerBlockEntity triggeredSpawnerBlock) {
		super(NarratorManager.EMPTY);
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
				Identifier.of(this.entityAttributeIdentifierField.getText()),
				new EntityAttributeModifier(
						Identifier.of(this.entityAttributeModifierIdentifierField.getText()),
						ItemUtils.parseDouble(this.entityAttributeModifierValueField.getText()),
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
		Multimap<RegistryEntry<EntityAttribute>, EntityAttributeModifier> entityAttributeModifiers = Multimaps.newMultimap(Maps.newLinkedHashMap(), ArrayList::new);
		entityAttributeModifiers.putAll(this.triggeredSpawnerBlock.getEntityAttributeModifiers());
		List<RegistryEntry<EntityAttribute>> entityAttributeModifiersKeys = new ArrayList<>(entityAttributeModifiers.keySet());
		for (RegistryEntry<EntityAttribute> key : entityAttributeModifiersKeys) {
			Collection<EntityAttributeModifier> modifierCollection = entityAttributeModifiers.get(key);
			List<EntityAttributeModifier> modifierList = modifierCollection.stream().toList();
			for (EntityAttributeModifier entityAttributeModifier : modifierList) {
				this.entityAttributeModifiersList.add(new MutablePair<>(Registries.ATTRIBUTE.getId(key.value()), entityAttributeModifier));
			}
		}

		this.cycleScreenPageButton = this.addDrawableChild(CyclingButtonWidget.builder(ScreenPage::asText).values((ScreenPage[]) ScreenPage.values()).initially(this.screenPage).omitKeyText().build(this.width / 2 - 154, 20, 308, 20, Text.empty(), (button, screenPage) -> {
			this.screenPage = screenPage;
			this.updateWidgets();
		}));

		// --- misc page ---

		this.entitySpawnPositionOffsetXField = new TextFieldWidget(this.textRenderer, this.width / 2 - 154, 60, 100, 20, Text.empty());
		this.entitySpawnPositionOffsetXField.setText(Integer.toString(this.triggeredSpawnerBlock.getEntitySpawnPositionOffset().getX()));
		this.addSelectableChild(this.entitySpawnPositionOffsetXField);
		this.entitySpawnPositionOffsetYField = new TextFieldWidget(this.textRenderer, this.width / 2 - 50, 60, 100, 20, Text.empty());
		this.entitySpawnPositionOffsetYField.setText(Integer.toString(this.triggeredSpawnerBlock.getEntitySpawnPositionOffset().getY()));
		this.addSelectableChild(this.entitySpawnPositionOffsetYField);
		this.entitySpawnPositionOffsetZField = new TextFieldWidget(this.textRenderer, this.width / 2 + 54, 60, 100, 20, Text.empty());
		this.entitySpawnPositionOffsetZField.setText(Integer.toString(this.triggeredSpawnerBlock.getEntitySpawnPositionOffset().getZ()));
		this.addSelectableChild(this.entitySpawnPositionOffsetZField);

		this.entitySpawnOrientationPitchField = new TextFieldWidget(this.textRenderer, this.width / 2 - 154, 95, 150, 20, Text.empty());
		this.entitySpawnOrientationPitchField.setText(Double.toString(this.triggeredSpawnerBlock.getEntitySpawnOrientationPitch()));
		this.addSelectableChild(this.entitySpawnOrientationPitchField);
		this.entitySpawnOrientationYawField = new TextFieldWidget(this.textRenderer, this.width / 2 + 4, 95, 150, 20, Text.empty());
		this.entitySpawnOrientationYawField.setText(Double.toString(this.triggeredSpawnerBlock.getEntitySpawnOrientationYaw()));
		this.addSelectableChild(this.entitySpawnOrientationYawField);

		this.spawningMode = this.triggeredSpawnerBlock.getSpawningMode();
		this.cycleSpawningModeButton = this.addDrawableChild(CyclingButtonWidget.builder(TriggeredSpawnerBlockEntity.SpawningMode::asText).values(TriggeredSpawnerBlockEntity.SpawningMode.values()).initially(this.spawningMode).omitKeyText().build(this.width / 2 - 154, 130, 100, 20, Text.empty(), (button, spawningMode) -> {
			this.spawningMode = spawningMode;
		}));

		this.isPersistent = this.triggeredSpawnerBlock.isPersistent();
		this.toggleIsPersistentButton = this.addDrawableChild(CyclingButtonWidget.onOffBuilder().initially(this.isPersistent).omitKeyText().build(this.width / 2 - 50, 130, 100, 20, Text.empty(), (button, isPersistent) -> {
			this.isPersistent = isPersistent;
		}));

		this.enableSpawnEffects = this.triggeredSpawnerBlock.enableSpawnEffects();
		this.toggleEnableSpawnEffectsButton = this.addDrawableChild(CyclingButtonWidget.onOffBuilder(TOGGLE_ENABLE_SPAWN_EFFECTS_BUTTON_ON_LABEL_TEXT, TOGGLE_ENABLE_SPAWN_EFFECTS_BUTTON_OFF_LABEL_TEXT).initially(this.enableSpawnEffects).omitKeyText().build(this.width / 2 + 54, 130, 100, 20, Text.empty(), (button, enableSpawnEffects) -> {
			this.enableSpawnEffects = enableSpawnEffects;
		}));

		this.entityTypeIdField = new TextFieldWidget(this.textRenderer, this.width / 2 - 154, 165, 308, 20, Text.empty());
		this.entityTypeIdField.setMaxLength(128);
		this.entityTypeIdField.setText(this.triggeredSpawnerBlock.getEntityTypeId());
		this.addSelectableChild(this.entityTypeIdField);

		// --- entity attribute modifier page ---

		this.removeEntryButton0 = this.addDrawableChild(new TexturedButtonWidget(this.width / 2 - 141, 47, 20, 20, REMOVE_ENTRY_BUTTON_TEXTURES, button -> this.removeEntry(0)));
		this.removeEntryButton1 = this.addDrawableChild(new TexturedButtonWidget(this.width / 2 - 141, 81, 20, 20, REMOVE_ENTRY_BUTTON_TEXTURES, button -> this.removeEntry(1)));
		this.removeEntryButton2 = this.addDrawableChild(new TexturedButtonWidget(this.width / 2 - 141, 115, 20, 20, REMOVE_ENTRY_BUTTON_TEXTURES, button -> this.removeEntry(2)));
		this.entityAttributeIdentifierField = new TextFieldWidget(this.textRenderer, this.width / 2 - 154, 140, 308, 20, Text.empty());
		this.entityAttributeIdentifierField.setPlaceholder(ENTITY_ATTRIBUTE_IDENTIFIER_FIELD_PLACEHOLDER_TEXT);
		this.addSelectableChild(this.entityAttributeIdentifierField);
		this.entityAttributeModifierIdentifierField = new TextFieldWidget(this.textRenderer, this.width / 2 - 154, 164, 308, 20, Text.empty());
		this.entityAttributeModifierIdentifierField.setPlaceholder(ENTITY_ATTRIBUTE_MODIFIER_IDENTIFIER_FIELD_PLACEHOLDER_TEXT);
		this.addSelectableChild(this.entityAttributeModifierIdentifierField);
		this.entityAttributeModifierValueField = new TextFieldWidget(this.textRenderer, this.width / 2 - 154, 188, 100, 20, Text.empty());
		this.entityAttributeModifierValueField.setPlaceholder(ENTITY_ATTRIBUTE_MODIFIER_VALUE_FIELD_PLACEHOLDER_TEXT);
		this.addSelectableChild(this.entityAttributeModifierValueField);
		this.cycleEntityAttributeModifierOperationButton = this.addDrawableChild(CyclingButtonWidget.builder(TriggeredSpawnerBlockEntity.VisualEntityAttributeModifierOperation::asText).values(TriggeredSpawnerBlockEntity.VisualEntityAttributeModifierOperation.values()).initially(this.newEntityAttributeModifierOperation).omitKeyText().build(this.width / 2 - 50, 188, 100, 20, Text.empty(), (button, newEntityAttributeModifierOperationButton) -> {
			this.newEntityAttributeModifierOperation = newEntityAttributeModifierOperationButton;
			this.updateWidgets();
		}));
		this.addNewEntityAttributeModifierButton = this.addDrawableChild(ButtonWidget.builder(Text.translatable("gui.list_entry.add"), button -> this.addNewEntityAttributeModifier()).dimensions(this.width / 2 + 54, 188, 100, 20).build());

		// --- triggered block page ---

		this.useRelayBlockPositionOffsetXField = new TextFieldWidget(this.textRenderer, this.width / 2 - 154, 95, 100, 20, Text.empty());
		this.useRelayBlockPositionOffsetXField.setMaxLength(128);
		this.useRelayBlockPositionOffsetXField.setText(Integer.toString(this.triggeredSpawnerBlock.getUseRelayBlockPositionOffset().getX()));
		this.addSelectableChild(this.useRelayBlockPositionOffsetXField);
		this.useRelayBlockPositionOffsetYField = new TextFieldWidget(this.textRenderer, this.width / 2 - 50, 95, 100, 20, Text.empty());
		this.useRelayBlockPositionOffsetYField.setMaxLength(128);
		this.useRelayBlockPositionOffsetYField.setText(Integer.toString(this.triggeredSpawnerBlock.getUseRelayBlockPositionOffset().getY()));
		this.addSelectableChild(this.useRelayBlockPositionOffsetYField);
		this.useRelayBlockPositionOffsetZField = new TextFieldWidget(this.textRenderer, this.width / 2 + 54, 95, 100, 20, Text.empty());
		this.useRelayBlockPositionOffsetZField.setMaxLength(128);
		this.useRelayBlockPositionOffsetZField.setText(Integer.toString(this.triggeredSpawnerBlock.getUseRelayBlockPositionOffset().getZ()));
		this.addSelectableChild(this.useRelayBlockPositionOffsetZField);

		// --- triggered block page ---

		this.triggeredBlockPositionOffsetXField = new TextFieldWidget(this.textRenderer, this.width / 2 - 154, 95, 50, 20, Text.empty());
		this.triggeredBlockPositionOffsetXField.setMaxLength(128);
		this.triggeredBlockPositionOffsetXField.setText(Integer.toString(this.triggeredSpawnerBlock.getTriggeredBlock().getLeft().getX()));
		this.addSelectableChild(this.triggeredBlockPositionOffsetXField);
		this.triggeredBlockPositionOffsetYField = new TextFieldWidget(this.textRenderer, this.width / 2 - 100, 95, 50, 20, Text.empty());
		this.triggeredBlockPositionOffsetYField.setMaxLength(128);
		this.triggeredBlockPositionOffsetYField.setText(Integer.toString(this.triggeredSpawnerBlock.getTriggeredBlock().getLeft().getY()));
		this.addSelectableChild(this.triggeredBlockPositionOffsetYField);
		this.triggeredBlockPositionOffsetZField = new TextFieldWidget(this.textRenderer, this.width / 2 - 46, 95, 50, 20, Text.empty());
		this.triggeredBlockPositionOffsetZField.setMaxLength(128);
		this.triggeredBlockPositionOffsetZField.setText(Integer.toString(this.triggeredSpawnerBlock.getTriggeredBlock().getLeft().getZ()));
		this.addSelectableChild(this.triggeredBlockPositionOffsetZField);
		this.triggeredBlockResets = this.triggeredSpawnerBlock.getTriggeredBlock().getRight();
		this.toggleTriggeredBlockResetsButton = this.addDrawableChild(CyclingButtonWidget.onOffBuilder(Text.translatable("gui.triggered_block.toggle_triggered_block_resets_button_label.on"), Text.translatable("gui.triggered_block.toggle_triggered_block_resets_button_label.off")).initially(this.triggeredBlockResets).omitKeyText().build(this.width / 2 + 8, 95, 150, 20, Text.empty(), (button, triggeredBlockResets) -> {
			this.triggeredBlockResets = triggeredBlockResets;
		}));

		this.addDrawableChild(ButtonWidget.builder(ScreenTexts.DONE, button -> this.done()).dimensions(this.width / 2 - 4 - 150, 212, 150, 20).build());
		this.addDrawableChild(ButtonWidget.builder(ScreenTexts.CANCEL, button -> this.cancel()).dimensions(this.width / 2 + 4, 212, 150, 20).build());
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
	public void resize(MinecraftClient client, int width, int height) {
		ScreenPage var = this.screenPage;
		TriggeredSpawnerBlockEntity.SpawningMode var1 = this.spawningMode;
		TriggeredSpawnerBlockEntity.VisualEntityAttributeModifierOperation var2 = this.newEntityAttributeModifierOperation;
		List<MutablePair<Identifier, EntityAttributeModifier>> list = new ArrayList<>(this.entityAttributeModifiersList);
		String string = this.entitySpawnPositionOffsetXField.getText();
		String string1 = this.entitySpawnPositionOffsetYField.getText();
		String string2 = this.entitySpawnPositionOffsetZField.getText();
		String string3 = this.entitySpawnOrientationPitchField.getText();
		String string4 = this.entitySpawnOrientationYawField.getText();
		String string5 = this.entityTypeIdField.getText();
		String string6 = this.entityAttributeIdentifierField.getText();
		String string7 = this.entityAttributeModifierIdentifierField.getText();
		String string8 = this.entityAttributeModifierValueField.getText();
		String string9 = this.useRelayBlockPositionOffsetXField.getText();
		String string10 = this.useRelayBlockPositionOffsetYField.getText();
		String string11 = this.useRelayBlockPositionOffsetZField.getText();
		String string12 = this.triggeredBlockPositionOffsetXField.getText();
		String string13 = this.triggeredBlockPositionOffsetYField.getText();
		String string14 = this.triggeredBlockPositionOffsetZField.getText();
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
		this.entitySpawnPositionOffsetXField.setText(string);
		this.entitySpawnPositionOffsetYField.setText(string1);
		this.entitySpawnPositionOffsetZField.setText(string2);
		this.entitySpawnOrientationPitchField.setText(string3);
		this.entitySpawnOrientationYawField.setText(string4);
		this.entityTypeIdField.setText(string5);
		this.entityAttributeIdentifierField.setText(string6);
		this.entityAttributeModifierIdentifierField.setText(string7);
		this.entityAttributeModifierValueField.setText(string8);
		this.useRelayBlockPositionOffsetXField.setText(string9);
		this.useRelayBlockPositionOffsetYField.setText(string10);
		this.useRelayBlockPositionOffsetZField.setText(string11);
		this.triggeredBlockPositionOffsetXField.setText(string12);
		this.triggeredBlockPositionOffsetYField.setText(string13);
		this.triggeredBlockPositionOffsetZField.setText(string14);
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
			this.scrollAmount = MathHelper.clamp(this.scrollAmount + f, 0.0f, 1.0f);
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

		if (this.screenPage == ScreenPage.MISC) {
			context.drawTextWithShadow(this.textRenderer, ENTITY_SPAWN_POSITION_OFFSET_LABEL_TEXT, this.width / 2 - 153, 50, 0xA0A0A0);
			this.entitySpawnPositionOffsetXField.render(context, mouseX, mouseY, delta);
			this.entitySpawnPositionOffsetYField.render(context, mouseX, mouseY, delta);
			this.entitySpawnPositionOffsetZField.render(context, mouseX, mouseY, delta);

			context.drawTextWithShadow(this.textRenderer, ENTITY_SPAWN_ORIENTATION_PITCH_LABEL_TEXT, this.width / 2 - 153, 85, 0xA0A0A0);
			this.entitySpawnOrientationPitchField.render(context, mouseX, mouseY, delta);
			context.drawTextWithShadow(this.textRenderer, ENTITY_SPAWN_ORIENTATION_YAW_LABEL_TEXT, this.width / 2 + 5, 85, 0xA0A0A0);
			this.entitySpawnOrientationYawField.render(context, mouseX, mouseY, delta);

			context.drawTextWithShadow(this.textRenderer, SPAWNING_MODE_LABEL_TEXT, this.width / 2 - 153, 120, 0xA0A0A0);
			context.drawTextWithShadow(this.textRenderer, IS_PERSISTENT_LABEL_TEXT, this.width / 2 - 49, 120, 0xA0A0A0);
			context.drawTextWithShadow(this.textRenderer, ENABLE_SPAWN_EFFECTS_LABEL_TEXT, this.width / 2 + 55, 120, 0xA0A0A0);

			context.drawTextWithShadow(this.textRenderer, ENTITY_TYPE_LABEL_TEXT, this.width / 2 - 153, 155, 0xA0A0A0);
			this.entityTypeIdField.render(context, mouseX, mouseY, delta);
		} else if (this.screenPage == ScreenPage.ENTITY_ATTRIBUTE_MODIFIER) {

			for (int i = this.scrollPosition; i < Math.min(this.scrollPosition + VISIBLE_LIST_ENTRIES, this.entityAttributeModifiersList.size()); i++) {
				EntityAttributeModifier entityAttributeModifier = this.entityAttributeModifiersList.get(i).getRight();

				context.drawTextWithShadow(this.textRenderer, this.entityAttributeModifiersList.get(i).getLeft().toString(), this.width / 2 - 117, 46 + ((i - this.scrollPosition) * 34), 0xA0A0A0);
				context.drawTextWithShadow(this.textRenderer, entityAttributeModifier.value() + ", " + entityAttributeModifier.operation(), this.width / 2 - 117, 59 + ((i - this.scrollPosition) * 34), 0xA0A0A0);
			}
			if (this.entityAttributeModifiersList.size() > VISIBLE_LIST_ENTRIES) {
				context.drawGuiTexture(SCROLL_BAR_BACKGROUND_8_92_TEXTURE, this.width / 2 - 153, 45, 8, 92);
				int k = (int) (83.0f * this.scrollAmount);
				context.drawGuiTexture(SCROLLER_VERTICAL_6_7_TEXTURE, this.width / 2 - 152, 45 + 1 + k, 6, 7);
			}

			this.entityAttributeIdentifierField.render(context, mouseX, mouseY, delta);
			this.entityAttributeModifierIdentifierField.render(context, mouseX, mouseY, delta);
			this.entityAttributeModifierValueField.render(context, mouseX, mouseY, delta);

		} else if (this.screenPage == ScreenPage.USE_RELAY_BLOCK) {
			context.drawTextWithShadow(this.textRenderer, USE_RELAY_BLOCK_POSITION_OFFSET_LABEL_TEXT, this.width / 2 - 153, 85, 0xA0A0A0);
			this.useRelayBlockPositionOffsetXField.render(context, mouseX, mouseY, delta);
			this.useRelayBlockPositionOffsetYField.render(context, mouseX, mouseY, delta);
			this.useRelayBlockPositionOffsetZField.render(context, mouseX, mouseY, delta);
		} else if (this.screenPage == ScreenPage.TRIGGERED_BLOCK) {
			context.drawTextWithShadow(this.textRenderer, TRIGGERED_BLOCK_POSITION_OFFSET_LABEL_TEXT, this.width / 2 - 153, 85, 0xA0A0A0);
			this.triggeredBlockPositionOffsetXField.render(context, mouseX, mouseY, delta);
			this.triggeredBlockPositionOffsetYField.render(context, mouseX, mouseY, delta);
			this.triggeredBlockPositionOffsetZField.render(context, mouseX, mouseY, delta);
		}
	}

	@Override
	protected boolean updateBlock() {
		ClientPlayNetworking.send(
				new UpdateTriggeredSpawnerBlockPacket(
						this.triggeredSpawnerBlock.getPos(),
						new BlockPos(
								ItemUtils.parseInt(this.entitySpawnPositionOffsetXField.getText()),
								ItemUtils.parseInt(this.entitySpawnPositionOffsetYField.getText()),
								ItemUtils.parseInt(this.entitySpawnPositionOffsetZField.getText())
						),
						ItemUtils.parseDouble(this.entitySpawnOrientationPitchField.getText()),
						ItemUtils.parseDouble(this.entitySpawnOrientationYawField.getText()),
						this.spawningMode.asString(),
						this.isPersistent,
						this.enableSpawnEffects,
						this.entityTypeIdField.getText(),
						new ArrayList<>(this.entityAttributeModifiersList),
						new BlockPos(
								ItemUtils.parseInt(this.triggeredBlockPositionOffsetXField.getText()),
								ItemUtils.parseInt(this.triggeredBlockPositionOffsetYField.getText()),
								ItemUtils.parseInt(this.triggeredBlockPositionOffsetZField.getText())
						),
						this.triggeredBlockResets,
						new BlockPos(
								ItemUtils.parseInt(this.useRelayBlockPositionOffsetXField.getText()),
								ItemUtils.parseInt(this.useRelayBlockPositionOffsetYField.getText()),
								ItemUtils.parseInt(this.useRelayBlockPositionOffsetZField.getText())
						)
				)
		);
		return true;
	}

	public enum ScreenPage implements StringIdentifiable {
		MISC("misc"),
		ENTITY_ATTRIBUTE_MODIFIER("entity_attribute_modifier"),
		USE_RELAY_BLOCK("use_relay_block"),
		TRIGGERED_BLOCK("triggered_block");

		private final String name;

		ScreenPage(String name) {
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
			return Text.translatable("gui.triggered_spawner_block.screen_page." + this.name);
		}
	}
}
