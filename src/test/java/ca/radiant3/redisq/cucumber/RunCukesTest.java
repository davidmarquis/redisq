package ca.radiant3.redisq.cucumber;

import cucumber.api.junit.Cucumber;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@Cucumber.Options(
        features = {"classpath:cucumber/features"},
        glue = {"ca.radiant3.redisq.cucumber.steps", "cucumber.runtime.java.spring.hooks"}
)
public class RunCukesTest {
}