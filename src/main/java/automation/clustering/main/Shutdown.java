package automation.clustering.main;

import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class Shutdown {

    @Autowired
    private ApplicationContext context;

    public void stopApplication(int exitCode) {
        int code = SpringApplication.exit(context, () -> exitCode);
        System.exit(code);
    }
}
