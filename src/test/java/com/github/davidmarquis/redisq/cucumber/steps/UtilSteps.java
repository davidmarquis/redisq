package com.github.davidmarquis.redisq.cucumber.steps;

import cucumber.api.java.en.And;

import static java.lang.Thread.sleep;

public class UtilSteps extends Steps {

    @And("^we wait (\\d+) milliseconds$")
    public void we_wait_milliseconds(int waitMillis) throws Throwable {
        sleep(waitMillis);
    }
}
