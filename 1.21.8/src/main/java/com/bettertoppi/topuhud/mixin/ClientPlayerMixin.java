package com.bettertoppi.topuhud.mixin;

import com.bettertoppi.topuhud.hud.HudManager;
import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayerEntity.class)
public final class ClientPlayerMixin {
    @Inject(method="attack",at=@At("HEAD"))
    private void topuhud$attack(CallbackInfo ci){
        HudManager.registerHit();
    }
}
