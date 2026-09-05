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
    private final Screen parent;
    private TextFieldWidget searchBox;
    private TopuUtilityManager.Category category;
    private int scroll;

    public TopuHudScreen(Screen parent) { super(Text.literal("Topu Utility HUD")); this.parent = parent; }
    @Override protected void init() { clearAndBuild(); }

    private void clearAndBuild() {
        this.clearChildren();
        int left = 24, sidebar = 148;
        searchBox = new TextFieldWidget(this.textRenderer, left + sidebar + 18, 22,
                this.width - left - sidebar - 42, 20, Text.literal("Search utilities"));
        searchBox.setMaxLength(40);
        searchBox.setPlaceholder(Text.literal("Search 50 utilities..."));
        searchBox.setChangedListener(s -> { scroll = 0; rebuildButtons(); });
        this.addDrawableChild(searchBox);
        int y = 62;
        addCategory("ALL", null, left, y); y += 26;
        for (TopuUtilityManager.Category c : TopuUtilityManager.Category.values()) { addCategory(pretty(c.name()), c, left, y); y += 26; }
        rebuildButtons();
    }

    private void addCategory(String label, TopuUtilityManager.Category c, int x, int y) {
        this.addDrawableChild(ButtonWidget.builder(Text.literal(label), btn -> { category = c; scroll = 0; rebuildButtons(); }).dimensions(x, y, 132, 22).build());
    }

    private void rebuildButtons() {
        List<net.minecraft.client.gui.widget.ClickableWidget> remove = new ArrayList<>();
        for (var child : this.children()) if (child instanceof ButtonWidget b && b.getX() > 160) remove.add(b);
        remove.forEach(this::remove);
        TopuUtilityManager.Utility[] utilities = TopuUtilityManager.search(searchBox == null ? "" : searchBox.getText(), category);
        int start = Math.max(0, Math.min(scroll, Math.max(0, utilities.length - 12)));
        int visible = Math.min(12, utilities.length - start);
        int contentLeft = 190;
        int colWidth = Math.max(170, (this.width - contentLeft - 24) / 2);
        for (int i = 0; i < visible; i++) {
            TopuUtilityManager.Utility u = utilities[start + i];
            int col = i % 2, row = i / 2;
            int x = contentLeft + col * colWidth, y = 58 + row * 43;
            ButtonWidget b = ButtonWidget.builder(Text.literal(u.name() + "  •  " + (TopuUtilityManager.isEnabled(u) ? "ON" : "OFF")), btn -> {
                boolean value = !TopuUtilityManager.isEnabled(u);
                TopuUtilityManager.setEnabled(u, value); ConfigManager.save();
                btn.setMessage(Text.literal(u.name() + "  •  " + (value ? "ON" : "OFF")));
            }).dimensions(x, y, colWidth - 10, 34).build();
            this.addDrawableChild(b);
        }
    }

    private static String pretty(String value) { return value.substring(0, 1) + value.substring(1).toLowerCase(); }

    @Override public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (mouseX > 175 && verticalAmount != 0) {
            TopuUtilityManager.Utility[] utilities = TopuUtilityManager.search(searchBox == null ? "" : searchBox.getText(), category);
            int max = Math.max(0, utilities.length - 12);
            scroll += verticalAmount < 0 ? 2 : -2; scroll = Math.max(0, Math.min(scroll, max)); rebuildButtons(); return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override public void close() { ConfigManager.save(); MinecraftClient.getInstance().setScreen(parent); }

    @Override public void render(DrawContext drawContext, int mouseX, int mouseY, float delta) {
        drawContext.fill(0, 0, width, height, 0xF20A0D12);
        drawContext.fill(0, 0, width, 56, 0xFF111722);
        drawContext.fill(0, 56, 166, height, 0xFF0E131C);
        drawContext.fill(166, 56, width, 57, 0xFF293243);
        drawContext.drawText(this.textRenderer, Text.literal("TOPU"), 24, 20, 0xFFFFFFFF, true);
        drawContext.drawText(this.textRenderer, Text.literal("UTILITY HUD"), 67, 20, 0xFF9AA7BC, false);
        drawContext.drawText(this.textRenderer, Text.literal("50 BUILT-IN UTILITIES"), 24, 43, 0xFF6F7D91, false);
        drawContext.drawText(this.textRenderer, Text.literal("RIGHT SHIFT"), width - 105, 43, 0xFF7F8CA1, false);
        String active = category == null ? "ALL UTILITIES" : pretty(category.name());
        drawContext.drawText(this.textRenderer, Text.literal(active), 190, 42, 0xFFB9C6D9, true);
        drawContext.drawText(this.textRenderer, Text.literal("Scroll to browse"), width - 115, 42, 0xFF657287, false);
        super.render(drawContext, mouseX, mouseY, delta);
    }
}
