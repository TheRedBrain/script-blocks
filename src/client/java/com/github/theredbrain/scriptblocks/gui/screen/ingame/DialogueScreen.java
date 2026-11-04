package com.github.theredbrain.scriptblocks.gui.screen.ingame;

import com.github.theredbrain.scriptblocks.ScriptBlocks;
import com.github.theredbrain.scriptblocks.data.DialogueAnswer;
import com.github.theredbrain.scriptblocks.network.packet.DialogueAnswerPacket;
import com.github.theredbrain.scriptblocks.registry.ScriptBlocksDynamicRegistries;
import com.github.theredbrain.scriptblocks.screen.DialogueScreenHandler;
import com.github.theredbrain.scriptblocks.util.DialogueHelper;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;

import java.util.Optional;

@Environment(value = EnvType.CLIENT)
public class DialogueScreen extends AbstractContainerScreen<DialogueScreenHandler> {
	public static final Identifier GENERIC_BACKGROUND_TEXTURE = ScriptBlocks.identifier("container/generic_background");
	private static final Identifier SCROLL_BAR_BACKGROUND_TEXTURE = ScriptBlocks.identifier("scroll_bar/scroll_bar_background");
	private static final Identifier SCROLLER_VERTICAL_6_7_TEXTURE = ScriptBlocks.identifier("scroll_bar/scroller_vertical_6_7");
	private static final int TOTAL_LINE_AMOUNT = 8;
	private static final int LINE_HEIGHT = 26;

	private Button answerButton0;
	private Button answerButton1;
	private Button answerButton2;
	private Button answerButton3;
	private Button answerButton4;
	private Button answerButton5;
	private Button answerButton6;
	private Button answerButton7;

	private int backgroundWidth;
	private int backgroundHeight;
	private int x;
	private int y;
	private int dialogueTextScrollPosition = 0;
	private float dialogueTextScrollAmount = 0.0f;
	private boolean dialogueTextMouseClicked = false;
	private int answersScrollPosition = 0;
	private float answersScrollAmount = 0.0f;
	private boolean answersMouseClicked = false;

	public DialogueScreen(DialogueScreenHandler handler, Inventory inventory, Component title) {
		super(handler, inventory, title, 218, 222);
	}

	private void answer(int index) {
		int finalIndex = index + this.answersScrollPosition - this.menu.answersStartingIndex;

		if (finalIndex >= 0 && finalIndex < this.menu.visibleAnswersList.size()) {
			String currentAnswerIdentifierString = this.menu.visibleAnswersList.get(finalIndex);

			if (!currentAnswerIdentifierString.isEmpty()) {
				ClientPlayNetworking.send(new DialogueAnswerPacket(
						Identifier.parse(currentAnswerIdentifierString),
						this.menu.dataBlockPos,
						this.menu.dialogueUsedBlocksList,
						this.menu.dialogueTriggeredBlocksList
				));
			}
		}
	}

