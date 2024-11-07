package com.github.theredbrain.scriptblocks.gui.screen.ingame;

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
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.CyclingButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.NarratorManager;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.glfw.GLFW;

@Environment(value = EnvType.CLIENT)
public class JigsawPlacerBlockScreen extends Screen {
	private static final Text FIRST_STRUCTURE_POOL_STRING_FIELD_TEXT = Text.translatable("gui.jigsaw_placer_block.first_structure_pool_string_field");
	private static final Text FIRST_DATA_PROVIDING_BLOCK_POSITION_TEXT = Text.translatable("gui.data_provider_block.firstDataProvidingBlockPositionOffset");
	private static final Text SECOND_STRUCTURE_POOL_STRING_FIELD_TEXT = Text.translatable("gui.jigsaw_placer_block.second_structure_pool_string_field");
	private static final Text SECOND_DATA_PROVIDING_BLOCK_POSITION_TEXT = Text.translatable("gui.data_provider_block.secondDataProvidingBlockPositionOffset");
	private static final Text JOINT_LABEL_TEXT = Text.translatable("jigsaw_block.joint_label");
	private static final Text TARGET_TEXT = Text.translatable("jigsaw_block.target");
	private static final Text TRIGGERED_BLOCK_POSITION_TEXT = Text.translatable("gui.triggered_block.triggeredBlockPositionOffset");
	private final JigsawPlacerBlockEntity jigsawPlacerBlock;
	private TextFieldWidget firstStructurePoolStringField;
	private TextFieldWidget firstDataProvidingBlockPosOffsetXField;
	private TextFieldWidget firstDataProvidingBlockPosOffsetYField;
	private TextFieldWidget firstDataProvidingBlockPosOffsetZField;
	private TextFieldWidget firstCheckedDataIdField;
	private TextFieldWidget secondStructurePoolStringField;
	private TextFieldWidget secondDataProvidingBlockPosOffsetXField;
	private TextFieldWidget secondDataProvidingBlockPosOffsetYField;
	private TextFieldWidget secondDataProvidingBlockPosOffsetZField;
	private TextFieldWidget secondCheckedDataIdField;
	private TextFieldWidget targetField;
	private CyclingButtonWidget<JigsawBlockEntity.Joint> jointRotationButton;
	private TextFieldWidget triggeredBlockPositionOffsetXField;
	private TextFieldWidget triggeredBlockPositionOffsetYField;
	private TextFieldWidget triggeredBlockPositionOffsetZField;
	private JigsawBlockEntity.Joint joint;
	private boolean triggeredBlockResets;

