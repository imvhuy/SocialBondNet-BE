package org.socialbondnet.postservice.publisher;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RabbitMQProducer {
    @Value("${rabbitmq.routing.json.key}")
    private String jsonRoutingKey;
    @Value("${rabbitmq.json.name}")
    private String jsonQueue;
    @Value("${rabbitmq.exchange.name}")
    private String exchange;
    private final RabbitTemplate rabbitTemplate;

    private static final Logger LOGGER = LoggerFactory.getLogger(RabbitMQProducer.class);

    public void sendMessage(Object message) {
        LOGGER.info("Producing message to RabbitMQ: {}", message.toString());
        rabbitTemplate.convertAndSend(exchange, jsonRoutingKey, message);
    }
}
