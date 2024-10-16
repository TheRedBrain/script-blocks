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
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.CyclingButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
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
public class TriggeredSpawnerBlockScreen extends Screen {
	private static final Text ENTITY_SPAWN_POSITION_OFFSET_LABEL_TEXT = Text.translatable("gui.triggered_spawner_block.entity_spawn_position_offset_label");
	private static final Text ENTITY_SPAWN_ORIENTATION_PITCH_LABEL_TEXT = Text.translatable("gui.triggered_spawner_block.entity_spawn_orientation_pitch_label");
	private static final Text ENTITY_SPAWN_ORIENTATION_YAW_LABEL_TEXT = Text.translatable("gui.triggered_spawner_block.entity_spawn_orientation_yaw_label");
	private static final Text SPAWNING_MODE_LABEL_TEXT = Text.translatable("gui.triggered_spawner_block.spawning_mode_label");
	private static final Text ENTITY_TYPE_LABEL_TEXT = Text.translatable("gui.triggered_spawner_block.entity_type_label");
	private static final Text REMOVE_BUTTON_LABEL_TEXT = Text.translatable("gui.list_entry.remove");
	private static final Text NEW_ENTITY_ATTRIBUTE_MODIFIER_IDENTIFIER_FIELD_PLACEHOLDER_TEXT = Text.translatable("gui.triggered_spawner_block.new_entity_attribute_modifier_identifier_label");
	private static final Text NEW_ENTITY_ATTRIBUTE_MODIFIER_NAME_FIELD_PLACEHOLDER_TEXT = Text.translatable("gui.triggered_spawner_block.new_entity_attribute_modifier_name_label");
	private static final Text NEW_ENTITY_ATTRIBUTE_MODIFIER_VALUE_FIELD_PLACEHOLDER_TEXT = Text.translatable("gui.triggered_spawner_block.new_entity_attribute_modifier_value_label");
	private static final Text USE_RELAY_BLOCK_POSITION_OFFSET_LABEL_TEXT = Text.translatable("gui.triggered_spawner_block.useRelayBlockPositionOffset");
	private static final Text TRIGGERED_BLOCK_POSITION_OFFSET_LABEL_TEXT = Text.translatable("gui.triggered_block.triggeredBlockPositionOffset");

	private static final Identifier SCROLL_BAR_BACKGROUND_8_92_TEXTURE = ScriptBlocks.identifier("scroll_bar/scroll_bar_background_8_92");
	private static final Identifier SCROLLER_VERTICAL_6_7_TEXTURE = ScriptBlocks.identifier("scroll_bar/scroller_vertical_6_7");

	private final TriggeredSpawnerBlockEntity triggeredSpawnerBlock;

	private CyclingButtonWidget<CreativeScreenPage> creativeScreenPageButton;
	private TextFieldWidget entitySpawnPositionOffsetXField;
	private TextFieldWidget entitySpawnPositionOffsetYField;
	private TextFieldWidget entitySpawnPositionOffsetZField;
	private TextFieldWidget entitySpawnOrientationPitchField;
	private TextFieldWidget entitySpawnOrientationYawField;
	private CyclingButtonWidget<TriggeredSpawnerBlockEntity.SpawningMode> cycleSpawningModeButton;
	private TriggeredSpawnerBlockEntity.SpawningMode spawningMode;

	private TextFieldWidget entityTypeIdField;

	private ButtonWidget removeListEntryButton0;
	private ButtonWidget removeListEntryButton1;
	private ButtonWidget removeListEntryButton2;
	private TextFieldWidget newEntityAttributeModifierIdentifierField;
	private TextFieldWidget newEntityAttributeModifierNameField;
	private TextFieldWidget newEntityAttributeModifierValueField;
	private ButtonWidget newEntityAttributeModifierOperationButton;
	private ButtonWidget addNewEntityAttributeModifierButton;

	private TextFieldWidget useRelayBlockPositionOffsetXField;
	private TextFieldWidget useRelayBlockPositionOffsetYField;
	private TextFieldWidget useRelayBlockPositionOffsetZField;

