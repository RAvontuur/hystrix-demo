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

/**
 * Created by avontuur on 16-7-17.
 */
@Configuration
@EnableAspectJAutoProxy
@ComponentScan
public class Application {

    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(Application.class);


    @Bean
    public HystrixCommandAspect hystrixAspect() {
        return new HystrixCommandAspect();
    }

    private static MyServiceClient myServiceClient;

    public static void main(String[] args) throws IOException {
        ApplicationContext context =
                new AnnotationConfigApplicationContext(Application.class);
        myServiceClient = context.getBean(MyServiceClient.class);

        failingCalls(300);
        //timeoutCalls(30);

        successfulCalls(400);
    }

    private static void successfulCalls(int n) {
        for (int i = 0; i < n; i++) {
            try {
                Thread.sleep(20l);
                myServiceClient.callService(i, 1);
            } catch (Exception e) {
                LOGGER.error("EXCEPTION {} {}", i, e.getMessage());
            }
        }
    }

    private static void timeoutCalls(int n) {
        // these calls should cause a timeout
        for (int i = 0; i < n; i++) {
            try {
                myServiceClient.callService(i, 2000);
            } catch (Exception e) {
                LOGGER.error("EXCEPTION {} {}", i, e.getMessage());
            }
        }
    }

    private static void failingCalls(int n) {
        // failing calls
        for (int i = 1000000; i < 1000000 + n; i++) {
            try {
                Thread.sleep(20l);
                myServiceClient.callService(i, 100);
            } catch (Exception e) {
                LOGGER.error("EXCEPTION {} {}", i, e.getMessage());
            }
        }
    }
}
