Feature: A consumer consumes messages on a RedisQ queue
  Scenario: A consumer registers on a queue
    Given A queue named "some.queue" exists
    And   A consumer with ID "consumer1" is watching a queue named "some.queue"

    Then  Consumer with ID "consumer1" should be registered as a consumer of queue "some.queue" in Redis

  Scenario: A single consumer dequeues a message on its queue
    Given A queue named "some.queue" exists
    And   A producer exists on queue named "some.queue"
    And   A consumer with ID "consumer1" is watching a queue named "some.queue"

    When  The producer creates a message with content "bazinga!"
    And   submits the message to all consumers
    And   we wait 500 milliseconds

    Then  The consumer should have received the message with content "bazinga!"

  Scenario: Two consumers with the same consumer ID watching a queue, only 1 should receive a published message
    Given A queue named "some.queue" exists
    And   A producer exists on queue named "some.queue"
    And   A consumer with ID "consumer1" is watching a queue named "some.queue"
    And   A consumer with ID "consumer1" is watching a queue named "some.queue"

    When  The producer creates a message with content "bazinga!"
    And   submits the message to all consumers
    And   we wait 500 milliseconds

    Then  1 consumer should have received the message with content "bazinga!"

  Scenario: Two consumers with different consumer IDs watching a queue, all should receive a published message
    Given A queue named "some.queue" exists
    And   A producer exists on queue named "some.queue"
    And   A consumer with ID "consumer1" is watching a queue named "some.queue"
    And   A consumer with ID "consumer2" is watching a queue named "some.queue"

    When  The producer creates a message with content "bazinga!"
    And   submits the message to all consumers
    And   we wait 500 milliseconds

    Then  2 consumers should have received the message with content "bazinga!"

  Scenario: A consumer raises a RetryableMessageException to trigger the retry
    Given A queue named "some.queue" exists
    And   A producer exists on queue named "some.queue"
    And   A consumer with ID "consumer1" is watching a queue named "some.queue"
    And   That consumer is setup to retry message consumption 2 times
    And   That consumer's message listener throws a RetryableMessageException during execution

    When  The producer creates a message with content "bazinga!"
    And   submits the message to all consumers
    And   we wait 500 milliseconds

    Then  Consumer with ID "consumer1" should have processed message with content "bazinga!" 2 times


