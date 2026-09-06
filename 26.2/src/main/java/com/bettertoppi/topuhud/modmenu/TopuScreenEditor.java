package com.bettertoppi.topuhud.modmenu;

import com.bettertoppi.topuhud.config.ConfigManager;
import com.bettertoppi.topuhud.config.TopuHudConfig;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

public final class TopuScreenEditor extends Screen {
    private final Screen parent; private final TopuHudConfig config=ConfigManager.get(); private boolean dragging; private double offsetX,offsetY;
    public TopuScreenEditor(Screen parent){super(Component.literal("Topu HUD Editor"));this.parent=parent;}
    @Override protected void init(){addRenderableWidget(Button.builder(Component.literal("DONE"),b->closeEditor()).bounds(width/2-50,height-28,100,20).build());}
    private void closeEditor(){dragging=false;ConfigManager.save();minecraft.setScreenAndShow(parent);}
    @Override public void onClose(){closeEditor();}
    @Override public boolean mouseClicked(MouseButtonEvent e,boolean doubleClick){if(e.button()==0&&e.x()>=config.utilityX-6&&e.x()<=config.utilityX+216&&e.y()>=config.utilityY-6&&e.y()<=config.utilityY+440){dragging=true;offsetX=e.x()-config.utilityX;offsetY=e.y()-config.utilityY;return true;}return super.mouseClicked(e,doubleClick);}
    @Override public boolean mouseDragged(MouseButtonEvent e,double dx,double dy){if(dragging&&e.button()==0){config.utilityX=(int)Math.max(0,Math.min(width-220,e.x()-offsetX));config.utilityY=(int)Math.max(0,Math.min(height-60,e.y()-offsetY));return true;}return super.mouseDragged(e,dx,dy);}
    @Override public boolean mouseReleased(MouseButtonEvent e){if(e.button()==0){dragging=false;ConfigManager.save();return true;}return super.mouseReleased(e);}
    @Override public void extractRenderState(GuiGraphicsExtractor g,int mouseX,int mouseY,float delta){g.fill(0,0,width,height,0xCC070A10);g.text(font,"TOPU HUD EDITOR",20,20,0xFFFFFFFF,true);g.text(font,"Drag the utility HUD block, release the mouse, then press DONE.",20,40,0xFF9AA6B7,false);g.fill(config.utilityX-4,config.utilityY-4,config.utilityX+214,config.utilityY+430,0xA0101520);g.outline(config.utilityX-4,config.utilityY-4,218,434,0xFF6FA8FF);g.text(font,"UTILITY HUD",config.utilityX,config.utilityY,0xFFFFFFFF,true);g.text(font,"Coordinates: 0, 100, 0",config.utilityX,config.utilityY+22,0xFFE7EDF7,false);g.text(font,"Auto Sprint: ON",config.utilityX,config.utilityY+40,0xFFE7EDF7,false);g.text(font,"50 modules available",config.utilityX,config.utilityY+58,0xFF7D8BA0,false);super.extractRenderState(g,mouseX,mouseY,delta);}
}
