package com.cmcplugins.asmr;

import com.cmcplugins.asmr.core.AsmrKind;
import com.cmcplugins.asmr.core.AsmrMode;
import java.util.Locale;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class AsmrMenuScreen extends Screen {
    private AsmrKind kind = ClientAsmr.kind() == null ? AsmrKind.TAP : ClientAsmr.kind();
    private AsmrMode mode = ClientAsmr.mode() == null ? AsmrMode.ALTERNATE : ClientAsmr.mode();
    private Button kindButton;
    private Button modeButton;
    private Button enabledButton;
    private Button volumeValue;
    private Button intervalValue;
    private Button orbitValue;
    private Button radiusValue;

    public AsmrMenuScreen() {
        super(Component.translatable("asmr.gui.title"));
    }

    @Override
    protected void init() {
        int x = width / 2 - 105;
        int top = height / 2 - 88;
        kindButton = addRenderableWidget(Button.builder(kindLabel(), button -> {
            kind = AsmrKind.values()[(kind.ordinal() + 1) % AsmrKind.values().length];
            refreshLabels();
        }).bounds(x, top, 210, 20).build());
        modeButton = addRenderableWidget(Button.builder(modeLabel(), button -> {
            mode = AsmrMode.values()[(mode.ordinal() + 1) % AsmrMode.values().length];
            refreshLabels();
        }).bounds(x, top + 22, 210, 20).build());
        enabledButton = addRenderableWidget(Button.builder(enabledLabel(), button -> {
            AsmrConfig.ENABLED.set(!AsmrConfig.ENABLED.get());
            refreshLabels();
        }).bounds(x, top + 44, 210, 20).build());
        volumeValue = settingRow(x, top + 66, volumeLabel(),
            () -> changeVolume(-1), () -> changeVolume(1));
        intervalValue = settingRow(x, top + 88, intervalLabel(),
            () -> changeInterval(-1), () -> changeInterval(1));
        orbitValue = settingRow(x, top + 110, orbitLabel(),
            () -> changeOrbitSpeed(-1), () -> changeOrbitSpeed(1));
        radiusValue = settingRow(x, top + 132, radiusLabel(),
            () -> changeRadius(-1), () -> changeRadius(1));
        addRenderableWidget(Button.builder(Component.translatable("asmr.gui.play"), button -> sendPlay())
            .bounds(x, top + 154, 68, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("asmr.gui.stop"), button -> sendStop())
            .bounds(x + 71, top + 154, 68, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("asmr.gui.close"), button -> onClose())
            .bounds(x + 142, top + 154, 68, 20).build());
    }

    private Button settingRow(int x, int y, Component label, Runnable decrease, Runnable increase) {
        addRenderableWidget(Button.builder(Component.literal("−"), button -> {
            decrease.run();
            refreshLabels();
        }).bounds(x, y, 28, 20).build());
        Button value = addRenderableWidget(Button.builder(label, button -> {})
            .bounds(x + 31, y, 148, 20).build());
        value.active = false;
        addRenderableWidget(Button.builder(Component.literal("+"), button -> {
            increase.run();
            refreshLabels();
        }).bounds(x + 182, y, 28, 20).build());
        return value;
    }

    private void refreshLabels() {
        kindButton.setMessage(kindLabel());
        modeButton.setMessage(modeLabel());
        enabledButton.setMessage(enabledLabel());
        volumeValue.setMessage(volumeLabel());
        intervalValue.setMessage(intervalLabel());
        orbitValue.setMessage(orbitLabel());
        radiusValue.setMessage(radiusLabel());
    }

    private Component kindLabel() {
        return Component.translatable("asmr.gui.kind",
            Component.translatable("asmr.kind." + kind.name().toLowerCase(Locale.ROOT)));
    }

    private Component modeLabel() {
        return Component.translatable("asmr.gui.mode",
            Component.translatable("asmr.mode." + mode.name().toLowerCase(Locale.ROOT)));
    }

    private Component enabledLabel() {
        return Component.translatable("asmr.gui.enabled",
            Component.translatable(AsmrConfig.ENABLED.get() ? "asmr.gui.on" : "asmr.gui.off"));
    }

    private Component volumeLabel() {
        return Component.translatable("asmr.gui.volume", Math.round(AsmrConfig.VOLUME.get() * 100));
    }

    private Component intervalLabel() {
        return Component.translatable("asmr.gui." + kind.name().toLowerCase(Locale.ROOT) + "_interval", currentInterval());
    }

    private Component orbitLabel() {
        return Component.translatable("asmr.gui.orbit_speed",
            String.format(Locale.ROOT, "%.1f", 1200.0 / AsmrConfig.ORBIT_PERIOD.get()));
    }

    private Component radiusLabel() {
        return Component.translatable("asmr.gui.radius", String.format(Locale.ROOT, "%.2f", AsmrConfig.RADIUS.get()));
    }

    private void changeVolume(int step) {
        int units = (int) Math.round(AsmrConfig.VOLUME.get() * 20);
        AsmrConfig.VOLUME.set(Math.max(0, Math.min(20, units + step)) / 20.0);
    }

    private int currentInterval() {
        return AsmrConfig.intervalFor(kind);
    }

    private void changeInterval(int step) {
        AsmrConfig.changeInterval(kind, step);
    }

    private void changeOrbitSpeed(int step) {
        int period = AsmrConfig.ORBIT_PERIOD.get() - step * 10;
        AsmrConfig.ORBIT_PERIOD.set(Math.max(40, Math.min(400, period)));
    }

    private void changeRadius(int step) {
        int units = (int) Math.round(AsmrConfig.RADIUS.get() * 4);
        AsmrConfig.RADIUS.set(Math.max(1, Math.min(16, units + step)) / 4.0);
    }

    private void sendPlay() {
        if (minecraft != null && minecraft.getConnection() != null)
            minecraft.getConnection().sendCommand("asmr play " + kind.name().toLowerCase(Locale.ROOT)
                + " " + mode.name().toLowerCase(Locale.ROOT));
    }

    private void sendStop() {
        ClientAsmr.stopLocally();
        if (minecraft != null && minecraft.getConnection() != null)
            minecraft.getConnection().sendCommand("asmr stop");
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        int top = height / 2 - 88;
        graphics.drawCenteredString(font, title, width / 2, top - 16, 0xFFFFFF);
        graphics.drawCenteredString(font, Component.translatable(ClientAsmr.kind() == null
            ? "asmr.gui.status.stopped" : "asmr.gui.status.playing"), width / 2, top + 180, 0xD0D0D0);
    }

    @Override
    public boolean isPauseScreen() { return false; }
}