	@Override
	protected void init() {
		if (this.menu.dialogue == null) {
			this.minecraft.setScreen(null);
			return;
		}

		super.init();

		this.answerButton0 = this.addRenderableWidget(Button.builder(Component.empty(), button -> this.answer(0)).bounds(this.leftPos + 7, this.topPos + 10, this.imageWidth - 14, 20).build());
		this.answerButton1 = this.addRenderableWidget(Button.builder(Component.empty(), button -> this.answer(1)).bounds(this.leftPos + 7, this.topPos + 36, this.imageWidth - 14, 20).build());
		this.answerButton2 = this.addRenderableWidget(Button.builder(Component.empty(), button -> this.answer(2)).bounds(this.leftPos + 7, this.topPos + 62, this.imageWidth - 14, 20).build());
		this.answerButton3 = this.addRenderableWidget(Button.builder(Component.empty(), button -> this.answer(3)).bounds(this.leftPos + 7, this.topPos + 88, this.imageWidth - 14, 20).build());
		this.answerButton4 = this.addRenderableWidget(Button.builder(Component.empty(), button -> this.answer(4)).bounds(this.leftPos + 7, this.topPos + 114, this.imageWidth - 14, 20).build());
		this.answerButton5 = this.addRenderableWidget(Button.builder(Component.empty(), button -> this.answer(5)).bounds(this.leftPos + 7, this.topPos + 140, this.imageWidth - 14, 20).build());
		this.answerButton6 = this.addRenderableWidget(Button.builder(Component.empty(), button -> this.answer(6)).bounds(this.leftPos + 7, this.topPos + 166, this.imageWidth - 14, 20).build());
		this.answerButton7 = this.addRenderableWidget(Button.builder(Component.empty(), button -> this.answer(7)).bounds(this.leftPos + 7, this.topPos + 192, this.imageWidth - 14, 20).build());

		if (this.menu.visibleAnswersList.size() > TOTAL_LINE_AMOUNT - this.menu.getAnswersStartingIndex()) {
			this.answerButton0.setWidth(this.imageWidth - 26);
			this.answerButton1.setWidth(this.imageWidth - 26);
			this.answerButton2.setWidth(this.imageWidth - 26);
			this.answerButton3.setWidth(this.imageWidth - 26);
			this.answerButton4.setWidth(this.imageWidth - 26);
			this.answerButton5.setWidth(this.imageWidth - 26);
			this.answerButton6.setWidth(this.imageWidth - 26);
			this.answerButton7.setWidth(this.imageWidth - 26);
		}

		this.updateWidgets();
		this.dialogueTextScrollPosition = 0;
		this.dialogueTextScrollAmount = 0.0f;
		this.answersScrollPosition = 0;
		this.answersScrollAmount = 0.0f;
	}

	private void updateWidgets() {

		this.answerButton0.visible = false;
		this.answerButton1.visible = false;
		this.answerButton2.visible = false;
		this.answerButton3.visible = false;
		this.answerButton4.visible = false;
		this.answerButton5.visible = false;
		this.answerButton6.visible = false;
		this.answerButton7.visible = false;

		if (this.menu.visibleAnswersList.size() == this.menu.unlockedAnswersList.size()) {
			int index = this.menu.getAnswersStartingIndex();
			for (int i = 0; i < Math.min(TOTAL_LINE_AMOUNT - this.menu.getAnswersStartingIndex(), this.menu.visibleAnswersList.size()); i++) {
				boolean visible = !this.menu.visibleAnswersList.get(i).isEmpty();
				boolean active = visible && !this.menu.unlockedAnswersList.get(i).isEmpty();
				if (index == 0) {
					this.answerButton0.visible = visible;
					this.answerButton0.active = active;
				} else if (index == 1) {
					this.answerButton1.visible = visible;
					this.answerButton1.active = active;
				} else if (index == 2) {
					this.answerButton2.visible = visible;
					this.answerButton2.active = active;
				} else if (index == 3) {
					this.answerButton3.visible = visible;
					this.answerButton3.active = active;
				} else if (index == 4) {
					this.answerButton4.visible = visible;
					this.answerButton4.active = active;
				} else if (index == 5) {
					this.answerButton5.visible = visible;
					this.answerButton5.active = active;
				} else if (index == 6) {
					this.answerButton6.visible = visible;
					this.answerButton6.active = active;
				} else if (index == 7) {
					this.answerButton7.visible = visible;
					this.answerButton7.active = active;
				}
				index++;
			}
		}
	}

	@Override
	public void resize(final int width, final int height) {
		int number = this.dialogueTextScrollPosition;
		float number1 = this.dialogueTextScrollAmount;
		int number2 = this.answersScrollPosition;
		float number3 = this.answersScrollAmount;
		this.init(width, height);
		this.dialogueTextScrollPosition = number;
		this.dialogueTextScrollAmount = number1;
		this.answersScrollPosition = number2;
		this.answersScrollAmount = number3;
		this.updateWidgets();
		this.dialogueTextScrollPosition = 0;
		this.dialogueTextScrollAmount = 0.0f;
		this.answersScrollPosition = 0;
		this.answersScrollAmount = 0.0f;
	}

