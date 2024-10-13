package com.github.theredbrain.scriptblocks.gui.screen.ingame;

import com.github.theredbrain.scriptblocks.ScriptBlocks;
import com.github.theredbrain.scriptblocks.block.entity.HousingBlockEntity;
import com.github.theredbrain.scriptblocks.network.packet.AddStatusEffectPacket;
import com.github.theredbrain.scriptblocks.network.packet.LeaveHouseFromHousingScreenPacket;
import com.github.theredbrain.scriptblocks.network.packet.ResetHouseHousingBlockPacket;
import com.github.theredbrain.scriptblocks.network.packet.SetHousingBlockOwnerPacket;
import com.github.theredbrain.scriptblocks.network.packet.UpdateHousingBlockAdventurePacket;
import com.github.theredbrain.scriptblocks.network.packet.UpdateHousingBlockCreativePacket;
import com.github.theredbrain.scriptblocks.registry.StatusEffectsRegistry;
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
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3i;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Environment(value = EnvType.CLIENT)
public class CreativeHousingScreen extends Screen {
	// adventure
	private static final Text TITLE_OWNER_LABEL_TEXT = Text.translatable("gui.housing_screen.title.owner");
	private static final Text TITLE_CO_OWNER_LABEL_TEXT = Text.translatable("gui.housing_screen.title.co_owner");
	private static final Text TITLE_CO_OWNER_LIST_LABEL_TEXT = Text.translatable("gui.housing_screen.co_owner_list.title");
	private static final Text TITLE_CO_OWNER_LIST_DESCRIPTION_LABEL_TEXT = Text.translatable("gui.housing_screen.co_owner_list.description");
	private static final Text TITLE_TRUSTED_LABEL_TEXT = Text.translatable("gui.housing_screen.title.trusted");
	private static final Text TITLE_TRUSTED_LIST_LABEL_TEXT = Text.translatable("gui.housing_screen.trusted_list.title");
	private static final Text TITLE_TRUSTED_LIST_DESCRIPTION_LABEL_TEXT = Text.translatable("gui.housing_screen.trusted_list.description");
	private static final Text TITLE_GUEST_LABEL_TEXT = Text.translatable("gui.housing_screen.title.guest");
	private static final Text TITLE_GUEST_LIST_LABEL_TEXT = Text.translatable("gui.housing_screen.guest_list.title");
	private static final Text TITLE_GUEST_LIST_DESCRIPTION_LABEL_TEXT = Text.translatable("gui.housing_screen.guest_list.description");
	private static final Text TITLE_STRANGER_LABEL_TEXT = Text.translatable("gui.housing_screen.title.stranger");
	private static final Text LEAVE_CURRENT_HOUSE_BUTTON_LABEL_TEXT = Text.translatable("gui.housing_screen.leave_current_house_button_label");
	private static final Text OPEN_RESET_HOUSE_SCREEN_BUTTON_LABEL_TEXT = Text.translatable("gui.housing_screen.open_reset_house_screen_button_label");
	private static final Text TOGGLE_ADVENTURE_BUILDING_OFF_BUTTON_LABEL_TEXT = Text.translatable("gui.housing_screen.toggle_adventure_building_off_button_label");
	private static final Text TOGGLE_ADVENTURE_BUILDING_ON_BUTTON_LABEL_TEXT = Text.translatable("gui.housing_screen.toggle_adventure_building_on_button_label");
	private static final Text UNCLAIM_HOUSE_BUTTON_LABEL_TEXT = Text.translatable("gui.housing_screen.unclaim_house_button_label");
	private static final Text CLAIM_HOUSE_BUTTON_LABEL_TEXT = Text.translatable("gui.housing_screen.claim_house_button_label");
	private static final Text OPEN_CO_OWNER_LIST_BUTTON_LABEL_TEXT = Text.translatable("gui.housing_screen.open_co_owner_list_button_label");
	private static final Text NEW_CO_OWNER_FIELD_PLACEHOLDER_TEXT = Text.translatable("gui.housing_screen.new_co_owner_field.place_holder");
	private static final Text ADD_NEW_CO_OWNER_BUTTON_LABEL_TEXT = Text.translatable("gui.housing_screen.add_new_co_owner_button_label");
	private static final Text OPEN_TRUSTED_PERSONS_LIST_BUTTON_LABEL_TEXT = Text.translatable("gui.housing_screen.open_trusted_list_button_label");
	private static final Text NEW_TRUSTED_PERSON_FIELD_PLACEHOLDER_TEXT = Text.translatable("gui.housing_screen.new_trusted_person_field.place_holder");
	private static final Text ADD_NEW_TRUSTED_PERSON_BUTTON_LABEL_TEXT = Text.translatable("gui.housing_screen.add_new_trusted_person_button_label");
	private static final Text OPEN_GUEST_LIST_BUTTON_LABEL_TEXT = Text.translatable("gui.housing_screen.open_guest_list_button_label");
	private static final Text NEW_GUEST_FIELD_PLACEHOLDER_TEXT = Text.translatable("gui.housing_screen.new_guest_field.place_holder");
	private static final Text ADD_NEW_GUEST_BUTTON_LABEL_TEXT = Text.translatable("gui.housing_screen.add_new_guest_button_label");
	private static final Text REMOVE_LIST_ENTRY_BUTTON_LABEL_TEXT = Text.translatable("gui.housing_screen.remove_list_entry_button_label");

