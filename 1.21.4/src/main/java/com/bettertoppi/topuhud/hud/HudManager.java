package com.bettertoppi.topuhud.hud;

import com.bettertoppi.topuhud.config.ConfigManager;
import com.bettertoppi.topuhud.config.TopuHudConfig;
import com.bettertoppi.topuhud.modmenu.TopuHudScreen;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

import net.minecraft.text.Text;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.MathHelper;

import org.lwjgl.glfw.GLFW;

import java.util.ArrayDeque;
import java.util.Deque;

public final class HudManager {

    private static KeyBinding menuKey;
    private static KeyBinding editKey;
    private static KeyBinding sneakKey;

    private static boolean sneakToggled;
    private static boolean editMode;
    private static boolean leftMouseWasDown;

    private static HudId draggingHud;
    private static int dragOffsetX;
    private static int dragOffsetY;

    private static final Deque<Long> clickTimes =
            new ArrayDeque<>();

    private static long lastHitTime;
    private static int combo;

    private static double tpsEstimate = 20.0;
    private static long lastTickNanos = System.nanoTime();

    private enum HudId {
        ARMOR,
        FPS,
        PING,
        TPS,
        CPS,
        COMBO,
        TOTEM,
        POTION,
        EFFECTS,
        GAPPLE,
        WARNING,
        ENEMY,
        COOLDOWN
    }

    private HudManager() {
    }

    // ============================================================
    // INITIALIZATION
    // ============================================================

    public static void initialize() {

        menuKey = KeyBindingHelper.registerKeyBinding(
                new KeyBinding(
                        "key.topuhud.menu",
                        InputUtil.Type.KEYSYM,
                        GLFW.GLFW_KEY_RIGHT_SHIFT,
                        "category.topuhud"
                )
        );

        editKey = KeyBindingHelper.registerKeyBinding(
                new KeyBinding(
                        "key.topuhud.edit",
                        InputUtil.Type.KEYSYM,
                        GLFW.GLFW_KEY_RIGHT_CONTROL,
                        "category.topuhud"
                )
        );

        sneakKey = KeyBindingHelper.registerKeyBinding(
                new KeyBinding(
                        "key.topuhud.sneak",
                        InputUtil.Type.KEYSYM,
                        GLFW.GLFW_KEY_RIGHT_ALT,
                        "category.topuhud"
                )
        );

        ClientTickEvents.END_CLIENT_TICK.register(
                HudManager::tick
        );

        /*
         * Fabric API 1.21.4 HudRenderCallback:
         * onHudRender(DrawContext, RenderTickCounter)
         */
        HudRenderCallback.EVENT.register(
                (drawContext, tickCounter) ->
                        HudManager.render(
                                MinecraftClient.getInstance(),
                                drawContext,
                                tickCounter.getTickDelta(true)
                        )
        );
    }

    public static void setMenuOpen(boolean value) {
        // Menu state is controlled by Minecraft's current screen.
    }

    public static void setEditMode(boolean value) {

        editMode = value;

        ConfigManager.get().editMode = value;

        if (!value) {
            draggingHud = null;
            ConfigManager.save();
        }
    }

    public static void registerClick() {
        clickTimes.addLast(
                System.currentTimeMillis()
        );
    }

    public static void registerHit() {

        registerClick();

        lastHitTime = System.currentTimeMillis();
        combo++;
    }

    // ============================================================
    // CLIENT TICK
    // ============================================================

