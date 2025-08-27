package org.vt.service.impl;

import com.ogya.logging.avro.schema.OrderObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.vt.service.KafkaProducerService;

import java.util.UUID;

@Service
public class KafkaProducerServiceImpl implements KafkaProducerService {

    @Value("${config.kafka.topic-name-1}")
    private String topicName;

    private Logger logger = LoggerFactory.getLogger(KafkaProducerServiceImpl.class);

    private final KafkaTemplate<String, OrderObject> template;

    KafkaProducerServiceImpl(KafkaTemplate<String, OrderObject> template) {
        this.template = template;
    }

    @Override
    public void send(OrderObject orderObject) {
        var sendResult = template.send(topicName, UUID.randomUUID().toString(), orderObject);
        sendResult.addCallback(result -> logger.info("message sent successfully!"),
                failure -> logger.error("failed to send message: ", failure));
    }
}
