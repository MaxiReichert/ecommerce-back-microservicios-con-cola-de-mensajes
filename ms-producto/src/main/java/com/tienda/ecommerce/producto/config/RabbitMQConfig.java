package com.tienda.ecommerce.producto.config;

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

    @Value("${rabbitmq.exchange.pedidos}")
    private String exchangeName;

    @Value("${rabbitmq.queue.stock-descontar}")
    private String queueStockDescontar;

    @Value("${rabbitmq.routing-key.stock-descontar}")
    private String rkStockDescontar;

    // ─── Exchange ─────────────────────────────────────────────────────────────

    @Bean
    public TopicExchange pedidosExchange() {
        return new TopicExchange(exchangeName, true, false);
    }

    // ─── Queue ────────────────────────────────────────────────────────────────

    /**
     * Cola de la cual ms-producto consume eventos de "descontar stock".
     * Publicada por ms-pedido al confirmar un pedido.
     */
    @Bean
    public Queue queueStockDescontar() {
        return QueueBuilder.durable(queueStockDescontar).build();
    }

    // ─── Binding ──────────────────────────────────────────────────────────────

    @Bean
    public Binding bindingStockDescontar(Queue queueStockDescontar, TopicExchange pedidosExchange) {
        return BindingBuilder.bind(queueStockDescontar).to(pedidosExchange).with(rkStockDescontar);
    }

    // ─── Serialización JSON ───────────────────────────────────────────────────

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }
}
