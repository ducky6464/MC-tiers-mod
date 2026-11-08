package com.ducky6464.mctiers;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;

public final class ModeSelectionScreen extends Screen {
    private final Screen parent;

    public ModeSelectionScreen(Screen parent) {
        super(Text.literal("MC-Tiers"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        List<ModeConfig.ModeOption> modes = ModeConfig.getModes();
        int columns = 3;
        int gap = 5;
        int buttonHeight = 22;
        int panelWidth = Math.min(330, this.width - 16);
        int buttonWidth = Math.max(72, (panelWidth - 28 - (columns - 1) * gap) / columns);
        int rows = (int) Math.ceil(modes.size() / (double) columns);
        int gridHeight = rows * buttonHeight + Math.max(0, rows - 1) * gap;
        int panelHeight = 48 + gridHeight + 48;
        int panelX = (this.width - panelWidth) / 2;
        int panelY = Math.max(8, (this.height - panelHeight) / 2);
        int startX = panelX + (panelWidth - (columns * buttonWidth + (columns - 1) * gap)) / 2;
        int startY = panelY + 48;

        for (int i = 0; i < modes.size(); i++) {
            ModeConfig.ModeOption option = modes.get(i);
            int column = i % columns;
            int row = i / columns;
            boolean selected = ModeConfig.getMode().equals(option.id());
            String label = (selected ? "✓ " : "") + option.label();

            addDrawableChild(ButtonWidget.builder(Text.literal(label), button -> {
                ModeConfig.setMode(option.id());
                if (client != null) client.setScreen(new ModeSelectionScreen(parent));
            }).dimensions(startX + column * (buttonWidth + gap),
                    startY + row * (buttonHeight + gap), buttonWidth, buttonHeight).build());
        }

        int doneY = startY + gridHeight + 10;
        addDrawableChild(ButtonWidget.builder(Text.literal("Done"), button -> close())
                .dimensions(this.width / 2 - 48, doneY, 96, 20).build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        List<ModeConfig.ModeOption> modes = ModeConfig.getModes();
        int columns = 3;
        int gap = 5;
        int buttonHeight = 22;
        int panelWidth = Math.min(330, this.width - 16);
        int buttonWidth = Math.max(72, (panelWidth - 28 - (columns - 1) * gap) / columns);
        int rows = (int) Math.ceil(modes.size() / (double) columns);
        int gridHeight = rows * buttonHeight + Math.max(0, rows - 1) * gap;
        int panelHeight = 48 + gridHeight + 48;
        int panelX = (this.width - panelWidth) / 2;
        int panelY = Math.max(8, (this.height - panelHeight) / 2);

        context.fill(0, 0, this.width, this.height, 0xB9000000);
        context.fill(panelX, panelY, panelX + panelWidth, panelY + panelHeight, 0xF0141925);
        context.fill(panelX, panelY, panelX + panelWidth, panelY + 2, 0xFF55D6D0);
        context.fill(panelX + 1, panelY + 2, panelX + panelWidth - 1, panelY + 3, 0xFF283D4A);
        context.drawCenteredTextWithShadow(textRenderer,
                Text.literal("MC-TIERS").formatted(Formatting.BOLD, Formatting.AQUA),
                width / 2, panelY + 10, 0xFFFFFF);
        context.drawCenteredTextWithShadow(textRenderer,
                Text.literal("SELECT A GAME MODE").formatted(Formatting.GRAY),
                width / 2, panelY + 27, 0xFFB8C4D0);

        int doneY = panelY + 48 + gridHeight + 10;
        context.drawCenteredTextWithShadow(textRenderer,
                Text.literal("Mode list syncs with the website").formatted(Formatting.DARK_GRAY),
                width / 2, doneY + 25, 0xFF808B99);

        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public void close() {
        if (client != null) client.setScreen(parent);
    }
}