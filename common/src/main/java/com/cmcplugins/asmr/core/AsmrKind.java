package com.cmcplugins.asmr.core;

public enum AsmrKind {
    TAP, SCRATCH, CHIME, VOICE, RAIN, PAPER, BRUSH, FIRE;

    public static AsmrKind fromCommand(String value) {
        if ("scratch".equals(value)) return SCRATCH;
        if ("chime".equals(value)) return CHIME;
        if ("voice".equals(value)) return VOICE;
        if ("rain".equals(value)) return RAIN;
        if ("paper".equals(value)) return PAPER;
        if ("brush".equals(value)) return BRUSH;
        if ("fire".equals(value)) return FIRE;
        return TAP;
    }
}
