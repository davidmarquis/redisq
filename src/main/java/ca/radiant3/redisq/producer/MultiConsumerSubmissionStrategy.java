package ca.radiant3.redisq.producer;

import ca.radiant3.redisq.Message;
import ca.radiant3.redisq.MessageQueue;
import ca.radiant3.redisq.persistence.RedisOps;
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
