package com.cmcplugins.asmr;

import com.cmcplugins.asmr.core.AsmrAudioEngine;
import com.cmcplugins.asmr.core.AsmrKind;
import com.cmcplugins.asmr.core.AsmrMode;
import com.cmcplugins.asmr.core.AsmrPattern;
import com.cmcplugins.asmr.core.AsmrSynth;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.KeyMapping;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.event.TickEvent;
import org.lwjgl.glfw.GLFW;

public final class ClientAsmr {
    private static AsmrKind kind;
    private static AsmrMode mode;
    private static long ticks;
    private static long sequence;
    private static long seed;
    private static long nextTick;
    private static double orbitTurns;
    private static boolean muted;
    private static final KeyMapping OPEN_MENU = new KeyMapping("key.asmr.open",
        InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_K, KeyMapping.Category.MISC);

    private ClientAsmr() {}

    public static void register() {
        RegisterKeyMappingsEvent.BUS.addListener(event -> event.register(OPEN_MENU));
        TickEvent.ClientTickEvent.Post.BUS.addListener(ClientAsmr::tick);
    }

    public static AsmrKind kind() { return kind; }

    public static AsmrMode mode() { return mode; }

    public static void openMenu() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player != null) minecraft.setScreen(new AsmrMenuScreen());
    }

    public static void stopLocally() {
        kind = null;
        AsmrAudioEngine.stopAll();
        ClientVoice.stopAll();
    }

    public static void receive(AsmrPacket packet) {
        AsmrAudioEngine.stopAll();
        ClientVoice.stopAll();
        kind = packet.playing ? packet.kind : null;
        mode = packet.mode;
        ticks = 0L;
        sequence = 0L;
        nextTick = 0L;
        orbitTurns = 0.0;
        seed = System.nanoTime();
        muted = false;
    }

    private static void tick(TickEvent.ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        while (OPEN_MENU.consumeClick()) {
            if (minecraft.screen == null) openMenu();
        }
        if (minecraft.player == null || minecraft.level == null) {
            if (kind != null) receive(new AsmrPacket(false, AsmrKind.TAP, AsmrMode.ALTERNATE));
            return;
        }
        if (kind == null) return;
        if (!AsmrConfig.ENABLED.get()) {
            if (!muted) {
                AsmrAudioEngine.stopAll();
                ClientVoice.stopAll();
            }
            muted = true;
            return;
        }
        muted = false;
        if (ticks >= nextTick) {
            int interval = AsmrConfig.intervalFor(kind);
            if (kind == AsmrKind.CHIME) interval = AsmrPattern.chimeDelay(interval, sequence, seed);
            double[] position = mode == AsmrMode.ORBIT
                ? AsmrPattern.orbitPosition(orbitTurns, AsmrConfig.RADIUS.get())
                : AsmrPattern.position(mode, ticks, sequence, seed, AsmrConfig.RADIUS.get());
            float volume = (float) (AsmrConfig.VOLUME.get() * minecraft.options.getSoundSourceVolume(SoundSource.MASTER));
            if (kind == AsmrKind.VOICE) ClientVoice.play(position, AsmrConfig.VOLUME.get().floatValue(), seed + sequence);
            else AsmrAudioEngine.play(AsmrSynth.generate(kind, seed + sequence), position, volume);
            sequence++;
            nextTick = ticks + interval;
        }
        ticks++;
        if (mode == AsmrMode.ORBIT)
            orbitTurns = (orbitTurns + 1.0 / AsmrConfig.ORBIT_PERIOD.get()) % 1.0;
    }
}
