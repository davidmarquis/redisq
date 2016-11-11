Feature: A producer publishes messages on a RedisQ queue
  Scenario: On a queue with no registered consumers, a Producers submits a message
    Given A queue named "some.topic" exists
    And   A producer exists on queue named "some.topic"
    And   No consumer is watching the queue

    When The producer creates a message with content "toto"
    And  submits the message to all consumers

    Then A Redis hash should exist in key "redisq.some.topic.messages.1"
    And  having attributes [id,payload,creation,ttl]
    And  with these values:
       | id      | 1    |
       | payload | toto |

    Then A Redis list should exist in key "redisq.some.topic.queues.default" with 1 element

  Scenario: On a queue with no registered consumers, a Producers submits a message with a specified TTL
    Given A queue named "some.topic" exists
    And   A producer exists on queue named "some.topic"
    And   No consumer is watching the queue

    When The producer creates a message with content "toto"
    And  specifies a time to live of 3 HOURS
    And  submits the message to all consumers

    Then A Redis hash should exist in key "redisq.some.topic.messages.1"
    And  A Redis key "redisq.some.topic.messages.1" should be set to expire
    And  with these values:
       | ttl | 10800 |

  Scenario: On a queue with no registered consumers, a submitted message with TTL expires
    Given A queue named "some.topic" exists
    And   A producer exists on queue named "some.topic"
    And   No consumer is watching the queue

    When The producer creates a message with content "toto"
    And  specifies a time to live of 1 SECONDS
    And  submits the message to all consumers

    And we wait 2000 milliseconds

    Then No Redis key should match pattern "redisq.some.topic.messages.*"

  Scenario: On a queue with no registered consumers, a Producers submits 2 messages
    Given A queue named "some.topic" exists
    And   A producer exists on queue named "some.topic"
    And   No consumer is watching the queue

    When The producer creates a message with content "toto"
    And  submits the message to all consumers

    When The producer creates a message with content "tata"
    And  submits the message to all consumers

    Then A Redis hash should exist in key "redisq.some.topic.messages.1"
    And  with these values:
      | id | 1 |

    Then A Redis hash should exist in key "redisq.some.topic.messages.2"
    And  with these values:
      | id | 2 |

    Then A Redis list should exist in key "redisq.some.topic.queues.default" with 2 elements

  Scenario: On a queue with 2 registered consumers, a Producer publishes a message to all Consumers
    Given A queue named "some.topic" exists
    And   A producer exists on queue named "some.topic"
    And   A consumer with ID "consumer1" is registered on queue named "some.topic"
    And   A consumer with ID "consumer2" is registered on queue named "some.topic"

    When The producer creates a message with content "toto"
    And  submits the message to all consumers

    Then A Redis list should exist in key "redisq.some.topic.queues.consumer1" with 1 element
    And  A Redis list should exist in key "redisq.some.topic.queues.consumer2" with 1 element

  Scenario: On a queue with 2 registered consumers, a Producer publishes a message to a specific Consumer
    Given A queue named "some.topic" exists
    And   A producer exists on queue named "some.topic"
    And   A consumer with ID "consumer1" is registered on queue named "some.topic"
    And   A consumer with ID "consumer2" is registered on queue named "some.topic"

    When The producer creates a message with content "toto"
    And  submits the message to specific consumer "consumer1"

    Then A Redis list should exist in key "redisq.some.topic.queues.consumer1" with 1 element
    And  No Redis key should match pattern "redisq.some.topic.queues.consumer2"

  Scenario: Queue with Random queue/dequeue stategy
    Given A queue named "some.topic" exists
    And   this queue has a Random queue/dequeue strategy configured
    And   A producer exists on queue named "some.topic"
    And   A consumer with ID "consumer1" is registered on queue named "some.topic"

    When  The producer creates a message with content "bazinga!"
    And   submits the message to all consumers
    And   The producer creates a message with content "more bazinga!"
    And   submits the message to all consumers
    And   we wait 100 milliseconds
    And   A consumer with ID "consumer1" is watching a queue named "some.topic"
    And   we wait 500 milliseconds

    Then  The consumer should have received the following messages:
    | bazinga! |
    | more bazinga! |