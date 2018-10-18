package exam.tripapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;

@SpringBootApplication(exclude = {SecurityAutoConfiguration.class })
public class TurappApplication {

	public static void main(String[] args) {
		SpringApplication.run(TurappApplication.class, args);
	}
}
