package com.tienda.ecommerce.producto.servicio;

import com.tienda.ecommerce.producto.dto.ProductoDTO;

import java.util.List;

public interface ProductoServicio {

    /**
     * Retorna todos los productos disponibles.
     * Los filtros por categoría o nombre se realizan en el frontend.
     */
    List<ProductoDTO> buscarTodos();

    /**
     * Busca un producto por ID. Útil para validar precios desde otros módulos.
     */
    ProductoDTO buscarPorId(Long id);

    /**
     * Descuenta {@code cantidad} unidades del stock del producto.
     * Lanzado desde el consumidor RabbitMQ tras confirmar un pedido.
     *
     * @throws com.tienda.ecommerce.compartido.excepcion.EntidadNoEncontradaException si el producto no existe
     * @throws IllegalStateException si el stock resultante sería negativo
     */
    void descontarStock(Long productoId, Integer cantidad);
}
