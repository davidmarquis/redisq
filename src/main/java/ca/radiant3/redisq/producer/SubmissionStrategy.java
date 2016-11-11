package ca.radiant3.redisq.producer;

import ca.radiant3.redisq.Message;
import ca.radiant3.redisq.MessageQueue;

public interface SubmissionStrategy {

    void submit(MessageQueue queue, Message<?> message);

    void submit(MessageQueue queue, Message<?> message, String consumer);
}
