package com.github.davidmarquis.redisq.queuing;

import com.github.davidmarquis.redisq.consumer.MessageCallback;

/**
 * Delegation of the queuing and dequeuing mechanism for a MessageQueue.
 *
 * The queuing strategy will be applied every time a producer needs to produce a message.
 * The dequeuing strategy is applied continuously on the main consumer loop.
 *
 * Note: The same strategy must be set on both the consumer and producer sides. If message production and consumptions
 * are setup in 2 different applications, their strategies should match otherwise bad results are expected to happen.
 */
public interface QueueDequeueStrategy {

    /**
     * The strategy should take the next message in queue and call the MessageCallback callback interface once
     * the Message has been loaded. This method can block for a certain time, but should regularly release the block
     * and let the main consumer loop continue (or stop when needed!).
     * @param queueName name of the queue we're dequeuing from.
     * @param consumerId name of a specific consumer we're dequeuing for.
     * @param callback the callback to be called when the next message is dequeued.
     */
    void dequeueNextMessage(String queueName, String consumerId, MessageCallback callback);

    /**
     * Enqueue a message in the queue. The actual enqueue mechanism is not determined, but general contract is that
     * a message that is enqueue here should immediately be available when calling dequeueNextMessage.
     * @param queueName name of the queue we're enqueuing to.
     * @param consumerId name of a specific consumer we're enqueuing for.
     * @param messageId the message ID to enqueue (the message itself is already persisted when we come here).
     */
    void enqueueMessage(String queueName, String consumerId, String messageId);
}
