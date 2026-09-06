package com.bettertoppi.topuhud;

import com.bettertoppi.topuhud.config.ConfigManager;
import com.bettertoppi.topuhud.hud.TopuUtilityHud;
import com.bettertoppi.topuhud.modmenu.TopuHudScreen;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

public final class TopuHudClient implements ClientModInitializer {
    private static final KeyMapping.Category CATEGORY=KeyMapping.Category.register(Identifier.fromNamespaceAndPath("topuhud","main"));
    private static final KeyMapping MENU=KeyMappingHelper.registerKeyMapping(new KeyMapping("key.topuhud.menu",InputConstants.Type.KEYSYM,GLFW.GLFW_KEY_RIGHT_SHIFT,CATEGORY));
    @Override public void onInitializeClient(){
        ConfigManager.load();
        TopuUtilityHud.initialize();
        ClientTickEvents.END_CLIENT_TICK.register(client->{while(MENU.consumeClick())client.setScreenAndShow(new TopuHudScreen(client.gui.screen()));});
    }
}
