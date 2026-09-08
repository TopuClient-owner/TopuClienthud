package com.bettertoppi.topuhud.hud;

import com.bettertoppi.topuhud.config.ConfigManager;
import com.bettertoppi.topuhud.config.TopuHudConfig;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import org.lwjgl.glfw.GLFW;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Locale;

/** Feature-complete core Topu HUD port for unobfuscated 26.x APIs. */
public final class HudManager {
    public enum HudId { ARMOR,FPS,PING,TPS,CPS,COMBO,TOTEM,POTION,EFFECTS,GAPPLE,WARNING,ENEMY,COOLDOWN,BLOCK_OVERLAY,KEYSTROKES,MEMORY }
    private static KeyMapping editKey,sneakKey;
    private static boolean editMode,sneakToggled,mouseDown;
    private static final Deque<Long> clickTimes=new ArrayDeque<>();
    private static long lastHit;
    private static int combo;
    private static double tps=20.0;
    private static long lastTick=System.nanoTime();
    private HudManager(){}

    public static void initialize(){
        KeyMapping.Category cat=KeyMapping.Category.register(Identifier.fromNamespaceAndPath("topuhud","hud"));
        editKey=KeyMappingHelper.registerKeyMapping(new KeyMapping("key.topuhud.edit",InputConstants.Type.KEYSYM,GLFW.GLFW_KEY_RIGHT_CONTROL,cat));
        sneakKey=KeyMappingHelper.registerKeyMapping(new KeyMapping("key.topuhud.sneak",InputConstants.Type.KEYSYM,GLFW.GLFW_KEY_RIGHT_ALT,cat));
        ClientTickEvents.END_CLIENT_TICK.register(HudManager::tick);
        HudElementRegistry.addLast(Identifier.fromNamespaceAndPath("topuhud","core_hud"),HudManager::render);
    }
    public static void registerClick(){clickTimes.addLast(System.currentTimeMillis());}
    public static void registerHit(){registerClick();lastHit=System.currentTimeMillis();combo++;}
    public static void setEditMode(boolean v){editMode=v;ConfigManager.get().editMode=v;if(!v)ConfigManager.save();}
    public static boolean isEditMode(){return editMode;}

    private static void tick(Minecraft mc){
        while(editKey.consumeClick())setEditMode(!editMode);
        while(sneakKey.consumeClick())sneakToggled=!sneakToggled;
        long now=System.nanoTime(),dt=now-lastTick;lastTick=now;
        if(dt>0){double rate=Math.min(20.0,1_000_000_000.0/dt);tps=tps*.94+rate*.06;tps=Mth.clamp(tps,0,20);}
        long cutoff=System.currentTimeMillis()-1000;while(!clickTimes.isEmpty()&&clickTimes.peekFirst()<cutoff)clickTimes.removeFirst();
        if(System.currentTimeMillis()-lastHit>1500)combo=0;
        Player p=mc.player;if(p==null)return;TopuHudConfig c=ConfigManager.get();
        if(c.toggleSneak)p.setCrouching(sneakToggled);
        if(c.autoSprint&&mc.screen==null&&mc.options.keyUp.isDown()&&!p.isCrouching()&&!p.isPassenger()&&p.getFoodData().getFoodLevel()>6)p.setSprinting(true);
        boolean left=GLFW.glfwGetMouseButton(mc.getWindow().handle(),GLFW.GLFW_MOUSE_BUTTON_LEFT)==GLFW.GLFW_PRESS;
        if(left&&!mouseDown&&mc.hitResult instanceof EntityHitResult h&&h.getEntity() instanceof LivingEntity){lastHit=System.currentTimeMillis();combo++;registerClick();}
        mouseDown=left;
    }

