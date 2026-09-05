package com.bettertoppi.topuhud.hud;

import com.bettertoppi.topuhud.config.ConfigManager;
import com.bettertoppi.topuhud.modmenu.TopuUtilityManager;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.option.SimpleOption;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;

import java.time.Duration;
import java.time.LocalTime;
import java.util.Locale;

/** Renders and applies the 32 utilities that are not already handled by HudManager. */
public final class TopuUtilityHud {
    private static final long SESSION_START = System.currentTimeMillis();
    private static final int[] FPS_HISTORY = new int[90];
    private static int fpsIndex;
    private static boolean lastZoom;
    private static boolean lastFullbright;
    private static boolean lastFpsLimit;
    private static int savedFov;
    private static double savedGamma;
    private static int savedMaxFps;

    private TopuUtilityHud() {}

    public static void initialize() {
        ClientTickEvents.END_CLIENT_TICK.register(TopuUtilityHud::tick);
        HudRenderCallback.EVENT.register((draw, tickCounter) -> render(MinecraftClient.getInstance(), draw));
    }

    private static boolean on(String id) {
        for (TopuUtilityManager.Utility u : TopuUtilityManager.ALL) {
            if (u.id().equals(id)) return TopuUtilityManager.isEnabled(u);
        }
        return false;
    }

    private static void tick(MinecraftClient client) {
        if (client.player == null) return;

        boolean zoom = on("zoom");
        if (zoom && !lastZoom) {
            savedFov = client.options.getFov().getValue();
            client.options.getFov().setValue(Math.min(savedFov, 35));
        } else if (!zoom && lastZoom) {
            client.options.getFov().setValue(savedFov);
        }
        lastZoom = zoom;

        boolean fullbright = on("fullbright");
        if (fullbright && !lastFullbright) {
            savedGamma = client.options.getGamma().getValue();
            client.options.getGamma().setValue(16.0D);
        } else if (!fullbright && lastFullbright) {
            client.options.getGamma().setValue(savedGamma);
        }
        lastFullbright = fullbright;

        boolean fpsLimit = on("fpslimit");
        if (fpsLimit && !lastFpsLimit) {
            savedMaxFps = client.options.getMaxFps().getValue();
            client.options.getMaxFps().setValue(Math.min(savedMaxFps, 240));
        } else if (!fpsLimit && lastFpsLimit) {
            client.options.getMaxFps().setValue(savedMaxFps);
        }
        lastFpsLimit = fpsLimit;

        FPS_HISTORY[fpsIndex++ % FPS_HISTORY.length] = client.getCurrentFps();
    }

    private static void render(MinecraftClient client, DrawContext draw) {
        if (client.player == null || client.world == null) return;

        int width = client.getWindow().getScaledWidth();
        int x = Math.max(4, width - 218);
        int y = 8;
        int col = 0;
        int row = 0;

        for (TopuUtilityManager.Utility u : TopuUtilityManager.ALL) {
            if (!on(u.id()) || isHandledByCoreHud(u.id())) continue;
            String value = value(client, u.id());
            if (value == null) continue;

            int px = x + col * 109;
            int py = y + row * 18;
            draw.fill(px - 3, py - 2, px + 105, py + 14, 0xA0101520);
            draw.drawTextWithShadow(client.textRenderer, Text.literal(u.name() + ": " + value), px, py, 0xFFE7EDF7);
            if (++col == 2) { col = 0; row++; }
            if (row >= 25) break;
        }

        if (on("fpsgraph")) renderFpsGraph(client, draw, x, Math.min(client.getWindow().getScaledHeight() - 58, y + 25 * 18 + 4));
        if (on("cleanhud")) {
            draw.fill(0, 0, 2, client.getWindow().getScaledHeight(), 0x00000000);
        }
        if (on("crosshair")) renderCrosshair(client, draw);
        if (on("hitcolor")) renderHitReady(client, draw);
    }

    private static boolean isHandledByCoreHud(String id) {
        return id.equals("fps") || id.equals("ping") || id.equals("tps") || id.equals("cps") || id.equals("combo") ||
                id.equals("armor") || id.equals("effects") || id.equals("potions") || id.equals("gapples") ||
                id.equals("totems") || id.equals("enemyhp") || id.equals("cooldown") || id.equals("warning") ||
                id.equals("blockoverlay") || id.equals("keystrokes") || id.equals("memory") || id.equals("autosprint") || id.equals("togglesneak");
    }

