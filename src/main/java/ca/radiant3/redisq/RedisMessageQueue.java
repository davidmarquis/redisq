package ca.radiant3.redisq;

import ca.radiant3.redisq.consumer.MessageCallback;
import ca.radiant3.redisq.persistence.RedisOps;
import ca.radiant3.redisq.queuing.FIFOQueueDequeueStrategy;
import ca.radiant3.redisq.queuing.QueueDequeueStrategy;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.util.Collection;

public class RedisMessageQueue implements MessageQueue {

    private static final String DEFAULT_CONSUMER_ID = "default";

    @Autowired
    private RedisOps redisOps;

    private String queueName;

    private String defaultConsumerId = DEFAULT_CONSUMER_ID;

    private QueueDequeueStrategy queueDequeueStrategy;

    @PostConstruct
    public void initialize() {
        if (queueDequeueStrategy == null) {
            queueDequeueStrategy = new FIFOQueueDequeueStrategy(redisOps);
        }
    }

    public String getQueueName() {
        return queueName;
    }

    public Collection<String> getCurrentConsumerIds() {
        return redisOps.getRegisteredConsumers(queueName);
    }

    public long getSize() {
        return getSizeForConsumer(getDefaultConsumerId());
    }

    public long getSizeForConsumer(String consumerId) {
        Long size = redisOps.getQueueSizeForConsumer(queueName, consumerId);
        return (size == null) ? 0 : size;
    }

    public void empty() {
        redisOps.emptyQueue(queueName);
    }

    public String getDefaultConsumerId() {
        return defaultConsumerId;
    }

    public void enqueue(Message<?> message, String... consumers) {
        redisOps.saveMessage(queueName, message);

        for (String consumer : consumers) {
            queueDequeueStrategy.enqueueMessage(queueName, consumer, message.getId());
        }
    }

    public void dequeue(String consumer, MessageCallback callback) {
        queueDequeueStrategy.dequeueNextMessage(queueName, consumer, callback);
    }

    public void setQueueName(String queueName) {
        this.queueName = queueName;
    }

    public void setRedisOps(RedisOps redisOps) {
        this.redisOps = redisOps;
    }

    public void setDefaultConsumerId(String defaultConsumerId) {
        this.defaultConsumerId = defaultConsumerId;
    }

    public void setQueueDequeueStrategy(QueueDequeueStrategy queueDequeueStrategy) {
        this.queueDequeueStrategy = queueDequeueStrategy;
    }
}
