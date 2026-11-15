package com.cmcplugins.asmr.core;

public final class AsmrPattern {
    private AsmrPattern() {}

    public static double[] position(AsmrMode mode, long elapsedTicks, long sequence, long seed, double radius) {
        return position(mode, elapsedTicks, sequence, seed, radius, 160);
    }

    public static double[] position(AsmrMode mode, long elapsedTicks, long sequence, long seed,
                                    double radius, int orbitPeriodTicks) {
        double angle;
        if (mode == AsmrMode.ALTERNATE) {
            angle = (sequence & 1L) == 0L ? Math.PI / 2.0 : -Math.PI / 2.0;
        } else if (mode == AsmrMode.ORBIT) {
            int period = Math.max(1, orbitPeriodTicks);
            angle = 2.0 * Math.PI * (elapsedTicks % period) / period;
        } else {
            long bits = mix(seed + sequence * 0x9E3779B97F4A7C15L);
            angle = 2.0 * Math.PI * ((bits >>> 11) * 0x1.0p-53);
        }
        return new double[] { Math.sin(angle) * radius, 0.0, Math.cos(angle) * radius };
    }

    public static int chimeDelay(int baseIntervalTicks, long sequence, long seed) {
        int spread = Math.max(1, baseIntervalTicks);
        long bits = mix(seed + sequence * 0x9E3779B97F4A7C15L);
        return Math.max(5, spread / 2 + (int) ((bits >>> 1) % spread));
    }

    public static double[] orbitPosition(double turns, double radius) {
        double angle = 2.0 * Math.PI * turns;
        return new double[] {Math.sin(angle) * radius, 0.0, Math.cos(angle) * radius};
    }

    private static long mix(long x) {
        x ^= x >>> 30;
        x *= 0xBF58476D1CE4E5B9L;
        x ^= x >>> 27;
        x *= 0x94D049BB133111EBL;
        return x ^ (x >>> 31);
    }
}
