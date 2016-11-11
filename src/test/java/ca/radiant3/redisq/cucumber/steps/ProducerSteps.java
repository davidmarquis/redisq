package ca.radiant3.redisq.cucumber.steps;

import ca.radiant3.redisq.producer.MessageProducer;
import ca.radiant3.redisq.producer.MessageProducerImpl;
import ca.radiant3.redisq.producer.MessageSender;
import cucumber.api.java.Before;
import cucumber.api.java.en.And;
import cucumber.api.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import java.util.concurrent.TimeUnit;

public class ProducerSteps {
    @Autowired
    private ApplicationContext ctx;
    @Autowired
    private QueueSteps queueSteps;

    private MessageSender<String> messageSender;

    private MessageProducer<String> messageProducer;

    @Before
    public void beforeScenario() {
        messageProducer = null;
        messageSender = null;
    }

    @When("^A producer exists on queue named \\\"([^\\\"]*)\\\"$")
    public void A_producer_pushes_a_message_on_queue_for_all_consumers(String queueName) throws Throwable {
        messageProducer = aMessageProducer(queueName);
    }

    @When("^The producer creates a message with content \\\"([^\\\"]*)\\\"$")
    public void The_producer_creates_a_message_with_content(String messageContent) throws Throwable {
        messageSender = messageProducer.create(messageContent);
    }

    @And("^submits the message to all consumers$")
    public void submits_the_message_to_all_consumers() throws Throwable {
        messageSender.submit();
    }

    @And("^submits the message to specific consumer \\\"([^\\\"]*)\\\"$")
    public void submits_the_message_to_specific_consumer(String consumerId) throws Throwable {
        messageSender.withTargetConsumer(consumerId).submit();
    }

    @And("^specifies a time to live of (\\d+) ([A-Z]*)$")
    public void specifies_a_time_to_live_of_HOURS(int time, TimeUnit timeUnit) throws Throwable {
        messageSender.withTimeToLive(time, timeUnit);
    }

    private MessageProducer<String> aMessageProducer(String queue) {
        MessageProducerImpl<String> producer = ctx.getAutowireCapableBeanFactory().createBean(MessageProducerImpl.class);
        producer.setQueue(queueSteps.queueWithName(queue));

        return producer;
    }
}
