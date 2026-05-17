package com.tienda.ecommerce.producto.mensajeria;

import com.tienda.ecommerce.producto.dto.StockDescontarEventoDTO;
import com.tienda.ecommerce.producto.servicio.ProductoServicio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Consumidor de la cola {@code stock.descontar.queue}.
 * <p>
 * Cuando ms-pedido confirma un pedido, publica un evento con los ítems.
 * Este consumidor descuenta el stock de cada producto involucrado.
 */
@Component
public class StockConsumidor {

    private static final Logger log = LoggerFactory.getLogger(StockConsumidor.class);

    private final ProductoServicio productoServicio;

    public StockConsumidor(ProductoServicio productoServicio) {
        this.productoServicio = productoServicio;
    }

    @RabbitListener(queues = "${rabbitmq.queue.stock-descontar}")
    public void descontarStock(StockDescontarEventoDTO evento) {
        log.info("[StockConsumidor] Evento recibido para pedido: {}", evento.getCodigoPedido());

        for (StockDescontarEventoDTO.ItemStockDTO item : evento.getItems()) {
            try {
                productoServicio.descontarStock(item.getProductoId(), item.getCantidad());
                log.info("[StockConsumidor] Stock descontado — productoId={}, cantidad={}",
                        item.getProductoId(), item.getCantidad());
            } catch (Exception e) {
                log.error("[StockConsumidor] Error al descontar stock — productoId={}: {}",
                        item.getProductoId(), e.getMessage());
            }
        }
    }
}
