package com.bettertoppi.topuhud.modmenu;

import com.bettertoppi.topuhud.config.ConfigManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import java.util.ArrayList;
import java.util.List;

public final class TopuHudScreen extends Screen {
    private final Screen parent; private TextFieldWidget searchBox; private TopuUtilityManager.Category category; private int scroll;
    public TopuHudScreen(Screen parent){super(Text.literal("Topu Utility HUD"));this.parent=parent;}
    @Override protected void init(){clearAndBuild();}
    private void clearAndBuild(){clearChildren();int sidebar=172;searchBox=new TextFieldWidget(textRenderer,sidebar+28,18,width-sidebar-56,24,Text.literal("Search"));searchBox.setMaxLength(40);searchBox.setPlaceholder(Text.literal("Search 50 utilities..."));searchBox.setChangedListener(s->{scroll=0;rebuildButtons();});addDrawableChild(searchBox);int y=76;addCategory("ALL",null,22,y);y+=30;for(TopuUtilityManager.Category c:TopuUtilityManager.Category.values()){addCategory(pretty(c.name()),c,22,y);y+=30;}rebuildButtons();}
    private void addCategory(String label,TopuUtilityManager.Category c,int x,int y){addDrawableChild(ButtonWidget.builder(Text.literal(label),b->{category=c;scroll=0;rebuildButtons();}).dimensions(x,y,142,24).build());}
    private void rebuildButtons(){List<net.minecraft.client.gui.widget.ClickableWidget> remove=new ArrayList<>();for(var child:children())if(child instanceof ButtonWidget b&&b.getX()>185)remove.add(b);remove.forEach(this::remove);TopuUtilityManager.Utility[] u=TopuUtilityManager.search(searchBox==null?"":searchBox.getText(),category);int start=Math.max(0,Math.min(scroll,Math.max(0,u.length-10))),visible=Math.min(10,u.length-start),left=202;int cw=Math.max(180,(width-left-30)/2);for(int i=0;i<visible;i++){TopuUtilityManager.Utility x=u[start+i];int col=i%2,row=i/2,px=left+col*cw,py=82+row*52;boolean enabled=TopuUtilityManager.isEnabled(x);ButtonWidget b=ButtonWidget.builder(Text.literal((enabled?"● ":"○ ")+x.name()+"  "+(enabled?"ON":"OFF")),btn->{boolean v=!TopuUtilityManager.isEnabled(x);TopuUtilityManager.setEnabled(x,v);ConfigManager.save();btn.setMessage(Text.literal((v?"● ":"○ ")+x.name()+"  "+(v?"ON":"OFF")));}).dimensions(px,py,cw-12,42).build();addDrawableChild(b);}}
    private static String pretty(String v){return v.substring(0,1)+v.substring(1).toLowerCase();}
    @Override public boolean mouseScrolled(double x,double y,double h,double v){if(x>185&&v!=0){TopuUtilityManager.Utility[] u=TopuUtilityManager.search(searchBox==null?"":searchBox.getText(),category);int max=Math.max(0,u.length-10);scroll+=v<0?2:-2;scroll=Math.max(0,Math.min(scroll,max));rebuildButtons();return true;}return super.mouseScrolled(x,y,h,v);}
    @Override public void close(){ConfigManager.save();MinecraftClient.getInstance().setScreen(parent);}
    @Override public void render(DrawContext d,int mx,int my,float delta){d.fill(0,0,width,height,0xFF080B11);d.fill(0,0,width,62,0xFF101722);d.fill(0,62,172,height,0xFF0C1119);d.fill(172,62,174,height,0xFF263244);d.drawText(textRenderer,Text.literal("TOPU"),24,20,0xFFFFFFFF,true);d.drawText(textRenderer,Text.literal("HUD"),76,20,0xFF6FA8FF,true);d.drawText(textRenderer,Text.literal("50 BUILT-IN UTILITIES"),24,40,0xFF7F8CA1,false);d.drawText(textRenderer,Text.literal("RIGHT SHIFT"),width-92,40,0xFF6F7D91,false);String active=category==null?"ALL UTILITIES":pretty(category.name());d.drawText(textRenderer,Text.literal(active),202,48,0xFFE5ECF7,true);TopuUtilityManager.Utility[] filtered=TopuUtilityManager.search(searchBox==null?"":searchBox.getText(),category);d.drawText(textRenderer,Text.literal(filtered.length+" modules"),width-88,48,0xFF6F7D91,false);d.drawText(textRenderer,Text.literal("CATEGORIES"),22,64,0xFF5E6D82,true);d.drawText(textRenderer,Text.literal("TOPU UTILITY CONTROL"),202,72,0xFF65758A,false);if(filtered.length==0)d.drawText(textRenderer,Text.literal("No utilities found"),202,100,0xFF8B97AA,false);super.render(d,mx,my,delta);}
}
