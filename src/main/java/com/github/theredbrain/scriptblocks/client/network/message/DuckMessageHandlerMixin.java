package com.github.theredbrain.scriptblocks.client.network.message;

import net.minecraft.text.Text;

public interface DuckMessageHandlerMixin {
    void scriptblocks$onAnnouncement(Text announcement);
}
