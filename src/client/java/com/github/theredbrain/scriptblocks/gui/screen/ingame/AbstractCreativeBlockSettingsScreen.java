package com.github.theredbrain.scriptblocks.gui.screen.ingame;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public abstract class AbstractCreativeBlockSettingsScreen extends Screen {
	protected AbstractCreativeBlockSettingsScreen(Component title) {
		super(title);
	}

	protected void done() {
		if (this.updateBlock()) {
			this.onClose();
		}
	}

	protected void cancel() {
		this.onClose();
	}

	protected void updateWidgets() {

	}

	protected boolean updateBlock() {
		return true;
	}

	@Override
	public boolean isInGameUi() {
		return true;
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

}
