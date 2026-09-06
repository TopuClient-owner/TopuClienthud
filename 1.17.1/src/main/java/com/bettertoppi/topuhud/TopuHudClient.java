package com.bettertoppi.topuhud;

import com.bettertoppi.topuhud.config.ConfigManager;
import com.bettertoppi.topuhud.hud.HudManager;
import com.bettertoppi.topuhud.hud.TopuUtilityHud;
import net.fabricmc.api.ClientModInitializer;

public final class TopuHudClient implements ClientModInitializer {
    @Override public void onInitializeClient() {
        ConfigManager.load();
        HudManager.initialize();
        TopuUtilityHud.initialize();
    }
}
