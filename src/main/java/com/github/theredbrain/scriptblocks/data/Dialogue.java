package com.github.theredbrain.scriptblocks.data;

import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

public final class Dialogue {
    private final List<String> dialogueTextList;
    private final List<String> answerList;
    private final String unlockAdvancement;
    private final String lockAdvancement;
    private final boolean cancellable;

    public Dialogue(List<String> dialogueTextList, List<String> answerList, String unlockAdvancement, String lockAdvancement, boolean cancellable) {
        this.dialogueTextList = dialogueTextList;
        this.answerList = answerList;
        this.unlockAdvancement = unlockAdvancement;
        this.lockAdvancement = lockAdvancement;
        this.cancellable = cancellable;
    }

    public List<String> getDialogueTextList() {
        return this.dialogueTextList;
    }

    public List<Identifier> getAnswerList() {
        List<Identifier> answerIdentifiersList = new ArrayList<>();
        for (String answer : this.answerList) {
            if (Identifier.isValid(answer)) {
                answerIdentifiersList.add(new Identifier(answer));
            }
        }
        return answerIdentifiersList;
    }

    public String getUnlockAdvancement() {
        if (Identifier.isValid(this.unlockAdvancement)) {
            return this.unlockAdvancement;
        }
        return "";
    }

    public String getLockAdvancement() {
        if (Identifier.isValid(this.lockAdvancement)) {
            return this.lockAdvancement;
        }
        return "";
    }

    public boolean isCancellable() {
        return this.cancellable;
    }
}
