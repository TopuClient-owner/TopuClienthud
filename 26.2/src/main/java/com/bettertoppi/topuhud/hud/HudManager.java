package com.bettertoppi.topuhud.hud;

import com.bettertoppi.topuhud.config.ConfigManager;
import com.bettertoppi.topuhud.config.TopuHudConfig;
import com.bettertoppi.topuhud.modmenu.TopuHudScreen;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import org.lwjgl.glfw.GLFW;

import java.lang.reflect.Method;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Locale;

/**
 * Complete core TopuHUD renderer for Minecraft 26.x.
 *
 * This is intentionally a single feature-complete manager so the three
 * 26.x projects expose the same HUD behaviour and editor API.
 */
public final class HudManager {
    public enum HudId {
        ARMOR, FPS, PING, TPS, CPS, COMBO, TOTEM, POTION, EFFECTS,
        GAPPLE, WARNING, ENEMY, COOLDOWN, BLOCK_OVERLAY, KEYSTROKES, MEMORY
    }

    private static KeyMapping menuKey;
    private static KeyMapping editKey;
    private static KeyMapping sneakKey;

    private static boolean editMode;
    private static boolean sneakToggled;
    private static boolean mouseWasDown;
    private static HudId draggingHud;
    private static int dragOffsetX;
    private static int dragOffsetY;

    private static final Deque<Long> clickTimes = new ArrayDeque<>();
    private static long lastHitTime;
    private static int combo;
    private static double tpsEstimate = 20.0;
    private static long lastTickNanos = System.nanoTime();

    private HudManager() {}

    public static void initialize() {
        KeyMapping.Category category = KeyMapping.Category.register(
                Identifier.fromNamespaceAndPath("topuhud", "hud")
        );
        menuKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.topuhud.menu", InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_RIGHT_SHIFT, category));
        editKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.topuhud.edit", InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_RIGHT_CONTROL, category));
        sneakKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.topuhud.sneak", InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_RIGHT_ALT, category));

        ClientTickEvents.END_CLIENT_TICK.register(HudManager::tick);
        HudElementRegistry.addLast(
                Identifier.fromNamespaceAndPath("topuhud", "core_hud"),
                HudManager::render
        );
    }

    public static void registerClick() {
        clickTimes.addLast(System.currentTimeMillis());
    }

    public static void registerHit() {
        registerClick();
        lastHitTime = System.currentTimeMillis();
        combo++;
    }

    public static boolean isEditMode() { return editMode; }

    public static void setEditMode(boolean value) {
        editMode = value;
        ConfigManager.get().editMode = value;
        if (!value) {
            draggingHud = null;
            ConfigManager.save();
        }
    }

    private static void tick(Minecraft mc) {
        if (menuKey != null) {
            while (menuKey.consumeClick()) {
                if (mc.screen == null) openScreen(mc, new TopuHudScreen(null));
            }
        }
        if (editKey != null) {
            while (editKey.consumeClick()) setEditMode(!editMode);
        }
        if (sneakKey != null) {
            while (sneakKey.consumeClick()) sneakToggled = !sneakToggled;
        }

        long now = System.nanoTime();
        long delta = now - lastTickNanos;
        lastTickNanos = now;
        if (delta > 0) {
            double rate = Math.min(20.0, 1_000_000_000.0 / delta);
            tpsEstimate = Mth.clamp(tpsEstimate * .94 + rate * .06, 0.0, 20.0);
        }

        long cutoff = System.currentTimeMillis() - 1000L;
        while (!clickTimes.isEmpty() && clickTimes.peekFirst() < cutoff) clickTimes.removeFirst();
        if (System.currentTimeMillis() - lastHitTime > 1500L) combo = 0;

        Player player = mc.player;
        if (player == null) return;
        TopuHudConfig c = ConfigManager.get();

        if (c.toggleSneak) player.setCrouching(sneakToggled);
        if (c.autoSprint && mc.screen == null && mc.options.keyUp.isDown()
                && !player.isCrouching() && !player.isPassenger()
                && player.getFoodData().getFoodLevel() > 6) {
            player.setSprinting(true);
        }

        if (editMode && mc.screen == null) updateDrag(mc);
    }

