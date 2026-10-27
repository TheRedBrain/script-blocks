package com.github.theredbrain.scriptblocks.gui.screen.ingame;

import com.github.theredbrain.scriptblocks.block.entity.TeamControllerBlockEntity;
import com.github.theredbrain.scriptblocks.network.packet.UpdateTeamControllerBlockPacket;
import com.github.theredbrain.scriptblocks.util.ItemUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.ChatFormatting;
import net.minecraft.client.GameNarrator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.scores.Team;
import java.util.Arrays;
import java.util.Optional;

@Environment(value = EnvType.CLIENT)
public class TeamControllerBlockScreen extends AbstractCreativeBlockSettingsScreen {
	private static final Component HIDE_AREA_LABEL_TEXT = Component.translatable("gui.team_controller_block.hide_area_label");
	private static final Component SHOW_AREA_LABEL_TEXT = Component.translatable("gui.team_controller_block.show_area_label");
	private static final Component AREA_DIMENSIONS_LABEL_TEXT = Component.translatable("gui.team_controller_block.area_dimensions_label");
	private static final Component AREA_POSITION_OFFET_LABEL_TEXT = Component.translatable("gui.team_controller_block.area_position_offset_label");
	private static final Component PVP_CONTROLLER_BLOCK_POSITION_OFFET_LABEL_TEXT = Component.translatable("gui.team_controller_block.pvp_controller_block_position_offset_label");
	private static final Component ENABLE_TICKING_TRUE_LABEL_TEXT = Component.translatable("gui.team_controller_block.enable_ticking_true_label");
	private static final Component ENABLE_TICKING_FALSE_LABEL_TEXT = Component.translatable("gui.team_controller_block.enable_ticking_false_label");
	private static final Component TEAM_IDENTIFIER_LABEL_TEXT = Component.translatable("gui.team_controller_block.team_identifier_label");
	private static final Component DISPLAY_NAME_LABEL_TEXT = Component.translatable("gui.team_controller_block.display_name_label");
	private static final Component TEAM_COLOR_LABEL_TEXT = Component.translatable("gui.team_controller_block.team_color_label");
	private static final Component FRIENDLY_FIRE_LABEL_TEXT = Component.translatable("gui.team_controller_block.friendly_fire_label");
	private static final Component FRIENDLY_INVISIBLES_LABEL_TEXT = Component.translatable("gui.team_controller_block.friendly_invisibles_label");
	private static final Component NAME_TAG_VISIBILITY_LABEL_TEXT = Component.translatable("gui.team_controller_block.name_tag_visibility_label");
	private static final Component DEATH_MESSAGE_VISIBILITY_LABEL_TEXT = Component.translatable("gui.team_controller_block.death_message_visibility_label");
	private static final Component COLLISION_RULE_LABEL_TEXT = Component.translatable("gui.team_controller_block.collision_rule_label");
	private static final Component PREFIX_LABEL_TEXT = Component.translatable("gui.team_controller_block.prefix_label");
	private static final Component SUFFIX_LABEL_TEXT = Component.translatable("gui.team_controller_block.suffix_label");
	private final TeamControllerBlockEntity teamControllerBlockEntity;
	private ScreenPage screenPage;
	private CycleButton<ScreenPage> cycleScreenPageButton;
	private CycleButton<Boolean> toggleShowAreaButton;
	private EditBox areaDimensionsXField;
	private EditBox areaDimensionsYField;
	private EditBox areaDimensionsZField;
	private EditBox areaPositionOffsetXField;
	private EditBox areaPositionOffsetYField;
	private EditBox areaPositionOffsetZField;
	private boolean showArea;
	private EditBox pvpControllerBlockPositionOffsetXField;
	private EditBox pvpControllerBlockPositionOffsetYField;
	private EditBox pvpControllerBlockPositionOffsetZField;
	private CycleButton<Boolean> toggleEnableTickingButton;
	private boolean enableTicking;

