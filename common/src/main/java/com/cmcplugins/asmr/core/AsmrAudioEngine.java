package com.cmcplugins.asmr.core;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.SourceDataLine;

public final class AsmrAudioEngine {
    private static final Object LOCK = new Object();
    private static final Object OUTPUT_LOCK = new Object();
    private static final Queue<Job> QUEUE = new ArrayDeque<Job>();
    private static volatile long generation;
    private static volatile SourceDataLine output;
    private static Thread thread;
    private static volatile boolean failed;
    private static volatile boolean closing;
    private static volatile int activeVoices;

    private AsmrAudioEngine() {}

    public static void play(short[] pcm, double[] position, float volume) {
        synchronized (LOCK) {
            if (failed || closing || pcm == null || pcm.length == 0) return;
            if (QUEUE.size() >= 12) return;
            double pan = Math.max(-1.0, Math.min(1.0, position[0] / 2.0));
            double left = Math.sqrt((1.0 - pan) / 2.0) * volume;
            double right = Math.sqrt((1.0 + pan) / 2.0) * volume;
            QUEUE.add(new Job(pcm, left, right, generation));
            if (thread == null || !thread.isAlive()) startThread();
            LOCK.notifyAll();
        }
    }

    private static void startThread() {
        thread = new Thread(AsmrAudioEngine::run, "ASMR audio");
        thread.setDaemon(true);
        thread.start();
    }

    public static void stopAll() {
        synchronized (LOCK) {
            generation++;
            QUEUE.clear();
            activeVoices = 0;
            LOCK.notifyAll();
        }
        SourceDataLine line = output;
        if (line != null) {
            synchronized (OUTPUT_LOCK) {
                if (line.isOpen()) line.flush();
            }
        }
    }

    public static boolean failed() {
        return failed;
    }

    static int activeVoices() {
        return activeVoices;
    }

    public static void close() {
        Thread current;
        synchronized (LOCK) {
            closing = true;
            generation++;
            QUEUE.clear();
            activeVoices = 0;
            current = thread;
            LOCK.notifyAll();
        }
        SourceDataLine line = output;
        if (line != null) {
            synchronized (OUTPUT_LOCK) {
                if (line.isOpen()) {
                    line.stop();
                    line.flush();
                    line.close();
                }
            }
        }
        if (current != null && current != Thread.currentThread()) {
            try {
                current.join(1000L);
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
            }
        }
        synchronized (LOCK) {
            if (thread == current && (current == null || !current.isAlive())) thread = null;
            closing = false;
        }
    }

    private static void run() {
        SourceDataLine line = null;
        try {
            AudioFormat format = new AudioFormat(AsmrSynth.SAMPLE_RATE, 16, 2, true, false);
            line = AudioSystem.getSourceDataLine(format);
            line.open(format, 8192);
            line.start();
            output = line;
            mix(line);
        } catch (Throwable error) {
            if (!closing) {
                failed = true;
                System.err.println("ASMR：音频输出异常：" + error.getMessage());
                error.printStackTrace();
            }
        } finally {
            if (line != null) {
                synchronized (OUTPUT_LOCK) {
                    if (line.isOpen()) line.close();
                }
            }
            output = null;
            activeVoices = 0;
            synchronized (LOCK) {
                if (thread == Thread.currentThread()) thread = null;
                if (!closing && !failed && !QUEUE.isEmpty()) startThread();
            }
        }
    }

    private static void mix(SourceDataLine line) {
        List<Voice> voices = new ArrayList<Voice>();
        byte[] bytes = new byte[512 * 4];
        long idleSince = System.nanoTime();
        while (!closing) {
            long currentGeneration;
            synchronized (LOCK) {
                currentGeneration = generation;
                for (Iterator<Voice> iterator = voices.iterator(); iterator.hasNext();) {
                    if (iterator.next().job.generation != currentGeneration) iterator.remove();
                }
                while (!QUEUE.isEmpty() && voices.size() < 8) {
                    Job job = QUEUE.remove();
                    if (job.generation == currentGeneration) voices.add(new Voice(job));
                }
                activeVoices = voices.size();
                if (voices.isEmpty()) {
                    if (System.nanoTime() - idleSince > 30_000_000_000L) return;
                    try {
                        LOCK.wait(1000L);
                    } catch (InterruptedException ignored) {
                        Thread.currentThread().interrupt();
                        return;
                    }
                    continue;
                }
            }
            idleSince = System.nanoTime();
            for (int frame = 0; frame < 512; frame++) {
                double left = 0.0;
                double right = 0.0;
                for (Voice voice : voices) {
                    if (voice.offset >= voice.job.pcm.length) continue;
                    double sample = voice.job.pcm[voice.offset++] / 32768.0;
                    left += sample * voice.job.left;
                    right += sample * voice.job.right;
                }
                int index = frame * 4;
                writeSample(bytes, index, left);
                writeSample(bytes, index + 2, right);
            }
            for (Iterator<Voice> iterator = voices.iterator(); iterator.hasNext();) {
                Voice voice = iterator.next();
                if (voice.offset >= voice.job.pcm.length) iterator.remove();
            }
            synchronized (OUTPUT_LOCK) {
                if (generation == currentGeneration && !closing && line.isOpen())
                    line.write(bytes, 0, bytes.length);
            }
        }
    }

    private static void writeSample(byte[] bytes, int index, double sample) {
        double limited = sample / (1.0 + Math.abs(sample) * 0.25);
        int value = (int) Math.round(Math.max(-1.0, Math.min(1.0, limited)) * 32767.0);
        bytes[index] = (byte) value;
        bytes[index + 1] = (byte) (value >>> 8);
    }

    private static final class Job {
        final short[] pcm;
        final double left;
        final double right;
        final long generation;

        Job(short[] pcm, double left, double right, long generation) {
            this.pcm = pcm;
            this.left = left;
            this.right = right;
            this.generation = generation;
        }
    }

    private static final class Voice {
        final Job job;
        int offset;

        Voice(Job job) {
            this.job = job;
        }
    }
}
