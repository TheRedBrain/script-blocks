package com.github.theredbrain.scriptblocks.gui.screen.ingame;

import com.github.theredbrain.scriptblocks.ScriptBlocks;
import com.github.theredbrain.scriptblocks.data.DialogueAnswer;
import com.github.theredbrain.scriptblocks.network.packet.DialogueAnswerPacket;
import com.github.theredbrain.scriptblocks.registry.CustomDynamicRegistries;
import com.github.theredbrain.scriptblocks.screen.DialogueScreenHandler;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

import java.util.Optional;

@Environment(value = EnvType.CLIENT)
public class DialogueScreen extends HandledScreen<DialogueScreenHandler> {
	public static final Identifier GENERIC_BACKGROUND_TEXTURE = ScriptBlocks.identifier("container/generic_background");
	private static final Identifier SCROLL_BAR_BACKGROUND_TEXTURE = ScriptBlocks.identifier("scroll_bar/scroll_bar_background");
	private static final Identifier SCROLLER_VERTICAL_6_7_TEXTURE = ScriptBlocks.identifier("scroll_bar/scroller_vertical_6_7");
	private static final int TOTAL_LINE_AMOUNT = 8;
	private static final int LINE_HEIGHT = 26;

	private ButtonWidget answerButton0;
	private ButtonWidget answerButton1;
	private ButtonWidget answerButton2;
	private ButtonWidget answerButton3;
	private ButtonWidget answerButton4;
	private ButtonWidget answerButton5;
	private ButtonWidget answerButton6;
	private ButtonWidget answerButton7;

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

	public DialogueScreen(DialogueScreenHandler handler, PlayerInventory inventory, Text title) {
		super(handler, inventory, title);
	}

	private void answer(int index) {
		ScriptBlocks.info("answer");
		ScriptBlocks.info("index: " + index);
		ScriptBlocks.info("this.answersScrollPosition: " + this.answersScrollPosition);
		ScriptBlocks.info("this.handler.visibleAnswersList.size(): " + this.handler.visibleAnswersList.size());
		int finalIndex = index + this.answersScrollPosition - this.handler.answersStartingIndex;
		ScriptBlocks.info("finalIndex: " + finalIndex);
		if (finalIndex >= 0 && finalIndex < this.handler.visibleAnswersList.size()) {
			ScriptBlocks.info("this.handler.answersStartingIndex: " + this.handler.answersStartingIndex);
			String currentAnswerIdentifierString = this.handler.visibleAnswersList.get(finalIndex);

			ScriptBlocks.info("currentAnswerIdentifier: " + currentAnswerIdentifierString);
			if (!currentAnswerIdentifierString.isEmpty()) {
				ClientPlayNetworking.send(new DialogueAnswerPacket(
						Identifier.of(currentAnswerIdentifierString),
						this.handler.dataBlockPos,
						this.handler.dialogueUsedBlocksList,
						this.handler.dialogueTriggeredBlocksList
				));
			}
		}
	}