	private EditBox teamIdentifierField;
	private EditBox displayNameField;
	private Button cycleTeamColorButton;
	private ChatFormatting teamColor;
	private CycleButton<Boolean> toggleFriendlyFireButton;
	private CycleButton<Boolean> toggleShowFriendlyInvisiblesButton;
	private boolean friendlyFire;
	private boolean showFriendlyInvisibles;
	private CycleButton<Team.Visibility> cycleNametagVisibilityButton;
	private CycleButton<Team.Visibility> cycleDeathMessageVisibilityButton;
	private CycleButton<Team.CollisionRule> cycleCollisionRuleButton;
	private Team.Visibility nametagVisibility;
	private Team.Visibility deathMessageVisibility;
	private Team.CollisionRule collisionRule;
	private EditBox prefixField;
	private EditBox suffixField;

	public TeamControllerBlockScreen(TeamControllerBlockEntity teamControllerBlockEntity) {
		super(GameNarrator.NO_TITLE);
		this.teamControllerBlockEntity = teamControllerBlockEntity;
		this.screenPage = ScreenPage.AREA;
	}

	private void cycleTeamColor() {
		int newIndex = this.teamColor.getId() + 1;
		if (newIndex > 15) {
			newIndex = -1;
		}
		this.teamColor = ChatFormatting.getById(newIndex);
		if (this.teamColor == null) {
			this.teamColor = ChatFormatting.RESET;
		}
		this.cycleTeamColorButton.setMessage(getTeamColorLabel(this.teamColor));
	}

	private Component getTeamColorLabel(ChatFormatting formatting) {
		return Component.translatable("formatting." + formatting.getName());
	}