	// creative
	private static final Text HIDE_INFLUENCE_AREA_LABEL_TEXT = Text.translatable("gui.housing_screen.hide_influence_area_label");
	private static final Text SHOW_INFLUENCE_AREA_LABEL_TEXT = Text.translatable("gui.housing_screen.show_influence_area_label");
	private static final Text INFLUENCE_AREA_DIMENSIONS_LABEL_TEXT = Text.translatable("gui.housing_screen.influence_area_dimensions_label");
	private static final Text INFLUENCE_AREA_POSITION_OFFET_LABEL_TEXT = Text.translatable("gui.housing_screen.influence_area_position_offset_label");
	private static final Text RESET_OWNER_BUTTON_LABEL_TEXT = Text.translatable("gui.housing_block.reset_owner_button_label");
	private static final Text TRIGGERED_BLOCK_POSITION_OFFSET_LABEL_TEXT = Text.translatable("gui.triggered_block.triggeredBlockPositionOffset");
	public static final Identifier BACKGROUND_218_215_TEXTURE = ScriptBlocks.identifier("textures/gui/container/generic_218_215_background.png");
	public static final Identifier BACKGROUND_218_95_TEXTURE = ScriptBlocks.identifier("textures/gui/container/generic_218_95_background.png");
	public static final Identifier BACKGROUND_218_71_TEXTURE = ScriptBlocks.identifier("textures/gui/container/generic_218_71_background.png");
	private static final Identifier PLAYER_LISTS_SCROLLER_BACKGROUND_TEXTURE = ScriptBlocks.identifier("container/housing_screen/player_lists_scroller_background");
	private static final Identifier SCROLLER_TEXTURE = ScriptBlocks.identifier("container/scroller");
	@Nullable
	private final HousingBlockEntity housingBlockEntity;

