package com.bettertoppi.topuhud.hud;

import com.bettertoppi.topuhud.config.ConfigManager;
import com.bettertoppi.topuhud.config.TopuHudConfig;
import com.bettertoppi.topuhud.modmenu.TopuUtilityManager;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.EntityHitResult;
import java.time.Duration;
import java.time.LocalTime;
import java.util.Locale;

/** All Topu utility modules ported to the unobfuscated 26.x HUD API. */
public final class TopuUtilityHud {
    private static final long SESSION_START=System.currentTimeMillis();
    private static int jumps;
    private static boolean wasGround=true;
    private static long lastClick;
    private static float savedFov=-1;
    private TopuUtilityHud(){}

    public static void initialize(){
        ClientTickEvents.END_CLIENT_TICK.register(TopuUtilityHud::tick);
        HudElementRegistry.addLast(Identifier.fromNamespaceAndPath("topuhud","utilities"),TopuUtilityHud::render);
    }
    private static boolean on(String id){for(TopuUtilityManager.Utility u:TopuUtilityManager.ALL)if(u.id().equals(id))return TopuUtilityManager.isEnabled(u);return false;}
    private static void tick(Minecraft mc){
        Player p=mc.player;if(p==null)return;
        if(!p.onGround()&&wasGround)jumps++;wasGround=p.onGround();
        if(on("autosprint")&&mc.screen==null&&mc.options.keyUp.isDown()&&!p.isCrouching()&&!p.isPassenger()&&p.getFoodData().getFoodLevel()>6)p.setSprinting(true);
        if(on("zoom")&&mc.options.keyShift.isDown()){if(savedFov<0)savedFov=mc.options.fov().get();mc.options.fov().set(Math.max(30,savedFov*0.45f));}else if(savedFov>=0){mc.options.fov().set((int)savedFov);savedFov=-1;}
    }
    private static void render(GuiGraphicsExtractor g,net.minecraft.client.DeltaTracker delta){
        Minecraft mc=Minecraft.getInstance();Player p=mc.player;if(p==null)return;TopuHudConfig c=ConfigManager.get();int x=c.utilityX,y=c.utilityY,row=0;
        for(TopuUtilityManager.Utility u:TopuUtilityManager.ALL){if(!TopuUtilityManager.isEnabled(u))continue;if(u.id().equals("zoom")||u.id().equals("fullbright")||u.id().equals("fpslimit")||u.id().equals("lowlatency")||u.id().equals("cleanhud")||u.id().equals("minimalparticles")||u.id().equals("itemphysics")||u.id().equals("hitsound"))continue;String v=value(mc,p,u.id());draw(g,mc,u.name()+": "+v,x,y+row*18);if(++row>=24)break;}
        if(on("crosshair")){int cx=mc.getWindow().getGuiScaledWidth()/2,cy=mc.getWindow().getGuiScaledHeight()/2;g.fill(cx-1,cy-7,cx+2,cy+8,0xFFFFFFFF);g.fill(cx-7,cy-1,cx+8,cy+2,0xFFFFFFFF);}
        if(on("hitcolor")&&System.currentTimeMillis()-lastClick<180){int cx=mc.getWindow().getGuiScaledWidth()/2,cy=mc.getWindow().getGuiScaledHeight()/2;g.outline(cx-9,cy-9,18,18,0xFFFF405F);}
        if(on("fpsgraph")){int gy=mc.getWindow().getGuiScaledHeight()-55;draw(g,mc,"FPS graph: "+mc.getFps(),x,gy);g.fill(x,gy+15,x+Math.min(180,mc.getFps()/2),gy+18,0xFF55FF88);}
    }
    private static void draw(GuiGraphicsExtractor g,Minecraft mc,String s,int x,int y){g.fill(x-4,y-3,x+215,y+14,0xA0101520);g.text(mc.font,Component.literal(s),x,y,0xFFE7EDF7,true);}
    private static String value(Minecraft mc,Player p,String id){
        return switch(id){
            case "fps"->Integer.toString(mc.getFps());
            case "ping"->{int n=-1;if(mc.getConnection()!=null){var i=mc.getConnection().getPlayerInfo(p.getUUID());if(i!=null)n=i.getLatency();}yield n<0?"-":n+" ms";}
            case "tps"->"20.0*";
            case "cps"->Integer.toString(mc.mouseHandler.isLeftPressed()?1:0);
            case "combo"->"-";
            case "coordinates"->p.blockPosition().getX()+", "+p.blockPosition().getY()+", "+p.blockPosition().getZ();
            case "direction","facing"->p.getDirection().getName().toUpperCase(Locale.ROOT);
            case "speed"->String.format(Locale.ROOT,"%.2f b/s",p.getDeltaMovement().horizontalDistance()*20.0);
            case "jumpcount"->Integer.toString(jumps);
            case "fall"->String.format(Locale.ROOT,"%.1f blocks",p.fallDistance);
            case "sprintstatus"->p.isSprinting()?"SPRINTING":"WALKING";
            case "velocity"->String.format(Locale.ROOT,"%.2f / %.2f / %.2f",p.getDeltaMovement().x,p.getDeltaMovement().y,p.getDeltaMovement().z);
            case "attackindicator"->String.format(Locale.ROOT,"%.0f%%",p.getAttackStrengthScale(0)*100);
            case "reach"->mc.hitResult==null?"-":String.format(Locale.ROOT,"%.2f",mc.hitResult.getLocation().distanceTo(p.getEyePosition()));
            case "target"->mc.hitResult instanceof EntityHitResult h?h.getEntity().getName().getString():"None";
            case "clock"->LocalTime.now().withNano(0).toString();
            case "server"->mc.getCurrentServer()!=null?mc.getCurrentServer().ip:"Singleplayer";
            case "biome"->mc.level==null?"-":mc.level.getBiome(p.blockPosition()).value().getName().getString();
            case "chunk"->(p.blockPosition().getX()>>4)+", "+(p.blockPosition().getZ()>>4);
            case "light"->mc.level==null?"-":Integer.toString(mc.level.getMaxLocalRawBrightness(p.blockPosition()));
            case "durability"->durability(p.getMainHandItem());
            case "helditem"->p.getMainHandItem().isEmpty()?"Empty":p.getMainHandItem().getHoverName().getString();
            case "gamemode"->mc.gameMode==null?"-":mc.gameMode.getPlayerMode().getLongDisplayName().getString();
            case "difficulty"->mc.level==null?"-":mc.level.getDifficulty().getKey().getString();
            case "memory"->(Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory())/1048576+" MB";
            case "session"->{long s=Duration.ofMillis(System.currentTimeMillis()-SESSION_START).getSeconds();yield String.format(Locale.ROOT,"%02d:%02d:%02d",s/3600,(s/60)%60,s%60);}
            case "totems"->Integer.toString(count(p,Items.TOTEM_OF_UNDYING));
            case "gapples"->Integer.toString(count(p,Items.GOLDEN_APPLE)+count(p,Items.ENCHANTED_GOLDEN_APPLE));
            case "potions"->Integer.toString(count(p,Items.POTION)+count(p,Items.SPLASH_POTION)+count(p,Items.LINGERING_POTION));
            default->"ON";
        };
    }
    private static int count(Player p,net.minecraft.world.item.Item item){int n=0;for(ItemStack s:p.getInventory().items)if(s.is(item))n+=s.getCount();for(ItemStack s:p.getInventory().offhand)if(s.is(item))n+=s.getCount();return n;}
    private static String durability(ItemStack s){return s.isDamageableItem()?Integer.toString(s.getMaxDamage()-s.getDamageValue()):"N/A";}
}
