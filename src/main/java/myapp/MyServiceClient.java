package myapp;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixProperty;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MyServiceClient {
    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(MyServiceClient.class);

    @Autowired
    private MyService myService;

    @HystrixCommand(
            fallbackMethod = "fallback",
            commandProperties = {
                    @HystrixProperty(name = "execution.isolation.thread.timeoutInMilliseconds", value = "500")
            })
    public MyResult callService(long id, long sleep) {
        return myService.retrieveResult(id, sleep);
    }

    private MyResult fallback(long id, long sleep) {

        MyResult myResult = new MyResult();
        myResult.setResult("Fallback: " + id);

        LOGGER.warn(myResult.getResult());
        return myResult;
    }

}
