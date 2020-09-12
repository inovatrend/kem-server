package kep.main.KEP;

import kep.main.KEP.kafka.KafkaUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.annotation.PostConstruct;
import java.util.concurrent.ExecutionException;

@SpringBootApplication
public class KepApplication {

	private final KafkaUtils kafkaUtils;

	public KepApplication(KafkaUtils kafkaUtils) {
		this.kafkaUtils = kafkaUtils;
	}

	@PostConstruct
	public void initialize() throws ExecutionException, InterruptedException {
		kafkaUtils.init();
	}

	public static void main(String[] args) {
		SpringApplication.run(KepApplication.class, args);
	}

}
