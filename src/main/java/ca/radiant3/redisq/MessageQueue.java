package ca.radiant3.redisq;

import ca.radiant3.redisq.consumer.MessageCallback;

import java.util.Collection;

/**
 * Read-only representation of a message queue. To setup either a producer or consumer on a Redis
 * queue, a MessageQueue instance must exist and given as a dependency to either MessageProducer
 * or MessageConsumer.
 */
public interface MessageQueue {

    String getQueueName();

    String getDefaultConsumerId();

    Collection<String> getCurrentConsumerIds();

    long getSize();

    long getSizeForConsumer(String consumerId);

    void empty();

    void enqueue(Message<?> message, String... consumers);

    void dequeue(String consumer, MessageCallback callback);
}
