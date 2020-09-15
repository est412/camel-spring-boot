/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.camel.model.springboot;

import javax.annotation.Generated;

/**
 * Resilience4j Circuit Breaker EIP configuration
 * 
 * Generated by camel-package-maven-plugin - do not edit this file!
 */
@Generated("org.apache.camel.springboot.maven.SpringBootAutoConfigurationMojo")
public class Resilience4jConfigurationDefinitionCommon {

    /**
     * Refers to an existing
     * io.github.resilience4j.circuitbreaker.CircuitBreaker instance to lookup
     * and use from the registry. When using this, then any other circuit
     * breaker options are not in use.
     */
    private String circuitBreakerRef;
    /**
     * Refers to an existing
     * io.github.resilience4j.circuitbreaker.CircuitBreakerConfig instance to
     * lookup and use from the registry.
     */
    private String configRef;
    /**
     * Configures the failure rate threshold in percentage. If the failure rate
     * is equal or greater than the threshold the CircuitBreaker transitions to
     * open and starts short-circuiting calls. The threshold must be greater
     * than 0 and not greater than 100. Default value is 50 percentage.
     */
    private Float failureRateThreshold;
    /**
     * Configures the number of permitted calls when the CircuitBreaker is half
     * open. The size must be greater than 0. Default size is 10.
     */
    private Integer permittedNumberOfCallsInHalfOpenState = 10;
    /**
     * Configures the size of the sliding window which is used to record the
     * outcome of calls when the CircuitBreaker is closed. slidingWindowSize
     * configures the size of the sliding window. Sliding window can either be
     * count-based or time-based. If slidingWindowType is COUNT_BASED, the last
     * slidingWindowSize calls are recorded and aggregated. If slidingWindowType
     * is TIME_BASED, the calls of the last slidingWindowSize seconds are
     * recorded and aggregated. The slidingWindowSize must be greater than 0.
     * The minimumNumberOfCalls must be greater than 0. If the slidingWindowType
     * is COUNT_BASED, the minimumNumberOfCalls cannot be greater than
     * slidingWindowSize . If the slidingWindowType is TIME_BASED, you can pick
     * whatever you want. Default slidingWindowSize is 100.
     */
    private Integer slidingWindowSize = 100;
    /**
     * Configures the type of the sliding window which is used to record the
     * outcome of calls when the CircuitBreaker is closed. Sliding window can
     * either be count-based or time-based. If slidingWindowType is COUNT_BASED,
     * the last slidingWindowSize calls are recorded and aggregated. If
     * slidingWindowType is TIME_BASED, the calls of the last slidingWindowSize
     * seconds are recorded and aggregated. Default slidingWindowType is
     * COUNT_BASED.
     */
    private String slidingWindowType = "COUNT_BASED";
    /**
     * Configures the minimum number of calls which are required (per sliding
     * window period) before the CircuitBreaker can calculate the error rate.
     * For example, if minimumNumberOfCalls is 10, then at least 10 calls must
     * be recorded, before the failure rate can be calculated. If only 9 calls
     * have been recorded the CircuitBreaker will not transition to open even if
     * all 9 calls have failed. Default minimumNumberOfCalls is 100
     */
    private Integer minimumNumberOfCalls = 100;
    /**
     * Enables writable stack traces. When set to false, Exception.getStackTrace
     * returns a zero length array. This may be used to reduce log spam when the
     * circuit breaker is open as the cause of the exceptions is already known
     * (the circuit breaker is short-circuiting calls).
     */
    private Boolean writableStackTraceEnabled = true;
    /**
     * Configures the wait duration (in seconds) which specifies how long the
     * CircuitBreaker should stay open, before it switches to half open. Default
     * value is 60 seconds.
     */
    private Integer waitDurationInOpenState = 60;
    /**
     * Enables automatic transition from OPEN to HALF_OPEN state once the
     * waitDurationInOpenState has passed.
     */
    private Boolean automaticTransitionFromOpenToHalfOpenEnabled = false;
    /**
     * Configures a threshold in percentage. The CircuitBreaker considers a call
     * as slow when the call duration is greater than slowCallDurationThreshold
     * Duration. When the percentage of slow calls is equal or greater the
     * threshold, the CircuitBreaker transitions to open and starts
     * short-circuiting calls. The threshold must be greater than 0 and not
     * greater than 100. Default value is 100 percentage which means that all
     * recorded calls must be slower than slowCallDurationThreshold.
     */
    private Float slowCallRateThreshold;
    /**
     * Configures the duration threshold (seconds) above which calls are
     * considered as slow and increase the slow calls percentage. Default value
     * is 60 seconds.
     */
    private Integer slowCallDurationThreshold = 60;

