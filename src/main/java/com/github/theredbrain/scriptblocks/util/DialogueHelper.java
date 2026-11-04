package com.github.theredbrain.scriptblocks.util;

import com.github.theredbrain.scriptblocks.data.Dialogue;
import com.github.theredbrain.scriptblocks.data.Dialogue;
import com.github.theredbrain.scriptblocks.data.DialogueAnswer;
import com.github.theredbrain.scriptblocks.registry.ScriptBlocksDynamicRegistries;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

public class DialogueHelper {

	@Nullable
	public static Dialogue getDialogue(RegistryAccess registryAccess, Identifier identifier) {
		Dialogue dialogue = null;
		Optional<Holder.Reference<Registry<Dialogue>>> optionalDialogueRegistryReference = registryAccess.get(ScriptBlocksDynamicRegistries.DIALOGUE_REGISTRY_KEY);
		if (optionalDialogueRegistryReference.isPresent()) {
			Optional<Holder.Reference<Dialogue>> optionalDialogueReference = optionalDialogueRegistryReference.get().value().get(identifier);
			if (optionalDialogueReference.isPresent()) {
				dialogue = optionalDialogueReference.get().value();
			}
		}
		return dialogue;
	}

	@Nullable
	public static DialogueAnswer getDialogueAnswer(RegistryAccess registryAccess, Identifier identifier) {
		DialogueAnswer dialogueAnswer = null;
		Optional<Holder.Reference<Registry<DialogueAnswer>>> optionalDialogueAnswerRegistryReference = registryAccess.get(ScriptBlocksDynamicRegistries.DIALOGUE_ANSWER_REGISTRY_KEY);
		if (optionalDialogueAnswerRegistryReference.isPresent()) {
			Optional<Holder.Reference<DialogueAnswer>> optionalDialogueAnswerReference = optionalDialogueAnswerRegistryReference.get().value().get(identifier);
			if (optionalDialogueAnswerReference.isPresent()) {
				dialogueAnswer = optionalDialogueAnswerReference.get().value();
			}
		}
		return dialogueAnswer;
	}
}
