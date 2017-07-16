package myapp;

import org.slf4j.Logger;
import org.springframework.stereotype.Service;

@Service
public class MyService {
    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(MyService.class);


    public MyResult retrieveResult(long id, long sleep) {

        try {
            Thread.sleep(sleep);
        } catch (InterruptedException e) {
            throw new IllegalArgumentException(Thread.currentThread().getName() + ": " + e.getClass().getName() + " " + e.getMessage());
        }

        if (id >= 1000000) {
            throw new IllegalArgumentException(Thread.currentThread().getName() + ": id " + id + " must be smaller than 1000000");
        }

        MyResult myResult = new MyResult();
        myResult.setResult("my-service-" + id);

        LOGGER.info(myResult.getResult());

        return myResult;
    }
}
