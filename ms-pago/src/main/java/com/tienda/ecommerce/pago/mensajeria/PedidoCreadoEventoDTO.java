package com.tienda.ecommerce.pago.mensajeria;

import java.util.List;

/**
 * Espejo del evento publicado por ms-pedido.
 * Jackson deserializa el JSON de la cola en esta clase.
 */
public class PedidoCreadoEventoDTO {

    private String codigoPedido;
    private String metodoPago;
    private String codigoDescuento;
    private Double subtotal;
    private Double envio;
    private List<ItemEventoDTO> items;

    public PedidoCreadoEventoDTO() {}

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

    public static class ItemEventoDTO {
        private Long productoId;
        private Integer cantidad;
        private Double precioUnitario;

        public ItemEventoDTO() {}

        public Long getProductoId() { return productoId; }
        public void setProductoId(Long productoId) { this.productoId = productoId; }

        public Integer getCantidad() { return cantidad; }
        public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }

        public Double getPrecioUnitario() { return precioUnitario; }
        public void setPrecioUnitario(Double precioUnitario) { this.precioUnitario = precioUnitario; }
    }
}
