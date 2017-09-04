package com.github.davidmarquis.redisq.consumer;

import com.github.davidmarquis.redisq.Message;
import com.github.davidmarquis.redisq.consumer.retry.RetryableMessageException;

public interface MessageListener<T> {

    void onMessage(Message<T> message) throws RetryableMessageException;
}
