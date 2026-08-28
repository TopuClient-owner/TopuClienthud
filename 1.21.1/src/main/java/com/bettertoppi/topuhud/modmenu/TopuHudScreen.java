package com.bettertoppi.topuhud.modmenu;

import com.bettertoppi.topuhud.config.ConfigManager;
import com.bettertoppi.topuhud.config.TopuHudConfig;
import com.bettertoppi.topuhud.hud.HudManager;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public final class TopuHudScreen extends Screen {

private final Screen parent;
private final TopuHudConfig config;

public TopuHudScreen(Screen parent) {
    super(Text.literal("Topu HUD"));

    this.parent = parent;
    this.config = ConfigManager.get();
}

@Override
protected void init() {

    int centerX = this.width / 2;

    /*
     * Open the HUD editor.
     */
    this.addDrawableChild(
            ButtonWidget.builder(
                    Text.literal("Edit HUD"),
                    button -> openEditor()
            ).dimensions(
                    centerX - 75,
                    this.height / 2 - 35,
                    150,
                    20
            ).build()
    );

    /*
     * Done button.
     */
    this.addDrawableChild(
            ButtonWidget.builder(
                    Text.literal("Done"),
                    button -> closeScreen()
            ).dimensions(
                    centerX - 75,
                    this.height / 2,
                    150,
                    20
            ).build()
    );
}

private void openEditor() {

    MinecraftClient.getInstance()
            .setScreen(
                    new TopuScreenEditor(this)
            );
}

private void closeScreen() {

    ConfigManager.save();

    MinecraftClient.getInstance()
            .setScreen(parent);
}

@Override
public void close() {
    closeScreen();
}

@Override
public void render(
        DrawContext drawContext,
        int mouseX,
        int mouseY,
        float delta
) {

    /*
     * Background.
     */
    drawContext.fill(
            0,
            0,
            this.width,
            this.height,
            0xCC101010
    );

    /*
     * Header.
     */
    drawContext.fill(
            0,
            0,
            this.width,
            32,
            0xEE181818
    );

    drawContext.drawText(
            this.textRenderer,
            Text.literal("TOPU HUD"),
            10,
            10,
            0xFFFFFF,
            true
    );

    /*
     * Description.
     */
    drawContext.drawText(
            this.textRenderer,
            Text.literal(
                    "Configure and position your Topu HUD modules"
            ),
            10,
            45,
            0xAAAAAA,
            false
    );

    /*
     * Status information.
     */
    int y = 75;

    drawContext.drawText(
            this.textRenderer,
            Text.literal(
                    "HUD modules can be moved using Edit HUD."
            ),
            10,
            y,
            0xDDDDDD,
            false
    );

    /*
     * Render buttons.
     */
    super.render(
            drawContext,
            mouseX,
            mouseY,
            delta
    );
}

}
