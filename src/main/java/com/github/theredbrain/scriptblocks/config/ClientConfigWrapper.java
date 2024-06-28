package com.github.theredbrain.scriptblocks.config;

import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import me.shedaniel.autoconfig.serializer.PartitioningSerializer;

@Config(
		name = "scriptblocks"
)
public class ClientConfigWrapper extends PartitioningSerializer.GlobalData {
	@ConfigEntry.Category("client")
	@ConfigEntry.Gui.TransitiveObject
	public ClientConfig client = new ClientConfig();

	public ClientConfigWrapper() {
	}
}
