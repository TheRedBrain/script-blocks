package com.github.theredbrain.scriptblocks_test;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ScriptBlocksTest implements ModInitializer {
	public static final String MOD_ID = "scriptblocks";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("This test was scripted!");
	}
}
