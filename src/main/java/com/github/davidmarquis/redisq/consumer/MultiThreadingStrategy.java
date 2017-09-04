package com.github.davidmarquis.redisq.consumer;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class MultiThreadingStrategy implements ThreadingStrategy {

    private static final Logger log = LoggerFactory.getLogger(MultiThreadingStrategy.class);

    private static final long MAX_WAIT_MILLIS_WHEN_STOPPING_THREADS = 30000;

    private int numThreads;
    private List<DequeueThread> dequeueThreads;

    /**
     * @param numThreads number of item processing threads to spawn.
     */
    public MultiThreadingStrategy(int numThreads) {
        this.numThreads = numThreads;
        this.dequeueThreads = new ArrayList<DequeueThread>(numThreads);
    }

    public void start(String queueName, Runnable callback) {
        for (int i = 0; i < numThreads; i++) {
            DequeueThread dequeueThread = new DequeueThread(callback);

            dequeueThread.setName(String.format("redisq-consumer[%s]%s", queueName, i));
            dequeueThread.start();

            dequeueThreads.add(dequeueThread);

            log.debug(String.format("Started message consumer thread [%s]", dequeueThread.getName()));
        }
    }

    public void stop() {
        try {
            for (DequeueThread dequeueThread : dequeueThreads) {
                log.debug(String.format("Stopping message consuming thread [%s]", dequeueThread.getName()));
                dequeueThread.stopRequested = true;
            }
            waitForAllThreadsToTerminate();
        } finally {
            dequeueThreads.clear();
        }
    }

    private void waitForAllThreadsToTerminate() {
        for (DequeueThread dequeueThread : dequeueThreads) {
            try {
                dequeueThread.join(MAX_WAIT_MILLIS_WHEN_STOPPING_THREADS);
            } catch (InterruptedException e) {
                log.warn(String.format("Unable to join thread [%s].", dequeueThread.getName()));
            }
        }
    }

    protected class DequeueThread extends Thread {
        private boolean stopRequested = false;
        private Runnable callback;

        public DequeueThread(Runnable callback) {
            this.callback = callback;
        }

        @Override
        public void run() {
            while (!stopRequested && !isInterrupted()) {
                try {
                    callback.run();
                } catch (Throwable t) {
                    log.error("Exception while handling next queue item.", t);
                }
            }
        }
    }
}