	private CyclingButtonWidget<CreativeScreenPage> creativeScreenPageButton;
	private CyclingButtonWidget<Boolean> showRestrictBlockBreakingAreaButton;
	private TextFieldWidget restrictBlockBreakingAreaDimensionsXField;
	private TextFieldWidget restrictBlockBreakingAreaDimensionsYField;
	private TextFieldWidget restrictBlockBreakingAreaDimensionsZField;
	private TextFieldWidget restrictBlockBreakingAreaPositionOffsetXField;
	private TextFieldWidget restrictBlockBreakingAreaPositionOffsetYField;
	private TextFieldWidget restrictBlockBreakingAreaPositionOffsetZField;
	private TextFieldWidget triggeredBlockPositionOffsetXField;
	private TextFieldWidget triggeredBlockPositionOffsetYField;
	private TextFieldWidget triggeredBlockPositionOffsetZField;
	private CyclingButtonWidget<Boolean> toggleTriggeredBlockResetsButton;
	private boolean triggeredBlockResets;
	private CyclingButtonWidget<HousingBlockEntity.OwnerMode> toggleOwnerModeButton;
	private ButtonWidget resetOwnerButton;
	private ButtonWidget saveCreativeButton;
	private ButtonWidget cancelCreativeButton;

	private CreativeScreenPage creativeScreenPage;
	private List<String> coOwnerList = new ArrayList<>(List.of());
	private List<String> trustedPersonsList = new ArrayList<>(List.of());
	private List<String> guestList = new ArrayList<>(List.of());
	private boolean showInfluenceArea = false;
	private int backgroundWidth;
	private int backgroundHeight;
	private int x;
	private int y;
	private int scrollPosition = 0;
	private float scrollAmount = 0.0f;
	private boolean mouseClicked = false;
	private HousingBlockEntity.OwnerMode ownerMode = HousingBlockEntity.OwnerMode.DIMENSION_OWNER;

	public CreativeHousingScreen(@Nullable HousingBlockEntity housingBlockEntity) {
		super(NarratorManager.EMPTY);
		this.housingBlockEntity = housingBlockEntity;
		this.creativeScreenPage = CreativeScreenPage.INFLUENCE;
	}

	private void saveCreative() {
		if (this.updateHousingBlockCreative()) {
			this.close();
		}
	}

	private void cancel() {
		this.close();
	}

