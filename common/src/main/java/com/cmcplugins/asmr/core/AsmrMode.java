package com.cmcplugins.asmr.core;

public enum AsmrMode {
    ALTERNATE, ORBIT, RANDOM;

    public static AsmrMode fromCommand(String value) {
        if ("orbit".equals(value)) return ORBIT;
        if ("random".equals(value)) return RANDOM;
        return ALTERNATE;
    }
}
