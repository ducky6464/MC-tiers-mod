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
        super(Text.literal("MC-Tiers Mode Selector"));
        this.parent = parent;
    }

    private int columns() {
        return this.width < 300 ? 2 : 3;
    }

    private int panelWidth() {
        return Math.min(360, this.width - 16);
    }

    private int buttonWidth(int columns) {
        return Math.max(72, (panelWidth() - 32 - (columns - 1) * 8) / columns);
    }

    private int rows() {
        return (int) Math.ceil(ModeConfig.getModes().size() / (double) columns());
    }

    private int gridHeight() {
        return rows() * 26 + Math.max(0, rows() - 1) * 8;
    }

    private int panelHeight() {
        return 82 + gridHeight() + 54;
    }

    private int panelY() {
        return Math.max(6, (this.height - panelHeight()) / 2);
    }

    @Override
    protected void init() {
        List<ModeConfig.ModeOption> modes = ModeConfig.getModes();
        int columns = columns();
        int gap = 8;
        int buttonHeight = 26;
        int panelX = (this.width - panelWidth()) / 2;
        int startX = panelX + (panelWidth() - (columns * buttonWidth(columns) + (columns - 1) * gap)) / 2;
        int startY = panelY() + 76;

        for (int i = 0; i < modes.size(); i++) {
            ModeConfig.ModeOption option = modes.get(i);
            int column = i % columns;
            int row = i / columns;
            boolean selected = ModeConfig.getMode().equals(option.id());
            String label = (selected ? "✓  " : "") + option.label();

            addDrawableChild(ButtonWidget.builder(Text.literal(label), button -> {
                ModeConfig.setMode(option.id());
                if (client != null) client.setScreen(new ModeSelectionScreen(parent));
            }).dimensions(startX + column * (buttonWidth(columns) + gap),
                    startY + row * (buttonHeight + gap), buttonWidth(columns), buttonHeight).build());
        }

        int doneY = startY + gridHeight() + 12;
        addDrawableChild(ButtonWidget.builder(Text.literal("Done"), button -> close())
                .dimensions(this.width / 2 - 54, doneY, 108, 22).build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        int panelWidth = panelWidth();
        int panelHeight = panelHeight();
        int panelX = (this.width - panelWidth) / 2;
        int panelY = panelY();

        context.fill(0, 0, this.width, this.height, 0xC9000000);
        context.fill(panelX, panelY, panelX + panelWidth, panelY + panelHeight, 0xF0121722);
        context.fill(panelX, panelY, panelX + panelWidth, panelY + 3, 0xFF56D9D0);
        context.fill(panelX + 1, panelY + 3, panelX + panelWidth - 1, panelY + 4, 0xFF293C4A);

        context.drawCenteredTextWithShadow(textRenderer,
                Text.literal("MC-TIERS").formatted(Formatting.BOLD, Formatting.AQUA),
                this.width / 2, panelY + 13, 0xFFFFFFFF);
        context.drawCenteredTextWithShadow(textRenderer,
                Text.literal("PLAYER RANKING DISPLAY").formatted(Formatting.GRAY),
                this.width / 2, panelY + 31, 0xFFB8C4D0);
        context.drawCenteredTextWithShadow(textRenderer,
                Text.literal("Choose which mode appears beside player names").formatted(Formatting.GRAY),
                this.width / 2, panelY + 49, 0xFF9AA8B7);

        int selectedY = panelY + 76 + gridHeight() + 39;
        String selectedLabel = ModeConfig.getModes().stream()
                .filter(mode -> mode.id().equals(ModeConfig.getMode()))
                .map(ModeConfig.ModeOption::label)
                .findFirst().orElse(ModeConfig.getMode());
        context.drawCenteredTextWithShadow(textRenderer,
                Text.literal("ACTIVE MODE: " + selectedLabel.toUpperCase())
                        .formatted(Formatting.AQUA),
                this.width / 2, selectedY, 0xFF56D9D0);

        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public void close() {
        if (client != null) client.setScreen(parent);
    }
}