	@Override
	protected void init() {
		this.cycleScreenPageButton = this.addRenderableWidget(CycleButton.builder(ScreenPage::asText, this.screenPage).withValues(ScreenPage.values()).displayOnlyValue().create(this.width / 2 - 154, 20, 300, 20, Component.empty(), (button, screenPage) -> {
			this.screenPage = screenPage;
			this.updateWidgets();
		}));

		this.showArea = this.teamControllerBlockEntity.showArea();
		this.toggleShowAreaButton = this.addRenderableWidget(CycleButton.booleanBuilder(HIDE_AREA_LABEL_TEXT, SHOW_AREA_LABEL_TEXT, this.showArea).displayOnlyValue().create(this.width / 2 - 154, 44, 300, 20, Component.empty(), (button, showApplicationArea) -> {
			this.showArea = showApplicationArea;
		}));

		this.areaDimensionsXField = new EditBox(this.font, this.width / 2 - 154, 79, 100, 20, Component.empty());
		this.areaDimensionsXField.setMaxLength(128);
		this.areaDimensionsXField.setValue(Integer.toString(this.teamControllerBlockEntity.getAreaDimensions().getX()));
		this.addWidget(this.areaDimensionsXField);

		this.areaDimensionsYField = new EditBox(this.font, this.width / 2 - 50, 79, 100, 20, Component.empty());
		this.areaDimensionsYField.setMaxLength(128);
		this.areaDimensionsYField.setValue(Integer.toString(this.teamControllerBlockEntity.getAreaDimensions().getY()));
		this.addWidget(this.areaDimensionsYField);

		this.areaDimensionsZField = new EditBox(this.font, this.width / 2 + 54, 79, 100, 20, Component.empty());
		this.areaDimensionsZField.setMaxLength(128);
		this.areaDimensionsZField.setValue(Integer.toString(this.teamControllerBlockEntity.getAreaDimensions().getZ()));
		this.addWidget(this.areaDimensionsZField);

		this.areaPositionOffsetXField = new EditBox(this.font, this.width / 2 - 154, 114, 100, 20, Component.empty());
		this.areaPositionOffsetXField.setMaxLength(128);
		this.areaPositionOffsetXField.setValue(Integer.toString(this.teamControllerBlockEntity.getAreaPositionOffset().getX()));
		this.addWidget(this.areaPositionOffsetXField);

		this.areaPositionOffsetYField = new EditBox(this.font, this.width / 2 - 50, 114, 100, 20, Component.empty());
		this.areaPositionOffsetYField.setMaxLength(128);
		this.areaPositionOffsetYField.setValue(Integer.toString(this.teamControllerBlockEntity.getAreaPositionOffset().getY()));
		this.addWidget(this.areaPositionOffsetYField);

		this.areaPositionOffsetZField = new EditBox(this.font, this.width / 2 + 54, 114, 100, 20, Component.empty());
		this.areaPositionOffsetZField.setMaxLength(128);
		this.areaPositionOffsetZField.setValue(Integer.toString(this.teamControllerBlockEntity.getAreaPositionOffset().getZ()));
		this.addWidget(this.areaPositionOffsetZField);

		this.pvpControllerBlockPositionOffsetXField = new EditBox(this.font, this.width / 2 - 154, 149, 100, 20, Component.empty());
		this.pvpControllerBlockPositionOffsetXField.setMaxLength(128);
		this.pvpControllerBlockPositionOffsetXField.setValue(Integer.toString(this.teamControllerBlockEntity.getPVPControllerBlockPositionOffset().getX()));
		this.addWidget(this.pvpControllerBlockPositionOffsetXField);

		this.pvpControllerBlockPositionOffsetYField = new EditBox(this.font, this.width / 2 - 50, 149, 100, 20, Component.empty());
		this.pvpControllerBlockPositionOffsetYField.setMaxLength(128);
		this.pvpControllerBlockPositionOffsetYField.setValue(Integer.toString(this.teamControllerBlockEntity.getPVPControllerBlockPositionOffset().getY()));
		this.addWidget(this.pvpControllerBlockPositionOffsetYField);

		this.pvpControllerBlockPositionOffsetZField = new EditBox(this.font, this.width / 2 + 54, 149, 100, 20, Component.empty());
		this.pvpControllerBlockPositionOffsetZField.setMaxLength(128);
		this.pvpControllerBlockPositionOffsetZField.setValue(Integer.toString(this.teamControllerBlockEntity.getPVPControllerBlockPositionOffset().getZ()));
		this.addWidget(this.pvpControllerBlockPositionOffsetZField);

		this.enableTicking = this.teamControllerBlockEntity.enableTicking();
		this.toggleEnableTickingButton = this.addRenderableWidget(CycleButton.booleanBuilder(ENABLE_TICKING_TRUE_LABEL_TEXT, ENABLE_TICKING_FALSE_LABEL_TEXT, this.enableTicking).displayOnlyValue().create(this.width / 2 - 154, 44, 300, 20, Component.empty(), (button, enableTicking) -> {
			this.enableTicking = enableTicking;
		}));


		this.teamIdentifierField = new EditBox(this.font, this.width / 2 - 154, 55, 300, 20, Component.empty());
		this.teamIdentifierField.setMaxLength(128);
		this.teamIdentifierField.setValue(this.teamControllerBlockEntity.getTeamIdentifier());
		this.addWidget(this.teamIdentifierField);

		this.displayNameField = new EditBox(this.font, this.width / 2 - 154, 90, 300, 20, Component.empty());
		this.displayNameField.setMaxLength(128);
		this.displayNameField.setValue(this.teamControllerBlockEntity.getDisplayNameString());
		this.addWidget(this.displayNameField);

		this.teamColor = this.teamControllerBlockEntity.getTeamColor();
		this.cycleTeamColorButton = this.addRenderableWidget(Button.builder(getTeamColorLabel(this.teamColor), (button) -> this.cycleTeamColor()).bounds(this.width / 2 - 154, 125, 100, 20).build());

		this.friendlyFire = this.teamControllerBlockEntity.friendlyFire();
		this.toggleFriendlyFireButton = this.addRenderableWidget(CycleButton.booleanBuilder(Component.literal("True"), Component.literal("False"), this.friendlyFire).displayOnlyValue().create(this.width / 2 - 50, 125, 100, 20, Component.empty(), (button, friendlyFire) -> {
			this.friendlyFire = friendlyFire;
		}));

		this.showFriendlyInvisibles = this.teamControllerBlockEntity.showFriendlyInvisibles();
		this.toggleShowFriendlyInvisiblesButton = this.addRenderableWidget(CycleButton.booleanBuilder(Component.literal("True"), Component.literal("False"), this.showFriendlyInvisibles).displayOnlyValue().create(this.width / 2 + 54, 125, 100, 20, Component.empty(), (button, showFriendlyInvisibles) -> {
			this.showFriendlyInvisibles = showFriendlyInvisibles;
		}));

		Team.Visibility var = this.teamControllerBlockEntity.getNametagVisibility();
		this.nametagVisibility = var != null ? var : Team.Visibility.ALWAYS;
		this.cycleNametagVisibilityButton = this.addRenderableWidget(CycleButton.builder(Team.Visibility::getDisplayName, this.nametagVisibility).withValues(Team.Visibility.values()).displayOnlyValue().create(this.width / 2 - 154, 160, 100, 20, Component.empty(), (button, nametagVisibility) -> {
			this.nametagVisibility = nametagVisibility;
		}));

		Team.Visibility var1 = this.teamControllerBlockEntity.getDeathMessageVisibility();
		this.deathMessageVisibility = var1 != null ? var1 : Team.Visibility.ALWAYS;
		this.cycleDeathMessageVisibilityButton = this.addRenderableWidget(CycleButton.builder(Team.Visibility::getDisplayName, this.deathMessageVisibility).withValues(Team.Visibility.values()).displayOnlyValue().create(this.width / 2 - 50, 160, 100, 20, Component.empty(), (button, deathMessageVisibility) -> {
			this.deathMessageVisibility = deathMessageVisibility;
		}));

		Team.CollisionRule var2 = this.teamControllerBlockEntity.getCollisionRule();
		this.collisionRule = var2 != null ? var2 : Team.CollisionRule.ALWAYS;
		this.cycleCollisionRuleButton = this.addRenderableWidget(CycleButton.builder(Team.CollisionRule::getDisplayName, this.collisionRule).withValues(Team.CollisionRule.values()).displayOnlyValue().create(this.width / 2 + 54, 160, 100, 20, Component.empty(), (button, collisionRule) -> {
			this.collisionRule = collisionRule;
		}));

		this.prefixField = new EditBox(this.font, this.width / 2 - 154, 195, 150, 20, Component.empty());
		this.prefixField.setMaxLength(128);
		this.prefixField.setValue(this.teamControllerBlockEntity.getPrefixString());
		this.addWidget(this.prefixField);

		this.suffixField = new EditBox(this.font, this.width / 2 + 4, 195, 150, 20, Component.empty());
		this.suffixField.setMaxLength(128);
		this.suffixField.setValue(this.teamControllerBlockEntity.getSuffixString());
		this.addWidget(this.suffixField);

		this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> this.done()).bounds(this.width / 2 - 4 - 150, 219, 150, 20).build());
		this.addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL, button -> this.cancel()).bounds(this.width / 2 + 4, 219, 150, 20).build());
		this.updateWidgets();
	}

	@Override
	protected void setInitialFocus() {
		this.setInitialFocus(this.cycleScreenPageButton);
	}

	@Override
	protected void updateWidgets() {

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
		this.toggleEnableTickingButton.visible = false;

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
			this.toggleEnableTickingButton.visible = true;

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
	public void resize(Minecraft client, int width, int height) {
		ScreenPage var = this.screenPage;
		Team.Visibility var1 = this.nametagVisibility;
		Team.Visibility var2 = this.deathMessageVisibility;
		Team.CollisionRule var3 = this.collisionRule;
		ChatFormatting var4 = this.teamColor;
		boolean bool = this.showArea;
		boolean bool1 = this.friendlyFire;
		boolean bool2 = this.showFriendlyInvisibles;
		boolean bool3 = this.enableTicking;
		String string = this.areaDimensionsXField.getValue();
		String string1 = this.areaDimensionsYField.getValue();
		String string2 = this.areaDimensionsZField.getValue();
		String string3 = this.areaPositionOffsetXField.getValue();
		String string4 = this.areaPositionOffsetYField.getValue();
		String string5 = this.areaPositionOffsetZField.getValue();
		String string6 = this.pvpControllerBlockPositionOffsetXField.getValue();
		String string7 = this.pvpControllerBlockPositionOffsetYField.getValue();
		String string8 = this.pvpControllerBlockPositionOffsetZField.getValue();
		String string9 = this.teamIdentifierField.getValue();
		String string10 = this.displayNameField.getValue();
		String string11 = this.prefixField.getValue();
		String string12 = this.suffixField.getValue();
		this.init(client, width, height);
		this.screenPage = var;
		this.cycleScreenPageButton.setValue(this.screenPage);
		this.nametagVisibility = var1;
		this.cycleNametagVisibilityButton.setValue(this.nametagVisibility);
		this.deathMessageVisibility = var2;
		this.cycleDeathMessageVisibilityButton.setValue(this.deathMessageVisibility);
		this.collisionRule = var3;
		this.cycleCollisionRuleButton.setValue(this.collisionRule);
		this.teamColor = var4;
		this.cycleTeamColorButton.setMessage(getTeamColorLabel(this.teamColor));
		this.showArea = bool;
		this.toggleShowAreaButton.setValue(this.showArea);
		this.friendlyFire = bool1;
		this.toggleFriendlyFireButton.setValue(this.friendlyFire);
		this.showFriendlyInvisibles = bool2;
		this.toggleShowFriendlyInvisiblesButton.setValue(this.showFriendlyInvisibles);
		this.enableTicking = bool3;
		this.toggleEnableTickingButton.setValue(this.enableTicking);
		this.areaDimensionsXField.setValue(string);
		this.areaDimensionsYField.setValue(string1);
		this.areaDimensionsZField.setValue(string2);
		this.areaPositionOffsetXField.setValue(string3);
		this.areaPositionOffsetYField.setValue(string4);
		this.areaPositionOffsetZField.setValue(string5);
		this.pvpControllerBlockPositionOffsetXField.setValue(string6);
		this.pvpControllerBlockPositionOffsetYField.setValue(string7);
		this.pvpControllerBlockPositionOffsetZField.setValue(string8);
		this.teamIdentifierField.setValue(string9);
		this.displayNameField.setValue(string10);
		this.prefixField.setValue(string11);
		this.suffixField.setValue(string12);
	}

	@Override
	public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {

		super.render(context, mouseX, mouseY, delta);

		if (this.screenPage == ScreenPage.AREA) {
			context.drawString(this.font, AREA_DIMENSIONS_LABEL_TEXT, this.width / 2 - 153, 69, 0xA0A0A0);
			this.areaDimensionsXField.render(context, mouseX, mouseY, delta);
			this.areaDimensionsYField.render(context, mouseX, mouseY, delta);
			this.areaDimensionsZField.render(context, mouseX, mouseY, delta);
			context.drawString(this.font, AREA_POSITION_OFFET_LABEL_TEXT, this.width / 2 - 153, 104, 0xA0A0A0);
			this.areaPositionOffsetXField.render(context, mouseX, mouseY, delta);
			this.areaPositionOffsetYField.render(context, mouseX, mouseY, delta);
			this.areaPositionOffsetZField.render(context, mouseX, mouseY, delta);
			context.drawString(this.font, PVP_CONTROLLER_BLOCK_POSITION_OFFET_LABEL_TEXT, this.width / 2 - 153, 139, 0xA0A0A0);
			this.pvpControllerBlockPositionOffsetXField.render(context, mouseX, mouseY, delta);
			this.pvpControllerBlockPositionOffsetYField.render(context, mouseX, mouseY, delta);
			this.pvpControllerBlockPositionOffsetZField.render(context, mouseX, mouseY, delta);
		} else if (this.screenPage == ScreenPage.TEAM) {
			context.drawString(this.font, TEAM_IDENTIFIER_LABEL_TEXT, this.width / 2 - 153, 45, 0xA0A0A0);
			this.teamIdentifierField.render(context, mouseX, mouseY, delta);
			context.drawString(this.font, DISPLAY_NAME_LABEL_TEXT, this.width / 2 - 153, 80, 0xA0A0A0);
			this.displayNameField.render(context, mouseX, mouseY, delta);
			context.drawString(this.font, TEAM_COLOR_LABEL_TEXT, this.width / 2 - 153, 115, 0xA0A0A0);
			context.drawString(this.font, FRIENDLY_FIRE_LABEL_TEXT, this.width / 2 - 49, 115, 0xA0A0A0);
			context.drawString(this.font, FRIENDLY_INVISIBLES_LABEL_TEXT, this.width / 2 + 55, 115, 0xA0A0A0);
			context.drawString(this.font, NAME_TAG_VISIBILITY_LABEL_TEXT, this.width / 2 - 153, 150, 0xA0A0A0);
			context.drawString(this.font, DEATH_MESSAGE_VISIBILITY_LABEL_TEXT, this.width / 2 - 49, 150, 0xA0A0A0);
			context.drawString(this.font, COLLISION_RULE_LABEL_TEXT, this.width / 2 + 55, 150, 0xA0A0A0);
			context.drawString(this.font, PREFIX_LABEL_TEXT, this.width / 2 - 153, 185, 0xA0A0A0);
			context.drawString(this.font, SUFFIX_LABEL_TEXT, this.width / 2 + 5, 185, 0xA0A0A0);
			this.prefixField.render(context, mouseX, mouseY, delta);
			this.suffixField.render(context, mouseX, mouseY, delta);
		}
	}

	@Override
	protected boolean updateBlock() {
		ClientPlayNetworking.send(new UpdateTeamControllerBlockPacket(
				this.teamControllerBlockEntity.getPos(),
				this.enableTicking,
				this.showArea,
				new Vec3i(
						ItemUtils.parseInt(this.areaDimensionsXField.getValue()),
						ItemUtils.parseInt(this.areaDimensionsYField.getValue()),
						ItemUtils.parseInt(this.areaDimensionsZField.getValue())
				),
				new BlockPos(
						ItemUtils.parseInt(this.areaPositionOffsetXField.getValue()),
						ItemUtils.parseInt(this.areaPositionOffsetYField.getValue()),
						ItemUtils.parseInt(this.areaPositionOffsetZField.getValue())
				),
				new BlockPos(
						ItemUtils.parseInt(this.pvpControllerBlockPositionOffsetXField.getValue()),
						ItemUtils.parseInt(this.pvpControllerBlockPositionOffsetYField.getValue()),
						ItemUtils.parseInt(this.pvpControllerBlockPositionOffsetZField.getValue())
				),
				this.teamIdentifierField.getValue(),
				this.displayNameField.getValue(),
				this.teamColor.getId(),
				this.friendlyFire,
				this.showFriendlyInvisibles,
				this.nametagVisibility,
				this.deathMessageVisibility,
				this.collisionRule,
				this.prefixField.getValue(),
				this.suffixField.getValue()
		));
		return true;
	}

	public enum ScreenPage implements StringRepresentable {
		AREA("area"),
		TEAM("team");

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
			return Component.translatable("gui.team_controller_block.screen_page." + this.name);
		}
	}
}
