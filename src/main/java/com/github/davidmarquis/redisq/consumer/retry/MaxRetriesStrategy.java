package com.github.davidmarquis.redisq.consumer.retry;

import com.github.davidmarquis.redisq.Message;
import com.github.davidmarquis.redisq.MessageQueue;
import com.github.davidmarquis.redisq.persistence.RedisOps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class MaxRetriesStrategy<T> implements MessageRetryStrategy<T>{

    private static final Logger log = LoggerFactory.getLogger(MaxRetriesStrategy.class);

    @Autowired
    private RedisOps redisOps;

    private int maxRetries;

    public MaxRetriesStrategy(int maxRetries) {
        this.maxRetries = maxRetries;
    }

    public void retry(Message<T> message, MessageQueue queue, String consumerId) {
        int currentRetries = message.getRetryCount();

        // currentRetries starts at zero
        if (currentRetries >= (maxRetries - 1)) {
            log.debug(String.format("Max retries [%s] reached for message with ID [%s] on queue [%s]",
                    maxRetries, message.getId(), queue.getQueueName()));
            return;
        }

        message.setRetryCount(++currentRetries);
        queue.enqueue(message, consumerId);
    }

    public void setRedisOps(RedisOps redisOps) {
        this.redisOps = redisOps;
    }
}
