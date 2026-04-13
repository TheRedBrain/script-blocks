package com.github.theredbrain.scriptblocks.gui.screen.ingame;

import com.github.theredbrain.scriptblocks.ScriptBlocks;
import com.github.theredbrain.scriptblocks.block.entity.JigsawPlacerBlockEntity;
import com.github.theredbrain.scriptblocks.network.packet.UpdateJigsawPlacerBlockPacket;
import com.github.theredbrain.scriptblocks.util.ItemUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.block.JigsawBlock;
import net.minecraft.block.entity.JigsawBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ButtonTextures;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.CyclingButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.TexturedButtonWidget;
import net.minecraft.client.util.NarratorManager;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import org.apache.commons.lang3.tuple.MutablePair;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

@Environment(value = EnvType.CLIENT)
public class JigsawPlacerBlockScreen extends Screen {
	private static final Text STRUCTURE_POOL_STRING_FIELD_TEXT = Text.translatable("gui.jigsaw_placer_block.structure_pool_string_field");
	private static final Text NEW_DATA_BLOCK_LABEL_TEXT = Text.translatable("gui.jigsaw_placer_block.new_data_block_label");
	private static final Text NEW_STATIC_APPENDIX_LABEL_TEXT = Text.translatable("gui.jigsaw_placer_block.new_static_appendix_label");
	private static final Text TARGET_LABEL_TEXT = Text.translatable("jigsaw_block.target");
	private static final Text JOINT_LABEL_TEXT = Text.translatable("jigsaw_block.joint_label");
	private static final Text TRIGGERED_BLOCK_POSITION_TEXT = Text.translatable("gui.jigsaw_placer_block.triggered_block_label");
	private static final Identifier SCROLL_BAR_BACKGROUND_8_54_TEXTURE = ScriptBlocks.identifier("scroll_bar/scroll_bar_background_8_54");
	private static final Identifier SCROLLER_TEXTURE = ScriptBlocks.identifier("scroll_bar/scroller_vertical_6_7");
	public static final ButtonTextures REMOVE_ENTRY_BUTTON_TEXTURES = new ButtonTextures(
			Identifier.of(ScriptBlocks.MOD_ID, "widgets/remove_entry_button"), Identifier.of(ScriptBlocks.MOD_ID, "widgets/remove_entry_button_highlighted")
	);
	private static final int VISIBLE_LIST_ELEMENTS = 2;
	private final JigsawPlacerBlockEntity jigsawPlacerBlock;
	private TextFieldWidget structurePoolStringField;

	private final List<MutablePair<BlockPos, MutablePair<String, String>>> structurePoolStringAppendices = new ArrayList<>(List.of());
	private ButtonWidget removeStructurePoolAppendixButton0;
	private ButtonWidget removeStructurePoolAppendixButton1;

	private TextFieldWidget newAppendixBlockPosOffsetXField;
	private TextFieldWidget newAppendixBlockPosOffsetYField;
	private TextFieldWidget newAppendixBlockPosOffsetZField;
	private TextFieldWidget newAppendixDataIdField;
	private TextFieldWidget newStaticAppendixStringField;

	private TextFieldWidget targetField;
	private CyclingButtonWidget<JigsawBlockEntity.Joint> jointRotationButton;

	private TextFieldWidget triggeredBlockPositionOffsetXField;
	private TextFieldWidget triggeredBlockPositionOffsetYField;
	private TextFieldWidget triggeredBlockPositionOffsetZField;
	private CyclingButtonWidget<Boolean> toggleTriggeredBlockResetsButton;

	private JigsawBlockEntity.Joint joint;
	private boolean triggeredBlockResets;
	private int scrollPosition = 0;
	private float scrollAmount = 0.0f;
	private boolean mouseClicked = false;

	public JigsawPlacerBlockScreen(JigsawPlacerBlockEntity jigsawPlacerBlock) {
		super(NarratorManager.EMPTY);
		this.jigsawPlacerBlock = jigsawPlacerBlock;
	}

	private void removeStructurePoolAppendix(int index) {
		if (index + this.scrollPosition < this.structurePoolStringAppendices.size()) {
			this.structurePoolStringAppendices.remove(index + this.scrollPosition);
		}
		this.scrollPosition = 0;
		this.scrollAmount = 0.0f;
		this.updateWidgets();
	}

