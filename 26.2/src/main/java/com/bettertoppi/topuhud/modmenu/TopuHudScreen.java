package com.bettertoppi.topuhud.modmenu;

import com.bettertoppi.topuhud.config.ConfigManager;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class TopuHudScreen extends Screen {
    private final Screen parent; private final TopuUtilityManager.Category category; private final int page;
    public TopuHudScreen(Screen parent){this(parent,null,0);} public TopuHudScreen(Screen parent,TopuUtilityManager.Category category){this(parent,category,0);} private TopuHudScreen(Screen parent,TopuUtilityManager.Category category,int page){super(Component.literal("TopuHUD ClickGUI"));this.parent=parent;this.category=category;this.page=page;}
    @Override protected void init(){
        addRenderableWidget(Button.builder(Component.literal("ALL"),b->open(null,0)).bounds(18,72,120,20).build());
        int y=96;for(TopuUtilityManager.Category c:TopuUtilityManager.Category.values()){final var cat=c;addRenderableWidget(Button.builder(Component.literal(pretty(c.name())),b->open(cat,0)).bounds(18,y,120,20).build());y+=24;}
        addRenderableWidget(Button.builder(Component.literal("HUD EDITOR"),b->minecraft.setScreenAndShow(new TopuScreenEditor(this))).bounds(18,height-50,120,20).build());
        addRenderableWidget(Button.builder(Component.literal("DONE"),b->minecraft.setScreenAndShow(parent)).bounds(18,height-26,120,20).build()); addUtilities();
    }
    private void addUtilities(){TopuUtilityManager.Utility[] all=TopuUtilityManager.search("",category);int start=Math.min(page*12,Math.max(0,all.length-1));int visible=Math.min(12,all.length-start);for(int i=0;i<visible;i++){TopuUtilityManager.Utility u=all[start+i];int col=i%2,row=i/2,x=170+col*210,y=88+row*42;addRenderableWidget(Button.builder(Component.literal(label(u)),b->{TopuUtilityManager.setEnabled(u,!TopuUtilityManager.isEnabled(u));ConfigManager.save();b.setMessage(Component.literal(label(u)));}).bounds(x,y,198,28).build());}if(all.length>12){addRenderableWidget(Button.builder(Component.literal("<"),b->minecraft.setScreenAndShow(new TopuHudScreen(parent,category,Math.max(0,page-1)))).bounds(width-90,height-30,30,20).build());addRenderableWidget(Button.builder(Component.literal(">"),b->minecraft.setScreenAndShow(new TopuHudScreen(parent,category,Math.min((all.length-1)/12,page+1)))).bounds(width-54,height-30,30,20).build());}}
    private void open(TopuUtilityManager.Category c,int p){minecraft.setScreenAndShow(new TopuHudScreen(parent,c,p));}
    private static String label(TopuUtilityManager.Utility u){return(TopuUtilityManager.isEnabled(u)?"● ON  ":"○ OFF ")+u.name();}
    private static String pretty(String s){return s.substring(0,1)+s.substring(1).toLowerCase();}
    @Override public void extractRenderState(GuiGraphicsExtractor g,int mouseX,int mouseY,float delta){g.fill(0,0,width,height,0xFF070A10);g.fill(0,0,width,60,0xFF0E141E);g.fill(0,60,154,height,0xFF0A1018);g.fill(154,60,156,height,0xFF263246);g.fill(22,58,136,60,0xFF6FA8FF);g.text(font,"TOPU",22,18,0xFFFFFFFF,true);g.text(font,"HUD",68,18,0xFF6FA8FF,true);g.text(font,"UTILITY CONTROL",22,38,0xFF8390A4,false);String title=category==null?"ALL UTILITIES":pretty(category.name());g.text(font,title,170,66,0xFFEAF1FB,true);g.text(font,"50 MODULES",width-85,66,0xFF78869A,false);super.extractRenderState(g,mouseX,mouseY,delta);}
}
