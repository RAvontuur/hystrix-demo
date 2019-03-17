package myapp;

import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandProperties;
import com.netflix.hystrix.HystrixThreadPoolProperties;
import org.slf4j.Logger;

/**
 * Created by avontuur on 16-7-17.
 */
public class ApplicationHystrixCommand {

    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(ApplicationHystrixCommand.class);

    private static MyService myService = new MyService();


    public static void main(String[] args) throws InterruptedException {
        //timeoutCalls(50);
        failingCalls(50);

        successfulCalls(500);
    }

    private static MyResult callService(final int id, final int sleep) {
        HystrixCommandProperties.Setter hystrixProps = HystrixCommandProperties.defaultSetter()
                .withCircuitBreakerRequestVolumeThreshold(20)  //default 20 (will not trip in case of low failure rate)
                .withCircuitBreakerSleepWindowInMilliseconds(10000) //default 5000
                .withMetricsRollingStatisticalWindowInMilliseconds(50000) //default 10000
                .withExecutionIsolationThreadInterruptOnTimeout(false);
        HystrixThreadPoolProperties.Setter threadPoolSettings = HystrixThreadPoolProperties.defaultSetter();

        HystrixCommand<MyResult> cmd = new HystrixCommand<MyResult>(
                HystrixCommand.Setter.withGroupKey(() -> "my-service")
                        .andCommandPropertiesDefaults(hystrixProps)
                        .andThreadPoolPropertiesDefaults(threadPoolSettings)) {
            @Override
            protected MyResult run() {
                return myService.retrieveResult(id, sleep);
            }

            @Override
            protected MyResult getFallback() {
                return new MyResult("fallback");
            }
        };

        MyResult result = cmd.execute();
        if (cmd.isResponseShortCircuited()) {
            LOGGER.warn("response short circuited");
        }
        if (cmd.isResponseTimedOut()) {
            LOGGER.warn("response timed out");
        }
        if (cmd.isFailedExecution()) {
            LOGGER.error("failed execution: {}", cmd.getExecutionException().getMessage());
        }
        return result;
    }


    private static void successfulCalls(int n) throws InterruptedException {
        int sleep = 500;
        for (int i = 0; i < n; i++) {
            Thread.sleep(sleep);
            callService(i, 1);
        }
    }

    private static void timeoutCalls(int n) {
        // these calls should cause a timeout
        int sleep = 5000;
        for (int i = 0; i < n; i++) {
            callService(i, sleep);
        }
    }

    private static void failingCalls(int n) throws InterruptedException {
        // failing calls
        int sleep = 900;
        for (int i = 1000000; i < 1000000 + n; i++) {
            Thread.sleep(sleep);
            callService(i, 100);
        }
    }
}
