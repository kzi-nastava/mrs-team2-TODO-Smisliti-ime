package rs.getgo.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class GetGoBeApplication {

    public static void main(String[] args) {
        SpringApplication.run(GetGoBeApplication.class, args);
    }
}