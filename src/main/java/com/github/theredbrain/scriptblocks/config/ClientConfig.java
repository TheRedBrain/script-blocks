package com.github.theredbrain.scriptblocks.config;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.Comment;

@Config(
        name = "client"
)
public class ClientConfig implements ConfigData {
    @Comment("Additional debug messages are shown in-game.")
    public boolean show_debug_messages = false;
    public ClientConfig() {

    }
}
