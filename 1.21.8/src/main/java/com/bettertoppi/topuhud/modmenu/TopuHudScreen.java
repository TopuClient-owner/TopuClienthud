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
    private void clearAndBuild(){clearChildren();int left=24,sidebar=148;searchBox=new TextFieldWidget(textRenderer,left+sidebar+18,22,width-left-sidebar-42,20,Text.literal("Search utilities"));searchBox.setMaxLength(40);searchBox.setPlaceholder(Text.literal("Search 50 utilities..."));searchBox.setChangedListener(s->{scroll=0;rebuildButtons();});addDrawableChild(searchBox);int y=62;addCategory("ALL",null,left,y);y+=26;for(TopuUtilityManager.Category c:TopuUtilityManager.Category.values()){addCategory(pretty(c.name()),c,left,y);y+=26;}rebuildButtons();}
    private void addCategory(String label,TopuUtilityManager.Category c,int x,int y){addDrawableChild(ButtonWidget.builder(Text.literal(label),b->{category=c;scroll=0;rebuildButtons();}).dimensions(x,y,132,22).build());}
    private void rebuildButtons(){List<net.minecraft.client.gui.widget.ClickableWidget> remove=new ArrayList<>();for(var child:children())if(child instanceof ButtonWidget b&&b.getX()>160)remove.add(b);remove.forEach(this::remove);TopuUtilityManager.Utility[] u=TopuUtilityManager.search(searchBox==null?"":searchBox.getText(),category);int start=Math.max(0,Math.min(scroll,Math.max(0,u.length-12))),visible=Math.min(12,u.length-start),left=190;int cw=Math.max(170,(width-left-24)/2);for(int i=0;i<visible;i++){TopuUtilityManager.Utility x=u[start+i];int col=i%2,row=i/2,px=left+col*cw,py=58+row*43;ButtonWidget b=ButtonWidget.builder(Text.literal(x.name()+"  •  "+(TopuUtilityManager.isEnabled(x)?"ON":"OFF")),btn->{boolean v=!TopuUtilityManager.isEnabled(x);TopuUtilityManager.setEnabled(x,v);ConfigManager.save();btn.setMessage(Text.literal(x.name()+"  •  "+(v?"ON":"OFF")));}).dimensions(px,py,cw-10,34).build();addDrawableChild(b);}}
    private static String pretty(String v){return v.substring(0,1)+v.substring(1).toLowerCase();}
    @Override public boolean mouseScrolled(double x,double y,double h,double v){if(x>175&&v!=0){TopuUtilityManager.Utility[] u=TopuUtilityManager.search(searchBox==null?"":searchBox.getText(),category);int max=Math.max(0,u.length-12);scroll+=v<0?2:-2;scroll=Math.max(0,Math.min(scroll,max));rebuildButtons();return true;}return super.mouseScrolled(x,y,h,v);}
    @Override public void close(){ConfigManager.save();MinecraftClient.getInstance().setScreen(parent);}
    @Override public void render(DrawContext d,int mx,int my,float delta){d.fill(0,0,width,height,0xF20A0D12);d.fill(0,0,width,56,0xFF111722);d.fill(0,56,166,height,0xFF0E131C);d.fill(166,56,width,57,0xFF293243);d.drawText(textRenderer,Text.literal("TOPU"),24,20,0xFFFFFFFF,true);d.drawText(textRenderer,Text.literal("UTILITY HUD"),67,20,0xFF9AA7BC,false);d.drawText(textRenderer,Text.literal("50 BUILT-IN UTILITIES"),24,43,0xFF6F7D91,false);d.drawText(textRenderer,Text.literal("RIGHT SHIFT"),width-105,43,0xFF7F8CA1,false);d.drawText(textRenderer,Text.literal(category==null?"ALL UTILITIES":pretty(category.name())),190,42,0xFFB9C6D9,true);d.drawText(textRenderer,Text.literal("Scroll to browse"),width-115,42,0xFF657287,false);super.render(d,mx,my,delta);}
}
