package com.github.theredbrain.scriptblocks.gui.screen.ingame;

import com.github.theredbrain.scriptblocks.ScriptBlocks;
import com.github.theredbrain.scriptblocks.block.entity.TriggeredCounterBlockEntity;
import com.github.theredbrain.scriptblocks.network.packet.UpdateTriggeredCounterBlockPacket;
import com.github.theredbrain.scriptblocks.util.ItemUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.GameNarrator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import org.apache.commons.lang3.tuple.MutablePair;
import org.lwjgl.glfw.GLFW;

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
	private Button saveButton;
	private Button cancelButton;
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
				if (this.minecraft != null && this.minecraft.player != null) {
					this.minecraft.player.displayClientMessage(Component.translatable("gui.dialogue_block.entry_already_in_list"));
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
		this.toggleTriggeredBlockResetsButton = this.addRenderableWidget(CycleButton.booleanBuilder(Component.translatable("gui.triggered_block.toggle_triggered_block_resets_button_label.on"), Component.translatable("gui.triggered_block.toggle_triggered_block_resets_button_label.off")).initially(this.triggeredBlockResets).omitKeyText().build(this.width / 2 + 62, 153, 100, 20, Component.empty(), (button, triggeredBlockResets) -> {
			this.triggeredBlockResets = triggeredBlockResets;
		}));

		this.addNewTriggeredBlockPositionOffsetButton = this.addRenderableWidget(Button.builder(ADD_BUTTON_LABEL_TEXT, button -> this.addTriggeredBlockEntry()).bounds(this.width / 2 - 4 - 150, 177, 300, 20).build());
		this.saveButton = this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> this.done()).bounds(this.width / 2 - 4 - 150, 201, 150, 20).build());
		this.cancelButton = this.addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL, button -> this.cancel()).bounds(this.width / 2 + 4, 201, 150, 20).build());
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
	public void resize(Minecraft client, int width, int height) {
		List<MutablePair<Integer, MutablePair<BlockPos, Boolean>>> list = new ArrayList<>(this.triggeredBlocksList);
		String string = this.newTriggeredBlockCounterField.getValue();
		String string1 = this.newTriggeredBlockPositionOffsetXField.getValue();
		String string2 = this.newTriggeredBlockPositionOffsetYField.getValue();
		String string3 = this.newTriggeredBlockPositionOffsetZField.getValue();
		this.init(client, width, height);
		this.triggeredBlocksList.clear();
		this.triggeredBlocksList.addAll(list);
		this.newTriggeredBlockCounterField.setValue(string);
		this.newTriggeredBlockPositionOffsetXField.setValue(string1);
		this.newTriggeredBlockPositionOffsetYField.setValue(string2);
		this.newTriggeredBlockPositionOffsetZField.setValue(string3);
		this.updateWidgets();
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		this.mouseClicked = false;
		if (this.triggeredBlocksList.size() > 5) {
			int i = this.width / 2 - 152;
			int j = 21;
			if (mouseX >= (double) i && mouseX < (double) (i + 6) && mouseY >= (double) j && mouseY < (double) (j + 114)) {
				this.mouseClicked = true;
			}
		}
		return super.mouseClicked(mouseX, mouseY, button);
	}

	@Override
	public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
		if (this.triggeredBlocksList.size() > 5 && this.mouseClicked) {
			int i = this.triggeredBlocksList.size() - 5;
			float f = (float) deltaY / (float) i;
			this.scrollAmount = Mth.clamp(this.scrollAmount + f, 0.0f, 1.0f);
			this.scrollPosition = (int) ((double) (this.scrollAmount * (float) i));
		}
		return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
		if (this.triggeredBlocksList.size() > 5 && mouseX >= (double) (this.width / 2 - 152) && mouseX <= (double) (this.width / 2 + 50) && mouseY >= 20 && mouseY <= 135) {
			int i = this.triggeredBlocksList.size() - 5;
			float f = (float) verticalAmount / (float) i;
			this.scrollAmount = Mth.clamp(this.scrollAmount - f, 0.0f, 1.0f);
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
	public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {

		super.render(context, mouseX, mouseY, delta);

		for (int i = this.scrollPosition; i < Math.min(this.scrollPosition + 5, this.triggeredBlocksList.size()); i++) {
			context.drawString(this.font, this.triggeredBlocksList.get(i).toString(), this.width / 2 - 141, 26 + ((i - this.scrollPosition) * 24), 0xA0A0A0);
		}
		if (this.triggeredBlocksList.size() > 5) {
//            context.drawGuiTexture(TRIGGERED_BLOCKS_LIST_SCROLLER_BACKGROUND_TEXTURE, this.width / 2 - 153, 20, 8, 116);
			context.blit(TRIGGERED_BLOCKS_LIST_SCROLLER_BACKGROUND_TEXTURE, this.width / 2 - 153, 20, 0, 0, 8, 116);
			int k = (int) (107.0f * this.scrollAmount);
//            context.drawGuiTexture(SCROLLER_TEXTURE, this.width / 2 - 152, 20 + 1 + k, 6, 7);
			context.blit(SCROLLER_TEXTURE, this.width / 2 - 152, 20 + 1 + k, 0, 0, 6, 7);
		}
		context.drawString(this.font, NEW_TRIGGERED_BLOCK_POSITION_TEXT, this.width / 2 - 99, 140, 0xA0A0A0);
		this.newTriggeredBlockCounterField.render(context, mouseX, mouseY, delta);
		this.newTriggeredBlockPositionOffsetXField.render(context, mouseX, mouseY, delta);
		this.newTriggeredBlockPositionOffsetYField.render(context, mouseX, mouseY, delta);
		this.newTriggeredBlockPositionOffsetZField.render(context, mouseX, mouseY, delta);
	}

	@Override
	protected boolean updateBlock() {
		ClientPlayNetworking.send(new UpdateTriggeredCounterBlockPacket(
				this.triggeredCounterBlock.getPos(),
				this.triggeredBlocksList)
		);
		return true;
	}
}
