package com.abiddarris.common.utils;

import static com.abiddarris.common.utils.Preconditions.checkNonNull;

import java.util.ArrayDeque;
import java.util.Queue;

public class Looper {

    private final Object lock = new Object();
    private final Queue<Runnable> runnables = new ArrayDeque<>();

    private volatile boolean running;

    public void submitRunnable(Runnable runnable) {
        checkNonNull(runnable, "runnable cannot be null");
        runnables.offer(runnable);

        notifyLooperThread();
    }

    private void notifyLooperThread() {
        synchronized (lock) {
            lock.notify();
        }
    }

    public void loop() {
        running = true;
        while (running) {
            Runnable runnable = runnables.poll();
            if (runnable == null) {
                synchronized (lock) {
                    try {
                        lock.wait();
                    } catch (InterruptedException e) {
                    }
                }
                continue;
            }

            runnable.run();
        }
    }

    public boolean isRunning() {
        return running;
    }

    public void stop() {
        running = false;

        notifyLooperThread();
    }
}