	private void addNewStructurePoolStringAppendix() {
		MutablePair<BlockPos, MutablePair<String, String>> newStructurePoolStringAppendix = new MutablePair<>(
				new BlockPos(
						ItemUtils.parseInt(this.newAppendixBlockPosOffsetXField.getText()),
						ItemUtils.parseInt(this.newAppendixBlockPosOffsetYField.getText()),
						ItemUtils.parseInt(this.newAppendixBlockPosOffsetZField.getText())
				),
				new MutablePair<>(
						this.newAppendixDataIdField.getText(),
						this.newStaticAppendixStringField.getText()
				)
		);
		if (newStructurePoolStringAppendix.getLeft().equals(BlockPos.ORIGIN)) {
			if (this.client != null && this.client.player != null) {
				this.client.player.sendMessage(Text.translatable("gui.jigsaw_placer_block.appendix_offset_is_origin"));
			}
			return;
		}
		for (MutablePair<BlockPos, MutablePair<String, String>> structurePoolStringAppendix : this.structurePoolStringAppendices) {
			if (structurePoolStringAppendix.equals(newStructurePoolStringAppendix)) {
				if (this.client != null && this.client.player != null) {
					this.client.player.sendMessage(Text.translatable("gui.jigsaw_placer_block.structure_pool_string_appendix_is_duplicate"));
				}
				return;
			}
		}
		this.structurePoolStringAppendices.add(newStructurePoolStringAppendix);
		this.scrollPosition = 0;
		this.scrollAmount = 0.0f;
		this.updateWidgets();
	}

	private void done() {
		if (this.updateJigsawPlacerBlock()) {
			this.close();
		}
	}

	private void cancel() {
		this.close();
	}

