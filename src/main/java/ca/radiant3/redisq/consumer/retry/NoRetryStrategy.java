package ca.radiant3.redisq.consumer.retry;

import ca.radiant3.redisq.Message;
import ca.radiant3.redisq.MessageQueue;

public class NoRetryStrategy<T> implements MessageRetryStrategy<T> {

    public void retry(Message<T> message, MessageQueue queue, String consumerId) {
        /* no-op */
    }
}