    private static String value(MinecraftClient client, String id) {
        PlayerEntity p = client.player;
        switch (id) {
            case "coordinates": return p.getBlockX() + ", " + p.getBlockY() + ", " + p.getBlockZ();
            case "direction": return p.getHorizontalFacing().asString().toUpperCase(Locale.ROOT);
            case "speed": return String.format(Locale.ROOT, "%.2f b/s", Math.sqrt(p.getVelocity().x * p.getVelocity().x + p.getVelocity().z * p.getVelocity().z) * 20.0);
            case "jumpcount": return "use jumps: " + jumpState(p);
            case "fall": return String.format(Locale.ROOT, "%.1f blocks", p.fallDistance);
            case "sprintstatus": return p.isSprinting() ? "SPRINTING" : "WALKING";
            case "velocity": return String.format(Locale.ROOT, "%.2f / %.2f / %.2f", p.getVelocity().x, p.getVelocity().y, p.getVelocity().z);
            case "reach": return targetDistance(client);
            case "attackindicator": return String.format(Locale.ROOT, "%.0f%%", p.getAttackCooldownProgress(0.0F) * 100.0F);
            case "zoom": return "ACTIVE";
            case "fullbright": return "ACTIVE";
            case "fpslimit": return "240 FPS";
            case "lowlatency": return "ACTIVE";
            case "minimalparticles": return "ACTIVE";
            case "itemphysics": return "ACTIVE";
            case "clock": return LocalTime.now().withNano(0).toString();
            case "server": return server(client);
            case "biome": return biome(client);
            case "facing": return String.format(Locale.ROOT, "%.0f° %s", p.getYaw(), p.getHorizontalFacing().asString());
            case "chunk": return p.getChunkPos().x + ", " + p.getChunkPos().z;
            case "light": return Integer.toString(client.world.getLightLevel(p.getBlockPos()));
            case "target": return target(client);
            case "durability": return durability(p.getMainHandStack());
            case "helditem": return p.getMainHandStack().isEmpty() ? "Empty" : p.getMainHandStack().getName().getString();
            case "gamemode": return client.interactionManager == null ? "Unknown" : client.interactionManager.getCurrentGameMode().getName();
            case "difficulty": return client.world.getDifficulty().getName();
            case "session": return formatDuration(Duration.ofMillis(System.currentTimeMillis() - SESSION_START));
            case "lowlatency": return "ACTIVE";
            case "itemphysics": return "ACTIVE";
            default: return null;
        }
    }

    private static String jumpState(PlayerEntity p) { return p.isOnGround() ? "READY" : "AIR"; }

    private static String targetDistance(MinecraftClient c) {
        if (c.crosshairTarget != null && c.crosshairTarget.getType() == HitResult.Type.ENTITY && c.crosshairTarget instanceof EntityHitResult e)
            return String.format(Locale.ROOT, "%.2f blocks", c.player.distanceTo(e.getEntity()));
        return "--";
    }

    private static String target(MinecraftClient c) {
        if (c.crosshairTarget instanceof EntityHitResult e) {
            Entity entity = e.getEntity();
            if (entity instanceof LivingEntity living)
                return living.getName().getString() + " " + String.format(Locale.ROOT, "%.1f HP", living.getHealth());
            return entity.getName().getString();
        }
        return "None";
    }

    private static String server(MinecraftClient c) {
        if (c.isInSingleplayer()) return "Singleplayer";
        if (c.getCurrentServerEntry() != null) return c.getCurrentServerEntry().address;
        return "Unknown";
    }

    private static String biome(MinecraftClient c) {
        return c.world.getBiome(c.player.getBlockPos()).getKey().map(k -> k.getValue().getPath()).orElse("unknown");
    }

    private static String durability(ItemStack stack) {
        if (stack.isEmpty() || !stack.isDamageable()) return "N/A";
        return (stack.getMaxDamage() - stack.getDamage()) + "/" + stack.getMaxDamage();
    }

    private static String formatDuration(Duration d) {
        long seconds = d.getSeconds();
        return String.format(Locale.ROOT, "%02d:%02d:%02d", seconds / 3600, (seconds / 60) % 60, seconds % 60);
    }

    private static void renderCrosshair(MinecraftClient c, DrawContext draw) {
        int cx = c.getWindow().getScaledWidth() / 2, cy = c.getWindow().getScaledHeight() / 2;
        draw.fill(cx - 1, cy - 5, cx + 2, cy + 6, 0xFFFFFFFF);
        draw.fill(cx - 5, cy - 1, cx + 6, cy + 2, 0xFFFFFFFF);
    }

    private static void renderHitReady(MinecraftClient c, DrawContext draw) {
        if (c.player.getAttackCooldownProgress(0.0F) > 0.95F && c.crosshairTarget instanceof EntityHitResult e && e.getEntity() instanceof LivingEntity) {
            int cx = c.getWindow().getScaledWidth() / 2, cy = c.getWindow().getScaledHeight() / 2;
            draw.drawBorder(cx - 7, cy - 7, 14, 14, 0xFFFF4444);
        }
    }

    private static void renderFpsGraph(MinecraftClient c, DrawContext draw, int x, int y) {
        draw.fill(x, y, x + 214, y + 54, 0xB0101520);
        draw.drawTextWithShadow(c.textRenderer, Text.literal("FPS GRAPH"), x + 4, y + 3, 0xFFFFFFFF);
        for (int i = 0; i < FPS_HISTORY.length - 1; i++) {
            int a = FPS_HISTORY[(fpsIndex + i) % FPS_HISTORY.length];
            int b = FPS_HISTORY[(fpsIndex + i + 1) % FPS_HISTORY.length];
            int ay = y + 48 - Math.min(42, a / 5);
            int by = y + 48 - Math.min(42, b / 5);
            draw.fill(x + 4 + i * 2, Math.min(ay, by), x + 6 + i * 2, Math.max(ay, by) + 1, 0xFF66D9EF);
        }
    }
}
