package com.tienda.ecommerce.pago.mensajeria;

import com.tienda.ecommerce.pago.dto.PagoResultado;
import com.tienda.ecommerce.pago.servicio.DescuentoServicio;
import com.tienda.ecommerce.pago.servicio.PagoServicio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Consumidor de la cola {@code pedido.creado.queue}.
 * <p>
 * Cuando ms-pedido publica un evento de pedido creado:
 * <ol>
 *   <li>Valida el código de descuento (si hay uno)</li>
 *   <li>Calcula el total final</li>
 *   <li>Procesa el pago según el método</li>
 *   <li>Si el pago es aprobado → publica {@code StockDescontarEventoDTO}
 *       en la cola {@code stock.descontar.queue} para que ms-producto
 *       descuente el stock</li>
 * </ol>
 */
@Component
public class PedidoCreadoConsumidor {

    private static final Logger log = LoggerFactory.getLogger(PedidoCreadoConsumidor.class);

    private final PagoServicio pagoServicio;
    private final DescuentoServicio descuentoServicio;
    private final RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.exchange.pedidos}")
    private String exchange;

    @Value("${rabbitmq.routing-key.stock-descontar}")
    private String rkStockDescontar;

    public PedidoCreadoConsumidor(PagoServicio pagoServicio,
                                   DescuentoServicio descuentoServicio,
                                   RabbitTemplate rabbitTemplate) {
        this.pagoServicio = pagoServicio;
        this.descuentoServicio = descuentoServicio;
        this.rabbitTemplate = rabbitTemplate;
    }

    @RabbitListener(queues = "${rabbitmq.queue.pedido-creado}")
    public void procesarPedido(PedidoCreadoEventoDTO evento) {
        log.info("[PedidoCreadoConsumidor] Evento recibido — pedido={}, metodo={}",
                evento.getCodigoPedido(), evento.getMetodoPago());

        // 1. Validar descuento
        double porcentajeDescuento = descuentoServicio.obtenerPorcentajeDescuento(evento.getCodigoDescuento());
        double descuento = evento.getSubtotal() * porcentajeDescuento;
        double total = evento.getSubtotal() - descuento + evento.getEnvio();

        log.info("[PedidoCreadoConsumidor] descuento={}%, total calculado={}", porcentajeDescuento * 100, total);

        // 2. Procesar pago
        PagoResultado resultado = pagoServicio.procesarPago(evento.getMetodoPago(), total);
        log.info("[PedidoCreadoConsumidor] Resultado pago — estado={}, txId={}",
                resultado.getEstadoPago(), resultado.getIdTransaccion());

        // 3. Si el pago fue aprobado → publicar evento de descuento de stock
        if ("PAGADO".equals(resultado.getEstadoPago())) {
            List<StockDescontarEventoDTO.ItemStockDTO> itemsStock = evento.getItems().stream()
                    .map(i -> new StockDescontarEventoDTO.ItemStockDTO(i.getProductoId(), i.getCantidad()))
                    .collect(Collectors.toList());

            StockDescontarEventoDTO eventoStock = new StockDescontarEventoDTO(evento.getCodigoPedido(), itemsStock);
            rabbitTemplate.convertAndSend(exchange, rkStockDescontar, eventoStock);

            log.info("[PedidoCreadoConsumidor] Pago APROBADO — evento StockDescontar publicado para pedido={}",
                    evento.getCodigoPedido());
        } else {
            log.warn("[PedidoCreadoConsumidor] Pago PENDIENTE/RECHAZADO para pedido={} — stock no descontado",
                    evento.getCodigoPedido());
        }
    }
}
