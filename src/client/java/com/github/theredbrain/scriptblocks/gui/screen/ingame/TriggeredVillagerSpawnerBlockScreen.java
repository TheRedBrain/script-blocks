package com.github.theredbrain.scriptblocks.gui.screen.ingame;

import com.github.theredbrain.scriptblocks.ScriptBlocks;
import com.github.theredbrain.scriptblocks.block.entity.TriggeredSpawnerBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.TriggeredVillagerSpawnerBlockEntity;
import com.github.theredbrain.scriptblocks.network.packet.UpdateTriggeredVillagerSpawnerBlockPacket;
import com.github.theredbrain.scriptblocks.util.ItemUtils;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.CyclingButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.NarratorManager;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.village.VillagerData;
import net.minecraft.village.VillagerProfession;
import net.minecraft.village.VillagerType;
import org.apache.commons.lang3.tuple.MutablePair;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Environment(value = EnvType.CLIENT)
public class TriggeredVillagerSpawnerBlockScreen extends AbstractCreativeBlockSettingsScreen {
	private static final Text ENTITY_SPAWN_POSITION_OFFSET_LABEL_TEXT = Text.translatable("gui.triggered_spawner_block.entity_spawn_position_offset_label");
	private static final Text ENTITY_SPAWN_ORIENTATION_PITCH_LABEL_TEXT = Text.translatable("gui.triggered_spawner_block.entity_spawn_orientation_pitch_label");
	private static final Text ENTITY_SPAWN_ORIENTATION_YAW_LABEL_TEXT = Text.translatable("gui.triggered_spawner_block.entity_spawn_orientation_yaw_label");
	private static final Text SPAWNING_MODE_LABEL_TEXT = Text.translatable("gui.triggered_spawner_block.spawning_mode_label");
	private static final Text ENTITY_TYPE_LABEL_TEXT = Text.translatable("gui.triggered_spawner_block.entity_type_label");
	private static final Text VILLAGER_TYPE_LABEL_TEXT = Text.translatable("gui.triggered_villager_spawner_block.villager_type_field_label");
	private static final Text VILLAGER_PROFESSION_LABEL_TEXT = Text.translatable("gui.triggered_villager_spawner_block.villager_profession_field_label");
	private static final Text VILLAGER_LEVEL_LABEL_TEXT = Text.translatable("gui.triggered_villager_spawner_block.villager_level_field_label");
	private static final Text REMOVE_BUTTON_LABEL_TEXT = Text.translatable("gui.list_entry.remove");
	private static final Text NEW_ENTITY_ATTRIBUTE_MODIFIER_IDENTIFIER_FIELD_PLACEHOLDER_TEXT = Text.translatable("gui.triggered_spawner_block.new_entity_attribute_modifier_identifier_label");
	private static final Text NEW_ENTITY_ATTRIBUTE_MODIFIER_NAME_FIELD_PLACEHOLDER_TEXT = Text.translatable("gui.triggered_spawner_block.new_entity_attribute_modifier_name_label");
	private static final Text NEW_ENTITY_ATTRIBUTE_MODIFIER_VALUE_FIELD_PLACEHOLDER_TEXT = Text.translatable("gui.triggered_spawner_block.new_entity_attribute_modifier_value_label");
	private static final Text USE_RELAY_BLOCK_POSITION_OFFSET_LABEL_TEXT = Text.translatable("gui.use_relay_block.relay_block_position_offset");
	private static final Text TRIGGERED_BLOCK_POSITION_OFFSET_LABEL_TEXT = Text.translatable("gui.triggered_block.triggered_block_position_offset");

	private static final Identifier SCROLL_BAR_BACKGROUND_8_92_TEXTURE = ScriptBlocks.identifier("scroll_bar/scroll_bar_background_8_92");
	private static final Identifier SCROLLER_VERTICAL_6_7_TEXTURE = ScriptBlocks.identifier("scroll_bar/scroller_vertical_6_7");

	private final TriggeredVillagerSpawnerBlockEntity triggeredVillagerSpawnerBlock;