	@Override
	protected void init() {
		this.coOwnerList.clear();
		this.trustedPersonsList.clear();
		this.guestList.clear();
		if (this.housingBlockEntity != null) {
			this.coOwnerList.addAll(this.housingBlockEntity.getCoOwnerList());
			this.trustedPersonsList.addAll(this.housingBlockEntity.getTrustedList());
			this.guestList.addAll(this.housingBlockEntity.getGuestList());
			this.showInfluenceArea = housingBlockEntity.getShowInfluenceArea();
			this.ownerMode = housingBlockEntity.getOwnerMode();
		}
			this.backgroundWidth = 218;
			this.backgroundHeight = this.ownerMode == HousingBlockEntity.OwnerMode.INTERACTION ? 95 : 71;
			this.x = (this.width - this.backgroundWidth) / 2;
			this.y = (this.height - this.backgroundHeight) / 2;

		super.init();

		this.creativeScreenPageButton = this.addDrawableChild(CyclingButtonWidget.builder(CreativeScreenPage::asText).values((CreativeScreenPage[]) CreativeScreenPage.values()).initially(this.creativeScreenPage).omitKeyText().build(this.width / 2 - 154, 20, 300, 20, Text.empty(), (button, creativeScreenPage) -> {
			this.creativeScreenPage = creativeScreenPage;
			this.updateWidgets();
		}));

		// --- influence area page ---

		this.showRestrictBlockBreakingAreaButton = this.addDrawableChild(CyclingButtonWidget.onOffBuilder(HIDE_INFLUENCE_AREA_LABEL_TEXT, SHOW_INFLUENCE_AREA_LABEL_TEXT).initially(this.showInfluenceArea).omitKeyText().build(this.width / 2 - 153, 45, 300, 20, Text.empty(), (button, showInfluenceArea) -> {
			this.showInfluenceArea = showInfluenceArea;
		}));

		this.restrictBlockBreakingAreaDimensionsXField = new TextFieldWidget(this.textRenderer, this.width / 2 - 154, 80, 100, 20, Text.empty());
		this.restrictBlockBreakingAreaDimensionsXField.setMaxLength(128);
		this.restrictBlockBreakingAreaDimensionsXField.setText(Integer.toString(this.housingBlockEntity != null ? this.housingBlockEntity.getInfluenceAreaDimensions().getX() : 0));
		this.addSelectableChild(this.restrictBlockBreakingAreaDimensionsXField);

		this.restrictBlockBreakingAreaDimensionsYField = new TextFieldWidget(this.textRenderer, this.width / 2 - 50, 80, 100, 20, Text.empty());
		this.restrictBlockBreakingAreaDimensionsYField.setMaxLength(128);
		this.restrictBlockBreakingAreaDimensionsYField.setText(Integer.toString(this.housingBlockEntity != null ? this.housingBlockEntity.getInfluenceAreaDimensions().getY() : 0));
		this.addSelectableChild(this.restrictBlockBreakingAreaDimensionsYField);

		this.restrictBlockBreakingAreaDimensionsZField = new TextFieldWidget(this.textRenderer, this.width / 2 + 54, 80, 100, 20, Text.empty());
		this.restrictBlockBreakingAreaDimensionsZField.setMaxLength(128);
		this.restrictBlockBreakingAreaDimensionsZField.setText(Integer.toString(this.housingBlockEntity != null ? this.housingBlockEntity.getInfluenceAreaDimensions().getZ() : 0));
		this.addSelectableChild(this.restrictBlockBreakingAreaDimensionsZField);

		this.restrictBlockBreakingAreaPositionOffsetXField = new TextFieldWidget(this.textRenderer, this.width / 2 - 154, 115, 100, 20, Text.empty());
		this.restrictBlockBreakingAreaPositionOffsetXField.setMaxLength(128);
		this.restrictBlockBreakingAreaPositionOffsetXField.setText(Integer.toString(this.housingBlockEntity != null ? this.housingBlockEntity.getRestrictBlockBreakingAreaPositionOffset().getX() : 0));
		this.addSelectableChild(this.restrictBlockBreakingAreaPositionOffsetXField);

		this.restrictBlockBreakingAreaPositionOffsetYField = new TextFieldWidget(this.textRenderer, this.width / 2 - 50, 115, 100, 20, Text.empty());
		this.restrictBlockBreakingAreaPositionOffsetYField.setMaxLength(128);
		this.restrictBlockBreakingAreaPositionOffsetYField.setText(Integer.toString(this.housingBlockEntity != null ? this.housingBlockEntity.getRestrictBlockBreakingAreaPositionOffset().getY() : 0));
		this.addSelectableChild(this.restrictBlockBreakingAreaPositionOffsetYField);

		this.restrictBlockBreakingAreaPositionOffsetZField = new TextFieldWidget(this.textRenderer, this.width / 2 + 54, 115, 100, 20, Text.empty());
		this.restrictBlockBreakingAreaPositionOffsetZField.setMaxLength(128);
		this.restrictBlockBreakingAreaPositionOffsetZField.setText(Integer.toString(this.housingBlockEntity != null ? this.housingBlockEntity.getRestrictBlockBreakingAreaPositionOffset().getZ() : 0));
		this.addSelectableChild(this.restrictBlockBreakingAreaPositionOffsetZField);

		// --- triggered block page ---

		this.triggeredBlockPositionOffsetXField = new TextFieldWidget(this.textRenderer, this.width / 2 - 154, 80, 50, 20, Text.empty());
		this.triggeredBlockPositionOffsetXField.setMaxLength(128);
		this.triggeredBlockPositionOffsetXField.setText(Integer.toString(this.housingBlockEntity != null ? this.housingBlockEntity.getTriggeredBlock().getLeft().getX() : 0));
		this.addSelectableChild(this.triggeredBlockPositionOffsetXField);

		this.triggeredBlockPositionOffsetYField = new TextFieldWidget(this.textRenderer, this.width / 2 - 100, 80, 50, 20, Text.empty());
		this.triggeredBlockPositionOffsetYField.setMaxLength(128);
		this.triggeredBlockPositionOffsetYField.setText(Integer.toString(this.housingBlockEntity != null ? this.housingBlockEntity.getTriggeredBlock().getLeft().getY() : 0));
		this.addSelectableChild(this.triggeredBlockPositionOffsetYField);

		this.triggeredBlockPositionOffsetZField = new TextFieldWidget(this.textRenderer, this.width / 2 - 46, 80, 50, 20, Text.empty());
		this.triggeredBlockPositionOffsetZField.setMaxLength(128);
		this.triggeredBlockPositionOffsetZField.setText(Integer.toString(this.housingBlockEntity != null ? this.housingBlockEntity.getTriggeredBlock().getLeft().getZ() : 0));
		this.addSelectableChild(this.triggeredBlockPositionOffsetZField);

		this.triggeredBlockResets = this.housingBlockEntity != null ? this.housingBlockEntity.getTriggeredBlock().getRight() : false;
		this.toggleTriggeredBlockResetsButton = this.addDrawableChild(CyclingButtonWidget.onOffBuilder(Text.translatable("gui.triggered_block.toggle_triggered_block_resets_button_label.on"), Text.translatable("gui.triggered_block.toggle_triggered_block_resets_button_label.off")).initially(this.triggeredBlockResets).omitKeyText().build(this.width / 2 + 8, 80, 150, 20, Text.empty(), (button, triggeredBlockResets) -> {
			this.triggeredBlockResets = triggeredBlockResets;
		}));
		// --- owner page ---

		this.toggleOwnerModeButton = this.addDrawableChild(CyclingButtonWidget.builder(HousingBlockEntity.OwnerMode::asText).values((HousingBlockEntity.OwnerMode[]) HousingBlockEntity.OwnerMode.values()).initially(this.ownerMode).omitKeyText().build(this.width / 2 - 153, 70, 300, 20, Text.empty(), (button, ownerMode) -> {
			this.ownerMode = ownerMode;
		}));

		this.resetOwnerButton = this.addDrawableChild(ButtonWidget.builder(RESET_OWNER_BUTTON_LABEL_TEXT, button -> this.trySetHouseOwner(false)).dimensions(this.width / 2 - 4 - 150, 94, 300, 20).build());

		this.saveCreativeButton = this.addDrawableChild(ButtonWidget.builder(ScreenTexts.DONE, button -> this.saveCreative()).dimensions(this.width / 2 - 4 - 150, 210, 150, 20).build());
		this.cancelCreativeButton = this.addDrawableChild(ButtonWidget.builder(ScreenTexts.CANCEL, button -> this.cancel()).dimensions(this.width / 2 + 4, 210, 150, 20).build());

		this.updateWidgets();
	}

