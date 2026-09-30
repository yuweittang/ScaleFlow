package Scaleflow;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync /**Turn on Spring's async method support */
@SpringBootApplication
public class ScaleflowApplication {

	public static void main(String[] args) {
		SpringApplication.run(ScaleflowApplication.class, args);
	}

}