	@Override
	protected void init() {

		this.structurePoolStringAppendices.clear();
		this.structurePoolStringAppendices.addAll(this.jigsawPlacerBlock.getStructurePoolStringAppendices());

		int structurePoolLabelTextWidth = this.textRenderer.getWidth(STRUCTURE_POOL_STRING_FIELD_TEXT);
		this.structurePoolStringField = new TextFieldWidget(this.textRenderer, this.width / 2 - 203 + 4 + structurePoolLabelTextWidth, 11, 408 - 5 - structurePoolLabelTextWidth, 20, Text.empty());
		this.structurePoolStringField.setMaxLength(128);
		this.structurePoolStringField.setText(this.jigsawPlacerBlock.getStructurePoolString());
		this.addSelectableChild(this.structurePoolStringField);

		this.removeStructurePoolAppendixButton0 = this.addDrawableChild(new TexturedButtonWidget(this.width / 2 - 191, 40, 20, 20, REMOVE_ENTRY_BUTTON_TEXTURES, button -> this.removeStructurePoolAppendix(0)));
		this.removeStructurePoolAppendixButton1 = this.addDrawableChild(new TexturedButtonWidget(this.width / 2 - 191, 74, 20, 20, REMOVE_ENTRY_BUTTON_TEXTURES, button -> this.removeStructurePoolAppendix(1)));

		this.addDrawableChild(ButtonWidget.builder(Text.translatable("gui.jigsaw_placer_block.add_new_structure_pool_string_appendix_button_label"), button -> this.addNewStructurePoolStringAppendix()).dimensions(this.width / 2 - 204, 103, 408, 20).build());

		this.newAppendixBlockPosOffsetXField = new TextFieldWidget(this.textRenderer, this.width / 2 - 204, 138, 30, 20, Text.empty());
		this.newAppendixBlockPosOffsetXField.setMaxLength(128);
		this.newAppendixBlockPosOffsetXField.setPlaceholder(Text.literal("X"));
		this.addSelectableChild(this.newAppendixBlockPosOffsetXField);

		this.newAppendixBlockPosOffsetYField = new TextFieldWidget(this.textRenderer, this.width / 2 - 174, 138, 30, 20, Text.empty());
		this.newAppendixBlockPosOffsetYField.setMaxLength(128);
		this.newAppendixBlockPosOffsetYField.setPlaceholder(Text.literal("Y"));
		this.addSelectableChild(this.newAppendixBlockPosOffsetYField);

		this.newAppendixBlockPosOffsetZField = new TextFieldWidget(this.textRenderer, this.width / 2 - 144, 138, 30, 20, Text.empty());
		this.newAppendixBlockPosOffsetZField.setMaxLength(128);
		this.newAppendixBlockPosOffsetZField.setPlaceholder(Text.literal("Z"));
		this.addSelectableChild(this.newAppendixBlockPosOffsetZField);

		this.newAppendixDataIdField = new TextFieldWidget(this.textRenderer, this.width / 2 - 110, 138, 106, 20, Text.empty());
		this.newAppendixDataIdField.setMaxLength(128);
		this.addSelectableChild(this.newAppendixDataIdField);

		this.newStaticAppendixStringField = new TextFieldWidget(this.textRenderer, this.width / 2 + 4, 138, 200, 20, Text.empty());
		this.newStaticAppendixStringField.setMaxLength(128);
		this.addSelectableChild(this.newStaticAppendixStringField);

		int targetLabelTextWidth = this.textRenderer.getWidth(TARGET_LABEL_TEXT);
		this.targetField = new TextFieldWidget(this.textRenderer, this.width / 2 - 203 + 4 + targetLabelTextWidth, 162, 200 - 5 - targetLabelTextWidth, 20, Text.translatable("jigsaw_block.target"));
		this.targetField.setMaxLength(128);
		this.targetField.setText(this.jigsawPlacerBlock.getTarget().toString());
		this.addSelectableChild(this.targetField);

		int jointTextWidth = this.textRenderer.getWidth(JOINT_LABEL_TEXT);
		this.joint = this.jigsawPlacerBlock.getJoint();
		this.jointRotationButton = this.addDrawableChild(
				CyclingButtonWidget.<JigsawBlockEntity.Joint>builder(JigsawBlockEntity.Joint::asText)
						.values(JigsawBlockEntity.Joint.values())
						.initially(this.joint)
						.omitKeyText()
						.build(this.width / 2 + 5 + jointTextWidth + 4, 162, 200 - 5 - jointTextWidth, 20, JOINT_LABEL_TEXT, (button, joint) -> this.joint = joint)
		);
		boolean bl;
		this.jointRotationButton.active = bl = JigsawBlock.getFacing(this.jigsawPlacerBlock.getCachedState()).getAxis().isVertical();
		this.jointRotationButton.visible = bl;

		this.triggeredBlockPositionOffsetXField = new TextFieldWidget(this.textRenderer, this.width / 2 - 94, 186, 30, 20, Text.empty());
		this.triggeredBlockPositionOffsetXField.setMaxLength(128);
		this.triggeredBlockPositionOffsetXField.setText(Integer.toString(this.jigsawPlacerBlock.getTriggeredBlock().getLeft().getX()));
		this.triggeredBlockPositionOffsetXField.setPlaceholder(Text.literal("X"));
		this.addSelectableChild(this.triggeredBlockPositionOffsetXField);
		this.triggeredBlockPositionOffsetYField = new TextFieldWidget(this.textRenderer, this.width / 2 - 64, 186, 30, 20, Text.empty());
		this.triggeredBlockPositionOffsetYField.setMaxLength(128);
		this.triggeredBlockPositionOffsetYField.setText(Integer.toString(this.jigsawPlacerBlock.getTriggeredBlock().getLeft().getY()));
		this.triggeredBlockPositionOffsetYField.setPlaceholder(Text.literal("Y"));
		this.addSelectableChild(this.triggeredBlockPositionOffsetYField);
		this.triggeredBlockPositionOffsetZField = new TextFieldWidget(this.textRenderer, this.width / 2 - 34, 186, 30, 20, Text.empty());
		this.triggeredBlockPositionOffsetZField.setMaxLength(128);
		this.triggeredBlockPositionOffsetZField.setText(Integer.toString(this.jigsawPlacerBlock.getTriggeredBlock().getLeft().getZ()));
		this.triggeredBlockPositionOffsetZField.setPlaceholder(Text.literal("Z"));
		this.addSelectableChild(this.triggeredBlockPositionOffsetZField);

		this.triggeredBlockResets = this.jigsawPlacerBlock.getTriggeredBlock().getRight();
		this.toggleTriggeredBlockResetsButton = this.addDrawableChild(CyclingButtonWidget.onOffBuilder(Text.translatable("gui.jigsaw_placer_block.toggle_triggered_block_resets_button_label.on"), Text.translatable("gui.jigsaw_placer_block.toggle_triggered_block_resets_button_label.off")).initially(this.triggeredBlockResets).omitKeyText().build(this.width / 2 + 4, 186, 200, 20, Text.empty(), (button, triggeredBlockResets) -> {
			this.triggeredBlockResets = triggeredBlockResets;
		}));

		this.addDrawableChild(ButtonWidget.builder(ScreenTexts.DONE, button -> this.done()).dimensions(this.width / 2 - 4 - 200, 210, 200, 20).build());
		this.addDrawableChild(ButtonWidget.builder(ScreenTexts.CANCEL, button -> this.cancel()).dimensions(this.width / 2 + 4, 210, 200, 20).build());

		this.updateWidgets();
	}