	public JigsawPlacerBlockScreen(JigsawPlacerBlockEntity jigsawPlacerBlock) {
		super(NarratorManager.EMPTY);
		this.jigsawPlacerBlock = jigsawPlacerBlock;
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
		boolean bl;

		// 11

		this.firstStructurePoolStringField = new TextFieldWidget(this.textRenderer, this.width / 2 - 154, 11, 200, 20, Text.empty());
		this.firstStructurePoolStringField.setMaxLength(128);
		this.firstStructurePoolStringField.setText(this.jigsawPlacerBlock.getFirstStructurePoolString());
		this.addSelectableChild(this.firstStructurePoolStringField);

		// 46
		
		this.firstDataProvidingBlockPosOffsetXField = new TextFieldWidget(this.textRenderer, this.width / 2 - 154, 46, 50, 20, Text.empty());
		this.firstDataProvidingBlockPosOffsetXField.setMaxLength(128);
		this.firstDataProvidingBlockPosOffsetXField.setText(Integer.toString(this.jigsawPlacerBlock.getFirstDataProvidingBlockPosOffset().getX()));
		this.addSelectableChild(this.firstDataProvidingBlockPosOffsetXField);

		this.firstDataProvidingBlockPosOffsetYField = new TextFieldWidget(this.textRenderer, this.width / 2 - 104, 46, 50, 20, Text.empty());
		this.firstDataProvidingBlockPosOffsetYField.setMaxLength(128);
		this.firstDataProvidingBlockPosOffsetYField.setText(Integer.toString(this.jigsawPlacerBlock.getFirstDataProvidingBlockPosOffset().getY()));
		this.addSelectableChild(this.firstDataProvidingBlockPosOffsetYField);

		this.firstDataProvidingBlockPosOffsetZField = new TextFieldWidget(this.textRenderer, this.width / 2 - 54, 46, 50, 20, Text.empty());
		this.firstDataProvidingBlockPosOffsetZField.setMaxLength(128);
		this.firstDataProvidingBlockPosOffsetZField.setText(Integer.toString(this.jigsawPlacerBlock.getFirstDataProvidingBlockPosOffset().getZ()));
		this.addSelectableChild(this.firstDataProvidingBlockPosOffsetZField);

		this.firstCheckedDataIdField = new TextFieldWidget(this.textRenderer, this.width / 2, 46, 150, 20, Text.empty());
		this.firstCheckedDataIdField.setMaxLength(128);
		this.firstCheckedDataIdField.setText(this.jigsawPlacerBlock.getFirstCheckedDataId());
		this.addSelectableChild(this.firstCheckedDataIdField);
		
		// 81

		this.secondStructurePoolStringField = new TextFieldWidget(this.textRenderer, this.width / 2 - 154, 81, 200, 20, Text.empty());
		this.secondStructurePoolStringField.setMaxLength(128);
		this.secondStructurePoolStringField.setText(this.jigsawPlacerBlock.getSecondStructurePoolString());
		this.addSelectableChild(this.secondStructurePoolStringField);

		// 116

		this.secondDataProvidingBlockPosOffsetXField = new TextFieldWidget(this.textRenderer, this.width / 2 - 154, 116, 50, 20, Text.empty());
		this.secondDataProvidingBlockPosOffsetXField.setMaxLength(128);
		this.secondDataProvidingBlockPosOffsetXField.setText(Integer.toString(this.jigsawPlacerBlock.getSecondDataProvidingBlockPosOffset().getX()));
		this.addSelectableChild(this.secondDataProvidingBlockPosOffsetXField);

		this.secondDataProvidingBlockPosOffsetYField = new TextFieldWidget(this.textRenderer, this.width / 2 - 104, 116, 50, 20, Text.empty());
		this.secondDataProvidingBlockPosOffsetYField.setMaxLength(128);
		this.secondDataProvidingBlockPosOffsetYField.setText(Integer.toString(this.jigsawPlacerBlock.getSecondDataProvidingBlockPosOffset().getY()));
		this.addSelectableChild(this.secondDataProvidingBlockPosOffsetYField);

		this.secondDataProvidingBlockPosOffsetZField = new TextFieldWidget(this.textRenderer, this.width / 2 - 54, 116, 50, 20, Text.empty());
		this.secondDataProvidingBlockPosOffsetZField.setMaxLength(128);
		this.secondDataProvidingBlockPosOffsetZField.setText(Integer.toString(this.jigsawPlacerBlock.getSecondDataProvidingBlockPosOffset().getZ()));
		this.addSelectableChild(this.secondDataProvidingBlockPosOffsetZField);

		this.secondCheckedDataIdField = new TextFieldWidget(this.textRenderer, this.width / 2, 116, 150, 20, Text.empty());
		this.secondCheckedDataIdField.setMaxLength(128);
		this.secondCheckedDataIdField.setText(this.jigsawPlacerBlock.getSecondCheckedDataId());
		this.addSelectableChild(this.secondCheckedDataIdField);

		// 151

		this.targetField = new TextFieldWidget(this.textRenderer, this.width / 2 - 153, 151, 150, 20, Text.translatable("jigsaw_block.target"));
		this.targetField.setMaxLength(128);
		this.targetField.setText(this.jigsawPlacerBlock.getTarget().toString());
		this.addSelectableChild(this.targetField);

		this.joint = this.jigsawPlacerBlock.getJoint();
		this.jointRotationButton = this.addDrawableChild(
				CyclingButtonWidget.<JigsawBlockEntity.Joint>builder(JigsawBlockEntity.Joint::asText)
						.values(JigsawBlockEntity.Joint.values())
						.initially(this.joint)
						.omitKeyText()
						.build(this.width / 2 + 4, 151, 150, 20, JOINT_LABEL_TEXT, (button, joint) -> this.joint = joint)
		);
		this.jointRotationButton.active = bl = JigsawBlock.getFacing(this.jigsawPlacerBlock.getCachedState()).getAxis().isVertical();
		this.jointRotationButton.visible = bl;

		// 186

		this.triggeredBlockPositionOffsetXField = new TextFieldWidget(this.textRenderer, this.width / 2 - 154, 186, 50, 20, Text.empty());
		this.triggeredBlockPositionOffsetXField.setMaxLength(128);
		this.triggeredBlockPositionOffsetXField.setText(Integer.toString(this.jigsawPlacerBlock.getTriggeredBlock().getLeft().getX()));
		this.addSelectableChild(this.triggeredBlockPositionOffsetXField);
		this.triggeredBlockPositionOffsetYField = new TextFieldWidget(this.textRenderer, this.width / 2 - 100, 186, 50, 20, Text.empty());
		this.triggeredBlockPositionOffsetYField.setMaxLength(128);
		this.triggeredBlockPositionOffsetYField.setText(Integer.toString(this.jigsawPlacerBlock.getTriggeredBlock().getLeft().getY()));
		this.addSelectableChild(this.triggeredBlockPositionOffsetYField);
		this.triggeredBlockPositionOffsetZField = new TextFieldWidget(this.textRenderer, this.width / 2 - 46, 186, 50, 20, Text.empty());
		this.triggeredBlockPositionOffsetZField.setMaxLength(128);
		this.triggeredBlockPositionOffsetZField.setText(Integer.toString(this.jigsawPlacerBlock.getTriggeredBlock().getLeft().getZ()));
		this.addSelectableChild(this.triggeredBlockPositionOffsetZField);
		this.triggeredBlockResets = this.jigsawPlacerBlock.getTriggeredBlock().getRight();
		this.addDrawableChild(CyclingButtonWidget.onOffBuilder(Text.translatable("gui.triggered_block.toggle_triggered_block_resets_button_label.on"), Text.translatable("gui.triggered_block.toggle_triggered_block_resets_button_label.off")).initially(this.triggeredBlockResets).omitKeyText().build(this.width / 2 + 8, 186, 150, 20, Text.empty(), (button, triggeredBlockResets) -> {
			this.triggeredBlockResets = triggeredBlockResets;
		}));
		
		this.addDrawableChild(ButtonWidget.builder(ScreenTexts.DONE, button -> this.done()).dimensions(this.width / 2 - 4 - 150, 210, 150, 20).build());
		this.addDrawableChild(ButtonWidget.builder(ScreenTexts.CANCEL, button -> this.cancel()).dimensions(this.width / 2 + 4, 210, 150, 20).build());
	}