    private static void openScreen(Minecraft mc, Screen screen) {
        try {
            Method m = mc.getClass().getMethod("setScreen", Screen.class);
            m.invoke(mc, screen);
            return;
        } catch (ReflectiveOperationException ignored) {}
        try {
            Object gui = mc.gui;
            Method m = gui.getClass().getMethod("setScreen", Screen.class);
            m.invoke(gui, screen);
        } catch (ReflectiveOperationException ignored) {}
    }

    private static int[] pos(TopuHudConfig c, HudId id) {
        return switch (id) {
            case ARMOR -> new int[]{c.armorX, c.armorY};
            case FPS -> new int[]{c.fpsX, c.fpsY};
            case PING -> new int[]{c.pingX, c.pingY};
            case TPS -> new int[]{c.tpsX, c.tpsY};
            case CPS -> new int[]{c.cpsX, c.cpsY};
            case COMBO -> new int[]{c.comboX, c.comboY};
            case TOTEM -> new int[]{c.totemX, c.totemY};
            case POTION -> new int[]{c.potionX, c.potionY};
            case EFFECTS -> new int[]{c.effectsX, c.effectsY};
            case GAPPLE -> new int[]{c.gappleX, c.gappleY};
            case WARNING -> new int[]{c.warningX, c.warningY};
            case ENEMY -> new int[]{c.enemyHealthX, c.enemyHealthY};
            case COOLDOWN -> new int[]{c.cooldownX, c.cooldownY};
            case BLOCK_OVERLAY -> new int[]{c.blockOverlayX, c.blockOverlayY};
            case KEYSTROKES -> new int[]{c.keystrokesX, c.keystrokesY};
            case MEMORY -> new int[]{c.memoryX, c.memoryY};
        };
    }

    private static void setPos(TopuHudConfig c, HudId id, int x, int y) {
        switch (id) {
            case ARMOR -> { c.armorX=x; c.armorY=y; }
            case FPS -> { c.fpsX=x; c.fpsY=y; }
            case PING -> { c.pingX=x; c.pingY=y; }
            case TPS -> { c.tpsX=x; c.tpsY=y; }
            case CPS -> { c.cpsX=x; c.cpsY=y; }
            case COMBO -> { c.comboX=x; c.comboY=y; }
            case TOTEM -> { c.totemX=x; c.totemY=y; }
            case POTION -> { c.potionX=x; c.potionY=y; }
            case EFFECTS -> { c.effectsX=x; c.effectsY=y; }
            case GAPPLE -> { c.gappleX=x; c.gappleY=y; }
            case WARNING -> { c.warningX=x; c.warningY=y; }
            case ENEMY -> { c.enemyHealthX=x; c.enemyHealthY=y; }
            case COOLDOWN -> { c.cooldownX=x; c.cooldownY=y; }
            case BLOCK_OVERLAY -> { c.blockOverlayX=x; c.blockOverlayY=y; }
            case KEYSTROKES -> { c.keystrokesX=x; c.keystrokesY=y; }
            case MEMORY -> { c.memoryX=x; c.memoryY=y; }
        }
    }

    private static boolean enabled(TopuHudConfig c, HudId id) {
        return switch (id) {
            case ARMOR -> c.armorHud;
            case FPS -> c.fpsCounter;
            case PING -> c.pingDisplay;
            case TPS -> c.tpsDisplay;
            case CPS -> c.cpsDisplay;
            case COMBO -> c.comboCounter;
            case TOTEM -> c.totemCounter;
            case POTION -> c.potionCounter;
            case EFFECTS -> c.potionEffects;
            case GAPPLE -> c.gappleCounter;
            case WARNING -> c.armorWarning;
            case ENEMY -> c.enemyHealth;
            case COOLDOWN -> c.cooldown;
            case BLOCK_OVERLAY -> c.blockOverlay;
            case KEYSTROKES -> c.keystrokes;
            case MEMORY -> c.memory;
        };
    }

