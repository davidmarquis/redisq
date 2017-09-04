package com.github.davidmarquis.redisq.cucumber.steps;

import com.github.davidmarquis.redisq.MessageQueue;
import com.github.davidmarquis.redisq.RedisMessageQueue;
import com.github.davidmarquis.redisq.persistence.RedisOps;
import com.github.davidmarquis.redisq.queuing.RandomQueueDequeueStrategy;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class QueueSteps extends Steps {

    @Autowired
    private ApplicationContext ctx;
    @Autowired
    private RedisOps redisOps;

    private Map<String, MessageQueue> queues = new HashMap<String, MessageQueue>();
    private String lastQueueCreated = null;

    @Before
    public void beforeScenario() {
        queues.clear();
    }

    @Given("^A queue named \\\"([^\\\"]*)\\\" exists$")
    public void A_queue_named_exists(String queueName) throws Throwable {
        RedisMessageQueue queue = ctx.getAutowireCapableBeanFactory().createBean(RedisMessageQueue.class);
        queue.setQueueName(queueName);

        queues.put(queueName, queue);
        lastQueueCreated = queueName;
    }

    public MessageQueue queueWithName(String name) {
        MessageQueue queue = queues.get(name);
        assertThat(queue, is(notNullValue()));

        return queue;
    }

    @And("^this queue has a Random queue/dequeue strategy configured$")
    public void this_queue_has_a_Random_queue_dequeue_strategy_configured() throws Throwable {
        RedisMessageQueue queue = (RedisMessageQueue) queueWithName(lastQueueCreated);

        queue.setQueueDequeueStrategy(new RandomQueueDequeueStrategy(redisOps));
    }
}
