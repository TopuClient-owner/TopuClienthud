package com.bettertoppi.topuhud;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

public final class TopuHudClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        HudRenderCallback.EVENT.register(TopuHudClient::render);
    }

    private static void render(MatrixStack matrices, float tickDelta) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null) return;

        int x = 6;
        int y = 6;
        int row = 0;
        row = line(matrices, mc, x, y, row, "TOPU HUD");
        row = line(matrices, mc, x, y, row, "FPS: " + mc.getCurrentFps());
        row = line(matrices, mc, x, y, row, "XYZ: " + mc.player.getBlockPos().getX() + ", " + mc.player.getBlockPos().getY() + ", " + mc.player.getBlockPos().getZ());
        row = line(matrices, mc, x, y, row, "Facing: " + mc.player.getHorizontalFacing().asString().toUpperCase());
        row = line(matrices, mc, x, y, row, mc.player.isSprinting() ? "SPRINTING" : "WALKING");
        line(matrices, mc, x, y, row, "Memory: " + ((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1048576) + " MB");
    }

    private static int line(MatrixStack matrices, MinecraftClient mc, int x, int y, int row, String text) {
        int py = y + row * 10;
        mc.textRenderer.drawWithShadow(matrices, Text.of(text), x, py, 0xFFE7EDF7);
        return row + 1;
    }
}
