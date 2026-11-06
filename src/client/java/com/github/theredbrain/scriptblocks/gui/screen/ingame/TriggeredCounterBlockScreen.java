package com.github.theredbrain.scriptblocks.gui.screen.ingame;

import com.github.theredbrain.scriptblocks.ScriptBlocks;
import com.github.theredbrain.scriptblocks.block.entity.TriggeredCounterBlockEntity;
import com.github.theredbrain.scriptblocks.network.packet.UpdateTriggeredCounterBlockPacket;
import com.github.theredbrain.scriptblocks.util.ItemUtils;
import com.mojang.blaze3d.platform.cursor.CursorTypes;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.GameNarrator;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import org.apache.commons.lang3.tuple.MutablePair;

import java.util.ArrayList;
import java.util.List;

@Environment(value = EnvType.CLIENT)
public class TriggeredCounterBlockScreen extends AbstractCreativeBlockSettingsScreen {
	private static final Component NEW_TRIGGERED_BLOCK_COUNTER_FIELD_PLACEHOLDER_TEXT = Component.translatable("gui.triggered_counter_block.counter_threshold.placeholder");
	private static final Component NEW_TRIGGERED_BLOCK_POSITION_TEXT = Component.translatable("gui.triggered_block.new_triggered_block_position_offset");
	private static final Component NEW_POSITION_X_FIELD_PLACEHOLDER_TEXT = Component.translatable("gui.triggered_block.triggered_block_position_offset_x.placeholder");
	private static final Component NEW_POSITION_Y_FIELD_PLACEHOLDER_TEXT = Component.translatable("gui.triggered_block.triggered_block_position_offset_y.placeholder");
	private static final Component NEW_POSITION_Z_FIELD_PLACEHOLDER_TEXT = Component.translatable("gui.triggered_block.triggered_block_position_offset_z.placeholder");
	private static final Component REMOVE_BUTTON_LABEL_TEXT = Component.translatable("gui.remove");
	private static final Component ADD_BUTTON_LABEL_TEXT = Component.translatable("gui.add");
	private static final Identifier TRIGGERED_BLOCKS_LIST_SCROLLER_BACKGROUND_TEXTURE = ScriptBlocks.identifier("scroll_bar/scroll_bar_background_8_116");
	private static final Identifier SCROLLER_TEXTURE = ScriptBlocks.identifier("scroll_bar/scroller_vertical_6_7");
	private final TriggeredCounterBlockEntity triggeredCounterBlock;
	private Button removeListEntryButton0;
	private Button removeListEntryButton1;
	private Button removeListEntryButton2;
	private Button removeListEntryButton3;
	private Button removeListEntryButton4;
	private EditBox newTriggeredBlockCounterField;
	private EditBox newTriggeredBlockPositionOffsetXField;
	private EditBox newTriggeredBlockPositionOffsetYField;
	private EditBox newTriggeredBlockPositionOffsetZField;
	private CycleButton<Boolean> toggleTriggeredBlockResetsButton;
	private boolean triggeredBlockResets;
	private Button addNewTriggeredBlockPositionOffsetButton;
	private final List<MutablePair<Integer, MutablePair<BlockPos, Boolean>>> triggeredBlocksList = new ArrayList<>(List.of());
	private int scrollPosition = 0;
	private float scrollAmount = 0.0f;
	private boolean mouseClicked = false;

	public TriggeredCounterBlockScreen(TriggeredCounterBlockEntity triggeredCounterBlock) {
		super(GameNarrator.NO_TITLE);
		this.triggeredCounterBlock = triggeredCounterBlock;
	}