    private static void tick(MinecraftClient client) {

        if (menuKey != null) {

            while (menuKey.wasPressed()) {

                if (client.currentScreen == null) {

                    client.setScreen(
                            new TopuHudScreen(null)
                    );
                }
            }
        }

        if (editKey != null) {

            while (editKey.wasPressed()) {

                setEditMode(
                        !editMode
                );
            }
        }

        if (sneakKey != null) {

            while (sneakKey.wasPressed()) {

                sneakToggled =
                        !sneakToggled;
            }
        }

        if (client.player == null)
            return;

        TopuHudConfig config =
                ConfigManager.get();

        // ========================================================
        // TOGGLE SNEAK
        // ========================================================

        if (config.toggleSneak) {

            client.player.setSneaking(
                    sneakToggled
            );
        }

        // ========================================================
        // AUTO SPRINT
        // ========================================================

        if (config.autoSprint &&
                client.currentScreen == null &&
                client.options.forwardKey.isPressed() &&
                !client.player.isSneaking() &&
                client.player.getHungerManager()
                        .getFoodLevel() > 6) {

            client.player.setSprinting(true);
        }

        // ========================================================
        // TPS ESTIMATE
        // ========================================================

        long now = System.nanoTime();

        long delta =
                now - lastTickNanos;

        lastTickNanos =
                now;

        if (delta > 0) {

            double tickRate =
                    1_000_000_000.0 / delta;

            tickRate =
                    Math.min(20.0, tickRate);

            tpsEstimate =
                    tpsEstimate * 0.94 +
                    tickRate * 0.06;

            tpsEstimate =
                    MathHelper.clamp(
                            tpsEstimate,
                            0.0,
                            20.0
                    );
        }

        // ========================================================
        // CPS WINDOW
        // ========================================================

        long cutoff =
                System.currentTimeMillis() -
                1000L;

        while (!clickTimes.isEmpty() &&
                clickTimes.peekFirst() < cutoff) {

            clickTimes.removeFirst();
        }

        // ========================================================
        // COMBO TIMEOUT
        // ========================================================

        if (System.currentTimeMillis() -
                lastHitTime > 1500L) {

            combo = 0;
        }

        // ========================================================
        // HUD EDIT MODE
        // ========================================================

        if (editMode &&
                client.currentScreen == null) {

            boolean mouseDown =
                    GLFW.glfwGetMouseButton(
                            client.getWindow().getHandle(),
                            GLFW.GLFW_MOUSE_BUTTON_LEFT
                    ) == GLFW.GLFW_PRESS;

            if (mouseDown &&
                    !leftMouseWasDown) {

                beginDrag(client);
            }

            if (mouseDown) {
                updateDrag(client);
            }

            if (!mouseDown &&
                    leftMouseWasDown) {

                draggingHud = null;
                ConfigManager.save();
            }

            leftMouseWasDown = mouseDown;

        } else {

            leftMouseWasDown = false;
            draggingHud = null;
        }
    }

    // ============================================================
    // MOUSE POSITION
    // ============================================================

    private static int getMouseGuiX(
            MinecraftClient client) {

        double scale =
                client.getWindow().getScaledWidth()
                        / (double) client.getWindow().getWidth();

        return (int) Math.round(
                client.mouse.getX() * scale
        );
    }

    private static int getMouseGuiY(
            MinecraftClient client) {

        double scale =
                client.getWindow().getScaledHeight()
                        / (double) client.getWindow().getHeight();

        return (int) Math.round(
                client.mouse.getY() * scale
        );
    }

    // ============================================================
    // POSITIONS
    // ============================================================

    private static int[] getPosition(
            TopuHudConfig config,
            HudId id) {

        return switch (id) {

            case ARMOR ->
                    new int[]{
                            config.armorX,
                            config.armorY
                    };

            case FPS ->
                    new int[]{
                            config.fpsX,
                            config.fpsY
                    };

            case PING ->
                    new int[]{
                            config.pingX,
                            config.pingY
                    };

            case TPS ->
                    new int[]{
                            config.tpsX,
                            config.tpsY
                    };

            case CPS ->
                    new int[]{
                            config.cpsX,
                            config.cpsY
                    };

            case COMBO ->
                    new int[]{
                            config.comboX,
                            config.comboY
                    };

            case TOTEM ->
                    new int[]{
                            config.totemX,
                            config.totemY
                    };

            case POTION ->
                    new int[]{
                            config.potionX,
                            config.potionY
                    };

            case EFFECTS ->
                    new int[]{
                            config.effectsX,
                            config.effectsY
                    };

            case GAPPLE ->
                    new int[]{
                            config.gappleX,
                            config.gappleY
                    };

            case WARNING ->
                    new int[]{
                            config.warningX,
                            config.warningY
                    };

            case ENEMY ->
                    new int[]{
                            config.enemyHealthX,
                            config.enemyHealthY
                    };

            case COOLDOWN ->
                    new int[]{
                            config.cooldownX,
                            config.cooldownY
                    };
        };
    }