	@Override
	protected void setInitialFocus() {
		this.setInitialFocus(this.firstStructurePoolStringField);
	}

	@Override
	public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
		this.renderInGameBackground(context);
	}

	@Override
	public void resize(MinecraftClient client, int width, int height) {
		String string = this.firstStructurePoolStringField.getText();
		String string1 = this.firstDataProvidingBlockPosOffsetXField.getText();
		String string2 = this.firstDataProvidingBlockPosOffsetYField.getText();
		String string3 = this.firstDataProvidingBlockPosOffsetZField.getText();
		String string4 = this.firstCheckedDataIdField.getText();
		String string5 = this.secondStructurePoolStringField.getText();
		String string6 = this.secondDataProvidingBlockPosOffsetXField.getText();
		String string7 = this.secondDataProvidingBlockPosOffsetYField.getText();
		String string8 = this.secondDataProvidingBlockPosOffsetZField.getText();
		String string9 = this.secondCheckedDataIdField.getText();
		String string10 = this.targetField.getText();
		String string11 = this.triggeredBlockPositionOffsetXField.getText();
		String string12 = this.triggeredBlockPositionOffsetYField.getText();
		String string13 = this.triggeredBlockPositionOffsetZField.getText();
		JigsawBlockEntity.Joint joint = this.joint;
		boolean bl = this.triggeredBlockResets;
		this.init(client, width, height);
		this.firstStructurePoolStringField.setText(string);
		this.firstDataProvidingBlockPosOffsetXField.setText(string1);
		this.firstDataProvidingBlockPosOffsetYField.setText(string2);
		this.firstDataProvidingBlockPosOffsetZField.setText(string3);
		this.firstCheckedDataIdField.setText(string4);
		this.secondStructurePoolStringField.setText(string5);
		this.secondDataProvidingBlockPosOffsetXField.setText(string6);
		this.secondDataProvidingBlockPosOffsetYField.setText(string7);
		this.secondDataProvidingBlockPosOffsetZField.setText(string8);
		this.secondCheckedDataIdField.setText(string9);
		this.targetField.setText(string10);
		this.triggeredBlockPositionOffsetXField.setText(string11);
		this.triggeredBlockPositionOffsetYField.setText(string12);
		this.triggeredBlockPositionOffsetZField.setText(string13);
		this.joint = joint;
		this.jointRotationButton.setValue(joint);
		this.triggeredBlockResets = bl;
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (super.keyPressed(keyCode, scanCode, modifiers)) {
			return true;
		}
		if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
			this.done();
			return true;
		}
		return false;
	}

	private boolean updateJigsawPlacerBlock() {
		ClientPlayNetworking.send(new UpdateJigsawPlacerBlockPacket(
				this.jigsawPlacerBlock.getPos(),
				this.firstStructurePoolStringField.getText(),
				new BlockPos(
						ItemUtils.parseInt(this.firstDataProvidingBlockPosOffsetXField.getText()),
						ItemUtils.parseInt(this.firstDataProvidingBlockPosOffsetYField.getText()),
						ItemUtils.parseInt(this.firstDataProvidingBlockPosOffsetZField.getText())
				),
				this.firstCheckedDataIdField.getText(),
				this.secondStructurePoolStringField.getText(),
				new BlockPos(
						ItemUtils.parseInt(this.secondDataProvidingBlockPosOffsetXField.getText()),
						ItemUtils.parseInt(this.secondDataProvidingBlockPosOffsetYField.getText()),
						ItemUtils.parseInt(this.secondDataProvidingBlockPosOffsetZField.getText())
				),
				this.secondCheckedDataIdField.getText(),
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

		context.drawTextWithShadow(this.textRenderer, FIRST_STRUCTURE_POOL_STRING_FIELD_TEXT, this.width / 2 - 153, 1, 0xA0A0A0);
		this.firstStructurePoolStringField.render(context, mouseX, mouseY, delta);

		context.drawTextWithShadow(this.textRenderer, FIRST_DATA_PROVIDING_BLOCK_POSITION_TEXT, this.width / 2 - 153, 36, 0xA0A0A0);
		this.firstDataProvidingBlockPosOffsetXField.render(context, mouseX, mouseY, delta);
		this.firstDataProvidingBlockPosOffsetYField.render(context, mouseX, mouseY, delta);
		this.firstDataProvidingBlockPosOffsetZField.render(context, mouseX, mouseY, delta);
		this.firstCheckedDataIdField.render(context, mouseX, mouseY, delta);

		context.drawTextWithShadow(this.textRenderer, SECOND_STRUCTURE_POOL_STRING_FIELD_TEXT, this.width / 2 - 153, 71, 0xA0A0A0);
		this.secondStructurePoolStringField.render(context, mouseX, mouseY, delta);

		context.drawTextWithShadow(this.textRenderer, SECOND_DATA_PROVIDING_BLOCK_POSITION_TEXT, this.width / 2 - 153, 106, 0xA0A0A0);
		this.secondDataProvidingBlockPosOffsetXField.render(context, mouseX, mouseY, delta);
		this.secondDataProvidingBlockPosOffsetYField.render(context, mouseX, mouseY, delta);
		this.secondDataProvidingBlockPosOffsetZField.render(context, mouseX, mouseY, delta);
		this.secondCheckedDataIdField.render(context, mouseX, mouseY, delta);

		context.drawTextWithShadow(this.textRenderer, TARGET_TEXT, this.width / 2 - 153, 141, 0xA0A0A0);
		this.targetField.render(context, mouseX, mouseY, delta);

		context.drawTextWithShadow(this.textRenderer, TRIGGERED_BLOCK_POSITION_TEXT, this.width / 2 - 153, 176, 0xA0A0A0);
		this.triggeredBlockPositionOffsetXField.render(context, mouseX, mouseY, delta);
		this.triggeredBlockPositionOffsetYField.render(context, mouseX, mouseY, delta);
		this.triggeredBlockPositionOffsetZField.render(context, mouseX, mouseY, delta);
	}

	@Override
	public boolean shouldPause() {
		return false;
	}
}
