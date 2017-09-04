package com.github.davidmarquis.redisq.cucumber.steps;

import cucumber.api.java.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;

public class RedisHooks extends Steps {

    @Autowired
    private RedisTemplate redisTemplate;


    @Qualifier("jedisConnectionFactory")
    @Autowired
    private JedisConnectionFactory jedisConnectionFactory;

    @Before
    public void setupGlobal() {
        jedisConnectionFactory.setDatabase(2);

        redisTemplate.execute(new RedisCallback<Object>() {
            public String doInRedis(RedisConnection connection) throws DataAccessException {
                connection.flushDb();
                return null;
            }
        });
    }
}
