package com.bettertoppi.topuhud.hud;

import com.bettertoppi.topuhud.modmenu.TopuUtilityManager;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.hit.EntityHitResult;
import java.lang.reflect.Method;
import java.time.Duration;
import java.time.LocalTime;
import java.util.Locale;

public final class TopuUtilityHud {
    private static final long SESSION_START = System.currentTimeMillis();
    private static final int[] FPS_HISTORY = new int[90];
    private static int fpsIndex, jumps;
    private static boolean wasOnGround = true, lastZoom, lastFullbright, lastFpsLimit;
    private static int savedFov, savedMaxFps;
    private static double savedGamma;
    private TopuUtilityHud() {}
    public static void initialize() { ClientTickEvents.END_CLIENT_TICK.register(TopuUtilityHud::tick); HudRenderCallback.EVENT.register((draw, tickCounter) -> render(MinecraftClient.getInstance(), draw)); }
    private static boolean on(String id) { for (TopuUtilityManager.Utility u : TopuUtilityManager.ALL) if (u.id().equals(id)) return TopuUtilityManager.isEnabled(u); return false; }
    private static void tick(MinecraftClient client) {
        if (client.player == null) return;
        PlayerEntity p = client.player;
        if (!p.isOnGround() && wasOnGround) jumps++;
        wasOnGround = p.isOnGround();
        if (on("autosprint") && client.currentScreen == null && client.options.forwardKey.isPressed() && !p.isSneaking() && p.getHungerManager().getFoodLevel() > 6) p.setSprinting(true);
        boolean zoom = on("zoom");
        if (zoom && !lastZoom) { savedFov = client.options.getFov().getValue(); client.options.getFov().setValue(Math.min(savedFov, 35)); } else if (!zoom && lastZoom) client.options.getFov().setValue(savedFov); lastZoom = zoom;
        boolean fullbright = on("fullbright");
        if (fullbright && !lastFullbright) { savedGamma = client.options.getGamma().getValue(); client.options.getGamma().setValue(16.0D); } else if (!fullbright && lastFullbright) client.options.getGamma().setValue(savedGamma); lastFullbright = fullbright;
        boolean fpsLimit = on("fpslimit");
        if (fpsLimit && !lastFpsLimit) { savedMaxFps = client.options.getMaxFps().getValue(); client.options.getMaxFps().setValue(Math.min(savedMaxFps, 240)); } else if (!fpsLimit && lastFpsLimit) client.options.getMaxFps().setValue(savedMaxFps); lastFpsLimit = fpsLimit;
        FPS_HISTORY[fpsIndex++ % FPS_HISTORY.length] = client.getCurrentFps();
    }
    private static void render(MinecraftClient client, DrawContext draw) {
        if (client.player == null || client.world == null) return;
        renderLegacyHud(client, draw);
        int width = client.getWindow().getScaledWidth(), x = Math.max(4, width - 218), y = 8, col = 0, row = 0;
        for (TopuUtilityManager.Utility u : TopuUtilityManager.ALL) { if (!on(u.id()) || isCore(u.id()) || u.id().equals("fpsgraph")) continue; String value = value(client, u.id()); if (value == null) continue; int px = x + col * 109, py = y + row * 18; draw.fill(px - 3, py - 2, px + 105, py + 14, 0xA0101520); draw.drawTextWithShadow(client.textRenderer, Text.literal(u.name() + ": " + value), px, py, 0xFFE7EDF7); if (++col == 2) { col = 0; row++; } if (row >= 25) break; }
        if (on("fpsgraph")) renderFpsGraph(client, draw, x, Math.min(client.getWindow().getScaledHeight() - 58, y + 25 * 18 + 4));
        if (on("crosshair")) renderCrosshair(client, draw);
        if (on("hitcolor")) renderHitReady(client, draw);
    }
    private static void renderLegacyHud(MinecraftClient client, DrawContext draw) { try { Method m = HudManager.class.getDeclaredMethod("render", MinecraftClient.class, DrawContext.class, float.class); m.setAccessible(true); m.invoke(null, client, draw, 0.0F); } catch (Throwable ignored) {} }
    private static boolean isCore(String id) { return id.equals("fps") || id.equals("ping") || id.equals("tps") || id.equals("cps") || id.equals("combo") || id.equals("armor") || id.equals("effects") || id.equals("potions") || id.equals("gapples") || id.equals("totems") || id.equals("enemyhp") || id.equals("cooldown") || id.equals("warning") || id.equals("blockoverlay") || id.equals("keystrokes") || id.equals("memory") || id.equals("autosprint") || id.equals("togglesneak"); }
    private static String value(MinecraftClient client, String id) { PlayerEntity p = client.player; return switch (id) {
        case "coordinates" -> p.getBlockX() + ", " + p.getBlockY() + ", " + p.getBlockZ();
        case "direction" -> p.getHorizontalFacing().asString().toUpperCase(Locale.ROOT);
        case "speed" -> String.format(Locale.ROOT, "%.2f b/s", Math.sqrt(p.getVelocity().x * p.getVelocity().x + p.getVelocity().z * p.getVelocity().z) * 20.0);
        case "jumpcount" -> Integer.toString(jumps);
        case "fall" -> String.format(Locale.ROOT, "%.1f blocks", p.fallDistance);
        case "sprintstatus" -> p.isSprinting() ? "SPRINTING" : "WALKING";
        case "velocity" -> String.format(Locale.ROOT, "%.2f / %.2f / %.2f", p.getVelocity().x, p.getVelocity().y, p.getVelocity().z);
        case "reach" -> targetDistance(client);
        case "attackindicator" -> String.format(Locale.ROOT, "%.0f%%", p.getAttackCooldownProgress(0.0F) * 100.0F);
        case "zoom" -> "ACTIVE"; case "fullbright" -> "ACTIVE"; case "fpslimit" -> "240 FPS"; case "lowlatency" -> "ACTIVE"; case "cleanhud" -> "ACTIVE"; case "minimalparticles" -> "ACTIVE"; case "itemphysics" -> "ACTIVE";
        case "clock" -> LocalTime.now().withNano(0).toString(); case "server" -> server(client); case "biome" -> biome(client);
        case "facing" -> String.format(Locale.ROOT, "%.0f° %s", p.getYaw(), p.getHorizontalFacing().asString());
        case "chunk" -> p.getChunkPos().x + ", " + p.getChunkPos().z; case "light" -> Integer.toString(client.world.getLightLevel(p.getBlockPos()));
        case "target" -> target(client); case "durability" -> durability(p.getMainHandStack());
        case "helditem" -> p.getMainHandStack().isEmpty() ? "Empty" : p.getMainHandStack().getName().getString();
        case "gamemode" -> client.interactionManager == null ? "Unknown" : client.interactionManager.getCurrentGameMode().getName();
        case "difficulty" -> client.world.getDifficulty().getName(); case "session" -> formatDuration(Duration.ofMillis(System.currentTimeMillis() - SESSION_START)); default -> null; }; }
    private static String targetDistance(MinecraftClient c) { if (c.crosshairTarget instanceof EntityHitResult e) return String.format(Locale.ROOT, "%.2f blocks", c.player.distanceTo(e.getEntity())); return "--"; }
    private static String target(MinecraftClient c) { if (c.crosshairTarget instanceof EntityHitResult e) { Entity entity=e.getEntity(); if(entity instanceof LivingEntity living) return living.getName().getString()+" "+String.format(Locale.ROOT,"%.1f HP",living.getHealth()); return entity.getName().getString(); } return "None"; }
    private static String server(MinecraftClient c) { if(c.isInSingleplayer()) return "Singleplayer"; if(c.getCurrentServerEntry()!=null) return c.getCurrentServerEntry().address; return "Unknown"; }
    private static String biome(MinecraftClient c) { return c.world.getBiome(c.player.getBlockPos()).getKey().map(k->k.getValue().getPath()).orElse("unknown"); }
    private static String durability(ItemStack stack) { if(stack.isEmpty()||!stack.isDamageable()) return "N/A"; return (stack.getMaxDamage()-stack.getDamage())+"/"+stack.getMaxDamage(); }
    private static String formatDuration(Duration d) { long s=d.getSeconds(); return String.format(Locale.ROOT,"%02d:%02d:%02d",s/3600,(s/60)%60,s%60); }
    private static void renderCrosshair(MinecraftClient c, DrawContext draw) { int cx=c.getWindow().getScaledWidth()/2,cy=c.getWindow().getScaledHeight()/2; draw.fill(cx-1,cy-5,cx+2,cy+6,0xFFFFFFFF); draw.fill(cx-5,cy-1,cx+6,cy+2,0xFFFFFFFF); }
    private static void renderHitReady(MinecraftClient c, DrawContext draw) { if(c.player.getAttackCooldownProgress(0.0F)>0.95F&&c.crosshairTarget instanceof EntityHitResult e&&e.getEntity() instanceof LivingEntity){int cx=c.getWindow().getScaledWidth()/2,cy=c.getWindow().getScaledHeight()/2;draw.drawBorder(cx-7,cy-7,14,14,0xFFFF4444);} }
    private static void renderFpsGraph(MinecraftClient c, DrawContext draw,int x,int y){draw.fill(x,y,x+214,y+54,0xB0101520);draw.drawTextWithShadow(c.textRenderer,Text.literal("FPS GRAPH"),x+4,y+3,0xFFFFFFFF);for(int i=0;i<FPS_HISTORY.length-1;i++){int a=FPS_HISTORY[(fpsIndex+i)%FPS_HISTORY.length],b=FPS_HISTORY[(fpsIndex+i+1)%FPS_HISTORY.length];int ay=y+48-Math.min(42,a/5),by=y+48-Math.min(42,b/5);draw.fill(x+4+i*2,Math.min(ay,by),x+6+i*2,Math.max(ay,by)+1,0xFF66D9EF);}}
}
