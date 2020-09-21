package kep.main.KEP.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@AllArgsConstructor
@Document(indexName = "kafka_monitor")
public class KafkaMonitorMetrics {

    @Id
    @Field(type = FieldType.Keyword)
    @JsonProperty("id")
    public Long id;

    @JsonProperty("lag")
    public Long lag;

    @JsonProperty("topicName")
    public String topicName;
}
