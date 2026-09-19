package net.mcsm.sift.client;

import net.mcsm.sift.entity.VoidDwellerEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;

/**
 * Client hooks for Sift - dialogue overlay with typewriting effect
 */
public final class McsmSiftClient {

    private static float dialogueProgress = 0f;
    private static String currentDialogue = "";
    private static VoidDwellerEntity currentDweller = null;

    private McsmSiftClient() {}

    public static void openDwellerDialogue(VoidDwellerEntity dweller) {
        currentDweller = dweller;
        currentDialogue = dweller.getAllLoreForScreen().get(dweller.getRandom().nextInt(dweller.getAllLoreForScreen().size()));
        dialogueProgress = 0f;
        Minecraft mc = Minecraft.getInstance();
        if (mc != null) {
            mc.setScreen(new DwellerDialogueScreen(Component.literal(dweller.getDwellerType().displayName)));
        }
    }

    public static class DwellerDialogueScreen extends Screen {

        private int tickCount = 0;
        private float typewriterProgress = 0f;

        protected DwellerDialogueScreen(Component title) {
            super(title);
        }

        @Override
        protected void init() {
            super.init();
            typewriterProgress = 0f;
        }

        @Override
        public void tick() {
            super.tick();
            tickCount++;
            // Typewriting dialogue engine - animated fade-in typewriting effect
            typewriterProgress = Mth.clamp(typewriterProgress + 0.05f, 0f, 1f);
        }

        @Override
        public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            // Semi-transparent background with iridescent tint
            graphics.fill(0, 0, this.width, this.height, 0x88000000);

            int centerX = this.width / 2;
            int centerY = this.height / 2;

            // Glowing amethyst-purple chat logs
            int bgColor = 0xCC1A0A2E;
            int borderColor = 0xFFAA55FF;

            // Dialogue box
            int boxWidth = Math.min(400, this.width - 40);
            int boxHeight = 120;
            int boxX = centerX - boxWidth / 2;
            int boxY = centerY - boxHeight / 2 + 40;

            // Outer glow - full-bright neon
            graphics.fill(boxX - 2, boxY - 2, boxX + boxWidth + 2, boxY + boxHeight + 2, borderColor);
            graphics.fill(boxX, boxY, boxX + boxWidth, boxY + boxHeight, bgColor);

            // Title with glow
            Component titleComp = Component.literal(currentDweller != null ? currentDweller.getDwellerType().displayName : "Void Dweller")
                .withStyle(s -> s.withColor(currentDweller != null ? currentDweller.getDwellerType().glowColor : 0xFFAA55FF).withBold(true));
            graphics.drawCenteredString(this.font, titleComp, centerX, boxY + 10, 0xFFFFFF);

            // Typewriting effect - show characters progressively
            int charsToShow = (int)(currentDialogue.length() * typewriterProgress);
            String visibleText = currentDialogue.substring(0, Math.min(charsToShow, currentDialogue.length()));

            // Word wrap
            var wrapped = this.font.split(Component.literal(visibleText), boxWidth - 20);
            int y = boxY + 30;
            for (var line : wrapped) {
                graphics.drawString(this.font, line, boxX + 10, y, 0xFFCC88FF, true);
                y += 10;
                if (y > boxY + boxHeight - 15) break;
            }

            // Cursor blink
            if (typewriterProgress < 1f && (tickCount / 10) % 2 == 0) {
                graphics.drawString(this.font, "_", boxX + 10 + this.font.width(visibleText) % (boxWidth - 20), y - 10, 0xFFAA55FF);
            }

            // Hint
            if (typewriterProgress >= 1f) {
                Component hint = Component.literal("[Press ESC to close, E to hear echo]").withStyle(s -> s.withColor(0xFF888888));
                graphics.drawCenteredString(this.font, hint, centerX, boxY + boxHeight + 10, 0xFFFFFF);
            }

            super.render(graphics, mouseX, mouseY, partialTick);
        }

        @Override
        public boolean isPauseScreen() {
            return false;
        }

        @Override
        public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
            if (keyCode == 256) { // ESC
                this.onClose();
                return true;
            }
            if (keyCode == 69) { // E - echo
                if (currentDweller != null) {
                    // Play echo sound
                }
            }
            return super.keyPressed(keyCode, scanCode, modifiers);
        }

        @Override
        public void onClose() {
            super.onClose();
            currentDweller = null;
            currentDialogue = "";
        }
    }

    public static void tickClient() {
        McsmSiftSkyRenderer.INSTANCE.tick();
        SiftRiftRenderer.INSTANCE.tick();
        FabricDistortionRenderer.INSTANCE.tick();
        WorldReentryOverlay.INSTANCE.tick();
        VoidEntryCinematic.INSTANCE.tick();
        // V2 - black hole backdrop growing bigger perspective
        try {
            net.mcsm.extras.client.McsmBlackHoleBackdrop.tick();
            net.mcsm.extras.client.McsmAnimatedSkybox.tick();
            // V2 NEXT-GEN Phase 1 - shooting stars tick
            net.mcsm.extras.client.McsmShootingStars.tick();
            // V2 NEXT-GEN Phase 2 - ambient world fireflies + bioluminescent
            net.mcsm.extras.client.McsmAmbientWorld.tick();
            // V2 NEXT-GEN Phase 4 - time warp effect
            net.mcsm.extras.client.McsmTimeWarpEffect.tick();
        } catch (Throwable ignored) {}

        // Auto-trigger cinematic when entering void from overworld - merged void
        try {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null) {
                int y = (int) mc.player.getY();
                if (y == net.mcsm.sift.McsmVoidTiers.FABRIC_BOTTOM - 1 || y == net.mcsm.sift.McsmVoidTiers.FABRIC_BOTTOM - 2) {
                    if (!VoidEntryCinematic.INSTANCE.isActive()) {
                        VoidEntryCinematic.INSTANCE.trigger();
                        WorldReentryOverlay.INSTANCE.triggerBlackout(40);
                    }
                }
            }
        } catch (Throwable ignored) {}
    }

    public static void renderCinematic(net.minecraft.client.gui.GuiGraphics graphics, float partialTick) {
        if (VoidEntryCinematic.INSTANCE.isActive()) {
            var poseStack = graphics.pose();
            poseStack.pushPose();
            VoidEntryCinematic.INSTANCE.render(poseStack);
            poseStack.popPose();
        }
        if (WorldReentryOverlay.INSTANCE.isInBlackout()) {
            var poseStack = graphics.pose();
            poseStack.pushPose();
            WorldReentryOverlay.INSTANCE.render(poseStack);
            poseStack.popPose();
        }
        if (FabricDistortionRenderer.INSTANCE.getGlobalWarp() > 0.01f) {
            var poseStack = graphics.pose();
            poseStack.pushPose();
            FabricDistortionRenderer.INSTANCE.renderDistortionOverlay(poseStack, partialTick);
            poseStack.popPose();
        }
    }
}
