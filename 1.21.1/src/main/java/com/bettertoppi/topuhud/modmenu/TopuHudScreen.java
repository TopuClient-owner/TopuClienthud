package com.bettertoppi.topuhud.modmenu;

import com.bettertoppi.topuhud.config.ConfigManager;
import com.bettertoppi.topuhud.config.TopuHudConfig;
import com.bettertoppi.topuhud.hud.HudManager;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public final class TopuScreenEditor extends Screen {

    private final Screen parent;

    private final TopuHudConfig config;

    private HudManager.Id dragging;

    private int dragOffsetX;
    private int dragOffsetY;

    private boolean draggingMouse;

    public TopuScreenEditor(Screen parent) {
        super(Text.literal("Topu HUD Editor"));

        this.parent = parent;
        this.config = ConfigManager.get();
    }

    @Override
    protected void init() {

        int centerX = this.width / 2;

        this.addDrawableChild(
                ButtonWidget.builder(
                        Text.literal("Done"),
                        button -> closeEditor()
                ).dimensions(
                        centerX - 50,
                        this.height - 30,
                        100,
                        20
                ).build()
        );
    }

    private void closeEditor() {

        dragging = null;
        draggingMouse = false;

        ConfigManager.save();

        MinecraftClient.getInstance()
                .setScreen(parent);
    }

    @Override
    public void close() {
        closeEditor();
    }

    // ============================================================
    // MOUSE
    // ============================================================

    @Override
    public boolean mouseClicked(
            double mouseX,
            double mouseY,
            int button
    ) {

        if (button != 0) {
            return super.mouseClicked(
                    mouseX,
                    mouseY,
                    button
            );
        }

        HudManager.Id[] ids =
                HudManager.Id.values();

        /*
         * Iterate backwards so the last HUD element
         * is treated as being on top.
         */
        for (int i = ids.length - 1; i >= 0; i--) {

            HudManager.Id id = ids[i];

            if (!HudManager.isEnabledForEditor(id)) {
                continue;
            }

            int[] position =
                    HudManager.getPositionForEditor(
                            config,
                            id
                    );

            int width =
                    HudManager.getWidthForEditor(id);

            int height =
                    HudManager.getHeightForEditor(id);

            if (mouseX >= position[0]
                    && mouseX <= position[0] + width
                    && mouseY >= position[1]
                    && mouseY <= position[1] + height) {

                dragging = id;

                dragOffsetX =
                        (int) mouseX - position[0];

                dragOffsetY =
                        (int) mouseY - position[1];

                draggingMouse = true;

                /*
                 * Returning true prevents Minecraft's
                 * normal screen handling from processing
                 * this click.
                 */
                return true;
            }
        }

        return super.mouseClicked(
                mouseX,
                mouseY,
                button
        );
    }

    @Override
    public boolean mouseDragged(
            double mouseX,
            double mouseY,
            int button,
            double deltaX,
            double deltaY
    ) {

        if (button != 0) {
            return false;
        }

        if (dragging == null) {
            return false;
        }

        int newX =
                (int) mouseX -
                        dragOffsetX;

        int newY =
                (int) mouseY -
                        dragOffsetY;

        /*
         * Keep the HUD inside the screen.
         */
        int width =
                HudManager.getWidthForEditor(
                        dragging
                );

        int height =
                HudManager.getHeightForEditor(
                        dragging
                );

        newX = Math.max(
                0,
                Math.min(
                        newX,
                        this.width - width
                )
        );

        newY = Math.max(
                0,
                Math.min(
                        newY,
                        this.height - height
                )
        );

        HudManager.setPositionForEditor(
                config,
                dragging,
                newX,
                newY
        );

        draggingMouse = true;

        return true;
    }

    @Override
    public boolean mouseReleased(
            double mouseX,
            double mouseY,
            int button
    ) {

        if (button == 0 && dragging != null) {

            dragging = null;
            draggingMouse = false;

            ConfigManager.save();

            return true;
        }

        return super.mouseReleased(
                mouseX,
                mouseY,
                button
        );
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
         * Dark editor background.
         */
        drawContext.fill(
                0,
                0,
                this.width,
                this.height,
                0xAA101010
        );

        /*
         * Header.
         */
        drawContext.fill(
                0,
                0,
                this.width,
                28,
                0xEE181818
        );

        drawContext.drawText(
                this.textRenderer,
                Text.literal("TOPU HUD EDITOR"),
                10,
                9,
                0xFFFFFF,
                true
        );

        /*
         * Instructions.
         */
        drawContext.drawText(
                this.textRenderer,
                Text.literal(
                        "Drag HUD elements • Release mouse to place"
                ),
                10,
                36,
                0xAAAAAA,
                false
        );

        /*
         * Draw HUD editor boxes.
         */
        for (HudManager.Id id :
                HudManager.Id.values()) {

            if (!HudManager.isEnabledForEditor(id)) {
                continue;
            }

            int[] position =
                    HudManager.getPositionForEditor(
                            config,
                            id
                    );

            int width =
                    HudManager.getWidthForEditor(id);

            int height =
                    HudManager.getHeightForEditor(id);

            boolean selected =
                    dragging == id;

            int borderColor =
                    selected
                            ? 0xFF00FF88
                            : 0xFF777777;

            /*
             * Background.
             */
            drawContext.fill(
                    position[0],
                    position[1],
                    position[0] + width,
                    position[1] + height,
                    selected
                            ? 0x5533AA77
                            : 0x55222222
            );

            /*
             * Border.
             */
            drawBorder(
                    drawContext,
                    position[0],
                    position[1],
                    width,
                    height,
                    borderColor
            );

            /*
             * Module name.
             */
            drawContext.drawText(
                    this.textRenderer,
                    getDisplayName(id),
                    position[0] + 4,
                    position[1] + 4,
                    selected
                            ? 0x00FF88
                            : 0xFFFFFF,
                    true
            );
        }

        /*
         * Let Minecraft render the Done button.
         */
        super.render(
                drawContext,
                mouseX,
                mouseY,
                delta
        );
    }

    // ============================================================
    // BORDER
    // ============================================================

    private void drawBorder(
            DrawContext drawContext,
            int x,
            int y,
            int width,
            int height,
            int color
    ) {

        drawContext.fill(
                x,
                y,
                x + width,
                y + 1,
                color
        );

        drawContext.fill(
                x,
                y + height - 1,
                x + width,
                y + height,
                color
        );

        drawContext.fill(
                x,
                y,
                x + 1,
                y + height,
                color
        );

        drawContext.fill(
                x + width - 1,
                y,
                x + width,
                y + height,
                color
        );
    }

    // ============================================================
    // DISPLAY NAMES
    // ============================================================

    private Text getDisplayName(
            HudManager.Id id
    ) {

        return switch (id) {

            case ARMOR ->
                    Text.literal("Armor");

            case FPS ->
                    Text.literal("FPS");

            case PING ->
                    Text.literal("Ping");

            case TPS ->
                    Text.literal("TPS");

            case CPS ->
                    Text.literal("CPS");

            case COMBO ->
                    Text.literal("Combo");

            case TOTEM ->
                    Text.literal("Totems");

            case POTION ->
                    Text.literal("Potions");

            case EFFECTS ->
                    Text.literal("Effects");

            case GAPPLE ->
                    Text.literal("Gapples");

            case WARNING ->
                    Text.literal("Armor Warning");

            case ENEMY ->
                    Text.literal("Enemy HP");

            case COOLDOWN ->
                    Text.literal("Attack Cooldown");
        };
    }
}
