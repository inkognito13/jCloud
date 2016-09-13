package su.orange.jcloud;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.io.IOException;

/**
 * @author Dmitry Tarasov
 *         Date: 02/24/2016
 *         Time: 14:01
 */
@SpringBootApplication
@EnableScheduling
@EnableAsync
public class App {
    
    public static final String DEBUG_RPOFILE = "debug";
    public static final String STAGING_PROFILE = "staging";
    
    public static void main(String[] args) throws IOException {
        SpringApplication.run(App.class, args);
    }
}
