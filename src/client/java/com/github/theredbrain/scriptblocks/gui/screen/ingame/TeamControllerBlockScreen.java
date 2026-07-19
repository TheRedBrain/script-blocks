package com.github.theredbrain.scriptblocks.gui.screen.ingame;

import com.github.theredbrain.scriptblocks.block.entity.TeamControllerBlockEntity;
import com.github.theredbrain.scriptblocks.network.packet.UpdateTeamControllerBlockPacket;
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
import net.minecraft.scoreboard.AbstractTeam;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;

import java.util.Arrays;
import java.util.Optional;

@Environment(value = EnvType.CLIENT)
public class TeamControllerBlockScreen extends Screen {
	private static final Text HIDE_AREA_LABEL_TEXT = Text.translatable("gui.team_controller_block.hide_area_label");
	private static final Text SHOW_AREA_LABEL_TEXT = Text.translatable("gui.team_controller_block.show_area_label");
	private static final Text AREA_DIMENSIONS_LABEL_TEXT = Text.translatable("gui.team_controller_block.area_dimensions_label");
	private static final Text AREA_POSITION_OFFET_LABEL_TEXT = Text.translatable("gui.team_controller_block.area_position_offset_label");
	private static final Text PVP_CONTROLLER_BLOCK_POSITION_OFFET_LABEL_TEXT = Text.translatable("gui.team_controller_block.pvp_controller_block_position_offset_label");
	private static final Text TEAM_IDENTIFIER_LABEL_TEXT = Text.translatable("gui.team_controller_block.team_identifier_label");
	private static final Text DISPLAY_NAME_LABEL_TEXT = Text.translatable("gui.team_controller_block.display_name_label");
	private static final Text TEAM_COLOR_LABEL_TEXT = Text.translatable("gui.team_controller_block.team_color_label");
	private static final Text FRIENDLY_FIRE_LABEL_TEXT = Text.translatable("gui.team_controller_block.friendly_fire_label");
	private static final Text FRIENDLY_INVISIBLES_LABEL_TEXT = Text.translatable("gui.team_controller_block.friendly_invisibles_label");
	private static final Text NAME_TAG_VISIBILITY_LABEL_TEXT = Text.translatable("gui.team_controller_block.name_tag_visibility_label");
	private static final Text DEATH_MESSAGE_VISIBILITY_LABEL_TEXT = Text.translatable("gui.team_controller_block.death_message_visibility_label");
	private static final Text COLLISION_RULE_LABEL_TEXT = Text.translatable("gui.team_controller_block.collision_rule_label");
	private static final Text PREFIX_LABEL_TEXT = Text.translatable("gui.team_controller_block.prefix_label");
	private static final Text SUFFIX_LABEL_TEXT = Text.translatable("gui.team_controller_block.suffix_label");
	private final TeamControllerBlockEntity teamControllerBlockEntity;
	private ScreenPage screenPage;
	private CyclingButtonWidget<ScreenPage> cycleScreenPageButton;
	private CyclingButtonWidget<Boolean> toggleShowAreaButton;
	private TextFieldWidget areaDimensionsXField;
	private TextFieldWidget areaDimensionsYField;
	private TextFieldWidget areaDimensionsZField;
	private TextFieldWidget areaPositionOffsetXField;
	private TextFieldWidget areaPositionOffsetYField;
	private TextFieldWidget areaPositionOffsetZField;
	private boolean showArea;
	private TextFieldWidget pvpControllerBlockPositionOffsetXField;
	private TextFieldWidget pvpControllerBlockPositionOffsetYField;
	private TextFieldWidget pvpControllerBlockPositionOffsetZField;

	private TextFieldWidget teamIdentifierField;
	private TextFieldWidget displayNameField;
	private ButtonWidget cycleTeamColorButton;
	private Formatting teamColor;
	private CyclingButtonWidget<Boolean> toggleFriendlyFireButton;
	private CyclingButtonWidget<Boolean> toggleShowFriendlyInvisiblesButton;
	private boolean friendlyFire;
	private boolean showFriendlyInvisibles;
	private CyclingButtonWidget<AbstractTeam.VisibilityRule> cycleNametagVisibilityButton;
	private CyclingButtonWidget<AbstractTeam.VisibilityRule> cycleDeathMessageVisibilityButton;
	private CyclingButtonWidget<AbstractTeam.CollisionRule> cycleCollisionRuleButton;
	private AbstractTeam.VisibilityRule nametagVisibility;
	private AbstractTeam.VisibilityRule deathMessageVisibility;
	private AbstractTeam.CollisionRule collisionRule;
	private TextFieldWidget prefixField;
	private TextFieldWidget suffixField;