    private static int[] pos(TopuHudConfig c,HudId id){return switch(id){case ARMOR->new int[]{c.armorX,c.armorY};case FPS->new int[]{c.fpsX,c.fpsY};case PING->new int[]{c.pingX,c.pingY};case TPS->new int[]{c.tpsX,c.tpsY};case CPS->new int[]{c.cpsX,c.cpsY};case COMBO->new int[]{c.comboX,c.comboY};case TOTEM->new int[]{c.totemX,c.totemY};case POTION->new int[]{c.potionX,c.potionY};case EFFECTS->new int[]{c.effectsX,c.effectsY};case GAPPLE->new int[]{c.gappleX,c.gappleY};case WARNING->new int[]{c.warningX,c.warningY};case ENEMY->new int[]{c.enemyHealthX,c.enemyHealthY};case COOLDOWN->new int[]{c.cooldownX,c.cooldownY};case BLOCK_OVERLAY->new int[]{c.blockOverlayX,c.blockOverlayY};case KEYSTROKES->new int[]{c.keystrokesX,c.keystrokesY};case MEMORY->new int[]{c.memoryX,c.memoryY};};}
    private static void setPos(TopuHudConfig c,HudId id,int x,int y){switch(id){case ARMOR->{c.armorX=x;c.armorY=y;}case FPS->{c.fpsX=x;c.fpsY=y;}case PING->{c.pingX=x;c.pingY=y;}case TPS->{c.tpsX=x;c.tpsY=y;}case CPS->{c.cpsX=x;c.cpsY=y;}case COMBO->{c.comboX=x;c.comboY=y;}case TOTEM->{c.totemX=x;c.totemY=y;}case POTION->{c.potionX=x;c.potionY=y;}case EFFECTS->{c.effectsX=x;c.effectsY=y;}case GAPPLE->{c.gappleX=x;c.gappleY=y;}case WARNING->{c.warningX=x;c.warningY=y;}case ENEMY->{c.enemyHealthX=x;c.enemyHealthY=y;}case COOLDOWN->{c.cooldownX=x;c.cooldownY=y;}case BLOCK_OVERLAY->{c.blockOverlayX=x;c.blockOverlayY=y;}case KEYSTROKES->{c.keystrokesX=x;c.keystrokesY=y;}case MEMORY->{c.memoryX=x;c.memoryY=y;}}}
    private static boolean enabled(TopuHudConfig c,HudId id){return switch(id){case ARMOR->c.armorHud;case FPS->c.fpsCounter;case PING->c.pingDisplay;case TPS->c.tpsDisplay;case CPS->c.cpsDisplay;case COMBO->c.comboCounter;case TOTEM->c.totemCounter;case POTION->c.potionCounter;case EFFECTS->c.potionEffects;case GAPPLE->c.gappleCounter;case WARNING->c.armorWarning;case ENEMY->c.enemyHealth;case COOLDOWN->c.cooldown;case BLOCK_OVERLAY->c.blockOverlay;case KEYSTROKES->c.keystrokes;case MEMORY->c.memory;};}
    public static int[] getPositionForEditor(TopuHudConfig c,HudId id){return pos(c,id);}public static void setPositionForEditor(TopuHudConfig c,HudId id,int x,int y){setPos(c,id,x,y);}public static boolean isEnabledForEditor(HudId id){return enabled(ConfigManager.get(),id);}public static int getWidthForEditor(HudId id){return width(id);}public static int getHeightForEditor(HudId id){return height(id);}
    private static int width(HudId id){return switch(id){case ARMOR->120;case EFFECTS,WARNING->180;case ENEMY->165;case COOLDOWN->130;case BLOCK_OVERLAY,MEMORY->170;case KEYSTROKES->110;default->150;};}private static int height(HudId id){return switch(id){case EFFECTS->80;case KEYSTROKES->90;default->24;};}

