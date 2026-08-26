package com.bettertoppi.topuhud.hud;

import com.bettertoppi.topuhud.config.ConfigManager;
import com.bettertoppi.topuhud.config.TopuHudConfig;
import com.bettertoppi.topuhud.modmenu.TopuHudScreen;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.glfw.GLFW;
import java.util.ArrayDeque;
import java.util.Deque;

public final class HudManager {
    private static KeyBinding menu,edit,sneak;
    private static boolean editMode,sneakOn,leftDown;
    private static Id dragging;
    private static int ox,oy;
    private static final Deque<Long> clicks=new ArrayDeque<>();
    private static long lastHit;
    private static int combo;
    private static double tps=20;
    private static long lastTick=System.nanoTime();

    enum Id{ARMOR,FPS,PING,TPS,CPS,COMBO,TOTEM,POTION,EFFECTS,GAPPLE,WARNING,ENEMY,COOLDOWN}

    public static void initialize(){
        menu=KeyBindingHelper.registerKeyBinding(new KeyBinding("key.topuhud.menu",InputUtil.Type.KEYSYM,GLFW.GLFW_KEY_RIGHT_SHIFT,"category.topuhud"));
        edit=KeyBindingHelper.registerKeyBinding(new KeyBinding("key.topuhud.edit",InputUtil.Type.KEYSYM,GLFW.GLFW_KEY_RIGHT_CONTROL,"category.topuhud"));
        sneak=KeyBindingHelper.registerKeyBinding(new KeyBinding("key.topuhud.sneak",InputUtil.Type.KEYSYM,GLFW.GLFW_KEY_RIGHT_ALT,"category.topuhud"));
        ClientTickEvents.END_CLIENT_TICK.register(HudManager::tick);
        HudRenderCallback.EVENT.register(HudManager::render);
    }

    public static void setMenuOpen(boolean v){ }
    public static void setEditMode(boolean v){editMode=v;ConfigManager.get().editMode=v;if(!v){dragging=null;ConfigManager.save();}}
    public static void registerClick(){clicks.addLast(System.currentTimeMillis());}
    public static void registerHit(){registerClick();lastHit=System.currentTimeMillis();combo++;}

    private static void tick(MinecraftClient mc){
        while(menu.wasPressed() && mc.currentScreen==null) mc.setScreen(new TopuHudScreen(null));
        while(edit.wasPressed()) setEditMode(!editMode);
        while(sneak.wasPressed()) sneakOn=!sneakOn;
        if(mc.player==null)return;
        TopuHudConfig c=ConfigManager.get();
        if(c.toggleSneak)mc.player.setSneaking(sneakOn);
        if(c.autoSprint && mc.currentScreen==null && mc.options.forwardKey.isPressed() && !mc.player.isSneaking()
                && mc.player.getHungerManager().getFoodLevel()>6)mc.player.setSprinting(true);

        long now=System.nanoTime(),d=now-lastTick;lastTick=now;
        if(d>0){double r=1_000_000_000.0/d;tps=tps*.94+Math.min(20,r)*.06;tps=MathHelper.clamp(tps,0,20);}
        long cut=System.currentTimeMillis()-1000;
        while(!clicks.isEmpty()&&clicks.peekFirst()<cut)clicks.removeFirst();
        if(System.currentTimeMillis()-lastHit>1500)combo=0;

        if(editMode && mc.currentScreen==null){
            boolean down=GLFW.glfwGetMouseButton(mc.getWindow().getHandle(),GLFW.GLFW_MOUSE_BUTTON_LEFT)==GLFW.GLFW_PRESS;
            if(down&&!leftDown)beginDrag(mc);
            if(down)drag(mc);
            if(!down&&leftDown){dragging=null;ConfigManager.save();}
            leftDown=down;
        }else{leftDown=false;dragging=null;}
    }

    private static int mx(MinecraftClient mc){return(int)Math.round(mc.mouse.getX()*mc.getWindow().getScaledWidth()/mc.getWindow().getWidth());}
    private static int my(MinecraftClient mc){return(int)Math.round(mc.mouse.getY()*mc.getWindow().getScaledHeight()/mc.getWindow().getHeight());}

    private static int[] pos(TopuHudConfig c,Id id){return switch(id){
        case ARMOR->new int[]{c.armorX,c.armorY};case FPS->new int[]{c.fpsX,c.fpsY};
        case PING->new int[]{c.pingX,c.pingY};case TPS->new int[]{c.tpsX,c.tpsY};
        case CPS->new int[]{c.cpsX,c.cpsY};case COMBO->new int[]{c.comboX,c.comboY};
        case TOTEM->new int[]{c.totemX,c.totemY};case POTION->new int[]{c.potionX,c.potionY};
        case EFFECTS->new int[]{c.effectsX,c.effectsY};case GAPPLE->new int[]{c.gappleX,c.gappleY};
        case WARNING->new int[]{c.warningX,c.warningY};case ENEMY->new int[]{c.enemyHealthX,c.enemyHealthY};
        case COOLDOWN->new int[]{c.cooldownX,c.cooldownY};};}

