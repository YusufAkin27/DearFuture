package example.DearFuture;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(excludeName = "org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration")
@EnableScheduling
public class DearFutureApplication {

	//merhaba ben melisa
	public static void main(String[] args) {
		SpringApplication.run(DearFutureApplication.class, args);
	}

}
