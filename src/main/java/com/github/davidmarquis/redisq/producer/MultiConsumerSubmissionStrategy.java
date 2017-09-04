package com.github.davidmarquis.redisq.producer;

import com.github.davidmarquis.redisq.Message;
import com.github.davidmarquis.redisq.MessageQueue;
import com.github.davidmarquis.redisq.persistence.RedisOps;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collection;

/**
 * Submits messages to all registered consumers on a queue.
 */
public class MultiConsumerSubmissionStrategy extends SingleConsumerSubmissionStrategy {

    @Autowired
    public MultiConsumerSubmissionStrategy(RedisOps redisOps) {
        super(redisOps);
    }

    @Override
    public void submit(MessageQueue queue, Message<?> message) {

        Collection<String> allConsumers = redisOps.getRegisteredConsumers(queue.getQueueName());
        if (allConsumers.isEmpty()) {
            // use single consumer behavior
            super.submit(queue, message);
        } else {
            queue.enqueue(message, allConsumers.toArray(new String[allConsumers.size()]));
        }
    }
}
