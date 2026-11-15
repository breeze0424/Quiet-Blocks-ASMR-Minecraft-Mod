package com.cmcplugins.asmr.core;

import java.util.Random;

public final class AsmrSynth {
    public static final int SAMPLE_RATE = 44100;

    private AsmrSynth() {}

    public static short[] generate(AsmrKind kind, long seed) {
        if (kind == AsmrKind.SCRATCH) return scratch(seed);
        if (kind == AsmrKind.CHIME) return chime(seed);
        if (kind == AsmrKind.RAIN) return rain(seed);
        if (kind == AsmrKind.PAPER) return paper(seed);
        if (kind == AsmrKind.BRUSH) return brush(seed);
        if (kind == AsmrKind.FIRE) return fire(seed);
        if (kind == AsmrKind.VOICE) throw new IllegalArgumentException("人声由游戏音效系统播放");
        return tap(seed);
    }

    private static short[] tap(long seed) {
        Random random = new Random(seed);
        int count = (int) (SAMPLE_RATE * 0.11);
        short[] pcm = new short[count];
        double base = 215.0 + random.nextDouble() * 12.0;
        double filteredNoise = 0.0;
        for (int i = 0; i < count; i++) {
            double t = (double) i / SAMPLE_RATE;
            filteredNoise = filteredNoise * 0.55 + (random.nextDouble() * 2.0 - 1.0) * 0.45;
            double attack = Math.min(1.0, t / 0.001);
            double body = Math.sin(2.0 * Math.PI * base * t) * Math.exp(-55.0 * t);
            double click = filteredNoise * Math.exp(-110.0 * t);
            double release = Math.min(1.0, (0.11 - t) / 0.015);
            pcm[i] = sample(attack * release * (0.32 * body + 0.20 * click));
        }
        return pcm;
    }

    private static short[] scratch(long seed) {
        Random random = new Random(seed);
        int count = (int) (SAMPLE_RATE * 0.48);
        short[] pcm = new short[count];
        double low = 0.0;
        double smooth = 0.0;
        for (int i = 0; i < count; i++) {
            double t = (double) i / SAMPLE_RATE;
            double raw = random.nextDouble() * 2.0 - 1.0;
            low += 0.08 * (raw - low);
            smooth += 0.006 * (raw - smooth);
            double band = low - smooth;
            double envelope = Math.min(1.0, t / 0.025) * Math.min(1.0, (0.48 - t) / 0.07);
            double texture = 0.63 + 0.37 * Math.sin(2.0 * Math.PI * 17.0 * t);
            pcm[i] = sample(0.62 * band * envelope * texture);
        }
        return pcm;
    }

    private static short[] chime(long seed) {
        Random random = new Random(seed);
        double[] notes = {392.0, 440.0, 523.25, 587.33, 659.25};
        double base = notes[random.nextInt(notes.length)] * (0.99 + random.nextDouble() * 0.02);
        int count = (int) (SAMPLE_RATE * 0.9);
        short[] pcm = new short[count];
        double noise = 0.0;
        for (int i = 0; i < count; i++) {
            double t = (double) i / SAMPLE_RATE;
            noise = noise * 0.6 + (random.nextDouble() * 2.0 - 1.0) * 0.4;
            double attack = Math.min(1.0, t / 0.003);
            double tail = Math.min(1.0, (0.9 - t) / 0.08);
            double fundamental = Math.sin(2.0 * Math.PI * base * t) * Math.exp(-5.5 * t);
            double shimmer = Math.sin(2.0 * Math.PI * base * 2.71 * t) * Math.exp(-8.0 * t);
            double strike = noise * Math.exp(-90.0 * t);
            pcm[i] = sample(attack * tail * (0.20 * fundamental + 0.08 * shimmer + 0.06 * strike));
        }
        return pcm;
    }

    private static short[] rain(long seed) {
        Random random = new Random(seed);
        double duration = 1.1;
        short[] pcm = new short[(int) (SAMPLE_RATE * duration)];
        double background = 0.0;
        double dropAge = 1.0;
        double dropFrequency = 650.0;
        int nextDrop = 0;
        for (int i = 0; i < pcm.length; i++) {
            double t = (double) i / SAMPLE_RATE;
            if (i >= nextDrop) {
                dropAge = 0.0;
                dropFrequency = 480.0 + random.nextDouble() * 440.0;
                nextDrop = i + (int) (SAMPLE_RATE * (0.035 + random.nextDouble() * 0.10));
            }
            double raw = random.nextDouble() * 2.0 - 1.0;
            background += 0.07 * (raw - background);
            double drop = Math.sin(2.0 * Math.PI * dropFrequency * dropAge) * Math.exp(-85.0 * dropAge);
            double envelope = fade(t, duration, 0.05, 0.10);
            pcm[i] = sample(envelope * (0.10 * background + 0.16 * drop));
            dropAge += 1.0 / SAMPLE_RATE;
        }
        return pcm;
    }

    private static short[] paper(long seed) {
        Random random = new Random(seed);
        double duration = 0.65;
        short[] pcm = new short[(int) (SAMPLE_RATE * duration)];
        double low = 0.0;
        double slow = 0.0;
        for (int i = 0; i < pcm.length; i++) {
            double t = (double) i / SAMPLE_RATE;
            double raw = random.nextDouble() * 2.0 - 1.0;
            low += 0.16 * (raw - low);
            slow += 0.009 * (raw - slow);
            double crinkle = 0.65 + 0.35 * Math.sin(2.0 * Math.PI * 9.0 * t);
            pcm[i] = sample(0.58 * (low - slow) * crinkle * fade(t, duration, 0.025, 0.09));
        }
        return pcm;
    }

    private static short[] brush(long seed) {
        Random random = new Random(seed);
        double duration = 1.1;
        short[] pcm = new short[(int) (SAMPLE_RATE * duration)];
        double soft = 0.0;
        double deep = 0.0;
        for (int i = 0; i < pcm.length; i++) {
            double t = (double) i / SAMPLE_RATE;
            double raw = random.nextDouble() * 2.0 - 1.0;
            soft += 0.055 * (raw - soft);
            deep += 0.002 * (raw - deep);
            double sweep = 0.75 + 0.25 * Math.sin(2.0 * Math.PI * 3.0 * t);
            pcm[i] = sample(0.80 * (soft - deep) * sweep * fade(t, duration, 0.09, 0.15));
        }
        return pcm;
    }

    private static short[] fire(long seed) {
        Random random = new Random(seed);
        double duration = 1.1;
        short[] pcm = new short[(int) (SAMPLE_RATE * duration)];
        double low = 0.0;
        double pop = 0.0;
        for (int i = 0; i < pcm.length; i++) {
            double t = (double) i / SAMPLE_RATE;
            double raw = random.nextDouble() * 2.0 - 1.0;
            low += 0.025 * (raw - low);
            if (random.nextDouble() < 0.00035) pop = 0.55 + random.nextDouble() * 0.45;
            pop *= 0.997;
            pcm[i] = sample((0.14 * low + 0.32 * pop * raw) * fade(t, duration, 0.05, 0.12));
        }
        return pcm;
    }

    private static double fade(double t, double duration, double attack, double release) {
        return Math.min(1.0, t / attack) * Math.min(1.0, (duration - t) / release);
    }

    private static short sample(double value) {
        double limited = Math.max(-0.85, Math.min(0.85, value));
        return (short) Math.round(limited * 32767.0);
    }
}
