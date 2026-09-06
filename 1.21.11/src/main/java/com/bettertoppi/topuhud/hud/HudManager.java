package com.bettertoppi.topuhud.hud;

import com.bettertoppi.topuhud.config.ConfigManager;
import com.bettertoppi.topuhud.config.TopuHudConfig;
import com.bettertoppi.topuhud.modmenu.TopuHudScreen;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.hit.EntityHitResult;
import org.lwjgl.glfw.GLFW;
import java.util.ArrayDeque;
import java.util.Deque;

public final class HudManager {
    private static final Deque<Long> clicks=new ArrayDeque<>();
    private static long lastHit;
    private static int combo;
    private static boolean rshift;
    public enum HudId { ARMOR,FPS,PING,TPS,CPS,COMBO,TOTEM,POTION,EFFECTS,GAPPLE,WARNING,ENEMY,COOLDOWN,BLOCK_OVERLAY,KEYSTROKES,MEMORY }
    private HudManager(){}
    public static void initialize(){
        ClientTickEvents.END_CLIENT_TICK.register(c->{
            if(GLFW.glfwGetKey(c.getWindow().getHandle(),GLFW.GLFW_KEY_RIGHT_SHIFT)==GLFW.GLFW_PRESS&&!rshift){if(c.currentScreen==null)c.setScreen(new TopuHudScreen(null));}rshift=GLFW.glfwGetKey(c.getWindow().getHandle(),GLFW.GLFW_KEY_RIGHT_SHIFT)==GLFW.GLFW_PRESS;
            long cut=System.currentTimeMillis()-1000;while(!clicks.isEmpty()&&clicks.peekFirst()<cut)clicks.removeFirst();if(System.currentTimeMillis()-lastHit>1500)combo=0;
        });
        HudRenderCallback.EVENT.register((d,t)->render(MinecraftClient.getInstance(),d));
    }
    public static void registerClick(){clicks.addLast(System.currentTimeMillis());}
    public static void registerHit(){registerClick();lastHit=System.currentTimeMillis();combo++;}
    private static void render(MinecraftClient c,DrawContext d){if(c.player==null||c.world==null)return;TopuHudConfig q=ConfigManager.get();
        if(q.armorHud)armor(c,d,q.armorX,q.armorY); if(q.fpsCounter)txt(d,"FPS: "+c.getCurrentFps(),q.fpsX,q.fpsY); if(q.pingDisplay)ping(c,d,q.pingX,q.pingY); if(q.tpsDisplay)txt(d,"TPS: 20.0",q.tpsX,q.tpsY); if(q.cpsDisplay)txt(d,"CPS: "+clicks.size(),q.cpsX,q.cpsY); if(q.comboCounter)txt(d,"Combo: "+combo,q.comboX,q.comboY); if(q.totemCounter)count(d,c.player,Items.TOTEM_OF_UNDYING,"Totems",q.totemX,q.totemY); if(q.potionCounter)potion(d,c.player,q.potionX,q.potionY); if(q.gappleCounter)count(d,c.player,Items.GOLDEN_APPLE,"Gapples",q.gappleX,q.gappleY); if(q.potionEffects)effects(c,d,q.effectsX,q.effectsY); if(q.armorWarning)warning(c,d,q.warningX,q.warningY); if(q.enemyHealth)enemy(c,d,q.enemyHealthX,q.enemyHealthY); if(q.cooldown)cooldown(c,d,q.cooldownX,q.cooldownY); if(q.blockOverlay)block(c,d,q.blockOverlayX,q.blockOverlayY); if(q.keystrokes)keys(c,d,q.keystrokesX,q.keystrokesY); if(q.memory)memory(d,q.memoryX,q.memoryY);
    }
    private static void txt(DrawContext d,String s,int x,int y){MinecraftClient c=MinecraftClient.getInstance();d.drawTextWithShadow(c.textRenderer,Text.literal(s),x,y,0xFFE7EDF7);}
    private static void armor(MinecraftClient c,DrawContext d,int x,int y){EquipmentSlot[] a={EquipmentSlot.HEAD,EquipmentSlot.CHEST,EquipmentSlot.LEGS,EquipmentSlot.FEET};for(int i=0;i<4;i++){ItemStack s=c.player.getEquippedStack(a[i]);if(!s.isEmpty()){int px=x+i*30;d.drawItem(s,px,y);if(s.isDamageable())txt(d,""+(s.getMaxDamage()-s.getDamage()),px+2,y+18);}}}
    private static void ping(MinecraftClient c,DrawContext d,int x,int y){int p=-1;if(c.getNetworkHandler()!=null&&c.getNetworkHandler().getPlayerListEntry(c.player.getUuid())!=null)p=c.getNetworkHandler().getPlayerListEntry(c.player.getUuid()).getLatency();txt(d,"Ping: "+(p<0?"—":p+" ms"),x,y);}
    private static void count(DrawContext d,PlayerEntity p,net.minecraft.item.Item item,String n,int x,int y){int z=0;for(int i=0;i<36;i++){ItemStack s=p.getInventory().getStack(i);if(s.isOf(item))z+=s.getCount();}if(p.getOffHandStack().isOf(item))z+=p.getOffHandStack().getCount();txt(d,n+": "+z,x,y);}
    private static void potion(DrawContext d,PlayerEntity p,int x,int y){int z=0;for(int i=0;i<36;i++){ItemStack s=p.getInventory().getStack(i);if(s.isOf(Items.POTION)||s.isOf(Items.SPLASH_POTION)||s.isOf(Items.LINGERING_POTION))z+=s.getCount();}txt(d,"Potions: "+z,x,y);}
    private static void effects(MinecraftClient c,DrawContext d,int x,int y){int yy=y;int n=0;for(var e:c.player.getStatusEffects()){txt(d,e.getEffectType().value().getName().getString()+" "+Math.max(0,e.getDuration()/20)+"s",x,yy);yy+=12;if(++n>=5)break;}}
    private static void warning(MinecraftClient c,DrawContext d,int x,int y){for(EquipmentSlot s:new EquipmentSlot[]{EquipmentSlot.HEAD,EquipmentSlot.CHEST,EquipmentSlot.LEGS,EquipmentSlot.FEET}){ItemStack a=c.player.getEquippedStack(s);if(a.isDamageable()&&100.0*(a.getMaxDamage()-a.getDamage())/a.getMaxDamage()<=40){txt(d,"⚠ ARMOR LOW",x,y);return;}}}
    private static void enemy(MinecraftClient c,DrawContext d,int x,int y){if(c.crosshairTarget instanceof EntityHitResult h&&h.getEntity() instanceof LivingEntity e)txt(d,"Enemy HP: "+String.format(java.util.Locale.ROOT,"%.1f",e.getHealth()),x,y);}
    private static void cooldown(MinecraftClient c,DrawContext d,int x,int y){int v=Math.round(c.player.getAttackCooldownProgress(0)*100);txt(d,"Attack: "+v+"%",x,y);d.fill(x,y+12,x+120,y+16,0xAA222222);d.fill(x,y+12,x+Math.round(120*v/100f),y+16,0xFF55D98B);}
    private static void block(MinecraftClient c,DrawContext d,int x,int y){if(c.crosshairTarget instanceof net.minecraft.util.hit.BlockHitResult h)txt(d,"Block: "+c.world.getBlockState(h.getBlockPos()).getBlock().getName().getString(),x,y);else txt(d,"Block: —",x,y);}
    private static void keys(MinecraftClient c,DrawContext d,int x,int y){String s="W"+(c.options.forwardKey.isPressed()?"*":" ")+" A"+(c.options.leftKey.isPressed()?"*":" ")+" S"+(c.options.backKey.isPressed()?"*":" ")+" D"+(c.options.rightKey.isPressed()?"*":"");txt(d,s,x,y);txt(d,"SPACE"+(c.options.jumpKey.isPressed()?"*":""),x,y+14);}
    private static void memory(DrawContext d,int x,int y){Runtime r=Runtime.getRuntime();long u=(r.totalMemory()-r.freeMemory())/1048576L,m=r.maxMemory()/1048576L;txt(d,"Memory: "+u+" / "+m+" MB",x,y);}
}
