package myapp;

import com.netflix.hystrix.contrib.javanica.aop.aspectj.HystrixCommandAspect;
import org.slf4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.Integer.max;
import static java.lang.Integer.min;


/**
 * Created by avontuur on 16-7-17.
 */
@Configuration
@EnableAspectJAutoProxy
@ComponentScan
public class ApplicationMultiThreaded {

    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(ApplicationMultiThreaded.class);


    @Bean
    public HystrixCommandAspect hystrixAspect() {
        return new HystrixCommandAspect();
    }

    private static MyServiceClient myServiceClient;
    private static FastServiceClient fastServiceClient;

    public static void main(String[] args) throws IOException, InterruptedException {
        ApplicationContext context =
                new AnnotationConfigApplicationContext(ApplicationMultiThreaded.class);
        myServiceClient = context.getBean(MyServiceClient.class);
        fastServiceClient = context.getBean(FastServiceClient.class);

        final AtomicInteger counter1 = new AtomicInteger(1);
        final AtomicInteger counter2 = new AtomicInteger(1);
        final AtomicInteger counter3 = new AtomicInteger(1);

        ExecutorService executor = Executors.newFixedThreadPool(100);

        Runnable runnableSlow = () -> {
            // slow call
            makeCall(counter1.getAndIncrement(), 400);
        };

        Runnable runnableFast = () -> {
            // fast call
            fastServiceClient.callService(counter2.getAndIncrement());
        };

        final long startTime = System.currentTimeMillis();

        while (true) {
            int sleeptime = func(20, 30, 1, 100, startTime);
            if (counter3.getAndIncrement() % 10 == 1) {
                float rate = 1000F / sleeptime;
                LOGGER.info("counter :" + counter3.get() + " request-rate :" + rate + " /s");
            }

            Thread.sleep(sleeptime);
            executor.execute(runnableFast);
            executor.execute(runnableSlow);
        }

    }

    private static int func(int a, int b, int c, int d, long startTime) {
        long curTime = System.currentTimeMillis() - startTime;
        float step = (d - c) / (1000F * a);
        if (curTime < a * 1000) {
            return max(c, min(d, (int) (d - (curTime * step))));
        }

        if (curTime > b * 1000) {
            return max(c, min(d, (int) (c + (curTime - (b * 1000) * step))));
        }

        return c;
    }

    private static void makeCall(int i, int sleep) {
        try {
            myServiceClient.callService(i, sleep);
        } catch (Exception e) {
            LOGGER.error("EXCEPTION {} {}", i, e.getMessage());
        }
    }
}
