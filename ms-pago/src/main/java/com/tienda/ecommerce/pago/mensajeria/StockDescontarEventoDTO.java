package com.tienda.ecommerce.pago.mensajeria;

import java.util.List;

/**
 * Evento que ms-pago publica hacia ms-producto
 * para que descuente el stock de los ítems del pedido aprobado.
 */
public class StockDescontarEventoDTO {

    private String codigoPedido;
    private List<ItemStockDTO> items;

    public StockDescontarEventoDTO() {}

    public StockDescontarEventoDTO(String codigoPedido, List<ItemStockDTO> items) {
        this.codigoPedido = codigoPedido;
        this.items = items;
    }

    public String getCodigoPedido() { return codigoPedido; }
    public void setCodigoPedido(String codigoPedido) { this.codigoPedido = codigoPedido; }

    public List<ItemStockDTO> getItems() { return items; }
    public void setItems(List<ItemStockDTO> items) { this.items = items; }

    public static class ItemStockDTO {
        private Long productoId;
        private Integer cantidad;

        public ItemStockDTO() {}

        public ItemStockDTO(Long productoId, Integer cantidad) {
            this.productoId = productoId;
            this.cantidad = cantidad;
        }

        public Long getProductoId() { return productoId; }
        public void setProductoId(Long productoId) { this.productoId = productoId; }

        public Integer getCantidad() { return cantidad; }
        public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }
    }
}