	private void updateWidgets() {

		this.creativeScreenPageButton.visible = false;

		this.showRestrictBlockBreakingAreaButton.visible = false;
		this.restrictBlockBreakingAreaDimensionsXField.setVisible(false);
		this.restrictBlockBreakingAreaDimensionsYField.setVisible(false);
		this.restrictBlockBreakingAreaDimensionsZField.setVisible(false);
		this.restrictBlockBreakingAreaPositionOffsetXField.setVisible(false);
		this.restrictBlockBreakingAreaPositionOffsetYField.setVisible(false);
		this.restrictBlockBreakingAreaPositionOffsetZField.setVisible(false);

		this.triggeredBlockPositionOffsetXField.setVisible(false);
		this.triggeredBlockPositionOffsetYField.setVisible(false);
		this.triggeredBlockPositionOffsetZField.setVisible(false);
		this.toggleTriggeredBlockResetsButton.visible = false;

		this.toggleOwnerModeButton.visible = false;
		this.resetOwnerButton.visible = false;

		this.saveCreativeButton.visible = false;
		this.cancelCreativeButton.visible = false;

			this.creativeScreenPageButton.visible = true;

			if (this.creativeScreenPage == CreativeScreenPage.INFLUENCE) {

				this.showRestrictBlockBreakingAreaButton.visible = true;
				this.restrictBlockBreakingAreaDimensionsXField.setVisible(true);
				this.restrictBlockBreakingAreaDimensionsYField.setVisible(true);
				this.restrictBlockBreakingAreaDimensionsZField.setVisible(true);
				this.restrictBlockBreakingAreaPositionOffsetXField.setVisible(true);
				this.restrictBlockBreakingAreaPositionOffsetYField.setVisible(true);
				this.restrictBlockBreakingAreaPositionOffsetZField.setVisible(true);

			} else if (this.creativeScreenPage == CreativeScreenPage.TRIGGERED_BLOCK) {

				this.triggeredBlockPositionOffsetXField.setVisible(true);
				this.triggeredBlockPositionOffsetYField.setVisible(true);
				this.triggeredBlockPositionOffsetZField.setVisible(true);
				this.toggleTriggeredBlockResetsButton.visible = true;

			} else if (this.creativeScreenPage == CreativeScreenPage.OWNER) {

				this.toggleOwnerModeButton.visible = true;
				this.resetOwnerButton.visible = true;

			}

			this.saveCreativeButton.visible = true;
			this.cancelCreativeButton.visible = true;
	}