	@Override
	public boolean mouseClicked(final MouseButtonEvent event, final boolean doubleClick) {
		this.dialogueTextMouseClicked = false;
		this.answersMouseClicked = false;
		int visibleDialogueTextAmount = Math.min(TOTAL_LINE_AMOUNT - this.menu.getDialogueTextsStartingIndex(), this.menu.visibleAnswersList.isEmpty() ? TOTAL_LINE_AMOUNT : this.menu.getAnswersStartingIndex() - this.menu.getDialogueTextsStartingIndex());
		int visibleAnswersAmount = TOTAL_LINE_AMOUNT - this.menu.getAnswersStartingIndex();
		double dialogueLinesMinY = this.topPos + 7 + 1 + this.menu.getDialogueTextsStartingIndex() * LINE_HEIGHT;
		double visibleAnswersMinY = this.topPos + 7 + 1 + this.menu.getAnswersStartingIndex() * LINE_HEIGHT;
		double dialogueLinesMaxY = Math.min(dialogueLinesMinY - 2 + visibleDialogueTextAmount * LINE_HEIGHT, visibleAnswersMinY);
		double visibleAnswersMaxY = visibleAnswersMinY - 2 + visibleAnswersAmount * LINE_HEIGHT;
		if (this.menu.getDialogueTextsStartingIndex() < this.menu.getAnswersStartingIndex()
				&& this.menu.getDialogueTextsStartingIndex() < TOTAL_LINE_AMOUNT
				&& this.menu.dialogueTextList.size() > visibleDialogueTextAmount) {
			int i = this.leftPos + this.imageWidth - 14;
			if (event.x() >= (double) i && event.x() < (double) (i + 6) && event.y() >= dialogueLinesMinY && event.y() < dialogueLinesMaxY) {
				this.dialogueTextMouseClicked = true;
			}
		}
		if (this.menu.getAnswersStartingIndex() < TOTAL_LINE_AMOUNT
				&& this.menu.visibleAnswersList.size() > visibleAnswersAmount) {
			int i = this.leftPos + this.imageWidth - 14;
			if (event.x() >= (double) i && event.x() < (double) (i + 6) && event.y() >= visibleAnswersMinY && event.y() < visibleAnswersMaxY) {
				this.answersMouseClicked = true;
			}
		}
		return super.mouseClicked(event, doubleClick);
	}

	@Override
	public boolean mouseDragged(final MouseButtonEvent event, final double dx, final double dy) {
		boolean updateWidgets = false;
		if (this.menu.dialogueTextList.size() > this.menu.getAnswersStartingIndex()
				&& this.dialogueTextMouseClicked) {
			int i = this.menu.dialogueTextList.size() - (Math.min(TOTAL_LINE_AMOUNT - this.menu.getDialogueTextsStartingIndex(), this.menu.getAnswersStartingIndex()));
			float f = (float) dy / (float) i;
			this.dialogueTextScrollAmount = Mth.clamp(this.dialogueTextScrollAmount + f, 0.0f, 1.0f);
			this.dialogueTextScrollPosition = (int) ((double) (this.dialogueTextScrollAmount * (float) i));
			updateWidgets = true;
		}
		if (this.menu.visibleAnswersList.size() > TOTAL_LINE_AMOUNT - this.menu.getAnswersStartingIndex()
				&& this.answersMouseClicked) {
			int i = this.menu.visibleAnswersList.size() - TOTAL_LINE_AMOUNT - this.menu.getAnswersStartingIndex();
			float f = (float) dy / (float) i;
			this.answersScrollAmount = Mth.clamp(this.answersScrollAmount + f, 0.0f, 1.0f);
			this.answersScrollPosition = (int) ((double) (this.answersScrollAmount * (float) i));
			updateWidgets = true;
		}
		if (updateWidgets) {
			updateWidgets();
		}
		return super.mouseDragged(event, dx, dy);
	}