    private static void setPosition(
            TopuHudConfig config,
            HudId id,
            int x,
            int y) {

        switch (id) {

            case ARMOR -> {
                config.armorX = x;
                config.armorY = y;
            }

            case FPS -> {
                config.fpsX = x;
                config.fpsY = y;
            }

            case PING -> {
                config.pingX = x;
                config.pingY = y;
            }

            case TPS -> {
                config.tpsX = x;
                config.tpsY = y;
            }

            case CPS -> {
                config.cpsX = x;
                config.cpsY = y;
            }

            case COMBO -> {
                config.comboX = x;
                config.comboY = y;
            }

            case TOTEM -> {
                config.totemX = x;
                config.totemY = y;
            }

            case POTION -> {
                config.potionX = x;
                config.potionY = y;
            }

            case EFFECTS -> {
                config.effectsX = x;
                config.effectsY = y;
            }

            case GAPPLE -> {
                config.gappleX = x;
                config.gappleY = y;
            }

            case WARNING -> {
                config.warningX = x;
                config.warningY = y;
            }

            case ENEMY -> {
                config.enemyHealthX = x;
                config.enemyHealthY = y;
            }

            case COOLDOWN -> {
                config.cooldownX = x;
                config.cooldownY = y;
            }
        }
    }

    // ============================================================
    // ENABLED
    // ============================================================

    private static boolean isEnabled(HudId id) {

        TopuHudConfig config =
                ConfigManager.get();

        return switch (id) {

            case ARMOR ->
                    config.armorHud;

            case FPS ->
                    config.fpsCounter;

            case PING ->
                    config.pingDisplay;

            case TPS ->
                    config.tpsDisplay;

            case CPS ->
                    config.cpsDisplay;

            case COMBO ->
                    config.comboCounter;

            case TOTEM ->
                    config.totemCounter;

            case POTION ->
                    config.potionCounter;

            case EFFECTS ->
                    config.potionEffects;

            case GAPPLE ->
                    config.gappleCounter;

            case WARNING ->
                    config.armorWarning;

            case ENEMY ->
                    config.enemyHealth;

            case COOLDOWN ->
                    config.cooldown;
        };
    }

    // ============================================================
    // HUD SIZE
    // ============================================================

    private static int getWidth(HudId id) {

        return switch (id) {

            case ARMOR ->
                    120;

            case EFFECTS ->
                    180;

            case WARNING ->
                    180;

            case ENEMY ->
                    165;

            case COOLDOWN ->
                    130;

            default ->
                    150;
        };
    }

    private static int getHeight(HudId id) {

        return switch (id) {

            case ARMOR ->
                    28;

            case EFFECTS ->
                    80;

            default ->
                    22;
        };
    }

    // ============================================================
    // DRAGGING
    // ============================================================

    private static void beginDrag(
            MinecraftClient client) {

        int mouseX =
                getMouseGuiX(client);

        int mouseY =
                getMouseGuiY(client);

        for (HudId id :
                HudId.values()) {

            if (!isEnabled(id))
                continue;

            int[] pos =
                    getPosition(
                            ConfigManager.get(),
                            id
                    );

            if (mouseX >= pos[0] &&
                    mouseX <= pos[0] + getWidth(id) &&
                    mouseY >= pos[1] &&
                    mouseY <= pos[1] + getHeight(id)) {

                draggingHud =
                        id;

                dragOffsetX =
                        mouseX - pos[0];

                dragOffsetY =
                        mouseY - pos[1];

                return;
            }
        }
    }

