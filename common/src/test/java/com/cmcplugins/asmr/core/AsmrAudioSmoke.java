package com.cmcplugins.asmr.core;

public final class AsmrAudioSmoke {
    public static void main(String[] args) throws InterruptedException {
        for (AsmrKind kind : AsmrKind.values()) {
            if (kind != AsmrKind.VOICE) checkStop(kind);
        }
        AsmrAudioEngine.close();
        if (AsmrAudioEngine.failed()) throw new AssertionError("立体声音频设备不可用");
        System.out.println("立体声音频冒烟检查通过");
    }

    private static void checkStop(AsmrKind kind) throws InterruptedException {
        long deadline = System.currentTimeMillis() + 2000L;
        for (int i = 0; i < 30 && AsmrAudioEngine.activeVoices() == 0; i++) {
            AsmrAudioEngine.play(AsmrSynth.generate(kind, i), new double[] {1.5, 0.0, 0.0}, 0.02f);
            Thread.sleep(10L);
            if (System.currentTimeMillis() > deadline) break;
        }
        if (AsmrAudioEngine.activeVoices() == 0) throw new AssertionError("声音未开始播放：" + kind);
        AsmrAudioEngine.stopAll();
        if (AsmrAudioEngine.activeVoices() != 0) throw new AssertionError("停止后仍有声音源：" + kind);
    }

}
