package ca.radiant3.redisq.queuing;

import ca.radiant3.redisq.consumer.MessageCallback;
import ca.radiant3.redisq.persistence.RedisOps;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.atLeastOnce;

@RunWith(MockitoJUnitRunner.class)
public class RandomQueueDequeueStrategyTest {

    private RandomQueueDequeueStrategy randomQueueDequeueStrategy;

    @Mock
    private RedisOps redisOps;

    @Before
    public void setup() {
        randomQueueDequeueStrategy = new RandomQueueDequeueStrategy(redisOps);
        randomQueueDequeueStrategy.setDequeueTimeoutSeconds(1);
    }

    @Test
    public void test_dequeue_messages() {

        // given
        String queueName = "test_queue_name";
        String consumerId = "test_consumer_id";

        when(redisOps.randomPopFromSet("test_queue_name", "test_consumer_id")).thenReturn("2", "1", "3", null);

        MessageCallback callback = mock(MessageCallback.class);

        // execute
        randomQueueDequeueStrategy.dequeueNextMessage(queueName, consumerId, callback);

        // assert
        ArgumentCaptor<String> argument = ArgumentCaptor.forClass(String.class);
        verify(callback, atLeastOnce()).handle(argument.capture());

        assertThat(argument.getAllValues(), is(Arrays.asList("2", "1", "3")));

        verify(redisOps).waitOnSet("test_queue_name", "test_consumer_id", 1);
    }

    @Test
    public void test_enqueue_messages() {

        // given
        String queueName = "test_queue_name";
        String consumerId = "test_consumer_id";

        // execute
        randomQueueDequeueStrategy.enqueueMessage(queueName, consumerId, "some_id");

        // assert
        verify(redisOps).enqueueMessageInSet("test_queue_name", "test_consumer_id", "some_id");
        verify(redisOps).notifyWaitersOnSet("test_queue_name", "test_consumer_id");
    }
}