	private TextFieldWidget triggeredBlockPositionOffsetXField;
	private TextFieldWidget triggeredBlockPositionOffsetYField;
	private TextFieldWidget triggeredBlockPositionOffsetZField;
	private CyclingButtonWidget<Boolean> toggleTriggeredBlockResetsButton;
	private boolean triggeredBlockResets;

	private ButtonWidget doneButton;
	private ButtonWidget cancelButton;

	List<MutablePair<Identifier, EntityAttributeModifier>> entityAttributeModifiersList = new ArrayList<>();
	private CreativeScreenPage creativeScreenPage;
	private EntityAttributeModifier.Operation newEntityAttributeModifierOperation;
	private int scrollPosition = 0;
	private float scrollAmount = 0.0f;
	private boolean mouseClicked = false;

	public TriggeredSpawnerBlockScreen(TriggeredSpawnerBlockEntity triggeredSpawnerBlock) {
		super(NarratorManager.EMPTY);
		this.triggeredSpawnerBlock = triggeredSpawnerBlock;
		this.creativeScreenPage = CreativeScreenPage.MISC;
		this.newEntityAttributeModifierOperation = EntityAttributeModifier.Operation.ADD_VALUE;
	}

	private void deleteListEntry(int index) {
		this.entityAttributeModifiersList.remove(index + this.scrollPosition);
		this.scrollPosition = 0;
		this.scrollAmount = 0.0f;
		updateWidgets();
	}

	private void addNewEntityAttributeModifier() {
		// TODO check for existing entries and validate fields
		this.entityAttributeModifiersList.add(new MutablePair<>(
				Identifier.of(this.newEntityAttributeModifierIdentifierField.getText()),
				new EntityAttributeModifier(
						Identifier.of(this.newEntityAttributeModifierNameField.getText()),
						ItemUtils.parseDouble(this.newEntityAttributeModifierValueField.getText()),
						this.newEntityAttributeModifierOperation
				)
		));
		this.scrollPosition = 0;
		this.scrollAmount = 0.0f;
		updateWidgets();
	}

	private void cycleNewEntityAttributeModifierOperationButton() {
		if (this.newEntityAttributeModifierOperation == EntityAttributeModifier.Operation.ADD_VALUE) {
			this.newEntityAttributeModifierOperation = EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE;
		} else if (this.newEntityAttributeModifierOperation == EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE) {
			this.newEntityAttributeModifierOperation = EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL;
		} else if (this.newEntityAttributeModifierOperation == EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL) {
			this.newEntityAttributeModifierOperation = EntityAttributeModifier.Operation.ADD_VALUE;
		}
		this.newEntityAttributeModifierOperationButton.setMessage(Text.translatable("gui.entity_attribute_modifier.operation." + this.newEntityAttributeModifierOperation.getId()));
		updateWidgets();
	}

	private void done() {
		this.updateTriggeredSpawnerBlock();
		this.close();
	}

