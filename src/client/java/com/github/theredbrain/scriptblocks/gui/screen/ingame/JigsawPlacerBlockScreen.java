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
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

@Environment(value = EnvType.CLIENT)
public class JigsawPlacerBlockScreen extends Screen {
	private static final int VISIBLE_STRUCTURE_POOL_LIST_ENTRY = 2;
	private static final Text JOINT_LABEL_TEXT = Text.translatable("jigsaw_block.joint_label");
	private static final Text TARGET_TEXT = Text.translatable("jigsaw_block.target");
	private static final Text TRIGGERED_BLOCK_POSITION_TEXT = Text.translatable("gui.triggered_block.triggeredBlockPositionOffset");
	private static final Text DATA_PROVIDING_BLOCK_POSITION_TEXT = Text.translatable("gui.data_provider_block.dataProvidingBlockPositionOffset");
	private static final Text NEW_STRUCTURE_POOL_FIELD_TEXT = Text.translatable("gui.jigsaw_placer_block.new_structure_pool_field");
	private static final Text ADD_NEW_STRUCTURE_POOL_BUTTON_LABEL_TEXT = Text.translatable("gui.jigsaw_placer_block.add_new_structure_pool_button_label");
	private static final Identifier SCROLL_BAR_BACKGROUND_8_70_TEXTURE = ScriptBlocks.identifier("scroll_bar/scroll_bar_background_8_70");
	private static final Identifier SCROLLER_TEXTURE = ScriptBlocks.identifier("scroll_bar/scroller_vertical_6_7");
	public static final ButtonTextures REMOVE_ENTRY_BUTTON_TEXTURES = new ButtonTextures(
			Identifier.of(ScriptBlocks.MOD_ID, "widgets/remove_entry_button"), Identifier.of(ScriptBlocks.MOD_ID, "widgets/remove_entry_button_highlighted")
	);
	private final JigsawPlacerBlockEntity jigsawPlacerBlock;
	private ButtonWidget removeStructurePoolButton0;
	private ButtonWidget removeStructurePoolButton1;
	private TextFieldWidget newStructurePoolField;
	private ButtonWidget addNewStructurePoolButton;
	private TextFieldWidget targetField;
	private CyclingButtonWidget<JigsawBlockEntity.Joint> jointRotationButton;
	private TextFieldWidget triggeredBlockPositionOffsetXField;
	private TextFieldWidget triggeredBlockPositionOffsetYField;
	private TextFieldWidget triggeredBlockPositionOffsetZField;
	private CyclingButtonWidget<Boolean> toggleTriggeredBlockResetsButton;
	private TextFieldWidget dataProvidingBlockPosOffsetXField;
	private TextFieldWidget dataProvidingBlockPosOffsetYField;
	private TextFieldWidget dataProvidingBlockPosOffsetZField;
	private TextFieldWidget checkedDataIdField;
	private List<String> structurePoolList = new ArrayList<>();
	private ButtonWidget doneButton;
	private JigsawBlockEntity.Joint joint;
	private boolean triggeredBlockResets;
	private int structurePoolListScrollPosition = 0;
	private float structurePoolListScrollAmount = 0.0f;
	private boolean structurePoolListMouseClicked = false;

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

	private void addNewStructurePoolToList(String identifier) {
		this.structurePoolList.add(identifier);
		this.updateDoneButtonState();
		this.updateWidgets();
	}

	private void removeStructurePoolFromList(int index) {
		if (index + this.structurePoolListScrollPosition < this.structurePoolList.size()) {
			this.structurePoolList.remove(index + this.structurePoolListScrollPosition);
		}
		this.updateDoneButtonState();
		this.updateWidgets();
	}

