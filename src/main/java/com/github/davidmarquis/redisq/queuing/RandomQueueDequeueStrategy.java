package com.github.davidmarquis.redisq.queuing;

import com.github.davidmarquis.redisq.consumer.MessageCallback;
import com.github.davidmarquis.redisq.persistence.RedisOps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Implementation of a random queue.
 * <ul>
 *  <li>Enqueuing: Messages are added to a Redis set, then a notification is added to a Redis list.</li>
 *  <li>Dequeuing: Messages are popped from the set for as long as there are items, then the helper list
 *  is checked for addition using a blocking operation.</li>
 * </ul>
 * A dequeueTimeout can be set to configure the time to wait for a message in the queue between passes.
 */
public class RandomQueueDequeueStrategy implements QueueDequeueStrategy {
    private static final Logger log = LoggerFactory.getLogger(RandomQueueDequeueStrategy.class);

    protected RedisOps redisOps;

    /**
     * Amount of time to wait for each dequeue operation in the backing redis list.
     */
    private long dequeueTimeoutSeconds = 1;

    /**
     * Since the dequeuing mechanism is a while loop, the dequeuing mechanism will dequeue items from the queue
     * for as long as there are items available. This does not play safe with graceful shutdown of the application,
     * so it's possible to set a batch size in order to give back control to the main thread occasionally.
     * Default value is -1, meaning dequeuing is not batched.
     */
    private long dequeueBatchSize = -1;

    @Autowired
    public RandomQueueDequeueStrategy(RedisOps redisOps) {
        this.redisOps = redisOps;
    }

    public void enqueueMessage(String queueName, String consumerId, String messageId) {

        log.debug(String.format("[Random] Enqueuing message ID [%s] to queue [%s(%s)]", messageId, queueName, consumerId));

        redisOps.enqueueMessageInSet(queueName, consumerId, messageId);
        redisOps.notifyWaitersOnSet(queueName, consumerId);
    }

    public void dequeueNextMessage(String queueName, String consumerId, MessageCallback callback) {

        boolean isBatched = (dequeueBatchSize > 0);
        int batchCount = 0;
        String messageId;

        while((messageId = redisOps.randomPopFromSet(queueName, consumerId)) != null) {

            log.debug(String.format("[Random] Dequeued message ID [%s] from queue [%s(%s)]", messageId, queueName, consumerId));

            callback.handle(messageId);

            if (isBatched && batchCount++ >= dequeueBatchSize) {
                // most of the time, the set will still contain items, so we just return instead of breaking.
                return;
            }

            if (Thread.currentThread().isInterrupted()) {
                return;
            }
        }

        redisOps.waitOnSet(queueName, consumerId, dequeueTimeoutSeconds);
    }

    public void setDequeueBatchSize(long dequeueBatchSize) {
        this.dequeueBatchSize = dequeueBatchSize;
    }

    public void setDequeueTimeoutSeconds(long dequeueTimeoutSeconds) {
        this.dequeueTimeoutSeconds = dequeueTimeoutSeconds;
    }
}
