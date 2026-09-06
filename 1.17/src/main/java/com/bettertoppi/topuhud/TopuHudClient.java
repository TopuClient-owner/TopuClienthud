package com.bettertoppi.topuhud;

import com.bettertoppi.topuhud.config.ConfigManager;
import com.bettertoppi.topuhud.config.TopuHudConfig;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;

public final class TopuHudClient implements ClientModInitializer {
    @Override public void onInitializeClient() {
        ConfigManager.load();
        HudRenderCallback.EVENT.register(TopuHudClient::render);
    }

    private static void render(MatrixStack matrices, float tickDelta) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null) return;
        TopuHudConfig cfg = ConfigManager.get();
        int x = Math.max(4, cfg.utilityX), y = Math.max(4, cfg.utilityY);
        int row = 0;
        if (cfg.fpsCounter) row = line(matrices, mc, x, y, row, "FPS: " + MinecraftClient.getCurrentFps());
        if (cfg.utilityCoordinates) row = line(matrices, mc, x, y, row, "XYZ: " + mc.player.getBlockPos().getX() + ", " + mc.player.getBlockPos().getY() + ", " + mc.player.getBlockPos().getZ());
        if (cfg.utilityDirection) row = line(matrices, mc, x, y, row, "Facing: " + mc.player.getHorizontalFacing().asString().toUpperCase());
        if (cfg.utilitySprintStatus) row = line(matrices, mc, x, y, row, mc.player.isSprinting() ? "SPRINTING" : "WALKING");
        if (cfg.utilityVelocity) row = line(matrices, mc, x, y, row, String.format("Velocity: %.2f / %.2f / %.2f", mc.player.getVelocity().x, mc.player.getVelocity().y, mc.player.getVelocity().z));
        if (cfg.memory) row = line(matrices, mc, x, y, row, "Memory: " + ((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1048576) + " MB");
        if (cfg.utilityCrosshair) {
            int cx = mc.getWindow().getScaledWidth() / 2, cy = mc.getWindow().getScaledHeight() / 2;
            DrawableHelper.fill(matrices, cx - 1, cy - 5, cx + 2, cy + 6, 0xFFFFFFFF);
            DrawableHelper.fill(matrices, cx - 5, cy - 1, cx + 6, cy + 2, 0xFFFFFFFF);
        }
    }

    private static int line(MatrixStack matrices, MinecraftClient mc, int x, int y, int row, String text) {
        DrawableHelper.fill(matrices, x - 3, y + row * 18 - 2, x + 190, y + row * 18 + 15, 0xA0101520);
        DrawableHelper.drawTextWithShadow(matrices, mc.textRenderer, new LiteralText(text), x, y + row * 18, 0xFFE7EDF7);
        return row + 1;
    }
}
