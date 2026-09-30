package com.github.theredbrain.scriptblocks.util;

import com.github.theredbrain.scriptblocks.ScriptBlocks;
import com.github.theredbrain.scriptblocks.datafixer.JigsawPlacerBlockEntityIdFix;
import com.mojang.datafixers.schemas.Schema;
import me.bjtmastermind.easy_data_fix.api.DataFixerRegistry;
import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint;
import net.minecraft.datafixer.schema.IdentifierNormalizingSchema;

public class ScriptBlocksDataFixer implements PreLaunchEntrypoint {
	@Override
	public void onPreLaunch() {
		ScriptBlocks.info("Running DataFixer!");
		DataFixerRegistry.addDataFix("Change JigsawPlacerBlockEntity ID", builder -> {
			// dataVersion = The Minecraft data version used in the version your upgrading to.
			// SchemaVersion = The builtin schema class that goes with the data version.
			// Example: (4773, V4771::new) = 26.1 full release
			Schema schema = builder.addSchema(3945, IdentifierNormalizingSchema::new);
			builder.addFixer(new JigsawPlacerBlockEntityIdFix(schema, false));
		});
	}
}
