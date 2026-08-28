
package com.bettertoppi.topuhud.config;

public class TopuHudConfig {

    // ============================================================
    // HUD ENABLE / DISABLE
    // ============================================================

    public boolean armorHud = true;

    public boolean fpsCounter = true;

    public boolean pingDisplay = true;

    public boolean tpsDisplay = false;

    public boolean cpsDisplay = true;

    public boolean comboCounter = true;

    public boolean totemCounter = true;

    public boolean potionCounter = true;

    public boolean potionEffects = true;

    public boolean gappleCounter = true;

    public boolean armorWarning = true;

    public boolean enemyHealth = true;

    public boolean cooldown = true;

    // ============================================================
    // PLAYER FEATURES
    // ============================================================

    public boolean toggleSneak = false;

    public boolean autoSprint = false;

    // ============================================================
    // EDITOR
    // ============================================================

    public boolean editMode = false;

    // ============================================================
    // HUD POSITIONS
    // ============================================================

    public int armorX = 10;
    public int armorY = 45;

    public int fpsX = 10;
    public int fpsY = 80;

    public int pingX = 10;
    public int pingY = 102;

    public int tpsX = 10;
    public int tpsY = 124;

    public int cpsX = 10;
    public int cpsY = 146;

    public int comboX = 10;
    public int comboY = 168;

    public int totemX = 10;
    public int totemY = 190;

    public int potionX = 10;
    public int potionY = 212;

    public int effectsX = 10;
    public int effectsY = 234;

    public int gappleX = 10;
    public int gappleY = 316;

    public int warningX = 10;
    public int warningY = 338;

    public int enemyHealthX = 10;
    public int enemyHealthY = 360;

    public int cooldownX = 10;
    public int cooldownY = 390;

    // ============================================================
    // RESET
    // ============================================================

    public void reset() {

        armorHud = true;
        fpsCounter = true;
        pingDisplay = true;
        tpsDisplay = false;
        cpsDisplay = true;
        comboCounter = true;
        totemCounter = true;
        potionCounter = true;
        potionEffects = true;
        gappleCounter = true;
        armorWarning = true;
        enemyHealth = true;
        cooldown = true;

        toggleSneak = false;
        autoSprint = false;

        editMode = false;

        armorX = 10;
        armorY = 45;

        fpsX = 10;
        fpsY = 80;

        pingX = 10;
        pingY = 102;

        tpsX = 10;
        tpsY = 124;

        cpsX = 10;
        cpsY = 146;

        comboX = 10;
        comboY = 168;

        totemX = 10;
        totemY = 190;

        potionX = 10;
        potionY = 212;

        effectsX = 10;
        effectsY = 234;

        gappleX = 10;
        gappleY = 316;

        warningX = 10;
        warningY = 338;

        enemyHealthX = 10;
        enemyHealthY = 360;

        cooldownX = 10;
        cooldownY = 390;
    }
}