	@Override
	protected void init() {
		boolean bl;
		this.structurePoolList.addAll(this.jigsawPlacerBlock.getStructurePoolList());

		this.removeStructurePoolButton0 = this.addDrawableChild(new TexturedButtonWidget(this.width / 2 - 141, 22, 20, 20, REMOVE_ENTRY_BUTTON_TEXTURES, button -> this.removeStructurePoolFromList(0)));
		this.removeStructurePoolButton1 = this.addDrawableChild(new TexturedButtonWidget(this.width / 2 - 141, 46, 20, 20, REMOVE_ENTRY_BUTTON_TEXTURES, button -> this.removeStructurePoolFromList(1)));

		this.newStructurePoolField = new TextFieldWidget(this.textRenderer, this.width / 2 - 154, 81, 200, 20, Text.empty());
		this.newStructurePoolField.setMaxLength(128);
		this.addSelectableChild(this.newStructurePoolField);

		this.addNewStructurePoolButton = this.addDrawableChild(ButtonWidget.builder(ADD_NEW_STRUCTURE_POOL_BUTTON_LABEL_TEXT, button -> this.addNewStructurePoolToList(this.newStructurePoolField.getText())).dimensions(this.width / 2 + 54, 81, 100, 20).build());

//		this.poolField = new TextFieldWidget(this.textRenderer, this.width / 2 - 153, 55, 300, 20, Text.translatable("jigsaw_block.pool"));
//		this.poolField.setMaxLength(128);
//		this.poolField.setText(this.jigsawPlacerBlock.getPool().getValue().toString());
//		this.poolField.setChangedListener(pool -> this.updateDoneButtonState());
//		this.addSelectableChild(this.poolField);
		// 33   22
		// 57   46
		// 92   81
		this.targetField = new TextFieldWidget(this.textRenderer, this.width / 2 - 153, 116, 150, 20, Text.translatable("jigsaw_block.target"));
		this.targetField.setMaxLength(128);
		this.targetField.setText(this.jigsawPlacerBlock.getTarget().toString());
		this.targetField.setChangedListener(target -> this.updateDoneButtonState());
		this.addSelectableChild(this.targetField);
		this.joint = this.jigsawPlacerBlock.getJoint();
//		int i = this.textRenderer.getWidth(JOINT_LABEL_TEXT) + 10;
		this.jointRotationButton = this.addDrawableChild(
				CyclingButtonWidget.<JigsawBlockEntity.Joint>builder(JigsawBlockEntity.Joint::asText)
						.values(JigsawBlockEntity.Joint.values())
						.initially(this.joint)
						.omitKeyText()
						.build(this.width / 2 + 4, 116, 150, 20, JOINT_LABEL_TEXT, (button, joint) -> this.joint = joint)
		);
//		this.jointRotationButton = this.addDrawableChild(CyclingButtonWidget.builder(JigsawBlockEntity.Joint::asText).values((JigsawBlockEntity.Joint[]) JigsawBlockEntity.Joint.values()).initially(this.joint).omitKeyText().build(this.width / 2 + 4, 116, 150, 20, JOINT_LABEL_TEXT, (button, joint) -> {
//			this.joint = joint;
//		}));
		this.jointRotationButton.active = bl = JigsawBlock.getFacing(this.jigsawPlacerBlock.getCachedState()).getAxis().isVertical();
		this.jointRotationButton.visible = bl;

		this.triggeredBlockPositionOffsetXField = new TextFieldWidget(this.textRenderer, this.width / 2 - 154, 151, 50, 20, Text.translatable(""));
		this.triggeredBlockPositionOffsetXField.setMaxLength(128);
		this.triggeredBlockPositionOffsetXField.setText(Integer.toString(this.jigsawPlacerBlock.getTriggeredBlock().getLeft().getX()));
		this.addSelectableChild(this.triggeredBlockPositionOffsetXField);
		this.triggeredBlockPositionOffsetYField = new TextFieldWidget(this.textRenderer, this.width / 2 - 100, 151, 50, 20, Text.translatable(""));
		this.triggeredBlockPositionOffsetYField.setMaxLength(128);
		this.triggeredBlockPositionOffsetYField.setText(Integer.toString(this.jigsawPlacerBlock.getTriggeredBlock().getLeft().getY()));
		this.addSelectableChild(this.triggeredBlockPositionOffsetYField);
		this.triggeredBlockPositionOffsetZField = new TextFieldWidget(this.textRenderer, this.width / 2 - 46, 151, 50, 20, Text.translatable(""));
		this.triggeredBlockPositionOffsetZField.setMaxLength(128);
		this.triggeredBlockPositionOffsetZField.setText(Integer.toString(this.jigsawPlacerBlock.getTriggeredBlock().getLeft().getZ()));
		this.addSelectableChild(this.triggeredBlockPositionOffsetZField);
		this.triggeredBlockResets = this.jigsawPlacerBlock.getTriggeredBlock().getRight();
		this.toggleTriggeredBlockResetsButton = this.addDrawableChild(CyclingButtonWidget.onOffBuilder(Text.translatable("gui.triggered_block.toggle_triggered_block_resets_button_label.on"), Text.translatable("gui.triggered_block.toggle_triggered_block_resets_button_label.off")).initially(this.triggeredBlockResets).omitKeyText().build(this.width / 2 + 8, 151, 150, 20, Text.empty(), (button, triggeredBlockResets) -> {
			this.triggeredBlockResets = triggeredBlockResets;
		}));

		this.dataProvidingBlockPosOffsetXField = new TextFieldWidget(this.textRenderer, this.width / 2 - 154, 186, 50, 20, Text.empty());
		this.dataProvidingBlockPosOffsetXField.setMaxLength(128);
		this.dataProvidingBlockPosOffsetXField.setText(Integer.toString(this.jigsawPlacerBlock.getDataProvidingBlockPosOffset().getX()));
		this.addSelectableChild(this.dataProvidingBlockPosOffsetXField);

		this.dataProvidingBlockPosOffsetYField = new TextFieldWidget(this.textRenderer, this.width / 2 - 104, 186, 50, 20, Text.empty());
		this.dataProvidingBlockPosOffsetYField.setMaxLength(128);
		this.dataProvidingBlockPosOffsetYField.setText(Integer.toString(this.jigsawPlacerBlock.getDataProvidingBlockPosOffset().getY()));
		this.addSelectableChild(this.dataProvidingBlockPosOffsetYField);

		this.dataProvidingBlockPosOffsetZField = new TextFieldWidget(this.textRenderer, this.width / 2 - 54, 186, 50, 20, Text.empty());
		this.dataProvidingBlockPosOffsetZField.setMaxLength(128);
		this.dataProvidingBlockPosOffsetZField.setText(Integer.toString(this.jigsawPlacerBlock.getDataProvidingBlockPosOffset().getZ()));
		this.addSelectableChild(this.dataProvidingBlockPosOffsetZField);

		this.checkedDataIdField = new TextFieldWidget(this.textRenderer, this.width / 2, 186, 150, 20, Text.empty());
		this.checkedDataIdField.setMaxLength(128);
		this.checkedDataIdField.setText(this.jigsawPlacerBlock.getCheckedDataId());
		this.addSelectableChild(this.checkedDataIdField);

		this.doneButton = this.addDrawableChild(ButtonWidget.builder(ScreenTexts.DONE, button -> this.done()).dimensions(this.width / 2 - 4 - 150, 210, 150, 20).build());
		this.addDrawableChild(ButtonWidget.builder(ScreenTexts.CANCEL, button -> this.cancel()).dimensions(this.width / 2 + 4, 210, 150, 20).build());
		this.updateDoneButtonState();
		this.updateWidgets();
	}

