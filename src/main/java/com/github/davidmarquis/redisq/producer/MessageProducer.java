package com.github.davidmarquis.redisq.producer;

/**
 * Interface that should be used in client code to publish/produce messages on a message queue.
 *
 * <code>
 * queue.create("Some payload").submit();
 * </code>
 *
 * @param <T>
 */
public interface MessageProducer<T> {

    void submit(T payload);

    MessageSender<T> create(T payload);
}