    public String getCircuitBreakerRef() {
        return circuitBreakerRef;
    }

    public void setCircuitBreakerRef(String circuitBreakerRef) {
        this.circuitBreakerRef = circuitBreakerRef;
    }

    public String getConfigRef() {
        return configRef;
    }

    public void setConfigRef(String configRef) {
        this.configRef = configRef;
    }

    public Float getFailureRateThreshold() {
        return failureRateThreshold;
    }

    public void setFailureRateThreshold(Float failureRateThreshold) {
        this.failureRateThreshold = failureRateThreshold;
    }

    public Integer getPermittedNumberOfCallsInHalfOpenState() {
        return permittedNumberOfCallsInHalfOpenState;
    }

    public void setPermittedNumberOfCallsInHalfOpenState(
            Integer permittedNumberOfCallsInHalfOpenState) {
        this.permittedNumberOfCallsInHalfOpenState = permittedNumberOfCallsInHalfOpenState;
    }

    public Integer getSlidingWindowSize() {
        return slidingWindowSize;
    }

    public void setSlidingWindowSize(Integer slidingWindowSize) {
        this.slidingWindowSize = slidingWindowSize;
    }

    public String getSlidingWindowType() {
        return slidingWindowType;
    }

    public void setSlidingWindowType(String slidingWindowType) {
        this.slidingWindowType = slidingWindowType;
    }

    public Integer getMinimumNumberOfCalls() {
        return minimumNumberOfCalls;
    }

    public void setMinimumNumberOfCalls(Integer minimumNumberOfCalls) {
        this.minimumNumberOfCalls = minimumNumberOfCalls;
    }

    public Boolean getWritableStackTraceEnabled() {
        return writableStackTraceEnabled;
    }

    public void setWritableStackTraceEnabled(Boolean writableStackTraceEnabled) {
        this.writableStackTraceEnabled = writableStackTraceEnabled;
    }

    public Integer getWaitDurationInOpenState() {
        return waitDurationInOpenState;
    }

    public void setWaitDurationInOpenState(Integer waitDurationInOpenState) {
        this.waitDurationInOpenState = waitDurationInOpenState;
    }

    public Boolean getAutomaticTransitionFromOpenToHalfOpenEnabled() {
        return automaticTransitionFromOpenToHalfOpenEnabled;
    }

    public void setAutomaticTransitionFromOpenToHalfOpenEnabled(
            Boolean automaticTransitionFromOpenToHalfOpenEnabled) {
        this.automaticTransitionFromOpenToHalfOpenEnabled = automaticTransitionFromOpenToHalfOpenEnabled;
    }

    public Float getSlowCallRateThreshold() {
        return slowCallRateThreshold;
    }

    public void setSlowCallRateThreshold(Float slowCallRateThreshold) {
        this.slowCallRateThreshold = slowCallRateThreshold;
    }

    public Integer getSlowCallDurationThreshold() {
        return slowCallDurationThreshold;
    }

    public void setSlowCallDurationThreshold(Integer slowCallDurationThreshold) {
        this.slowCallDurationThreshold = slowCallDurationThreshold;
    }
}