package com.bettertoppi.topuhud.modmenu;

import com.bettertoppi.topuhud.config.ConfigManager;
import com.bettertoppi.topuhud.config.TopuHudConfig;
import com.bettertoppi.topuhud.hud.HudManager;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public final class TopuHudScreen extends Screen {
    private final Screen parent;
    private static final String[][] MODULES={
        {"Armor HUD","armor"},{"FPS Counter","fps"},{"Ping Display","ping"},{"Server TPS","tps"},
        {"CPS Display","cps"},{"Combo Counter","combo"},{"Totem Counter","totem"},{"Potion Effects","effects"},
        {"Potion Counter","potion"},{"Gapple Counter","gapple"},{"Auto Sprint","sprint"},{"Toggle Sneak","sneak"},
        {"Armor Warning","warning"},{"Enemy Health","enemy"},{"Attack Cooldown","cooldown"}
    };

    public TopuHudScreen(Screen parent){super(Text.literal("Topu HUD"));this.parent=parent;}

    @Override protected void init(){
        int x=width/2-220,y=54;
        for(String[] m:MODULES){
            String name=m[0],id=m[1];
            addDrawableChild(ButtonWidget.builder(Text.literal(label(name,id)),b->{toggle(id);b.setMessage(Text.literal(label(name,id)));})
                    .dimensions(x,y,205,21).build());
            y+=24;
            if(y>height-70){y=54;x+=214;}
        }
        addDrawableChild(ButtonWidget.builder(Text.literal("Edit HUD [Right Ctrl]"),b->HudManager.setEditMode(true))
                .dimensions(width/2-105,height-42,210,22).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Close"),b->close())
                .dimensions(width-82,10,68,20).build());
    }

    private String label(String n,String id){return n+" ["+(enabled(id)?"ON":"OFF")+"]";}
    private boolean enabled(String id){TopuHudConfig c=ConfigManager.get();return switch(id){
        case "armor"->c.armorHud;case "fps"->c.fpsCounter;case "ping"->c.pingDisplay;case "tps"->c.tpsDisplay;
        case "cps"->c.cpsDisplay;case "combo"->c.comboCounter;case "totem"->c.totemCounter;case "effects"->c.potionEffects;
        case "potion"->c.potionCounter;case "gapple"->c.gappleCounter;case "sprint"->c.autoSprint;case "sneak"->c.toggleSneak;
        case "warning"->c.armorWarning;case "enemy"->c.enemyHealth;case "cooldown"->c.cooldown;default->false;};}
    private void toggle(String id){TopuHudConfig c=ConfigManager.get();switch(id){
        case "armor"->c.armorHud=!c.armorHud;case "fps"->c.fpsCounter=!c.fpsCounter;case "ping"->c.pingDisplay=!c.pingDisplay;
        case "tps"->c.tpsDisplay=!c.tpsDisplay;case "cps"->c.cpsDisplay=!c.cpsDisplay;case "combo"->c.comboCounter=!c.comboCounter;
        case "totem"->c.totemCounter=!c.totemCounter;case "effects"->c.potionEffects=!c.potionEffects;case "potion"->c.potionCounter=!c.potionCounter;
        case "gapple"->c.gappleCounter=!c.gappleCounter;case "sprint"->c.autoSprint=!c.autoSprint;case "sneak"->c.toggleSneak=!c.toggleSneak;
        case "warning"->c.armorWarning=!c.armorWarning;case "enemy"->c.enemyHealth=!c.enemyHealth;case "cooldown"->c.cooldown=!c.cooldown;}
        ConfigManager.save();}
    @Override public void render(DrawContext d,int mx,int my,float dt){
        d.fill(0,0,width,height,0xEA101014);d.fill(0,0,width,42,0xFF18181D);
        d.drawText(textRenderer,Text.literal("TOPU HUD"),16,12,0x00FF88,true);
        d.drawText(textRenderer,Text.literal("Right Ctrl = move HUD • Right Alt = toggle sneak"),112,12,0xAAAAAA,false);
        super.render(d,mx,my,dt);
    }
    @Override public void close(){ConfigManager.save();HudManager.setMenuOpen(false);if(client!=null)client.setScreen(parent);}
}