	private void cancel() {
		this.close();
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

		this.creativeScreenPageButton = this.addDrawableChild(CyclingButtonWidget.builder(CreativeScreenPage::asText).values((CreativeScreenPage[]) CreativeScreenPage.values()).initially(this.creativeScreenPage).omitKeyText().build(this.width / 2 - 154, 20, 300, 20, Text.empty(), (button, creativeScreenPage) -> {
			this.creativeScreenPage = creativeScreenPage;
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
		this.cycleSpawningModeButton = this.addDrawableChild(CyclingButtonWidget.builder(TriggeredSpawnerBlockEntity.SpawningMode::asText).values((TriggeredSpawnerBlockEntity.SpawningMode[]) TriggeredSpawnerBlockEntity.SpawningMode.values()).initially(this.spawningMode).omitKeyText().build(this.width / 2 - 154, 130, 150, 20, Text.empty(), (button, spawningMode) -> {
			this.spawningMode = spawningMode;
		}));

		this.entityTypeIdField = new TextFieldWidget(this.textRenderer, this.width / 2 - 154, 165, 300, 20, Text.empty());
		this.entityTypeIdField.setMaxLength(128);
		this.entityTypeIdField.setText(this.triggeredSpawnerBlock.getEntityTypeId());
		this.addSelectableChild(this.entityTypeIdField);

		// --- entity attribute modifier page ---
		
		this.removeListEntryButton0 = this.addDrawableChild(ButtonWidget.builder(REMOVE_BUTTON_LABEL_TEXT, button -> this.deleteListEntry(0)).dimensions(this.width / 2 + 54, 47, 100, 20).build());
		this.removeListEntryButton1 = this.addDrawableChild(ButtonWidget.builder(REMOVE_BUTTON_LABEL_TEXT, button -> this.deleteListEntry(1)).dimensions(this.width / 2 + 54, 81, 100, 20).build());
		this.removeListEntryButton2 = this.addDrawableChild(ButtonWidget.builder(REMOVE_BUTTON_LABEL_TEXT, button -> this.deleteListEntry(2)).dimensions(this.width / 2 + 54, 115, 100, 20).build());
		this.newEntityAttributeModifierIdentifierField = new TextFieldWidget(this.textRenderer, this.width / 2 - 154, 140, 300, 20, Text.empty());
		this.newEntityAttributeModifierIdentifierField.setPlaceholder(NEW_ENTITY_ATTRIBUTE_MODIFIER_IDENTIFIER_FIELD_PLACEHOLDER_TEXT);
		this.addSelectableChild(this.newEntityAttributeModifierIdentifierField);
		this.newEntityAttributeModifierNameField = new TextFieldWidget(this.textRenderer, this.width / 2 - 154, 164, 300, 20, Text.empty());
		this.newEntityAttributeModifierNameField.setPlaceholder(NEW_ENTITY_ATTRIBUTE_MODIFIER_NAME_FIELD_PLACEHOLDER_TEXT);
		this.addSelectableChild(this.newEntityAttributeModifierNameField);
		this.newEntityAttributeModifierValueField = new TextFieldWidget(this.textRenderer, this.width / 2 - 154, 188, 100, 20, Text.empty());
		this.newEntityAttributeModifierValueField.setPlaceholder(NEW_ENTITY_ATTRIBUTE_MODIFIER_VALUE_FIELD_PLACEHOLDER_TEXT);
		this.addSelectableChild(this.newEntityAttributeModifierValueField);
		this.newEntityAttributeModifierOperationButton = this.addDrawableChild(ButtonWidget.builder(Text.translatable("gui.entity_attribute_modifier.operation." + this.newEntityAttributeModifierOperation.getId()), button -> this.cycleNewEntityAttributeModifierOperationButton()).dimensions(this.width / 2 - 50, 188, 100, 20).build());
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

		this.doneButton = this.addDrawableChild(ButtonWidget.builder(ScreenTexts.DONE, button -> this.done()).dimensions(this.width / 2 - 4 - 150, 212, 150, 20).build());
		this.cancelButton = this.addDrawableChild(ButtonWidget.builder(ScreenTexts.CANCEL, button -> this.cancel()).dimensions(this.width / 2 + 4, 212, 150, 20).build());
		this.setInitialFocus(this.entitySpawnPositionOffsetXField);
		this.updateWidgets();
	}

	private void updateWidgets() {

		this.creativeScreenPageButton.visible = false;

		this.entitySpawnPositionOffsetXField.setVisible(false);
		this.entitySpawnPositionOffsetYField.setVisible(false);
		this.entitySpawnPositionOffsetZField.setVisible(false);

		this.cycleSpawningModeButton.visible = false;

		this.entityTypeIdField.setVisible(false);

		this.removeListEntryButton0.visible = false;
		this.removeListEntryButton1.visible = false;
		this.removeListEntryButton2.visible = false;
		this.newEntityAttributeModifierIdentifierField.setVisible(false);
		this.newEntityAttributeModifierNameField.setVisible(false);
		this.newEntityAttributeModifierValueField.setVisible(false);
		this.newEntityAttributeModifierOperationButton.visible = false;
		this.addNewEntityAttributeModifierButton.visible = false;

		this.useRelayBlockPositionOffsetXField.setVisible(false);
		this.useRelayBlockPositionOffsetYField.setVisible(false);
		this.useRelayBlockPositionOffsetZField.setVisible(false);

		this.triggeredBlockPositionOffsetXField.setVisible(false);
		this.triggeredBlockPositionOffsetYField.setVisible(false);
		this.triggeredBlockPositionOffsetZField.setVisible(false);
		this.toggleTriggeredBlockResetsButton.visible = false;

		this.doneButton.visible = false;
		this.cancelButton.visible = false;

		this.creativeScreenPageButton.visible = true;

		if (this.creativeScreenPage == CreativeScreenPage.MISC) {

			this.entitySpawnPositionOffsetXField.setVisible(true);
			this.entitySpawnPositionOffsetYField.setVisible(true);
			this.entitySpawnPositionOffsetZField.setVisible(true);

			this.cycleSpawningModeButton.visible = true;

			this.entityTypeIdField.setVisible(true);

		} else if (this.creativeScreenPage == CreativeScreenPage.ENTITY_ATTRIBUTE_MODIFIER) {

			int index = 0;
			for (int i = 0; i < Math.min(3, this.entityAttributeModifiersList.size()); i++) {
				if (index == 0) {
					this.removeListEntryButton0.visible = true;
				} else if (index == 1) {
					this.removeListEntryButton1.visible = true;
				} else if (index == 2) {
					this.removeListEntryButton2.visible = true;
				}
				index++;
			}

			this.newEntityAttributeModifierIdentifierField.setVisible(true);
			this.newEntityAttributeModifierNameField.setVisible(true);
			this.newEntityAttributeModifierValueField.setVisible(true);
			this.newEntityAttributeModifierOperationButton.visible = true;
			this.addNewEntityAttributeModifierButton.visible = true;

		} else if (this.creativeScreenPage == CreativeScreenPage.USE_RELAY_BLOCK) {

			this.useRelayBlockPositionOffsetXField.setVisible(true);
			this.useRelayBlockPositionOffsetYField.setVisible(true);
			this.useRelayBlockPositionOffsetZField.setVisible(true);

		} else if (this.creativeScreenPage == CreativeScreenPage.TRIGGERED_BLOCK) {

			this.triggeredBlockPositionOffsetXField.setVisible(true);
			this.triggeredBlockPositionOffsetYField.setVisible(true);
			this.triggeredBlockPositionOffsetZField.setVisible(true);
			this.toggleTriggeredBlockResetsButton.visible = true;

		}

		this.doneButton.visible = true;
		this.cancelButton.visible = true;

	}

	@Override
	public void resize(MinecraftClient client, int width, int height) {
		CreativeScreenPage var = this.creativeScreenPage;
		TriggeredSpawnerBlockEntity.SpawningMode var1 = this.spawningMode;
		EntityAttributeModifier.Operation var2 = this.newEntityAttributeModifierOperation;
		List<MutablePair<Identifier, EntityAttributeModifier>> list = this.entityAttributeModifiersList;
		String string = this.entitySpawnPositionOffsetXField.getText();
		String string1 = this.entitySpawnPositionOffsetYField.getText();
		String string2 = this.entitySpawnPositionOffsetZField.getText();
		String string3 = this.entitySpawnOrientationPitchField.getText();
		String string4 = this.entitySpawnOrientationYawField.getText();
		String string5 = this.entityTypeIdField.getText();
		String string6 = this.newEntityAttributeModifierIdentifierField.getText();
		String string7 = this.newEntityAttributeModifierNameField.getText();
		String string8 = this.newEntityAttributeModifierValueField.getText();
		String string9 = this.useRelayBlockPositionOffsetXField.getText();
		String string10 = this.useRelayBlockPositionOffsetYField.getText();
		String string11 = this.useRelayBlockPositionOffsetZField.getText();
		String string12 = this.triggeredBlockPositionOffsetXField.getText();
		String string13 = this.triggeredBlockPositionOffsetYField.getText();
		String string14 = this.triggeredBlockPositionOffsetZField.getText();
		boolean bl = this.triggeredBlockResets;
		this.init(client, width, height);
		this.creativeScreenPage = var;
		this.spawningMode = var1;
		this.newEntityAttributeModifierOperation = var2;
		this.entityAttributeModifiersList = list;
		this.entitySpawnPositionOffsetXField.setText(string);
		this.entitySpawnPositionOffsetYField.setText(string1);
		this.entitySpawnPositionOffsetZField.setText(string2);
		this.entitySpawnOrientationPitchField.setText(string3);
		this.entitySpawnOrientationYawField.setText(string4);
		this.entityTypeIdField.setText(string5);
		this.newEntityAttributeModifierIdentifierField.setText(string6);
		this.newEntityAttributeModifierNameField.setText(string7);
		this.newEntityAttributeModifierValueField.setText(string8);
		this.useRelayBlockPositionOffsetXField.setText(string9);
		this.useRelayBlockPositionOffsetYField.setText(string10);
		this.useRelayBlockPositionOffsetZField.setText(string11);
		this.triggeredBlockPositionOffsetXField.setText(string12);
		this.triggeredBlockPositionOffsetYField.setText(string13);
		this.triggeredBlockPositionOffsetZField.setText(string14);
		this.triggeredBlockResets = bl;
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		this.mouseClicked = false;
		if (this.creativeScreenPage == CreativeScreenPage.ENTITY_ATTRIBUTE_MODIFIER
				&& this.entityAttributeModifiersList.size() > 3) {
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
		if (this.creativeScreenPage == CreativeScreenPage.ENTITY_ATTRIBUTE_MODIFIER
				&& this.entityAttributeModifiersList.size() > 3
				&& this.mouseClicked) {
			int i = this.entityAttributeModifiersList.size() - 3;
			float f = (float) deltaY / (float) i;
			this.scrollAmount = MathHelper.clamp(this.scrollAmount + f, 0.0f, 1.0f);
			this.scrollPosition = (int) ((double) (this.scrollAmount * (float) i));
		}
		return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
		if (this.creativeScreenPage == CreativeScreenPage.ENTITY_ATTRIBUTE_MODIFIER
				&& this.entityAttributeModifiersList.size() > 3
				&& mouseX >= (double) (this.width / 2 - 152) && mouseX <= (double) (this.width / 2 + 50)
				&& mouseY >= 45 && mouseY <= 137) {
			int i = this.entityAttributeModifiersList.size() - 3;
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

	private void updateTriggeredSpawnerBlock() {

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
						this.entityTypeIdField.getText(),
						this.entityAttributeModifiersList,
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
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {

		super.render(context, mouseX, mouseY, delta);

		if (this.creativeScreenPage == CreativeScreenPage.MISC) {
			context.drawTextWithShadow(this.textRenderer, ENTITY_SPAWN_POSITION_OFFSET_LABEL_TEXT, this.width / 2 - 153, 50, 0xA0A0A0);
			this.entitySpawnPositionOffsetXField.render(context, mouseX, mouseY, delta);
			this.entitySpawnPositionOffsetYField.render(context, mouseX, mouseY, delta);
			this.entitySpawnPositionOffsetZField.render(context, mouseX, mouseY, delta);

			context.drawTextWithShadow(this.textRenderer, ENTITY_SPAWN_ORIENTATION_PITCH_LABEL_TEXT, this.width / 2 - 153, 85, 0xA0A0A0);
			this.entitySpawnOrientationPitchField.render(context, mouseX, mouseY, delta);
			context.drawTextWithShadow(this.textRenderer, ENTITY_SPAWN_ORIENTATION_YAW_LABEL_TEXT, this.width / 2 + 5, 85, 0xA0A0A0);
			this.entitySpawnOrientationYawField.render(context, mouseX, mouseY, delta);

			context.drawTextWithShadow(this.textRenderer, SPAWNING_MODE_LABEL_TEXT, this.width / 2 - 153, 120, 0xA0A0A0);

			context.drawTextWithShadow(this.textRenderer, ENTITY_TYPE_LABEL_TEXT, this.width / 2 - 153, 155, 0xA0A0A0);
			this.entityTypeIdField.render(context, mouseX, mouseY, delta);
		} else if (this.creativeScreenPage == CreativeScreenPage.ENTITY_ATTRIBUTE_MODIFIER) {

			for (int i = this.scrollPosition; i < Math.min(this.scrollPosition + 3, this.entityAttributeModifiersList.size()); i++) {
				EntityAttributeModifier entityAttributeModifier = this.entityAttributeModifiersList.get(i).getRight();

				context.drawTextWithShadow(this.textRenderer, this.entityAttributeModifiersList.get(i).getLeft() + ": ", this.width / 2 - 141, 46 + ((i - this.scrollPosition) * 34), 0xA0A0A0);
				context.drawTextWithShadow(this.textRenderer, entityAttributeModifier.value() + ", " + entityAttributeModifier.operation(), this.width / 2 - 141, 59 + ((i - this.scrollPosition) * 34), 0xA0A0A0);
			}
			if (this.entityAttributeModifiersList.size() > 3) {
                    context.drawGuiTexture(SCROLL_BAR_BACKGROUND_8_92_TEXTURE, this.width / 2 - 153, 45, 8, 92);
				int k = (int) (83.0f * this.scrollAmount);
                    context.drawGuiTexture(SCROLLER_VERTICAL_6_7_TEXTURE, this.width / 2 - 152, 45 + 1 + k, 6, 7);
			}

			this.newEntityAttributeModifierIdentifierField.render(context, mouseX, mouseY, delta);
			this.newEntityAttributeModifierNameField.render(context, mouseX, mouseY, delta);
			this.newEntityAttributeModifierValueField.render(context, mouseX, mouseY, delta);

		} else if (this.creativeScreenPage == CreativeScreenPage.USE_RELAY_BLOCK) {
			context.drawTextWithShadow(this.textRenderer, USE_RELAY_BLOCK_POSITION_OFFSET_LABEL_TEXT, this.width / 2 - 153, 85, 0xA0A0A0);
			this.useRelayBlockPositionOffsetXField.render(context, mouseX, mouseY, delta);
			this.useRelayBlockPositionOffsetYField.render(context, mouseX, mouseY, delta);
			this.useRelayBlockPositionOffsetZField.render(context, mouseX, mouseY, delta);
		} else if (this.creativeScreenPage == CreativeScreenPage.TRIGGERED_BLOCK) {
			context.drawTextWithShadow(this.textRenderer, TRIGGERED_BLOCK_POSITION_OFFSET_LABEL_TEXT, this.width / 2 - 153, 85, 0xA0A0A0);
			this.triggeredBlockPositionOffsetXField.render(context, mouseX, mouseY, delta);
			this.triggeredBlockPositionOffsetYField.render(context, mouseX, mouseY, delta);
			this.triggeredBlockPositionOffsetZField.render(context, mouseX, mouseY, delta);
		}
	}

	@Override
	public boolean shouldPause() {
		return false;
	}

	public static enum CreativeScreenPage implements StringIdentifiable {
		MISC("misc"),
		ENTITY_ATTRIBUTE_MODIFIER("entity_attribute_modifier"),
		USE_RELAY_BLOCK("use_relay_block"),
		TRIGGERED_BLOCK("triggered_block");

		private final String name;

		private CreativeScreenPage(String name) {
			this.name = name;
		}

		@Override
		public String asString() {
			return this.name;
		}

		public static Optional<CreativeScreenPage> byName(String name) {
			return Arrays.stream(CreativeScreenPage.values()).filter(creativeScreenPage -> creativeScreenPage.asString().equals(name)).findFirst();
		}

		public Text asText() {
			return Text.translatable("gui.triggered_spawner_block.creativeScreenPage." + this.name);
		}
	}
}
