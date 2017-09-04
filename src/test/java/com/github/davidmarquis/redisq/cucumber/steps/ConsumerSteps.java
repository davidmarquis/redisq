package com.github.davidmarquis.redisq.cucumber.steps;

import com.github.davidmarquis.redisq.Message;
import com.github.davidmarquis.redisq.consumer.MessageConsumer;
import com.github.davidmarquis.redisq.consumer.retry.MaxRetriesStrategy;
import com.github.davidmarquis.redisq.persistence.RedisOps;
import com.github.davidmarquis.redisq.utils.RecordingMessageListener;
import cucumber.api.DataTable;
import cucumber.api.java.After;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import java.util.*;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class ConsumerSteps extends Steps {
    @Autowired
    private ApplicationContext ctx;

    @Autowired
    private RedisOps redisOps;

    @Autowired
    private QueueSteps queueSteps;

    private Map<String, List<MessageConsumer>> consumersById = new HashMap<String, List<MessageConsumer>>();
    private MessageConsumer lastCreatedConsumer = null;

    @After
    public void afterScenario() throws Throwable {
        if (consumersById.isEmpty()) {
            return;
        }

        for (List<MessageConsumer> consumers : consumersById.values()) {
            for (MessageConsumer consumer : consumers) {
                consumer.stopConsumer();
            }
        }

        consumersById.clear();
    }

    @Given("^A consumer with ID \\\"(.*)\\\" is watching a queue named \\\"(.*)\\\"$")
    public void A_consumer_is_watching_a_queue_named(String consumerId, String queueName) throws Throwable {

        MessageConsumer<String> consumer = new MessageConsumer<String>();
        ctx.getAutowireCapableBeanFactory().autowireBean(consumer);

        consumer.setQueue(queueSteps.queueWithName(queueName));
        consumer.setMessageListener(new RecordingMessageListener());
        consumer.setConsumerId(consumerId);

        List<MessageConsumer> consumers = consumersById.get(consumerId);
        if (consumers == null) {
            consumers = new ArrayList<MessageConsumer>();
            consumersById.put(consumerId, consumers);
        }
        consumers.add(consumer);
        lastCreatedConsumer = consumer;

        consumer.initialize();
    }

    @Then("^Consumer with ID \\\"([^\\\"]*)\\\" should be registered as a consumer of queue \\\"([^\\\"]*)\\\" in Redis$")
    public void Consumer_with_ID_should_be_registered_as_a_consumer_of_queue_in_Redis(String consumerId, String queueName) throws Throwable {
        Collection<String> consumers = redisOps.getRegisteredConsumers(queueName);

        assertThat(consumers, hasItem(consumerId));
    }

    @Then("^The consumer should have received the message with content \\\"([^\\\"]*)\\\"$")
    public void The_consumer_should_receive_the_message_with_content(String messageContent) throws Throwable {
        RecordingMessageListener recorder = recorderForConsumer(theOnlyConsumerExisting());
        LinkedList<Message<String>> receivedMessages = recorder.getRecordedMessages();

        assertThat(receivedMessages.size(), is(1));
        assertThat(receivedMessages.peekFirst().getPayload(), is(messageContent));
    }

    @And("^A consumer with ID \\\"([^\\\"]*)\\\" is registered on queue named \\\"([^\\\"]*)\\\"$")
    public void A_consumer_with_ID_is_registered_on_queue_named(String consumerId, String queue) throws Throwable {
        redisOps.ensureConsumerRegistered(queue, consumerId);
    }

    @And("^No consumer is watching the queue$")
    public void No_consumer_is_watching_the_queue() throws Throwable {
        /* no-op, informational step only */
    }

    @Then("^(\\d+) consumer[s]? should have received the message with content \\\"([^\\\"]*)\\\"$")
    public void Only_consumer_should_have_received_the_message_with_content(int expectedNumberOfConsumers, String messageContent) throws Throwable {
        assertThat(countMessagesReceivedWithContent(messageContent), is(expectedNumberOfConsumers));
    }

    @And("^That consumer is setup to retry message consumption (\\d+) times$")
    public void That_consumer_is_setup_to_retry_message_consumption_times(int maxRetries) throws Throwable {
        if (lastCreatedConsumer == null) {
            throw new IllegalStateException("No consumer was created by tests.");
        }

        MaxRetriesStrategy retryStrategy = new MaxRetriesStrategy(maxRetries);
        retryStrategy.setRedisOps(redisOps);
        lastCreatedConsumer.setRetryStrategy(retryStrategy);
    }

    @And("^That consumer's message listener throws a RetryableMessageException during execution$")
    public void That_consumer_s_message_listener_throws_a_RetryableMessageException_during_execution() throws Throwable {
        RecordingMessageListener recorder = recorderForConsumer(lastCreatedConsumer);
        recorder.setThrowRetryException(true);
    }

    @Then("^Consumer with ID \"([^\"]*)\" should have processed message with content \"([^\"]*)\" (\\d+) times$")
    public void Consumer_with_ID_should_have_processed_message_with_content_times(String consumerId, String content, int times) throws Throwable {
        RecordingMessageListener recorder = recorderForConsumer(consumerId);
        List<Message<String>> messages = recorder.extractRecordedMessagesWithContent(content);

        assertThat(messages.size(), is(times));
    }

    private int countMessagesReceivedWithContent(String expectedContent) {
        int count = 0;
        for (String consumerId : consumersById.keySet()) {
            List<RecordingMessageListener> recordersForConsumer = recordersForConsumer(consumerId);
            for (RecordingMessageListener recorder : recordersForConsumer) {
                for (Message<String> message : recorder.getRecordedMessages()) {
                    String payload = message.getPayload();
                    if (payload.equals(expectedContent)) {
                        count++;
                    }
                }
            }
        }
        return count;
    }

    private String theOnlyConsumerExisting() {
        assertThat("At this point, only 1 consumer should exist", consumersById.size(), is(1));

        return consumersById.keySet().iterator().next();
    }

    private RecordingMessageListener recorderForConsumer(MessageConsumer consumer) {
        return (RecordingMessageListener) consumer.getMessageListener();
    }

    private List<RecordingMessageListener> recordersForConsumer(String consumerId) {
        List<MessageConsumer> consumers = consumersById.get(consumerId);

        assertThat("Consumer not found", consumers, is(notNullValue()));

        List<RecordingMessageListener> listeners = new ArrayList<RecordingMessageListener>(consumers.size());
        for (MessageConsumer consumer : consumers) {
            listeners.add(recorderForConsumer(consumer));
        }
        return listeners;
    }


    private RecordingMessageListener recorderForConsumer(String consumerId) {
        List<RecordingMessageListener> recorders = recordersForConsumer(consumerId);

        assertThat(recorders.size(), is(1));

        return recorders.get(0);
    }

    @Then("^The consumer should have received the following messages:$")
    public void The_consumer_should_have_received_the_following_messages(DataTable expected) throws Throwable {

        List<String> expectedMessages = expected.asList(String.class);

        RecordingMessageListener recorder = recorderForConsumer(theOnlyConsumerExisting());
        LinkedList<Message<String>> receivedMessages = recorder.getRecordedMessages();

        assertThat(receivedMessages.size(), is(expectedMessages.size()));
        for (String expectedMessage : expectedMessages) {

            List<Message<String>> messages = recorder.extractRecordedMessagesWithContent(expectedMessage);

            assertThat(messages.size(), is(1));
        }
    }
}
