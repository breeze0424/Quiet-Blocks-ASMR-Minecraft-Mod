package com.cmcplugins.asmr;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.SimpleSound;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;

public final class ClientVoice {
    private static final List<ISound> ACTIVE = new ArrayList<>();

    private ClientVoice() {}

    public static void play(double[] localPosition, float volume, long seed) {
        Minecraft minecraft = Minecraft.getInstance();
        SoundHandler sounds = minecraft.getSoundManager();
        ACTIVE.removeIf(sound -> !sounds.isActive(sound));
        if (ACTIVE.size() >= 6) {
            sounds.stop(ACTIVE.remove(0));
        }
        float pitch = 0.85f + (float) ((seed >>> 4) & 7L) * 0.035f;
        ISound voice = new SimpleSound(SoundEvents.VILLAGER_AMBIENT.getLocation(),
            SoundCategory.MASTER, volume, pitch, false, 0, ISound.AttenuationType.NONE,
            localPosition[0], localPosition[1], localPosition[2], true);
        sounds.play(voice);
        ACTIVE.add(voice);
    }

    public static void stopAll() {
        SoundHandler sounds = Minecraft.getInstance().getSoundManager();
        for (ISound sound : ACTIVE) sounds.stop(sound);
        ACTIVE.clear();
    }
}