	public TeamControllerBlockScreen(TeamControllerBlockEntity teamControllerBlockEntity) {
		super(NarratorManager.EMPTY);
		this.teamControllerBlockEntity = teamControllerBlockEntity;
		this.screenPage = ScreenPage.AREA;
	}

	private void cycleTeamColor() {
		int newIndex = this.teamColor.getColorIndex() + 1;
		if (newIndex > 15) {
			newIndex = -1;
		}
		this.teamColor = Formatting.byColorIndex(newIndex);
		if (this.teamColor == null) {
			this.teamColor = Formatting.RESET;
		}
		this.cycleTeamColorButton.setMessage(getTeamColorLabel(this.teamColor));
	}

	private void done() {
		if (this.updateTeamControllerBlock()) {
			this.close();
		}
	}

	private void cancel() {
		this.close();
	}

	@Override
	protected void init() {
		this.cycleScreenPageButton = this.addDrawableChild(CyclingButtonWidget.builder(ScreenPage::asText).values((ScreenPage[]) ScreenPage.values()).initially(this.screenPage).omitKeyText().build(this.width / 2 - 154, 20, 300, 20, Text.empty(), (button, screenPage) -> {
			this.screenPage = screenPage;
			this.updateWidgets();
		}));

		this.showArea = this.teamControllerBlockEntity.showArea();
		this.toggleShowAreaButton = this.addDrawableChild(CyclingButtonWidget.onOffBuilder(HIDE_AREA_LABEL_TEXT, SHOW_AREA_LABEL_TEXT).initially(this.showArea).omitKeyText().build(this.width / 2 - 154, 44, 300, 20, Text.empty(), (button, showApplicationArea) -> {
			this.showArea = showApplicationArea;
		}));

		this.areaDimensionsXField = new TextFieldWidget(this.textRenderer, this.width / 2 - 154, 79, 100, 20, Text.empty());
		this.areaDimensionsXField.setMaxLength(128);
		this.areaDimensionsXField.setText(Integer.toString(this.teamControllerBlockEntity.getAreaDimensions().getX()));
		this.addSelectableChild(this.areaDimensionsXField);

		this.areaDimensionsYField = new TextFieldWidget(this.textRenderer, this.width / 2 - 50, 79, 100, 20, Text.empty());
		this.areaDimensionsYField.setMaxLength(128);
		this.areaDimensionsYField.setText(Integer.toString(this.teamControllerBlockEntity.getAreaDimensions().getY()));
		this.addSelectableChild(this.areaDimensionsYField);

		this.areaDimensionsZField = new TextFieldWidget(this.textRenderer, this.width / 2 + 54, 79, 100, 20, Text.empty());
		this.areaDimensionsZField.setMaxLength(128);
		this.areaDimensionsZField.setText(Integer.toString(this.teamControllerBlockEntity.getAreaDimensions().getZ()));
		this.addSelectableChild(this.areaDimensionsZField);

		this.areaPositionOffsetXField = new TextFieldWidget(this.textRenderer, this.width / 2 - 154, 114, 100, 20, Text.empty());
		this.areaPositionOffsetXField.setMaxLength(128);
		this.areaPositionOffsetXField.setText(Integer.toString(this.teamControllerBlockEntity.getAreaPositionOffset().getX()));
		this.addSelectableChild(this.areaPositionOffsetXField);

		this.areaPositionOffsetYField = new TextFieldWidget(this.textRenderer, this.width / 2 - 50, 114, 100, 20, Text.empty());
		this.areaPositionOffsetYField.setMaxLength(128);
		this.areaPositionOffsetYField.setText(Integer.toString(this.teamControllerBlockEntity.getAreaPositionOffset().getY()));
		this.addSelectableChild(this.areaPositionOffsetYField);

		this.areaPositionOffsetZField = new TextFieldWidget(this.textRenderer, this.width / 2 + 54, 114, 100, 20, Text.empty());
		this.areaPositionOffsetZField.setMaxLength(128);
		this.areaPositionOffsetZField.setText(Integer.toString(this.teamControllerBlockEntity.getAreaPositionOffset().getZ()));
		this.addSelectableChild(this.areaPositionOffsetZField);

		this.pvpControllerBlockPositionOffsetXField = new TextFieldWidget(this.textRenderer, this.width / 2 - 154, 149, 100, 20, Text.empty());
		this.pvpControllerBlockPositionOffsetXField.setMaxLength(128);
		this.pvpControllerBlockPositionOffsetXField.setText(Integer.toString(this.teamControllerBlockEntity.getPVPControllerBlockPositionOffset().getX()));
		this.addSelectableChild(this.pvpControllerBlockPositionOffsetXField);

		this.pvpControllerBlockPositionOffsetYField = new TextFieldWidget(this.textRenderer, this.width / 2 - 50, 149, 100, 20, Text.empty());
		this.pvpControllerBlockPositionOffsetYField.setMaxLength(128);
		this.pvpControllerBlockPositionOffsetYField.setText(Integer.toString(this.teamControllerBlockEntity.getPVPControllerBlockPositionOffset().getY()));
		this.addSelectableChild(this.pvpControllerBlockPositionOffsetYField);

		this.pvpControllerBlockPositionOffsetZField = new TextFieldWidget(this.textRenderer, this.width / 2 + 54, 149, 100, 20, Text.empty());
		this.pvpControllerBlockPositionOffsetZField.setMaxLength(128);
		this.pvpControllerBlockPositionOffsetZField.setText(Integer.toString(this.teamControllerBlockEntity.getPVPControllerBlockPositionOffset().getZ()));
		this.addSelectableChild(this.pvpControllerBlockPositionOffsetZField);


		this.teamIdentifierField = new TextFieldWidget(this.textRenderer, this.width / 2 - 154, 55, 300, 20, Text.empty());
		this.teamIdentifierField.setMaxLength(128);
		this.teamIdentifierField.setText(this.teamControllerBlockEntity.getTeamIdentifier());
		this.addSelectableChild(this.teamIdentifierField);

		this.displayNameField = new TextFieldWidget(this.textRenderer, this.width / 2 - 154, 90, 300, 20, Text.empty());
		this.displayNameField.setMaxLength(128);
		this.displayNameField.setText(this.teamControllerBlockEntity.getDisplayNameString());
		this.addSelectableChild(this.displayNameField);

		this.teamColor = this.teamControllerBlockEntity.getTeamColor();
		this.cycleTeamColorButton = this.addDrawableChild(ButtonWidget.builder(getTeamColorLabel(this.teamColor), (button) -> this.cycleTeamColor()).dimensions(this.width / 2 - 154, 125, 100, 20).build());

		this.friendlyFire = this.teamControllerBlockEntity.friendlyFire();
		this.toggleFriendlyFireButton = this.addDrawableChild(CyclingButtonWidget.onOffBuilder(Text.literal("True"), Text.literal("False")).initially(this.friendlyFire).omitKeyText().build(this.width / 2 - 50, 125, 100, 20, Text.empty(), (button, friendlyFire) -> {
			this.friendlyFire = friendlyFire;
		}));

		this.showFriendlyInvisibles = this.teamControllerBlockEntity.showFriendlyInvisibles();
		this.toggleShowFriendlyInvisiblesButton = this.addDrawableChild(CyclingButtonWidget.onOffBuilder(Text.literal("True"), Text.literal("False")).initially(this.showFriendlyInvisibles).omitKeyText().build(this.width / 2 + 54, 125, 100, 20, Text.empty(), (button, showFriendlyInvisibles) -> {
			this.showFriendlyInvisibles = showFriendlyInvisibles;
		}));

		AbstractTeam.VisibilityRule var = AbstractTeam.VisibilityRule.getRule(this.teamControllerBlockEntity.getNametagVisibility());
		this.nametagVisibility = var != null ? var : AbstractTeam.VisibilityRule.ALWAYS;
		this.cycleNametagVisibilityButton = this.addDrawableChild(CyclingButtonWidget.builder(AbstractTeam.VisibilityRule::getDisplayName).values((AbstractTeam.VisibilityRule[]) AbstractTeam.VisibilityRule.values()).initially(this.nametagVisibility).omitKeyText().build(this.width / 2 - 154, 160, 100, 20, Text.empty(), (button, nametagVisibility) -> {
			this.nametagVisibility = nametagVisibility;
		}));

		AbstractTeam.VisibilityRule var1 = AbstractTeam.VisibilityRule.getRule(this.teamControllerBlockEntity.getDeathMessageVisibility());
		this.deathMessageVisibility = var1 != null ? var1 : AbstractTeam.VisibilityRule.ALWAYS;
		this.cycleDeathMessageVisibilityButton = this.addDrawableChild(CyclingButtonWidget.builder(AbstractTeam.VisibilityRule::getDisplayName).values((AbstractTeam.VisibilityRule[]) AbstractTeam.VisibilityRule.values()).initially(this.deathMessageVisibility).omitKeyText().build(this.width / 2 - 50, 160, 100, 20, Text.empty(), (button, deathMessageVisibility) -> {
			this.deathMessageVisibility = deathMessageVisibility;
		}));

		AbstractTeam.CollisionRule var2 = AbstractTeam.CollisionRule.getRule(this.teamControllerBlockEntity.getCollisionRule());
		this.collisionRule = var2 != null ? var2 : AbstractTeam.CollisionRule.ALWAYS;
		this.cycleCollisionRuleButton = this.addDrawableChild(CyclingButtonWidget.builder(AbstractTeam.CollisionRule::getDisplayName).values((AbstractTeam.CollisionRule[]) AbstractTeam.CollisionRule.values()).initially(this.collisionRule).omitKeyText().build(this.width / 2 + 54, 160, 100, 20, Text.empty(), (button, collisionRule) -> {
			this.collisionRule = collisionRule;
		}));

		this.prefixField = new TextFieldWidget(this.textRenderer, this.width / 2 - 154, 195, 150, 20, Text.empty());
		this.prefixField.setMaxLength(128);
		this.prefixField.setText(this.teamControllerBlockEntity.getPrefixString());
		this.addSelectableChild(this.prefixField);

		this.suffixField = new TextFieldWidget(this.textRenderer, this.width / 2 + 4, 195, 150, 20, Text.empty());
		this.suffixField.setMaxLength(128);
		this.suffixField.setText(this.teamControllerBlockEntity.getSuffixString());
		this.addSelectableChild(this.suffixField);

		this.addDrawableChild(ButtonWidget.builder(ScreenTexts.DONE, button -> this.done()).dimensions(this.width / 2 - 4 - 150, 219, 150, 20).build());
		this.addDrawableChild(ButtonWidget.builder(ScreenTexts.CANCEL, button -> this.cancel()).dimensions(this.width / 2 + 4, 219, 150, 20).build());
		this.updateWidgets();
	}

