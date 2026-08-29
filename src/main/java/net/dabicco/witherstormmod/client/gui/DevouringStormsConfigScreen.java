package net.dabicco.witherstormmod.client.gui;

import net.dabicco.witherstormmod.config.DabyWSClientConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/**
 * DevouringStormsConfigScreen — Enhanced config UI for Devouring Storms.
 */
public class DevouringStormsConfigScreen extends Screen {

    private final Screen parent;
    private int currentTab = 0;
    private static final String[] TAB_NAMES = {
        "Visuals", "Storm Body", "Combat", "Audio", "Devouring Storms"
    };

    public DevouringStormsConfigScreen(Screen parent) {
        super(Component.literal("Devouring Storms: Config"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int tabWidth = 100;
        int totalWidth = TAB_NAMES.length * (tabWidth + 4);
        int startX = (this.width - totalWidth) / 2;

        for (int i = 0; i < TAB_NAMES.length; i++) {
            final int tab = i;
            this.addRenderableWidget(Button.builder(
                Component.literal(TAB_NAMES[i]),
                b -> switchTab(tab)
            ).bounds(startX + i * (tabWidth + 4), 4, tabWidth, 20).build());
        }

        populateTab(currentTab);

        this.addRenderableWidget(Button.builder(
            Component.literal("Done"),
            b -> this.onClose()
        ).bounds(this.width / 2 - 100, this.height - 28, 200, 20).build());
    }

    private void switchTab(int tab) {
        currentTab = tab;
        this.clearWidgets();
        this.init();
    }

    private void populateTab(int tab) {
        int y = 40;
        switch (tab) {
            case 0 -> {
                addToggle("Storm Atmosphere", y, DabyWSClientConfig.stormAtmosphere); y += 24;
                addToggle("Atmosphere Pulse", y, DabyWSClientConfig.atmospherePulse); y += 24;
                addToggle("Phase Fog Palettes", y, DabyWSClientConfig.phaseFogPalettes); y += 24;
                addToggle("Storm Fog", y, DabyWSClientConfig.stormFog); y += 24;
                addToggle("Sun Glow", y, DabyWSClientConfig.sunGlow); y += 24;
            }
            case 1 -> {
                addToggle("Blob FX", y, DabyWSClientConfig.stormBlobFX); y += 24;
                addToggle("Cataclysm Halos", y, DabyWSClientConfig.cataclysmHalos); y += 24;
                addToggle("Black Glare", y, DabyWSClientConfig.blackGlare); y += 24;
                addToggle("Filled Subphases", y, DabyWSClientConfig.filledSubphases); y += 24;
                addToggle("Reverse Shading", y, DabyWSClientConfig.reverseShading); y += 24;
            }
            case 2 -> {
                addToggle("Tentacle Physics", y, DabyWSClientConfig.tentaclePhysics); y += 24;
                addToggle("Phase Animation", y, DabyWSClientConfig.phaseAnim); y += 24;
                addToggle("Storm Shadow", y, DabyWSClientConfig.stormShadow); y += 24;
                addToggle("Impact Light", y, DabyWSClientConfig.impactLight); y += 24;
            }
            case 3 -> {
                addToggle("Storm Music", y, DabyWSClientConfig.stormMusic); y += 24;
                addToggle("Storm Ambience", y, DabyWSClientConfig.stormAmbience); y += 24;
                addToggle("Beam Hum", y, DabyWSClientConfig.beamHum); y += 24;
                addToggle("Infected Mob Sound", y, DabyWSClientConfig.infectedMobSound); y += 24;
            }
            case 4 -> {
                addToggle("Infinite Growth", y, DabyWSClientConfig.infiniteGrowth); y += 24;
                addToggle("Infection Spreading", y, DabyWSClientConfig.infectionSpreading); y += 24;
                addToggle("Corrupt Blocks", y, DabyWSClientConfig.infectionCorruptBlocks); y += 24;
            }
        }
    }

    private void addToggle(String label, int y, boolean value) {
        String prefix = value ? "\u00a7aON " : "\u00a7cOFF";
        this.addRenderableWidget(Button.builder(
            Component.literal(prefix + " \u00a7f" + label),
            b -> {}
        ).bounds(this.width / 2 - 150, y, 300, 20).build());
    }

    @Override
    public void onClose() {
        DabyWSClientConfig.save();
        Minecraft.getInstance().gui.setScreen(this.parent);
    }
}
