package com.github.theredbrain.scriptblocks.config;

import com.github.theredbrain.scriptblocks.ScriptBlocks;
import me.fzzyhmstrs.fzzy_config.annotations.Comment;
import me.fzzyhmstrs.fzzy_config.annotations.ConvertFrom;
import me.fzzyhmstrs.fzzy_config.config.Config;

@ConvertFrom(fileName = "client.json5", folder = "scriptblocks")
public class ClientConfig extends Config {

	public ClientConfig() {
		super(ScriptBlocks.identifier("client"));
	}

	@Comment("Rendering very large areas can lead to lag/crashes. This setting allows to toggle the rendering. Useful if the game crashes when trying to render large areas.")
	public boolean disable_area_renderer = true;

}
