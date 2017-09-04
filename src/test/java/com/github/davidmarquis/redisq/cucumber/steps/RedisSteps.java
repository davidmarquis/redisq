package com.github.davidmarquis.redisq.cucumber.steps;

import cucumber.api.DataTable;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Then;
import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * Steps related to raw Redis lookups (used for validating Redis data content)
 */
public class RedisSteps extends Steps {
    @Autowired
    private RedisTemplate<String, ?> redisTemplate;

    private Map currentCheckedHash = null;

    @Before
    public void beforeScenario() {
        currentCheckedHash = null;
    }

    @Then("^A Redis hash should exist in key \\\"([^\\\"]*)\\\"$")
    public void A_Redis_hash_should_exist_in_key_with_values(String key) throws Throwable {

        assertThat("Key should exist", redisTemplate.hasKey(key), is(true));

        currentCheckedHash = redisTemplate.opsForHash().entries(key);

        assertThat(currentCheckedHash, is(notNullValue()));
    }

    @And("^with these values:$")
    public void with_these_values(DataTable expected) throws Throwable {
        Map<String, String> actuals = currentCheckedHash;

        List<List<String>> keyValues = expected.raw();
        for (List<String> keyValue : keyValues) {
            String lookupKey = keyValue.get(0);
            String expectedValue = keyValue.get(1);

            assertThat(actuals, hasEntry(lookupKey, expectedValue));
        }
    }

    @And("^having attributes \\[(.*)\\]$")
    public void having_attributes_id_payload_creation_ttl(List<String> attributes) throws Throwable {
        Map actuals = currentCheckedHash;

        Set<Object> keys = actuals.keySet();
        assertThat(keys, hasItems(attributes.toArray()));
    }

    @Then("^No Redis key should match pattern \\\"([^\\\"]*)\\\"$")
    public void No_key_should_match_pattern(String keyPattern) throws Throwable {
        assertThat("No key should match pattern", redisTemplate.keys(keyPattern).isEmpty(), is(true));
    }

    @Then("^A Redis list should exist in key \\\"([^\\\"]*)\\\" with (\\d+) element[s]?$")
    public void A_Redis_list_should_exist_in_key_with_element(String key, long expectedListSize) throws Throwable {
        assertThat("Key should exist", redisTemplate.hasKey(key), is(true));
        assertThat("Redis list size", redisTemplate.opsForList().size(key), is(expectedListSize));
    }

    @Then("^A Redis key \\\"([^\\\"]*)\\\" should be set to expire$")
    public void A_Redis_key_should_be_set_to_expire(String key) throws Throwable {
        Long expiry = redisTemplate.getExpire(key);
        assertThat("Key should expire", expiry, is(notNullValue()));
    }
}
