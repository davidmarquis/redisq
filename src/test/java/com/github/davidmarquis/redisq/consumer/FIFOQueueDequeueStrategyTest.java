package com.github.davidmarquis.redisq.consumer;

import com.github.davidmarquis.redisq.persistence.RedisOps;
import com.github.davidmarquis.redisq.queuing.FIFOQueueDequeueStrategy;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.*;
import static org.mockito.internal.verification.VerificationModeFactory.times;

@RunWith(MockitoJUnitRunner.class)
public class FIFOQueueDequeueStrategyTest {

    private FIFOQueueDequeueStrategy defaultDequeueStrategy;

    @Mock
    private RedisOps redisOps;

    @Before
    public void setup() {
        defaultDequeueStrategy = new FIFOQueueDequeueStrategy(redisOps);
        defaultDequeueStrategy.setDequeueTimeoutSeconds(1);
    }

    @Test
    public void test_dequeue_message() {

        String queueName = "test_queue_name";
        String consumerId = "test_consumer_id";

        when(redisOps.dequeueMessageFromHead("test_queue_name", "test_consumer_id", 1)).thenReturn("1");

        MessageCallback callback = mock(MessageCallback.class);

        defaultDequeueStrategy.dequeueNextMessage(queueName, consumerId, callback);

        verify(callback, times(1)).handle("1");
    }

    @Test
    public void test_dequeue_message_no_message_in_queue() {

        String queueName = "test_queue_name";
        String consumerId = "test_consumer_id";

        when(redisOps.dequeueMessageFromHead("test_queue_name", "test_consumer_id", 1)).thenReturn(null);

        MessageCallback callback = mock(MessageCallback.class);

        defaultDequeueStrategy.dequeueNextMessage(queueName, consumerId, callback);

        verify(callback, never()).handle(any(String.class));
    }
}
