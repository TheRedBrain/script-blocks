package com.github.theredbrain.scriptblocks.gui.screen.ingame;

import com.github.theredbrain.scriptblocks.ScriptBlocks;
import com.github.theredbrain.scriptblocks.block.entity.JigsawPlacerBlockEntity;
import com.github.theredbrain.scriptblocks.network.packet.UpdateJigsawPlacerBlockPacket;
import com.github.theredbrain.scriptblocks.util.ItemUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.GameNarrator;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.JigsawBlock;
import net.minecraft.world.level.block.entity.JigsawBlockEntity;
import org.apache.commons.lang3.tuple.MutablePair;

import java.util.ArrayList;
import java.util.List;

@Environment(value = EnvType.CLIENT)
public class JigsawPlacerBlockScreen extends AbstractCreativeBlockSettingsScreen {
	private static final Component STRUCTURE_POOL_STRING_FIELD_TEXT = Component.translatable("gui.jigsaw_placer_block.structure_pool_string_field");
	private static final Component NEW_DATA_BLOCK_LABEL_TEXT = Component.translatable("gui.jigsaw_placer_block.new_data_block_label");
	private static final Component NEW_STATIC_APPENDIX_LABEL_TEXT = Component.translatable("gui.jigsaw_placer_block.new_static_appendix_label");
	private static final Component TARGET_LABEL_TEXT = Component.translatable("jigsaw_block.target");
	private static final Component JOINT_LABEL_TEXT = Component.translatable("jigsaw_block.joint_label");
	private static final Component TRIGGERED_BLOCK_POSITION_TEXT = Component.translatable("gui.jigsaw_placer_block.triggered_block_label");
	private static final Identifier SCROLL_BAR_BACKGROUND_8_54_TEXTURE = ScriptBlocks.identifier("scroll_bar/scroll_bar_background_8_54");
	private static final Identifier SCROLLER_TEXTURE = ScriptBlocks.identifier("scroll_bar/scroller_vertical_6_7");
	public static final WidgetSprites REMOVE_ENTRY_BUTTON_TEXTURES = new WidgetSprites(
			Identifier.fromNamespaceAndPath(ScriptBlocks.MOD_ID, "widgets/remove_entry_button"), Identifier.fromNamespaceAndPath(ScriptBlocks.MOD_ID, "widgets/remove_entry_button_highlighted")
	);
	private static final int VISIBLE_LIST_ELEMENTS = 2;
	private final JigsawPlacerBlockEntity jigsawPlacerBlock;
	private EditBox structurePoolStringField;

	private final List<MutablePair<BlockPos, MutablePair<String, String>>> structurePoolStringAppendices = new ArrayList<>(List.of());
	private Button removeStructurePoolAppendixButton0;
	private Button removeStructurePoolAppendixButton1;

	private EditBox newAppendixBlockPosOffsetXField;
	private EditBox newAppendixBlockPosOffsetYField;
	private EditBox newAppendixBlockPosOffsetZField;
	private EditBox newAppendixDataIdField;
	private EditBox newStaticAppendixStringField;

	private EditBox targetField;
	private CycleButton<JigsawBlockEntity.JointType> jointRotationButton;

	private EditBox triggeredBlockPositionOffsetXField;
	private EditBox triggeredBlockPositionOffsetYField;
	private EditBox triggeredBlockPositionOffsetZField;
	private CycleButton<Boolean> toggleTriggeredBlockResetsButton;

	private JigsawBlockEntity.JointType joint;
	private boolean triggeredBlockResets;
	private int scrollPosition = 0;
	private float scrollAmount = 0.0f;
	private boolean mouseClicked = false;