	private void addTriggeredBlockEntry() {
		int newTriggeredBlockCounter = ItemUtils.parseInt(this.newTriggeredBlockCounterField.getValue());
		for (MutablePair<Integer, MutablePair<BlockPos, Boolean>> entry : this.triggeredBlocksList) {
			if (entry.getLeft().equals(newTriggeredBlockCounter)) {
				if (this.minecraft.player != null) {
					this.minecraft.player.sendSystemMessage(Component.translatable("gui.dialogue_block.entry_already_in_list"));
				}
				return;
			}
		}
		this.triggeredBlocksList.add(
				new MutablePair<>(
						ItemUtils.parseInt(this.newTriggeredBlockCounterField.getValue()),
						new MutablePair<>(
								new BlockPos(
										ItemUtils.parseInt(this.newTriggeredBlockPositionOffsetXField.getValue()),
										ItemUtils.parseInt(this.newTriggeredBlockPositionOffsetYField.getValue()),
										ItemUtils.parseInt(this.newTriggeredBlockPositionOffsetZField.getValue())
								),
								this.triggeredBlockResets
						)
				)
		);
		this.scrollPosition = 0;
		this.scrollAmount = 0.0f;
		this.updateWidgets();
	}

	private void deleteTriggeredBlockEntry(int index) {
		if (index + this.scrollPosition < this.triggeredBlocksList.size()) {
			this.triggeredBlocksList.remove(index + this.scrollPosition);
		}
		this.scrollPosition = 0;
		this.scrollAmount = 0.0f;
		this.updateWidgets();
	}