    private static void setPos(TopuHudConfig c,Id id,int x,int y){switch(id){
        case ARMOR->{c.armorX=x;c.armorY=y;}case FPS->{c.fpsX=x;c.fpsY=y;}case PING->{c.pingX=x;c.pingY=y;}
        case TPS->{c.tpsX=x;c.tpsY=y;}case CPS->{c.cpsX=x;c.cpsY=y;}case COMBO->{c.comboX=x;c.comboY=y;}
        case TOTEM->{c.totemX=x;c.totemY=y;}case POTION->{c.potionX=x;c.potionY=y;}case EFFECTS->{c.effectsX=x;c.effectsY=y;}
        case GAPPLE->{c.gappleX=x;c.gappleY=y;}case WARNING->{c.warningX=x;c.warningY=y;}
        case ENEMY->{c.enemyHealthX=x;c.enemyHealthY=y;}case COOLDOWN->{c.cooldownX=x;c.cooldownY=y;}}}

    private static boolean enabled(Id id){TopuHudConfig c=ConfigManager.get();return switch(id){
        case ARMOR->c.armorHud;case FPS->c.fpsCounter;case PING->c.pingDisplay;case TPS->c.tpsDisplay;
        case CPS->c.cpsDisplay;case COMBO->c.comboCounter;case TOTEM->c.totemCounter;case POTION->c.potionCounter;
        case EFFECTS->c.potionEffects;case GAPPLE->c.gappleCounter;case WARNING->c.armorWarning;
        case ENEMY->c.enemyHealth;case COOLDOWN->c.cooldown;};}

    private static int w(Id id){return switch(id){case ARMOR->120;case EFFECTS->180;case ENEMY->165;case WARNING->180;case COOLDOWN->130;default->150;};}
    private static int h(Id id){return id==Id.ARMOR?28:id==Id.EFFECTS?80:22;}

    private static void beginDrag(MinecraftClient mc){
        int x=mx(mc),y=my(mc);
        for(Id id:Id.values())if(enabled(id)){
            int[] p=pos(ConfigManager.get(),id);
            if(x>=p[0]&&x<=p[0]+w(id)&&y>=p[1]&&y<=p[1]+h(id)){dragging=id;ox=x-p[0];oy=y-p[1];return;}
        }
    }
    private static void drag(MinecraftClient mc){
        if(dragging==null)return;
        setPos(ConfigManager.get(),dragging,Math.max(0,mx(mc)-ox),Math.max(0,my(mc)-oy));
    }

    private static void render(MinecraftClient mc,DrawContext d,float dt){
        if(mc.player==null||mc.world==null)return;
        TopuHudConfig c=ConfigManager.get();
        if(c.armorHud)armor(mc,d,c.armorX,c.armorY);
        if(c.fpsCounter)txt(d,"FPS: "+mc.getCurrentFps(),c.fpsX,c.fpsY);
        if(c.pingDisplay)ping(mc,d,c.pingX,c.pingY);
        if(c.tpsDisplay)txt(d,String.format("TPS*: %.1f",tps),c.tpsX,c.tpsY);
        if(c.cpsDisplay)txt(d,"CPS: "+clicks.size(),c.cpsX,c.cpsY);
        if(c.comboCounter)txt(d,"Combo: "+combo,c.comboX,c.comboY);
        if(c.totemCounter)count(d,mc.player,Items.TOTEM_OF_UNDYING,"Totems",c.totemX,c.totemY);
        if(c.potionEffects)effects(d,mc.player,c.effectsX,c.effectsY);
        if(c.potionCounter)potion(d,mc.player,c.potionX,c.potionY);
        if(c.gappleCounter)gapples(d,mc.player,c.gappleX,c.gappleY);
        if(c.armorWarning)warning(mc,d,c.warningX,c.warningY);
        if(c.enemyHealth)enemy(mc,d,c.enemyHealthX,c.enemyHealthY);
        if(c.cooldown)cooldown(mc,d,c.cooldownX,c.cooldownY);

        if(editMode){
            d.fill(0,0,mc.getWindow().getScaledWidth(),22,0x77000000);
            d.drawText(mc.textRenderer,Text.literal("TOPU HUD EDIT MODE — drag modules"),8,5,0x00FF88,true);
            for(Id id:Id.values())if(enabled(id)){int[] p=pos(c,id);d.drawBorder(p[0]-2,p[1]-2,w(id)+4,h(id)+4,dragging==id?0x00FF88:0x66777777);}
        }
    }