    private static void updateDrag(
            MinecraftClient client) {

        if (draggingHud == null)
            return;

        int mouseX =
                getMouseGuiX(client);

        int mouseY =
                getMouseGuiY(client);

        setPosition(
                ConfigManager.get(),
                draggingHud,
                Math.max(
                        0,
                        mouseX - dragOffsetX
                ),
                Math.max(
                        0,
                        mouseY - dragOffsetY
                )
        );
    }

    // ============================================================
    // RENDER
    // ============================================================

    private static void render(
            MinecraftClient client,
            DrawContext drawContext,
            float tickDelta) {

        if (client.player == null ||
                client.world == null) {

            return;
        }

        TopuHudConfig config =
                ConfigManager.get();

        if (config.armorHud) {

            renderArmor(
                    client,
                    drawContext,
                    config.armorX,
                    config.armorY
            );
        }

        if (config.fpsCounter) {

            drawText(
                    drawContext,
                    "FPS: " +
                            client.getCurrentFps(),
                    config.fpsX,
                    config.fpsY
            );
        }

        if (config.pingDisplay) {

            renderPing(
                    client,
                    drawContext,
                    config.pingX,
                    config.pingY
            );
        }

        if (config.tpsDisplay) {

            drawText(
                    drawContext,
                    String.format(
                            "TPS*: %.1f",
                            tpsEstimate
                    ),
                    config.tpsX,
                    config.tpsY
            );
        }

        if (config.cpsDisplay) {

            drawText(
                    drawContext,
                    "CPS: " +
                            clickTimes.size(),
                    config.cpsX,
                    config.cpsY
            );
        }

        if (config.comboCounter) {

            drawText(
                    drawContext,
                    "Combo: " +
                            combo,
                    config.comboX,
                    config.comboY
            );
        }

        if (config.totemCounter) {

            renderCount(
                    drawContext,
                    client.player,
                    Items.TOTEM_OF_UNDYING,
                    "Totems",
                    config.totemX,
                    config.totemY
            );
        }

        if (config.potionEffects) {

            renderEffects(
                    drawContext,
                    client.player,
                    config.effectsX,
                    config.effectsY
            );
        }

        if (config.potionCounter) {

            renderPotionCount(
                    drawContext,
                    client.player,
                    config.potionX,
                    config.potionY
            );
        }

        if (config.gappleCounter) {

            renderGappleCount(
                    drawContext,
                    client.player,
                    config.gappleX,
                    config.gappleY
            );
        }

        if (config.armorWarning) {

            renderArmorWarning(
                    client,
                    drawContext,
                    config.warningX,
                    config.warningY
            );
        }

        if (config.enemyHealth) {

            renderEnemyHealth(
                    client,
                    drawContext,
                    config.enemyHealthX,
                    config.enemyHealthY
            );
        }

        if (config.cooldown) {

            renderCooldown(
                    client,
                    drawContext,
                    config.cooldownX,
                    config.cooldownY
            );
        }

        // ========================================================
        // EDIT MODE
        // ========================================================

        if (editMode) {

            int screenWidth =
                    client.getWindow()
                            .getScaledWidth();

            drawContext.fill(
                    0,
                    0,
                    screenWidth,
                    22,
                    0x77000000
            );

            drawContext.drawText(
                    client.textRenderer,
                    Text.literal(
                            "TOPU HUD EDIT MODE"
                    ),
                    8,
                    5,
                    0x00FF88,
                    true
            );

            for (HudId id :
                    HudId.values()) {

                if (!isEnabled(id))
                    continue;

                int[] position =
                        getPosition(
                                config,
                                id
                        );

                drawContext.drawBorder(
                        position[0] - 2,
                        position[1] - 2,
                        getWidth(id) + 4,
                        getHeight(id) + 4,
                        draggingHud == id
                                ? 0xFF00FF88
                                : 0x66777777
                );
            }
        }
    }

    // ============================================================
    // ARMOR HUD
    // ============================================================

