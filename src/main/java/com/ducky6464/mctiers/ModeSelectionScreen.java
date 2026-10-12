package com.ducky6464.mctiers;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public final class ModeSelectionScreen extends Screen {
    private final Screen parent;

    public ModeSelectionScreen(Screen parent) {
        super(Text.literal("MC-Tiers: Select Game Mode"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int buttonWidth = 150;
        int gap = 8;
        int columns = 3;
        int totalWidth = columns * buttonWidth + (columns - 1) * gap;
        int startX = (this.width - totalWidth) / 2;
        int startY = Math.max(50, this.height / 2 - 65);

        for (int i = 0; i < ModeConfig.MODES.length; i++) {
            final String mode = ModeConfig.MODES[i];
            int column = i % columns;
            int row = i / columns;
            String label = prettyName(mode);
            if (ModeConfig.getMode().equalsIgnoreCase(mode)) {
                label = "✓ " + label + " (Selected)";
            }
            addDrawableChild(ButtonWidget.builder(Text.literal(label), button -> {
                ModeConfig.setMode(mode);
                if (client != null) client.setScreen(new ModeSelectionScreen(parent));
            }).dimensions(startX + column * (buttonWidth + gap), startY + row * 26, buttonWidth, 20).build());
        }

        addDrawableChild(ButtonWidget.builder(Text.literal("Done"), button -> close())
                .dimensions(this.width / 2 - 75, startY + 3 * 26 + 8, 150, 20).build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(textRenderer, title, width / 2, 20, 0xFFFFFF);
        context.drawCenteredTextWithShadow(textRenderer,
                Text.literal("Press K anytime to change the displayed tier mode"),
                width / 2, 36, 0xAAAAAA);
        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public void close() {
        if (client != null) client.setScreen(parent);
    }

    private static String prettyName(String mode) {
        return switch (mode) {
            case "uhc" -> "UHC";
            case "netpot" -> "Netherite Pot";
            case "diamondpot" -> "Diamond Pot";
            case "diamond_smp" -> "Diamond SMP";
            default -> mode.substring(0, 1).toUpperCase() + mode.substring(1);
        };
    }
}