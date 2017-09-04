package com.github.davidmarquis.redisq.consumer.retry;

import com.github.davidmarquis.redisq.Message;
import com.github.davidmarquis.redisq.MessageQueue;

public interface MessageRetryStrategy<T> {

    void retry(Message<T> message, MessageQueue queue, String consumerId);
}