	@Override
	public void resize(MinecraftClient client, int width, int height) {
		List<String> list = new ArrayList<>(this.coOwnerList);
		List<String> list1 = new ArrayList<>(this.trustedPersonsList);
		List<String> list2 = new ArrayList<>(this.guestList);
		boolean bool = this.showInfluenceArea;
		HousingBlockEntity.OwnerMode var = this.ownerMode;
		int number = this.scrollPosition;
		float number1 = this.scrollAmount;
		String string3 = this.restrictBlockBreakingAreaDimensionsXField.getText();
		String string4 = this.restrictBlockBreakingAreaDimensionsYField.getText();
		String string5 = this.restrictBlockBreakingAreaDimensionsZField.getText();
		String string6 = this.restrictBlockBreakingAreaPositionOffsetXField.getText();
		String string7 = this.restrictBlockBreakingAreaPositionOffsetYField.getText();
		String string8 = this.restrictBlockBreakingAreaPositionOffsetZField.getText();
		String string9 = this.triggeredBlockPositionOffsetXField.getText();
		String string10 = this.triggeredBlockPositionOffsetYField.getText();
		String string11 = this.triggeredBlockPositionOffsetZField.getText();
		boolean boolean2 = this.triggeredBlockResets;
		this.init(client, width, height);
		this.coOwnerList.clear();
		this.trustedPersonsList.clear();
		this.guestList.clear();
		this.coOwnerList.addAll(list);
		this.trustedPersonsList.addAll(list1);
		this.guestList.addAll(list2);
		this.showInfluenceArea = bool;
		this.ownerMode = var;
		this.scrollPosition = number;
		this.scrollAmount = number1;
		this.restrictBlockBreakingAreaDimensionsXField.setText(string3);
		this.restrictBlockBreakingAreaDimensionsYField.setText(string4);
		this.restrictBlockBreakingAreaDimensionsZField.setText(string5);
		this.restrictBlockBreakingAreaPositionOffsetXField.setText(string6);
		this.restrictBlockBreakingAreaPositionOffsetYField.setText(string7);
		this.restrictBlockBreakingAreaPositionOffsetZField.setText(string8);
		this.triggeredBlockPositionOffsetXField.setText(string9);
		this.triggeredBlockPositionOffsetYField.setText(string10);
		this.triggeredBlockPositionOffsetZField.setText(string11);
		this.triggeredBlockResets = boolean2;
		this.updateWidgets();
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
			this.saveCreative();
			return true;
		}
		return super.keyPressed(keyCode, scanCode, modifiers);
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {

		super.render(context, mouseX, mouseY, delta);

			if (this.creativeScreenPage == CreativeScreenPage.INFLUENCE) {
				context.drawTextWithShadow(this.textRenderer, INFLUENCE_AREA_DIMENSIONS_LABEL_TEXT, this.width / 2 - 153, 70, 0xA0A0A0);
				this.restrictBlockBreakingAreaDimensionsXField.render(context, mouseX, mouseY, delta);
				this.restrictBlockBreakingAreaDimensionsYField.render(context, mouseX, mouseY, delta);
				this.restrictBlockBreakingAreaDimensionsZField.render(context, mouseX, mouseY, delta);
				context.drawTextWithShadow(this.textRenderer, INFLUENCE_AREA_POSITION_OFFET_LABEL_TEXT, this.width / 2 - 153, 105, 0xA0A0A0);
				this.restrictBlockBreakingAreaPositionOffsetXField.render(context, mouseX, mouseY, delta);
				this.restrictBlockBreakingAreaPositionOffsetYField.render(context, mouseX, mouseY, delta);
				this.restrictBlockBreakingAreaPositionOffsetZField.render(context, mouseX, mouseY, delta);
			} else if (this.creativeScreenPage == CreativeScreenPage.TRIGGERED_BLOCK) {
				context.drawTextWithShadow(this.textRenderer, TRIGGERED_BLOCK_POSITION_OFFSET_LABEL_TEXT, this.width / 2 - 153, 70, 0xA0A0A0);
				this.triggeredBlockPositionOffsetXField.render(context, mouseX, mouseY, delta);
				this.triggeredBlockPositionOffsetYField.render(context, mouseX, mouseY, delta);
				this.triggeredBlockPositionOffsetZField.render(context, mouseX, mouseY, delta);
			}
	}

