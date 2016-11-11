package ca.radiant3.redisq.queuing;

import ca.radiant3.redisq.persistence.RedisOps;
import ca.radiant3.redisq.consumer.MessageCallback;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Queue/Dequeue strategy that works upon FIFO principles (First In First Out).
 * <ul>
 *  <li>Enqueuing: Messages are enqueued at the tail of the Redis list that is backing the queue.</li>
 *  <li>Dequeuing: Messages are dequeued from the head of the Redis list, using a BLPOP Redis operation.</li>
 * </ul>
 * A dequeueTimeout can be set to configure the time to wait for a message in the queue between passes.
 */
public class FIFOQueueDequeueStrategy implements QueueDequeueStrategy {
    private static final Logger log = LoggerFactory.getLogger(FIFOQueueDequeueStrategy.class);

    protected RedisOps redisOps;

    /**
     * Amount of time to wait for each dequeue operation in the backing redis list.
     */
    private long dequeueTimeoutSeconds = 1;

    @Autowired
    public FIFOQueueDequeueStrategy(RedisOps redisOps) {
        this.redisOps = redisOps;
    }

    public void enqueueMessage(String queueName, String consumerId, String messageId) {

        log.debug(String.format("[FIFO] Enqueuing message ID [%s] to queue [%s(%s)]", messageId, queueName, consumerId));

        redisOps.enqueueMessageAtTail(queueName, consumerId, messageId);
    }

    public void dequeueNextMessage(String queueName, String consumerId, MessageCallback callback) {

        String messageId = redisOps.dequeueMessageFromHead(queueName, consumerId, dequeueTimeoutSeconds);
        if (StringUtils.isNotEmpty(messageId)) {
            log.debug(String.format("[FIFO] Dequeued message ID [%s] from queue [%s(%s)]", messageId, queueName, consumerId));

            callback.handle(messageId);
        }
    }

    public void setDequeueTimeoutSeconds(long dequeueTimeoutSeconds) {
        this.dequeueTimeoutSeconds = dequeueTimeoutSeconds;
    }
}