    private static void armor(MinecraftClient mc,DrawContext d,int x,int y){
        for(int i=0;i<4;i++){ItemStack s=mc.player.getInventory().getArmorStack(i);if(s.isEmpty())continue;int px=x+(3-i)*28;d.drawItem(s,px,y);
            if(s.isDamageable()){int left=s.getMaxDamage()-s.getDamage();double pct=100.0*left/s.getMaxDamage();d.drawText(mc.textRenderer,Text.literal(""+left),px+16,y+17,pct<=40?0xFF4444:pct<=70?0xFFD23F:0x55FF55,true);}}
    }
    private static void ping(MinecraftClient mc,DrawContext d,int x,int y){int p=-1;if(mc.getNetworkHandler()!=null){var e=mc.getNetworkHandler().getPlayerListEntry(mc.player.getUuid());if(e!=null)p=e.getLatency();}txt(d,"Ping: "+(p<0?"—":p+" ms"),x,y);}
    private static void count(DrawContext d,PlayerEntity p,Item item,String label,int x,int y){int n=0;for(ItemStack s:p.getInventory().main)if(s.isOf(item))n+=s.getCount();for(ItemStack s:p.getInventory().offHand)if(s.isOf(item))n+=s.getCount();txt(d,label+": "+n,x,y);}
    private static void potion(DrawContext d,PlayerEntity p,int x,int y){int n=0;for(ItemStack s:p.getInventory().main)if(s.isOf(Items.POTION)||s.isOf(Items.SPLASH_POTION)||s.isOf(Items.LINGERING_POTION))n+=s.getCount();txt(d,"Potions: "+n,x,y);}
    private static void gapples(DrawContext d,PlayerEntity p,int x,int y){int n=0;for(ItemStack s:p.getInventory().main)if(s.isOf(Items.GOLDEN_APPLE)||s.isOf(Items.ENCHANTED_GOLDEN_APPLE))n+=s.getCount();for(ItemStack s:p.getInventory().offHand)if(s.isOf(Items.GOLDEN_APPLE)||s.isOf(Items.ENCHANTED_GOLDEN_APPLE))n+=s.getCount();txt(d,"Gapples: "+n,x,y);}
    private static void effects(DrawContext d,PlayerEntity p,int x,int y){int yy=y,n=0;for(StatusEffectInstance e:p.getStatusEffects()){txt(d,e.getEffectType().value().getName().getString()+" "+Math.max(0,e.getDuration()/20)+"s",x,yy);yy+=12;if(++n>=6)break;}}
    private static void warning(MinecraftClient mc,DrawContext d,int x,int y){for(int i=0;i<4;i++){ItemStack s=mc.player.getInventory().getArmorStack(i);if(!s.isDamageable())continue;double p=100.0*(s.getMaxDamage()-s.getDamage())/s.getMaxDamage();if(p<=40){d.fill(x-4,y-4,x+176,y+19,0xAA550000);d.drawText(mc.textRenderer,Text.literal("ARMOR LOW "+Math.round(p)+"%"),x,y,0xFF5555,true);return;}}}
    private static void enemy(MinecraftClient mc,DrawContext d,int x,int y){if(!(mc.crosshairTarget instanceof EntityHitResult h)||!(h.getEntity() instanceof LivingEntity e))return;float max=Math.max(1,e.getMaxHealth()),hp=Math.max(0,e.getHealth()),r=MathHelper.clamp(hp/max,0,1);int col=r<=.3?0xFF4444:r<=.6?0xFFFF55:0x55FF55;d.fill(x-3,y-3,x+165,y+22,0xAA111111);d.drawText(mc.textRenderer,Text.literal(String.format("Enemy HP %.1f / %.1f",hp,max)),x,y,0xFFFFFF,true);d.fill(x,y+14,x+(int)(155*r),y+18,col);}
    private static void cooldown(MinecraftClient mc,DrawContext d,int x,int y){float v=mc.player.getAttackCooldownProgress(0);int w=120;d.fill(x,y,x+w,y+8,0xAA222222);d.fill(x,y,x+Math.round(w*v),y+8,v>=.99?0xFF55FF55:0xFFFFAA33);txt(d,"Attack: "+Math.round(v*100)+"%",x,y+10);}
    private static void txt(DrawContext d,String s,int x,int y){d.drawText(MinecraftClient.getInstance().textRenderer,Text.literal(s),x,y,0xFFFFFF,true);}
}
