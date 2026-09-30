package com.github.theredbrain.scriptblocks.datafixer;
//
//import com.google.common.collect.Maps;
//import com.mojang.datafixers.DataFix;
//import com.mojang.datafixers.DataFixUtils;
//import com.mojang.datafixers.TypeRewriteRule;
//import com.mojang.datafixers.schemas.Schema;
//import com.mojang.datafixers.types.Type;
//import com.mojang.datafixers.types.templates.TaggedChoice;
//import net.minecraft.datafixer.TypeReferences;
//
//import java.util.Map;
//
// TODO 26.1.2
//public class JigsawPlacerBlockEntityIdFix extends DataFix {
//	private static final Map<String, String> RENAMED_BLOCK_ENTITIES = DataFixUtils.make(Maps.<String, String>newHashMap(), map -> {
//		map.put("scriptblocks:structure_placer_block", "scriptblocks:jigsaw_placer_block");
//	});
//
//	public JigsawPlacerBlockEntityIdFix(Schema outputSchema, boolean changesType) {
//		super(outputSchema, changesType);
//	}
//
//	@Override
//	public TypeRewriteRule makeRule() {
//		Type<?> type = this.getInputSchema().getType(TypeReferences.ITEM_STACK);
//		Type<?> type2 = this.getOutputSchema().getType(TypeReferences.ITEM_STACK);
//		TaggedChoice.TaggedChoiceType<String> taggedChoiceType = (TaggedChoice.TaggedChoiceType<String>)this.getInputSchema().findChoiceType(TypeReferences.BLOCK_ENTITY);
//		TaggedChoice.TaggedChoiceType<String> taggedChoiceType2 = (TaggedChoice.TaggedChoiceType<String>)this.getOutputSchema().findChoiceType(TypeReferences.BLOCK_ENTITY);
//		return TypeRewriteRule.seq(
//				this.convertUnchecked("item stack block entity name hook converter", type, type2),
//				this.fixTypeEverywhere(
//						"JigsawPlacerBlockEntityIdFix",
//						taggedChoiceType,
//						taggedChoiceType2,
//						dynamicOps -> pair -> pair.mapFirst(oldName -> (String)RENAMED_BLOCK_ENTITIES.getOrDefault(oldName, oldName))
//				)
//		);
//	}
//}
