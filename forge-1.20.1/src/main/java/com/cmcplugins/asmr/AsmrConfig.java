package com.cmcplugins.asmr;

import com.cmcplugins.asmr.core.AsmrKind;
import net.minecraftforge.common.ForgeConfigSpec;

public final class AsmrConfig {
    public static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.BooleanValue ENABLED;
    public static final ForgeConfigSpec.DoubleValue VOLUME;
    public static final ForgeConfigSpec.IntValue TAP_INTERVAL;
    public static final ForgeConfigSpec.IntValue SCRATCH_INTERVAL;
    public static final ForgeConfigSpec.IntValue CHIME_INTERVAL;
    public static final ForgeConfigSpec.IntValue VOICE_INTERVAL;
    public static final ForgeConfigSpec.IntValue RAIN_INTERVAL;
    public static final ForgeConfigSpec.IntValue PAPER_INTERVAL;
    public static final ForgeConfigSpec.IntValue BRUSH_INTERVAL;
    public static final ForgeConfigSpec.IntValue FIRE_INTERVAL;
    public static final ForgeConfigSpec.DoubleValue RADIUS;
    public static final ForgeConfigSpec.IntValue ORBIT_PERIOD;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        builder.push("audio");
        ENABLED = builder.comment("本机是否播放 ASMR。")
            .define("enabled", true);
        VOLUME = builder.comment("ASMR 音量，0 到 1。")
            .defineInRange("volume", 0.35, 0.0, 1.0);
        TAP_INTERVAL = builder.comment("敲击间隔，单位：游戏刻。")
            .defineInRange("tapIntervalTicks", 16, 5, 100);
        SCRATCH_INTERVAL = builder.comment("刮擦间隔，单位：游戏刻。")
            .defineInRange("scratchIntervalTicks", 24, 5, 100);
        CHIME_INTERVAL = builder.comment("风铃平均间隔，实际间隔会随机变化。")
            .defineInRange("chimeIntervalTicks", 14, 5, 100);
        VOICE_INTERVAL = builder.comment("村民哼声间隔。")
            .defineInRange("voiceIntervalTicks", 70, 20, 200);
        RAIN_INTERVAL = builder.comment("雨点间隔。")
            .defineInRange("rainIntervalTicks", 20, 20, 100);
        PAPER_INTERVAL = builder.comment("翻书声间隔。")
            .defineInRange("paperIntervalTicks", 24, 10, 100);
        BRUSH_INTERVAL = builder.comment("软刷声间隔。")
            .defineInRange("brushIntervalTicks", 20, 20, 100);
        FIRE_INTERVAL = builder.comment("篝火声间隔。")
            .defineInRange("fireIntervalTicks", 20, 20, 100);
        RADIUS = builder.comment("左右声道的宽度，不影响游戏原声。")
            .defineInRange("radius", 1.5, 0.25, 4.0);
        ORBIT_PERIOD = builder.comment("转一圈所需的游戏刻数，越小越快。")
            .defineInRange("orbitPeriodTicks", 160, 40, 400);
        builder.pop();
        SPEC = builder.build();
    }

    private AsmrConfig() {}

    public static int intervalFor(AsmrKind kind) {
        return intervalValue(kind).get();
    }

    public static void changeInterval(AsmrKind kind, int step) {
        int minimum = kind == AsmrKind.VOICE
            || kind == AsmrKind.RAIN || kind == AsmrKind.BRUSH || kind == AsmrKind.FIRE ? 20
            : kind == AsmrKind.PAPER ? 10 : 5;
        int maximum = kind == AsmrKind.VOICE ? 200 : 100;
        ForgeConfigSpec.IntValue setting = intervalValue(kind);
        setting.set(Math.max(minimum, Math.min(maximum, setting.get() + step)));
    }

    private static ForgeConfigSpec.IntValue intervalValue(AsmrKind kind) {
        switch (kind) {
            case TAP: return TAP_INTERVAL;
            case SCRATCH: return SCRATCH_INTERVAL;
            case CHIME: return CHIME_INTERVAL;
            case VOICE: return VOICE_INTERVAL;
            case RAIN: return RAIN_INTERVAL;
            case PAPER: return PAPER_INTERVAL;
            case BRUSH: return BRUSH_INTERVAL;
            case FIRE: return FIRE_INTERVAL;
            default: throw new IllegalStateException();
        }
    }
}
