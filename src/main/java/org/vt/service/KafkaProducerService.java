package org.vt.service;

import com.ogya.logging.avro.schema.OrderObject;

public interface KafkaProducerService {
    public void send(OrderObject orderObject);
}