	private CyclingButtonWidget<ScreenPage> cycleScreenPageButton;
	private TextFieldWidget entitySpawnPositionOffsetXField;
	private TextFieldWidget entitySpawnPositionOffsetYField;
	private TextFieldWidget entitySpawnPositionOffsetZField;
	private TextFieldWidget entitySpawnOrientationPitchField;
	private TextFieldWidget entitySpawnOrientationYawField;
	private CyclingButtonWidget<TriggeredSpawnerBlockEntity.SpawningMode> cycleSpawningModeButton;
	private TriggeredSpawnerBlockEntity.SpawningMode spawningMode;

	private TextFieldWidget entityTypeIdField;

	private final List<net.minecraft.village.VillagerType> villagerTypes = new ArrayList<>();
	private final List<net.minecraft.village.VillagerProfession> villagerProfessions = new ArrayList<>();
	private VillagerType villagerType;
	private CyclingButtonWidget<VillagerType> cycleVillagerTypeButton;
	private VillagerProfession villagerProfession;
	private CyclingButtonWidget<VillagerProfession> cycleVillagerProfessionButton;
	private TextFieldWidget villagerLevelField;

	private ButtonWidget removeListEntryButton0;
	private ButtonWidget removeListEntryButton1;
	private ButtonWidget removeListEntryButton2;
	private TextFieldWidget newEntityAttributeModifierIdentifierField;
	private TextFieldWidget newEntityAttributeModifierNameField;
	private TextFieldWidget newEntityAttributeModifierValueField;
	private CyclingButtonWidget<TriggeredSpawnerBlockEntity.VisualEntityAttributeModifierOperation> cycleNewEntityAttributeModifierOperationButton;
	private ButtonWidget addNewEntityAttributeModifierButton;

	private TextFieldWidget useRelayBlockPositionOffsetXField;
	private TextFieldWidget useRelayBlockPositionOffsetYField;
	private TextFieldWidget useRelayBlockPositionOffsetZField;

	private TextFieldWidget triggeredBlockPositionOffsetXField;
	private TextFieldWidget triggeredBlockPositionOffsetYField;
	private TextFieldWidget triggeredBlockPositionOffsetZField;
	private CyclingButtonWidget<Boolean> toggleTriggeredBlockResetsButton;
	private boolean triggeredBlockResets;

	List<MutablePair<Identifier, EntityAttributeModifier>> entityAttributeModifiersList = new ArrayList<>();
	private ScreenPage screenPage;
	private TriggeredSpawnerBlockEntity.VisualEntityAttributeModifierOperation newEntityAttributeModifierOperation;
	private int scrollPosition = 0;
	private float scrollAmount = 0.0f;
	private boolean mouseClicked = false;

