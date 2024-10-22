package com.github.theredbrain.scriptblocks.config;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.Comment;

import java.util.ArrayList;
import java.util.List;

@Config(
		name = "server"
)
public class ServerConfig implements ConfigData {
	@Comment("""
			World Spawn is chosen randomly from the following lists.
			It is recommended to set the gamerule 'spawnRadius' to 0.
			""")
	// TODO define a location entrance, where the world spawn should be
	public boolean use_predefined_position_for_world_spawn = false;
	@Comment("""
			A random value from worldSpawnXList is chosen.
			If worldSpawnYList and worldSpawnZList have an entry
			at the same index, that is the new Spawn Point.
			If not, the normal Spawn Point is used.
			""")
	public List<Integer> worldSpawnXList = new ArrayList<>();
	public List<Integer> worldSpawnYList = new ArrayList<>();
	public List<Integer> worldSpawnZList = new ArrayList<>();
	@Comment("Set to 'true' for the vanilla behaviour")
	public boolean shouldJigSawGenerationBeDeterministic = true;
	@Comment("Set to 'true' for the vanilla behaviour")
	public boolean shouldJigSawStructuresBeRandomlyRotated = true;

	@Comment("Globally enables/disables debug logging. When set to false, no debug logs are send.")
	public boolean enable_debug_logging = false;

	@Comment("Debug log is shown in the server console.")
	public boolean enable_debug_console_logging = true;
	@Comment("Debug messages are send in game.")
	public boolean enable_debug_messages = true;

	@Comment("Enables debug messages for the teleporter block and all corresponding mechanics.")
	public boolean enable_teleporter_debugging = true;

	public ServerConfig() {

	}
}
