package ca.radiant3.redisq.consumer.retry;

import ca.radiant3.redisq.Message;
import ca.radiant3.redisq.MessageQueue;

public interface MessageRetryStrategy<T> {

    void retry(Message<T> message, MessageQueue queue, String consumerId);
}
