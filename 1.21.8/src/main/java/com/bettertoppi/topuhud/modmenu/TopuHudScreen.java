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
         * EDIT HUD
         */
        this.addDrawableChild(
                ButtonWidget.builder(
                        Text.literal("Edit HUD"),
                        button -> openEditor()
                ).dimensions(
                        centerX - 75,
                        50,
                        150,
                        20
                ).build()
        );

        /*
         * HUD MODULE BUTTONS
         */
        int startY = 85;
        int rowHeight = 24;

        addToggleButton(
                "Armor",
                centerX - 160,
                startY,
                () -> config.armorHud,
                value -> config.armorHud = value
        );

        addToggleButton(
                "FPS",
                centerX + 10,
                startY,
                () -> config.fpsCounter,
                value -> config.fpsCounter = value
        );

        addToggleButton(
                "Ping",
                centerX - 160,
                startY + rowHeight,
                () -> config.pingDisplay,
                value -> config.pingDisplay = value
        );

        addToggleButton(
                "TPS",
                centerX + 10,
                startY + rowHeight,
                () -> config.tpsDisplay,
                value -> config.tpsDisplay = value
        );

        addToggleButton(
                "CPS",
                centerX - 160,
                startY + rowHeight * 2,
                () -> config.cpsDisplay,
                value -> config.cpsDisplay = value
        );

        addToggleButton(
                "Combo",
                centerX + 10,
                startY + rowHeight * 2,
                () -> config.comboCounter,
                value -> config.comboCounter = value
        );

        addToggleButton(
                "Totems",
                centerX - 160,
                startY + rowHeight * 3,
                () -> config.totemCounter,
                value -> config.totemCounter = value
        );

        addToggleButton(
                "Potions",
                centerX + 10,
                startY + rowHeight * 3,
                () -> config.potionCounter,
                value -> config.potionCounter = value
        );

        addToggleButton(
                "Effects",
                centerX - 160,
                startY + rowHeight * 4,
                () -> config.potionEffects,
                value -> config.potionEffects = value
        );

        addToggleButton(
                "Gapples",
                centerX + 10,
                startY + rowHeight * 4,
                () -> config.gappleCounter,
                value -> config.gappleCounter = value
        );

        addToggleButton(
                "Armor Warning",
                centerX - 160,
                startY + rowHeight * 5,
                () -> config.armorWarning,
                value -> config.armorWarning = value
        );

        addToggleButton(
                "Enemy HP",
                centerX + 10,
                startY + rowHeight * 5,
                () -> config.enemyHealth,
                value -> config.enemyHealth = value
        );

        addToggleButton(
                "Attack Cooldown",
                centerX - 160,
                startY + rowHeight * 6,
                () -> config.cooldown,
                value -> config.cooldown = value
        );

        /*
         * AUTO SPRINT
         */
        addToggleButton(
                "Auto Sprint",
                centerX + 10,
                startY + rowHeight * 6,
                () -> config.autoSprint,
                value -> config.autoSprint = value
        );

        /*
         * TOGGLE SNEAK
         */
        addToggleButton(
                "Toggle Sneak",
                centerX - 160,
                startY + rowHeight * 7,
                () -> config.toggleSneak,
                value -> config.toggleSneak = value
        );

        /*
         * DONE
         */
        this.addDrawableChild(
                ButtonWidget.builder(
                        Text.literal("Done"),
                        button -> closeScreen()
                ).dimensions(
                        centerX - 75,
                        this.height - 30,
                        150,
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
            int x,
            int y,
            BooleanSupplier getter,
            BooleanConsumer setter
    ) {

        ButtonWidget button =
                ButtonWidget.builder(
                        getToggleText(name, getter.getAsBoolean()),
                        clicked -> {

                            boolean newValue =
                                    !getter.getAsBoolean();

                            setter.accept(newValue);

                            clicked.setMessage(
                                    getToggleText(
                                            name,
                                            newValue
                                    )
                            );

                            ConfigManager.save();
                        }
                ).dimensions(
                        x,
                        y,
                        150,
                        20
                ).build();

        this.addDrawableChild(button);
    }

    private Text getToggleText(
            String name,
            boolean enabled
    ) {

        return Text.literal(
                name +
                        ": " +
                        (enabled
                                ? "ON"
                                : "OFF")
        );
    }

    /*
     * ============================================================
     * EDITOR
     * ============================================================
     */

    private void openEditor() {

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
         * Background
         */
        drawContext.fill(
                0,
                0,
                this.width,
                this.height,
                0xCC101010
        );

        /*
         * Header
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
         * Description
         */
        drawContext.drawText(
                this.textRenderer,
                Text.literal(
                        "Enable or disable HUD modules"
                ),
                10,
                36,
                0xAAAAAA,
                false
        );

        /*
         * Render buttons
         */
        super.render(
                drawContext,
                mouseX,
                mouseY,
                delta
        );
    }

    /*
     * ============================================================
     * SMALL FUNCTIONAL INTERFACES
     * ============================================================
     */

    @FunctionalInterface
    private interface BooleanSupplier {

        boolean getAsBoolean();
    }

    @FunctionalInterface
    private interface BooleanConsumer {

        void accept(boolean value);
    }
}
