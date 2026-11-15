package com.cmcplugins.asmr;

import com.cmcplugins.asmr.core.AsmrKind;
import com.cmcplugins.asmr.core.AsmrMode;
import com.mojang.blaze3d.matrix.MatrixStack;
import java.util.Locale;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;

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
        super(new TranslationTextComponent("asmr.gui.title"));
    }

    @Override
    protected void init() {
        int x = width / 2 - 105;
        int top = height / 2 - 88;
        kindButton = addButton(new Button(x, top, 210, 20, kindLabel(), button -> {
            kind = AsmrKind.values()[(kind.ordinal() + 1) % AsmrKind.values().length];
            refreshLabels();
        }));
        modeButton = addButton(new Button(x, top + 22, 210, 20, modeLabel(), button -> {
            mode = AsmrMode.values()[(mode.ordinal() + 1) % AsmrMode.values().length];
            refreshLabels();
        }));
        enabledButton = addButton(new Button(x, top + 44, 210, 20, enabledLabel(), button -> {
            AsmrConfig.ENABLED.set(!AsmrConfig.ENABLED.get());
            refreshLabels();
        }));
        volumeValue = settingRow(x, top + 66, volumeLabel(),
            () -> changeVolume(-1), () -> changeVolume(1));
        intervalValue = settingRow(x, top + 88, intervalLabel(),
            () -> changeInterval(-1), () -> changeInterval(1));
        orbitValue = settingRow(x, top + 110, orbitLabel(),
            () -> changeOrbitSpeed(-1), () -> changeOrbitSpeed(1));
        radiusValue = settingRow(x, top + 132, radiusLabel(),
            () -> changeRadius(-1), () -> changeRadius(1));
        addButton(new Button(x, top + 154, 68, 20, new TranslationTextComponent("asmr.gui.play"), button -> sendPlay()));
        addButton(new Button(x + 71, top + 154, 68, 20, new TranslationTextComponent("asmr.gui.stop"), button -> sendStop()));
        addButton(new Button(x + 142, top + 154, 68, 20, new TranslationTextComponent("asmr.gui.close"), button -> onClose()));
    }

    private Button settingRow(int x, int y, ITextComponent label, Runnable decrease, Runnable increase) {
        addButton(new Button(x, y, 28, 20, new StringTextComponent("−"), button -> {
            decrease.run();
            refreshLabels();
        }));
        Button value = addButton(new Button(x + 31, y, 148, 20, label, button -> {}));
        value.active = false;
        addButton(new Button(x + 182, y, 28, 20, new StringTextComponent("+"), button -> {
            increase.run();
            refreshLabels();
        }));
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

    private ITextComponent kindLabel() {
        return new TranslationTextComponent("asmr.gui.kind",
            new TranslationTextComponent("asmr.kind." + kind.name().toLowerCase(Locale.ROOT)));
    }

    private ITextComponent modeLabel() {
        return new TranslationTextComponent("asmr.gui.mode",
            new TranslationTextComponent("asmr.mode." + mode.name().toLowerCase(Locale.ROOT)));
    }

    private ITextComponent enabledLabel() {
        return new TranslationTextComponent("asmr.gui.enabled",
            new TranslationTextComponent(AsmrConfig.ENABLED.get() ? "asmr.gui.on" : "asmr.gui.off"));
    }

    private ITextComponent volumeLabel() {
        return new TranslationTextComponent("asmr.gui.volume", Math.round(AsmrConfig.VOLUME.get() * 100));
    }

    private ITextComponent intervalLabel() {
        return new TranslationTextComponent("asmr.gui." + kind.name().toLowerCase(Locale.ROOT) + "_interval", currentInterval());
    }

    private ITextComponent orbitLabel() {
        return new TranslationTextComponent("asmr.gui.orbit_speed",
            String.format(Locale.ROOT, "%.1f", 1200.0 / AsmrConfig.ORBIT_PERIOD.get()));
    }

    private ITextComponent radiusLabel() {
        return new TranslationTextComponent("asmr.gui.radius", String.format(Locale.ROOT, "%.2f", AsmrConfig.RADIUS.get()));
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
        if (minecraft != null && minecraft.player != null)
            minecraft.player.chat("/asmr play " + kind.name().toLowerCase(Locale.ROOT)
                + " " + mode.name().toLowerCase(Locale.ROOT));
    }

    private void sendStop() {
        ClientAsmr.stopLocally();
        if (minecraft != null && minecraft.player != null) minecraft.player.chat("/asmr stop");
    }

    @Override
    public void render(MatrixStack matrix, int mouseX, int mouseY, float partialTick) {
        renderBackground(matrix);
        super.render(matrix, mouseX, mouseY, partialTick);
        int top = height / 2 - 88;
        drawCenteredString(matrix, font, title, width / 2, top - 16, 0xFFFFFF);
        drawCenteredString(matrix, font, new TranslationTextComponent(ClientAsmr.kind() == null
            ? "asmr.gui.status.stopped" : "asmr.gui.status.playing"), width / 2, top + 180, 0xD0D0D0);
    }

    @Override
    public boolean isPauseScreen() { return false; }
}