	public TriggeredVillagerSpawnerBlockScreen(TriggeredVillagerSpawnerBlockEntity triggeredVillagerSpawnerBlock) {
		super(NarratorManager.EMPTY);
		this.triggeredVillagerSpawnerBlock = triggeredVillagerSpawnerBlock;
		this.screenPage = ScreenPage.MISC;
		this.newEntityAttributeModifierOperation = TriggeredSpawnerBlockEntity.VisualEntityAttributeModifierOperation.ADD_VALUE;
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
						this.newEntityAttributeModifierOperation.getOperation()
				)
		));
		this.scrollPosition = 0;
		this.scrollAmount = 0.0f;
		updateWidgets();
	}

	private void updateVillagerRegistryLists() {
		if (this.client != null && this.client.world != null) {
			Registry<net.minecraft.village.VillagerType> villagerTypeRegistry = this.client.world.getRegistryManager().get(RegistryKeys.VILLAGER_TYPE);
			this.villagerTypes.clear();
			List<RegistryEntry.Reference<VillagerType>> villagerTypeReferenceList = villagerTypeRegistry.streamEntries().toList();
			for (RegistryEntry.Reference<VillagerType> entry : villagerTypeReferenceList) {
				this.villagerTypes.add(entry.value());
			}
			Registry<net.minecraft.village.VillagerProfession> villagerProfessionRegistry = this.client.world.getRegistryManager().get(RegistryKeys.VILLAGER_PROFESSION);
			this.villagerProfessions.clear();
			List<RegistryEntry.Reference<VillagerProfession>> villagerProfessionReferenceList = villagerProfessionRegistry.streamEntries().toList();
			for (RegistryEntry.Reference<VillagerProfession> entry : villagerProfessionReferenceList) {
				this.villagerProfessions.add(entry.value());
			}
		}
	}

	@Override
	protected void init() {
		this.entityAttributeModifiersList.clear();
		Multimap<RegistryEntry<EntityAttribute>, EntityAttributeModifier> entityAttributeModifiers = Multimaps.newMultimap(Maps.newLinkedHashMap(), ArrayList::new);
		entityAttributeModifiers.putAll(this.triggeredVillagerSpawnerBlock.getEntityAttributeModifiers());
		List<RegistryEntry<EntityAttribute>> entityAttributeModifiersKeys = new ArrayList<>(entityAttributeModifiers.keySet());
		for (RegistryEntry<EntityAttribute> key : entityAttributeModifiersKeys) {
			Collection<EntityAttributeModifier> modifierCollection = entityAttributeModifiers.get(key);
			List<EntityAttributeModifier> modifierList = modifierCollection.stream().toList();
			for (EntityAttributeModifier entityAttributeModifier : modifierList) {
				this.entityAttributeModifiersList.add(new MutablePair<>(Registries.ATTRIBUTE.getId(key.value()), entityAttributeModifier));
			}
		}

		this.cycleScreenPageButton = this.addDrawableChild(CyclingButtonWidget.builder(ScreenPage::asText).values((ScreenPage[]) ScreenPage.values()).initially(this.screenPage).omitKeyText().build(this.width / 2 - 154, 20, 300, 20, Text.empty(), (button, screenPage) -> {
			this.screenPage = screenPage;
			this.updateWidgets();
		}));

		// --- misc page ---

		this.entitySpawnPositionOffsetXField = new TextFieldWidget(this.textRenderer, this.width / 2 - 154, 60, 100, 20, Text.empty());
		this.entitySpawnPositionOffsetXField.setText(Integer.toString(this.triggeredVillagerSpawnerBlock.getEntitySpawnPositionOffset().getX()));
		this.addSelectableChild(this.entitySpawnPositionOffsetXField);
		this.entitySpawnPositionOffsetYField = new TextFieldWidget(this.textRenderer, this.width / 2 - 50, 60, 100, 20, Text.empty());
		this.entitySpawnPositionOffsetYField.setText(Integer.toString(this.triggeredVillagerSpawnerBlock.getEntitySpawnPositionOffset().getY()));
		this.addSelectableChild(this.entitySpawnPositionOffsetYField);
		this.entitySpawnPositionOffsetZField = new TextFieldWidget(this.textRenderer, this.width / 2 + 54, 60, 100, 20, Text.empty());
		this.entitySpawnPositionOffsetZField.setText(Integer.toString(this.triggeredVillagerSpawnerBlock.getEntitySpawnPositionOffset().getZ()));
		this.addSelectableChild(this.entitySpawnPositionOffsetZField);

		this.entitySpawnOrientationPitchField = new TextFieldWidget(this.textRenderer, this.width / 2 - 154, 95, 150, 20, Text.empty());
		this.entitySpawnOrientationPitchField.setText(Double.toString(this.triggeredVillagerSpawnerBlock.getEntitySpawnOrientationPitch()));
		this.addSelectableChild(this.entitySpawnOrientationPitchField);
		this.entitySpawnOrientationYawField = new TextFieldWidget(this.textRenderer, this.width / 2 + 4, 95, 150, 20, Text.empty());
		this.entitySpawnOrientationYawField.setText(Double.toString(this.triggeredVillagerSpawnerBlock.getEntitySpawnOrientationYaw()));
		this.addSelectableChild(this.entitySpawnOrientationYawField);

		this.spawningMode = this.triggeredVillagerSpawnerBlock.getSpawningMode();
		this.cycleSpawningModeButton = this.addDrawableChild(CyclingButtonWidget.builder(TriggeredSpawnerBlockEntity.SpawningMode::asText).values((TriggeredSpawnerBlockEntity.SpawningMode[]) TriggeredSpawnerBlockEntity.SpawningMode.values()).initially(this.spawningMode).omitKeyText().build(this.width / 2 - 154, 130, 150, 20, Text.empty(), (button, spawningMode) -> {
			this.spawningMode = spawningMode;
		}));

		this.entityTypeIdField = new TextFieldWidget(this.textRenderer, this.width / 2 - 154, 165, 300, 20, Text.empty());
		this.entityTypeIdField.setMaxLength(128);
		this.entityTypeIdField.setText(this.triggeredVillagerSpawnerBlock.getEntityTypeId());
		this.addSelectableChild(this.entityTypeIdField);

		// --- villager data page ---

		VillagerData villagerData = this.triggeredVillagerSpawnerBlock.getVillagerData();

		this.updateVillagerRegistryLists();

		this.villagerType = villagerData.getType();
		this.cycleVillagerTypeButton = this.addDrawableChild(CyclingButtonWidget.<VillagerType>builder((villagerType) -> Text.translatable("gui.triggered_villager_spawner_block.villager_type." + villagerType.toString().replaceAll(":", ".")))
				.values(new CyclingButtonWidget.Values<VillagerType>() {
					@Override
					public List<VillagerType> getCurrent() {
						return TriggeredVillagerSpawnerBlockScreen.this.villagerTypes;
					}

					@Override
					public List<VillagerType> getDefaults() {
						return TriggeredVillagerSpawnerBlockScreen.this.villagerTypes;
					}
				})
				.initially(this.villagerType)
				.omitKeyText()
				.build(this.width / 2 - 154, 60, 300, 20, Text.empty(), (button, villagerType) -> {
					this.villagerType = villagerType;
				})
		);
		this.addSelectableChild(this.cycleVillagerTypeButton);

		this.villagerProfession = villagerData.getProfession();
		this.cycleVillagerProfessionButton = this.addDrawableChild(CyclingButtonWidget.<VillagerProfession>builder((villagerProfession) -> Text.translatable("gui.triggered_villager_spawner_block.villager_profession." + villagerProfession.toString().replaceAll(":", ".")))
				.values(new CyclingButtonWidget.Values<VillagerProfession>() {
					@Override
					public List<VillagerProfession> getCurrent() {
						return TriggeredVillagerSpawnerBlockScreen.this.villagerProfessions;
					}

					@Override
					public List<VillagerProfession> getDefaults() {
						return TriggeredVillagerSpawnerBlockScreen.this.villagerProfessions;
					}
				})
				.initially(this.villagerProfession)
				.omitKeyText()
				.build(this.width / 2 - 154, 95, 300, 20, Text.empty(), (button, villagerProfession) -> {
					ScriptBlocks.info("villagerProfession: " + villagerProfession);
					this.villagerProfession = villagerProfession;
				})
		);
		this.addSelectableChild(this.cycleVillagerProfessionButton);

		this.villagerLevelField = new TextFieldWidget(this.textRenderer, this.width / 2 - 154, 130, 300, 20, Text.empty());
		this.villagerLevelField.setMaxLength(128);
		this.villagerLevelField.setText(Integer.toString(villagerData.getLevel()));
		this.addSelectableChild(this.villagerLevelField);

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
		this.cycleNewEntityAttributeModifierOperationButton = this.addDrawableChild(CyclingButtonWidget.builder(TriggeredSpawnerBlockEntity.VisualEntityAttributeModifierOperation::asText).values(TriggeredSpawnerBlockEntity.VisualEntityAttributeModifierOperation.values()).initially(this.newEntityAttributeModifierOperation).omitKeyText().build(this.width / 2 - 50, 188, 100, 20, Text.empty(), (button, newEntityAttributeModifierOperationButton) -> {
			this.newEntityAttributeModifierOperation = newEntityAttributeModifierOperationButton;
			this.updateWidgets();
		}));
		this.addNewEntityAttributeModifierButton = this.addDrawableChild(ButtonWidget.builder(Text.translatable("gui.list_entry.add"), button -> this.addNewEntityAttributeModifier()).dimensions(this.width / 2 + 54, 188, 100, 20).build());

		// --- triggered block page ---

		this.useRelayBlockPositionOffsetXField = new TextFieldWidget(this.textRenderer, this.width / 2 - 154, 95, 100, 20, Text.empty());
		this.useRelayBlockPositionOffsetXField.setMaxLength(128);
		this.useRelayBlockPositionOffsetXField.setText(Integer.toString(this.triggeredVillagerSpawnerBlock.getUseRelayBlockPositionOffset().getX()));
		this.addSelectableChild(this.useRelayBlockPositionOffsetXField);
		this.useRelayBlockPositionOffsetYField = new TextFieldWidget(this.textRenderer, this.width / 2 - 50, 95, 100, 20, Text.empty());
		this.useRelayBlockPositionOffsetYField.setMaxLength(128);
		this.useRelayBlockPositionOffsetYField.setText(Integer.toString(this.triggeredVillagerSpawnerBlock.getUseRelayBlockPositionOffset().getY()));
		this.addSelectableChild(this.useRelayBlockPositionOffsetYField);
		this.useRelayBlockPositionOffsetZField = new TextFieldWidget(this.textRenderer, this.width / 2 + 54, 95, 100, 20, Text.empty());
		this.useRelayBlockPositionOffsetZField.setMaxLength(128);
		this.useRelayBlockPositionOffsetZField.setText(Integer.toString(this.triggeredVillagerSpawnerBlock.getUseRelayBlockPositionOffset().getZ()));
		this.addSelectableChild(this.useRelayBlockPositionOffsetZField);

		// --- triggered block page ---

		this.triggeredBlockPositionOffsetXField = new TextFieldWidget(this.textRenderer, this.width / 2 - 154, 95, 50, 20, Text.empty());
		this.triggeredBlockPositionOffsetXField.setMaxLength(128);
		this.triggeredBlockPositionOffsetXField.setText(Integer.toString(this.triggeredVillagerSpawnerBlock.getTriggeredBlock().getLeft().getX()));
		this.addSelectableChild(this.triggeredBlockPositionOffsetXField);
		this.triggeredBlockPositionOffsetYField = new TextFieldWidget(this.textRenderer, this.width / 2 - 100, 95, 50, 20, Text.empty());
		this.triggeredBlockPositionOffsetYField.setMaxLength(128);
		this.triggeredBlockPositionOffsetYField.setText(Integer.toString(this.triggeredVillagerSpawnerBlock.getTriggeredBlock().getLeft().getY()));
		this.addSelectableChild(this.triggeredBlockPositionOffsetYField);
		this.triggeredBlockPositionOffsetZField = new TextFieldWidget(this.textRenderer, this.width / 2 - 46, 95, 50, 20, Text.empty());
		this.triggeredBlockPositionOffsetZField.setMaxLength(128);
		this.triggeredBlockPositionOffsetZField.setText(Integer.toString(this.triggeredVillagerSpawnerBlock.getTriggeredBlock().getLeft().getZ()));
		this.addSelectableChild(this.triggeredBlockPositionOffsetZField);
		this.triggeredBlockResets = this.triggeredVillagerSpawnerBlock.getTriggeredBlock().getRight();
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

		this.entityTypeIdField.setVisible(false);

		this.cycleVillagerTypeButton.visible = false;
		this.cycleVillagerProfessionButton.visible = false;
		this.villagerLevelField.setVisible(false);

		this.removeListEntryButton0.visible = false;
		this.removeListEntryButton1.visible = false;
		this.removeListEntryButton2.visible = false;
		this.newEntityAttributeModifierIdentifierField.setVisible(false);
		this.newEntityAttributeModifierNameField.setVisible(false);
		this.newEntityAttributeModifierValueField.setVisible(false);
		this.cycleNewEntityAttributeModifierOperationButton.visible = false;
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

			this.entityTypeIdField.setVisible(true);

		} else if (this.screenPage == ScreenPage.VILLAGER_DATA) {

			this.cycleVillagerTypeButton.visible = true;
			this.cycleVillagerProfessionButton.visible = true;
			this.villagerLevelField.setVisible(true);

		} else if (this.screenPage == ScreenPage.ENTITY_ATTRIBUTE_MODIFIER) {

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
			this.cycleNewEntityAttributeModifierOperationButton.visible = true;
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
		VillagerType var3 = this.villagerType;
		VillagerProfession var4 = this.villagerProfession;
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
		String string15 = this.villagerLevelField.getText();
		boolean bl = this.triggeredBlockResets;
		this.init(client, width, height);
		this.screenPage = var;
		this.cycleScreenPageButton.setValue(this.screenPage);
		this.spawningMode = var1;
		this.cycleSpawningModeButton.setValue(this.spawningMode);
		this.newEntityAttributeModifierOperation = var2;
		this.cycleNewEntityAttributeModifierOperationButton.setValue(this.newEntityAttributeModifierOperation);
		this.villagerType = var3;
		this.cycleVillagerTypeButton.setValue(this.villagerType);
		this.villagerProfession = var4;
		this.cycleVillagerProfessionButton.setValue(this.villagerProfession);
		this.entityAttributeModifiersList.clear();
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
		this.villagerLevelField.setText(string15);
		this.triggeredBlockResets = bl;
		this.toggleTriggeredBlockResetsButton.setValue(this.triggeredBlockResets);
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		this.mouseClicked = false;
		if (this.screenPage == ScreenPage.ENTITY_ATTRIBUTE_MODIFIER
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
		if (this.screenPage == ScreenPage.ENTITY_ATTRIBUTE_MODIFIER
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
		if (this.screenPage == ScreenPage.ENTITY_ATTRIBUTE_MODIFIER
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

			context.drawTextWithShadow(this.textRenderer, ENTITY_TYPE_LABEL_TEXT, this.width / 2 - 153, 155, 0xA0A0A0);
			this.entityTypeIdField.render(context, mouseX, mouseY, delta);
		} else if (this.screenPage == ScreenPage.VILLAGER_DATA) {

			context.drawTextWithShadow(this.textRenderer, VILLAGER_TYPE_LABEL_TEXT, this.width / 2 - 153, 50, 0xA0A0A0);
			context.drawTextWithShadow(this.textRenderer, VILLAGER_PROFESSION_LABEL_TEXT, this.width / 2 - 153, 85, 0xA0A0A0);
			context.drawTextWithShadow(this.textRenderer, VILLAGER_LEVEL_LABEL_TEXT, this.width / 2 - 153, 120, 0xA0A0A0);
			this.villagerLevelField.render(context, mouseX, mouseY, delta);

		} else if (this.screenPage == ScreenPage.ENTITY_ATTRIBUTE_MODIFIER) {

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
				new UpdateTriggeredVillagerSpawnerBlockPacket(
						this.triggeredVillagerSpawnerBlock.getPos(),
						new BlockPos(
								ItemUtils.parseInt(this.entitySpawnPositionOffsetXField.getText()),
								ItemUtils.parseInt(this.entitySpawnPositionOffsetYField.getText()),
								ItemUtils.parseInt(this.entitySpawnPositionOffsetZField.getText())
						),
						ItemUtils.parseDouble(this.entitySpawnOrientationPitchField.getText()),
						ItemUtils.parseDouble(this.entitySpawnOrientationYawField.getText()),
						this.spawningMode.asString(),
						this.entityTypeIdField.getText(),
						new VillagerData(this.villagerType, this.villagerProfession, Math.clamp(ItemUtils.parseInt(this.villagerLevelField.getText()), 1, 5)),
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
		return true;
	}

	public enum ScreenPage implements StringIdentifiable {
		MISC("misc"),
		VILLAGER_DATA("villager_data"),
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
