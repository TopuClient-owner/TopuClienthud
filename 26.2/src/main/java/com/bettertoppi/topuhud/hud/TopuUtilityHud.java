package com.bettertoppi.topuhud.hud;

import com.bettertoppi.topuhud.config.ConfigManager;
import com.bettertoppi.topuhud.config.TopuHudConfig;
import com.bettertoppi.topuhud.modmenu.TopuUtilityManager;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import java.time.Duration;
import java.time.LocalTime;
import java.util.Locale;

public final class TopuUtilityHud {
    private static final long START=System.currentTimeMillis();
    private static int jumps;
    private static boolean lastGround=true;
    private TopuUtilityHud(){}

    public static void initialize(){
        ClientTickEvents.END_CLIENT_TICK.register(TopuUtilityHud::tick);
        HudElementRegistry.addLast(Identifier.fromNamespaceAndPath("topuhud","utilities"), TopuUtilityHud::render);
    }

    private static boolean on(String id){for(TopuUtilityManager.Utility u:TopuUtilityManager.ALL)if(u.id().equals(id))return TopuUtilityManager.isEnabled(u);return false;}

    private static void tick(Minecraft mc){
        Player p=mc.player;
        if(p==null)return;
        if(!p.onGround()&&lastGround)jumps++;
        lastGround=p.onGround();
        if(on("autosprint")&&mc.screen==null&&mc.options.keyUp.isDown()&&!p.isCrouching()&&!p.isPassenger())p.setSprinting(true);
    }

    private static void render(GuiGraphicsExtractor g, net.minecraft.client.DeltaTracker delta){
        Minecraft mc=Minecraft.getInstance();
        Player p=mc.player;
        if(p==null)return;
        TopuHudConfig cfg=ConfigManager.get();
        int x=cfg.utilityX,y=cfg.utilityY;
        int row=0;
        for(TopuUtilityManager.Utility u:TopuUtilityManager.ALL){
            if(!TopuUtilityManager.isEnabled(u))continue;
            String value=value(mc,p,u.id());
            int py=y+row*18;
            g.fill(x-4,py-3,x+210,py+14,0xA0101520);
            g.text(mc.font,u.name()+": "+value,x,py,0xFFE7EDF7,true);
            row++;
            if(row>=24)break;
        }
        if(on("crosshair")){
            int cx=mc.getWindow().getGuiScaledWidth()/2,cy=mc.getWindow().getGuiScaledHeight()/2;
            g.fill(cx-1,cy-6,cx+2,cy+7,0xFFFFFFFF);g.fill(cx-6,cy-1,cx+7,cy+2,0xFFFFFFFF);
        }
        if(on("hitcolor")){
            int cx=mc.getWindow().getGuiScaledWidth()/2,cy=mc.getWindow().getGuiScaledHeight()/2;
            g.outline(cx-8,cy-8,16,16,0xFFFF4D6D);
        }
    }

    private static String value(Minecraft mc,Player p,String id){
        return switch(id){
            case "fps" -> Integer.toString(mc.getFps());
            case "coordinates" -> p.blockPosition().getX()+", "+p.blockPosition().getY()+", "+p.blockPosition().getZ();
            case "direction","facing" -> p.getDirection().getName().toUpperCase(Locale.ROOT);
            case "speed" -> String.format(Locale.ROOT,"%.2f b/s",p.getDeltaMovement().horizontalDistance()*20.0);
            case "jumpcount" -> Integer.toString(jumps);
            case "fall" -> String.format(Locale.ROOT,"%.1f blocks",p.fallDistance);
            case "sprintstatus" -> p.isSprinting()?"SPRINTING":"WALKING";
            case "velocity" -> String.format(Locale.ROOT,"%.2f / %.2f / %.2f",p.getDeltaMovement().x,p.getDeltaMovement().y,p.getDeltaMovement().z);
            case "attackindicator" -> "READY";
            case "clock" -> LocalTime.now().withNano(0).toString();
            case "helditem" -> p.getMainHandItem().isEmpty()?"Empty":p.getMainHandItem().getHoverName().getString();
            case "durability" -> p.getMainHandItem().isDamageable()?Integer.toString(p.getMainHandItem().getMaxDamage()-p.getMainHandItem().getDamageValue()):"N/A";
            case "memory" -> (Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory())/1048576+" MB";
            case "session" -> {long s=Duration.ofMillis(System.currentTimeMillis()-START).getSeconds();yield String.format(Locale.ROOT,"%02d:%02d:%02d",s/3600,(s/60)%60,s%60);}
            case "server" -> mc.getCurrentServer()!=null?mc.getCurrentServer().ip:"Singleplayer";
            case "chunk" -> (p.blockPosition().getX()>>4)+", "+(p.blockPosition().getZ()>>4);
            default -> "ON";
        };
    }
}
