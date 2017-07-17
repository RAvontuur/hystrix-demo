package myapp;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixProperty;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class FastServiceClient {
    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(FastServiceClient.class);

    public void callService(long id) {
        LOGGER.info("call to good performing service {}", id);
    }
}
