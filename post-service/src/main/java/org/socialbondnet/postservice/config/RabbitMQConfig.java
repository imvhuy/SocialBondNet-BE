package org.socialbondnet.postservice.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    @Value("${rabbitmq.queue.name}")
    private String queue;
    @Value("${rabbitmq.json.name}")
    private String jsonQueue;
    @Value("${rabbitmq.exchange.name}")
    private String exchange;
    @Value("${rabbitmq.routing.key}")
    private String routingKey;
    @Value("${rabbitmq.routing.json.key}")
    private String jsonRoutingKey;
    @Bean
    public Queue queue() {
        return new Queue(queue);
    }
    @Bean
    public Queue jsonQueue() {
        return new Queue(jsonQueue);
    }
    @Bean
    public Exchange exchange() {
        return new TopicExchange(exchange);
    }
    @Bean
    public Binding binding(Queue queue, Exchange exchange) {
        return BindingBuilder
                .bind(queue)
                .to(exchange)
                .with(routingKey)
                .noargs();
    }
    @Bean
    public Binding jsonBinding(Queue jsonQueue, Exchange exchange) {
        return BindingBuilder
                .bind(jsonQueue)
                .to(exchange)
                .with(jsonRoutingKey)
                .noargs();
    }
    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }
    @Bean
    public AmqpTemplate amqpTemplate(ConnectionFactory connectionFactory) {
       RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
       rabbitTemplate.setMessageConverter(messageConverter());
       return rabbitTemplate;
    }
}