	@Override
	public boolean shouldPause() {
		return false;
	}

	private boolean updateHousingBlockCreative() {
		BlockPos housingBlockPos = new BlockPos(0, 0, 0);
		if (this.housingBlockEntity != null) {
			housingBlockPos = this.housingBlockEntity.getPos();
		}
		ClientPlayNetworking.send(new UpdateHousingBlockCreativePacket(
				housingBlockPos,
				this.showInfluenceArea,
				new Vec3i(
						ItemUtils.parseInt(this.restrictBlockBreakingAreaDimensionsXField.getText()),
						ItemUtils.parseInt(this.restrictBlockBreakingAreaDimensionsYField.getText()),
						ItemUtils.parseInt(this.restrictBlockBreakingAreaDimensionsZField.getText())
				),
				new BlockPos(
						ItemUtils.parseInt(this.restrictBlockBreakingAreaPositionOffsetXField.getText()),
						ItemUtils.parseInt(this.restrictBlockBreakingAreaPositionOffsetYField.getText()),
						ItemUtils.parseInt(this.restrictBlockBreakingAreaPositionOffsetZField.getText())
				),
				new BlockPos(
						ItemUtils.parseInt(this.triggeredBlockPositionOffsetXField.getText()),
						ItemUtils.parseInt(this.triggeredBlockPositionOffsetYField.getText()),
						ItemUtils.parseInt(this.triggeredBlockPositionOffsetZField.getText())
				),
				this.triggeredBlockResets,
				this.ownerMode.asString()
		));
		return true;
	}

	private void trySetHouseOwner(boolean claim) {
		if (this.housingBlockEntity != null && this.client != null && this.client.player != null) {
			ClientPlayNetworking.send(new SetHousingBlockOwnerPacket(
					this.housingBlockEntity.getPos(),
					claim ? this.client.player.getUuidAsString() : ""
			));
		}
		this.close();
	}

	public static enum CreativeScreenPage implements StringIdentifiable {
		INFLUENCE("influence"),
		TRIGGERED_BLOCK("triggered_block"),
		OWNER("owner");

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
			return Text.translatable("gui.housing_screen.creativeScreenPage." + this.name);
		}
	}
}
