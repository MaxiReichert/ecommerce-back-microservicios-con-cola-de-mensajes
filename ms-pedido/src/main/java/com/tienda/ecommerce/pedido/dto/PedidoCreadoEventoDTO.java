package com.tienda.ecommerce.pedido.dto;

import java.util.List;

/**
 * Evento publicado a RabbitMQ cuando se crea un pedido.
 * ms-pago es el consumidor: valida descuento, procesa pago y notifica a ms-producto.
 */
public class PedidoCreadoEventoDTO {

    private String codigoPedido;
    private String metodoPago;
    private String codigoDescuento;
    private Double subtotal;
    private Double envio;
    private List<ItemEventoDTO> items;

    // ─── Constructor vacío requerido por Jackson ──────────────────────────────
    public PedidoCreadoEventoDTO() {}

    public PedidoCreadoEventoDTO(String codigoPedido, String metodoPago, String codigoDescuento,
                                  Double subtotal, Double envio, List<ItemEventoDTO> items) {
        this.codigoPedido = codigoPedido;
        this.metodoPago = metodoPago;
        this.codigoDescuento = codigoDescuento;
        this.subtotal = subtotal;
        this.envio = envio;
        this.items = items;
    }

    public String getCodigoPedido() { return codigoPedido; }
    public void setCodigoPedido(String codigoPedido) { this.codigoPedido = codigoPedido; }

    public String getMetodoPago() { return metodoPago; }
    public void setMetodoPago(String metodoPago) { this.metodoPago = metodoPago; }

    public String getCodigoDescuento() { return codigoDescuento; }
    public void setCodigoDescuento(String codigoDescuento) { this.codigoDescuento = codigoDescuento; }

    public Double getSubtotal() { return subtotal; }
    public void setSubtotal(Double subtotal) { this.subtotal = subtotal; }

    public Double getEnvio() { return envio; }
    public void setEnvio(Double envio) { this.envio = envio; }

    public List<ItemEventoDTO> getItems() { return items; }
    public void setItems(List<ItemEventoDTO> items) { this.items = items; }

    // ─── Inner DTO ────────────────────────────────────────────────────────────

    public static class ItemEventoDTO {
        private Long productoId;
        private Integer cantidad;
        private Double precioUnitario;

        public ItemEventoDTO() {}

        public ItemEventoDTO(Long productoId, Integer cantidad, Double precioUnitario) {
            this.productoId = productoId;
            this.cantidad = cantidad;
            this.precioUnitario = precioUnitario;
        }

        public Long getProductoId() { return productoId; }
        public void setProductoId(Long productoId) { this.productoId = productoId; }

        public Integer getCantidad() { return cantidad; }
        public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }

        public Double getPrecioUnitario() { return precioUnitario; }
        public void setPrecioUnitario(Double precioUnitario) { this.precioUnitario = precioUnitario; }
    }
}
