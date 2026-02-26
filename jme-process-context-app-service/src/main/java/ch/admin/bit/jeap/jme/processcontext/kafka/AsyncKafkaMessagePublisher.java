package ch.admin.bit.jeap.jme.processcontext.kafka;

import ch.admin.bit.jeap.messaging.avro.AvroMessage;
import ch.admin.bit.jeap.messaging.avro.AvroMessageKey;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class AsyncKafkaMessagePublisher extends KafkaMessagePublisher {

    AsyncKafkaMessagePublisher(KafkaTemplate<AvroMessageKey, AvroMessage> bitClusterTemplate,
                               KafkaTemplate<AvroMessageKey, AvroMessage> otherClusterTemplate,
                               TopicConfiguration topicConfiguration) {
        super(bitClusterTemplate, otherClusterTemplate, topicConfiguration);
    }

    @Override
    protected void send(AvroMessage message, String topic, String cluster) {
        if ("other".equalsIgnoreCase(cluster)) {
            otherClusterTemplate.send(topic, message);
        } else {
            bitClusterTemplate.send(topic, message);
        }
    }
}
