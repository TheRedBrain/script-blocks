package com.github.theredbrain.scriptblocks.gui.screen.ingame;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public abstract class AbstractCreativeBlockSettingsScreen extends Screen {
	protected AbstractCreativeBlockSettingsScreen(Text title) {
		super(title);
	}

	protected void done() {
		if (this.updateBlock()) {
			this.close();
		}
	}

	protected void cancel() {
		this.close();
	}

	protected void updateWidgets() {

	}

	protected boolean updateBlock() {
		return true;
	}

	@Override
	public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
		this.renderInGameBackground(context);
	}

	@Override
	public boolean shouldPause() {
		return false;
	}

}
