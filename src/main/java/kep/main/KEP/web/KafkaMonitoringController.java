package kep.main.KEP.web;
import kep.main.KEP.kafka.KafkaLagProcessor;
import kep.main.KEP.model.KafkaMonitorMetrics;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/monitoring")
public class KafkaMonitoringController {
    private final KafkaLagProcessor kafkaLagProcessor;

    public KafkaMonitoringController(KafkaLagProcessor kafkaLagProcessor) {
        this.kafkaLagProcessor = kafkaLagProcessor;
    }

    @RequestMapping("/lag")
    public List<KafkaMonitorMetrics> processConsumerLag() {
        return kafkaLagProcessor.returnKafkaConsumerLag();
    }
}