    private static void render(GuiGraphicsExtractor g,net.minecraft.client.DeltaTracker delta){Minecraft mc=Minecraft.getInstance();Player p=mc.player;if(p==null||mc.level==null)return;TopuHudConfig c=ConfigManager.get();
        if(c.armorHud)armor(g,mc,p,c.armorX,c.armorY);if(c.fpsCounter)text(g,mc,"FPS: "+mc.getFps(),c.fpsX,c.fpsY);if(c.pingDisplay)ping(g,mc,c.pingX,c.pingY);if(c.tpsDisplay)text(g,mc,String.format(Locale.ROOT,"TPS*: %.1f",tps),c.tpsX,c.tpsY);if(c.cpsDisplay)text(g,mc,"CPS: "+clickTimes.size(),c.cpsX,c.cpsY);if(c.comboCounter)text(g,mc,"Combo: "+combo,c.comboX,c.comboY);if(c.totemCounter)count(g,mc,p,Items.TOTEM_OF_UNDYING,"Totems",c.totemX,c.totemY);if(c.potionCounter)potion(g,mc,p,c.potionX,c.potionY);if(c.potionEffects)effects(g,mc,p,c.effectsX,c.effectsY);if(c.gappleCounter)gapples(g,mc,p,c.gappleX,c.gappleY);if(c.armorWarning)warning(g,mc,p,c.warningX,c.warningY);if(c.enemyHealth)enemy(g,mc,c.enemyHealthX,c.enemyHealthY);if(c.cooldown)cooldown(g,mc,p,c.cooldownX,c.cooldownY);if(c.blockOverlay)block(g,mc,c.blockOverlayX,c.blockOverlayY);if(c.keystrokes)keys(g,mc,c.keystrokesX,c.keystrokesY);if(c.memory)memory(g,mc,c.memoryX,c.memoryY);
        if(editMode){g.fill(0,0,mc.getWindow().getGuiScaledWidth(),22,0x77000000);text(g,mc,"TOPU HUD EDIT MODE - RIGHT CTRL TO EXIT",8,5);for(HudId id:HudId.values())if(enabled(c,id)){int[] q=pos(c,id);g.outline(q[0]-2,q[1]-2,width(id)+4,height(id)+4,0x66777777);}}
    }
    private static void text(GuiGraphicsExtractor g,Minecraft mc,String s,int x,int y){g.text(mc.font,Component.literal(s),x,y,0xFFFFFFFF,true);}private static void box(GuiGraphicsExtractor g,int x,int y,int w,int h){g.fill(x-4,y-3,x+w,y+h,0xAA101520);g.outline(x-4,y-3,w+4,h+3,0xFF596579);}
    private static void armor(GuiGraphicsExtractor g,Minecraft mc,Player p,int x,int y){int low=101;for(int i=0;i<4;i++){ItemStack s=p.getInventory().getArmor(i);if(s.isEmpty())continue;int pct=s.isDamageableItem()?(s.getMaxDamage()-s.getDamageValue())*100/s.getMaxDamage():100;low=Math.min(low,pct);text(g,mc,"Armor "+pct+"%",x,y+i*12);}}
    private static void ping(GuiGraphicsExtractor g,Minecraft mc,int x,int y){int ping=-1;if(mc.getConnection()!=null&&mc.player!=null){var e=mc.getConnection().getPlayerInfo(mc.player.getUUID());if(e!=null)ping=e.getLatency();}text(g,mc,"Ping: "+(ping<0?"-":ping+" ms"),x,y);}
    private static int countItem(Player p,Item item){int n=0;for(ItemStack s:p.getInventory().items)if(s.is(item))n+=s.getCount();for(ItemStack s:p.getInventory().offhand)if(s.is(item))n+=s.getCount();return n;}
    private static void count(GuiGraphicsExtractor g,Minecraft mc,Player p,Item item,String label,int x,int y){text(g,mc,label+": "+countItem(p,item),x,y);}private static void potion(GuiGraphicsExtractor g,Minecraft mc,Player p,int x,int y){text(g,mc,"Potions: "+(countItem(p,Items.POTION)+countItem(p,Items.SPLASH_POTION)+countItem(p,Items.LINGERING_POTION)),x,y);}private static void gapples(GuiGraphicsExtractor g,Minecraft mc,Player p,int x,int y){text(g,mc,"Gapples: "+(countItem(p,Items.GOLDEN_APPLE)+countItem(p,Items.ENCHANTED_GOLDEN_APPLE)),x,y);}
    private static void effects(GuiGraphicsExtractor g,Minecraft mc,Player p,int x,int y){int yy=y,n=0;for(MobEffectInstance e:p.getActiveEffects()){text(g,mc,e.getEffect().value().getDisplayName().getString()+" "+Math.max(0,e.getDuration()/20)+"s",x,yy);yy+=12;if(++n>=6)break;}}
    private static void warning(GuiGraphicsExtractor g,Minecraft mc,Player p,int x,int y){for(ItemStack s:p.getInventory().getArmor()){if(!s.isDamageableItem())continue;int pct=(s.getMaxDamage()-s.getDamageValue())*100/s.getMaxDamage();if(pct<=40){box(g,x,y,180,23);text(g,mc,"ARMOR LOW "+pct+"%",x,y+2);return;}}}
    private static void enemy(GuiGraphicsExtractor g,Minecraft mc,int x,int y){if(!(mc.hitResult instanceof EntityHitResult h)||!(h.getEntity() instanceof LivingEntity e))return;float max=Math.max(1,e.getMaxHealth()),hp=Math.max(0,e.getHealth()),r=Mth.clamp(hp/max,0,1);box(g,x,y,165,25);text(g,mc,String.format(Locale.ROOT,"Enemy HP %.1f / %.1f",hp,max),x,y+2);g.fill(x,y+16,x+(int)(155*r),y+20,r<.3?0xFFFF4444:r<.6?0xFFFFFF55:0xFF55FF55);}
    private static void cooldown(GuiGraphicsExtractor g,Minecraft mc,Player p,int x,int y){float r=p.getAttackStrengthScale(0);g.fill(x,y,x+120,y+8,0xAA222222);g.fill(x,y,x+(int)(120*r),y+8,r>=.99?0xFF55FF55:0xFFFFAA33);text(g,mc,"Attack: "+Math.round(r*100)+"%",x,y+10);}
    private static void block(GuiGraphicsExtractor g,Minecraft mc,int x,int y){if(!(mc.hitResult instanceof BlockHitResult h)||mc.level==null)return;BlockPos p=h.getBlockPos();box(g,x,y,170,23);text(g,mc,"Block: "+mc.level.getBlockState(p).getBlock().getName().getString(),x,y+2);}
    private static void keys(GuiGraphicsExtractor g,Minecraft mc,int x,int y){int s=24,gap=2;key(g,mc,"W",x+s+gap,y,s,mc.options.keyUp.isDown());key(g,mc,"A",x,y+s+gap,s,mc.options.keyLeft.isDown());key(g,mc,"S",x+s+gap,y+s+gap,s,mc.options.keyDown.isDown());key(g,mc,"D",x+(s+gap)*2,y+s+gap,s,mc.options.keyRight.isDown());key(g,mc,"SPACE",x,y+(s+gap)*2,s*3+gap*2,mc.options.keyJump.isDown());}
    private static void key(GuiGraphicsExtractor g,Minecraft mc,String s,int x,int y,int w,boolean d){g.fill(x,y,x+w,y+22,d?0xAA006644:0xAA222222);g.outline(x,y,w,22,d?0xFF00FF88:0xFF777777);g.text(mc.font,Component.literal(s),x+(w-mc.font.width(s))/2,y+7,0xFFFFFFFF,true);}
    private static void memory(GuiGraphicsExtractor g,Minecraft mc,int x,int y){Runtime r=Runtime.getRuntime();long used=r.totalMemory()-r.freeMemory();text(g,mc,String.format(Locale.ROOT,"RAM: %d / %d MB",used/1048576,r.maxMemory()/1048576),x,y);}
}
