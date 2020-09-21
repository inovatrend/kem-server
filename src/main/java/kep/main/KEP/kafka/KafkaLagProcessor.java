package kep.main.KEP.kafka;

import kep.main.KEP.model.KafkaMonitorMetrics;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class KafkaLagProcessor {

    List<KafkaMonitorMetrics> kafkaMonitorMetricsList = new ArrayList<>();

    public void saveKafkaTopicLag(KafkaMonitorMetrics kafkaMonitorMetrics) {
        kafkaMonitorMetricsList.add(kafkaMonitorMetrics);
    }

    public List<KafkaMonitorMetrics> returnKafkaTopicLag() {
            return kafkaMonitorMetricsList;
    }

}
