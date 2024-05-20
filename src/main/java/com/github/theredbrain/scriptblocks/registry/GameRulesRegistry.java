package com.github.theredbrain.scriptblocks.registry;

import net.fabricmc.fabric.api.gamerule.v1.GameRuleFactory;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleRegistry;
import net.minecraft.world.GameRules;

public class GameRulesRegistry {
    public static final GameRules.Key<GameRules.BooleanRule> SCRIPT_BLOCKS_DROP_THEMSELF =
            GameRuleRegistry.register("scriptBlocksDropThemself", GameRules.Category.DROPS, GameRuleFactory.createBooleanRule(true));
    public static void init() {}
}
