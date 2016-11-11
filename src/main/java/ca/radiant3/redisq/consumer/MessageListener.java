package ca.radiant3.redisq.consumer;

import ca.radiant3.redisq.Message;
import ca.radiant3.redisq.consumer.retry.RetryableMessageException;

public interface MessageListener<T> {

    void onMessage(Message<T> message) throws RetryableMessageException;
}
