package com.github.davidmarquis.redisq.consumer;

/**
 * Pluggable strategy for managing threads on consumers.
 */
public interface ThreadingStrategy {
    /**
     * Implementation must start the main dequeuing loop here.
     * @param queueName The name of the queue we're dequeuing from.
     * @param callback the callback to be called for each run loop. Running this callback will actually
     *                 dequeue the next message from the queue and process it entirely.
     */
    void start(String queueName, Runnable callback);

    void stop();
}
