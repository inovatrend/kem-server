package kep.main.KEP;

import kep.main.KEP.kafka.KafkaElasticUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.annotation.PostConstruct;
import java.util.concurrent.ExecutionException;

@SpringBootApplication
public class KepApplication {

	private final KafkaElasticUtils kafkaElasticUtils;

	public KepApplication(KafkaElasticUtils kafkaElasticUtils) {
		this.kafkaElasticUtils = kafkaElasticUtils;
	}

	@PostConstruct
	public void initialize() throws ExecutionException, InterruptedException {
		kafkaElasticUtils.init();
	}

	public static void main(String[] args) {
		SpringApplication.run(KepApplication.class, args);
	}

}