    private static void renderArmor(
            MinecraftClient client,
            DrawContext drawContext,
            int x,
            int y) {

        for (int i = 0; i < 4; i++) {

            ItemStack stack =
                    client.player
                            .getInventory()
                            .getArmorStack(i);

            if (stack.isEmpty())
                continue;

            int slotX =
                    x + (3 - i) * 28;

            drawContext.drawItem(
                    stack,
                    slotX,
                    y
            );

            if (stack.isDamageable()) {

                int remaining =
                        stack.getMaxDamage() -
                        stack.getDamage();

                double percent =
                        100.0 *
                        remaining /
                        stack.getMaxDamage();

                int color;

                if (percent <= 40.0) {
                    color = 0xFF4444;
                } else if (percent <= 70.0) {
                    color = 0xFFD23F;
                } else {
                    color = 0x55FF55;
                }

                drawContext.drawText(
                        client.textRenderer,
                        Text.literal(
                                String.valueOf(
                                        remaining
                                )
                        ),
                        slotX + 16,
                        y + 17,
                        color,
                        true
                );
            }
        }
    }

    // ============================================================
    // PING
    // ============================================================

    private static void renderPing(
            MinecraftClient client,
            DrawContext drawContext,
            int x,
            int y) {

        int ping = -1;

        if (client.getNetworkHandler() != null) {

            var entry =
                    client.getNetworkHandler()
                            .getPlayerListEntry(
                                    client.player.getUuid()
                            );

            if (entry != null) {
                ping = entry.getLatency();
            }
        }

        drawText(
                drawContext,
                "Ping: " +
                        (ping < 0
                                ? "—"
                                : ping + " ms"),
                x,
                y
        );
    }

    // ============================================================
    // COUNTERS
    // ============================================================

    private static void renderCount(
            DrawContext drawContext,
            PlayerEntity player,
            Item item,
            String label,
            int x,
            int y) {

        int count = 0;

        for (ItemStack stack :
                player.getInventory().main) {

            if (stack.isOf(item)) {
                count += stack.getCount();
            }
        }

        for (ItemStack stack :
                player.getInventory().offHand) {

            if (stack.isOf(item)) {
                count += stack.getCount();
            }
        }

        drawText(
                drawContext,
                label + ": " + count,
                x,
                y
        );
    }

    private static void renderPotionCount(
            DrawContext drawContext,
            PlayerEntity player,
            int x,
            int y) {

        int count = 0;

        for (ItemStack stack :
                player.getInventory().main) {

            if (stack.isOf(Items.POTION) ||
                    stack.isOf(Items.SPLASH_POTION) ||
                    stack.isOf(Items.LINGERING_POTION)) {

                count += stack.getCount();
            }
        }

        drawText(
                drawContext,
                "Potions: " + count,
                x,
                y
        );
    }

    private static void renderGappleCount(
            DrawContext drawContext,
            PlayerEntity player,
            int x,
            int y) {

        int count = 0;

        for (ItemStack stack :
                player.getInventory().main) {

            if (stack.isOf(
                            Items.GOLDEN_APPLE) ||
                    stack.isOf(
                            Items.ENCHANTED_GOLDEN_APPLE)) {

                count += stack.getCount();
            }
        }

        for (ItemStack stack :
                player.getInventory().offHand) {

            if (stack.isOf(
                            Items.GOLDEN_APPLE) ||
                    stack.isOf(
                            Items.ENCHANTED_GOLDEN_APPLE)) {

                count += stack.getCount();
            }
        }

        drawText(
                drawContext,
                "Gapples: " + count,
                x,
                y
        );
    }

    // ============================================================
    // POTION EFFECTS
    // ============================================================

    private static void renderEffects(
            DrawContext drawContext,
            PlayerEntity player,
            int x,
            int y) {

        int currentY = y;
        int shown = 0;

        for (StatusEffectInstance effect :
                player.getStatusEffects()) {

            String name =
                    effect.getEffectType()
                            .value()
                            .getName()
                            .getString();

            int seconds =
                    Math.max(
                            0,
                            effect.getDuration() / 20
                    );

            drawText(
                    drawContext,
                    name + " " + seconds + "s",
                    x,
                    currentY
            );

            currentY += 12;
            shown++;

            if (shown >= 6)
                break;
        }
    }

