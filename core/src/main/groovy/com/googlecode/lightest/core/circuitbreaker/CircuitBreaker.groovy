package com.googlecode.lightest.core.circuitbreaker

public class CircuitBreaker {

    private static CircuitBreaker instance = null

    private static int timedOut = 0
    private static final int MAX_TOTAL_TIME_OUTS = 6
    private static final int MAX_ENV_TIME_OUTS = 3
    private static int cb_state = 1

    private static Map<String, Integer> envMap = new HashMap<String, Integer>()

    private CircuitBreaker() {}

    public synchronized void addTimeout(String env) {
        envMap.put(env, envMap.get(env) + 1)

        if (envMap.get(env) == MAX_ENV_TIME_OUTS) {
            timedOut++
        }
        if (timedOut > 1 || envMap.get(env) == MAX_TOTAL_TIME_OUTS) {
            cb_state = 0
        } else {
            cb_state = 1
        }
    }

    public synchronized boolean isClosed() {
        return cb_state
    }

    public int getThreshold() {
        return MAX_ENV_TIME_OUTS;
    }

    public int getCeiling() {
        return MAX_TOTAL_TIME_OUTS;
    }

    public void setEnvMap(String env) {
        envMap.put(env, 0)
    }

    public synchronized maybeResetTimeout(String env) {
        if (envMap.get(env) >= MAX_ENV_TIME_OUTS) {
            timedOut--
        }
        envMap.put(env, 0)
    }

    public String getCBData() {
        StringBuilder s = new StringBuilder("\nCB tripped caused by: \n")
        for (String env : envMap.keySet()) {
            s.append("Environment: ")
            s.append(env)
            s.append(" had ")
            s.append(envMap.get(env))
            s.append(" ConditionWatch timeouts.\n")
        }
        return s.toString()
    }

    public static CircuitBreaker getInstance() {
        if (instance == null) {
            instance = new CircuitBreaker()
        }
        return instance
    }
}

