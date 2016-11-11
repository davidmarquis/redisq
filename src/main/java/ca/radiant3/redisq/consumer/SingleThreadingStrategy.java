package ca.radiant3.redisq.consumer;

public class SingleThreadingStrategy extends MultiThreadingStrategy {

    public SingleThreadingStrategy() {
        super(1);
    }
}
