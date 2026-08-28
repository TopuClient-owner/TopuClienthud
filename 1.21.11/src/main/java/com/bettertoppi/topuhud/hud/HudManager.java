
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
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.util.InputUtil;

import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.MathHelper;

import org.lwjgl.glfw.GLFW;

import java.util.ArrayDeque;
import java.util.Deque;

public final class HudManager {

    private static KeyBinding menuKey;
    private static KeyBinding editKey;
    private static KeyBinding sneakKey;

    private static boolean editMode;
    private static boolean sneakEnabled;
    private static boolean leftMouseDown;

    private static HudId draggingHud;

    private static int dragOffsetX;
    private static int dragOffsetY;

    private static final Deque<Long> clickTimes =
            new ArrayDeque<>();

    private static long lastHitTime = 0L;
    private static int combo = 0;

    private static double tpsEstimate = 20.0;
    private static long lastTickNanos = System.nanoTime();

    private static final KeyBinding.Category TOPU_HUD_CATEGORY =
            KeyBinding.Category.create(
                    Identifier.of("topuhud", "category")
            );

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
                        TOPU_HUD_CATEGORY
                )
        );

        editKey = KeyBindingHelper.registerKeyBinding(
                new KeyBinding(
                        "key.topuhud.edit",
                        InputUtil.Type.KEYSYM,
                        GLFW.GLFW_KEY_RIGHT_CONTROL,
                        TOPU_HUD_CATEGORY
                )
        );

        sneakKey = KeyBindingHelper.registerKeyBinding(
                new KeyBinding(
                        "key.topuhud.sneak",
                        InputUtil.Type.KEYSYM,
                        GLFW.GLFW_KEY_RIGHT_ALT,
                        TOPU_HUD_CATEGORY
                )
        );

        ClientTickEvents.END_CLIENT_TICK.register(
                HudManager::tick
        );

        HudRenderCallback.EVENT.register(
                HudManager::render
        );
    }

    // ============================================================
    // PUBLIC METHODS
    // ============================================================

    public static void setMenuOpen(boolean value) {
        // Menu state is handled by Minecraft's current screen.
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

        lastHitTime =
                System.currentTimeMillis();

        combo++;
    }

    // ============================================================
    // CLIENT TICK
    // ============================================================

    private static void tick(MinecraftClient client) {

        // --------------------------------------------------------
        // MENU KEY
        // --------------------------------------------------------

        if (menuKey != null) {

            while (menuKey.wasPressed()) {

                if (client.currentScreen == null) {

                    client.setScreen(
                            new TopuHudScreen(null)
                    );
                }
            }
        }

        // --------------------------------------------------------
        // EDIT MODE KEY
        // --------------------------------------------------------

        if (editKey != null) {

            while (editKey.wasPressed()) {

                setEditMode(
                        !editMode
                );
            }
        }

        // --------------------------------------------------------
        // TOGGLE SNEAK KEY
        // --------------------------------------------------------

        if (sneakKey != null) {

            while (sneakKey.wasPressed()) {

                sneakEnabled =
                        !sneakEnabled;
            }
        }

        if (client.player == null) {
            return;
        }

        TopuHudConfig config =
                ConfigManager.get();

        // --------------------------------------------------------
        // TOGGLE SNEAK
        // --------------------------------------------------------

        if (config.toggleSneak) {

            client.player.setSneaking(
                    sneakEnabled
            );
        }

        // --------------------------------------------------------
        // AUTO SPRINT
        // --------------------------------------------------------

        if (config.autoSprint
                && client.currentScreen == null
                && client.options.forwardKey.isPressed()
                && !client.player.isSneaking()
                && client.player.getHungerManager()
                        .getFoodLevel() > 6) {

            client.player.setSprinting(true);
        }

        // --------------------------------------------------------
        // TPS ESTIMATE
        // --------------------------------------------------------

        long now =
                System.nanoTime();

        long delta =
                now - lastTickNanos;

        lastTickNanos =
                now;

        if (delta > 0L) {

            double currentRate =
                    1_000_000_000.0 /
                            delta;

            currentRate =
                    Math.min(
                            20.0,
                            currentRate
                    );

            tpsEstimate =
                    tpsEstimate * 0.94 +
                    currentRate * 0.06;

            tpsEstimate =
                    MathHelper.clamp(
                            tpsEstimate,
                            0.0,
                            20.0
                    );
        }

        // --------------------------------------------------------
        // CPS WINDOW
        // --------------------------------------------------------

        long cutoff =
                System.currentTimeMillis() - 1000L;

        while (!clickTimes.isEmpty()
                && clickTimes.peekFirst() < cutoff) {

            clickTimes.removeFirst();
        }

        // --------------------------------------------------------
        // COMBO RESET
        // --------------------------------------------------------

        if (lastHitTime > 0L
                && System.currentTimeMillis() -
                        lastHitTime > 1500L) {

            combo = 0;
        }

        // --------------------------------------------------------
        // HUD EDIT MODE
        // --------------------------------------------------------

        if (editMode
                && client.currentScreen == null) {

            boolean mouseDown =
                    GLFW.glfwGetMouseButton(
                            client.getWindow().getHandle(),
                            GLFW.GLFW_MOUSE_BUTTON_LEFT
                    ) == GLFW.GLFW_PRESS;

            if (mouseDown && !leftMouseDown) {

                beginDrag(client);
            }

            if (mouseDown) {

                updateDrag(client);
            }

            if (!mouseDown && leftMouseDown) {

                draggingHud = null;

                ConfigManager.save();
            }

            leftMouseDown =
                    mouseDown;

        } else {

            leftMouseDown = false;
            draggingHud = null;
        }
    }

    // ============================================================
    // MOUSE POSITION
    // ============================================================

    private static int getMouseX(
            MinecraftClient client) {

        return (int) Math.round(
                client.mouse.getX()
                        * client.getWindow().getScaledWidth()
                        / client.getWindow().getWidth()
        );
    }

    private static int getMouseY(
            MinecraftClient client) {

        return (int) Math.round(
                client.mouse.getY()
                        * client.getWindow().getScaledHeight()
                        / client.getWindow().getHeight()
        );
    }

    // ============================================================
    // HUD POSITION
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
    // HUD DIMENSIONS
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
                getMouseX(client);

        int mouseY =
                getMouseY(client);

        TopuHudConfig config =
                ConfigManager.get();

        HudId[] ids =
                HudId.values();

        for (int i = ids.length - 1;
             i >= 0;
             i--) {

            HudId id =
                    ids[i];

            if (!isEnabled(id)) {
                continue;
            }

            int[] position =
                    getPosition(
                            config,
                            id
                    );

            if (mouseX >= position[0]
                    && mouseX <=
                            position[0] +
                                    getWidth(id)
                    && mouseY >= position[1]
                    && mouseY <=
                            position[1] +
                                    getHeight(id)) {

                draggingHud =
                        id;

                dragOffsetX =
                        mouseX -
                                position[0];

                dragOffsetY =
                        mouseY -
                                position[1];

                return;
            }
        }
    }

    private static void updateDrag(
            MinecraftClient client) {

        if (draggingHud == null) {
            return;
        }

        int mouseX =
                getMouseX(client);

        int mouseY =
                getMouseY(client);

        setPosition(
                ConfigManager.get(),
                draggingHud,
                Math.max(
                        0,
                        mouseX -
                                dragOffsetX
                ),
                Math.max(
                        0,
                        mouseY -
                                dragOffsetY
                )
        );
    }

    // ============================================================
    // HUD RENDER
    // ============================================================

    private static void render(
            DrawContext drawContext,
            RenderTickCounter tickCounter) {

        MinecraftClient client =
                MinecraftClient.getInstance();

        if (client.player == null
                || client.world == null) {

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

            renderItemCount(
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

        // --------------------------------------------------------
        // EDIT MODE
        // --------------------------------------------------------

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
                            "TOPU HUD EDIT MODE - Right Ctrl to exit"
                    ),
                    8,
                    5,
                    0x00FF88,
                    true
            );

            for (HudId id :
                    HudId.values()) {

                if (!isEnabled(id)) {
                    continue;
                }

                int[] position =
                        getPosition(
                                config,
                                id
                        );

                drawBorder(
                        drawContext,
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
    // BORDER
    // ============================================================

    private static void drawBorder(
            DrawContext drawContext,
            int x,
            int y,
            int width,
            int height,
            int color) {

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
    // ARMOR HUD
    // ============================================================

    private static void renderArmor(
            MinecraftClient client,
            DrawContext drawContext,
            int x,
            int y) {

        EquipmentSlot[] armorSlots = {
                EquipmentSlot.FEET,
                EquipmentSlot.LEGS,
                EquipmentSlot.CHEST,
                EquipmentSlot.HEAD
        };

        for (int i = 0; i < armorSlots.length; i++) {

            ItemStack stack =
                    client.player.getEquippedStack(
                            armorSlots[i]
                    );

            if (stack.isEmpty()) {
                continue;
            }

            int slotX =
                    x + (3 - i) * 28;

            drawContext.drawItem(
                    stack,
                    slotX,
                    y
            );

            if (!stack.isDamageable()) {
                continue;
            }

            int maxDamage =
                    stack.getMaxDamage();

            if (maxDamage <= 0) {
                continue;
            }

            int remaining =
                    maxDamage -
                            stack.getDamage();

            double percent =
                    100.0 *
                            remaining /
                            maxDamage;

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

                ping =
                        entry.getLatency();
            }
        }

        drawText(
                drawContext,
                "Ping: " +
                        (
                                ping < 0
                                        ? "—"
                                        : ping + " ms"
                        ),
                x,
                y
        );
    }

    // ============================================================
    // GENERIC ITEM COUNT
    // ============================================================

    private static void renderItemCount(
            DrawContext drawContext,
            PlayerEntity player,
            Item item,
            String label,
            int x,
            int y) {

        int count = 0;

        for (ItemStack stack :
                player.getInventory()
                        .getMainStacks()) {

            if (stack.isOf(item)) {

                count +=
                        stack.getCount();
            }
        }

        ItemStack offhand =
                player.getOffHandStack();

        if (offhand.isOf(item)) {

            count +=
                    offhand.getCount();
        }

        drawText(
                drawContext,
                label + ": " + count,
                x,
                y
        );
    }

    // ============================================================
    // POTION COUNT
    // ============================================================

    private static void renderPotionCount(
            DrawContext drawContext,
            PlayerEntity player,
            int x,
            int y) {

        int count = 0;

        for (ItemStack stack :
                player.getInventory()
                        .getMainStacks()) {

            if (stack.isOf(Items.POTION)
                    || stack.isOf(Items.SPLASH_POTION)
                    || stack.isOf(Items.LINGERING_POTION)) {

                count +=
                        stack.getCount();
            }
        }

        ItemStack offhand =
                player.getOffHandStack();

        if (offhand.isOf(Items.POTION)
                || offhand.isOf(Items.SPLASH_POTION)
                || offhand.isOf(Items.LINGERING_POTION)) {

            count +=
                    offhand.getCount();
        }

        drawText(
                drawContext,
                "Potions: " + count,
                x,
                y
        );
    }

    // ============================================================
    // GAPPLE COUNT
    // ============================================================

    private static void renderGappleCount(
            DrawContext drawContext,
            PlayerEntity player,
            int x,
            int y) {

        int count = 0;

        for (ItemStack stack :
                player.getInventory()
                        .getMainStacks()) {

            if (stack.isOf(Items.GOLDEN_APPLE)
                    || stack.isOf(
                            Items.ENCHANTED_GOLDEN_APPLE)) {

                count +=
                        stack.getCount();
            }
        }

        ItemStack offhand =
                player.getOffHandStack();

        if (offhand.isOf(Items.GOLDEN_APPLE)
                || offhand.isOf(
                        Items.ENCHANTED_GOLDEN_APPLE)) {

            count +=
                    offhand.getCount();
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

            if (shown >= 6) {
                break;
            }
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

        EquipmentSlot[] armorSlots = {
                EquipmentSlot.FEET,
                EquipmentSlot.LEGS,
                EquipmentSlot.CHEST,
                EquipmentSlot.HEAD
        };

        for (EquipmentSlot slot :
                armorSlots) {

            ItemStack stack =
                    client.player.getEquippedStack(
                            slot
                    );

            if (!stack.isDamageable()) {
                continue;
            }

            int maxDamage =
                    stack.getMaxDamage();

            if (maxDamage <= 0) {
                continue;
            }

            double percent =
                    100.0 *
                            (maxDamage -
                                    stack.getDamage()) /
                            maxDamage;

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
                                        Math.round(
                                                percent
                                        ) +
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

        if (!(hit.getEntity()
                instanceof LivingEntity entity)) {

            return;
        }

        float maxHealth =
                Math.max(
                        1.0f,
                        entity.getMaxHealth()
                );

        float health =
                Math.max(
                        0.0f,
                        entity.getHealth()
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

