package com.bettertoppi.topuhud.modmenu;

import com.bettertoppi.topuhud.config.ConfigManager;
import com.bettertoppi.topuhud.config.TopuHudConfig;

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

// ============================================================
// INIT
// ============================================================

@Override
protected void init() {

    int centerX = this.width / 2;

    /*
     * --------------------------------------------------------
     * EDIT HUD
     * --------------------------------------------------------
     */

    this.addDrawableChild(
            ButtonWidget.builder(
                    Text.literal("Edit HUD"),
                    button -> openEditor()
            ).dimensions(
                    centerX - 75,
                    this.height - 70,
                    150,
                    20
            ).build()
    );

    /*
     * --------------------------------------------------------
     * DONE
     * --------------------------------------------------------
     */

    this.addDrawableChild(
            ButtonWidget.builder(
                    Text.literal("Done"),
                    button -> closeScreen()
            ).dimensions(
                    centerX - 75,
                    this.height - 40,
                    150,
                    20
            ).build()
    );

    /*
     * --------------------------------------------------------
     * HUD MODULE TOGGLES
     * --------------------------------------------------------
     */

    int startY = 75;

    int leftX = centerX - 155;
    int rightX = centerX + 5;

    int row = 0;

    /*
     * Armor
     */
    addToggleButton(
            "Armor HUD",
            leftX,
            startY + row * 24,
            config.armorHud,
            value -> config.armorHud = value
    );

    /*
     * FPS
     */
    addToggleButton(
            "FPS Counter",
            rightX,
            startY + row * 24,
            config.fpsCounter,
            value -> config.fpsCounter = value
    );

    row++;

    /*
     * Ping
     */
    addToggleButton(
            "Ping",
            leftX,
            startY + row * 24,
            config.pingDisplay,
            value -> config.pingDisplay = value
    );

    /*
     * TPS
     */
    addToggleButton(
            "TPS",
            rightX,
            startY + row * 24,
            config.tpsDisplay,
            value -> config.tpsDisplay = value
    );

    row++;

    /*
     * CPS
     */
    addToggleButton(
            "CPS",
            leftX,
            startY + row * 24,
            config.cpsDisplay,
            value -> config.cpsDisplay = value
    );

    /*
     * Combo
     */
    addToggleButton(
            "Combo",
            rightX,
            startY + row * 24,
            config.comboCounter,
            value -> config.comboCounter = value
    );

    row++;

    /*
     * Totems
     */
    addToggleButton(
            "Totems",
            leftX,
            startY + row * 24,
            config.totemCounter,
            value -> config.totemCounter = value
    );

    /*
     * Potions
     */
    addToggleButton(
            "Potions",
            rightX,
            startY + row * 24,
            config.potionCounter,
            value -> config.potionCounter = value
    );

    row++;

    /*
     * Potion Effects
     */
    addToggleButton(
            "Potion Effects",
            leftX,
            startY + row * 24,
            config.potionEffects,
            value -> config.potionEffects = value
    );

    /*
     * Gapples
     */
    addToggleButton(
            "Gapples",
            rightX,
            startY + row * 24,
            config.gappleCounter,
            value -> config.gappleCounter = value
    );

    row++;

    /*
     * Armor Warning
     */
    addToggleButton(
            "Armor Warning",
            leftX,
            startY + row * 24,
            config.armorWarning,
            value -> config.armorWarning = value
    );

    /*
     * Enemy HP
     */
    addToggleButton(
            "Enemy HP",
            rightX,
            startY + row * 24,
            config.enemyHealth,
            value -> config.enemyHealth = value
    );

    row++;

    /*
     * Attack Cooldown
     */
    addToggleButton(
            "Attack Cooldown",
            leftX,
            startY + row * 24,
            config.cooldown,
            value -> config.cooldown = value
    );
}

// ============================================================
// TOGGLE BUTTON
// ============================================================

private void addToggleButton(
        String name,
        int x,
        int y,
        boolean enabled,
        java.util.function.Consumer<Boolean> setter
) {

    ButtonWidget button =
            ButtonWidget.builder(
                    Text.literal(
                            name +
                                    ": " +
                                    (enabled
                                            ? "ON"
                                            : "OFF")
                    ),
                    clicked -> {

                        /*
                         * Toggle the value.
                         */
                        boolean newValue =
                                !isButtonCurrentlyEnabled(
                                        name
                                );

                        setter.accept(
                                newValue
                        );

                        /*
                         * Save immediately.
                         */
                        ConfigManager.save();

                        /*
                         * Rebuild the screen so
                         * the button text updates.
                         */
                        clearAndInit();
                    }
            ).dimensions(
                    x,
                    y,
                    150,
                    20
            ).build();

    this.addDrawableChild(button);
}

// ============================================================
// GET CURRENT VALUE
// ============================================================

private boolean isButtonCurrentlyEnabled(
        String name
) {

    return switch (name) {

        case "Armor HUD" ->
                config.armorHud;

        case "FPS Counter" ->
                config.fpsCounter;

        case "Ping" ->
                config.pingDisplay;

        case "TPS" ->
                config.tpsDisplay;

        case "CPS" ->
                config.cpsDisplay;

        case "Combo" ->
                config.comboCounter;

        case "Totems" ->
                config.totemCounter;

        case "Potions" ->
                config.potionCounter;

        case "Potion Effects" ->
                config.potionEffects;

        case "Gapples" ->
                config.gappleCounter;

        case "Armor Warning" ->
                config.armorWarning;

        case "Enemy HP" ->
                config.enemyHealth;

        case "Attack Cooldown" ->
                config.cooldown;

        default ->
                false;
    };
}

// ============================================================
// OPEN EDITOR
// ============================================================

private void openEditor() {

    MinecraftClient.getInstance()
            .setScreen(
                    new TopuScreenEditor(this)
            );
}

// ============================================================
// CLOSE
// ============================================================

private void closeScreen() {

    ConfigManager.save();

    MinecraftClient.getInstance()
            .setScreen(parent);
}

@Override
public void close() {
    closeScreen();
}

// ============================================================
// RENDER
// ============================================================

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

    /*
     * Title.
     */
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
                    "Enable or disable modules, then use Edit HUD to move them."
            ),
            10,
            45,
            0xAAAAAA,
            false
    );

    /*
     * Module status.
     */
    drawContext.drawText(
            this.textRenderer,
            Text.literal(
                    "HUD MODULES"
            ),
            this.width / 2 - 45,
            62,
            0x00FF88,
            true
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