	@Override
	protected void setInitialFocus() {
		this.setInitialFocus(this.cycleScreenPageButton);
	}

	@Override
	public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
		this.renderInGameBackground(context);
	}

	private void updateWidgets() {

		this.toggleShowAreaButton.visible = false;
		this.areaDimensionsXField.visible = false;
		this.areaDimensionsYField.visible = false;
		this.areaDimensionsZField.visible = false;
		this.areaPositionOffsetXField.visible = false;
		this.areaPositionOffsetYField.visible = false;
		this.areaPositionOffsetZField.visible = false;
		this.pvpControllerBlockPositionOffsetXField.setVisible(false);
		this.pvpControllerBlockPositionOffsetYField.setVisible(false);
		this.pvpControllerBlockPositionOffsetZField.setVisible(false);

		this.teamIdentifierField.setVisible(false);
		this.displayNameField.setVisible(false);
		this.cycleTeamColorButton.visible = false;
		this.toggleFriendlyFireButton.visible = false;
		this.toggleShowFriendlyInvisiblesButton.visible = false;
		this.cycleNametagVisibilityButton.visible = false;
		this.cycleDeathMessageVisibilityButton.visible = false;
		this.cycleCollisionRuleButton.visible = false;
		this.prefixField.setVisible(false);
		this.suffixField.setVisible(false);

		if (this.screenPage == ScreenPage.AREA) {

			this.toggleShowAreaButton.visible = true;
			this.areaDimensionsXField.visible = true;
			this.areaDimensionsYField.visible = true;
			this.areaDimensionsZField.visible = true;
			this.areaPositionOffsetXField.visible = true;
			this.areaPositionOffsetYField.visible = true;
			this.areaPositionOffsetZField.visible = true;
			this.pvpControllerBlockPositionOffsetXField.setVisible(true);
			this.pvpControllerBlockPositionOffsetYField.setVisible(true);
			this.pvpControllerBlockPositionOffsetZField.setVisible(true);

		} else if (this.screenPage == ScreenPage.TEAM) {

			this.teamIdentifierField.setVisible(true);
			this.displayNameField.setVisible(true);
			this.cycleTeamColorButton.visible = true;
			this.toggleFriendlyFireButton.visible = true;
			this.toggleShowFriendlyInvisiblesButton.visible = true;
			this.cycleNametagVisibilityButton.visible = true;
			this.cycleDeathMessageVisibilityButton.visible = true;
			this.cycleCollisionRuleButton.visible = true;
			this.prefixField.setVisible(true);
			this.suffixField.setVisible(true);

		}
	}

	@Override
	public void resize(MinecraftClient client, int width, int height) {
		ScreenPage var = this.screenPage;
		AbstractTeam.VisibilityRule var1 = this.nametagVisibility;
		AbstractTeam.VisibilityRule var2 = this.deathMessageVisibility;
		AbstractTeam.CollisionRule var3 = this.collisionRule;
		Formatting var4 = this.teamColor;
		boolean bool = this.showArea;
		boolean bool1 = this.friendlyFire;
		boolean bool2 = this.showFriendlyInvisibles;
		String string = this.areaDimensionsXField.getText();
		String string1 = this.areaDimensionsYField.getText();
		String string2 = this.areaDimensionsZField.getText();
		String string3 = this.areaPositionOffsetXField.getText();
		String string4 = this.areaPositionOffsetYField.getText();
		String string5 = this.areaPositionOffsetZField.getText();
		String string6 = this.pvpControllerBlockPositionOffsetXField.getText();
		String string7 = this.pvpControllerBlockPositionOffsetYField.getText();
		String string8 = this.pvpControllerBlockPositionOffsetZField.getText();
		String string9 = this.teamIdentifierField.getText();
		String string10 = this.displayNameField.getText();
		String string11 = this.prefixField.getText();
		String string12 = this.suffixField.getText();
		this.init(client, width, height);
		this.screenPage = var;
		this.cycleScreenPageButton.setValue(var);
		this.nametagVisibility = var1;
		this.cycleNametagVisibilityButton.setValue(var1);
		this.deathMessageVisibility = var2;
		this.cycleDeathMessageVisibilityButton.setValue(var2);
		this.collisionRule = var3;
		this.cycleCollisionRuleButton.setValue(var3);
		this.teamColor = var4;
		this.cycleTeamColorButton.setMessage(getTeamColorLabel(var4));
		this.showArea = bool;
		this.toggleShowAreaButton.setValue(bool);
		this.friendlyFire = bool1;
		this.toggleFriendlyFireButton.setValue(bool1);
		this.showFriendlyInvisibles = bool2;
		this.toggleShowFriendlyInvisiblesButton.setValue(bool2);
		this.areaDimensionsXField.setText(string);
		this.areaDimensionsYField.setText(string1);
		this.areaDimensionsZField.setText(string2);
		this.areaPositionOffsetXField.setText(string3);
		this.areaPositionOffsetYField.setText(string4);
		this.areaPositionOffsetZField.setText(string5);
		this.pvpControllerBlockPositionOffsetXField.setText(string6);
		this.pvpControllerBlockPositionOffsetYField.setText(string7);
		this.pvpControllerBlockPositionOffsetZField.setText(string8);
		this.teamIdentifierField.setText(string9);
		this.displayNameField.setText(string10);
		this.prefixField.setText(string11);
		this.suffixField.setText(string12);
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {

		super.render(context, mouseX, mouseY, delta);

		if (this.screenPage == ScreenPage.AREA) {
			context.drawTextWithShadow(this.textRenderer, AREA_DIMENSIONS_LABEL_TEXT, this.width / 2 - 153, 69, 0xA0A0A0);
			this.areaDimensionsXField.render(context, mouseX, mouseY, delta);
			this.areaDimensionsYField.render(context, mouseX, mouseY, delta);
			this.areaDimensionsZField.render(context, mouseX, mouseY, delta);
			context.drawTextWithShadow(this.textRenderer, AREA_POSITION_OFFET_LABEL_TEXT, this.width / 2 - 153, 104, 0xA0A0A0);
			this.areaPositionOffsetXField.render(context, mouseX, mouseY, delta);
			this.areaPositionOffsetYField.render(context, mouseX, mouseY, delta);
			this.areaPositionOffsetZField.render(context, mouseX, mouseY, delta);
			context.drawTextWithShadow(this.textRenderer, PVP_CONTROLLER_BLOCK_POSITION_OFFET_LABEL_TEXT, this.width / 2 - 153, 139, 0xA0A0A0);
			this.pvpControllerBlockPositionOffsetXField.render(context, mouseX, mouseY, delta);
			this.pvpControllerBlockPositionOffsetYField.render(context, mouseX, mouseY, delta);
			this.pvpControllerBlockPositionOffsetZField.render(context, mouseX, mouseY, delta);
		} else if (this.screenPage == ScreenPage.TEAM) {
			context.drawTextWithShadow(this.textRenderer, TEAM_IDENTIFIER_LABEL_TEXT, this.width / 2 - 153, 45, 0xA0A0A0);
			this.teamIdentifierField.render(context, mouseX, mouseY, delta);
			context.drawTextWithShadow(this.textRenderer, DISPLAY_NAME_LABEL_TEXT, this.width / 2 - 153, 80, 0xA0A0A0);
			this.displayNameField.render(context, mouseX, mouseY, delta);
			context.drawTextWithShadow(this.textRenderer, TEAM_COLOR_LABEL_TEXT, this.width / 2 - 153, 115, 0xA0A0A0);
			context.drawTextWithShadow(this.textRenderer, FRIENDLY_FIRE_LABEL_TEXT, this.width / 2 - 49, 115, 0xA0A0A0);
			context.drawTextWithShadow(this.textRenderer, FRIENDLY_INVISIBLES_LABEL_TEXT, this.width / 2 + 55, 115, 0xA0A0A0);
			context.drawTextWithShadow(this.textRenderer, NAME_TAG_VISIBILITY_LABEL_TEXT, this.width / 2 - 153, 150, 0xA0A0A0);
			context.drawTextWithShadow(this.textRenderer, DEATH_MESSAGE_VISIBILITY_LABEL_TEXT, this.width / 2 - 49, 150, 0xA0A0A0);
			context.drawTextWithShadow(this.textRenderer, COLLISION_RULE_LABEL_TEXT, this.width / 2 + 55, 150, 0xA0A0A0);
			context.drawTextWithShadow(this.textRenderer, PREFIX_LABEL_TEXT, this.width / 2 - 153, 185, 0xA0A0A0);
			context.drawTextWithShadow(this.textRenderer, SUFFIX_LABEL_TEXT, this.width / 2 + 5, 185, 0xA0A0A0);
			this.prefixField.render(context, mouseX, mouseY, delta);
			this.suffixField.render(context, mouseX, mouseY, delta);
		}
	}

	@Override
	public boolean shouldPause() {
		return false;
	}

	private boolean updateTeamControllerBlock() {
		ClientPlayNetworking.send(new UpdateTeamControllerBlockPacket(
				this.teamControllerBlockEntity.getPos(),
				this.showArea,
				new Vec3i(
						ItemUtils.parseInt(this.areaDimensionsXField.getText()),
						ItemUtils.parseInt(this.areaDimensionsYField.getText()),
						ItemUtils.parseInt(this.areaDimensionsZField.getText())
				),
				new BlockPos(
						ItemUtils.parseInt(this.areaPositionOffsetXField.getText()),
						ItemUtils.parseInt(this.areaPositionOffsetYField.getText()),
						ItemUtils.parseInt(this.areaPositionOffsetZField.getText())
				),
				new BlockPos(
						ItemUtils.parseInt(this.pvpControllerBlockPositionOffsetXField.getText()),
						ItemUtils.parseInt(this.pvpControllerBlockPositionOffsetYField.getText()),
						ItemUtils.parseInt(this.pvpControllerBlockPositionOffsetZField.getText())
				),
				this.teamIdentifierField.getText(),
				this.displayNameField.getText(),
				this.teamColor.getColorIndex(),
				this.friendlyFire,
				this.showFriendlyInvisibles,
				this.nametagVisibility.name,
				this.deathMessageVisibility.name,
				this.collisionRule.name,
				this.prefixField.getText(),
				this.suffixField.getText()
		));
		return true;
	}

	private Text getTeamColorLabel(Formatting formatting) {
		return Text.translatable("formatting." + formatting.getName());
	}

	public static enum ScreenPage implements StringIdentifiable {
		AREA("area"),
		TEAM("team");

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
			return Text.translatable("gui.team_controller_block.screenPage." + this.name);
		}
	}
}
