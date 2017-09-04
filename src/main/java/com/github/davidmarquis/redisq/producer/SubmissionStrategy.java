package com.github.davidmarquis.redisq.producer;

import com.github.davidmarquis.redisq.Message;
import com.github.davidmarquis.redisq.MessageQueue;

public interface SubmissionStrategy {

    void submit(MessageQueue queue, Message<?> message);

    void submit(MessageQueue queue, Message<?> message, String consumer);
}
