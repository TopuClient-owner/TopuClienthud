package com.bettertoppi.topuhud.modmenu;

import com.bettertoppi.topuhud.config.ConfigManager;
import com.bettertoppi.topuhud.config.TopuHudConfig;
import com.bettertoppi.topuhud.hud.HudManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

public final class TopuScreenEditor extends Screen {
    private final Screen parent;
    private final TopuHudConfig config=ConfigManager.get();
    private HudManager.HudId draggingHud;
    private TopuUtilityManager.Utility draggingUtility;
    private int offsetX,offsetY;

    public TopuScreenEditor(Screen p){super(Component.literal("Topu HUD Editor"));parent=p;}

    @Override protected void init(){
        addRenderableWidget(Button.builder(Component.literal("DONE"),b->{ConfigManager.save();switchScreen(parent);})
                .bounds(width/2-55,height-30,110,22).build());
    }

    private static void switchScreen(Screen s){
        Minecraft mc=Minecraft.getInstance();
        try{if(mc.gui!=null){var m=mc.gui.getClass().getMethod("setScreen",Screen.class);m.invoke(mc.gui,s);return;}}catch(Exception ignored){}
        try{var m=mc.getClass().getMethod("setScreen",Screen.class);m.invoke(mc,s);}catch(Exception ignored){}
    }

    @Override public void onClose(){ConfigManager.save();switchScreen(parent);}

    private boolean utilityVisible(TopuUtilityManager.Utility u){
        String id=u.id();
        return !id.equals("zoom")&&!id.equals("fullbright")&&!id.equals("fpslimit")&&!id.equals("lowlatency")
                &&!id.equals("cleanhud")&&!id.equals("minimalparticles")&&!id.equals("itemphysics")&&!id.equals("hitsound")
                &&TopuUtilityManager.isEnabled(u);
    }

    private int utilityIndex(TopuUtilityManager.Utility target){
        int i=0;for(TopuUtilityManager.Utility u:TopuUtilityManager.ALL){if(utilityVisible(u)){if(u.id().equals(target.id()))return i;i++;}}return -1;
    }
    private int[] utilityPos(TopuUtilityManager.Utility u){int i=utilityIndex(u);return ConfigManager.getUtilityPosition(u.id(),250,72+i*24);}
    private void setUtilityPos(TopuUtilityManager.Utility u,int x,int y){ConfigManager.setUtilityPosition(u.id(),x,y);}

    @Override public boolean mouseClicked(MouseButtonEvent e,boolean dbl){
        if(super.mouseClicked(e,dbl))return true;
        if(e.button()!=0)return false;
        for(int i=HudManager.HudId.values().length-1;i>=0;i--){HudManager.HudId id=HudManager.HudId.values()[i];if(!HudManager.isEnabledForEditor(id))continue;int[] p=HudManager.getPositionForEditor(config,id);int w=HudManager.getWidthForEditor(id),h=HudManager.getHeightForEditor(id);if(e.x()>=p[0]&&e.x()<=p[0]+w&&e.y()>=p[1]&&e.y()<=p[1]+h){draggingHud=id;draggingUtility=null;offsetX=(int)e.x()-p[0];offsetY=(int)e.y()-p[1];return true;}}
        for(int i=TopuUtilityManager.ALL.length-1;i>=0;i--){TopuUtilityManager.Utility u=TopuUtilityManager.ALL[i];if(!utilityVisible(u))continue;int[] p=utilityPos(u);if(e.x()>=p[0]&&e.x()<=p[0]+220&&e.y()>=p[1]&&e.y()<=p[1]+20){draggingUtility=u;draggingHud=null;offsetX=(int)e.x()-p[0];offsetY=(int)e.y()-p[1];return true;}}
        return false;
    }

    @Override public boolean mouseDragged(MouseButtonEvent e,double dx,double dy){
        if(e.button()!=0)return super.mouseDragged(e,dx,dy);
        if(draggingHud!=null){int w=HudManager.getWidthForEditor(draggingHud),h=HudManager.getHeightForEditor(draggingHud);int x=Math.max(0,Math.min(width-w,(int)e.x()-offsetX));int y=Math.max(34,Math.min(height-36-h,(int)e.y()-offsetY));HudManager.setPositionForEditor(config,draggingHud,x,y);return true;}
        if(draggingUtility!=null){int x=Math.max(0,Math.min(width-220,(int)e.x()-offsetX));int y=Math.max(34,Math.min(height-36-20,(int)e.y()-offsetY));setUtilityPos(draggingUtility,x,y);return true;}
        return super.mouseDragged(e,dx,dy);
    }

    @Override public boolean mouseReleased(MouseButtonEvent e){if(e.button()==0&&(draggingHud!=null||draggingUtility!=null)){draggingHud=null;draggingUtility=null;ConfigManager.save();return true;}return super.mouseReleased(e);}

    @Override public void extractRenderState(GuiGraphicsExtractor g,int mx,int my,float d){
        g.fill(0,0,width,height,0xEE080B12);g.fill(0,0,width,52,0xFF111827);
        g.text(font,Component.literal("TOPU HUD EDITOR"),18,12,0xFFFFFFFF,true);
        g.text(font,Component.literal("Drag each HUD module separately"),18,31,0xFF9AA6B7,false);
        for(HudManager.HudId id:HudManager.HudId.values()){
            if(!HudManager.isEnabledForEditor(id))continue;int[] p=HudManager.getPositionForEditor(config,id);int w=HudManager.getWidthForEditor(id),h=HudManager.getHeightForEditor(id);boolean s=draggingHud==id;g.fill(p[0],p[1],p[0]+w,p[1]+h,s?0xAA245C46:0xAA18212E);g.outline(p[0],p[1],w,h,s?0xFF38E8A0:0xFF70809A);g.text(font,Component.literal(display(id)),p[0]+5,p[1]+5,s?0xFF38E8A0:0xFFFFFFFF,true);
        }
        for(TopuUtilityManager.Utility u:TopuUtilityManager.ALL){if(!utilityVisible(u))continue;int[] p=utilityPos(u);boolean s=draggingUtility==u;g.fill(p[0],p[1],p[0]+220,p[1]+20,s?0xAA245C46:0xAA18212E);g.outline(p[0],p[1],220,20,s?0xFF38E8A0:0xFF58677D);g.text(font,Component.literal(u.name()),p[0]+5,p[1]+5,s?0xFF38E8A0:0xFFE7EDF7,true);}
        super.extractRenderState(g,mx,my,d);
    }
    private static String display(HudManager.HudId id){return switch(id){case ARMOR->"Armor";case FPS->"FPS";case PING->"Ping";case TPS->"TPS";case CPS->"CPS";case COMBO->"Combo";case TOTEM->"Totems";case POTION->"Potions";case EFFECTS->"Effects";case GAPPLE->"Gapples";case WARNING->"Armor Warning";case ENEMY->"Enemy HP";case COOLDOWN->"Attack Cooldown";case BLOCK_OVERLAY->"Block Overlay";case KEYSTROKES->"Keystrokes";case MEMORY->"Memory";};}
}
