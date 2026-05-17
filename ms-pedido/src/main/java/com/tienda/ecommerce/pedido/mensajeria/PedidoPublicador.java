package com.tienda.ecommerce.pedido.mensajeria;

import com.tienda.ecommerce.pedido.dto.PedidoCreadoEventoDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Publicador de eventos de pedido hacia RabbitMQ.
 * ms-pago es el consumidor del evento PedidoCreado.
 */
@Component
public class PedidoPublicador {

    private static final Logger log = LoggerFactory.getLogger(PedidoPublicador.class);

    private final RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.exchange.pedidos}")
    private String exchange;

    @Value("${rabbitmq.routing-key.pedido-creado}")
    private String routingKey;

    public PedidoPublicador(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publicarPedidoCreado(PedidoCreadoEventoDTO evento) {
        log.info("[PedidoPublicador] Publicando evento PedidoCreado — codigo={}", evento.getCodigoPedido());
        rabbitTemplate.convertAndSend(exchange, routingKey, evento);
        log.info("[PedidoPublicador] Evento publicado exitosamente");
    }
}