	@Override
	public boolean mouseScrolled(final double x, final double y, final double scrollX, final double scrollY) {
		boolean updateWidgets = false;
		int visibleDialogueTextAmount = Math.min(TOTAL_LINE_AMOUNT - this.menu.getDialogueTextsStartingIndex(), this.menu.visibleAnswersList.isEmpty() ? TOTAL_LINE_AMOUNT : this.menu.getAnswersStartingIndex() - this.menu.getDialogueTextsStartingIndex());
		int visibleAnswersAmount = TOTAL_LINE_AMOUNT - this.menu.getAnswersStartingIndex();
		double dialogueLinesMinY = this.topPos + 7 + this.menu.getDialogueTextsStartingIndex() * LINE_HEIGHT;
		double visibleAnswersMinY = this.topPos + 7 + this.menu.getAnswersStartingIndex() * LINE_HEIGHT;
		double dialogueLinesMaxY = Math.min(dialogueLinesMinY + visibleDialogueTextAmount * LINE_HEIGHT, visibleAnswersMinY);
		double visibleAnswersMaxY = visibleAnswersMinY + visibleAnswersAmount * LINE_HEIGHT;
		if (this.menu.dialogueTextList.size() > visibleDialogueTextAmount
				&& x >= (double) (this.leftPos + 7) && x <= (double) (this.leftPos + this.imageWidth - 7)
				&& y >= dialogueLinesMinY && y <= dialogueLinesMaxY) {
			int i = this.menu.dialogueTextList.size() - visibleDialogueTextAmount;
			float f = (float) scrollY / (float) i;
			this.dialogueTextScrollAmount = Mth.clamp(this.dialogueTextScrollAmount - f, 0.0f, 1.0f);
			this.dialogueTextScrollPosition = (int) ((double) (this.dialogueTextScrollAmount * (float) i));
			updateWidgets = true;
		}
		if (this.menu.visibleAnswersList.size() > visibleAnswersAmount
				&& x >= (double) (this.leftPos + 7) && x <= (double) (this.leftPos + this.imageWidth - 7)
				&& y >= visibleAnswersMinY && y <= visibleAnswersMaxY) {
			int i = this.menu.visibleAnswersList.size() - visibleAnswersAmount;
			float f = (float) scrollY / (float) i;
			this.answersScrollAmount = Mth.clamp(this.answersScrollAmount - f, 0.0f, 1.0f);
			this.answersScrollPosition = (int) ((double) (this.answersScrollAmount * (float) i));
			updateWidgets = true;
		}
		if (updateWidgets) {
			updateWidgets();
		}
		return super.mouseScrolled(x, y, scrollX, scrollY);
	}

	@Override
	public void extractBackground(final GuiGraphicsExtractor graphics, final int mouseX, final int mouseY, final float a) {
		super.extractBackground(graphics, mouseX, mouseY, a);
		graphics.blitSprite(RenderPipelines.GUI_TEXTURED, GENERIC_BACKGROUND_TEXTURE, this.leftPos, this.topPos, this.imageWidth, this.imageHeight);
	}

