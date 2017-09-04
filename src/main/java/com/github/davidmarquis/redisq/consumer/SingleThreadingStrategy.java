package com.github.davidmarquis.redisq.consumer;

public class SingleThreadingStrategy extends MultiThreadingStrategy {

    public SingleThreadingStrategy() {
        super(1);
    }
}