    // ============================================================
    // ARMOR WARNING
    // ============================================================

    private static void renderArmorWarning(
            MinecraftClient client,
            DrawContext drawContext,
            int x,
            int y) {

        for (int i = 0; i < 4; i++) {

            ItemStack stack =
                    client.player
                            .getInventory()
                            .getArmorStack(i);

            if (!stack.isDamageable())
                continue;

            double percent =
                    100.0 *
                    (stack.getMaxDamage() -
                            stack.getDamage()) /
                    stack.getMaxDamage();

            if (percent <= 40.0) {

                drawContext.fill(
                        x - 4,
                        y - 4,
                        x + 176,
                        y + 19,
                        0xAA550000
                );

                drawContext.drawText(
                        client.textRenderer,
                        Text.literal(
                                "ARMOR LOW " +
                                        Math.round(percent) +
                                        "%"
                        ),
                        x,
                        y,
                        0xFF5555,
                        true
                );

                return;
            }
        }
    }

    // ============================================================
    // ENEMY HEALTH
    // ============================================================

    private static void renderEnemyHealth(
            MinecraftClient client,
            DrawContext drawContext,
            int x,
            int y) {

        if (!(client.crosshairTarget
                instanceof EntityHitResult hit)) {

            return;
        }

        Entity entity =
                hit.getEntity();

        if (!(entity
                instanceof LivingEntity living)) {

            return;
        }

        float maxHealth =
                Math.max(
                        1.0f,
                        living.getMaxHealth()
                );

        float health =
                Math.max(
                        0.0f,
                        living.getHealth()
                );

        float ratio =
                MathHelper.clamp(
                        health / maxHealth,
                        0.0f,
                        1.0f
                );

        int color;

        if (ratio <= 0.30f) {
            color = 0xFF4444;
        } else if (ratio <= 0.60f) {
            color = 0xFFFF55;
        } else {
            color = 0x55FF55;
        }

        drawContext.fill(
                x - 3,
                y - 3,
                x + 165,
                y + 22,
                0xAA111111
        );

        drawContext.drawText(
                client.textRenderer,
                Text.literal(
                        String.format(
                                "Enemy HP %.1f / %.1f",
                                health,
                                maxHealth
                        )
                ),
                x,
                y,
                0xFFFFFF,
                true
        );

        drawContext.fill(
                x,
                y + 14,
                x + (int) (155 * ratio),
                y + 18,
                color
        );
    }

    // ============================================================
    // ATTACK COOLDOWN
    // ============================================================

    private static void renderCooldown(
            MinecraftClient client,
            DrawContext drawContext,
            int x,
            int y) {

        float cooldown =
                client.player
                        .getAttackCooldownProgress(
                                0.0f
                        );

        int width = 120;

        drawContext.fill(
                x,
                y,
                x + width,
                y + 8,
                0xAA222222
        );

        int fillWidth =
                Math.round(
                        width * cooldown
                );

        int color =
                cooldown >= 0.99f
                        ? 0xFF55FF55
                        : 0xFFFFAA33;

        drawContext.fill(
                x,
                y,
                x + fillWidth,
                y + 8,
                color
        );

        drawText(
                drawContext,
                "Attack: " +
                        Math.round(
                                cooldown * 100
                        ) +
                        "%",
                x,
                y + 10
        );
    }

    // ============================================================
    // TEXT
    // ============================================================

    private static void drawText(
            DrawContext drawContext,
            String text,
            int x,
            int y) {

        MinecraftClient client =
                MinecraftClient.getInstance();

        drawContext.drawText(
                client.textRenderer,
                Text.literal(text),
                x,
                y,
                0xFFFFFF,
                true
        );
    }
}