	public JigsawPlacerBlockScreen(JigsawPlacerBlockEntity jigsawPlacerBlock) {
		super(GameNarrator.NO_TITLE);
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
						ItemUtils.parseInt(this.newAppendixBlockPosOffsetXField.getValue()),
						ItemUtils.parseInt(this.newAppendixBlockPosOffsetYField.getValue()),
						ItemUtils.parseInt(this.newAppendixBlockPosOffsetZField.getValue())
				),
				new MutablePair<>(
						this.newAppendixDataIdField.getValue(),
						this.newStaticAppendixStringField.getValue()
				)
		);
		if (newStructurePoolStringAppendix.getLeft().equals(BlockPos.ZERO)) {
			if (this.minecraft.player != null) {
				this.minecraft.player.sendSystemMessage(Component.translatable("gui.jigsaw_placer_block.appendix_offset_is_origin"));
			}
			return;
		}
		for (MutablePair<BlockPos, MutablePair<String, String>> structurePoolStringAppendix : this.structurePoolStringAppendices) {
			if (structurePoolStringAppendix.equals(newStructurePoolStringAppendix)) {
				if (this.minecraft.player != null) {
					this.minecraft.player.sendSystemMessage(Component.translatable("gui.jigsaw_placer_block.structure_pool_string_appendix_is_duplicate"));
				}
				return;
			}
		}
		this.structurePoolStringAppendices.add(newStructurePoolStringAppendix);
		this.scrollPosition = 0;
		this.scrollAmount = 0.0f;
		this.updateWidgets();
	}

	@Override
	protected void init() {

		this.structurePoolStringAppendices.clear();
		this.structurePoolStringAppendices.addAll(this.jigsawPlacerBlock.getStructurePoolStringAppendices());

		int structurePoolLabelTextWidth = this.font.width(STRUCTURE_POOL_STRING_FIELD_TEXT);
		this.structurePoolStringField = new EditBox(this.font, this.width / 2 - 203 + 4 + structurePoolLabelTextWidth, 11, 408 - 5 - structurePoolLabelTextWidth, 20, Component.empty());
		this.structurePoolStringField.setMaxLength(128);
		this.structurePoolStringField.setValue(this.jigsawPlacerBlock.getStructurePoolString());
		this.addWidget(this.structurePoolStringField);

		this.removeStructurePoolAppendixButton0 = this.addRenderableWidget(new ImageButton(this.width / 2 - 191, 40, 20, 20, REMOVE_ENTRY_BUTTON_TEXTURES, button -> this.removeStructurePoolAppendix(0)));
		this.removeStructurePoolAppendixButton1 = this.addRenderableWidget(new ImageButton(this.width / 2 - 191, 74, 20, 20, REMOVE_ENTRY_BUTTON_TEXTURES, button -> this.removeStructurePoolAppendix(1)));

		this.addRenderableWidget(Button.builder(Component.translatable("gui.jigsaw_placer_block.add_new_structure_pool_string_appendix_button_label"), button -> this.addNewStructurePoolStringAppendix()).bounds(this.width / 2 - 204, 103, 408, 20).build());

		this.newAppendixBlockPosOffsetXField = new EditBox(this.font, this.width / 2 - 204, 138, 30, 20, Component.empty());
		this.newAppendixBlockPosOffsetXField.setMaxLength(128);
		this.newAppendixBlockPosOffsetXField.setHint(Component.literal("X"));
		this.addWidget(this.newAppendixBlockPosOffsetXField);

		this.newAppendixBlockPosOffsetYField = new EditBox(this.font, this.width / 2 - 174, 138, 30, 20, Component.empty());
		this.newAppendixBlockPosOffsetYField.setMaxLength(128);
		this.newAppendixBlockPosOffsetYField.setHint(Component.literal("Y"));
		this.addWidget(this.newAppendixBlockPosOffsetYField);

		this.newAppendixBlockPosOffsetZField = new EditBox(this.font, this.width / 2 - 144, 138, 30, 20, Component.empty());
		this.newAppendixBlockPosOffsetZField.setMaxLength(128);
		this.newAppendixBlockPosOffsetZField.setHint(Component.literal("Z"));
		this.addWidget(this.newAppendixBlockPosOffsetZField);

		this.newAppendixDataIdField = new EditBox(this.font, this.width / 2 - 110, 138, 106, 20, Component.empty());
		this.newAppendixDataIdField.setMaxLength(128);
		this.addWidget(this.newAppendixDataIdField);

		this.newStaticAppendixStringField = new EditBox(this.font, this.width / 2 + 4, 138, 200, 20, Component.empty());
		this.newStaticAppendixStringField.setMaxLength(128);
		this.addWidget(this.newStaticAppendixStringField);

		int targetLabelTextWidth = this.font.width(TARGET_LABEL_TEXT);
		this.targetField = new EditBox(this.font, this.width / 2 - 203 + 4 + targetLabelTextWidth, 162, 200 - 5 - targetLabelTextWidth, 20, Component.translatable("jigsaw_block.target"));
		this.targetField.setMaxLength(128);
		this.targetField.setValue(this.jigsawPlacerBlock.getTarget().toString());
		this.addWidget(this.targetField);

		int jointTextWidth = this.font.width(JOINT_LABEL_TEXT);
		this.joint = this.jigsawPlacerBlock.getJoint();
		this.jointRotationButton = this.addRenderableWidget(
				CycleButton.builder(JigsawBlockEntity.JointType::getTranslatedName, this.joint)
						.create(this.width / 2 + 5 + jointTextWidth + 4, 162, 200 - 5 - jointTextWidth, 20, JOINT_LABEL_TEXT, (button, joint) -> this.joint = joint)
		);
		boolean bl;
		this.jointRotationButton.active = bl = JigsawBlock.getFrontFacing(this.jigsawPlacerBlock.getBlockState()).getAxis().isVertical();
		this.jointRotationButton.visible = bl;

		this.triggeredBlockPositionOffsetXField = new EditBox(this.font, this.width / 2 - 94, 186, 30, 20, Component.empty());
		this.triggeredBlockPositionOffsetXField.setMaxLength(128);
		this.triggeredBlockPositionOffsetXField.setValue(Integer.toString(this.jigsawPlacerBlock.getTriggeredBlock().getLeft().getX()));
		this.triggeredBlockPositionOffsetXField.setHint(Component.literal("X"));
		this.addWidget(this.triggeredBlockPositionOffsetXField);
		this.triggeredBlockPositionOffsetYField = new EditBox(this.font, this.width / 2 - 64, 186, 30, 20, Component.empty());
		this.triggeredBlockPositionOffsetYField.setMaxLength(128);
		this.triggeredBlockPositionOffsetYField.setValue(Integer.toString(this.jigsawPlacerBlock.getTriggeredBlock().getLeft().getY()));
		this.triggeredBlockPositionOffsetYField.setHint(Component.literal("Y"));
		this.addWidget(this.triggeredBlockPositionOffsetYField);
		this.triggeredBlockPositionOffsetZField = new EditBox(this.font, this.width / 2 - 34, 186, 30, 20, Component.empty());
		this.triggeredBlockPositionOffsetZField.setMaxLength(128);
		this.triggeredBlockPositionOffsetZField.setValue(Integer.toString(this.jigsawPlacerBlock.getTriggeredBlock().getLeft().getZ()));
		this.triggeredBlockPositionOffsetZField.setHint(Component.literal("Z"));
		this.addWidget(this.triggeredBlockPositionOffsetZField);

		this.triggeredBlockResets = this.jigsawPlacerBlock.getTriggeredBlock().getRight();
		this.toggleTriggeredBlockResetsButton = this.addRenderableWidget(CycleButton.booleanBuilder(Component.translatable("gui.jigsaw_placer_block.toggle_triggered_block_resets_button_label.on"), Component.translatable("gui.jigsaw_placer_block.toggle_triggered_block_resets_button_label.off"), this.triggeredBlockResets).create(this.width / 2 + 4, 186, 200, 20, Component.empty(), (button, triggeredBlockResets) -> {
			this.triggeredBlockResets = triggeredBlockResets;
		}));

		this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> this.done()).bounds(this.width / 2 - 4 - 200, 210, 200, 20).build());
		this.addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL, button -> this.cancel()).bounds(this.width / 2 + 4, 210, 200, 20).build());

		this.updateWidgets();
	}

	@Override
	protected void setInitialFocus() {
		this.setInitialFocus(this.structurePoolStringField);
	}

	@Override
	protected void updateWidgets() {

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
	public void resize(final int width, final int height) {
		List<MutablePair<BlockPos, MutablePair<String, String>>> list = new ArrayList<>(this.structurePoolStringAppendices);
		String string = this.structurePoolStringField.getValue();
		String string5 = this.newStaticAppendixStringField.getValue();
		String string6 = this.newAppendixBlockPosOffsetXField.getValue();
		String string7 = this.newAppendixBlockPosOffsetYField.getValue();
		String string8 = this.newAppendixBlockPosOffsetZField.getValue();
		String string9 = this.newAppendixDataIdField.getValue();
		String string10 = this.targetField.getValue();
		String string11 = this.triggeredBlockPositionOffsetXField.getValue();
		String string12 = this.triggeredBlockPositionOffsetYField.getValue();
		String string13 = this.triggeredBlockPositionOffsetZField.getValue();
		JigsawBlockEntity.JointType joint = this.joint;
		boolean bl = this.triggeredBlockResets;
		this.init(width, height);
		this.structurePoolStringAppendices.clear();
		this.structurePoolStringAppendices.addAll(list);
		this.structurePoolStringField.setValue(string);
		this.newStaticAppendixStringField.setValue(string5);
		this.newAppendixBlockPosOffsetXField.setValue(string6);
		this.newAppendixBlockPosOffsetYField.setValue(string7);
		this.newAppendixBlockPosOffsetZField.setValue(string8);
		this.newAppendixDataIdField.setValue(string9);
		this.targetField.setValue(string10);
		this.triggeredBlockPositionOffsetXField.setValue(string11);
		this.triggeredBlockPositionOffsetYField.setValue(string12);
		this.triggeredBlockPositionOffsetZField.setValue(string13);
		this.joint = joint;
		this.jointRotationButton.setValue(this.joint);
		this.triggeredBlockResets = bl;
		this.toggleTriggeredBlockResetsButton.setValue(this.triggeredBlockResets);
	}

	@Override
	public boolean mouseClicked(final MouseButtonEvent event, final boolean doubleClick) {
		this.mouseClicked = false;
		if (this.structurePoolStringAppendices.size() > VISIBLE_LIST_ELEMENTS) {
			int i = this.width / 2 - 202;
			int j = 40;
			if (event.x() >= (double) i && event.x() < (double) (i + 6) && event.y() >= (double) j && event.y() < (double) (j + 54)) {
				this.mouseClicked = true;
			}
		}
		return super.mouseClicked(event, doubleClick);
	}

	@Override
	public boolean mouseDragged(final MouseButtonEvent event, final double dx, final double dy) {
		if (this.structurePoolStringAppendices.size() > VISIBLE_LIST_ELEMENTS
				&& this.mouseClicked) {
			int i = this.structurePoolStringAppendices.size() - VISIBLE_LIST_ELEMENTS;
			float f = (float) dy / (float) i;
			this.scrollAmount = Mth.clamp(this.scrollAmount + f, 0.0f, 1.0f);
			this.scrollPosition = (int) ((double) (this.scrollAmount * (float) i));
		}
		return super.mouseDragged(event, dx, dy);
	}

	@Override
	public boolean mouseScrolled(final double x, final double y, final double scrollX, final double scrollY) {
		if (this.structurePoolStringAppendices.size() > VISIBLE_LIST_ELEMENTS
				&& x >= (double) (this.width / 2 - 202) && x <= (double) (this.width / 2 + 204)
				&& y >= 40 && y <= 94) {
			int i = this.structurePoolStringAppendices.size() - VISIBLE_LIST_ELEMENTS;
			float f = (float) scrollY / (float) i;
			this.scrollAmount = Mth.clamp(this.scrollAmount - f, 0.0f, 1.0f);
			this.scrollPosition = (int) ((double) (this.scrollAmount * (float) i));
		}
		return super.mouseScrolled(x, y, scrollX, scrollY);
	}

	@Override
	public boolean keyPressed(final KeyEvent event) {
		if (event.isConfirmation()) {
			this.done();
			return true;
		}
		return super.keyPressed(event);
	}

	@Override
	public void extractRenderState(final GuiGraphicsExtractor graphics, final int mouseX, final int mouseY, final float a) {

		super.extractRenderState(graphics, mouseX, mouseY, a);

		graphics.text(this.font, STRUCTURE_POOL_STRING_FIELD_TEXT, this.width / 2 - 203, 17, 0xA0A0A0);
		this.structurePoolStringField.extractRenderState(graphics, mouseX, mouseY, a);

		for (int i = this.scrollPosition; i < Math.min(this.scrollPosition + VISIBLE_LIST_ELEMENTS, this.structurePoolStringAppendices.size()); i++) {
			MutablePair<BlockPos, MutablePair<String, String>> structurePoolStringAppendix = this.structurePoolStringAppendices.get(i);
			BlockPos dataBlockPositionOffset = structurePoolStringAppendix.getLeft();
			MutableComponent text1 = Component.translatable("gui.jigsaw_placer_block.structure_pool_string_appendices.entry.1", dataBlockPositionOffset.getX(), dataBlockPositionOffset.getY(), dataBlockPositionOffset.getZ(), structurePoolStringAppendix.getRight().getLeft());
			MutableComponent text2 = Component.translatable("gui.jigsaw_placer_block.structure_pool_string_appendices.entry.2", structurePoolStringAppendix.getRight().getRight());

			graphics.text(this.font, text1, this.width / 2 - 167, 41 + ((i - this.scrollPosition) * 34), 0xA0A0A0);
			graphics.text(this.font, text2, this.width / 2 - 167, 51 + ((i - this.scrollPosition) * 34), 0xA0A0A0);
		}
		if (this.structurePoolStringAppendices.size() > VISIBLE_LIST_ELEMENTS) {
			graphics.blitSprite(RenderPipelines.GUI_TEXTURED, SCROLL_BAR_BACKGROUND_8_54_TEXTURE, this.width / 2 - 203, 40, 8, 54);
			int k = (int) (45.0f * this.scrollAmount);
			graphics.blitSprite(RenderPipelines.GUI_TEXTURED, SCROLLER_TEXTURE, this.width / 2 - 202, 40 + 1 + k, 6, 7);
		}

		graphics.text(this.font, NEW_DATA_BLOCK_LABEL_TEXT, this.width / 2 - 203, 128, 0xA0A0A0);
		this.newAppendixBlockPosOffsetXField.extractRenderState(graphics, mouseX, mouseY, a);
		this.newAppendixBlockPosOffsetYField.extractRenderState(graphics, mouseX, mouseY, a);
		this.newAppendixBlockPosOffsetZField.extractRenderState(graphics, mouseX, mouseY, a);
		this.newAppendixDataIdField.extractRenderState(graphics, mouseX, mouseY, a);
		graphics.text(this.font, NEW_STATIC_APPENDIX_LABEL_TEXT, this.width / 2 + 5, 128, 0xA0A0A0);
		this.newStaticAppendixStringField.extractRenderState(graphics, mouseX, mouseY, a);

		graphics.text(this.font, TARGET_LABEL_TEXT, this.width / 2 - 203, 168, 0xA0A0A0);
		this.targetField.extractRenderState(graphics, mouseX, mouseY, a);

		graphics.text(this.font, JOINT_LABEL_TEXT, this.width / 2 + 5, 168, 0xA0A0A0);

		int textWidth = this.font.width(TRIGGERED_BLOCK_POSITION_TEXT);
		graphics.text(this.font, TRIGGERED_BLOCK_POSITION_TEXT, this.width / 2 - 94 - 4 - textWidth, 192, 0xA0A0A0);
		this.triggeredBlockPositionOffsetXField.extractRenderState(graphics, mouseX, mouseY, a);
		this.triggeredBlockPositionOffsetYField.extractRenderState(graphics, mouseX, mouseY, a);
		this.triggeredBlockPositionOffsetZField.extractRenderState(graphics, mouseX, mouseY, a);
	}

	@Override
	protected boolean updateBlock() {
		ClientPlayNetworking.send(new UpdateJigsawPlacerBlockPacket(
				this.jigsawPlacerBlock.getBlockPos(),
				this.structurePoolStringField.getValue(),
				this.structurePoolStringAppendices,
				this.targetField.getValue(),
				this.joint,
				new BlockPos(
						ItemUtils.parseInt(this.triggeredBlockPositionOffsetXField.getValue()),
						ItemUtils.parseInt(this.triggeredBlockPositionOffsetYField.getValue()),
						ItemUtils.parseInt(this.triggeredBlockPositionOffsetZField.getValue())
				),
				this.triggeredBlockResets
		));
		return true;
	}
}
