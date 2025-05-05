package indiv.neitdev.nollie_furniture;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class NollieFurnitureApplication {

	public static void main(String[] args) {
		SpringApplication.run(NollieFurnitureApplication.class, args);
	}

}
