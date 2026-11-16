package com.cmcplugins.asmr;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;

public final class ClientVoice {
    private static final List<SoundInstance> ACTIVE = new ArrayList<>();

    private ClientVoice() {}

    public static void play(double[] localPosition, float volume, long seed) {
        Minecraft minecraft = Minecraft.getInstance();
        SoundManager sounds = minecraft.getSoundManager();
        ACTIVE.removeIf(sound -> !sounds.isActive(sound));
        if (ACTIVE.size() >= 6) {
            sounds.stop(ACTIVE.remove(0));
        }
        float pitch = 0.85f + (float) ((seed >>> 4) & 7L) * 0.035f;
        SoundInstance voice = new SimpleSoundInstance(SoundEvents.VILLAGER_AMBIENT.getLocation(),
            SoundSource.MASTER, volume, pitch, RandomSource.create(seed),
            false, 0, SoundInstance.Attenuation.NONE,
            localPosition[0], localPosition[1], localPosition[2], true);
        sounds.play(voice);
        ACTIVE.add(voice);
    }

    public static void stopAll() {
        SoundManager sounds = Minecraft.getInstance().getSoundManager();
        for (SoundInstance sound : ACTIVE) sounds.stop(sound);
        ACTIVE.clear();
    }
}
