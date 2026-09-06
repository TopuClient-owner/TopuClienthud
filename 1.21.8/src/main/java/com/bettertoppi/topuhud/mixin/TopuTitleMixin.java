package com.bettertoppi.topuhud.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public final class TopuTitleMixin {
    @Inject(method="render", at=@At("HEAD"))
    private void topuhud$background(DrawContext d, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        int w=d.getScaledWindowWidth(),h=d.getScaledWindowHeight();
        d.fill(0,0,w,h,0x52060A12);d.fill(0,0,4,h,0xFF6FA8FF);d.fill(w-4,0,w,h,0xFF6FA8FF);
        d.fill(22,22,174,62,0xB0101723);d.fill(22,62,174,64,0xFF6FA8FF);
        MinecraftClient mc=MinecraftClient.getInstance();d.drawTextWithShadow(mc.textRenderer,Text.literal("TOPU"),34,31,0xFFFFFFFF);d.drawTextWithShadow(mc.textRenderer,Text.literal("CLIENT"),79,31,0xFF6FA8FF);d.drawTextWithShadow(mc.textRenderer,Text.literal("PVP PERFORMANCE EDITION"),34,48,0xFF7F8CA1);
    }
    @Inject(method="render", at=@At("TAIL"))
    private void topuhud$footer(DrawContext d, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        int w=d.getScaledWindowWidth(),h=d.getScaledWindowHeight();d.fill(22,h-28,w-22,h-27,0xAA6FA8FF);d.drawTextWithShadow(MinecraftClient.getInstance().textRenderer,Text.literal("TOPU CLIENT  •  PERFORMANCE  •  PVP"),28,h-20,0xFF8F9DB2);
    }
}