	@Override
	public void extractRenderState(final GuiGraphicsExtractor graphics, final int mouseX, final int mouseY, final float a) {

		super.extractRenderState(graphics, mouseX, mouseY, a);

		int visibleDialogueTextAmount = Math.min(TOTAL_LINE_AMOUNT - this.menu.getDialogueTextsStartingIndex(), this.menu.visibleAnswersList.isEmpty() ? TOTAL_LINE_AMOUNT : this.menu.getAnswersStartingIndex() - this.menu.getDialogueTextsStartingIndex());
		int visibleAnswersAmount = TOTAL_LINE_AMOUNT - this.menu.getAnswersStartingIndex();
		int dialogueLinesMinY = this.topPos + 7 + (this.menu.getDialogueTextsStartingIndex() * LINE_HEIGHT);
//		int dialogueLinesMaxY = dialogueLinesMinY + (visibleDialogueTextAmount * LINE_HEIGHT);
		int visibleAnswersMinY = this.topPos + 7 + (this.menu.getAnswersStartingIndex() * LINE_HEIGHT);
//		int visibleAnswersMaxY = visibleAnswersMinY + (visibleAnswersAmount * LINE_HEIGHT);

		if (this.menu.getDialogueTextsStartingIndex() < this.menu.getAnswersStartingIndex()) {
			for (int i = this.dialogueTextScrollPosition; i < Math.min(this.dialogueTextScrollPosition + visibleDialogueTextAmount, this.menu.dialogueTextList.size()); i++) {
				String text = this.menu.dialogueTextList.get(i);
				if (!text.isEmpty()) {
					graphics.text(this.font, Component.translatable(text), this.leftPos + 8, this.topPos + 7 + 8 + ((i - this.dialogueTextScrollPosition + this.menu.getDialogueTextsStartingIndex()) * LINE_HEIGHT), 0x404040, false);
				}
			}
			if (this.menu.dialogueTextList.size() > visibleDialogueTextAmount) {
				graphics.blitSprite(RenderPipelines.GUI_TEXTURED, SCROLL_BAR_BACKGROUND_TEXTURE, this.leftPos + this.imageWidth - 15, dialogueLinesMinY, 8, visibleDialogueTextAmount * LINE_HEIGHT);
				int k = (int) ((visibleDialogueTextAmount * LINE_HEIGHT - 9) * this.dialogueTextScrollAmount);
				graphics.blitSprite(RenderPipelines.GUI_TEXTURED, SCROLLER_VERTICAL_6_7_TEXTURE, this.leftPos + this.imageWidth - 14, dialogueLinesMinY + 1 + k, 6, 7);
			}
		}
		int index = this.menu.getAnswersStartingIndex();
		for (int i = this.answersScrollPosition; i < Math.min(this.answersScrollPosition + visibleAnswersAmount, this.menu.visibleAnswersList.size()); i++) {
			String text = "";
			String answerIdentifier = this.menu.visibleAnswersList.get(i);

			if (!answerIdentifier.isEmpty()) {
				DialogueAnswer dialogueAnswer = null;
				Identifier identifier = Identifier.tryParse(answerIdentifier);
				if (this.menu.world != null && identifier != null) {
					dialogueAnswer = DialogueHelper.getDialogueAnswer(this.menu.world.registryAccess(), identifier);
				}

				if (dialogueAnswer != null) {
					text = dialogueAnswer.answerText();
				}
				if (index == 0) {
					this.answerButton0.setMessage(Component.translatable(text));
				} else if (index == 1) {
					this.answerButton1.setMessage(Component.translatable(text));
				} else if (index == 2) {
					this.answerButton2.setMessage(Component.translatable(text));
				} else if (index == 3) {
					this.answerButton3.setMessage(Component.translatable(text));
				} else if (index == 4) {
					this.answerButton4.setMessage(Component.translatable(text));
				} else if (index == 5) {
					this.answerButton5.setMessage(Component.translatable(text));
				} else if (index == 6) {
					this.answerButton6.setMessage(Component.translatable(text));
				} else if (index == 7) {
					this.answerButton7.setMessage(Component.translatable(text));
				}
			}
			index++;
		}
		if (this.menu.visibleAnswersList.size() > visibleAnswersAmount) {
			graphics.blitSprite(RenderPipelines.GUI_TEXTURED, SCROLL_BAR_BACKGROUND_TEXTURE, this.leftPos + this.imageWidth - 15, visibleAnswersMinY, 8, visibleAnswersAmount * LINE_HEIGHT);
			int k = (int) ((visibleAnswersAmount * LINE_HEIGHT - 9) * this.answersScrollAmount);
			graphics.blitSprite(RenderPipelines.GUI_TEXTURED, SCROLLER_VERTICAL_6_7_TEXTURE, this.leftPos + this.imageWidth - 14, visibleAnswersMinY + 1 + k, 6, 7);
		}
	}

	@Override
	public boolean shouldCloseOnEsc() {
		return this.menu.dialogue == null || this.menu.dialogue.cancellable(); // TODO emergency exit
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}
}
