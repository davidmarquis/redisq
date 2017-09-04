package com.github.davidmarquis.redisq.consumer.retry;

import com.github.davidmarquis.redisq.Message;
import com.github.davidmarquis.redisq.MessageQueue;

public class NoRetryStrategy<T> implements MessageRetryStrategy<T> {

    public void retry(Message<T> message, MessageQueue queue, String consumerId) {
        /* no-op */
    }
}
