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
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.glfw.GLFW;
import java.util.ArrayDeque;
import java.util.Deque;

public final class HudManager {
    private static KeyBinding menuKey, editKey, sneakKey;
    private static boolean sneakToggled, editMode;
    private static final Deque<Long> clickTimes = new ArrayDeque<>();
    private static long lastHitTime;
    private static int combo;
    private static double tpsEstimate = 20.0;
    private static long lastTickNanos = System.nanoTime();
    public enum HudId { ARMOR,FPS,PING,TPS,CPS,COMBO,TOTEM,POTION,EFFECTS,GAPPLE,WARNING,ENEMY,COOLDOWN,BLOCK_OVERLAY,KEYSTROKES,MEMORY }
    private HudManager() {}
    public static void initialize() {
        menuKey=KeyBindingHelper.registerKeyBinding(new KeyBinding("key.topuhud.menu",InputUtil.Type.KEYSYM,GLFW.GLFW_KEY_RIGHT_SHIFT,"category.topuhud"));
        editKey=KeyBindingHelper.registerKeyBinding(new KeyBinding("key.topuhud.edit",InputUtil.Type.KEYSYM,GLFW.GLFW_KEY_RIGHT_CONTROL,"category.topuhud"));
        sneakKey=KeyBindingHelper.registerKeyBinding(new KeyBinding("key.topuhud.sneak",InputUtil.Type.KEYSYM,GLFW.GLFW_KEY_RIGHT_ALT,"category.topuhud"));
        ClientTickEvents.END_CLIENT_TICK.register(HudManager::tick);
        HudRenderCallback.EVENT.register((draw,tickCounter)->render(MinecraftClient.getInstance(),draw,tickCounter.getTickProgress(false)));
    }
    public static void setMenuOpen(boolean value) {}
    public static void setEditMode(boolean value){editMode=value;ConfigManager.get().editMode=value;MinecraftClient c=MinecraftClient.getInstance();if(value){if(c.currentScreen==null)c.mouse.unlockCursor();}else{if(c.currentScreen==null)c.mouse.lockCursor();ConfigManager.save();}}
    public static void registerClick(){clickTimes.addLast(System.currentTimeMillis());}
    public static void registerHit(){registerClick();lastHitTime=System.currentTimeMillis();combo++;}
    private static void tick(MinecraftClient c){if(menuKey!=null&&menuKey.wasPressed()&&c.currentScreen==null)c.setScreen(new TopuHudScreen(null));if(editKey!=null&&editKey.wasPressed())setEditMode(!editMode);if(sneakKey!=null&&sneakKey.wasPressed())sneakToggled=!sneakToggled;if(c.player==null)return;TopuHudConfig cfg=ConfigManager.get();if(cfg.toggleSneak)c.player.setSneaking(sneakToggled);if(cfg.autoSprint&&c.currentScreen==null&&c.options.forwardKey.isPressed()&&!c.player.isSneaking()&&c.player.getHungerManager().getFoodLevel()>6)c.player.setSprinting(true);long now=System.nanoTime(),delta=now-lastTickNanos;lastTickNanos=now;if(delta>0){double rate=Math.min(20.0,1_000_000_000.0/delta);tpsEstimate=MathHelper.clamp(tpsEstimate*.94+rate*.06,0,20);}long cutoff=System.currentTimeMillis()-1000;while(!clickTimes.isEmpty()&&clickTimes.peekFirst()<cutoff)clickTimes.removeFirst();if(System.currentTimeMillis()-lastHitTime>1500)combo=0;}
    public static int[] getPositionForEditor(TopuHudConfig c,HudId id){switch(id){case ARMOR:return new int[]{c.armorX,c.armorY};case FPS:return new int[]{c.fpsX,c.fpsY};case PING:return new int[]{c.pingX,c.pingY};case TPS:return new int[]{c.tpsX,c.tpsY};case CPS:return new int[]{c.cpsX,c.cpsY};case COMBO:return new int[]{c.comboX,c.comboY};case TOTEM:return new int[]{c.totemX,c.totemY};case POTION:return new int[]{c.potionX,c.potionY};case EFFECTS:return new int[]{c.effectsX,c.effectsY};case GAPPLE:return new int[]{c.gappleX,c.gappleY};case WARNING:return new int[]{c.warningX,c.warningY};case ENEMY:return new int[]{c.enemyHealthX,c.enemyHealthY};case COOLDOWN:return new int[]{c.cooldownX,c.cooldownY};case BLOCK_OVERLAY:return new int[]{c.blockOverlayX,c.blockOverlayY};case KEYSTROKES:return new int[]{c.keystrokesX,c.keystrokesY};case MEMORY:return new int[]{c.memoryX,c.memoryY};default:return new int[]{10,10};}}
    public static void setPositionForEditor(TopuHudConfig c,HudId id,int x,int y){switch(id){case ARMOR:c.armorX=x;c.armorY=y;break;case FPS:c.fpsX=x;c.fpsY=y;break;case PING:c.pingX=x;c.pingY=y;break;case TPS:c.tpsX=x;c.tpsY=y;break;case CPS:c.cpsX=x;c.cpsY=y;break;case COMBO:c.comboX=x;c.comboY=y;break;case TOTEM:c.totemX=x;c.totemY=y;break;case POTION:c.potionX=x;c.potionY=y;break;case EFFECTS:c.effectsX=x;c.effectsY=y;break;case GAPPLE:c.gappleX=x;c.gappleY=y;break;case WARNING:c.warningX=x;c.warningY=y;break;case ENEMY:c.enemyHealthX=x;c.enemyHealthY=y;break;case COOLDOWN:c.cooldownX=x;c.cooldownY=y;break;case BLOCK_OVERLAY:c.blockOverlayX=x;c.blockOverlayY=y;break;case KEYSTROKES:c.keystrokesX=x;c.keystrokesY=y;break;case MEMORY:c.memoryX=x;c.memoryY=y;break;}}
    public static boolean isEnabledForEditor(HudId id){TopuHudConfig c=ConfigManager.get();switch(id){case ARMOR:return c.armorHud;case FPS:return c.fpsCounter;case PING:return c.pingDisplay;case TPS:return c.tpsDisplay;case CPS:return c.cpsDisplay;case COMBO:return c.comboCounter;case TOTEM:return c.totemCounter;case POTION:return c.potionCounter;case EFFECTS:return c.potionEffects;case GAPPLE:return c.gappleCounter;case WARNING:return c.armorWarning;case ENEMY:return c.enemyHealth;case COOLDOWN:return c.cooldown;case BLOCK_OVERLAY:return c.blockOverlay;case KEYSTROKES:return c.keystrokes;case MEMORY:return c.memory;default:return false;}}
    public static int getWidthForEditor(HudId id){switch(id){case ARMOR:return 120;case EFFECTS:return 180;case WARNING:return 180;case ENEMY:return 165;case COOLDOWN:return 130;case KEYSTROKES:return 110;case BLOCK_OVERLAY:return 150;default:return 150;}}
    public static int getHeightForEditor(HudId id){switch(id){case ARMOR:return 36;case KEYSTROKES:return 70;case BLOCK_OVERLAY:return 80;case EFFECTS:return 40;default:return 18;}}
    private static void render(MinecraftClient c,DrawContext d,float delta){if(c.player==null||c.world==null)return;TopuHudConfig cfg=ConfigManager.get();draw(c,d,cfg,HudId.FPS,cfg.fpsCounter,"FPS: "+c.getCurrentFps());draw(c,d,cfg,HudId.PING,cfg.pingDisplay,"Ping: "+ping(c)+" ms");draw(c,d,cfg,HudId.TPS,cfg.tpsDisplay,"TPS: "+String.format(java.util.Locale.ROOT,"%.1f",tpsEstimate));draw(c,d,cfg,HudId.CPS,cfg.cpsDisplay,"CPS: "+clickTimes.size());draw(c,d,cfg,HudId.COMBO,cfg.comboCounter,"Combo: "+combo);draw(c,d,cfg,HudId.COOLDOWN,cfg.cooldown,"Cooldown: "+Math.round(c.player.getAttackCooldownProgress(delta)*100)+"%");draw(c,d,cfg,HudId.MEMORY,cfg.memory,"Memory: "+usedMemory()+" MB");draw(c,d,cfg,HudId.ENEMY,cfg.enemyHealth,targetText(c));draw(c,d,cfg,HudId.ARMOR,cfg.armorHud,armorText(c.player));draw(c,d,cfg,HudId.KEYSTROKES,cfg.keystrokes,keys(c));}
    private static void draw(MinecraftClient c,DrawContext d,TopuHudConfig cfg,HudId id,boolean enabled,String text){if(!enabled)return;int[] p=getPositionForEditor(cfg,id);d.fill(p[0]-3,p[1]-2,p[0]+getWidthForEditor(id),p[1]+16,0xA0101520);d.drawTextWithShadow(c.textRenderer,Text.literal(text),p[0],p[1],0xFFE7EDF7);}
    private static String armorText(PlayerEntity p){return "Armor: "+dur(p.getEquippedStack(EquipmentSlot.HEAD))+" | "+dur(p.getEquippedStack(EquipmentSlot.CHEST))+" | "+dur(p.getEquippedStack(EquipmentSlot.LEGS))+" | "+dur(p.getEquippedStack(EquipmentSlot.FEET));}
    private static String dur(ItemStack s){return s.isEmpty()?"-":(s.isDamageable()?Integer.toString(s.getMaxDamage()-s.getDamage()):"∞");}
    private static String targetText(MinecraftClient c){if(!(c.crosshairTarget instanceof EntityHitResult))return "Enemy: None";net.minecraft.entity.Entity e=((EntityHitResult)c.crosshairTarget).getEntity();if(e instanceof LivingEntity)return "Enemy: "+e.getName().getString()+" "+String.format(java.util.Locale.ROOT,"%.1f HP",((LivingEntity)e).getHealth());return "Enemy: "+e.getName().getString();}
    private static String keys(MinecraftClient c){return "WASD "+(c.options.forwardKey.isPressed()?"W":"-")+(c.options.leftKey.isPressed()?"A":"-")+(c.options.backKey.isPressed()?"S":"-")+(c.options.rightKey.isPressed()?"D":"-");}
    private static int usedMemory(){Runtime r=Runtime.getRuntime();return (int)((r.totalMemory()-r.freeMemory())/1048576L);}
    private static int ping(MinecraftClient c){try{if(c.getNetworkHandler()!=null&&c.player!=null)return c.getNetworkHandler().getPlayerListEntry(c.player.getUuid()).getLatency();}catch(Exception ignored){}return 0;}
}
