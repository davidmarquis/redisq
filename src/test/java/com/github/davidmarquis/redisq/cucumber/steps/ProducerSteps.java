package com.github.davidmarquis.redisq.cucumber.steps;

import com.github.davidmarquis.redisq.producer.DefaultMessageProducer;
import com.github.davidmarquis.redisq.producer.MessageProducer;
import com.github.davidmarquis.redisq.producer.MessageSender;
import cucumber.api.java.Before;
import cucumber.api.java.en.And;
import cucumber.api.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import java.util.concurrent.TimeUnit;

public class ProducerSteps extends Steps {
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

    @SuppressWarnings("unchecked")
    private MessageProducer<String> aMessageProducer(String queue) {
        DefaultMessageProducer<String> producer = ctx.getAutowireCapableBeanFactory().createBean(DefaultMessageProducer.class);
        producer.setQueue(queueSteps.queueWithName(queue));

        return producer;
    }
}
