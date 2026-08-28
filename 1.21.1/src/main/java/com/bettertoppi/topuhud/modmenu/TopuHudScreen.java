
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

    @Override
    protected void init() {

        int centerX = this.width / 2;

        /*
         * ========================================================
         * EDIT HUD
         * ========================================================
         */

        this.addDrawableChild(
                ButtonWidget.builder(
                        Text.literal("Edit HUD"),
                        button -> openEditor()
                ).dimensions(
                        centerX - 100,
                        55,
                        200,
                        20
                ).build()
        );

        /*
         * ========================================================
         * HUD TOGGLES
         * ========================================================
         */

        int startY = 90;
        int spacing = 24;

        addToggleButton(
                "Armor HUD",
                config.armorHud,
                centerX - 100,
                startY,
                value -> config.armorHud = value
        );

        addToggleButton(
                "FPS Counter",
                config.fpsCounter,
                centerX - 100,
                startY + spacing,
                value -> config.fpsCounter = value
        );

        addToggleButton(
                "Ping",
                config.pingDisplay,
                centerX - 100,
                startY + spacing * 2,
                value -> config.pingDisplay = value
        );

        addToggleButton(
                "CPS",
                config.cpsDisplay,
                centerX - 100,
                startY + spacing * 3,
                value -> config.cpsDisplay = value
        );

        addToggleButton(
                "Combo",
                config.comboCounter,
                centerX - 100,
                startY + spacing * 4,
                value -> config.comboCounter = value
        );

        addToggleButton(
                "Totems",
                config.totemCounter,
                centerX - 100,
                startY + spacing * 5,
                value -> config.totemCounter = value
        );

        /*
         * ========================================================
         * NEW FEATURES
         * ========================================================
         */

        addToggleButton(
                "Block Overlay",
                config.blockOverlay,
                centerX + 10,
                startY,
                value -> config.blockOverlay = value
        );

        addToggleButton(
                "Crosshair Customizer",
                config.crosshairCustomizer,
                centerX + 10,
                startY + spacing,
                value -> config.crosshairCustomizer = value
        );

        addToggleButton(
                "Keystrokes",
                config.keystrokes,
                centerX + 10,
                startY + spacing * 2,
                value -> config.keystrokes = value
        );

        addToggleButton(
                "Memory",
                config.memoryDisplay,
                centerX + 10,
                startY + spacing * 3,
                value -> config.memoryDisplay = value
        );

        /*
         * ========================================================
         * DONE
         * ========================================================
         */

        this.addDrawableChild(
                ButtonWidget.builder(
                        Text.literal("Done"),
                        button -> closeScreen()
                ).dimensions(
                        centerX - 100,
                        this.height - 35,
                        200,
                        20
                ).build()
        );
    }

    /*
     * ============================================================
     * TOGGLE BUTTON
     * ============================================================
     */

    private void addToggleButton(
            String name,
            boolean enabled,
            int x,
            int y,
            java.util.function.Consumer<Boolean> setter
    ) {

        ButtonWidget button =
                ButtonWidget.builder(
                        Text.literal(
                                name + ": " +
                                        (enabled
                                                ? "ON"
                                                : "OFF")
                        ),
                        b -> {

                            boolean newValue =
                                    !getCurrentValue(
                                            name
                                    );

                            setter.accept(
                                    newValue
                            );

                            b.setMessage(
                                    Text.literal(
                                            name +
                                                    ": " +
                                                    (newValue
                                                            ? "ON"
                                                            : "OFF")
                                    )
                            );

                            ConfigManager.save();
                        }
                ).dimensions(
                        x,
                        y,
                        90,
                        20
                ).build();

        this.addDrawableChild(button);
    }

    /*
     * ============================================================
     * CURRENT VALUE
     * ============================================================
     *
     * Used by toggle buttons.
     */

    private boolean getCurrentValue(
            String name
    ) {

        return switch (name) {

            case "Armor HUD" ->
                    config.armorHud;

            case "FPS Counter" ->
                    config.fpsCounter;

            case "Ping" ->
                    config.pingDisplay;

            case "CPS" ->
                    config.cpsDisplay;

            case "Combo" ->
                    config.comboCounter;

            case "Totems" ->
                    config.totemCounter;

            case "Block Overlay" ->
                    config.blockOverlay;

            case "Crosshair Customizer" ->
                    config.crosshairCustomizer;

            case "Keystrokes" ->
                    config.keystrokes;

            case "Memory" ->
                    config.memoryDisplay;

            default ->
                    false;
        };
    }

    /*
     * ============================================================
     * OPEN EDITOR
     * ============================================================
     */

    private void openEditor() {

        ConfigManager.save();

        MinecraftClient.getInstance()
                .setScreen(
                        new TopuScreenEditor(this)
                );
    }

    /*
     * ============================================================
     * CLOSE
     * ============================================================
     */

    private void closeScreen() {

        ConfigManager.save();

        MinecraftClient.getInstance()
                .setScreen(parent);
    }

    @Override
    public void close() {
        closeScreen();
    }

    /*
     * ============================================================
     * RENDER
     * ============================================================
     */

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
                38,
                0xEE181818
        );

        /*
         * Title.
         */
        drawContext.drawText(
                this.textRenderer,
                Text.literal("TOPU HUD"),
                10,
                12,
                0xFFFFFF,
                true
        );

        /*
         * Description.
         */
        drawContext.drawText(
                this.textRenderer,
                Text.literal(
                        "Configure your Topu HUD modules"
                ),
                10,
                48,
                0xAAAAAA,
                false
        );

        /*
         * Section labels.
         */

        int centerX =
                this.width / 2;

        drawContext.drawText(
                this.textRenderer,
                Text.literal("HUD MODULES"),
                centerX - 100,
                78,
                0x00FF88,
                true
        );

        drawContext.drawText(
                this.textRenderer,
                Text.literal("NEW FEATURES"),
                centerX + 10,
                78,
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