	@Override
	protected void init() {
		if (this.handler.dialogue == null && this.client != null) {
			this.client.setScreen(null);
			return;
		}

		this.backgroundWidth = 218;
		this.backgroundHeight = 222;
		this.x = (this.width - this.backgroundWidth) / 2;
		this.y = (this.height - this.backgroundHeight) / 2;

		super.init();

		this.answerButton0 = this.addDrawableChild(ButtonWidget.builder(Text.empty(), button -> this.answer(0)).dimensions(this.x + 7, this.y + 10, this.backgroundWidth - 14, 20).build());
		this.answerButton1 = this.addDrawableChild(ButtonWidget.builder(Text.empty(), button -> this.answer(1)).dimensions(this.x + 7, this.y + 36, this.backgroundWidth - 14, 20).build());
		this.answerButton2 = this.addDrawableChild(ButtonWidget.builder(Text.empty(), button -> this.answer(2)).dimensions(this.x + 7, this.y + 62, this.backgroundWidth - 14, 20).build());
		this.answerButton3 = this.addDrawableChild(ButtonWidget.builder(Text.empty(), button -> this.answer(3)).dimensions(this.x + 7, this.y + 88, this.backgroundWidth - 14, 20).build());
		this.answerButton4 = this.addDrawableChild(ButtonWidget.builder(Text.empty(), button -> this.answer(4)).dimensions(this.x + 7, this.y + 114, this.backgroundWidth - 14, 20).build());
		this.answerButton5 = this.addDrawableChild(ButtonWidget.builder(Text.empty(), button -> this.answer(5)).dimensions(this.x + 7, this.y + 140, this.backgroundWidth - 14, 20).build());
		this.answerButton6 = this.addDrawableChild(ButtonWidget.builder(Text.empty(), button -> this.answer(6)).dimensions(this.x + 7, this.y + 166, this.backgroundWidth - 14, 20).build());
		this.answerButton7 = this.addDrawableChild(ButtonWidget.builder(Text.empty(), button -> this.answer(7)).dimensions(this.x + 7, this.y + 192, this.backgroundWidth - 14, 20).build());

		if (this.handler.visibleAnswersList.size() > TOTAL_LINE_AMOUNT - this.handler.getAnswersStartingIndex()) {
			this.answerButton0.setWidth(this.backgroundWidth - 26);
			this.answerButton1.setWidth(this.backgroundWidth - 26);
			this.answerButton2.setWidth(this.backgroundWidth - 26);
			this.answerButton3.setWidth(this.backgroundWidth - 26);
			this.answerButton4.setWidth(this.backgroundWidth - 26);
			this.answerButton5.setWidth(this.backgroundWidth - 26);
			this.answerButton6.setWidth(this.backgroundWidth - 26);
			this.answerButton7.setWidth(this.backgroundWidth - 26);
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

		int index = this.handler.getAnswersStartingIndex();
		for (int i = 0; i < Math.min(TOTAL_LINE_AMOUNT - this.handler.getAnswersStartingIndex(), this.handler.visibleAnswersList.size()); i++) {
			boolean visible = !this.handler.visibleAnswersList.get(i).isEmpty();
			boolean active = visible && !this.handler.unlockedAnswersList.get(i).isEmpty();
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

	@Override
	public void resize(MinecraftClient client, int width, int height) {
		int number = this.dialogueTextScrollPosition;
		float number1 = this.dialogueTextScrollAmount;
		int number2 = this.answersScrollPosition;
		float number3 = this.answersScrollAmount;
		this.init(client, width, height);
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
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		this.dialogueTextMouseClicked = false;
		this.answersMouseClicked = false;
		int visibleDialogueTextAmount = Math.min(TOTAL_LINE_AMOUNT - this.handler.getDialogueTextsStartingIndex(), this.handler.visibleAnswersList.isEmpty() ? TOTAL_LINE_AMOUNT : this.handler.getAnswersStartingIndex() - this.handler.getDialogueTextsStartingIndex());
		int visibleAnswersAmount = TOTAL_LINE_AMOUNT - this.handler.getAnswersStartingIndex();
		double dialogueLinesMinY = this.y + 7 + 1 + this.handler.getDialogueTextsStartingIndex() * LINE_HEIGHT;
		double visibleAnswersMinY = this.y + 7 + 1 + this.handler.getAnswersStartingIndex() * LINE_HEIGHT;
		double dialogueLinesMaxY = Math.min(dialogueLinesMinY - 2 + visibleDialogueTextAmount * LINE_HEIGHT, visibleAnswersMinY);
		double visibleAnswersMaxY = visibleAnswersMinY - 2 + visibleAnswersAmount * LINE_HEIGHT;
		if (this.handler.getDialogueTextsStartingIndex() < this.handler.getAnswersStartingIndex()
				&& this.handler.getDialogueTextsStartingIndex() < TOTAL_LINE_AMOUNT
				&& this.handler.dialogueTextList.size() > visibleDialogueTextAmount) {
			int i = this.x + this.backgroundWidth - 14;
			if (mouseX >= (double) i && mouseX < (double) (i + 6) && mouseY >= dialogueLinesMinY && mouseY < dialogueLinesMaxY) {
				this.dialogueTextMouseClicked = true;
			}
		}
		if (this.handler.getAnswersStartingIndex() < TOTAL_LINE_AMOUNT
				&& this.handler.visibleAnswersList.size() > visibleAnswersAmount) {
			int i = this.x + this.backgroundWidth - 14;
			if (mouseX >= (double) i && mouseX < (double) (i + 6) && mouseY >= visibleAnswersMinY && mouseY < visibleAnswersMaxY) {
				this.answersMouseClicked = true;
			}
		}
		return super.mouseClicked(mouseX, mouseY, button);
	}

	@Override
	public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
		boolean updateWidgets = false;
		if (this.handler.dialogueTextList.size() > this.handler.getAnswersStartingIndex()
				&& this.dialogueTextMouseClicked) {
			int i = this.handler.dialogueTextList.size() - (Math.min(TOTAL_LINE_AMOUNT - this.handler.getDialogueTextsStartingIndex(), this.handler.getAnswersStartingIndex()));
			float f = (float) deltaY / (float) i;
			this.dialogueTextScrollAmount = MathHelper.clamp(this.dialogueTextScrollAmount + f, 0.0f, 1.0f);
			this.dialogueTextScrollPosition = (int) ((double) (this.dialogueTextScrollAmount * (float) i));
			updateWidgets = true;
		}
		if (this.handler.visibleAnswersList.size() > TOTAL_LINE_AMOUNT - this.handler.getAnswersStartingIndex()
				&& this.answersMouseClicked) {
			int i = this.handler.visibleAnswersList.size() - TOTAL_LINE_AMOUNT - this.handler.getAnswersStartingIndex();
			float f = (float) deltaY / (float) i;
			this.answersScrollAmount = MathHelper.clamp(this.answersScrollAmount + f, 0.0f, 1.0f);
			this.answersScrollPosition = (int) ((double) (this.answersScrollAmount * (float) i));
			updateWidgets = true;
		}
		if (updateWidgets) {
			updateWidgets();
		}
		return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
		boolean updateWidgets = false;
		int visibleDialogueTextAmount = Math.min(TOTAL_LINE_AMOUNT - this.handler.getDialogueTextsStartingIndex(), this.handler.visibleAnswersList.isEmpty() ? TOTAL_LINE_AMOUNT : this.handler.getAnswersStartingIndex() - this.handler.getDialogueTextsStartingIndex());
		int visibleAnswersAmount = TOTAL_LINE_AMOUNT - this.handler.getAnswersStartingIndex();
		double dialogueLinesMinY = this.y + 7 + this.handler.getDialogueTextsStartingIndex() * LINE_HEIGHT;
		double visibleAnswersMinY = this.y + 7 + this.handler.getAnswersStartingIndex() * LINE_HEIGHT;
		double dialogueLinesMaxY = Math.min(dialogueLinesMinY + visibleDialogueTextAmount * LINE_HEIGHT, visibleAnswersMinY);
		double visibleAnswersMaxY = visibleAnswersMinY + visibleAnswersAmount * LINE_HEIGHT;
		if (this.handler.dialogueTextList.size() > visibleDialogueTextAmount
				&& mouseX >= (double) (this.x + 7) && mouseX <= (double) (this.x + this.backgroundWidth - 7)
				&& mouseY >= dialogueLinesMinY && mouseY <= dialogueLinesMaxY) {
			int i = this.handler.dialogueTextList.size() - visibleDialogueTextAmount;
			float f = (float) verticalAmount / (float) i;
			this.dialogueTextScrollAmount = MathHelper.clamp(this.dialogueTextScrollAmount - f, 0.0f, 1.0f);
			this.dialogueTextScrollPosition = (int) ((double) (this.dialogueTextScrollAmount * (float) i));
			updateWidgets = true;
		}
		if (this.handler.visibleAnswersList.size() > visibleAnswersAmount
				&& mouseX >= (double) (this.x + 7) && mouseX <= (double) (this.x + this.backgroundWidth - 7)
				&& mouseY >= visibleAnswersMinY && mouseY <= visibleAnswersMaxY) {
			int i = this.handler.visibleAnswersList.size() - visibleAnswersAmount;
			float f = (float) verticalAmount / (float) i;
			this.answersScrollAmount = MathHelper.clamp(this.answersScrollAmount - f, 0.0f, 1.0f);
			this.answersScrollPosition = (int) ((double) (this.answersScrollAmount * (float) i));
			updateWidgets = true;
		}
		if (updateWidgets) {
			updateWidgets();
		}
		return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
	}

	@Override
	public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
		super.renderBackground(context, mouseX, mouseY, delta);
		this.drawBackground(context, delta, mouseX, mouseY);
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {

		super.render(context, mouseX, mouseY, delta);

		int visibleDialogueTextAmount = Math.min(TOTAL_LINE_AMOUNT - this.handler.getDialogueTextsStartingIndex(), this.handler.visibleAnswersList.isEmpty() ? TOTAL_LINE_AMOUNT : this.handler.getAnswersStartingIndex() - this.handler.getDialogueTextsStartingIndex());
		int visibleAnswersAmount = TOTAL_LINE_AMOUNT - this.handler.getAnswersStartingIndex();
		int dialogueLinesMinY = this.y + 7 + (this.handler.getDialogueTextsStartingIndex() * LINE_HEIGHT);
//		int dialogueLinesMaxY = dialogueLinesMinY + (visibleDialogueTextAmount * LINE_HEIGHT);
		int visibleAnswersMinY = this.y + 7 + (this.handler.getAnswersStartingIndex() * LINE_HEIGHT);
//		int visibleAnswersMaxY = visibleAnswersMinY + (visibleAnswersAmount * LINE_HEIGHT);

		if (this.handler.getDialogueTextsStartingIndex() < this.handler.getAnswersStartingIndex()) {
			for (int i = this.dialogueTextScrollPosition; i < Math.min(this.dialogueTextScrollPosition + visibleDialogueTextAmount, this.handler.dialogueTextList.size()); i++) {
				String text = this.handler.dialogueTextList.get(i);
				if (!text.isEmpty()) {
					context.drawText(this.textRenderer, Text.translatable(text), this.x + 8, this.y + 7 + 8 + ((i - this.dialogueTextScrollPosition + this.handler.getDialogueTextsStartingIndex()) * LINE_HEIGHT), 0x404040, false);
				}
			}
			if (this.handler.dialogueTextList.size() > visibleDialogueTextAmount) {
				context.drawGuiTexture(SCROLL_BAR_BACKGROUND_TEXTURE, this.x + this.backgroundWidth - 15, dialogueLinesMinY, 8, visibleDialogueTextAmount * LINE_HEIGHT);
				int k = (int) ((visibleDialogueTextAmount * LINE_HEIGHT - 9) * this.dialogueTextScrollAmount);
				context.drawGuiTexture(SCROLLER_VERTICAL_6_7_TEXTURE, this.x + this.backgroundWidth - 14, dialogueLinesMinY + 1 + k, 6, 7);
			}
		}
		int index = this.handler.getAnswersStartingIndex();
		for (int i = this.answersScrollPosition; i < Math.min(this.answersScrollPosition + visibleAnswersAmount, this.handler.visibleAnswersList.size()); i++) {
			String text = "";
			String answerIdentifier = this.handler.visibleAnswersList.get(i);

			if (!answerIdentifier.isEmpty()) {
				DialogueAnswer dialogueAnswer = null;
				if (this.handler.world != null) {
					Optional<RegistryEntry.Reference<DialogueAnswer>> optionalDialogueAnswerReference = this.handler.world.getRegistryManager().get(CustomDynamicRegistries.DIALOGUE_ANSWER_REGISTRY_KEY).getEntry(Identifier.of(answerIdentifier));
					if (optionalDialogueAnswerReference.isPresent()) {
						dialogueAnswer = optionalDialogueAnswerReference.get().value();
					}
				}

				if (dialogueAnswer != null) {
					text = dialogueAnswer.answerText();
				}
				if (index == 0) {
					this.answerButton0.setMessage(Text.translatable(text));
				} else if (index == 1) {
					this.answerButton1.setMessage(Text.translatable(text));
				} else if (index == 2) {
					this.answerButton2.setMessage(Text.translatable(text));
				} else if (index == 3) {
					this.answerButton3.setMessage(Text.translatable(text));
				} else if (index == 4) {
					this.answerButton4.setMessage(Text.translatable(text));
				} else if (index == 5) {
					this.answerButton5.setMessage(Text.translatable(text));
				} else if (index == 6) {
					this.answerButton6.setMessage(Text.translatable(text));
				} else if (index == 7) {
					this.answerButton7.setMessage(Text.translatable(text));
				}
			}
			index++;
		}
		if (this.handler.visibleAnswersList.size() > visibleAnswersAmount) {
			context.drawGuiTexture(SCROLL_BAR_BACKGROUND_TEXTURE, this.x + this.backgroundWidth - 15, visibleAnswersMinY, 8, visibleAnswersAmount * LINE_HEIGHT);
			int k = (int) ((visibleAnswersAmount * LINE_HEIGHT - 9) * this.answersScrollAmount);
			context.drawGuiTexture(SCROLLER_VERTICAL_6_7_TEXTURE, this.x + this.backgroundWidth - 14, visibleAnswersMinY + 1 + k, 6, 7);
		}
	}

	@Override
	protected void drawForeground(DrawContext context, int mouseX, int mouseY) {
	}

	@Override
	protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
		context.drawGuiTexture(GENERIC_BACKGROUND_TEXTURE, this.x, this.y, this.backgroundWidth, this.backgroundHeight);
	}

	@Override
	public boolean shouldCloseOnEsc() {
		return this.handler.dialogue == null || this.handler.dialogue.cancellable(); // TODO emergency exit
	}

	@Override
	public boolean shouldPause() {
		return false;
	}
}