	@Override
	protected void setInitialFocus() {
		this.setInitialFocus(this.targetField);
	}

	@Override
	public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
		this.renderInGameBackground(context);
	}

	private void updateWidgets() {
		this.removeStructurePoolButton0.visible = false;
		this.removeStructurePoolButton1.visible = false;

		int index = 0;
		for (int i = 0; i < Math.min(VISIBLE_STRUCTURE_POOL_LIST_ENTRY, this.structurePoolList.size()); i++) {
			if (index == 0) {
				this.removeStructurePoolButton0.visible = true;
			} else if (index == 1) {
				this.removeStructurePoolButton1.visible = true;
			}
			index++;
		}
	}

	private void updateDoneButtonState() {
		Identifier id1 = Identifier.tryParse(this.targetField.getText());
		Identifier id2 = null;
		if (!this.structurePoolList.isEmpty()) {
			id2 = Identifier.tryParse(this.structurePoolList.getFirst());
		}
		this.doneButton.active = id1 != null && id2 != null;
	}

	@Override
	public void resize(MinecraftClient client, int width, int height) {
		String string = this.targetField.getText();
		String string1 = this.triggeredBlockPositionOffsetXField.getText();
		String string2 = this.triggeredBlockPositionOffsetYField.getText();
		String string3 = this.triggeredBlockPositionOffsetZField.getText();
		String string4 = this.dataProvidingBlockPosOffsetXField.getText();
		String string5 = this.dataProvidingBlockPosOffsetYField.getText();
		String string6 = this.dataProvidingBlockPosOffsetZField.getText();
		String string7 = this.checkedDataIdField.getText();
		JigsawBlockEntity.Joint joint = this.joint;
		boolean bl = this.triggeredBlockResets;
		List<String> list = new ArrayList<>(this.structurePoolList);
		this.init(client, width, height);
		this.targetField.setText(string);
		this.triggeredBlockPositionOffsetXField.setText(string1);
		this.triggeredBlockPositionOffsetYField.setText(string2);
		this.triggeredBlockPositionOffsetZField.setText(string3);
		this.dataProvidingBlockPosOffsetXField.setText(string4);
		this.dataProvidingBlockPosOffsetYField.setText(string5);
		this.dataProvidingBlockPosOffsetZField.setText(string6);
		this.checkedDataIdField.setText(string7);
		this.joint = joint;
		this.jointRotationButton.setValue(joint);
		this.triggeredBlockResets = bl;
		this.structurePoolList.clear();
		this.structurePoolList.addAll(list);
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		this.structurePoolListMouseClicked = false;
		int x = this.width / 2 - 153;
		int y = 24;
		if (this.structurePoolList.size() > VISIBLE_STRUCTURE_POOL_LIST_ENTRY) {
			if (mouseX >= (double) x && mouseX < (double) (x + 6) && mouseY >= (double) y && mouseY < (double) (y + 42)) {
				this.structurePoolListMouseClicked = true;
			}
		}
		return super.mouseClicked(mouseX, mouseY, button);
	}

	@Override
	public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
		if (this.structurePoolList.size() > VISIBLE_STRUCTURE_POOL_LIST_ENTRY
				&& this.structurePoolListMouseClicked) {
			int i = this.structurePoolList.size() - VISIBLE_STRUCTURE_POOL_LIST_ENTRY;
			float f = (float) deltaY / (float) i;
			this.structurePoolListScrollAmount = MathHelper.clamp(this.structurePoolListScrollAmount + f, 0.0f, 1.0f);
			this.structurePoolListScrollPosition = (int) ((double) (this.structurePoolListScrollAmount * (float) i));
		}
		return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
		int x = this.width / 2 - 153;
		int y = 24;
		if (this.structurePoolList.size() > VISIBLE_STRUCTURE_POOL_LIST_ENTRY
				&& mouseX >= x && mouseX <= x + 307 && mouseY >= y && mouseY <= y + 42) {
			int i = this.structurePoolList.size() - VISIBLE_STRUCTURE_POOL_LIST_ENTRY;
			float f = (float) verticalAmount / (float) i;
			this.structurePoolListScrollAmount = MathHelper.clamp(this.structurePoolListScrollAmount - f, 0.0f, 1.0f);
			this.structurePoolListScrollPosition = (int) ((double) (this.structurePoolListScrollAmount * (float) i));
		}
		return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (super.keyPressed(keyCode, scanCode, modifiers)) {
			return true;
		}
		if (this.doneButton.active && (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER)) {
			this.done();
			return true;
		}
		return false;
	}

	private boolean updateJigsawPlacerBlock() {
		ClientPlayNetworking.send(new UpdateJigsawPlacerBlockPacket(
				this.jigsawPlacerBlock.getPos(),
				this.targetField.getText(),
				this.structurePoolList,
				this.joint,
				new BlockPos(
						ItemUtils.parseInt(this.triggeredBlockPositionOffsetXField.getText()),
						ItemUtils.parseInt(this.triggeredBlockPositionOffsetYField.getText()),
						ItemUtils.parseInt(this.triggeredBlockPositionOffsetZField.getText())
				),
				this.triggeredBlockResets,
				new BlockPos(
						ItemUtils.parseInt(this.dataProvidingBlockPosOffsetXField.getText()),
						ItemUtils.parseInt(this.dataProvidingBlockPosOffsetYField.getText()),
						ItemUtils.parseInt(this.dataProvidingBlockPosOffsetZField.getText())
				),
				this.checkedDataIdField.getText()
		));
		return true;
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {

		super.render(context, mouseX, mouseY, delta);
		for (int i = this.structurePoolListScrollPosition; i < Math.min(this.structurePoolListScrollPosition + VISIBLE_STRUCTURE_POOL_LIST_ENTRY, this.structurePoolList.size()); i++) {
			String text = i + ": " + this.structurePoolList.get(i);
			context.drawTextWithShadow(this.textRenderer, text, this.width / 2 - 117, 28 + ((i - this.structurePoolListScrollPosition) * 24), 0xA0A0A0);
		}
		if (this.structurePoolList.size() > VISIBLE_STRUCTURE_POOL_LIST_ENTRY) {
			context.drawGuiTexture(SCROLL_BAR_BACKGROUND_8_70_TEXTURE, this.width / 2 - 153, 24, 8, 42);
			int k = (int) (33.0f * this.structurePoolListScrollAmount);
			context.drawGuiTexture(SCROLLER_TEXTURE, this.width / 2 - 152, 24 + 1 + k, 6, 7);
		}

		context.drawTextWithShadow(this.textRenderer, NEW_STRUCTURE_POOL_FIELD_TEXT, this.width / 2 - 153, 71, 0xA0A0A0);
		this.newStructurePoolField.render(context, mouseX, mouseY, delta);

		context.drawTextWithShadow(this.textRenderer, TARGET_TEXT, this.width / 2 - 153, 106, 0xA0A0A0);
		this.targetField.render(context, mouseX, mouseY, delta);

		context.drawTextWithShadow(this.textRenderer, TRIGGERED_BLOCK_POSITION_TEXT, this.width / 2 - 153, 141, 0xA0A0A0);
		this.triggeredBlockPositionOffsetXField.render(context, mouseX, mouseY, delta);
		this.triggeredBlockPositionOffsetYField.render(context, mouseX, mouseY, delta);
		this.triggeredBlockPositionOffsetZField.render(context, mouseX, mouseY, delta);

		context.drawTextWithShadow(this.textRenderer, DATA_PROVIDING_BLOCK_POSITION_TEXT, this.width / 2 - 153, 176, 0xA0A0A0);
		this.dataProvidingBlockPosOffsetXField.render(context, mouseX, mouseY, delta);
		this.dataProvidingBlockPosOffsetYField.render(context, mouseX, mouseY, delta);
		this.dataProvidingBlockPosOffsetZField.render(context, mouseX, mouseY, delta);
		this.checkedDataIdField.render(context, mouseX, mouseY, delta);
	}

	@Override
	public boolean shouldPause() {
		return false;
	}
}
