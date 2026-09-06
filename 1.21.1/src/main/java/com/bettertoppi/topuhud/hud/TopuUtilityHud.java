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
    private static final long SESSION_START=System.currentTimeMillis(); private static final int[] FPS_HISTORY=new int[90]; private static int fpsIndex,jumps; private static boolean wasOnGround=true,lastZoom,lastFullbright,lastFpsLimit; private static int savedFov,savedMaxFps; private static double savedGamma;
    private TopuUtilityHud(){}
    public static void initialize(){ClientTickEvents.END_CLIENT_TICK.register(TopuUtilityHud::tick);HudRenderCallback.EVENT.register((draw,tickCounter)->render(MinecraftClient.getInstance(),draw));}
    private static boolean on(String id){for(TopuUtilityManager.Utility u:TopuUtilityManager.ALL)if(u.id().equals(id))return TopuUtilityManager.isEnabled(u);return false;}
    private static void tick(MinecraftClient c){if(c.player==null)return;PlayerEntity p=c.player;if(!p.isOnGround()&&wasOnGround)jumps++;wasOnGround=p.isOnGround();boolean z=on("zoom");if(z&&!lastZoom){savedFov=c.options.getFov().getValue();c.options.getFov().setValue(Math.min(savedFov,35));}else if(!z&&lastZoom)c.options.getFov().setValue(savedFov);lastZoom=z;boolean f=on("fullbright");if(f&&!lastFullbright){savedGamma=c.options.getGamma().getValue();c.options.getGamma().setValue(16.0D);}else if(!f&&lastFullbright)c.options.getGamma().setValue(savedGamma);lastFullbright=f;boolean l=on("fpslimit");if(l&&!lastFpsLimit){savedMaxFps=c.options.getMaxFps().getValue();c.options.getMaxFps().setValue(Math.min(savedMaxFps,240));}else if(!l&&lastFpsLimit)c.options.getMaxFps().setValue(savedMaxFps);lastFpsLimit=l;FPS_HISTORY[fpsIndex++%FPS_HISTORY.length]=c.getCurrentFps();}
    private static void render(MinecraftClient c,DrawContext d){if(c.player==null||c.world==null)return;renderLegacyHud(c,d);int w=c.getWindow().getScaledWidth(),x=Math.max(4,w-218),y=8,col=0,row=0;for(TopuUtilityManager.Utility u:TopuUtilityManager.ALL){if(!on(u.id())||isCore(u.id())||u.id().equals("fpsgraph"))continue;String v=value(c,u.id());if(v==null)continue;int px=x+col*109,py=y+row*18;d.fill(px-3,py-2,px+105,py+14,0xA0101520);d.drawTextWithShadow(c.textRenderer,Text.literal(u.name()+": "+v),px,py,0xFFE7EDF7);if(++col==2){col=0;row++;}if(row>=25)break;}if(on("fpsgraph"))graph(c,d,x,Math.min(c.getWindow().getScaledHeight()-58,y+454));if(on("crosshair"))crosshair(c,d);if(on("hitcolor"))hitReady(c,d);}
    private static void renderLegacyHud(MinecraftClient c,DrawContext d){try{Method m=HudManager.class.getDeclaredMethod("render",MinecraftClient.class,DrawContext.class,float.class);m.setAccessible(true);m.invoke(null,c,d,0.0F);}catch(Throwable ignored){}}
    private static boolean isCore(String id){return id.equals("fps")||id.equals("ping")||id.equals("tps")||id.equals("cps")||id.equals("combo")||id.equals("armor")||id.equals("effects")||id.equals("potions")||id.equals("gapples")||id.equals("totems")||id.equals("enemyhp")||id.equals("cooldown")||id.equals("warning")||id.equals("blockoverlay")||id.equals("keystrokes")||id.equals("memory")||id.equals("autosprint")||id.equals("togglesneak");}
    private static String value(MinecraftClient c,String id){PlayerEntity p=c.player;return switch(id){case"coordinates"->p.getBlockX()+", "+p.getBlockY()+", "+p.getBlockZ();case"direction"->p.getHorizontalFacing().asString().toUpperCase(Locale.ROOT);case"speed"->String.format(Locale.ROOT,"%.2f b/s",Math.sqrt(p.getVelocity().x*p.getVelocity().x+p.getVelocity().z*p.getVelocity().z)*20.0);case"jumpcount"->Integer.toString(jumps);case"fall"->String.format(Locale.ROOT,"%.1f blocks",p.fallDistance);case"sprintstatus"->p.isSprinting()?"SPRINTING":"WALKING";case"velocity"->String.format(Locale.ROOT,"%.2f / %.2f / %.2f",p.getVelocity().x,p.getVelocity().y,p.getVelocity().z);case"reach"->targetDistance(c);case"attackindicator"->String.format(Locale.ROOT,"%.0f%%",p.getAttackCooldownProgress(0.0F)*100.0F);case"zoom","fullbright","lowlatency","cleanhud","minimalparticles","itemphysics"->"ACTIVE";case"fpslimit"->"240 FPS";case"clock"->LocalTime.now().withNano(0).toString();case"server"->server(c);case"biome"->biome(c);case"facing"->String.format(Locale.ROOT,"%.0f° %s",p.getYaw(),p.getHorizontalFacing().asString());case"chunk"->p.getChunkPos().x+", "+p.getChunkPos().z;case"light"->Integer.toString(c.world.getLightLevel(p.getBlockPos()));case"target"->target(c);case"durability"->durability(p.getMainHandStack());case"helditem"->p.getMainHandStack().isEmpty()?"Empty":p.getMainHandStack().getName().getString();case"gamemode"->c.interactionManager==null?"Unknown":c.interactionManager.getCurrentGameMode().getName().toUpperCase(Locale.ROOT);case"difficulty"->c.world.getDifficulty().getName();case"session"->duration(Duration.ofMillis(System.currentTimeMillis()-SESSION_START));default->null;};}
    private static String targetDistance(MinecraftClient c){if(c.crosshairTarget instanceof EntityHitResult e)return String.format(Locale.ROOT,"%.2f blocks",c.player.distanceTo(e.getEntity()));return"--";}
    private static String target(MinecraftClient c){if(c.crosshairTarget instanceof EntityHitResult e){Entity en=e.getEntity();if(en instanceof LivingEntity l)return l.getName().getString()+" "+String.format(Locale.ROOT,"%.1f HP",l.getHealth());return en.getName().getString();}return"None";}
    private static String server(MinecraftClient c){if(c.isInSingleplayer())return"Singleplayer";if(c.getCurrentServerEntry()!=null)return c.getCurrentServerEntry().address;return"Unknown";}
    private static String biome(MinecraftClient c){return c.world.getBiome(c.player.getBlockPos()).getKey().map(k->k.getValue().getPath()).orElse("unknown");}
    private static String durability(ItemStack s){if(s.isEmpty()||!s.isDamageable())return"N/A";return(s.getMaxDamage()-s.getDamage())+"/"+s.getMaxDamage();}
    private static String duration(Duration d){long s=d.getSeconds();return String.format(Locale.ROOT,"%02d:%02d:%02d",s/3600,(s/60)%60,s%60);}
    private static void crosshair(MinecraftClient c,DrawContext d){int x=c.getWindow().getScaledWidth()/2,y=c.getWindow().getScaledHeight()/2;d.fill(x-1,y-5,x+2,y+6,0xFFFFFFFF);d.fill(x-5,y-1,x+6,y+2,0xFFFFFFFF);}
    private static void hitReady(MinecraftClient c,DrawContext d){if(c.player.getAttackCooldownProgress(0.0F)>.95F&&c.crosshairTarget instanceof EntityHitResult e&&e.getEntity() instanceof LivingEntity){int x=c.getWindow().getScaledWidth()/2,y=c.getWindow().getScaledHeight()/2;d.drawBorder(x-7,y-7,14,14,0xFFFF4444);}}
    private static void graph(MinecraftClient c,DrawContext d,int x,int y){d.fill(x,y,x+214,y+54,0xB0101520);d.drawTextWithShadow(c.textRenderer,Text.literal("FPS GRAPH"),x+4,y+3,0xFFFFFFFF);for(int i=0;i<FPS_HISTORY.length-1;i++){int a=FPS_HISTORY[(fpsIndex+i)%FPS_HISTORY.length],b=FPS_HISTORY[(fpsIndex+i+1)%FPS_HISTORY.length],ay=y+48-Math.min(42,a/5),by=y+48-Math.min(42,b/5);d.fill(x+4+i*2,Math.min(ay,by),x+6+i*2,Math.max(ay,by)+1,0xFF66D9EF);}}
}
