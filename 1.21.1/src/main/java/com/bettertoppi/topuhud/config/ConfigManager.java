package com.bettertoppi.topuhud.config;

public final class TopuHudConfig {

// ============================================================
// EXISTING HUD MODULES
// ============================================================

public boolean armorHud = true;
public boolean fpsCounter = true;
public boolean pingDisplay = true;
public boolean tpsDisplay = true;

public boolean cpsDisplay = true;
public boolean comboCounter = true;
public boolean totemCounter = true;
public boolean potionEffects = true;

public boolean potionCounter = true;
public boolean gappleCounter = true;

public boolean armorWarning = true;
public boolean enemyHealth = true;
public boolean cooldown = true;

// ============================================================
// NEW TOPUCLIENT FEATURES
// ============================================================

/*
 * Shows the block currently being looked at.
 */
public boolean blockOverlay = true;

/*
 * WASD + mouse button keystrokes HUD.
 */
public boolean keystrokes = true;

/*
 * JVM memory usage HUD.
 */
public boolean memory = true;

/*
 * Custom crosshair.
 */
public boolean customCrosshair = true;

// ============================================================
// OTHER SETTINGS
// ============================================================

public boolean autoSprint = true;
public boolean toggleSneak = true;
public boolean editMode = false;

// ============================================================
// EXISTING HUD POSITIONS
// ============================================================

public int armorX = 10;
public int armorY = 10;

public int fpsX = 10;
public int fpsY = 70;

public int pingX = 10;
public int pingY = 86;

public int tpsX = 10;
public int tpsY = 102;

public int cpsX = 10;
public int cpsY = 118;

public int comboX = 10;
public int comboY = 134;

public int totemX = 10;
public int totemY = 150;

public int potionX = 10;
public int potionY = 166;

public int effectsX = 10;
public int effectsY = 182;

public int gappleX = 10;
public int gappleY = 214;

public int warningX = 10;
public int warningY = 232;

public int enemyHealthX = 10;
public int enemyHealthY = 250;

public int cooldownX = 10;
public int cooldownY = 270;

// ============================================================
// NEW HUD POSITIONS
// ============================================================

/*
 * Block Overlay
 */
public int blockOverlayX = 10;
public int blockOverlayY = 294;

/*
 * Keystrokes
 */
public int keystrokesX = 10;
public int keystrokesY = 330;

/*
 * Memory
 */
public int memoryX = 10;
public int memoryY = 410;

// ============================================================
// CROSSHAIR SETTINGS
// ============================================================

/*
 * Crosshair style:
 *
 * 0 = Plus
 * 1 = Cross
 * 2 = Dot
 * 3 = T
 */
public int crosshairStyle = 0;

/*
 * Crosshair size in pixels.
 */
public int crosshairSize = 7;

/*
 * Crosshair line thickness.
 */
public int crosshairThickness = 2;

/*
 * Gap between the center and crosshair lines.
 */
public int crosshairGap = 3;

/*
 * ARGB color.
 *
 * Default = bright green.
 */
public int crosshairColor = 0xFF00FF88;

}
