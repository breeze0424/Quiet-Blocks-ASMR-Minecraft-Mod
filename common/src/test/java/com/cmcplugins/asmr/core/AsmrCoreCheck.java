package com.cmcplugins.asmr.core;

import java.util.Arrays;

public final class AsmrCoreCheck {
    public static void main(String[] args) {
        for (AsmrKind kind : AsmrKind.values()) {
            if (kind == AsmrKind.VOICE) continue;
            short[] a = AsmrSynth.generate(kind, 12345L);
            short[] b = AsmrSynth.generate(kind, 12345L);
            require(Arrays.equals(a, b), "合成结果必须可复现");
            require(a.length > 4000 && a.length < AsmrSynth.SAMPLE_RATE * 2, "样本长度不正确");
            int peak = 0;
            for (short sample : a) peak = Math.max(peak, Math.abs((int) sample));
            require(peak > 100 && peak < 30000, "声音幅度不正确");
        }
        short[] tap = AsmrSynth.generate(AsmrKind.TAP, 12345L);
        long initial = 0L;
        long tail = 0L;
        for (int i = 0; i < 1000; i++) {
            initial += Math.abs((int) tap[i]);
            tail += Math.abs((int) tap[tap.length - 1000 + i]);
        }
        require(tail * 10 < initial, "敲击尾音衰减过慢");
        double[] left = AsmrPattern.position(AsmrMode.ALTERNATE, 0, 0, 1, 1.5);
        double[] right = AsmrPattern.position(AsmrMode.ALTERNATE, 16, 1, 1, 1.5);
        require(left[0] > 0 && right[0] < 0, "左右交替失败");
        double[] a = AsmrPattern.position(AsmrMode.RANDOM, 0, 3, 7, 1.5);
        double[] b = AsmrPattern.position(AsmrMode.RANDOM, 0, 3, 7, 1.5);
        require(Arrays.equals(a, b), "随机方位应由种子稳定决定");
        double[] fastOrbit = AsmrPattern.position(AsmrMode.ORBIT, 40, 0, 7, 1.5, 80);
        double[] slowOrbit = AsmrPattern.position(AsmrMode.ORBIT, 40, 0, 7, 1.5, 160);
        require(fastOrbit[2] < -1.4 && slowOrbit[0] > 1.4, "环绕速度参数未生效");
        double[] quarterTurn = AsmrPattern.orbitPosition(0.25, 1.5);
        require(quarterTurn[0] > 1.4 && Math.abs(quarterTurn[2]) < 0.01, "环绕相位计算错误");
        int firstDelay = AsmrPattern.chimeDelay(14, 0, 7);
        require(firstDelay >= 5 && firstDelay <= 20, "风铃间隔越界");
        require(firstDelay == AsmrPattern.chimeDelay(14, 0, 7), "风铃间隔应由种子稳定决定");
        boolean varied = false;
        for (int i = 1; i < 12; i++) {
            if (AsmrPattern.chimeDelay(14, i, 7) != firstDelay) varied = true;
        }
        require(varied, "风铃间隔应有变化");
        System.out.println("ASMR 核心检查通过");
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
