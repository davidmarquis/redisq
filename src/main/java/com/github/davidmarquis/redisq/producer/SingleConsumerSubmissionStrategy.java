package com.github.davidmarquis.redisq.producer;

import com.github.davidmarquis.redisq.Message;
import com.github.davidmarquis.redisq.MessageQueue;
import com.github.davidmarquis.redisq.persistence.RedisOps;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Submits messages to a single consumer (the default consumer configured on the queue).
 * Using this strategy slightly improves the performance on message submission, because there is no need
 * to query Redis for the list of registered consumers on the queue for each message.
 */
public class SingleConsumerSubmissionStrategy implements SubmissionStrategy {

    protected RedisOps redisOps;

    @Autowired
    public SingleConsumerSubmissionStrategy(RedisOps redisOps) {
        this.redisOps = redisOps;
    }

    public void submit(MessageQueue queue, Message<?> message) {

        submit(queue, message, queue.getDefaultConsumerId());
    }

    public void submit(MessageQueue queue, Message<?> message, String consumer) {

        queue.enqueue(message, consumer);
    }
}
