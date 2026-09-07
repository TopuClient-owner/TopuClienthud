package com.bettertoppi.topuhud.hud;

import com.bettertoppi.topuhud.modmenu.TopuUtilityManager;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.hit.EntityHitResult;
import java.util.Locale;

public final class TopuUtilityHud {
    private static int jumps;
    private static boolean wasOnGround=true;
    private TopuUtilityHud(){}
    public static void initialize(){ClientTickEvents.END_CLIENT_TICK.register(TopuUtilityHud::tick);HudRenderCallback.EVENT.register((draw,tickCounter)->render(MinecraftClient.getInstance(),draw));}
    private static boolean on(String id){for(TopuUtilityManager.Utility u:TopuUtilityManager.ALL)if(u.id().equals(id))return TopuUtilityManager.isEnabled(u);return false;}
    private static void tick(MinecraftClient c){if(c.player==null)return;PlayerEntity p=c.player;if(!p.isOnGround()&&wasOnGround)jumps++;wasOnGround=p.isOnGround();if(on("autosprint")&&c.currentScreen==null&&c.options.forwardKey.isPressed()&&!p.isSneaking()&&p.getHungerManager().getFoodLevel()>6)p.setSprinting(true);}
    private static void render(MinecraftClient c,DrawContext d){if(c.player==null||c.world==null)return;int w=c.getWindow().getScaledWidth(),x=Math.max(4,w-218),y=8,col=0,row=0;for(TopuUtilityManager.Utility u:TopuUtilityManager.ALL){if(!on(u.id())||isCore(u.id())||u.id().equals("fpsgraph"))continue;String v=value(c,u.id());if(v==null)continue;int px=x+col*109,py=y+row*18;d.fill(px-3,py-2,px+105,py+14,0xA0101520);d.drawTextWithShadow(c.textRenderer,Text.literal(u.name()+": "+v),px,py,0xFFE7EDF7);if(++col==2){col=0;row++;}if(row>=25)break;}if(on("crosshair"))crosshair(c,d);if(on("hitcolor"))hitReady(c,d);}
    private static boolean isCore(String id){return id.equals("fps")||id.equals("ping")||id.equals("tps")||id.equals("cps")||id.equals("combo")||id.equals("armor")||id.equals("effects")||id.equals("potions")||id.equals("gapples")||id.equals("totems")||id.equals("enemyhp")||id.equals("cooldown")||id.equals("warning")||id.equals("blockoverlay")||id.equals("keystrokes")||id.equals("memory")||id.equals("autosprint")||id.equals("togglesneak");}
    private static String value(MinecraftClient c,String id){PlayerEntity p=c.player;switch(id){case"coordinates":return p.getBlockX()+", "+p.getBlockY()+", "+p.getBlockZ();case"direction":return p.getHorizontalFacing().asString().toUpperCase(Locale.ROOT);case"speed":return String.format(Locale.ROOT,"%.2f b/s",Math.sqrt(p.getVelocity().x*p.getVelocity().x+p.getVelocity().z*p.getVelocity().z)*20.0);case"jumpcount":return Integer.toString(jumps);case"fall":return String.format(Locale.ROOT,"%.1f blocks",p.fallDistance);case"sprintstatus":return p.isSprinting()?"SPRINTING":"WALKING";case"velocity":return String.format(Locale.ROOT,"%.2f / %.2f / %.2f",p.getVelocity().x,p.getVelocity().y,p.getVelocity().z);case"reach":return targetDistance(c);case"attackindicator":return String.format(Locale.ROOT,"%.0f%%",p.getAttackCooldownProgress(0.0F)*100.0F);case"zoom":case"fullbright":case"lowlatency":case"cleanhud":case"minimalparticles":case"itemphysics":return"ACTIVE";case"fpslimit":return"240 FPS";case"clock":return java.time.LocalTime.now().withNano(0).toString();case"server":return server(c);case"biome":return biome(c);case"facing":return String.format(Locale.ROOT,"%.0f° %s",p.getYaw(),p.getHorizontalFacing().asString());case"chunk":return p.getChunkPos().x+", "+p.getChunkPos().z;case"light":return Integer.toString(c.world.getLightLevel(p.getBlockPos()));case"target":return target(c);case"durability":return durability(p.getMainHandStack());case"helditem":return p.getMainHandStack().isEmpty()?"Empty":p.getMainHandStack().getName().getString();case"gamemode":return c.interactionManager==null?"Unknown":String.valueOf(c.interactionManager.getCurrentGameMode()).toUpperCase(Locale.ROOT);case"difficulty":return c.world.getDifficulty().getName();case"session":return"Active";default:return null;}}
    private static String targetDistance(MinecraftClient c){if(c.crosshairTarget instanceof EntityHitResult)return String.format(Locale.ROOT,"%.2f blocks",c.player.distanceTo(((EntityHitResult)c.crosshairTarget).getEntity()));return"--";}
    private static String target(MinecraftClient c){if(c.crosshairTarget instanceof EntityHitResult){net.minecraft.entity.Entity e=((EntityHitResult)c.crosshairTarget).getEntity();if(e instanceof LivingEntity)return e.getName().getString()+" "+String.format(Locale.ROOT,"%.1f HP",((LivingEntity)e).getHealth());return e.getName().getString();}return"None";}
    private static String server(MinecraftClient c){if(c.isInSingleplayer())return"Singleplayer";if(c.getCurrentServerEntry()!=null)return c.getCurrentServerEntry().address;return"Unknown";}
    private static String biome(MinecraftClient c){return c.world.getBiome(c.player.getBlockPos()).getKey().map(k->k.getValue().getPath()).orElse("unknown");}
    private static String durability(ItemStack s){if(s.isEmpty()||!s.isDamageable())return"N/A";return(s.getMaxDamage()-s.getDamage())+"/"+s.getMaxDamage();}
    private static void crosshair(MinecraftClient c,DrawContext d){int x=c.getWindow().getScaledWidth()/2,y=c.getWindow().getScaledHeight()/2;d.fill(x-1,y-5,x+2,y+6,0xFFFFFFFF);d.fill(x-5,y-1,x+6,y+2,0xFFFFFFFF);}
    private static void hitReady(MinecraftClient c,DrawContext d){if(c.player.getAttackCooldownProgress(0.0F)>.95F&&c.crosshairTarget instanceof EntityHitResult&&((EntityHitResult)c.crosshairTarget).getEntity() instanceof LivingEntity){int x=c.getWindow().getScaledWidth()/2,y=c.getWindow().getScaledHeight()/2;d.drawBorder(x-7,y-7,14,14,0xFFFF4444);}}
}
