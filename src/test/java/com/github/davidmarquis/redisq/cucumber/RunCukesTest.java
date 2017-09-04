package com.github.davidmarquis.redisq.cucumber;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;

@RunWith(Cucumber.class)
@CucumberOptions(
        features = {"classpath:cucumber/features"},
        glue = {"com.github.davidmarquis.redisq.cucumber.steps", "cucumber.api.spring"}
)
@ContextConfiguration("classpath:cucumber.xml")
public class RunCukesTest {
}