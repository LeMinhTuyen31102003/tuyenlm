package example.ecommerce.tuyenlm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class TuyenlmApplication {

	public static void main(String[] args) {
		SpringApplication.run(TuyenlmApplication.class, args);
	}

}