	@Override
	protected void init() {
		this.triggeredBlocksList.clear();
		List<Integer> keyList = new ArrayList<>(this.triggeredCounterBlock.getTriggeredBlocks().keySet());
		for (int key : keyList) {
			this.triggeredBlocksList.add(new MutablePair<>(key, this.triggeredCounterBlock.getTriggeredBlocks().get(key)));
		}
		this.removeListEntryButton0 = this.addRenderableWidget(Button.builder(REMOVE_BUTTON_LABEL_TEXT, button -> this.deleteTriggeredBlockEntry(0)).bounds(this.width / 2 + 54, 20, 100, 20).build());
		this.removeListEntryButton1 = this.addRenderableWidget(Button.builder(REMOVE_BUTTON_LABEL_TEXT, button -> this.deleteTriggeredBlockEntry(1)).bounds(this.width / 2 + 54, 44, 100, 20).build());
		this.removeListEntryButton2 = this.addRenderableWidget(Button.builder(REMOVE_BUTTON_LABEL_TEXT, button -> this.deleteTriggeredBlockEntry(2)).bounds(this.width / 2 + 54, 68, 100, 20).build());
		this.removeListEntryButton3 = this.addRenderableWidget(Button.builder(REMOVE_BUTTON_LABEL_TEXT, button -> this.deleteTriggeredBlockEntry(3)).bounds(this.width / 2 + 54, 92, 100, 20).build());
		this.removeListEntryButton4 = this.addRenderableWidget(Button.builder(REMOVE_BUTTON_LABEL_TEXT, button -> this.deleteTriggeredBlockEntry(4)).bounds(this.width / 2 + 54, 116, 100, 20).build());
		this.newTriggeredBlockCounterField = new EditBox(this.font, this.width / 2 - 154, 153, 50, 20, Component.translatable(""));
		this.newTriggeredBlockCounterField.setHint(NEW_TRIGGERED_BLOCK_COUNTER_FIELD_PLACEHOLDER_TEXT);
		this.addWidget(this.newTriggeredBlockCounterField);
		this.newTriggeredBlockPositionOffsetXField = new EditBox(this.font, this.width / 2 - 100, 153, 50, 20, Component.translatable(""));
		this.newTriggeredBlockPositionOffsetXField.setHint(NEW_POSITION_X_FIELD_PLACEHOLDER_TEXT);
		this.addWidget(this.newTriggeredBlockPositionOffsetXField);
		this.newTriggeredBlockPositionOffsetYField = new EditBox(this.font, this.width / 2 - 46, 153, 50, 20, Component.translatable(""));
		this.newTriggeredBlockPositionOffsetYField.setHint(NEW_POSITION_Y_FIELD_PLACEHOLDER_TEXT);
		this.addWidget(this.newTriggeredBlockPositionOffsetYField);
		this.newTriggeredBlockPositionOffsetZField = new EditBox(this.font, this.width / 2 + 8, 153, 50, 20, Component.translatable(""));
		this.newTriggeredBlockPositionOffsetZField.setHint(NEW_POSITION_Z_FIELD_PLACEHOLDER_TEXT);
		this.addWidget(this.newTriggeredBlockPositionOffsetZField);

		this.triggeredBlockResets = false;
		this.toggleTriggeredBlockResetsButton = this.addRenderableWidget(CycleButton.booleanBuilder(Component.translatable("gui.triggered_block.toggle_triggered_block_resets_button_label.on"), Component.translatable("gui.triggered_block.toggle_triggered_block_resets_button_label.off"), this.triggeredBlockResets)
				.displayOnlyValue()
				.create(this.width / 2 + 62, 153, 100, 20, Component.empty(), (button, triggeredBlockResets) -> {
			this.triggeredBlockResets = triggeredBlockResets;
		}));

		this.addNewTriggeredBlockPositionOffsetButton = this.addRenderableWidget(Button.builder(ADD_BUTTON_LABEL_TEXT, button -> this.addTriggeredBlockEntry()).bounds(this.width / 2 - 4 - 150, 177, 300, 20).build());
		this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> this.done()).bounds(this.width / 2 - 4 - 150, 201, 150, 20).build());
		this.addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL, button -> this.cancel()).bounds(this.width / 2 + 4, 201, 150, 20).build());
		this.setInitialFocus(this.newTriggeredBlockCounterField);
		this.updateWidgets();
	}

	@Override
	protected void setInitialFocus() {
		this.setInitialFocus(this.newTriggeredBlockCounterField);
	}

	@Override
	protected void updateWidgets() {
		this.removeListEntryButton0.visible = false;
		this.removeListEntryButton1.visible = false;
		this.removeListEntryButton2.visible = false;
		this.removeListEntryButton3.visible = false;
		this.removeListEntryButton4.visible = false;

		this.newTriggeredBlockCounterField.setVisible(false);
		this.newTriggeredBlockPositionOffsetXField.setVisible(false);
		this.newTriggeredBlockPositionOffsetYField.setVisible(false);
		this.newTriggeredBlockPositionOffsetZField.setVisible(false);
		this.toggleTriggeredBlockResetsButton.visible = false;

		this.addNewTriggeredBlockPositionOffsetButton.visible = false;

		int index = 0;
		for (int i = 0; i < Math.min(5, this.triggeredBlocksList.size()); i++) {
			if (index == 0) {
				this.removeListEntryButton0.visible = true;
			} else if (index == 1) {
				this.removeListEntryButton1.visible = true;
			} else if (index == 2) {
				this.removeListEntryButton2.visible = true;
			} else if (index == 3) {
				this.removeListEntryButton3.visible = true;
			} else if (index == 4) {
				this.removeListEntryButton4.visible = true;
			}
			index++;
		}

		this.newTriggeredBlockCounterField.setVisible(true);
		this.newTriggeredBlockPositionOffsetXField.setVisible(true);
		this.newTriggeredBlockPositionOffsetYField.setVisible(true);
		this.newTriggeredBlockPositionOffsetZField.setVisible(true);
		this.toggleTriggeredBlockResetsButton.visible = true;

		this.addNewTriggeredBlockPositionOffsetButton.visible = true;
	}

	@Override
	public void resize(final int width, final int height) {
		List<MutablePair<Integer, MutablePair<BlockPos, Boolean>>> list = new ArrayList<>(this.triggeredBlocksList);
		String string = this.newTriggeredBlockCounterField.getValue();
		String string1 = this.newTriggeredBlockPositionOffsetXField.getValue();
		String string2 = this.newTriggeredBlockPositionOffsetYField.getValue();
		String string3 = this.newTriggeredBlockPositionOffsetZField.getValue();
		this.init(width, height);
		this.triggeredBlocksList.clear();
		this.triggeredBlocksList.addAll(list);
		this.newTriggeredBlockCounterField.setValue(string);
		this.newTriggeredBlockPositionOffsetXField.setValue(string1);
		this.newTriggeredBlockPositionOffsetYField.setValue(string2);
		this.newTriggeredBlockPositionOffsetZField.setValue(string3);
		this.updateWidgets();
	}

	@Override
	public boolean mouseClicked(final MouseButtonEvent event, final boolean doubleClick) {
		this.mouseClicked = false;
		if (this.triggeredBlocksList.size() > 5) {
			int i = this.width / 2 - 152;
			int j = 21;
			if (event.x() >= (double) i && event.x() < (double) (i + 6) && event.y() >= (double) j && event.y() < (double) (j + 114)) {
				this.mouseClicked = true;
			}
		}
		return super.mouseClicked(event, doubleClick);
	}

	@Override
	public boolean mouseDragged(final MouseButtonEvent event, final double dx, final double dy) {
		if (this.triggeredBlocksList.size() > 5 && this.mouseClicked) {
			int i = this.triggeredBlocksList.size() - 5;
			float f = (float) dy / (float) i;
			this.scrollAmount = Mth.clamp(this.scrollAmount + f, 0.0f, 1.0f);
			this.scrollPosition = (int) ((double) (this.scrollAmount * (float) i));
		}
		return super.mouseDragged(event, dx, dy);
	}

	@Override
	public boolean mouseScrolled(final double x, final double y, final double scrollX, final double scrollY) {
		if (this.triggeredBlocksList.size() > 5 && x >= (double) (this.width / 2 - 152) && x <= (double) (this.width / 2 + 50) && y >= 20 && y <= 135) {
			int i = this.triggeredBlocksList.size() - 5;
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

		for (int i = this.scrollPosition; i < Math.min(this.scrollPosition + 5, this.triggeredBlocksList.size()); i++) {
			graphics.text(this.font, this.triggeredBlocksList.get(i).toString(), this.width / 2 - 141, 26 + ((i - this.scrollPosition) * 24), -6250336);
		}
		if (this.triggeredBlocksList.size() > 5) {
			graphics.blitSprite(RenderPipelines.GUI_TEXTURED, TRIGGERED_BLOCKS_LIST_SCROLLER_BACKGROUND_TEXTURE, this.width / 2 - 153, 20, 8, 116);
			int k = (int) (107.0f * this.scrollAmount);
			graphics.blitSprite(RenderPipelines.GUI_TEXTURED, SCROLLER_TEXTURE, this.width / 2 - 152, 20 + 1 + k, 6, 7);
		}
		graphics.text(this.font, NEW_TRIGGERED_BLOCK_POSITION_TEXT, this.width / 2 - 99, 140, -6250336);
		this.newTriggeredBlockCounterField.extractRenderState(graphics, mouseX, mouseY, a);
		this.newTriggeredBlockPositionOffsetXField.extractRenderState(graphics, mouseX, mouseY, a);
		this.newTriggeredBlockPositionOffsetYField.extractRenderState(graphics, mouseX, mouseY, a);
		this.newTriggeredBlockPositionOffsetZField.extractRenderState(graphics, mouseX, mouseY, a);
	}

	// TODO
//	private void extractScroller(final GuiGraphicsExtractor graphics, final int xo, final int yo, final int mouseX, final int mouseY, int steps) {
////		int steps = offers.size() + 1 - 7;
//		if (steps > 1) {
//			int leftOver = 139 - (27 + (steps - 1) * 139 / steps);
//			int stepHeight = 1 + leftOver / steps + 139 / steps;
//			int maxScrollerOff = 113;
//			int scrollerYOff = Math.min(113, this.scrollOff * stepHeight);
//			if (this.scrollOff == steps - 1) {
//				scrollerYOff = 113;
//			}
//
//			int scrollerX = xo + 94;
//			int scrollerY = yo + 18 + scrollerYOff;
//			graphics.blitSprite(RenderPipelines.GUI_TEXTURED, SCROLLER_SPRITE, scrollerX, scrollerY, 6, 27);
//			if (mouseX >= scrollerX && mouseX < xo + 94 + 6 && mouseY >= scrollerY && mouseY <= scrollerY + 27) {
//				graphics.requestCursor(this.isDragging ? CursorTypes.RESIZE_NS : CursorTypes.POINTING_HAND);
//			}
//		} else {
//			graphics.blitSprite(RenderPipelines.GUI_TEXTURED, SCROLLER_DISABLED_SPRITE, xo + 94, yo + 18, 6, 27);
//		}
//	}

	@Override
	protected boolean updateBlock() {
		ClientPlayNetworking.send(new UpdateTriggeredCounterBlockPacket(
				this.triggeredCounterBlock.getBlockPos(),
				this.triggeredBlocksList)
		);
		return true;
	}
}