	@Override
	protected void setInitialFocus() {
		this.setInitialFocus(this.structurePoolStringField);
	}

	@Override
	public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
		this.renderInGameBackground(context);
	}

	private void updateWidgets() {

		this.removeStructurePoolAppendixButton0.visible = false;
		this.removeStructurePoolAppendixButton1.visible = false;

		int index = 0;
		for (int i = 0; i < Math.min(VISIBLE_LIST_ELEMENTS, this.structurePoolStringAppendices.size()); i++) {
			if (index == 0) {
				this.removeStructurePoolAppendixButton0.visible = true;
			} else if (index == 1) {
				this.removeStructurePoolAppendixButton1.visible = true;
			}
			index++;
		}
	}

	@Override
	public void resize(MinecraftClient client, int width, int height) {
		List<MutablePair<BlockPos, MutablePair<String, String>>> list = new ArrayList<>(this.structurePoolStringAppendices);
		String string = this.structurePoolStringField.getText();
		String string5 = this.newStaticAppendixStringField.getText();
		String string6 = this.newAppendixBlockPosOffsetXField.getText();
		String string7 = this.newAppendixBlockPosOffsetYField.getText();
		String string8 = this.newAppendixBlockPosOffsetZField.getText();
		String string9 = this.newAppendixDataIdField.getText();
		String string10 = this.targetField.getText();
		String string11 = this.triggeredBlockPositionOffsetXField.getText();
		String string12 = this.triggeredBlockPositionOffsetYField.getText();
		String string13 = this.triggeredBlockPositionOffsetZField.getText();
		JigsawBlockEntity.Joint joint = this.joint;
		boolean bl = this.triggeredBlockResets;
		this.init(client, width, height);
		this.structurePoolStringAppendices.clear();
		this.structurePoolStringAppendices.addAll(list);
		this.structurePoolStringField.setText(string);
		this.newStaticAppendixStringField.setText(string5);
		this.newAppendixBlockPosOffsetXField.setText(string6);
		this.newAppendixBlockPosOffsetYField.setText(string7);
		this.newAppendixBlockPosOffsetZField.setText(string8);
		this.newAppendixDataIdField.setText(string9);
		this.targetField.setText(string10);
		this.triggeredBlockPositionOffsetXField.setText(string11);
		this.triggeredBlockPositionOffsetYField.setText(string12);
		this.triggeredBlockPositionOffsetZField.setText(string13);
		this.joint = joint;
		this.jointRotationButton.setValue(joint);
		this.triggeredBlockResets = bl;
		this.toggleTriggeredBlockResetsButton.setValue(bl);
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		this.mouseClicked = false;
		if (this.structurePoolStringAppendices.size() > VISIBLE_LIST_ELEMENTS) {
			int i = this.width / 2 - 202;
			int j = 40;
			if (mouseX >= (double) i && mouseX < (double) (i + 6) && mouseY >= (double) j && mouseY < (double) (j + 54)) {
				this.mouseClicked = true;
			}
		}
		return super.mouseClicked(mouseX, mouseY, button);
	}

	@Override
	public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
		if (this.structurePoolStringAppendices.size() > VISIBLE_LIST_ELEMENTS
				&& this.mouseClicked) {
			int i = this.structurePoolStringAppendices.size() - VISIBLE_LIST_ELEMENTS;
			float f = (float) deltaY / (float) i;
			this.scrollAmount = MathHelper.clamp(this.scrollAmount + f, 0.0f, 1.0f);
			this.scrollPosition = (int) ((double) (this.scrollAmount * (float) i));
		}
		return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
		if (this.structurePoolStringAppendices.size() > VISIBLE_LIST_ELEMENTS
				&& mouseX >= (double) (this.width / 2 - 202) && mouseX <= (double) (this.width / 2 + 204)
				&& mouseY >= 40 && mouseY <= 94) {
			int i = this.structurePoolStringAppendices.size() - VISIBLE_LIST_ELEMENTS;
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

	private boolean updateJigsawPlacerBlock() {
		ClientPlayNetworking.send(new UpdateJigsawPlacerBlockPacket(
				this.jigsawPlacerBlock.getPos(),
				this.structurePoolStringField.getText(),
				this.structurePoolStringAppendices,
				this.targetField.getText(),
				this.joint,
				new BlockPos(
						ItemUtils.parseInt(this.triggeredBlockPositionOffsetXField.getText()),
						ItemUtils.parseInt(this.triggeredBlockPositionOffsetYField.getText()),
						ItemUtils.parseInt(this.triggeredBlockPositionOffsetZField.getText())
				),
				this.triggeredBlockResets
		));
		return true;
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {

		super.render(context, mouseX, mouseY, delta);

		context.drawTextWithShadow(this.textRenderer, STRUCTURE_POOL_STRING_FIELD_TEXT, this.width / 2 - 203, 17, 0xA0A0A0);
		this.structurePoolStringField.render(context, mouseX, mouseY, delta);

		for (int i = this.scrollPosition; i < Math.min(this.scrollPosition + VISIBLE_LIST_ELEMENTS, this.structurePoolStringAppendices.size()); i++) {
			MutablePair<BlockPos, MutablePair<String, String>> structurePoolStringAppendix = this.structurePoolStringAppendices.get(i);
			BlockPos dataBlockPositionOffset = structurePoolStringAppendix.getLeft();
			MutableText text1 = Text.translatable("gui.jigsaw_placer_block.structure_pool_string_appendices.entry.1", dataBlockPositionOffset.getX(), dataBlockPositionOffset.getY(), dataBlockPositionOffset.getZ(), structurePoolStringAppendix.getRight().getLeft());
			MutableText text2 = Text.translatable("gui.jigsaw_placer_block.structure_pool_string_appendices.entry.2", structurePoolStringAppendix.getRight().getRight());

			context.drawTextWithShadow(this.textRenderer, text1, this.width / 2 - 167, 41 + ((i - this.scrollPosition) * 34), 0xA0A0A0);
			context.drawTextWithShadow(this.textRenderer, text2, this.width / 2 - 167, 51 + ((i - this.scrollPosition) * 34), 0xA0A0A0);
		}
		if (this.structurePoolStringAppendices.size() > VISIBLE_LIST_ELEMENTS) {
			context.drawGuiTexture(SCROLL_BAR_BACKGROUND_8_54_TEXTURE, this.width / 2 - 203, 40, 8, 54);
			int k = (int) (45.0f * this.scrollAmount);
			context.drawGuiTexture(SCROLLER_TEXTURE, this.width / 2 - 202, 40 + 1 + k, 6, 7);
		}

		context.drawTextWithShadow(this.textRenderer, NEW_DATA_BLOCK_LABEL_TEXT, this.width / 2 - 203, 128, 0xA0A0A0);
		this.newAppendixBlockPosOffsetXField.render(context, mouseX, mouseY, delta);
		this.newAppendixBlockPosOffsetYField.render(context, mouseX, mouseY, delta);
		this.newAppendixBlockPosOffsetZField.render(context, mouseX, mouseY, delta);
		this.newAppendixDataIdField.render(context, mouseX, mouseY, delta);
		context.drawTextWithShadow(this.textRenderer, NEW_STATIC_APPENDIX_LABEL_TEXT, this.width / 2 + 5, 128, 0xA0A0A0);
		this.newStaticAppendixStringField.render(context, mouseX, mouseY, delta);

		context.drawTextWithShadow(this.textRenderer, TARGET_LABEL_TEXT, this.width / 2 - 203, 168, 0xA0A0A0);
		this.targetField.render(context, mouseX, mouseY, delta);

		context.drawTextWithShadow(this.textRenderer, JOINT_LABEL_TEXT, this.width / 2 + 5, 168, 0xA0A0A0);

		int textWidth = this.textRenderer.getWidth(TRIGGERED_BLOCK_POSITION_TEXT);
		context.drawTextWithShadow(this.textRenderer, TRIGGERED_BLOCK_POSITION_TEXT, this.width / 2 - 94 - 4 - textWidth, 192, 0xA0A0A0);
		this.triggeredBlockPositionOffsetXField.render(context, mouseX, mouseY, delta);
		this.triggeredBlockPositionOffsetYField.render(context, mouseX, mouseY, delta);
		this.triggeredBlockPositionOffsetZField.render(context, mouseX, mouseY, delta);
	}

	@Override
	public boolean shouldPause() {
		return false;
	}
}
