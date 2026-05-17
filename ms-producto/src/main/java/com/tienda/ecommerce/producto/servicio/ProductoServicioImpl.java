package com.tienda.ecommerce.producto.servicio;

import com.tienda.ecommerce.compartido.excepcion.EntidadNoEncontradaException;
import com.tienda.ecommerce.producto.dominio.Producto;
import com.tienda.ecommerce.producto.dto.ProductoDTO;
import com.tienda.ecommerce.producto.repositorio.ProductoRepositorio;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class ProductoServicioImpl implements ProductoServicio {

    private final ProductoRepositorio productoRepositorio;

    public ProductoServicioImpl(ProductoRepositorio productoRepositorio) {
        this.productoRepositorio = productoRepositorio;
    }

    @Override
    public List<ProductoDTO> buscarTodos() {
        return productoRepositorio.findAll()
                .stream()
                .map(ProductoDTO::desdeEntidad)
                .toList();
    }

    @Override
    public ProductoDTO buscarPorId(Long id) {
        return productoRepositorio.findById(id)
                .map(ProductoDTO::desdeEntidad)
                .orElseThrow(() -> new EntidadNoEncontradaException(
                        "Producto no encontrado con id: " + id));
    }

    @Override
    @Transactional
    public void descontarStock(Long productoId, Integer cantidad) {
        Producto producto = productoRepositorio.findById(productoId)
                .orElseThrow(() -> new EntidadNoEncontradaException(
                        "Producto no encontrado con id: " + productoId));

        int nuevoStock = producto.getStock() - cantidad;
        if (nuevoStock < 0) {
            throw new IllegalStateException(
                    "Stock insuficiente para el producto id=" + productoId +
                    " (stock actual: " + producto.getStock() + ", solicitado: " + cantidad + ")");
        }

        producto.setStock(nuevoStock);
        productoRepositorio.save(producto);
    }
}
