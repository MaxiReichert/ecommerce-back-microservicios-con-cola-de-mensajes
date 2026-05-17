package com.tienda.ecommerce.pedido.config;

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

    // ─── Valores inyectados desde application.properties ─────────────────────

    @Value("${rabbitmq.exchange.pedidos}")
    private String exchangeName;

    @Value("${rabbitmq.queue.pedido-creado}")
    private String queuePedidoCreado;

    @Value("${rabbitmq.queue.pago-resultado}")
    private String queuePagoResultado;

    @Value("${rabbitmq.queue.stock-descontar}")
    private String queueStockDescontar;

    @Value("${rabbitmq.routing-key.pedido-creado}")
    private String rkPedidoCreado;

    @Value("${rabbitmq.routing-key.pago-resultado}")
    private String rkPagoResultado;

    @Value("${rabbitmq.routing-key.stock-descontar}")
    private String rkStockDescontar;

    // ─── Exchange ─────────────────────────────────────────────────────────────

    /**
     * Exchange de tipo Topic para el dominio de pedidos.
     * durable=true → sobrevive reinicios del broker.
     */
    @Bean
    public TopicExchange pedidosExchange() {
        return new TopicExchange(exchangeName, true, false);
    }

    // ─── Queues ───────────────────────────────────────────────────────────────

    /**
     * Cola donde ms-pedido publica eventos "pedido creado".
     * ms-pago es el consumidor de esta cola.
     */
    @Bean
    public Queue queuePedidoCreado() {
        return QueueBuilder.durable(queuePedidoCreado).build();
    }

    /**
     * Cola donde ms-pago publica el resultado del pago.
     * ms-pedido es el consumidor de esta cola (para actualizar estadoPago).
     */
    @Bean
    public Queue queuePagoResultado() {
        return QueueBuilder.durable(queuePagoResultado).build();
    }

    /**
     * Cola donde ms-pedido publica el evento para descontar stock.
     * ms-producto es el consumidor de esta cola.
     */
    @Bean
    public Queue queueStockDescontar() {
        return QueueBuilder.durable(queueStockDescontar).build();
    }

    // ─── Bindings ─────────────────────────────────────────────────────────────

    @Bean
    public Binding bindingPedidoCreado(Queue queuePedidoCreado, TopicExchange pedidosExchange) {
        return BindingBuilder.bind(queuePedidoCreado).to(pedidosExchange).with(rkPedidoCreado);
    }

    @Bean
    public Binding bindingPagoResultado(Queue queuePagoResultado, TopicExchange pedidosExchange) {
        return BindingBuilder.bind(queuePagoResultado).to(pedidosExchange).with(rkPagoResultado);
    }

    @Bean
    public Binding bindingStockDescontar(Queue queueStockDescontar, TopicExchange pedidosExchange) {
        return BindingBuilder.bind(queueStockDescontar).to(pedidosExchange).with(rkStockDescontar);
    }

    // ─── Serialización JSON ───────────────────────────────────────────────────

    /**
     * Convierte los mensajes a/desde JSON usando Jackson.
     * Reemplaza la serialización binaria por defecto de Spring AMQP.
     */
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /**
     * Configura el RabbitTemplate con el converter JSON y confirma publicaciones.
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }
}