    public static int[] getPositionForEditor(TopuHudConfig c, HudId id) { return pos(c, id); }
    public static void setPositionForEditor(TopuHudConfig c, HudId id, int x, int y) { setPos(c,id,x,y); }
    public static boolean isEnabledForEditor(HudId id) { return enabled(ConfigManager.get(), id); }
    public static int getWidthForEditor(HudId id) { return width(id); }
    public static int getHeightForEditor(HudId id) { return height(id); }

    private static int width(HudId id) {
        return switch (id) {
            case ARMOR -> 130; case EFFECTS -> 190; case WARNING -> 190;
            case ENEMY -> 170; case COOLDOWN -> 130; case KEYSTROKES -> 115;
            case BLOCK_OVERLAY -> 190; case MEMORY -> 180; default -> 155;
        };
    }
    private static int height(HudId id) {
        return switch (id) {
            case ARMOR -> 30; case EFFECTS -> 82; case KEYSTROKES -> 94;
            case BLOCK_OVERLAY, MEMORY -> 26; default -> 24;
        };
    }

    private static void updateDrag(Minecraft mc) {
        boolean down = GLFW.glfwGetMouseButton(mc.getWindow().handle(), GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;
        int mx = (int)Math.round(mc.mouseHandler.xpos() * mc.getWindow().getGuiScaledWidth() / (double)mc.getWindow().getScreenWidth());
        int my = (int)Math.round(mc.mouseHandler.ypos() * mc.getWindow().getGuiScaledHeight() / (double)mc.getWindow().getScreenHeight());
        if (down && !mouseWasDown) {
            HudId[] ids = HudId.values();
            TopuHudConfig c = ConfigManager.get();
            for (int i=ids.length-1;i>=0;i--) {
                HudId id=ids[i]; if(!enabled(c,id)) continue;
                int[] p=pos(c,id);
                if(mx>=p[0]&&mx<=p[0]+width(id)&&my>=p[1]&&my<=p[1]+height(id)) {
                    draggingHud=id; dragOffsetX=mx-p[0]; dragOffsetY=my-p[1]; break;
                }
            }
        }
        if (down && draggingHud != null) {
            TopuHudConfig c=ConfigManager.get(); int nx=mx-dragOffsetX, ny=my-dragOffsetY;
            nx=Mth.clamp(nx,0,Math.max(0,mc.getWindow().getGuiScaledWidth()-width(draggingHud)));
            ny=Mth.clamp(ny,22,Math.max(22,mc.getWindow().getGuiScaledHeight()-height(draggingHud)));
            setPos(c,draggingHud,nx,ny);
        }
        if(!down&&mouseWasDown){draggingHud=null;ConfigManager.save();}
        mouseWasDown=down;
    }

    private static void render(GuiGraphicsExtractor g, net.minecraft.client.DeltaTracker delta) {
        Minecraft mc=Minecraft.getInstance(); Player p=mc.player;
        if(p==null||mc.level==null)return;
        TopuHudConfig c=ConfigManager.get();
        if(c.armorHud) armor(g,mc,p,c.armorX,c.armorY);
        if(c.fpsCounter) text(g,mc,"FPS: "+mc.getFps(),c.fpsX,c.fpsY);
        if(c.pingDisplay) ping(g,mc,c.pingX,c.pingY);
        if(c.tpsDisplay) text(g,mc,String.format(Locale.ROOT,"TPS: %.1f",tpsEstimate),c.tpsX,c.tpsY);
        if(c.cpsDisplay) text(g,mc,"CPS: "+clickTimes.size(),c.cpsX,c.cpsY);
        if(c.comboCounter) text(g,mc,"Combo: "+combo,c.comboX,c.comboY);
        if(c.totemCounter) itemCount(g,mc,p,Items.TOTEM_OF_UNDYING,"Totems",c.totemX,c.totemY);
        if(c.potionCounter) potionCount(g,mc,p,c.potionX,c.potionY);
        if(c.potionEffects) effects(g,mc,p,c.effectsX,c.effectsY);
        if(c.gappleCounter) gappleCount(g,mc,p,c.gappleX,c.gappleY);
        if(c.armorWarning) armorWarning(g,mc,p,c.warningX,c.warningY);
        if(c.enemyHealth) enemy(g,mc,c.enemyHealthX,c.enemyHealthY);
        if(c.cooldown) cooldown(g,mc,p,c.cooldownX,c.cooldownY);
        if(c.blockOverlay) block(g,mc,c.blockOverlayX,c.blockOverlayY);
        if(c.keystrokes) keys(g,mc,c.keystrokesX,c.keystrokesY);
        if(c.memory) memory(g,mc,c.memoryX,c.memoryY);
        if(editMode) editorOverlay(g,mc,c);
    }

    private static void text(GuiGraphicsExtractor g,Minecraft mc,String s,int x,int y){g.text(mc.font,Component.literal(s),x,y,0xFFFFFFFF,true);}

    private static int count(Player p,Item item){
        int n=0;
        for(int i=0;i<p.getInventory().getContainerSize();i++){ItemStack s=p.getInventory().getItem(i);if(s.is(item))n+=s.getCount();}
        ItemStack off=p.getOffhandItem();if(off.is(item))n+=off.getCount();return n;
    }
    private static void itemCount(GuiGraphicsExtractor g,Minecraft mc,Player p,Item item,String label,int x,int y){text(g,mc,label+": "+count(p,item),x,y);}
    private static void potionCount(GuiGraphicsExtractor g,Minecraft mc,Player p,int x,int y){text(g,mc,"Potions: "+(count(p,Items.POTION)+count(p,Items.SPLASH_POTION)+count(p,Items.LINGERING_POTION)),x,y);}
    private static void gappleCount(GuiGraphicsExtractor g,Minecraft mc,Player p,int x,int y){text(g,mc,"Gapples: "+(count(p,Items.GOLDEN_APPLE)+count(p,Items.ENCHANTED_GOLDEN_APPLE)),x,y);}

    private static void armor(GuiGraphicsExtractor g,Minecraft mc,Player p,int x,int y){
        int i=0; for(ItemStack s:p.getInventory().getArmor()){
            if(s.isEmpty()){i++;continue;}
            int pct=s.isDamageableItem()?Math.max(0,(s.getMaxDamage()-s.getDamageValue())*100/s.getMaxDamage()):100;
            text(g,mc,"Armor "+pct+"%",x,y+i*13); i++;
        }
    }
    private static void ping(GuiGraphicsExtractor g,Minecraft mc,int x,int y){
        int n=-1;if(mc.getConnection()!=null&&mc.player!=null){var i=mc.getConnection().getPlayerInfo(mc.player.getUUID());if(i!=null)n=i.getLatency();}
        text(g,mc,"Ping: "+(n<0?"-":n+" ms"),x,y);
    }
    private static void effects(GuiGraphicsExtractor g,Minecraft mc,Player p,int x,int y){
        int yy=y,n=0; for(MobEffectInstance e:p.getActiveEffects()){
            text(g,mc,e.getEffect().value().getDisplayName().getString()+" "+Math.max(0,e.getDuration()/20)+"s",x,yy);
            yy+=12;if(++n>=6)break;
        }
    }
    private static void armorWarning(GuiGraphicsExtractor g,Minecraft mc,Player p,int x,int y){
        for(ItemStack s:p.getInventory().getArmor()) if(s.isDamageableItem()){
            int pct=Math.max(0,(s.getMaxDamage()-s.getDamageValue())*100/s.getMaxDamage());
            if(pct<=40){g.fill(x-4,y-3,x+190,y+22,0xAA550000);text(g,mc,"ARMOR LOW  "+pct+"%",x,y+3);return;}
        }
    }
    private static void enemy(GuiGraphicsExtractor g,Minecraft mc,int x,int y){
        if(!(mc.hitResult instanceof EntityHitResult h)||!(h.getEntity() instanceof LivingEntity e))return;
        float max=Math.max(1f,e.getMaxHealth()),hp=Math.max(0f,e.getHealth()),r=Mth.clamp(hp/max,0f,1f);
        text(g,mc,String.format(Locale.ROOT,"Enemy HP %.1f / %.1f",hp,max),x,y);
        int color=r<.3f?0xFFFF4444:r<.6f?0xFFFFFF55:0xFF55FF55;g.fill(x,y+14,x+(int)(155*r),y+18,color);
    }
    private static void cooldown(GuiGraphicsExtractor g,Minecraft mc,Player p,int x,int y){
        float r=p.getAttackStrengthScale(0);g.fill(x,y,x+120,y+8,0xAA222222);g.fill(x,y,x+(int)(120*r),y+8,r>=.99f?0xFF55FF55:0xFFFFAA33);text(g,mc,"Attack: "+Math.round(r*100)+"%",x,y+10);
    }
    private static void block(GuiGraphicsExtractor g,Minecraft mc,int x,int y){
        if(mc.hitResult instanceof BlockHitResult h&&mc.level!=null){BlockPos p=h.getBlockPos();String n=mc.level.getBlockState(p).getBlock().getName().getString();g.fill(x-4,y-3,x+190,y+22,0xAA111111);text(g,mc,"Block: "+n,x,y+3);}
    }
    private static void keys(GuiGraphicsExtractor g,Minecraft mc,int x,int y){
        String a="W "+(mc.options.keyUp.isDown()?"■":"□")+"  A "+(mc.options.keyLeft.isDown()?"■":"□")+"  S "+(mc.options.keyDown.isDown()?"■":"□")+"  D "+(mc.options.keyRight.isDown()?"■":"□");
        text(g,mc,a,x,y);text(g,mc,"SPACE "+(mc.options.keyJump.isDown()?"■":"□")+"   LMB "+(mousePressed(mc,0)?"■":"□")+"   RMB "+(mousePressed(mc,1)?"■":"□"),x,y+16);
    }
    private static boolean mousePressed(Minecraft mc,int button){return GLFW.glfwGetMouseButton(mc.getWindow().handle(),button)==GLFW.GLFW_PRESS;}
    private static void memory(GuiGraphicsExtractor g,Minecraft mc,int x,int y){Runtime r=Runtime.getRuntime();text(g,mc,"RAM: "+(r.totalMemory()-r.freeMemory())/1048576+" / "+r.maxMemory()/1048576+" MB",x,y);}

    private static void editorOverlay(GuiGraphicsExtractor g,Minecraft mc,TopuHudConfig c){
        g.fill(0,0,mc.getWindow().getGuiScaledWidth(),22,0x77000000);
        text(g,mc,"TOPU HUD EDIT MODE - RIGHT CTRL TO EXIT",8,5);
        for(HudId id:HudId.values()) if(enabled(c,id)){
            int[] p=pos(c,id);int color=draggingHud==id?0xFF00FF88:0x88777777;
            border(g,p[0]-2,p[1]-2,width(id)+4,height(id)+4,color);
        }
    }
    private static void border(GuiGraphicsExtractor g,int x,int y,int w,int h,int color){
        g.fill(x,y,x+w,y+1,color);g.fill(x,y+h-1,x+w,y+h,color);g.fill(x,y,x+1,y+h,color);g.fill(x+w-1,y,x+w,y+h,color);
    }
}
